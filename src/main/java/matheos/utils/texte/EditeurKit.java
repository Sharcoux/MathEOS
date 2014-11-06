/** «Copyright 2013 François Billioud»
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

package matheos.utils.texte;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.StyledEditorKit.StyledTextAction;
import javax.swing.text.html.HTMLEditorKit;
import matheos.IHM;
import matheos.texte.Editeur;
import matheos.texte.composants.ComposantTexte;
import matheos.texte.composants.JLabelText;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.ActionGroup;
import matheos.utils.boutons.Bouton;
import matheos.utils.boutons.MenuDeroulant;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.ImageManager;
import matheos.utils.managers.Traducteur;
import matheos.utils.objets.Icone;

/**
 * Cette classe permet de gérer la plupart des actions de style classique.
 * Les différents composants Swing proposés ne sont généré que dans le cas où ils sont
 * utilisés.
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class EditeurKit {

    public static final String STRIKE_COLOR_ATTRIBUTE = "text-decoration-color";
    
    private JTextComponent textEditor = null;

    private Bouton boutonBold = null;
    private Bouton boutonItalic = null;
    private Bouton boutonUnderline = null;
    private Bouton boutonStrike = null;
    private Bouton boutonLeftAlined = null;
    private Bouton boutonCenterAlined = null;
    private Bouton boutonRightAlined = null;
    private Bouton boutonTitle = null;
    private Bouton boutonSubTitle = null;
    private ChoixCouleur menuCouleur = null;
//    private ChoixTaillePolice menuTaille = null;


    public EditeurKit(JTextComponent textComponent) {textEditor = textComponent;}
    public EditeurKit() {}

    /**
     * Permet de limiter la portée des objets à un seul éditeur de texte
     * @param textComponent le seul editeur concerné par cet EditeurKit
     */
    public void setTextEditor(JTextComponent textComponent) {textEditor = textComponent;}

    /**
     * Cette fonction remet tous les boutons dans leur état initial
     */
    public void reset() {
        if(isBoldImplemented()) {boutonBold.setSelected(false);}
        if(isItalicImplemented()) {boutonItalic.setSelected(false);}
        if(isUnderlinedImplemented()) {boutonUnderline.setSelected(false);}
        if(isStrikeImplemented()) {boutonStrike.setSelected(false);}
        if(isCenterAlignmentImplemented() || isRightAlignmentImplemented()) {boutonLeftAlined.setSelected(true);}
        if(isForegroundColorImplemented()) {menuCouleur.setSelectedIndex(0);}
//        if(isFontSizeImplemented()) {menuTaille.setSelectedIndex(0);}
    }

    public boolean isBold() {return getBoutonBold().isSelected();}
    public boolean isItalic() {return getBoutonItalic().isSelected();}
    public boolean isUnderlined() {return getBoutonUnderline().isSelected();}
    public boolean isStroken() {return getBoutonStrike().isSelected();}
    public boolean isLeftAlined() {return getBoutonLeftAlined().isSelected();}
    public boolean isCenterAlined() {return getBoutonCenterAlined().isSelected();}
    public boolean isRightAlined() {return getBoutonRightAlined().isSelected();}
    public Color getForeground() {return getMenuCouleur().getSelectedCouleur();}
//    /** renvoie la font-size au format RTF (pt) **/
//    public int getRTFFontSize() {return getMenuTaille().getSelectedTaillePolice();}
//    /** renvoie la font-size au format CSS (em) **/
//    public double getCSSFontSize() {return JsoupTools.convertPT2EM(getRTFFontSize());}

    private boolean isBoldImplemented() {return boutonBold!=null;}
    private boolean isItalicImplemented() {return boutonItalic!=null;}
    private boolean isUnderlinedImplemented() {return boutonUnderline!=null;}
    private boolean isStrikeImplemented() {return boutonStrike!=null;}
    private boolean isLeftAlignmentImplemented() {return boutonLeftAlined!=null;}
    private boolean isCenterAlignmentImplemented() {return boutonCenterAlined!=null;}
    private boolean isRightAlignmentImplemented() {return boutonRightAlined!=null;}
    private boolean isForegroundColorImplemented() {return menuCouleur!=null;}
    private boolean isTitleImplemented() {return boutonTitle!=null;}
    private boolean isSubTitleImplemented() {return boutonSubTitle!=null;}
