/**
 * «Copyright 2013 François Billioud»
 *
 * This file is part of MathEOS.
 *
 * MathEOS is free software: you can redistribute it and/or modify under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * MathEOS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY.
 *
 * You should have received a copy of the GNU General Public License along with
 * MathEOS. If not, see <http://www.gnu.org/licenses/>.
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
package matheos.graphic;

import matheos.graphic.composants.ComposantGraphique;
import matheos.utils.interfaces.Undoable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author François Billioud
 */
public class UndoableListComposant extends ListComposant implements Serializable, Cloneable, Undoable {
    private static final long serialVersionUID = 1L;

    private CommandeList commande = new CommandeList(this);

    public UndoableListComposant() {
        commande = new CommandeList(this);
        commande.addPropertyChangeListener(new UndoableStateListener());
    }

    @Override
    public boolean add(ComposantGraphique o) {
        ComposantGraphique existing = get(o);
        if(existing!=null) {
            //XXX envisager l'ajout d'une commande pour les changements de couleur
            existing.setCouleur(o.getCouleur());
            existing.setNom(o.getNom());
            existing.setPointille(o.isPointille());
            return false;
        } else {
            addUndoCommande(commande.new AddCommande(o));
            return super.add(o);
        }
    }

    @Override
    public boolean addAll(Collection<? extends ComposantGraphique> c) {
        if(c.isEmpty()) {return false;}
        addUndoCommande(commande.new AddAllCommande(c));
        return super.addAll(c);
    }

    @Override
    public boolean remove(Object o) {
        if(!(o instanceof ComposantGraphique) || !contient((ComposantGraphique)o)) {return false;}
        addUndoCommande(commande.new RemoveCommande((ComposantGraphique) o));
        return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> L) {
        ListComposant toRemove = new ListComposant();
        for(Object o : L) {if((o instanceof ComposantGraphique) && contient((ComposantGraphique)o)) {toRemove.add((ComposantGraphique)o);}}
        if(toRemove.isEmpty()) {return false;}
        addUndoCommande(commande.new RemoveAllCommande(L));
        return super.removeAll(L);
    }

    @Override
    public void clear() {
        if(this.isEmpty()) {return;}
        addUndoCommande(commande.new ClearCommande());
        super.clear();
    }

    /**
     * permet de vider les actions enregistrées dans les listes Undo et Redo.
     */
    public void clearUndoRedo() {
        if(peutAnnuler()) { firePropertyChange(Undoable.PEUT_ANNULER, true, false); }
        if(peutRefaire()) { firePropertyChange(Undoable.PEUT_REFAIRE, true, false); }
        commande.undoList.clear();
        commande.redoList.clear();
        commande.setModified(false);
    }

    /**
     * Permet de charger une UndoableListComposant dans la liste actuelle
     * @param L la liste à charger
     * @param conservateUndo précise si la liste des actions annulable doit être chargée aussi
     */
    public void charger(UndoableListComposant L, boolean conservateUndo) {
        charger(L);
        if(conservateUndo) {
            commande.undoList = L.commande.undoList;
            commande.modified = L.commande.modified;
        }
    }

    /**
     * Permet de charger une UndoableListComposant dans la liste actuelle
     * @param L la liste à charger
     */
    public void charger(ListComposant L) {
        super.clear();
        super.addAll(L);
        if(peutAnnuler()) { firePropertyChange(Undoable.PEUT_ANNULER, true, false); }
        if(peutRefaire()) { firePropertyChange(Undoable.PEUT_REFAIRE, true, false); }
        this.commande = new CommandeList(this);
        commande.addPropertyChangeListener(new UndoableStateListener());
        setModified(false);
//        IHM.updateUndoRedo();
    }

    private boolean addUndoCommande(Commande c) {
        return commande.addUndoableCommande(c);
    }

    protected List<Commande> getUndoList() {return commande.undoList;}
    protected List<Commande> getRedoList() {return commande.redoList;}

    public void annuler() {commande.annuler();}
    public void refaire() {commande.refaire();}
    public boolean peutAnnuler() {return commande.peutAnnuler();}
    public boolean peutRefaire() {return commande.peutRefaire();}
    public void setModified(boolean b) {commande.setModified(b);}
    public boolean hasBeenModified() {return commande.hasBeenModified();}

    protected interface Commande {

        void annuler(List L);

        void refaire(List L);
    }

    /**
     * Cette class est générique. En cas d'utilisation pour rendre d'autres
     * types de liste "undoable", il faudrait
     * rendre cette classe publique et la mettre dans le package utils
     */
    private static class CommandeList implements Undoable, Serializable {
        private LinkedList<Commande> undoList = new LinkedList<>();
        private transient LinkedList<Commande> redoList = new LinkedList<>();
        private final transient List liste;
        
        private int marque = 0;

        private final transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
        
