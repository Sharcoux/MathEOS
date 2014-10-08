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

package matheos.graphic;

import matheos.graphic.composants.Composant;
import matheos.graphic.composants.ComposantGraphique;
import matheos.graphic.composants.Point;
import matheos.graphic.composants.Segment;
import matheos.graphic.composants.Texte;
import matheos.graphic.composants.Vecteur;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataObject;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.ActionGroup;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.Traducteur;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;

/**
 * Cette classe définit les interactions entre l'utilisateur et un espace
 * mathématique virtuel. Un module est indépendant du modèle et de la vue.
 * @author François Billioud
 */
public abstract class Module {

    public static final String MODE_PROPERTY = "mode";
    public static final String RIGHT_CLIC_AVAILABLE_PROPERTY = "right-clic";
    public static final String TEXT_FOCUSABLE_PROPERTY = "text-focusable";
    
    public static final int NORMAL = 0;
    
    private Point.XY curseur = new Point.XY(0,0);
    private boolean mouseOut = true;
    protected boolean isMouseOut() {return mouseOut;}
    protected Point curseur() {return curseur;}

    private Color couleur;
    public void setCouleur(Color c) {couleur = c;}
    public Color getCouleur() {return couleur;}
    
    private static Color couleurTemporaire = ColorManager.get("color temp component");
    private static Color couleurSelection = ColorManager.get("color focused component");
    protected Color getCouleurTemporaire() {return couleurTemporaire;}

    private ListComposant permanentList;
    private ListComposant temporaryList = new ListComposant();
    private List<String> messages = new LinkedList<>();
    protected ListComposant getPermanentList() {return permanentList;}
    protected ListComposant getTemporaryList() {return temporaryList;}
    protected List<String> getMessages() {return messages;}

    public void setElementsPermanents(ListComposant L) {
        permanentList = L;
    }
    
    private Module.ComposantEnlighter choixPotentielEnlighter = new Module.ComposantEnlighter();
    private List<Action> actionsClicDroit = new LinkedList<>();
    /** Définit cg comme le composant visé par le clic gauche et le met en couleur
     * @param cg le composant répondant à getFiltreClicGauche().accept(cg), ou null**/
    protected void setChoixPotentiel(ComposantGraphique cg) {
        choixPotentielEnlighter.setComposant(cg);
        fireUpdate();
    }
    /** définit cg comme le composant visé par le clic-droit et crée le menu contextuel
     * @param cg composant répondant aux contraintes du filtreClicDroit, ou null **/
    protected void setChoixClicDroit(ComposantGraphique cg) {
        List<Action> actions = getActionsClicDroit(cg);
        boolean wasAvailable = !actionsClicDroit.isEmpty(), isAvailable = !actions.isEmpty();
        actionsClicDroit = actions;
        if(wasAvailable!=isAvailable) {fireRightClicChanged(wasAvailable, isAvailable);}
    }
//        public MenuContextuel getMenuContextuel() { return menuClicDroit; }

    //On vérifie que les actions sont valides avant de les transmettre
    private List<Action> getActionsClicDroit(ComposantGraphique cg) {
        List<ActionClicDroit> actionsCD = listeActionsClicDroit;
        List<Action> actions = new LinkedList<>();
        for(ActionClicDroit action : actionsCD) {
            action.setComposant(cg);
            if(!action.isNull()) {actions.add(action.clone());}//On clone l'action, sinon le composant ciblé (cg) peut évoluer avant d'avoir choisi une des options si on bouge la souris
        }
        return actions;
    }
    
    /** Liste des actions à considérer dans le menu contextuel **/
    protected List<ActionClicDroit> listeActionsClicDroit = new LinkedList<>();

    protected ComposantGraphique getChoixPotentiel() {return choixPotentielEnlighter.getComposant();}
    
    public abstract Action getAction(int action);
    public ActionComplete.Toggle getToggleAction(int action) {return (ActionComplete.Toggle)getAction(action);}

    private Kit kit;
//    private KitListener kitListener = new KitListenerImpl();
    protected void setKit(Kit kit) {
//        if(this.kit!=null) {this.kit.removeKitListener();}
        this.kit = kit;
//        if(kit!=null) {kit.setKitListener(this);}
        clearTemporaryElements();
    }
    protected Kit getKit() { return kit; }
    
