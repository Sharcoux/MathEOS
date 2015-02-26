/**
 * Copyright (C) 2014 François Billioud
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
 **/

package matheos.graphic.geometrie;

import matheos.graphic.Filtre;
import matheos.graphic.ListComposant;
import matheos.graphic.Module.ModuleGraph;
import matheos.graphic.composants.*;
import matheos.graphic.composants.Composant.Intersectable;
import matheos.graphic.composants.Composant.Legendable;
import matheos.graphic.composants.Composant.Projetable;
import matheos.graphic.geometrie.Constructeurs.ApercuArc2P;
import matheos.graphic.geometrie.Constructeurs.ApercuArcPSP;
import matheos.graphic.geometrie.Constructeurs.Arc3P;
import matheos.graphic.geometrie.Constructeurs.ArcPSPP;
import matheos.graphic.geometrie.Constructeurs.Cercle;
import matheos.graphic.geometrie.Constructeurs.Constructeur;
import matheos.graphic.geometrie.Constructeurs.ConstructeurLigne;
import matheos.graphic.geometrie.Constructeurs.DroiteAP;
import matheos.graphic.geometrie.Constructeurs.Ligne2P;
import matheos.graphic.geometrie.Constructeurs.LigneOrthogonale;
import matheos.graphic.geometrie.Constructeurs.LignePLP;
import matheos.graphic.geometrie.Constructeurs.LigneParallele;
import matheos.graphic.geometrie.Constructeurs.PointP;
import matheos.graphic.geometrie.Constructeurs.PointProjetableP;
import matheos.graphic.geometrie.Constructeurs.SegmentSPP;
import matheos.graphic.geometrie.Constructeurs.SegmentSSP;
import matheos.graphic.geometrie.Constructeurs.SegmentSSSP;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.dialogue.DialogueComplet;
import matheos.utils.dialogue.DialogueEvent;
import matheos.utils.dialogue.DialogueListener;
import matheos.utils.managers.PermissionManager;
import matheos.utils.objets.maps.ListClassMap;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import matheos.graphic.composants.Texte.Legende;
import matheos.utils.dialogue.DialogueBloquant;

/**
 *
 * @author François Billioud
 */
public class ModuleGeometrie extends ModuleGraph {

    public static final int SEGMENT = 7;
    public static final int DEMI_DROITE = 8;
    public static final int DROITE = 9;
    public static final int ARC = 10;
    
    public static final int POINTS_DE_CONSTRUCTION = 11;
    public static final int MESURES = 12;
    
    @Override
    protected Kit creerKit(int mode) {
        Kit kit;
        switch(mode) {
            case NORMAL : kit = new KitNormal(); break;
            case POINT : kit = new KitPoint(); break;
            case SEGMENT : kit = new KitSegment(); break;
            case DEMI_DROITE : kit = new KitDemiDroite(); break;
            case DROITE : kit = new KitDroite(); break;
            case ARC : kit = new KitArc(); break;
            case TEXTE : kit = new KitTexte(); break;
            case COLORER : kit = new KitColorer(); break;
            case RENOMMER : kit = new KitRenommer(); break;
            case DRAGAGE : kit = new KitDragage(); break;
            case SUPPRIMER : kit = new KitSupprimer(); break;
            default : kit = null;
        }
        return kit;
    }

    private final ActionChangeMode actionSegment = creerActionChangeMode(SEGMENT, "graphic segment");
    private final ActionChangeMode actionDemiDroite = creerActionChangeMode(DEMI_DROITE, "geometry half-line");
    private final ActionChangeMode actionDroite = creerActionChangeMode(DROITE, "graphic line");
    private final ActionChangeMode actionArc = creerActionChangeMode(ARC, "geometry arc");
    
    ActionComplete.Toggle actionPointsConstruction = new ActionPointsConstruction();
    Action actionPrecisionMesures = new ActionPrecisionMesures();
    
    @Override
    public Action getAction(int action) {
        Action a;
        switch(action) {
            case SEGMENT : a = actionSegment; break;
            case DEMI_DROITE : a = actionDemiDroite; break;
            case DROITE : a = actionDroite; break;
            case ARC : a = actionArc; break;
                
            case POINTS_DE_CONSTRUCTION : a = actionPointsConstruction; break;
            case MESURES : a = actionPrecisionMesures; break;
            default : a = super.getAction(action);
        }
        return a;
    }
    
