/** «Copyright 2011,2013 François Billioud, Guillaume Varoquaux»
 *
 * This file is part of Bomehc.
 *
 * Bomehc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bomehc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Bomehc. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Additional Terms according section 7 of GNU/GPL V3 :
 *
 * These additional terms refer to the source code of bomehc.
 *
 * According to GNU GPL v3, section 7 b) :
 * You should mention any contributor of the work as long as his/her contribution
 * is meaningful in a covered work. If you convey a source code using a part of the
 * source code of Bomehc, you should keep the original author in the resulting
 * source code. If you propagate a covered work with the same objectives as the
 * Program (help student to attend maths classes with an adapted software), you
 * should mention «Ludovic Faubourg», «Frédéric Marinoni» as author of the idea of
 * this software. In any case, if you propagate a covered work you have to mention
 * François Billioud and Guillaume Varoquaux as author of the realisation of Bomehc
 * software. The paternity of the authors have to appear in a legible, unobscured
 * manner, showing clearly their link to the covered work in any document,
 * web pages,... which describe the project or participate to the distribution of
 * the covered work.
 */

package bomehc.clavier;

import bomehc.utils.dialogue.DialogueBloquant;
import bomehc.utils.dialogue.math.DialogueMath;
import bomehc.utils.dialogue.math.DialogueMathChapeauAngle;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import bomehc.utils.managers.ImageManager;
import bomehc.utils.dialogue.math.DialogueMathEquation;
import bomehc.utils.dialogue.math.DialogueMathExposant;
import bomehc.utils.dialogue.math.DialogueMathFraction;
import bomehc.utils.dialogue.math.DialogueMathIndice;
import bomehc.utils.dialogue.math.DialogueMathParentheseLarge;
import bomehc.utils.dialogue.math.DialogueMathRacineCarree;
import bomehc.utils.dialogue.math.DialogueMathSysteme;
import bomehc.utils.managers.PermissionManager;
import bomehc.utils.objets.Icone;
import bomehc.utils.texte.EditeurIO;
import bomehc.utils.texte.JLimitedMathTextPane;
import bomehc.utils.texte.JMathTextPane;


/**
 *
 * @author François Billioud, Guillaume Varoquaux
 */
@SuppressWarnings("serial")
public final class ClavierCaractereSpeciaux extends Clavier {

    public static final int BOUTON_PARENTHESE_LEFT = 0;
    public static final int BOUTON_PARENTHESE_RIGHT = 1;
    public static final int BOUTON_CROCHET_LEFT = 2;
    public static final int BOUTON_CROCHET_RIGHT = 3;
    public static final int BOUTON_ACCOLADE_LEFT = 4;
    public static final int BOUTON_ACCOLADE_DROIT = 5;
    public static final int BOUTON_RACINE_CARREE = 6;
    public static final int BOUTON_EXPOSANT = 7;
    public static final int BOUTON_INDICE = 8;
    public static final int BOUTON_FRACTION = 9;
    public static final int BOUTON_PARENTHESE_LARGE = 10;
    public static final int BOUTON_SYSTEME = 11;
    public static final int BOUTON_INFERIEUR = 12;
    public static final int BOUTON_SUPERIEUR = 13;
    public static final int BOUTON_INF_EGAL = 14;
    public static final int BOUTON_SUP_EGAL = 15;
    public static final int BOUTON_ENV_EGAL = 16;
    public static final int BOUTON_DIFFERENT = 17;
    public static final int BOUTON_PLUS_MOINS = 18;
    public static final int BOUTON_DELTA = 19;
    public static final int BOUTON_PI = 20;
    public static final int BOUTON_CHAPEAU_ANGLE = 21;
    public static final int BOUTON_FLECHE_FONCTION = 22;
    public static final int BOUTON_PERPENDICULAIRE = 23;
    public static final int BOUTON_PARALLELE = 24;
    public static final int BOUTON_APPARTIENT = 25;
    public static final int BOUTON_N_APPARTIENT_PAS = 26;
    public static final int BOUTON_INFINI = 27;
    public static final int BOUTON_EQUATION = 28;
    public static final int BOUTON_PUCE = 29;
    public static final int NOMBRE_BOUTON = 30;

