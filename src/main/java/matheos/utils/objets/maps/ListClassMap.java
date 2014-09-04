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

package matheos.utils.objets.maps;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author François Billioud
 * @param <K> Informations supplémentaires sur les classes
 * @param <V> Le type d'objets stockés dans cette map
 */
public class ListClassMap<K,V> implements Map<List<Class<K>>, V> {
    
    private V head;
    private final Map<Class<K>, ListClassMap<K,V>> map = new ClassMap<>();
    
    /** Renvoie les classes enregistrées à ce niveau de la map. Càd les classes
     * pour lesquelles this.getMap({classe}) ne renvoie pas null.
     * @return 
     */
    public Set<Class<K>> getAcceptedClasses() {return map.keySet();}

    /**
     * Renvoie la ListClassMap enregistré pour la classe précisée
     * @param c la classe à chercher
     * @return la ListClassMap des objets V enregistrés pour la class c ou une map vide sinon
     */
    public ListClassMap<K,V> getMap(Class c) {
        return map.get(c)==null ? new ListClassMap<K, V>() : map.get(c);
    }
    
    public ListClassMap<K,V> getMap(List<Class<K>> L) {
        if(L==null) {return null;}
        if(L.isEmpty()) {return this;}
        Class c;
        c = L.get(0);
        List<Class<K>> queue = new LinkedList<>(L.subList(1, L.size()));
        return getMap(c).getMap(queue);
    }
    public V get(List<Class<K>> L) {
        if(L==null) {return null;}
        return getMap(L).head;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty() && head==null;
    }

    @Override
    public boolean containsKey(Object key) {
        if(key instanceof Class) {return map.containsKey(key);}
        if(key instanceof List) {return !getMap((List)key).isEmpty();}
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if(head.equals(value)) {return true;}
        for(ListClassMap<K,V> v : map.values()) {
            if(v.containsValue(value)) {return true;}
        }
        return false;
    }
    
    public V get() {return head;}
    
    public V get(Class c) {
        return getMap(c).head;
    }

    @Override
    public V get(Object key) {
        if(key instanceof Class) {return get((Class)key);}
        try {
            List<Class<K>> list = (List<Class<K>>)key;
            return get(list);
        } catch(ClassCastException ex) {
            return null;
        }
    }

    @Override
    public V put(List<Class<K>> key, V value) {
        V returnValue = null;
        if(key.isEmpty()) {
            if(head!=null) {returnValue = head;}
            head = value;
            return returnValue;
        } else {
            Class c = key.get(0);
            List<Class<K>> queue = new LinkedList<>(key.subList(1, key.size()));
            ListClassMap<K,V> m = getMap(c);
            returnValue = m.head;
            m.put(queue, value);
            map.put(c, m);
            return returnValue;
        }
    }

    @Override
    public V remove(Object key) {
        V returnValue;
        try {
            List<Class<K>> L = (List<Class<K>>) key;
            if(L.isEmpty()) {returnValue = head;head = null; return returnValue;}
            else {
                Class c = L.get(0);
                List<Class<K>> queue = L.subList(1, L.size()-1);
                ListClassMap<K,V> m = getMap(c);
                if(m.isEmpty()) {return null;}
                returnValue = m.remove(queue);
                if(returnValue!=null) {
                    if(m.head==null && m.map.isEmpty()) {map.remove(c);}//Si un noeud est vide, on le supprime
                }
                return returnValue;
            }
        } catch(ClassCastException ex) {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends List<Class<K>>, ? extends V> m) {
        for(Entry<? extends List<Class<K>>, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        head = null;
        map.clear();
    }

    @Override
    public Set<List<Class<K>>> keySet() {
        Set<List<Class<K>>> set = new HashSet<>();
        if(head!=null) {set.add(new LinkedList<Class<K>>());}//WARNING modifier le type de liste ici doit entrainer la modification du warning ci-dessous
        for(Entry<Class<K>, ListClassMap<K,V>> entry : map.entrySet()) {
            Set<List<Class<K>>> setFils = entry.getValue().keySet();
            for(List<Class<K>> L : setFils) {
                LinkedList<Class<K>> file = (LinkedList<Class<K>>)L;//WARNING l'implémentation doit travailler avec des LinkedList
                file.addFirst(entry.getKey());
                set.add(file);
            }
        }
        return set;
    }

    @Override
    public Collection<V> values() {
        Collection<V> values = new LinkedList<>();
        if(head!=null) {values.add(head);}
        for(ListClassMap<K,V> L : map.values()) {
            values.addAll(L.values());
        }
        return values;
    }

    @Override
    public Set<Entry<List<Class<K>>, V>> entrySet() {
        Set<Entry<List<Class<K>>, V>> set = new HashSet<>();
        if(head!=null) {
            set.add(new AbstractMap.SimpleEntry<>((List<Class<K>>)new LinkedList<Class<K>>(), head));//WARNING modifier le type de liste ici doit entrainer la modification du warning ci-dessous
        }
        for(Entry<Class<K>, ListClassMap<K,V>> entry : map.entrySet()) {
            Set<Entry<List<Class<K>>, V>> setFils = entry.getValue().entrySet();
            for(Entry<List<Class<K>>, V> entryFils : setFils) {
                LinkedList<Class<K>> file = (LinkedList<Class<K>>)entryFils.getKey();//WARNING l'implémentation doit travailler avec des LinkedList
                file.addFirst(entry.getKey());
                set.add(new AbstractMap.SimpleEntry<>((List<Class<K>>)file, entryFils.getValue()));
            }
        }
        return set;
    }
}
    
