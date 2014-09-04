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

import matheos.utils.dialogue.DialogueBloquant;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.managers.Traducteur;
import matheos.utils.texte.JMathTextPane;
import java.awt.event.ComponentAdapter;

/**
 *
 * @author François Billioud, Guillaume Varoquaux
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

    private class PanelChapeauAngle extends JPanel {

        private static final int EPAISSEUR = 2;
        private static final int PREFERRED_WIDTH = 300;
        private static final int PREFERRED_HEIGHT = 100;
        private static final int SIDE_MARGIN = 120; // Marge gauche + droite pour le JComboBox
        private static final int TOP_BOTTOM_MARGIN = 10; // Marge en haut et en bas
        private static final int ESPACE_BAS_CHAPEAU = 10;
        private static final int ESPACE_HAUT_CHAPEAU = 10;

        private final JLimitedMathTextPane text;
    
        private PanelChapeauAngle() {
            super();
            
            text = new JMathTextField(ANGLE_ID);
            
            this.setSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            this.setLayout(null);
            text.setLongueurMax(3);
//            text.dimensionner();
            text.setSize(text.getMinimumSize());
            text.setAlignmentCenter(true);
            text.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    positionComponent();
                }
            });

            positionComponent();
            this.add(text);
            repaint();
        }

        private void positionComponent() {
            int largeur = Math.max(text.getWidth() + SIDE_MARGIN, PREFERRED_WIDTH);

            int hauteurSurMilieu = Math.max(text.getHeight() / 2 + ESPACE_BAS_CHAPEAU + ESPACE_HAUT_CHAPEAU + EPAISSEUR + TOP_BOTTOM_MARGIN, PREFERRED_HEIGHT / 2);
            int hauteurSousMilieu = Math.max(text.getHeight() / 2 + TOP_BOTTOM_MARGIN, PREFERRED_HEIGHT / 2);
            int hauteur = hauteurSurMilieu + hauteurSousMilieu;

            this.setSize(largeur, hauteur);
            DialogueMathChapeauAngle.this.setSize(this.getWidth() + 6, this.getHeight() + 100);

            text.setLocation(largeur / 2 - text.getWidth() / 2, hauteur / 2 - text.getHeight() / 2);

            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            super.paintComponent(g); // Redessine le Panel avant d'ajouter les composants
            Stroke epaisseur = new BasicStroke(EPAISSEUR);
            g2d.setStroke(epaisseur);

            Point pointGauche = new Point(this.getWidth() / 2 - text.getWidth() / 2, this.getHeight() / 2 - text.getHeight() / 2 - ESPACE_BAS_CHAPEAU);
            Point pointMilieu = new Point(this.getWidth() / 2, this.getHeight() / 2 - text.getHeight() / 2 - ESPACE_BAS_CHAPEAU - ESPACE_HAUT_CHAPEAU);
            Point pointDroit = new Point(this.getWidth() / 2 + text.getWidth() / 2, this.getHeight() / 2 - text.getHeight() / 2 - ESPACE_BAS_CHAPEAU);

            g2d.drawLine((int) pointGauche.getX(), (int) pointGauche.getY(), (int) pointMilieu.getX(), (int) pointMilieu.getY());
            g2d.drawLine((int) pointMilieu.getX(), (int) pointMilieu.getY(), (int) pointDroit.getX(), (int) pointDroit.getY());

            g2d.setStroke(new BasicStroke(1));
        }

    }
}
