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

import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.io.StringReader;
import javax.swing.SwingUtilities;
import matheos.texte.Editeur;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author François Billioud
 */
public abstract class SVGComponent extends SVGPanel implements ComposantTexte.Image {
    
    private Element svg;
    private long id = System.currentTimeMillis();// L'id unique du JLabelImage permettant de l'identifier
    private boolean selected = false;
    private int largeurMax = Integer.MAX_VALUE;
    private int largeurInitiale;
    private double coef;
    
    public SVGComponent(String svg, int largeur, int hauteur) {
        setSVGString(svg);
        
        this.coef = hauteur/(double)largeur;
        this.largeurInitiale = largeur;
        setSize(largeur, hauteur);
    }
    
    private String correctionSvg(String svgToCorrect) {
        String correctedSVG = svgToCorrect.replaceAll("&times;", "&#x000d7;");//JMathComponent ne lit pas le HTML
        correctedSVG = correctedSVG.replaceAll("&divide;", "&#x000f7;");//JMathComponent ne lit pas le HTML
        correctedSVG = correctedSVG.replaceAll("&plusmn;", "&#177;");//JMathComponent ne lit pas le HTML
        correctedSVG = correctedSVG.replaceAll("xml:space=\"preserve\"", "xml:space=\"default\"");//JMathComponent ne lit pas les \n (JMathComponent c'est un peu de la merde...)
//        svg = svg.replaceAll("<img preserveaspectratio", "<image preserveaspectratio");//HACK : bug 364 JSoup. A supprimer après release 1.7.4
        correctedSVG = correctedSVG.replaceAll("viewbox", "viewBox");//HACK : SVGSalamander ne comprend pas viewbox
        return correctedSVG;
    }
    
    @Override
    public int getLargeurMax() {
        Editeur editeur = (Editeur) SwingUtilities.getAncestorOfClass(Editeur.class, this);
        return Math.min(largeurMax, (editeur!=null && editeur.getWidth()!=0) ? editeur.getWidth() : Integer.MAX_VALUE);
    }
    public void setLargeurMax(int largeur) {largeurMax = largeur;}
    @Override
    public Dimension getMaximumSize() {int l = largeurInitiale/*getLargeurMax()*/;return new Dimension(l, (int) (l*coef));}
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(largeurInitiale, (int) (largeurInitiale*coef));
    }

    @Override
    public void setId(long id) {this.id = id;}
    @Override
    public long getId() {return id;}
    
    public String getSVGString() {return correctionSvg(svg.outerHtml());}
    public Element getSVG() {return svg;}
    public final void setSVGString(String svg) {
        Document d = Jsoup.parse(svg);d.outputSettings(new Document.OutputSettings().prettyPrint(false));
        setSVG(d.select("svg").first());
    }
    public final void setSVG(Element svg) {
        this.svg = svg;
        this.coef = Double.parseDouble(svg.attr("height"))/Double.parseDouble(svg.attr("width"));//le coef a pu changer
        SVGUniverse uni = new SVGUniverse();
        StringReader r = new StringReader(getSVGString());
        uni.loadSVG(r,id+".svg");
        super.setSvgUniverse(uni);
        super.setSvgURI(uni.getStreamBuiltURI(id+".svg"));
        super.setScaleToFit(true);
        super.setAntiAlias(true);
        setSize(largeurInitiale);
        repaint();
    }
    public void updateSVG() {
        setSVG(svg);
    }

    private Color couleurSelection = Color.DARK_GRAY;
    public void setCouleurSelection(Color couleurSelection) {
        this.couleurSelection = couleurSelection;
    }

    @Override
    public boolean isSelected() {return selected;}
    
    @Override
    public void setSelected(boolean b) {
        if(selected==b) {return;}
        selected = b;
        this.setBackground(b ? couleurSelection : Color.WHITE);
        setOpaque(b);
        this.repaint();
    }
    
    @Override
    public void setEnabled(boolean b) {
        setOpaque(b ? isSelected() : b);
        super.setEnabled(b);
    }

    @Override
    public void setSize(int largeur) {
        largeurInitiale = largeur;
        int l = Math.min(getLargeurMax(), largeur);
        this.setSize(largeur, (int) (largeur*coef));
//        svg = getHTMLRepresentation();//Pour garder une cohérence entre le svg réel et celui de la variable
    }
    @Override
    public void setFontSize(float size) {
        setSize(Math.round(size/getFont().getSize2D()*getHeight()));
        setFont(getFont().deriveFont(size));
    }

    @Override
    public float getFontSize() {
        return getFont().getSize2D();
    }
}

