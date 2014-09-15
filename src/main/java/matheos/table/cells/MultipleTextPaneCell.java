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

package matheos.table.cells;

import matheos.table.Table;
import matheos.table.TableLayout;
import static matheos.table.TableLayout.Cell.BACKGROUND_COLOR;
import static matheos.table.TableLayout.Cell.EDITING_STATE;
import static matheos.table.TableLayout.Cell.SELECTED_STATE;
import static matheos.utils.interfaces.Undoable.MODIFIED;
import matheos.utils.objets.GlobalDispatcher;
import static matheos.utils.objets.Navigation.isFirstCharacterFocused;
import static matheos.utils.objets.Navigation.isFirstLineFocused;
import static matheos.utils.objets.Navigation.isLastCharacterFocused;
import static matheos.utils.objets.Navigation.isLastLineFocused;
import matheos.utils.texte.EditeurKit;
import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.texte.JMathTextPane;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * Cette classe définit les comportements d'une cellule qui contiendrait un ou plusieurs CellTextPanes.
 * @author François Billioud
 */
public abstract class MultipleTextPaneCell extends TableLayout.Cell {
    private final List<JLimitedMathTextPane> listTextPane = new LinkedList<>();
    private final Table tableOwner;
//        private final Navigation navigation = new Navigation();

