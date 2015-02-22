/**
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of Bomehc.
 *
 * Bomehc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bomehc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Bomehc. If not, see <http://www.gnu.org/licenses/>.
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
 **/

package bomehc.utils.objets.maps;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author François Billioud
 * @param <K> Paramètres des classes passées en clé (Class<K>)
 * @param <V> Classe des valeurs de la map. Les clés sont des classes
 */
public class ClassMap<K,V> extends HashMap<Class<K>, V> {
    public V get(Class c) {
        if(c.isPrimitive()) {return this.get(builtInMap.get(c.toString()));}
        V v = getClassOrInterface(c);
        if(v!=null) {return v;}
        
        Class classe = c;
        while((classe = classe.getSuperclass())!=null) {
            v = getClassOrInterface(classe);
            if(v!=null) {return v;}
        }
        return null;
    }
    
    @Override
    public V put(Class<K> key, V value) {
        if(key==null) {return super.put(null, value);}
        Class<K> k = key.isPrimitive() ? builtInMap.get(key.toString()) : key;
        return super.put(k, value);
    }
    
    private V getClassOrInterface(Class c) {
        V v = super.get(c);
        if(v!=null || c.isInterface()) {return v;}
        for(Class i : c.getInterfaces()) {
            v = get(i);
            if(v!=null) {return v;}
        }
        return null;
    }
    
    @Override
    public V get(Object o) {
        if(o instanceof Class) {return get((Class)o);}
        else { return get(o.getClass()); }
    }
    
    @Override
    public boolean containsKey(Object c) {
        return this.get(c)!=null;
//        if(!(c instanceof Class)) {return false;}
//        if(super.containsKey(c)) {return true;}
//        Class classe = (Class)c;
//        while((classe = classe.getSuperclass())!=null) {
//            if(super.containsKey(classe)) {return true;}
//        }
//        return false;
    }
    
    private static final Map<String,Class> builtInMap = new HashMap<String,Class>();
    static {
       builtInMap.put("int", Integer.class );
       builtInMap.put("long", Long.class );
       builtInMap.put("double", Double.class );
       builtInMap.put("float", Float.class );
       builtInMap.put("boolean", Boolean.class );
       builtInMap.put("char", Character.class );
       builtInMap.put("byte", Byte.class );
       builtInMap.put("void", Void.class );
       builtInMap.put("short", Short.class );
    }
}
