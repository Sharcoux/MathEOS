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

import java.awt.event.ComponentEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;

import matheos.utils.objets.Icone;
import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.managers.ImageManager;
import matheos.utils.librairies.ImageTools;
import matheos.utils.texte.JMathTextPane;
import java.awt.event.ComponentAdapter;

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
    
    private class PanelSysteme extends JPanel {

        private static final int PREFERRED_WIDTH = 300;
        private static final int PREFERRED_HEIGHT = 100;
        private static final int SIDE_MARGIN = 120; // Marge gauche + droite pour le JComboBox
        private static final int TOP_BOTTOM_MARGIN = 10; // Marge en haut et en bas
        private static final int DEPASSEMENT_ACCOLADE = 10;
        private static final int ECART_LIGNE = 5;
        private final JLabel accoladeHaute;
        private final JLabel accoladeBasse;
        private final Icone iconeAccoladeHaute;
        private final Icone iconeAccoladeBasse;
//        private double coefIcon = 1;

        private final JMathTextField premiereEquation;
        private final JMathTextField deuxiemeEquation;
    
        private PanelSysteme() {
            super();
            
            premiereEquation = new JMathTextField(EQUATION1);
            deuxiemeEquation = new JMathTextField(EQUATION2);
            
            this.setSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            this.setLayout(null);
//            premiereEquation.setMinimumSize(new Dimension(100, 30));
//            premiereEquation.dimensionner();
            premiereEquation.setSize(premiereEquation.getMinimumSize());
            premiereEquation.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    positionComponent();
                }
            });

//            deuxiemeEquation.setMinimumSize(new Dimension(100, 30));
//            deuxiemeEquation.dimensionner();
            deuxiemeEquation.setSize(deuxiemeEquation.getMinimumSize());
            deuxiemeEquation.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    positionComponent();
                }
            });

            iconeAccoladeHaute = ImageManager.getIcone("high accolade");
            iconeAccoladeBasse = ImageManager.getIcone("low accolade");

//            Image image = iconeAccoladeHaute.getImage();
            // Permet de récupérer les dimensions de l'image en attendant la fin de son chargement
//            MediaTracker tracker = new MediaTracker(this);
//            tracker.addImage(image, 0);
//            try {
//                tracker.waitForID(0);
//            } catch (InterruptedException e) {
//            }
//            coefIcon = ((double) image.getWidth(this)) / ((double) image.getHeight(this));

            accoladeHaute = new JLabel(iconeAccoladeHaute);
            accoladeBasse = new JLabel(iconeAccoladeBasse);

            positionComponent();
            this.add(premiereEquation);
            this.add(deuxiemeEquation);
            this.add(accoladeHaute);
            this.add(accoladeBasse);
            repaint();
        }

        private void positionComponent() {
//            int largeur = PREFERRED_WIDTH;
//            int hauteur = PREFERRED_HEIGHT;

            int hauteurIconeHaute = premiereEquation.getHeight() + DEPASSEMENT_ACCOLADE;
            int hauteurIconeBasse = deuxiemeEquation.getHeight() + DEPASSEMENT_ACCOLADE;
            iconeAccoladeHaute.setImage(ImageTools.getScaledInstance(ImageTools.imageToBufferedImage(iconeAccoladeHaute.getImage()), hauteurIconeHaute, hauteurIconeHaute, ImageTools.Quality.HIGH, ImageTools.FIT_EXACT));
            iconeAccoladeBasse.setImage(ImageTools.getScaledInstance(ImageTools.imageToBufferedImage(iconeAccoladeBasse.getImage()), hauteurIconeBasse, hauteurIconeBasse, ImageTools.Quality.HIGH, ImageTools.FIT_EXACT));
            accoladeHaute.setSize(iconeAccoladeHaute.getIconWidth(), iconeAccoladeHaute.getIconHeight());
            accoladeBasse.setSize(iconeAccoladeBasse.getIconWidth(), iconeAccoladeBasse.getIconHeight());

            int largeur = Math.max(Math.max(premiereEquation.getWidth(), deuxiemeEquation.getWidth()) + SIDE_MARGIN, PREFERRED_WIDTH);
            int hauteurSurLigne = Math.max(accoladeHaute.getHeight() + TOP_BOTTOM_MARGIN, PREFERRED_HEIGHT / 2);
            int hauteurSousLigne = Math.max(accoladeBasse.getHeight(), PREFERRED_HEIGHT / 2);
            int hauteur = hauteurSurLigne + hauteurSousLigne;

            this.setSize(largeur, hauteur);
            DialogueMathSysteme.this.setSize(this.getWidth() + 6, this.getHeight() + 100);

            int largeurMax = Math.max(premiereEquation.getWidth(), deuxiemeEquation.getWidth());
            premiereEquation.setLocation(largeur / 2 - largeurMax / 2, hauteurSurLigne - ECART_LIGNE - premiereEquation.getHeight());
            deuxiemeEquation.setLocation(largeur / 2 - largeurMax / 2, hauteurSurLigne + ECART_LIGNE);

            accoladeHaute.setLocation(this.getWidth() / 2 - largeurMax / 2 - accoladeHaute.getWidth(), hauteurSurLigne - accoladeHaute.getHeight());
            accoladeBasse.setLocation(this.getWidth() / 2 - largeurMax / 2 - accoladeBasse.getWidth(), hauteurSurLigne);

            repaint();
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
