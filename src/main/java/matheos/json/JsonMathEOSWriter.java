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

import matheos.arevoir.inutilise.JsonMathEOS;
import matheos.graphic.composants.Texte;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataObject;
import matheos.utils.managers.ColorManager;
import matheos.utils.objets.maps.ClassMap;
import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author François Billioud
 */
public class JsonMathEOSWriter {
    static final String ELEMENTS = "stringValues";
    
    private final SortedSet<Integer> references = Collections.synchronizedSortedSet(new TreeSet<Integer>());//Lors du parcours, permet de repérer lorsqu'un objet fait référence à un autre
    private final SortedSet<Integer> visites = Collections.synchronizedSortedSet(new TreeSet<Integer>());//Lors du parcours permet d'identifier l'objet principal par rapport à l'objet qui s'y réfère
    private static final List<Class> notReferences = new LinkedList<>();//Liste des classes pour lesquels les objets, même égaux ne sont pas des références l'un de l'autre
    private static final ClassMap<Object, JsonWriter> writers = new ClassMap<>();
    public static final void addWriter(Class c, JsonWriter w) {writers.put(c, w);}
    
    static {
//        addWriter(Point.class, new PointWriter());
//        addWriter(Segment.class, new SegmentWriter());
//        addWriter(Droite.class, new DroiteWriter());
//        addWriter(DemiDroite.class, new DemiDroiteWriter());
//        addWriter(Arc.class, new ArcWriter());
        addWriter(Texte.class, new GraphiqueTexteWriter());
        addWriter(DataObject.class, new DataObjectWriter());
        addWriter(Color.class, new ColorWriter());
//        addWriter(Enregistrable.class, new EnregistrableWriter());
        
//        notReferences.add(Data.class);
        notReferences.add(Color.class);
        notReferences.add(Integer.class);
        notReferences.add(Boolean.class);
        notReferences.add(Byte.class);
        notReferences.add(Double.class);
        notReferences.add(Float.class);
        notReferences.add(Short.class);
        notReferences.add(Long.class);
        notReferences.add(Character.class);
        notReferences.add(String.class);
    }
    

    public static interface JsonWriter {
         void write(Object o, StringBuilder b, JsonMathEOSWriter writer);
    }
    
//    private static class EnregistrableWriter implements JsonWriter {
//        @Override
//        public void write(Object o, StringBuilder b, JsonMathEOSWriter writer) {
//            Enregistrable e = (Enregistrable)o;
//            b.append(",\"data\":\"").append(e.getDonnees());
//        }
//    }
    private static class ColorWriter implements JsonWriter {
        @Override
        public void write(Object o, StringBuilder b, JsonMathEOSWriter writer) {
            Color color = (Color) o;
            b.append(",\"value\":\"").append(ColorManager.getRGBHexa(color)).append("\"");
        }
    }
    private static class DataObjectWriter implements JsonWriter {
        @Override
        public void write(Object o, StringBuilder b, JsonMathEOSWriter writer) {
            DataObject data = (DataObject) o;
            for(Map.Entry<String, Data> donnee : data.getDataEntries()) {
                b.append(",");
                writeJsonUtf8String(donnee.getKey(),b);
                b.append(":");
                b.append(writer.objectToJsonCore(donnee.getValue()));
            }
            b.append(",\""+ELEMENTS+"\":{");
            boolean first = true;
            for(Map.Entry<String, String> donnee : data.getElementEntries()) {
                if(first) {first=false;} else {b.append(",");}
                writeJsonUtf8String(donnee.getKey(),b);
                b.append(":");
                writeJsonUtf8String(donnee.getValue(), b);
            }
            b.append("}");
            writer.writeFields(o, o.getClass(), DataObject.class, b);
        }
    }
//    private static class DataWriter implements JsonWriter {
//        @Override
//        public void write(Object o, StringBuilder b, JsonMathEOSWriter writer) {
//            Data data = (Data) o;
//
////            b.append(",\"" + DATA_CLASS_NAME + "\":\"").append(o.getClass().getName()).append("\",");
//            b.append(",");
////                out.write("\"data\":{ ");
//            boolean first = true;
//            for(Map.Entry<String, Data> donnee : data.data) {
//                if(first) {first=false;} else {b.append(",");}
//                b.append("\"").append(donnee.getKey()).append("\":");
//                b.append(writer.objectToJsonImpl(donnee.getValue()));
//            }
////                out.write("}");
//            if(!first) {b.append(",");first=true;}
//            b.append("\""+ELEMENTS+"\":{");
//            for(Map.Entry<String, String> donnee : data.elements) {
//                if(first) {first=false;} else {b.append(",");}
//                b.append("\"").append(donnee.getKey()).append("\":");
//                writeJsonUtf8String(donnee.getValue(), b);
//            }
//            b.append("}");
//        }
//    }
    private static class GraphiqueTexteWriter implements JsonWriter {
        @Override
        public void write(Object o, StringBuilder b, JsonMathEOSWriter writer) {
            Texte texte = (Texte) o;

            b.append(",\"x\":");
            b.append(texte.getX());
            b.append(",\"y\":");
            b.append(texte.getY());
            b.append(",\"content\":");
            writeJsonUtf8String(texte.getContenu(), b);
            writer.writeFields(texte, Texte.class.getSuperclass(), null, b);
            writer.writeFields(texte, o.getClass(), Texte.class, b);
        }
    }
    
