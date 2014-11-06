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

import matheos.texte.composants.ComposantTexte;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import net.sourceforge.jeuclid.swing.JMathComponent;
import java.awt.Component;
import java.awt.event.MouseListener;

/**
 * Classe gérant la sélection des MathComponent dans un JMathTextPane. Lorsqu'un
 * JMathComponent est sélectionné, sa couleur change.
 *
 * @author François Billioud
 */
public class ComponentSelectionListener implements MouseMotionListener, CaretListener, FocusListener, MouseListener {

    private final JMathTextPane jtp;

    public ComponentSelectionListener(JMathTextPane jtp) {this.jtp = jtp;}
    
    public void mouseDragged(MouseEvent e) {
        verifierSelection();
    }

    public void mouseMoved(MouseEvent e) {}

    public void caretUpdate(CaretEvent e) {
        verifierSelection();
    }

    public void focusGained(FocusEvent e) {
        verifierSelection();
    }

    public void focusLost(FocusEvent e) {
        for(Component c : jtp.getComponentMap().values()) {
            if(c instanceof JMathComponent) {
                MathTools.setSelected((JMathComponent) c, false);
            } else if(c instanceof ComposantTexte) {
                ((ComposantTexte) c).setSelected(false);
            }
        }
        //afin de revérifier la sélection lorsqu'on récupère le focus
        oldSelectionStart = 0;
        oldSelectionEnd = 0;
    }

    private int oldSelectionStart = 0;
    private int oldSelectionEnd = 0;
    
    private void verifierSelection() {
        boolean modificationOccured = false;
        int selectionStart = jtp.getSelectionStart();
        int selectionEnd = jtp.getSelectionEnd();
        
        if(selectionStart<oldSelectionStart) {
            for(int i=selectionStart;i<Math.min(selectionEnd, oldSelectionStart);i++) {
                setSelected(i,true);
                modificationOccured = true;
            }
        }
        if(oldSelectionStart<selectionStart) {
            for(int i=oldSelectionStart;i<Math.min(selectionStart, oldSelectionEnd);i++) {
                setSelected(i,false);
                modificationOccured = true;
            }
        }
        if(selectionEnd>oldSelectionEnd) {
            for(int i=Math.max(oldSelectionEnd, selectionStart);i<selectionEnd;i++) {
                setSelected(i,true);
                modificationOccured = true;
            }
        }
        if(oldSelectionEnd>selectionEnd) {
            for(int i=Math.max(selectionEnd, oldSelectionStart);i<oldSelectionEnd;i++) {
                setSelected(i,false);
                modificationOccured = true;
            }
        }
        if(modificationOccured) {jtp.repaint();}
        oldSelectionStart = selectionStart;
        oldSelectionEnd = selectionEnd;
//        //les images anciennement sélectionnées sont à déselectionner
//        for(JMathComponent math : foregroundColors.keySet()) {deselectionner(math);}
//        for(JLabelImage img : imagesSelected) {deselectionner(img);}
//        //on resélectionne les images encore dans la sélection
//        for(int i=jtp.getSelectionStart();i<jtp.getSelectionEnd();i++) {
//            Component c = jtp.getComponentAt(i);
////            Component c = (Component)jtp.getHTMLdoc().getCharacterElement(i).getAttributes().getAttribute(StyleConstants.ComponentAttribute);
//            if(c instanceof JMathComponent) { selectionner((JMathComponent) c);}
//            else if(c instanceof JLabelImage) { selectionner((JLabelImage) c); }
//            else if(jtp.isComponentPosition(i)) System.out.println("Pb");
//        }
    }
    
    private void setSelected(int i, boolean b) {
            Component c = jtp.getComponentAt(i);
            if(c instanceof JMathComponent) { MathTools.setSelected((JMathComponent) c, b);}
            else if(c instanceof ComposantTexte) { ((ComposantTexte) c).setSelected(b);}
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        verifierSelection();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}

