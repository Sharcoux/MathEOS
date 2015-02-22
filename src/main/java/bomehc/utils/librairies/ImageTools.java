/** «Copyright 2013 François Billioud»
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

package bomehc.utils.librairies;

import bomehc.utils.managers.ColorManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageObserver;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

/**
 *
 * @author Guillaume
 */
@SuppressWarnings("serial")
public abstract class ImageTools {

    public static enum Quality {FAST,AUTO,DEFAULT,HIGH,OPTIMAL}
//    public static final int FAST_QUALITY = 0;
//    public static final int HIGH_QUALITY = 1;
//    public static final int MEDIUM_QUALITY = 2;
    
    /**
     * Méthode permettant de créer une image à partir d'un Composant
     *
     * @param component le composant dont on veut créer une image de son
     * apparence.
     * @return Une BufferedImage qui correspond au visuel du composant placé en
     * paramètre.
     */
    public static BufferedImage getImageFromComponent(Component component) {
        if (component == null) {
            return null;
        }

        int width = (int) component.getPreferredSize().getWidth();
        int height = (int) component.getPreferredSize().getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics g = image.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        component.print(g);
        g.dispose();

        return image;
    }

    /**
     * Méthode qui crée un tableau de bytes à partir d'une image
     *
     * @param img
     * @return
     */
    public static byte[] getArrayFromImage(BufferedImage img) {
/*        int[] pixels = new int[width * height];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0, width);
        try {
            pg.grabPixels();
        } catch (InterruptedException ex) {
            Logger.getLogger(ImageTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pixels;*/
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "PNG", output);
            return output.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(ImageTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Méthode qui crée une BufferedImage à partir d'un tableau de bytes
     *
     * @param Tbyte
     * @return
     */
    public static BufferedImage getImageFromArray(byte[] Tbyte) {
//        MemoryImageSource mis = new MemoryImageSource(width, height, pixels, 0, width);
//        Toolkit tk = Toolkit.getDefaultToolkit();
//        return imageToBufferedImage(tk.createImage(mis));
        ByteArrayInputStream input = new ByteArrayInputStream(Tbyte);
        try {
            return ImageIO.read(input);
        } catch (IOException ex) {
            Logger.getLogger(ImageTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Méthode qui convertit une image en BufferedImage.
     *
     * @param im l'image à convertir
     * @return une BufferedImage créée à partir de l'image passée en paramètre
     */
    public static BufferedImage imageToBufferedImage(Image im) {
        if(im==null) {return null;}
        if(im instanceof BufferedImage) {return (BufferedImage) im;}//On tente un simple Cast
        //On vérifie que l'image est bien chargée
        if(im.getWidth(null)==-1 || im.getHeight(null)==-1) {
            MediaTracker tracker = new MediaTracker(new Component(){});
            tracker.addImage(im, 0);
            try {
                tracker.waitForID(0);
            } catch (InterruptedException ex1) {
                Logger.getLogger(ImageTools.class.getName()).log(Level.SEVERE, null, ex1);
                if(im.getWidth(null)==-1 || im.getHeight(null)==-1) return null;
            }
        }
        //On dessine l'image dans un buffer
        BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics bg = bi.createGraphics();
        bg.drawImage(im, 0, 0, null);
        bg.dispose();
        return bi;
    }

    /**
     * Retourne une image aux dimensions passées en paramètre. Cette méthode est
     * optimisée en qualité.
     *
     * @param original l'image originale à redimensionner
     * @param largeur la nouvelle largeur
     * @param hauteur la nouvelle hauteur
     * @return l'image originale redimensionnée
     */
//    public static BufferedImage getScaledInstance(BufferedImage original, int largeur, int hauteur) {
//        BufferedImage newImage = new BufferedImage(largeur, hauteur, original.getType());
//        Graphics g = newImage.getGraphics();
//        g.drawImage(original, 0, 0, largeur, hauteur, null); // scaled drawing of choice
//        g.dispose();
//        return newImage;
//        //Image im = original.getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH);
//    }

    public static final Scalr.Mode FIT_EXACT = Scalr.Mode.FIT_EXACT;
    public static final Scalr.Mode FIT_TO_HEIGHT = Scalr.Mode.FIT_TO_HEIGHT;
    public static final Scalr.Mode FIT_TO_WIDTH = Scalr.Mode.FIT_TO_WIDTH;
    public static final Scalr.Mode AUTOMATIC = Scalr.Mode.AUTOMATIC;
    
    /**
     * Retourne une image aux dimensions passées en paramètre.
     *
     * @param original l'image originale à redimensionner
     * @param largeur la nouvelle largeur
     * @param hauteur la nouvelle hauteur
     * @param quality la qualité du redimensionnement, rapide ou de qualité
     * @return l'image originale redimensionnée
     */
    public static BufferedImage getScaledInstance(BufferedImage original, int largeur, int hauteur, Quality q, Scalr.Mode mode) {
        Scalr.Method method;
        switch (q) {
            case AUTO:method=Scalr.Method.AUTOMATIC;break;
            case DEFAULT:method=Scalr.Method.BALANCED;break;
            case FAST:method=Scalr.Method.SPEED;break;
            case HIGH:method=Scalr.Method.QUALITY;break;
            case OPTIMAL:method=Scalr.Method.ULTRA_QUALITY;break;
            default:method=Scalr.Method.BALANCED;
        }
        return Scalr.resize(original, method, Scalr.Mode.FIT_EXACT, largeur, hauteur);
    }

    /**
     * Retourne une image aux dimensions passées en paramètre. Cette méthode est
     * optimisée en vitesse, tout en fournissant une qualité correcte.
     *
     * @param original l'image originale à redimensionner
     * @param largeur la nouvelle largeur
     * @param hauteur la nouvelle hauteur
     * @return l'image originale redimensionnée
     */
//    public static BufferedImage getFastScaledInstance(BufferedImage original, int largeur, int hauteur) {
//
//        BufferedImage buffered = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_4BYTE_ABGR);
//        Graphics2D g2 = buffered.createGraphics();
//        double coef = (double) largeur / original.getWidth(null);
//        if (coef >= 1) { //Agrandissement
//            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g2.drawImage(original, 0, 0, largeur, hauteur, null);
//        } else { //Rétrécissement
//            int w = original.getWidth(null);
//            int h = original.getHeight(null);
//            BufferedImage copie = copy(original);
//            do {
//                if (w > largeur) {
//                    w = (int) (w / 1.5);
//                    if (w < largeur) {
//                        w = largeur;
//                    }
//                }
//                if (h > hauteur) {
//                    h = (int) (h / 1.5);
//                    if (h < hauteur) {
//                        h = hauteur;
//                    }
//                }
//                BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//                Graphics2D g2tmp = tmp.createGraphics();
//                g2tmp.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//                g2tmp.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//                g2tmp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                g2tmp.drawImage(copie, 0, 0, w, h, null);
//                g2tmp.dispose();
//
//                copie = tmp;
//            } while (w != largeur || h != hauteur);
//            buffered = copie;
//        }
//
//        return buffered;
//    }

    /**
     * copie une BufferedImage
     * @param bi l'image originale
     * @return la copie
     */
    public static BufferedImage copy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    
    private static final int SHADOW_DISTANCE = 3;
    public static BufferedImage getShadowedImage(BufferedImage source, ImageObserver observer) {
        BufferedImage shadow = imageToBufferedImage(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(source.getSource(), new ShadowFilter())));
        Graphics g = shadow.getGraphics().create(-SHADOW_DISTANCE, -SHADOW_DISTANCE, shadow.getWidth()+SHADOW_DISTANCE, shadow.getHeight()+SHADOW_DISTANCE);
        g.drawImage(source, 0, 0, observer);
        return shadow;
    }
    private static class ShadowFilter extends RGBImageFilter {
        public ShadowFilter() {
            canFilterIndexColorModel = true;
        }

        public int filterRGB(int x, int y, int rgb) {
            int alpha = (rgb >> 24) & 0xff;
            return alpha << 24;
        }
    }
    
    /**
     * Permet de changer une couleur sur une image par une autre couleur.
     *
     * @param original l'image à transformer
     * @param colorToChange la couleur d'origine que l'on souhaite recolorer
     * @param newColor la couleur que l'on souhaite obtenir à l'arrivée
     * @return une image dont la couleur colorToChange à été transformée par
     * newColor
     */
    public static Image changeColorToOther(Image original, Color colorToChange, Color newColor) {
        return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(original.getSource(), new FilterColorToColor(colorToChange, newColor)));
    }

    /**
     * Permet de changer les pixels d'une image proches d'une couleur par une
     * autre couleur.
     *
     * @param original l'image à transformer
     * @param colorToChange la couleur d'origine que l'on souhaite recolorer
     * @param newColor la couleur que l'on souhaite obtenir à l'arrivée
     * @param delta l'entier définissant à plus ou moins quelle proportion de
     * colorToChange on souhaite recolorer un pixel
     * @return une image dont la couleur colorToChange à été transformée par
     * newColor
     */
    public static Image changeColorToOther(Image original, Color colorToChange, Color newColor, int delta) {
        return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(original.getSource(), new FilterColorToColor(colorToChange, newColor, delta)));
    }

    /**
     * Permet de rendre transparente une couleur sur une image.
     *
     * @param original l'image à transformer
     * @param colorToChange la couleur à rendre transparente
     * @return une image dont la couleur colorToChange a été rendue trnsparente
     */
    public static Image changeColorToTransparent(Image original, Color colorToChange) {
        return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(original.getSource(), new FilterColorToTransparent(colorToChange)));
    }

