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

import java.awt.Color;
import matheos.graphic.OutilsGraph;
import matheos.graphic.Repere;
import matheos.graphic.composants.Texte.Legende;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import matheos.graphic.ListComposant;
import static matheos.graphic.composants.Composant.Legendable;
import static matheos.graphic.composants.ComposantGraphique.STROKE_SIZE;
import matheos.utils.managers.ColorManager;

/**
 *
 * @author François Billioud
 */
public class Arc extends ComposantGraphique implements Serializable, Composant.Projetable, Legendable, Composant.Intersectable, Composant.Identificable {
    private static final long serialVersionUID = 1L;
	
//    private Arc() {}//pour le JSON
    /** @param angle en radian **/
    protected Arc(Point centre, Point depart, double angle) {
        this.centre = centre;
        this.depart = depart;
        this.angle = angle;
    }
    
    private final Point centre;
    private final Point depart;
    private double angle;

    public double rayon() { return OutilsGraph.distance(getCentre(), getDepart()); }
    public Point getCentre() { return centre; }
    public Point getDepart() { return depart; }
    public Point getFin() {
        Vecteur v = new Vecteur(getCentre(),getDepart());
        v = v.rotation(getAngle());
        return getCentre().plus(v);
    }

    /**
     * Renvoie l'angle d'ouverture de l'arc en radian
     * @return double l'angle d'ouverture en radian
     */
    public double getAngle() {
        return angle;
    }

    @Override
    public boolean dependsOn(ComposantGraphique cg) {
        return cg.estEgalA(centre) || cg.estEgalA(depart);
    }
    /**
     * ouvre l'arc jusqu'à teta radians
     * @param teta l'angle d'ouverture en radian
     */
    protected void setAngle(double teta) {
        angle=teta;
    }
    
    @Override
    public int distance2Pixel(Point P, Repere repere) {
        Point H = projectionSurArc(P);
        return H.distance2Pixel(P, repere);
    }

    @Override
    public boolean estEgalA(Composant cg) {
        try {
            Arc C = (Arc)cg;
            return (C.getCentre().estEgalA(getCentre()) && C.getDepart().estEgalA(getDepart()) && Math.abs(getAngle()-C.getAngle())<Repere.ZERO_ABSOLU);//FIXME Attention au cas de 2 arcs de sens opposés, ou deux cercles de départ différent
        } catch(Exception e) {return false;}
    }

    public Point projectionSurCercle(Point P) {
        Vecteur v = new Vecteur(getCentre(),P);
        return getCentre().plus(v.unitaire().fois(rayon()));
    }

    public Point projectionSurArc(Point P) {
        Point H = projectionSurCercle(P);
        if(this.contient(H)) {return H;}
        if(OutilsGraph.distance(getDepart(), H)<=OutilsGraph.distance(getFin(), H)) {return getDepart();}
        return getFin();
    }

    public boolean contient(Point P) {
        if(P==null) {return false;}
        else {
            double angleDCP = OutilsGraph.angle(getDepart(),getCentre(),P);
            if(getAngle()>0) {
                if(angleDCP<0) {angleDCP += 2*Math.PI;}
            }
            else {
                if(angleDCP>0) {angleDCP -= 2*Math.PI;}
            }
            return (Math.abs(getAngle())>2*Math.PI || Math.abs(angleDCP)<Math.abs(getAngle())) && (Math.abs(OutilsGraph.distance(getCentre(),P)-rayon())<Repere.ZERO_ABSOLU);
        }
    }

    public boolean estTangeante(Droite d) {
        Point H = d.projeteOrthogonal(getCentre());
        double CH = OutilsGraph.distance(getCentre(), H);
        return Math.abs(CH-rayon())<Repere.ZERO_ABSOLU;
    }

