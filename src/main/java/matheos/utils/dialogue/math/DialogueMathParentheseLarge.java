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

    private class PanelParentheseLarge extends JPanel {

        private static final int PREFERRED_WIDTH = 300;
        private static final int PREFERRED_HEIGHT = 100;
        private static final int SIDE_MARGIN = 120; // Marge gauche + droite pour le JComboBox
        private static final int TOP_BOTTOM_MARGIN = 10; // Marge en haut et en bas
        private static final int DEPASSEMENT_PARENTHESE = 5;
        private final JLabel parentheseGauche;
        private final JLabel parentheseDroite;
        private final Icone iconeParentheseGauche;
        private final Icone iconeParentheseDroite;

        private final JLimitedMathTextPane champ;
    
        private PanelParentheseLarge(){
            super();
            
            champ = new JMathTextField(CONTENU);
            
            this.setSize(PREFERRED_WIDTH,PREFERRED_HEIGHT);
//            this.setLayout(null);
//            champ.dimensionner();
            champ.setSize(champ.getMinimumSize());
            champ.setAlignmentCenter(true);
            champ.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    positionComponent();
                }
            });

            
            iconeParentheseGauche = ImageManager.getIcone("left parenthese");
            iconeParentheseDroite = ImageManager.getIcone("right parenthese");

//            Image image = iconeParentheseGauche.getImage();
            // Permet de récupérer les dimensions de l'image en attendant la fin de son chargement
//            MediaTracker tracker=new MediaTracker(this);
//            tracker.addImage(image,0);
//            try{tracker.waitForID(0);} catch (InterruptedException e) {}
//            coefIcon = ((double)image.getWidth(this))/((double)image.getHeight(this));

            parentheseGauche = new JLabel(iconeParentheseGauche);
            parentheseDroite = new JLabel(iconeParentheseDroite);

            this.add(parentheseGauche);
            this.add(champ);
            this.add(parentheseDroite);
            positionComponent();
            repaint();
        }

        private void positionComponent(){
//            int largeur = PREFERRED_WIDTH;
//            int hauteur = PREFERRED_HEIGHT;

            int hauteurIcone = champ.getHeight() + DEPASSEMENT_PARENTHESE*2;
//            iconeParentheseGauche.setImage(ImageTools.getScaledInstance(ImageTools.imageToBufferedImage(iconeParentheseGauche.getImage()), hauteurIcone, hauteurIcone, ImageTools.Quality.HIGH));
//            iconeParentheseDroite.setImage(ImageTools.getScaledInstance(ImageTools.imageToBufferedImage(iconeParentheseDroite.getImage()), hauteurIcone, hauteurIcone, ImageTools.Quality.HIGH));
            iconeParentheseGauche.setImage(ImageManager.getIcone("left parenthese", hauteurIcone, hauteurIcone).getImage());
            iconeParentheseDroite.setImage(ImageManager.getIcone("right parenthese", hauteurIcone, hauteurIcone).getImage());
            parentheseGauche.setIcon(iconeParentheseGauche);
            parentheseDroite.setIcon(iconeParentheseDroite);
            parentheseGauche.setSize(iconeParentheseGauche.getIconWidth(), iconeParentheseGauche.getIconHeight());
            parentheseDroite.setSize(iconeParentheseDroite.getIconWidth(), iconeParentheseDroite.getIconHeight());
//
            int largeur = Math.max(champ.getWidth() + SIDE_MARGIN, PREFERRED_WIDTH);
            int hauteur = Math.max(parentheseGauche.getHeight() + TOP_BOTTOM_MARGIN, PREFERRED_HEIGHT);

            this.setSize(largeur, hauteur);
            DialogueMathParentheseLarge.this.setSize(this.getWidth()+6, this.getHeight()+100);

            champ.setLocation(largeur/2 - champ.getWidth()/2, hauteur/2 - champ.getHeight()/2);

            parentheseGauche.setLocation(largeur/2 - champ.getWidth()/2 - parentheseGauche.getWidth(), hauteur/2 - parentheseGauche.getHeight()/2);
            parentheseDroite.setLocation(largeur/2 + champ.getWidth()/2, hauteur/2 - parentheseDroite.getHeight()/2);

            repaint();
        }
    }
}


