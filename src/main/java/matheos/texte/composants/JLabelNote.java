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

package matheos.texte.composants;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import matheos.texte.Editeur;
import matheos.utils.dialogue.DialogueComplet;
import matheos.utils.dialogue.DialogueEvent;
import matheos.utils.dialogue.DialogueListener;
import matheos.utils.librairies.JsoupTools;
import matheos.utils.managers.ColorManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author François Billioud
 */
public class JLabelNote extends SVGComponent implements ComposantTexte.Image {

    /** Constante permettant d'identifier un JLabelNote **/
    public static final String JLABEL_NOTE = "markComponent";
    
    public JLabelNote(String numerateur, String denominateur, int largeur, int hauteur) {
        super("<?xml version=\"1.0\" standalone=\"no\"?>\n" +
"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"\n" +
"\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
"<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" width='"+largeur+"' height='"+hauteur+"' viewBox='0 0 100 100' style=\"stroke:#000000; fill:#000000; font-weight:normal; font-size:40;\">\n" +
"	<line x1=\"0\" x2=\"100\" y1=\"100\" y2=\"0\" style=\"stroke-width:3\" />\n" +
"	<text id=\"numerator\" x=\"0\" y=\"40\">"+numerateur+"</text>\n" +
"	<text id=\"denominator\" x=\"50\" y=\"90\">"+denominateur+"</text>\n" +
"</svg>", largeur, hauteur);
        setValues(numerateur, denominateur);//Pour adapter le positionnement en fonction de la taille des valeurs
    }
    
    public JLabelNote(String svg, int largeur, int hauteur) {
        super(svg, largeur, hauteur);
    }
    
    @Override
    public String getHTMLRepresentation(SVG_RENDERING svgAllowed, boolean mathMLAllowed) {
        getSVG().attr("id", getId() + "").attr("width",getWidth()+"").attr("height",getHeight()+"");
        return super.getHTMLRepresentation(svgAllowed, mathMLAllowed);
    }

    public static JLabelNote creerJLabelNoteFromHTML(String svg) {
        Document d = Jsoup.parse(svg);d.outputSettings(new Document.OutputSettings().prettyPrint(false));
        Element svgElement = d.select("svg").first();
        String id = svgElement.attr("id");
        String widthString = svgElement.attr("width");
        String heightString = svgElement.attr("height");
        int width = widthString.isEmpty() ? 0 : Integer.parseInt(widthString);
        int height = heightString.isEmpty() ? 0 : Integer.parseInt(heightString);
        JLabelNote note = new JLabelNote(svg, width, height);
        if(!id.isEmpty()) {
            try {
                note.setId(Long.parseLong(svgElement.attr("id")));
            } catch (NumberFormatException e) {System.out.println("id du JLabelTP non trouvé : "+svgElement.outerHtml());}
        }
        return note;
    }
    
    public String getNumerateur() {return getSVG().getElementById("numerator").text();}
    public String getDenominateur() {return getSVG().getElementById("denominator").text();}
    public final void setValues(String numerateur, String denominateur) {
        getSVG().getElementById("numerator").html(numerateur);
        getSVG().getElementById("denominator").html(denominateur);
        //Adapte la taille pour aller jusqu'à 3 chiffres
        int numSize = (numerateur).length();
        int denSize = (denominateur).length();
        getSVG().getElementById("numerator").attr("x",(25-numSize*5)+"").attr("style","font-size:"+(45-numSize*5)+";");
        getSVG().getElementById("denominator").attr("x",(55-denSize*5)+"").attr("style","font-size:"+(45-denSize*5)+";");
        updateSVG();
    }

    @Override
    public void setStroken(boolean b) {}

    @Override
    public boolean isStroken() {return false;}

    @Override
    public void setStrikeColor(Color c) {}

    @Override
    public Color getStrikeColor() {return Color.BLACK;}

    @Override
    public void setForeground(Color c) {
        if(getSVG()!=null) {
            JsoupTools.setStyleAttribute(getSVG(), "stroke", c==null?"#000000":ColorManager.getRGBHexa(c));updateSVG();
            JsoupTools.setStyleAttribute(getSVG(), "fill", c==null?"#000000":ColorManager.getRGBHexa(c));updateSVG();
        }
        super.setForeground(c);
    }
    @Override
    public Color getForeground() {return getSVG()==null||JsoupTools.getStyle(getSVG(),"stroke").isEmpty() ? super.getForeground() : ColorManager.getColorFromHexa(JsoupTools.getStyle(getSVG(),"stroke"));}
    
    @Override
    public Object copy() {
        return new JLabelNote(getNumerateur(), getDenominateur(), getWidth(), getHeight());
    }

    public static class NoteListener extends MouseAdapter {
        private final Editeur editeur;
        public NoteListener(Editeur editeur) { this.editeur = editeur; }
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getComponent() instanceof JLabelNote) {
                final JLabelNote note = (JLabelNote)e.getComponent();
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 2) {
                        if(!editeur.isEditable()) {return;}
                        DialogueComplet d = new DialogueComplet("dialog mark scale");
                        d.setInitialValue("numerator", note.getNumerateur());
                        d.setInitialValue("denominator", note.getDenominateur());
                        d.addDialogueListener(new DialogueListener() {
                            @Override
                            public void dialoguePerformed(DialogueEvent event) {
                                if(!event.isConfirmButtonPressed()) {return;}
                                note.setValues(event.getInputString("numerator"),event.getInputString("denominator"));
                            }
                        });
                    } else {
//                        Editeur editeur = (Editeur) SwingUtilities.getAncestorOfClass(Editeur.class, JLabelTP.this);
                        javax.swing.text.Element element = editeur.getHTMLdoc().getElement(Editeur.getSpanId(note.getId()));
                        editeur.select(element.getStartOffset(),element.getEndOffset());
                    }
                } else {
                    if(!editeur.isEditable()) {return;}
                    JDialog dialogueImageTaille = new ComposantTexte.Image.ImageSizeEditor(note);
                }
            }
        }
    }
}