    private void clearTemporaryElements() {
        temporaryList.clear();
        messages.clear();
        fireTemporaryObjects();
        fireMessages();
    }
    
    private int mode = 0;
    protected void setMode(int mode) {
        int old = this.mode;
        this.mode = mode;
        if(old!=mode) {fireModeChanged(old, mode);}
        clearTemporaryElements();
    }
    protected int getMode() { return mode; }

    protected abstract Kit creerKit(int mode);

    public abstract void retourModeNormal();
    public void charger(UndoableListComposant listeObjetsConstruits, Data donneesModule) {
        setElementsPermanents(listeObjetsConstruits);
    }
    public abstract Data getDonnees();
    
    /**
     * Signale au constructeur que la souris a quitté l'espaceDessin
     * @param curseur position du curseur
     */
    public final void mouseOut(Point curseur) {
        mouseOut = true;
        clearTemporaryElements();
    }

    /**
     * Signale au constructeur que la souris a rejoint l'espaceDessin
     * @param curseur position du curseur
     */
    public final void mouseIn(Point curseur) {mouseOut = false;}


    /**
     * Signale un clic au constructeur
     * @param cg le composantGraphique répondant au filtre actuel
     * @param souris position de la souris
     * @param curseur position du curseur (qui prend peut avoir été corrigée par rapport à celle de la souris)
     */
    public void mouseLeftPressed(ComposantGraphique cg, Point souris, Point curseur) {}

    /**
     * Signale un clic au constructeur
     * @param cg le composantGraphique répondant au filtre actuel
     * @param souris position de la souris
     * @param curseur position du curseur (qui prend peut avoir été corrigée par rapport à celle de la souris)
     */
    public void mouseRightPressed(ComposantGraphique cg, Point souris, Point curseur) {
        if(!actionsClicDroit.isEmpty()) {fireContextMenu();}
    }

    /**
     * Signale la fin d'un clic au constructeur
     * @param cg le composantGraphique répondant au filtre actuel
     * @param souris position de la souris
     * @param curseur position du curseur (qui prend peut avoir été corrigée par rapport à celle de la souris)
     */
    public void mouseRightReleased(ComposantGraphique cg, Point souris, Point curseur) {}

    /**
     * Signale la fin d'un clic complet au constructeur
     * @param cg le composantGraphique répondant au filtre actuel
     * @param souris position de la souris
     * @param curseur position du curseur (qui prend peut avoir été corrigée par rapport à celle de la souris)
     */
    public void mouseRightClicked(ComposantGraphique cg, Point souris, Point curseur) {}

    /**
     * Signale la fin d'un clic complet au constructeur
     * @param cg le composantGraphique répondant au filtre actuel
     * @param souris la position de la souris au moment de l'appel
     * @param curseur position du curseur (qui prend peut avoir été corrigée par rapport à celle de la souris)
     */
    public void mouseLeftClicked(ComposantGraphique cg, Point souris, Point curseur) {
        if(kit!=null) {
            if(cg!=null) {setChoixPotentiel(cg);}
            else if(getFiltreClicGauche().accepte(curseur) && kit!=null && kit.accepteCurseur()) {setChoixPotentiel(curseur);}
            else {setChoixPotentiel(null);}
            ComposantGraphique choix = getChoixPotentiel();
            if(choix!=null) {
                setChoixPotentiel(null);//Afin que l'objet choisi ne soit plus influencé par le changement de couleur du choixPotentielEnlighter
                boolean isFinished = kit.select(choix,souris);//pas d'appel si pas de composant pertinent à proposer
                //XXX on peut envisager d'envoyer un signal à des constructeurListener pour dire que la construction est terminée
                if(isFinished) {clearTemporaryElements();}
            }
        }
    }

    /**
     * Signale la fin d'un clic au constructeur
     * @param cg le composantGraphique répondant au filtre actuel
     * @param souris la position de la souris au moment de l'appel
     * @param curseur position du curseur (qui prend peut avoir été corrigée par rapport à celle de la souris)
     */
    public void mouseLeftReleased(ComposantGraphique cg, Point souris, Point curseur) {}

