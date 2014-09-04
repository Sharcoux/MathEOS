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

package matheos.utils.librairies;

import matheos.utils.objets.maps.BidiMap;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.StyleConstants;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 *
 * @author François Billioud
 */
public abstract class JsoupTools {
    /**
     * permet de décomposer la partie "style" du html en couple objet/valeur
     * @param styleAttr la chaine html retournée par Element.attr("style");
     * @return une HashMap contenant les couples objet/valeur trouvés dans le style ou une map vierge sinon
     */
    public static HashMap<String, String> getStyleMap(String styleAttr) {
        HashMap<String, String> map = new HashMap<>();
        for(String s : styleAttr.split(";")) {
            String[] T = s.split(":");
            if(T.length>1) map.put(T[0], T[1]);
        }
        return map;
    }

    /**
     * permet de lire une propriété de style de l'objet e.
     * Le style est cherché dans les attributs html ET dans l'attribut "style"
     * @param e l'élément JSoup ciblé
     * @param cible la propriété ciblée (html ou css, les 2 seront cherchées)
     * @return la valeur lue ou une chaine vide si pas de valeur
     */
    public static String getStyle(Element e, String cible) {
        Map<String, String> styles = getStyleMap(e.attr("style"));
        String result = styles.get(cible);
        if(result==null) {
            result = e.attr(cible);
            if(result.isEmpty()) {
                String newCible = null;
                if(HTMLtoCSSAttribute.containsKey(cible) && !HTMLtoCSSAttribute.get(cible).equals(cible)) {
                    newCible = HTMLtoCSSAttribute.get(cible);
                } else if(HTMLtoCSSAttribute.containsValue(cible)) {newCible = HTMLtoCSSAttribute.getKey(cible);}
                if(newCible==null) {return "";}
                result = styles.get(newCible);
                if(result==null) {result = e.attr(newCible);}
            }
        }
        return result;
    }
    