    private final ActionClicDroit actionMilieu = new ActionMilieu();
    private final ActionClicDroit actionOrthogonal = new ActionOrthogonal();
    private final ActionClicDroit actionParallele = new ActionParallele();
    private final ActionClicDroit actionRenommerClicDroit = new ActionRenommerClicDroit();
    private final ActionClicDroit actionCoder = new ActionCoder();
    {
        listeActionsClicDroit.add(actionRenommerClicDroit);
        listeActionsClicDroit.add(actionCoder);
        listeActionsClicDroit.add(actionMilieu);
        listeActionsClicDroit.add(actionOrthogonal);
        listeActionsClicDroit.add(actionParallele);
    }
    
    private class ActionPointsConstruction extends ActionComplete.Toggle {
        private ActionPointsConstruction() {
            super("geometry display construction", true);
        }
        @Override
        public void actionPerformed(ActionEvent e) {}
    }
    private class ActionPrecisionMesures extends ActionComplete {
        private ActionPrecisionMesures() { super("geometry mesures precision"); }
        @Override
        public void actionPerformed(ActionEvent e) {
            DialogueComplet dialogue = new DialogueComplet("dialog geometry precision");
            dialogue.setInitialValue("distance",Constructeurs.precisionDistance+"");
            dialogue.setInitialValue("angle",Constructeurs.precisionAngle+"");
            dialogue.addDialogueListener(new DialogueListener() {
                @Override
                public void dialoguePerformed(DialogueEvent event) {
                    if(event.isConfirmButtonPressed()) {
                        Constructeurs.precisionDistance = event.getInputDouble("distance");
                        Constructeurs.precisionAngle = event.getInputDouble("angle");
                    }
                }
            });
        }
    }
    private class ActionMilieu extends ActionClicDroit {
        { filtre = new Filtre(Segment.class).nonPassif(); }
        private ActionMilieu() { super("geometry middle"); }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!PermissionManager.isTracerMilieuAllowed()) {DialogueBloquant.dialogueBloquant("not allowed");return;}
            Point M = ((Segment)cg).milieu();
            M.setCouleur(getCouleur());
            fireObjectsCreated(new ObjectCreation(M));
            renommer(M);
        }
        @Override
        public ActionClicDroit clone() {ActionClicDroit action = new ActionMilieu();action.setComposant(cg);return action;}
        @Override
        public Filtre getFiltre() {return PermissionManager.isTracerMilieuAllowed() ? filtre : Filtre.filtreTotal();}
    }
    private class ActionOrthogonal extends ActionClicDroit {
        { filtre = new Filtre(Ligne.class).nonPassif(); }
        private ActionOrthogonal() {super("geometry create orthogonal");}
        @Override
        public void actionPerformed(ActionEvent e) {
            assert cg instanceof Ligne : "Erreur : action orthogonal sur composant non ligne !";
            assert getKit() instanceof KitLigne : "Erreur : actionOrthogonale accédée hors mode création de ligne";
            KitLigne k = (KitLigne) getKit();
            k.setMode(KitLigne.ORTHOGONAL);
            k.select((Ligne)cg, curseur());
        }
        @Override
        public ActionClicDroit clone() {ActionClicDroit action = new ActionOrthogonal();action.setComposant(cg);return action;}
        @Override
        public Filtre getFiltre() { return (getKit() instanceof KitLigne) ? filtre : Filtre.filtreTotal(); }
    }
    private class ActionParallele extends ActionClicDroit {
        {filtre = new Filtre(Ligne.class).nonPassif();}
        public ActionParallele() { super("geometry create parallele"); }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!PermissionManager.isTracerParalleleAllowed()) {DialogueBloquant.dialogueBloquant("not allowed");return;}
            assert cg instanceof Ligne : "Erreur : action parallele sur composant non ligne !";
            assert getKit() instanceof KitLigne : "Erreur : action parallele sur composant non ligne !";
            KitLigne k = (KitLigne) getKit();
            k.setMode(KitLigne.PARALLELE);
            k.select((Ligne)cg, curseur());
        }
        @Override
        public Filtre getFiltre() {
            if(!PermissionManager.isTracerParalleleAllowed()) {return Filtre.filtreTotal();}
            if(getKit() instanceof KitLigne) {return filtre;}
            return Filtre.filtreTotal();
        }
        @Override
        public ActionClicDroit clone() {ActionClicDroit action = new ActionParallele();action.setComposant(cg);return action;}
    }
    
    private abstract class KitComposant<T extends ComposantGraphique> extends Kit {
        protected final ListComposant selectedComponents = new ListComposant();
        protected ListClassMap<T,Constructeur> constructeurs = new ListClassMap<>();
        protected ListClassMap<T,Constructeur> apercus = new ListClassMap<>();
        protected ListClassMap<T,Filtre.VerificationSpeciale> verificationsSpeciales = new ListClassMap<>();
        private Filtre filtreConstructeurs = Filtre.filtreLaxiste();
        private Filtre filtreApercus = Filtre.filtreLaxiste();
        protected Constructeur constructeur; 

        public KitComposant() {
            initMaps();
            filtreConstructeurs = new FiltreComposant(constructeurs.getAcceptedClasses());
            filtreApercus = new FiltreComposant(apercus.getAcceptedClasses());
            filtre = new Filtre.UnionFilter(filtreConstructeurs, filtreApercus);
        }
        
        private class FiltreComposant extends Filtre {
            FiltreComposant(Collection classesAcceptees) {
                super(classesAcceptees);
                nonPassif();
                /** Ajoute aux filtres les conditions de vérification particulières **/
                addVerificateur(new Filtre.VerificationSpeciale() {
                    @Override
                    public boolean accepte(ComposantGraphique cg) {
                        List<Class<? extends ComposantGraphique>> L = selectedComponents.toClassList();
                        L.add(cg.getClass());//On a choisit d'ajouter le composant actuel dans la liste des classes avant d'accéder au filtre
                        Filtre.VerificationSpeciale verif = verificationsSpeciales.get(L);
                        return verif==null ? true : verif.accepte(cg);
                    }
                });
            }
        }
        protected class EqualsVerification implements Filtre.VerificationSpeciale {
            protected static final boolean ONLY_EQUALS = true;
            protected static final boolean EXCLUDES = false;
            private final boolean onlyEquals;
            private final int[] indexes;
            protected EqualsVerification(boolean onlyEquals, int... indexes) {
                this.onlyEquals = onlyEquals;
                this.indexes = indexes;
            }
            @Override
            public boolean accepte(ComposantGraphique cg) {
                for(int i : indexes) {
                    if(onlyEquals != cg.estEgalA(selectedComponents.get(i))) {return false;}//Si l'une des conditions n'est pas remplie
                }
                return true;
            }
        }
        
        @Override
        public boolean select(ComposantGraphique choix, Point souris) {
            ComposantGraphique cg;
            //Si cg est en fait le curseur, le point utilisé pour la construction
            //n'existe pas encore. On effectue donc un traitement particulier afin de
            //positionner notre nouveau point selon les règles les plus adaptées
            if(choix instanceof Point && !getPermanentList().contient(choix)) {
                Constructeur c = constructeurs.get(Point.class);
                if(c==null) {c = apercus.get(Point.class);}
                cg = c.pointDeConstructionNonExistant(selectedComponents, (Point)choix, souris);
            } else { cg = choix; }
            
            //on met à jour les maps et le filtre
            selectedComponents.add(cg);
            constructeurs = constructeurs.getMap(cg.getClass());assert !constructeurs.isEmpty() : "Erreur : le filtre du kitComposant n'a pas marché car plus aucun constructeur ne convient";
            apercus = apercus.getMap(cg.getClass());
            filtreConstructeurs = new FiltreComposant(constructeurs.getAcceptedClasses());
            filtreApercus = new FiltreComposant(apercus.getAcceptedClasses());
            filtre = new Filtre.UnionFilter(filtreConstructeurs, filtreApercus);
            
            //On execute la construction
            constructeur = constructeurs.get();
            if(constructeur!=null) {
                ListComposant L = new ListComposant(selectedComponents);
                L.add(souris);
                ObjectCreation o = constructeur.construire(L);
                if(o!=null) {
                    ComposantGraphique objetConstruit = creerComposantPermanent(o.getMainElement());
                    //On ajoute tous les éléments (main et annexes) en une seule fois pour pouvoir annuler en un coup
                    ListComposant annexes = new ListComposant();
                    for(ComposantGraphique comp : o.getAnnexElements()) {
                        annexes.add(creerComposantTemporaire(comp));//ajout du composant secondaire
                    }
                    //On ajoute les points de construction
                    if(actionPointsConstruction.isSelected()) {
                        for(Point P : objetConstruit.pointsSupplementaires()) {
                            annexes.addOnce(creerComposantTemporaire(P));
                        }
                    }
                    //On ajoute les éventuels points d'intersection
                    if(objetConstruit instanceof Intersectable) {
                        for(ComposantGraphique comp : getPermanentList()) {
                            if(comp instanceof Intersectable && !comp.estEgalA(objetConstruit)) {
                                List<Point> LPoints = ((Intersectable)comp).pointsDIntersection((Intersectable)objetConstruit);
                                for(int i=0; i<LPoints.size(); i++) {
                                    annexes.addOnce(creerComposantTemporaire(new Point.Intersection((Intersectable)comp,(Intersectable)objetConstruit, i)));
                                }
                            }
                        }
                    }
                    fireObjectsCreated(new ObjectCreation(objetConstruit, annexes));
                    reinitialize();
                    return true;
                }
            }
            return false;
        }
        @Override
        public ListComposant apercu(ComposantGraphique choix, Point souris) {
            ComposantGraphique cg;
            ListComposant L = new ListComposant(selectedComponents);
            //Si cg est en fait le curseur, le point utilisé pour la construction
            //n'existe pas encore. On effectue donc un traitement particulier afin de
            //positionner notre nouveau point selon les règles les plus adaptées
            if(choix instanceof Point && !getPermanentList().contient(choix)) {
                Constructeur c = constructeurs.get(Point.class);
                if(c==null) {c = apercus.get(Point.class);}
                cg = c.pointDeConstructionNonExistant(selectedComponents, (Point)choix, souris);
            } else { cg = choix; }
            L.add(cg);
            L.add(souris);//On passe la souris au constructeur dans tous les cas
            Constructeur apercu = apercus.get(cg.getClass());
            if(apercu==null) {apercu = constructeurs.get(cg.getClass());}//on tente d'utiliser un constructeur à la place
            if(apercu==null) {//on tente d'utiliser la souris
                Filtre f = new Filtre.UnionFilter<>(
                        new FiltreComposant(constructeurs.getMap(cg.getClass()).getAcceptedClasses()),
                        new FiltreComposant(apercus.getMap(cg.getClass()).getAcceptedClasses()));
                if(f.accepte(souris)) {//on vérifie que les constructeurs acceptent la souris
                    apercu = apercus.getMap(cg.getClass()).get(Point.class);
                    if(apercu==null) {apercu = constructeurs.getMap(cg.getClass()).get(Point.class);}
                }
            }
            if(apercu!=null) {
                ObjectCreation o = apercu.construire(L);
                if(o!=null) {
                    for(ComposantGraphique c : o.getList()) {
                        if(c instanceof Legendable) {
                            Legende l = ((Legendable)c).getLegende();
                            if(l!=null && !getPermanentList().contient(l)) {creerComposantTemporaire(l);}
                        }
                    }
                    for(ComposantGraphique c : o.getAnnexElements()) {
                        if(c!=choix) creerComposantTemporaire(c);
                    }
                    if(o.getMainElement()!=null) {
                        creerComposantPermanent(o.getMainElement());
                    }
                    return o.getList();
                }
            }
            return new ListComposant();
        }
        /** Initialise les maps et les filtres **/
        protected abstract void initMaps();
        protected void reinitialize() {
            selectedComponents.clear();
            constructeurs.clear();
            apercus.clear();
            initMaps();
            filtreConstructeurs = new FiltreComposant(constructeurs.getAcceptedClasses());
            filtreApercus = new FiltreComposant(apercus.getAcceptedClasses());
            filtre = new Filtre.UnionFilter(filtreConstructeurs, filtreApercus);
        }
    }
    private abstract class KitLigne extends KitComposant {
        private final ConstructeurLigne.TYPE type;
        private static final int NORMAL = 0;
        private static final int ORTHOGONAL = 1;
        private static final int PARALLELE = 2;
        private int mode = NORMAL;
        KitLigne(ConstructeurLigne.TYPE type) {
            this.type = type;
            reinitialize();
        }
        public void setMode(int mode) {this.mode = mode;reinitialize();}
        @Override
        public void initMaps() {
            switch(mode) {
                case ORTHOGONAL : initMapsOrthogonal(); break;
                case PARALLELE : initMapsParallele(); break;
                default : initMapsNormal();
            }
        }
        protected void initMapsNormal() {
            constructeurs.put(Arrays.<Class>asList(Point.class, Point.class), new Ligne2P(type));
            constructeurs.put(Arrays.<Class>asList(Point.class, Ligne.class, Point.class), new LignePLP(type));
            apercus.put(Arrays.<Class>asList(Point.class), new PointP(true));
            
            verificationsSpeciales.put(Arrays.<Class>asList(Point.class, Point.class), new EqualsVerification(EqualsVerification.EXCLUDES, 0));
            verificationsSpeciales.put(Arrays.<Class>asList(Point.class, Ligne.class, Point.class), new EqualsVerification(EqualsVerification.EXCLUDES, 0));
            verificationsSpeciales.put(Arrays.<Class>asList(Point.class, Ligne.class), new Filtre.VerificationSpeciale() {
                @Override
                public boolean accepte(ComposantGraphique cg) {
                    return cg instanceof Ligne && ((Ligne)cg).contient((Point)selectedComponents.get(0));
                }
            });
        }
        protected void initMapsOrthogonal() {
            constructeurs.put(Arrays.<Class>asList(Ligne.class, Point.class), new LigneOrthogonale(type));
            constructeurs.put(Arrays.<Class>asList(Ligne.class, Point.class, Point.class), new LigneOrthogonale(type));
//            verificationsSpeciales.put(Arrays.<Class>asList(Ligne.class, Point.class), new Filtre.VerificationSpeciale() {
//                @Override
//                public boolean accepte(ComposantGraphique cg) {//la ligne ne contient pas le point
//                    return !((Ligne)selectedComponents.get(0)).droite().contient((Point)cg);
//                }
//            });
        }
        protected void initMapsParallele() {
            constructeurs.put(Arrays.<Class>asList(Ligne.class, Point.class), new LigneParallele(type));
            verificationsSpeciales.put(Arrays.<Class>asList(Ligne.class, Point.class), new Filtre.VerificationSpeciale() {
                @Override
                public boolean accepte(ComposantGraphique cg) {//la ligne ne contient pas le point
                    return !((Ligne)selectedComponents.get(0)).droite().contient((Point)cg);
                }
            });
        }
        @Override
        public boolean select(ComposantGraphique cg, Point souris) {
            boolean b = super.select(cg, souris);
            if(b) {setMode(NORMAL);}
            return b;
        }
    }
    private class KitPoint extends KitComposant {
        @Override
        protected void initMaps() {
            constructeurs.put(Arrays.<Class>asList(Point.class), new PointP(false));
            constructeurs.put(Arrays.<Class>asList(Ligne.class), new PointProjetableP());
            constructeurs.put(Arrays.<Class>asList(Arc.class), new PointProjetableP());
            
            Filtre.VerificationSpeciale nonBorne = new Filtre.VerificationSpeciale() {
                @Override
                public boolean accepte(ComposantGraphique cg) {
                    Projetable p = (Projetable)selectedComponents.get(0);
                    return p.projection((Point)cg).estEgalA(p.projeteOrthogonal((Point)cg));
                }
            };
            verificationsSpeciales.put(Arrays.<Class>asList(Ligne.class, Point.class), nonBorne);
            verificationsSpeciales.put(Arrays.<Class>asList(Arc.class, Point.class), nonBorne);
        }
        //On renomme les points à la création
        @Override
        protected void fireObjectsCreated(ObjectCreation o) {
            super.fireObjectsCreated(o);
            if(o!=null) {renommer(o.getMainElement());}
        }
    }
    private class KitSegment extends KitLigne {
        public KitSegment() {
            super(ConstructeurLigne.TYPE.SEGMENT);
        }
        @Override
        protected void initMapsNormal() {
            super.initMapsNormal();
            constructeurs.put(Arrays.<Class>asList(Segment.class, Segment.class, Segment.class), new SegmentSSSP());
            constructeurs.put(Arrays.<Class>asList(Segment.class, Segment.class, Point.class), new SegmentSSP());
            constructeurs.put(Arrays.<Class>asList(Segment.class, Point.class, Point.class), new SegmentSPP());
            apercus.put(Arrays.<Class>asList(Segment.class, Point.class), new SegmentSPP());
            
            verificationsSpeciales.put(Arrays.<Class>asList(Segment.class, Segment.class), new EqualsVerification(EqualsVerification.ONLY_EQUALS, 0));
            verificationsSpeciales.put(Arrays.<Class>asList(Segment.class, Segment.class, Segment.class), new EqualsVerification(EqualsVerification.ONLY_EQUALS, 0));
            verificationsSpeciales.put(Arrays.<Class>asList(Segment.class, Point.class, Point.class), new EqualsVerification(EqualsVerification.EXCLUDES, 1));
        }
        protected void initMapsParallele() {
            constructeurs.put(Arrays.<Class>asList(Ligne.class, Point.class, Point.class), new LigneParallele(ConstructeurLigne.TYPE.SEGMENT));
            apercus.put(Arrays.<Class>asList(Ligne.class, Point.class), new LigneParallele(ConstructeurLigne.TYPE.SEGMENT));
        }
    }
    private class KitDemiDroite extends KitLigne {
        public KitDemiDroite() {
            super(ConstructeurLigne.TYPE.DEMI_DROITE);
        }
        protected void initMapsParallele() {
            constructeurs.put(Arrays.<Class>asList(Ligne.class, Point.class, Point.class), new LigneParallele(ConstructeurLigne.TYPE.DEMI_DROITE));
            apercus.put(Arrays.<Class>asList(Ligne.class, Point.class), new LigneParallele(ConstructeurLigne.TYPE.DEMI_DROITE));
        }
    }
    private class KitDroite extends KitLigne {
        public KitDroite() {
            super(ConstructeurLigne.TYPE.DROITE);
        }
        @Override
        protected void initMapsNormal() {
            super.initMapsNormal();
            constructeurs.put(Arrays.<Class>asList(Arc.class, Point.class), new DroiteAP());
            apercus.put(Arrays.<Class>asList(Arc.class), new DroiteAP());//HACK pour remettre à zéro la variable "origine" entre l'apercu et le contructeur
        }
    }
    private class KitArc extends KitComposant {
        @Override
        protected void initMaps() {
            constructeurs.put(Arrays.<Class>asList(Segment.class), new Cercle());
            constructeurs.put(Arrays.<Class>asList(Point.class, Point.class, Point.class), new Arc3P());
            constructeurs.put(Arrays.<Class>asList(Point.class, Segment.class, Point.class, Point.class), new ArcPSPP());
            apercus.put(Arrays.<Class>asList(Point.class), new PointP(true));
            apercus.put(Arrays.<Class>asList(Point.class, Point.class), new ApercuArc2P());
            apercus.put(Arrays.<Class>asList(Point.class, Segment.class, Point.class), new ApercuArcPSP());
            
            verificationsSpeciales.put(Arrays.<Class>asList(Point.class, Point.class), new EqualsVerification(EqualsVerification.EXCLUDES, 0));
            verificationsSpeciales.put(Arrays.<Class>asList(Point.class, Point.class, Point.class), new EqualsVerification(EqualsVerification.EXCLUDES, 0, 1));
            verificationsSpeciales.put(Arrays.<Class>asList(Point.class, Segment.class, Point.class), new EqualsVerification(EqualsVerification.EXCLUDES, 0));
            verificationsSpeciales.put(Arrays.<Class>asList(Point.class, Segment.class, Point.class, Point.class), new EqualsVerification(EqualsVerification.EXCLUDES, 0, 2));
        }
    }
    
}
