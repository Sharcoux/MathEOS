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

import matheos.proportionality.OngletProportionality;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.Data.Enregistrable;
import matheos.sauvegarde.DataObject;
import matheos.sauvegarde.DataTexte;
import matheos.table.TableEdits.ContentEdit;
import matheos.table.TableLayout.Cell;
import matheos.table.TableLayout.TableModel;
import static matheos.table.TableLayout.TableModel.COLUMN;
import static matheos.table.TableLayout.TableModel.ROW;
import matheos.table.cells.BasicCell;
import matheos.table.cells.SplitCell;
import matheos.utils.interfaces.ComponentInsertionListener;
import matheos.utils.interfaces.Editable;
import matheos.utils.interfaces.Undoable;
import matheos.utils.managers.ColorManager;
import matheos.utils.objets.ComponentInsertionSupport;
import matheos.utils.objets.GeneralUndoManager;
import matheos.utils.objets.Navigation;
import matheos.utils.texte.EditeurKit;

/**
 * Cette classe contient tous les éléments permettant de représenter et d'afficher le modèle.
 * @author François Billioud
 */
public class Table extends JPanel implements Editable, Undoable, Enregistrable, TableModel, TableLayout.ContentEditListener {
    private static final int MAX_FONT_SIZE = 30;
    private static final int MIN_FONT_SIZE = 6;
    
    public static final int HAUT = 1;
    public static final int GAUCHE = 2;
    public static final int BAS = 4;
    public static final int DROITE = 8;

    
    public static final String EDITING_PROPERTY = "editing";
    
    private final ArrayList<Line.Row> rowAccess = new ArrayList<>();
    private final ArrayList<Line.Column> columnAccess = new ArrayList<>();
    
    private final TableLayout layout;
    private final Navigation navigation = new Navigation();
    private GeneralUndoManager undo;
    private final EditeurKit editeurKit = new EditeurKit();
    
    /** écoute les interactions avec les cellules. C'est le controlleur **/
    private final CellInteractionListener cellListener;
    
    /** Cellule en cours d'édition **/
    private Cell editingCell = null;
    /** Indique si les cellules peuvent être éditées **/
    private boolean editable = true;
    /** Selection actuelle (cellules) **/
    private final Selection selection;
    /** Contenu initial de la cellule en cours d'édition **/
    private DataTexte initialContent = null;
    
    private boolean firstCaseSplitted = false;
    void setFirstCaseSplitted(boolean b) {
        if(b==firstCaseSplitted) return;
        replaceCell(0, 0, b ? new SplitCell(this) : new BasicCell(this));
        firstCaseSplitted = b;
        revalidate();
        repaint();
    }
    boolean isFirstCaseSplitted() {return firstCaseSplitted;}
    
