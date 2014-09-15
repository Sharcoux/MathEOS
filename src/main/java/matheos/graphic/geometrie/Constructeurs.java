/**
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of MathEOS
 *
 * MathEOS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 *
 **/

package matheos.graphic.geometrie;

import matheos.graphic.ListComposant;
import matheos.graphic.Module.ObjectCreation;
import matheos.graphic.OutilsGraph;
import matheos.graphic.Repere;
import matheos.graphic.composants.Arc;
import matheos.graphic.composants.Composant;
import matheos.graphic.composants.Composant.Intersectable;
import matheos.graphic.composants.Composant.Projetable;
import matheos.graphic.composants.ComposantGraphique;
import matheos.graphic.composants.DemiDroite;
import matheos.graphic.composants.Droite;
import matheos.graphic.composants.Ligne;
import matheos.graphic.composants.Point;
import matheos.graphic.composants.Segment;
import matheos.graphic.composants.Texte;
import matheos.graphic.composants.Vecteur;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author François Billioud
 */
class Constructeurs {

    /** permet la construction des objets géométriques à partir d'une liste d'éléments **/
    static abstract class Constructeur {
        /** construit un objet à partir d'éléments donnés.
         * 
         * @param objets liste d'éléments sélectionnés sur le dessin
         * @param curseur position du curseur sur le dessin
         * @param souris position exacte de la souris sur le dessin
         * @return l'objet crée et ses éléments de construction à travers un ObjectCreation.
         */
        public abstract ObjectCreation construire(ListComposant objets);
        
        public Point pointDeConstructionNonExistant(ListComposant objets, Point curseur, Point souris) {return curseur;}
    }
    
    /** Permet de factoriser la construction des éléments Ligne **/
    static abstract class ConstructeurLigne extends Constructeur {
        public static enum TYPE {SEGMENT, DEMI_DROITE, DROITE};
        
        private final TYPE type;
        protected TYPE getType() {return type;}

        protected ConstructeurLigne(TYPE type) {
            this.type = type;
        }
    }
    
    /** permet de factoriser la construction des éléments Ligne.AB **/
    private static abstract class ConstructeurLigneSpecial extends ConstructeurLigne {
        ConstructeurLigneSpecial(TYPE type) {
            super(type);
        }
        
        protected class ConstructionElements {
            Point A, B;
            ListComposant objetsAnnexes;
            protected ConstructionElements(Point A, Point B) {
                this(A,B,new ListComposant());
            }
            protected ConstructionElements(Point A, Point B, ListComposant objetsAnnexes) {
                this.A = A;
                this.B = B;
                this.objetsAnnexes = objetsAnnexes;
            }
        }
        abstract ConstructionElements creerElements(ListComposant objets);
        
        @Override
        public ObjectCreation construire(ListComposant objets) {
            ConstructionElements elts = creerElements(objets);
            Ligne l;
            switch(getType()) {
                case SEGMENT : Segment AB = new Segment.AB(elts.A, elts.B);
                    AB.setLegende(approxime(AB.longueur())+"");
                    l = AB;
                    break;
                case DEMI_DROITE : l = new DemiDroite.AB(elts.A, elts.B); break;
                case DROITE : l = new Droite.AB(elts.A, elts.B); break;
                default : l = new Segment.AB(elts.A, elts.B);
            }
            return new ObjectCreation(l, elts.objetsAnnexes);
        }
    }
    
    /** Construit un Point à partir du curseur ou d'un point **/
    static class PointP extends Constructeur {
        @Override
        public ObjectCreation construire(ListComposant objets) {
            return new ObjectCreation(objets.get(0));
        }
    }

    /** Construit le projeté d'un point existant ou de la souris sur un élément **/
    static class PointProjetableP extends Constructeur {
        @Override
        public ObjectCreation construire(ListComposant objets) {
            Projetable support = (Projetable)objets.get(0);
            Point H = support.projection((Point)objets.get(1));
            if(support instanceof Segment || support instanceof DemiDroite) {
                Point A = ((Ligne)support).getOrigine();
                Texte distance = new TexteSegment(new Segment.AB(A,H));
                return new ObjectCreation(H, new ListComposant(distance));
            } else {
                return new ObjectCreation(H);
            }
        }
        @Override
        public Point pointDeConstructionNonExistant(ListComposant objets, Point curseur, Point souris) {
            Projetable support = (Projetable)objets.get(0);
            Point H = support.projection(souris);
            if(support instanceof Segment || support instanceof DemiDroite) {
                Point A = ((Ligne)support).getOrigine();
                return approxime(A, H);
            } else {
                return souris;
            }
        }
    }