    /**
     * Permet de rendre transparente les pixels d'une image proches d'une
     * couleur.
     *
     * @param original l'image à transformer
     * @param colorToChange la couleur à rendre transparente
     * @param delta l'entier définissant à plus ou moins quelle proportion de
     * colorToChange on souhaite appliquer la transformation à un pixel
     * @return une image dont la couleur colorToChange a été rendue trnsparente
     */
    public static Image changeColorToTransparent(Image original, Color colorToChange, int delta) {
        return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(original.getSource(), new FilterColorToTransparent(colorToChange, delta)));
    }

    /**
     * Permet de donner une couleur à des pixels transparents d'une image.
     *
     * @param original l'image à transformer
     * @param colorToSet la couleur par laquelle on souhaite remplacer les
     * pixels transparents
     * @return une image dont les pixels transparents (alpha == 0) ont été
     * remplacés par la couleur passée en paramètre
     */
    public static Image changeTransparentToColor(Image original, Color colorToSet) {
        return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(original.getSource(), new FilterTransparentToColor(colorToSet)));
    }

    /**
     * Permet de donner une couleur à des pixels d'une image dont la valeur max
     * de l'alpha est donné par delta.
     *
     * @param original l'image à transformer
     * @param colorToSet la couleur par laquelle on souhaite remplacer les
     * pixels transparents
     * @param delta l'entier définissant la valeur max de l'alpha en desous
     * duquel les pixels seront colorisés.
     * @return une image dont les pixels transparents (alpha == 0) ont été
     * remplacés par la couleur passée en paramètre
     */
    public static Image changeTransparentToColor(Image original, Color colorToSet, int delta) {
        return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(original.getSource(), new FilterTransparentToColor(colorToSet, delta)));
    }

    /**
     * Permet de changer chaque pixel d'une image par sa couleur en négatif.
     *
     * @param original l'image à transformer
     * @return une image en négative de celle passée en paramètre
     */
    public static Image changeToNegative(Image original) {
        return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(original.getSource(), new FilterColorToNegative()));
    }

    /**
     * Classe interne gérant la recoloration d'une image.
     */
    private static abstract class FilterColor extends RGBImageFilter {

        protected Color colorToChange;
        protected Color newColor;
        protected int delta;

        protected FilterColor(Color colorToChange, Color newColor, int delta) {
            this.colorToChange = colorToChange;
            this.newColor = newColor;
            this.delta = delta;
        }

        protected abstract Color findColorToSet(int rouge, int vert, int bleue, int alpha);

        protected abstract boolean pixelToChange(int rouge, int vert, int bleue, int alpha);

        @Override
        public int filterRGB(int x, int y, int rgb) {
            ColorModel cm = ColorModel.getRGBdefault();
            //On récupère le modèle de couleur RGBA (4 octets pour coder une couleur.)
            int alpha = cm.getAlpha(rgb); //Récupération des valeurs rgba des pixels de l'image.
            int rouge = cm.getRed(rgb);
            int vert = cm.getGreen(rgb);
            int bleu = cm.getBlue(rgb);

            if (pixelToChange(rouge, vert, bleu, alpha)) {
                Color colorToSet = findColorToSet(rouge, vert, bleu, alpha);
                alpha = colorToSet.getAlpha();
                bleu = colorToSet.getBlue();
                vert = colorToSet.getGreen();
                rouge = colorToSet.getRed();
                int intVal = ((alpha & 0xFF) << 24) | ((rouge & 0xFF) << 16) | ((vert & 0xFF) << 8) | (bleu & 0xFF);
                return intVal;
            }
            return rgb;
        }
    }

    /**
     * Classe interne permettant de changer une couleur sur une image par uen
     * autre.
     */
    private static class FilterColorToColor extends FilterColor {

        protected FilterColorToColor(Color colorToChange, Color newColor) {
            this(colorToChange, newColor, 0);
        }

        protected FilterColorToColor(Color colorToChange, Color newColor, int delta) {
            super(colorToChange, newColor, delta);
        }

        @Override
        protected boolean pixelToChange(int rouge, int vert, int bleue, int alpha) {
            return (rouge <= colorToChange.getRed() + delta && rouge >= colorToChange.getRed() - delta
                    && vert <= colorToChange.getGreen() + delta && vert >= colorToChange.getGreen() - delta
                    && bleue <= colorToChange.getBlue() + delta && bleue >= colorToChange.getBlue() - delta
                    && alpha <= colorToChange.getAlpha() + delta && alpha >= colorToChange.getAlpha() - delta);
        }

        @Override
        protected Color findColorToSet(int rouge, int vert, int bleue, int alpha) {
            return newColor;
        }
    }

    /**
     * Classe interne permettant de rendre transparente une couleur sur une
     * image.
     */
    private static class FilterColorToTransparent extends FilterColor {

        protected FilterColorToTransparent(Color colorToChange) {
            this(colorToChange, 0);
        }

        protected FilterColorToTransparent(Color colorToChange, int delta) {
            super(colorToChange, null, delta);
        }

        @Override
        protected boolean pixelToChange(int rouge, int vert, int bleue, int alpha) {
            return (rouge <= colorToChange.getRed() + delta && rouge >= colorToChange.getRed() - delta
                    && vert <= colorToChange.getGreen() + delta && vert >= colorToChange.getGreen() - delta
                    && bleue <= colorToChange.getBlue() + delta && bleue >= colorToChange.getBlue() - delta);
        }

        @Override
        protected Color findColorToSet(int rouge, int vert, int bleue, int alpha) {
            return ColorManager.transparent();
        }
    }

    /**
     * Classe interne permettant de donner une couleur aux zones transparentes
     * d'une image.
     */
    private static class FilterTransparentToColor extends FilterColor {

        protected FilterTransparentToColor(Color colorToSet) {
            this(colorToSet, 0);
        }

        protected FilterTransparentToColor(Color colorToSet, int delta) {
            super(null, colorToSet, delta);
        }

        @Override
        protected boolean pixelToChange(int rouge, int vert, int bleue, int alpha) {
            return (alpha >= 0 && alpha <= delta);
        }

        @Override
        protected Color findColorToSet(int rouge, int vert, int bleue, int alpha) {
            return newColor;
        }
    }

    /**
     * Classe interne permettant de recolorer chaque pixel d'une image en
     * négatif.
     */
    private static class FilterColorToNegative extends FilterColor {

        protected FilterColorToNegative() {
            super(null, null, 0);
        }

        @Override
        protected boolean pixelToChange(int rouge, int vert, int bleue, int alpha) {
            return (alpha != 0);
        }

        @Override
        protected Color findColorToSet(int rouge, int vert, int bleue, int alpha) {
            int red = 255 - rouge;
            int green = 255 - vert;
            int blue = 255 - bleue;
            return new Color(red, green, blue, alpha);
        }
    }
    }
