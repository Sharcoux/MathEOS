/** «Copyright 2014 François Billioud»
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

package matheos.graphic.fonctions;

import matheos.graphic.Repere;
import matheos.graphic.composants.Composant;
import matheos.graphic.composants.ComposantGraphique;
import matheos.graphic.composants.Point;
import matheos.graphic.composants.Polynome;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import matheos.graphic.composants.Droite;
import matheos.graphic.composants.Texte.Legende;
import matheos.graphic.composants.Vecteur;

/**
 *
 * @author François Billioud
 */
public class Fonction extends ComposantGraphique implements Composant.Projetable, Composant.Legendable {

    private ComposantGraphique representation;
    private Fonction(ComposantGraphique polynome) {//pour le JSON
        setRepresentation(polynome);
    }
    
    public Fonction(LinkedList<Double> listeCoefs) {
        int degre = -1;
        for(int i = 0; i<listeCoefs.size(); i++) {
            if(listeCoefs.get(i)!=0) {degre=i;}
        }
        if(degre<2) {
            if(degre==-1) {setRepresentation(new Droite.AVecteur(new Point.XY(0,0), new Vecteur(1,0)));}
            else if(degre==0) {double b = listeCoefs.get(0);setRepresentation(new Droite.AVecteur(new Point.XY(0,b),new Vecteur(1,0)));}
            else {double b = listeCoefs.get(0);double a = listeCoefs.get(1);setRepresentation(new Droite.AVecteur(new Point.XY(0,b),new Vecteur(1,a)));}
        } else {
            setRepresentation(new Polynome(listeCoefs));
        }
    }
    public Fonction(List<Point> listeComposants) {
        if(listeComposants.size()<=2) {
            if(listeComposants.isEmpty()) {setRepresentation(new Droite.AVecteur(new Point.XY(0,0), new Vecteur(1,0)));}
            else if(listeComposants.size()==1) {Point P = listeComposants.get(0);setRepresentation(new Droite.AVecteur(P,new Vecteur(1,0)));}
            else {Point P1 = listeComposants.get(0);Point P2 = listeComposants.get(1);setRepresentation(new Droite.AB(P1, P2));}
        } else {
            setRepresentation(new Polynome(listeComposants));
        }
    }
    
    private ComposantGraphique getRepresentation() {return representation;}
    private Legendable getRepresentationLegendable() {return (Legendable)representation;}
    private Projetable getRepresentationProjetable() {return (Projetable)representation;}
    private void setRepresentation(ComposantGraphique cg) {
        if(representation!=null) {representation.removePropertyChangeListener(dispatcher);}
        representation = cg;
        if(representation!=null) {representation.addPropertyChangeListener(dispatcher);}
    }
    private final PropertyChangeListener dispatcher = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    };

    @Override
    public String getNom() {return getRepresentation().getNom();}
    @Override
    public void setNom(String nom) {getRepresentation().setNom(nom);}
    @Override
    public Legende getLegende() {return getRepresentationLegendable().getLegende();}
    @Override
    public void setLegende(Legende legende) {getRepresentationLegendable().setLegende(legende);}
    @Override
    public void setLegende(String texte) {getRepresentationLegendable().setLegende(texte);}
    @Override
    public Color getCouleur() {return getRepresentation().getCouleur();}
    @Override
    public void setCouleur(Color couleur) {getRepresentation().setCouleur(couleur);}
    @Override
    public void fireLegendeChanged(Legende oldOne, Legende newOne) {getRepresentationLegendable().fireLegendeChanged(oldOne, newOne);}
    @Override
    public void setLegendeColor(Color c) {getRepresentationLegendable().setLegendeColor(c);}
    
    @Override
    public void passif(boolean b) {
        getRepresentation().passif(b);
    }
    @Override
    public boolean estPassif() {return getRepresentation().estPassif();}

    private boolean xPositif = true;
    public void xPositif(boolean b) {
        xPositif = b;
    }
    public boolean xPositif() {return xPositif;}
    
    @Override
    public void dessine(Repere repere, Graphics2D g2D) {
        if(xPositif()) {
            if(repere.getXMax()<=0) {return;}//La fonction n'est pas dans le dessin
            if(repere.getXMin()<0) {
                int largeur = repere.xDistance2Pixel(repere.getXMax()-0);
                int hauteur = repere.yDistance2Pixel(repere.getYMax()-repere.getYMin());
                Repere r = new Repere(largeur, hauteur);
                r.setOrthonormal(false);
                r.setArea(0, repere.getXMax(), repere.getYMin(), repere.getYMax());

                //Création d'un espace partiel sur lequel dessiner
                BufferedImage im = new BufferedImage(largeur, hauteur, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = im.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(g2D.getStroke());
                g.setFont(g2D.getFont());
                getRepresentation().dessine(r, g);
                
                g2D.drawImage(im, repere.xReel2Pixel(0), 0, null);
                return;
            }
        }
        getRepresentation().dessine(repere, g2D);
    }
    
    @Override
    public String getSVGRepresentation(Repere repere) {
        return getRepresentation().getSVGRepresentation(repere);
    }
    
    @Override
    protected void dessineComposant(Repere repere, Graphics2D g2D) {
        getRepresentation().dessine(repere, g2D);
    }

    @Override
    public int distance2Pixel(Point point, Repere repere) {
        Repere r = repere;
        if(xPositif()) {
            if(repere.getXMin()<0) {
                int largeur = repere.xDistance2Pixel(repere.getXMax()-0);
                int hauteur = repere.yDistance2Pixel(repere.getYMax()-repere.getYMin());
                r = new Repere(largeur, hauteur);
                r.setOrthonormal(false);
                r.setArea(0, repere.getXMax(), repere.getYMin(), repere.getYMax());
            }
        }
        return getRepresentation().distance2Pixel(point, r);
    }

    @Override
    public boolean estEgalA(Composant cg) {
        if(cg instanceof Fonction) {return getRepresentation().estEgalA(((Fonction)cg).getRepresentation());}
        return getRepresentation().estEgalA(cg);
    }

    @Override
    public List<Point> pointsSupplementaires() {
        return getRepresentation().pointsSupplementaires();
    }

    @Override
    public boolean dependsOn(ComposantGraphique cg) {
        return getRepresentation().dependsOn(cg);
    }

    @Override
    public Point projection(Point P) {
        return getRepresentationProjetable().projection(P);
    }

    @Override
    public Point projeteOrthogonal(Point P) {
        return getRepresentationProjetable().projeteOrthogonal(P);
    }

}
