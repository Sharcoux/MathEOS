/** «Copyright 2011 François Billioud»
 *
 * This file is part of Bomehc.
 *
 * Bomehc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bomehc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Bomehc. If not, see <http://www.gnu.org/licenses/>.
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
 */

package bomehc.utils.boutons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.JPanel;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class PanelAction extends JPanel implements MouseListener {

    public PanelAction() {
        super();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        addMouseListener(this);
    }


    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        ActionEvent event = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"ActionPerformed");
        fireActionPerformed(event);
    }

    /**
     * Adds an <code>ActionListener</code> to the button.
     * @param l the <code>ActionListener</code> to be added
     */
    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    /**
     * Removes an <code>ActionListener</code> from the button.
     * @param l the listener to be removed
     */
    public void removeActionListener(ActionListener l) {
	listenerList.remove(ActionListener.class, l);
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the <code>event</code>
     * parameter.
     *
     * @param event  the <code>ActionEvent</code> object
     */
    public void fireActionPerformed(ActionEvent event) {
        for(ActionListener l : listenerList.getListeners(ActionListener.class)) {
            l.actionPerformed(event);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(enabled){
            if(Arrays.binarySearch(listenerList.getListeners(MouseListener.class), this) < 0){
                this.addMouseListener(this);
            }
        }else{
            this.removeMouseListener(this);
        }
    }
    
    

}