//    private boolean isFontSizeImplemented() {return menuTaille!=null;}

    public Bouton getBoutonBold() {return !isBoldImplemented() ? boutonBold=new Bouton(new ActionGras()) : boutonBold;}
    public Bouton getBoutonItalic() {return !isItalicImplemented() ? boutonItalic=new Bouton(new ActionItalic()) : boutonItalic;}
    public Bouton getBoutonUnderline() {return !isUnderlinedImplemented() ? boutonUnderline=new Bouton(new ActionSouligner()) : boutonUnderline;}
    public Bouton getBoutonStrike() {return !isStrikeImplemented() ? boutonStrike=new Bouton(new ActionBarrer()) : boutonStrike;}
    public Bouton getBoutonLeftAlined() {
        if(!isLeftAlignmentImplemented()) {
            Action left = new ActionAlignerGauche();
            addAlignementAction(left);
            boutonLeftAlined = new Bouton(left);
        }
        return boutonLeftAlined;
    }
    public Bouton getBoutonCenterAlined() {
        if(!isCenterAlignmentImplemented()) {
            Action center = new ActionAlignerCentre();
            addAlignementAction(center);
            boutonCenterAlined = new Bouton(center);
        }
        return boutonCenterAlined;
    }
    public Bouton getBoutonRightAlined() {
        if(!isRightAlignmentImplemented()) {
            Action right = new ActionAlignerDroite();
            addAlignementAction(right);
            boutonRightAlined = new Bouton(right);
        }
        return boutonRightAlined;
    }
    public Bouton getBoutonTitle() {return !isTitleImplemented() ? boutonTitle=new BoutonTitle() : boutonTitle;}
    public Bouton getBoutonSubTitle() {return !isSubTitleImplemented() ? boutonSubTitle=new BoutonSubTitle() : boutonSubTitle;}
    public ChoixCouleur getMenuCouleur() {return !isForegroundColorImplemented() ? menuCouleur=new ChoixCouleur() : menuCouleur;}
