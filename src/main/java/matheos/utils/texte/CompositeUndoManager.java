/** «Copyright 2013 François Billioud»
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
 */

package matheos.utils.texte;

import matheos.utils.interfaces.Undoable;
import matheos.utils.managers.GeneralUndoManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

/**
 * Classe gérant les undo et redo dans un JTextPane. Les edits sont groupés pour éviter d'annuler
 * caractère par caractère.
 * @author François Billioud
 */
public class CompositeUndoManager extends GeneralUndoManager implements Undoable {

    private CompoundEdit compositeEdit = null;
    
    public boolean isComposing() {
        return compositeEdit!=null;
    }
    
    /** Renvoie les modifications jusqu'à présent.
     * Attention, dans l'état actuel, appeler cette fonction validera les modifications
     * et génèrera un nouvel edit. Deux appels successifs ne renverront donc pas le
     * même edit.
     * @return les modifications jusqu'au moment de l'appel
     */
    public CompoundEdit getComposingEdit() {
        if(compositeEdit==null) {return null;}
        compositeEdit.end();
        return compositeEdit;
    }
    
    /** enregistre les changements dans un edit et recommence un nouvel edit **/
    public void valider() {
        if(compositeEdit==null) {return;}
        compositeEdit.end();
        addEditToUndoManager(compositeEdit);
        compositeEdit=null;
    }
    
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        addEdit(e.getEdit());
    }
    
    @Override
    public boolean addEdit(UndoableEdit edit) {
        setModified(true);
        if(!peutAnnuler()) { firePropertyChange(Undoable.PEUT_ANNULER, false, true); }
        if(compositeEdit==null) {compositeEdit = new CompoundEdit();}
        return compositeEdit.addEdit(edit);
    }
    
    public boolean validateAndAddEdit(UndoableEdit edit) {
        valider();
        return addEditToUndoManager(edit);
    }
    
    private boolean addEditToUndoManager(UndoableEdit anEdit) {
        return super.addEdit(anEdit);
    }
    
    @Override
    public void undo() {
        if(peutAnnuler()) {
            valider();
            super.undo();
        }
    }
    
    @Override
    public void discardAllEdits() {
        compositeEdit = null;
        super.discardAllEdits();
    }

    @Override
    public boolean peutAnnuler() { return canUndo() || isComposing(); }

    @Override
    public boolean peutRefaire() { return canRedo() && !isComposing(); }

}
