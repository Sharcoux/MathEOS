/**
 * Copyright (C) 2015 François Billioud
 *
 * This file is part of MathEOS
 *
 * MathEOS is free software: you can redistribute it and/or modify
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
 *
 **/

package matheos.proportionality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import matheos.sauvegarde.Data;
import matheos.table.DataTable;
import matheos.table.Table;
import matheos.table.TableEdits;
import matheos.table.TableLayout;
import matheos.table.TableLayout.Cell;

/**
 *
 * @author François Billioud
 */
public class ProportionalityTable extends Table {
    
    private final Map<Integer, Set<Fleche>> listesFleches = new HashMap<>();
    {
        listesFleches.put(HAUT, new HashSet<Fleche>());
        listesFleches.put(BAS, new HashSet<Fleche>());
        listesFleches.put(DROITE, new HashSet<Fleche>());
        listesFleches.put(GAUCHE, new HashSet<Fleche>());
    }

    public ProportionalityTable(int initialRowCount, int initialColumnCount) {
        super(initialRowCount, initialColumnCount);
    }
    
    
    @Override
    public void clear() {
        super.clear();
        for(Map.Entry<Integer, Set<Fleche>> entry : listesFleches.entrySet()) {
            for(Fleche f : entry.getValue()) {
                deleteArrow(entry.getKey(), f);
            }
        }
    }
    
    @Override
    protected ArrayList<TableLayout.Cell> deleteRow(int i) {
        ArrayList<TableLayout.Cell> L = super.deleteRow(i);
        if(getRowCount()==0) {
            listesFleches.get(GAUCHE).clear();
            listesFleches.get(DROITE).clear();
        }
        return L;
    }
    
    @Override
    protected ArrayList<TableLayout.Cell> deleteColumn(int j) {
        ArrayList<TableLayout.Cell> L = super.deleteColumn(j);
        if(getColumnCount()==0) {
            listesFleches.get(GAUCHE).clear();
            listesFleches.get(DROITE).clear();
        }
        return L;
    }
    
    @Override
    public void charger(Data data) {
        DataProportionality dataProportionality;
        if(data instanceof DataProportionality) {dataProportionality = (DataProportionality) data;}
        else {
            dataProportionality = new DataProportionality();
            dataProportionality.putAll(data);
        }
        super.charger(dataProportionality);
        //crée les flèches
        clearArrows(HAUT);addArrows(HAUT, dataProportionality.getListeFleches(HAUT));
        clearArrows(BAS);addArrows(BAS, dataProportionality.getListeFleches(BAS));
        clearArrows(DROITE);addArrows(DROITE, dataProportionality.getListeFleches(DROITE));
        clearArrows(GAUCHE);addArrows(GAUCHE, dataProportionality.getListeFleches(GAUCHE));
    }
    
    @Override
    public DataProportionality getDonnees() {
        DataTable dataTable = super.getDonnees();
        DataProportionality data = new DataProportionality();
        data.putAll(dataTable);
        
        //enregistre les flèches dans le data global
        data.writeFleches(listesFleches.get(HAUT), DataProportionality.TOP_ARROW);
        data.writeFleches(listesFleches.get(BAS), DataProportionality.BOTTOM_ARROW);
        data.writeFleches(listesFleches.get(DROITE), DataProportionality.RIGHT_ARROW);
        data.writeFleches(listesFleches.get(GAUCHE), DataProportionality.LEFT_ARROW);
        
        return data;
    }
    
    private void clearArrows(int orientation) {
        Iterator<Fleche> iter = listesFleches.get(orientation).iterator();
        while(iter.hasNext()) {
            Fleche f = iter.next();
            iter.remove();
            fireArrowDeleted(orientation, f);
        }
    }
    
    private void addArrows(int orientation, Set<Fleche> set) {
        for(Fleche f : set) {insertArrow(orientation, f);}
    }

    private void fireArrowDeleted(int orientation, Fleche arrow) {
        for(TableLayout.TableModelListener l : getTableModelListeners()) {
            if(l instanceof ModelListener) {
                ((ModelListener)l).arrowDeleted(orientation, arrow);
            }
        }
    }
    
    private void fireArrowInserted(int orientation, Fleche arrow) {
        for(TableLayout.TableModelListener l : getTableModelListeners()) {
            if(l instanceof ModelListener) {
                ((ModelListener)l).arrowInserted(orientation, arrow);
            }
        }
    }
    
    void deleteArrow(int orientation, Fleche arrow) {
        arrow.setTable(null);
        listesFleches.get(orientation).remove(arrow);
        fireArrowDeleted(orientation, arrow);
    }
    
    void insertArrow(int orientation, Fleche arrow) {
        arrow.setTable(this);
        listesFleches.get(orientation).add(arrow);
        fireArrowInserted(orientation, arrow);
    }
    
    Set<Fleche> getArrows(int direction) {
        return listesFleches.get(direction);
    }
    
    List<Fleche> getAllArrows() {
        List<Fleche> L = new LinkedList<>();
        for(Set<Fleche> s : listesFleches.values()) {
            L.addAll(s);
        }
        return L;
    }
    
    interface ModelListener extends TableLayout.TableModelListener {
        void arrowInserted(int direction, Fleche fleche);
        void arrowDeleted(int direction, Fleche fleche);
    }

    class ArrowInsertedEdit extends AbstractUndoableEdit {
        private final Fleche fleche;
        ArrowInsertedEdit(Fleche fleche) {
            this.fleche = fleche;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            deleteArrow(fleche.getOrientation(), fleche);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            insertArrow(fleche.getOrientation(), fleche);
        }
    }

    class ArrowDeletedEdit extends AbstractUndoableEdit {
        private final Fleche fleche;
        ArrowDeletedEdit(Fleche fleche) {
            this.fleche = fleche;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            insertArrow(fleche.getOrientation(), fleche);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            deleteArrow(fleche.getOrientation(), fleche);
        }
    }
    
    class SuppressionEdit extends TableEdits.LineChangeEdit.SuppressionEdit {
        private final List<Fleche> colateral;

        SuppressionEdit(int index, boolean line, TableLayout.Cell[] cells, List<Fleche> colateral) {
            super(index, ProportionalityTable.this, line, cells);
            this.colateral = colateral;
        }

        @Override
        protected ArrayList<TableLayout.Cell> insert() {
            ArrayList<Cell> L = super.insert();
            for(Fleche f : colateral) {
                insertArrow(f.getOrientation(), f);
            }
            return L;
        }
    }

}
