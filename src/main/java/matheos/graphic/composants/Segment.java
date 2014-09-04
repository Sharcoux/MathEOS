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
import matheos.utils.managers.ColorManager;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author François Billioud
 */
public class Segment extends Ligne implements Serializable, Composant.Legendable, Composant.Intersectable {
    private static final long serialVersionUID = 1L;

//    private Segment() {}//pour le JSON
    
    private final Point A;
    private final Point B;
    protected Segment(Point A, Point B) {
        this.A = A;
        this.B = B;
    }
    public Point getA() {
        return A;
    }
    public Point getB() {
        return B;
    }

    @Override
    public boolean dependsOn(ComposantGraphique cg) {
        return cg.estEgalA(A) || cg.estEgalA(B);
    }

    @Override
    public Point getOrigine() { return getA(); }

    @Override
    public Vecteur vecteur() {
        return new Vecteur(getA(),getB());
    }

    @Override
    public boolean contient(Point P) {
        if(P==null) {return false;}
        else {
            Vecteur AP = new Vecteur(getA(),P);
            Vecteur AB = new Vecteur(getA(),getB());
            double k = AB.coefProp(AP);
            return AB.estParallele(AP) && k>-Repere.ZERO_ABSOLU && k-1<Repere.ZERO_ABSOLU;
        }
    }

    public double longueur() {
        Vecteur v = vecteur();
        return v.longueur();
    }

    public Point milieu() {
        return new Point.Milieu(getA(),getB());
    }

    @Override
    protected void dessineComposant(Repere repere,Graphics2D g2D) {
        Point PA=getA(), PB = getB();
        int xA=repere.xReel2Pixel(PA.x());
        int yA=repere.yReel2Pixel(PA.y());
        int xB=repere.xReel2Pixel(PB.x());
        int yB=repere.yReel2Pixel(PB.y());
        g2D.drawLine(xA,yA,xB,yB);
        if(legende!=null) {
            legende.setPosition(getDefaultNameCoord(repere));
            legende.dessine(repere, g2D);
        }
        if(!marque.equals("")) {dessineMarque(repere, g2D);}
    }

    public Point getDefaultNameCoord(Repere repere) {
//        Vecteur v = new Vecteur(A,B).vecteurOrthogonal();//vecteur de la mediatrice
//        return milieu().plus(repere.distance2Reel(15, v));//calcul 20 pixels dans la direction v
        Vecteur vecteur = vecteur();
        Point M = milieu();
        int delta = legende==null ? 15 : legende.getTextComponent().getHeight();
        if(vecteur.y()*vecteur.x()>=-repere.zeroRelatif()) {return M;}
        else {
            Point P = M.plus(repere.distance2Reel(delta, new Vecteur(0,1)));
            Vecteur ortho = vecteur.vecteurOrthogonal().unitaire();
            Vecteur v = ortho.fois(ortho.prodScal(new Vecteur(M,P)));
            if(v.x()<0) {v=v.fois(-1);}
            return M.plus(v);
        }
    }

    private Legende legende = null;
    @Override
    public void setLegende(String texte) {
        if(texte==null) {
            if(this.legende!=null) {
                this.legende.setDependance(null);
                this.legende.firePropertyChange(Texte.EXIST_PROPERTY, true, false);
                this.legende=null;
            }
        } else if(this.legende!=null) {
            this.legende.setText(texte);
        } else {
            this.legende = new Texte.Legende(texte);
            this.legende.setDependance(this);
            this.legende.setCouleur(ColorManager.get("color temp component"));
        }
    }
    @Override
    public Legende getLegende() {return legende;}

