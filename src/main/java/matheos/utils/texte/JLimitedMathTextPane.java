/** «Copyright 2012,2013 François Billioud, Guillaume Varoquaux»
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

import matheos.utils.librairies.DimensionTools.DimensionT;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.StyledEditorKit.AlignmentAction;

import net.sourceforge.jeuclid.swing.JMathComponent;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.LaFFixManager;
import java.awt.Component;
import java.awt.Container;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;

import javax.swing.text.html.HTMLEditorKit;
import matheos.table.OngletTable;
import matheos.table.OngletTableLayout;


/**
 * Cette classe permet de contraindre le nombre de lignes maximales autorisées
 * (1 par défaut) pour le JMathTextPane, ainsi que le nombre maximale de
 * caractères contenues (illimité par défaut). Ele permet également de définir
 * une redimension automatique du JMathTextPane en fonction de l'évolution de
 * son contenu (redimensionnemetn automatique par défaut)
 *
 * @author François Billioud, Guillaume Varoquaux
 */
@SuppressWarnings("serial")
public class JLimitedMathTextPane extends JMathTextPane implements LaFFixManager.BackgroundTrouble {

    private static final String EOL = "\n";
    private static final Color BACKGROUND_COLOR = ColorManager.get("color element background"); //Couleur de fond du JLimitedMathTextPane

    private int maxLines = 1; // Nombre de ligne maximum du JLimitedMathTextPane
    private boolean adaptableSize = true; // Définir si le JTextPane est affecté par un redimensionnemetn automatique (true) ou non (false)
    private int longueurMax = -1; // Nombre de caractère maximum autorisé


    public JLimitedMathTextPane() {
        super();
        htmlDoc.setDocumentFilter(new LimitedTextFiltre());
//        this.addContainerListener(this);//rien ne sert d'écouter cette partie de cette façon. Il vaut mieux se placer à la fin des appels à insertComponent
    }

    public JLimitedMathTextPane(int nbMaxLigne) {
        this();
        this.maxLines = nbMaxLigne;
    }

    public JLimitedMathTextPane(int nbMaxLigne, int longueurMax) {
        this(nbMaxLigne);
        this.longueurMax = longueurMax;
    }

    public JLimitedMathTextPane(boolean adaptableSize) {
        this();
        this.adaptableSize = adaptableSize;
    }

    public JLimitedMathTextPane(int nbMaxLigne, boolean adaptableSize) {
        this(nbMaxLigne);
        this.adaptableSize = adaptableSize;
    }

    /**
     * @param maxLines the maxLines to set
     */
    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    /**
     * Permet de récupérer le nombre maximale de lignes autorisées pour ce
     * JTextPane
     *
     * @return Le nombre maximale de lignes autorisées.
     */
    public int getMaxLines() {
        return maxLines;
    }

    /**
     * Détermine si ce champ a la propriété d'adapter ses dimensions à son
     * contenu.
     *
     * @return true si ce champ s'adapte à son contenu; false sinon.
     */
    public boolean isAdaptableSize() {
        return adaptableSize;
    }

    /**
     * Ajoute au champ la propriété d'adapter ses dimensions à son contenu.
     *
     * @param adaptableSize true si ce champ s'adapte à son contenu; false
     * sinon.
     */
    public void setAdaptableSize(boolean adaptableSize) {
        this.adaptableSize = adaptableSize;
    }

    /**
     * Détermine si ce champ est limité en nombre de caractère ou non.
     *
     * @return true si ce champs a une longueur maximale; false sinon.
     */
    public boolean isLimitedLength() {
        return longueurMax>=0;
    }

    /**
     * Détermine le nombre maximum de caractères autorisés dans ce JTextPane si
     * celui-ci est limité en nombre (isLimitedLength == true)
     *
     * @return le nombre maximum de caractères autorisés dans ce JTextPane.
     */
    public int getLongueurMax() {
        return longueurMax;
    }

    /**
     * Permet de limiter le nombre de caractère contenu dans le JTextPane.
     *
     * @param longueurMax le nombre maximale de caractères autorisés.
     */
    public void setLongueurMax(int longueurMax) {
        this.longueurMax = longueurMax;
    }

    /**
     * Méthode permettant d'initialiser le style du document.
     */
    public void resetStyle() {
        if(editeurKit!=null) {getEditeurKit().reset();}
        undo.discardAllEdits();
        repaint();
    }

