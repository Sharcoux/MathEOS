/** «Copyright 2012,2013 François Billioud»
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

package matheos;

import matheos.patchs.Patch;
import matheos.sauvegarde.DataProfil;
import matheos.utils.dialogue.DialogueBloquant;
import matheos.utils.fichiers.Adresse;
import matheos.utils.fichiers.FichierConfig;
import matheos.utils.fichiers.FichierML;
import matheos.utils.managers.Traducteur;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gère le matching entre la configuration par défaut, les paramètres de la
 * dernière utilisations, et les paramètres du profil utilisé.
 * La classe Configuration est la gardienne de l'instance Profil utilisée et des paramètres utilisateurs.
 * Elle assure que le fichier de configuration, le fichier de profil, la configuration et le profil
 * sont synchronisés.
 * @author François Billioud
 */
public final class Configuration {

    private static final String VERSION = "1.1";
    private static final Integer ID_VERSION = 3;
    private static final String ADRESSE_SITE = "http://matheos.free.fr/";

//    private static final String FICHIER_CONFIGURATION = System.getProperty("user.home")+Adresse.separatorChar+"MathEOS"+Adresse.separatorChar+"config.ini"; //adresse configuration initiale du logiciel
    private static final String INSTALL_PARAMETERS_FILE = "config.ini"; //adresse configuration initiale du logiciel
    private static final String USER_PARAMETERS_FILENAME = "user.ini"; //adresse configuration initiale du logiciel

    private static String dossierCourant = null;   //contient le dossier parent du fichier en cours d'utilisation
    private static final FichierConfig userConfig;       //contient les paramètres du dernier utilisateur
    private static final FichierConfig installConfig;    //contient les paramètres d'installation
    private static DataProfil profil = null;        //contient le Profil actuellement utilisé
    private static final FichierML fSources;       //contient la localisation des images du logiciel
    private static final FichierML fLangue;        //contient les traductions

    public static String getVersion() {return VERSION; }
    public static Integer getIdVersion() {return ID_VERSION; }
    public static String getAdresseSite() {return ADRESSE_SITE; }
    public static String getDossierCourant() {return dossierCourant; }
    public static String getDossierThemes() {return installConfig.getProperty("dossierThemes");}
    public static String getDossierLangues() {return installConfig.getProperty("dossierLangues");}
    public static String getDossierApplication() {return installConfig.getProperty("dossierApp");}
    public static String getDossierTemp() {return getDossierApplication()+"temp"+Adresse.separator;}
    
    public static String getURLDossierImagesTemp() {
        String adresse = "file:///"+getDossierTemp().replace(Adresse.separator, "/");//+"/";
        return adresse;
    }
    static DataProfil getProfil() {return profil;}

    static void setProfil(DataProfil newProfil) {
        profil = newProfil;
        Patch.patcher(profil);//Met à jour le ficher si nécessaire
        if(profil.getAdresseFichier()!=null) {
            userConfig.setProperty("profil",profil.getAdresseFichier());
            setDossierCourant(new Adresse(profil.getAdresseFichier()).getParent()+Adresse.separatorChar);
        }
        setLangue(profil.getLangue());
        setTheme(profil.getTheme());
    }

    static void setProfil(String adresseProfil) {
        try {
            DataProfil p = (DataProfil) new Adresse(adresseProfil).chargement();//lit les données du document
            setProfil(p);//lit les paramètres utilisateur contenus dans le fichier
            p.setAdresseProfil(adresseProfil);//On met à jour les données du document au cas où le fichier ait été déplacé
        } catch (ClassCastException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "adresse : "+adresseProfil, ex);
            String error = String.format(Traducteur.traduire("error invalid file"), (Object)adresseProfil);
            DialogueBloquant.error(Traducteur.traduire("error"), error);
        }
    }

    static private void setDossierCourant(String dossier) {
        dossierCourant = dossier;
        userConfig.setProperty("dossierUtilisateur", dossier);
    }
    
    static void setAdresseFichierCourant(String adresse) {
        setDossierCourant(adresse);
        profil.setAdresseProfil(adresse);
        userConfig.setProperty("profil", adresse);
    }
    
    static void setLangue(String langue) {
        if (!getLangue().equals(langue)) {
            if(!getLangue().equals(installConfig.getProperty("langue"))) {fLangue.chargement();}//On réinitialise les textes par défaut
            if(!langue.equals(installConfig.getProperty("langue"))) {fLangue.fusionner(new FichierML(getDossierLangues()+ langue + "." + Adresse.EXTENSION_LANGUE));}//on modifie les données avec celles du nouveau fichier
            userConfig.setProperty("langue", langue);
            profil.setLangue(langue);
        }
    }
    public static FichierML getFichierLangue() {return fLangue;}
    
    static void setTheme(String theme) {
        if (!getTheme().equals(theme)) {
            if(!getTheme().equals(installConfig.getProperty("theme"))) {fSources.chargement();}//On réinitialise les sources par défaut
            if(!theme.equals(installConfig.getProperty("theme"))) {fSources.fusionner(new FichierML(getDossierThemes()+ theme + "." + Adresse.EXTENSION_THEME));}//on modifie les données avec celles du nouveau fichier
            userConfig.setProperty("theme", theme);
            profil.setTheme(theme);
        }
    }
    static public FichierML getFichierTheme() {return fSources;}