    /** Permet de créer une ligne avec 2 points, le curseur, la souris **/
    static class Ligne2P extends ConstructeurLigneSpecial {
        Ligne2P(TYPE type) {
            super(type);
        }
        @Override
        protected ConstructionElements creerElements(ListComposant objets) {
            Point A = (Point)objets.get(0), B = (Point)objets.get(1);
            return new ConstructionElements(A, B);
        }
        @Override
        public Point pointDeConstructionNonExistant(ListComposant objets, Point curseur, Point souris) {
            if(objets.size()==0) {return curseur;}
            else {return approxime((Point)objets.get(0), souris);}
        }

    }

    /** Construit une ligne avec un angle par rapport à un axe, à partir d'un point, d'une ligne et de la souris ou d'un point existant **/
    static class LignePLP extends ConstructeurLigneSpecial {
        private double angle = 0;

        LignePLP(TYPE mode) {
            super(mode);
        }

        @Override
        protected ConstructionElements creerElements(ListComposant objets) {
            Ligne axe = (Ligne)objets.get(1);
            Point A = (Point)objets.get(0);
            Point B = (Point)objets.get(2);
            if(axe.contient(A)) {
                Arc c = construireMarqueAngulaire(axe, A, B, angle);
                angle = c.getAngle();
                return new ConstructionElements(A,B, new ListComposant(c));
            } else {//crée une parallèle à l'axe
                Vecteur v = axe.vecteur().unitaire();
                return new ConstructionElements(A, A.plus(v.fois(new Vecteur(A,B).prodScal(v))));
            }
        }
        @Override
        public Point pointDeConstructionNonExistant(ListComposant objets, Point curseur, Point souris) {
            if(objets.size()==0) {return curseur;}
            Point A = (Point)objets.get(0);
            Ligne axe = (Ligne)objets.get(1);
            if(axe.contient(A)) {
                Arc c = construireMarqueAngulaire(axe, A, souris, angle);
                return A.plus(new Vecteur(A,c.getFin()).unitaire().fois(approxime(OutilsGraph.distance(A, souris))));
            } else {
                return curseur;
            }
        }
    }
    
    /** Construit une ligne parallèle à un axe à partir de l'axe et d'un, 2 points, le curseur ou la souris **/
    static class LigneParallele extends ConstructeurLigne {
        LigneParallele(TYPE mode) {super(mode);}

        @Override
        public ObjectCreation construire(ListComposant objets) {
            Ligne axe = (Ligne) objets.get(0);
            Point A = (Point)objets.get(1);
            Point B = objets.size()>2 ? (Point)objets.get(2) : A.plus(axe.vecteur().unitaire());
            Ligne l;
            switch(getType()) {
                case SEGMENT : 
                    double longueur = new Vecteur(A,B).prodScal(axe.vecteur().unitaire());
                    if(!new Vecteur(A,B).estParallele(axe.vecteur())) {longueur = approxime(longueur);}
                    Segment AB = new Segment.Parallele(axe, A, longueur);
                    AB.setLegende(Math.abs(approxime(longueur))+"");
                    l = AB;
                    break;
                case DEMI_DROITE : l = new DemiDroite.Parallele(A, axe, axe.vecteur().estDeMemeSigne(new Vecteur(A,B))); break;
                case DROITE : l = new Droite.Parallele(axe, A); break;
                default : l = new Droite.Parallele(axe, A);
            }
            return new ObjectCreation(l);
        }
        @Override
        public Point pointDeConstructionNonExistant(ListComposant objets, Point curseur, Point souris) {
            if(objets.size()==1) {return curseur;}
            Ligne axe = (Ligne) objets.get(0);
            return approxime((Point)objets.get(1),axe.projection(souris));
        }
    }
    /** Construit une ligne orthogonale à un axe à partir de l'axe et d'un point ou du curseur **/
    static class LigneOrthogonale extends ConstructeurLigne {
        LigneOrthogonale(TYPE mode) {super(mode);}