    /**
     * Signale la fin d'un clic au constructeur
     * @param cg le composantGraphique répondant au filtre actuel
     * @param souris la position de la souris au moment de l'appel
     * @param curseur position du curseur (qui prend peut avoir été corrigée par rapport à celle de la souris)
     * @param distanceDrag vecteur représentant le draggage depuis le dernier MousePressed
     */
    public void mouseLeftDragReleased(ComposantGraphique cg, Point souris, Point curseur, Vecteur distanceDrag) {}

    /**
     * Signale un mouvement de souris au constructeur
     * @param cgGauche le composantGraphique répondant au filtre clicGauche
     * @param cgDroit le composantGraphique répondant au filtre clicDroit
     * @param souris la position de la souris
     * @param curseur position du curseur (qui prend peut avoir été corrigée par rapport à celle de la souris)
     */
    public void mouseMove(ComposantGraphique cgGauche, ComposantGraphique cgDroit, Point souris, Point curseur) {
        this.curseur.setPosition(curseur);
        temporaryList = new ListComposant();
        if(cgGauche!=null) { 
            setChoixPotentiel(cgGauche); 
        }
        else if(getFiltreClicGauche().accepte(curseur) && kit!=null && kit.accepteCurseur()) {setChoixPotentiel(curseur);}
        else {
            setChoixPotentiel(null);
        }
        ComposantGraphique choix = getChoixPotentiel();
        if(choix!=null && kit!=null) {temporaryList.addAll(kit.apercu(choix, souris));}

        setChoixClicDroit(cgDroit);
        
        fireTemporaryObjects();
        
        //Ajouter ici la lecture d'éventuels messages d'aide
        //if(kit!=null) {messages = kit.getMessages();}
        fireMessages();

    }

    /**
     * Signale un drag au constructeur
     * @param cg le composantGraphique répondant au filtre actuel
     * @param deplacementTotal la distance totale déplacée depuis le drag
     * @param origine la position de la souris au dernier appel
     * @param souris la position actuelle de la souris
     * @param curseur position du curseur (qui prend peut avoir été corrigée par rapport à celle de la souris)
     */
    public void mouseLeftDrag(ComposantGraphique cg, Vecteur deplacementTotal, Point origine, Point souris, Point curseur) {
        fireDeplacerRepere(new Vecteur(souris, origine));//Le repère se déplace dans le sens opposé à la souris
    }

    /**
     * Renvoie le filtre définissant les éléments attendus par le clic droit
     * @return filtre clicDroit
     */
    public Filtre getFiltreClicDroit() {
        List<Filtre> L = new LinkedList<>();
        for(ActionClicDroit action : listeActionsClicDroit) {
            L.add(action.getFiltre());
        }
        Filtre f = new Filtre.UnionFilter(L);
        return f;
    }
    
    /**
     * Renvoie le filtre définissant les éléments attendus par le clic gauche
     * @return filtre clicGauche
     */
    public Filtre getFiltreClicGauche() {
        if(kit!=null) {return kit.getFiltreCourant();}
        else {return Filtre.filtreTotal();}
    }

    //Gestion des ModuleListener
    private ModuleListener controller;
    public void setGraphController(ModuleListener controller) {this.controller = controller; addPropertyChangeListener(controller);}
    public void removeGraphController() {removePropertyChangeListener(this.controller); this.controller = null;}
    protected void fireObjectsCreated(ObjectCreation creation) {
        ListComposant toAdd;
        ComposantGraphique mainElement = creation.getMainElement();
        ComposantGraphique mainElementAlreadyExisting = permanentList.get(mainElement);
        if(mainElementAlreadyExisting!=null) {
            mainElementAlreadyExisting.setCouleur(mainElement.getCouleur());
            mainElementAlreadyExisting.setNom(mainElement.getNom());
            mainElementAlreadyExisting.setPointille(mainElement.isPointille());
            ((UndoableListComposant)permanentList).setModified(true);//HACK pour détecter les changements de couleur ou de nom
            toAdd = creation.getAnnexElements();
        } else {
            toAdd = creation.getList();
        }
        this.controller.objectsCreated(toAdd);
    }
    protected void fireObjectsRemoved(ListComposant L) {
        this.controller.objectsRemoved(L);
    }
    protected void fireTemporaryObjects() {
        this.controller.temporaryObjects(getTemporaryList());
    }
    protected void fireMessages() {
        this.controller.temporaryMessages(getMessages());
    }
    protected void fireUpdate() {
        this.controller.objectsUpdated();
    }
    protected void fireContextMenu() {
        this.controller.showContextMenu(actionsClicDroit);
    }
    protected void fireDeplacerRepere(Vecteur deplacement) {
        this.controller.deplacerRepere(deplacement);
    }