    /**
     * Permet de donner une propriété de centrage du texte au JTextPane. Le
     * texte se centre par rapport à la largeur du JTextPane.
     */
    public void setAlignmentCenter(Boolean b) {
        htmlDoc.removeUndoableEditListener(undo);
        AlignmentAction alignementCentre = new HTMLEditorKit.AlignmentAction(b ? "center" : "left", 1);
        alignementCentre.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
        htmlDoc.addUndoableEditListener(undo);
    }

    /**
     * Surcharge de la méthode d'insertion des composants pour gérer le redimensionnement
     * lors de leur insertion
     * @param mathComponent le composant à insérer.
     */
    @Override
    public void insererJMathComponent(final JMathComponent mathComponent) {
        super.insererJMathComponent(mathComponent);
        //redimensionner en cas de changement de l'alignement des MathComponents
        mathComponent.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                mathComponent.removeComponentListener(this);
                mathComponent.setSize(mathComponent.getPreferredSize());
                mathComponent.addComponentListener(this);
                mathComponent.repaint();
                dimensionner();
            }
        });
    }
    
    @Override
    protected void insertComponent(Component c, AttributeSet attr, long id, String type) {
        super.insertComponent(c, attr, id, type);
        if(adaptableSize) dimensionner();
//        if(adaptableSize) revalidate();
    }

    /**
     * Méthode permettant d'adapter les dimensions du JTextPane à son contenu.
     */
    public void dimensionner() {//TODO refaire en utilisant repaint et getPreferredSize uniquement
        Dimension min = getMinimumSize(), pref = getPreferredSize();
        Dimension size = new DimensionT(pref).max(min);
        setSize(size);
    }
    
    DimensionT fixedDimension;
    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
        fixedDimension = new DimensionT(d);
    }
    
    @Override
    public DimensionT getPreferredSize() {
        if(!adaptableSize && fixedDimension!=null) {return fixedDimension;}
//        if(!isAdaptableSize()) {return super.getPreferredSize();}
//        FontMetrics fm = getFontMetrics(getFont());
        DimensionT d = new DimensionT(getContentSize());
//        d.width+=fm.charWidth('a');
        //HACK pour calculer correctement la taille du composant avec ou sans bordure
        if(getBorder()==null) {return d.plus(getMargin());}
        if(getBorder().getBorderInsets(this).equals(new Insets(0,0,0,0))) {return d.plus(getMargin());}
        return d.plus(getBorder().getBorderInsets(this));
    }
    @Override
    public DimensionT getMinimumSize() {
//        if(!isAdaptableSize()) {return super.getMinimumSize();}
        FontMetrics fm = getFontMetrics(getFont());
        return new DimensionT(fm.getMaxAdvance(), fm.getHeight());
    }
    
    /**
     * Méthode retournant si le texte sélectionné dans un JMathTextPane n'est
     * pas trop long pour être ajouté dans un JLimitedMathTextPane
     *
     * @param txt le JMathTextPane contenant la sélection
     * @return true si la sélection est trop longue pour entrer dans un
     * JLimitedMathTextPane; false sinon
     */
    public static boolean isContentTooLong(JMathTextPane txt) {//TODO : A revoir complètement
        if (txt.getSelectedText() == null) {
            return false;
        } else {
            int largeurMax = txt.getParent().getWidth()-20;
            try {
                return txt.getStringWidth(txt.getSelectionStart(), txt.getSelectionEnd()) >= largeurMax;
    //            return MathTools.getStringWidth(txt, txt.getSelectionStart(), txt.getSelectionEnd(), FONT.deriveFont(FONT_SIZE_MAX)) >= largeurMax;
            } catch (BadLocationException ex) {
                Logger.getLogger(JLimitedMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
    }

    /**
     * Méthode retournant si le texte sélectionné dans un JMathTextPane n'est
     * pas trop long pour être ajouté dans un JLimitedMathTextPane
     *
     * @param txt le JMathTextPane contenant la sélection
     * @param enterAsSeparator true si les "entrée" créent des nouvelles lignes; 
     * false si on doit tout compter en une seule ligne
     * @return true si la sélection est trop longue pour entrer dans un
     * JLimitedMathTextPane; false sinon
     */
    public static boolean isContentTooLong(JMathTextPane text, boolean enterAsSeparator) {//A revoir completement
        if(!enterAsSeparator){
            return isContentTooLong(text);
        }
        if (text.getSelectedText() == null) {
            return false;
        } else {
            String[] chaines = text.getSelectedText().split(EOL);
            int index = text.getSelectionStart();
            for(String contenu : chaines){
                int largeurMax = text.getParent().getWidth()-10;
                try {
    //                if(MathTools.getStringWidth(text, text.getSelectionStart(), text.getSelectionEnd(), FONT.deriveFont(FONT_SIZE_MAX)) >= largeurMax){
                    if(text.getStringWidth(text.getSelectionStart(), text.getSelectionEnd()) >= largeurMax){
                        return true;
                    }else{
                        index = index + contenu.length() + 1;
                    }
                } catch (BadLocationException ex) {
                    Logger.getLogger(JLimitedMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return false;
        }
    }

    /**
     * Méthode retournant le nombre de lignes contenu dans le
     * JLimitedMathTextPane
     *
     * @return le nombre totale de lignes saisies par l'utilisateur
     */
    private int getNombreDeLignes() {
        return getLignes().length;
    }

    /**
     * Méthode retournant le tableau ligne par ligne du
     * JLimitedMathTextPane
     *
     * @return le tableau des lignes sous forme de String
     */
    private String[] getLignes() {
        return getText().split(EOL);
    }

//    private TextChangedEvent creerDialogueEvent(DocumentEvent.EventType eventType, int offset, int lenght, Component component) {
//        return new TextChangedEvent(this, eventType, offset, lenght, component);
//    }
//
//    public void addTextChangedListener(TextChangedListener listener) {
//        listenerList.add(TextChangedListener.class, listener);
//    }
//
//    public void removeTextChangedListener(TextChangedListener listener) {
//        listenerList.remove(TextChangedListener.class, listener);
//    }
//
//    protected void fireTextChangedReponse(TextChangedEvent event) {
//        for (TextChangedListener l : listenerList.getListeners(TextChangedListener.class)) {
//            l.textChanged(event);
//        }
//    }

    private boolean peutForcerLigne = false;
    private boolean forcageEnCours = false;
    private KeyListener forcingKeyListener;
    public void setForcageLigne(boolean b) {
        peutForcerLigne = b;
        if(b) {
            forcingKeyListener = new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if(KeyEvent.VK_ENTER==e.getKeyCode() && e.isShiftDown()) {
                        forcageEnCours=true;
                    }
                }
            };
            addKeyListener(forcingKeyListener);
        } else {
            removeKeyListener(forcingKeyListener);
        }
    }
    
    /**
     * Filtre gérant le nombre maximale de caractères autorisés dans ce
     * JTextPane (si isLimitedLength == true) et le nombre maximale de lignes
     * autorisées.
     */
    public class LimitedTextFiltre extends JMathTextPane.Filtre {

        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            String str = string;
            
            // Vérification de la longueur max autorisée
            if (JLimitedMathTextPane.this.isLimitedLength()) {
                int newLength = JLimitedMathTextPane.this.getLength() + str.length();
                int maxLength = JLimitedMathTextPane.this.getLongueurMax();
                if (newLength > maxLength) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }

            //Si on appuie sur enter alors qu'on a atteint le nombre maximum de lignes, on laisse tomber
            if(str.equals(EOL) && getNombreDeLignes()==getMaxLines() && !forcageEnCours) {
                if(getMaxLines()==1) {
//                    KeyboardFocusManager.getCurrentKeyboardFocusManager().downFocusCycle();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
                return;
            }
            
            //S'il y a un "Enter" et qu'on est arrivé au nombre maximum de lignes, on remplacer le "Enter" par un espace
            if (str.contains(EOL)) {
                if (JLimitedMathTextPane.this.getNombreDeLignes() >= maxLines) {
                    if(forcageEnCours) {forcageEnCours=false;} else {str = str.replaceAll(EOL, " ");}
                }
            }
            //On remplace les tabulations par des espaces
            if (str.contains("\t")) {
                str = str.replaceAll("\t", " ");
            }
            super.insertString(fb, offset, str, attr);
            if(adaptableSize) dimensionner();
//            if(adaptableSize) revalidate();
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String str, AttributeSet attrs) throws BadLocationException {
            if (str.equals("²") && length != 0) {
                return;//on traite ce cas avec les keyboardListeners
            }

            super.replace(fb, offset, length, str, attrs);
        }
        
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
                if(adaptableSize) dimensionner();
//                if(adaptableSize) revalidate();
        }
    }

}
