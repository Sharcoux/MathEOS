/** «Copyright 2011 François Billioud»
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
package matheos.operations2;

import java.awt.Dimension;
import java.awt.Graphics2D;
import matheos.elements.Onglet.OngletTP;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataObject;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.ActionGroup;
import matheos.utils.interfaces.Undoable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static matheos.operations2.Operation.*;
import matheos.utils.dialogue.DialogueBloquant;
import matheos.utils.managers.PermissionManager;

/**
 *
 * @author François Billioud
 */
public class OngletOperations extends OngletTP {
    
    private static final String OPERATION = "type";
    private static final String CONTENT = "content";

    private Operation operationActive;
    private final Map<String, Operation> operations = new HashMap<>();
    {
        operations.put(ADDITION, new Addition());
        operations.put(SOUSTRACTION, new Soustraction());
        operations.put(MULTIPLICATION, new Multiplication());
        operations.put(DIVISION, new Division());
    }

    private final ActionGroup group = new ActionGroup();
    private final Map<String, ActionComplete.Toggle> actionsOperations = new HashMap<>();
    {
        actionsOperations.put(ADDITION, new ActionAddition());
        actionsOperations.put(SOUSTRACTION, new ActionSoustraction());
        actionsOperations.put(MULTIPLICATION, new ActionMultiplication());
        actionsOperations.put(DIVISION, new ActionDivision());
        for(ActionComplete action : actionsOperations.values()) {
            group.add(action);
        }
    }
    
    private final PropertyChangeListener operationListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            OngletOperations.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    };
    
    public OngletOperations() {
        barreOutils.addSeparateurOnRight();
        barreOutils.addSwitchOnRight(actionsOperations.get(ADDITION));
        barreOutils.addSwitchOnRight(actionsOperations.get(SOUSTRACTION));
        barreOutils.addSwitchOnRight(actionsOperations.get(MULTIPLICATION));
        barreOutils.addSwitchOnRight(actionsOperations.get(DIVISION));
        
        //écoute les modifications
        this.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                OngletOperations.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });//Attention, il faut aussi écouter les opérations qu'on ajoute à l'onglet

        setOperationActive(getMultiplication()); //Opération par défaut
    }
    
    private Operation getAddition() {return operations.get(ADDITION);}
    private Operation getSoustraction() {return operations.get(SOUSTRACTION);}
    private Operation getMultiplication() {return operations.get(MULTIPLICATION);}
    private Operation getDivision() {return operations.get(DIVISION);}

    /**
     * Redéfinition de la méthode getIdTP() de l'onglet pour renvoyer l'id de l'opération active.
     * @return l'id du JLabelTP associée à l'opération active
     */
    @Override
    public long getIdTP() {
        return operationActive.getIdTP();
    }

    /**
     * Redéfinition de la méthode setIdTP() de l'onglet pour insérer l'id dans l'opération active.
     * @param idTP l'id du JLabelTP correspondant à l'opération active
     */
    @Override
    public void setIdTP(long idTP) {
        super.setIdTP(idTP);
        operationActive.setIdTP(idTP);
    }

    @Override
    public void setActionEnabled(PermissionManager.ACTION actionID, boolean b) {
    }
    
    @Override
    protected void nouveauTP() {
        getOperationActive().nouveau();
    }

    private void setOperationActive(Operation newOperation) {
        if(newOperation==operationActive) {return;}
        Operation previousOperation = operationActive;
        
        if (previousOperation != null) {
            this.remove(previousOperation);
            previousOperation.removePropertyChangeListener(operationListener);
        }
        
        operationActive = newOperation;
        setModified(newOperation.hasBeenModified());
        firePropertyChange(Undoable.PEUT_ANNULER, previousOperation==null ? false : previousOperation.peutAnnuler(), newOperation.peutAnnuler());
        firePropertyChange(Undoable.PEUT_REFAIRE, previousOperation==null ? false : previousOperation.peutRefaire(), newOperation.peutRefaire());
        
        this.add(newOperation);
        newOperation.addPropertyChangeListener(operationListener);

        actionsOperations.get(newOperation.getName()).setSelected(true);
        revalidate();
        repaint();
    }

    public Operation getOperationActive() {
        return operationActive;
    }

    private class ActionOperation extends ActionComplete.Toggle {
        private final String operation;
        private ActionOperation(String operation) {super(operation,false);this.operation = operation;}
        @Override
        public void actionPerformed(ActionEvent e) {
            setOperationActive(operations.get(this.operation));
        }
    }
    
    private class ActionAddition extends ActionOperation {
//        {putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ADD,KeyEvent.SHIFT_DOWN_MASK));}
        private ActionAddition() {super(ADDITION);}
    }

    private class ActionSoustraction extends ActionOperation {
//        {putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,KeyEvent.SHIFT_DOWN_MASK));}
        private ActionSoustraction() {super(SOUSTRACTION);}
    }

    private class ActionMultiplication extends ActionOperation {
//        {putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY,KeyEvent.SHIFT_DOWN_MASK));}
        private ActionMultiplication() {super(MULTIPLICATION);}
    }

    private class ActionDivision extends ActionOperation {
//        {putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE,KeyEvent.SHIFT_DOWN_MASK));}
        private ActionDivision() {super(DIVISION);}
    }

    @Override
    public Graphics2D capturerImage(Graphics2D g) {
        return operationActive.capturerImage(g);
    }
    @Override
    public Dimension getInsertionSize() {
        return operationActive.getSize();
    }

    @Override
    public void activeContenu(boolean b) {
        getOperationActive().setEnabled(b);
        setEnabled(b);
    }

    @Override
    public Data getDonneesTP() {
        Data data = new DataObject();
        data.putElement(OPERATION, operationActive.getName());
        data.putData(CONTENT, operationActive.getDonnees());
        return data;
    }

    @Override
    public void chargement(/*long id, */Data data) {
        try {
            Operation op = operations.get(data.getElement(OPERATION));
            op.charger(data.getData(CONTENT));
            setOperationActive(op);
        } catch (Exception ex) {
            DialogueBloquant.error("error reading TP");
            Logger.getLogger(OngletOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void zoomP() {
    }

    @Override
    public void zoomM() {
    }

    public void annuler() {
        operationActive.annuler();
    }

    public void refaire() {
        operationActive.refaire();
    }

    public boolean peutAnnuler() {
        return operationActive.peutAnnuler();
    }

    public boolean peutRefaire() {
        return operationActive.peutRefaire();
    }

    @Override
    public boolean hasBeenModified() {
        return operationActive.hasBeenModified();//TODO : mettre en place les annuler/refaire sur les opérations
    }
    
    @Override
    public void setModified(boolean b) {
        operationActive.setModified(b);//TODO : mettre en place les annuler/refaire sur les opérations
    }
}