    public static double getSizedStyle(Element e, String attribute) {
        String s = getStyle(e, attribute).replaceAll("[a-zA-Z]", "");
        if(!s.isEmpty()) {
            try {
                return Double.parseDouble(s);
            } catch(NumberFormatException ex) {
                Logger.getLogger(JsoupTools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return 0;
    }
    
    /**
     * permet d'écrire une propriété de style de l'objet e.
     * Le style est cherché dans les attributs html ET dans l'attribut "style"
     * @param e l'élément JSoup ciblé
     * @param cible la propriété ciblée
     * @param value la valeur désirée, null pour retirer. La valeur sera ajouté au css via l'attribut styles
     */
    public static void setStyleAttribute(Element e, String cible, String value) {
        String html = cible;String css = cible;
        if(HTMLtoCSSAttribute.containsKey(cible)) {css = HTMLtoCSSAttribute.get(cible);}
        if(HTMLtoCSSAttribute.containsValue(cible)) {html = HTMLtoCSSAttribute.getKey(cible);}
        Map styles = getStyleMap(e.attr("style"));
        styles.remove(css);styles.remove(html);e.removeAttr(html);e.removeAttr(css);//nettoyage
        if(value!=null) {styles.put(css, value);}
        setStyle(e, styles);
    }

    /**
     * permet d'ajouter des propriétés de style à l'objet e.
     * Les possibles conflits avec les attributs html sont évités
     * Le style est  dans l'attribut "style"
     * @param e l'élément JSoup ciblé
     * @param styles les styles à ajouter
     */
    public static void addStyles(Element e, Map<String, String> styles) {
        for(Entry<String,String> entry : styles.entrySet()) {setStyleAttribute(e, entry.getKey(), entry.getValue());}
    }

    /**
     * renvoie la chaine de caractère à insérer dans l'attribut style de l'élément
     * souhaité. Ex : "font-size:5;color:#ff0000;"
     * @param stylesMap les styles sous forme de Map
     * @return la chaine à insérer
     */
    private static String createStyleAttribute(Map<String, String> stylesMap) {
        String styles = "";
        for(String s : stylesMap.keySet()) {
            styles+= s + ":" + stylesMap.get(s) + ";";
        }
        return styles;
    }

    /**
     * récupère l'attribut color d'un Element.
     * L'objet retourné est directement un objet Color
     * @param e l'element considéré
     * @return la couleur lue ou null
     */
    public static Color getColor(Element e) {
        String couleur = getStyle(e, "color");
        if(couleur.isEmpty()) {return null;}
        Color color = (Color) CSSToJavaValue.get(couleur);
        return color!=null ? color : new Color(Long.decode(couleur).intValue());
    }
    
    /**
     * récupère l'attribut d'alignement d'un Element.
     * L'objet retourné est directement un entier Java correspondant aux StyleConstants
     * @param e l'element considéré
     * @return le style lu ou <code>StyleConstants.ALIGN_LEFT</code> si non trouvé
     */
    public static int getAlignment(Element e) {
        String alignment = getStyle(e, "text-align");
        if(alignment.isEmpty()) {return StyleConstants.ALIGN_LEFT;}
        Integer align = (Integer) CSSToJavaValue.get(alignment);
        return align==null ? StyleConstants.ALIGN_LEFT : align;
    }
    
    /**
     * récupère l'attribut font-size d'un Element.
     * L'objet retourné est directement au format "pt".
     * La taille est cherchée dans les attributs html et css
     * @param e l'element considéré
     * @return la taille lue au format pt, ou 12 par défaut
     */
    public static int getFontSize(Element e) {
        String fontSize = getStyle(e, "font-size");
        return convertFontSize2PT(fontSize);
    }
        
    /** Permet de convertir les tailles de texte HTML3 en unités "em" conformes CSS3 **/
    public static final Double[] FONT_CONVERSION_EM = {0.0, 0.7, 0.8, 1.0, 1.2, 1.5, 2.0, 3.0};
    /** Permet de convertir les tailles de texte HTML3 en unités "px" **/
    public static final Integer[] FONT_CONVERSION_PX = {0, 11, 13, 16, 19, 24, 32, 48};
    /** Permet de convertir les tailles de texte HTML3 en unités "pt" conformes RTF **/
    public static final Integer[] FONT_CONVERSION_PT = {0, 8, 10, 12, 14, 18, 24, 36};
    private static int convertHTML2PT(int html) {
        return FONT_CONVERSION_PT[html];
    }

    public static int convertFontSize2PT(String fontSize) {
        try {
            if(fontSize==null || fontSize.isEmpty()) {return 14;}
            if(fontSize.endsWith("em")) {return convertEM2PT(Double.parseDouble(fontSize.substring(0, fontSize.length()-2).trim()));}
            if(fontSize.endsWith("px")) {return convertPX2PT(Integer.parseInt(fontSize.substring(0, fontSize.length()-2).trim()));}
            if(fontSize.endsWith("%")) {return convertPercent2PT(Integer.parseInt(fontSize.substring(0, fontSize.length()-1).trim()));}
            if(fontSize.endsWith("pt")) {return Integer.parseInt(fontSize.substring(0, fontSize.length()-2).trim());}
            int size = Math.round(Float.parseFloat(fontSize));
            if(size<=7) {return FONT_CONVERSION_PT[size];}
            return size;
        } catch(NumberFormatException ex) {
            Logger.getLogger(JsoupTools.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("impossible de convertir : "+fontSize);
        }
        return 14;
    }
    
    private static int convertEM2PT(double em) {
        return convertHTML2PT(convertEM2HTML(em));
    }
    private static int convertEM2HTML(double em) {
        return search(FONT_CONVERSION_EM, em);
    }
    
    private static int convertPX2PT(int px) {
        return convertHTML2PT(convertPX2HTML(px));
    }
    private static int convertPX2HTML(double em) {
        return search(FONT_CONVERSION_PX, em);
    }
    
    private static int convertPercent2PT(int percent) {
        return convertHTML2PT(convertEM2HTML(percent/100.0));
    }
    
    public static double convertPT2EM(int pt) {
        return FONT_CONVERSION_EM[convertPT2HTML(pt)];
    }
    private static int convertPT2HTML(int pt) {
        return search(FONT_CONVERSION_PT,pt);
    }
    
    
    private static int search(Object[] a, Object t) {
        int i = Arrays.binarySearch(a, t);
        if(i<0) {i = -i-1;}
        if(i>a.length-1) {i=a.length-1;}
        return i;
    }

    /**
     * Transforme la couleur passée en paramètre en une chaine hexadécimale.
     * Ex : Color.red devient ffff0000
     * @param c la couleur cherchée
     * @return la chaine hexadécimale
     */
//    public static String colorToString(Color c) {
//        return Integer.toHexString(c.getRGB());
//    }

    /**
     * Transforme la couleur passée en paramètre en une chaine hexadécimale type HTML.
     * Ex : Color.red devient #ff0000
     * @param c la couleur cherchée
     * @return la chaine hexadécimale
     */
    public static String colorToHTMLString(Color c) {
        return String.format("#%06X", (0xFFFFFF & c.getRGB()));
    }

    public static void removeComments(Node node) {
        for (int i = 0; i < node.childNodes().size();) {
            Node child = node.childNode(i);
            if (child.nodeName().equals("#comment"))
                child.remove();
            else {
                removeComments(child);
                i++;
            }
        }
    }
    
    /**
     * Convertit les attributs de l'élément ciblés en leurs équivalents CSS
     * @param e l'élément à modifier
     */
    public static void convertAttributesToCSS(Element e) {
        Map<String, String> styles = getStyleMap(e.attr("style"));
        for(Attribute attribute : e.attributes()) {
            String key = attribute.getKey();
            String value = attribute.getValue();
            String css = HTMLtoCSSAttribute.getValue(key);
            if(css!=null) {
                styles.put(css, value);
                e.removeAttr(key);
            }
        }
        setStyle(e, styles);
        setStyleAttribute(e, "font-size", getFontSize(e)+"pt");//Conversion de la taille en pt
    }
    
    /**
     * Convertit les styles de l'élément ciblés en leurs équivalents HTML
     * @param e l'élément à modifier
     */
    public static void convertStylesToHTMLAttribute(Element e) {
        Map<String, String> styles = getStyleMap(e.attr("style"));
        for(Entry<String, String> style : styles.entrySet()) {
            String attribute = HTMLtoCSSAttribute.getKey(style.getKey());
            if(attribute!=null) {
                String value = style.getValue();
                if(attribute.equals("size")) {value = convertPT2HTML(convertFontSize2PT(value))+"";}
                e.attr(attribute, value);
            }
            styles.remove(style.getKey());
        }
        setStyle(e, styles);
    }

    /**
     * remplace l'attribut "style" de l'élément par le contenu de la map,
     * ou supprime l'attribut "style" si la map est vide
     * @param e l'élément à modifier
     * @param styles la map des styles css de l'élément
     */
    public static void setStyle(Element e, Map<String, String> styles) {
        if(styles.isEmpty()) {e.removeAttr("style");}
        else {e.attr("style", createStyleAttribute(styles));}
    }
    
    private static final BidiMap<String, String> HTMLtoCSSAttribute;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("size", "font-size");
        map.put("face", "font-family");
        map.put("bgcolor", "background-color");
        map.put("background", "background-image");
        map.put("align", "text-align");
        map.put("valign", "vertical-align");
        map.put("alink", "active");
        map.put("vlink", "visited");
        map.put("color", "color");
        map.put("width", "width");
        map.put("height", "height");
        HTMLtoCSSAttribute = new BidiMap<>(map);
    }
    
    public static final Map<String, Object> CSSToJavaValue;
    static {
        Map<String, Object> map = new HashMap<>();
        map.put("left", StyleConstants.ALIGN_LEFT);
        map.put("center", StyleConstants.ALIGN_CENTER);
        map.put("right", StyleConstants.ALIGN_RIGHT);
        map.put("justified", StyleConstants.ALIGN_JUSTIFIED);
        map.put("black", Color.BLACK);
        map.put("white", Color.WHITE);
        map.put("blue", Color.BLUE);
        map.put("green", Color.GREEN);
        map.put("red", Color.RED);
        CSSToJavaValue = new BidiMap<>(map);
    }

    private JsoupTools() {throw new AssertionError("try to instanciate utilitary class");}
}