    //Gestion des PropertyChangeListener
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    protected void fireModeChanged(int oldValue, int newValue) {
        support.firePropertyChange(MODE_PROPERTY, oldValue, newValue);
    }
    protected void fireRightClicChanged(boolean oldValue, boolean newValue) {
        support.firePropertyChange(RIGHT_CLIC_AVAILABLE_PROPERTY, oldValue, newValue);
    }
    protected void fireTextFocusableChanged(boolean oldValue, boolean newValue) {
        support.firePropertyChange(TEXT_FOCUSABLE_PROPERTY, oldValue, newValue);
    }
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    /**
     * Cette classe permet la mise en application d'une fonctionnalité spécifique
     * pour un module donné.
     * @author François Billioud
     */
    public abstract class Kit {
        /** 
         * Ajoute le composant aux objets sélectionnés pour la construction
         * @param cg le composant à ajouter
         * @param souris position de la souris
         * @return true si la construction est complète. false sinon
         **/
        public abstract boolean select(ComposantGraphique cg, Point souris);
        /** 
         * Donne un apercu des éléments générés par l'ajout de cg aux objets sélectionnés
         * @param cg le composant à ajouter
         * @param souris position de la souris
         * @return La liste des objets à afficher
         **/
        public abstract ListComposant apercu(ComposantGraphique cg, Point souris);
        
        protected Filtre filtre = Filtre.filtreLaxiste();
        /** renvoie le filtre permettant de dire pour tout ComposantGraphique s'il peut faire partie de la construction **/
        public Filtre getFiltreCourant() {
            return filtre;
        }
        
        /**
         * Détermine si le curseur est considéré comme un composant théoriquement acceptable pour la construction.
         * Toutefois, si le filtre n'accepte pas le curseur, le curseur sera tout de même refusé.
         **/
        public boolean accepteCurseur() {return true;}
        
        //Gestion des KitListener
//        private Module kitListener;
//        public void setKitListener(Module listener) {this.kitListener = listener;}
//        public void removeKitListener() {this.kitListener = null;}
        protected void fireObjectsCreated(ObjectCreation o) {
            Module.this.fireObjectsCreated(o);
        }
        protected void fireObjectsRemoved(ListComposant L) {
            Module.this.fireObjectsRemoved(L);
        }
        protected void fireObjectsUpdated() {
            Module.this.fireUpdate();
        }
        
        protected ComposantGraphique creerComposantPermanent(ComposantGraphique cg) {
            cg.setCouleur(getCouleur());
            return cg;
        }
        protected ComposantGraphique creerComposantTemporaire(ComposantGraphique cg) {
            cg.setCouleur(getCouleurTemporaire());
            if(cg instanceof Texte) {((Texte)cg).setEditable(false);}
            return cg;
        }
    }
    
//    protected class KitListenerImpl implements KitListener {
//        public void objectsCreated(ObjectCreation o) { fireObjectsCreated(o); }
//        public void objectsRemoved(ListComposant L) { fireObjectsRemoved(L); }
//        public void objectsUpdated() { fireUpdate(); }
//    }
//    
//    protected static interface KitListener {
//        public void fireObjectsCreated(ObjectCreation o);
//        public void fireObjectsRemoved(ListComposant L);
//        public void fireUpdate();
//    }
//    
    public static interface ModuleListener extends PropertyChangeListener {
        public void objectsCreated(ListComposant o);
        public void temporaryObjects(ListComposant L);
        public void objectsRemoved(ListComposant L);
        public void temporaryMessages(List<String> messages);
        public void objectsUpdated();
        public void showContextMenu(List<Action> actions);
        public void deplacerRepere(Vecteur deplacement);
    }
    
