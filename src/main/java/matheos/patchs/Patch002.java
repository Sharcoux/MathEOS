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

package matheos.patchs;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import matheos.sauvegarde.DataProfil;
import matheos.sauvegarde.DataTexte;
import matheos.texte.composants.JLabelTP;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Ce patch convertit les anciennes images de TP en SVG
 * @author François Billioud
 */
public class Patch002 extends Patch {
    
    @Override
    protected int getLastSupportedVersion() {
        //retourne la plus vieille version n'ayant pas besoin de ce patch.
        return 5;
    }

    @Override
    protected boolean apply(DataProfil profil) {
        List<DataTexte> dataTextes = getAllElements(DataTexte.class, profil);
        for(DataTexte dataTexte : dataTextes) {
            org.jsoup.nodes.Document doc = Jsoup.parse(dataTexte.getContenuHTML());
            Elements spanTP = doc.select("span."+JLabelTP.JLABEL_TP);
            for(Element spanElt : spanTP) {
                String spanID = spanElt.attr("id");
                BufferedImage oldImage = dataTexte.putImage(spanID, null);
                int width = oldImage.getWidth(), height = oldImage.getHeight();
                
                // Get a DOMImplementation.
                DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

                // Create an instance of org.w3c.dom.Document.
                String svgNS = "http://www.w3.org/2000/svg";
                Document document = domImpl.createDocument(svgNS, "svg", null);

                // Create an instance of the SVG Generator.
                SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
                svgGenerator.setSVGCanvasSize(new Dimension(width, height));
                svgGenerator.drawImage(oldImage, 0, 0, null);
                
                Element svgElement;
                try (StringWriter w = new StringWriter()) {
                    svgGenerator.stream(w,true);
                    svgElement = Jsoup.parse(w.toString()).outputSettings(new OutputSettings().prettyPrint(false)).select("svg").first();
                } catch (IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    svgElement = Jsoup.parse("<svg></svg>").select("svg").first();
                }
                Element imgElt = spanElt.select("img").first();
                String id = imgElt.attr("id"), title = imgElt.attr("title");
                svgElement.attr("id", id).attr("width",width+"").attr("height",height+"").attr("title", title);
                String svg = svgElement.outerHtml();
                spanElt.html(svg);
            }
            dataTexte.setContenuHTML(doc.html());
        }
        return true;
    }

    @Override
    protected Patch previous() {
        //retourne le précédent patch qui doit être appliqué avant celui-ci. null si aucun patch nécessaire
        return new Patch001();
    }


}
