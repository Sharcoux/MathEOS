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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.InlineView;
import matheos.utils.managers.ColorManager;
import static matheos.utils.texte.EditeurKit.STRIKE_COLOR_ATTRIBUTE;

/**
 *
 * @author François Billioud
 */
public class HTMLBetterFactory extends HTMLEditorKit.HTMLFactory {
    @Override
    public View create(Element elem) {
        AttributeSet attrs = elem.getAttributes();
        Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
        Object o = (elementName != null) ? null : attrs.getAttribute(StyleConstants.NameAttribute);
        if (o == HTML.Tag.CONTENT) {
            return new StrikeView(elem);
        }
        return super.create(elem);
    }
    
    private class StrikeView extends InlineView {
        public StrikeView(Element elem) { super(elem); }
        
        public void paint(Graphics g, Shape allocation) {
            setStrikeThrough(false);//On va la peindre nous-même
            super.paint(g, allocation);
            Object textDecorationValue = getAttributes().getAttribute(CSS.Attribute.TEXT_DECORATION);
            if(textDecorationValue!=null && textDecorationValue.toString().equals("line-through")) {
                paintStrikeLine(g, allocation);
            }
        }

        public void paintStrikeLine(Graphics g, Shape a) {
            String colorHexa = (String) getElement().getAttributes().getAttribute(STRIKE_COLOR_ATTRIBUTE);
            Color c = colorHexa == null ? Color.BLACK : ColorManager.getColorFromHexa(colorHexa);
//            int y = a.getBounds().y + a.getBounds().height - (int) getGlyphPainter().getDescent(this);

//            y -= (int) (getGlyphPainter().getAscent(this) * 0.3f);
            int y = a.getBounds().y+a.getBounds().height/2;
            int x1 = (int) a.getBounds().getX();
            int x2 = (int) (a.getBounds().getX() + a.getBounds().getWidth());

            Color oldColor = g.getColor(); Stroke oldStroke = ((Graphics2D)g).getStroke();//pour mémoire
            ((Graphics2D)g).setStroke(new BasicStroke(2)); g.setColor(c);//affecte les bons paramètres d'épaisseur et de couleur
            g.drawLine(x1, y, x2, y);//dessine le trait
            g.setColor(oldColor); ((Graphics2D)g).setStroke(oldStroke);//restitue les paramètres d'origine

        }
    }
}
