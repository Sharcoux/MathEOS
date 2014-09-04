/** «Copyright 2013 François Billioud»
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

package matheos.graphic.composants;

import matheos.graphic.Repere;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author François Billioud
 */
public class Droite extends DroiteAbstraite implements Serializable {
    private static final long serialVersionUID = 1L;

//    private Droite() {}//pour le JSON
    public Droite(Point origine, Vecteur vecteur) {
        this.origine = origine;
        this.vectDir = vecteur;
    }
    private final Point origine;
    private final Vecteur vectDir;

    @Override
    public Vecteur vecteur() {
        return vectDir;
    }

    @Override
    public Point getOrigine() {
        return origine;
    }

    @Override
    public boolean dependsOn(ComposantGraphique cg) {
        return cg.estEgalA(origine);
    }

    @Override
    public int distance2Pixel(Point P, Repere repere) {
        Point H = this.projeteOrthogonal(P);
        return P.distance2Pixel(H,repere);
    }

    public double y(double x) {
        return this.a()*x + this.b();
    }
    public double x(double y) {
        return (y-this.b())/this.a();
    }

    @Override
    public boolean estEgalA(Composant cg) {
        try {
            Droite d = (Droite)cg;
            return (this.contient(d.getOrigine()) && d.vecteur().estParallele(vecteur()));
        } catch(Exception e) {return false;}
    }

    @Override
    protected void dessineComposant(Repere repere,Graphics2D g2D) {
        double xMin = repere.getXMin(), xMax = repere.getXMax();
        double yMin = repere.getYMin(), yMax = repere.getYMax();

        int xA, yA, xB, yB;
        
        if(vecteur().x()!=0) {
            xA = 0;
            yA = repere.yReel2Pixel(y(xMin));
            xB = repere.largeur();
            yB = repere.yReel2Pixel(y(xMax));
        } else {
            xA = repere.xReel2Pixel(getOrigine().x());
            yA = repere.hauteur();
            xB = xA;
            yB = 0;
        }
        g2D.drawLine(xA, yA, xB, yB);
        if(!getNom().equals("")) {
            if(vecteur().x()==0) { g2D.drawString(getNom(), xB+15, yB+15); }
            else {
                if(y(xMax)<yMax && y(xMax)>yMin) {g2D.drawString(getNom(), xB-35, yB);}
                else {
                    if(a()>0) {g2D.drawString(getNom(), repere.xReel2Pixel(x(yMax))-25, repere.yReel2Pixel(yMax)+15);}
                    else {g2D.drawString(getNom(), repere.xReel2Pixel(x(yMin))+15, repere.yReel2Pixel(yMin)-15);}
                }
            }
        }
    }

    @Override
    public List<Point> pointsSupplementaires() {
        return new LinkedList<>();
    }


    @Override
    public Point projection(Point P) {
        return projeteOrthogonal(P);
    }
    
    public static class Orthogonale extends Droite {
        private final Ligne perpendiculaire;
        public Orthogonale(Ligne perpendiculaire, Point passantPar) {
            super(passantPar, perpendiculaire.vecteur().vecteurOrthogonal());
            this.perpendiculaire = perpendiculaire;
        }
        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(perpendiculaire)||cg.estEgalA(getOrigine());
        }

        @Override
        public Vecteur vecteur() {
            return perpendiculaire.vecteur().vecteurOrthogonal();
        }
    }
    public static class Tangente extends Droite {
        private final Arc c;
        private final Point P;
        public Tangente(Arc c, Point P) {
            super(c.projectionSurCercle(P), new Vecteur(c.getCentre(),P).vecteurOrthogonal());
            this.c = c;
            this.P = P;
        }

        @Override
        public Vecteur vecteur() {
            return new Vecteur(c.getCentre(),P).vecteurOrthogonal();
        }

        @Override
        public Point getOrigine() {
            return c.projectionSurCercle(P);
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(c) || cg.estEgalA(P);
        }
        
    }
    public static class Parallele extends Droite {
        private final Ligne parallele;
        public Parallele(Ligne parallele, Point passantPar) {
            super(passantPar, parallele.vecteur());
            this.parallele = parallele;
        }

        @Override
        public Vecteur vecteur() {
            return parallele.vecteur();
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(getOrigine()) || cg.estEgalA(parallele);
        }
    }
    public static class AB extends Droite {
        private final Point B;
        public AB(Point A, Point B) {
            super(A, new Vecteur(A,B));
            this.B = B;
        }

        @Override
        public Vecteur vecteur() {
            return new Vecteur(getOrigine(),B);
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(getOrigine())||cg.estEgalA(B);
        }
    }
    public static class AVecteur extends Droite {
        public AVecteur(Point A, Vecteur v) {
            super(A,v);
        }
    }
}
