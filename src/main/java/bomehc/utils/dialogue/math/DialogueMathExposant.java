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
public class DialogueMathExposant extends DialogueMath{
    
    private static final String MANTICE = "mantice";//TODO Vérifier l'orthographe ^^
    private static final String EXPOSANT = "exposant";

    public DialogueMathExposant(JMathTextPane texteParent, String initialManticeValue, String initialExposantValue){
        this(texteParent);
        setInputInitialValue(MANTICE, initialManticeValue);
        setInputInitialValue(EXPOSANT, initialExposantValue);
    }
    public DialogueMathExposant(JMathTextPane texteParent){
        super("dialog power", texteParent);
    }

    @Override
    protected String createMathMLString() {
        JMathTextPane mantice = getInput(MANTICE);
        JMathTextPane exposant = getInput(EXPOSANT);
        return "<math><msup><mrow>" + writeMathMLString(mantice) + "</mrow><mrow>"+writeMathMLString(exposant)+"</mrow></msup></math>";
    }

    @Override
    protected JPanel getCenterPane() {
        return new PanelExposant();
    }

    @Override
    protected String getMasterTag() {
        return "msup";
    }

    private class PanelExposant extends MathPanel {

        private static final int ESPACE_DISTANCE_EXPOSANT = 10;//Espace vertical entre le champ et l'exposant

        private final JLimitedMathTextPane champ;
        private final JLimitedMathTextPane exposant;

        @Override
        protected void dessiner(Graphics2D g2D) {}//Rien à dessiner

        private final class Layout extends MathLayout {
            @Override
            public void layoutContainer(Container parent) {
                parent.setSize(preferredLayoutSize(parent));
                
                int largeur = parent.getWidth(), hauteur = parent.getHeight();
                int lObjects = champ.getWidth() + exposant.getWidth();
                int hObjects = champ.getHeight()+ exposant.getHeight()+ESPACE_DISTANCE_EXPOSANT;
                int x = (int) ((largeur-lObjects)/2.0);
                int y = (int) ((hauteur-hObjects)/2.0);
                champ.setLocation(x, y+exposant.getHeight()+ESPACE_DISTANCE_EXPOSANT);
                exposant.setLocation(x+champ.getWidth(), y);
            }
            @Override
            public Dimension preferredLayoutSize(Container parent) {
                int lObjects = champ.getWidth() + exposant.getWidth();
                int hObjects = champ.getHeight()+ exposant.getHeight()+ESPACE_DISTANCE_EXPOSANT;
                return new DimensionTools.DimensionT(lObjects,hObjects).plus(2*MARGIN, 2*MARGIN).max(minimumLayoutSize(parent));
            }
        }
        
        private PanelExposant(){
            this.setLayout(new Layout());
            
            champ = new JMathTextField(MANTICE);
            champ.setSize(champ.getMinimumSize());
            
            exposant = new JMathTextField(EXPOSANT);
            exposant.setFontSize(15);
            exposant.setSize(exposant.getMinimumSize());

            this.add(champ,MANTICE);
            this.add(exposant,EXPOSANT);
            champ.requestFocus();
        }

    }

}

