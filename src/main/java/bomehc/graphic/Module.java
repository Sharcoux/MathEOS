/*
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of bomehc.
 *
 * bomehc is free software: you can redistribute it and/or modify
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
 * These additional terms refer to the source code of bomehc.
 *
 * According to GNU GPL v3, section 7 b) :
 * You should mention any contributor of the work as long as his/her contribution
 * is meaningful in a covered work. If you convey a source code using a part of the
 * source code of Bomehc, you should keep the original author in the resulting
 * source code. If you propagate a covered work with the same objectives as the
 * Program (help student to attend maths classes with an adapted software), you
 * should mention «Ludovic Faubourg», «Frédéric Marinoni» as author of the idea of
 * this software. In any case, if you propagate a covered work you have to mention
 * François Billioud and Guillaume Varoquaux as author of the realisation of Bomehc
 * software. The paternity of the authors have to appear in a legible, unobscured
 * manner, showing clearly their link to the covered work in any document,
 * web pages,... which describe the project or participate to the distribution of
 * the covered work.
 */

package bomehc.graphic;

import bomehc.graphic.composants.Composant;
import bomehc.graphic.composants.ComposantGraphique;
import bomehc.graphic.composants.Point;
import bomehc.graphic.composants.Segment;
import bomehc.graphic.composants.Texte;
import bomehc.graphic.composants.Vecteur;
import bomehc.sauvegarde.Data;
import bomehc.sauvegarde.DataObject;
import bomehc.utils.boutons.ActionComplete;
import bomehc.utils.boutons.ActionGroup;
import bomehc.utils.managers.ColorManager;
import bomehc.utils.managers.Traducteur;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import bomehc.IHM;
import bomehc.graphic.composants.Composant.Legendable;
import bomehc.graphic.composants.Texte.Legende;
import bomehc.utils.dialogue.DialogueBloquant;
import bomehc.utils.managers.CursorManager;
import bomehc.utils.managers.ImageManager;
import bomehc.utils.managers.PermissionManager;
import bomehc.utils.objets.Icone;

/**
 * Cette classe définit les interactions entre l'utilisateur et un espace
 * mathématique virtuel. Un module est indépendant du modèle et de la vue.
 * @author François Billioud
 */
public abstract class Module {

    public static final String MODE_PROPERTY = "mode";
    public static final String RIGHT_CLIC_AVAILABLE_PROPERTY = "right-clic";
    public static final String TEXT_FOCUSABLE_PROPERTY = "text-focusable";
    public static final String COLOR_PROPERTY = "color";
    
    //JComboBox pour les couleurs
    public static final Icone[] REF_COULEURS;
    public static final Color[] COULEURS;
    static {
        String[] balises = IHM.getThemeElementBloc("color drawing");
        REF_COULEURS = new Icone[balises.length];
        COULEURS = new Color[balises.length];
        for (int i = 0; i < balises.length; i++) {
            REF_COULEURS[i] = ImageManager.getIcone("icon " + balises[i], 40, 20);//XXX créer une image plutôt
            COULEURS[i] = ColorManager.get(balises[i]);
        }
    }
    
    public static final int NORMAL = 0;
    
    private Point.XY curseur = new Point.XY(0,0);
    private boolean mouseOut = true;
    protected boolean isMouseOut() {return mouseOut;}
    protected Point curseur() {return curseur;}

    private Color couleur;
    public void setCouleur(Color c) {
        if(c==couleur) {return;}
        Color old = couleur;
        couleur = c;
        support.firePropertyChange(COLOR_PROPERTY, old, c);
    }
    public Color getCouleur() {return couleur;}
    
    private final Color couleurTemporaire = ColorManager.get("color temp component");
    private final Color couleurSelection = ColorManager.get("color focused component");
    protected Color getCouleurTemporaire() {return couleurTemporaire;}

    private UndoableListComposant permanentList;
    private ListComposant temporaryList = new ListComposant();
    private List<String> messages = new LinkedList<>();
    protected UndoableListComposant getPermanentList() {return permanentList;}
    protected ListComposant getTemporaryList() {return temporaryList;}
    protected List<String> getMessages() {return messages;}

    public void setElementsPermanents(UndoableListComposant L) {
        if(permanentList!=null) {permanentList.removeListComposantListener(legendeUndoListener);}
        permanentList = L;
        if(L!=null) {L.addListComposantListener(legendeUndoListener);}
    }
    
