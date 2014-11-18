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

import matheos.graphic.composants.*;
import matheos.graphic.composants.Composant.Intersectable;

import java.util.LinkedList;
import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Classe regroupant des méthodes utiles pour la gestion des LinkedList de ComposantGraphiques
 * L'utilisation d'un Set aurait peut-être été plus judicieux à condition de savoir garder l'ordre des éléments
 * @author François Billioud
 */
@SuppressWarnings("unchecked")
public class ListComposant extends LinkedList<ComposantGraphique> implements Serializable {
    //TODO étudier l'utilisation d'un set et redéfinir la méthode equals plutôt que estEgalA
    private static final long serialVersionUID = 1L;

    /** Constante en pixels indiquant l'imprécision de l'utilisateur */ //TODO devrait être rendu non finale et éditable
    private static final int TOLERANCE = EspaceDessin.TOLERANCE;
    
    /** L'ordre de priorité des objets lors de leur sélection dans la liste, en fonction de leur Class **/
    public static final List<Class<? extends ComposantGraphique>> DEFAULT_PRIORITY_ORDER;
    static {
        Class[] sOrder = {Point.class, Texte.class, Arc.class, Segment.class, DemiDroite.class, Droite.class, ComposantGraphique.class};
        DEFAULT_PRIORITY_ORDER = Arrays.<Class<? extends ComposantGraphique>>asList(sOrder);
    }
    private final transient List<ListComposantListener> listeners = new LinkedList<>();
    public void addListComposantListener(ListComposantListener l) {listeners.add(l);}
    public void removeListComposantListener(ListComposantListener l) {listeners.remove(l);}
    public List<ListComposantListener> getListeners() {return listeners;}
    private void fireAdd(ComposantGraphique cg) {
        for(ListComposantListener l : listeners) { l.add(this, cg); }
    }
    private void fireAddAll(Collection<? extends ComposantGraphique> L) {
        for(ListComposantListener l : listeners) { l.addAll(this, L); }
    }
    private void fireRemove(ComposantGraphique cg) {
        for(ListComposantListener l : listeners) { l.remove(this, cg); }
    }
    private void fireRemoveAll(Collection<? extends ComposantGraphique> L) {
        for(ListComposantListener l : listeners) { l.removeAll(this, L); }
    }
    private void fireClear(Collection<? extends ComposantGraphique> L) {
        for(ListComposantListener l : listeners) { l.clear(this, L); }
    }

    /** gère les propertyChangeListener **/
    private final transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
    public void firePropertyChange(String property, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(property, oldValue, newValue);
    }
    
    public ListComposant() {
        super();
    }

    public ListComposant(ComposantGraphique... composants) {
        this(Arrays.asList(composants));
    }
    
    public ListComposant(Collection<? extends ComposantGraphique> L) {
        super();
        this.addAll(L);
    }

    /**
     * cherche le composant graphique ayant le nom indiqué, dans la liste
     * @param sNom nom
     * @return
     */
//    public ComposantGraphique cherche(String sNom) {
//        for(ComposantGraphique cg : this) {
//            if(cg.getNom().equals(sNom)) { return cg; }
//        }
//        return null;
//    }

    /**
     * Crée la liste des éléments considérés comme potentiellement sélectionnés.
     * C'est à dire la liste des éléments situés à une distance inférieur à la variable
     * "tolerance" de la position réelle représentée par le point P du repère "repere"
     * @param P le point étudié
     * @param repere le repère considéré
     * @return la liste des éléments rencontrés
     */
//    public ListComposant preSelection(Point P, Repere repere, List<Class> types) {
//        ListComposant L = new ListComposant();
//        for(ComposantGraphique cg : this) {
//            if(types.contains(cg.getClass())) {
//                if(cg.distance2Pixel(P,repere)<EspaceDessin.TOLERANCE) { L.add(cg);}
//            }
//        }
//        return L;
//    }

    /**
     * Filtre la liste. Pas d'effet de bord. si filtre est null, renvoie une copie de la liste
     * @param filtre le filtre à appliquer
     * @return la liste des composants correspondant aux normes du filtre
     */
    public ListComposant filtrer(Filtre filtre) {
        ListComposant L = new ListComposant();
        for(ComposantGraphique cg : this) {
            if(filtre==null || filtre.accepte(cg)) {
                L.add(cg);
            }
        }
        return L;
    }

