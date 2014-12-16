/** «Copyright 2012 François Billioud»
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
package matheos.graphic.geometrie;

import matheos.graphic.Module;
import matheos.graphic.UndoableListComposant;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataObject;
import matheos.utils.boutons.ActionComplete;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.Action;
import matheos.graphic.ListComposant;
import matheos.graphic.composants.Composant;
import matheos.graphic.composants.ComposantGraphique;
import matheos.graphic.composants.Point;
import matheos.graphic.composants.Texte.Legende;
import matheos.graphic.composants.Vecteur;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class Visionneuse extends Module {

    private UndoableListComposant getListe() {
        return undoListe;
    }
    
    private UndoableListComposant undoListe = new UndoableListComposant();
    {
        undoListe.addListComposantListener(new ListComposant.ListComposantListener() {
            @Override
            public boolean add(ListComposant source, ComposantGraphique cg) {
                fireObjectsCreated(new ObjectCreation(cg));
                return true;
            }

            @Override
            public boolean addAll(ListComposant source, Collection<? extends ComposantGraphique> L) {
                fireObjectsCreated(new ObjectCreation(null, new ListComposant(L)));
                return true;
            }

            @Override
            public boolean remove(ListComposant source, ComposantGraphique cg) {
                fireObjectsRemoved(new ListComposant(cg));
                return true;
            }

            @Override
            public boolean removeAll(ListComposant source, Collection<? extends ComposantGraphique> L) {
                fireObjectsRemoved(new ListComposant(L));
                return true;
            }

            @Override
            public boolean clear(ListComposant source, Collection<? extends ComposantGraphique> L) {
                fireObjectsRemoved(new ListComposant(L));
                return true;
            }
        });
    }
    
    @Override
    public void charger(UndoableListComposant liste, Data donneesModule) {
        super.charger(liste, donneesModule);
        undoListe.clear();
        for(ComposantGraphique c : liste) {
            ListComposant L = new ListComposant();
            L.add(c);
            if(c instanceof Composant.Intersectable) {
                L.addAll(L.intersection((Composant.Intersectable)c));
            }
            if(c instanceof Composant.Legendable) {
                Legende l = ((Composant.Legendable)c).getLegende();
                if(l!=null) {L.add(l);}
            } else if(c instanceof Legende) {
                Composant.Legendable l = ((Legende)c).getDependance();
                if(l!=null) {L.add((ComposantGraphique)l);}
            }
            undoListe.addAllOnce(L);
        }
        restart.setEnabled(undoListe.peutAnnuler());
        previous.setEnabled(undoListe.peutAnnuler());
        next.setEnabled(undoListe.peutRefaire());
        last.setEnabled(undoListe.peutRefaire());
    }

    private final Action restart = new ActionRestart();
    private final Action previous = new ActionPrecedent();
    private final Action next = new ActionSuivant();
    private final Action last = new ActionLast();
    private final Action cancel = new ActionAnnulerConstruction();

    public static final int ACTION_RESTART = 1;
    public static final int ACTION_PREVIOUS = 2;
    public static final int ACTION_NEXT = 3;
    public static final int ACTION_LAST = 4;
    public static final int ACTION_CANCEL = 5;
    
    @Override
    public Action getAction(int action) {
        Action a;
        switch(action) {
            case ACTION_RESTART : a = restart; break;
            case ACTION_PREVIOUS : a = previous; break;
            case ACTION_NEXT : a = next; break;
            case ACTION_LAST : a = last; break;
            case ACTION_CANCEL : a = cancel; break;
            default : a = null;
        }
        return a;
    }

    @Override
    protected Kit creerKit(int mode) {
        return null;
    }

    @Override
    public void retourModeNormal() {
        fireModeChanged(MODE_CONSTRUCTION, MODE_NORMAL);
    }

    @Override
    public Data getDonnees() {
        return new DataObject();
    }
    
    private void restart() {
        while(getListe().peutAnnuler()) {getListe().annuler();}
        restart.setEnabled(false);
        previous.setEnabled(false);
        last.setEnabled(true);
        next.setEnabled(true);
    }
    private void toLast() {
        while(getListe().peutRefaire()) {getListe().refaire();}
        last.setEnabled(false);
        next.setEnabled(false);
        previous.setEnabled(true);
        restart.setEnabled(true);
    }
    private void previous() {
        getListe().annuler();
        last.setEnabled(true);
        next.setEnabled(true);
        previous.setEnabled(getListe().peutAnnuler());
        restart.setEnabled(getListe().peutAnnuler());
    }
    private void next() {
        getListe().refaire();
        previous.setEnabled(true);
        restart.setEnabled(true);
        last.setEnabled(getListe().peutRefaire());
        next.setEnabled(getListe().peutRefaire());
    }

    @Override
    public void mouseLeftPressed(ComposantGraphique cg, Point souris, Point curseur) {}

    @Override
    public void mouseLeftReleased(ComposantGraphique cg, Point souris, Point curseur) {}

    @Override
    public void mouseLeftDragReleased(ComposantGraphique cg, Point souris, Point curseur, Vecteur distanceDrag) {}
    
    private class ActionRestart extends ActionComplete {
        private ActionRestart() { super("geometry restart"); }
        public void actionPerformed(ActionEvent e) { restart(); }
    }

    private class ActionLast extends ActionComplete {
        private ActionLast() { super("geometry last"); }
        public void actionPerformed(ActionEvent e) { toLast(); }
    }

    private class ActionPrecedent extends ActionComplete {
        private ActionPrecedent() { super("geometry previous"); }
        public void actionPerformed(ActionEvent e) { previous(); }
    }

    private class ActionSuivant extends ActionComplete {
        private ActionSuivant() { super("geometry next"); }
        public void actionPerformed(ActionEvent e) { next(); }
    }


    public static final int MODE_CONSTRUCTION = 20;
    public static final int MODE_NORMAL = 0;
    private class ActionAnnulerConstruction extends ActionComplete {
        private ActionAnnulerConstruction() {
            super("geometry cancel construction");
        }
        public void actionPerformed(ActionEvent e) {
            toLast();//on remet l'espace dessin à son état initial
            Visionneuse.this.fireModeChanged(MODE_CONSTRUCTION, MODE_NORMAL);
        }
    }

}
