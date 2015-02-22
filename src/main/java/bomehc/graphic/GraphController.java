/**
 * «Copyright 2013 François Billioud»
 *
 * This file is part of Bomehc.
 *
 * Bomehc is free software: you can redistribute it and/or modify under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * Bomehc is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY.
 *
 * You should have received a copy of the GNU General Public License along with
 * Bomehc. If not, see <http://www.gnu.org/licenses/>.
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
 */package bomehc.graphic;

import bomehc.graphic.EspaceDessin.EspaceDessinListener;
import bomehc.graphic.composants.ComposantGraphique;
import bomehc.graphic.composants.Point;
import bomehc.graphic.composants.Texte;
import bomehc.graphic.composants.Vecteur;
import bomehc.json.Json;
import bomehc.sauvegarde.Data;
import bomehc.sauvegarde.Data.Enregistrable;
import bomehc.sauvegarde.DataObject;
import bomehc.utils.interfaces.Undoable;
import bomehc.utils.objets.MenuContextuel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.SwingPropertyChangeSupport;
import bomehc.graphic.Module.ModuleListener;
import bomehc.utils.managers.CursorManager;
import bomehc.utils.texte.JMathTextPane;

/**
 * Cette classe fait le lien entre le modèle, l'espaceDessin et les modules
 * @author François Billioud
 */
public class GraphController implements Undoable, ModuleListener, Enregistrable {

    //nom des données
    private static final String LISTE = "liste";
    private static final String REPERE = "repere";
    private static final String MODULES = "modules";

    private final UndoableListComposant objetsPermanents = new UndoableListComposant();
    private final ListComposant objetsTemporaires = new ListComposant();
    private List<String> messages = new LinkedList<>();
    
    public UndoableListComposant getListeObjetsConstruits() { return objetsPermanents; }
    public ListComposant getListeObjetsTemporaires() { return objetsTemporaires; }
    public Repere getRepere() { return dessin.getRepere(); }
    
    public void deplacerRepere(Vecteur deplacement) {
        dessin.deplacerRepere(deplacement);
    }
    
    public void setCursor(Cursor c) {
        dessin.setCursor(c);
        for(ComposantGraphique cg : getListeObjetsConstruits()) {
            if(cg instanceof Texte) {
                JMathTextPane jtp = ((Texte)cg).getTextComponent();
                jtp.getHTMLEditorKit().setDefaultCursor(c);
                jtp.setCursor(c);
            }
        }
    }
    
    private ComposantGraphique composantClicDroit = null;
    private ComposantGraphique composantClicGauche = null;
    
    private final EspaceDessin dessin;
    private Module module;
    public GraphController(EspaceDessin dessin) {
        this.dessin = dessin;
        //On indique au dessin les données à représenter
        dessin.setElementsPermanents(objetsPermanents);
        dessin.setElementsTemporaires(objetsTemporaires);
        dessin.setMessages(messages);
        //On écoute la souris sur le dessin
        dessin.addEspaceDessinListener(new DessinListener());
        //On écoute les modifications effectuées sur le dessin
        dessin.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                GraphController.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        //On écoute le modèle
        objetsPermanents.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                GraphController.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
    }

    public Module getModule() {return module;}
    
    public void setModule(Module m) {
        if(this.module!=null) {
            setModuleProperties(this.module);
            if(m!=null) {m.setCouleur(this.module.getCouleur());}
            this.module.removeGraphController();
        }
        this.module = m;
        if(this.module!=null) {
            Data donneesModule = getModuleProperties(this.module);
            this.module.setGraphController(this);
            this.module.charger(getListeObjetsConstruits(), donneesModule);
            temporaryObjects(this.module.getTemporaryList());
            temporaryMessages(this.module.getMessages());
        }
    }
    
    public void setCouleur(Color c) {
        if(this.module!=null) module.setCouleur(c);
    }

    @Override
    public void annuler() {
        objetsPermanents.annuler();
    }

    @Override
    public void refaire() {
        objetsPermanents.refaire();
    }

    @Override
    public boolean peutAnnuler() {
        return objetsPermanents.peutAnnuler();
    }

    @Override
    public boolean peutRefaire() {
        return objetsPermanents.peutRefaire();
    }