    /** Contient le textPane en cours d'édition. C'est celui qui recevra le focus à la prochaine édition de la cellule **/
    private JMathTextPane currentEditor;
    private final FocusListener currentEditorListener = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            if(isEditing()) {currentEditor = (JMathTextPane) e.getComponent();}
        }
    };

    private MultipleTextPaneCell(Table tableOwner) {
        this.tableOwner = tableOwner;
        this.globalDispatcher = new CellTextFieldDispatcher();

        setOpaque(true);
        setBackground(TableLayout.Cell.BACKGROUND);
    }

    /** Fournit les textPanes à utiliser pour la cellule. charge à l'utilisateur de les ajouter à la vue **/
    public MultipleTextPaneCell(Table tableOwner, JLimitedMathTextPane... textFields) {
        this(tableOwner);
        for(JLimitedMathTextPane txt : textFields) {addTextPane(txt);}
    }
    /** Les textPanes sont créés et ajoutés à la vue par cette cellule **/
    public MultipleTextPaneCell(int nbTextField, Table tableOwner) {
        this(tableOwner);
        for(int i=0; i<nbTextField; i++) {
            JLimitedMathTextPane txt = new CellTextPane(tableOwner);
            add(txt);
            addTextPane(txt);
        }
    }

    /** ici s'ajoutent les actions à effectuer pour initialiser chaque textPane **/
    protected void addTextPane(JLimitedMathTextPane txt) {
        if(currentEditor==null) {currentEditor = txt;}//on sélectionne le premier éditeur
        listTextPane.add(txt);
        txt.getDocument().addDocumentListener(changeListener);
        txt.addFocusListener(currentEditorListener);
        txt.addPropertyChangeListener(textChangeListener);
        txt.addKeyListener(globalDispatcher);
        txt.addMouseListener(globalDispatcher);
        txt.addMouseMotionListener(globalDispatcher);
        txt.addMouseWheelListener(globalDispatcher);
        txt.addFocusListener(globalDispatcher);
//            navigation.addComponent(txt);
    }

    private final PropertyChangeListener textChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(MODIFIED)) {
                if((boolean)evt.getNewValue()) {
                    setModified(true);
                } else {
                    if(modified==false) {return;}
                    JMathTextPane c = (JMathTextPane)evt.getSource();
                    for(JLimitedMathTextPane txt : listTextPane) {//Si tous les autres n'ont aucune modification en cours, l'état passe à modified:false
                        if(txt!=c && txt.hasBeenModified()) {return;}
                    }
                    setModified(false);
                }
            } else {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        }
    };

    @Override
    public void setFontSize(int f) {
        for(JLimitedMathTextPane txt : listTextPane) {txt.setFontSize(f);}
    }
    @Override
    public int getFontSize() {
        return getCellEditor().getFont().getSize();
    }

    private boolean editing = true;
    @Override
    public boolean isEditing() {return editing;}
    @Override
    public void setEditing(boolean b) {
        if(b==editing) {return;}
        editing = b;
        if(b) {//L'ordre est important car le focus est transmit imméditement lors de l'appel à setFocusable(false)
            for(JLimitedMathTextPane txt : listTextPane) {txt.setEditable(true);}
            setFocusable(false);
            getCellEditor().requestFocusInWindow();
        } else {
            setFocusable(true);
            for(JLimitedMathTextPane txt : listTextPane) {txt.setEditable(false);}
        }
        firePropertyChange(EDITING_STATE,!b,b);
        setBackgroundManual(b ? Color.WHITE : (isSelected() ? TableLayout.Cell.FOCUSED_COLOR : getColor()));
    }

    private Color color = TableLayout.Cell.BACKGROUND;
    @Override
    public Color getColor() { return color; }
    @Override
    public void setColor(Color c) {
        if(c==null ? color==null : c.equals(color)) {return;}
        Color old = color;
        color = c;
        if(!isSelected()) {
            setBackgroundManual(color);
        }
        setModified(true);
        firePropertyChange(BACKGROUND_COLOR, old, color);
    }

    @Override
    public void setBackgroundManual(Color c) {
        for(JLimitedMathTextPane txt : listTextPane) {txt.setBackgroundManual(c);}
        setBackground(c);
    }

    @Override
    public void setCursor(Cursor c) {
        for(JLimitedMathTextPane txt : listTextPane) {txt.setCursor(c);}
        super.setCursor(c);
    }

    private boolean selected = false;
    @Override
    public boolean isSelected() { return selected; }
    @Override
    public void setSelected(boolean b) {
        if(b==selected) {return;}
        selected = b;
        firePropertyChange(SELECTED_STATE,!b,b);
        setBackgroundManual(b ? TableLayout.Cell.FOCUSED_COLOR : getColor());
    }

    @Override
    public JTextComponent getCellEditor() {
        return currentEditor;
    }

    @Override
    public void setEditeurKit(EditeurKit editeur) {
        for(JLimitedMathTextPane txt : listTextPane) {txt.setEditeurKit(editeur);}
    }

    @Override
    public void clear() {
        for(JLimitedMathTextPane txt : listTextPane) {txt.clear();}
    }

    @Override
    public void discardEdits() {
        for(JLimitedMathTextPane txt : listTextPane) {txt.getUndo().discardAllEdits();}
    }

    private final List<TableLayout.ContentEditListener> contentListeners = new LinkedList<>();
    @Override
    public void addContentEditListener(TableLayout.ContentEditListener listener) {
        contentListeners.add(listener);
    }
    @Override
    public void removeContentEditListener(TableLayout.ContentEditListener listener) {
        contentListeners.remove(listener);
    }
    protected void fireContentEditChanged() {
        for(TableLayout.ContentEditListener listener : contentListeners) {listener.contentEdited(this);}
    }
    private final DocumentListener changeListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {fireContentEditChanged();}
        @Override
        public void removeUpdate(DocumentEvent e) {fireContentEditChanged();}
        @Override
        public void changedUpdate(DocumentEvent e) {}
    };

    //XXX On peut envisager de doter la cellule d'un UndoManager propre... A voir
    @Override
    public void annuler() { if(isEditing()) {currentEditor.annuler();} else {tableOwner.annuler();} }
    @Override
    public void refaire() { if(isEditing()) {currentEditor.refaire();} else {tableOwner.refaire();} }
    @Override
    public boolean peutAnnuler() { return isEditing() ? currentEditor.peutAnnuler() : tableOwner.peutAnnuler(); }
    @Override
    public boolean peutRefaire() { return isEditing() ? currentEditor.peutRefaire() : tableOwner.peutRefaire(); }
    @Override
    public void copier() { if(isEditing()) {currentEditor.copier();} else {tableOwner.copier();} }
    @Override
    public void coller() { if(isEditing()) {currentEditor.coller();} else {tableOwner.coller();} }
    @Override
    public void couper() { if(isEditing()) {currentEditor.couper();} else {tableOwner.couper();} }
    @Override
    public boolean peutCopier() { return isEditing() ? currentEditor.peutCopier() : tableOwner.peutCopier(); }
    @Override
    public boolean peutColler() { return isEditing() ? currentEditor.peutColler() : tableOwner.peutColler(); }
    @Override
    public boolean peutCouper() { return isEditing() ? currentEditor.peutCouper() : tableOwner.peutCouper(); }

    private boolean modified = false;
    @Override
    public boolean hasBeenModified() { return modified; }
    @Override
    public void setModified(boolean b) {
        if(b==modified) {return;}
        modified = b;
        if(!b) { for(JLimitedMathTextPane txt : listTextPane) {txt.setModified(b);} }
        firePropertyChange(MODIFIED,!b,b);
    }

    private final CellTextFieldDispatcher globalDispatcher;
    /** Classe qui permet de rediriger depuis la cellule les évènements ayant lieu sur l'un de ses textPane **/
    private class CellTextFieldDispatcher extends GlobalDispatcher {
        private CellTextFieldDispatcher() { super(MultipleTextPaneCell.this); }
        @Override
        public boolean canDispatch(ComponentEvent e) {
            if(!isEditing()) {return true;}
            else {
                if(e instanceof KeyEvent) {
                    KeyEvent key = (KeyEvent)e;
                    if(key.getKeyCode()==KeyEvent.VK_ENTER) { return true; }
                    //HACK on ne transmet les touches des flèches que lorsqu'on arrive aux extremités du textPane
                    if(key.getKeyCode()==KeyEvent.VK_UP || key.getKeyCode()==KeyEvent.VK_DOWN || key.getKeyCode()==KeyEvent.VK_LEFT || key.getKeyCode()==KeyEvent.VK_RIGHT) {
                        JTextComponent text = getCellEditor();
                        int pos = text.getCaretPosition();
                        boolean r = false;
                        switch(key.getKeyCode()) {
                            case KeyEvent.VK_DOWN : if(isLastLineFocused(text, pos)) {r=true;} break;
                            case KeyEvent.VK_UP : if(isFirstLineFocused(text, pos)) {r=true;} break;
                            case KeyEvent.VK_LEFT : if(isFirstCharacterFocused(text, pos)) {r=true;} break;
                            case KeyEvent.VK_RIGHT : if(isLastCharacterFocused(text, pos)) {r=true;} break;
                        }
                        if(r) {return true;}
                    }
                } else {
                    if(e instanceof FocusEvent) {return true;}
                }
                return false;
            }
        }
    }
}

