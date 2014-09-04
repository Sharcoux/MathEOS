/*
 * «Copyright 2011 François Billioud, Guillaume Varoquaux»
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
package matheos.operations;

import matheos.utils.interfaces.Undoable;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.Traducteur;
import matheos.utils.objets.Navigation;
import matheos.utils.texte.LimitedTextField;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author François Billioud
 */
public abstract class OperationType extends JPanel implements ActionListener, ComponentListener, MouseListener, Serializable, Undoable {

    private static final long serialVersionUID = 1L;
    public int dateCreation = 0;// JourMoisAnnée du jour de création du Panel
    public int timeCreation = 0; // HeureMinuteSeconde de la création du Panel
    public static float coef = 1;    // Proportionnalité par rapport à la taille de la fenêtre
    public static int x0 = 0; // Constante qui permet de centrer l'opération sur la longueur
    protected long idTP = 0; //id du TP correspondant à l'opération insérée dans le cours

    public static enum Operation {

        addition(Addition.class), soustraction(Soustraction.class), division(Division.class), multiplication(Multiplication.class);
        private Class classe;

        Operation(Class c) {
            classe = c;
        }

        public Class getClasse() {
            return classe;
        }
    }

    public OperationType() {
        // On repère la date de création du Panel
        Calendar date = GregorianCalendar.getInstance();
        dateCreation = date.get(Calendar.DAY_OF_MONTH) * 1000000 + (date.get(Calendar.MONTH) + 1) * 10000 + date.get(Calendar.YEAR);
        timeCreation = date.get(Calendar.HOUR_OF_DAY) * 10000 + date.get(Calendar.MINUTE) * 100 + date.get(Calendar.SECOND);
    }

    public long getIdTP() {
        return idTP;
    }

    public void setIdTP(long idTP) {
        this.idTP = idTP;
        //HACK à supprimer après lar refonte des opérations
        setModified(true);//permet d'afficher le bouton update
    }

    public abstract OperationType nouveau();

    public abstract JPanel impression();

    /**
     * Méthode qui calcul les valeurs des coefficients coef et x0
     * en fonction de la taille de la fenêtre.
     */
    public void CalculCoef() {
        float l = super.getSize().width;
        float h = super.getSize().height;

        coef = 1;
        if (super.getSize().width >= 784) {
            l = 784;
        }
        if ((super.getSize().height) >= 657) {
            h = 657;
        }

        if (l < 784 || h < 657) // On définit le coef de proportionnalité
        {
            l = l / 784;
            h = h / 657;
            if (l < h) {
                coef = l;
                x0 = (int) ((super.getSize().width - 784 * coef) / 2);
            } else {
                coef = h;
                x0 = (int) ((super.getSize().width - 784 * coef) / 2);
            }
        } else {
            x0 = (int) ((super.getSize().width - l) / 2);
        }
    }

    /**
     * Méthode permettant de prendre uen capture d'écran d'un JPanel donné selon des dimensions (x0, y0, x1, y1)
     * et de stocker l'image dans un fichier.
     * @param panneau Le JPanel à copier.
     * @param fichier Le fichier dans lequel sera sauvegardé l'image du JPanel.
     * @param x0 Le point de départ sur l'axe x de la capture d'écran
     * @param y0 Le point de départ sur l'axe y de la capture d'écran
     * @param x1 Le point de fin sur l'axe x de la capture d'écran
     * @param y1 Le point de fin sur l'axe y de la capture d'écran
     * @throws IOException
     */
    public BufferedImage sauverJPanelDansFileSelonZone() {
        int X0 = tailleX0(), x1 = tailleX1(), y0 = tailleY0(), y1 = tailleY1();
        JPanel panneau = impression();

        if (panneau.getSize().width != 0 && panneau.getSize().height != 0) {
            BufferedImage tamponSauvegarde = new BufferedImage(x1 - X0, y1 - y0, BufferedImage.TYPE_3BYTE_BGR);
            Graphics g = tamponSauvegarde.createGraphics(); //On crée un Graphic que l'on insère dans tamponSaueagarde
            g.setColor(Color.WHITE);
            g.translate(-X0, -y0);
            panneau.paint(g);
            //ImageIO.write(tamponSauvegarde, "JPG", fichier);
            return tamponSauvegarde;
        }
        return null;
    }
    
//    private void writeObject(ObjectOutputStream out) throws IOException {
//        UndoManager m = undo;
//        undo = null;
//        out.defaultWriteObject();
//        undo = m;
//    }
//
//    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//        undo = new UndoManager();
//        in.defaultReadObject();
//    }

    /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en x du début de l'opération
     */
    public abstract int tailleX0();

    /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en y du début de l'opération
     */
    public abstract int tailleY0();

    /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en x de la fin de l'opération
     */
    public abstract int tailleX1();

    /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en y de la fin de l'opération
     */
    public abstract int tailleY1();

    protected abstract void positionComponent();
    
    public void annuler() {
//        if(undo.canUndo()) {undo.undo();}
    }
    public void refaire() {
//        if(undo.canRedo()) {undo.redo();}
    }