    public ClavierCaractereSpeciaux() {
        super();
        panelClavier = new PanelCaractereSpeciaux();
        this.setSize(panelClavier.getWidth(), panelClavier.getHeight());
        this.add(panelClavier);
        
        updateBoutonsAutorises();
        
        repaint();
    }

    public void updateBoutonsAutorises() {
        activerBouton(ClavierCaractereSpeciaux.BOUTON_FLECHE_FONCTION, PermissionManager.isFonctionsAllowed());
        activerBouton(ClavierCaractereSpeciaux.BOUTON_INFINI, PermissionManager.isFonctionsAllowed());
        
        activerBouton(ClavierCaractereSpeciaux.BOUTON_APPARTIENT, PermissionManager.isCaracteresCollegeAllowed());
        activerBouton(ClavierCaractereSpeciaux.BOUTON_CHAPEAU_ANGLE, PermissionManager.isCaracteresCollegeAllowed());
        activerBouton(ClavierCaractereSpeciaux.BOUTON_DIFFERENT, PermissionManager.isCaracteresCollegeAllowed());
        activerBouton(ClavierCaractereSpeciaux.BOUTON_ENV_EGAL, PermissionManager.isCaracteresCollegeAllowed());
        activerBouton(ClavierCaractereSpeciaux.BOUTON_EXPOSANT, PermissionManager.isCaracteresCollegeAllowed());
        activerBouton(ClavierCaractereSpeciaux.BOUTON_INDICE, PermissionManager.isCaracteresCollegeAllowed());
        activerBouton(ClavierCaractereSpeciaux.BOUTON_N_APPARTIENT_PAS, PermissionManager.isCaracteresCollegeAllowed());
        activerBouton(ClavierCaractereSpeciaux.BOUTON_PARALLELE, PermissionManager.isCaracteresCollegeAllowed());
        activerBouton(ClavierCaractereSpeciaux.BOUTON_PERPENDICULAIRE, PermissionManager.isCaracteresCollegeAllowed());
        activerBouton(ClavierCaractereSpeciaux.BOUTON_PI, PermissionManager.isCaracteresCollegeAllowed());
        
        activerBouton(ClavierCaractereSpeciaux.BOUTON_INF_EGAL, PermissionManager.isComparateursSpeciauxAllowed());
        activerBouton(ClavierCaractereSpeciaux.BOUTON_SUP_EGAL, PermissionManager.isComparateursSpeciauxAllowed());

        activerBouton(ClavierCaractereSpeciaux.BOUTON_RACINE_CARREE, PermissionManager.isRacineCarreeAllowed());

        activerBouton(ClavierCaractereSpeciaux.BOUTON_EQUATION, PermissionManager.isCaracteresAvancesAllowed());
        activerBouton(ClavierCaractereSpeciaux.BOUTON_SYSTEME, PermissionManager.isCaracteresAvancesAllowed());
    }
    
/*    public void activeBoutonsOnglet(Onglet onglet) {
        activeAll();
        switch (onglet.getName()) {
            case TEXTE:
                activeBoutonsTexte();
                break;
            case FONCTION:
                activeBoutonsFonction();
                break;
            case TABLEAUX:
                activeBoutonsTableau();
                break;
            case OPERATIONS:
                activeBoutonsOperation();
                break;
            case GEOMETRIE:
                activeBoutonsGeometrie();
                break;
            default:
                activeBoutonsTexte();
                break;
        }
        panelClavier.repaint();
    }
*/
    private class PanelCaractereSpeciaux extends PanelClavier {

