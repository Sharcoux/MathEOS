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
import static matheos.utils.dialogue.math.DialogueMath.MathLayout.MARGIN;
import matheos.utils.librairies.DimensionTools;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class DialogueMathFraction extends DialogueMath{

    private static final String NUMERATEUR = "numerateur";
    private static final String DENOMINATEUR = "denominateur";
    
    public DialogueMathFraction(JMathTextPane texteParent, String numerateur, String denominateur){
        this(texteParent);
        setInputInitialValue(NUMERATEUR, numerateur);
        setInputInitialValue(DENOMINATEUR, denominateur);
    }
    public DialogueMathFraction(JMathTextPane texteParent){
        super("dialog fraction", texteParent);
    }

    @Override
    protected String createMathMLString() {
        JMathTextPane numerateur = getInput(NUMERATEUR);
        JMathTextPane denominateur = getInput(DENOMINATEUR);
        return "<math><mfrac>"+createRowString(numerateur)+createRowString(denominateur) + "</mfrac></math>";
    }
    
    private String createRowString(JMathTextPane textPane) {
        String content = writeMathMLString(textPane);
        return "<mrow>"+content+"</mrow>";
    }

    @Override
    protected JPanel getCenterPane() {
        return new PanelFraction();
    }

    @Override
    protected String getMasterTag() {
        return "mfrac";
    }

    private class PanelFraction extends MathPanel {

        private static final int ECART_LIGNE = 6;//Espace entre un champ et la ligne de séparation

        private final JLimitedMathTextPane numerateur;
        private final JLimitedMathTextPane denominateur;
        
        private final class Layout extends MathLayout {
            @Override
            public void layoutContainer(Container parent) {
                parent.setSize(preferredLayoutSize(parent));
                
                int largeur = parent.getWidth(), hauteur = parent.getHeight();
                int hObjects = numerateur.getHeight()+denominateur.getHeight()+2*ECART_LIGNE;
                int xA = (int) ((largeur-numerateur.getWidth())/2.0);
                int xB = (int) ((largeur-denominateur.getWidth())/2.0);
                int yA = (int) ((hauteur-hObjects)/2.0);
                int yB = hauteur-yA-denominateur.getHeight();
                numerateur.setLocation(xA, yA);
                denominateur.setLocation(xB, yB);
            }
            @Override
            public Dimension preferredLayoutSize(Container parent) {
                int lObjects = Math.max(numerateur.getWidth(), denominateur.getWidth());
                int hObjects = numerateur.getHeight()+denominateur.getHeight()+2*ECART_LIGNE;
                return new DimensionTools.DimensionT(lObjects,hObjects).plus(2*MARGIN, 2*MARGIN).max(minimumLayoutSize(parent));
            }
        }
        
        private PanelFraction(){
            this.setLayout(new Layout());
            
            numerateur = new JMathTextField(NUMERATEUR);
            numerateur.setSize(numerateur.getMinimumSize());
            numerateur.setAlignmentCenter(true);

            denominateur = new JMathTextField(DENOMINATEUR);
            denominateur.setSize(denominateur.getMinimumSize());
            denominateur.setAlignmentCenter(true);
            
            this.add(numerateur, NUMERATEUR);
            this.add(denominateur, DENOMINATEUR);
        }

        @Override
        protected void dessiner(Graphics2D  g2D){
            int x1 = (getWidth()-Math.max(numerateur.getWidth(), denominateur.getWidth()))/2;
            int x2 = getWidth()-x1;
            int y = denominateur.getY()-ECART_LIGNE;
            g2D.drawLine(x1, y, x2, y);
        }

    }
   
     
}
