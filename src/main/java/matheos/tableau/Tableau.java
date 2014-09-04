/*
 * «Copyright 2011 Tristan Coulange»
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
 */
package matheos.tableau;

import static matheos.tableau.TableConstants.*;

import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import matheos.elements.ChangeModeListener;
import matheos.tableau.UndoManagerTableau.ModeleTableauEdit;
import matheos.utils.interfaces.Editable;
import matheos.utils.managers.FontManager;
import matheos.utils.objets.MenuContextuel;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.texte.EditeurIO;
import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.texte.JMathTextPane;

import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Classe qui représente le tableau. Il est créé à partir de modeleTableau.
 *
 * @author Tristan
 */
@SuppressWarnings("serial")
public class Tableau extends JTable implements Editable {

    private ModeleTableau modeleTableau;
    private TableauRenderer tableauRenderer;
    private TableauEditor editor;
    private TableauEditor separateEditor;
    private ControlleurTableau controlleur;
    private static final Font POLICE = FontManager.get("font table");
    private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    /**
     * Crée le tableau à partir du modeleTableau, puis la matrice
     * tableauCouleurs qui contient les couleurs de chaque cellule. Crée editor
     * qui permettra de sélectionner tout le contenu d'une cellule en double
     * cliquant.
     *
     * @param nbreLignes
     * @param nbreColonnes
     */
    public Tableau(ControlleurTableau controlleur, int nbreLignes, int nbreColonnes) {
        super(nbreLignes, nbreColonnes);
        this.setFont(POLICE);
        this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        modeleTableau = new ModeleTableau(nbreLignes, nbreColonnes);
        setModeleTableau(controlleur, modeleTableau);
        editor = TableauEditor.createTableauEditor(TableauEditor.TYPE_EDITOR.NORMAL, controlleur);
        editor.addCellEditorListener(controlleur);
        separateEditor = TableauEditor.createTableauEditor(TableauEditor.TYPE_EDITOR.SEPARATE, controlleur);
        separateEditor.addCellEditorListener(controlleur);
        tableauRenderer = new TableauRenderer(controlleur);
        this.controlleur = controlleur;

        this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(true);

        addMouseListener(new ChangeModeListener(ChangeModeListener.TP));

        addFocusListener(new ActiveCopierCollerListener());
        addKeyListener(new TransmitToEditorKeyListener());
        editor.getComponent().addKeyListener(new TransmitFromEditor());
        addMouseListener(new PopUpMenuMouseListener());

        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copier");
        this.getActionMap().put("copier", new ActionCopier("copier"));
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), "coller");
        this.getActionMap().put("coller", new ActionColler("coller"));
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), "couper");
        this.getActionMap().put("couper", new ActionCouper("couper"));
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "editer");
        this.getActionMap().put("editer", new ActionCommencerEdition("editer"));
    }

    public final void setModeleTableau(ControlleurTableau controlleur, ModeleTableau modeleTableau) {
        this.modeleTableau = modeleTableau;
        modeleTableau.addTableModelListener(controlleur);
        setModel(modeleTableau);
    }

    @Override
    public ModeleTableau getModel() {
        return modeleTableau;
    }

    public FIRST_CASE getFirstCase() {
        return modeleTableau.getFirstCase();
    }

    public void setFirstCase(FIRST_CASE firstCase) {
        modeleTableau.setFirstCase(firstCase);
    }

    /**
     * Adapte les dimensions du tableau: les largeurs de colonnes, la hauteur
     * des cellules, la taille de la police, position du tableau... en fonction
     * du coef, qui est lui-même une fonction du contenu du tableau.
     *
     * @param coef
     */
    public void adapterDimensions(double coef) {
        double correction = 0;
        //Correctif pour éviter d'avoir "..." dans les cases quand le coef se réduit
        //On réduit la Font afin de maintenir une majoration sur la largeur normale qui permet
        //de calculer un coef majoré, et donc d'éviter ces "..."
        if (coef != 1) {
            correction = 2 * coef;
        }
        this.setFont(this.getFont().deriveFont((float) (FONT_SIZE_TABLEAU * coef - correction)));
//        FontMetrics fm = this.getFontMetrics(this.getFont());
        int[] largeurMax = new int[this.getColumnCount()];
        for (int i = 0; i < this.getRowCount(); i++) {
            int hauteurMax = 0;

            for (int j = 0; j < this.getColumnCount(); j++) {
                int largeur = calculerLargeurCellule(i, j);
                //Correctif pour éviter d'avoir "..." dans les cases quand le coef se réduit
                int largeur2 = 0;
                if (this.getValueAt(i, j) instanceof ModeleCellule) {
                    ModeleCellule modeleCellule = ((ModeleCellule) this.getValueAt(i, j));
                    if (modeleCellule.getRenderer() == null) {
                        //HACK en attendant que le modèle ne soit plus représenté par un JLabel mais par un JMathTextPane
                        JMathTextPane jtp = new JLimitedMathTextPane();
                        jtp.charger(modeleCellule.getContent());
                        largeur2 = jtp.getPreferredWidth()+3;//getMaxStringWidth(modeleCellule.getContent().contenuHTML, fm) + 3;
                    }
                }
                largeur = Math.max((int) (largeur * coef), largeur2);
                int hauteur = calculerHauteurCellule(i, j);
                if (largeur > largeurMax[j]) {
                    largeurMax[j] = largeur;
                }
                if (hauteur > hauteurMax) {
                    hauteurMax = hauteur;
                }
            }
            hauteurMax = (int) (Math.max(hauteurMax, HAUTEUR_MIN_CELLULE) * coef);
            this.setRowHeight(i, hauteurMax);
        }
        for (int j = 0; j < this.getColumnCount(); j++) {
            largeurMax[j] = (int) (Math.max(largeurMax[j], LARGEUR_MIN_CELLULE * coef));
            this.getColumnModel().getColumn(j).setPreferredWidth(largeurMax[j]);
        }
        revalidate();
        repaint();
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if (row == 0 && column == 0 && getFirstCase().equals(FIRST_CASE.SEPARATE)) {
            return separateEditor;
        }
        return editor;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return tableauRenderer;
    }

    /**
     * Calcule la largeur "normale" du tableau en fontion du contenu de façon à
     * déterminer le coefficient par lequel les dimensions doivent être
     * multipliées.
     *
     * @return
     */
    public int calculerLargeurNormaliseeTableau() {
        int largeurTableau = 0;

        for (int i = 0; i < this.getColumnCount(); i++) {
            int max = 0;

            for (int j = 0; j < this.getRowCount(); j++) {
                int taille = calculerLargeurCellule(j, i);
                if (taille > max) {
                    max = taille;
                }
            }
            max = Math.max(max, LARGEUR_MIN_CELLULE);
            largeurTableau = largeurTableau + max;
        }

        return largeurTableau;
    }

    /**
     * Calcule la hauteur "normale" du tableau de façon à déterminer le
     * coefficient par lequel les dimensions doivent être multipliées.
     *
     * @return
     */
    public int calculerHauteurNormaliseeTableau() {
        int hauteurTableau = 0;
        for (int i = 0; i < this.getRowCount(); i++) {
            int max = 0;

            for (int j = 0; j < this.getColumnCount(); j++) {
                int hauteur = calculerHauteurCellule(i, j);
                if (hauteur > max) {
                    max = hauteur;
                }
            }
            max = Math.max(max, (int) (HAUTEUR_MIN_CELLULE));
            hauteurTableau = hauteurTableau + max;
        }
        return hauteurTableau;
    }

    public int calculerLargeurColonne(int column) {
        int[] tailleCellules = new int[getRowCount()];
        for (int row = 0; row < tailleCellules.length; row++) {
            tailleCellules[row] = calculerLargeurCellule(row, column);
        }
        Arrays.sort(tailleCellules);
        return tailleCellules[getRowCount() - 1];
    }

    public int calculerLargeurLigne(int row) {
        int[] tailleCellules = new int[getColumnCount()];
        for (int column = 0; column < tailleCellules.length; column++) {
            tailleCellules[column] = calculerHauteurCellule(row, column);
        }
        Arrays.sort(tailleCellules);
        return tailleCellules[getColumnCount() - 1];
    }

    /**
     * Calcul la largeur d'une cellule
     *
     * @param row l'entier désignant la ligne de la cellule en question
     * @param colum l'entier désignant la colonne de la cellule en question
     * @return la largeur de la cellule
     */
    public int calculerLargeurCellule(int row, int column) {
        int taille = 0;
        Object value = this.getValueAt(row, column);
        if (value instanceof ModeleCellule) {
            ModeleCellule cellule = (ModeleCellule) value;
            if (cellule.getRenderer() != null) {
                taille = cellule.getRenderer().getWidth();
            } else {
//                FontMetrics fm = getFontMetrics(POLICE);
                //taille = fm.stringWidth(cellule.getContent().toStringContent());
                //HACK en attendant que le modèle ne soit plus représenté par un JLabel mais par un JMathTextPane
                JMathTextPane jtp = new JLimitedMathTextPane();
//                EditeurIO.read(jtp, cellule.getContent());
                taille = jtp.getPreferredWidth();//getMaxStringWidth(cellule.getContent().contenuHTML, fm);
            }
        } else if (this.getValueAt(row, column) instanceof String) {
            FontMetrics fm = getFontMetrics(POLICE);
            taille = fm.stringWidth((String) value);
        }
        if (row == 0 && column == 0 && getFirstCase().equals(FIRST_CASE.SEPARATE)) {
            taille = Math.max(taille, LARGEUR_CELLULE_SEPAREE);
        }
        taille = taille + 1;
        return Math.max(taille, LARGEUR_MIN_CELLULE);
    }

    /**
     * Calcul la hauteur d'une cellule
     *
     * @param row l'entier désignant la ligne de la cellule en question
     * @param colum l'entier désignant la colonne de la cellule en question
     * @return la hauteur de la cellule
     */
    public int calculerHauteurCellule(int row, int column) {
        int taille = 0;
        Object value = this.getValueAt(row, column);
        if (value instanceof ModeleCellule) {
            ModeleCellule cellule = (ModeleCellule) value;
            if (cellule.getRenderer() != null) {
                taille = cellule.getRenderer().getHeight();
            } else {
//                FontMetrics fm = getFontMetrics(POLICE);
                //taille = fm.getHeight();
                JMathTextPane jtp = new JLimitedMathTextPane();
//                EditeurIO.read(jtp, cellule.getContent());
                taille = jtp.getPreferredHeight();//getMaxStringHeight(cellule.getContent().contenuHTML, fm);
            }
        } else if (value instanceof String) {
            FontMetrics fm = getFontMetrics(POLICE);
            taille = fm.getHeight();
        }
        if (row == 0 && column == 0 && getFirstCase().equals(FIRST_CASE.SEPARATE)) {
            taille = Math.max(taille, HAUTEUR_CELLULE_SEPAREE);
        }
        taille = taille + 1;
        return Math.max(taille, HAUTEUR_MIN_CELLULE);
    }
    
    private int getMaxStringWidth(String chaine, FontMetrics fm) {
        //HACK
        JMathTextPane jtp = new JLimitedMathTextPane();
        jtp.setText(chaine);
        chaine = jtp.getText();
        String[] chaines = chaine.split("\n");
        int[] longueurs = new int[chaines.length];
        for (int i = 0; i < chaines.length; i++) {
            longueurs[i] = fm.stringWidth(chaines[i]);
        }
        Arrays.sort(longueurs);
        if (chaines.length > 0) {
            return longueurs[chaines.length - 1];
        }
        return 0;
    }

    private int getMaxStringHeight(String chaine, FontMetrics fm) {
        //HACK
        JMathTextPane jtp = new JLimitedMathTextPane();
        jtp.setText(chaine);
        chaine = jtp.getText();
        String[] chaines = chaine.split("\n");
        int hauteur = 0;
        for (int i = 0; i < chaines.length; i++) {
            hauteur += fm.getHeight();
        }
        return hauteur;
    }
    
    public boolean peutCouper() {
        //TODO check if there is a selection
        return true;
    }

    public boolean peutCopier() {
        //TODO check if there is a selection
        return true;
    }

    public boolean peutColler() {
        //TODO check if there is a selection
        return true;
    }

    @Override
    public void copier() {
        int[] rowSelected = getSelectedRows();
        int[] columnSelected = getSelectedColumns();
        if (rowSelected.length == 0 || columnSelected.length == 0) {
            return;
        }
        int indexFirstRow = rowSelected[0];
        int indexFirstColumn = columnSelected[0];
        ModeleTableau modeleTransferable = modeleTableau.extractModeleTableau(indexFirstRow, indexFirstColumn, rowSelected.length, columnSelected.length).clone();
        try {
            clipboard.setContents(new ModeleTableauTransferable(modeleTransferable), null);
        } catch (IllegalStateException e1) {
        }
    }

    @Override
    public void couper() {
        int[] rowSelected = getSelectedRows();
        int[] columnSelected = getSelectedColumns();
        int indexFirstRow = rowSelected[0];
        int indexFirstColumn = columnSelected[0];
        if (rowSelected.length == 0 || columnSelected.length == 0) {
            return;
        }
        copier();
        ModeleTableau oldModeleTableau = modeleTableau.extractModeleTableau(indexFirstRow, indexFirstColumn, rowSelected.length, columnSelected.length);
        ModeleTableau tableauVierge = new ModeleTableau(rowSelected.length, columnSelected.length);
        modeleTableau.pasteModeleTableau(indexFirstRow, indexFirstColumn, tableauVierge);
        controlleur.addEdit(new ModeleTableauEdit(indexFirstRow, indexFirstColumn, oldModeleTableau, tableauVierge));
    }

    @Override
    public void coller() {
        if (clipboard.isDataFlavorAvailable(ModeleTableauTransferable.tableauFlavor)) {
            int[] rowSelected = Tableau.this.getSelectedRows();
            int[] columnSelected = Tableau.this.getSelectedColumns();
            if (rowSelected.length == 0 || columnSelected.length == 0) {
                return;
            }
            int indexFirstRow = rowSelected[0];
            int indexFirstColumn = columnSelected[0];
            try {
                ModeleTableau modeleTransferable = (ModeleTableau) clipboard.getData(ModeleTableauTransferable.tableauFlavor);
                ModeleTableau oldModeleTableau = modeleTableau.extractModeleTableau(indexFirstRow, indexFirstColumn, modeleTransferable.getRowCount(), modeleTransferable.getColumnCount()).clone();
                modeleTableau.pasteModeleTableau(indexFirstRow, indexFirstColumn, modeleTransferable.clone());
                controlleur.addEdit(new ModeleTableauEdit(indexFirstRow, indexFirstColumn, oldModeleTableau, modeleTransferable));
            } catch (UnsupportedFlavorException ex) {
                Logger.getLogger(Tableau.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Tableau.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class ActionCopier extends AbstractAction {

        private ActionCopier(String nom) {
            super(nom);
        }

        public void actionPerformed(ActionEvent e) {
            Tableau.this.copier();
        }
    }

    private class ActionColler extends AbstractAction {

        private ActionColler(String nom) {
            super(nom);
        }

        public void actionPerformed(ActionEvent e) {
            Tableau.this.coller();
        }
    }

    private class ActionCouper extends AbstractAction {

        private ActionCouper(String nom) {
            super(nom);
        }

        public void actionPerformed(ActionEvent e) {
            Tableau.this.couper();
        }
    }

    private class ActionCommencerEdition extends AbstractAction {

        private ActionCommencerEdition(String nom) {
            super(nom);
        }

        public void actionPerformed(ActionEvent e) {
            Tableau.this.editCellAt(getSelectedRow(), getSelectedColumn());
        }
    }

    private class ActiveCopierCollerListener implements FocusListener {

        private boolean peutCouper = false;
        private boolean peutCopier = false;
        
        @Override
        public void focusGained(FocusEvent e) {
            if (getEditingColumn() != -1 && getEditingRow() != -1) {
                if (e.getOppositeComponent() != null) {
                    e.getOppositeComponent().requestFocusInWindow();
                }
            }
            if (getSelectedRow() != -1 && getSelectedRow() != -1) {
                if(peutCouper!=true) {
                    peutCouper = true;
                    Tableau.this.firePropertyChange(Editable.PEUT_COUPER, !peutCouper, peutCouper);
                }
                if(peutCopier!=true) {
                    peutCopier = true;
                    Tableau.this.firePropertyChange(Editable.PEUT_COPIER, !peutCopier, peutCopier);
                }
//                IHM.activeCouperCopier(true);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if(peutCouper!=false) {
                peutCouper = false;
                Tableau.this.firePropertyChange(Editable.PEUT_COUPER, !peutCouper, peutCouper);
            }
            if(peutCopier!=false) {
                peutCopier = false;
                Tableau.this.firePropertyChange(Editable.PEUT_COPIER, !peutCopier, peutCopier);
            }
//            IHM.activeCouperCopier(false);
        }
    }

    private class TransmitToEditorKeyListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            if (getSelectedRow() == 0 && getSelectedColumn() == 0 && getFirstCase().equals(FIRST_CASE.NOT_VISIBLE)) {
                return;
            }
            if (e.getModifiersEx() == 0 || e.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK) {
                editor.getComponent().requestFocusInWindow();
                KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(editor.getComponent(), new KeyEvent(editor.getComponent(), KeyEvent.KEY_TYPED, 0, 0, e.getKeyCode(), e.getKeyChar()));
            } else {
                editor.cancelCellEditing();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    private class TransmitFromEditor implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (editor.getComponent() instanceof JLimitedMathTextPane) {
                JLimitedMathTextPane textPane = (JLimitedMathTextPane) editor.getComponent();
                if ((e.getKeyCode() == KeyEvent.VK_UP && textPane.getCaretLine() == 1) || (e.getKeyCode() == KeyEvent.VK_DOWN && textPane.getCaretLine() == textPane.getNumberOfLines()) || (e.getKeyCode() == KeyEvent.VK_LEFT && textPane.getCaretPosition() == 0)
                        || (e.getKeyCode() == KeyEvent.VK_RIGHT && textPane.getCaretPosition() == textPane.getLength())) {
                    editor.stopCellEditing();
                    requestFocusInWindow();
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(Tableau.this, new KeyEvent(Tableau.this, KeyEvent.KEY_PRESSED, 0, 0, e.getKeyCode(), e.getKeyChar()));
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    private class PopUpMenuMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                MenuContextuel menuContextuel = new MenuContextuel();
                if (rowAtPoint(e.getPoint()) == 0 && columnAtPoint(e.getPoint()) == 0) {
                    setRowSelectionInterval(0, 0);
                    setColumnSelectionInterval(0, 0);
                    menuContextuel.addCheckBox(new FirstCaseNormaleAction(), getFirstCase().equals(FIRST_CASE.NORMAL));
                    menuContextuel.addCheckBox(new FirstCaseHiddenAction(), getFirstCase().equals(FIRST_CASE.NOT_VISIBLE));
                    menuContextuel.addCheckBox(new FirstCaseSeparateAction(), getFirstCase().equals(FIRST_CASE.SEPARATE));
                    menuContextuel.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    private class FirstCaseNormaleAction extends ActionComplete {

        private FirstCaseNormaleAction() {
            super("table normal first case");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (getFirstCase().equals(FIRST_CASE.NORMAL)) {
                return;
            }
            Object o = getValueAt(0, 0);
            controlleur.addEdit(new UndoManagerTableau.FirstCaseEdit(getFirstCase(), FIRST_CASE.NORMAL, o));
            setFirstCase(FIRST_CASE.NORMAL);
            revalidate();
            repaint();
        }
    }

    private class FirstCaseHiddenAction extends ActionComplete {

        private FirstCaseHiddenAction() {
            super("table hide first case");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (getFirstCase().equals(FIRST_CASE.NOT_VISIBLE)) {
                return;
            }
            Object o = getValueAt(0, 0);
            controlleur.addEdit(new UndoManagerTableau.FirstCaseEdit(getFirstCase(), FIRST_CASE.NOT_VISIBLE, o));
            setFirstCase(FIRST_CASE.NOT_VISIBLE);
            revalidate();
            repaint();
        }
    }

    private class FirstCaseSeparateAction extends ActionComplete {

        private FirstCaseSeparateAction() {
            super("table separate first case");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (getFirstCase().equals(FIRST_CASE.SEPARATE)) {
                return;
            }
            Object o = getValueAt(0, 0);
            controlleur.addEdit(new UndoManagerTableau.FirstCaseEdit(getFirstCase(), FIRST_CASE.SEPARATE, o));
            setFirstCase(FIRST_CASE.SEPARATE);
            revalidate();
            repaint();
        }
    }
}