        private PanelCaractereSpeciaux() {
            GridLayout grille = new GridLayout(5, 6, 5, 5);
            this.setLayout(grille);
            this.setSize(355, 245);

            bouton = new BoutonClavier[NOMBRE_BOUTON];

            bouton[BOUTON_PARENTHESE_LEFT] = new BoutonClavier(new ActionBoutonPARENTHESELEFT());
            bouton[BOUTON_PARENTHESE_RIGHT] = new BoutonClavier(new ActionBoutonPARENTHESERIGHT());
            bouton[BOUTON_CROCHET_LEFT] = new BoutonClavier(new ActionBoutonCROCHETLEFT());
            bouton[BOUTON_CROCHET_RIGHT] = new BoutonClavier(new ActionBoutonCROCHETRIGHT());
            bouton[BOUTON_ACCOLADE_LEFT] = new BoutonClavier(new ActionBoutonACCOLADELEFT());
            bouton[BOUTON_ACCOLADE_DROIT] = new BoutonClavier(new ActionBoutonACCOLADERIGHT());
            bouton[BOUTON_RACINE_CARREE] = new BoutonClavier(new ActionBoutonRACINECARREE());
            bouton[BOUTON_EXPOSANT] = new BoutonClavier(new ActionBoutonEXPOSANT());
            bouton[BOUTON_INDICE] = new BoutonClavier(new ActionBoutonINDICE());
            bouton[BOUTON_FRACTION] = new BoutonClavier(new ActionBoutonFRACTION());
            bouton[BOUTON_PARENTHESE_LARGE] = new BoutonClavier(new ActionBoutonPARENTHESELARGE());
            bouton[BOUTON_SYSTEME] = new BoutonClavier(new ActionBoutonSYSTEME());
            bouton[BOUTON_INFERIEUR] = new BoutonClavier(new ActionBoutonINFERIEUR());
            bouton[BOUTON_SUPERIEUR] = new BoutonClavier(new ActionBoutonSUPERIEUR());
            bouton[BOUTON_INF_EGAL] = new BoutonClavier(new ActionBoutonINFEGAL());
            bouton[BOUTON_SUP_EGAL] = new BoutonClavier(new ActionBoutonSUPEGAL());
            bouton[BOUTON_ENV_EGAL] = new BoutonClavier(new ActionBoutonENVEGAL());
            bouton[BOUTON_DIFFERENT] = new BoutonClavier(new ActionBoutonDIFFERENT());
            bouton[BOUTON_PLUS_MOINS] = new BoutonClavier(new ActionBoutonPLUSMOINS());
            bouton[BOUTON_DELTA] = new BoutonClavier(new ActionBoutonDELTA());
            bouton[BOUTON_PI] = new BoutonClavier(new ActionBoutonPI());
            bouton[BOUTON_CHAPEAU_ANGLE] = new BoutonClavier(new ActionBoutonCHAPEAUANGLE());
            bouton[BOUTON_FLECHE_FONCTION] = new BoutonClavier(new ActionBoutonFLECHEFONCTION());
            bouton[BOUTON_PERPENDICULAIRE] = new BoutonClavier(new ActionBoutonPERPENDICULAIRE());
            bouton[BOUTON_PARALLELE] = new BoutonClavier(new ActionBoutonPARALLELE());
            bouton[BOUTON_APPARTIENT] = new BoutonClavier(new ActionBoutonAPPARTIENT());
            bouton[BOUTON_N_APPARTIENT_PAS] = new BoutonClavier(new ActionBoutonNAPPARTIENTPAS());
            bouton[BOUTON_INFINI] = new BoutonClavier(new ActionBoutonINFINI());
            bouton[BOUTON_EQUATION] = new BoutonClavier(new ActionBoutonEQUATION());
            bouton[BOUTON_PUCE] = new BoutonClavier(new ActionBoutonPUCE());

            for (int i = 0; i <= bouton.length - 1; i++) {
                this.add(bouton[i]);
            }
            revalidate();
            repaint();
        }
    }

