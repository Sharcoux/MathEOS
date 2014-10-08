/**
 * Copyright (C) 2014 François Billioud
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
 *
 **/

package matheos.table;

import matheos.json.Json;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.Data.Enregistrable;
import matheos.sauvegarde.DataObject;
import matheos.sauvegarde.DataTexte;
import static matheos.table.Model.DataTable.*;
import matheos.table.TableLayout.Cell;
import matheos.table.TableLayout.CellFactory;
import matheos.table.TableLayout.ContentEditListener;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Le modèle du tableau. Il contient les lignes, les colonnes, les cellules et permet
 * d'en ajouter, d'en supprimer, de récupérer leurs indices...
 * @author François Billioud
 */
public class Model implements TableLayout.TableModel, ContentEditListener, Enregistrable {
    private final ArrayList<Row> rowAccess = new ArrayList<>();
    private final ArrayList<Column> columnAccess = new ArrayList<>();

    private final CellFactory cellFactory;
    public Model(CellFactory cellFactory) {this.cellFactory = cellFactory;}
    public Model(CellFactory cellFactory, int initialRows, int initialColumns) {
        this.cellFactory = cellFactory;
        for(int i=0; i<initialRows; i++) { insertRow(i); }
        for(int j=0; j<initialColumns; j++) { insertColumn(j); }
    }
    
    private Map<Integer, Set<Fleche>> listesFleches = new HashMap<>();
    {
        listesFleches.put(HAUT, new HashSet<Fleche>());
        listesFleches.put(BAS, new HashSet<Fleche>());
        listesFleches.put(DROITE, new HashSet<Fleche>());
        listesFleches.put(GAUCHE, new HashSet<Fleche>());
    }
    
    @Override
    public Cell[] get(boolean line, int index) {
        ArrayList<Cell> L = line==ROW ? rowAccess.get(index) : columnAccess.get(index);
        return L.toArray(new Cell[getCount(!line)]);
    }
    @Override
    public Cell getCell(int i, int j) {
        return rowAccess.get(i).get(j);
    }
    @Override
    public Cell[] getAllCells() {
        int l = getRowCount()*getColumnCount();
        ArrayList<Cell> L = new ArrayList<>(l);
        int n = getRowCount(); int m = getColumnCount();
        for(int i=0; i<n; i++) {
            Cell[] row = get(ROW, i);
            for(int j=0; j<m; j++) {
                L.add(row[j]);
            }
        }
        return L.toArray(new Cell[l]);
    }
    @Override
    public int getRowCount() {return rowAccess.size();}
    @Override
    public int getColumnCount() {return columnAccess.size();}
    @Override
    public int getCount(boolean line) {return line==ROW ? getRowCount() : getColumnCount();}

    public void clear() {
        int n = getRowCount(), m = getColumnCount();
        Cell[][] tableContent = new Cell[n][m];
        for(int i = 0; i<n; i++) {
            tableContent[i] = get(ROW, i);
            for(int j=0; j<m; j++) {cellRemoved(tableContent[i][j]);}
        }
        rowAccess.clear();
        columnAccess.clear();
        for(TableLayout.TableModelListener l : listeners) {l.cleared(tableContent);}
        for(Map.Entry<Integer, Set<Fleche>> entry : listesFleches.entrySet()) {
            for(Fleche f : entry.getValue()) {
                deleteArrow(entry.getKey(), f);
            }
        }
    }

    @Override
    public void contentEdited(Cell c) {
        for(TableLayout.TableModelListener l : listeners) {l.contentEdited(c, c.getDonnees());}
    }
    
    private void cellAdded(Cell c) {
        c.addContentEditListener(this);
    }
    private void cellRemoved(Cell c) {
        c.removeContentEditListener(this);
    }

    public Cell replaceCell(int row, int column, Cell newCell) {
        if(row>=getRowCount() || row<0 || column>=getColumnCount() || column<0) {return null;}
        Cell oldCell = rowAccess.get(row).set(column, newCell);
        columnAccess.get(column).set(row, newCell);
        cellRemoved(oldCell);
        cellAdded(newCell);
        for(TableLayout.TableModelListener l : listeners) {l.cellReplaced(oldCell, newCell);}
        return oldCell;
    }
    
