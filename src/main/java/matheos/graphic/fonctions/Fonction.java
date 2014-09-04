/** «Copyright 2014 François Billioud»
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

package matheos.graphic.fonctions;

import matheos.graphic.Repere;
import matheos.graphic.composants.Composant;
import matheos.graphic.composants.ComposantGraphique;
import matheos.graphic.composants.Point;
import matheos.graphic.composants.Polynome;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author François Billioud
 */
public class Fonction extends ComposantGraphique implements Composant.Projetable {

    private final Polynome representation;
    private Fonction(Polynome polynome) {//pour le JSON
        representation = polynome;
    }
    
    public Fonction(LinkedList<Double> listeCoefs) {
        representation = new Polynome(listeCoefs);
    }
    public Fonction(List<Point> listeComposants) {
        representation = new Polynome(listeComposants);
    }

    @Override
    public String getNom() {return representation.getNom();}
    @Override
    public void setNom(String nom) {representation.setNom(nom);}
    @Override
    public Color getCouleur() {return representation.getCouleur();}
    @Override
    public void setCouleur(Color couleur) {representation.setCouleur(couleur);}
    
    @Override
    public void passif(boolean b) {
        representation.passif(b);
    }
    @Override
    public boolean estPassif() {return representation.estPassif();}

    private boolean xPositif = true;
    public void xPositif(boolean b) {
        xPositif = b;
    }
    public boolean xPositif() {return xPositif;}
    
    @Override
    public void dessine(Repere repere, Graphics2D g2D) {
        if(xPositif()) {
            if(repere.getXMax()<=0) {return;}//La fonction n'est pas dans le dessin
            if(repere.getXMin()<0) {
                int largeur = repere.xDistance2Pixel(repere.getXMax()-0);
                int hauteur = repere.yDistance2Pixel(repere.getYMax()-repere.getYMin());
                Repere r = new Repere(largeur, hauteur);
                r.setOrthonormal(false);
                r.setArea(0, repere.getXMax(), repere.getYMin(), repere.getYMax());

                //Création d'un espace partiel sur lequel dessiner
                BufferedImage im = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = im.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,g2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
                g.setStroke(g2D.getStroke());
                g.setFont(g2D.getFont());
                representation.dessine(r, g);
                
                g2D.drawImage(im, repere.xReel2Pixel(0), 0, null);
                return;
            }
        }
        representation.dessine(repere, g2D);
    }
    
    @Override
    protected void dessineComposant(Repere repere, Graphics2D g2D) {
        representation.dessine(repere, g2D);
    }

    @Override
    public int distance2Pixel(Point point, Repere repere) {
        Repere r = repere;
        if(xPositif()) {
            if(repere.getXMin()<0) {
                int largeur = repere.xDistance2Pixel(repere.getXMax()-0);
                int hauteur = repere.yDistance2Pixel(repere.getYMax()-repere.getYMin());
                r = new Repere(largeur, hauteur);
                r.setOrthonormal(false);
                r.setArea(0, repere.getXMax(), repere.getYMin(), repere.getYMax());
            }
        }
        return representation.distance2Pixel(point, r);
    }

    @Override
    public boolean estEgalA(Composant cg) {
        if(cg instanceof Fonction) {return representation.estEgalA(((Fonction)cg).representation);}
        return representation.estEgalA(cg);
    }

//    @Override
//    public List<Point> pointsDIntersection(ComposantGraphique cg) {
//        if(cg instanceof Fonction) {return representation.pointsDIntersection(((Fonction)cg).representation);}
//        return representation.pointsDIntersection(cg);
//    }

    @Override
    public List<Point> pointsSupplementaires() {
        return representation.pointsSupplementaires();
    }

    @Override
    public boolean dependsOn(ComposantGraphique cg) {
        return representation.dependsOn(cg);
    }

    @Override
    public Point projection(Point P) {
        return representation.projection(P);
    }

    @Override
    public Point projeteOrthogonal(Point P) {
        return representation.projeteOrthogonal(P);
    }

}
