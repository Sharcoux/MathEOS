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
public class DialogueMathRacineCarree extends DialogueMath{

    private static final String CONTENU = "contenu";
    
    public DialogueMathRacineCarree(JMathTextPane texteParent, String initialValue){
        this(texteParent);
        setInputInitialValue(CONTENU, initialValue);
    }
    public DialogueMathRacineCarree(JMathTextPane texteParent){
        super("dialog square root", texteParent);
    }
    
    @Override
    protected String createMathMLString() {
        JMathTextPane text = getInput(CONTENU);
        return "<math><msqrt><mrow>" + writeMathMLString(text) + "</mrow></msqrt></math>";
    }
    
    @Override
    protected String getMasterTag() {
        return "msqrt";
    }
    
    @Override
    protected JPanel getCenterPane() {
        return new PanelRacineCarree();
    }

     private class PanelRacineCarree extends MathPanel {

        private static final int DEPASSEMENT_RACINE = 5;//espace entre le champ et la racine, en haut et à gauche
        private static final int LARGEUR_V = 10;//largeur utilisée pour dessiner le V de la racine
        private static final int PATTE = 4;//largeur utilisée pour dessiner la patte au début de la racine

        private final JLimitedMathTextPane champ;
        
        private final class Layout extends MathLayout {
            @Override
            public void layoutContainer(Container parent) {
                parent.setSize(preferredLayoutSize(parent));
                
                int largeur = parent.getWidth(), hauteur = parent.getHeight();
                int lRacine = DEPASSEMENT_RACINE+LARGEUR_V+PATTE;
                int lObjects = champ.getWidth()+lRacine+DEPASSEMENT_RACINE, hObjects = champ.getHeight()+2*DEPASSEMENT_RACINE;
                int x = (int) ((largeur-lObjects)/2.0);
                int y = (int) ((hauteur-hObjects)/2.0);
                champ.setLocation(x+lRacine,y+DEPASSEMENT_RACINE);
            }
            @Override
            public Dimension preferredLayoutSize(Container parent) {
                int lRacine = DEPASSEMENT_RACINE+LARGEUR_V+PATTE;
                return new DimensionTools.DimensionT(champ.getPreferredSize()).plus(2*MARGIN+lRacine+DEPASSEMENT_RACINE, 2*MARGIN+2*DEPASSEMENT_RACINE).max(minimumLayoutSize(parent));
            }
        }
        
        private PanelRacineCarree(){
            this.setLayout(new Layout());
            
            champ = new JMathTextField(CONTENU);
            champ.setSize(champ.getMinimumSize());

            this.add(champ, CONTENU);
        }

        @Override
        protected void dessiner(Graphics2D g2D){
            int largeur = getWidth(), hauteur = getHeight();
            int lRacine = DEPASSEMENT_RACINE+LARGEUR_V+PATTE;
            int lObjects = champ.getWidth()+lRacine+DEPASSEMENT_RACINE, hObjects = champ.getHeight()+2*DEPASSEMENT_RACINE;
            int x = (int) ((largeur-lObjects)/2.0);
            int y = (int) ((hauteur-hObjects)/2.0);
            int offset = x, h = y+hObjects/2+PATTE;
            
            int[] xPoints = {offset, offset+=PATTE, offset+=LARGEUR_V/2, offset+=LARGEUR_V/2, offset+=champ.getWidth()+2*DEPASSEMENT_RACINE};
            int[] yPoints = {h, h-=PATTE, h+=champ.getHeight()/2+DEPASSEMENT_RACINE, y, y};
            g2D.drawPolyline(xPoints, yPoints, 5);
        }

    }

}