    private ArrayList<Cell> insertRow(int i) {
        //fait l'insertion dans les deux listes d'accès
        Row L = new Row();
        int m = getColumnCount();
        for(int j = 0; j<m; j++) {
            Cell cell = cellFactory.create();
            cellAdded(cell);
            L.add(cell);
            
            Column column = columnAccess.get(j);
            column.applyStyleToCell(cell);
            column.add(i, cell);
        }
        rowAccess.add(i, L);

        //prévient les listeners
        Cell[] T = L.toArray(new Cell[L.size()]);
        for(TableLayout.TableModelListener l : listeners) {l.rowInserted(T, i);}

        return L;
    }

    private ArrayList<Cell> insertColumn(int j) {
        //fait l'insertion dans les deux listes d'accès
        Column L = new Column();
        int n = getRowCount();
        for(int i = 0; i<n; i++) {
            Cell cell = cellFactory.create();
            cellAdded(cell);
            L.add(cell);
            
            Row row = rowAccess.get(i);
            row.add(j, cell);
            row.applyStyleToCell(cell);
        }
        columnAccess.add(j, L);

        //prévient les listeners
        Cell[] T = L.toArray(new Cell[L.size()]);
        for(TableLayout.TableModelListener l : listeners) {l.columnInserted(T, j);}

        return L;
    }
    
    @Override
    public ArrayList<Cell> insert(boolean line, int index) {
        return line==ROW ? insertRow(index) : insertColumn(index);
    }

    private ArrayList<Cell> deleteRow(int i) {
        //fait la suppression dans les deux listes d'accès
        int m = getColumnCount();
        for(int j = 0; j<m; j++) {
            Cell cell = columnAccess.get(j).remove(i);
//            removeCell(cell);//vérifier que layout.deleteRow ne suffit pas
        }
        ArrayList<Cell> L = rowAccess.remove(i);

        //prévient les listeners
        Cell[] T = L.toArray(new Cell[L.size()]);
        for(TableLayout.TableModelListener l : listeners) {l.rowDeleted(T, i);}
        
        if(getRowCount()==0) {
            listesFleches.get(GAUCHE).clear();
            listesFleches.get(DROITE).clear();
        }
        return L;
    }
    
    private ArrayList<Cell> deleteColumn(int j) {
        int n = getRowCount();
        for(int i = 0; i<n; i++) {
            Cell cell = rowAccess.get(i).remove(j);
//            removeCell(cell);
        }
        ArrayList<Cell> L = columnAccess.remove(j);

        //prévient les listeners
        Cell[] T = L.toArray(new Cell[L.size()]);
        for(TableLayout.TableModelListener l : listeners) {l.columnDeleted(T, j);}

        if(getColumnCount()==0) {
            listesFleches.get(GAUCHE).clear();
            listesFleches.get(DROITE).clear();
        }
        
        return L;
    }
    
    @Override
    public ArrayList<Cell> delete(boolean line, int index) {
        return line==ROW ? deleteRow(index) : deleteColumn(index);
    }
    
    public Color getColor(boolean line, int index) {
        return line==ROW ? rowAccess.get(index).getBackground() : columnAccess.get(index).getBackground();
    }
    
    public void setColor(boolean line, int index, Color c) {
        if(line==ROW) {
            rowAccess.get(index).setBackground(c);
        } else {
            columnAccess.get(index).setBackground(c);
        }
    }
    
    private final LinkedList<TableLayout.TableModelListener> listeners = new LinkedList<>();
    @Override
    public void addTableModelListener(TableLayout.TableModelListener l) {
        if(!listeners.contains(l)) listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableLayout.TableModelListener l) {
        listeners.remove(l);
    }