    @Override
    //Attention : le dessin se fait à partir de centre, rayon et angle
    protected void dessineComposant(Repere repere, Graphics2D g2D) {
        int xCentre = repere.xReel2Pixel(getCentre().x());
        int yCentre = repere.yReel2Pixel(getCentre().y());
        int xRayon = repere.xDistance2Pixel(rayon());
        int yRayon = repere.yDistance2Pixel(rayon());
        double angleDepart = (new Vecteur(getCentre(),getDepart())).orientation()*180/Math.PI;//rad2deg
        //double angleFin = (new Vecteur(centre,fin)).orientation2Pixel(repere)*180/Math.PI;
        double angleTot = getAngle()*180/Math.PI;//rad2deg

//        g2D.drawArc(xCentre-xRayon,yCentre-yRayon,xRayon*2,yRayon*2,(int)angleDepart,(int)(angleTot));
        if(Math.abs(angleTot)>=360) {
            g2D.drawOval(xCentre-xRayon, yCentre-yRayon, 2*xRayon, 2*yRayon);
        } else {
            g2D.draw(new Arc2D.Double(xCentre-xRayon, yCentre-yRayon, 2*xRayon, 2*yRayon, angleDepart, angleTot, Arc2D.Double.OPEN));
        }
        legendeSupport.dessine(repere, g2D, getDefaultLegendeCoord(repere));
        if(!marque.isEmpty()) {dessineMarque(repere, g2D);}
    }

    @Override
    public String getSVGRepresentation(Repere repere) {
        int xCentre = repere.xReel2Pixel(getCentre().x());
        int yCentre = repere.yReel2Pixel(getCentre().y());
        int xRayon = repere.xDistance2Pixel(rayon());
        int yRayon = repere.yDistance2Pixel(rayon());
        double angleDepart = (new Vecteur(getCentre(),getDepart())).orientation()*180/Math.PI;//rad2deg
        //double angleFin = (new Vecteur(centre,fin)).orientation2Pixel(repere)*180/Math.PI;
        double angleTot = getAngle()*180/Math.PI;//rad2deg

//        g2D.drawArc(xCentre-xRayon,yCentre-yRayon,xRayon*2,yRayon*2,(int)angleDepart,(int)(angleTot));
        String s;
        if(Math.abs(angleTot)>=360) {
            s = "<ellipse cx='"+xCentre+"' cy='"+yCentre+"' rx='"+xRayon+"' ry='"+yRayon+"' style='stroke:"+ColorManager.getRGBHexa(getCouleur())+";stroke-width:"+STROKE_SIZE+";' />";
        } else {
            s = "";
        }
        if(getLegende()!=null) {s+="\n"+getLegende().getSVGRepresentation(repere);}
        if(!marque.isEmpty()) {s+="\n"+marqueReperesentation(repere).getSVGRepresentation(repere);}
        return s;
    }
    
    public Point getDefaultLegendeCoord(Repere repere) {
        Vecteur v = new Vecteur(getCentre(),getDepart()).rotation(getAngle()/2);//vecteur de la bissectrice
        double a = v.orientation();
        double texteWidth = getLegende()==null ? 0 : repere.xDistance2Reel(getLegende().getTextComponent().getWidth());
        double texteHeight = getLegende()==null ? 0 : repere.yDistance2Reel(getLegende().getTextComponent().getHeight());
        if(a<=0) {
            if(a>-Math.PI/2) {return getCentre().plus(v);}
            else {return getCentre().plus(v).plus(new Vecteur(-texteWidth,0));}
        } else {
            if(a>Math.PI/2) {return getCentre().plus(v).plus(new Vecteur(-texteWidth,texteHeight));}
            else {return getCentre().plus(v).plus(new Vecteur(0,texteHeight));}
        }
    }
    
    //gère la légende
    private final transient SupportLegende legendeSupport = new SupportLegende(this);
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
    
