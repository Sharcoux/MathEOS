/** «Copyright 2011,2014 François Billioud»
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

package bomehc.utils.dialogue.math;

import bomehc.utils.dialogue.DialogueBloquant;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import bomehc.utils.texte.JLimitedMathTextPane;
import bomehc.utils.managers.Traducteur;
import bomehc.utils.texte.JMathTextPane;
import bomehc.utils.librairies.DimensionTools.DimensionT;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class DialogueMathChapeauAngle extends DialogueMath {

    public static final String ANGLE_ID = "angle";
    
    public DialogueMathChapeauAngle(JMathTextPane texteParent, String angle) {
        this(texteParent);
        setInputInitialValue(ANGLE_ID, angle);
    }
    public DialogueMathChapeauAngle(JMathTextPane texteParent) {
        super(Traducteur.traduire("dialog hat"), texteParent);
    }

    @Override
    protected String createMathMLString() {
        JMathTextPane textMath = getInput(ANGLE_ID);
        return "<math><mover><mrow><mn>" + textMath.getText() + "</mn></mrow><mn>^</mn></mover></math>";
    }

    @Override
    protected JPanel getCenterPane() {
        return new PanelChapeauAngle();
    }

    @Override
    protected boolean verifications() {
        if(!super.verifications()) {return false;}// test les paramètres communs aux DialogueMath (
        JMathTextPane textPane = getInput(ANGLE_ID);
        String angle = textPane.getText();
        if(angle.length()!=1 && angle.length()!=3) {
            DialogueBloquant.error("dialog angle arguments");
            textPane.requestFocusInWindow();
            return false;
        }
        return true;
    }
    @Override
    protected String getMasterTag() {
        return "mover";
    }

    private class PanelChapeauAngle extends MathPanel {

        private static final int HAUTEUR_CHAPEAU = 10;//hauteur du chapeau
        private static final int GAP_CHAPEAU = 5;//espace entre le champ et le chapeau

        private final JLimitedMathTextPane text;
        
        private final class Layout extends MathLayout {
            @Override
            public void layoutContainer(Container parent) {
                int largeur = parent.getWidth(), hauteur = parent.getHeight();
                int x = (int) ((largeur-text.getWidth())/2.0);
                int y = (int) (hauteur/2.0);
                text.setLocation(x,y);
            }
            @Override
            public Dimension preferredLayoutSize(Container parent) {
                return new DimensionT(text.getPreferredSize()).plus(2*MARGIN,2*MARGIN+HAUTEUR_CHAPEAU+GAP_CHAPEAU).max(minimumLayoutSize(parent));
            }
        }
    
        private PanelChapeauAngle() {
            setLayout(new Layout());
            
            text = new JMathTextField(ANGLE_ID);
            text.setLongueurMax(3);
            text.setSize(text.getMinimumSize());
            text.setAlignmentCenter(true);

            this.add(text,ANGLE_ID);
        }

        @Override
        public void dessiner(Graphics2D g2D) {
            int largeur = getWidth(), hauteur = getHeight();
            int x = (int) ((largeur-text.getWidth())/2.0);
            int y = (int) (hauteur/2.0);
            int xMilieu = x+text.getWidth()/2;
            int xFin = x+text.getWidth();

            g2D.drawLine(x, y-GAP_CHAPEAU, xMilieu, y-GAP_CHAPEAU-HAUTEUR_CHAPEAU);
            g2D.drawLine(xMilieu, y-GAP_CHAPEAU-HAUTEUR_CHAPEAU, xFin, y-GAP_CHAPEAU);
        }

    }
}
