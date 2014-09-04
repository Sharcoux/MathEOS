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

package matheos.utils.objets;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.EventListener;

/**
 * Il suffit d'ajouter ce listener sur un élément pour que les évènements Mouse
 * qu'il reçoit soient transmis à son parent comme si l'enfant était transparent.
 * Par exemple, le clic sur un bouton sera transmit au panel qui le contient, avec
 * les coordonnées et la source modifiées pour simuler un clic direct sur le panel
 * @author François Billioud
 */
public class DispatchMouseToParent implements MouseMotionListener, MouseListener, MouseWheelListener {

    @Override
    public void mouseMoved(MouseEvent e) {
        MouseMotionListener[] listeners = getListenerAncestor(e, MouseMotionListener.class);
        if(listeners!=null) for(MouseMotionListener l : listeners) l.mouseMoved(e);
    }
    public void mouseDragged(MouseEvent e) {
        MouseMotionListener[] listeners = getListenerAncestor(e, MouseMotionListener.class);
        if(listeners!=null) for(MouseMotionListener l : listeners) l.mouseDragged(e);
    }

    public void mouseClicked(MouseEvent e) {
        MouseListener[] listeners = getListenerAncestor(e, MouseListener.class);
        if(listeners!=null) for(MouseListener l : listeners) l.mouseClicked(e);
    }
    public void mousePressed(MouseEvent e) {
        MouseListener[] listeners = getListenerAncestor(e, MouseListener.class);
        if(listeners!=null) for(MouseListener l : listeners) l.mousePressed(e);
    }
    public void mouseReleased(MouseEvent e) {
        MouseListener[] listeners = getListenerAncestor(e, MouseListener.class);
        if(listeners!=null) for(MouseListener l : listeners) l.mouseReleased(e);
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void mouseWheelMoved(MouseWheelEvent e) {
        MouseWheelListener[] listeners = getListenerAncestor(e, MouseWheelListener.class);
        if(listeners!=null) for(MouseWheelListener l : listeners) l.mouseWheelMoved(e);
    }

    private <T extends EventListener> T[] getListenerAncestor(MouseEvent e, Class<T> c) {
        if(c == null || e == null || e.getComponent() == null) return null;
        java.awt.Point P = e.getComponent().getLocation();
        e.translatePoint(P.x, P.y);

        Container parent = e.getComponent().getParent();
//        while(parent != null) {
        if(parent!=null && Component.class.isInstance(parent)) {
            e.setSource(parent);
            return ((Component)parent).getListeners(c);
        }
        return null;
    }

}