    public class ActionChangeMode extends ActionComplete.Toggle {
        private int mode;
        public ActionChangeMode(int mode, String aspect) {
            super(aspect, false);
            this.mode = mode;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            setMode(mode);
            Kit kit = creerKit(mode);
            setKit(kit);
        }
    }
    
    /** Action à utiliser pour une action de menu contextuel.
     * La variable filtre doit être modifiée pour modifier les cas d'utilisation.
     * On peut envisager de mettre cette variable dans les paramètres du constructeur
     **/
    public static abstract class ActionClicDroit extends ActionComplete {
        protected Filtre filtre = Filtre.filtreLaxiste();
        protected ComposantGraphique cg;
        public ActionClicDroit(String aspect) {super(aspect);}
        public ActionClicDroit(String aspect, ComposantGraphique cg) {
            this(aspect);this.cg = cg;
        }
        public void setComposant(ComposantGraphique cg) {this.cg = cg;}
        public Filtre getFiltre() {return filtre;}
        public boolean isNull() {return !getFiltre().accepte(cg);}
        @Override
        public abstract ActionClicDroit clone();
    }
    
    /**
     * Cette classe définit le fonctionnement général d'un module de la partie graph.
     * @author François Billioud
     */
    public static abstract class ModuleGraph extends Module {
        
        public static final int POINT = 1;
        public static final int SUPPRIMER = 2;
        public static final int RENOMMER = 3;
        public static final int TEXTE = 4;
        public static final int COLORER = 5;
    
        public static final int ACTION_POINTILLES_LECTURE = 100;
        public static final int ACTION_COORDONNEES_CURSEUR = 101;

        public ActionComplete.Toggle actionAffichagePointilles = new ActionAffichagePointilles(false);
        public ActionComplete.Toggle actionAffichageCoordonnees = new ActionAffichageCoordonnees(false);
        private class ActionAffichagePointilles extends ActionComplete.Toggle {
            private ActionAffichagePointilles(boolean etatInitial) {
                super("graphic display readlines", etatInitial);
            }
            @Override
            public void actionPerformed(ActionEvent e) { fireTemporaryObjects(); }
        }

        private class ActionAffichageCoordonnees extends ActionComplete.Toggle {
            private ActionAffichageCoordonnees(boolean etatInitial) {
                super("graphic display cursor", etatInitial);
            }
            @Override
            public void actionPerformed(ActionEvent e) { fireMessages(); }
        }
        
        @Override
        protected ListComposant getTemporaryList() {
            ListComposant L = new ListComposant(super.getTemporaryList());
            if (actionAffichagePointilles.isSelected() && !isMouseOut()) {
                Segment s1 = new Segment.AB(curseur(),new Point.XY(curseur().x(),0));
                Segment s2 = new Segment.AB(curseur(),new Point.XY(0,curseur().y()));
                s1.setPointille(true);
                s2.setPointille(true);
                L.add(s1);
                L.add(s2);
            }
            return L;
        }
        
        @Override
        protected List<String> getMessages() {//On ajoute les coordonnées du curseur aux messages
            List<String> m = new LinkedList<>(super.getMessages());
            if(actionAffichageCoordonnees.isSelected() && !isMouseOut()) {
                m.add(0, "(" + ((double) Math.round(curseur().x() * 100)) / 100 + " , " + ((double) Math.round(curseur().y() * 100)) / 100 + ")");
            }
            return m;
        }
        
        @Override
        public Action getAction(int action) {
            Action a;
            switch(action) {
                case NORMAL : a = actionNormal; break;
                case POINT : a = actionPoints; break;
                case TEXTE : a = actionTexte; break;
                case COLORER : a = actionColorer; break;
                case RENOMMER : a = actionRenommer; break;
                case SUPPRIMER : a = actionSupprimer; break;
                    
                case ACTION_COORDONNEES_CURSEUR : a = actionAffichageCoordonnees; break;
                case ACTION_POINTILLES_LECTURE : a = actionAffichagePointilles; break;
                default : a = null;
            }
            return a;
        }
        
