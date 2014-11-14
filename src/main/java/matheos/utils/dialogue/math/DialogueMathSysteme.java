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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.swing.JPanel;

import matheos.utils.managers.ImageManager;
import matheos.utils.texte.JMathTextPane;
import static matheos.utils.dialogue.math.DialogueMath.MathLayout.MARGIN;
import matheos.utils.librairies.DimensionTools;
import matheos.utils.objets.Icone;

/**
 *
 * @author François Billioud, Guillaume Varoquaux
 */
@SuppressWarnings("serial")
public class DialogueMathSysteme extends DialogueMath {

    private static final String EQUATION1 = "equation 1";
    private static final String EQUATION2 = "equation 2";
    
    public DialogueMathSysteme(JMathTextPane texteParent, String equation1, String equation2) {
        this(texteParent);
        setInputInitialValue(EQUATION1, equation1);
        setInputInitialValue(EQUATION2, equation2);
    }
    public DialogueMathSysteme(JMathTextPane texteParent) {
        super("dialog equation system", texteParent);
    }

    public String construireChaine(String premiereLigne, String deuxiemeLigne) {
        String chaineRenvoyee = "<mrow><mo>{</mo><mrow><mtable columnalign='left'><mtr><mtd><mrow>" + premiereLigne + "</mrow></mtd></mtr><mtr><mtd><mrow>" + deuxiemeLigne + "</mrow></mtd></mtr></mtable></mrow></mrow>";
        return chaineRenvoyee;
    }

    @Override
    protected String createMathMLString() {
        JMathTextPane equation1 = getInput(EQUATION1);
        JMathTextPane equation2 = getInput(EQUATION2);
        return "<math><mrow><mo>{</mo><mrow><mtable columnalign='left'><mtr><mtd><mrow>" + writeMathMLString(equation1) + "</mrow></mtd></mtr><mtr><mtd><mrow>"+writeMathMLString(equation2)+"</mrow></mtd></mtr></mtable></mrow></mrow></math>";
    }

    @Override
    protected JPanel getCenterPane() {
        return new PanelSysteme();
    }

    @Override
    protected String getMasterTag() {
        return "mtable";
    }
    
    private class PanelSysteme extends MathPanel {

        private static final int ECART_LIGNE = 10;
        private static final int LARGEUR_ACCOLADE = 20;
        private static final int DEPASSEMENT_ACCOLADE = 5;

        private final JMathTextField premiereEquation;
        private final JMathTextField deuxiemeEquation;
        
        private final Icone accolade = ImageManager.getIcone("accolade");

        private final class Layout extends MathLayout {
            @Override
            public void layoutContainer(Container parent) {
                parent.setSize(preferredLayoutSize(parent));
                
                int largeur = parent.getWidth(), hauteur = parent.getHeight();
                int lObjects = Math.max(premiereEquation.getWidth(), deuxiemeEquation.getWidth())+LARGEUR_ACCOLADE+DEPASSEMENT_ACCOLADE;
                int hObjects = premiereEquation.getHeight()+deuxiemeEquation.getHeight()+ECART_LIGNE+2*DEPASSEMENT_ACCOLADE;
                int x = (int) ((largeur-lObjects)/2.0+LARGEUR_ACCOLADE+DEPASSEMENT_ACCOLADE);
                int yA = (int) ((hauteur-hObjects)/2.0+DEPASSEMENT_ACCOLADE);
                int yB = hauteur-yA-deuxiemeEquation.getHeight();
                premiereEquation.setLocation(x, yA);
                deuxiemeEquation.setLocation(x, yB);
                
                accolade.setSize(LARGEUR_ACCOLADE, hObjects);
            }
            @Override
            public Dimension preferredLayoutSize(Container parent) {
                int lObjects = Math.max(premiereEquation.getWidth(), deuxiemeEquation.getWidth())+LARGEUR_ACCOLADE+DEPASSEMENT_ACCOLADE;
                int hObjects = premiereEquation.getHeight()+deuxiemeEquation.getHeight()+ECART_LIGNE+2*DEPASSEMENT_ACCOLADE;
                return new DimensionTools.DimensionT(lObjects,hObjects).plus(2*MARGIN, 2*MARGIN).max(minimumLayoutSize(parent));
            }
        }
        
        private PanelSysteme() {
            
            this.setLayout(new Layout());
            
            premiereEquation = new JMathTextField(EQUATION1);
            premiereEquation.setSize(premiereEquation.getMinimumSize());

            deuxiemeEquation = new JMathTextField(EQUATION2);
            deuxiemeEquation.setSize(deuxiemeEquation.getMinimumSize());

            this.add(premiereEquation, EQUATION1);
            this.add(deuxiemeEquation, EQUATION2);
        }

        @Override
        protected void dessiner(Graphics2D g2D) {
            int largeur = getWidth(), hauteur = getHeight();
            int lObjects = Math.max(premiereEquation.getWidth(), deuxiemeEquation.getWidth())+LARGEUR_ACCOLADE+DEPASSEMENT_ACCOLADE;
            int hObjects = premiereEquation.getHeight()+deuxiemeEquation.getHeight()+ECART_LIGNE+2*DEPASSEMENT_ACCOLADE;
            int x = (int) ((largeur-lObjects)/2.0);
            int y = (int) ((hauteur-hObjects)/2.0);
            g2D.drawImage(accolade.getImage(), x, y, null);
        }
    
    }
    
    /**
     * Définit si le texte en sélection dans un JMathTextPane peut être mis dans
     * un système d'équation ou non.
     * 
     * @param textPane le JMathTextPane contenant le texte sélectionner
     * @return true si on peut faire un système d'équation du texte sélectionné;
     * false sinon
     */
    public static boolean isAllowedToBecomeSystem(JMathTextPane textPane){
        if (textPane.getSelectedText() == null) {
            return false;
        }
        String[] chaines = textPane.getSelectedText().split("\n");
        return chaines.length == 2;
    }
}
