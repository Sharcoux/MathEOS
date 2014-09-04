/**
 * «Copyright 2013 François Billioud»
 *
 * This file is part of MathEOS.
 *
 * MathEOS is free software: you can redistribute it and/or modify under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * MathEOS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY.
 *
 * You should have received a copy of the GNU General Public License along with
 * MathEOS. If not, see <http://www.gnu.org/licenses/>.
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
package matheos.graphic;

import matheos.graphic.composants.*;
import java.util.Collection;

/**
 *
 * @author François Billioud
 */
public abstract class OutilsGraph {

    /**
     * renvoie l'angle que fait le segment [AB] avec l'axe horizontal (Ox)
     * @return : angle en radian
     */
    public static double orientation(Point A,Point B) {
        return (new Vecteur(A,B)).orientation();
    }

    /**
     * renvoie l'angle que fait la ligne l1 avec la ligne l2
     * @return : angle en radian
     */
    public static double angle(Ligne l1, Ligne l2) {
        return (l2.orientation()-l1.orientation());
    }

    /**
     * renvoie l'angle que fait le vecteur u avec le vecteur v
     * @return : angle en radian
     */
    public static double angle(Vecteur u, Vecteur v) {
        return (v.orientation()-u.orientation());
    }

    /**
     * renvoie l'angle que fait le segment [BA] avec le segment [BC] (càd. l'angle ABC)
     * Attention, l'angle peut varier à Pi près.
     * @return : angle en radian
     */
    public static double angle(Point A, Point B, Point C) {
        return angle(new Segment.AB(B,A),new Segment.AB(B,C));
    }

    public static double distance(Point A, Point B) {
        return (new Vecteur(A,B)).longueur();
    }

    /** renvoie la nouvelle position du point cible après avoir arrondi la distance origine-cible **/
    public static Point approxime(Point origine, Point cible, double precision) {
        Vecteur v = new Vecteur(origine, cible);
        return origine.plus(v.unitaire().fois(approxime(v.longueur(),precision)));
    }
    
    /** Arrondi avec la précision indiquée. Par exemple, 0.1 pour un résultat au dixième **/
    private static double approxime(double d,double precision) {
        return precision<1 ? Math.round(d/precision)/((1/precision)) : Math.round(d/precision)*precision;
    }
    
    public static Point milieu(Point A, Point B) {
        return new Point.Milieu(A,B);
    }

    public static Point barycentre(Point A, double p1, Point B, double p2) {
        if(p1+p2==0) {return null;}
        else {
            Vecteur v = new Vecteur(A,B);
            return A.plus(v.fois(p2/(p1+p2)));
        }
    }

    private static double moyenneX(Collection<? extends Point> points) {
        double xMoy = 0.;
        for(Point P : points) {
            xMoy += P.x();
        }
        return xMoy/points.size();
    }
    private static double moyenneY(Collection<? extends Point> points) {
        double yMoy = 0.;
        for(Point P : points) {
            yMoy += P.y();
        }
        return yMoy/points.size();
    }
    public static Point barycentre(final Collection<? extends Point> points) {
        if(points==null || points.size()<1) {return null;}
        return new Point((Point)points.toArray()[0]) {
            private final Collection<? extends Point> pts = points;
            @Override
            public double x() {
                return moyenneX(pts);
            }
            @Override
            public double y() {
                return moyenneY(pts);
            }
            @Override
            public boolean dependsOn(ComposantGraphique cg) {
                return cg instanceof Point && pts.contains((Point)cg);
            }
        };
    }

    public static Point intersectionSimple(Ligne d1, Ligne d2) {
        if(d1.vecteur().estParallele(d2.vecteur())) {return null;}
        else {
            double ux = d1.vecteur().x();
            double uy = d1.vecteur().y();
            double vx = d2.vecteur().x();
            double vy = d2.vecteur().y();
            double xA = d1.getOrigine().x();
            double yA = d1.getOrigine().y();
            double xB = d2.getOrigine().x();
            double yB = d2.getOrigine().y();
            double x = (ux*(vx*(yA-yB) + vy*xB) - uy*vx*xA)/(ux*vy - uy*vx);
            double y = (uy*(vy*(xA-xB) + vx*yB) - ux*vy*yA)/(uy*vx - ux*vy);
            Point P = new Point.XY(x,y);
            return d1.contient(P) && d2.contient(P) ? P : null;
        }
    }

}
