/**
 * «Copyright 2012 François Billioud»
 *
 * This file is part of Bomehc.
 *
 * Bomehc is free software: you can redistribute it and/or modify under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * Bomehc is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY.
 *
 * You should have received a copy of the GNU General Public License along with
 * Bomehc. If not, see <http://www.gnu.org/licenses/>.
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

import bomehc.Configuration;
import bomehc.utils.fichiers.Adresse;
import bomehc.utils.fichiers.FichierML;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author François Billioud
 */
public abstract class Traducteur {

//    private static final String DOSSIER_LANGUES = Configuration.getDossierLangues();

    /** contient la liste des langues disponibles **/
//    private static String[] listeLangues = (new Adresse(DOSSIER_LANGUES)).listeNomFichiers(Adresse.EXTENSION_LANGUE);
    
    private static FichierML fLangue() { return Configuration.getFichierLangue(); }

    public static String[] getListeLangues() {
        return (new Adresse(Configuration.getDossierLangues())).listeNomFichiers(Adresse.EXTENSION_LANGUE);
    }

//    public static void setLangue(String langue) {
//        if (!Traducteur.getCurrentLanguage().equals(langue)) {
//            fLangue = new FichierML(DOSSIER_LANGUES + langue + "." + Adresse.EXTENSION_LANGUE);
//        }
//    }

    public static boolean isAvailable(String langue) {
        List<String> L = Arrays.asList(getListeLangues());
        return L.contains(langue);
    }

    public static String traduire(String reference) {
        return fLangue().getContenu(reference);
    }
    /** pour le debuggage **/
    public static String traduire(String reference, boolean check) {
        return fLangue().getContenu(reference, check);
    }

    /**
     * Récupère le texte dans le fichier langue, et le convertie au format HTML
     * si ce n'était pas initialement fait.
     *
     * @param reference la clé du texte à récupérer dans le fichier de langue
     * @return la chaine au format HTML
     */
    public static String traduireEnHTML(String reference) {
        String traduction = traduire(reference);
        if (traduction.startsWith("<html") && traduction.endsWith("</html>")) {
            return traduction;
        } else {
            return "<html>" + traduction + "</html>";
        }
    }

    /**
     * Récupère le texte dans le fichier langue, et change sa taille en fonction
     * de l'attribut size. Attention : il faut que ce qui va afficher cette
     * String puisse interpréter correctement le HTML.
     *
     * @param reference la clé du texte à récupérer dans le fichier de langue
     * @param size la taille du texte (en html) que l'on souhaite appliquer
     * @return une chaine html correspondant à la valeur de la clé dans le
     * fichier de langue et ayant une taille html correspondante à la size
     * donnée en paramètre
     *
    public static String traduire(String reference, int size) {
        String traduction = traduire(reference);
        if (traduction == null) {
            return null;
        }
        if (traduction.startsWith("<html") && traduction.endsWith("</html>")) {
            traduction = traduction.replaceAll("<html.*?>", "").replaceAll("</html>", "");
        }
        return "<html><font size='" + size + "'>" + traduction + "</font></html>";
    }*/

    public static String traduireEn(String reference, String langue) {
        FichierML fL = (Traducteur.getCurrentLanguage().equals(langue) ? fLangue() : new FichierML(Configuration.getDossierLangues() + langue + "." + Adresse.EXTENSION_LANGUE));
        return fL.getContenu(reference);
    }

    public static String[] getListeClasses() {
        return fLangue().getContenuBloc("classes");
    }

    public static String[] getInfoDialogue(String balise) {
        return fLangue().getContenuBloc(balise);
    }

    public static String getCurrentLanguage() {
        return Configuration.getLangue();
    }

    /**
     * Renvoie l'index de la classe dans le tableau des classes à partir de la
     * chaine représentant la classe ("6ème", "5ème",...)
     *
     * @param classe la chaine représentant la classe
     * @return l'index dans le tableau des classes de la classe passée en
     * paramètre
     */
    public static int getindexClasse(String classe) {
        for (int i = 0; i < getListeClasses().length; i++) {
            if (getListeClasses()[i].equals(classe)) {
                return i;
            }
        }
        return -1;
    }
}
