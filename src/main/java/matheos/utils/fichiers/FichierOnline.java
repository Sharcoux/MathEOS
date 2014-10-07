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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cette classe crée une table d'éléments lus sous la forme key::value à
 * partir d'un URL.
 * @author François Billioud
 */
public class FichierOnline {

    /** Tableau contenant les éléments lues dans le fichier et leur valeur **/
    private final HashMap<String, String> contenu = new HashMap<>();

    /** Adresse du fichier **/
    private URL url;

    /**
     * Methode permettant de lire un fichier, d'en trouver les balises et
     * de créer le Fichier correspondant.
     * @param nom : nom du fichier à lire
     */
    public FichierOnline(URL url) throws FileNotFoundException {
        this.url = url;
        chargement();
    }

    /**
     * Methode permettant de lire un fichier, d'en trouver les balises et
     * de créer le Fichier correspondant.
     * @param nom : nom du fichier à lire
     */
    public FichierOnline(String adresse) throws FileNotFoundException {
        String[] T = adresse.split("://");
        String protocol = T[0];
        String[] T2 = T[1].split("/",2);
        String domain = T2[0];
        String file = T2[1];
        try {
            url = new URL(protocol, domain, file);
            chargement();
        } catch (MalformedURLException ex) {
            Logger.getLogger(FichierOnline.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Charge les données depuis le fichier
     */
    public final void chargement() throws FileNotFoundException {
        contenu.clear();

        try (Scanner lecteur = new Scanner(url.openStream(), "UTF-8")) {
            //charge une ligne
            while (lecteur.hasNextLine()) {
                String ligneLue = lecteur.nextLine();
                //lecture par mots-clés
                String[] ligne = ligneLue.split("::");
                if(ligne.length>1) addContenu(ligne[0], ligne[1]);
            }
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            Logger.getLogger(FichierOnline.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Renvoie le contenu associé à la clé passée en paramètre.
     * Le boolean permet d'indiquer si la clé doit obligatoirement être présente
     * @param key clé de la valeur à chercher
     * @return valeur lue dans le fichier ou null si non présent
     */
    public String getContenu(String key) {
        return contenu.get(key);
    }

    /**
     * Renvoie le contenu associé à la clé passée en paramètre.
     * Le boolean permet d'indiquer si la clé doit obligatoirement être présente
     * @param key clé de la valeur à chercher
     * @param neverNull true si l'absence doit générer une erreur
     * @return valeur lue dans le fichier ou null si non présent
     */
    public String getContenu(String key, boolean neverNull) {
        String result = getContenu(key);
        if(result==null && neverNull) {Main.erreurCritique(null,key+" not found in "+this.url, 1);}
        return result;
    }

    private void addContenu(String key, String value) {
        contenu.put(key, value);
    }

}