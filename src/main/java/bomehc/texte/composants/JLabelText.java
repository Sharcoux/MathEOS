/** «Copyright 2012,2013 François Billioud»
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
 */

package bomehc.texte.composants;

import bomehc.texte.Editeur;
import bomehc.utils.librairies.JsoupTools;
import bomehc.utils.managers.ColorManager;
import bomehc.utils.managers.Traducteur;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import static bomehc.utils.texte.EditeurKit.STRIKE_COLOR_ATTRIBUTE;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class JLabelText extends JLabel implements ComposantTexte {

    /** Constante permettant d'identifier un JLabelTP **/
    public static final String JLABEL_TEXTE = "labelText";
    private static final String EDITABLE_PROPERTY = "editable";
    
    private org.jsoup.nodes.Element span;
//    private org.jsoup.nodes.Element htmlRepresentation;
    private long id = System.currentTimeMillis();
    
    private boolean selected = false;
    private boolean stroken = false;
    private boolean underlineMemory;//petit hack car rayer le titre concurrence le soulignement
    private Color strikeColor = Color.BLACK;
    public void setStroken(boolean b) {
        if(stroken==b) {return;}
        stroken=b;
        if(b) {
            HashMap<String, String> styles = new HashMap<>();
            styles.put("text-decoration", "line-through");
            styles.put(STRIKE_COLOR_ATTRIBUTE, ColorManager.getRGBHexa(strikeColor));
            JsoupTools.addStyles(span, styles);
        } else {
            JsoupTools.setStyleAttribute(span, "text-decoration", underlineMemory ? "underline" : null);
            JsoupTools.setStyleAttribute(span, STRIKE_COLOR_ATTRIBUTE, null);
        }
        updateContent();
    }
    public boolean isStroken() {return stroken;}
    public void setStrikeColor(Color c) {strikeColor = c;}
    public Color getStrikeColor() {return strikeColor;}
    
    /** permet de dissocier la taille d'affichage de la taille du modèle **/
    private float displayedFontSize = 14;
    
    /** définit si l'on peut éditer l'élément par clic droit. vrai par défaut **/
    public void setEditable(boolean b) {
        span.attr(EDITABLE_PROPERTY,b+"");
    }
    
    /** définit si l'on peut supprimer cet élément. vrai par défaut **/
    public void setRemovable(boolean b) {
        span.attr(REMOVABLE_PROPERTY,b+"");
    }
    
    /** renvoie si l'on peut éditer l'élément par clic droit. vrai par défaut **/
    public boolean isEditable() {return !span.hasAttr(EDITABLE_PROPERTY) || span.attr(EDITABLE_PROPERTY).equals("true");}
    /** renvoie si l'on peut supprimer cet élément. vrai par défaut **/
    public boolean isRemovable() {return !span.hasAttr(REMOVABLE_PROPERTY) || span.attr(REMOVABLE_PROPERTY).equals("true");}

    private JLabelText(Element span) {
        this.span = span;
        span.ownerDocument().outputSettings().prettyPrint(false);//escapeMode(Entities.EscapeMode.xhtml);//pour que "é" ne devienne pas "&ecute;"
        underlineMemory = JsoupTools.getStyle(span, "text-decoration").equals("underline");
        
        String idString = span.attr("id");
        if(!idString.isEmpty()) {
            try {
                id = Long.parseLong(span.attr("id"));
            } catch (NumberFormatException ex) {System.out.println("id du JLabelText non trouvé : "+span.outerHtml());}
        }
        //initialize JLabel
        initializeJLabel();
        updateContent();
    }
    
    public JLabelText(String contenuHTML) {
        this(Jsoup.parse(contenuHTML).select("span").first());
    }
    
    public JLabelText(String contenu, int taille, Color couleur, boolean souligne, boolean gras) {
        Document doc = Jsoup.parse("<span id='"+id+"'>"+contenu+"</span>");
        span = doc.getElementById(""+id);
        span.ownerDocument().outputSettings().prettyPrint(false);//escapeMode(Entities.EscapeMode.xhtml);//pour que "é" ne devienne pas "&ecute;"
        underlineMemory = souligne;
        
        //Styles CSS
        Map<String, String> styles = new HashMap<>();
        if(taille!=0) { styles.put("font-size", taille+"pt"); }
        if(couleur!=null) { styles.put("color", JsoupTools.colorToHTMLString(couleur)); }
        if(souligne) { styles.put("text-decoration", "underline"); }
        if(gras) { styles.put("font-weight", "bold"); }
        JsoupTools.setStyle(span, styles);
        
        //Styles HTML
//        if(souligne) { content.wrap("<u></u>"); }
//        if(gras) { content.wrap("<b></b>"); }
//        Element font = Jsoup.parse("<font></font>").body().child(0);
//        if(taille!=0) {font.attr("size",taille+"");}
//        if(couleur!=null) {font.attr("color",JsoupTools.colorToHTMLString(couleur));}
//        if(taille!=0 || couleur!=null) {doc.body().child(0).wrap(font.outerHtml());}
        
        //initialize JLabel
        initializeJLabel();
        updateContent();
    }
    
    private String HTML5toHTML3() {
//        String html = span.outerHtml();
//        Element body = Jsoup.parse(html).body();
//        
//        float taille = (float) (JsoupTools.getFontSize(span)*displayedFontSize/14.0);
//        String couleur = JsoupTools.getStyle(span, "color");
//        
//        if(taille!=0) {
//            JsoupTools.setStyleAttribute(body.select("span").first(),"font-size",null);
//            body.attr("size", taille+"");
//        }
//        if(couleur!=null) {
//            JsoupTools.setStyleAttribute(body.select("span").first(),"color",null);
//            body.attr("color", couleur+"");
//        }
//        
//        if(taille!=0 || couleur!=null) {
//            body.tagName("font");
//            html = body.outerHtml();
//        }
        Element copy = span.clone();
        float taille = (float) (JsoupTools.getFontSize(span)*displayedFontSize/14.0);
        JsoupTools.setStyleAttribute(copy, "font-size", Math.round(taille)+"pt");
        String html = copy.outerHtml();
        if(JsoupTools.getStyle(span,"text-decoration").equals("underline")) { html = "<u>"+html+"</u>"; }
        if(JsoupTools.getStyle(span,"font-weight").equals("bold")) { html="<b>"+html+"</b>"; }
        System.out.println(html);
        return "<html>"+html+"</html>";
    }
    
    @Override
    public float getFontSize() {
        return displayedFontSize;
//        return JsoupTools.getFontSize(span);
//        return getFont().getSize2D();
    }
    
    @Override
    public void setFontSize(float fontSize) {
        this.displayedFontSize = fontSize;
//        JsoupTools.setStyleAttribute(span, "font-size", Math.round(fontSize)+"");
//        setFont(getFont().deriveFont(fontSize));
        updateContent();
    }
    
    private void initializeJLabel() {
//        setFont(new Font("Arial", JsoupTools.getStyle(span,"font-weight").equals("bold") ? Font.BOLD : Font.PLAIN, (int)getFontSize()));
        setFont(new Font("Arial", JsoupTools.getStyle(span,"font-weight").equals("bold") ? Font.BOLD : Font.PLAIN, 14));
//        setOpaque(true);
//        setBackground(ColorManager.transparent());
    }
    
    public JLabelText copy() {return new JLabelText(getHTMLRepresentation(SVG_RENDERING.SVG, true));}
    
    public String getHTMLRepresentation(SVG_RENDERING svgAllowed, boolean mathMLAllowed) {
        return span.outerHtml();
    }

    public long getId() {return id;}

    public static JLabelText creerJLabelTextFromHTML(String html) {
        return new JLabelText(html);
    }

    public void setId(long newId) {
        this.id = newId;
    }
    
    @Override
    public boolean isSelected() {return selected;}
    
    @Override
    public void setSelected(boolean b) {
        if(selected==b) {return;}
        selected = b;
        setOpaque(b);
        setBackground(b ? couleurSelection : Color.WHITE);
        super.setForeground(b ? ColorManager.get("color focused") : Color.BLACK);
        repaint();
    }
    
    @Override
    public Color getForeground() {
        if(span!=null) { return JsoupTools.getColor(span); }
        return super.getForeground();
    }
    @Override
    public void setForeground(Color couleur) {
        super.setForeground(couleur);
        if(span!=null) {
            JsoupTools.setStyleAttribute(span, "color", ColorManager.getRGBHexa(couleur));
            updateContent();
        }
    }
    
    public final void updateContent() {
        super.setText(HTML5toHTML3());
//        float fontSize = JsoupTools.getFontSize(span)*getFont().getSize2D()/12f;
//        float fontSize = JsoupTools.getFontSize(span);
//        FontMetrics fm = getFontMetrics(getFont().deriveFont(fontSize));
//        Dimension d = new Dimension(fm.stringWidth(span.html())+5,fm.getHeight()+5);
        System.out.println(getPreferredSize());
        Dimension d = getPreferredSize();
//        setPreferredSize(d);
        setMaximumSize(d);
        setMinimumSize(d);
        setSize(d);
        repaint();
    }
    
    public static final String CONTENT_PROPERTY = "content";
    public final void setContent(String content) {
        if(span.html().equals(content)) {return;}
        firePropertyChange(CONTENT_PROPERTY, span.html(), content);
        span.html(content);
        updateContent();
    }
    
    public String getContent() {
        return span.text();
    }

    private Color couleurSelection = ColorManager.get("color disabled");
    public void setSelectionColor(Color selectionColor) {
        couleurSelection = selectionColor;
    }

    public static class LabelTextMouseListener extends MouseAdapter {
        protected Editeur editeur;
        public LabelTextMouseListener(Editeur editeur) {this.editeur = editeur;}
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getComponent() instanceof JLabelText) {
                JLabelText text = (JLabelText) e.getComponent();
                if(editeur!=null) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        //On sélectionne le JLabel
                        javax.swing.text.Element element = editeur.getHTMLdoc().getElement(Editeur.getSpanId(text.getId()));
                        editeur.select(element.getStartOffset(),element.getEndOffset());
                        text.setSelected(true);
                    }
                    if (SwingUtilities.isRightMouseButton(e)) {
                        if(!text.isEditable()) {return;}
                        //On sélectionne le JLabel
                        javax.swing.text.Element element = editeur.getHTMLdoc().getElement(Editeur.getSpanId(text.getId()));
                        editeur.select(element.getStartOffset(),element.getEndOffset());
                        //On propose un changement de nom
                        String s = (String) JOptionPane.showInputDialog(editeur, Traducteur.traduire("dialog rename message"), Traducteur.traduire("dialog rename title"), JOptionPane.QUESTION_MESSAGE, null, null, text.getContent());
                        if(s!=null && !s.isEmpty()) {text.setContent(s);}
                    }
                }
            }
        }
    }
    
    public static class LabelChangeEdit extends AbstractUndoableEdit {
        private final String oldContent;
        private final String newContent;
        private final JLabelText source;
        public LabelChangeEdit(JLabelText source, String oldContent, String newContent) {
            this.source = source;
            this.oldContent = oldContent;
            this.newContent = newContent;
        }
        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            source.span.html(oldContent);
            source.updateContent();
        }
        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            source.span.html(newContent);
            source.updateContent();
        }
    }
}