        @Override
        public ObjectCreation construire(ListComposant objets) {
            Ligne axe = (Ligne) objets.get(0);
            Point P = (Point) objets.get(1);
            Point H = axe.projection(P);
            if(getType()!=TYPE.DROITE && axe.contient(P)) {
                if(objets.size()<=3) {return null;}
                P = (Point) objets.get(2);
            }
            Point A = axe.droiteOrthogonale(H).projeteOrthogonal(P);
            Ligne l;
            switch(getType()) {
                case SEGMENT : if(!A.estEgalA(P)) {A=approxime(H, A);}
                    Segment AB = new Segment.Orthogonal(axe, A);
                    AB.setLegende(approxime(AB.longueur())+"");
                    l = AB;
                    break;
                case DEMI_DROITE : l = new DemiDroite.Orthogonale(axe, A); break;
                case DROITE : l = new Droite.Orthogonale(axe, A); break;
                default : l = new Droite.Orthogonale(axe, A);
            }
            return new ObjectCreation(l,new ListComposant(new MarqueOrthogonale(axe, l)));
        }
        @Override
        public Point pointDeConstructionNonExistant(ListComposant objets, Point curseur, Point souris) {
            Ligne axe = (Ligne) objets.get(0);
            return approxime(axe.projeteOrthogonal(souris),souris);
        }
    }
    
//    static class ApercuSegmentSP implements Constructeur {
//        @Override
//        public ObjectCreation construire(ListComposant objets, Point curseur, Point souris) {
//            Point P = objets.size()>1 ? (Point)objets.get(1) : curseur;
//            return new ObjectCreation(new Segment.ParalleleDeMemeLongueur((Segment)objets.get(0), P, true));
//        }
//    }
    /** Construit un segment parallele de même longueur **/
    static class SegmentSPP extends Constructeur {
        @Override
        public ObjectCreation construire(ListComposant objets) {
            Segment AB = (Segment) objets.get(0);
            Point C = (Point)objets.get(1), D = (Point)objets.getLast();//On utilise la souris plutôt que le curseur si possible
            return new ObjectCreation(new Segment.ParalleleDeMemeLongueur(AB,C,new Vecteur(C,D).estDeMemeSigne(AB.vecteur())));//XXX créer ici les points supplémentaires ?
        }
    }
    
    /** Construit un rectangle **/
    static class SegmentSSP extends Constructeur {
        @Override
        public ObjectCreation construire(ListComposant objets) {
            Segment AB = (Segment) objets.get(0);
            Point C = (Point)objets.get(2);
            Vecteur v = AB.vecteur().vecteurOrthogonal().unitaire();
            double longueur = new Vecteur(AB.getA(), C).prodScal(v);
            Segment CD = new Segment.Rectangle(AB,longueur);
            Segment AC = new Segment.AB(AB.getA(), CD.getA());
            AC.setLegende(Math.abs(approxime(longueur))+"");
            Segment BD = new Segment.AB(AB.getB(), CD.getB());
            ListComposant annexes = new ListComposant(AC,BD);
            annexes.add(new MarqueOrthogonale(AB, BD));
            annexes.add(new MarqueOrthogonale(AB, AC));
            annexes.add(new MarqueOrthogonale(CD, AC));
            annexes.add(new MarqueOrthogonale(CD, BD));
            return new ObjectCreation(CD, annexes);//XXX créer ici les points supplémentaires ?
        }
        @Override
        public Point pointDeConstructionNonExistant(ListComposant objets, Point curseur, Point souris) {
            return approxime(((Segment)objets.get(0)).projeteOrthogonal(souris),souris);
        }
    }
    /** Construit un carré **/
    static class SegmentSSSP extends Constructeur {
        @Override
        public ObjectCreation construire(ListComposant objets) {
            Segment AB = (Segment) objets.get(0);
            Point C = (Point)objets.getLast();//on utilise la souris plutôt que le curseur si possible
            Vecteur v = AB.vecteur().vecteurOrthogonal();
            Segment CD = new Segment.Carre(AB,new Vecteur(AB.getA(),C).estDeMemeSigne(v));
            Segment AC = new Segment.AB(AB.getA(), CD.getA());
            Segment BD = new Segment.AB(AB.getB(), CD.getB());
            ListComposant annexes = new ListComposant(AC,BD);
            annexes.add(new MarqueOrthogonale(AB, BD));
            annexes.add(new MarqueOrthogonale(AB, AC));
            annexes.add(new MarqueOrthogonale(CD, AC));
            annexes.add(new MarqueOrthogonale(CD, BD));
            return new ObjectCreation(CD, annexes);//XXX créer ici les points supplémentaires ?
        }
    }
    
