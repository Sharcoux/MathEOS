/** «Copyright 2013 François Billioud»
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

package matheos.graphic;

import matheos.graphic.composants.ComposantGraphique;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * classe permettant de filtrer les éléments d'une ListComposant.
 * @author François Billioud
 */
public class Filtre<T extends ComposantGraphique>  {

    protected Set<Class<T>> classesAcceptees = null;
    private final ListComposant exclus = new ListComposant();
    private final List<VerificationSpeciale> verifications = new LinkedList<>();

    /**
     * Crée un nouveau filtre n'acceptant que les objets implémentant les classes passées en paramètre.
     * Actuellement, null n'est jamais accepté
     * Un filtre null accepte tout
     * Un filtre vide n'accepte rien
     * @param classesAcceptees la liste des classes.
     */
    public Filtre(Collection<Class<T>> classesAcceptees) {
        if(classesAcceptees!=null) {this.classesAcceptees = new LinkedHashSet<>(classesAcceptees);}
    }

    /**
     * Crée un nouveau filtre n'acceptant que les objets implémentant les classes passées en paramètre.
     * Actuellement, null n'est jamais accepté
     * Un filtre null accepte tout
     * Un filtre vide n'accepte rien
     * @param classesAcceptees la liste des classes.
     */
    public Filtre(Class<T>... classesAcceptees) {
        this(Arrays.<Class<T>>asList(classesAcceptees));
    }

    public void exclure(ComposantGraphique cg) {
        exclus.add(cg);
    }

    public void exclure(ListComposant L) {
        exclus.addAllOnce(L);
    }

    public void addVerificateur(VerificationSpeciale verificateur) {
        verifications.add(verificateur);
    }
    
    private boolean isInstanceOfAcceptedClass(ComposantGraphique cg) {
        boolean isInstance = false;
        for(Class<? extends ComposantGraphique> c : classesAcceptees) {if(c.isInstance(cg)) {isInstance= true;}}
        return isInstance;
    }

    /** Retourne true ssi le composant est accepté par le filtre. null n'est jamais accepté **/
    public boolean accepte(ComposantGraphique cg) {
        if(cg==null) {return false;}
        if(classesAcceptees==null) {return true;}
        
        if(!isInstanceOfAcceptedClass(cg)) {return false;}
        
        if(exclus.contient(cg)) {return false;}//renvoie faux si l'objet est exclu
        
        for(VerificationSpeciale verificateur : verifications) {
            if(!verificateur.accepte(cg)) { return false; }
        }
        return true;
    }

    /** Renvoie les classes acceptées par le filtre dans leur ordre de priorité **/
    public Set<Class<T>> getClassesAcceptees() { return classesAcceptees; }

    public static interface VerificationSpeciale {
        public boolean accepte(ComposantGraphique cg);
    }
    
    /** Filtre les objets passifs. Seuls les objets actifs son acceptés.
     * @return l'objet lui-même, pour le chaînage **/
    public Filtre nonPassif() {
        addVerificateur(new VerificationSpeciale() {
            @Override
            public boolean accepte(ComposantGraphique cg) {
                return !cg.estPassif();
            }
        });
        return this;//pour le chainage
    }

    /** retourne un filtre ne laissant rien passer **/
    public static Filtre filtreTotal() {return new Filtre();}
    /** retourne un filtre laissant tout passer **/
    public static Filtre filtreLaxiste() {Filtre f = new Filtre();f.classesAcceptees=null;return f;}

    /** Filtre composé : il agit comme des filtres en parallèles. Si l'un des filtres accepte l'élément, l'union l'accepte aussi **/
    public static class UnionFilter<K extends ComposantGraphique> extends Filtre<K> {
        List<Filtre<K>> filtres = new LinkedList<>();
        public UnionFilter(List<Filtre<K>> filtres) {
            super((Collection)null);
            this.classesAcceptees = new ClassTreeSet();
            this.filtres = filtres;
            for(Filtre f : filtres) {
                if(f.getClassesAcceptees()==null) {continue;}
                classesAcceptees.addAll(f.getClassesAcceptees());
            }
        }
        public UnionFilter(Filtre<K>... filtres) {this(Arrays.asList(filtres));}
        @Override
        public boolean accepte(ComposantGraphique cg) {
            if(!super.accepte(cg)) {return false;}
            for(Filtre f : filtres) {if(f.accepte(cg)) {return true;}}
            return false;
        }
    }
    
    protected class ClassComparator implements Comparator<Class<T>> {
        @Override
        public int compare(Class c1, Class c2) {
            return getValueOf(c1)-getValueOf(c2);
        }
        private int getValueOf(Class c) {
            int value = 0;
            for(Class ref : ListComposant.DEFAULT_PRIORITY_ORDER) {
                if(ref==ComposantGraphique.class) {return value+c.hashCode();}//HACK : toutes les classes sont assignableFrom ComposantGraphique
                if(ref.isAssignableFrom(c)) {return value;}
                value++;
            }
            return value+c.hashCode();//2 classes identiques sont égales, mais pas 2 classes différentes non définies dans Priority_Order
        }
    }
    
    /* Classe permettant l'union de filtres */
    protected class ClassTreeSet extends TreeSet<Class<T>> {
        protected ClassTreeSet() {
            super(new ClassComparator());
        }
        @Override
        public boolean add(Class<T> e) {
            List<Class<T>> toRemove = new LinkedList<>();
            for(Class<T> classe : this) {
                if(classe.isAssignableFrom(e)) {return false;}
                if(e.isAssignableFrom(classe)) {toRemove.add(classe);}
            }
            removeAll(toRemove);
            return super.add(e);
        }
        @Override
        public boolean remove(Object o) {
            if(!(o instanceof Class)) {return false;}
            List<Class<T>> toRemove = new LinkedList<>();
            Class<T> e = (Class<T>) o;
            for(Class<T> classe : this) {
                if(classe.isAssignableFrom(e)) {}
                if(e.isAssignableFrom(classe)) {toRemove.add(classe);}
            }
            for(Class<T> c : toRemove) {super.remove(c);}
            return !toRemove.isEmpty();
        }
    }
}