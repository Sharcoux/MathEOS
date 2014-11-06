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
import static matheos.graphic.composants.ComposantGraphique.STROKE_SIZE;
import matheos.utils.managers.ColorManager;

/**
 *
 * @author François Billioud
 */
public class DemiDroite extends DroiteAbstraite implements Serializable, Composant.Intersectable {
    private static final long serialVersionUID = 1L;

//    private DemiDroite() {}//pour le JSON
    protected DemiDroite(Point origine, Vecteur vecteur) {
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
    public boolean contient(Point P) {
        return super.contient(P) && vecteur().estDeMemeSigne(new Vecteur(getOrigine(),P));
    }

    public Point projectionSurDemiDroite(Point P) {
        Point H = projeteOrthogonal(P);
        if(this.contient(H)) {return H;}
        return getOrigine();
    }

    @Override
    public int distance2Pixel(Point P, Repere repere) {
        Point H = this.projectionSurDemiDroite(P);
        return H.distance2Pixel(P,repere);
    }

    @Override
    public boolean estEgalA(Composant cg) {
        try {
            DemiDroite d = (DemiDroite)cg;
            return (d.getOrigine().estEgalA(getOrigine()) && d.vecteur().estParallele(vecteur()));
        } catch(Exception e) {return false;}
    }

    @Override
    protected void dessineComposant(Repere repere,Graphics2D g2D) {
        double xMin = repere.getXMin(), xMax = repere.getXMax();
        double yMin = repere.getYMin(), yMax = repere.getYMax();

        double sens = vecteur().x();
        java.awt.Point P1 = repere.reel2Pixel(getOrigine());
        java.awt.Point P2;
        if(sens>0) {
            P2 = repere.reel2Pixel(new Point(xMax, a()*xMax+b()));
        } else { if(sens==0) {
             if(vecteur().y()>0) {
                P2 = repere.reel2Pixel(new Point(getOrigine().x(), yMax));
            } else {
                P2 = repere.reel2Pixel(new Point(getOrigine().x(), yMin));
            }
        } else {
            P2 = repere.reel2Pixel(new Point(xMin, a()*xMin+b()));
        }}
        g2D.drawLine(P1.x, P1.y, P2.x, P2.y);
    }
    
    @Override
    public String getSVGRepresentation(Repere repere) {
        double xMin = repere.getXMin(), xMax = repere.getXMax();
        double yMin = repere.getYMin(), yMax = repere.getYMax();

        double sens = vecteur().x();
        java.awt.Point P1 = repere.reel2Pixel(getOrigine());
        java.awt.Point P2;
        if(sens>0) {
            P2 = repere.reel2Pixel(new Point(xMax, a()*xMax+b()));
        } else { if(sens==0) {
             if(vecteur().y()>0) {
                P2 = repere.reel2Pixel(new Point(getOrigine().x(), yMax));
            } else {
                P2 = repere.reel2Pixel(new Point(getOrigine().x(), yMin));
            }
        } else {
            P2 = repere.reel2Pixel(new Point(xMin, a()*xMin+b()));
        }}
        String s = "<line x1='"+P1.x+"' y1='"+P1.y+"' x2='"+P2.x+"' y2='"+P2.y+"' style='stroke:"+ColorManager.getRGBHexa(getCouleur())+";stroke-width:"+STROKE_SIZE+";' />";
        return s;
    }
    
    @Override
    public List<Point> pointsSupplementaires() {
        List<Point> rep = new LinkedList<>();
        rep.add(new Point(getOrigine().x(),getOrigine().y()) {
            public double x() {return getOrigine().x();}
            public double y() {return getOrigine().y();}
            public boolean dependsOn(ComposantGraphique cg) { return cg.estEgalA(DemiDroite.this); }
        });
        return rep;
    }

    @Override
    public List<Point> pointsDIntersection(Intersectable cg) {
        List<Point> L = super.pointsDIntersection(cg);
        List<Point> reponse = new LinkedList<>();
        for(Point P : L) {if(contient(P)) {reponse.add(P);}}
        return reponse;
    }

    @Override
    public Point projection(Point P) {
        return projectionSurDemiDroite(P);
    }

    public static class AB extends DemiDroite {
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
    public static class AVecteur extends DemiDroite {
        public AVecteur(Point A, Vecteur v) {
            super(A,v);
        }
    }
    public static class Parallele extends DemiDroite {
        private final Ligne l;
        private final boolean deMemeSens;
        public Parallele(Point A, Ligne l, boolean deMemeSens) {
            super(A,deMemeSens ? l.vecteur() : l.vecteur().fois(-1));
            this.l = l;
            this.deMemeSens = deMemeSens;
        }
        @Override
        public Vecteur vecteur() {
            return deMemeSens ? l.vecteur() : l.vecteur().fois(-1);
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(getOrigine()) || cg.estEgalA(l);
        }
    }
    public static class Orthogonale extends DemiDroite {
        private final Ligne l;
        private final Point A;
        private final boolean estDeMemeSens;
        public Orthogonale(Ligne l, Point A) {
            super(l.projeteOrthogonal(A),new Vecteur(l.projeteOrthogonal(A),A));
            this.l = l;
            this.A = A;
            this.estDeMemeSens = l.vecteur().vecteurOrthogonal().estDeMemeSigne(new Vecteur(l.projeteOrthogonal(A),A));
        }
        @Override
        public Point getOrigine() {
            return l.projeteOrthogonal(A);
        }
        @Override
        public Vecteur vecteur() {
            return estDeMemeSens ? l.vecteur().vecteurOrthogonal() : l.vecteur().vecteurOrthogonal().fois(-1);
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(A) || cg.estEgalA(l);
        }
    }
    


}
