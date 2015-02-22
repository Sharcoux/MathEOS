/**
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of Bomehc
 *
 * Bomehc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 *
 **/

package bomehc.table;

import bomehc.sauvegarde.DataTexte;
import bomehc.table.TableLayout.Cell;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import bomehc.table.TableLayout.Coord;

/**
 *
 * @author François Billioud
 */
public abstract class TableEdits {
    public static class ContentEdit extends AbstractUndoableEdit {
        private final Cell cell;
        private final DataTexte oldContent, newContent;
        public ContentEdit(Cell editingCell, DataTexte oldContent, DataTexte newContent) {
            this.cell = editingCell;
            this.oldContent = oldContent;
            this.newContent = newContent;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            cell.charger(oldContent);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            cell.charger(newContent);
        }
    }
    
    public static class ClearContentEdit extends AbstractUndoableEdit {
        private final Coord debut;
        private final DataTexte[][] previousContent;
        private final Table table;
        public ClearContentEdit(Coord debut, DataTexte[][] previousContent, Table table) {
            this.debut = debut;
            this.previousContent = previousContent;
            this.table = table;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            int n = previousContent.length, m = previousContent[0].length;
            for(int i=0; i<n; i++) {
                for(int j=0; j<m; j++) {
                    table.getCell(i+debut.ligne, j+debut.colonne).charger(previousContent[i][j]);
                }
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            int n = previousContent.length+debut.ligne, m = previousContent[0].length+debut.colonne;
            for(int i=debut.ligne; i<n; i++) {
                for(int j=debut.colonne; j<m; j++) {
                    table.getCell(i, j).clear();
                    table.getCell(i, j).discardEdits();
                }
            }
        }
    }
    
    public static class ReplaceContentEdit extends AbstractUndoableEdit {
        private final Coord debut;
        private final DataTexte[][] previousContent;
        private final DataTexte[][] newContent;
        private final Table table;
        public ReplaceContentEdit(Coord debut, DataTexte[][] previousContent, DataTexte[][] newContent, Table table) {
            this.debut = debut;
            this.previousContent = previousContent;
            this.newContent = newContent;
            this.table = table;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            int n = previousContent.length, m = previousContent[0].length;
            for(int i=0; i<n; i++) {
                for(int j=0; j<m; j++) {
                    table.getCell(i+debut.ligne, j+debut.colonne).charger(previousContent[i][j]);
                }
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            int n = newContent.length, m = newContent[0].length;
            for(int i=0; i<n; i++) {
                for(int j=0; j<m; j++) {
                    table.getCell(i+debut.ligne, j+debut.colonne).charger(newContent[i][j]);
                }
            }
        }
    }
    
    public static class ColorEdit extends AbstractUndoableEdit {
        private final Cell cell;
        private final Color oldColor, newColor;
        public ColorEdit(Cell editingCell, Color oldColor, Color newColor) {
            this.cell = editingCell;
            this.oldColor = oldColor;
            this.newColor = newColor;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            cell.setColor(oldColor);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            cell.setColor(newColor);
        }
    }
    
    public static class LineColorEdit extends AbstractUndoableEdit {
        private final int index;
        private final boolean line;
        private final Color oldColor, newColor;
        private final Table table;
        public LineColorEdit(int index, boolean line, Color oldColor, Color newColor, Table table) {
            this.index = index;
            this.line = line;
            this.oldColor = oldColor;
            this.newColor = newColor;
            this.table = table;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            table.setColor(line, index, oldColor);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            table.setColor(line, index, newColor);
        }
    }
    
    /**
     * Edit pour les insertions/suppressions de lignes/colonnes dans le modèle
     */
    public static abstract class LineChangeEdit extends AbstractUndoableEdit {
        private final int index;
        protected final Table table;
        private final boolean line;
        private final boolean insert;
        
        public static class InsertionEdit extends LineChangeEdit {
            public InsertionEdit(int index, Table table, boolean line) {
                super(index, table, line, true);
            }
        }
        public static class SuppressionEdit extends LineChangeEdit {
            private final DataTexte[] contents;
            
            public SuppressionEdit(int index, Table table, boolean line, Cell[] cells) {
                super(index, table, line, false);
                this.contents = new DataTexte[cells.length];
                for(int i=0; i<cells.length; i++) {
                    this.contents[i] = cells[i].getDonnees();
                }
            }
            
            @Override
            protected ArrayList<Cell> insert() {
                ArrayList<Cell> L = super.insert();
                Cell[] T = L.toArray(new Cell[contents.length]);
                for(int i=0; i<contents.length; i++) {
                    T[i].charger(contents[i]);
                }
                return L;
            }
        }

        /**
         * Crée un édit correspondant à l'insertion ou la suppression d'une ligne ou d'une colonne dans le modèle.
         * @param index index où a lieu l'évènement
         * @param model le modèle concerné
         * @param line boolean indiquant s'il s'agit d'une ligne ou d'une colonne
         * @param cells les cellules concernées. On en récupère ainsi le contenu. laisser null en cas d'insertion
         * @param insert true s'il s'agit d'une insertion. false s'il s'agit d'une suppression
         */
        public LineChangeEdit(int index, Table table, boolean line, boolean insert) {
            this.index = index;
            this.table = table;
            this.line = line;
            this.insert = insert;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            apply(!insert);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            apply(insert);
        }
        
        protected ArrayList<Cell> insert() {
            return table.insert(line, index);
        }
        
        protected ArrayList<Cell> delete() {
            return table.delete(line, index);
        }
        
        private ArrayList<Cell> apply(boolean insertion) {
            return insertion ? insert() : delete();
        }
    }
}