        @Override
        public Data getDonnees() {
            Data donnees = new DataObject();
            donnees.putElement("coordonnees curseur", actionAffichageCoordonnees.isSelected()+"");
            donnees.putElement("affichage pointilles", actionAffichagePointilles.isSelected()+"");
            return donnees;
        }
        
        @Override
        public void charger(UndoableListComposant listeObjetsConstruits, Data donneesModule) {
            super.charger(listeObjetsConstruits, donneesModule);
            actionAffichageCoordonnees.setSelected(Boolean.parseBoolean(donneesModule.getElement("coordonnees curseur")));
            actionAffichagePointilles.setSelected(Boolean.parseBoolean(donneesModule.getElement("affichage pointilles")));
        }
        
        @Override
        public void setMode(int mode) {
            int old = this.getMode();
            super.setMode(mode);
            if(mode!=old) {fireTextFocusableChanged(mode!=NORMAL, mode==NORMAL);}
        }

        @Override
        public void retourModeNormal() {
            setMode(NORMAL);
            actionNormal.setSelected(true);
            setKit(creerKit(NORMAL));
        }
        
        protected final ActionGroup actions = new ActionGroup();
        protected final ActionChangeMode actionNormal = creerActionChangeMode(NORMAL, "graphic base");
        protected final ActionChangeMode actionPoints = creerActionChangeMode(POINT, "graphic dot");
        protected final ActionChangeMode actionColorer = creerActionChangeMode(COLORER, "graphic paint");
        protected final ActionChangeMode actionRenommer = creerActionChangeMode(RENOMMER, "graphic rename");
        protected final ActionChangeMode actionSupprimer = creerActionChangeMode(SUPPRIMER, "graphic remove");
        protected final ActionChangeMode actionTexte = creerActionChangeMode(TEXTE, "graphic text");
        {
            actionNormal.setSelected(true);
        }
        
        protected ActionChangeMode creerActionChangeMode(int mode, String aspect) {
            ActionChangeMode action = new ActionChangeMode(mode, aspect);
            actions.add(action);
            return action;
        }
        
    }
    
