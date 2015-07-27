/** «Copyright 2012 François Billioud»
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

import matheos.IHM;
import matheos.utils.objets.Icone;
import matheos.utils.managers.ImageManager;
import matheos.utils.managers.Traducteur;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.KeyStroke;
import matheos.utils.librairies.ImageTools;

/**
 * Classe qui permet de recenser toutes les infos nécessaires à la création d'un bouton
 * d'un composant quelconque du logiciel
 * @author François Billioud
 */
public class InfoBouton {
    public static final String MENU = " menu";
    /** Le mot-clé pour l'icone de rollover **/
    public static final String PASSAGE = " rollover";
    /** Le mot-clé pour l'icone de selection **/
    public static final String SELECTION = " selected";
    /** Le mot-clé pour le texte du tooltip **/
    public static final String DESCRIPTION = " description";
    /** Le mot-clé pour la propriété d'affichage de l'arrière-plan **/
    public static final String FOND = " background";
    /** Le mot-clé pour la propriété de raccourci clavier **/
    public static final String ACCELERATOR_KEY = " shortcut";

    /** contient la balise traduisant l'aspect du bouton **/
    protected String balise;

    /** contient le nom du bouton **/
    protected String nom;

    /** source de l'icone du bouton de base **/
    protected Icone icone;

    /** source de l'icone au passage de la souris **/
    protected Icone iconePassage = null;

    /** source de l'icone en cas de clic **/
    protected Icone iconeSelection = null;

    /** source de l'icone pour les menus **/
    protected Icone iconeMenu;

    /** description de l'effet du bouton **/
    protected String texteDescription;

    /** Si le bouton doit afficher un fond ou non **/
    protected boolean drawBackground = true;

    /** Une éventuelle touche de raccourci avec ctrl (exemple  t pour ctrl+t) **/
    protected String acceleratorKey;

    /**
     * Lis les informations nécessaires au composant passé en paramètre
     * @param balise balise du composant concerné
     */
    InfoBouton(String balise) {
        this(balise, 0, 0);
    }

    /**
     * Lis les informations nécessaires au composant passé en paramètre et dimensionne les icones
     * @param balise balise du composant concerné
     * @param largeur largeur désirée pour les icones
     * @param hauteur hauteur désirée pour les icones
     */
    InfoBouton(String balise, int largeur, int hauteur) {
        nom = Traducteur.traduire(balise);
        texteDescription = Traducteur.traduire(balise + DESCRIPTION);
        drawBackground = !"no".equals(IHM.getThemeElement(balise + FOND));
        acceleratorKey = Traducteur.traduire(balise + ACCELERATOR_KEY);
        this.balise = balise;
        
        if(largeur==0 || hauteur==0) {
            icone = ImageManager.getIcone(balise);
            if(icone!=null) {
                iconeSelection = ImageManager.getIcone(balise + SELECTION);
                iconePassage = ImageManager.getIcone(balise + PASSAGE);
                if(iconePassage==null && !drawBackground) {//On crée l'icone de rollover avec une ombre de l'icone normale
                    iconePassage = new Icone();
                    iconePassage.setImage(ImageTools.getShadowedImage(ImageTools.imageToBufferedImage(icone.getImage()),iconePassage.getImageObserver()));
                }
            }
            iconeMenu = ImageManager.getIcone(balise + MENU);
        } else {
            icone = ImageManager.getIcone(balise, largeur, hauteur);
            if(icone!=null) {
                iconeSelection = ImageManager.getIcone(balise + SELECTION, largeur, hauteur);
                iconePassage = ImageManager.getIcone(balise + PASSAGE, largeur, hauteur);
                if(iconePassage==null && !drawBackground) {//On crée l'icone de rollover avec une ombre de l'icone normale
                    iconePassage = new Icone(ImageTools.getShadowedImage((BufferedImage)icone.getImage(),null));
                }
            }
            iconeMenu = ImageManager.getIcone(balise + MENU, largeur, hauteur);
        }
        
        if(acceleratorKey!=null && texteDescription!=null) {
            if(!texteDescription.startsWith("<html>")) {texteDescription+=" ("+acceleratorKey+")";}
//            texteDescription=(texteDescription==null ? "" : texteDescription)+" ("+acceleratorKey+")";
        }
    }

    /**
     * Lis les informations depuis le composant passé en paramètre
     * @param bouton bouton concerné
     */
    InfoBouton(AbstractButton bouton) {
        nom = bouton.getText();
        icone = (Icone) bouton.getIcon();
        iconePassage = (Icone) bouton.getRolloverIcon();
        iconeSelection = (Icone) bouton.getSelectedIcon();
        iconeMenu = (Icone) bouton.getAction().getValue(Action.SMALL_ICON);
        texteDescription = bouton.getToolTipText();
        drawBackground = bouton.isContentAreaFilled();
        this.balise = bouton.getActionCommand();
        
        Object o = ((KeyStroke)bouton.getAction().getValue(Action.ACCELERATOR_KEY));
        if(o!=null) {
            String s = o.toString();
            acceleratorKey = (s.contains("ctrl")?"+ctrl":"")+(s.contains("alt")?"+alt":"")+(s.contains("shift")?"+shift":"")+s.charAt(s.length()-1);
        } else {acceleratorKey=null;}
    }

    /**
     * Ajoute les icones au bouton.
     * @param bouton le bouton à renseigner
     */
    protected void setIconesBouton(AbstractButton bouton) {
        bouton.setIcon(icone);
        bouton.setRolloverIcon(iconePassage);
        bouton.setSelectedIcon(iconeSelection);
    }

    /**
     * Ajoute les données liées à l'aspect de l'action (lues dans les fichiers thème et langue)
     * @param action l'action à renseigner
     */
    protected void addParametersToAction(Action action) {
        if(action==null) return;
        action.putValue(Action.NAME, nom);                                  //nom (menu,bouton)
        if(icone!=null) {
            action.putValue(Action.LARGE_ICON_KEY, icone);                  //icone du bouton
        }
        if(iconePassage!=null) {
            action.putValue(ActionComplete.ROLLOVER_ICON, iconePassage);    //icone au passage de la souris
        }
        if(iconeSelection!=null) {
            action.putValue(ActionComplete.SELECTED_ICON, iconeSelection);  //icone lors de la sélection
        }
        if(iconeMenu!=null) {
            action.putValue(ActionComplete.SMALL_ICON, iconeMenu);          //icone pour les menus
        }
        action.putValue(Action.SHORT_DESCRIPTION, texteDescription);        //description utilisée pour les tooltip
        action.putValue(Action.ACTION_COMMAND_KEY, balise);                 //contient la balise permettant de charger les icones du bouton
        action.putValue(ActionComplete.DRAW_BACKGROUND, drawBackground);    //Définit si le bouton doit dessiner son background
        
        if(acceleratorKey!=null) {
            char key = Character.toUpperCase(acceleratorKey.charAt(acceleratorKey.length()-1));
            int modifier = (acceleratorKey.contains("ctrl")?KeyEvent.CTRL_DOWN_MASK:0)
                    +(acceleratorKey.contains("alt")?KeyEvent.ALT_DOWN_MASK:0)
                    +(acceleratorKey.contains("shift")?KeyEvent.SHIFT_DOWN_MASK:0);
            action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key,modifier));
        }
    }

}