//    public ChoixTaillePolice getMenuTaille() {return !isFontSizeImplemented() ? menuTaille=new ChoixTaillePolice() : menuTaille;}

    private ActionGroup aligmentGroup = null;
    private void addAlignementAction(Action a) {
        if(aligmentGroup==null) {aligmentGroup = new ActionGroup();}
        aligmentGroup.add(a);
    }

    /**
     * Adapte l'état des boutons de style au style passé en paramètre.<br/>
     * Les ActionListener des JComboBox doivent être désactivées afin de ne
     * pas générer un faux appel à ActionPerformed() lorsqu'ils sont
     * modifiés.
     * @param astParagraphe L'AttributeSet correspondant au style du paragraphe considéré.
     * @param ast L'AttributeSet correspondant au style considéré.
     */
    public void updateBoutons(AttributeSet astParagraphe, AttributeSet ast) {

        // met à jour les boutons de base (gras, italique, souligner,
        // aligner à gauche et centrer
        boolean[] etat = {StyleConstants.isBold(ast), StyleConstants.isItalic(ast), StyleConstants.isUnderline(ast), StyleConstants.isStrikeThrough(ast),
            StyleConstants.getAlignment(astParagraphe) == StyleConstants.ALIGN_LEFT,
            StyleConstants.getAlignment(astParagraphe) == StyleConstants.ALIGN_CENTER,
            StyleConstants.getAlignment(astParagraphe) == StyleConstants.ALIGN_RIGHT};
        Bouton[] listeComposants = {boutonBold, boutonItalic, boutonUnderline, boutonStrike, boutonLeftAlined, boutonCenterAlined, boutonRightAlined};

        for (int i = 0; i < listeComposants.length; i++) {
            if(listeComposants[i]!=null) listeComposants[i].setSelected(etat[i]);
        }

        if(isForegroundColorImplemented()) {menuCouleur.setSelectedCouleur(StyleConstants.getForeground(ast));}
/*        if(isFontSizeImplemented()) {
//            Integer fontSize = (Integer)ast.getAttribute(JMathTextPane.FONTSIZE);
            menuTaille.setSelectedTaillePolice(StyleConstants.getFontSize(ast));//fontSize==null ? StyleConstants.getRTFFontSize(ast) : fontSize);
        }*/
    }

    public AttributeSet getStyleAttributes() {
        MutableAttributeSet attr = new SimpleAttributeSet();
        if(isBoldImplemented()) StyleConstants.setBold(attr, isBold());
        if(isItalicImplemented()) StyleConstants.setItalic(attr, isItalic());
        if(isUnderlinedImplemented()) StyleConstants.setUnderline(attr, isUnderlined());
        if(isStrikeImplemented()) {
            StyleConstants.setStrikeThrough(attr, isStroken());
            attr.addAttribute(STRIKE_COLOR_ATTRIBUTE, ColorManager.getRGBHexa(getForeground()));
        }
        if(isCenterAlignmentImplemented()) StyleConstants.setAlignment(attr, isCenterAlined() ? StyleConstants.ALIGN_CENTER : StyleConstants.ALIGN_LEFT);
        if(isForegroundColorImplemented()) StyleConstants.setForeground(attr, getForeground());
/*        if(isFontSizeImplemented()) {
            StyleConstants.setFontSize(attr, getRTFFontSize());
            attr.addAttribute(StyleConstants.FontSize, getRTFFontSize());
            
//            attr.addAttribute(CSS.Attribute.FONT_SIZE, getMenuTaille().getSelectedIndex()+4);
//            attr.addAttribute(JMathTextPane.FONTSIZE, getRTFFontSize());//cet attribut contient la fontsize vue par l'éditeur html
        }*/
        return attr;
    }
    
    private void validatePreviousEdits() {
        if(textEditor!=null && textEditor instanceof JMathTextPane) {
            CompositeUndoManager undo = ((JMathTextPane)textEditor).getUndo();
            if(undo!=null) {undo.valider();}
        }
    }


    private static final Icone[] REF_COULEURS;
    public static final Color[] COULEURS;
    static {
        String[] balises = IHM.getThemeElementBloc("color ink");
        REF_COULEURS = new Icone[balises.length];
        COULEURS = new Color[balises.length];
        for (int i = 0; i < balises.length; i++) {
            REF_COULEURS[i] = ImageManager.getIcone("icon " + balises[i], 40, 20);
            COULEURS[i] =ColorManager.get(balises[i]);
        }
    }
    /**
     * Cette classe permet de créer un menu déroulant permettant de sélectionner une couleur
     */
    public class ChoixCouleur extends MenuDeroulant {
        private final ActionListener actionCouleur = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color color = getSelectedCouleur();
                validatePreviousEdits();
                if(textEditor!=null) {
                    textEditor.setCaretColor(color);
                }
                (new HTMLEditorKit.ForegroundAction("couleur", color)).actionPerformed(null);
                validatePreviousEdits();
            }
        };
        public ChoixCouleur() {
            super(REF_COULEURS, "text color");
            setSelectedIndex(0);
            addActionListener(actionCouleur);
        }
        public Color getSelectedCouleur() {return COULEURS[getSelectedIndex()==-1 ? 0 : getSelectedIndex()];}
        public Color getCouleurAtIndex(int i) {try{return COULEURS[i];} catch(IndexOutOfBoundsException e) {return COULEURS[0];}}
        public void setSelectedCouleur(Color couleur) {
            removeActionListener(actionCouleur);
            super.setSelectedIndex(Arrays.asList(COULEURS).indexOf(couleur));
            textEditor.setCaretColor(couleur);
            addActionListener(actionCouleur);
        }
    }


    /** Tailles disponibles en unités HTML3 **/
//    public static final Integer[] TAILLES_DISPONIBLES = {4, 5, 6};
    /** Tailles disponibles en unités RTF (PT) **/
    public static final Integer[] TAILLES_PT = {14, 18, 24};/*new Integer[TAILLES_DISPONIBLES.length];
    static {
        for(int i = 0; i<TAILLES_DISPONIBLES.length; i++) {
            TAILLES_PT[i] = JMathTextPane.FONT_CONVERSION_PT[TAILLES_DISPONIBLES[i]];
        }
    }*/