    /** Construit une tangeante à un arc **/
    static class DroiteAP extends Constructeur {
        Point origine = null;
        @Override
        public ObjectCreation construire(ListComposant objets) {
            if(origine==null) {origine = (Point)objets.getLast();}//L'endroit du cercle ou on a cliqué. On utilise la souris plutôt que le curseur si possible
            Arc c = (Arc) objets.get(0);
            Point P = (Point) objets.getLast();//On utilise la souris plutôt que le curseur si possible
            //HACK : les points tangeants sont les points d'intersections du cercle de diametre AC avec c
            Arc intersection = new Arc.Cercle(new Segment.AB(P,c.getCentre()));
            List<Point> pointsTangeants = c.intersection(intersection);
            assert !pointsTangeants.isEmpty() : "Erreur : la vérification spéciale n'a pas fonctionnée";
            Point point = pointsTangeants.get(0);
            if(origine!=null && pointsTangeants.size()>1) {
                Point point2 = pointsTangeants.get(1);
                if(OutilsGraph.distance(origine, point)>OutilsGraph.distance(origine, point2)) {point = point2;}//On prend le point le plus près du clic originel
            }
            return new ObjectCreation(new Droite.Tangente(c, point));
        }
    }
    /** aperçu du tracé du rayon d'un cercle **/
    static class ApercuArc2P extends Constructeur {
        @Override
        public ObjectCreation construire(ListComposant objets) {
            Point centre = (Point)objets.get(0);
            Point debut = (Point)objets.get(1);
            
            Segment AB = new Segment.AB(centre, debut);
            AB.setLegende(approxime(AB.longueur())+"");
            AB.setPointille(true);
            return new ObjectCreation(AB, new ListComposant(centre, debut));
        }
        @Override
        public Point pointDeConstructionNonExistant(ListComposant objets, Point curseur, Point souris) {
            if(objets.size()==0) {return curseur;}
            Point centre = (Point)objets.get(0);
            return approxime(centre, souris);
        }
    }
    /** tracé d'un arc à partir du centre, du début et de l'ouverture **/
    static class Arc3P extends Constructeur {
        private double angle = 0;
        @Override
        public ObjectCreation construire(ListComposant objets) {
            Point centre = (Point) objets.get(0);
            Point debut = (Point) objets.get(1);
            Point fin = (Point) objets.getLast();//On utilise la souris plutôt que le curseur si possible
            return new ObjectCreation(new Arc.Angulaire(centre, debut, angle=corrigerAngle(angle, OutilsGraph.angle(debut, centre, fin))));//XXX créer ici les points supplémentaires ?
        }
    }
    /** aperçu d'un arc à partir du centre, de son rayon et du début **/
    static class ApercuArcPSP extends Constructeur {
        @Override
        public ObjectCreation construire(ListComposant objets) {
            Point centre = (Point) objets.get(0);
            Point direction = (Point)objets.getLast();//On utilise la souris plutôt que le curseur si possible
            Segment AB = (Segment) objets.get(1);
            Vecteur v = new Vecteur(centre,direction).unitaire().fois(AB.longueur());
            Point debut = centre.plus(v);
//            TexteSegment rayon = new TexteSegment(new Segment.AB(centre, debut));
            ListComposant L = new ListComposant(centre, debut);
            if(!AB.contient(centre)) {Segment CD = new Segment.AB(centre, debut);CD.setPointille(true);L.add(CD);}
            return new ObjectCreation(null, L);
        }
        @Override
        public Point pointDeConstructionNonExistant(ListComposant objets, Point curseur, Point souris) {
            if(objets.size()==0) {return curseur;}
            Point centre = (Point) objets.get(0);
            Segment s = (Segment) objets.get(1);
            return centre.plus(new Vecteur(centre,souris).unitaire().fois(s.longueur()));
        }
    }
    static class ArcPSPP extends Constructeur {
        double angle = 0;
        @Override
        public ObjectCreation construire(ListComposant objets) {
            Point centre = (Point) objets.get(0), debut = (Point) objets.get(2), fin = (Point) objets.get(3);
            Segment AB = (Segment) objets.get(1);
            double rayon = AB.longueur();
            Arc c;
            angle = corrigerAngle(angle, OutilsGraph.angle(debut, centre, fin));
            c = new Arc.Rayon(centre, rayon, debut, angle);
            c.setLegende(Math.abs(approximeDeg(c.getAngle()))+"°");
            return new ObjectCreation(c);//XXX créer ici les points supplémentaires ?
        }
        @Override
        public Point pointDeConstructionNonExistant(ListComposant objets, Point curseur, Point souris) {
            if(objets.size()==0) {return curseur;}
            Point centre = (Point) objets.get(0);
            Segment AB = (Segment) objets.get(1);
            double rayon = AB.longueur();
            if(objets.size()==2) {
                return centre.plus(new Vecteur(centre,souris).unitaire().fois(rayon));
            } else {
                Point debut = (Point) objets.get(2);
                Arc c = new Arc.Rayon(centre, rayon, debut, angle=approximeDeg(corrigerAngle(angle, OutilsGraph.angle(debut, centre, souris))));
                return c.getFin();
            }
        }
    }
    static class Cercle extends Constructeur {
        @Override
        public ObjectCreation construire(ListComposant objets) {
            return new ObjectCreation(new Arc.Cercle((Segment)objets.get(0)));
        }
    }
    