        /** Définit le nombre maximum d'opérations enregistrées dans la liste undo **/
        private final int MAX_UNDO = 70;
        

        public CommandeList(List L) { liste = L; }

        /** Ce flag est levé lorsqu'une action non undoable s'apprête à être effectuée
         * par exemple, les actions undo et redo elles-mêmes.
         **/
        private transient boolean flagUndoAction = false;
        public boolean addUndoableCommande(Commande c) {
            if(flagUndoAction) {flagUndoAction = false; return false; }
            if(undoList.size()>=MAX_UNDO) {return false;}
            addUndoAction(c);
            setModified(true);
            if(peutRefaire()) {
                redoList.clear();
                firePropertyChange(Undoable.PEUT_REFAIRE, true, false);
            }
            if(marque<0) {marque = 0;}
            marque++;
            return true;
        }
        
        private void addUndoAction(Commande c) {
            if(!peutAnnuler()) { firePropertyChange(Undoable.PEUT_ANNULER, false, true); }
            undoList.push(c);
        }
        
        private void addRedoAction(Commande c) {
            if(!peutRefaire()) { firePropertyChange(Undoable.PEUT_REFAIRE, false, true); }
            redoList.push(c);
        }

        private Commande removeUndoAction() {
            Commande c = undoList.pop();
            if(!peutAnnuler()) { firePropertyChange(Undoable.PEUT_ANNULER, true, false); }
            return c;
        }

        private Commande removeRedoAction() {
            Commande c = redoList.pop();
            if(!peutRefaire()) { firePropertyChange(Undoable.PEUT_REFAIRE, true, false); }
            return c;
        }
        
        @Override
        public void annuler() {
            if (!undoList.isEmpty()) {
                Commande c = removeUndoAction();
                flagUndoAction = true;
                c.annuler(liste);
                addRedoAction(c);
                marque--;
                setModified(marque!=0);
            }
        }

        @Override
        public void refaire() {
            if (!redoList.isEmpty()) {
                Commande c = removeRedoAction();
                flagUndoAction = true;
                c.refaire(liste);
                addUndoAction(c);
                marque++;
                setModified(marque!=0);
            }
        }

        @Override
        public boolean peutAnnuler() {
            return !undoList.isEmpty();
        }

        @Override
        public boolean peutRefaire() {
            return !redoList.isEmpty();
        }

//        private void notifyEdit() {
//            redoList.clear();
//            setModified(true);
//            IHM.notifyUndoableAction();
//        }

        private boolean modified = false;
        public boolean hasBeenModified() { return modified; }
        public void setModified(boolean modified) {
            if(this.modified==modified) {return;}
            firePropertyChange(Undoable.MODIFIED, this.modified, modified);
            this.modified = modified;
            if(modified==false) {marque = 0;}
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            changeSupport.addPropertyChangeListener(listener);
        }
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            changeSupport.removePropertyChangeListener(listener);
        }
        
        private void firePropertyChange(String property, boolean oldValue, boolean newValue) {
            changeSupport.firePropertyChange(property, oldValue, newValue);
        }
        
        private class AddCommande implements Commande, Serializable {
            private static final long serialVersionUID = 1L;

            private final Object objet;

            private AddCommande(Object o) {
                objet = o;
            }

            public void annuler(List L) {
                L.remove(objet);
            }

            public void refaire(List L) {
                L.add(objet);
            }
        }

        private class RemoveCommande implements Commande, Serializable {
            private static final long serialVersionUID = 1L;

            private final Object objet;

            private RemoveCommande(Object o) {
                objet = o;
            }

            public void annuler(List L) {
                L.add(objet);
            }

            public void refaire(List L) {
                L.remove(objet);
            }
        }

        private class AddAllCommande implements Commande, Serializable {
            private static final long serialVersionUID = 1L;

            private final List listeComposants;

            private AddAllCommande(Collection c) {
                listeComposants = new LinkedList(c);
            }

            public void annuler(List L) {
                L.removeAll(listeComposants);
            }

            public void refaire(List L) {
                L.addAll(listeComposants);
            }
        }

        private class RemoveAllCommande implements Commande, Serializable {
            private static final long serialVersionUID = 1L;

            private final Collection listeComposants;

            private RemoveAllCommande(Collection c) {
                listeComposants = c;
            }

            public void annuler(List L) {
                L.addAll(listeComposants);
            }

            public void refaire(List L) {
                L.removeAll(listeComposants);
            }
        }

        private class ClearCommande implements Commande, Serializable {
            private static final long serialVersionUID = 1L;

            private final List listeComposants;

            private ClearCommande() {
                listeComposants = new LinkedList(liste);
            }

            public void annuler(List L) {
                L.addAll(listeComposants);
            }

            public void refaire(List L) {
                L.clear();
            }
        }

    }


    private class UndoableStateListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            UndoableListComposant.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }
}

