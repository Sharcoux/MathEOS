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

import matheos.elements.ChangeModeListener;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataTexte;
import matheos.table.Model.Coord;
import matheos.table.TableEdits.ContentEdit;
import matheos.table.TableLayout.Cell;
import matheos.table.TableLayout.CellFactory;
import static matheos.table.TableLayout.TableModel.COLUMN;
import static matheos.table.TableLayout.TableModel.ROW;
import matheos.table.TableLayout.TableModelListener;
import matheos.table.cells.BasicCell;
import matheos.table.cells.CellTextPane;
import matheos.table.cells.SplitCell;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.interfaces.Editable;
import matheos.utils.interfaces.Undoable;
import matheos.utils.librairies.TransferableTools;
import matheos.utils.managers.CursorManager;
import matheos.utils.managers.GeneralUndoManager;
import matheos.utils.objets.MenuContextuel;
import matheos.utils.objets.Navigation;
import matheos.utils.texte.EditeurKit;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

/**
 * Cette classe contient tous les éléments permettant de représenter et d'afficher le modèle.
 * @author François Billioud
 */
public class Table extends JPanel implements Editable, Undoable, CellFactory {
    private static final int MAX_FONT_SIZE = 30;
    private static final int MIN_FONT_SIZE = 6;
    
    private final static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); // Presse papier pour le copier-coller
    
    private final TableLayout layout;
    private final Model model;
    private final Navigation navigation = new Navigation();
    private GeneralUndoManager undo = new GeneralUndoManager();
    private final EditeurKit editeurKit = new EditeurKit();
    
    private final ChangeModeListener modeListener = new ChangeModeListener(ChangeModeListener.TP);
    
    /** Cellule en cours d'édition **/
    private Cell editingCell = null;
    /** Indique si les cellules peuvent être éditées **/
    private boolean editable = true;
    /** Selection actuelle (cellules) **/
    private final Selection selection = new Selection();
    /** Contenu initial de la cellule en cours d'édition **/
    private DataTexte initialContent = null;
    
    private boolean isFirstCaseSplitted = false;
    
    private boolean shiftPressed = false;
    private boolean mousePressed = false;
    
    public Table(int initialRowCount, int initialColumnCount) {
        //initialisation du model
        model = new Model(this);
        layout = new TableLayout(model,this);
        model.addTableModelListener(modelListener);//Attention, l'ordre est important ici.
        
        //initialisation des cellules
        for(int i=0; i<initialRowCount; i++) {model.insert(ROW,0);}
        for(int j=0; j<initialColumnCount; j++) {model.insert(COLUMN,0);}
        
        //création du layout
        setLayout(layout);
        setOpaque(false);
        
        //écoute des changements de mode
        addMouseListener(modeListener);

//        model.getCell(0, 0).requestFocusInWindow();
//        selection.set(model.getCell(0, 0));
        
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
    
    private final PropertyChangeListener undoListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            //On transmet les messages de disponibilité du UndoManager
            Table.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    };
    public void setUndoManager(GeneralUndoManager undo) {
        this.removePropertyChangeListener(undoListener);
        this.undo = undo;
        undo.addPropertyChangeListener(undoListener);
    }
    
    public GeneralUndoManager getUndoManager() {return undo;}
    
    public void setColoringMode(boolean b) {coloringMode = b;selection.clearSelection();}
    private boolean coloringMode = false;
    /** Permet de colorer les cases par clic **/
    private final MouseAdapter colorFocusListener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if(!coloringMode) {return;}
            Cell c = (Cell)e.getComponent();
            Color oldColor = c.getColor(), newColor;
            boolean isAlreadyColored = !oldColor.equals(Cell.BACKGROUND);
            c.setColor(newColor = isAlreadyColored ? Cell.BACKGROUND : Cell.COLOR_1);
            undo.addEdit(new TableEdits.ColorEdit(editingCell, oldColor, newColor));
        }
    };
//    public TableLayout getTableLayout() {
//        return layout;
//    }
//    
    public Model getTableModel() {
        return model;
    }
    
