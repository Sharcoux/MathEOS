/*
 * «Copyright 2011-2013 Francois Billioud»
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
 * along with MathEOS.  If not, see <http://www.gnu.org/licenses/>.
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

/**
 * Ce package contient toute l'interface graphique du logiciel
 */
package matheos;

import matheos.utils.dialogue.DialogueBloquant;
import matheos.utils.fichiers.Adresse;
import matheos.utils.managers.Traducteur;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataFile;
 

/**
 * Programme principal. Les variables globales y sont stockées sous forme de variables statiques. <br/>
 * On y charge le fichier de langue et les sources des icônes.<br/>
 * On définit aussi la variable "mode" qui indique si l'on est en mode "traitement de texte" ou "TP"<br/>
 * On définit en plus "taille" qui indique la position du séparateur (1/4, 1/2 ou 3/4)<br/>
 * Enfin, on définit la variable i1 destinée à contenir toute l'interface (JFrame) du programme<br/>
 */
public class Main {

    /**
     * Méthode principale du programme.<br/>
     * Elle change le Look And Feel et crée l'interface du programme.
     * @param args : parametres du pogramme (vide)
     */
    public static void main(String[] args) {
        //charge la configuration du fichier config
//        Configuration.initialize();
//        System.out.println("config initialized");
    	//récupère le fichier .bmc s'il existe
        String importFile = null;
        if(args.length>0) {
            String fichier = args[0];
            if(Adresse.isMathEOSFileName(fichier)) {
                Configuration.setProfil(fichier);
            } else if(fichier.endsWith(Adresse.EXTENSION_MathEOS_EXPORT_FILE)) {
                importFile = fichier;
            } else {
                erreurCritique(null, fichier+" n'est pas un fichier matheos", 1);
            }
        }
        
        final String fichier = importFile;
//        Loading.start();
        System.out.println("loading started");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                IHM.lancer();
                
                //Ouverture de l'éventuel fichier à importer
                if(fichier!=null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                        Adresse file = new Adresse(fichier);
                            if(!file.exists()) {return;}
                            Object content = file.chargement();
                            if(!(content instanceof Data)) {
                                DialogueBloquant.error(Traducteur.traduire("error"), String.format(Traducteur.traduire("error invalid file"),file.getAbsolutePath()));
                                return;
                            }
                            DataFile fileContent = (DataFile) content;
                            IHM.askForImport(fileContent);
                        }
                    });
                }
            }
        });
        System.out.println("IHM started");
    }

    /**
     * Affiche un message d'erreur et interromp l'execution du logiciel
     * @param message motif de l'erreur
     * @param code code à faire apparaître en fin d'execution
     */
    public static void erreurCritique(Exception ex, String message, int code) {
        if(ex!=null) {Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);}
        DialogueBloquant.error(Traducteur.traduire("error"), "Fichiers corrompus!\nRéinstaller le logiciel.\n"+message);
        System.exit(code);
    }

    public static void erreurDetectee(Exception ex, String id) {
        if(ex!=null) {Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);}
        DialogueBloquant.error(Traducteur.traduire("error"), "le chargement du composant : "+id+" a échoué\nMerci d'envoyez votre fichier à f.billioud@gmail.com pour debuggage.\nN'enregistrez surtout pas avant cet envoi.\nUne fois le fichier envoyé, vous pouvez poursuivre normalement votre travail.");
    }

}
