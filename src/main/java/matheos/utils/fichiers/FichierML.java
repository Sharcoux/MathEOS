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
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe qui lit un fichier, repère les balises de la forme "Balise<"
 * et en extrait les lignes jusqu'à la balise ">". Cette classe crée
 * aussi une table d'éléments lus sous la forme key::value
 * @author François Billioud
 */
public class FichierML {

    /** Variable marquant le début d'une liste de noms **/
    private static final String DEBUT = "<";

    /** Variable marquant la fin d'une liste de noms **/
    private static final String ARRET = ">";

    /** Tableau contenant les balises lues dans le fichier et leur contenu **/
    private final HashMap<String, String[]> contenuBloc = new HashMap<>();

    /** Tableau contenant les éléments lues dans le fichier et leur valeur **/
    private final HashMap<String, String> contenu = new HashMap<>();

    /** Adresse du fichier **/
    private Adresse adresse;

    /**
     * Methode permettant de lire un fichier, d'en trouver les balises et
     * de créer le Fichier correspondant.
     * @param nom : nom du fichier à lire
     */
    public FichierML(File nom) {
        this(nom.toString());
    }

    /**
     * Methode permettant de lire un fichier, d'en trouver les balises et
     * de créer le Fichier correspondant.
     * @param nom : nom du fichier à lire
     */
    public FichierML(String nom) {
        adresse = new Adresse(nom);
        if(!adresse.exists()) {matheos.Main.erreurCritique(null,"le fichier "+nom+" n'existe pas", 1);}
        else {
            chargement();
        }//charge un document existant
    }

    /**
     * Charge les données depuis le fichier
     */
    public final void chargement() {
        contenu.clear();
        contenuBloc.clear();

        try (Scanner lecteur = new Scanner(adresse, "UTF-8")) {

            //charge une ligne
            while (lecteur.hasNextLine()) {
                String ligneLue = lecteur.nextLine();

                //lecture par balises
                if(ligneLue.endsWith(DEBUT)) {// Cherche la balise
                    String nomBalise = ligneLue.replaceAll(DEBUT, "");
                    LinkedList<String> lignesLues = new LinkedList<>();
                    //lis le contenu
                    boolean poursuite = true;
                    do {
                        ligneLue = lecteur.nextLine();
                        if(ligneLue.equals(ARRET)) {poursuite = false;}
                        else {
                            lignesLues.add(ligneLue);
                        }
                    } while (poursuite);
                    String[] T = lignesLues.toArray(new String[lignesLues.size()]);
                    addContenuBloc(nomBalise, T);
                }

                //lecture par mots-clés
                String[] ligne = ligneLue.split("::");
                if(ligne.length>1) addContenu(ligne[0], ligne[1]);

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FichierML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * renvoie le nom du fichier privé de son extension
     * @return le nom du fichier privé de son extension
     */
    public String getNom() {
        return adresse.getNom();
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
        if(result==null && neverNull) {Main.erreurCritique(null,key+" not found in "+this.adresse, 1);}
        return result;
    }

    private void addContenu(String key, String value) {
        contenu.put(key, value);
    }

    public String[] getContenuBloc(String key) {
        return contenuBloc.get(key);
    }

    private void addContenuBloc(String key, String[] value) {
        contenuBloc.put(key, value);
    }
    
    /** Ajoute dans ce fichier les données lues depuis un autre. Les modifications ne sont pas enregistrées
     * @param fichier : le fichier contenant les données à ajouter/écraser**/
    public void fusionner(FichierML fichier) {
        contenu.putAll(fichier.contenu);
        contenuBloc.putAll(fichier.contenuBloc);
    }

}