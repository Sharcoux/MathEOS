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

package matheos.texte;

import matheos.IHM;
import matheos.texte.composants.JLabelText;
import matheos.utils.objets.Blinking;
import matheos.utils.managers.Traducteur;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.dialogue.DialogueBloquant;
import matheos.utils.managers.ColorManager;
import matheos.utils.texte.EditeurKit;
import matheos.utils.texte.JMathTextPane;

import java.awt.event.ActionEvent;
import matheos.sauvegarde.DataFile;
import matheos.utils.boutons.Bouton;
import matheos.utils.managers.PermissionManager;


/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class OngletCahierDeCours extends OngletTexte {

    private Bouton exporterCours;
    
    public OngletCahierDeCours() {
//        adapter = Adapters.COURS;
        creation = getBarreOutils().addBoutonOnRight(new ActionNouveauChapitre());
        blinking = new Blinking(creation);
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
        String titre = DialogueBloquant.input("dialog new chapter");

        if (titre==null) { return false; }
        if (titre.isEmpty()) { return nouveauChapitre(); }

        IHM.nouveauChapitre(titre);
        return true;
    }
    
    @Override
    public void importer(DataFile file, boolean newChapter) {
        if(newChapter) {//On doit lier le cahier de cours et d'exercice pour la création de chapitres
            IHM.nouveauChapitre(file.getTitre());
            cahier.setContenu(file.getContenu());
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
    public void setActionEnabled(PermissionManager.ACTION actionID, boolean activer) {
        if(actionID==PermissionManager.ACTION.OUTILS_PROF) {
            if(activer) {
                exporterCours = getBarreOutils().addBoutonOnRight(new ActionExporterCours());
            } else {
                if(exporterCours!=null) {
                    getBarreOutils().removeComponent(exporterCours);
                    exporterCours = null;
                }
            }
        }
    }
    
    private class ActionExporterCours extends ActionComplete {
        private ActionExporterCours() {super("lesson export");}
        @Override
        public void actionPerformed(ActionEvent e) {exporter();}
    }
}

