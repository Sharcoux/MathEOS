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
import matheos.json.Json;
import matheos.utils.managers.ColorManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.BasicStroke;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

/**
 *
 * @author François Billioud
 */
public abstract class ComposantGraphique implements Composant, Cloneable, Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String NAME_PROPERTY = "nom";
    public static final String COLOR_PROPERTY = "couleur";
    public static final String DOT_PROPERTY = "pointille";
    public static final String PASSIVE_PROPERTY = "passif";
    public static final String COORDINATE_PROPERTY = "coordonnees";
    public static final String EXIST_PROPERTY = "existe";
    
    protected static final int STROKE_SIZE = 4;

    private String nom = "";
    private Color couleur = ColorManager.get("color drawing1");
    private boolean pointille = false;
    
    private final transient PropertyChangeSupport support = new PropertyChangeSupport(this);
    public void addPropertyChangeListener(PropertyChangeListener l) {support.addPropertyChangeListener(l);}
    public void removePropertyChangeListener(PropertyChangeListener l) {support.removePropertyChangeListener(l);}
    protected void firePropertyChange(String property, Object oldValue, Object newValue) {
        support.firePropertyChange(property, oldValue, newValue);
    }
    
    public String getNom() {return nom;}
    public void setNom(String nom) {
        if(nom == null ? this.nom == null : nom.equals(this.nom)) {return;}
        String old = this.nom;
        this.nom = nom;
        firePropertyChange(NAME_PROPERTY, old, nom);
    }
    public Color getCouleur() {return couleur;}
    public void setCouleur(Color couleur) {
        if(couleur.equals(this.couleur)) {return;}
        Color old = this.couleur;
        this.couleur = couleur;
        firePropertyChange(COLOR_PROPERTY, old, couleur);
    }
    public boolean isPointille() {return pointille;}
    public void setPointille(boolean b) {
        if(b==pointille) {return;}
        pointille = b;
        firePropertyChange(DOT_PROPERTY, !b, b);
    }

    public ComposantGraphique() {}

    public void dessine(Repere repere, Graphics2D g2D) {
        g2D.setColor(this.getCouleur());
//        g2D.setFont(FontManager.get("font graphic text component"));
        BasicStroke stroke = (BasicStroke) g2D.getStroke();
        if(isPointille()) {
            float[] T = {5,5};
            BasicStroke dashedStroke = new BasicStroke(stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin(), stroke.getMiterLimit(), T, .0f);
            g2D.setStroke(dashedStroke);
        }
        dessineComposant(repere, g2D);
        if(isPointille()) {
            g2D.setStroke(stroke);
        }
    }
    
    public abstract String getSVGRepresentation(Repere repere);

    protected abstract void dessineComposant(Repere repere, Graphics2D g2D);


    protected boolean estPassif = false;
    /**
     * Rend le composant passif. Un composant passif ne permet pas de faire des constructions,
     * ne participe pas aux calculs d'intersection. Il est simplement décoratif.
     */
    public void passif(boolean b) {
        if(b==estPassif) {return;}
        estPassif = b;
        firePropertyChange(PASSIVE_PROPERTY, !b, b);
    }
    /**
     * Indique si le composant est passif.
     * Un composant passif ne permet pas de faire des constructions,
     * ne participe pas aux calculs d'intersection. Il est simplement décoratif.
     */
    public boolean estPassif() {return estPassif;}

    /**
     * Renvoie les points supplémentaires à dessiner (extrémités, centre...)
     * @return la liste des points
     */
    public abstract List<Point> pointsSupplementaires();

    @Override
    public ComposantGraphique clone() {
        try {
            return (ComposantGraphique) Json.jsonCloning(this);
        } catch(IOException e) {
            try {
                return (ComposantGraphique) super.clone();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(ComposantGraphique.class.getName()).log(Level.SEVERE, null, ex);
                return this;
            }
        }
        
    }
    
    /**
     * Indique si le composant cg est utilisé par ce composantGraphique dans sa construction.
     * @param cg composant graphique
     * @return true si cg est utilisé, false s'il est indépendant
     */
    public abstract boolean dependsOn(ComposantGraphique cg);

}