//    private static String getDonneeConfig(String s) {return userConfig.getContenu(s); }
    public static String getTheme() {return userConfig.getProperty("theme"); }
    public static String getLangue() {return userConfig.getProperty("langue"); }
    public static String getNomUtilisateur() {return profil.getNom().toUpperCase()+" "+profil.getPrenom(); }
    public static String getClasse() {return profil.getClasse()+" "+profil.getClasseID(); }

    /** Charge la configuration du fichier .config, ou charge la configuration par défaut si impossible **/
    static {

        //lis le fichier des paramètres d'installation :
        Adresse installConfigFile = new Adresse(INSTALL_PARAMETERS_FILE);
        
        //fixe manuellement les paramètres en cas de disparition du fichier de config
        if(!installConfigFile.exists()) {
            System.out.println(installConfigFile+" not found");
            Map<String, String> properties = new HashMap<>();
            properties.put("langue", "francais");
            properties.put("theme", "default");
            properties.put("dossierThemes", "Themes"+Adresse.separatorChar);
            properties.put("dossierLangues", "Langues"+Adresse.separatorChar);
            properties.put("dossierUtilisateur", System.getProperty("user.home")+Adresse.separatorChar+"MathEOS"+Adresse.separatorChar);
            properties.put("dossierApp", System.getProperty("user.home")+Adresse.separatorChar+"MathEOS"+Adresse.separatorChar);
            properties.put("singleUser", "true");
            installConfig = new FichierConfig(properties.get("dossierApp")+INSTALL_PARAMETERS_FILE);
            installConfig.setProperties(properties);
        } else {
            installConfig = new FichierConfig(installConfigFile);
        }
        
        //On charge les fichiers par défaut
        fLangue = new FichierML(getDossierLangues() + installConfig.getProperty("langue") + "." + Adresse.EXTENSION_LANGUE);
        fSources = new FichierML(getDossierThemes()+ installConfig.getProperty("theme") + "." + Adresse.EXTENSION_THEME);

        //Lecture du fichier de préférences utilisateur
        Adresse userConfigFile = new Adresse(getDossierApplication()+USER_PARAMETERS_FILENAME);
        userConfig = new FichierConfig(userConfigFile);
        if(!installConfig.getProperty("singleUser").equals("true") || !userConfigFile.exists()) {
            //fixe manuellement les paramètres en cas de première utilisation ou d'utilisation multi utilisateur
            Map<String, String> properties = new HashMap<>();
            properties.put("profil", "default");
            properties.put("langue", installConfig.getProperty("langue"));
            properties.put("theme", installConfig.getProperty("theme"));
            properties.put("dossierUtilisateur", installConfig.getProperty("dossierUtilisateur"));
            userConfig.setProperties(properties);
        } else {
            //On charge le thème et la langue de l'utilisateur
            //charge automatiquement le dernier document si on est en mode mono-utilisateur
            Adresse fichierProfil = new Adresse(userConfig.getProperty("profil"));
            if(!fichierProfil.exists()) {
                System.out.println(fichierProfil+" not found");
            } else {
                setProfil(userConfig.getProperty("profil"));
            }
            //setProfil ne charge pas la langue et le theme lors de l'initialisation. On le fait ici
            if(!installConfig.getProperty("langue").equals(getLangue())) {
                fLangue.fusionner(new FichierML(getDossierLangues() + getLangue() + "." + Adresse.EXTENSION_LANGUE));//on modifie les données avec celles du nouveau fichier
            }
            if(!installConfig.getProperty("theme").equals(getTheme())) {
                fSources.fusionner(new FichierML(getDossierThemes()+ getTheme() + "." + Adresse.EXTENSION_THEME));//on modifie les données avec celles du nouveau fichier
            }
        }

        //dossier par défaut si le profil n'est pas chargé
        if(dossierCourant==null) {dossierCourant = installConfig.getProperty("dossierUtilisateur");}
        
        //crée le dossier temporaire pour l'application
        Adresse a = new Adresse(getDossierTemp());
        a.mkdirs();
        a.deleteOnExit();
        
        System.out.println("config initialized");
    }

    private Configuration() {throw new AssertionError("try to instanciate utility class");}
}
