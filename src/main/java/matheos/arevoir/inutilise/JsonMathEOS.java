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
package matheos.arevoir.inutilise;

//import matheos.graphic.composants.Texte;
//import matheos.json.JsonReader.JsonBetterReader;
//import matheos.sauvegarde.Data;
//import matheos.utils.managers.ColorManager;
//import java.io.IOException;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;

/**
 *
 * @author François Billioud
 */
public abstract class JsonMathEOS {

//    static abstract class Readers {
//        static class ColorReader extends JsonBetterReader {
//            public Object read(Object o, LinkedList<JsonObject> stack) throws IOException {
//                if (o instanceof String) {
//                    return ColorManager.getColorFromHexa((String)o);
//                }
//                
//                JsonObject jObj = (JsonObject) o;
//                if (jObj.containsKey("value")) {
//                    return ColorManager.getColorFromHexa((String)jObj.get("value"));
//                }
//                
//                throw new IOException("Color missing 'value' field, pos = " + jObj.pos);
//            }
//        }
//        static class GraphicTexteReader extends JsonBetterReader {
//            public Object read(Object o, LinkedList<JsonObject> stack) throws IOException {
//                JsonObject jObj = (JsonObject) o;
//                double x = Double.valueOf(jObj.get("x").toString());jObj.remove("x");
//                double y = Double.valueOf(jObj.get("y").toString());jObj.remove("y");
//                String content = (String) jObj.get("content");jObj.remove("content");
//                
//                Texte texte;
//                try{
//                    String type = jObj.type;
//                    Class c = JsonReader.classForName2(type);
//                    texte = (Texte) JsonReader.newInstance(c, jObj);
//                } catch (SecurityException | IllegalArgumentException ex) {
//                    Logger.getLogger(JsonMathEOS.class.getName()).log(Level.SEVERE, null, ex);
//                    texte = new Texte(x,y,content);
//                }
//                assignFields(stack, jObj, o);
//                
//                return texte;
//            }
//        }
//        static class DataReader extends JsonBetterReader {
//            public Data read(Object o, LinkedList<JsonObject> stack) throws IOException {
//                JsonObject jObj = (JsonObject) o;
//                Data data;
//                try{
//                    String type = jObj.type;
//                    Class c = JsonReader.classForName2(type);
//                    data = (Data) JsonReader.newInstance(c, jObj);
//                } catch (SecurityException | IllegalArgumentException ex) {
//                    Logger.getLogger(JsonMathEOS.class.getName()).log(Level.SEVERE, null, ex);
//                    data = new Data();
//                }
//                for(Map.Entry<String, JsonObject> entry : (Set<Map.Entry<String, JsonObject>>)jObj.entrySet()) {
//                    switch (entry.getKey()) {
//                        case JsonMathEOSWriter.ELEMENTS:
//                            for(Map.Entry<String, String> e : (Set<Map.Entry<String, String>>)entry.getValue().entrySet()) {
//                                data.putElement(e.getKey(), e.getValue());
//                            }   break;
//                        default:
//                            data.putData(entry.getKey(), read(entry.getValue(),stack));
//                            break;
//                    }
//                }
//                return data;
//            }
//        }
//    }
    

}