    //gère le repérage des segments identiques
    private static final int LONGUEUR_MARQUE = 8;
    private static final int ECART_MARQUE = 4;
    private String marque = "";
    public void setMarque(String s) {
        marque = s;
    }
    /** permet de dessiner une marque de reconnaissance sur un segment afin de signifier qu'il est identique à un autre **/
    private void dessineMarque(Repere repere,Graphics2D g2D) {
        Vecteur v = vecteur().rotation(Math.PI/4);
        v = repere.distance2Reel(LONGUEUR_MARQUE,v);
        Vecteur vecteur = vecteur();
        Point I = milieu();
        Segment s1=null,s2=null;
        switch (marque) {
            case "/":
                s1 = new Segment(I.moins(v),I.plus(v));
                break;
            case "//":
                Point I1 = I.plus(repere.distance2Reel(ECART_MARQUE, vecteur));
                Point I2 = I.moins(repere.distance2Reel(ECART_MARQUE, vecteur));
                s1 = new Segment(I1.moins(v),I1.plus(v));
                s2 = new Segment(I2.moins(v),I2.plus(v));
                break;
            case "X":
                Vecteur u = v.rotation(Math.PI/2);
                s1 = new Segment(I.moins(v),I.plus(v));
                s2 = new Segment(I.moins(u),I.plus(u));
                break;
        }
        if(s1!=null) {
            s1.setCouleur(couleur);
            s1.dessine(repere, g2D);
        }
        if(s2!=null) {
            s2.setCouleur(couleur);
            s2.dessine(repere, g2D);
        }
    }

    public Point projectionSurSegment(Point P) {
        Point H = projeteOrthogonal(P);
        if(this.contient(H)) {return H;}
        Point PA = getA(), PB = getB();
        Vecteur v = new Vecteur(getA(),H);
        if(v.estDeMemeSigne(new Vecteur(PA,PB))) {return PB;}
        return PA;
    }

    @Override
    public int distance2Pixel(Point P, Repere repere) {
        Point H = projectionSurSegment(P);
        return P.distance2Pixel(H,repere);
    }

    @Override
    public boolean estEgalA(Composant cg) {
        try{
            Segment AB=(Segment)cg;
            return (AB.getA().estEgalA(getA()) && AB.getB().estEgalA(getB()));
        } catch(Exception e) {return false;}
    }

    public Point intersection(Segment AB) {
        Point P = (new Droite.AB(AB.getA(),AB.getB())).intersection(this);
        if(this.contient(P) && AB.contient(P)) {return P;}
        else {return null;}
    }

    public LinkedList<Point> intersection(Arc c) {
        LinkedList<Point> L = (new Droite.AB(getA(),getB())).intersection(c);
        if(L==null || L.isEmpty()) {return L;}
        else {
            LinkedList<Point> reponse = new LinkedList<>();
            Point P1 = (Point)L.poll();
            if(this.contient(P1)) {reponse.add(P1);}
            Point P2 = (Point)L.peek();//peek() renvoi null si L est vide
            if(this.contient(P2)) {reponse.add(P2);}//contient(null) renvoi false
            return reponse;
        }
    }