    //gère le repérage des angles identiques
    private static final int LONGUEUR_MARQUE = 8;
    private static final int ECART_MARQUE = 4;
    private String marque = "";
    public void setMarque(String s) {
        marque = s;
    }
    /** permet de dessiner une marque de reconnaissance sur un angle afin de signifier qu'il est identique à un autre **/
    private void dessineMarque(Repere repere,Graphics2D g2D) {
        marqueReperesentation(repere).dessine(repere, g2D);
    }
    
    private ListComposant marqueReperesentation(Repere repere) {
        ListComposant L = new ListComposant();
        Vecteur v = new Vecteur(getCentre(),getDepart()).rotation(getAngle()/2);//vecteur de la bissectrice
        Point I = getCentre().plus(v);
        v = repere.distance2Reel(LONGUEUR_MARQUE, v);
        Segment s1=null,s2=null;
        switch (marque) {
            case "/":
                s1 = new Segment(I.moins(v),I.plus(v));
                break;
            case "//":
                Vecteur u = repere.distance2Reel(ECART_MARQUE, v.vecteurOrthogonal());
                Point I1 = I.plus(u), I2 = I.moins(u);
                s1 = new Segment(I1.moins(v),I1.plus(v));
                s2 = new Segment(I2.moins(v),I2.plus(v));
                break;
            case "X":
                v = v.rotation(Math.PI/4);
                s1 = new Segment(I.moins(v),I.plus(v));
                v = v.vecteurOrthogonal();
                s2 = new Segment(I.moins(v),I.plus(v));
                break;
        }
        if(s1!=null) {
            s1.setCouleur(getCouleur());
            L.add(s1);
        }
        if(s2!=null) {
            s2.setCouleur(getCouleur());
            L.add(s2);
        }
        return L;
    }

    public LinkedList<Point> intersection(Arc c) {
        if(this.estEgalA(c)) {return new LinkedList<>();}
        LinkedList<Point> reponse = new LinkedList<>();
        double CC = OutilsGraph.distance(getCentre(),c.getCentre());
        double dist = CC-rayon()-c.rayon();
        if(Math.abs(dist)<Repere.ZERO_ABSOLU) {
            reponse.add(c.projectionSurCercle(getCentre()));
            return reponse;
        }
        else {
            if(dist>0 || CC<Repere.ZERO_ABSOLU) {return new LinkedList<>();}
            else {
                Vecteur v = (new Vecteur(getCentre(),c.getCentre())).unitaire();
                double rayon = rayon(), cRayon = c.rayon();
                Point H = getCentre().plus(v.fois(CC/2)).plus(v.fois((rayon*rayon - cRayon*cRayon)/CC/2));
                Droite d = (new Droite.AB(getCentre(),c.getCentre())).droiteOrthogonale(H);
                LinkedList<Point> L = d.intersection(c);
                Point P1=(Point)L.poll();
                if(this.contient(P1)) {reponse.add(P1);}
                Point P2=(Point)L.peek();//peek() retourne null si L est vide
                if(this.contient(P2)) {reponse.add(P2);}
                return reponse;
            }
        }
    }

    @Override
    public List<Point> pointsSupplementaires() {
        List<Point> rep = new LinkedList<>();
        rep.add(new Point(getCentre().x(),getCentre().y()) {
            public double x() {return getCentre().x();}
            public double y() {return getCentre().y();}
            public boolean dependsOn(ComposantGraphique cg) {return cg.estEgalA(getCentre());}
        });
        if(Math.abs(getAngle())<2*Math.PI) {
            rep.add(new Point(getDepart().x(),getDepart().y()) {
                public double x() {return getDepart().x();}
                public double y() {return getDepart().y();}
                public boolean dependsOn(ComposantGraphique cg) {return cg.estEgalA(getDepart());}
            });
            rep.add(new Point(getFin().x(), getFin().y()) {
                public double x() {return getFin().x();}
                public double y() {return getFin().y();}
                public boolean dependsOn(ComposantGraphique cg) {return cg.estEgalA(getFin());}
            });
        }
        return rep;
    }

