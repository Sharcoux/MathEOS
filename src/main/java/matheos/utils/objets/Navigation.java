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

import matheos.utils.texte.JLimitedMathTextPane;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import javax.swing.text.JTextComponent;


/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class Navigation implements Serializable {//PENDING supprimer l'interface serializable après refonte des opérations

    private List<Component> liste = new LinkedList<>();    // Liste qui enregistre les éléments de type "Navigation" créés
    
    private static final int UP = KeyEvent.VK_UP;
    private static final int DOWN = KeyEvent.VK_DOWN;
    private static final int LEFT = KeyEvent.VK_LEFT;
    private static final int RIGHT = KeyEvent.VK_RIGHT;
    private static final int ENTER = KeyEvent.VK_ENTER;
    private static final int TAB = KeyEvent.VK_TAB;
    
    public Navigation() {};
    public Navigation(Component... components) {
        for(Component c : components) {addComponent(c);}
    };
    
    public void addComponent(Component c){
        c.addKeyListener(keyListener);
        liste.add(c);
    }
    
    public void removeComponent(Component c) {
        c.removeKeyListener(keyListener);
        liste.remove(c);
    }
    
    public void clear() {
        liste.clear();
    }
    
    private Map<Integer, Boolean> keysDisabled = new HashMap<>();
    public void setKeyEnabled(int code, boolean b) {
        if(b) {keysDisabled.remove(code);}
        else {keysDisabled.put(code, true);}
    }

    private KeyListener keyListener = new KeyDetection();
    private class KeyDetection extends KeyAdapter implements Serializable {
        @Override
        public void keyPressed(KeyEvent e) {
            if(keysDisabled.get(e.getKeyCode())!=null) {return;}//disactivated key
            treatKey(e.getComponent(), e);
        }
    }
    
    private void treatKey(Component c, KeyEvent e) {
        if(c instanceof JTextComponent) {
            treatKeyOverText((JTextComponent)c, e);
        } else if(c instanceof JComboBox) {
            treatKeyOverCombo((JComboBox)c, e);
        } else {
            treatKeyOverComponent(c, e);
        }
    }
    
    private void treatKeyOverText(JTextComponent text, KeyEvent e) {
        int previousCaretPosition = text.getCaretPosition();
        int keyCode = e.getKeyCode();
        switch(keyCode) {
            case KeyEvent.VK_DOWN : if(isLastLineFocused(text, previousCaretPosition)) {down(text);} break;
            case KeyEvent.VK_UP : if(isFirstLineFocused(text, previousCaretPosition)) {up(text);} break;
            case KeyEvent.VK_LEFT : if(isFirstCharacterFocused(text, previousCaretPosition)) {left(text);} break;
            case KeyEvent.VK_RIGHT : if(isLastCharacterFocused(text, previousCaretPosition)) {right(text);} break;
        }
        if(text instanceof JTextField || 
                (text instanceof JLimitedMathTextPane && ((JLimitedMathTextPane)text).getMaxLines()==1)) {
            if(keyCode==KeyEvent.VK_ENTER) { next(text); }
        }
    }
    private void treatKeyOverCombo(JComboBox combo, KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch(keyCode) {
            case KeyEvent.VK_DOWN : if(isLastObjectFocused(combo)) {down(combo);} break;
            case KeyEvent.VK_UP : if(isFirstObjectFocused(combo)) {up(combo);} else {combo.showPopup();} break;
            case KeyEvent.VK_LEFT : left(combo); break;
            case KeyEvent.VK_RIGHT : right(combo); break;
            case KeyEvent.VK_ENTER : next(combo); break;
        }
    }
    private void treatKeyOverComponent(Component c, KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch(keyCode) {
            case KeyEvent.VK_DOWN : down(c); break;
            case KeyEvent.VK_UP : up(c); break;
            case KeyEvent.VK_LEFT : left(c); break;
            case KeyEvent.VK_RIGHT : right(c); break;
        }
    }
    
    public static final boolean isFirstLineFocused(JTextComponent text, int previousCaretPosition) {
        if(!text.isEditable()) {return true;}//Si le caret n'est pas présent, toutes ces déclarations sont valables
        int pos = previousCaretPosition;//text.getCaretPosition();
        String[] T = text.getText().split("\n");
        return pos <= T[0].length()+1;
    }

    public static final boolean isFirstCharacterFocused(JTextComponent text, int previousCaretPosition) {
        if(!text.isEditable()) {return true;}//Si le caret n'est pas présent, toutes ces déclarations sont valables
        return previousCaretPosition==0;//text.getCaretPosition()==0;
    }

    public static final boolean isLastLineFocused(JTextComponent text, int previousCaretPosition) {
        if(!text.isEditable()) {return true;}//Si le caret n'est pas présent, toutes ces déclarations sont valables
        int pos = previousCaretPosition;//text.getCaretPosition();
        String[] T = text.getText().split("\n");
        int caracterCount = 0;
        for(int i = 0; i<T.length-1; i++) {
            caracterCount+=T[i].length()+1;
        }
        return pos>=caracterCount;
    }

    public static final boolean isLastCharacterFocused(JTextComponent text, int previousCaretPosition) {
        if(!text.isEditable()) {return true;}//Si le caret n'est pas présent, toutes ces déclarations sont valables
        return previousCaretPosition==text.getDocument().getLength();//text.getCaretPosition()==text.getDocument().getLength()
    }
    
    private boolean isLastObjectFocused(JComboBox combo) {
        return combo.getSelectedIndex()==combo.getItemCount()-1;
    }

    private boolean isFirstObjectFocused(JComboBox combo) {
        return combo.getSelectedIndex()==0;
    }
    
    private class Bounds {
        private int top;
        private int left;
        private int right;
        private int bottom;
        Bounds(Component c) {this(c.getLocationOnScreen(),c.getSize());}
        Bounds(Point origine,Dimension size) {this(new Rectangle(origine.x, origine.y, size.width, size.height));}
        Bounds(Rectangle r) {
            top = r.y;
            left = r.x;
            bottom = r.y + r.height;
            right = r.x + r.width;
        }
    }
    
    private int calculXDist(Bounds bSource, Bounds bTarget) {
//        int moy = (bSource.left+bSource.right)/2-(bTarget.left+bTarget.right)/2;
//        return moy*moy;
        if(bTarget.left>=bSource.right) {return bTarget.left - bSource.right+1;}
        else if(bTarget.right<=bSource.left) {return bSource.left-bTarget.right+1;}
        else {return 0;}
    }

    private int calculYDist(Bounds bSource, Bounds bTarget) {//attention, l'axe y se compte de haut en bas
//        int moy = (bSource.top+bSource.bottom)/2-(bTarget.bottom+bTarget.top)/2;
//        return moy*moy;
        if(bTarget.top>=bSource.bottom) {return bTarget.top - bSource.bottom+1;}
        else if(bTarget.bottom<=bSource.top) {return bSource.top - bTarget.bottom+1;}
        else {return 0;}
    }

    private static enum DIRECTION {UP, DOWN, LEFT, RIGHT};
    private Component closest(Component source, DIRECTION direction){
        Component closest = null;
//        int xMinDist = Integer.MAX_VALUE;
//        int yMinDist = Integer.MAX_VALUE;
        int minDist = Integer.MAX_VALUE;
        Bounds bSource = new Bounds(source);
        
        for(Component target : liste) {
            if(target==source || !target.isFocusable() || !target.isShowing()) {continue;}
            Bounds bTarget = new Bounds(target);
            int xDist = 0;
            int yDist = 0;
            int dist = 0;
            
            switch(direction) {
                case UP :
                    xDist = calculXDist(bSource, bTarget)*10;
                    yDist = bSource.top - bTarget.bottom /*+ xDist*xDist*/;//Pénalité sur l'axe x
                    break;
                case DOWN :
                    xDist = calculXDist(bSource, bTarget)*10;
                    yDist = bTarget.top - bSource.bottom /*+ xDist*xDist*/;//Pénalité sur l'axe x
//                    if(yDist==yMinDist) {continue;}//HACK pour privilégier le composant précédent par rapport au suivant
                    break;
                case LEFT :
                    yDist = calculYDist(bSource, bTarget)*10;
                    xDist = bSource.left - bTarget.right /*+ yDist*yDist*/;//Pénalité sur l'axe y
                    break;
                case RIGHT :
                    yDist = calculYDist(bSource, bTarget)*10;
                    xDist = bTarget.left - bSource.right /*+ yDist*yDist*/;//Pénalité sur l'axe y
//                    if(xDist==xMinDist) {continue;}//HACK pour privilégier le composant précédent par rapport au suivant
                    break;
            }
            dist = xDist*xDist+yDist*yDist;
            if(xDist>=0 && yDist>=0 && dist<=minDist) {
//            if(xDist<=xMinDist && xDist>=0) {
//                if(yDist<=yMinDist && yDist>=0) {
                    closest = target;
//                    xMinDist = xDist;
//                    yMinDist = yDist;
                    minDist = dist;
//                }
            }
        }
        return closest;
    }
    
    public void up(Component source) {
        Component closest = closest(source, DIRECTION.UP);
        if(closest!=null) {closest.requestFocusInWindow();}
    }
    
    public void down(Component source) {
        Component closest = closest(source, DIRECTION.DOWN);
        if(closest!=null) {closest.requestFocusInWindow();}
    }
    
    public void left(Component source) {
        Component closest = closest(source, DIRECTION.LEFT);
        if(closest!=null) {closest.requestFocusInWindow();}
    }
    
    public void right(Component source) {
        Component closest = closest(source, DIRECTION.RIGHT);
        if(closest!=null) {closest.requestFocusInWindow();}
    }
    
    public void next(Component source) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
//        Component next = closest(source, DIRECTION.RIGHT);
//        if(next==null) {next = closest(source, DIRECTION.DOWN);}
//        if(next!=null) {
//            Component c = next;
//            while(c!=null) {
//                c = closest(c, DIRECTION.LEFT);
//                if(c!=null) {next = c;}
//            }
//            next.requestFocusInWindow();
//        }
    }
    
    public void previous(Component source) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent(source);
//        Component previous = closest(source, DIRECTION.LEFT);
//        if(previous==null) {previous = closest(source, DIRECTION.UP);}
//        if(previous!=null) {
//            Component c = previous;
//            while(c!=null) {
//                c = closest(c, DIRECTION.RIGHT);
//                if(c!=null) {previous = c;}
//            }
//            previous.requestFocusInWindow();
//        }
    }
    
}
