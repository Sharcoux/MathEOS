/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package matheos.arevoir.inutilise;

import matheos.utils.objets.MenuContextuel;
import matheos.utils.dialogue.DialogueMathAlignement;
import matheos.utils.texte.JMathTextPane;
import matheos.utils.texte.MathTools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import net.sourceforge.jeuclid.swing.JMathComponent;

/**
 *
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class MenuContextuelMathComponent extends MenuContextuel{

    private JMathComponent math;

    public MenuContextuelMathComponent(JMathComponent math){
        super();
        this.math = math;
        this.add(new EditMathAction("Editer symbole"));
        this.add(new EditMathAlignementAction("Modifier alignement"));
    }

    private class EditMathAction extends AbstractAction{
        private EditMathAction(String nom) {super(nom);}
        public void actionPerformed(ActionEvent e) {
            JMathComponent math = MenuContextuelMathComponent.this.math;
            JMathTextPane parent = (JMathTextPane) SwingUtilities.getAncestorOfClass(JMathTextPane.class, math);
            if(parent!=null) {
                parent.setCaretPosition(parent.getMathPosition(math)+1);
//                EditMathManager mathManager = new EditMathManager(math, parent);
//                mathManager.editMath();
                MathTools.edit(math, parent);
            }
        }
    }

    private class EditMathAlignementAction extends AbstractAction{
        private EditMathAlignementAction(String nom) {super(nom);}
        public void actionPerformed(ActionEvent e) {
            JMathComponent math = MenuContextuelMathComponent.this.math;
            JMathTextPane parent = (JMathTextPane) SwingUtilities.getAncestorOfClass(JMathTextPane.class, math);
            if(parent!=null) {
//              if(!ClavierManager.getInstance().hasChild(parent) && parent.isFocusOwner()){
                new DialogueMathAlignement(parent,math);
//              }
            }
        }
    }
}
