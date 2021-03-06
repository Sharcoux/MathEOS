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

package matheos.sauvegarde;

import matheos.json.Json;
import matheos.utils.librairies.ImageTools;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * class permettant d'enregistrer l'ensemble des données d'un JMathTextPane, Component compris.
 * @author François Billioud
 */
public class DataTexte extends DataObject {
    private static final long serialVersionUID = 1L;

    //nom des données
    private static final String CONTENU_HTML = "contenuHTML";
    private static final String DONNEES_IMAGES = "donneesImages";
//    private static final String DONNEES_TP = "donneesTP";
    
    //permet de conserver les images en cas de réutilisation
//    private final transient HashMap<String, BufferedImage> donneesImages = new HashMap<>();
    
    public String getContenuHTML() {return getElement(CONTENU_HTML);}
    public void setContenuHTML(String contenu) {putElement(CONTENU_HTML, contenu);}
    
    /**
     * Récupère l'image associé à l'id passée en paramètre.
     * @param spanID id du span englobant l'image
     * @return l'image chargée depuis le DataTexte
     */
    public BufferedImage getImage(String spanID) {

        Data donneesImages = getData(DONNEES_IMAGES);
        if(donneesImages==null) {return null;}
        
        BufferedImage im = null;
//        im = donneesImages.get(id);
//        if(im==null) {
            String image = donneesImages.getElement(spanID);
            try {
                byte[] byteImage = (byte[]) Json.toJava(image,byte[].class);
                im = ImageTools.getImageFromArray(byteImage);
//                donneesImages.put(id, im);
            } catch(IOException ex) {
                System.out.println("error reading image : "+spanID);
                Logger.getLogger(DataTexte.class.getName()).log(Level.SEVERE, null, ex);
            }
//        }
        return im;
    }
    
    public Set<String> imageIds() {
        Data donneesImages = getData(DONNEES_IMAGES);
        if(donneesImages==null) {return new HashSet<>();}
        return donneesImages.getElementKeys();
    }
    
    /**
     * Enregistre l'image dans le dataTexte. L'image ne peut pas figurer dans le html lui-même. Pour cette raison
     * elle doit être enregistrée à côté.
     * @param spanID identifiant du span englobant l'image
     * @param image l'image à enregistrer, ou null pour supprimer l'image
     * @return l'ancienne image enregistrée pour cet id
     */
    public BufferedImage putImage(String spanID, BufferedImage image) {
        BufferedImage oldImage = getImage(spanID);
        Data donneesImages = getData(DONNEES_IMAGES);
        if(donneesImages==null) {donneesImages = new DataObject();putData(DONNEES_IMAGES, donneesImages);}
        if(image==null) {
            donneesImages.removeElementByKey(spanID);
        } else {
            try {
                byte[] byteImage = ImageTools.getArrayFromImage(image);
                String im = Json.toJson(byteImage);
                donneesImages.putElement(spanID, im);
            } catch(IOException ex) {
                System.out.println("error writing image : "+spanID);
                Logger.getLogger(DataTexte.class.getName()).log(Level.SEVERE, null, ex);
                donneesImages.getElement(spanID);
            }
        }
        return oldImage;
    }
    
    /**
     * Enregistre les données nécessaires à la reconstruction du TP pour pouvoir
     * l'éditer.
     * @param spanID id du span englobant le TP
     * @param data les données à enregistrer
     * @return les anciennes données enregistrées pour cet id, ou null
     */
    public Data putTP(String spanID, Data data) {
        return putData(spanID,data);
    }
    
    /**
     * Récupère les données enregistrées pour l'id passé en paramètre
     * @param spanID id du span englobant
     * @return un Data contenant les données du tp.
     */
    public Data getTP(String spanID) {
        return getData(spanID);
    }
    
    public DataTexte(String contenu) {
        putElement(CONTENU_HTML, contenu);
    }
    

}
