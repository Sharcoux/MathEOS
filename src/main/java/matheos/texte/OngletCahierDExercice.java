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
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataTexte;
import matheos.texte.composants.JLabelText;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.Traducteur;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.dialogue.DialogueBloquant;
import matheos.utils.texte.EditeurKit;
import matheos.utils.texte.JMathTextPane;
import java.awt.Color;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import matheos.sauvegarde.DataFile;
import matheos.utils.boutons.Bouton;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class OngletCahierDExercice extends OngletTexte {
    private final Bouton nouvelExercice;
    
    //XXX penser à supprimer ce système si les exercices deviennent indépendants
    private static final String NOMBRE_D_EXERCICES = "nombre d'exos";
    private int nombreDExos = 1;
    public OngletCahierDExercice() {
        creation = getBarreOutils().addBoutonOnRight(new ActionNouveauChapitre());
        nouvelExercice = getBarreOutils().addBoutonOnRight(new ActionNouvelExercice());
    }

    @Override
    protected String[] getTitres() {
        String[] titres = cahier.getTitres();
        for(int i = 0; i<titres.length; i++) {
            titres[i] = Traducteur.traduire("chapter")+" "+(i+1)+" : "+titres[i];
        }
        return titres;
    }
    
    @Override
    protected void chargerEditeur(Data data) {
        super.chargerEditeur(data);
        String value = data.getElement(NOMBRE_D_EXERCICES);
        nombreDExos = (value!=null ? Integer.parseInt(value) : 1);
    }
    
    @Override
    public DataTexte getDonneesEditeur() {
        DataTexte donnees = super.getDonneesEditeur();
        donnees.putElement(NOMBRE_D_EXERCICES, nombreDExos+"");
        return donnees;
    }

    public boolean nouveauChapitre() {
        String titre = DialogueBloquant.input("dialog new chapter");

        if (titre==null) { return false; }
        if (titre.isEmpty()) { return nouveauChapitre(); }

        IHM.nouveauChapitre(titre);
        nombreDExos = 1;
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
    protected void setNouveauCahier(boolean b) {
        if(isNouveauCahier()==b) {return;}
        super.setNouveauCahier(b);
        nouvelExercice.setEnabled(!b);
    }
    
    /**
     * Méthode permettant d'afficher un exercice en titre.
     */
    private void addExercice() {
        nombreDExos++;
        try {
//            String couleurExercice = ColorManager.getRGBHexa("color exercise chapter");
            long id = System.currentTimeMillis();
            int fontSizeExo = EditeurKit.TAILLES_PT[1];
            Color couleurExercice = ColorManager.get("color exercise chapter");
            new HTMLEditorKit().insertHTML(editeur.getHTMLdoc(), editeur.getLength(), "<HR />", 1, 0, Tag.HR);
            editeur.insererLabel(new JLabelText(Traducteur.traduire("exercise") + " " + nombreDExos, EditeurKit.TAILLES_PT[1], couleurExercice, true, true));
//            new HTMLEditorKit().insertHTML(editeur.getHTMLdoc(), editeur.getLength(), "<p style='text-align:left;font-size:1.5em;color:"+couleurExercice+"'><b><u>"+Traducteur.traduire("exercise") + " " + nombreDExos +"</u></b></p>", 0, 0, Tag.P);
//            new HTMLEditorKit().insertHTML(editeur.getHTMLdoc(), editeur.getLength(), 
//                "<p style='text-align:left;color:"+couleurExercice+";font-size:"+fontSizeExo+";'><span class='"+JMathTextPane.MathEOS_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='"+id+"s'><span id='"+id+"' style='text-decoration:underline;color:"+couleurExercice+";font-size:"+fontSizeExo+";font-weight:bold;'>"+Traducteur.traduire("exercise") +" "+nombreDExos+"</span></span></p>"
//                    , 0, 0, Tag.P);
//            editeur.insererHTML("&nbsp;", editeur.getLength(), Tag.CONTENT);
            editeur.getHTMLdoc().insertString(editeur.getLength(), "\n", editeur.getCharacterAttributes());
//            insererHTML("&nbsp;", editeur.getLength(), Tag.CONTENT);
        } catch (IOException | BadLocationException ex) {
            Logger.getLogger(Editeur.class.getName()).log(Level.SEVERE, null, ex);
        }
        editeur.setCaretPosition(editeur.getLength());
        editeur.getEditeurKit().reset();
        editeur.requestFocus();
    }

    @Override
    public String creerEnTete(String sujet, int index) {
        String titre = Traducteur.traduire("exercises of chapter")+" "+index+" : "+sujet;
        String couleurChapitre = ColorManager.getRGBHexa("color lesson chapter");
        String couleurExercice = ColorManager.getRGBHexa("color exercise chapter");
        int fontSizeChapitre = EditeurKit.TAILLES_PT[2];
        int fontSizeExo = EditeurKit.TAILLES_PT[1];
        int fontSize = EditeurKit.TAILLES_PT[0];
        long id = System.currentTimeMillis();
//        String enTete = "<p id='title' style='text-align:center;color:"+couleurChapitre+";font-size:"+fontSizeChapitre+"pt;'><b><u>"+titre+"</u></b></p>"
//                +"<p style='text-align:left;font-size:"+fontSizeExo+"pt;color:"+couleurExercice+"'><b><u>"+Traducteur.traduire("exercise") + " 1</u></b></p>"
//                +"<p style='text-align:left;color:#000000;font-size:"+fontSize+"pt;'>&nbsp;</p>";
        String enTete = "<p style='text-align:center;color:#000000"+/*";font-size:"+fontSizeChapitre+"pt"+*/";'><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='1s'><span id='1' editable='true' removable='false' style='text-decoration:underline;color:"+couleurChapitre+";font-size:"+fontSizeChapitre+";font-weight:bold;'>"+titre+"</span></span></p>"
                +"<p style='text-align:left;color:#000000"+/*";font-size:"+fontSizeExo+*/";'><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='"+id+"s'><span id='"+id+"' editable='true' removable='false' style='text-decoration:underline;color:"+couleurExercice+";font-size:"+fontSizeExo+";font-weight:bold;'>"+Traducteur.traduire("exercise") +" "+nombreDExos+"</span></span></p>"
                +"<p style='text-align:left;color:#000000"+/*";font-size:"+fontSize+"pt"+*/";'>&nbsp;</p>";
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

    public class ActionNouvelExercice extends ActionComplete {
        public ActionNouvelExercice() {
            super("new exercise");
        }
        public void actionPerformed(ActionEvent e) {
            if(cahier.getIndexCourant()==-1) {
                DialogueBloquant.warning(Traducteur.traduire("warning"), Traducteur.traduire("no chapter"));
                return;
            }
            addExercice();
        }
    }
}

