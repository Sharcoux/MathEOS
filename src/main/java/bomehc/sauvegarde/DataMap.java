/**
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of Bomehc
 *
 * Bomehc is free software: you can redistribute it and/or modify
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
 *
 **/

package bomehc.sauvegarde;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author François Billioud
 */
public class DataMap<V> extends HashMap<String, V> {

//    public boolean containsKey(String key) {
//        for(Map.Entry<String, V> e : this) {if(e.getKey().equals(key)) {return true;}}
//        return false;
//    }
//
//    public boolean containsValue(V value) {
//        for(Map.Entry<String, V> e : this) {if(e.getValue().equals(value)) {return true;}}
//        return false;
//    }
//
//    public V get(String key) {
//        for(Map.Entry<String, V> e : this) {if(e.getKey().equals(key)) {return e.getValue();}}
//        return null;
//    }
//
//    public V put(String key, V value) {
//        return addEntry(new DataEntry(key,value));
//    }
    
//    private V addEntry(DataEntry entry) {
//        String key = entry.getKey();
//        Map.Entry<String, V> existingEntry = null;
//        for(Map.Entry<String, V> e : this) {if(e.getKey().equals(key)) {existingEntry = e;}}
//        if(existingEntry==null) {super.add(entry);return null;}
//        V old = existingEntry.getValue();
//        existingEntry.setValue(entry.getValue());
//        return old;
//    }
//    
//    @Override
//    public boolean add(Map.Entry<String, V> entry) {
//        V result;
//        if(DataEntry.class.isInstance(entry)) {
//            result = addEntry((DataEntry) entry);
//        } else {
//            result = addEntry(new DataEntry(entry.getKey(), entry.getValue()));
//        }
//        return result==null;
//    }
//
//    @Override
//    public boolean addAll(Collection<? extends Map.Entry<String, V>> L) {
//        boolean result = true;
//        for(Map.Entry<String, V> entry : L) {
//            result &= add(entry);
//        }
//        return result;super.s
//    }
//    public Map.Entry<String, V> get(int i) {
//        Iterator<Map.Entry<String,V>> iter = this.entrySet().iterator();
//        Map.Entry<String, V> result = null;
//        while(iter.hasNext() && i>0) {
//            iter.next();
//            i--;
//        }
//        return iter.next();
//    }
    
    public V removeKey(String key) {
//        int i = 0;
//        for(Map.Entry<String, V> e : this) {
//            if(e.getKey().equals(key)) {super.remove(i);return e.getValue();}
//            i++;
//        }
//        return null;
        return super.remove(key);
    }

    public String removeValue(V value) {
//        int i = 0;
//        for(Map.Entry<String, V> e : this) {
//            if(e.getValue().equals(value)) {super.remove(i);return e.getKey();}
//            i++;
//        }
//        return null;
        LinkedList<Map.Entry<String, V>> L = new LinkedList<>(this.entrySet());
        while(!L.isEmpty()) {
            Map.Entry<String, V> entry = L.poll();
            if(entry.getValue().equals(value)) {
                removeKey(entry.getKey());
                return entry.getKey();
            }
        }
        return null;
    }

//    public void putAll(Map<? extends String, ? extends V> m) {
//        for(Map.Entry<? extends String,? extends V> e : m.entrySet()) {
//            put(e.getKey(), e.getValue());
//        }
//    }

//    public String[] keyArray() {
//        String[] T = new String[size()];
//        return this.keySet().toArray(T);
////        int i = 0;
////        for(Map.Entry<String, V> e : this.entrySet()) {T[i]=e.getKey();i++;}
////        return T;
//    }

//    public Set<String> keySet() {
//        Set<String> s = new HashSet<>();
//        for(Map.Entry<String, V> e : this.entrySet()) {s.add(e.getKey());}
//        return s;
//    }
//
//    public Collection<V> values() {
//        List<V> L = new LinkedList<>();
//        for(Map.Entry<String, V> e : this.entrySet()) {L.add(e.getValue());}
//        return L;
//    }

//    protected final class DataEntry implements EditableEntry<V> {
//        private String key;
//        private V value;
//
//        protected DataEntry(String key, V value) {
//            this.key = key;
//            this.value = value;
//        }
//
//        @Override
//        public String getKey() {
//            return key;
//        }
//
//        @Override
//        public V getValue() {
//            return value;
//        }
//
//        @Override
//        public V setValue(V value) {
//            V old = this.value;
//            this.value = value;
//            return old;
//        }
//        
//        @Override
//        public String setKey(String key, boolean removeDuplicate) {
//            if(this.key.equals(key)) {return key;}
//            String old = this.key;
//            if(removeDuplicate) {DataMap.this.removeKey(key);}
//            this.key = key;
//            return old;
//        }
//        
//    }
//    
//    public static interface EditableEntry<V> extends Map.Entry<String, V> {
//        public String setKey(String key, boolean removeDuplicate);
//    }
}


