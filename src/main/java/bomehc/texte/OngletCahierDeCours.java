/** «Copyright 2012,2013 François Billioud»
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

package bomehc.texte;

import bomehc.IHM;
import bomehc.texte.composants.JLabelText;
import bomehc.utils.managers.Traducteur;
import bomehc.utils.boutons.ActionComplete;
import bomehc.utils.dialogue.DialogueBloquant;
import bomehc.utils.managers.ColorManager;
import bomehc.utils.texte.EditeurKit;
import bomehc.utils.texte.JMathTextPane;

import java.awt.event.ActionEvent;
import bomehc.sauvegarde.DataFile;
import bomehc.utils.boutons.Bouton;
import bomehc.utils.managers.ImageManager;
import bomehc.utils.managers.PermissionManager;


/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class OngletCahierDeCours extends OngletTexte {

    private Bouton exporterCours;
    private Bouton importerCours;
    
    public OngletCahierDeCours() {
        creation = getBarreOutils().addBoutonOnRight(new ActionNouveauChapitre());
    }

    @Override
    protected String[] getTitres() {
        String[] titres = cahier.getTitres();
        for(int i = 0; i<titres.length; i++) {
            titres[i] = Traducteur.traduire("chapter")+" "+(i+1)+" : "+titres[i];
        }
        return titres;
    }

    public boolean nouveauChapitre() {
        String titre = DialogueBloquant.input("dialog new chapter", "", ImageManager.getIcone("new chapter"));

        if (titre==null) { return false; }
        if (titre.isEmpty()) { return nouveauChapitre(); }

        IHM.nouveauChapitre(titre);
        editeur.requestFocusInWindow();//Hack car sinon l'éditeur ne reprend pas le focus. Aucune explication
        return true;
    }
    
    @Override
    public void importer(DataFile file, boolean newChapter) {
        if(newChapter) {//On doit lier le cahier de cours et d'exercice pour la création de chapitres
            IHM.nouveauChapitre(file.getTitre());
            cahier.setContenu(file.getContenu());
            chargerEditeur(file.getContenu());
        } else {
            super.importer(file, newChapter);
        }
    }

    @Override
    public String creerEnTete(String sujet, int index) {
        String titre = Traducteur.traduire("chapter")+" "+index+" : "+sujet;
        String couleurChapitre = ColorManager.getRGBHexa("color lesson chapter");
        int fontSizeChapitre = EditeurKit.TAILLES_PT[2];
        int fontSize = EditeurKit.TAILLES_PT[0];
//        String enTete = "<p id='title' style='text-align:center;color:"+couleurChapitre+";font-size:"+fontSizeChapitre+"pt;'><b><u>"+titre+"</u></b></p>"
//                +"<p style='text-align:left;color:#000000;font-size:"+fontSize+"pt;'>&nbsp;</p>";
        String enTete = "<p style='text-align:center;color:#000000"+/*";font-size:"+fontSizeChapitre+"pt;"+*/"'><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='1s'><span id='1' editable='true' removable='false' style='text-decoration:underline;color:"+couleurChapitre+";font-size:"+fontSizeChapitre+";font-weight:bold;'>"+titre+"</span></span></p>"
                +"<p style='text-align:left;color:#000000"+/*+";font-size:"+fontSize+"pt"+*/";'>&nbsp;</p>";
        return enTete;
    }


    public class ActionNouveauChapitre extends ActionComplete {
        public ActionNouveauChapitre() {
            super("new chapter");
        }
        public void actionPerformed(ActionEvent e) {
//            if(saveChanges()) { nouveauChapitre(); }
            nouveauChapitre();//les saveChange sont mutualisés par l'IHM car le changement concerne le cours ET les exercices
        }
    }
    
    @Override
    protected void setCahierViergeState(boolean b) {
        if(isNouveauCahier()==b) {return;}
        super.setCahierViergeState(b);
//        if(importerCours!=null) importerCours.setEnabled(!b);
        if(exporterCours!=null) exporterCours.setEnabled(!b);
    }
    
    private boolean outilsProfEnabled = false;
    private boolean outilsInitialises = false;

    @Override
    public void setActionEnabled(PermissionManager.ACTION actionID, boolean activer) {
        super.setActionEnabled(actionID, activer);
        if(actionID==PermissionManager.ACTION.OUTILS_PROF) {
            if(activer==outilsProfEnabled && outilsInitialises) {return;}
            outilsProfEnabled = activer;
            
            //création
            if(activer) {
                exporterCours = getBarreOutils().addBoutonOnRight(new ActionExporterCours());
            } else {
                importerCours = getBarreOutils().addBoutonOnRight(new ActionImporterCours());
            }
            if(!outilsInitialises) {outilsInitialises = true;} else {
                //nettoyage
                if(activer) {
                    getBarreOutils().removeComponent(importerCours);
                    importerCours = null;
                } else {
                    getBarreOutils().removeComponent(exporterCours);
                    exporterCours = null;
                }
            }
            getBarreOutils().revalidate();
        }
    }
    
    private class ActionExporterCours extends ActionComplete {
        private ActionExporterCours() {super("lesson export");}
        @Override
        public void actionPerformed(ActionEvent e) {IHM.choixFichierExport(exporter());}
    }
    private class ActionImporterCours extends ActionComplete {
        private ActionImporterCours() {super("lesson import");}
        @Override
        public void actionPerformed(ActionEvent e) {
            DataFile f = IHM.choixFichierImport();
            if(f==null) {return;}
            boolean titleMatching = f.getTitre().equals(getCahier().getTitreCourant());
            if(!titleMatching) {//Cas de la création d'un nouveau chapitre à partir du fichier
                DialogueBloquant.CHOICE choix = DialogueBloquant.dialogueBloquant("chapter import warning", DialogueBloquant.MESSAGE_TYPE.WARNING, DialogueBloquant.OPTION.OK_CANCEL, ImageManager.getIcone("lesson import"), f.getTitre());
                if(choix!=DialogueBloquant.CHOICE.OK) {return;}
            }
            importer(f, !titleMatching);
        }
    }
}

