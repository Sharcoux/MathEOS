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
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.texte.JMathTextPane;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;

/**
 *
 * @author François Billioud, Guillaume Varoquaux
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
//        JPanel panel = new JPanel();
//        panel.setLayout(new SpringLayout());
//        panel.add(new JLimitedMathTextPane(1, true));
//        panel.add(new JLimitedMathTextPane(1, true));
        return new PanelRacineCarree();
    }

     private class PanelRacineCarree extends JPanel {

        private static final int EPAISSEUR = 2;
        private static final int PREFERRED_WIDTH = 300;
        private static final int PREFERRED_HEIGHT = 100;
        private static final int SIDE_MARGIN = 120; // Marge gauche + droite pour le JComboBox
        private static final int TOP_BOTTOM_MARGIN = 10; // Marge en haut et en bas
        private static final int ESPACE_BARRE_HAUT = 10;
        private static final int ESPACE_CHAMP_POINT_MILIEU_HAUT = 5;
        private static final int ESPACE_ANGLE_RACINE = 10;
        private static final int ESPACE_DEPASSEMENT_RACINE = 5;

        private final JLimitedMathTextPane champ;
        
        private PanelRacineCarree(){
            super();
            
            champ = new JMathTextField(CONTENU);
            
            this.setSize(PREFERRED_WIDTH,PREFERRED_HEIGHT);
            this.setLayout(null);
//            champ.dimensionner();
            champ.setSize(champ.getMinimumSize());
            champ.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    positionComponent();
                }
            });

            positionComponent();
            this.add(champ);
            repaint();
        }

        private void positionComponent(){
            int largeur = Math.max(champ.getWidth() + SIDE_MARGIN, PREFERRED_WIDTH);

            int hauteurSurMilieu = Math.max(champ.getHeight()/2+ESPACE_BARRE_HAUT+EPAISSEUR+TOP_BOTTOM_MARGIN ,PREFERRED_HEIGHT/2);
            int hauteurSousMilieu = Math.max(champ.getHeight()/2 + EPAISSEUR + TOP_BOTTOM_MARGIN, PREFERRED_HEIGHT/2);
            int hauteur = hauteurSurMilieu + hauteurSousMilieu;

            this.setSize(largeur, hauteur);
            DialogueMathRacineCarree.this.setSize(this.getWidth()+6, this.getHeight()+100);

            champ.setLocation(largeur/2 - champ.getWidth()/2, hauteur/2 - champ.getHeight()/2);

            repaint();
        }

        @Override
        public void paintComponent(Graphics  g){
            Graphics2D g2d = (Graphics2D)g;
            super.paintComponent(g); // Redessine le Panel avant d'ajouter les composants
            Stroke epaisseur = new BasicStroke(EPAISSEUR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(epaisseur);
            Point pointGauche = new Point(this.getWidth()/2 - champ.getWidth()/2 - ESPACE_CHAMP_POINT_MILIEU_HAUT - ESPACE_ANGLE_RACINE-5, this.getHeight()/2);
            Point pointMilieu = new Point(this.getWidth()/2 - champ.getWidth()/2 - ESPACE_CHAMP_POINT_MILIEU_HAUT - ESPACE_ANGLE_RACINE,this.getHeight()/2-ESPACE_BARRE_HAUT);
            Point pointMilieuBas = new Point(this.getWidth()/2 - champ.getWidth()/2 - ESPACE_CHAMP_POINT_MILIEU_HAUT - ESPACE_ANGLE_RACINE/2, this.getHeight()/2 + champ.getHeight()/2+ESPACE_DEPASSEMENT_RACINE);
            Point pointMilieuHaut = new Point(this.getWidth()/2 - champ.getWidth()/2 - ESPACE_CHAMP_POINT_MILIEU_HAUT,this.getHeight()/2 - champ.getHeight()/2 - ESPACE_ANGLE_RACINE);
            Point pointDroit = new Point(this.getWidth()/2 + champ.getWidth()/2 + ESPACE_DEPASSEMENT_RACINE,this.getHeight()/2 - champ.getHeight()/2 - ESPACE_ANGLE_RACINE);

            g2d.drawLine((int) pointGauche.getX(), (int) pointGauche.getY(), (int) pointMilieu.getX(), (int) pointMilieu.getY());
            g2d.drawLine((int) pointMilieu.getX(), (int) pointMilieu.getY(), (int) pointMilieuBas.getX(), (int) pointMilieuBas.getY());
            g2d.drawLine((int) pointMilieuBas.getX(), (int) pointMilieuBas.getY(), (int) pointMilieuHaut.getX(), (int) pointMilieuHaut.getY());
            g2d.drawLine((int) pointMilieuHaut.getX(), (int) pointMilieuHaut.getY(), (int) pointDroit.getX(), (int) pointDroit.getY());

            g2d.setStroke(new BasicStroke(1));
        }

    }

}

