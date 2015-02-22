/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bomehc.clavier;

import bomehc.utils.dialogue.math.DialogueMath;
import bomehc.utils.dialogue.math.DialogueMathExposant;
import bomehc.utils.texte.EditeurIO;
import bomehc.utils.texte.JMathTextPane;
import bomehc.utils.texte.MathTools;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Guillaume
 */
public abstract class ParticularKeyListener implements KeyListener {

    /**
     * Permet de mettre au carré une chaine sélectionnée dans un JMathTextPane
     */
    public static class CarreKeyListener extends ParticularKeyListener {

        private boolean carreCreated;

        @Override
        public void keyTyped(KeyEvent e) {
            if (carreCreated) {
                carreCreated = false;
                e.consume();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == '²') {
                if (e.getSource() instanceof JMathTextPane) {
                    JMathTextPane text = (JMathTextPane) e.getSource();
                    if (text.getSelectedText() != null) {
                        DialogueMath dialogue = new DialogueMathExposant(text, EditeurIO.export2htmlMathML(text, text.getSelectionStart(), text.getSelectionEnd()-text.getSelectionStart()), "<mn>2</mn>");
                        dialogue.addDialogueMathListener(dialogue.new CreateListener(text));
                        dialogue.addDialogueMathListener(new DialogueMath.DialogueMathListener() {
                            @Override
                            public void handleMathString(String answer) {
                                carreCreated = true;
                            }
                        });
                        e.consume();
                    }
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    /**
     * Remplace les caractères "a", "b", "x", "y" par des caractères en italique
     */
    public static class ABXYKeyListener extends ParticularKeyListener {

        private boolean xPressed;
        private CancelMiseEnForme cancelMiseEnForme = new CancelMiseEnForme();

        @Override
        public void keyTyped(KeyEvent e) {
            if (xPressed) {
                xPressed = false;
                e.consume();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            cancelMiseEnForme.remplacerSiNecessaire(e);
            if (e.getModifiers() != 0) {
                return;
            }
            String symbole = null;
            switch (e.getKeyCode()) {
                case KeyEvent.VK_X:
                    symbole = "<mi>x</mi>";
                    break;
                case KeyEvent.VK_Y:
                    symbole = "<mi>y</mi>";
                    break;
                case KeyEvent.VK_A:
                    symbole = "<mi>a</mi>";
                    break;
                case KeyEvent.VK_B:
                    symbole = "<mi>b</mi>";
                    break;
            }
            if (symbole != null) {
                if (e.getSource() instanceof JMathTextPane) {
                    e.consume();
                    JMathTextPane textPane = (JMathTextPane) e.getSource();
                    textPane.insererJMathComponent(MathTools.creerMathComponent(symbole));
                    xPressed = true;
                    cancelMiseEnForme.notifyRemplacementPossible(e);
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    /**
     * Remplace les inconnues des équations par des caractères penchées
     */
    public static class CaracteresKeyListener extends ParticularKeyListener {

        private String lastCaracteres = "";
        private int index = -1;
        private static final List<Character> caracteresSuivants = Arrays.asList(' ', '+', '-', '.', ',', '=', ')', '(', '\n', '\t', '*');
        private static final List<Character> caracteresPrecedents = Arrays.asList(' ', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '(', ')', '[', ']', '{', '}', '+', '-', '=', '\n', '\t', '.', ',', '*');
        private static final List<Character> caracteresIsolesAutorisees = Arrays.asList('a'); // Expression : "On a :"
        private static final List<Character> caracteresMathematiques = Arrays.asList('a','b','x','y','t');

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(!(e.getSource() instanceof JMathTextPane)) {return;}
            JMathTextPane text = (JMathTextPane) e.getSource();
            boolean modification = false;
            if (e.getModifiersEx() == 0) {
                if (caracteresMathematiques.contains(e.getKeyChar())) {
                    modification = true;
                    lastCaracteres += e.getKeyChar();
                    index = text.getCaretPosition() + 1;
                }
            }
            if (!lastCaracteres.equals("")) {
                if (caracteresSuivants.contains(e.getKeyChar())) {
                    if (isAllowedToBecomeMathematicSymbol(text)) {
                        int position = text.getCaret().getDot();
                        if (position != index) { return; }
                        try {
                            text.getDocument().remove(position - lastCaracteres.length(), lastCaracteres.length());
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }
                        for (char caracter : lastCaracteres.toCharArray()) {
                            if(caracteresMathematiques.contains(caracter)) {
                                text.insererJMathComponent(MathTools.creerMathComponent("<mi>"+caracter+"</mi>"));
                            }
                        }
                    }
                    // On a terminé la transformation, on réinitialise
                    lastCaracteres = "";
                    index = -1;
                } else if (modification == false && e.getModifiersEx() == 0) { // On réinitialise une nouvelle combinaison
                    lastCaracteres = "";
                    index = -1;
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        /**
         * Permet de savoir si l'état actuel du JMathTextPane passé en paramètre
         * permet d'accepter la transformation ou non. La transformation est
         * acceptée si les caractères situés avant la position du caret
         * correspondent à ceux stockés dans la variable temporaire de
         * modification (lastCaracteres), si le caractère précédent cette
         * séquence est un caracteresPrecedents autorisé, et enfin si la chaine
         * n'est pas un caractère isolé.
         *
         * @param text le <code>JMathTextPane</code> à vérifier
         * @return true si le composant accepte la transformation; false sinon
         */
        private boolean isAllowedToBecomeMathematicSymbol(JMathTextPane text) {
            int position = text.getCaret().getDot();
            if (position >= lastCaracteres.length()) {
                try {
                    String chaine = text.getDocument().getText(position - lastCaracteres.length(), lastCaracteres.length());
                    if (chaine.equals(lastCaracteres)) {
                        if (position - lastCaracteres.length() > 0) {
                            char caracterePrecedent = text.getDocument().getText(position - lastCaracteres.length() - 1, 1).charAt(0);
                            if (Arrays.asList(caracteresPrecedents).contains(caracterePrecedent)) {
                                // Si le caractère à transformer est un caractère isolé, on ne le transforme pas (ex : "On a : ")
                                if (caracterePrecedent == ' ' && chaine.length() == 1 && caracteresIsolesAutorisees.contains(chaine.charAt(0)) && !text.isMathComponentPosition(position - lastCaracteres.length() - 1)) {
                                    return false;
                                }
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

    }

    /**
     * Permet d'ajouter une fonctionnalité d'annulation de mise en forme par la
     * touche BACK_SPACE.
     */
    private static class CancelMiseEnForme {

        private String caractereRemplacement;
        private boolean supprActived;
        private int index = -1;

        /**
         * Remplace un caractere qui vient d'être mis en forme, par l'appuie de
         * la touche BACK_SPACE et réinitialise les attributs de toute façon.
         *
         * @param e
         */
        private void remplacerSiNecessaire(KeyEvent e) {
            if (!supprActived) {
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (e.getSource() instanceof JMathTextPane) {
                    JMathTextPane text = (JMathTextPane) e.getSource();
                    if (text.getSelectedText() == null && index == text.getCaretPosition()) {
                        text.setSelectionStart(index-1);
                        text.setSelectionEnd(index);
                        text.replaceSelection(caractereRemplacement);
                    }
                }
            }
            supprActived = false;
            caractereRemplacement = "";
            index = -1;
        }

        /**
         * Permet de notifier l'objet qu'un ajout a été fait et donc d'autoriser
         * la suppression de la mise en forme.
         *
         * @param e le KeyEvent
         */
        private void notifyRemplacementPossible(KeyEvent e) {
            if (e.getSource() instanceof JTextComponent) {
                this.caractereRemplacement = Character.toString(e.getKeyChar());
                index = ((JTextComponent) e.getSource()).getCaretPosition();
                supprActived = true;
            }
        }
    }
    
//public interface SymbolesConstants {

//	String X = "<mi>x</mi>";
//
//	String Y = "<mi>y</mi>";
//
//	String A = "<mi>a</mi>";
//
//	String B = "<mi>b</mi>";
//
//	String T = "<mi>t</mi>";

//	String MULTIPLIE = "<mo>&#x000d7;</mo>";
//
//	String DIVISE = "<mo>&#x000f7;</mo>";
//
//	String SUP_OU_EGAL = "<mo>&#8805;</mo>";
//
//	String INF_OU_EGAL = "<mo>&#8804;</mo>";
//
//	String ENV_EGAL = "<mo>&#8776;</mo>";
//
//	String DIFFERENT = "<mo>&#8800;</mo>";
//
//        String APPARTIENT = "<mo>&#x02208;</mo>";
//
//        String N_APPARTIENT_PAS = "<mo>&#x02209;</mo>";
//
//	String PLUS_OU_MOINS = "<mo>&#177;</mo>";
//
//	String DELTA = "<mi>&#916;</mi>";
//
//	String PI = "<mn>&#960;</mn>";
//
//	String FLECHE_FONCTION = "<mo>&#x021a6;</mo>";
//
//	String PERPENDICULAIRE = "<mo>&#8869;</mo>";

//        String PARALLELE = "//";

//        String INFINI = "<mn>&#x0221e;</mn>";
//
//	String PUCE = "<mtext>&#9679;</mtext>";
//}
}
