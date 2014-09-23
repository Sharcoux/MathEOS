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

package matheos.utils.fichiers;

import matheos.Main;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author François Billioud
 */
public class FichierConfig {
    
    /** Adresse du fichier **/
    private File adresse;
    /** Contenu du fichier, accessible par mots clés **/
    private final Map<String, String> contenu = new LinkedHashMap<>();
    
    /**
     * Methode permettant de lire un fichier, d'en trouver les balises et
     * de créer le Fichier correspondant.
     * @param adresse : adresse du fichier à lire
     */
    public FichierConfig(String adresse) {
        this(new File(adresse));
    }
    
    /**
     * Methode permettant de lire un fichier, d'en trouver les balises et
     * de créer le Fichier correspondant.
     * @param adresse : adresse du fichier à lire
     */
    public FichierConfig(File adresse) {
        this.adresse = adresse;
        if(adresse.exists()) {chargement();}
    }

    /**
     * Renvoie la valeur associée à la balise passée en paramètre, ou null si
     * la balise n'est pas dans la table
     * @param key la balise
     * @return la valeur associée
     */
    public String getProperty(String key) {
        return contenu.get(key);
    }

    public void addProperty(String key, String value) {
        setProperty(key, value);
    }

    public void setProperty(String key, String value) {
        contenu.put(key, value);
        sauvegarde(key, value);
    }

    public void removeProperty(String key) {
        contenu.remove(key);
        remove(key);
    }
    
    public void setProperties(Map<String, String> contenuSupplementaire) {
        contenu.putAll(contenuSupplementaire);
        sauvegarde();
    }

    public void deleteOnExit() {
        adresse.deleteOnExit();
    }

    /**
     * renvoie l'adresse exacte du fichier
     * @return une instance de la classe Adresse
     */
    public File getAdresse() {return adresse;}
    
    private void sauvegarde() {
        if(!adresse.exists()) {
            try {
                if (adresse.getParentFile() != null && !adresse.getParentFile().exists()) {
                    adresse.getParentFile().mkdirs();
                }
                adresse.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(FichierConfig.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try(FileWriter writer = new FileWriter(adresse);) {
            for(Entry<String, String> entry : contenu.entrySet()) {
                if(entry.getKey().startsWith("comment")) {writer.write(entry.getValue()+System.lineSeparator());}
                else {writer.write(entry.getKey()+"="+entry.getValue()+System.lineSeparator());}
            }
        }
        catch (IOException e) {
            Logger.getLogger(FichierConfig.class.getName()).log(Level.SEVERE, null, e);
//            Main.erreurCritique("impossible d'écrire "+this.getAdresse(), 1);
        }
    }

    private void sauvegarde(String key, String value) {
        sauvegarde();
    }

    private void remove(String key) {
        contenu.remove(key);
        sauvegarde(key, null);
    }

    private void chargement() {
        try (Scanner lecteur = new Scanner(new FileReader(adresse))) {
            int commentID = 0;
            while(lecteur.hasNextLine()) {
                String lineContent = lecteur.nextLine();
                if(!lineContent.contains("=")){contenu.put("comment"+commentID++, lineContent);}
                else {
                    String[] ligneLue = lineContent.split("=");
                    if(ligneLue.length==1) { contenu.put(ligneLue[0].trim(),""); }//HACK pour les chaines vides
                    else {contenu.put(ligneLue[0].trim(),ligneLue[1].trim());}
                }
            }
        }
        catch (Exception e) {
//            Logger.getLogger(FichierConfig.class.getName()).log(Level.SEVERE, null, e);
            Main.erreurCritique(e, "impossible de lire "+this.getAdresse(), 1);
        }
    }
    
}
