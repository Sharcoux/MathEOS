/** «Copyright 2011,2014 François Billioud»
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

package matheos.utils.dialogue.math;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.texte.JMathTextPane;
import matheos.utils.librairies.DimensionTools.DimensionT;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class DialogueMathEquation extends DialogueMath{

    private static final String EQUATION = "equation";
    
    public DialogueMathEquation(JMathTextPane texteParent, String initialValue) {
        this(texteParent);
        setInputInitialValue(EQUATION, initialValue);
    }
    public DialogueMathEquation(JMathTextPane texteParent){
        super("dialog equation", texteParent);
    }

    @Override
    protected String createMathMLString() {
        JMathTextPane text = getInput(EQUATION);
        return "<math><mrow>" + writeMathMLString(text) + "</mrow></math>";
    }

    @Override
    protected JPanel getCenterPane() {
        return new PanelEquation();
    }

    @Override
    protected String getMasterTag() {
        return "mrow";
    }

    private class PanelEquation extends MathPanel {

        private final JLimitedMathTextPane text;

        @Override
        protected void dessiner(Graphics2D g2D) {}//Rien à dessiner

        private final class Layout extends MathLayout {
            @Override
            public void layoutContainer(Container parent) {
                parent.setSize(preferredLayoutSize(parent));
                
                int largeur = parent.getWidth(), hauteur = parent.getHeight();
                int x = (int) ((largeur-text.getWidth())/2.0);
                int y = (int) ((hauteur-text.getHeight())/2.0);
                text.setLocation(x,y);
            }
            @Override
            public Dimension preferredLayoutSize(Container parent) {
                return new DimensionT(text.getPreferredSize()).plus(2*MARGIN, 2*MARGIN).max(minimumLayoutSize(parent));
            }
        }
        
        private PanelEquation(){
            setLayout(new Layout());
            
            text = new JMathTextField(EQUATION);
            text.setSize(text.getMinimumSize());
            
            this.add(text,EQUATION);
        }
    }

}


