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
package matheos.operations;

import java.awt.Dimension;
import java.awt.Graphics2D;
import matheos.elements.Onglet.OngletTP;
import matheos.json.Json;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataObject;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.ActionGroup;
import matheos.utils.interfaces.Undoable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import matheos.utils.managers.PermissionManager;

/**
 *
 * @author François Billioud
 */
public class OngletOperations extends OngletTP {

    //nom des données
    private static final String OPERATION = "operation";
    private static final String TYPE = "type";
    
    private OperationType operationActive;
    private Addition addition = new Addition();
    private Soustraction soustraction = new Soustraction();
    private Multiplication multiplication = new Multiplication();
    private Division division = new Division();
    private final PropertyChangeListener changeEventDispatcher = new UndoableStateListener();

    private final ActionGroup group = new ActionGroup();
    private final ActionComplete.Toggle actionAddition = new ActionAddition();
    private final ActionComplete.Toggle actionSoustraction = new ActionSoustraction();
    private final ActionComplete.Toggle actionMultiplication = new ActionMultiplication();
    private final ActionComplete.Toggle actionDivision = new ActionDivision();
    {
        group.add(actionAddition);
        group.add(actionSoustraction);
        group.add(actionMultiplication);
        group.add(actionDivision);
    }
    
    public OngletOperations() {
//        barreOutils.addBoutonOnRight(new ActionNouveau());
        barreOutils.addSeparateurOnRight();
        
        //écoute les modifications
        addition.addPropertyChangeListener(changeEventDispatcher);
        soustraction.addPropertyChangeListener(changeEventDispatcher);
        multiplication.addPropertyChangeListener(changeEventDispatcher);
        division.addPropertyChangeListener(changeEventDispatcher);

//        barreOutils.addSwitchOnRight(new ActionDivision(), null, "operation");
//        barreOutils.addSwitchOnRight(new ActionMultiplication(), null, "operation");
//        barreOutils.addSwitchOnRight(new ActionSoustraction(), null, "operation");
//        barreOutils.addSwitchOnRight(new ActionAddition(), null, "operation");

        barreOutils.addSwitchOnRight(actionDivision);
        barreOutils.addSwitchOnRight(actionMultiplication);
        barreOutils.addSwitchOnRight(actionSoustraction);
        barreOutils.addSwitchOnRight(actionAddition);

        setOperationActive(multiplication); //Opération par défaut
    }

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
        if (getOperationActive() instanceof Addition) {
            addition = (Addition) getOperationActive().nouveau();
            setOperationActive(addition);
        }
        if (getOperationActive() instanceof Soustraction) {
            soustraction = (Soustraction) getOperationActive().nouveau();
            setOperationActive(soustraction);
        }
        if (getOperationActive() instanceof Multiplication) {
            multiplication = (Multiplication) getOperationActive().nouveau();
            setOperationActive(multiplication);
        }
        if (getOperationActive() instanceof Division) {
            division = (Division) getOperationActive().nouveau();
            setOperationActive(division);
        }
    }

    private void setOperationActive(OperationType newOperation) {
        if(newOperation==operationActive) {return;}
        OperationType previousOperation = operationActive;
        
        //HACK a supprimer après la refonte des opérations
        newOperation.addPropertyChangeListener(changeEventDispatcher);
        
        if (previousOperation != null) {
            this.remove(previousOperation);
        }
        operationActive = newOperation;
        setModified(newOperation.hasBeenModified());
        firePropertyChange(Undoable.PEUT_ANNULER, previousOperation==null ? false : previousOperation.peutAnnuler(), newOperation.peutAnnuler());
        firePropertyChange(Undoable.PEUT_REFAIRE, previousOperation==null ? false : previousOperation.peutRefaire(), newOperation.peutRefaire());
        this.add(newOperation);

        if (newOperation instanceof Addition) { actionAddition.setSelected(true); addition = (Addition) operationActive;}
        else if (newOperation instanceof Soustraction) { actionSoustraction.setSelected(true); soustraction = (Soustraction) operationActive;}
        else if (newOperation instanceof Multiplication) { actionMultiplication.setSelected(true); multiplication = (Multiplication) operationActive;}
        else if (newOperation instanceof Division) { actionDivision.setSelected(true); division = (Division) operationActive;}
        revalidate();
        repaint();
    }

    public OperationType getOperationActive() {
        return operationActive;
    }

    private class ActionAddition extends ActionComplete.Toggle {
        {putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ADD,KeyEvent.SHIFT_DOWN_MASK));}
        private ActionAddition() {super("+",false);}
        public void actionPerformed(ActionEvent e) {
            setOperationActive(addition);
        }
    }

    private class ActionSoustraction extends ActionComplete.Toggle {
        {putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,KeyEvent.SHIFT_DOWN_MASK));}
        private ActionSoustraction() {super("-",false);}
        public void actionPerformed(ActionEvent e) {
            setOperationActive(soustraction);
        }
    }

    private class ActionMultiplication extends ActionComplete.Toggle {
        {putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY,KeyEvent.SHIFT_DOWN_MASK));}
        private ActionMultiplication() {super("*",true);}
        public void actionPerformed(ActionEvent e) {
            setOperationActive(multiplication);
        }
    }

    private class ActionDivision extends ActionComplete.Toggle {
        {putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE,KeyEvent.SHIFT_DOWN_MASK));}
        private ActionDivision() {super("/",false);}
        public void actionPerformed(ActionEvent e) {
            setOperationActive(division);
        }
    }

//    private class ActionNouveau extends ActionComplete {
//        private ActionNouveau() {super("new");}
//        public void actionPerformed(ActionEvent e) {
//            if (ecraserTP()) {
//                nouveau();
//            }
//        }
//    }

    @Override
    public Graphics2D capturerImage(Graphics2D g) {
        return operationActive.sauverJPanelDansFileSelonZone(g);
    }
    @Override
    public Dimension getInsertionSize() {
        return new Dimension(operationActive.tailleX1()-operationActive.tailleX0(), operationActive.tailleY1()-operationActive.tailleY0());
    }

    @Override
    public void activeContenu(boolean b) {
        getOperationActive().setEnabled(b);
        setEnabled(b);
    }

    @Override
    public Data getDonneesTP() {
        Data data = new DataObject();
        OperationType operation = getOperationActive();
        data.putElement(TYPE, operation.getClass().getSimpleName());
        operation.removePropertyChangeListener(changeEventDispatcher);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(operation);
            data.putElement(OPERATION, Json.toJson(baos.toByteArray()));
        } catch(IOException e) {
            Logger.getLogger(OngletOperations.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("impossible d'enregistrer l'operation "+operation);
        }
        operation.addPropertyChangeListener(changeEventDispatcher);
        return data;
    }

    @Override
    public void chargement(/*long id, */Data data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) Json.toJava(data.getElement(OPERATION),byte[].class));
            ObjectInputStream in = new ObjectInputStream(bais);
            setOperationActive((OperationType)in.readObject());
            operationActive.setIdTP(getIdTP());
//            setIdTP(id);
        } catch (IOException | ClassNotFoundException e) {
            Logger.getLogger(OngletOperations.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("impossible de charger l'operation");
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

    private class UndoableStateListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            OngletOperations.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }    
}