    @Override
    public List<Point> pointsDIntersection(Intersectable cg) {
        if(cg instanceof Arc) {return intersection((Arc)cg);}
        return cg.pointsDIntersection(this);
    }

    @Override
    public Point projection(Point P) {
        return projectionSurArc(P);
    }

    @Override
    public Point projeteOrthogonal(Point P) {
        return projectionSurCercle(P);
    }

//    public Arc(Point C, Point r, Point D, Point F) {
//        this(C,D,F);
//        setRayon(OutilsGraph.distance(C, r));
//    }
    
//    /**
//     * Construit un arc de centre C et de rayon r entre les points D et F
//     * @param C centre du cercle
//     * @param r rayon du cercle
//     * @param D origine de l'arc (si CD>r, D sera projeté sur l'arc)
//     * @param F fin de l'arc (si CF>r, F sera projeté sur l'arc)
//     */
//    public Arc(Point C, double r, Point D, Point F) {this(C,D,F);setRayon(r);}
//

    public static class ABC extends Arc {
        private final Point fin;
        public ABC(Point centre, Point depart, Point fin) {
            super(centre, depart, OutilsGraph.angle(depart, centre, fin));
            this.fin = fin;
        }

        @Override
        public Point getFin() {
            return fin;
        }

        @Override
        public double getAngle() {
            return OutilsGraph.angle(getDepart(), getCentre(), fin);
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(getCentre()) || cg.estEgalA(getDepart()) || cg.estEgalA(fin);
        }
    }
    
    public static class Angulaire extends Arc {
        public Angulaire(Point centre, Point depart, double radian) {
            super(centre, depart, radian);
        }
    }
    
    
    public static class Rayon extends Arc {
        private final double rayon;
        /**
         * Crée un arc définit par un centre, un rayon, un point de départ et une ouverture
         * @param centre centre de l'arc
         * @param rayon rayon de l'arc
         * @param depart point par lequel passe l'axe de départ
         * @param angle l'ouverture en radian
         */
        public Rayon(Point centre, double rayon, Point depart, double angle) {
            super(centre, centre.plus(new Vecteur(centre, depart).unitaire().fois(rayon)), angle);
            this.rayon = rayon;
        }
        
        @Override
        public double rayon() {return rayon;}
        
        @Override
        public Point getDepart() {
            Vecteur v = new Vecteur(getCentre(), super.getDepart()).unitaire().fois(rayon());
            return getCentre().plus(v);
        }
    }
    
    public static class Cercle extends Arc {
        private final Segment diametre;
        public Cercle(Segment diametre) {
            super(diametre.milieu(), diametre.getA(), Math.PI*2.);
            this.diametre = diametre;
        }

        @Override
        public double rayon() {
            return diametre.longueur()/2;
        }

        @Override
        public Point getCentre() {
            return diametre.milieu();
        }

        @Override
        public Point getDepart() {
            return diametre.getA();
        }

        @Override
        public Point getFin() {
            return getDepart();
        }

        @Override
        public double getAngle() {
            return 2*Math.PI;
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(diametre);
        }
    }
    
    public static class Editable extends Arc implements Draggable {
        public Editable(Point centre, Point.XY depart, double angle) {
            super(centre, depart, angle);
        }

        @Override
        public void drag(Vecteur v) {}
        @Override
        public void setPosition(Point P) {
            Point H = projectionSurCercle(P);
            if(contient(H)) {setRayon(OutilsGraph.distance(getCentre(), P));}
            else {setAngle(OutilsGraph.angle(getDepart(), getCentre(), P));}
        }
        
        public void setRayon(double r) {
            Vecteur v = new Vecteur(getCentre(),getDepart()).unitaire().fois(r);
            ((Point.XY)getDepart()).setPosition(getCentre().plus(v));
        }

        @Override
        public void setPosition(double x, double y) {
            this.setPosition(new Point(x,y));
        }
    
        @Override
        public void setAngle(double r) {
            super.setAngle(r);
        }
    }
    
}
