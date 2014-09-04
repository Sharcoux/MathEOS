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

package matheos.utils.fichiers;

import matheos.Main;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author François Billioud
 */
public class FichierTexte {

    /**
     * Adresse du fichier
     */
    private Adresse adresse;

    /**
     * Contenu du fichier, accessible par mots clés
     */
    private HashMap<String, String> contenu = new HashMap<String, String>();

    /**
     * Methode permettant de lire un fichier, d'en trouver les balises et
     * de créer le Fichier correspondant.
     * @param nom : nom du fichier à lire
     */
    public FichierTexte(File adresse) {
        this(adresse.toString());
    }

    /**
     * Methode permettant de lire un fichier, d'en trouver les balises et
     * de créer le Fichier correspondant.
     * @param nom : nom du fichier à lire
     */
    public FichierTexte(String adresse) {
        this.adresse = new Adresse(adresse);
        if(this.adresse.exists()) {chargement();}//charge un document existant
        else {sauvegarde();}//crée un nouveau document
    }

    /**
     * Methode permettant de lire les lignes du fichier.
     * Retourne un tableau contenant les lignes lues.
     * @param adresse l'adresse du fichier à lire
     * @return tableau de chaînes de caractères
     */
    public final void chargement() {

        Scanner lecteur=null;
        try {
            lecteur = new Scanner(new FileReader(adresse));
            while(lecteur.hasNextLine()) {
                String ligne = lecteur.nextLine();
                //Attention, lorsqu'on modifie conifg.ini à la main at qu'on sauvegarde, un point apparaît 
                //en premier caractere et fausse la lecture, d'où l'ajout d'une ligne en commentaire en première ligne
                if(!ligne.contains(":")){
                    continue;
                }
                String[] ligneLue = ligne.split(" : ");
                addContenu(ligneLue[0],ligneLue[1]);
            }
        }
        catch (Exception e) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            Main.erreurCritique(e,"impossible de lire "+this.getAdresse(), 1);
        }
        finally {
            if(lecteur!=null) {lecteur.close();}
        }
    }

    /**
     * Methode permettant d'écrire les données dans un fichier
     */
    public final void sauvegarde() {
        if(!adresse.exists()) {
            try {
                if (adresse.getParentFile() != null && !adresse.getParentFile().exists()) {
                    adresse.getParentFile().mkdirs();
                }
                adresse.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(FichierTexte.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            BufferedWriter ecriveur  = new BufferedWriter(new FileWriter(adresse));

            Iterator<Entry<String, String>> iter = getContenu().entrySet().iterator();
            ecriveur.write("--properties");
            ecriveur.newLine();
            while(iter.hasNext()) {
                Entry<String, String> e = iter.next();
                ecriveur.write(e.getKey() + " : " + e.getValue());
                ecriveur.newLine();
            }
            ecriveur.close();

        }
        catch (Exception e) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
            Main.erreurCritique(e,"impossible d'écrire "+this.getAdresse(), 1);
        }

    }

    /**
     * renvoie le nom du fichier privé de son extension
     * @return le nom du fichier privé de son extension
     */
    public String getNom() {
        return adresse.getNom();
    }

    public HashMap<String, String> getContenu() {
        return contenu;
    }

    /**
     * Renvoie la valeur associée à la balise passée en paramètre, ou null si
     * la balise n'est pas dans la table
     * @param key la balise
     * @return la valeur associée
     */
    public String getContenu(String key) {
        return contenu.get(key);
    }

    public void addContenu(String key, String value) {
        contenu.put(key, value);
        sauvegarde();
    }

    public void removeContenu(String key) {
        contenu.remove(key);
    }

    public void deleteOnExit() {
        adresse.deleteOnExit();
    }

    /**
     * renvoie l'adresse exacte du fichier
     * @return une instance de la classe Adresse
     */
    public Adresse getAdresse() {return adresse;}

}
