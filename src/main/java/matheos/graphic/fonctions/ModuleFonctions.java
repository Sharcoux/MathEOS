/*
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of matheos.
 *
 * matheos is free software: you can redistribute it and/or modify
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
 */

package matheos.graphic.fonctions;

import matheos.graphic.Filtre;
import matheos.graphic.ListComposant;
import matheos.graphic.Module;
import matheos.graphic.UndoableListComposant;
import matheos.graphic.composants.ComposantGraphique;
import matheos.graphic.composants.Droite;
import matheos.graphic.composants.Point;
import matheos.graphic.composants.Segment;
import matheos.graphic.composants.Texte;
import matheos.sauvegarde.Data;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.dialogue.DialogueComplet;
import matheos.utils.dialogue.DialogueEvent;
import matheos.utils.dialogue.DialogueListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;

/**
 *
 * @author François Billioud
 */
public class ModuleFonctions extends Module.ModuleGraph {

    /** L'ordre de priorité des objets lors de leur sélection, en fonction de leur Class **/
    public static final List<Class<? extends ComposantGraphique>> COMPONENT_PRIORITY_ORDER;
    static {
        Class[] sOrder = {Point.class, Texte.class, Segment.class, Droite.class, Fonction.class  };
        COMPONENT_PRIORITY_ORDER = Arrays.<Class<? extends ComposantGraphique>>asList(sOrder);
    }
    
//    public static enum MODE {
//        NORMAL, POINTS, SEGMENTS, DROITE, TRACE, SUPPRESSION, RENOMMER, TEXTE, COLORER;
//    }
    public static final int SEGMENTS = 6;
    public static final int DROITE = 7;
    public static final int TRACE = 8;
    public static final int XPLUS = 9;
    
    @Override
    protected Kit creerKit(int mode) {
        Kit kit;
        switch(mode) {
            case NORMAL : kit = new KitNormal(); break;
            case POINT : kit = new KitPoints(); break;
            case SEGMENTS : kit = new KitSegments(); break;
            case DROITE : kit = new KitDroite(); break;
            case TEXTE : kit = new KitTexte(); break;
            case COLORER : kit = new KitColorer(); break;
            case RENOMMER : kit = new KitRenommer(); break;
            case SUPPRIMER : kit = new KitSupprimer(); break;
            default : kit = null;
        }
        return kit;
    }
    
    @Override
    public Action getAction(int action) {
        Action a;
        switch(action) {
            case SEGMENTS : a = actionSegments; break;
            case DROITE : a = actionDroite; break;
                
            case XPLUS : a = actionXPlus; break;
            case TRACE : a = actionTrace; break;
            default : a = super.getAction(action);
        }
        return a;
    }
    
    private final ActionTrace actionTrace = new ActionTrace();
    private final ActionXPlus actionXPlus = new ActionXPlus();
    
    private final ActionChangeMode actionSegments = creerActionChangeMode(SEGMENTS, "graphic segment");
    private final ActionChangeMode actionDroite = creerActionChangeMode(DROITE, "graphic line");
    
    private final ActionClicDroit actionRenommerClicDroit = new ActionRenommerClicDroit();
    private final ActionClicDroit actionPointilles = new ActionPointilles();
    {
        listeActionsClicDroit.add(actionRenommerClicDroit);
        listeActionsClicDroit.add(actionPointilles);
    }
    
    @Override
    public Data getDonnees() {
        Data donnees = super.getDonnees();
        donnees.putElement("xplus", actionXPlus.isSelected()+"");
        return donnees;
    }
    @Override
    public void charger(UndoableListComposant listeObjetsConstruits, Data donneesModule) {
        actionXPlus.setSelected(Boolean.parseBoolean(donneesModule.getElement("xplus")));
        super.charger(listeObjetsConstruits, donneesModule);
    }
    
    private class KitPoints extends Kit {
        {filtre = new Filtre(Point.class);}
        @Override
        public boolean select(ComposantGraphique cg, Point souris) {
            Point P = (Point)creerComposantPermanent(cg);
            fireObjectsCreated(new ObjectCreation(P));
            return true;
        }
        @Override
        public ListComposant apercu(ComposantGraphique cg, Point souris) {
            return new ListComposant(cg);
        }
    }
    private class KitSegments extends Kit {
        {filtre = new Filtre(Point.class);}
        Point origine = null;
        @Override
        public boolean select(ComposantGraphique cg, Point souris) {
            Point P = (Point)creerComposantTemporaire(new Point.XY((Point)cg));
            if(origine==null) {
                origine = P;
                filtre.exclure(P);
                return false;
            } else {
                fireObjectsCreated(new ObjectCreation(creerComposantPermanent(new Segment.AB(origine, P)),new ListComposant(origine,P)));
                origine = null;
                return true;
            }
        }
        @Override
        public ListComposant apercu(ComposantGraphique cg, Point souris) {
            ListComposant L = new ListComposant();
            if(origine!=null) {
                L.add(origine);
                L.add(creerComposantTemporaire(new Segment.AB(origine, (Point)cg)));
                L.add(cg);
            } else {
                L.add(cg);
            }
            return L;
        }
    }
    private class KitDroite extends Kit {
        {filtre = new Filtre(Point.class);}
        Point origine = null;
        @Override
        public boolean select(ComposantGraphique cg, Point souris) {
            Point P = (Point)creerComposantTemporaire(new Point.XY((Point)cg));
            if(origine==null) {
                origine = P;
                filtre.exclure(P);
                return false;
            } else {
                List<Point> l = new LinkedList<>();
                l.add(origine);l.add(P);
                Fonction f = new Fonction(l);
                fireObjectsCreated(new ObjectCreation(creerComposantPermanent(f),new ListComposant(origine,P)));
                f.xPositif(actionXPlus.isSelected());
                renommer(f);
                origine = null;
                return true;
            }
        }
        @Override
        public ListComposant apercu(ComposantGraphique cg, Point souris) {
            ListComposant L = new ListComposant();
            if(origine!=null) {
                L.add(origine);
                L.add(creerComposantTemporaire(new Droite.AB(origine, (Point)cg)));
                L.add(cg);
            } else {
                L.add(cg);
            }
            return L;
        }
    }

    private class ActionTrace extends ActionComplete {
        private ActionTrace() { super("function trace"); }
        @Override
        public void actionPerformed(ActionEvent e) {
            retourModeNormal();
            final SaisieCoefficients saisie = new SaisieCoefficients();
            DialogueComplet dialogue = new DialogueComplet("dialog function trace", saisie);
            saisie.setNavigation(dialogue.getNavigation());
            dialogue.addValidation(saisie.new ValidationCoefficients());
            dialogue.addDialogueListener(new DialogueListener() {
                @Override
                public void dialoguePerformed(DialogueEvent event) {
                    if(event.isConfirmButtonPressed()) {
//                        String nom = event.getInputString("name");
                        Fonction f = new Fonction(saisie.getEntries());
                        f.setNom(saisie.getNom());
                        f.xPositif(actionXPlus.isSelected());
                        f.setCouleur(getCouleur());
                        ModuleFonctions.this.fireObjectsCreated(new ObjectCreation(f));
                    }
                }
            });
        }
    }
    
    private class ActionXPlus extends ActionComplete.Toggle {
        private ActionXPlus() { super("function x+",false); }
        @Override
        public void actionPerformed(ActionEvent e) {
            for(ComposantGraphique cg : getPermanentList()) {if(cg instanceof Fonction) {((Fonction)cg).xPositif(isSelected());}}
        }
    }
    
}
