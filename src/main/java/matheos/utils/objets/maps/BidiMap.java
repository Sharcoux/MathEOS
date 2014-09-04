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

package matheos.utils.objets.maps;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author François Billioud
 */
public class BidiMap<K, V> implements Map<K, V> {

    private LinkedList<K> keys = new LinkedList<K>();
    private LinkedList<V> values = new LinkedList<V>();
    public BidiMap(K[] keys, V[] values) {
        this.keys.addAll(Arrays.asList(keys));
        this.values.addAll(Arrays.asList(values));
    }
    public BidiMap(Map<K, V> map) {
        putAllKeys(map);
    }
    public BidiMap() {}

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    @Override
    public V get(Object key) {
        return getValue(key);
    }

    /**
     * Renvoie la valeur associée à la clé
     * @param key la clé à chercher
     * @return la valeur associée, ou null si la map ne contient pas la clé
     */
    public V getValue(Object key) {
        int index = keys.indexOf(key);
        return index==-1 ? null : values.get(keys.indexOf(key));
    }

    /**
     * Renvoie la valeur associée à la clé
     * @param value la clé à chercher
     * @return la valeur associée, ou null si la map ne contient pas la clé
     */
    public K getKey(Object value) {
        int index = values.indexOf(value);
        return index==-1 ? null : keys.get(values.indexOf(value));
    }

    @Override
    public V put(K key, V value) {
        return putKey(key, value);
    }

    public V putKey(K key, V value) {
        int i = keys.indexOf(key);
        if(i!=-1) {
            V oldValue = values.get(i);
            values.set(i, value);
            return oldValue;
        } else {
            keys.add(key);
            values.add(value);
            return null;
        }
    }

    public K putValue(V value, K key) {
        int i = values.indexOf(value);
        if(i!=-1) {
            K oldKey = keys.get(i);
            keys.set(i, key);
            return oldKey;
        } else {
            keys.add(key);
            values.add(value);
            return null;
        }
    }

    @Override
    public V remove(Object key) {
        return removeKey(key);
    }

    /**
     * supprime l'entrée associée à la clé passée en paramètre
     * @param key la clé de l'entrée à supprimer
     * @return la valeur associée à la clé
     */
    public V removeKey(Object key) {
        int i = keys.indexOf(key);
        if(i==-1) {return null;}
        keys.remove(i);
        return values.remove(i);
    }

    /**
     * supprime l'entrée associée à la valeur passée en paramètre
     * @param value la valeur de l'entrée à supprimer
     * @return la clé associée à la valeur
     */
    public K removeValue(Object value) {
        int i = values.indexOf(value);
        if(i==-1) {return null;}
        values.remove(i);
        return keys.remove(i);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        putAllKeys(m);
    }

    public void putAllKeys(Map<? extends K, ? extends V> m) {
        for(Entry<? extends K, ? extends V> entry : m.entrySet()) {
            putKey(entry.getKey(), entry.getValue());
        }
    }

    public void putAllValues(Map<? extends V, ? extends K> m) {
        for(Entry<? extends V, ? extends K> entry : m.entrySet()) {
            putValue(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        keys.clear();
        values.clear();
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<K>(keys);
    }

    @Override
    public Set<V> values() {
        return new HashSet<V>(values);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> set = new HashSet<Entry<K, V>>();
        ListIterator<K> iterKey = keys.listIterator();
        ListIterator<V> iterValue = values.listIterator();
        while(iterKey.hasNext()) {
            set.add(new EntryImpl(iterKey.next(), iterValue.next()));
        }
        return set;
    }
    
    private static class EntryImpl<K, V> implements Entry<K, V> {
        private K key;
        private V value;
        public EntryImpl(K key, V value) {
            this.key = key;
            this.value = value;
        }
        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        
    }
}