    public Table(int initialRowCount, int initialColumnCount) {
        //initialisation du model
        selection = new Selection(this);
        selection.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //Tansmet l'état du copier/coller
                Table.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        
        cellListener = new CellInteractionListener(this);
        cellListener.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(Undoable.MODIFIED)) {
                    if(evt.getNewValue().equals(Boolean.TRUE)) {setModified(true);}
                }
                //Si la cellule est en cours d'édition, on transfert le changement au niveu supérieur.
                //valable pour PEUT_ANNULER, PEUT-REFAIRE, PEUT_COPIER, PEUT_COLLER, PEUT_COUPER
                if(evt.getSource() instanceof TableLayout.Cell) {
                    TableLayout.Cell c = (TableLayout.Cell) evt.getSource();
                    if(isEditing(c)) {
                        Table.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                    }
                }
            }
        });
        
        //écoute les changements de status du UndoManager
        undo = new GeneralUndoManager();
        undo.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //On transmet les messages de disponibilité du UndoManager (annuler/refaire)
                Table.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        
        layout = new TableLayout(this,this);
        
        //initialisation des cellules
        for(int i=0; i<initialRowCount; i++) {insert(ROW,0);}
        for(int j=0; j<initialColumnCount; j++) {insert(COLUMN,0);}
        
        //création du layout
        setLayout(layout);
        setOpaque(false);
        

        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "copier");
        getActionMap().put("copier",new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {copier();}});
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), "couper");
        getActionMap().put("couper",new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {couper();}});
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "coller");
        getActionMap().put("coller",new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {coller();}});
        
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "annuler");
        getActionMap().put("annuler",new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {annuler();}});
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "refaire");
        getActionMap().put("refaire",new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {refaire();}});
        
    }
    
    /** Renvoie le UndoManager de la table **/
    public GeneralUndoManager getUndoManager() {return undo;}
    
    /** Renvoie la cellule en cours d'édition **/
    public Cell getEditingCell() {return editingCell;}
    
    /** Renvoie l'objet navigation de la table **/
    public Navigation getNavigation() {return navigation;}
    
    /** Permet d'activer le mode coloriage. Avec ce mode, les clics changent la couleur des cellules. Pas le contenu. **/
    public void setColoringMode(boolean b) {coloringMode = b;selection.clearSelection();}
    public boolean isColoring() {return coloringMode;}
    private boolean coloringMode = false;
    
    public Color getColor(boolean line, int index) {
        return line==ROW ? rowAccess.get(index).getBackground() : columnAccess.get(index).getBackground();
    }
    
    public void setColor(boolean line, int index, Color c) {
        Color old = getColor(line, index);
        if(line==ROW) {
            rowAccess.get(index).setBackground(c);
        } else {
            columnAccess.get(index).setBackground(c);
        }
        for(TableLayout.TableModelListener l : getTableModelListeners()) {l.colorChanged(old, c);}
        repaint();
    }
    
    public Selection getSelection() {return selection;}
    
    /** Permet de colorer les cases par clic **/
    private final MouseAdapter colorFocusListener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if(!coloringMode) {return;}
            Cell c = (Cell)e.getComponent();
            Color oldColor = c.getColor(), newColor;
            boolean isAlreadyColored = !oldColor.equals(c.BACKGROUND);
            c.setColor(newColor = isAlreadyColored ? c.BACKGROUND : c.COLOR_1);
            undo.addEdit(new TableEdits.ColorEdit(editingCell, oldColor, newColor));
        }
    };
    
    public boolean isEmpty() {
        return getRowCount()==0 || getColumnCount()==0;
    }

    @Override
    public Dimension getPreferredSize() {
        return layout.getBestSize().plus(1,1);
    }
    
    private int getMiniWidth() {
        Cell[] row = get(ROW,0);
        int miniWidth = Integer.MAX_VALUE;
        for(int i=0; i<row.length; i++) {
            miniWidth = Math.min(miniWidth, row[i].getMinimumSize().width);
        }
        return miniWidth;
    }

    private int getMiniHeight() {
        Cell[] column = get(COLUMN,0);
        int miniHeight = Integer.MAX_VALUE;
        for(int j=0; j<column.length; j++) {
            miniHeight = Math.min(miniHeight, column[j].getMinimumSize().height);
        }
        return miniHeight;
    }
    
    public Dimension getMinimumCellSize() {
        if(isEmpty()) {return new Dimension(40,40);}
        return new Dimension(getMiniWidth(),getMiniHeight());
    }

    @Override
    public Cell getCell(int i, int j) {
        return rowAccess.get(i).get(j);
    }
    
    @Override
    public TableLayout.Coord getCellCoordinates(Cell c) {
        int n = getRowCount();
        int m = getColumnCount();
        for(int i=0; i<n; i++) {
            Cell[] row = get(ROW,i);
            for(int j=0; j<m; j++) {
                if(c==row[j]) return new TableLayout.Coord(i,j);
            }
        }
        return null;
    }

    @Override
    public Cell[] get(boolean line, int index) {
        ArrayList<Cell> L = line==ROW ? rowAccess.get(index) : columnAccess.get(index);
        return L.toArray(new Cell[getCount(!line)]);
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
    
    /** Appelé avant la capture d'image pour l'insertion du TP dans l'éditeur de texte.
     * La sélection ne doit pas apparaitre dans l'image insérée
     */
    public void prepareTableForPicture() {
        if(isEditing()) {stopEdit();}
        selection.clearSelection();
    }
    
    /** Vide complètement la table **/
    @Override
    public void clear() {
        selection.clearSelection();
        int n = getRowCount(), m = getColumnCount();
        Cell[][] tableContent = new Cell[n][m];
        for(int i = 0; i<n; i++) {
            tableContent[i] = get(ROW, i);
            for(int j=0; j<m; j++) {cellRemoved(tableContent[i][j]);}
        }
        rowAccess.clear();
        columnAccess.clear();
        for(TableLayout.TableModelListener l : getTableModelListeners()) {l.cleared(tableContent);}
    }
    
    @Override
    public DataTable getDonnees() {
        DataTable data = new DataTable();
        int n = getRowCount(), m = getColumnCount();
        //enregistre les nombre de ligne/colonne dans le data global
        data.putElement(DataTable.ROW_COUNT, n+"");
        data.putElement(DataTable.COLUMN_COUNT, m+"");
        //enregistre les données des lignes dans un dataRows, et les cellules dans le data global
        Data dataRows = new DataObject();
        for(int i=0; i<n; i++) {
            Line.Row row = this.rowAccess.get(i);
            dataRows.putData(i+"", row.getDonnees());
            for(int j=0; j<m; j++) {
                data.putData(i+","+j,row.get(j).getDonnees());
            }
        }
        //enregistre les données des colonnes dans un dataColumn
        Data dataColumns = new DataObject();
        for(int j=0; j<m; j++) {
            Line.Column column = this.columnAccess.get(j);
            dataColumns.putData(j+"", column.getDonnees());
        }
        //enregistre les data créés dans le data global
        data.putData(DataTable.ROWS, dataRows);
        data.putData(DataTable.COLUMNS, dataColumns);

        return data;
    }
    
    @Override
    public void charger(Data data) {
        DataTable dataTable;
        if(data instanceof DataTable) {dataTable = (DataTable) data;}
        else {
            dataTable = new DataTable();
            dataTable.putAll(data);
        }
        clear();
        int n = dataTable.getRowCount();
        int m = dataTable.getColumnCount();
        for(int i=0; i<n; i++) {
            Line.Row row = new Line.Row();
            row.charger(dataTable.getDataLine(ROW, i));
            //fait l'insertion dans la liste rowAccess et crée les cellules
            DataTexte[] dataCells = dataTable.getRowCellContent(i);
            for(int j = 0; j<m; j++) {
                Line.Column column;
                if(i==0) {//On initialise la colonne au premier passage
                    column = new Line.Column();
                    column.charger(dataTable.getDataLine(COLUMN, j));
                    columnAccess.add(column);
                } else {//sinon on récupère la colonne dans la liste d'accès
                    column = columnAccess.get(j);
                }
                Cell cell = load(dataCells[j]);
                cellAdded(cell);
                row.add(cell);
                column.add(cell);
            }
            rowAccess.add(i, row);
            
            //prévient les listeners
            Cell[] T = row.toArray(new Cell[row.size()]);
            for(TableLayout.TableModelListener l : getTableModelListeners()) {l.rowInserted(T, i);}
        }
        //met à jour le columnAccess
        for(int j = 0; j<m; j++) {
            Line.Column column = new Line.Column();
            column.charger(dataTable.getDataLine(COLUMN, j));
            for(int i=0; i<n; i++) {
                column.add(rowAccess.get(i).get(j));
            }
        }
        getCell(0, 0).requestFocusInWindow();
        selection.set(getCell(0, 0));
        undo.discardAllEdits();
        revalidate();
        repaint();
    }

    @Override
    public void copier() {
        if(isEditing()) {editingCell.copier();} else {selection.copier();}
    }
    @Override
    public void couper() {if(isEditing()) {editingCell.couper();} else {selection.couper();}}
    @Override
    public void coller() {if(isEditing()) {editingCell.coller();} else {selection.coller();}}
    @Override
    public boolean peutCouper() {return selection.peutCouper() || (isEditing()&&editingCell.peutCouper());}
    @Override
    public boolean peutCopier() {return selection.peutCopier() || (isEditing()&&editingCell.peutCopier());}
    @Override
    public boolean peutColler() {return selection.peutColler() || (isEditing()&&editingCell.peutColler());}
    @Override
    public void annuler() {if(isEditing()) {editingCell.annuler();} else { undo.annuler();requestFocus(); }}
    @Override
    public void refaire() {if(isEditing()) {editingCell.refaire();} else { undo.refaire();requestFocus(); }}
    @Override
    public boolean peutAnnuler() { return undo.peutAnnuler() || (isEditing()&&editingCell.peutAnnuler()); }
    @Override
    public boolean peutRefaire() { return undo.peutRefaire() || (isEditing()&&editingCell.peutRefaire()); }
    @Override
    public boolean hasBeenModified() {return undo.hasBeenModified() || (isEditing()&&editingCell.hasBeenModified());}

    @Override
    public void setModified(boolean b) {
        undo.setModified(b);
        if(!b) {
            if(!b) { for(Cell c : getAllCells()) {c.setModified(b);} }
        } else {
            if(isEditing()) editingCell.setModified(true);
        }
    }
    
    @Override
    public void setEnabled(boolean b) {
        if(!b) {stopEdit();selection.clearSelection();}
        super.setEnabled(b);
        for(Cell c : getAllCells()) {c.setEnabled(b);}
        requestFocus();
    }
    
    public Cell create() {
        Cell c = new BasicCell(Table.this);
        c.setFontSize(getMaxFontSize());
        return c;
    }
    
    public Cell load(Data data) {
        Cell c;
        if(data.containsDataKey(SplitCell.SECOND_TEXT)) {
            c = new SplitCell(this);
        } else {
            c = new BasicCell(Table.this);
        }
        c.charger(data);
        c.setFontSize(getMaxFontSize());
        c.discardEdits();
        return c;
    }
    
    @Override
    public int getMaxFontSize() {
        return MAX_FONT_SIZE;
    }
    
    @Override
    public int getMinFontSize() {
        return MIN_FONT_SIZE;
    }
    
    /**
     * On écoute grâce à cette classe les modifications du modèle.
     */
//    private class ModelListener implements TableModelListener {
//        @Override
//        public void rowInserted(Cell[] row, int index) {for(Cell c : row) {cellAdded(c);} revalidate(); repaint(); }
//        @Override
//        public void columnInserted(Cell[] column, int index) { for(Cell c : column) {cellAdded(c);} revalidate();repaint();}
//        @Override
//        public void rowDeleted(Cell[] row, int index) { for(Cell c : row) {cellRemoved(c);} revalidate();repaint(); }
//        @Override
//        public void columnDeleted(Cell[] column, int index) { for(Cell c : column) {cellRemoved(c);} revalidate();repaint(); }
//        @Override
//        public void contentEdited(Cell c, Object newContent) {repaint();}//redessine les lignes si le contenu s'est agrandit
//        @Override
//        public void cellReplaced(Cell oldCell, Cell newCell) {Table.this.cellReplaced(oldCell, newCell);revalidate();repaint();}
//        @Override
//        public void cleared(Cell[][] table) { for(Cell[] row : table) {rowDeleted(row, 0);} }
//        @Override
//        public void colorChanged(Color oldColor, Color newColor) {repaint();}
//    };
    
    private void drawLines() {
        layout.drawLines(this, getGraphics());
    }
    

    @Override
    public void contentEdited(Cell c) {
        for(TableLayout.TableModelListener l : getTableModelListeners()) {l.contentEdited(c, c.getDonnees());}
    }
    
    @Override
    public Cell replaceCell(int row, int column, Cell newCell) {
        if(row>=getRowCount() || row<0 || column>=getColumnCount() || column<0) {return null;}
        Cell oldCell = rowAccess.get(row).set(column, newCell);
        columnAccess.get(column).set(row, newCell);
        cellRemoved(oldCell);
        cellAdded(newCell);
        for(TableLayout.TableModelListener l : getTableModelListeners()) {l.cellReplaced(oldCell, newCell);}
        return oldCell;
    }
    
    private void cellAdded(Cell cell) {
        cell.addContentEditListener(this);
        cell.setEditeurKit(editeurKit);
        JComponent c = cell;
        c.addMouseListener(colorFocusListener);
        insertionSupport.fireComponentInsertion(c);
        c.addMouseListener(cellListener);
        c.addMouseMotionListener(cellListener);
        c.addPropertyChangeListener(cellListener);
        c.addKeyListener(cellListener);
        c.addFocusListener(cellListener);
        navigation.addComponent(c);
    }
    private void cellRemoved(Cell cell) {
        cell.removeContentEditListener(this);
        cell.setEditeurKit(new EditeurKit());
        JComponent c = cell;
        c.removeMouseListener(colorFocusListener);
        insertionSupport.fireComponentRemoval(c);
        c.removeMouseListener(cellListener);
        c.removeMouseMotionListener(cellListener);
        c.removePropertyChangeListener(cellListener);
        c.removeFocusListener(cellListener);
        c.removeKeyListener(cellListener);
        navigation.removeComponent(c);
    }

    /** Renvoie si une cellule est en cours d'édition **/
    public boolean isEditing() {return editingCell!=null;}
    /** Renvoie l'EditeurKit commun aux cellules **/
    public EditeurKit getEditeurKit() {return editeurKit;}
    /** Définit si les cellules peuvent être éditées **/
    public void setEditable(boolean b) {
        editable = b;
        if(!b && isEditing()) {stopEdit();}
    }
    /** Indique si les cellules peuvent être éditées **/
    public boolean isEditable() {
        return editable;
    }
    
    private ArrayList<Cell> insertRow(int i) {
        //fait l'insertion dans les deux listes d'accès
        Line.Row L = new Line.Row();
        int m = getColumnCount();
        for(int j = 0; j<m; j++) {
            Cell cell = create();
            cellAdded(cell);
            L.add(cell);
            
            Line.Column column = columnAccess.get(j);
            column.applyStyleToCell(cell);
            column.add(i, cell);
        }
        rowAccess.add(i, L);

        //prévient les listeners
        Cell[] T = L.toArray(new Cell[L.size()]);
        for(TableLayout.TableModelListener l : getTableModelListeners()) {l.rowInserted(T, i);}

        return L;
    }

    private ArrayList<Cell> insertColumn(int j) {
        //fait l'insertion dans les deux listes d'accès
        Line.Column L = new Line.Column();
        int n = getRowCount();
        for(int i = 0; i<n; i++) {
            Cell cell = create();
            cellAdded(cell);
            L.add(cell);
            
            Line.Row row = rowAccess.get(i);
            row.add(j, cell);
            row.applyStyleToCell(cell);
        }
        columnAccess.add(j, L);

        //prévient les listeners
        Cell[] T = L.toArray(new Cell[L.size()]);
        for(TableLayout.TableModelListener l : getTableModelListeners()) {l.columnInserted(T, j);}

        return L;
    }
    
    @Override
    public ArrayList<Cell> insert(boolean line, int index) {
        return line==ROW ? insertRow(index) : insertColumn(index);
    }

    protected ArrayList<Cell> deleteRow(int i) {
        //fait la suppression dans les deux listes d'accès
        int m = getColumnCount();
        for(int j = 0; j<m; j++) {
            Cell cell = columnAccess.get(j).remove(i);
            cellRemoved(cell);//vérifier que layout.deleteRow ne suffit pas
        }
        ArrayList<Cell> L = rowAccess.remove(i);

        //prévient les listeners
        Cell[] T = L.toArray(new Cell[L.size()]);
        for(TableLayout.TableModelListener l : getTableModelListeners()) {l.rowDeleted(T, i);}
        
        return L;
    }
    
    protected ArrayList<Cell> deleteColumn(int j) {
        int n = getRowCount();
        for(int i = 0; i<n; i++) {
            Cell cell = rowAccess.get(i).remove(j);
            cellRemoved(cell);
        }
        ArrayList<Cell> L = columnAccess.remove(j);

        //prévient les listeners
        Cell[] T = L.toArray(new Cell[L.size()]);
        for(TableLayout.TableModelListener l : getTableModelListeners()) {l.columnDeleted(T, j);}

        return L;
    }
    
    @Override
    public ArrayList<Cell> delete(boolean line, int index) {
        return line==ROW ? deleteRow(index) : deleteColumn(index);
    }

    /**
     * Cette classe gère toutes les interactions qu'il peut y avoir entre l'utilisateur et les cases du tableau
     */
    boolean isEditing(TableLayout.Cell c) {return editingCell==c;}
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        layout.drawLines(this, g);
    }
    private Color selectionColor;
    public void editCell(Cell c) {
        if(!isEditable()) {
            firePropertyChange(OngletProportionality.MODE_PROPERTY, null, OngletProportionality.NORMAL);
        }
        if(editingCell!=null) {stopEdit();}
        editingCell = c;
        if(selection.isMultiple()) {selection.set(editingCell);}
        initialContent = c.getDonnees();
        
        //HACK pour cacher la selection (cf stopEdit)
        editingCell.getCellEditor().getCaret().setSelectionVisible(true);
        if(selectionColor==null) {selectionColor = editingCell.getCellEditor().getSelectionColor();}
        editingCell.getCellEditor().setSelectionColor(selectionColor);
        
        editingCell.setEditing(true);
        editingCell.getCellEditor().getCaret().setVisible(true);//HACK parce que le caret se cache
        if(editingCell.getCellEditor().getClientProperty("selectionStart")!=null) {
            editingCell.getCellEditor().setSelectionStart(Integer.parseInt((String) editingCell.getCellEditor().getClientProperty("selectionStart")));
            editingCell.getCellEditor().setSelectionEnd(Integer.parseInt((String) editingCell.getCellEditor().getClientProperty("selectionEnd")));
        }
        
        c.setModified(false);
        drawLines();
        firePropertyChange(EDITING_PROPERTY, false, true);
//        editingCell.revalidate();
//        editingCell.repaint();
        //TODO gestion undo
    }

    public void stopEdit() {
        if(!isEditing()) {return;}
        if(editingCell.hasBeenModified()) {
            undo.addEdit(new ContentEdit(editingCell, initialContent, editingCell.getDonnees()));
//            editingCell.discardEdits();
//            editingCell.setModified(false);
        }

        editingCell.getCellEditor().putClientProperty("selectionStart", editingCell.getCellEditor().getSelectionStart()+"");
        editingCell.getCellEditor().putClientProperty("selectionEnd", editingCell.getCellEditor().getSelectionEnd()+"");
        
        editingCell.setEditing(false);
        
        //HACK pour cacher la selection (cf stopEdit)
        editingCell.getCellEditor().getCaret().setVisible(false);//HACK pour cacher la selection
        editingCell.getCellEditor().getCaret().setSelectionVisible(false);//HACK pour cacher la selection
        editingCell.getCellEditor().setSelectionColor(ColorManager.transparent());//HACK pour cacher la selection
//        editingCell.getCellEditor().selectAll();
        
//        revalidate();
        editingCell = null;
        repaint();//On remet en place les lignes
        firePropertyChange(EDITING_PROPERTY, true, false);
    }

    private final ComponentInsertionSupport insertionSupport = new ComponentInsertionSupport();
    public void addComponentInsertionListener(ComponentInsertionListener e) {
        insertionSupport.addComponentInsertionListener(e);
    }
    public void removeComponentInsertionListener(ComponentInsertionListener e) {
        insertionSupport.removeComponentInsertionListener(e);
    }
    
    public TableLayout.TableModelListener[] getTableModelListeners() {
        return listenerList.getListeners(TableLayout.TableModelListener.class);
    }
    
    @Override
    public void addTableModelListener(TableLayout.TableModelListener l) {
        listenerList.add(TableLayout.TableModelListener.class, l);
    }

    @Override
    public void removeTableModelListener(TableLayout.TableModelListener l) {
        listenerList.remove(TableLayout.TableModelListener.class, l);
    }
}
