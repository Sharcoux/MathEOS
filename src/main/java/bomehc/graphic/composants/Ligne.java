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

package bomehc.graphic.composants;

import bomehc.graphic.OutilsGraph;
import bomehc.graphic.Repere;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public abstract class Ligne extends ComposantGraphique implements Composant.Projetable, Composant.Intersectable {

    public static enum LIGNE {
        DROITE(Droite.class), DEMI_DROITE(DemiDroite.class), SEGMENT(Segment.class);
        private final Class<? extends Ligne> classe;
        private LIGNE(Class<? extends Ligne> c) {classe = c;}
        public Class<? extends Ligne> getClasse(){return classe;}
    }
    
    public Droite droiteOrthogonale(Point A) {
        return new Droite.Orthogonale(this,A);
    }

    public Droite droiteParallele(Point A) {
        return new Droite.Parallele(this, A);
    }
    
    public boolean estParallele(Ligne d) {
        Vecteur vecteur = vecteur();
        Vecteur vecteurD = d.vecteur();
        return Math.abs(vecteur.x()*vecteurD.y() - vecteur.y()*vecteurD.x()) < Repere.ZERO_ABSOLU;
    }

    /**
     * Renvoie un vecteur directeur de la ligne
     * @return une vecteur directeur pour la ligne
     */
    public abstract Vecteur vecteur();
    
    /**
     * Renvoie un point de la ligne considéré comme origine.
     * @return Un point de la ligne considéré comme origine.
     */
    public abstract Point getOrigine();
    
    /**
     * Détermine si un point appartient à la ligne
     * @param P le point à étudier
     * @return true ssi le point P est sur la ligne.
     */
    public boolean contient(Point P) {
        if(P==null) {return false;}
        else {
            return (new Vecteur(getOrigine(),P).estParallele(vecteur()));//AP et AB alignés
        }
    }
    
    /**
     * Renvoie l'angle que fait le vecteur avec l'axe horizontal (Ox)\n L'angle est compris entre -PI et PI
     * @return l'angle en radian
     */
    public double orientation() {
        return vecteur().orientation();
    }
    
    /**
     * Renvoie la Droite correspondant à cette ligne. Càd la droite de même direction et de même origine.
     * @return la Droite passant par cette ligne.
     */
    public Droite droite() {
        return new Droite.Parallele(this, getOrigine());
    }

    @Override
    public Point projeteOrthogonal(Point P) {
        Droite d = new Droite(getOrigine(), vecteur());
        if(d.contient(P)) {return P;}
        return d.intersection(d.droiteOrthogonale(P));
    }
    
    @Override
    public List<Point> pointsDIntersection(Intersectable cg) {
        List<Point> liste;
        if(cg instanceof Arc) { liste = intersection((Arc)cg); }
        else if(cg instanceof Ligne) {
            liste = new LinkedList<>();
            Point P = intersection((Ligne)cg);
            if(P!=null) liste.add(P);
        } else {return cg.pointsDIntersection(this);}
        if(this instanceof Droite) {return liste;}//Pour gagner un peu de temps avec les droites
        List<Point> reponse = new LinkedList<>();
        for(Point P : liste) {if(contient(P)) {reponse.add(P);}}
        return reponse;

    }
    
    public Point intersection(Ligne d) {
        Vecteur vecteur = vecteur();
        Vecteur vecteurD = d.vecteur();
        if(vecteur.estParallele(vecteurD)) {return null;}
        else {
            Point origine = getOrigine();
            Point origineD = d.getOrigine();
            double ux = vecteur.x();
            double uy = vecteur.y();
            double vx = vecteurD.x();
            double vy = vecteurD.y();
            double xA = origine.x();
            double yA = origine.y();
            double xB = origineD.x();
            double yB = origineD.y();
            double x = (ux*(vx*(yA-yB) + vy*xB) - uy*vx*xA)/(ux*vy - uy*vx);
            double y = (uy*(vy*(xA-xB) + vx*yB) - ux*vy*yA)/(uy*vx - ux*vy);
            Point P = new Point.XY(x,y);
            if(d instanceof Droite && this instanceof Droite) {return P;}//Pour gagner un peu de temps avec les droites
            return (d.contient(P)&&this.contient(P)) ? P : null;
        }
    }
    
    public LinkedList<Point> intersection(Arc c) {
        LinkedList<Point> reponse = new LinkedList<>();

        Point centre = c.getCentre();
        double rayon = c.rayon();
        Point H = projeteOrthogonal(centre);
        double CH = OutilsGraph.distance(centre, H);
        if(Math.abs(CH-rayon)<Repere.ZERO_ABSOLU) {
            if(c.contient(H)) {reponse.add(H);}
            return reponse;}
        else {
            if(CH>rayon) {return reponse;}
            else {
                double HP = Math.sqrt(rayon*rayon - CH*CH);
                Vecteur hp = vecteur().unitaire().fois(HP);
                Point P1 = H.plus(hp);
                Point P2 = H.plus(hp.fois(-1));
                if(c.contient(P1)) {reponse.add(P1);}
                if(c.contient(P2)) {reponse.add(P2);}
                return reponse;
            }
        }
    }


}
