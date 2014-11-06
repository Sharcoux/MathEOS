/**
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of MathEOS
 *
 * MathEOS is free software: you can redistribute it and/or modify
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
 *
 **/

package matheos.json;

import matheos.graphic.composants.Texte;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataMap;
import matheos.utils.objets.maps.ClassMap;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author François Billioud
 */
public class JsonMathEOSReader {

    /** objets recréés associés à leur ID via cette map **/
    private final Map<Long, Object> resolvedID = new HashMap<>();
    /** map des objets en attente de résolution de référence. La clé est l'id de l'objet attendu, la valeur est la liste des objets attendant cette clé **/
    private final PendingReference pendingReferences = new PendingReference();
//    /** JsonObject de l'objet dont un fils est une référence **/
//    private final Map<Long, LinkedList<Map<String, Object>>> pendingRef = new HashMap<>();
//    /** Object dont un fils n'est pas encore attribué car référencé mais non encore résolu **/
//    private final Map<Long, LinkedList<Object>> pendingField = new HashMap<>();
//    /** nom du field dont l'objet est une référence. Etroitement lié à pendingRef et pendingField **/
//    private final Map<Long, LinkedList<String>> refKey = new HashMap<>();
    
    private static final ClassMap<Object, FieldsReader> fieldsReader = new ClassMap<>();
    public static final void addReader(Class c, FieldsReader w) {fieldsReader.put(c, w);}
    
    static {
        addReader(Data.class, new DataObjectReader());
        addReader(Color.class, new ColorReader());
        addReader(Texte.class, new GraphiqueTexteReader());
    }
    
    private static final ClassMap<Object, FieldsSetter> fieldsSetter = new ClassMap<>();
    public static final void addReader(Class c, FieldsSetter w) {fieldsSetter.put(c, w);}
    
    static {
//        addReader(Texte.class, new GraphiqueTexteReader());
    }
    
    public static interface FieldsReader {
        Map<String, Object> readFieldValues(JsonObject jObj, Class c, Map<String, Object> fieldValues, JsonMathEOSReader reader) throws IOException;
    }
    
    public static interface FieldsSetter {
        Object setFields(Object object, Map<String, Object> mapValues, JsonMathEOSReader reader);
    }
    
    private static class ColorReader implements FieldsReader {
        @Override
        public Map<String, Object> readFieldValues(JsonObject jObj, Class c, Map<String, Object> fieldValues, JsonMathEOSReader reader) throws IOException{
            reader.readFieldValues(jObj, Color.class.getSuperclass(), null, fieldValues);
            fieldValues.put("value", 0xff000000 | (int)Long.decode(jObj.get("value").toString()).intValue());
            reader.readFieldValues(jObj, c, Color.class, fieldValues);
            return fieldValues;
        }
    }
    
    private static class DataObjectReader implements FieldsReader {
        @Override
        public Map<String, Object> readFieldValues(JsonObject jObj, Class c, Map<String, Object> fieldValues, JsonMathEOSReader reader) throws IOException{
            //crée et rempli l'objet data et l'objet element
            DataMap<Data> data = new DataMap<>();
            DataMap<String> elements = new DataMap<>();
            
            Iterator<Map.Entry<String, Object>> iter = jObj.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, Object> entry = iter.next();
                String key = entry.getKey();
                if(key.equals(JsonMathEOSWriter.ELEMENTS)) {//créer l'objet elements
                    JsonObject jsonElements = (JsonObject) entry.getValue();
                    Set<Map.Entry> set = jsonElements.entrySet();
                    for(Map.Entry element : set) {
                        elements.put(element.getKey().toString(), element.getValue().toString());
                    }
                    fieldValues.put("elements", elements);
//                    jObj.put("elements", elements);
//                    jObj.remove(JsonMathEOSWriter.ELEMENTS);
                } else {
                    Data value = (Data)reader.readFieldValue(jObj, key, Data.class);
                    data.put(key, value);
//                    jObj.remove(key);
                }
            }
//            jObj.put("data", data);
            fieldValues.put("data", data);
            
