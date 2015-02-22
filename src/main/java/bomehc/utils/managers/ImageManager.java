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

package bomehc.utils.managers;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.ImageObserver;
import static java.awt.image.ImageObserver.ALLBITS;
import bomehc.IHM;
import bomehc.utils.objets.Icone;
import java.io.File;
import java.util.HashMap;
import bomehc.Configuration;
import bomehc.utils.librairies.ImageTools;

/**
 *
 * @author François Billioud
 */
public abstract class ImageManager {

    private static final HashMap<String, Icone> iconesMap = new HashMap<>();//relie la source à l'image correspondante
    private static final HashMap<String, String> sourceMap = new HashMap<>();//relie la balise à la source

    /**
     * Retourne une icone dont la source est précisée dans le fichier de thème à la balise spécifiée.
     * L'ImageManager tente à tout prix de réutiliser les images pour réduire le temps de chargement
     * @param balise balise de l'icone dans le fichier de thème
     * @return Une Icone
     */
    public static Icone getIcone(String balise) {
        //Récupération de la source
        String source = IHM.getThemeElement(balise);
        if(source==null) {return null;}
        
        
        //Gestion des images références d'une autre
        if(source.contains("@")) {
            String[] T = source.split("@");
            String transformation = T[0];
            String referenceBalise = T[1];
            if(referenceBalise.equals("application")) {
                source = Configuration.APPLICATION_ICON;
            } else {
                Icone reference = getIcone(referenceBalise);
                if(reference==null) {return null;}
                return new Icone(reference, Icone.getTransformation(transformation));
            }
        }
        
        //Met à jour la liaison entre balises et sources (les sources peuvent changer lors d'un changement de thème)
        String oldSource = sourceMap.get(balise);
        if(!source.equals(oldSource)) {//la source a changée ou n'a jamais été chargée
            if(oldSource!=null && !sourceMap.containsValue(oldSource)) {
                iconesMap.remove(oldSource);//On supprime la source qui n'est plus utilisée
            }
            sourceMap.put(balise, source);//On remplace l'ancienne source par la nouvelle
        }
        
        //Récupération d'une icone déjà chargée ou chargement d'une nouvelle icone
        Icone icone = iconesMap.get(source);
        if(icone==null) {//on ne charge l'icone depuis la source que si cela n'a encore jamais été fait ou que la source a changée
            String sourceAbsolue = Configuration.getAdresseAbsolueFichier(source);
            if(!new File(sourceAbsolue).exists()) {System.out.println(source+" introuvable");return null;}
//            Image img = Toolkit.getDefaultToolkit().getImage(source);
            icone = new Icone(sourceAbsolue);
            iconesMap.put(balise, icone);
            return icone;
        }
        return icone;
    }

    /**
     * Retourne une icone dont la source est précisée dans le fichier de thème à la balise spécifiée et aux dimensions spécifiées.
     * L'ImageManager tente à tout prix de réutiliser les images pour réduire le temps de chargement
     * @param balise balise de l'icone dans le fichier de thème
     * @param largeur largeur requise
     * @param hauteur hauteur requise
     * @return Une Icone à la bonne dimension
     */
    public static Icone getIcone(String balise,final int largeur, final int hauteur) {
        //Récupération de la source
        String source = IHM.getThemeElement(balise);
        if(source==null) {return null;}
        
        //Gestion des images références d'une autre
        if(source.contains("@")) {
            String[] T = source.split("@");
            String transformation = T[0];
            String referenceBalise = T[1];
            if(referenceBalise.equals("application")) {
                source = Configuration.APPLICATION_ICON;
            } else {
                return new Icone(getIcone(referenceBalise,largeur,hauteur), Icone.getTransformation(transformation));
            }
        }
        
        //Met à jour la liaison entre balises et sources (les sources peuvent changer lors d'un changement de thème)
        String oldSource = sourceMap.get(balise);
        if(!source.equals(oldSource)) {//la source a changée ou n'a jamais été chargée
            if(oldSource!=null && !sourceMap.containsValue(oldSource)) {
                iconesMap.remove(oldSource);//On supprime la source qui n'est plus utilisée
                iconesMap.remove(oldSource+largeur+":"+hauteur);//On supprime la source qui n'est plus utilisée
            }
            sourceMap.put(balise, source);//On remplace l'ancienne source par la nouvelle
        }
        
        //Récupération d'une icone déjà chargée ou chargement d'une nouvelle icone
//        Icone icone = iconesMap.get(source+largeur+":"+hauteur);//prenait trop de ressources
//        if(icone==null || !source.equals(sourceMap.get(balise))) {//aucune image de cette taille ou source modifiée
            Icone unsizedIcone = getIcone(balise);
            if(unsizedIcone==null) {return null;}
            //On crée une image de la bonne dimension à partir de l'icone précédemment chargée
//            icone = new Icone(ImageTools.getScaledInstance(ImageTools.imageToBufferedImage(unsizedIcone.getImage()), largeur, hauteur, ImageTools.Quality.AUTO, ImageTools.FIT_EXACT));
            if(unsizedIcone.getImageLoadStatus()==MediaTracker.COMPLETE) {
                return new Icone(ImageTools.getScaledInstance(ImageTools.imageToBufferedImage(unsizedIcone.getImage()), largeur, hauteur, ImageTools.Quality.AUTO, ImageTools.FIT_EXACT));
            }
            Image im = unsizedIcone.getImage();
            final Icone icone = new Icone(im);
            im.getWidth(new ImageObserver() {
                @Override
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    if((infoflags & ALLBITS)!=0) {
                        icone.setSize(largeur, hauteur);
                        return false;
                    }
                    return true;
                }
            });
            
            /*iconesMap.put(source+largeur+":"+hauteur, icone);*///consomme trop de ressources
//        }
        return icone;
    }

}
