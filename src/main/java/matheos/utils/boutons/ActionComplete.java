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

package matheos.utils.boutons;

import matheos.utils.objets.Icone;
import java.awt.Dimension;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Cette classe permet de créer une action qui prend en compte les rolloverIcon, SelectedIcon
 * et détermine si l'arrière-plan du bouton doit-être dessiné
 * @author François Billioud
 */
@SuppressWarnings("serial")
public abstract class ActionComplete extends AbstractAction {

    /** Le nom de la propriété correspondant à l'image de rollover **/
    public static final String ROLLOVER_ICON = "rollover icon";
    /** Le nom de la propriété correspondant à l'image de selection **/
    public static final String SELECTED_ICON = "selected icon";
    /** Le nom de la propriété correspondant au fait de devoir dessiner l'arrière-plan ou non **/
    public static final String DRAW_BACKGROUND = "draw background";

    /** Constructeur créant une action vierge **/
    public ActionComplete() {}

    /**
     * Constructeur créant une action à partir des fichiers de thème et de langue.<br/>
     * Dans le fichier de langue, on pourra indiquer :<br/>
     * <i>aspect</i>::titre du bouton<br/>
     * <i>aspect</i> <b>description</b>::tooltip<br/>
     * Dans le fichier de thème, on pourra indiquer :<br/>
     * <i>aspect</i>::icone du bouton<br/>
     * <i>aspect</i> <b>rollover</b>::icone à afficher au passage de la souris<br/>
     * <i>aspect</i> <b>selected</b>::icone à afficher quand le bouton est enfoncé<br/>
     * <i>aspect</i> <b>background</b>::<b>no</b> si l'arrière-plan ne doit pas être dessiné<br/>
     * @param aspect le nom de la balise à chercher dans les fichiers de langue et de theme
     */
    public ActionComplete(String aspect) {
        InfoBouton info = new InfoBouton(aspect);
        info.addParametersToAction(this);
    }

    public void setName(String nom) {
        putValue(NAME, nom);
    }
    
    public Dimension getSize() {
        if(getValue(LARGE_ICON_KEY)!=null) {
            Icone icon = (Icone) getValue(LARGE_ICON_KEY);
            return new Dimension(icon.getIconWidth(), icon.getIconHeight());
        }
        return null;
    }
    
    public void setSizeByWidth(int largeur) {
        if(getValue(LARGE_ICON_KEY)==null) {return;}
        Icone icon = (Icone) getValue(LARGE_ICON_KEY);
        InfoBouton info = new InfoBouton((String) getValue(Action.ACTION_COMMAND_KEY), largeur, icon.calculHauteur(largeur));
        info.addParametersToAction(this);//On recalcule les images depuis la source à la bonne dimension
    }
    public void setSizeByHeight(int hauteur) {
        if(getValue(LARGE_ICON_KEY)==null) {return;}
        Icone icon = (Icone) getValue(LARGE_ICON_KEY);
        InfoBouton info = new InfoBouton((String) getValue(Action.ACTION_COMMAND_KEY), icon.calculLargeur(hauteur), hauteur);
        info.addParametersToAction(this);//On recalcule les images depuis la source à la bonne dimension
    }
    public void setSize(int largeur, int hauteur) {
        if(getSize()==null) {return;}
        Dimension size = getSize();
        if(size.width==largeur) {return;}
        
        InfoBouton info = new InfoBouton((String) getValue(Action.ACTION_COMMAND_KEY), largeur, hauteur);
        info.addParametersToAction(this);//On recalcule les images depuis la source à la bonne dimension
    }

    /**
     * En plus de la gestion du rollover, de la selection et de l'arrière-plan,
     * cette classe permet de gérer la selection ou non du bouton, et de synchroniser
     * l'état sélectionné ou non de tous les boutons implémentant cette action.
     * @author François Billioud
     */
    public static abstract class Toggle extends ActionComplete {
        /** crée un action de type toggle dans l'état de selection spécifié **/
        public Toggle(boolean etatInitial) {
            setSelected(etatInitial);
        }
        /** crée un action de type toggle à partir de l'aspect lu dans le thème et dans le fichier de langue.
         * L'action est initialisée dans l'état de sélection spécifié en paramètre.
         **/
        public Toggle(String aspect, boolean etatInitial) {
            super(aspect);
            setSelected(etatInitial);
        }
        /** permet de définir l'état de sélection de l'action.
         * Ceci impact immédiatement les boutons qui implémentent cette action
         **/
        public void setSelected(boolean b) {
            putValue(ActionComplete.SELECTED_KEY, b);
        }
        /** permet de connaitre l'état, sélectionné ou non, de cette action **/
        public boolean isSelected() {return (Boolean)getValue(ActionComplete.SELECTED_KEY);}
    }

}
