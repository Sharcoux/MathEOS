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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.swing.JPanel;

import bomehc.utils.texte.JLimitedMathTextPane;
import bomehc.utils.texte.JMathTextPane;

import static bomehc.utils.dialogue.math.DialogueMath.MathLayout.MARGIN;
import bomehc.utils.librairies.DimensionTools;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class DialogueMathParentheseLarge extends DialogueMath{

    private static final String CONTENU = "contenu";
    
    public DialogueMathParentheseLarge(JMathTextPane texteParent, String initialValue){
        this(texteParent);
        setInputInitialValue(CONTENU, initialValue);
    }
    public DialogueMathParentheseLarge(JMathTextPane texteParent){
        super("dialog large brace", texteParent);
    }

    @Override
    protected String createMathMLString() {
        JMathTextPane text = getInput(CONTENU);
        return "<math><mfenced><mrow>" + writeMathMLString(text) + "</mrow></mfenced></math>";
    }

    @Override
    protected String getMasterTag() {
        return "mfenced";
    }
    
    @Override
    protected JPanel getCenterPane() {
        return new PanelParentheseLarge();
    }

    private class PanelParentheseLarge extends MathPanel {

        /** 
         * Espace autour du champ entouré par la parenthèse.
         * Càd marge à gauche, à droite, et hauteur supplémentaire de l parenthèse en haut et en bas
         **/
        private static final int DEPASSEMENT_PARENTHESE = 5;
        private static final int LARGEUR_PARENTHESE = 10;//Largeur utilisée pour dessiner le parenthèse. Ceci influence directement sa courbure

        private final JLimitedMathTextPane champ;
    
        private final class Layout extends MathLayout {
            @Override
            public void layoutContainer(Container parent) {
                parent.setSize(preferredLayoutSize(parent));
                
                int largeur = parent.getWidth(), hauteur = parent.getHeight();
                int x = (int) ((largeur-champ.getWidth())/2.0);
                int y = (int) ((hauteur-champ.getHeight())/2.0);
                champ.setLocation(x,y);
            }
            @Override
            public Dimension preferredLayoutSize(Container parent) {
                return new DimensionTools.DimensionT(champ.getPreferredSize()).plus(2*MARGIN+2*LARGEUR_PARENTHESE+2*DEPASSEMENT_PARENTHESE, 2*MARGIN+2*DEPASSEMENT_PARENTHESE).max(minimumLayoutSize(parent));
            }
        }
        
        private PanelParentheseLarge(){
            this.setLayout(new Layout());
            
            champ = new JMathTextField(CONTENU);
            champ.setSize(champ.getMinimumSize());
            champ.setAlignmentCenter(true);

            this.add(champ, CONTENU);
        }

        @Override
        protected void dessiner(Graphics2D g2D) {
            int largeur = getWidth(), hauteur = getHeight();
            int x = (int) ((largeur-champ.getWidth())/2.0);
            int y = (int) ((hauteur-champ.getHeight())/2.0);
            g2D.drawArc(x-LARGEUR_PARENTHESE-DEPASSEMENT_PARENTHESE, y-DEPASSEMENT_PARENTHESE, LARGEUR_PARENTHESE, champ.getHeight()+2*DEPASSEMENT_PARENTHESE, 100, 160);
            g2D.drawArc(largeur-x+DEPASSEMENT_PARENTHESE, y-DEPASSEMENT_PARENTHESE, LARGEUR_PARENTHESE, champ.getHeight()+2*DEPASSEMENT_PARENTHESE, -80, 160);
        }

    }
}