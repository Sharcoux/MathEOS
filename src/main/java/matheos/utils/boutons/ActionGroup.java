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

package matheos.utils.boutons;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;

/**
 * Cette clase vise à copier la logique des ButtonGroup pour l'adapter aux actions
 * @author François Billioud
 */
public class ActionGroup {

    public static final String ACTION_GROUP = "actionGroup";

    // the list of buttons participating in this group
    protected List<Action> actions = new LinkedList<>();

    /**
     * The current selection.
     */
    Action selection = null;

    /**
     * Creates a new <code>ButtonGroup</code>.
     */
    public ActionGroup() {}

    /**
     * Adds the action to the group.
     * @param a the action to be added
     */
    public void add(Action a) {
        if(a == null) {
            return;
        }
        actions.add(a);

        if (a.getValue(Action.SELECTED_KEY)!=null && a.getValue(Action.SELECTED_KEY).equals(Boolean.TRUE)) {
            if (selection == null) {
                selection = a;
            } else {
                a.putValue(Action.SELECTED_KEY, false);
            }
        }

        a.addPropertyChangeListener(selectionListener);
    }

    /**
     * Removes the action from the group.
     * @param a the action to be removed
     */
    public void remove(Action a) {
        if(a == null) {
            return;
        }
        actions.remove(a);
        if(a == selection) {
            selection = null;
        }
        a.removePropertyChangeListener(selectionListener);
    }

    /**
     * Clears the selection such that none of the actions
     * in the <code>ActionGroup</code> are selected.
     */
    public void clearSelection() {
        if (selection != null) {
            Action oldSelection = selection;
            selection = null;
            oldSelection.putValue(Action.SELECTED_KEY, false);
        }
    }

    /**
     * Returns all the actions that are participating in
     * this group.
     * @return a <code>List</code> of the actions in this group
     */
    public List<Action> getElements() {
        return actions;
    }

    /**
     * Returns the selected action.
     * @return the selected action
     */
    public Action getSelection() {
        return selection;
    }

    /**
     * Sets the selected value for the <code>Action</code>.
     * Only one action in the group may be selected at a time.
     * @param a the <code>Action</code>
     * @param b <code>true</code> if this action is to be
     *   selected, otherwise <code>false</code>
     */
    public void setSelected(Action a, boolean b) {
        if (b && a != null && a != selection) {
            if(actions.contains(a)) {
                Action oldSelection = selection;
                selection = a;
                if (oldSelection != null) {
                    oldSelection.putValue(Action.SELECTED_KEY, false);
                }
            }
            a.putValue(Action.SELECTED_KEY, true);
        }
    }

    /**
     * Returns whether a <code>Action</code> is selected.
     * @return <code>true</code> if the action is selected,
     *   otherwise returns <code>false</code>
     */
    public boolean isSelected(Action m) {
        return (m == selection);
    }

    /**
     * Returns the number of actions in the group.
     * @return the action count
     */
    public int getActionCount() {
	if (actions == null) {
	    return 0;
	} else {
	    return actions.size();
	}
    }

    /** Le listener qui permet, quand une action du groupe est sélectionnée, de déselectionner les autres **/
    private final PropertyChangeListener selectionListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if(!(evt.getSource() instanceof Action)) {return;}
            Action a = (Action)evt.getSource();
            a.removePropertyChangeListener(selectionListener);
            if(evt.getPropertyName().equals(Action.SELECTED_KEY)) {
                if(evt.getNewValue().equals(Boolean.TRUE)) {
                    setSelected(a, true);
                } else {
                    //l'une des lignes au choix selon si on accepte la désélection
//                    if(evt.getSource().equals(selection)) {selection=null;}//on enlève la sélection
                    if(a.equals(selection)) {selection.putValue(Action.SELECTED_KEY, true);}//on empêche la déselection
                }
            }
            a.addPropertyChangeListener(selectionListener);
        }
    };

}