    public static String objectToJson(Object o) {
        JsonMathEOSWriter jw = new JsonMathEOSWriter();
        jw.references.clear();
        jw.visites.clear();
        jw.checkReferences(o);
        jw.visites.clear();
        return jw.objectToJsonCore(o);
    }
    /** permet de découvrir si un objet fait référence à un objet déjà écrit. Ex : parent<->enfant **/
    private void checkReferences(Object o) {
        if(o==null) {return;}
        final Class c = o.getClass();
        if(c.isArray()) {
            for(int i = 0; i< Array.getLength(o); i++) {
                checkReferences(Array.get(o, i));
            }
            return;
        } else if(c.isPrimitive()) {
            return;
        } else if(!isReferencable(o)) {return;}
        else {
            final int code = System.identityHashCode(o);
//            final int code = o.hashCode();
            
            if(!visites.add(code)) {
                references.add(code);return;
            }//si l'objet a été visité 2 fois il s'agit d'une référence

            if(o instanceof List) {
                List L = (List)o;
                for(Object obj : L) {checkReferences(obj);}
            } else if(o instanceof Map) {
                Map m = (Map) o;
                for(Object obj : m.values()) {
                    checkReferences(obj);
                }
            }
            final Map<String, Field> map = getDeepDeclaredFields(c, null);
            for(final Field f : map.values()) {
                f.setAccessible(true);
                try {
                    checkReferences(f.get(o));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(JsonMathEOS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private static boolean isReferencable(Object o) {
        for(Class c : notReferences) {
            if(c.isInstance(o)) {return false;}
        }
        return true;
    }
    /** Appelé par objectToJson pour lancer la serialization **/
    private String objectToJsonCore(Object o) {
        if(o==null) {return "null";}
        final StringBuilder s = new StringBuilder();
        final Class c = o.getClass();
        
        final int code = o.hashCode();
        final boolean referenced = references.contains(code);
        final boolean visited = visites.contains(code);
        if(referenced && visited) {return s.append("{\"@ref\":").append(code).append("}").toString();}
        
        if(c.isArray()) {
            s.append('[');
            int n = Array.getLength(o);
            if(n>0) {
                s.append(objectToJsonCore(Array.get(o, 0)));
            }
            for(int i=1; i<n; i++) {
                s.append(",");
                s.append(objectToJsonCore(Array.get(o, i)));//best performances
            }
            s.append(']');
        } else if(c.isPrimitive()) {
            s.append(o.toString());
        } else if(o instanceof Collection) {
            Collection L = (Collection) o;
            s.append('{');
            if(referenced) {writeId(code,s);s.append(",");}
            writeType(c, s);
            s.append(",\"@items\":[");
            Iterator iter = L.iterator();
            if(iter.hasNext()) {
                s.append(objectToJsonCore(iter.next()));
            }
            while(iter.hasNext()) {
                s.append(",");
                s.append(objectToJsonCore(iter.next()));//best performances
            }
            s.append(']').append('}');
        } else if(o instanceof Map) {
            s.append('{');
            if(referenced) {writeId(code,s);s.append(",");}
            writeType(c, s);
//            s.append(",\"@keys\":[");
//            
//            StringBuilder keys = new StringBuilder();
//            StringBuilder values = new StringBuilder();
//            Set<Map.Entry> set = ((Map)o).entrySet();
//            boolean first = true;
//            for(Map.Entry entry : set) {
//                if(first) {first = false;} else {keys.append(",");values.append(",");}
//                keys.append(objectToJsonCore(entry.getKey()));
//                values.append(objectToJsonCore(entry.getValue()));
//            }
//            
//            s.append(keys).append("],\"@items\":\"[");
//            s.append(values).append("]}");
            s.append(",\"@map\":[");
            
            Set<Map.Entry> set = ((Map)o).entrySet();
            boolean first = true;
            for(Map.Entry entry : set) {
                if(first) {first = false;} else {s.append(",");}
                s.append("{\"key\":").append(objectToJsonCore(entry.getKey()));
                s.append(",\"value\":").append(objectToJsonCore(entry.getValue())).append("}");
            }
            
            s.append("]}");
        } else {
            writeObject(o, c, s);
        }
        return s.toString();
    }

    private void writeId(int id, StringBuilder b) {
        visites.add(id);
        b.append("\"@id\":").append(id);
    }
    private void writeType(Class c, StringBuilder b) {
        b.append("\"@type\":\"");
        b.append(c.getName());
        b.append('"');
    }
    /**
     * écrit les champs d'une classe depuis la classe départ jusqu'à la classe limite (exclue).
     * Attention, écrit une virgule initiale
     * @param o : l'objet dont les champs sont à écrire
     * @param depart : première classe dont les champs sont à considérer
     * @param limite : première classe dont les champs seront ignorés, ainsi que ceux de ses super-classes. null pour ignorer
     * @param b : le stringBuilder où écrire le résultat
     */
    private void writeFields(Object o, Class depart, Class limite, StringBuilder b) {
        Map<String, Field> meta = getDeepDeclaredFields(depart, limite);//tous les champs
//        if(meta.isEmpty()) {b.deleteCharAt(b.length()-1);}//Supprime la virgule initiale si la map est vide
        for(Map.Entry<String, Field> entry : meta.entrySet()) {
            b.append(",\"").append(entry.getKey()).append("\":");
            try {
                b.append(objectToJsonCore(entry.getValue().get(o)));
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(JsonMathEOS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void writeObject(Object o,Class c, StringBuilder b) {
        if (Byte.class.equals(c))
        {
            b.append(o.toString());
        }
        else if (Boolean.class.equals(c))
        {
            b.append(o.toString());
        }
        else if (Short.class.equals(c))
        {
//            b.append("{");
//            writeType(Short.class, b);
//            b.append(",\"value\":");
            b.append(o.toString());
//            b.append("}");
        }
        else if (Integer.class.equals(c))
        {
            b.append(o.toString());
        }
        else if (Long.class.equals(c))
        {
//            b.append("{");
//            writeType(Long.class, b);
//            b.append(",\"value\":");
            b.append(o.toString());
//            b.append("}");
        }
        else if (Double.class.equals(c))
        {
            b.append(o.toString());
        }
        else if (Float.class.equals(c))
        {
            b.append(o.toString());
        }
        else if (Character.class.equals(c))
        {
            writeJsonUtf8String(o.toString(),b);
        }
        else if (String.class.equals(c))
        {
            writeJsonUtf8String(o.toString(),b);
        }
        else if (Class.class.equals(c))
        {
            b.append("\"@type\":\"class\"");
            b.append(",\"value\":\"").append(c.getName()).append("\"");
        }
        else
        {
            b.append("{");
            final int code = o.hashCode();
            final boolean referenced = references.contains(code);
            if(referenced) {writeId(code,b);b.append(",");}
            
            writeType(c, b);
            
            JsonWriter w = writers.get(c);
            if(w!=null) {
                w.write(o, b, this);//doit commencer par une virgule
            }
            else {
                writeFields(o,c,null,b);
            }
            
            b.append("}");
        }
    }
    
    private static final Map<String, Map<String, Field>> _classMetaCache = new HashMap<>();
    /**
     * @param c Class instance
     * @param until Première classe dont les champs sont censés avoir déjà été pris en charge.
     * @return ClassMeta which contains fields of class.  The results are cached internally for performance
     *         when called again with same Class.
     */
    static Map<String, Field> getDeepDeclaredFields(Class c, Class until)
    {
        Map<String, Field> classInfo = new HashMap<>();
        Class curr = c;

        while (curr != until && curr != null)
        {
            Map<String, Field> classInfo2 = _classMetaCache.get(curr.getName());//si un parent a déjà été traité
            if(classInfo2==null) {
                classInfo2 = new HashMap<>();
                try
                {
                    Field[] local = curr.getDeclaredFields();

                    for (Field field : local)
                    {
                        if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()))
                        {    // speed up: do not process static fields.
                            if (!field.isAccessible())
                            {
                                try
                                {
                                    field.setAccessible(true);
                                }
                                catch (Exception ignored) { }
                            }
                            classInfo2.put(field.getName(), field);
                        }
                    }
                }
                catch (ThreadDeath t)
                {
                    throw t;
                }
                catch (Throwable ignored) { }
            }

            _classMetaCache.put(curr.getName(), classInfo2);
            classInfo.putAll(classInfo2);
            curr = curr.getSuperclass();
        }

        return classInfo;
    }
    
    /**
     * Write out special characters "\b, \f, \t, \n, \r", as such, backslash as \\
     * quote as \" and values less than an ASCII space (20hex) as "\\u00xx" format,
     * characters in the range of ASCII space to a '~' as ASCII, and anything higher in UTF-8.
     *
     * @param s String to be written in utf8 format on the output stream.
     * @throws IOException if an error occurs writing to the output stream.
     */
    public static void writeJsonUtf8String(String s, StringBuilder b)
    {
        b.append('\"');
        int len = s.length();

        for (int i = 0; i < len; i++)
        {
            char c = s.charAt(i);

            if (c < ' ')
            {    // Anything less than ASCII space, write either in \\u00xx form, or the special \t, \n, etc. form
                if (c == '\b')
                {
                    b.append("\\b");
                }
                else if (c == '\t')
                {
                    b.append("\\t");
                }
                else if (c == '\n')
                {
                    b.append("\\n");
                }
                else if (c == '\f')
                {
                    b.append("\\f");
                }
                else if (c == '\r')
                {
                    b.append("\\r");
                }
                else
                {
                    String hex = Integer.toHexString(c);
                    b.append("\\u");
                    int pad = 4 - hex.length();
                    for (int k = 0; k < pad; k++)
                    {
                        b.append('0');
                    }
                    b.append(hex);
                }
            }
            else if (c == '\\' || c == '"')
            {
                b.append('\\');
                b.append(c);
            }
            else
            {   // Anything else - write in UTF-8 form (multi-byte encoded) (OutputStreamWriter is UTF-8)
                b.append(c);
            }
        }
        b.append('\"');
    }}
