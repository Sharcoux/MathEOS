/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package matheos.tableau;

import matheos.utils.librairies.ImageTools;
import matheos.utils.objets.Navigation;
import matheos.utils.texte.EditeurIO;
import matheos.utils.texte.JLimitedMathTextPane;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ParagraphView;

/**
 *
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class CaseSeparee extends JPanel {

    private static final int ESPACE_COTE = 1;
    private final JLimitedMathTextPane texteHaut;
    private final JLimitedMathTextPane texteBas;
    private final SpringLayout layout;
    private final JScrollPane scrollHaut;
    private final JScrollPane scrollBas;
    private final Navigation navigation = new Navigation();

    public CaseSeparee() {
        setBackground(Color.WHITE);
        texteHaut = new JLimitedMathTextPane();
        texteBas = new JLimitedMathTextPane();
        texteHaut.setEditorKit(new WrapEditorKit());
        texteBas.setEditorKit(new WrapEditorKit());
        texteHaut.setBackground(Color.WHITE);
        texteBas.setBackground(Color.WHITE);
        texteHaut.setAdaptableSize(false);
        texteBas.setAdaptableSize(false);
        navigation.addComponent(texteHaut);
        navigation.addComponent(texteBas);
        scrollHaut = new JScrollPane(texteHaut, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollBas = new JScrollPane(texteBas, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollHaut.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        scrollBas.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        layout = new SpringLayout();
        this.setLayout(layout);
        
        this.add(scrollHaut);
        this.add(scrollBas);
    }

    public JLimitedMathTextPane getTexteHaut() {
        return texteHaut;
    }

    public JLimitedMathTextPane getTexteBas() {
        return texteBas;
    }
    
    public boolean hasbeenModified() {return getTexteBas().hasBeenModified() && getTexteHaut().hasBeenModified();}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(0, 0, this.getWidth(), this.getHeight());
        g2d.setStroke(new BasicStroke(1));
    }

    public void dimensionner() {
        Spring espaceCote = Spring.constant(ESPACE_COTE);
        int widthChamp = (int) (this.getPreferredSize().getWidth() / 2.5);
        int heightChamp = (int) (this.getPreferredSize().getHeight() / 2.5);
        scrollHaut.setPreferredSize(new Dimension(widthChamp, heightChamp));
        scrollBas.setPreferredSize(new Dimension(widthChamp, heightChamp));
        texteHaut.setFont(texteHaut.getFont().deriveFont((float) this.getPreferredSize().getHeight() / 4));
        texteBas.setFont(texteBas.getFont().deriveFont((float) this.getPreferredSize().getHeight() / 4));
        layout.putConstraint(SpringLayout.NORTH, scrollHaut, espaceCote, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, scrollHaut, 0, SpringLayout.HORIZONTAL_CENTER, this);
        layout.putConstraint(SpringLayout.WEST, scrollBas, espaceCote, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, scrollBas, 0, SpringLayout.VERTICAL_CENTER, this);
        revalidate();
        repaint();
    }

    public int getLargeurNormale() {
        return texteHaut.getPreferredWidth() + texteBas.getPreferredWidth();
    }

    public int getHauteurNormale() {
        return texteHaut.getPreferredHeight() + texteBas.getPreferredHeight();
    }

    public BufferedImage prendreImage() {
        BufferedImage imageHaut = getImage(texteHaut);
        BufferedImage imageBas = getImage(texteBas);
        int largeur = Math.max(2 * Math.max(imageHaut.getWidth(), imageBas.getWidth()) + 5, TableConstants.LARGEUR_CELLULE_SEPAREE);
        int hauteur = Math.max(2 * Math.max(imageHaut.getHeight(), imageBas.getHeight()) + 5, TableConstants.HAUTEUR_CELLULE_SEPAREE);
        BufferedImage buffered = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = buffered.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, largeur, hauteur);
        g.setColor(Color.BLACK);
        g.drawImage(imageHaut, (3 * largeur - 2 * imageHaut.getWidth()) / 4, 1, this);
        g.drawImage(imageBas, 1 + (largeur - 2 * imageBas.getWidth()) / 4, hauteur / 2, this);
        return buffered;
    }

    private BufferedImage getImage(JLimitedMathTextPane textePane) {
        JLimitedMathTextPane texteCopie = new JLimitedMathTextPane();
        texteCopie.setBackground(Color.WHITE);
        texteCopie.setFont(textePane.getFont().deriveFont((float) TableConstants.FONT_SIZE_TABLEAU));
        texteCopie.setAlignmentCenter(true);
        texteCopie.charger(EditeurIO.write(textePane));
//        texteCopie.majMathComponent();
        this.add(texteCopie);
//        texteCopie.dimensionner();
        BufferedImage buffered = ImageTools.getImageFromComponent(texteCopie);
        this.remove(texteCopie);
        buffered = ImageTools.imageToBufferedImage(ImageTools.changeColorToTransparent(buffered, Color.WHITE, 55));
        return buffered;
    }

    /**
     * Classe permettant d'associer à un éditeur de texte un layout de longueur
     * "infinie" afin que le texte ne revienne pas à la ligne automatiquement.
     */
    private class WrapEditorKit extends HTMLEditorKit {

        private final ViewFactory defaultFactory = new CaseSeparee.WrapEditorKit.WrapColumnFactory();

        @Override
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }

        private class WrapColumnFactory extends HTMLEditorKit.HTMLFactory {

            @Override
            public View create(Element elem) {
                View vue = super.create(elem);
                if (vue instanceof javax.swing.text.html.ParagraphView) {
                    return new CaseSeparee.NoWrapParagraphView(elem);
                }
                return vue;
            }
        }
    }

    /**
     * Classe gérant le non retour à la ligne automatique d'un éditeur de texte.
     */
    private class NoWrapParagraphView extends ParagraphView {

        NoWrapParagraphView(Element elem) {
            super(elem);
        }

        @Override
        public void layout(int width, int height) {
            super.layout(Short.MAX_VALUE, height);
        }

        @Override
        public float getMinimumSpan(int axis) {
            return super.getPreferredSpan(axis);
        }
    }
}