    private class ActionBoutonPARENTHESELEFT extends ActionBoutonTexte {
        ActionBoutonPARENTHESELEFT() {
            super("(");
        }
    }

    private class ActionBoutonPARENTHESERIGHT extends ActionBoutonTexte {
        ActionBoutonPARENTHESERIGHT() {
            super(")");
        }
    }

    private class ActionBoutonCROCHETLEFT extends ActionBoutonTexte {
        ActionBoutonCROCHETLEFT() {
            super("[");
        }
    }

    private class ActionBoutonCROCHETRIGHT extends ActionBoutonTexte {
        ActionBoutonCROCHETRIGHT() {
            super("]");
        }
    }

    private class ActionBoutonACCOLADELEFT extends ActionBoutonTexte {
        ActionBoutonACCOLADELEFT() {
            super("{");
        }
    }

    private class ActionBoutonACCOLADERIGHT extends ActionBoutonTexte {
        ActionBoutonACCOLADERIGHT() {
            super("}");
        }
    }

    private class ActionBoutonINFERIEUR extends ActionBoutonTexte {
        ActionBoutonINFERIEUR() {
            super("<");
        }
    }

    private class ActionBoutonSUPERIEUR extends ActionBoutonTexte {
        ActionBoutonSUPERIEUR() {
            super(">");
        }
    }

    private class ActionBoutonPARALLELE extends ActionBoutonTexte {
        ActionBoutonPARALLELE() {
            super("//");
        }
    }

    private class ActionBoutonRACINECARREE extends ActionBoutonMathMLSpecial {
        ActionBoutonRACINECARREE() {
            super(ImageManager.getIcone("special sqrt", 25, 25), SPECIAL.RACINE_CARREE);
        }
    }

    private class ActionBoutonEXPOSANT extends ActionBoutonMathMLSpecial {
        ActionBoutonEXPOSANT() {
            super("<html><font size='5'>x<sup>2</sup></font></html>", SPECIAL.EXPOSANT);
        }
    }

    private class ActionBoutonINDICE extends ActionBoutonMathMLSpecial {
        ActionBoutonINDICE() {
            super("<html><font size='5'>f<sub>i</sub></font></html>", SPECIAL.INDICE);
        }
    }

    private class ActionBoutonFRACTION extends ActionBoutonMathMLSpecial {
        ActionBoutonFRACTION() {
            super(ImageManager.getIcone("special fraction", 40, 40), SPECIAL.FRACTION);
        }
    }

    private class ActionBoutonPARENTHESELARGE extends ActionBoutonMathMLSpecial {
        ActionBoutonPARENTHESELARGE() {
            super(ImageManager.getIcone("special parenthese", 32, 32), SPECIAL.PARENTHESE_LARGE);
        }
    }

    private class ActionBoutonSYSTEME extends ActionBoutonMathMLSpecial {
        ActionBoutonSYSTEME() {
            super(ImageManager.getIcone("special equation", 25, 25), SPECIAL.SYSTEME);
        }
    }

    private class ActionBoutonCHAPEAUANGLE extends ActionBoutonMathMLSpecial {
        ActionBoutonCHAPEAUANGLE() {
            super(ImageManager.getIcone("special angle mark", 25, 25), SPECIAL.CHAPEAU_ANGLE);
        }

//        @Override
//        public void actionPerformed(ActionEvent e) {
//            if(getFocusedMathListener()==null) {return;}
//            
//            String selectedText = getFocusedMathListener().getSelectedText();
//            String angle;
//            if(selectedText != null && (selectedText.length()==1 || selectedText.length()==3)) {
//                angle = DialogueBloquant.input("dialog angle mark", selectedText);
//            } else {
//                angle = DialogueBloquant.input("dialog angle mark");
//            }
//            if(angle==null) {return;}
//            if(angle.length()!=1 && angle.length()!=3) {
//                DialogueBloquant.error("dialog angle arguments");
//                actionPerformed(e);
//                return;
//            } else {
//                getFocusedMathListener().insererJMathComponent(MathTools.creerMathComponent("<math><mover><mrow><mn>" + angle + "</mn></mrow><mn>^</mn></mover></math>"));
//            }
//        }
    }

