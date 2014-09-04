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

package matheos.utils.objets;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.EventListener;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Il suffit d'ajouter ce listener sur un élément pour que les évènements
 * qu'il reçoit soient transmis à la cible comme si les deux ne formaient qu'un
 * Par exemple, le clic sur un bouton sera transmit au panel qui le contient, avec
 * les coordonnées et la source modifiées pour simuler un clic direct sur le panel
 * @author François Billioud
 */public abstract class GlobalDispatcher implements FocusListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    private final JComponent target;

    public GlobalDispatcher(JComponent target) {
        this.target = target;
        
        target.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {mouseEntered = true;}
            @Override
            public void mouseExited(MouseEvent e) {mouseEntered = false;}
        });
        target.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { focused = true; }
            @Override
            public void focusLost(FocusEvent e) {if(!focusCatched) focused = false;}
        });
    }
    private boolean mouseEntered = false;
    private boolean focused = false;
    
    public abstract boolean canDispatch(ComponentEvent e);

    private <T extends EventListener> T[] getListenersAndAdaptEvent(ComponentEvent e, Class<T> c) {
        Component source = e.getComponent();
        if(e instanceof MouseEvent) {
            MouseEvent mouse = (MouseEvent) e;
            java.awt.Point P = SwingUtilities.convertPoint(source, 0, 0, target);//On translate la souris. les x et y sont déjà comptés
            mouse.translatePoint(P.x, P.y);
        }
        e.setSource(target);
        return target.getListeners(c);
    }
    private boolean focusCatched = false;
    @Override
    public void focusGained(FocusEvent evt) {
        focusCatched = evt.getOppositeComponent()==target;//On a récupéré le focus directement depuis la cible
        boolean alreadyFocused = focusCatched || focused;
        focusCatched = true;focused=true;
        if(!canDispatch(evt) || alreadyFocused) {return;}
        FocusEvent e = new FocusEvent(evt.getComponent(), evt.getID(), evt.isTemporary(), evt.getOppositeComponent());
        FocusListener[] listeners = getListenersAndAdaptEvent(e, FocusListener.class);
        for(FocusListener l : listeners) {l.focusGained(e);}
    }
    @Override
    public void focusLost(FocusEvent evt) {
        focusCatched = evt.getOppositeComponent()==target;//On a récupéré le focus directement depuis la cible
        boolean stillFocused = focusCatched;
        focusCatched = false;focused=false;
        if(!canDispatch(evt) || stillFocused) {return;}
        FocusEvent e = new FocusEvent(evt.getComponent(), evt.getID(), evt.isTemporary(), evt.getOppositeComponent());
        FocusListener[] listeners = getListenersAndAdaptEvent(e, FocusListener.class);
        for(FocusListener l : listeners) {l.focusLost(e);}
    }

    @Override
    public void keyTyped(KeyEvent evt) {
        if(!canDispatch(evt)) {return;}
        KeyEvent e = new KeyEvent(evt.getComponent(), evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getKeyCode(), evt.getKeyChar(), evt.getKeyLocation());
        KeyListener[] listeners = getListenersAndAdaptEvent(e, KeyListener.class);
        for(KeyListener l : listeners) {l.keyTyped(e);}
    }

    @Override
    public void keyPressed(KeyEvent evt) {
        if(!canDispatch(evt)) {return;}
        KeyEvent e = new KeyEvent(evt.getComponent(), evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getKeyCode(), evt.getKeyChar(), evt.getKeyLocation());
        KeyListener[] listeners = getListenersAndAdaptEvent(e, KeyListener.class);
        for(KeyListener l : listeners) {l.keyPressed(e);}
    }

    @Override
    public void keyReleased(KeyEvent evt) {
        if(!canDispatch(evt)) {return;}
        KeyEvent e = new KeyEvent(evt.getComponent(), evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getKeyCode(), evt.getKeyChar(), evt.getKeyLocation());
        KeyListener[] listeners = getListenersAndAdaptEvent(e, KeyListener.class);
        for(KeyListener l : listeners) {l.keyReleased(e);}
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        if(!canDispatch(evt)) {return;}
        MouseEvent e = new MouseEvent(evt.getComponent(), evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getX(), evt.getY(), evt.getClickCount(), false, evt.getButton());
        MouseListener[] listeners = getListenersAndAdaptEvent(e, MouseListener.class);
        for(MouseListener l : listeners) {l.mouseClicked(e);}
    }

    @Override
    public void mousePressed(MouseEvent evt) {
        if(!canDispatch(evt)) {return;}
        MouseEvent e = new MouseEvent(evt.getComponent(), evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getX(), evt.getY(), evt.getClickCount(), false, evt.getButton());
        MouseListener[] listeners = getListenersAndAdaptEvent(e, MouseListener.class);
        for(MouseListener l : listeners) {l.mousePressed(e);}
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        if(!canDispatch(evt)) {return;}
        MouseEvent e = new MouseEvent(evt.getComponent(), evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getX(), evt.getY(), evt.getClickCount(), false, evt.getButton());
        MouseListener[] listeners = getListenersAndAdaptEvent(e, MouseListener.class);
        for(MouseListener l : listeners) {l.mouseReleased(e);}
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
        if(!canDispatch(evt) || mouseEntered) {return;}
        MouseEvent e = new MouseEvent(evt.getComponent(), evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getX(), evt.getY(), evt.getClickCount(), false, evt.getButton());
        MouseListener[] listeners = getListenersAndAdaptEvent(e, MouseListener.class);
        for(MouseListener l : listeners) {l.mouseEntered(e);}
    }

    @Override
    public void mouseExited(MouseEvent evt) {
        if(!canDispatch(evt) || !mouseEntered) {return;}
        MouseEvent e = new MouseEvent(evt.getComponent(), evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getX(), evt.getY(), evt.getClickCount(), false, evt.getButton());
        MouseListener[] listeners = getListenersAndAdaptEvent(e, MouseListener.class);
        for(MouseListener l : listeners) {l.mouseExited(e);}
    }

    @Override
    public void mouseDragged(MouseEvent evt) {
        if(!canDispatch(evt)) {return;}
        MouseEvent e = new MouseEvent(evt.getComponent(), evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getX(), evt.getY(), evt.getClickCount(), false, evt.getButton());
        MouseMotionListener[] listeners = getListenersAndAdaptEvent(e, MouseMotionListener.class);
        for(MouseMotionListener l : listeners) {l.mouseDragged(e);}
    }

    @Override
    public void mouseMoved(MouseEvent evt) {
        if(!canDispatch(evt)) {return;}
        MouseEvent e = new MouseEvent(evt.getComponent(), evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getX(), evt.getY(), evt.getClickCount(), false, evt.getButton());
        MouseMotionListener[] listeners = getListenersAndAdaptEvent(e, MouseMotionListener.class);
        for(MouseMotionListener l : listeners) {l.mouseMoved(e);}
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent evt) {
        if(!canDispatch(evt)) {return;}
        MouseWheelEvent e = new MouseWheelEvent(evt.getComponent(), evt.getID(), evt.getWhen(), evt.getModifiers(), evt.getX(), evt.getY(), evt.getClickCount(), false, evt.getScrollType(), evt.getScrollAmount(), evt.getWheelRotation());
        MouseWheelListener[] listeners = getListenersAndAdaptEvent(e, MouseWheelListener.class);
        for(MouseWheelListener l : listeners) {l.mouseWheelMoved(e);}
    }

}