    public boolean peutAnnuler() {
        return false;//undo.canUndo();
    }
    public boolean peutRefaire() {
        return false;//undo.canRedo();
    }

    private boolean modified = false;
    public boolean hasBeenModified() {
//        return modified;
        return true;
    }
    public void setModified(boolean b) {
            firePropertyChange(Undoable.MODIFIED, !b, true);
//        if(modified!=b) {
//            modified = b;
//            firePropertyChange(Undoable.MODIFIED, !b, b);
//        }
    }
    
    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        setBackground(ColorManager.get(b ? "color panel operation" : "color disabled"));
    }
    
    private Navigation navigation = new Navigation();
//    private transient UndoManager undo = new UndoManager();
//    private class SelectAllListener extends KeyAdapter implements Serializable {
//        private static final long serialVersionUID = 1L;
//        @Override
//        public void keyReleased(KeyEvent e) {
//            if(e.getComponent() instanceof JTextField){
//                JTextField text = (JTextField) e.getComponent();
//                text.selectAll();
//            }
//        }
//    };
//    private class FocusSelectAllListener extends FocusAdapter implements Serializable {
//        private static final long serialVersionUID = 1L;
//        @Override
//        public void focusGained(FocusEvent e) {
//            if(e.getComponent() instanceof LimitedTextFieldOperations){
//                LimitedTextFieldOperations text = (LimitedTextFieldOperations)e.getComponent();
//                text.selectAll();
//                setModified(true);
//            }
//        }
//    };

    protected class LimitedTextFieldOperations extends LimitedTextField /*JTextField*/ implements Serializable {
        private static final long serialVersionUID = 1L;
//        private int max;
        protected LimitedTextFieldOperations(int taille) {
            super(taille);
//            max = taille;
            navigation.addComponent(this);
//            getDocument().addUndoableEditListener(undo);
//            addFocusListener(new FocusSelectAllListener());
//            if(max==1) {
//                ((AbstractDocument)getDocument()).setDocumentFilter(new ChiffreFieldFilter());
//                addKeyListener(new SelectAllListener());
//            } else {
//                ((AbstractDocument)getDocument()).setDocumentFilter(new OperationFieldFilter(max));
//            }
        }
        
//        private void writeObject(ObjectOutputStream out) throws IOException {
//            getDocument().removeUndoableEditListener(undo);
//            writeObject(out);//out.defaultWriteObject();
//            getDocument().addUndoableEditListener(undo);
//        }
        
//        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//            in.defaultReadObject();
//            getDocument().addUndoableEditListener(undo);
//        }
        /**
         * Méthode qui permet de tester si le nombre saisi dans le champ de texte est bien un décimal.
         * @return true si le nombre est bien un entier décimal; false sinon.
         */
        public boolean testDecimal() {
            String str = this.getText();
            try {
                Double.parseDouble(str.replace(',', '.'));
                return true;
            } catch(NumberFormatException e) {
                JOptionPane.showMessageDialog(this.getParent(), Traducteur.traduire("not decimal"), "", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

//        @Override
//        public void annuler() {
//            OperationType.this.annuler();
//        }
//
//        @Override
//        public void refaire() {
//            OperationType.this.refaire();
//        }
//
//        @Override
//        public boolean peutAnnuler() {
//            return OperationType.this.peutAnnuler();
//        }
//
//        @Override
//        public boolean peutRefaire() {
//            return OperationType.this.peutRefaire();
//        }
//
//        @Override
//        public boolean hasBeenModified() {
//            return OperationType.this.hasBeenModified();
//        }
//
//        @Override
//        public void setModified(boolean b) {
//            OperationType.this.setModified(b);
//        }
        
//        private class OperationFieldFilter extends DocumentFilter implements Serializable {
//            private static final long serialVersionUID = 1L;
//            private int max;
//            OperationFieldFilter(int max) {
//                this.max = max;
//            }
//            @Override
//            public void insertString(DocumentFilter.FilterBypass fb, int offset, String str, AttributeSet attr) throws BadLocationException {
//                replace(fb, 0, fb.getDocument().getLength(), str, attr);
//            }
//            @Override
//            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String str, AttributeSet attrs) throws BadLocationException {
//                int newLength = fb.getDocument().getLength() - length + str.length();
//                if (newLength <= max) {
//                    fb.replace(offset, length, str, attrs);
//                    setModified(true);
//                } else {
//                    Toolkit.getDefaultToolkit().beep();
//                }
//            }
//        };
//        private class ChiffreFieldFilter extends OperationFieldFilter implements Serializable {
//            private static final long serialVersionUID = 1L;
//            ChiffreFieldFilter() {super(1);}
//            @Override
//            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String str, AttributeSet attrs) throws BadLocationException {
//                int newLength = str.length();
//                if (newLength <= 1) {
//                    fb.replace(0, fb.getDocument().getLength(), str, attrs);
//                    setModified(true);
//                } else {
//                    Toolkit.getDefaultToolkit().beep();
//                }
//            }
//        }
    }
}