    /**
     * Crée la liste des éléments situés à une distance (en pixel) inférieure à la variable
     * "tolerance" de la position réelle représentée par le point P du repère "repere"
     * @param P le point étudié
     * @param repere le repère considéré
     * @return la liste des éléments rencontrés
     */
    public ListComposant zoneDAction(Point P, Repere repere) {
        ListComposant L = new ListComposant();
        for(ComposantGraphique cg : this) {
            if(cg.distance2Pixel(P,repere)<EspaceDessin.TOLERANCE) { L.add(cg);}
        }
        return L;
    }

    /**
     * Choisit l'élément le plus pertinent : càd le plus proche de P (en pixel), de plus haute priorité, dans la zone d'action, et respectant le filtre passé en paramètre.
     * @param P le point étudié
     * @param repere le repère considéré
     * @param filtre le filtre à utiliser. null revient à tout accepter
     * @param priorityOrder la priorité des élements selon leur classe
     * @return la liste des éléments rencontrés
     */
    public ComposantGraphique selection(Point P, Repere repere, Filtre filtre, Collection<Class<? extends ComposantGraphique>> priorityOrder) {
        return filtrer(filtre).zoneDAction(P, repere).selectClosest(P, repere, priorityOrder);
    }
    
    /** Renvoie le composant le plus proche de P ayant la plus haute priorité (la priorité domine sur la proximité) **/
    private ComposantGraphique selectClosest(Point P, Repere repere, Collection<Class<? extends ComposantGraphique>> priorityOrder) {
        ComposantGraphique selection;
        if(priorityOrder==null) {return selectClosest(P, repere, DEFAULT_PRIORITY_ORDER);}
        for(Class<? extends ComposantGraphique> c : priorityOrder) {
            selection = selectClosest(P, repere, c);
            if(selection!=null) return selection;
        }
        return null;
    }

    /**
     * Renvoie l'élément le plus proche du point étudié dans la zone de tolérance et appartenant à la classe
     * ayant la plus haute priorité
     * @param P le point étudié
     * @param repere le repère considéré
     * @param typesAdmis les types d'objets autorisés
     * @param priorityOrder l'ordre de priorité à utiliser
     * @return l'élément le plus pertinent
     */
//    public ComposantGraphique selection(Point P, Repere repere, List<Class<? extends ComposantGraphique>> typesAdmis, List<Class<? extends ComposantGraphique>> priorityOrder) {
//        ComposantGraphique selection = null;
//        ListComposant preSelection = zoneDAction(P, repere, new Filtre(typesAdmis));
//        for(Class c : priorityOrder) {
//            selection = preSelection.selectClosest(P, repere, c);
//            if(selection!=null) return selection;
//        }
//        return selection;
//    }

    /**
     * Renvoie l'élément le plus proche du point étudié (en pixel) dans la zone de tolérance et appartenant à la classe
     * ayant la plus haute priorité
     * @param P le point étudié
     * @param repere le repère considéré
     * @param types les types d'objets autorisés dans leur ordre de priorité
     * @return l'élément le plus pertinent
     */
    public ComposantGraphique selection(Point P, Repere repere, List<Class<? extends ComposantGraphique>> types) {
        return selection(P,repere,new Filtre(types),types);
    }

    /**
     * Renvoie l'élément le plus proche (en pixel) d'un point donné dans une liste d'éléments
     * @param P le point étudié
     * @param repere le repère considéré
     * @return l'élément le plus proche
     */
    public ComposantGraphique closest(Point P, Repere repere) {
        ComposantGraphique plusProche = null;
        int distance = Integer.MAX_VALUE;
        for(ComposantGraphique cg : this) {
            int d = cg.distance2Pixel(P,repere);
            if(d<distance) { plusProche = cg; distance = d; }
        }
        return plusProche;
    }

