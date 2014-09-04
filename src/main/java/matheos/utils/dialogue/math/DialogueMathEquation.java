/** «Copyright 2011,2014 François Billioud, Guillaume Varoquaux»
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

import java.awt.Dimension;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.texte.JMathTextPane;
import java.awt.event.ComponentAdapter;

/**
 *
 * @author François Billioud, Guillaume Varoquaux
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
        return "<math><mrow>" + createMathMLString(text) + "</mrow></math>";
    }

    @Override
    protected JPanel getCenterPane() {
        return new PanelEquation();
    }

    @Override
    protected String getMasterTag() {
        return "mrow";
    }

    private class PanelEquation extends JPanel {

        private static final int PREFERRED_WIDTH = 300;
        private static final int PREFERRED_HEIGHT = 100;
        private static final int SIDE_MARGIN = 120; // Marge gauche + droite pour le JComboBox
        private static final int TOP_BOTTOM_MARGIN = 10; // Marge en haut et en bas

        private final JLimitedMathTextPane text;

        private PanelEquation(){
            super();
            
            text = new JMathTextField(EQUATION);
            
            this.setSize(PREFERRED_WIDTH,PREFERRED_HEIGHT);
            this.setLayout(null);
//            text.setMinimumSize(new Dimension(100,30));
//            text.dimensionner();
            text.setSize(text.getMinimumSize());
            text.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    positionComponent();
                }
            });

            positionComponent();
            this.add(text);
            repaint();
        }

        private void positionComponent(){
            int largeur = PREFERRED_WIDTH;
            int hauteur = PREFERRED_HEIGHT;
            largeur = Math.max(text.getWidth() + SIDE_MARGIN, PREFERRED_WIDTH);

            hauteur = Math.max(text.getHeight() + TOP_BOTTOM_MARGIN*2, PREFERRED_HEIGHT);

            this.setSize(largeur, hauteur);
            DialogueMathEquation.this.setSize(this.getWidth()+6, this.getHeight()+100);

            text.setLocation(largeur/2 - text.getWidth()/2, hauteur/2 - text.getHeight()/2);
        }

    }

}


