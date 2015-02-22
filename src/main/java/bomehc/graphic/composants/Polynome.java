/** «Copyright 2014 François Billioud»
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
package bomehc.graphic.composants;

import java.awt.Color;
import bomehc.graphic.Repere;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import bomehc.graphic.composants.Composant.Legendable;
import static bomehc.graphic.composants.Composant.Legendable.LEGENDE_PROPERTY;
import static bomehc.graphic.composants.ComposantGraphique.STROKE_SIZE;
import bomehc.utils.managers.ColorManager;

/**
 *
 * @author François Billioud, Tristan
 */
@SuppressWarnings("serial")
public class Polynome extends ComposantGraphique implements Composant.Projetable, Composant.Intersectable, Legendable {

    /** la précision du tracé */
    public static final int NB_POINTS_DU_TRACE = 200;//TODO : rendre non final et éditable
    /** la liste des points par lesquels passe la fonction */
    protected List<Point> pointsDeConstruction = new LinkedList<>();
    protected List<Double> listeCoefs = new LinkedList<>();
    
    transient int[] xPoints = new int[NB_POINTS_DU_TRACE];
    transient int[] yPoints = new int[NB_POINTS_DU_TRACE];
    
    private Polynome(List<Double> listeCoefs, List<Point> listePoints) {//pour le JSON
        pointsDeConstruction = listePoints;
        this.listeCoefs = listeCoefs;
    }

    public Polynome(LinkedList<Double> listeCoefs) {
        if (listeCoefs == null) {
            System.out.println("ERREUR Polynome.constr : pas de coefs");
            this.listeCoefs.add(0d);
        } else {
            this.listeCoefs = listeCoefs;
        }
    }

    public Polynome(List<Point> listeComposants) {
        pointsDeConstruction = listeComposants;

        LinkedList<Double> coefsPolynomeFinal = new LinkedList<>();
        coefsPolynomeFinal.add(0.);
        Polynome polynomeFinal = new Polynome(coefsPolynomeFinal);

        for (Point A : pointsDeConstruction) {
            LinkedList<Double> coefsPolynome1 = new LinkedList<>();
            coefsPolynome1.add(A.y());
            Polynome polynome1 = new Polynome(coefsPolynome1);

            LinkedList<Double> coefsPolynome2 = new LinkedList<>();
            coefsPolynome2.add(1.);
            Polynome polynome2 = new Polynome(coefsPolynome2);

            for (ComposantGraphique cgB : pointsDeConstruction) {
                Point B = (Point) cgB;
                if (!A.estEgalA(B)) {
                    LinkedList<Double> coefsPolynome3 = new LinkedList<>();
                    coefsPolynome3.add(B.x() / (B.x() - A.x()));
                    coefsPolynome3.add(1. / (A.x() - B.x()));
                    Polynome polynome3 = new Polynome(coefsPolynome3);

                    polynome2 = multiplier(polynome2, polynome3);
                }
            }

            polynomeFinal = additionner(polynomeFinal, multiplier(polynome1, polynome2));
        }

        this.listeCoefs = polynomeFinal.listeCoefs;
    }

    public List<Double> getListeCoefs() {
        return listeCoefs;
    }

    public double f(double x) {
        double resultat = 0;
        if (!listeCoefs.isEmpty()) {
            for (int i = listeCoefs.size() - 1; i >= 0; i--) {
                resultat = resultat * x + listeCoefs.get(i);
            }
        }
        return resultat;
    }

    public String equation() {
        String resultat = "y = ";
        ListIterator<Double> L = listeCoefs.listIterator();
        int i = 0;
        while (L.hasNext()) {
            resultat += L.next() + " . x^" + i;
            if (L.hasNext()) {
                resultat += " + ";
            }
            i++;
        }
        return resultat;
    }
    
    //gère la légende
    private final transient SupportLegende legendeSupport = new SupportLegende(this);
    @Override
    public void setLegende(String texte) {legendeSupport.setLegende(texte);}
    @Override
    public void setLegende(Texte.Legende legende) {legendeSupport.setLegende(legende);}
    @Override
    public void setLegendeColor(Color c) {legendeSupport.setCouleur(c);}
    @Override
    public Texte.Legende getLegende() {return legendeSupport.getLegende();}
    @Override
    public void fireLegendeChanged(Texte.Legende oldOne, Texte.Legende newOne) {
        firePropertyChange(LEGENDE_PROPERTY, oldOne, newOne);
    }