    /**
     * Renvoie l'élément d'une classe donnée le plus proche (en pixel) d'un point donné dans une liste d'éléments
     * @param P le point étudié
     * @param repere le repère considéré
     * @param type le types d'objet à considérer
     * @return l'élément le plus proche, null sinon
     */
    public <T extends ComposantGraphique> T selectClosest(Point P, Repere repere, Class<T> type) {
        T plusProche = null;
        int distance = Integer.MAX_VALUE;
        for(ComposantGraphique cg : this) {
            if(type.isInstance(cg)) {
                int d = cg.distance2Pixel(P,repere);
                if(d<distance) { plusProche = (T)cg; distance = d; }
            }
        }
        return plusProche;
    }

    /**
     * transforme une liste de composant graphiques en la liste de leurs noms
     * @return la liste des noms des composants
     */
//    public LinkedList<String> toName() {
//        LinkedList<String> L = new LinkedList<String>();
//        for(ComposantGraphique cg : this) {
//            L.add(cg.getNom());
//        }
//        return L;
//    }


    /**
     * teste si la liste contient l'élément spécifié
     * @param cgRef l'élément à chercher
     * @return vrai ssi l'élément est dans la liste
     */
    public boolean contient(ComposantGraphique cgRef) {
        for(ComposantGraphique cg : this) {
            if(cgRef.estEgalA(cg)) {return true; }
        }
        return false;
    }

//    public Class<? extends ComposantGraphique>[] toClass() {
//        LinkedList<Class> L = new LinkedList<Class>();
//        for(ComposantGraphique cg : this) {L.add(cg.getClass());}
//        return L.toArray(new Class[L.size()]);
//    }
    public List<Class<? extends ComposantGraphique>> toClassList() {
        List<Class<? extends ComposantGraphique>> L = new LinkedList<>();
        for(ComposantGraphique cg : this) {L.add(cg.getClass());}
        return L;
    }

//    public Map<String,? extends ComposantGraphique> toMap(String[] T) {
//        Map<String,ComposantGraphique> reponse = new HashMap<String,ComposantGraphique>();
//        for(int i = 0; i<T.length; i++) {
//            reponse.put(T[i], this.get(i));
//        }
//        return reponse;
//    }

    public void dessine(Repere repere, Graphics2D g2D) {
        for(ComposantGraphique cg : this) cg.dessine(repere, g2D);
    }

    public boolean addOnce(ComposantGraphique cg) {
        if(contient(cg)) {return false;}
        else {return add(cg);}
    }
    @Override
    public boolean add(ComposantGraphique cg) {
        if(cg==null) {
            System.out.println("ajout null : "+this.size()+" : "+this.getLast());
            return false;}
        boolean b = super.add(cg);
        if(b) {fireAdd(cg);}
//        if(b) {fireAdd(cg);}
        return b;
    }
    @Override
    public boolean addAll(Collection<? extends ComposantGraphique> L) {
        if(L==null) {return false;}
        while(L.contains(null)) {
            System.out.println("ajout null : "+this.size()+" : "+this.getLast());
            L.remove(null);}
        boolean b = super.addAll(L);
        if(b) {fireAddAll(L);}
        return b;
    }
    @Override
    public void clear() {
        List<? extends ComposantGraphique> L = new LinkedList<>(this);
        super.clear();
        fireClear(L);
    }
    
    public ListComposant deepCopy() {
        ListComposant copie = new ListComposant();
        for(ComposantGraphique cg : this) {
            copie.add(cg.clone());
        }
        return copie;
    }

    /**
     * Ajoute les éléments différent des éléments de la liste passée en paramètre. L'égalité
     * doit être géométrique, et non pas en adresse mémoire.
     * @param L la liste des éléments à ajouter.
     */
    public void addAllOnce(List<? extends ComposantGraphique> L) {
        ListComposant aAjouter = new ListComposant();
        for(ComposantGraphique cg : L) { if(!contient(cg)) aAjouter.add(cg); }
        if(!aAjouter.isEmpty()) {this.addAll(aAjouter);}
    }