    protected JTextComponent focusedTextComponent=null;
    private ListComposant.ListComposantListener legendeUndoListener = new ListComposant.ListComposantListener() {
        private final FocusListener textFocusListened = new FocusListener() {
            public void focusGained(FocusEvent e) {focusedTextComponent = (JTextComponent) e.getSource();}
            public void focusLost(FocusEvent e) {}
        };
        @Override
        public boolean add(ListComposant source, ComposantGraphique cg) {
            if(cg instanceof Legende) {//HACK : si on annule la suppression d'une légende, il faut recréer le lien entre la légende et l'élément.
                Legende l = ((Legende)cg);
                if(l.getDependance()!=null) {
                    Color c = l.getCouleur();
                    l.getDependance().setLegende(l);//On s'assure ainsi que le composant et sa légende sont liés
                    l.getDependance().setLegendeColor(c);//On s'assure que la légende préserve sa couleur.
                }
            }
            if(cg instanceof Texte) {//HACK : Pour désélectionner un texte par clic hors du texte
                ((Texte)cg).getTextComponent().addFocusListener(textFocusListened);
            }
            return true;
        }
        @Override
        public boolean addAll(ListComposant source, Collection<? extends ComposantGraphique> L) {
            for(ComposantGraphique cg : L) {add(source, cg);}
            return true;
        }
        @Override
        public boolean remove(ListComposant source, ComposantGraphique cg) {
            if(cg instanceof Legendable && ((Legendable)cg).getLegende()!=null) {source.remove(((Legendable)cg).getLegende());}//si on supprime un légendable, on supprime la légende
            if(cg instanceof Legende) {//HACK : si on annule la création d'une légende, il faut supprimer la légende du composant associé
                Legende l = ((Legende)cg);
                if(l.getDependance()!=null) {l.getDependance().setLegende((Legende)null);}//On s'assure ainsi que le composant ne fait plus référence à la légende
            }
            if(cg instanceof Texte) {//HACK : Pour désélectionner un texte par clic hors du texte
                ((Texte)cg).getTextComponent().removeFocusListener(textFocusListened);
            }
            return true;
        }
        @Override
        public boolean removeAll(ListComposant source, Collection<? extends ComposantGraphique> L) {
            for(ComposantGraphique cg : L) {remove(source, cg);}
            return true;
        }
        @Override
        public boolean clear(ListComposant source, Collection<? extends ComposantGraphique> L) {
            for(ComposantGraphique cg : L) {remove(source, cg);}
            return true;
        }
    };
    
    private Module.ComposantEnlighter choixPotentielEnlighter = new Module.ComposantEnlighter();
    private List<Action> actionsClicDroit = new LinkedList<>();
    /** Définit cg comme le composant visé par le clic gauche et le met en couleur
     * @param cg le composant répondant à getFiltreClicGauche().accept(cg), ou null**/
    protected void setChoixPotentiel(ComposantGraphique cg) {
        choixPotentielEnlighter.setComposant(cg);
        fireUpdate(false);
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
        if(donneesModule.containsElementKey(COLOR_PROPERTY)) setCouleur(ColorManager.getColorFromHexa(donneesModule.getElement(COLOR_PROPERTY)));
        else {setCouleur(COULEURS[0]);}
    }
    public Data getDonnees() {
        Data donnees = new DataObject();
        donnees.putElement(COLOR_PROPERTY, ColorManager.getRGBHexa(getCouleur()));
        return donnees;
    }
    
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
    public abstract void mouseLeftPressed(ComposantGraphique cg, Point souris, Point curseur);

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
    public abstract void mouseLeftReleased(ComposantGraphique cg, Point souris, Point curseur);