    private static Arc construireMarqueAngulaire(Ligne axeInitial, Point ancrage, Point fin, double oldAngle) {
        Vecteur v = new Vecteur(ancrage, fin);
        Ligne axe;
        if(axeInitial instanceof Droite) {
            boolean deMemeSens = axeInitial.vecteur().rotation(oldAngle).estDeMemeSigne(v);
            axe = new DemiDroite.Parallele(ancrage,axeInitial,deMemeSens);
        } else { axe = axeInitial; }
        Vecteur u = axe.vecteur();
        double angle = approximeRad(corrigerAngle(oldAngle, OutilsGraph.angle(u, v)));
        MarqueAngulaire arc = new MarqueAngulaire(axe, angle, OutilsGraph.distance(ancrage, fin)*0.15);
        arc.setLegende(Math.abs(approximeDeg(angle))+"°");
        return arc;
    }
    
    private static class TexteSegment extends Texte {
        private final Segment AB;
        private TexteSegment(Segment AB) {
            this(AB, approxime(AB.longueur())+"");
        }
        private TexteSegment(Segment AB, String texte) {
            super(0, 0, texte);
            this.AB = AB;
            passif(true);
        }
        @Override
        protected void dessineComposant(Repere repere, Graphics2D g2D) {
            setPosition(AB.getDefaultNameCoord(repere));
            super.dessineComposant(repere, g2D);
        }
        @Override
        public int distance2Pixel(Point point, Repere repere) {
            setPosition(AB.getDefaultNameCoord(repere));
            return super.distance2Pixel(point, repere);
        }

    }
        
    private static class MarqueAngulaire extends Arc {
        private final double distanceMarque;//distance entre la marque et l'intersection
        private final Ligne axe;
//        private final transient Point.XY depart;
        
        private MarqueAngulaire(Ligne axeInitial, double angle, double distanceMarque) {
            super(axeInitial.getOrigine(), axeInitial.getOrigine().plus(axeInitial.vecteur().unitaire()),angle);
            this.axe = axeInitial;
//            this.depart = new Point.XY(axeInitial.projection(axeInitial.getOrigine().plus(axeInitial.vecteur().unitaire())));//On fait la projection pour que le point appartienne à la ligne
            this.distanceMarque = distanceMarque;
        }
        
        @Override
        public Point getCentre() {
            return axe.getOrigine();
        }
        
        @Override
        public Point getDepart() {
            return getCentre().plus(axe.vecteur().unitaire().fois(distanceMarque));
        }
        
//        @Override
//        protected void dessineComposant(Repere repere, Graphics2D g2D) {
//            depart.setPosition(getCentre().plus(repere.distance2Reel(distanceMarque, axe.vecteur())));
//            super.dessineComposant(repere, g2D);
//        }

        @Override
        public List<Point> pointsDIntersection(Intersectable cg) {
            return new LinkedList<>();
        }

        @Override
        public List<Point> pointsSupplementaires() {
            return new LinkedList<>();
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(axe);
        }
    }
    private static class MarqueOrthogonale extends ComposantGraphique {
        private final Ligne l1;
        private final Ligne l2;
        private final transient Point O;
        private final transient Vecteur u;
        private final transient Vecteur v;
        private static final int DISTANCE_MARQUE = 20;//distance en pixels entre la marque et l'intersection

        private MarqueOrthogonale(Ligne l1, Ligne l2) {
            this.l1 = l1;
            this.l2 = l2;
            O = new Point.Intersection(l1.droite(), l2.droite());
            u = getAdaptedVector(l1);
            v = getAdaptedVector(l2);
            estPassif = true;
        }
        