//    /**
//     * Cette classe permet de créer un menu déroulant permettant de sélectionner une taille de police
//     */
//    public class ChoixTaillePolice extends MenuDeroulant {
//        /**
//         * Transforme un taille RTF (pt) en sa taille la plus proche gérée par l'éditeur
//         * @param t la taille RTF à étudier (en pt)
//         * @return la taille à utiliser pour l'éditeur (en pt)
//         */
//        public int closestTaille(int t) {
////            int min = Integer.MAX_VALUE, result = t;
////            for(int i : TAILLES_PT) {
////                int d = Math.abs(t-i);
////                if(d<min) {min = d; result = i;}
////            }
////            System.out.println("t : "+t+"   r : "+result);
//            for(int i : TAILLES_PT) { if(t<i) {return i;} }
//            return TAILLES_PT[TAILLES_PT.length-1];
//        }
//        private ActionListener actionTaille = new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//
//    //            MutableAttributeSet att = new SimpleAttributeSet();
//    //            StyleConstants.setFontSize(att, TAILLES_DISPONIBLES[taille.getSelectedIndex()]);
//    //            att.addAttribute(FONTSIZE, TAILLES_DISPONIBLES[taille.getSelectedIndex()]);
//    //            htmlDoc.setCharacterAttributes(Editeur.this.getSelectionStart(), Editeur.this.getSelectionEnd() - Editeur.this.getSelectionStart(), att, false);
//                if(textEditor!=null) e.setSource(textEditor);
//                (new HTMLEditorKit.FontSizeAction("taille",getRTFFontSize())).actionPerformed(e);
//            }
//        };
//        public ChoixTaillePolice() {
//            super(TAILLES_PT, "text size");
//            setSelectedIndex(0);
//            addActionListener(actionTaille);
//        }
//        public int getSelectedTaillePolice() {return TAILLES_PT[getSelectedIndex()<0?0:getSelectedIndex()];}
//        public int getTaillePoliceAtIndex(int i) {try{return TAILLES_PT[i];} catch(IndexOutOfBoundsException e) {return TAILLES_PT[0];}}
//        /** selectionne la taille de police la plus proche de la taille passée en paramètre en pt.
//         * Le listener de changement de taille n'est pas informé de ce changement manuel.
//         * @param taille taille en pt
//         */
//        public void setSelectedTaillePolice(int taille) {
//            removeActionListener(actionTaille);
//            super.setSelectedItem(closestTaille(taille));
//            addActionListener(actionTaille);
//        }
//    }

    /**
     * Cette classe permet de créer un bouton qui effectue gras dans l'éditeur ayant le focus
     */
    public class ActionGras extends ActionComplete.Toggle {
        public ActionGras() {super("text bold",false);}
        @Override
        public void actionPerformed(ActionEvent e) {
            validatePreviousEdits();
            new HTMLEditorKit.BoldAction().actionPerformed(e);
            validatePreviousEdits();
        }
    }

    /**
     * Cette classe permet de créer un bouton qui effectue italic dans l'éditeur ayant le focus
     */
    public class ActionItalic extends ActionComplete.Toggle {
        public ActionItalic() {super("text italic",false);}
        @Override
        public void actionPerformed(ActionEvent e) {
            validatePreviousEdits();
            new HTMLEditorKit.ItalicAction().actionPerformed(e);
            validatePreviousEdits();
        }
    }

    /**
     * Cette classe permet de créer un bouton qui effectue souligner dans l'éditeur ayant le focus
     */
    public class ActionSouligner extends ActionComplete.Toggle {
        public ActionSouligner() {super("text underline",false);}
        @Override
        public void actionPerformed(ActionEvent e) {
            validatePreviousEdits();
            new HTMLEditorKit.UnderlineAction().actionPerformed(e);
            validatePreviousEdits();
        }
    }

    /**
     * Cette classe permet de créer un bouton qui effectue souligner dans l'éditeur ayant le focus
     */
    public class ActionBarrer extends ActionComplete.Toggle {
        public ActionBarrer() {super("text strike",false);}
        @Override
        public void actionPerformed(ActionEvent e) {
            StyledTextAction actionBarrer = new StyledEditorKit.StyledTextAction("strike") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JEditorPane editor = getEditor(e);
                    if (editor != null) {
                        StyledEditorKit kit = getStyledEditorKit(editor);
                        MutableAttributeSet inputAttributes = kit.getInputAttributes();
                        boolean strike = (StyleConstants.isStrikeThrough(inputAttributes));
                        SimpleAttributeSet toApply = new SimpleAttributeSet();
                        StyleConstants.setStrikeThrough(toApply, !strike);
                        toApply.addAttribute(STRIKE_COLOR_ATTRIBUTE, ColorManager.getRGBHexa(editor.getCaretColor()));
                        setCharacterAttributes(editor, toApply, false);
                        
                        //Ce bloc permet de barrer les composants
                        for(int i=editor.getSelectionStart(); i<editor.getSelectionEnd(); i++) {
                            Component c = ((JMathTextPane)editor).getComponentAt(i);
                            if(c!=null && c instanceof ComposantTexte) {
                                ((ComposantTexte)c).setStrikeColor(editor.getCaretColor());
                                ((ComposantTexte)c).setStroken(!strike);
                            }
                        }
                    }
                }
            };
            validatePreviousEdits();
            actionBarrer.actionPerformed(e);
            validatePreviousEdits();
        }
    }

    /**
     * Cette classe permet de créer un bouton qui effectue un alignement à gauche dans l'éditeur ayant le focus
     */
    public class ActionAlignerGauche extends ActionComplete.Toggle {
        public ActionAlignerGauche() {super("text left align",true);}
        @Override
        public void actionPerformed(ActionEvent e) {
            validatePreviousEdits();
            new HTMLEditorKit.AlignmentAction("left",0).actionPerformed(e);
            validatePreviousEdits();
        }
    }
 
    /**
     * Cette classe permet de créer un bouton qui effectue un centrage du texte dans l'éditeur ayant le focus
     */
    public class ActionAlignerCentre extends ActionComplete.Toggle {
        public ActionAlignerCentre() {super("text center align",false);}
        @Override
        public void actionPerformed(ActionEvent e) {
            validatePreviousEdits();
            new HTMLEditorKit.AlignmentAction("center",1).actionPerformed(e);
            validatePreviousEdits();
        }
    }
 
    /**
     * Cette classe permet de créer un bouton qui effectue un aligment à droite du texte dans l'éditeur ayant le focus
     */
    public class ActionAlignerDroite extends ActionComplete.Toggle {
        public ActionAlignerDroite() {super("text right align",false);}
        @Override
        public void actionPerformed(ActionEvent e) {
            validatePreviousEdits();
            new HTMLEditorKit.AlignmentAction("right",2).actionPerformed(e);
            validatePreviousEdits();
        }
    }
 
    /**
     * Cette classe permet de créer un bouton qui crée un titre dans l'éditeur ayant le focus
     */
    public class BoutonTitle extends Bouton {
        public BoutonTitle() {super(new ActionComplete("text title button") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textEditor==null || !(textEditor instanceof Editeur)) {return;}
                String s = JOptionPane.showInputDialog(textEditor, Traducteur.traduire("dialog title rename"));
                if(s==null || s.isEmpty()) {return;}
                ((Editeur)textEditor).insererLabel(new JLabelText(s, 24, textEditor.getCaretColor()/*ColorManager.get("color text title")*/, true, true));
            }
        });}
    }

    /**
     * Cette classe permet de créer un bouton qui crée un sous-titre dans l'éditeur ayant le focus
     */
    public class BoutonSubTitle extends Bouton {
        public BoutonSubTitle() {super(new ActionComplete("text subtitle button") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textEditor==null || !(textEditor instanceof Editeur)) {return;}
                String s = JOptionPane.showInputDialog(textEditor, Traducteur.traduire("dialog title rename"));
                if(s==null || s.isEmpty()) {return;}
                ((Editeur)textEditor).insererLabel(new JLabelText(s, 18, textEditor.getCaretColor()/*ColorManager.get("color text subtitle")*/, true, false));
            }
        });}
    }
}
