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


import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
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
        String content = createMathMLString(textPane);
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

    private class PanelFraction extends JPanel {

        private static final int EPAISSEUR = 2;
        private static final int PREFERRED_WIDTH = 300;
        private static final int PREFERRED_HEIGHT = 100;
        private static final int SIDE_MARGIN = 120; // Marge gauche + droite pour le JComboBox
        private static final int TOP_BOTTOM_MARGIN = 10; // Marge en haut et en bas
        private static final int ECART_LIGNE = 5;

        private final JLimitedMathTextPane numerateur;
        private final JLimitedMathTextPane denominateur;
        
        private PanelFraction(){
            super();
            
            numerateur = new JMathTextField(NUMERATEUR);
            denominateur = new JMathTextField(DENOMINATEUR);
            
            this.setSize(PREFERRED_WIDTH,PREFERRED_HEIGHT);
            this.setLayout(null);
//            numerateur.dimensionner();
            numerateur.setSize(numerateur.getMinimumSize());
            numerateur.setAlignmentCenter(true);
            numerateur.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    positionComponent();
                }
            });

//            denominateur.dimensionner();
            denominateur.setSize(denominateur.getMinimumSize());
            denominateur.setAlignmentCenter(true);
            denominateur.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    positionComponent();
                }
            });
            
//            if(getTexteParent().getSelectedText() != null){
//                if(getTexteParent() instanceof JLimitedMathTextPane && JLimitedMathTextPane.isContentTooLong(getTexteParent())){
//                    DialogueBloquant.warning("selection cut");
//                }
//                //PENDING
//                DataTexte data = getTexteParent().getSelectedText()!=null ? EditeurIO.write(getTexteParent().getHTMLdoc(), getTexteParent().getComponentMap(), getTexteParent().getSelectionStart(), getTexteParent().getSelectedText().length()) : EditeurIO.write(getTexteParent().getHTMLdoc(), getTexteParent().getComponentMap());
//                EditeurIO.read(numerateur, data);
//            }
            
            positionComponent();
            this.add(numerateur);
            this.add(denominateur);
            repaint();
        }

        private void positionComponent(){
            int largeur = PREFERRED_WIDTH;
            int hauteur = PREFERRED_HEIGHT;
            largeur = Math.max(Math.max(numerateur.getWidth(), denominateur.getWidth()) + SIDE_MARGIN, PREFERRED_WIDTH);
            int hauteurSurLigne = Math.max(numerateur.getHeight() + EPAISSEUR/2 + ECART_LIGNE + TOP_BOTTOM_MARGIN, PREFERRED_HEIGHT/2);
            int hauteurSousLigne = Math.max(denominateur.getHeight() + EPAISSEUR/2 + ECART_LIGNE + TOP_BOTTOM_MARGIN, PREFERRED_HEIGHT/2);
            hauteur = hauteurSurLigne + hauteurSousLigne;

            this.setSize(largeur, hauteur);
            DialogueMathFraction.this.setSize(this.getWidth()+6, this.getHeight()+100);

            numerateur.setLocation(largeur/2 - numerateur.getWidth()/2, hauteurSurLigne - ECART_LIGNE-EPAISSEUR/2 - numerateur.getHeight());
            denominateur.setLocation(largeur/2 - denominateur.getWidth()/2, hauteurSurLigne + ECART_LIGNE+EPAISSEUR/2);

            repaint();
        }

        @Override
        public void paintComponent(Graphics  g){
            Graphics2D g2d = (Graphics2D)g;
            super.paintComponent(g); // Redessine le Panel avant d'ajouter les composants
            Stroke epaisseur = new BasicStroke(EPAISSEUR);
            g2d.setStroke(epaisseur);
            int demiLargeur = Math.max(numerateur.getWidth()/2, denominateur.getWidth()/2);
            int centreLargeur = Math.max(this.getWidth()/2, PREFERRED_WIDTH/2);
            int centreHauteur = Math.max(Math.max(numerateur.getHeight() + EPAISSEUR/2 + ECART_LIGNE+TOP_BOTTOM_MARGIN, PREFERRED_HEIGHT/2), PREFERRED_HEIGHT/2);
            g2d.drawLine(centreLargeur - demiLargeur, centreHauteur, centreLargeur + demiLargeur, centreHauteur);

            g2d.setStroke(new BasicStroke(1));
        }

    }
   
     
}
