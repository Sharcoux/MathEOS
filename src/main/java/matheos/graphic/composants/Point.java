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
import matheos.graphic.composants.Composant.Legendable;
import matheos.graphic.composants.Texte.Legende;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import matheos.utils.managers.ColorManager;
/**
 *
 * @author François Billioud
 */
public class Point extends ComposantGraphique implements Serializable, Legendable {
    private static final long serialVersionUID = 1L;

//    public Point() {}//pour le JSON
    private double x;
    private double y;
    protected Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    protected Point(Point P) {
        this.x = P.x();
        this.y = P.y();
    }
    public double x() { return x; }
    public double y() { return y; }
    protected void x(double posX) {x=posX;}
    protected void y(double posY) {y=posY;}

    @Override
    public boolean dependsOn(ComposantGraphique cg) {
        return false;
    }

    @Override
    public int distance2Pixel(Point P, Repere repere) {
        return repere.distance2Pixel(new Vecteur(this,P));
    }

    @Override
    public boolean estEgalA(Composant cg) {
        try {
            Point P = (Point)cg;
            return (Math.abs(P.x()-x())<Repere.ZERO_ABSOLU && Math.abs(P.y()-y())<Repere.ZERO_ABSOLU);
        } catch(Exception e) {return false;}
    }

    public boolean estDansLEcran(Repere repere) {
        double xMin = repere.getXMin(), xMax = repere.getXMax();
        double yMin = repere.getYMin(), yMax = repere.getYMax();
        double X = x(), Y = y();
        return (X>xMin && X<xMax && Y>yMin && Y<yMax);
    }

    public Point plus(Vecteur v) {
        return new Point(x()+v.x(),y()+v.y());
    }

    public Point moins(Vecteur v) {
        return new Point(x()-v.x(),y()-v.y());
    }

    //gère la légende
    private final SupportLegende legendeSupport = new SupportLegende(this);
    @Override
    public void setLegende(String texte) {legendeSupport.setLegende(texte);}
    @Override
    public void setLegende(Legende legende) {legendeSupport.setLegende(legende);}
    @Override
    public void setLegendeColor(Color c) {legendeSupport.setCouleur(c);}
    @Override
    public Legende getLegende() {return legendeSupport.getLegende();}
    @Override
    public void fireLegendeChanged(Legende oldOne, Legende newOne) {
        firePropertyChange(LEGENDE_PROPERTY, oldOne, newOne);
    }

    @Override
    public void setCouleur(Color c) {
        super.setCouleur(c);
        if(getNom()!=null && !getNom().isEmpty()) {setLegendeColor(c);}
    }

    @Override
    public void setNom(String nom) {
        super.setNom(nom);
        setLegende(nom);
    }
        
    @Override
    protected void dessineComposant(Repere repere,Graphics2D g2D) {
        int xA=repere.xReel2Pixel(x());
        int yA=repere.yReel2Pixel(y());
        g2D.drawLine(xA-10,yA,xA+10,yA);
        g2D.drawLine(xA,yA+10,xA,yA-10);

        legendeSupport.dessine(repere, g2D, getDefaultLegendeCoord(repere));
    }
    
    @Override
    public String getSVGRepresentation(Repere repere) {
        int xA=repere.xReel2Pixel(x());
        int yA=repere.yReel2Pixel(y());
        return "<line x1='"+(xA-10)+"' y1='"+yA+"' x2='"+(xA+10)+"' y2='"+yA+"' style='stroke:"+ColorManager.getRGBHexa(getCouleur())+";stroke-width:"+STROKE_SIZE+";' />"
                +"\n"+"<line x1='"+xA+"' y1='"+(yA+10)+"' x2='"+xA+"' y2='"+(yA-10)+"' style='stroke:"+ColorManager.getRGBHexa(getCouleur())+";stroke-width:"+STROKE_SIZE+";' />";
    }

    private static final int DECALAGE = 30 ;
    public Point getDefaultLegendeCoord(Repere repere) {
        return new Point(x()-repere.xDistance2Reel(DECALAGE),y()+repere.yDistance2Reel(DECALAGE));
    }

    @Override
    public List<Point> pointsSupplementaires() {
        return new LinkedList<>();
    }

    public static class XY extends Point implements Draggable {

        public XY(double x, double y) {
            super(x,y);
        }
        public XY(Point P) {
            this(P.x(),P.y());
        }
        
        /**
         * Positionne le point à la poition réelle (posX,posY) dans le repère établit
         * @param posX : abscisse désirée
         * @param posY : ordonnée désirée
         */
        @Override
        public void setPosition(double posX,double posY) {
            Point old = new Point.XY(x(), y());
            x(posX);
            y(posY);
            Point P = new Point.XY(x(), y());
            firePropertyChange(COORDINATE_PROPERTY, old, P);
        }
        
        @Override
        public void setPosition(Point P) {
            setPosition(P.x(),P.y());
        }

        @Override
        public void drag(Vecteur v) {
            setPosition(this.plus(v));
        }

    }
    public static class Milieu extends Point {
        private Point A, B;
        public Milieu(Point A, Point B) {
            super((A.x()+B.x())/2.,(A.y()+B.y())/2.);
            this.A = A;
            this.B = B;
        }

        @Override
        public double x() {
            return (A.x()+B.x())/2.;
        }

        @Override
        public double y() {
            return (A.y()+B.y())/2.;
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(A) || cg.estEgalA(B);
        }
        
    }
    public static class Intersection extends Point {
        private final Intersectable cg1;
        private final Intersectable cg2;
        private final int i;
        public Intersection(Intersectable cg1, Intersectable cg2) {
            this(cg1, cg2, 0);
        }
        public Intersection(Intersectable cg1, Intersectable cg2, int index) {
            super(0,0);
            this.cg1 = cg1;
            this.cg2 = cg2;
            this.i = index;
            if(index!=1 && index!=0) {throw new IndexOutOfBoundsException("unreachable index : "+index);}
        }

        @Override
        public double x() {
            List<Point> L = cg1.pointsDIntersection(cg2);
            try {return L.get(i).x();}
            catch(IndexOutOfBoundsException e) {firePropertyChange(EXIST_PROPERTY, true, false);return 0;}
        }

        @Override
        public double y() {
            List<Point> L = cg1.pointsDIntersection(cg2);
            try {return L.get(i).y();}
            catch(IndexOutOfBoundsException e) {firePropertyChange(EXIST_PROPERTY, true, false);return 0;}
        }
        
        @Override
        public void dessineComposant(Repere r, Graphics2D g2D) {
            List<Point> L = cg1.pointsDIntersection(cg2);
            if(L.size()>i) {
                Point P = L.get(i);
                P.dessineComposant(r, g2D);
            }
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(cg1) || cg.estEgalA(cg2);
        }
    }
    
}
