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
import matheos.graphic.Repere;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import matheos.graphic.composants.Composant.Legendable;
import static matheos.graphic.composants.Composant.Legendable.LEGENDE_PROPERTY;
import static matheos.graphic.composants.ComposantGraphique.STROKE_SIZE;
import matheos.graphic.composants.Texte.Legende;
import matheos.utils.managers.ColorManager;

/**
 *
 * @author François Billioud
 */
public class Droite extends DroiteAbstraite implements Serializable, Legendable {
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
//        if(!getNom().isEmpty()) {
//            java.awt.Point P = getDefaultLegendeCoord(repere);
//            g2D.drawString(getNom(), P.x, P.y);
//        }
        if(getLegende()!=null) {legendeSupport.dessine(repere, g2D, repere.pixel2Reel(getDefaultLegendeCoord(repere)));}
    }
    
    @Override
    public String getSVGRepresentation(Repere repere) {
        double xMin = repere.getXMin(), xMax = repere.getXMax();

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
        String s = "<line x1='"+xA+"' y1='"+yA+"' x2='"+xB+"' y2='"+yB+"' style='stroke:"+ColorManager.getRGBHexa(getCouleur())+";stroke-width:"+STROKE_SIZE+";' />";
        if(getLegende()!=null) {s+="\n"+getLegende().getSVGRepresentation(repere);}
        return s;
    }

    public java.awt.Point getDefaultLegendeCoord(Repere repere) {
        double xMax = repere.getXMax();
        double yMin = repere.getYMin(), yMax = repere.getYMax();
        int xB, yB;
        java.awt.Point legende = repere.reel2Pixel(repere.pixel2Reel(new java.awt.Point(0,0)).plus(getLegende().getDeplacement()));
        Rectangle textArea = getLegende().getTextComponent().getBounds();
        
        if(vecteur().x()!=0) {
            xB = repere.largeur();
            yB = repere.yReel2Pixel(y(xMax));
        } else {
            xB = repere.xReel2Pixel(getOrigine().x());
            yB = 0;
        }
        if(vecteur().x()==0) { return new java.awt.Point(xB+15, yB+15); }
        else {
            if(y(xMax)<yMax && y(xMax)>yMin) { return new java.awt.Point(xB-5-textArea.width-Math.max(0, legende.x), yB); }//Le point est à droite
            else {
                double y = a()>0 ? yMax : yMin;
                java.awt.Point P = repere.reel2Pixel(new Point(x(y),y));
                P.translate(-5-textArea.width, a()>0 ? (5-Math.min(0, legende.y)) : (-5-textArea.height-Math.max(0, legende.y)));//le point est en haut ou en bas
                return P;
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