    protected void renommer(final ComposantGraphique cg) {
        PanelMarquage.renommer(cg);
        cg.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(ComposantGraphique.NAME_PROPERTY)) {
                    fireUpdate();
                    cg.removePropertyChangeListener(this);
                }
            }
        });
    }
    
    protected class KitNormal extends Kit {
        public KitNormal() {}
        {filtre = new Filtre(Texte.class).nonPassif();}
        @Override
        public boolean select(ComposantGraphique cg, Point souris) {
            ((Texte)cg).getTextComponent().requestFocus();
            return true;
        }
        @Override
        public ListComposant apercu(ComposantGraphique cg, Point souris) {return new ListComposant();}
    }
    protected class KitTexte extends Kit {
        public KitTexte() {}
        {filtre = new Filtre(Point.class);}
        @Override
        public boolean select(ComposantGraphique cg, Point souris) {
            Texte texte = (Texte)creerComposantPermanent(new Texte(souris));
            fireObjectsCreated(new ObjectCreation(texte));
            retourModeNormal();//XXX à discuter, mais plus pertinent
            texte.getTextComponent().requestFocus();
            return true;
        }
        @Override
        public ListComposant apercu(ComposantGraphique cg, Point souris) {
            Texte text = (Texte)creerComposantTemporaire(new Texte(souris,Traducteur.traduire("graphic your text here")));
            text.setEditable(false);
            return new ListComposant(text);
        }
    }
    protected class KitColorer extends Kit {
        public KitColorer() {}
        {filtre = Filtre.filtreLaxiste();}
        @Override
        public boolean select(ComposantGraphique cg, Point souris) {
            creerComposantPermanent(cg);
            fireObjectsUpdated();
            return true;
        }
        @Override
        public ListComposant apercu(ComposantGraphique cg, Point souris) {
            cg.setCouleur(getCouleur());
            return new ListComposant(cg);
        }
        @Override
        public boolean accepteCurseur() {return false;}
    }
    protected class KitSupprimer extends Kit {
        public KitSupprimer() {}
        {filtre = Filtre.filtreLaxiste();}
        @Override
        public boolean select(ComposantGraphique cg, Point souris) {
            ListComposant L = new ListComposant(cg);
            if(cg instanceof Composant.Legendable) {L.add(((Composant.Legendable)cg).getLegende());}//si on supprime un légendable, on supprime la légende
            if(cg instanceof Texte.Legende) {((Texte.Legende)cg).getDependance().setLegende(null);}//si on supprime une légende, on le signale au légendable
            fireObjectsRemoved(L);
            return true;
        }
        @Override
        public ListComposant apercu(ComposantGraphique cg, Point souris) {
            return new ListComposant(cg);
        }
        @Override
        public boolean accepteCurseur() {return false;}
    }
    protected class KitRenommer extends Kit {
        {filtre = PanelMarquage.getFiltreRenommer().nonPassif();}
        public KitRenommer() {}
        @Override
        public boolean select(ComposantGraphique cg, Point souris) {
            if(cg!=null) {renommer(cg);}
            return true;
        }
        @Override
        public ListComposant apercu(ComposantGraphique cg, Point souris) {
            return new ListComposant(cg);
        }
        @Override
        public boolean accepteCurseur() {return false;}
    }
    protected class ActionRenommerClicDroit extends ActionClicDroit {
        public ActionRenommerClicDroit() {super("graphic rename rightclic");}
        @Override
        public void actionPerformed(ActionEvent e) {renommer(cg);}
        {filtre = PanelMarquage.getFiltreRenommer().nonPassif();}
        @Override
        public ActionClicDroit clone() {ActionClicDroit action = new ActionRenommerClicDroit();action.setComposant(cg);return action;}
    }
    protected class ActionCoder extends ActionClicDroit {
        public ActionCoder() {super("graphic create mark");}
        @Override
        public void actionPerformed(ActionEvent e) {PanelMarquage.marquer(cg);}
        @Override
        public ActionClicDroit clone() {ActionClicDroit action = new ActionCoder();action.setComposant(cg);return action;}
        {filtre = PanelMarquage.getFiltreMarquer();}
    }
    protected class ActionPointilles extends ActionClicDroit {
        {filtre.addVerificateur(new Filtre.VerificationSpeciale() {
            @Override
            public boolean accepte(ComposantGraphique cg) {
                if(cg instanceof Point) {return false;}
                else if(cg instanceof Texte) {return false;}
                return true;
            }
        });}
        public ActionPointilles() { super("graphic dashed line"); }
        @Override
        public void actionPerformed(ActionEvent e) { cg.setPointille(!cg.isPointille()); }
        @Override
        public ActionClicDroit clone() {ActionClicDroit action = new ActionPointilles();action.setComposant(cg);return action;}
    }
    
    public static class ObjectCreation {
        private final ComposantGraphique mainElement;
        private final ListComposant annexElements;
        public ObjectCreation(ComposantGraphique mainElement) {
            this.mainElement = mainElement;
            this.annexElements = new ListComposant();
        }
        public ObjectCreation(ComposantGraphique mainElement, ListComposant annexElements) {
            this.mainElement = mainElement;
            this.annexElements = annexElements;
        }
        public ComposantGraphique getMainElement() { return mainElement; }
        public ListComposant getAnnexElements() { return annexElements; }
        /** Renvoie la liste contenant le mainElement et les annexElements **/
        public ListComposant getList() {
            ListComposant L = new ListComposant(annexElements);
            if(mainElement!=null) {L.addFirst(mainElement);}
            return L;
        }
    }
    
    public static class ComposantEnlighter {
        private Color focusedColor = couleurSelection;
        private Color oldColor;
        private ComposantGraphique composant = null;
        public ComposantEnlighter() {}
        public ComposantEnlighter(ComposantGraphique cg) { setComposant(cg); }
        public ComposantGraphique getComposant() {return composant;}
        public final void setComposant(ComposantGraphique cg) {
            resetColor();
            composant = cg;
            if(cg!=null) {
                oldColor = cg.getCouleur();
                composant.setCouleur(focusedColor);
            }
        }
        public void setFocusedColor(Color c) { focusedColor = c; if(composant!=null) {composant.setCouleur(c);} }
        public void resetColor() {if(composant!=null) composant.setCouleur(oldColor);}
    }

}