    @Override
    public boolean hasBeenModified() {
        return objetsPermanents.hasBeenModified();
    }

    @Override
    public void setModified(boolean b) {
        objetsPermanents.setModified(b);
    }

    protected void setTextsFocusable(boolean b) {
        for(ComposantGraphique cg : getListeObjetsConstruits().nonPassifs()) {
            if(cg instanceof Texte) { ((Texte)cg).setEditable(b); }
        }
    }

    public Data getDonnees() {
        setModuleProperties(module);//on enregistre les données du module actuel
        Data data = new DataObject();
        try {
            data.putElement(LISTE, Json.toJson(new ListComposant(objetsPermanents)));//elements.deepCopy());
        } catch (IOException ex) {
            data.putElement(LISTE, null);
            System.out.println("erreur lors de l'écriture de la liste des composants");
            Logger.getLogger(GraphController.class.getName()).log(Level.SEVERE, null, ex);
        }
        data.putData(MODULES, donneesModules);
        data.putData(REPERE, dessin.getRepere().getDonnees());
        return data;
    }
    public void charger(Data donnees) {
        ListComposant liste;
        try {
            liste = (ListComposant) Json.toJava(donnees.getElement(LISTE),ListComposant.class);
            if(liste==null) {liste = new ListComposant();}
        } catch(IOException ex) {
            liste = new ListComposant();
            Logger.getLogger(OngletGraph.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("erreur lors de la lecture de la liste des composants");
        }
        Data r = donnees.getData(REPERE);
        donneesModules = donnees.getData(MODULES);
        
        getListeObjetsConstruits().charger(liste);
        getRepere().charger(r);
        module.charger(getListeObjetsConstruits(), getModuleProperties(module));
            
        dessin.repaint();
    }
    private Data donneesModules = new DataObject();
    private void setModuleProperties(Module m) {
        donneesModules.putData(m.getClass().getSimpleName(), m.getDonnees());
    }
    private Data getModuleProperties(Module m) {
        Data data = donneesModules.getData(m.getClass().getSimpleName());
        return data==null ? new DataObject() : data;
    }
    
    public void objectsCreated(ListComposant L) {
        objetsPermanents.addAllOnce(L);
    }

    public void temporaryObjects(ListComposant L) {
        objetsTemporaires.clear();
        objetsTemporaires.addAll(L);
        dessin.repaint();
    }

    public void objectsRemoved(ListComposant L) {
        //TODO rajouter ici la suppression des objets devenus invalides (marques d'orthogonalité, texte, angle...)
        ListComposant aSupprimer = new ListComposant(L);

        objetsPermanents.removeAll(aSupprimer);
    }

    public void temporaryMessages(List<String> messages) {
        this.messages = new LinkedList<>(messages);
        dessin.setMessages(this.messages);
    }
    
    public void objectsUpdated(boolean hasBeenModified) {
        if(hasBeenModified) setModified(true);
        dessin.repaint();
    }
    
    public void showContextMenu(List<Action> actions) {
        MenuContextuel menu = new MenuContextuel(actions);
        java.awt.Point p = dessin.getMousePosition(true);
        menu.show(dessin, p.x, p.y);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch(evt.getPropertyName()) {
            case Module.RIGHT_CLIC_AVAILABLE_PROPERTY :
                if(evt.getNewValue()==Boolean.TRUE) {//Change le curseur lorsqu'un clic-droit est possible
                    setCursor(CursorManager.getCursor(Cursor.CUSTOM_CURSOR));
                } else {
                    setCursor(CursorManager.getCursor(Cursor.DEFAULT_CURSOR));
                }
                break;
            case Module.TEXT_FOCUSABLE_PROPERTY : setTextsFocusable((Boolean)evt.getNewValue()); break;
        }
    }

    private class DessinListener implements EspaceDessinListener {
        @Override
        public void mouseIn(Point souris) {if(module!=null) {module.mouseIn(souris);}}
        @Override
        public void mouseOut(Point souris) {if(module!=null) {module.mouseOut(souris);}}

        @Override
        public void mouseDragged(Vecteur drag, Point origine, Point destination) {
            if(module==null) {return;}
            module.mouseLeftDrag(composantClicGauche, drag, origine, destination, getRepere().pointMagnetique(destination));
        }

        @Override
        public void mouseDragReleased(Vecteur drag, Point origine, Point destination) {
            updateComposantsClic(destination);
            if(module==null) {return;}
            module.mouseLeftDragReleased(composantClicGauche, destination, getRepere().pointMagnetique(destination), drag);
        }

        @Override
        public void mouseMoved(Point origine, Point destination) {//Changer ici l'ordre de priorité des composants si besoin
            updateComposantsClic(destination);
            if(module!=null) {module.mouseMove(composantClicGauche, composantClicDroit, destination, getRepere().pointMagnetique(destination));}
        }
        
        private void updateComposantsClic(Point positionActuelle) {
            composantClicDroit = module==null ? null : dessin.getSelectedComponent(module.getFiltreClicDroit(), positionActuelle);
            composantClicGauche = module==null ? null : dessin.getSelectedComponent(module.getFiltreClicGauche(), positionActuelle);
        }

        @Override
        public void mouseClicked(Point souris, int button) {
            if(module==null) {return;}
            if(button==MouseEvent.BUTTON1) { module.mouseLeftClicked(composantClicGauche, souris, getRepere().pointMagnetique(souris)); }
            if(button==MouseEvent.BUTTON3) { module.mouseRightClicked(composantClicDroit, souris, getRepere().pointMagnetique(souris)); }
        }

        @Override
        public void mousePressed(Point souris, int button) {
            if(module==null) {return;}
            updateComposantsClic(souris);
            if(button==MouseEvent.BUTTON1) { module.mouseLeftPressed(composantClicGauche, souris, getRepere().pointMagnetique(souris)); }
            if(button==MouseEvent.BUTTON3) { module.mouseRightPressed(composantClicDroit, souris, getRepere().pointMagnetique(souris)); }
        }

        @Override
        public void mouseReleased(Point souris, int button) {
            if(module==null) {return;}
            updateComposantsClic(souris);
            if(button==MouseEvent.BUTTON1) { module.mouseLeftReleased(composantClicGauche, souris, getRepere().pointMagnetique(souris)); }
            if(button==MouseEvent.BUTTON3) { module.mouseRightReleased(composantClicDroit, souris, getRepere().pointMagnetique(souris)); }
        }
    }

//AJOUT DE LISTENERS
    /**
     * If any <code>PropertyChangeListeners</code> have been registered, the
     * <code>changeSupport</code> field describes them.
     */
    protected SwingPropertyChangeSupport changeSupport;

    /**
     * Supports reporting bound property changes.  This method can be called
     * when a bound property has changed and it will send the appropriate
     * <code>PropertyChangeEvent</code> to any registered
     * <code>PropertyChangeListeners</code>.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (changeSupport == null) {return;}
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Adds a <code>PropertyChangeListener</code> to the listener list.
     * The listener is registered for all properties.
     * <p>
     * A <code>PropertyChangeEvent</code> will get fired in response to setting
     * a bound property, e.g. <code>setFont</code>, <code>setBackground</code>,
     * or <code>setForeground</code>.
     * Note that if the current component is inheriting its foreground,
     * background, or font from its container, then no event will be
     * fired in response to a change in the inherited property.
     *
     * @param listener  The <code>PropertyChangeListener</code> to be added
     *
     * @see Action#addPropertyChangeListener
     */
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (changeSupport == null) {changeSupport = new SwingPropertyChangeSupport(this, true);}
        changeSupport.addPropertyChangeListener(listener);
    }


    /**
     * Removes a <code>PropertyChangeListener</code> from the listener list.
     * This removes a <code>PropertyChangeListener</code> that was registered
     * for all properties.
     *
     * @param listener  the <code>PropertyChangeListener</code> to be removed
     *
     * @see Action#removePropertyChangeListener
     */
    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (changeSupport == null) {return;}
        changeSupport.removePropertyChangeListener(listener);
    }


    /**
     * Returns an array of all the <code>PropertyChangeListener</code>s added
     * to this AbstractAction with addPropertyChangeListener().
     *
     * @return all of the <code>PropertyChangeListener</code>s added or an empty
     *         array if no listeners have been added
     * @since 1.4
     */
    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        if (changeSupport == null) {return new PropertyChangeListener[0];}
        return changeSupport.getPropertyChangeListeners();
    }

}