    @Override
    protected void dessineComposant(Repere repere, Graphics2D g2D) {
        double xMin = repere.getXMin(), xMax = repere.getXMax();
        double yMin = repere.getYMin(), yMax = repere.getYMax();
        double pas = (xMax - xMin) / (NB_POINTS_DU_TRACE - 1);
        double limiteHaut = 2 * yMax - yMin;//les points trop au-dessus ou au-dessous de l'espace de dessin auront la valeur limite (évite des tracés allant à +00)
        double limiteBas = 2 * yMin - yMax;

        double x = xMin;
        for (int i = 0; i < NB_POINTS_DU_TRACE; i++) {
            xPoints[i] = repere.xReel2Pixel(x);
            if (f(x) > limiteHaut) {
                yPoints[i] = repere.yReel2Pixel(limiteHaut);
            } else if (f(x) < limiteBas) {
                yPoints[i] = repere.yReel2Pixel(limiteBas);
            } else {
                yPoints[i] = repere.yReel2Pixel(f(x));
            }
            x += pas;
        }

        if(g2D==null) {return;}//dans le cas du HACK pour projeteOrthogonal
        g2D.drawPolyline(xPoints, yPoints, NB_POINTS_DU_TRACE);
        
        if(getLegende()!=null) {legendeSupport.dessine(repere, g2D, repere.pixel2Reel(getDefaultLegendeCoord(repere)));}
    }
    
    public java.awt.Point getDefaultLegendeCoord(Repere repere) {
        double xMin = repere.getXMin(), xMax = repere.getXMax();
        double yMin = repere.getYMin(), yMax = repere.getYMax();
        double pas = (xMax - xMin) / (NB_POINTS_DU_TRACE - 1);
        
        java.awt.Point legende = repere.reel2Pixel(repere.pixel2Reel(new java.awt.Point(0,0)).plus(getLegende().getDeplacement()));
        Rectangle textArea = getLegende().getTextComponent().getBounds();
        if(pas==0) { return new java.awt.Point(xPoints[0]+15, yPoints[NB_POINTS_DU_TRACE-1]+15); }
        else {
            double y = f(xMax);
            int xBPixel = xPoints[NB_POINTS_DU_TRACE-1], yBPixel = yPoints[NB_POINTS_DU_TRACE-1];
            if(y<yMax&& y>yMin) {return new java.awt.Point(xBPixel-5-textArea.width-Math.max(legende.x, 0), yBPixel);}//le point est à droite
            else {
                //On cherche le dernier point encore dans l'écran
                int iMax = -1;
                int yMaxPixel = repere.hauteur(), yMinPixel = 0;
                for(int i = 0; i<NB_POINTS_DU_TRACE; i++) {
                    if(yPoints[i]<=yMaxPixel && yPoints[i]>=yMinPixel) {iMax = i;}
                }
                if(iMax>=0) {
                    if(yPoints[iMax]<(yMaxPixel-yMinPixel)/2) {return new java.awt.Point(xPoints[iMax]-5-textArea.width, yPoints[iMax]+5-Math.min(0, legende.y));}//Le dernier point est en haut
                    else {return new java.awt.Point(xPoints[iMax]-5-textArea.width, yPoints[iMax]-5-textArea.height-Math.max(0, legende.y));}//le dernier point est en bas
                } else {
                    return new java.awt.Point(-100, -100);//hors du graph
                }
            }
        }
    }
    
    @Override
    public String getSVGRepresentation(Repere repere) {
        String s = "<polyline points='";
        for(int i=0; i<NB_POINTS_DU_TRACE; i++) {
            if(i>0) {s+=" ";}
            s+=xPoints[i]+","+yPoints[i];
        }
        s+="' style='stroke:"+ColorManager.getRGBHexa(getCouleur())+";stroke-width:"+STROKE_SIZE+";' />";
        if(getLegende()!=null) {s+="\n"+getLegende().getSVGRepresentation(repere);}
        return s;
    }

    public static Polynome additionner(Polynome polynome1, Polynome polynome2) {
        List<Double> coefsPolynome1 = polynome1.listeCoefs;
        List<Double> coefsPolynome2 = polynome2.listeCoefs;
        LinkedList<Double> coefsPolynomeSom = new LinkedList<>();
        ListIterator<Double> L1 = coefsPolynome1.listIterator();
        ListIterator<Double> L2 = coefsPolynome2.listIterator();

        while (L1.hasNext() && L2.hasNext()) {
            coefsPolynomeSom.add(L1.next() + L2.next());
        }
        while (L1.hasNext()) {
            coefsPolynomeSom.add(L1.next());
        }
        while (L2.hasNext()) {
            coefsPolynomeSom.add(L2.next());
        }

        return new Polynome(coefsPolynomeSom);
    }

