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
public class DialogueMathIndice extends DialogueMath{
    
    private static final String VALEUR = "valeur";
    private static final String INDICE = "indice";

    public DialogueMathIndice(JMathTextPane texteParent, String valeurInitiale, String indiceInitial){
        this(texteParent);
        setInputInitialValue(VALEUR, valeurInitiale);
        setInputInitialValue(INDICE, indiceInitial);
    }
    public DialogueMathIndice(JMathTextPane texteParent){
        super("dialog index", texteParent);
    }

    @Override
    protected String createMathMLString() {
        JMathTextPane valeur = getInput(VALEUR);
        JMathTextPane indice = getInput(INDICE);
        return "<math><msub><mrow>" + writeMathMLString(valeur) + "</mrow><mrow>"+writeMathMLString(indice)+"</mrow></msub></math>";
    }

    @Override
    protected String getMasterTag() {
        return "msub";
    }
    
    @Override
    protected JPanel getCenterPane() {
        return new PanelIndice();
    }

    private class PanelIndice extends MathPanel {

        private static final int ESPACE_DISTANCE_INDICE = 10;//Espace vertical entre le champ et l'indice

        private final JLimitedMathTextPane champ;
        private final JLimitedMathTextPane indice;
        
        @Override
        protected void dessiner(Graphics2D g2D) {}//Rien à dessiner

        private final class Layout extends MathLayout {
            @Override
            public void layoutContainer(Container parent) {
                parent.setSize(preferredLayoutSize(parent));
                
                int largeur = parent.getWidth(), hauteur = parent.getHeight();
                int lObjects = champ.getWidth() + indice.getWidth();
                int hObjects = champ.getHeight()+ indice.getHeight()+ESPACE_DISTANCE_INDICE;
                int x = (int) ((largeur-lObjects)/2.0);
                int y = (int) ((hauteur-hObjects)/2.0);
                champ.setLocation(x, y);
                indice.setLocation(x+champ.getWidth(), y+champ.getHeight()+ESPACE_DISTANCE_INDICE);
            }
            @Override
            public Dimension preferredLayoutSize(Container parent) {
                int lObjects = champ.getWidth() + indice.getWidth();
                int hObjects = champ.getHeight()+ indice.getHeight()+ESPACE_DISTANCE_INDICE;
                return new DimensionTools.DimensionT(lObjects,hObjects).plus(2*MARGIN, 2*MARGIN).max(minimumLayoutSize(parent));
            }
        }
        
        private PanelIndice(){
            this.setLayout(new Layout());
            
            champ = new JMathTextField(VALEUR);
            champ.setSize(champ.getMinimumSize());

            indice = new JMathTextField(INDICE);
            indice.setFontSize(15);
            indice.setSize(indice.getMinimumSize());

            this.add(champ, VALEUR);
            this.add(indice, INDICE);
        }

    }

}