/** «Copyright 2013,2014 François Billioud»
 *
 * This file is part of MathEOS.
 *
 * MathEOS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MathEOS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MathEOS. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Additional Terms according section 7 of GNU/GPL V3 :
 *
 * These additional terms refer to the source code of matheos.
 *
 * According to GNU GPL v3, section 7 b) :
 * You should mention any contributor of the work as long as his/her contribution
 * is meaningful in a covered work. If you convey a source code using a part of the
 * source code of MathEOS, you should keep the original author in the resulting
 * source code. If you propagate a covered work with the same objectives as the
 * Program (help student to attend maths classes with an adapted software), you
 * should mention «Ludovic Faubourg», «Frédéric Marinoni» as author of the idea of
 * this software. In any case, if you propagate a covered work you have to mention
 * François Billioud and Guillaume Varoquaux as author of the realisation of MathEOS
 * software. The paternity of the authors have to appear in a legible, unobscured
 * manner, showing clearly their link to the covered work in any document,
 * web pages,... which describe the project or participate to the distribution of
 * the covered work.
 */

package matheos.utils.dialogue;

import matheos.IHM;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.Bouton;
import matheos.utils.dialogue.InfoDialogueComplet.InfoComposant;
import matheos.utils.managers.FontManager;
import matheos.utils.managers.Traducteur;
import matheos.utils.objets.Navigation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.text.JTextComponent;

/**
 *
 * @author François Billioud
 */
public class DialogueComplet extends JDialog {

    public static final Font POLICE = FontManager.get("font dialog");
    public static final Font POLICE_DESCRIPTION = FontManager.get("font dialog description");
    
    public static final String[] OK_BUTTON = {"ok"};
    public static final String[] OK_CANCEL_BUTTONS = {"ok","cancel"};
    public static final String[] YES_NO_BUTTONS = {"yes","no"};
    public static final String[] YES_NO_CANCEL_BUTTONS = {"yes","no","cancel"};
    
    private final List<DialogueListener> listenerList = new LinkedList<>();
    private final Navigation navigation = new Navigation();
    private final Map<String, JComponent> champs = new HashMap<>();
    private final List<Validator> validations = new LinkedList<>();
    private String aspect;
    