    /**
     * Signale la fin d'un clic au constructeur
     * @param cg le composantGraphique répondant au filtre actuel
     * @param souris la position de la souris au moment de l'appel
     * @param curseur position du curseur (qui prend peut avoir été corrigée par rapport à celle de la souris)
     * @param distanceDrag vecteur représentant le draggage depuis le dernier MousePressed
     */
    public abstract void mouseLeftDragReleased(ComposantGraphique cg, Point souris, Point curseur, Vecteur distanceDrag);

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
        } else if(getFiltreClicGauche().accepte(curseur) && kit!=null && kit.accepteCurseur()) {
            setChoixPotentiel(curseur);
        } else {
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
    public ModuleListener getGraphController() {return controller;}
    public void setGraphController(ModuleListener controller) {this.controller = controller; addPropertyChangeListener(controller);}
    public void removeGraphController() {removePropertyChangeListener(this.controller); this.controller = null;}
    protected void fireObjectsCreated(ObjectCreation creation) {
        ListComposant toAdd;
        ComposantGraphique mainElement = creation.getMainElement();
        ComposantGraphique mainElementAlreadyExisting = permanentList.get(mainElement);
        if(mainElementAlreadyExisting!=null) {
            //HACK pour détecter si oui ou non une modification a lieu
            ModificationListener listener = new ModificationListener();
            mainElementAlreadyExisting.addPropertyChangeListener(listener);
            mainElementAlreadyExisting.setCouleur(mainElement.getCouleur());
            mainElementAlreadyExisting.setNom(mainElement.getNom());
            mainElementAlreadyExisting.setPointille(mainElement.isPointille());
            if(mainElement instanceof Legendable) {
                Legende l = ((Legendable)mainElement).getLegende();
                if(l!=null) {
                    ((Legendable)mainElementAlreadyExisting).setLegende(l);
                    l.setDependance((Legendable)mainElementAlreadyExisting);
                }
            }
            if(listener.modified) {fireUpdate(true);}
            mainElementAlreadyExisting.removePropertyChangeListener(listener);
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
    protected void fireUpdate(boolean hasBeenModified) {
        this.controller.objectsUpdated(hasBeenModified);
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
            //On ajoute les légendes éventuelles
            for(ComposantGraphique objet : o.getList()) {
                if(objet instanceof Legendable) {
                    Legendable l = (Legendable)objet;
                    if(l.getLegende()!=null) {
                        creerComposantTemporaire(l.getLegende());
                        l.setLegendeColor(getCouleurTemporaire());
                        o.annexElements.addOnce(l.getLegende());
                    }
                }
            }
            Module.this.fireObjectsCreated(o);
        }
        protected void fireObjectsRemoved(ListComposant L) {
            Module.this.fireObjectsRemoved(L);
        }
        protected void fireObjectsUpdated(boolean hasBeenModified) {
            Module.this.fireUpdate(hasBeenModified);
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
        public void objectsUpdated(boolean hasBeenModified);
        public void showContextMenu(List<Action> actions);
        public void deplacerRepere(Vecteur deplacement);
        public void setCursor(Cursor curseur);
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
        public static final int DRAGAGE = 6;
    
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
                case DRAGAGE : a = actionDragage; break;
                    
                case ACTION_COORDONNEES_CURSEUR : a = actionAffichageCoordonnees; break;
                case ACTION_POINTILLES_LECTURE : a = actionAffichagePointilles; break;
                default : a = null;
            }
            return a;
        }
        
        @Override
        public Data getDonnees() {
            Data donnees = super.getDonnees();
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
            if(mode!=old && (mode==NORMAL || old==NORMAL)) {fireTextFocusableChanged(mode!=NORMAL, mode==NORMAL);}
            if(old==DRAGAGE) {activeDrag(null, false);}//On remet le bon curseur
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
        protected final ActionChangeMode actionDragage = creerActionChangeMode(DRAGAGE, "graphic drag");
        {
            actionNormal.setSelected(true);
        }
        
        protected ActionChangeMode creerActionChangeMode(int mode, String aspect) {
            ActionChangeMode action = new ActionChangeMode(mode, aspect);
            actions.add(action);
            return action;
        }

        private boolean isDraging = false;
        private Composant.Draggable draggedComponent = null;
        private void activeDrag(Composant.Draggable comp, boolean active) {
            draggedComponent = comp;
            isDraging = active;
            getGraphController().setCursor(CursorManager.getCursor(active ? CursorManager.CLOSED_HAND_CURSOR : comp==null ? Cursor.DEFAULT_CURSOR : CursorManager.OPENED_HAND_CURSOR));
        }
        @Override
        public void mouseLeftDrag(ComposantGraphique cg, Vecteur deplacementTotal, Point origine, Point souris, Point curseur) {
            if(isDraging && draggedComponent!=null) {
                draggedComponent.drag(deplacementTotal);
                fireUpdate(true);
            }
            else {super.mouseLeftDrag(cg, deplacementTotal, origine, souris, curseur);}//Le repère se déplace dans le sens opposé à la souris
        }
        @Override
        public void mouseLeftDragReleased(ComposantGraphique cg, Point souris, Point curseur, Vecteur distanceDrag) {
            if(getMode()==DRAGAGE) activeDrag((cg instanceof Composant.Draggable) ? ((Composant.Draggable)cg) : null, false);
        }
        @Override
        public void mouseLeftReleased(ComposantGraphique cg, Point souris, Point curseur) {
            if(getMode()==DRAGAGE) activeDrag((cg instanceof Composant.Draggable) ? ((Composant.Draggable)cg) : null, false);
        }
        @Override
        public void mouseLeftPressed(ComposantGraphique cg, Point souris, Point curseur) {
            if(getMode()==DRAGAGE && cg!=null && cg instanceof Texte) {
                ModuleGraph.this.activeDrag((Composant.Draggable)cg, true);
            }
        }
        @Override
        public void mouseMove(ComposantGraphique cgGauche, ComposantGraphique cgDroit, Point souris, Point curseur) {
            if(getMode()==DRAGAGE) {
                if(cgGauche!=null && cgGauche instanceof Texte) {getGraphController().setCursor(CursorManager.getCursor(CursorManager.OPENED_HAND_CURSOR));}
                else {getGraphController().setCursor(CursorManager.getCursor(Cursor.DEFAULT_CURSOR));}
            }
            if(getFiltreClicDroit().accepte(cgGauche)) {cgDroit = cgGauche;}//Si le composant gauche est accepté, on préfère l'utiliser car c'est plus intuitif.
            super.mouseMove(cgGauche, cgDroit, souris, curseur);
        }

        protected class KitDragage extends Kit {
            {filtre = new Filtre(Texte.class);}
            public KitDragage() {}
            @Override
            public boolean select(ComposantGraphique cg, Point souris) {return true;}
            @Override
            public ListComposant apercu(ComposantGraphique cg, Point souris) {return new ListComposant();}
            @Override
            public boolean accepteCurseur() {return false;}
        }
    }
    
    protected void renommer(final ComposantGraphique cg) {
        cg.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(Legendable.LEGENDE_PROPERTY)) {
                    Legende l = (Legende) evt.getNewValue();
                    if(l!=null) {fireObjectsCreated(new ObjectCreation(l));}
                    Legende old = (Legende)evt.getOldValue();
                    if(old!=null) {fireObjectsRemoved(new ListComposant(old));}
                    cg.removePropertyChangeListener(this);
                }
            }
        });
        clearTemporaryElements();
        String oldName = cg.getNom();
        String newName = PanelMarquage.renommer(cg);
        if(newName!=null && !newName.equals(oldName)) {fireUpdate(true);}
    }
    
    protected class KitNormal extends Kit {
        public KitNormal() {}
        {filtre = new Filtre(Texte.class, Point.class).nonPassif();}
        @Override
        public boolean select(ComposantGraphique cg, Point souris) {
            if(cg!=null && cg instanceof Texte) {(focusedTextComponent = ((Texte)cg).getTextComponent()).requestFocus();}
            else {
                if(focusedTextComponent!=null) {
                    focusedTextComponent.setFocusable(false);
                    focusedTextComponent.setFocusable(true);
                    focusedTextComponent = null;
                }
            }
            return true;
        }
        @Override
        public ListComposant apercu(ComposantGraphique cg, Point souris) {
            return new ListComposant();
        }
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
            return new ListComposant(text);
        }
    }
    protected class KitColorer extends Kit {
        public KitColorer() {}
        {filtre = Filtre.filtreLaxiste();}
        @Override
        public boolean select(ComposantGraphique cg, Point souris) {
            creerComposantPermanent(cg);
            fireObjectsUpdated(true);
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
        public void actionPerformed(ActionEvent e) {
            if(!PermissionManager.isTracerMarquageAllowed()) {DialogueBloquant.dialogueBloquant("not allowed");return;}
            PanelMarquage.marquer(cg);
            fireUpdate(true);
        }
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
    
    private class ModificationListener implements PropertyChangeListener {
        boolean modified = false;
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            modified = true;
        }
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
    
    public class ComposantEnlighter {
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