    private class ActionBoutonEQUATION extends ActionBoutonMathMLSpecial {

        ActionBoutonEQUATION() {
            super("<html><font size='5'>Eq</font></html>", SPECIAL.EQUATION);
        }
    }

    private class ActionBoutonAPPARTIENT extends ActionBoutonHTMLString {
        ActionBoutonAPPARTIENT() {
            super("<html>&#x02208;</html>");
//            super("\u2208");
        }
    }

    private class ActionBoutonNAPPARTIENTPAS extends ActionBoutonHTMLString {
        ActionBoutonNAPPARTIENTPAS() {
            super("<html>&#x02209;</html>");
//            super("\u2209");
        }
    }

    private class ActionBoutonINFINI extends ActionBoutonTexte {
        ActionBoutonINFINI() {
//            super("<html>&#x0221e;</html>");
            super("\u221E");
        }
    }

    private class ActionBoutonINFEGAL extends ActionBoutonTexte {
        ActionBoutonINFEGAL() {
//            super("<html>&#8804;</html>");
            super("\u2264");
        }
    }

    private class ActionBoutonSUPEGAL extends ActionBoutonTexte {
        ActionBoutonSUPEGAL() {
//            super("<html>&#8805;</html>");
            super("\u2265");
        }
    }

    private class ActionBoutonENVEGAL extends ActionBoutonTexte {
        ActionBoutonENVEGAL() {
//            super("<html>&#8776</html>");
            super("\u2248");
        }
    }

    private class ActionBoutonDIFFERENT extends ActionBoutonTexte {
        ActionBoutonDIFFERENT() {
//            super("<html>&#8800</html>");
            super("\u2260");
        }
    }

    private class ActionBoutonPLUSMOINS extends ActionBoutonTexte {
        ActionBoutonPLUSMOINS() {
//            super("<html>&#177</html>");
            super("\u00B1");
        }
    }

    private class ActionBoutonDELTA extends ActionBoutonHTMLString {
        ActionBoutonDELTA() {
            super("<html><font size='5'>&#916;</font></html>");
//            super("\u0394");
        }
    }

    private class ActionBoutonPI extends ActionBoutonHTMLString {
        ActionBoutonPI() {
            super("<html><font size='5'>&#960;</font></html>");
//            super("\u03C0");
        }
    }

    private class ActionBoutonFLECHEFONCTION extends ActionBoutonHTMLString {
        ActionBoutonFLECHEFONCTION() {
            super("<html><font size='6'>&#8614;</font></html>");
//            super("\u21A6");
        }
    }

    private class ActionBoutonPERPENDICULAIRE extends ActionBoutonHTMLString {
        ActionBoutonPERPENDICULAIRE() {
            super("<html><font size='6'>&#8869;</font></html>");
//            super("\u22A5");
        }
    }

    private class ActionBoutonPUCE extends ActionBoutonHTMLString {
        ActionBoutonPUCE() {
            super("<html><font size='5'>&#9679;</font></html>");
//            super("\u25CF");
        }
    }
    
    /** Action qui traite un texte mathML spécial **/
    private static class ActionBoutonMathMLSpecial extends ActionBoutonMathML {
        SPECIAL type;
        private ActionBoutonMathMLSpecial(Icone apparence,SPECIAL type) {
            super(apparence);
            this.type = type;
        }
        private ActionBoutonMathMLSpecial(String apparence,SPECIAL type) {
            super(apparence);
            this.type = type;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            final JMathTextPane textPane = getFocusedMathListener();
            if (textPane==null) {return;}

            //tente de mettre la sélection dans le DialogueMath
            if (textPane.getSelectedText() != null) {
                if (JLimitedMathTextPane.isContentTooLong(textPane)) {
                    DialogueBloquant.error("dialog selection too long");
                    return;
                }
            }

            DialogueMath dialogue = getDialogueMath(type, textPane);
            dialogue.addDialogueMathListener(dialogue.new CreateListener(textPane));
        }
    }
    
