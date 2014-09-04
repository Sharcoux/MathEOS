/** «Copyright 2012 François Billioud»
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

package matheos.utils.objets;

import matheos.utils.librairies.ImageTools;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import org.imgscalr.Scalr;

/**
 * Cette classe permet une manipulation plus facile des icônes
 * @author François Billioud
 */

@SuppressWarnings("serial")
public class Icone extends ImageIcon implements Cloneable {

    public static enum TRANSFORMATION {COPIE, MIROIR_HORIZONTAL, MIROIR_VERTICAL, ROTATION_LEFT, ROTATION_RIGHT, INVERSE}
    public static TRANSFORMATION getTransformation(String transformation) {
        switch (transformation) {
            case "hMirror":
                return TRANSFORMATION.MIROIR_HORIZONTAL;
            case "vMirror":
                return TRANSFORMATION.MIROIR_VERTICAL;
            case "lRotate":
                return TRANSFORMATION.ROTATION_LEFT;
            case "rRotate":
                return TRANSFORMATION.ROTATION_RIGHT;
            case "invert":
                return TRANSFORMATION.INVERSE;
            default:
                return TRANSFORMATION.COPIE;
        }
    }
    
    public Icone(){}
    
    /**
     * Crée une icône à partir du seul nom de sa source
     * @param s : nom du fichier source de l'image
     */
    public Icone(String s) {
        super((s==null)?"":s);
    }

    /**
     * Crée une icône à la taille spécifiée
     * @param s : nom du fichier source de l'image
     * @param largeur : largeur attendue pour l'icône
     * @param hauteur : hauteur attendue pour l'icône
     */
    public Icone(String s, int largeur, int hauteur) {
        this(s);
        this.setScaledImage(largeur, hauteur, ImageTools.FIT_EXACT);
    }

    /**
     * Crée une icône à la taille spécifiée
     * @param s : nom du fichier source de l'image
     * @param largeur : largeur attendue pour l'icône
     * @param hauteur : hauteur attendue pour l'icône
     */
    public Icone(ImageIcon icon, TRANSFORMATION transformation) {
        this(icon.getImage());
        this.transformation = transformation;
    }
    private TRANSFORMATION transformation = TRANSFORMATION.COPIE;

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        if(transformation==TRANSFORMATION.COPIE) {super.paintIcon(c, g, x, y);}
    	Graphics2D g2 = (Graphics2D)g.create();
//        Rectangle rect = g.getClip()==null ? new Rectangle(0, 0, getIconWidth(), getIconHeight()) : g.getClipBounds();
        Rectangle rect = new Rectangle(x, y, getIconWidth(), getIconHeight());
        switch(transformation) {
            case INVERSE :
                g2.translate(rect.width, rect.height);
                g2.scale(-1, -1);
            	super.paintIcon(c, g2, -x, -y);
                break;
            case MIROIR_HORIZONTAL :
                g2.translate(rect.width, 0);
                g2.scale(-1, 1);
            	super.paintIcon(c, g2, -x, y);
                break;
            case MIROIR_VERTICAL :
                g2.translate(0, rect.height);
                g2.scale(1, -1);
            	super.paintIcon(c, g2, x, -y);
                break;
            case ROTATION_LEFT :
                g2.rotate(-Math.PI/2);
                g2.translate(-rect.height-y*2, 0);
            	super.paintIcon(c, g2, y, x);
                break;
            case ROTATION_RIGHT :
                g2.rotate(Math.PI/2);
                g2.translate(0, -rect.width-x*2);
            	super.paintIcon(c, g2, y, x);
                break;
        }
    }
    
    /**
     * Crée une icône à la taille spécifiée
     * @param s : nom du fichier source de l'image
     * @param largeur : largeur attendue pour l'icône
     */
    public Icone(String s, int largeur) {
        this(s);
//        this.setScaledImage(largeur, calculHauteur(largeur));
        this.setScaledImage(largeur, calculHauteur(largeur), ImageTools.FIT_TO_WIDTH);
    }


    private void setScaledImage(int largeur, int hauteur, Scalr.Mode mode) {
        if(largeur==0 || hauteur==0) return;
//        this.setImage(this.getImage().getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH));
        this.setImage(ImageTools.getScaledInstance(ImageTools.imageToBufferedImage(this.getImage()), largeur, hauteur, ImageTools.Quality.OPTIMAL, mode));
    }

    /**
     * Crée un icône à partir de l'image spécifiée
     * @param im image de l'icone
     */
    public Icone(Image im){
        super(im);
    }
    
    /**
     * Redimensionne l'icône à la taille spécifiée
     * @param largeur : nouvelle largeur de l'icône
     * @param hauteur : nouvelle hauteur de l'icône
     */
    public void setSize(int largeur, int hauteur) {
        this.setScaledImage(largeur, hauteur, ImageTools.FIT_EXACT);
    }

    /**
     * Redimensionne l'icône à la taille spécifiée
     * @param largeur : nouvelle largeur de l'icône
     */
    public void setSize(int largeur) {
        this.setScaledImage(largeur, calculHauteur(largeur), ImageTools.FIT_TO_WIDTH);
    }

    /** calcul la largeur proportionnelle à la hauteur de l'icone */
    private int calculHauteur(double l) {
        double h = l * this.getIconHeight()/this.getIconWidth();
        return (int) Math.round(h);
    }

    @Override
    public Icone clone(){
        Icone copy = null;
        try {
            copy = (Icone) super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Icone.class.getName()).log(Level.SEVERE, null, ex);
        }
        return copy;
    }
}