        /** on récupère un vecteur unitaire et correctement orienté depuis la ligne **/
        private Vecteur getAdaptedVector(Ligne l) {
            if(l instanceof Segment) {
                Segment AB = (Segment)l;
                Point A = AB.getA(), B = AB.getB();
                return new Vecteur(O, OutilsGraph.distance(O, A)>OutilsGraph.distance(O, B) ? A : B).unitaire(); //on renvoie le vecteur dans la direction la plus longue
            }
            return l.vecteur().unitaire();
        }

        @Override
        protected void dessineComposant(Repere repere, Graphics2D g2D) {
            Point A = l1.projection(O.plus(repere.distance2Reel(DISTANCE_MARQUE, u)));//On fait la projection pour que le point appartienne à la ligne
            Point B = l2.projection(O.plus(repere.distance2Reel(DISTANCE_MARQUE, v)));//On fait la projection pour que le point appartienne à la ligne
            Point I = A.plus(new Vecteur(O,B));
            (new Segment.AB(A, I)).dessine(repere, g2D);
            (new Segment.AB(B, I)).dessine(repere, g2D);
        }

        @Override
        public int distance2Pixel(Point point, Repere repere) {
            Point A = l1.projection(O.plus(repere.distance2Reel(DISTANCE_MARQUE, u)));
            Point B = l2.projection(O.plus(repere.distance2Reel(DISTANCE_MARQUE, v)));
            Point I = A.plus(new Vecteur(O,B));
            return I.distance2Pixel(point, repere);
        }

        @Override
        public boolean estEgalA(Composant cg) {
            return cg instanceof MarqueOrthogonale && ((MarqueOrthogonale)cg).l1.estEgalA(l1) && ((MarqueOrthogonale)cg).l2.estEgalA(l2);
        }

        @Override
        public List<Point> pointsSupplementaires() {
            return new LinkedList<>();
//            List<Point> L = new LinkedList<>();
//            L.addAll(l1.pointsDIntersection(cg));
//            L.addAll(l2.pointsDIntersection(cg));
//            return L;
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(l1) || cg.estEgalA(l2);
        }
        
    }
    
    static double precisionDistance = 0.1;
    static double precisionAngle = 5;
    /** arrondit un double à la précision définie par PRECISION_APPROXIMATION. Par défaut, au dixième **/
    private static double approxime(double d) {
        return approxime(d, precisionDistance);
    }
    /** Arrondi avec la précision indiquée. Par exemple, 0.1 pour un résultat au dixième **/
    private static double approxime(double d,double precision) {
        return precision<1 ? Math.round(d/precision)/((1/precision)) : Math.round(d/precision)*precision;
    }
//    /** Convertit un angle de degrés vers radian. **/
//    private static double deg2rad(double d) {
//        return d*Math.PI/180;
//    }
//    /** Convertit un angle de radian vers degrés. **/
//    private static double rad2deg(double r) {
//        return r*180/Math.PI;
//    }
    /** Arrondit un angle en degrés à la précision définie par PRECISION_ANGLE. Par défaut, 5 degrés. r en radian. **/
    private static double approximeDeg(double r) {
//        return approxime(rad2deg(d),1);
        return approxime(Math.toDegrees(r),precisionAngle);//arrondi à x° près
    }
    /** Arrondit un angle en radian à la précision définie par PRECISION_ANGLE. Par défaut, 5 degrés. r en radian **/
    private static double approximeRad(double r) {
//        return approxime(rad2deg(d),1);
        return approxime(r,Math.toRadians(precisionAngle));//arrondi à x° près
    }
    /** Permet d'assurer la continuité de la mesure d'angle à 2Pi près.
     * Si l'ancien et le nouveau on un écart très élevé, la fonction recherche un angle plus proche à 2Pi près
     **/
    private static double corrigerAngle(double ancien, double nouveau) {
        double delta = ancien - nouveau;
        double correctedAngle = nouveau;
        if (Math.abs(delta) > Math.PI) {
            correctedAngle = nouveau + Math.signum(delta) * 2 * Math.PI;
            if (Math.abs(correctedAngle) < 2 * Math.PI) {
                correctedAngle = corrigerAngle(ancien, correctedAngle);
            }
        }
        return correctedAngle;
    }
    /** renvoie la nouvelle position du point cible après avoir arrondi la distance origine-cible **/
    private static Point approxime(Point origine, Point cible) {
        return OutilsGraph.approxime(origine, cible, precisionDistance);
    }

    private Constructeurs() {throw new AssertionError("instanciating utilitary class");}
}