    @Override
    public void charger(Data data) {
        DataTable dataTable;
        if(data instanceof Model.DataTable) {dataTable = (DataTable) data;}
        else {
            dataTable = new DataTable();
            dataTable.putAll(data);
        }
        clear();
        int n = dataTable.getRowCount();
        int m = dataTable.getColumnCount();
        for(int i=0; i<n; i++) {
            Row row = new Row();
            row.charger(dataTable.getDataLine(ROW, i));
            //fait l'insertion dans la liste rowAccess et crée les cellules
            DataTexte[] dataCells = dataTable.getRowCellContent(i);
            for(int j = 0; j<m; j++) {
                Column column;
                if(i==0) {//On initialise la colonne au premier passage
                    column = new Column();
                    column.charger(dataTable.getDataLine(COLUMN, j));
                    columnAccess.add(column);
                } else {//sinon on récupère la colonne dans la liste d'accès
                    column = columnAccess.get(j);
                }
                Cell cell = cellFactory.load(dataCells[j]);
                cellAdded(cell);
                row.add(cell);
                column.add(cell);
            }
            rowAccess.add(i, row);
            
            //prévient les listeners
            Cell[] T = row.toArray(new Cell[row.size()]);
            for(TableLayout.TableModelListener l : listeners) {l.rowInserted(T, i);}
        }
        //met à jour le columnAccess
        for(int j = 0; j<m; j++) {
            Column column = new Column();
            column.charger(dataTable.getDataLine(COLUMN, j));
            for(int i=0; i<n; i++) {
                column.add(rowAccess.get(i).get(j));
            }
        }
        //crée les flèches
        clearArrows(HAUT);addArrows(HAUT, dataTable.getListeFleches(HAUT));
        clearArrows(BAS);addArrows(BAS, dataTable.getListeFleches(BAS));
        clearArrows(DROITE);addArrows(DROITE, dataTable.getListeFleches(DROITE));
        clearArrows(GAUCHE);addArrows(GAUCHE, dataTable.getListeFleches(GAUCHE));
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
        for(TableLayout.TableModelListener l : listeners) {
            if(l instanceof ModelListener) {
                ((ModelListener)l).arrowDeleted(orientation, arrow);
            }
        }
    }
    
    private void fireArrowInserted(int orientation, Fleche arrow) {
        for(TableLayout.TableModelListener l : listeners) {
            if(l instanceof ModelListener) {
                ((ModelListener)l).arrowInserted(orientation, arrow);
            }
        }
    }
    
    void deleteArrow(int orientation, Fleche arrow) {
        arrow.setModel(null);
        listesFleches.get(orientation).remove(arrow);
        fireArrowDeleted(orientation, arrow);
    }
    
    void insertArrow(int orientation, Fleche arrow) {
        arrow.setModel(this);
        listesFleches.get(orientation).add(arrow);
        fireArrowInserted(orientation, arrow);
    }
    
    Set<Fleche> getArrows(int direction) {
        return listesFleches.get(direction);
    }

    @Override
    public Data getDonnees() {
        DataTable data = new DataTable();
        int n=this.getRowCount(), m=this.getColumnCount();
        //enregistre les nombre de ligne/colonne dans le data global
        data.putElement(ROW_COUNT, n+"");
        data.putElement(COLUMN_COUNT, m+"");
        //enregistre les données des lignes dans un dataRows, et les cellules dans le data global
        Data dataRows = new DataObject();
        for(int i=0; i<n; i++) {
            Row row = this.rowAccess.get(i);
            dataRows.putData(i+"", row.getDonnees());
            for(int j=0; j<m; j++) {
                data.putData(i+","+j,row.get(j).getDonnees());
            }
        }
        //enregistre les données des colonnes dans un dataColumn
        Data dataColumns = new DataObject();
        for(int j=0; j<m; j++) {
            Column column = this.columnAccess.get(j);
            dataColumns.putData(j+"", column.getDonnees());
        }
        //enregistre les data créés dans le data global
        data.putData(ROWS, dataRows);
        data.putData(COLUMNS, dataColumns);

        //enregistre les flèches dans le data global
        data.writeFleches(listesFleches.get(HAUT), TOP_ARROW);
        data.writeFleches(listesFleches.get(BAS), BOTTOM_ARROW);
        data.writeFleches(listesFleches.get(DROITE), RIGHT_ARROW);
        data.writeFleches(listesFleches.get(GAUCHE), LEFT_ARROW);
        return data;
    }
    