    @Override
    public List<Point> pointsSupplementaires() {
        List<Point> rep = new LinkedList<>();
        rep.add(new Point(getA().x(),getA().y()) {
            public double x() {return getA().x();}
            public double y() {return getA().y();}
            public boolean dependsOn(ComposantGraphique cg) { return cg.estEgalA(Segment.this); }
        });
        rep.add(new Point(getB().x(),getB().y()) {
            public double x() {return getB().x();}
            public double y() {return getB().y();}
            public boolean dependsOn(ComposantGraphique cg) { return cg.estEgalA(Segment.this); }
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
        return projectionSurSegment(P);
    }
    
    public static class AB extends Segment {
        public AB(Point A, Point B) {
            super(A,B);
        }
    }
    
    public static class MemeLongueur extends Segment {
        private final Vecteur vectDir;
        private final Segment AB;
        public MemeLongueur(Segment AB, Point C, Vecteur vectDir) {
            super(C, C.plus(vectDir.fois(AB.longueur())));
            this.AB = AB;
            this.vectDir = vectDir.unitaire();
        }
        public MemeLongueur(Point C, Segment AB, Vecteur vectDir) {
            this(AB, C, vectDir);
        }

        @Override
        public Point getB() {
            return getA().plus(vectDir.fois(AB.longueur()));
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(getA())||cg.estEgalA(AB);
        }
    }
    
    public static class Parallele extends Segment {
        private final Ligne l;
        private final double longueur;
        /** 
         * Crée un segment parallèle à un autre, de longueur arbitraire.
         * @param l ligne parallèle
         * @param A départ du nouveau segment
         * @param longueur longeur du nouveau segment. Une valeur négative signifie un orientation opposée au vecteur AB
         */
        public Parallele(Ligne l, Point A, double longueur) {
            super(A, A.plus(l.vecteur().unitaire().fois(longueur)));
            this.l = l;
            this.longueur = longueur;
        }

        @Override
        public Point getB() {
            return getA().plus(l.vecteur().unitaire().fois(longueur));
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(getA()) || cg.estEgalA(l);
        }
    }
    
    public static class Orthogonal extends Segment {
        private final Ligne l;
        public Orthogonal(Ligne l, Point P) {
            super(l.projeteOrthogonal(P), P);
            this.l = l;
        }

        @Override
        public Point getA() {
            return l.projeteOrthogonal(getB());
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(getB()) || cg.estEgalA(l);
        }
    }
    
    public static class ParalleleDeMemeLongueur extends Segment {
        private final Segment AB;
        private final boolean sens;
        /**
         * Crée un Segment parallèle et de meme longueur que AB
         * @param AB segment référence
         * @param A départ
         * @param deMemeSens true si le segment CD est de meme sens que AB
         */
        public ParalleleDeMemeLongueur(Segment AB, Point A, boolean deMemeSens) {
            super(A, deMemeSens ? A.plus(AB.vecteur()) : A.moins(AB.vecteur()));
            this.AB = AB;
            this.sens = deMemeSens;
        }

        @Override
        public Point getB() {
            return sens ? getA().plus(AB.vecteur()) : getA().moins(AB.vecteur());
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(getA()) || cg.estEgalA(AB);
        }
    }
    
    public static class Rectangle extends Segment {
        private final Segment AB;
        private final double distance;
        public Rectangle(Segment AB, double distance) {
            super(AB.getA().plus(AB.vecteur().unitaire().vecteurOrthogonal().fois(distance)),AB.getB().plus(AB.vecteur().unitaire().vecteurOrthogonal().fois(distance)));
            this.AB = AB;
            this.distance = distance;
        }

        @Override
        public Point getA() {
            Vecteur v = AB.vecteur().unitaire();
            return AB.getA().plus(v.vecteurOrthogonal().fois(distance));
        }

        @Override
        public Point getB() {
            Vecteur v = AB.vecteur().unitaire();
            return AB.getB().plus(v.vecteurOrthogonal().fois(distance));
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(AB);
        }
    }
    
    public static class Carre extends Segment {
        private final Segment AB;
        private final boolean sens;
        /**
         * Crée un Segment parallèle et de meme longueur que AB
         * @param AB segment référence
         * @param positif true si l'angle en A est positif. Dans un cas classique, cela voudrait dire que CD est au-dessus de AB
         */
        public Carre(Segment AB, boolean positif) {
            super(positif ? AB.getA().plus(AB.vecteur().vecteurOrthogonal()) : AB.getA().moins(AB.vecteur().vecteurOrthogonal()),
                    positif ? AB.getB().plus(AB.vecteur().vecteurOrthogonal()) : AB.getB().moins(AB.vecteur().vecteurOrthogonal()));
            this.AB = AB;
            this.sens = positif;
        }

        @Override
        public Point getA() {
            Vecteur v = AB.vecteur();
            return sens ? AB.getA().plus(v.vecteurOrthogonal()) : AB.getA().moins(v.vecteurOrthogonal());
        }

        @Override
        public Point getB() {
            Vecteur v = AB.vecteur();
            return sens ? AB.getB().plus(v.vecteurOrthogonal()) : AB.getB().moins(v.vecteurOrthogonal());
        }

        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(AB);
        }
    }

}
