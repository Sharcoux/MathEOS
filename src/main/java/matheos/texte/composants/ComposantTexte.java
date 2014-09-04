/** «Copyright 2013,2014 François Billioud»
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

package matheos.texte.composants;

import java.awt.Color;

/**
 *
 * @author François Billioud
 */
public interface ComposantTexte {
    
    /**
     * Renvoie la représentation HTML du composant
     * @return la traduction du composant sous forme de chaîne HTML
     */
    public String getHTMLRepresentation();
    
    /**
     * Renvoie l'identifiant du composant. Il sert d'attribut ID dans la balise la plus large
     * du HTML renvoyé par getHTMLRepresentation()
     * @return un entier long permettant d'identifier le composant.
     */
    public long getId();
    
    /**
     * Permet de spécifier manuellement l'id d'un composant texte. Utile notamment lors du chargement
     * @param id l'id du composant
     */
    public void setId(long id);
    
    /**
     * Modifie le composant pour qu'il prenne un aspect "sélectionné"
     */
    public void selectionner();

    /**
     * Modifie le composant pour qu'il reprenne son aspect habituel
     */
    public void deselectionner();
    
    /**
     * Appelé lors de la désactivation de l'onglet Texte,
     * Modifie le composant pour lui donner un aspect désactivé.
     */
//    public void desactiver();
    
    
    /**
     * Définit la couleur d'avant-plan du composant
     * @param foreground couleur d'avant-plan
     */
    public void setForeground(Color foreground);
    
    /**
     * Renvoie la couleur d'avant-plan du composant
     * @return La couleur d'avant-plan
     */
    public Color getForeground();
    
    /**
     * Définit la couleur d'arrière plan du composant
     * @param background couleur d'arrière-plan
     */
    public void setBackground(Color background);
    
    /**
     * Renvoie la couleur d'arrière plan du composant
     * @return la couleur d'arrière-plan
     */
    public Color getBackground();
    
    /**
     * Définit la font-size à appliquer au composant.
     * Ceci permet le fonctionnement des boutons "zoom"
     * @param size la nouvelle taille de référence
     */
    public void setFontSize(float size);
    
    /**
     * Récupère le font-size à appliquer au composant.
     * Ce font-size sert de taille de référence pour la taille du composant.
     * @return Le font-size de référence
     */
    public float getFontSize();
}