    /**
     * Supprime les éléments égaux aux éléments de la liste passée en paramètre. L'égalité
     * doit être géométrique, et non pas en adresse mémoire.
     * @param L la liste des éléments à supprimer.
     */
    @Override
    public boolean removeAll(Collection<?> L) {
        LinkedList<ComposantGraphique> aSupprimer = new LinkedList<>();
        for(Object element : L) {
            if(element instanceof ComposantGraphique) {
                for(ComposantGraphique cg : this) { if(cg.estEgalA((ComposantGraphique)element)) aSupprimer.add(cg); }
            }
        }
        boolean b = super.removeAll(aSupprimer);
        if(b) {fireRemoveAll(aSupprimer);}
        return b;
    }

    /**
     * Supprime toutes les occurences d'un élément dans la liste. L'égalité
     * doit être géométrique, et non pas en adresse mémoire.
     */
    @Override
    public boolean remove(Object obj) {
        if(!(obj instanceof ComposantGraphique)) {return false;}
        ComposantGraphique element = (ComposantGraphique) obj;
        ListComposant aSupprimer = new ListComposant();
        for(ComposantGraphique cg : this) { if(cg.estEgalA(element)) aSupprimer.add(cg); }
        if(aSupprimer.size()>1) {
            return this.removeAll(aSupprimer);
        } else {
            boolean b = super.remove(element);
            if(b) {fireRemove(element);}
            return b;
        }
    }

    /**
     * Recupère le premier élément géométriquement égal à l'élément passé en paramètre
     * ou null s'il n'est pas contenu dans la liste.
     * @param element l'élément à chercher
     * @return l'élément contenu dans la liste ou null si non présent
     */
    public ComposantGraphique get(ComposantGraphique element) {
        for(ComposantGraphique cg : this) { if(cg.estEgalA(element)) return cg; }
        return null;
    }

    /**
     * Rencoie une liste contenant les éléments nonPassifs contenus dans cette ListComposant.
     * @return une nouvelle liste contenant les éléments non passifs
     */
    public ListComposant nonPassifs() {
        ListComposant L = new ListComposant();
        for(ComposantGraphique cg : this) {
            if(!cg.estPassif()) {L.add(cg);}
        }
        return L;
    }

    /**
     * Retourne la liste des points d'intersection entre cette liste et le composant passé en paramètre
     * @param composant composant à étudier
     * @return liste des points d'intersection
     */
    public List<Point> intersection(Intersectable composant) {
        List<Point> L = new LinkedList<>();
        for(ComposantGraphique cg : this) {
            if((cg instanceof Intersectable) && !cg.estEgalA((ComposantGraphique)composant)) {L.addAll(composant.pointsDIntersection((Intersectable) cg));}
        }
        return L;
    }

    public String getSVGRepresentation(Repere repere) {
        String s = "";
        for(ComposantGraphique cg : this) {
            s+=cg.getSVGRepresentation(repere)+"\n";
        }
        return s;
    }

    /**
     * classe permettant d'acouter les modifications apportées sur une ListComposant.
     * les évènements sont déclenchés APRES la modification de la liste.
     */
    public static interface ListComposantListener {
        public boolean add(ListComposant source, ComposantGraphique cg);
        public boolean addAll(ListComposant source, Collection<? extends ComposantGraphique> L);
        public boolean remove(ListComposant source, ComposantGraphique cg);
        public boolean removeAll(ListComposant source, Collection<? extends ComposantGraphique> L);
        /** le deuxième parametre correspond à la liste des éléments supprimés **/
        public boolean clear(ListComposant source, Collection<? extends ComposantGraphique> L);
    }
    /**
     * classe permettant d'acouter les modifications apportées sur une ListComposant.
     * les évènements sont déclenchés APRES la modification da la liste.
     */
    public static abstract class ListUtileAdapter implements ListComposantListener {
        public boolean add(ListComposant source, ComposantGraphique cg) {return false;}
        public boolean addAll(ListComposant source, Collection<? extends ComposantGraphique> L) {return false;}
        public boolean remove(ListComposant source, ComposantGraphique cg) {return false;}
        public boolean removeAll(ListComposant source, Collection<? extends ComposantGraphique> L) {return false;}
        public boolean clear(ListComposant source, Collection<? extends ComposantGraphique> L) {return false;}
    }
}
