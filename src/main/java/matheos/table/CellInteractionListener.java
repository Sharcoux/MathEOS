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

package matheos.table;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import matheos.table.cells.BasicCell;
import matheos.table.cells.CellTextPane;
import matheos.table.cells.SplitCell;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.managers.CursorManager;
import matheos.utils.objets.MenuContextuel;

/**
 *
 * @author François Billioud
 */
    public class CellInteractionListener extends MouseAdapter implements FocusListener, PropertyChangeListener, KeyListener {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private boolean shiftPressed = false;
    private boolean mousePressed = false;
    private final Table table;
    
    public CellInteractionListener(Table table) {
        this.table = table;
    }

    private boolean wasEditing = false;//permet de ne pas envoyer d'évènement lors de l'appuie sur enter(cf keyPressed/keyReleased)
    @Override
    public void mouseClicked(MouseEvent e) {
        TableLayout.Cell c = (TableLayout.Cell) e.getComponent();
        //Edition par double clic
        if (SwingUtilities.isLeftMouseButton(e)) {
            if(!table.isEditing(c)) {
                if (e.getClickCount() == 2) {table.editCell(c);}
            }
        } else if(SwingUtilities.isRightMouseButton(e)) {
            //Mise en place d'une case spéciale pour la case (0,0) par clic droit
            if(table.isEditing(c)) {return;}
            TableLayout.Coord coord = table.getCellCoordinates(c);
            if(coord.colonne==0 && coord.ligne==0) {
                List<Action> L = new LinkedList<>();
                L.add(new ActionComplete.Toggle("table first case "+(table.isFirstCaseSplitted() ? "normal" : "splitted"),false) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        table.replaceCell(0, 0, table.isFirstCaseSplitted() ? new BasicCell(table) : new SplitCell(table));
                        table.setFirstCaseSplitted(!table.isFirstCaseSplitted());
                    }
                });
                MenuContextuel menu = new MenuContextuel(L,e);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        TableLayout.Cell cell = (TableLayout.Cell) e.getComponent();
        if(table.isEditing(cell)) {return;}
        if(table.isEditing()) {table.stopEdit();}
        mousePressed = true;
        if(shiftPressed) {if(table.getSelection().getDepart()!=null) {table.getSelection().setArrivee(cell);}}
        else {table.getSelection().set(cell,null);}
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
        TableLayout.Cell cell = (TableLayout.Cell) e.getComponent();
        if(table.isEditing(cell)) {return;}
        cell.requestFocusInWindow();
    }

    @Override
    public void mouseMoved(MouseEvent e) {//FIXME : cette façon de gérer les curseurs est très inefficace. A revoir
        if(table.isEditing()) {return;}
        TableLayout.Cell c = (TableLayout.Cell) e.getComponent();
        TableLayout.Coord coord = table.getCellCoordinates(c);
        if(coord.ligne==0 && coord.colonne==0) {
            c.setCursor(CursorManager.getCursor(Cursor.CUSTOM_CURSOR));
        } else {
            c.setCursor(CursorManager.getCursor(Cursor.DEFAULT_CURSOR));
        }
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        Component c = table.getComponentAt(e.getX()+e.getComponent().getX(), e.getY()+e.getComponent().getY());
        if(!(c instanceof TableLayout.Cell)) {return;}
        TableLayout.Cell cell = (TableLayout.Cell) c;
        if(table.isEditing(cell)) {return;}
        if(table.getSelection().getDepart()==null) {table.getSelection().set(cell);}
        else {table.getSelection().setArrivee(cell);}
    }

    @Override
    public void keyTyped(KeyEvent e) {
        TableLayout.Cell c = (TableLayout.Cell) e.getComponent();
        char code = e.getKeyChar();
        //HACK keyTyped n'est pas envoyé pour les touches non charactères...
        if(table.isEditing(c) || !table.isEditable()) {return;}
        else if(code!='\n' && code!='\b' && code!='\u007f' && code!='' && !e.isControlDown() && !e.isActionKey() && !e.isAltDown()) {
            table.editCell(c);
            c.getCellEditor().selectAll();
            c.getCellEditor().replaceSelection(code+"");
        }
    }
    @Override
    public void keyPressed(KeyEvent e) {
        TableLayout.Cell c = (TableLayout.Cell) e.getComponent();
        int code = e.getKeyCode();

        //HACK keyTyped n'est pas envoyé pour les touches non charactères...
        switch(code) {
            case KeyEvent.VK_ENTER :
                wasEditing = table.isEditing(c);
                if(wasEditing) {
                    if(e.isShiftDown()) {//Permet de forcer la création d'une nouvelle ligne
                        try {
                            table.getEditingCell().getCellEditor().getDocument().insertString(table.getEditingCell().getCellEditor().getCaretPosition(), "\n", null);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        table.stopEdit();
                        table.getSelection().set(table.getEditingCell());
                    }
                }
                table.getNavigation().setKeyEnabled(KeyEvent.VK_ENTER, false);//on désactive enter pour éviter de naviger à la case suivante après validation
                break;
            case KeyEvent.VK_SHIFT :
                if(!table.isEditing()) {shiftPressed = true;}
                break;
            case KeyEvent.VK_DELETE :
                if(!table.isEditing()) {table.getSelection().clearContent();}
                break;
            case KeyEvent.VK_BACK_SPACE :
                if(!table.isEditing()) {table.getSelection().clearContent();}
                break;
            case KeyEvent.VK_ESCAPE :
                if(table.isEditing()) {table.stopEdit();table.annuler();}
                break;
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
        TableLayout.Cell c = (TableLayout.Cell) e.getComponent();
        int code = e.getKeyCode();

        switch(code) {
            case KeyEvent.VK_SHIFT : shiftPressed = false; break;
            case KeyEvent.VK_ENTER ://faire ceci lors du press donnerait lieu à l'insertion d'un "\n"
                if(!wasEditing && table.isEditable()) { c.getCellEditor().selectAll(); table.editCell(c); }
                table.getNavigation().setKeyEnabled(KeyEvent.VK_ENTER, true);
                break;
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        TableLayout.Cell c = (TableLayout.Cell) e.getComponent();
        Document d = c.getCellEditor().getDocument();
        if(d instanceof StyledDocument) {
            StyledDocument sd = (StyledDocument) d;
            AttributeSet ast = sd.getCharacterElement(0).getAttributes();
            AttributeSet astParagraphe = sd.getParagraphElement(0).getAttributes();
            table.getEditeurKit().updateBoutons(astParagraphe, ast);
        }
        if(!table.isEditing(c)) { c.getCellEditor().selectAll();}//la prise de focus à tendance à modifier les éléments sélectionnés
    }

    @Override
    public void focusLost(FocusEvent e) {
        Component o = e.getOppositeComponent();
        TableLayout.Cell c = (TableLayout.Cell) e.getComponent();
        if(o!=null && o.getParent()==c) {return;}//On ne considère pas les changements de focus vers un élément fils

        //HACK : le caret est parfois réactivé à la perte de focus
//            c.getCellEditor().getCaret().setSelectionVisible(false);
//            c.getCellEditor().selectAll();

        if(o instanceof CellTextPane) {
            System.out.println("Table.java ne devrait pas passer ici");
        }
        if(o instanceof TableLayout.Cell) {//On ne désactive le focus que s'il est perdu définitivement. ie pour une nouvelle cellule
            TableLayout.Cell newCell = (TableLayout.Cell) o;
            if(table.isEditing(newCell)) {return;}
            if(table.isEditing()) {table.stopEdit();}
            if(table.getSelection().getDepart()==null) {
                table.getSelection().set(newCell);
            } else if(shiftPressed || mousePressed) {
                table.getSelection().setArrivee(newCell);
            } else {
                table.getSelection().set(newCell);
            }
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        support.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {support.addPropertyChangeListener(listener);}
    public void removePropertyChangeListener(PropertyChangeListener listener) {support.removePropertyChangeListener(listener);}
}