    private static enum SPECIAL {RACINE_CARREE, FRACTION, EQUATION, SYSTEME, INDICE, EXPOSANT, PARENTHESE_LARGE, CHAPEAU_ANGLE};
    private static DialogueMath getDialogueMath(SPECIAL type, JMathTextPane textPane) {
        String selectedHTMLText = textPane.getSelectedText()==null ? null : EditeurIO.export2htmlMathML(textPane, textPane.getSelectionStart(), textPane.getSelectionEnd()-textPane.getSelectionStart());
        DialogueMath dialogue;
        switch(type) {
            case EQUATION : dialogue = selectedHTMLText==null ? new DialogueMathEquation(textPane) : new DialogueMathEquation(textPane, selectedHTMLText); break;
            case EXPOSANT : dialogue = selectedHTMLText==null ? new DialogueMathExposant(textPane) : new DialogueMathExposant(textPane, selectedHTMLText, ""); break;
            case FRACTION : dialogue = selectedHTMLText==null ? new DialogueMathFraction(textPane) : new DialogueMathFraction(textPane, selectedHTMLText, ""); break;
            case INDICE : dialogue = selectedHTMLText==null ? new DialogueMathIndice(textPane) : new DialogueMathIndice(textPane, selectedHTMLText, ""); break;
            case PARENTHESE_LARGE : dialogue = selectedHTMLText==null ? new DialogueMathParentheseLarge(textPane) : new DialogueMathParentheseLarge(textPane, selectedHTMLText); break;
            case RACINE_CARREE : dialogue = selectedHTMLText==null ? new DialogueMathRacineCarree(textPane) : new DialogueMathRacineCarree(textPane, selectedHTMLText); break;
            case SYSTEME : dialogue = selectedHTMLText==null ? new DialogueMathSysteme(textPane) : new DialogueMathSysteme(textPane, selectedHTMLText, ""); break;
            case CHAPEAU_ANGLE : dialogue = selectedHTMLText==null ? new DialogueMathChapeauAngle(textPane) : new DialogueMathChapeauAngle(textPane, selectedHTMLText); break;
            default: dialogue = null;
        }
        return dialogue;
    }

/*    private void activeAll() {
        for (JButtonClavier button : bouton) {
            button.setEnabled(true);
        }
    }

    private void activeBoutonsTexte() {
        bouton[BOUTON_CODIF_ANGLEDROIT].setEnabled(false);
        bouton[BOUTON_CODIF_ANGLE].setEnabled(false);
        bouton[BOUTON_ELEM_EGAUX].setEnabled(false);
    }

    private void activeBoutonsFonction() {
        bouton[BOUTON_ACCOLADE_LEFT].setEnabled(false);
        bouton[BOUTON_ACCOLADE_DROIT].setEnabled(false);
        bouton[BOUTON_SYSTEME].setEnabled(false);
        bouton[BOUTON_INFERIEUR].setEnabled(false);
        bouton[BOUTON_SUPERIEUR].setEnabled(false);
        bouton[BOUTON_INF_EGAL].setEnabled(false);
        bouton[BOUTON_SUP_EGAL].setEnabled(false);
        bouton[BOUTON_ENV_EGAL].setEnabled(false);
        bouton[BOUTON_DIFFERENT].setEnabled(false);
        bouton[BOUTON_PLUS_MOINS].setEnabled(false);
        bouton[BOUTON_CHAPEAU_ANGLE].setEnabled(false);
        bouton[BOUTON_PERPENDICULAIRE].setEnabled(false);
        bouton[BOUTON_PARALLELE].setEnabled(false);
        bouton[BOUTON_CODIF_ANGLEDROIT].setEnabled(false);
        bouton[BOUTON_CODIF_ANGLE].setEnabled(false);
        bouton[BOUTON_ELEM_EGAUX].setEnabled(false);
        bouton[BOUTON_PUCE].setEnabled(false);
    }

    private void activeBoutonsGeometrie() {
        bouton[BOUTON_ACCOLADE_LEFT].setEnabled(false);
        bouton[BOUTON_ACCOLADE_DROIT].setEnabled(false);
        bouton[BOUTON_SYSTEME].setEnabled(false);
        bouton[BOUTON_INFERIEUR].setEnabled(false);
        bouton[BOUTON_SUPERIEUR].setEnabled(false);
        bouton[BOUTON_INF_EGAL].setEnabled(false);
        bouton[BOUTON_SUP_EGAL].setEnabled(false);
        bouton[BOUTON_ENV_EGAL].setEnabled(false);
        bouton[BOUTON_DIFFERENT].setEnabled(false);
        bouton[BOUTON_PLUS_MOINS].setEnabled(false);
        bouton[BOUTON_FLECHE_FONCTION].setEnabled(false);
        bouton[BOUTON_PUCE].setEnabled(false);
    }

    private void activeBoutonsOperation() {
        bouton[BOUTON_PARENTHESE_LEFT].setEnabled(false);
        bouton[BOUTON_PARENTHESE_RIGHT].setEnabled(false);
        bouton[BOUTON_CROCHET_LEFT].setEnabled(false);
        bouton[BOUTON_CROCHET_RIGHT].setEnabled(false);
        bouton[BOUTON_ACCOLADE_LEFT].setEnabled(false);
        bouton[BOUTON_ACCOLADE_DROIT].setEnabled(false);
        bouton[BOUTON_RACINE_CARREE].setEnabled(false);
        bouton[BOUTON_EXPOSANT].setEnabled(false);
        bouton[BOUTON_INDICE].setEnabled(false);
        bouton[BOUTON_FRACTION].setEnabled(false);
        bouton[BOUTON_PARENTHESE_LARGE].setEnabled(false);
        bouton[BOUTON_SYSTEME].setEnabled(false);
        bouton[BOUTON_INFERIEUR].setEnabled(false);
        bouton[BOUTON_SUPERIEUR].setEnabled(false);
        bouton[BOUTON_INF_EGAL].setEnabled(false);
        bouton[BOUTON_SUP_EGAL].setEnabled(false);
        bouton[BOUTON_ENV_EGAL].setEnabled(false);
        bouton[BOUTON_DIFFERENT].setEnabled(false);
        bouton[BOUTON_PLUS_MOINS].setEnabled(false);
        bouton[BOUTON_DELTA].setEnabled(false);
        bouton[BOUTON_PI].setEnabled(false);
        bouton[BOUTON_CHAPEAU_ANGLE].setEnabled(false);
        bouton[BOUTON_FLECHE_FONCTION].setEnabled(false);
        bouton[BOUTON_PERPENDICULAIRE].setEnabled(false);
        bouton[BOUTON_PARALLELE].setEnabled(false);
        bouton[BOUTON_CODIF_ANGLEDROIT].setEnabled(false);
        bouton[BOUTON_CODIF_ANGLE].setEnabled(false);
        bouton[BOUTON_ELEM_EGAUX].setEnabled(false);
        bouton[BOUTON_EQUATION].setEnabled(false);
        bouton[BOUTON_PUCE].setEnabled(false);
    }

    private void activeBoutonsTableau() {
        bouton[BOUTON_CODIF_ANGLEDROIT].setEnabled(false);
        bouton[BOUTON_CODIF_ANGLE].setEnabled(false);
        bouton[BOUTON_ELEM_EGAUX].setEnabled(false);
        bouton[BOUTON_PUCE].setEnabled(false);
    }*/
}
