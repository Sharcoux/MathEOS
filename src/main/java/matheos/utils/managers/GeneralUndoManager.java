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

package matheos.utils.managers;

import matheos.utils.interfaces.Undoable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 *
 * @author François Billioud
 */
public class GeneralUndoManager extends UndoManager implements Undoable {
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private boolean modified = false;
    
    /** permet de savoir quand une succession de undo/redo ramène au résultat initial **/
    private int marque = 0;
    
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        setModified(true);
        if(!peutAnnuler()) { firePropertyChange(Undoable.PEUT_ANNULER, false, true); }
        addEdit(e.getEdit());
    }
    
    @Override
    public boolean addEdit(UndoableEdit edit) {
        if(!peutAnnuler()) { firePropertyChange(Undoable.PEUT_ANNULER, false, true); }
        marque++;
        setModified(true);
        return super.addEdit(edit);
    }
    
    @Override
    public void redo() {
        if(peutRefaire()) {
            if(!peutAnnuler()) { firePropertyChange(Undoable.PEUT_ANNULER, false, true); }
            super.redo();
            marque++;
            if(!peutRefaire()) { firePropertyChange(Undoable.PEUT_REFAIRE, true, false); }
            setModified(marque!=0);//On est revenu à une ancienne configuration
        }
    }
    
    @Override
    public void undo() {
        if(peutAnnuler()) {
            if(!peutRefaire()) { firePropertyChange(Undoable.PEUT_REFAIRE, false, true); }
            super.undo();
            marque--;
            if(!peutAnnuler()) { firePropertyChange(Undoable.PEUT_ANNULER, true, false); }
            setModified(marque!=0);//On est revenu à une ancienne configuration
        }
    }
    
    @Override
    public void discardAllEdits() {
        super.discardAllEdits();
        setModified(false);//les Edits sont supprimés. On considère donc logiquement que l'editeur n'a pas de modifications
    }

    @Override
    public void annuler() { undo(); }

    @Override
    public void refaire() { redo(); }

    @Override
    public boolean peutAnnuler() { return canUndo(); }

    @Override
    public boolean peutRefaire() { return canRedo(); }

    @Override
    public boolean hasBeenModified() { return modified; }

    @Override
    public void setModified(boolean b) {
        if(modified==b) {return;}
        modified = b;
        firePropertyChange(Undoable.MODIFIED, !b, b);
        if(b==false) {marque = 0;}
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    protected void firePropertyChange(String property, boolean oldValue, boolean newValue) {
        changeSupport.firePropertyChange(property, oldValue, newValue);
    }

}