    Coord getCellCoordinates(Cell c) {
        int n = getRowCount();
        int m = getColumnCount();
        for(int i=0; i<n; i++) {
            Cell[] row = get(ROW,i);
            for(int j=0; j<m; j++) {
                if(c==row[j]) return new Coord(i,j);
            }
        }
        return null;
    }

    @Override
    public int getMaxFontSize() {
        return cellFactory.getMaxFontSize();
    }
    
    @Override
    public int getMinFontSize() {
        return cellFactory.getMinFontSize();
    }
    
    private static abstract class Line extends ArrayList<Cell> implements Enregistrable {
        public static final String BACKGROUND_COLOR = "background";
        public static final String FOREGROUND_COLOR = "foreground";
        public static final String BOLD = "bold";
        public static final String ITALIC = "italic";
        public static final String UNDERLINED = "undelined";
        public static final String ALIGNMENT = "alignment";
        
        private final Data data;
        
        @Override
        public Data getDonnees() {return data;}
        @Override
        public void charger(Data data) {this.data.clear();this.data.putAll(data);}
        
        public Color getBackground() {
            if(data.getElement(BACKGROUND_COLOR)==null) {return null;}
            Color c = null;
            try {
                c = (Color) Json.toJava(data.getElement(BACKGROUND_COLOR),Color.class);
            } catch (IOException ex) {
                Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
            }
            return c;
        }
        public void setBackground(Color couleur) {
            Color old = getBackground();
            if(couleur==null) {
                data.removeElementByKey(BACKGROUND_COLOR);
                for(Cell c : this) {c.setColor(Cell.BACKGROUND);}
            }
            else {
                try {
                    data.putElement(BACKGROUND_COLOR, Json.toJson(couleur));
    //                for(Cell c : this) {c.setBackgroundManual(couleur);}//A utiliser pour donner une couleur différente aux cellules
                    for(Cell c : this) {c.setColor(couleur);}
                } catch (IOException ex) {
                    Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        public Color getForeground() {
            if(data.getElement(FOREGROUND_COLOR)==null) {return null;}
            Color c = null;
            try {
                c = (Color) Json.toJava(data.getElement(FOREGROUND_COLOR),Color.class);
            } catch (IOException ex) {
                Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
            }
            return c;
        }
        public void setForeground(Color couleur) {
            try {
                data.putElement(FOREGROUND_COLOR, Json.toJson(couleur));
            } catch (IOException ex) {
                Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void setBold(boolean bold) { data.putElement(BOLD, bold+""); }
        public void setItalic(boolean italic) { data.putElement(ITALIC, italic+""); }
        public void setUnderlined(boolean underlined) { data.putElement(UNDERLINED, underlined+""); }
        public void setAlignment(int alignment) { data.putElement(ALIGNMENT, alignment+""); }

        public boolean isBold() {
            String s = data.getElement(BOLD);
            return s!=null && Boolean.parseBoolean(s);
        }
        public boolean isItalic() {
            String s = data.getElement(ITALIC);
            return s!=null && Boolean.parseBoolean(s);
        }
        public boolean isUnderlined() {
            String s = data.getElement(UNDERLINED);
            return s!=null && Boolean.parseBoolean(s);
        }
        public int getAlignment() {
            String s = data.getElement(ALIGNMENT);
            return s==null ? 0 : Integer.parseInt(s);
        }

        /**
         * Crée une ligne ou colonne en initialisant les données des flèches et des styles. les cellules ne sont pas concernées
         * @param data contient les informations de style propres à la ligne, et les indices des flèches
         */
        private Line() {data = new DataObject();}
        
        public void applyStyleToCell(Cell c) {
            Color color = getBackground();
            if(color!=null) { c.setColor(color); }
            
            ActionEvent e = new ActionEvent(c, 0, "newCell");
            color = getForeground();
            if(color!=null) {new HTMLEditorKit.ForegroundAction("foreground", color).actionPerformed(e);}
            if(isBold()) {new HTMLEditorKit.BoldAction().actionPerformed(e);}
            if(isItalic()) {new HTMLEditorKit.ItalicAction().actionPerformed(e);}
            if(isUnderlined()) {new HTMLEditorKit.UnderlineAction().actionPerformed(e);}
            new HTMLEditorKit.AlignmentAction("alignment", getAlignment()).actionPerformed(e);
        }
    }
        
    static class Row extends Line {}
    static class Column extends Line {}
    
    public static final int HAUT = 1;
    public static final int GAUCHE = 2;
    public static final int BAS = 4;
    public static final int DROITE = 8;
    
    public static class DataTable extends DataObject {

        public static final String ROWS = "rows";
        public static final String COLUMNS = "columns";
        public static final String ROW_COUNT = "rowCount";
        public static final String COLUMN_COUNT = "columnCount";
        public static final String TOP_ARROW = "topArrow";
        public static final String BOTTOM_ARROW = "bottomArrow";
        public static final String RIGHT_ARROW = "rightArrow";
        public static final String LEFT_ARROW = "leftArrow";

        public DataTable() {}
        
        public Data getDataLine(boolean direction, int index) {
            return getData(direction==ROW ? ROWS : COLUMNS).getData(index+"");
        }
        public DataTexte getDataCell(int i, int j) {
            Data donnees = getData(i+","+j);
            if(donnees instanceof DataTexte) {return (DataTexte)donnees;}
            else {
                DataTexte dataTexte = new DataTexte("");
                dataTexte.putAll(donnees);
                return dataTexte;
            }
        }
        public int getRowCount() {return Integer.parseInt(getElement(ROW_COUNT));}
        public int getColumnCount() {return Integer.parseInt(getElement(COLUMN_COUNT));}
        
        private DataTexte[] getRowCellContent(int i) {
            int m = getColumnCount();
            DataTexte[] T = new DataTexte[m];
            for(int j=0;j<m;j++) {
                T[j] = getDataCell(i, j);
            }
            return T;
        }
        
        private Set<Fleche> getListeFleches(int orientation) {
            Set<Fleche> fleches = null;
            switch(orientation) {
                case HAUT : fleches = readFleches(TOP_ARROW, Fleche.ORIENTATION.HAUT); break;
                case BAS : fleches = readFleches(BOTTOM_ARROW, Fleche.ORIENTATION.BAS); break;
                case DROITE : fleches = readFleches(RIGHT_ARROW, Fleche.ORIENTATION.DROITE); break;
                case GAUCHE : fleches = readFleches(LEFT_ARROW, Fleche.ORIENTATION.GAUCHE); break;
            }
            if(fleches==null) {return new HashSet<>();}
            return fleches;
        }
        
        private Set<Fleche> readFleches(String key, Fleche.ORIENTATION orientation) {
            Set<Fleche> set = new HashSet<>();
            if(getElement(key)!=null) {
                try {
                    Set<Data> donnees = (Set) Json.toJava(getElement(key),HashSet.class);
                    for(Data donnee : donnees) {
                        set.add(new Fleche(orientation, donnee, null));
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return set;
        }
        private String writeFleches(Set<Fleche> set, String orientationKey) {
            Set<Data> donnees = new HashSet<>();
            for(Fleche f : set) { donnees.add(f.getDonnees()); }
            try {
                String s = Json.toJson(donnees);
                putElement(orientationKey, s);
            } catch (IOException ex) {
                Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
    
    interface ModelListener extends TableLayout.TableModelListener {
        void arrowInserted(int direction, Fleche fleche);
        void arrowDeleted(int direction, Fleche fleche);
    }
    
    static class Coord {
        int ligne;
        int colonne;
        Coord(int ligne, int colonne) {
            this.ligne = ligne;this.colonne = colonne;
        }
        Coord() {};
    }
    
}
