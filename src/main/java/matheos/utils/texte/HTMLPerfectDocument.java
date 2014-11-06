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
 * These additional terms refer to the source code of bomehc.
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

package matheos.utils.texte;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author François Billioud
 */
public class HTMLPerfectDocument implements StyledDocument {

    public static final String CHARACTER_ELEMENT = "content";
    public static final String EMPTY_ELEMENT = "empty";
    
    private Style style;
    private HTMLRootElement root = new HTMLRootElement();
    
    Map<HTMLElement, HTMLPosition> positionMap = new HashMap<>();
    Map<Object, Object> properties = new HashMap<>();
    
    EventListenerList list = new EventListenerList();
    
    @Override
    public Style addStyle(String nm, Style parent) {
        return style = parent;
    }

    @Override
    public void removeStyle(String nm) {
    }

    @Override
    public Style getStyle(String nm) {
        return style;
    }

    @Override
    public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
        for(int i=offset; i<offset+length; i++) {
            root.getCharacterElement(i).setCharacterAttributes(s,replace);
        }
    }

    @Override
    public void setParagraphAttributes(int offset, int length, AttributeSet s, boolean replace) {
        for(int i=offset; i<offset+length; i++) {
            getParagraphElement(i).setParagraphAttributes(s,replace);
        }
    }

    @Override
    public void setLogicalStyle(int pos, Style s) {
        style = s;
    }

    @Override
    public Style getLogicalStyle(int p) {
        return style;
    }

    @Override
    public HTMLParagraphElement getParagraphElement(int pos) {
        HTMLElement p = getCharacterElement(pos);
        while(p!=null && !p.isParagraphElement()) {p = p.getParentElement();}
        if(p==null) {return root.body().addNewParagraph();}
        return (HTMLParagraphElement) p;
    }

    @Override
    public HTMLElement getCharacterElement(int pos) {
        return getDefaultRootElement().getCharacterElement(pos);
    }

    @Override
    public Color getForeground(AttributeSet attr) {
        return StyleConstants.getForeground(attr);
    }

    @Override
    public Color getBackground(AttributeSet attr) {
        return StyleConstants.getBackground(attr);
    }

    @Override
    public Font getFont(AttributeSet attr) {
        int st = StyleConstants.isBold(attr) ? Font.BOLD : 0;
        st += StyleConstants.isItalic(attr) ? Font.ITALIC : 0;
        if(st==0) {st = Font.PLAIN;}
        return new Font(StyleConstants.getFontFamily(attr), st, StyleConstants.getFontSize(attr));
    }

    @Override
    public int getLength() {
        return getDefaultRootElement().size();
    }

    @Override
    public void addDocumentListener(DocumentListener listener) {
        list.add(DocumentListener.class, listener);
    }

    @Override
    public void removeDocumentListener(DocumentListener listener) {
        list.remove(DocumentListener.class, listener);
    }

    @Override
    public void addUndoableEditListener(UndoableEditListener listener) {
        list.add(UndoableEditListener.class, listener);
    }

    @Override
    public void removeUndoableEditListener(UndoableEditListener listener) {
        list.remove(UndoableEditListener.class, listener);
    }

    @Override
    public Object getProperty(Object key) {
        return properties.get(key);
    }

    @Override
    public void putProperty(Object key, Object value) {
        properties.put(key, value);
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        for(HTMLPosition pos : positionMap.values()) {
            if(pos.elt.contains(offs)) {pos.sizeReduced(offs-pos.elt.getStartOffset(), len);}
        }
        root.remove(offs, len);
    }

    @Override
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        for(HTMLPosition pos : positionMap.values()) {
            if(pos.elt.contains(offset)) {pos.sizeRaised(offset-pos.elt.getStartOffset(), str.length());}
        }
        root.insertString(offset, str, a);
    }

    @Override
    public String getText(int offset, int length) throws BadLocationException {
        return root.getText(offset, length);
    }

    @Override
    public void getText(int offset, int length, Segment txt) throws BadLocationException {
        txt.array = getText(offset, length).toCharArray();
        txt.count = length;
        txt.offset = offset;
    }

    @Override
    public Position getStartPosition() {
        return new Position() {
            @Override
            public int getOffset() {
                return 0;
            }
        };
    }

    @Override
    public Position getEndPosition() {
        return new Position() {
            @Override
            public int getOffset() {
                return getLength();
            }
        };
    }

    @Override
    public Position createPosition(final int offs) throws BadLocationException {
        if(offs<0 || offs>getLength()) {throw new BadLocationException("bad location Exception ", offs);}
        return new HTMLPosition(offs);
    }

    @Override
    public HTMLElement[] getRootElements() {
        HTMLElement root = getDefaultRootElement();
        HTMLElement[] T = {root};
        return T;
    }

    @Override
    public HTMLElement getDefaultRootElement() {
        return root;
    }

    @Override
    public void render(Runnable r) {
        r.run();
    }

    class HTMLPosition implements Position {
        private HTMLElement elt;
        private int localOffset;

        public HTMLPosition(int offset) {
            this.elt = getDefaultRootElement().getCharacterElement(offset);
            this.localOffset = offset-elt.getStartOffset();
        }
        
        public void toParent() {
            int start = elt.getStartOffset();
            elt = elt.getParentElement();
            localOffset = start;
        }
        
        public void sizeReduced(int localStart, int len) {
            if(localStart<localOffset) {
                int diff = localOffset-localStart;
                localOffset = Math.min(diff, len);
            }
        }
        
        public void sizeRaised(int localStart, int len) {
            if(localStart<localOffset) {localOffset+=len;}
            if(localOffset==localStart && localOffset!=0) {localOffset+=len;}
        }
        
        @Override
        public int getOffset() {
            return elt.getStartOffset()+localOffset;
        }
        
    }

    public abstract class HTMLElement implements javax.swing.text.Element {

        protected HTMLElement parent;
        protected final List<HTMLElement> children;
        protected final MutableAttributeSet attributes;
        private final String name;

        public HTMLElement(HTMLElement parent, String name, AttributeSet attributes, List<HTMLElement> children) {
            this.parent = parent;
            this.children = children;
            this.name = name;
            this.attributes = new SimpleAttributeSet(attributes);
            if(parent!=null) {this.attributes.setResolveParent(parent.getAttributes());}
        }
        
        public void insert(HTMLElement e, int position) {
            if(children==null) {return;}
            children.add(position, e);
        }
        public void append(HTMLElement e) {
            if(children==null) {return;}
            children.add(e);
        }
        
        public boolean contains(int offset) {
            int start = getStartOffset();
            int end = start+size();
            return start<=offset && end>=offset;
        }

        @Override
        public javax.swing.text.Document getDocument() {
            return HTMLPerfectDocument.this;
        }

        @Override
        public HTMLElement getParentElement() {
            return parent;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public MutableAttributeSet getAttributes() {
            return attributes;
        }

        @Override
        public int getStartOffset() {
            if(parent!=null) {return parent.getElementOffset(this);}
            return 0;
        }
        
        public void remove(HTMLElement e) {
            if(isLeaf()) {return;}
            int start = getStartOffset();
            for(HTMLPosition pos : positionMap.values()) {
                int i = getElementOffset(pos.elt);
                if(i>=0) {pos.localOffset = i - start;}
            }
            children.remove(e);
        }
        
        public String getText(int offset, int length) {
            if(isLeaf()) {return "";}
            String s = "";
            int startElt = getStartOffset();
            int endElt = startElt+size();
            int start = Math.max(startElt,offset);
            int end = Math.min(offset+length,endElt);
            int startChild = startElt;
            for(HTMLElement e : children) {
                int endChild = startChild+e.size();
                if(start>=endChild) {continue;}
                s+=e.getText(start, Math.min(end, endChild));
                if(endChild>=end) {return s;}
                if(e instanceof HTMLParagraphElement) {s+='\n';}
            }
            return s;
        }
        
        public int getElementOffset(HTMLElement htmlElt) {
            if(htmlElt.isLeaf()) {return 0;}
            int offset = getStartOffset();
            for(HTMLElement e : children) {
                if(e==htmlElt) {return offset;}
                offset+=htmlElt.size();
                if(htmlElt.isBlock()) {offset+=1;}//retour à la ligne
            }
            return -1;//element non trouvé
        }

        public int size() {
            if(isLeaf()) {return 0;}
            int size=0;
            for(HTMLElement e : children) {
                size+=e.size();
            }
            return size;
        }
        
        @Override
        public int getEndOffset() {
            return getStartOffset()+size();
        }

        @Override
        public int getElementIndex(int offset) {
            int localOffset = offset-getStartOffset();
            if(localOffset<0) {return 0;}
            if(localOffset>size()) {return -1;}
            if(isLeaf()) {return -1;}
            int end=0, i = 0;
            for(HTMLElement e : children) {
                end+=e.size();
                if(end>localOffset) {return i;}
                i++;
            }
            return -1;
        }
        
        public HTMLCharacterElement getCharacterElement(int offset) {
            int start = getStartOffset();
            if(start>offset) {return null;}//element non trouvé
            if(start==offset && this.isLeaf()) {return (this instanceof HTMLCharacterElement) ? ((HTMLCharacterElement)this) : new HTMLEmptyElement(this);}
            for(HTMLElement e : children) {
                start+=e.size();
                if(start>=offset) {return e.getCharacterElement(offset);}
            }
            return null;//non trouvé
        }

        public void remove(int offs, int len) {
            if(isLeaf()) {getParentElement().remove(this);return;}
            int start = getStartOffset();
            if(offs<=start && offs+len>start+size()) {remove(this);}
            else {
                for(HTMLElement e : children) {e.remove(offs, len);}
            }
        }
        
        public void insertString(int offs, String str, AttributeSet attr) {
            int start = getStartOffset();
            int end = start+size();
            if(offs<start||offs>end) {return;}
            int i = getElementIndex(offs);
            HTMLElement e = getElement(i);
            if(e instanceof HTMLCharacterElement) {
                List<HTMLElement> L = new LinkedList<>();
                for(int j=0; j<str.length(); j++) {
                    L.add(new HTMLCharacterElement(this, CHARACTER_ELEMENT, attr, str.charAt(j)));
                }
                children.addAll(offs, L);
            }
        }
        
        @Override
        public int getElementCount() {
            return children==null ? 0 : children.size();
        }

        @Override
        public HTMLElement getElement(int index) {
            return children==null ? null : children.get(index);
        }

        @Override
        public boolean isLeaf() {
            return children==null || children.isEmpty();
        }
        
        public boolean isParagraphElement() {
            return (this instanceof HTMLParagraphElement);
        }
        
        public boolean isBlock() {
            boolean block;
            switch(getName()) {
                case "div" : block = true; break;
                case "p" : block = true; break;
                case "hr" : block = true; break;
                case "table" : block = true; break;
                case "ul" : block = true; break;
                case "li" : block = true; break;
                case "tr" : block = true; break;
                default : block = false;
            }
            return block;
        }
        
    }
    
    public class HTMLCharacterElement extends HTMLElement {

        private char content;
        
        public HTMLCharacterElement(HTMLElement parent, String name, AttributeSet attributes, char content) {
            super(parent, name, attributes, null);
            this.content = content;
        }
        
        @Override
        public String getText(int offset, int length) {
            return offset==getStartOffset() ? content+"" : "";
        }
        
        @Override
        public void remove(int offs, int len) {
            int start = getStartOffset();
            if(start!=offs) {return;}//pas concerné
            if(getParentElement()==null) {return;}//aucun sens car pas de parent duquel retirer le noeud
            getParentElement().remove(this);
            parent = null;
        }
        
        @Override
        public int size() {return 1;}
        
        @Override
        public int getElementCount() {
            return 0;
        }

        @Override
        public HTMLElement getElement(int index) {
            return null;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }
        
        @Override
        public int getElementIndex(int offset) {return -1;}

        @Override
        public HTMLCharacterElement getCharacterElement(int offset) {
            int start = getStartOffset();
            if(start!=offset) {return null;}//element non trouvé
            return this;
        }
        
        public void setCharacterAttributes(AttributeSet att, boolean replace) {
            if(replace) {attributes.removeAttributes(att);}
            attributes.addAttributes(att);
        }
    }

    public class HTMLEmptyElement extends HTMLCharacterElement {

        public HTMLEmptyElement(HTMLElement parent) {
            super(parent, EMPTY_ELEMENT, new SimpleAttributeSet(), ' ');
        }
        
        @Override
        public int size() {return 0;}
        @Override
        public void remove(int offs, int len) {}
    }
    
    public class HTMLParagraphElement extends HTMLElement {

        public HTMLParagraphElement(HTMLElement parent, String name, AttributeSet attributes) {
            super(parent, name, attributes, new LinkedList<HTMLElement>());
        }
        public HTMLParagraphElement(HTMLElement parent, String name, AttributeSet attributes, List<HTMLElement> children) {
            super(parent, name, attributes, children);
        }
        public void setParagraphAttributes(AttributeSet att, boolean replace) {
            if(replace) {attributes.removeAttributes(att);}
            attributes.addAttributes(att);
        }
    }
    
    public class HTMLRootElement extends HTMLElement {
        private final HTMLHeadElement head;
        private final HTMLBodyElement body;
        public HTMLRootElement() {
            super(null, "html", null, new LinkedList<HTMLElement>());
            children.add(head = new HTMLHeadElement(this));
            children.add(body = new HTMLBodyElement(this));
        }
        public HTMLBodyElement body() {return body;}
        @Override
        public int getStartOffset() {return 0;}
        @Override
        public void remove(HTMLElement e) {return;}
        @Override
        public String getText(int offset, int length) {return body.getText(offset, length);}
        @Override
        public void remove(int offset, int length) {body.remove(offset, length);}
        @Override
        public void insertString(int offset, String str, AttributeSet att) {body.insertString(offset, str, att);}
        @Override
        public int getElementOffset(HTMLElement htmlElt) {return -1;}
        @Override
        public int size() {return body.size();}
        @Override
        public int getEndOffset() {return body.getEndOffset();}
        @Override
        public int getElementIndex(int offset) {return body.getElementIndex(offset);}
        @Override
        public HTMLCharacterElement getCharacterElement(int offset) {return body.getCharacterElement(offset);}
    }
    
    public class HTMLHeadElement extends HTMLElement {
        public HTMLHeadElement(HTMLRootElement parent) {
            super(parent, "head", null, null);
        }
        
        @Override
        public int getStartOffset() {return 0;}
        @Override
        public void remove(HTMLElement e) {return;}
        @Override
        public int getElementOffset(HTMLElement htmlElt) {return -1;}
        @Override
        public int size() {return 0;}
        @Override
        public int getEndOffset() {return 0;}
        @Override
        public int getElementIndex(int offset) {return -1;}
        @Override
        public HTMLCharacterElement getCharacterElement(int offset) {return null;}
    }
    public class HTMLBodyElement extends HTMLElement {
        public HTMLBodyElement(HTMLRootElement parent) {
            super(parent, "body", null, new LinkedList<HTMLElement>());
            children.add(new HTMLParagraphElement(this, "p", attributes, new LinkedList<HTMLElement>()));
        }
        
        @Override
        public int getStartOffset() {return 0;}
        @Override
        public void remove(HTMLElement e) {
            children.remove(e);
            if(children.isEmpty()) {//Le body ne doit pas être vide
                HTMLElement newP = addNewParagraph();
                for(HTMLPosition pos : positionMap.values()) {
                    if(pos.elt==e) {pos.elt=newP;}
                }
            } else {
                for(HTMLPosition pos : positionMap.values()) {
                    if(pos.elt==e) {pos.toParent();}
                }
            }
        }
        
        public HTMLParagraphElement addNewParagraph() {
            HTMLParagraphElement e = new HTMLParagraphElement(this, "p", attributes, new LinkedList<HTMLElement>());
            children.add(e);
            return e;
        }
    }
}
