/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package matheos.tableau;

import static matheos.tableau.TableConstants.TYPE_CASES;
import matheos.sauvegarde.DataTexte;
import matheos.utils.texte.EditeurIO;
import matheos.utils.texte.JLimitedMathTextPane;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 *
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class UndoManagerTableau extends UndoManager {

    private static transient ModeleTableau modeleTableau;
    private static transient ModeleFleches modeleFleches;

    public UndoManagerTableau(ModeleTableau modeleTableau, ModeleFleches modeleFleches) {
        UndoManagerTableau.modeleTableau = modeleTableau;
        UndoManagerTableau.modeleFleches = modeleFleches;
    }

    public void setModeleTableau(ModeleTableau modeleTableau) {
        UndoManagerTableau.modeleTableau = modeleTableau;
    }

    @Override
    public synchronized boolean addEdit(UndoableEdit anEdit) {
//        IHM.notifyUndoableAction();
        return super.addEdit(anEdit);
    }

    /**
     * Edit concernant la modification d'un texte d'une cellule du tableau.
     */
    public static class ContentEdit extends AbstractUndoableEdit {

        private ModeleCellule oldModeleCellule;
        private ModeleCellule newModeleCellule;
        private final int row;
        private final int column;

        public ContentEdit(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public void setOldModeleCellule(ModeleCellule oldModeleCellule) {
            this.oldModeleCellule = oldModeleCellule;
        }

        public void setNewModeleCellule(ModeleCellule newModeleCellule) {
            this.newModeleCellule = newModeleCellule;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            modeleTableau.setValueAt(newModeleCellule, row, column);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            modeleTableau.setValueAt(oldModeleCellule, row, column);
        }
    }

    /**
     * Edit concernant l'ajout d'une ligne ou d'une colonne au tableau.
     */
    public static class AddEdit extends AbstractUndoableEdit {

        private final TYPE_CASES type;
        private final int index;

        public AddEdit(TYPE_CASES type, int index) {
            this.type = type;
            this.index = index;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            switch (type) {
                case COLUMN:
                    modeleTableau.ajouterColonne(index);
                    modeleFleches.ajouterCases(type, index);
                    break;
                case ROW:
                    modeleTableau.ajouterLigne(index);
                    modeleFleches.ajouterCases(type, index);
                    break;
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            switch (type) {
                case COLUMN:
                    modeleTableau.supprimerColonne(index);
                    modeleFleches.supprimerCases(type, index);
                    break;
                case ROW:
                    modeleTableau.supprimerLigne(index);
                    modeleFleches.supprimerCases(type, index);
                    break;
            }
        }
    }

    /**
     * Edit concernant la suppression d'une ligne ou d'une colonne du tableau.
     */
    public static class RemoveEdit extends AbstractUndoableEdit {

        private final TYPE_CASES type;
        private final int index;
        private final List<ModeleCellule> celluleRemoved;
        private final ModeleFleches flechesRemoved;

        public RemoveEdit(TYPE_CASES type, int index, List<ModeleCellule> celluleRemoved, ModeleFleches flechesRemoved) {
            this.type = type;
            this.index = index;
            this.celluleRemoved = new ArrayList<>();
            for (ModeleCellule cellule : celluleRemoved) {
                this.celluleRemoved.add(cellule);
            }
            this.flechesRemoved = flechesRemoved;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            switch (type) {
                case COLUMN:
                    modeleTableau.supprimerColonne(index);
                    modeleFleches.supprimerCases(type, index);
                    break;
                case ROW:
                    modeleTableau.supprimerLigne(index);
                    modeleFleches.supprimerCases(type, index);
                    break;
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            List<ModeleCellule> cellules = new ArrayList<>();
            for (ModeleCellule cellule : celluleRemoved) {
                cellules.add(cellule);
            }
            switch (type) {
                case COLUMN:
                    modeleTableau.ajouterColonne(index, cellules);
                    modeleFleches.ajouterCases(type, index);
                    modeleFleches.setFleches(flechesRemoved);
                    break;
                case ROW:
                    modeleTableau.ajouterLigne(index, cellules);
                    modeleFleches.ajouterCases(type, index);
                    modeleFleches.setFleches(flechesRemoved);
                    break;
            }
        }
    }

    /**
     * Edit concernant la coloration d'une ligne ou d'une colonne du tableau.
     */
    public static class ColorateEdit extends AbstractUndoableEdit {

        private final TYPE_CASES type;
        private final int index;
        private final Color couleur;
        private final List<Color> oldCouleurs;

        public ColorateEdit(TYPE_CASES type, int index, Color couleur, List<Color> oldCouleurs) {
            this.type = type;
            this.index = index;
            this.couleur = couleur;
            this.oldCouleurs = oldCouleurs;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            switch (type) {
                case COLUMN:
                    modeleTableau.colorerColonne(index, couleur);
                    break;
                case ROW:
                    modeleTableau.colorerLigne(index, couleur);
                    break;
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            switch (type) {
                case COLUMN:
                    modeleTableau.decolorerColonne(index);
                    modeleTableau.colorerCases(type, index, oldCouleurs);
                    break;
                case ROW:
                    modeleTableau.decolorerLigne(index);
                    modeleTableau.colorerCases(type, index, oldCouleurs);
                    break;
            }
        }
    }

    /**
     * Edit concernant la décoloration d'une ligne ou d'une colonne du tableau.
     */
    public static class DecolorateEdit extends AbstractUndoableEdit {

        private final TYPE_CASES type;
        private final int index;
        private final Color couleur;

        public DecolorateEdit(TYPE_CASES type, int index, Color couleur) {
            this.type = type;
            this.index = index;
            this.couleur = couleur;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            switch (type) {
                case COLUMN:
                    modeleTableau.decolorerColonne(index);
                    break;
                case ROW:
                    modeleTableau.decolorerLigne(index);
                    break;
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            switch (type) {
                case COLUMN:
                    modeleTableau.colorerColonne(index, couleur);
                    break;
                case ROW:
                    modeleTableau.colorerLigne(index, couleur);
                    break;
            }
        }
    }

    /**
     * Edit concernant la coloration d'une cellule d'un tableau.
     */
    public static class ColorateCelluleEdit extends AbstractUndoableEdit {

        private final int row;
        private final int column;
        private final Color oldCouleur;
        private final Color newCouleur;

        public ColorateCelluleEdit(int row, int column, Color oldCouleur, Color newCouleur) {
            this.row = row;
            this.column = column;
            this.oldCouleur = oldCouleur;
            this.newCouleur = newCouleur;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            modeleTableau.colorerCase(row, column, newCouleur);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            modeleTableau.colorerCase(row, column, oldCouleur);
        }
    }

    /**
     * Edit concernant l'ajout d'une flèche de proportionnalité au tableau.
     */
    public static class AddFlecheEdit extends AbstractUndoableEdit {

        private final DataFleche modeleFleche;

        public AddFlecheEdit(DataFleche modeleFleche) {
            this.modeleFleche = modeleFleche;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            modeleFleches.add(modeleFleche);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            modeleFleches.remove(modeleFleche);
        }
    }

    /**
     * Edit concernant la suppression d'une flèche de proportionnalité au tableau.
     */
    public static class RemoveFlecheEdit extends AbstractUndoableEdit {

        private final DataFleche dataFleche;

        public RemoveFlecheEdit(DataFleche modeleFleche) {
            this.dataFleche = modeleFleche;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            modeleFleches.remove(dataFleche);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            modeleFleches.add(dataFleche);
        }
    }

    /**
     * Edit concernant la modification du champ de texte d'une flèche de proportionnalité.
     */
    public static class ContentFlecheEdit extends AbstractUndoableEdit {

        private final JLimitedMathTextPane jtp;
        private final DataTexte oldData;
        private final DataTexte newData;

        public ContentFlecheEdit(JLimitedMathTextPane jtp, DataTexte oldData, DataTexte newData) {
            this.jtp = jtp;
            this.oldData = oldData;
            this.newData = newData;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            jtp.clear();
            EditeurIO.read(jtp, newData);
            jtp.getUndo().discardAllEdits();
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            jtp.clear();
            EditeurIO.read(jtp, oldData);
            jtp.getUndo().discardAllEdits();
        }
    }

    /**
     * Edit concernant le cas d'un couper ou d'un coller sur le tableau.
     */
    public static class ModeleTableauEdit extends AbstractUndoableEdit {

        private final ModeleTableau oldModeleTableau;
        private final ModeleTableau newModeleTableau;
        private final int firstRow;
        private final int firstColumn;

        public ModeleTableauEdit(int firstRow, int firstColumn, ModeleTableau oldModeleTableau, ModeleTableau newModeleTableau) {
            this.firstRow = firstRow;
            this.firstColumn = firstColumn;
            this.oldModeleTableau = oldModeleTableau;
            this.newModeleTableau = newModeleTableau;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            modeleTableau.pasteModeleTableau(firstRow, firstColumn, newModeleTableau);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            modeleTableau.pasteModeleTableau(firstRow, firstColumn, oldModeleTableau);
        }
    }
    
    /**
     * Edit concernant le changement de type de la première case du tableau
     */
    public static class FirstCaseEdit extends AbstractUndoableEdit{
        
        private final TableConstants.FIRST_CASE oldType;
        private final TableConstants.FIRST_CASE newType;
        private Object oldFirstCellule;
        
        public FirstCaseEdit(TableConstants.FIRST_CASE oldType, TableConstants.FIRST_CASE newType, Object oldFirstCellule){
            this.oldType = oldType;
            this.newType = newType;
            if(oldFirstCellule != null && oldFirstCellule instanceof ModeleCellule){
                this.oldFirstCellule = oldFirstCellule;
            }
        }
        
        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            modeleTableau.setFirstCase(newType);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            modeleTableau.setFirstCase(oldType);
            if(oldFirstCellule != null && oldFirstCellule instanceof ModeleCellule){
                modeleTableau.setValueAt(oldFirstCellule, 0, 0);
            }
        }
    }
}