    public static Polynome multiplier(Polynome polynome1, Polynome polynome2) {
        List<Double> coefsPolynome1 = polynome1.listeCoefs;
        List<Double> coefsPolynome2 = polynome2.listeCoefs;
        LinkedList<Double> coefsPolynomeProd = new LinkedList<>();
        int longueurPolynome1 = coefsPolynome1.size();
        int longueurPolynome2 = coefsPolynome2.size();

        for (int i = 0; i < longueurPolynome1 + longueurPolynome2 - 1; i++) {
            double somme = 0.;
            for (int k = 0; k < longueurPolynome1; k++) {
                if (0 <= i - k && i - k < longueurPolynome2) {
                    somme += coefsPolynome1.get(k) * coefsPolynome2.get(i - k);
                }
            }
            coefsPolynomeProd.add(somme);
        }

        return new Polynome(coefsPolynomeProd);
    }

    @Override
    public boolean estEgalA(Composant cg) {
        if(cg instanceof Droite && getListeCoefs().size()==2) {
            return cg.estEgalA(new Droite.AB(new Point.XY(0,f(0)),new Point.XY(1,f(1))));//si la fonction est assimilable à une droite
        }
        try {
            Polynome polynome = (Polynome) cg;
            if (this.getListeCoefs().size() != polynome.getListeCoefs().size()) {
                return false;
            }
            for (int i = 0; i<this.getListeCoefs().size(); i++) {
                if (!Objects.equals(polynome.getListeCoefs().get(i), this.getListeCoefs().get(i))) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public int distance2Pixel(Point P, Repere repere) {
        //Point H = this.projetePolynome(P);
        //return P.distance2Pixel(H,repere);

        Point point = repere.pointReel(xPoints[0], yPoints[0]);
        int distanceMinimale = P.distance2Pixel(point, repere);
        for (int i = 1; i < NB_POINTS_DU_TRACE; i++) {
            point = repere.pointReel(xPoints[i], yPoints[i]);
            if (distanceMinimale > P.distance2Pixel(point, repere)) {
                distanceMinimale = P.distance2Pixel(point, repere);
            }
        }
        return distanceMinimale;
    }

/*    private Point projetePolynome(Point P) {
        return new Point(P.x(), this.f(P.x()));
    }*/

    @Override
    public List<Point> pointsDIntersection(Intersectable cg) {
//        XXX Décider si les points d'intersection ne devraient pas être représentés.
        //trouver les zéros du polynome différence. Pas d'intersection avec les arcs
        return new LinkedList<>();
    }

    @Override
    public List<Point> pointsSupplementaires() {
        return pointsDeConstruction;
    }

    @Override
    public boolean dependsOn(ComposantGraphique cg) {
        return cg instanceof Point && pointsDeConstruction.contains((Point)cg);
    }

    @Override
    public Point projection(Point P) {
        return new Point(P.x(), f(P.x()));
    }

    @Override
    public Point projeteOrthogonal(Point P) {
        Repere repere = new Repere(1, 1);
        int[] oldxPoints = Arrays.copyOf(xPoints, NB_POINTS_DU_TRACE);
        int[] oldyPoints = Arrays.copyOf(yPoints, NB_POINTS_DU_TRACE);
        
        //HACK on calcule les points x et y dans notre repère afin de trouver le plus proche
        dessineComposant(repere, null);
        
        Point point = repere.pointReel(xPoints[0], yPoints[0]);
        Point closest = point;
        int distanceMinimale = P.distance2Pixel(point, repere);
        for (int i = 1; i < NB_POINTS_DU_TRACE; i++) {
            point = repere.pointReel(xPoints[i], yPoints[i]);
            if (distanceMinimale > P.distance2Pixel(point, repere)) {
                distanceMinimale = P.distance2Pixel(point, repere);
                closest = point;
            }
        }

        //On remet les valeurs initiales
        xPoints = Arrays.copyOf(oldxPoints, NB_POINTS_DU_TRACE);
        yPoints = Arrays.copyOf(oldyPoints, NB_POINTS_DU_TRACE);
        return closest;
    }
}
