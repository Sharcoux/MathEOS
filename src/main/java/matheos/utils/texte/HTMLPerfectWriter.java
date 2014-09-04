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
    private MutableAttributeSet convAttr = new SimpleAttributeSet();
    private MutableAttributeSet oConvAttr = new SimpleAttributeSet();
    private ArrayList<HTML.Tag> tags = new ArrayList<HTML.Tag>(10);
    private ArrayList<Object> tagValues = new ArrayList<Object>(10);
    private ArrayList<HTML.Tag> tagsToRemove = new ArrayList<HTML.Tag>(10);
    @Override
    protected void writeAttributes(AttributeSet attr) throws IOException {
        // translate css attributes to html
        convAttr.removeAttributes(convAttr);
        convertToHTML(attr, convAttr);

        Enumeration names = convAttr.getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            System.out.println("examine : "+name+" : "+convAttr.getAttribute(name));
            if (name instanceof HTML.Tag ||
                name instanceof StyleConstants ||
                name == HTML.Attribute.ENDTAG) {
                continue;
            }
            System.out.println("write : "+name+" : "+convAttr.getAttribute(name));
            write(" " + name + "=\"" + convAttr.getAttribute(name) + "\"");
        }
    }
    AttributeSet convertToHTML(AttributeSet from, MutableAttributeSet to) {
        if (to == null) {
            to = convAttr;
        }
        to.removeAttributes(to);
        if (from == null) {
            return to;
        }
        Enumeration keys = from.getAttributeNames();
        String value = "";
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object attributeValue = from.getAttribute(key);
            if(attributeValue==null || attributeValue.equals("")) {continue;}
            if (key instanceof CSS.Attribute ) {
                value = value + " " + key + ":" + attributeValue + ";";
            } else {
                to.addAttribute(key, attributeValue);
            }
        }
        if (value.length() > 0) {
            to.addAttribute(HTML.Attribute.STYLE, value);
            System.out.println(value);
            if(value.contains("font-size")) {
                System.out.println("font-size detected");
            }
        }
        return to;
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
            System.out.println("name : "+name+" : "+name.getClass().getSimpleName()+" : "+attr.getAttribute(name));
            if (name instanceof HTML.Tag) {
                HTML.Tag tag = (HTML.Tag)name;
                if (tag == HTML.Tag.FORM || tags.contains(tag)) {
                    continue;
                }
                write('<');
                write(tag.toString());
                Object o = attr.getAttribute(tag);
                if (o != null && o instanceof AttributeSet) {
                    System.out.println("write o : "+((AttributeSet)o).getAttributeNames());
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
            System.out.println("tagToRemove : "+t);
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