//    public void insertRow(int i) {model.insertRow(i);}
//    public void insertColumn(int i) {model.insertColumn(i);}
//    public void deleteRow(int i) {model.deleteRow(i);}
//    public void deleteColumn(int i) {model.deleteColumn(i);}
    
    public boolean isEmpty() {
        return model.getRowCount()==0 || model.getColumnCount()==0;
    }

    @Override
    public Dimension getPreferredSize() {
        return layout.getBestSize().plus(1,1);
    }
    
    private int getMiniWidth() {
        Cell[] row = model.get(ROW,0);
        int miniWidth = Integer.MAX_VALUE;
        for(int i=0; i<row.length; i++) {
            miniWidth = Math.min(miniWidth, row[i].getMinimumSize().width);
        }
        return miniWidth;
    }

    private int getMiniHeight() {
        Cell[] column = model.get(COLUMN,0);
        int miniHeight = Integer.MAX_VALUE;
        for(int j=0; j<column.length; j++) {
            miniHeight = Math.min(miniHeight, column[j].getMinimumSize().height);
        }
        return miniHeight;
    }
    
    public Dimension getMinimumCellSize() {
        if(model.getRowCount()==0||model.getColumnCount()==0) {return new Dimension(40,40);}
        return new Dimension(getMiniWidth(),getMiniHeight());
    }

    public Cell getCell(int i, int j) {
        return model.getCell(i, j);
    }
    
    public void prepareTableForPicture() {
        if(isEditing()) {
            Cell old = editingCell;
            stopEdit();
        }
        selection.clearSelection();
    }
    
    public void clear() {
        selection.clearSelection();
        model.clear();
    }
    
    public Data getDonnees() {
        return model.getDonnees();
    }
    
    public void charger(Data dataTable) {
//        clear();
        model.charger(dataTable);
        model.getCell(0, 0).requestFocusInWindow();
        selection.set(model.getCell(0, 0));
    }
    
    private final ModelListener modelListener = new ModelListener();

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
    public void annuler() {if(isEditing()) {editingCell.annuler();} else { undo.annuler(); }}
    @Override
    public void refaire() {if(isEditing()) {editingCell.refaire();} else { undo.refaire(); }}
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
            if(!b) { for(Cell c : model.getAllCells()) {c.setModified(b);} }
        } else {
            if(isEditing()) editingCell.setModified(true);
        }
    }
    
    @Override
    public Cell create() {
        Cell c = new BasicCell(Table.this);
        c.setFontSize(getMaxFontSize());
        return c;
    }
    
    @Override
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
    private class ModelListener implements TableModelListener {
        @Override
        public void rowInserted(Cell[] row, int index) {for(Cell c : row) {cellAdded(c);} revalidate(); repaint(); }
        @Override
        public void columnInserted(Cell[] column, int index) { for(Cell c : column) {cellAdded(c);} revalidate();repaint();}
        @Override
        public void rowDeleted(Cell[] row, int index) { for(Cell c : row) {cellRemoved(c);} revalidate();repaint(); }
        @Override
        public void columnDeleted(Cell[] column, int index) { for(Cell c : column) {cellRemoved(c);} revalidate();repaint(); }
        @Override
        public void contentEdited(Cell c, Object newContent) {repaint();}//redessine les lignes si le contenu s'est agrandit
        @Override
        public void cellReplaced(Cell oldCell, Cell newCell) {Table.this.cellReplaced(oldCell, newCell);repaint();}
        @Override
        public void cleared(Cell[][] table) { for(Cell[] row : table) {rowDeleted(row, 0);} }
    };
    
    private void drawLines() {
        layout.drawLines(this, getGraphics());
    }
    
    private void cellAdded(Cell cell) {
        cell.setEditing(false);
        cell.setEditeurKit(editeurKit);
        JComponent c = cell;
        c.addMouseListener(colorFocusListener);
        c.addMouseListener(modeListener);
        c.addMouseListener(cellListener);
        c.addMouseMotionListener(cellListener);
        c.addPropertyChangeListener(cellListener);
        c.addKeyListener(cellListener);
        c.addFocusListener(cellListener);
        navigation.addComponent(c);
    }
    private void cellRemoved(Cell cell) {
        cell.setEditeurKit(new EditeurKit());
        JComponent c = cell;
        c.removeMouseListener(colorFocusListener);
        c.removeMouseListener(modeListener);
        c.removeMouseListener(cellListener);
        c.removeMouseMotionListener(cellListener);
        c.removePropertyChangeListener(cellListener);
        c.removeFocusListener(cellListener);
        c.removeKeyListener(cellListener);
        navigation.removeComponent(c);
    }
    private void cellReplaced(Cell oldCell, Cell newCell) {
        cellRemoved(oldCell);
        cellAdded(newCell);
        newCell.setFont(oldCell.getFont());
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
    private boolean isEditable() {
        return editable;
    }

    /**
     * Cette classe gère toutes les interactions qu'il peut y avoir entre l'utilisateur et les cases du tableau
     */
    private final CellInteractionListener cellListener = new CellInteractionListener();
    private class CellInteractionListener extends MouseAdapter implements FocusListener, PropertyChangeListener, KeyListener {
        private boolean wasEditing = false;//permet de ne pas envoyer d'évènement lors de l'appuie sur enter(cf keyPressed/keyReleased)
        private boolean isEditing(Cell c) {return editingCell==c;}
        @Override
        public void mouseClicked(MouseEvent e) {
            Cell c = (Cell) e.getComponent();
            //Edition par double clic
            if (SwingUtilities.isLeftMouseButton(e)) {
                if(!isEditing(c)) {
                    if (e.getClickCount() == 2) {editCell(c);}
                }
            } else if(SwingUtilities.isRightMouseButton(e)) {
                //Mise en place d'une case spéciale pour la case (0,0) par clic droit
                if(isEditing(c)) {return;}
                Coord coord = model.getCellCoordinates(c);
                if(coord.colonne==0 && coord.ligne==0) {
                    List<Action> L = new LinkedList<>();
                    L.add(new ActionComplete.Toggle("table first case "+(isFirstCaseSplitted ? "normal" : "splitted"),false) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            model.replaceCell(0, 0, isFirstCaseSplitted ? new BasicCell(Table.this) : new SplitCell(Table.this));
                            isFirstCaseSplitted = !isFirstCaseSplitted;
                        }
                    });
                    MenuContextuel menu = new MenuContextuel(L,e);
                }
            }
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            Cell cell = (Cell) e.getComponent();
            if(isEditing(cell)) {return;}
            if(Table.this.isEditing()) {stopEdit();}
            mousePressed = true;
            if(shiftPressed) {if(selection.depart!=null) {selection.setArrivee(cell);}}
            else {selection.set(cell,null);}
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            mousePressed = false;
        }
        
        @Override
        public void mouseMoved(MouseEvent e) {//FIXME : cette façon de gérer les curseurs est très inefficace. A revoir
            if(Table.this.isEditing()) {return;}
            Cell c = (Cell) e.getComponent();
            Coord coord = model.getCellCoordinates(c);
            if(coord.ligne==0 && coord.colonne==0) {
                c.setCursor(CursorManager.getCursor(Cursor.CUSTOM_CURSOR));
            } else {
                c.setCursor(CursorManager.getCursor(Cursor.DEFAULT_CURSOR));
            }
        }
        @Override
        public void mouseDragged(MouseEvent e) {
            Component c = Table.this.getComponentAt(e.getX()+e.getComponent().getX(), e.getY()+e.getComponent().getY());
            if(!(c instanceof Cell)) {return;}
            Cell cell = (Cell) c;
            if(isEditing(cell)) {return;}
            if(selection.depart==null) {selection.set(cell);}
            else {selection.setArrivee(cell);}
        }
        
        @Override
        public void keyTyped(KeyEvent e) {
            Cell c = (Cell) e.getComponent();
            char code = e.getKeyChar();
            //HACK keyTyped n'est pas envoyé pour les touches non charactères...
            if(isEditing(c) || !isEditable()) {return;}
            else if(code!='\n' && code!='\b' && code!='\u007f' && code!='' && !e.isControlDown() && !e.isActionKey() && !e.isAltDown()) {
                editCell(c);
                c.getCellEditor().selectAll();
                c.getCellEditor().replaceSelection(code+"");
            }
        }
        @Override
        public void keyPressed(KeyEvent e) {
            Cell c = (Cell) e.getComponent();
            int code = e.getKeyCode();
            
            //HACK keyTyped n'est pas envoyé pour les touches non charactères...
            switch(code) {
                case KeyEvent.VK_ENTER :
                    wasEditing = isEditing(c);
                    if(wasEditing) {
                        if(e.isShiftDown()) {//Permet de forcer la création d'une nouvelle ligne
                            try {
                                editingCell.getCellEditor().getDocument().insertString(editingCell.getCellEditor().getCaretPosition(), "\n", null);
                            } catch (BadLocationException ex) {
                                Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            stopEdit();
                            selection.set(editingCell);
                        }
                    }
                    navigation.setKeyEnabled(KeyEvent.VK_ENTER, false);//on désactive enter pour éviter de naviger à la case suivante après validation
                    break;
                case KeyEvent.VK_SHIFT :
                    if(!Table.this.isEditing()) {shiftPressed = true;}
                    break;
                case KeyEvent.VK_DELETE :
                    if(!Table.this.isEditing()) {selection.clearContent();}
                    break;
                case KeyEvent.VK_BACK_SPACE :
                    if(!Table.this.isEditing()) {selection.clearContent();}
                    break;
                case KeyEvent.VK_ESCAPE :
                    if(Table.this.isEditing()) {stopEdit();annuler();}
                    break;
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {
            Cell c = (Cell) e.getComponent();
            int code = e.getKeyCode();
            
            switch(code) {
                case KeyEvent.VK_SHIFT : shiftPressed = false; break;
                case KeyEvent.VK_ENTER ://faire ceci lors du press donnerait lieu à l'insertion d'un "\n"
                    if(!wasEditing && isEditable()) { c.getCellEditor().selectAll(); editCell(c); }
                    navigation.setKeyEnabled(KeyEvent.VK_ENTER, true);
                    break;
            }
        }
        
        @Override
        public void focusGained(FocusEvent e) {
            Cell c = (Cell) e.getComponent();
            Document d = c.getCellEditor().getDocument();
            if(d instanceof StyledDocument) {
                StyledDocument sd = (StyledDocument) d;
                AttributeSet ast = sd.getCharacterElement(0).getAttributes();
                AttributeSet astParagraphe = sd.getParagraphElement(0).getAttributes();
                editeurKit.updateBoutons(astParagraphe, ast);
            }
            if(!isEditing(c)) { c.getCellEditor().selectAll();}//la prise de focus à tendance à modifier les éléments sélectionnés
        }

        @Override
        public void focusLost(FocusEvent e) {
            Component o = e.getOppositeComponent();
            Cell c = (Cell) e.getComponent();
            if(o!=null && o.getParent()==c) {return;}//On ne considère pas les changements de focus vers un élément fils
            
            //HACK : le caret est parfois réactivé à la perte de focus
//            c.getCellEditor().getCaret().setSelectionVisible(false);
//            c.getCellEditor().selectAll();

            if(o instanceof CellTextPane) {
                System.out.println("Table.java ne devrait pas passer ici");
            }
            if(o instanceof Cell) {//On ne désactive le focus que s'il est perdu définitivement. ie pour une nouvelle cellule
                Cell newCell = (Cell) o;
                if(isEditing(newCell)) {return;}
                if(Table.this.isEditing()) {stopEdit();}
                if(selection.depart==null) {
                    selection.set(newCell);
                } else if(shiftPressed || mousePressed) {
                    selection.setArrivee(newCell);
                } else {
                    selection.set(newCell);
                }
            }
            
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(Undoable.MODIFIED)) {
                if(evt.getNewValue().equals(Boolean.TRUE)) {setModified(true);}
            }
            //Si la cellule est en cours d'édition, on transfert le changement au niveu supérieur.
            //valable pour PEUT_ANNULER, PEUT-REFAIRE, PEUT_COPIER, PEUT_COLLER, PEUT_COUPER
            if(evt.getSource() instanceof Cell) {
                Cell c = (Cell) evt.getSource();
                if(isEditing(c)) {
                    Table.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            }
        }
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        layout.drawLines(this, g);
    }
    private void editCell(Cell c) {
        if(!isEditable()) {
            firePropertyChange(OngletTable.MODE_PROPERTY, null, OngletTable.NORMAL);
        }
        if(editingCell!=null) {stopEdit();}
        editingCell = c;
        if(selection.isMultiple()) {selection.set(editingCell);}
        initialContent = c.getDonnees();
        editingCell.setEditing(true);
        editingCell.getCellEditor().getCaret().setVisible(true);//HACK parce que le caret se cache
        editingCell.getCellEditor().getCaret().setSelectionVisible(true);//HACK pour cacher la selection (cf stopEdit)
        if(editingCell.getCellEditor().getClientProperty("selectionStart")!=null) {
            editingCell.getCellEditor().setSelectionStart(Integer.parseInt((String) editingCell.getCellEditor().getClientProperty("selectionStart")));
            editingCell.getCellEditor().setSelectionEnd(Integer.parseInt((String) editingCell.getCellEditor().getClientProperty("selectionEnd")));
        }
        
        c.setModified(false);
        drawLines();
//        editingCell.revalidate();
//        editingCell.repaint();
        //TODO gestion undo
    }

    private void stopEdit() {
        if(editingCell==null) {return;}
        if(editingCell.hasBeenModified()) {
            undo.addEdit(new ContentEdit(editingCell, initialContent, editingCell.getDonnees()));
//            editingCell.discardEdits();
//            editingCell.setModified(false);
        }

        editingCell.getCellEditor().putClientProperty("selectionStart", editingCell.getCellEditor().getSelectionStart()+"");
        editingCell.getCellEditor().putClientProperty("selectionEnd", editingCell.getCellEditor().getSelectionEnd()+"");
        
        editingCell.setEditing(false);
        editingCell.getCellEditor().getCaret().setVisible(false);//HACK pour cacher la selection
        editingCell.getCellEditor().getCaret().setSelectionVisible(false);//HACK pour cacher la selection
//        editingCell.getCellEditor().selectAll();
        
//        revalidate();
        editingCell = null;
        repaint();//On remet en place les lignes
    }

    /**
     * Cette classe gère tout ce qui a trait à la sélection de cases dans le tableau.
     */
    private class Selection implements Editable {
        private int gauche;
        private int haut;
        private int droite;
        private int bas;
        private Coord depart = null;
        private Coord arrivee = null;
        
        private void ordonneColonnes(int x1, int x2) {
            if(x1<x2) {
                gauche = x1; droite = x2;
            } else {
                gauche = x2; droite = x1;
            }
        }
        private void ordonneLignes(int y1, int y2) {
            if(y1<y2) {
                haut = y1; bas = y2;
            } else {
                haut = y2; bas = y1;
            }
        }
        
        public boolean isMultiple() {return (gauche!=droite || haut!=bas);}
        
        public void set(Cell depart, Cell arrivee) {
            Coord origine = depart==null ? null : model.getCellCoordinates(depart);
            Coord destination = arrivee==null ? null : model.getCellCoordinates(arrivee);
            set(origine, destination);
        }
        
        public void set(Cell selectedCell) {
            Coord p = selectedCell==null ? null : model.getCellCoordinates(selectedCell);
            set(p,p);
        }
        
        private void set(Coord origine, Coord destination) {
            select(false);
            setDepart(origine);
            setArrivee(destination);
            select(true);
            Table.this.repaint();//pour redessiner les lignes
        }
        
        private void setDepart(Coord p) {
            boolean isDepartNull = depart==null;
            if(p!=null) {
//                if(arrivee!=null) {//On n'est pas sencé positionner le départ après l'arrivée
//                    ordonneLignes(p.ligne, arrivee.ligne);
//                    ordonneColonnes(p.colonne, arrivee.colonne);
//                } else {
                    gauche = p.colonne; droite = p.colonne; haut = p.ligne; bas = p.ligne;
//                }
            } else {
                gauche = droite = haut = bas = 0;
            }
            this.depart = p;
            if(isDepartNull != (depart==null)) {
                firePropertyChange(PEUT_COUPER, !isDepartNull, isDepartNull);
                firePropertyChange(PEUT_COPIER, !isDepartNull, isDepartNull);
            }
        }
        public void setDepart(Cell c) {
            select(false);
            Coord p = model.getCellCoordinates(c);
            setDepart(p);
            select(true);
            Table.this.repaint();//pour redessiner les lignes
        }
        
        private void setArrivee(Coord p) {
            if(p!=null) {//depart n'est pas null normalement. L'arrivée n'est pas sensé être définie avant le départ
                ordonneLignes(depart.ligne, p.ligne);
                ordonneColonnes(depart.colonne, p.colonne);
            } else {
                if(depart==null) {
                    droite = gauche-1; bas = haut-1;//permet de ne pas passer dans les boucles for
                } else {
                    droite = gauche; bas = haut;
                }
            }
            this.arrivee = p;
        }
        public void setArrivee(Cell c) {
            select(false);
            Coord p = model.getCellCoordinates(c);
            setArrivee(p);
            select(true);
            Table.this.repaint();//pour redessiner les lignes
        }
        
        private void select(boolean b) {
            for(int i=haut; i<=bas; i++) {
                for(int j=gauche; j<=droite; j++) {
                    if(!b || !coloringMode) model.getCell(i, j).setSelected(b);//En mode coloriage, le focus empeche de voir
                }
            }
        }
        
        private void clearSelection() {
            set(null);
        }
        
        private void clearContent() {
            DataTexte[][] previousContent = new DataTexte[bas-haut+1][droite-gauche+1];
            for(int i=haut; i<=bas; i++) {
                for(int j=gauche; j<=droite; j++) {
                    Cell c = model.getCell(i, j);
                    previousContent[i-haut][j-gauche] = c.getDonnees();
                    c.clear();
                    c.discardEdits();
                }
            }
            undo.addEdit(new TableEdits.ClearContentEdit(depart, previousContent, model));
        }

        @Override
        public void couper() {
            copier();
            clearContent();
        }
        @Override
        public void copier() {
            Transferable transfert;
            if(isMultiple()) {
                int n = bas-haut+1, m=droite-gauche+1;
                Cell[][] textPanes = new Cell[n][m];
                for(int i=0; i<n; i++) {
                    for(int j=0; j<m; j++) {
                        textPanes[i][j] = model.getCell(i+haut,j+gauche);
                    }
                }
                transfert = TransferableTools.createTransferableDataTexteArray(textPanes);
            } else {
                Cell c = model.getCell(depart.ligne, depart.colonne);
                transfert = TransferableTools.createTransferableDataTexte(c.getDonnees());
            }
            try {
                clipboard.setContents(transfert, null);
            } catch (IllegalStateException e1) {}
        }
        @Override
        public void coller() {
            try {
                if(clipboard.isDataFlavorAvailable(TransferableTools.matheosArrayFlavor)) {//copie d'un tableau de cases
                    DataTexte[][] data = (DataTexte[][]) clipboard.getData(TransferableTools.matheosArrayFlavor);
                    int n=data.length, m=data[0].length;
                    int nMax=model.getRowCount(), mMax=model.getColumnCount();
                    int nFinal = Math.min(nMax-depart.ligne, n), mFinal = Math.min(mMax-depart.colonne, m);
                    DataTexte[][] oldData = new DataTexte[nFinal][mFinal];
                    DataTexte[][] newData = new DataTexte[nFinal][mFinal];
                    for(int i=0; i<nFinal; i++) {
                        for(int j=0; j<mFinal; j++) {
                            Cell c = model.getCell(i+depart.ligne, j+depart.colonne);
                            newData[i][j] = data[i][j];
                            oldData[i][j] = c.getDonnees();
                            c.charger(data[i][j]);
                        }
                    }
                    undo.addEdit(new TableEdits.ReplaceContentEdit(depart, oldData, newData, model));
                } else {//copie d'un contenu d'une case dans une succession d'autres
                    for(int i=haut; i<=bas; i++) {
                        for(int j=gauche; j<=droite; j++) {
                            Cell c = model.getCell(i, j);
                            DataTexte oldData = c.getDonnees(), newData;
                            c.clear();
                            if(clipboard.isDataFlavorAvailable(TransferableTools.matheosFlavor)) {
                                newData = (DataTexte) clipboard.getData(TransferableTools.matheosFlavor);
                                c.charger(newData);
                            } else if(clipboard.isDataFlavorAvailable(TransferableTools.htmlFlavor)) {
                                String html = (String) clipboard.getData(TransferableTools.htmlFlavor);
                                c.charger(new DataTexte(html));
//                                EditeurIO.importHtml(c, html, 0);
                                newData = c.getDonnees();
                            } else if(clipboard.isDataFlavorAvailable(TransferableTools.textFlavor)) {
                                String text = (String) clipboard.getData(TransferableTools.textFlavor);
                                c.charger(new DataTexte(text));
                                newData = c.getDonnees();
                            } else {
                                newData = new DataTexte("");
                            }
                            undo.addEdit(new ContentEdit(c, oldData, newData));
                        }
                    }
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public boolean peutCouper() {return depart!=null;}
        @Override
        public boolean peutCopier() {return depart!=null;}
        @Override
        public boolean peutColler() {return depart!=null && !isEditing();}

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {}
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {}
    }
    
}
