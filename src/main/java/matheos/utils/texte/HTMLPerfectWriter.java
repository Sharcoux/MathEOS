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

package matheos.utils.texte;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLWriter;
import static matheos.utils.texte.EditeurKit.STRIKE_COLOR_ATTRIBUTE;

/**
 *
 * @author François Billioud
 */
public class HTMLPerfectWriter extends HTMLWriter {
    public HTMLPerfectWriter(Writer w, HTMLDocument doc, int pos, int len) {
        super(w, doc, pos, len);
    }
    public HTMLPerfectWriter(Writer w, HTMLDocument doc) {
        super(w, doc);
    }
    private final MutableAttributeSet convAttr = new SimpleAttributeSet();
    private final MutableAttributeSet oConvAttr = new SimpleAttributeSet();
    private final ArrayList<HTML.Tag> tags = new ArrayList<>(10);
    private final ArrayList<Object> tagValues = new ArrayList<>(10);
    private final ArrayList<HTML.Tag> tagsToRemove = new ArrayList<>(10);

    AttributeSet convertToHTML(AttributeSet from, MutableAttributeSet to) {
        if (to == null) {
            to = convAttr;
        }
        to.removeAttributes(to);
        convertToHTML32(from, to);
        return to;
    }
    
    /**
     * Create an older style of HTML attributes.  This will
     * convert character level attributes that have a StyleConstants
     * mapping over to an HTML tag/attribute.  Other CSS attributes
     * will be placed in an HTML style attribute.
     */
    private static void convertToHTML32(AttributeSet from, MutableAttributeSet to) {
        if (from == null) {
            return;
        }
        Enumeration keys = from.getAttributeNames();
        String value = "";
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key instanceof CSS.Attribute) {
                if ((key == CSS.Attribute.FONT_FAMILY) ||
                    (key == CSS.Attribute.FONT_SIZE) ||
                    (key == CSS.Attribute.COLOR)) {

                    createFontAttribute((CSS.Attribute)key, from, to);
                } else if (key == CSS.Attribute.FONT_WEIGHT) {
                    // add a bold tag is weight is bold
                    if (StyleConstants.isBold(from)) {
                        addAttribute(to, HTML.Tag.B, SimpleAttributeSet.EMPTY);
                    }
                } else if (key == CSS.Attribute.FONT_STYLE) {
                    String s = from.getAttribute(key).toString();
                    if (s.contains("italic")) {
                        addAttribute(to, HTML.Tag.I, SimpleAttributeSet.EMPTY);
                    }
                } else if (key == CSS.Attribute.TEXT_DECORATION) {
                    String decor = from.getAttribute(key).toString();
                    if (decor.contains("underline")) {
                        addAttribute(to, HTML.Tag.U, SimpleAttributeSet.EMPTY);
                    }
                    if (decor.contains("line-through")) {
                        if (value.length() > 0) {value += "; ";}
                        value += key + ": " + from.getAttribute(key);
                        if(from.isDefined(STRIKE_COLOR_ATTRIBUTE)) {
                            if (value.length() > 0) {value += "; ";}
                            value += key+"-color" + ": " + from.getAttribute(STRIKE_COLOR_ATTRIBUTE);
                        }
                        MutableAttributeSet set = new SimpleAttributeSet();
                        set.addAttribute(HTML.Attribute.STYLE, value);
                        addAttribute(to, HTML.Tag.FONT, set);
                    }
                } else if (key == CSS.Attribute.VERTICAL_ALIGN) {
                    String vAlign = from.getAttribute(key).toString();
                    if (vAlign.contains("sup")) {
                        addAttribute(to, HTML.Tag.SUP, SimpleAttributeSet.EMPTY);
                    }
                    if (vAlign.contains("sub")) {
                        addAttribute(to, HTML.Tag.SUB, SimpleAttributeSet.EMPTY);
                    }
                } else if (key == CSS.Attribute.TEXT_ALIGN) {
                    addAttribute(to, HTML.Attribute.ALIGN,
                                    from.getAttribute(key).toString());
                } else {
                    // default is to store in a HTML style attribute
                    if (value.length() > 0) {
                        value += "; ";
                    }
                    value += key + ": " + from.getAttribute(key);
                }
            } else {
                Object attr = from.getAttribute(key);
                if (attr instanceof AttributeSet) {
                    attr = ((AttributeSet)attr).copyAttributes();
                }
                addAttribute(to, key, attr);
            }
        }
        if (value.length() > 0) {
            to.addAttribute(HTML.Attribute.STYLE, value);
        }
    }
    
    /**
     * Add an attribute only if it doesn't exist so that we don't
     * loose information replacing it with SimpleAttributeSet.EMPTY
     */
    private static void addAttribute(MutableAttributeSet to, Object key, Object value) {
        Object attr = to.getAttribute(key);
        if (attr == null || attr == SimpleAttributeSet.EMPTY) {
            to.addAttribute(key, value);
        } else {
            if (attr instanceof MutableAttributeSet &&
                value instanceof AttributeSet) {
                ((MutableAttributeSet)attr).addAttributes((AttributeSet)value);
            }
        }
    }
    
    /**
     * Create/update an HTML &lt;font&gt; tag attribute.  The
     * value of the attribute should be a MutableAttributeSet so
     * that the attributes can be updated as they are discovered.
     */
    private static void createFontAttribute(CSS.Attribute a, AttributeSet from,
                                    MutableAttributeSet to) {
        MutableAttributeSet fontAttr = (MutableAttributeSet)
            to.getAttribute(HTML.Tag.FONT);
        if (fontAttr == null) {
            fontAttr = new SimpleAttributeSet();
            to.addAttribute(HTML.Tag.FONT, fontAttr);
        }
        // edit the parameters to the font tag
        String htmlValue = from.getAttribute(a).toString();
        if (a == CSS.Attribute.FONT_FAMILY) {
            fontAttr.addAttribute(HTML.Attribute.FACE, htmlValue);
        } else if (a == CSS.Attribute.FONT_SIZE) {
            fontAttr.addAttribute(HTML.Attribute.SIZE, htmlValue);
        } else if (a == CSS.Attribute.COLOR) {
            fontAttr.addAttribute(HTML.Attribute.COLOR, htmlValue);
        }
    }
    
    /**
     * Searches for embedded tags in the AttributeSet
     * and writes them out.  It also stores these tags in a vector
     * so that when appropriate the corresponding end tags can be
     * written out.
     *
     * @exception IOException on any I/O error
     */
    @Override
    protected void writeEmbeddedTags(AttributeSet attr) throws IOException {

        // translate css attributes to html
        attr = convertToHTML(attr, oConvAttr);

        Enumeration names = attr.getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
//            System.out.println("name : "+name+" : "+name.getClass().getSimpleName()+" : "+attr.getAttribute(name));
            if (name instanceof HTML.Tag) {
                HTML.Tag tag = (HTML.Tag)name;
                if (tag == HTML.Tag.FORM || tags.contains(tag)) {
                    continue;
                }
                write('<');
                write(tag.toString());
                Object o = attr.getAttribute(tag);
                if (o != null && o instanceof AttributeSet) {
//                    System.out.println("write o : "+((AttributeSet)o).getAttributeNames());
                    writeAttributes((AttributeSet)o);
                }
                write('>');
                tags.add(tag);
                tagValues.add(o);
            }
        }
    }
    /**
     * Searches the attribute set and for each tag
     * that is stored in the tag vector.  If the tag isnt found,
     * then the tag is removed from the vector and a corresponding
     * end tag is written out.
     *
     * @exception IOException on any I/O error
     */
    @Override
    protected void closeOutUnwantedEmbeddedTags(AttributeSet attr) throws IOException {

        tagsToRemove.clear();

        // translate css attributes to html
        attr = convertToHTML(attr, null);

        HTML.Tag t;
        Object tValue;
        int firstIndex = -1;
        int size = tags.size();
        // First, find all the tags that need to be removed.
        for (int i = size - 1; i >= 0; i--) {
            t = tags.get(i);
//            System.out.println("tagToRemove : "+t);
            tValue = tagValues.get(i);
            if ((attr == null) || noMatchForTagInAttributes(attr, t, tValue)) {
                firstIndex = i;
                tagsToRemove.add(t);
            }
        }
        if (firstIndex != -1) {
            // Then close them out.
            boolean removeAll = ((size - firstIndex) == tagsToRemove.size());
            for (int i = size - 1; i >= firstIndex; i--) {
                t = tags.get(i);
                if (removeAll || tagsToRemove.contains(t)) {
                    tags.remove(i);
                    tagValues.remove(i);
                }
                write('<');
                write('/');
                write(t.toString());
                write('>');
            }
            // Have to output any tags after firstIndex that still remaing,
            // as we closed them out, but they should remain open.
            size = tags.size();
            for (int i = firstIndex; i < size; i++) {
                t = tags.get(i);
                write('<');
                write(t.toString());
                Object o = tagValues.get(i);
                if (o != null && o instanceof AttributeSet) {
                    System.out.println("write o2 : "+o);
                    writeAttributes((AttributeSet)o);
                }
                write('>');
            }
        }
    }
    /**
     * Searches the attribute set for a tag, both of which
     * are passed in as a parameter.  Returns true if no match is found
     * and false otherwise.
     */
    private boolean noMatchForTagInAttributes(AttributeSet attr, HTML.Tag t,
                                              Object tagValue) {
        if (attr != null && attr.isDefined(t)) {
            Object newValue = attr.getAttribute(t);

            if ((tagValue == null) ? (newValue == null) :
                (newValue != null && tagValue.equals(newValue))) {
                return false;
            }
        }
        return true;
    }
}