    public DialogueComplet(String aspect) {
        this(new InfoDialogueComplet(aspect));
        this.aspect = aspect;
    }
    public DialogueComplet(String aspect, final JComponent component) {
        this(aspect, new ArrayList<JComponent>() {{add(component);}});//astuce pour une génération de liste en une ligne. cf "double brace initialization"
    }
    public DialogueComplet(String aspect, List<? extends JComponent> components) {
        this(new InfoDialogueComplet(aspect, components));
        this.aspect = aspect;
    }
    private DialogueComplet(InfoDialogueComplet infos) {
        super(IHM.getMainWindow(), infos.titre);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//        setFocusableWindowState(false);
        
        JPanel pane = new JPanel();
        setContentPane(pane);
        pane.setLayout(new BorderLayout());
        pane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        
        if(infos.componentsIds.size()>0) {pane.add(new CenterPane(infos), BorderLayout.CENTER);}
        if(infos.buttons == null || infos.buttons.length==0) {infos.buttons = OK_BUTTON;}
        pane.add(new ButtonPane(infos.buttons), BorderLayout.SOUTH);
        pack();//un premier pack pour déterminer la largeur de référence
        pane.add(new DescriptionPane(infos.description, pane.getPreferredSize().width), BorderLayout.NORTH);

        pack();
        java.awt.Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        this.setLocation(p.x-getWidth()/2,p.y-getHeight()/2);
        setResizable(false);
        setVisible(true);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                fireDialogueEvent(new DialogueEvent(DialogueComplet.this, DialogueEvent.CLOSE_BUTTON, null));
            }
        });
        
        //Fermer en cas d'appuie sur escape
        getRootPane().getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { DialogueComplet.this.dispose(); }
        });
    }
    
    protected Map<String, Object> getInputs() {
        Map<String, Object> reponses = new HashMap<>();
        for(Entry<String, JComponent> e : champs.entrySet()) {
            if(e.getValue() instanceof JTextComponent) {reponses.put(e.getKey(), ((JTextComponent)e.getValue()).getText());}
            else if(e.getValue() instanceof JComboBox) {reponses.put(e.getKey(), ((JComboBox)e.getValue()).getSelectedItem());}
            else if(e.getValue() instanceof JCheckBox) {reponses.put(e.getKey(), ((AbstractButton)e.getValue()).isSelected());}
            else if(e.getValue() instanceof JRadioButton) {reponses.put(e.getKey(), ((AbstractButton)e.getValue()).isSelected());}
            else if(e.getValue() instanceof JToggleButton) {reponses.put(e.getKey(), ((AbstractButton)e.getValue()).isSelected());}
        }
        return reponses;
    }
    
    public Map<String, JComponent> getInputComponents() {return champs;}

    public JComponent getInputComponent(String id) {return champs.get(id);}
    public JTextComponent getInputField(String id) {return (JTextComponent)champs.get(id);}
    public void setInitialValue(String fieldId, String value) {((JTextComponent)champs.get(fieldId)).setText(value);}

    /** Ajoute un set de contraintes à vérifier avant de valider la saisie **/
    public void addValidations(List<Validator> validations) {
        this.validations.addAll(validations);
    }
    
    /** Ajoute un set de contraintes à vérifier avant de valider la saisie **/
    public void addValidation(Validator validation) {this.validations.add(validation);}

    /** Renvoie le système de gestion de la navigation. On peut ainsi le partager **/
    public Navigation getNavigation() {
        return navigation;
    }
    
    /** permet de récupérer les données de langue contenues dans le fichier de langue par rapport à ce dialogue **/
    public Map<String, String> getInfoLangue() {return InfoDialogueComplet.readInfosLangue(aspect);}

    private class DescriptionPane extends JLabel {
        DescriptionPane(String description, int largeurMax) {
            super(decoupe(description, largeurMax-0));
            setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 30));
            setFont(POLICE_DESCRIPTION);
            System.out.println("description : "+getPreferredSize().width);
        }
    }
    
    private class CenterPane extends JPanel {
        private CenterPane(InfoDialogueComplet infos) {
            this(createLines(infos));
        }
        private CenterPane(Ligne... lignes) {
            this();
            for(Ligne l : lignes) {
                add(Box.createVerticalStrut(20));
                add(l);
            }
            add(Box.createVerticalGlue());
        }
        private CenterPane() {
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setFocusable(false);
        }
    }
    
    private class ButtonPane extends JPanel {
        ButtonPane(String... boutonsName) {
            LinkedList<Bouton> boutons = new LinkedList<>();
            for (String boutonsName1 : boutonsName) {
                Bouton bouton = new Bouton(new ActionDialog(boutonsName1));
                navigation.addComponent(bouton);
                bouton.setMaximumSize(new Dimension(250, 30));
                bouton.setPreferredSize(new Dimension(100, 30));
                bouton.setFocusable(true);
                bouton.setFont(POLICE);
                boutons.add(bouton);
            }
            
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            add(Box.createHorizontalGlue());
            add(boutons.poll());
            while(!boutons.isEmpty()) {
                add(Box.createHorizontalStrut(20));
                add(boutons.poll());
            }
            add(Box.createHorizontalGlue());
            setBorder(BorderFactory.createEmptyBorder(20, 80, 5, 80));
            
            setFocusable(false);
        }
    }
    
    private static final class Ligne extends JPanel {
        Ligne(String label, JComponent composant) {
            this(prepareLabel(label), composant);
        }
        Ligne(JComponent... components) {
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setFocusable(false);
            for(JComponent component : components) {
                add(Box.createHorizontalStrut(30));
                add(component);
            }
            add(Box.createHorizontalGlue());
            setMaximumSize(new Dimension(getMaximumSize().width, getPreferredSize().height));
        }
    }
    private static JLabel prepareLabel(String text) {
        JLabel label = new JLabel(text+" :");
        label.setFont(POLICE);
        return label;
    }
    
    private Ligne[] createLines(InfoDialogueComplet infos) {
        Ligne[] answer = new Ligne[infos.componentsIds.size()];
        for(int i = 0; i<infos.componentsIds.size(); i++) {
            String aspect = infos.componentsIds.get(i);
            InfoComposant infoComp = infos.getInfosComposant(aspect);
            final JComponent composant = infoComp.composant;
            composant.setFont(POLICE);
            composant.setMaximumSize(composant.getPreferredSize());
            if(composant instanceof JTextComponent) {
                composant.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        ((JTextComponent)composant).selectAll();
                    }
                });
            }

            navigation.addComponent(composant);
            champs.put(aspect, composant);
            
            String emptyAllowed = (String) composant.getClientProperty(InfoDialogueComplet.EMPTY_ALLOWED_PROPERTY);
            if("no".equals(emptyAllowed)) {validations.add(new ValidationNotEmpty(composant));}
            
            String type = (String) composant.getClientProperty(InfoDialogueComplet.TYPE_PROPERTY);
            if(type!=null && composant instanceof JTextComponent) {
                switch(type) {
                    case "decimal" : validations.add(new ValidationDouble((JTextComponent)composant));break;
                    case "integer" : validations.add(new ValidationInteger((JTextComponent)composant));break;
                }
            }
            
            Ligne ligne;
            if(composant instanceof Ligne) {
                ligne = (Ligne)composant;
            } else if(infoComp.label == null || infoComp.label.isEmpty()) {
                ligne = new Ligne(composant);
            } else {
                ligne = new Ligne(infoComp.label, infoComp.composant);
            }
            answer[i] = ligne;
        }
        return answer;
    }
    
    private class ActionDialog extends ActionComplete {
        private final String buttonID;
        /**
         * Crée une action envoyant le résultat de l'input aux listeners de ce dialog
         * @param buttonID le nom du bouton tel que défini dans le fichier lang. Il servira aussi d'id et de commande
         */
        ActionDialog(String buttonID) {super(buttonID); this.buttonID = buttonID;}
        @Override
        public void actionPerformed(ActionEvent e) {
            fireDialogueEvent(new DialogueEvent(DialogueComplet.this, buttonID, getInputs()));
        }
    }

    private String decoupe(String s, int longueurMax) {
        String[] T = s.split(" ");
        String resultat = "<HTML><CENTER>";
        String ligne = "";
        FontMetrics fm = getFontMetrics(POLICE_DESCRIPTION);
        for(String mot : T) {
            if(fm.stringWidth(ligne+" "+mot)<longueurMax) {
                ligne+=" "+mot;
            } else {
                resultat+=ligne+"<br/>";
                ligne = mot;
            }
        }
        return resultat + ligne + "</CENTER></HTML>";
    }

    public void addDialogueListener(DialogueListener d) {
        listenerList.add(d);
    }

    public void removeDialogueListener(DialogueListener l) {
        listenerList.remove(l);
    }

    /**
     * Vérifie que l'input passe l'étape de vérification
     * @param event l'event contenant les résultats de l'input
     * @return true si l'input valide les conditions, false sinon
     */
    public boolean checkDialogueEvent(DialogueEvent event) {
        if(!event.getCommand().equals(DialogueEvent.CANCEL_BUTTON) && !event.getCommand().equals(DialogueEvent.CLOSE_BUTTON)) {
            for(Validator v : validations) {
                if(!v.validate(event)) {
                    if(v.validationFails()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Teste le résultat en cas de clic sur "ok" ou "yes",
     * Envoie les résultats aux listeners sauf si le test a été effectué et a échoué
     * Ferme la fenêtre sauf si le test a été effectué et a échoué
     * @param event l'event contenant les résultats de l'input
     */
    protected void fireDialogueEvent(DialogueEvent event) {
        if(event.isConfirmButtonPressed()) {
            if(!checkDialogueEvent(event)) {return;}
        }
        for (DialogueListener l : listenerList) {
            l.dialoguePerformed(event);
        }
        dispose();
    }
    

    //XXX On peut envisager l'utilisation d'InputVerifier
    public static class ValidationDouble implements Validator {
        private final JTextComponent text;
        private static final String DECIMAL_POINT = Traducteur.traduire("decimal point");
        public ValidationDouble(JTextComponent text) {
            this.text = text;
        }

        @Override
        public boolean validate(DialogueEvent event) {
            this.text.setText(this.text.getText().replaceAll(DECIMAL_POINT, "."));//On accepte la virgule comme marque décimale
            try {Double.parseDouble(text.getText());} catch(NumberFormatException ex) {return false;}
            return true;
        }

        @Override
        public boolean validationFails() {
            DialogueBloquant.error(Traducteur.traduire("error"), Traducteur.traduire("not decimal"));
            text.setBorder(BorderFactory.createLineBorder(Color.red));
            text.requestFocusInWindow();
            text.repaint();
            return true;
        }
    }
    
    public static class ValidationInteger implements Validator {
        private final JTextComponent text;
        public ValidationInteger(JTextComponent text) {
            this.text = text;
        }

        @Override
        public boolean validate(DialogueEvent event) {
            try {Integer.parseInt(text.getText());} catch(NumberFormatException ex) {return false;}
            return true;
        }

        @Override
        public boolean validationFails() {
            DialogueBloquant.error(Traducteur.traduire("error"), Traducteur.traduire("not integer"));
            text.setBorder(BorderFactory.createLineBorder(Color.red));
            text.requestFocusInWindow();
            text.repaint();
            return true;
        }
    }
    
    public static class ValidationNotEmpty implements Validator {
        JComponent composant;
        public ValidationNotEmpty(JComponent composant) {
            this.composant = composant;
        }

        @Override
        public boolean validate(DialogueEvent event) {
            if(composant instanceof JTextComponent) {return !((JTextComponent)composant).getText().isEmpty();}
            else if(composant instanceof JComboBox) {return ((JComboBox)composant).getSelectedIndex()!=-1;}
            return true;
        }

        @Override
        public boolean validationFails() {
            DialogueBloquant.error(Traducteur.traduire("error"), Traducteur.traduire("not empty"));
            composant.setBorder(BorderFactory.createLineBorder(Color.red));
            composant.requestFocusInWindow();
            composant.repaint();
            return true;
        }
    }
}