            return fieldValues;
        }
    }
    private static class GraphiqueTexteReader implements FieldsReader {
        @Override
        public Map<String, Object> readFieldValues(JsonObject jObj, Class c, Map<String, Object> fieldValues, JsonMathEOSReader reader) throws IOException{
            fieldValues.put("content", jObj.get("content"));
            fieldValues.put("deplacement", reader.toJava((JsonObject) jObj.get("offset"),matheos.graphic.composants.Vecteur.class));
            reader.readFieldValues(jObj, c, Texte.class, fieldValues);
            reader.readFieldValues(jObj, Texte.class.getSuperclass(), null, fieldValues);
            return fieldValues;
        }
    }
//    private static class GraphiqueTexteSetter implements FieldsSetter {
//        @Override
//        public Object setFields(Object object, Map<String, Object> mapValues, JsonMathEOSReader reader) {
//            reader.setFields(object, Texte.class.getSuperclass(), null, mapValues);
//            Texte texte = (Texte) object;
//            texte.setPosition((double)mapValues.get("x"), (double)mapValues.get("y"));
//            texte.setText((String)mapValues.get("content"));
//            reader.setFields(object, object.getClass(), Texte.class, mapValues);
//            return texte;
//        }
//    }
    
    /** transforme le contenu du fichier json spécifié en objet java **/
    public static Object jsonToJava(InputStream stream, Class target) {
        JsonMathEOSReader jr = new JsonMathEOSReader();
        JsonObject obj = JsonReader.toMaps(stream);
        return jr.toJava(obj, null);
    }
    
    public static Object jsonToJava(InputStream stream) {
        return jsonToJava(stream, null);
    }
    
    /** transforme le json spécifié en objet java **/
    public static Object jsonToJava(String json, Class target) {
        JsonMathEOSReader jr = new JsonMathEOSReader();
        JsonObject obj = JsonReader.toMaps(json);
        return jr.toJava(obj, target);
    }
    
    /** transforme le json spécifié en objet java **/
    public static Object jsonToJava(String json) {
        return jsonToJava(json, null);
    }
    
    /** Transforme le jsonObject en objet Java, de class c si c est précisé **/
    private Object toJava(JsonObject jObj, Class c) {
        try {
            return this.jsonToJavaCore(jObj, c);
        } catch (IOException ex) {
            Logger.getLogger(JsonMathEOSReader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private Object instancierPrimitiveArray(Object[] values, Class objClass) {
        if(objClass!=null) {
            if(objClass.getComponentType().isPrimitive()) {//cas d'un tableau de primitives
                Object array = Array.newInstance(objClass.getComponentType(), values.length);
                for(int i=0;i<values.length;i++) {
                    try {
                        Array.set(array, i, JsonReader.newPrimitiveWrapper(objClass.getComponentType(), values[i]));
                    } catch (IOException ex) {
                        Logger.getLogger(JsonMathEOSReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return array;
            } else if(objClass.getComponentType().isArray()) {
                Object array = Array.newInstance(objClass.getComponentType(), values.length);
                for(int i=0;i<values.length;i++) {
                    if(values[i] instanceof JsonObject) {
                        try {
                            Array.set(array, i, jsonToJavaCore((JsonObject) values[i], objClass.getComponentType()));
                        } catch (IOException ex) {
                            Logger.getLogger(JsonMathEOSReader.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else {Array.set(array, i, instancierPrimitiveArray((Object[]) values[i], objClass.getComponentType()));}
                }
                return array;
            }
        }
        return values;
    }

    /** transforme le JsonObject en objet de classe objClass, ou jsonObj.type, si précisé, ou tente de déduire le type si null **/
    private Object jsonToJavaCore(JsonObject jsonObj, Class objClass) throws IOException {
        
        if(jsonObj.isPrimitive()) {//cas le plus simple : primitive
            return jsonObj.getPrimitiveValue();
        }
        
        if(jsonObj.isArray()) {//cas d'un array de primitives. On transforme les objets en primitives et on renvoie l'array
            if(objClass!=null) {
                if(objClass.getComponentType().isPrimitive()) {//cas d'un tableau de primitives
                    if(objClass.getComponentType().equals(byte.class)) {return jsonObj.toByteArray();}
                }
                if(objClass.getComponentType().isPrimitive() || objClass.getComponentType().isArray()) {return instancierPrimitiveArray(jsonObj.getArray(), objClass);}
            }
        }
        
        Object o = null;
        Class c = null;
        
        //Cas où on connait déjà la classe de l'objet à instancier
        if(objClass!=null || jsonObj.type != null) {
            //On choisit la class la plus adaptée pour créer l'objet
            c = instancierClass(jsonObj);
            if(c==null) {c = objClass;}//En cas d'échec, on tente d'utiliser la classe fournie en paramètre
            if(c!=null) {//l'instanciation a fonctionnée. On passe à l'instanciation de l'objet lui-même
                
                //cas d'un enum
                if(c.isEnum()) {
                    return Enum.valueOf(c, (String) jsonObj.get("name"));
                }
                
                //On essaie de construire les fils et on les range dans une map
                Map<String, Object> fieldValues = new HashMap<>();
                FieldsReader specialReader = fieldsReader.get(c);
                if(specialReader!=null) {//les objets de classes enregistrées via addReader sont chargées de remplir la map
                    specialReader.readFieldValues(jsonObj, c, fieldValues, this);
                } else {
                    //On passe en revue tous les champs pour leur attribuer leur valeur
                    readFieldValues(jsonObj, c, null, fieldValues);
                }

                //On essaie d'instancier l'objet
                try {
                    o = newInstance(c, fieldValues);
                } catch (IOException ex) {
                    Logger.getLogger(JsonMathEOSReader.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
                
                //On associe les valeurs aux champs de l'objet
                FieldsSetter specialPostReader = fieldsSetter.get(c);
                if(specialPostReader!=null) {//cas d'un objet special
                    o = specialPostReader.setFields(o, fieldValues, this);
                } else {
                    setFields(o, c, null, fieldValues);
                }
                
            }
        }
        
        //Cas des champs spéciaux
        if(jsonObj.isMap()) {//Si l'objet est une map
            Map m;
            if(o==null) {
                m = new HashMap();
            } else if(o instanceof Map) {
                m = (Map)o;
            } else {
                System.out.println("jsonObj.isMap()=true alors que l'objet n'est pas une map, pos = " + jsonObj.pos);
                return o;
            }
            Object map = jsonObj.get("@map");
            if(map==null) {return m;}
            Object[] mapItems = ((JsonObject)map).getArray();//vérifier s'il s'agit d'un Array ou d'un JsonObject
//            Object[] keys = (Object[]) jsonObj.get("@keys");
//            Object[] items = jsonObj.getArray();
//            
//            if (keys == null || items == null) {
//                if (keys != items) {
//                    throw new IOException("Map written where one of @keys or @items is empty, pos = " + jsonObj.pos);
//                }
//                return m;
//            }

//            int size = keys.length;
//            if (size != items.length) {
//                throw new IOException("Map written with @keys and @items entries of different sizes, pos = " + jsonObj.pos);
//            }
            
            instancierItemsMap(m, mapItems);
            o = m;
            
        } else if(jsonObj.isArray()) {//Si l'objet est un array
            Object[] items = jsonObj.getArray();
            Object[] array = instancierItemsArray(items);
            if(o==null) {
                //TODO s'assurer que les objets sont tous de meme type. Gérer le cas null, tableau vide, etc
                if(array.length==0) {
                    o = new Object[0];
                } else {
                    o = Array.newInstance(array[0].getClass(), items.length);//on parie que le premier objet est représentatif du tableau
                }
            }
            if(o.getClass().isArray()) {
                for(int i=0; i<items.length; i++) {Array.set(o, i, array[i]);}
            } else {
                System.out.println("jsonObj.isArray()=true alors que l'objet n'est pas un array, pos = " + jsonObj.pos);
                return o;
            }
            return o;
            
        } else if(jsonObj.isCollection()) {//Si l'objet est une collection, une liste
            Object[] items = jsonObj.getArray();
            Collection col;
            if(o==null) {
                col = new LinkedList();
            } else if(o instanceof Collection) {
                col = (Collection) o;
            } else {
                System.out.println("jsonObj.isCollection()=true alors que l'objet n'est pas une collection, pos = " + jsonObj.pos);
                return o;
            }
            instancierItemsCollection(col, items);
            o = col;
        } else {//cas désespéré : l'objet n'est toujours pas identifié. Il peut s'agir d'un type primitif stocké dans value
            if(o==null) {o = jsonObj.get("value");}
        }
        
        //Résout les références en attente, ou enregistre l'objet s'il est utilisé par un autre objet
        if(jsonObj.hasId()) {
            fixReferences(jsonObj.id, o);
        }
        
        return o;
    }
    
    /**
     * prépare les valeurs des champs d'une classe depuis la classe départ jusqu'à la classe limite (exclue).
     * @param jObj : JsonObject contenant les représentations JsonObject des champs de l'objet en cours d'étude
     * @param depart : première classe dont les champs sont à considérer
     * @param limite : première classe dont les champs seront ignorés, ainsi que ceux de ses super-classes. null pour ignorer
     * @param fieldValues : map des valeurs des champs, remplie par cette fonction
     */
    private Map<String, Object> readFieldValues(JsonObject jObj, Class depart, Class limite, Map<String, Object> fieldValues) throws IOException {
        Map<String, Field> fields = JsonMathEOSWriter.getDeepDeclaredFields(depart, limite);
        for(Map.Entry<String, Field> entry : fields.entrySet()) {
            final Field f = entry.getValue();
            String fieldName = f.getName();
            Object o = readFieldValue(jObj, fieldName, f.getType());
            fieldValues.put(f.getName(), o);
        }
        return fieldValues;
    }
    
    /**
     * Lit la valeur d'un champs depuis le JsonObject, le transforme en objet Java et l'ajoute à la map des valeurs
     * @param jsonParent le JsonObject de l'objet
     * @param field le Field dont on cherche à lire la valeur
     * @return l'objet ainsi créé
     * @throws IOException 
     */
    private Object readFieldValue(JsonObject jsonParent, String fieldName, Class fieldType) throws IOException {
        Object value = jsonParent.get(fieldName);
        
        if(value==null) {return null;}//la valeur de ce champ n'a pas été enregistrée. On le laisse donc à null.
        else if(value instanceof JsonObject) {
            JsonObject jsonFieldValue = (JsonObject)value;
            if(jsonFieldValue.containsKey("@ref")) {//Si la valeur est une référence vers un autre objet...
                long ref = (Long) jsonFieldValue.get("@ref");//regarder si l'objet est un Long, un string ou un JsonObject
                Object o = resolvedID.get(ref);
                if(o!=null) {//on remplace la référence par l'objet s'il est déjà instancié
                    return o;
                } else {//on rajoute l'objet aux références en attentes
                    UnresolvedValueEntry e = new UnresolvedValueEntry(jsonParent, fieldName, ref);
                    pendingReferences.add(e, ref);
                    return e;
                }
            } else {
                return jsonToJavaCore(jsonFieldValue, fieldType);
            }
        }
        //Conversion de types
        if((fieldType.isPrimitive() || JsonObject.isPrimitiveWrapper(fieldType)) && !value.getClass().getName().equals(fieldType.getName())) {
            JsonObject j = new JsonObject();
            j.setType(fieldType.getName());
            j.put("value", value);
            return jsonToJavaCore(j, fieldType);
        } else if(fieldType.isArray() && !fieldType.getComponentType().equals(value.getClass().getComponentType())) {
            JsonObject j = new JsonObject();
            j.setType(fieldType.getName());
            j.put("@items", value);
            return jsonToJavaCore(j, fieldType);
        } 
        return value;
    }
    
    /**
     * spécifie les champs d'une classe depuis la classe départ jusqu'à la classe limite (exclue).
     * @param o : l'objet dont les champs sont à spécifier
     * @param depart : première classe dont les champs sont à considérer
     * @param limite : première classe dont les champs seront ignorés, ainsi que ceux de ses super-classes. null pour ignorer
     * @param fields : map des valeurs des champs
     */
    private void setFields(Object o, Class depart, Class limite, Map<String, Object> fields) {
        for(Field f : JsonMathEOSWriter.getDeepDeclaredFields(depart, limite).values()) {
            String fieldName = f.getName();
            if(!fields.containsKey(fieldName)) {continue;}//Aucune info sur la valeur de ce champ
            Object value = fields.get(fieldName);
            setField(o, value, f);
        }
    }
    
    /**
     * Affecte la valeur value au champ f de l'objet o.
     * @param o l'objet étudié
     * @param value la valeur à attrbuer
     * @param field le champ considéré
     */
    private void setField(Object o, Object value, Field field) {
        //Si une référence est encore en suspend, ce n'est plus la map mais l'objet qui attend la référence
        if(value instanceof UnresolvedValueEntry) {
            UnresolvedValueEntry unresolvedEntry = (UnresolvedValueEntry) value;
            long id = unresolvedEntry.id;
            UnresolvedField unresolvedField = new UnresolvedField(o, unresolvedEntry.getFieldName());
            pendingReferences.replace(unresolvedEntry, unresolvedField, id);
//            List pendingReferencesForThisId = pendingReferences.get(id);
//            pendingReferencesForThisId.remove(unresolvedEntry);
//            pendingReferencesForThisId.add(unresolvedField);
        } else {
            try {
                field.set(o, value);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(JsonMathEOSReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /** Résout les problèmes de références concernant un objet nouvellement instancié **/
    private void fixReferences(Long id, Object newObject) {
        resolvedID.put(id, newObject);//on enregistre l'objet pour résoudre une future référence
        pendingReferences.fix(id, newObject);
    }
    
    /** instancie la classe de l'objet à instancier **/
    private Class instancierClass(JsonObject jsonObj) {
        String type = jsonObj.type;
        Class c = null;
        //On tente d'obtenir la classe de l'objet par son champ @type
        if(type!=null) {
            type = type.replaceAll("bomehc", "matheos");
            try {
                c = JsonReader.classForName2(type);
            } catch (IOException ex) {
                Logger.getLogger(JsonMathEOSReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return c;
    }
    
    private Object checkReference(UnresolvedReference unresolvedObject, Object value) {
        if(value instanceof JsonObject) {
            JsonObject json = (JsonObject) value;
            if(json.containsKey("@ref")) {
                long ref = (long) json.get("@ref");
                Object o = resolvedID.get(ref);
                if(o==null) {
                    pendingReferences.add(unresolvedObject, ref);
                    //return null;
                    return unresolvedObject;
                } else {
                    return o;
                }
            } else {
                try {
                    return jsonToJavaCore(json, null);
                } catch (IOException ex) {
                    Logger.getLogger(JsonMathEOSReader.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }
        } else {
            return value;
        }
     }
    
    /** instancie les clés et les valeurs d'une map et les associe à la map passée en paramètre **/
    private void instancierItemsMap(Map map, Object[] entries) {
        int size = entries.length;
        //on déserialise les couples clé/valeur
        for(int i=0; i<size; i++) {
            Object entry = entries[i];
            if(entry instanceof JsonObject) {
                JsonObject jsonEntry = (JsonObject) entry;
                Object key = jsonEntry.get("@key"), value = jsonEntry.get("@value");
                key = checkReference(new UnresolvedMapKey(map), key);
                value = checkReference(new UnresolvedMapEntry(map, key), value);
                map.put(key, value);
            }
        }
    }
    
    /** instancie les valeurs d'un array et les associe au tableau passé en paramètre **/
    private Object[] instancierItemsArray(Object[] items) {
        Object[] array = new Object[items.length];
        for(int i=0; i<items.length; i++) {
            Object o = checkReference(new UnresolvedArrayItem(array, i),items[i]);
            if(o instanceof UnresolvedReference) {o=null;}
            Array.set(array, i, o);
        }
        return array;
    }
    
    /** instancie les valeurs d'une list et les associe à la liste passée en paramètre **/
    private void instancierItemsCollection(Collection col, Object[] items) {
        if(items==null) {return;}
        for(int i=0; i<items.length; i++) {
            UnresolvedReference caseUnresolved = col instanceof List ? new UnresolvedListItem((List) col, i) : new UnresolvedCollectionItem(col);
            Object o = checkReference(caseUnresolved,items[i]);
            if(!(o instanceof UnresolvedReference)) {col.add(o);}
        }
    }
    
    private interface UnresolvedReference {
        public void resolve(Object referencedObject);
    }
    
    private class UnresolvedValueEntry extends UnresolvedMapEntry {
        final long id;
        private UnresolvedValueEntry(Map pendingMapValues, String key, long id) {
            super(pendingMapValues, key);
            this.id = id;
        }
        public String getFieldName() {return (String)key;}
    }
    private class UnresolvedMapKey implements UnresolvedReference {
        final Map pendingMap;
        Object resolvedKey;
        private UnresolvedMapKey(Map pendingMap) {
            this.pendingMap = pendingMap;
        }
        @Override
        public void resolve(Object referencedObject) {
            resolvedKey = referencedObject;
            Object value = pendingMap.get(this);
            pendingMap.put(referencedObject, value);
            pendingMap.remove(this);
        }
        
    }
    private class UnresolvedMapEntry implements UnresolvedReference {
        final Map pendingMap;
        final Object key;

        private UnresolvedMapEntry(Map pendingMap, Object key) {
            this.pendingMap = pendingMap;
            this.key = key;
        }
        
        @Override
        public void resolve(Object referencedObject) {
            Object actualKey;
            if(key instanceof UnresolvedMapKey) {
                actualKey = ((UnresolvedMapKey)referencedObject).resolvedKey;
            } else {
                actualKey = key;
            }
            pendingMap.put(actualKey, referencedObject);
        }
    }
    private class UnresolvedListItem implements UnresolvedReference {
        final List pendingList;
        final int index;

        private UnresolvedListItem(List pendingList, int index) {
            this.pendingList = pendingList;
            this.index = index;
        }
        
        @Override
        public void resolve(Object referencedObject) {
            pendingList.set(index, referencedObject);
        }
    }
    private class UnresolvedArrayItem implements UnresolvedReference {
        final Object pendingArray;
        final int index;

        private UnresolvedArrayItem(Object pendingArray, int index) {
            this.pendingArray = pendingArray;
            this.index = index;
        }
        
        @Override
        public void resolve(Object referencedObject) {
            Array.set(pendingArray, index, referencedObject);
        }
    }
    private class UnresolvedCollectionItem implements UnresolvedReference {
        final Collection pendingCollection;

        private UnresolvedCollectionItem(Collection pendingCollection) {
            this.pendingCollection = pendingCollection;
        }
        
        @Override
        public void resolve(Object referencedObject) {
            pendingCollection.add(referencedObject);
        }
    }
    private class UnresolvedField implements UnresolvedReference {
        final Object pendingObject;
        final String fieldName;

        private UnresolvedField(Object pendingObject, String fieldName) {
            this.pendingObject = pendingObject;
            this.fieldName = fieldName;
        }
        
        @Override
        public void resolve(Object referencedObject) {
            try {
                Field f = pendingObject.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(pendingObject, referencedObject);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(JsonMathEOSReader.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("field not set : "+fieldName+" , object : "+pendingObject);
            }
        }
    }

    
    /*********************************  Instanciation des objets    ********************************
    ***********************************************************************************************/
    private static final Class[] _emptyClassArray = new Class[]{};
    private static final Comparator constructorComparator = new Comparator<Constructor>() {
        @Override
        public int compare(Constructor c1, Constructor c2) {
            return c2.getParameterTypes().length-c1.getParameterTypes().length;
        }
    };
    
    static Object newInstance(Class c, Map<String, Object> fieldValues) throws IOException {
        //On essaye le constructeur par défaut
        try {
            Constructor constructor = c.getConstructor(_emptyClassArray);
            if (constructor != null) {
                return constructor.newInstance();
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // OK, this class does not have a public no-arg constructor.  Instantiate with
            // first constructor found, filling in constructor values with null or
            // defaults for primitives.
        }
        
        //On cherche un autre constructeur
        Constructor[] constructors = c.getDeclaredConstructors();
        if (constructors.length == 0)
        {
            throw new IOException("Cannot instantiate '" + c.getName() + "' - Primitive, interface, array[] or void");
        }
        
        //On essaye de trouver un constructeur qui match exactement les objets qu'on a dans la map des attributs
        Arrays.sort(constructors, constructorComparator);//On classe les constructeurs du plus complet au moins complet
        for (Constructor constructor : constructors)
        {
            constructor.setAccessible(true);
            Class[] argTypes = constructor.getParameterTypes();
            Object[] values = fillArgs(argTypes, fieldValues, true);
            if(values!=null) {
                try {
                    return constructor.newInstance(values);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {}
            }
        }
        
        //On réessaye les constructeurs du plus simple au plus complexe et on remplit les arguments avec les valeurs qu'on peut
        List<Constructor> L = new ArrayList<>(Arrays.asList(constructors));
        Collections.reverse(L);
        L.toArray(constructors);
        for (Constructor constructor : constructors) {
            Class[] argTypes = constructor.getParameterTypes();
            Object[] values = fillArgs(argTypes, fieldValues, false);
            try {
                return constructor.newInstance(values);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {}
        }

        throw new IOException("Could not instantiate " + c.getName() + " using any constructor");
    }

    private static Object[] fillArgs(Class[] argTypes, Map<String, Object> jObj, boolean matchExactly) {
        //on range les arguments qui pourraient correspondre
        ClassMap<Object,Queue> initialValues = new ClassMap<>();
        for(Class c : argTypes) {
            initialValues.put(c, new LinkedList());
        }
        for(Object o : jObj.values()) {
            if(o!=null ) {
                Queue q = initialValues.get(o.getClass());
                if(q!=null) {q.add(o);}
            }
        }
        
        Object[] values = new Object[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            values[i] = initialValues.get(argTypes[i]).poll();
            if(values[i]!=null) {continue;}//on a réussi à initialiser la valeur. On continue
            else if(matchExactly) {return null;}//l'initialisation a échouée. Les valeurs ne match pas exactement le constructeur
            else {//On est autorisé à compléter le remplissage avec les valeurs que l'on possède
                if (argTypes[i].isPrimitive()) {
                    if (argTypes[i].equals(byte.class)) {
                        values[i] = (byte) 0;
                    } else if (argTypes[i].equals(short.class)) {
                        values[i] = (short) 0;
                    } else if (argTypes[i].equals(int.class)) {
                        values[i] = 0;
                    } else if (argTypes[i].equals(long.class)) {
                        values[i] = 0L;
                    } else if (argTypes[i].equals(boolean.class)) {
                        values[i] = Boolean.FALSE;
                    } else if (argTypes[i].equals(float.class)) {
                        values[i] = 0.0f;
                    } else if (argTypes[i].equals(double.class)) {
                        values[i] = 0.0;
                    } else if (argTypes[i].equals(char.class)) {
                        values[i] = (char) 0;
                    }
                } else {
                    values[i] = null;
                }
            }
        }

        return values;
    }
    
    private static class PendingReference {
        Map<Long, LinkedList<UnresolvedReference>> map = new HashMap<>();
        void add(UnresolvedReference unresolvedObject, long id) {
            LinkedList<UnresolvedReference> L = map.get(id);
            if(L==null) {L = new LinkedList<>();map.put(id, L);}
            L.add(unresolvedObject);
        }
        void replace(UnresolvedReference oldObject, UnresolvedReference newObject, long id) {
            LinkedList<UnresolvedReference> L = map.get(id);
            if(L==null) {map.put(id, L = new LinkedList<>());}
            else {L.remove(oldObject);}
            L.add(newObject);
        }
        void fix(long id, Object referencedObject) {
            LinkedList<UnresolvedReference> L = map.get(id);
            if(L==null) {return;}
            while(!L.isEmpty()) {
                UnresolvedReference pending = L.poll();
                pending.resolve(referencedObject);
            }
        }
    }
}
