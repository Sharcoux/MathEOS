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
import matheos.graphic.composants.Texte.Legende;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 *
 * @author François Billioud
 */
public interface Composant {
    public void dessine(Repere repere, Graphics2D g2D);
    public Color getCouleur();
    public void setCouleur(Color couleur);
    /** Retourne la distance en pixels entre le composant et le point passé en paramètre **/
    public int distance2Pixel(Point point, Repere repere);

    public boolean estEgalA(Composant cg);

    public static enum DRAWABLE {
        TEXTE(Texte.class), Arc(Arc.class), Droite(Droite.class), DemiDroite(DemiDroite.class), Segment(Segment.class), Point(Point.class);
        private final Class<? extends Composant> classe;
        private DRAWABLE(Class<? extends Composant> c) { classe = c; }
        public Class<? extends Composant> getClasse() { return classe; }
    }
    public static enum GRAPHIQUE {
        TEXTE(Texte.class), Arc(Arc.class), Droite(Droite.class), DemiDroite(DemiDroite.class), Segment(Segment.class), Point(Point.class);
        private final Class<? extends ComposantGraphique> classe;
        private GRAPHIQUE(Class<? extends ComposantGraphique> c) { classe = c; }
        public Class<? extends ComposantGraphique> getClasse() { return classe; }
    }
    
    public static interface Draggable {
        public void setPosition(Point P);
        public void setPosition(double x, double y);
    }
    
    public static interface Projetable extends Composant {
        /**
         * Renvoie le point correspondant à la projection de P sur le composant.
         * Contrairement à projeteOrthogonal, le point renvoyé appartient à la ligne.
         * Si le projete orthogonal se situe hors du composant, projection renvoie l'extrémité
         * du composant la plus proche de P.
         * @param P le point à projeter
         * @see projeteOrthogonal(Point P)
         * @return le point du composant le plus proche de P.
         */
        public Point projection(Point P);
        
        /**
         * Renvoie le projeté orthogonal de P sur le composant.
         * Attention, celui-ci n'appartient pas forcément au composant.
         * @param P le point à projeter
         * @see projection(Point P)
         * @return le projeté orthogonal
         */
        public Point projeteOrthogonal(Point P);
    }
    
    public static interface Intersectable extends Composant {
        /**
         * Renvoie la liste des points d'intersection entre l'élément et son paramètre
         * @param cg
         * @return 
         */
        public abstract List<Point> pointsDIntersection(Intersectable cg);
    }
    
    public static interface Legendable extends Composant {
        public void setLegende(String texte);
        public Legende getLegende();
    }
}
