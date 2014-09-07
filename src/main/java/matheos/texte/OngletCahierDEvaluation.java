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

import matheos.Configuration;
import matheos.IHM;
import matheos.texte.composants.JLabelText;
import matheos.utils.objets.Blinking;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.PermissionManager;
import matheos.utils.managers.Traducteur;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.dialogue.DialogueBloquant;
import matheos.utils.dialogue.DialogueComplet;
import matheos.utils.dialogue.DialogueEvent;
import matheos.utils.dialogue.DialogueListener;
import matheos.utils.texte.EditeurKit;
import matheos.utils.texte.JMathTextPane;
import java.awt.Color;

import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Action;
import javax.swing.text.html.HTMLEditorKit;


/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class OngletCahierDEvaluation extends OngletTexte {

    private Map<String, Boolean> authorizations = new HashMap<String, Boolean>();
    
    public OngletCahierDEvaluation() {
        creation = getBarreOutils().addBoutonOnRight(new ActionNouvelleEvaluation());
        blinking = new Blinking(creation);
    }

    @Override
    protected String[] getTitres() {
        String[] titres = cahier.getTitres();
        for(int i = 0; i<titres.length; i++) {
            titres[i] = Traducteur.traduire("test")+" "+(i+1)+" : "+titres[i];
        }
        return titres;
    } 
    
    public String creerEnTete(String sujet, int numero) {
        String cBord = ColorManager.getRGBHexa("color border test");
        String cTitre = ColorManager.getRGBHexa("color text title");
        String cSujet = ColorManager.getRGBHexa("color text subtitle");
        String nom = Configuration.getNomUtilisateur();
        String classe = Configuration.getClasse();
//        int numero = cahier.getIndexCourant();
//        String sujet = cahier.getTitres()[numero];
        String controle = Traducteur.traduire("test title");//+" "+numero;
        String note = Traducteur.traduire("test mark");
        String observations = Traducteur.traduire("test remark");
        String signature = Traducteur.traduire("test signature");
        String date = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault()).format(new Date());
        int fontSize = EditeurKit.TAILLES_PT[0];
        int fontSizeTitle = EditeurKit.TAILLES_PT[2];
        int fontSizeMedium = EditeurKit.TAILLES_PT[1];
        long id = System.currentTimeMillis();
        String enTete = ""+
"		<div class=WordSection1>"+

"			<div id='header' style='font-size:"+fontSize+"pt;'>"+
"                            <div class=headerGauche style='padding-left:20px'>"+
                                "<p><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='9s'><span id='9' editable='false' removable='false' style='color:#000000;font-size:"+fontSize+";'>Nom, Prénom : "+nom+"</span></span></p>"+
                                "<p><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='10s'><span id='10' editable='false' removable='false' style='color:#000000;font-size:"+fontSize+";'>Classe : "+classe+"</span></span></p>"+
                            "</div>"+
"                            <div style='text-align:right;"/*position:absolute;top:20px;right:50px;*/+"'>"+
                                "<span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='11s'><span id='11' editable='false' removable='false' style='color:#000000;font-size:"+fontSize+";'>Date début : "+date+"</span></span>"+
                            "</div>"+
"                            <div id='fin' style='text-align:right;'>&nbsp;</div>"+
"                        </div>"+

"                        <div id='titre' style='text-align:center;'>"+
//"                            <div style='font-size:"+fontSizeTitle+"pt'>"+controle+"</div>"+
"                            <p style='text-align:center;color:"+cTitre+/*";font-size:"+fontSizeTitle+*/";'><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='"+id+"'><span id='"+id+"' editable='true' removable='false' style='font-decoration:underline;color:"+cTitre+";font-size:"+fontSizeTitle+";font-weight:bold;'>"+controle+"</span></span></p>"+
//"                            <div style='font-size:"+fontSizeMedium+"pt'>"+sujet+"</div>"+
"                            <p style='text-align:center;color:"+cSujet+/*";font-size:"+fontSizeMedium+*/";'><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='1s'><span id='1' editable='true' removable='false' style='font-decoration:underline;color:"+cSujet+";font-size:"+fontSizeMedium+";'>"+sujet+"</span></span></p>"+
"                        </div>"+

"                        <div id='rules' style='text-align:left;'>&nbsp</div>"+
                
"                        <div id='table' style='padding-top:20px;'>"+
"                            <table id='cadre' style='border-collapse:collapse;text-align:center;color:#000000;' cellspacing='0' cellpadding='1' align='center' width='90%' height='150px'>"+
"                                <tr style='text-align:center;vertical-align:top;height:20px;font-size:"+fontSize+"pt;' valign='top'>"+
"                                <td style='border:1px solid "+cBord+";vertical-align:text-top;width:15%;height:150px;'>"+
                                    note+" :"+
"                                </td>"+
"                                <td style='border:1px solid "+cBord+";height:150px;'>"+
                                    observations+" :"+
"                                </td>"+
"                                <td style='border:1px solid "+cBord+";width:20%;height:150px;' width=20% height='150'>"+
                                    signature+" :"+
"                                </td></tr>"+
"                             </table>"+
"                        </div>"+
"               </div>"
                +"<p style='text-align:left;color:#000000;font-size:"+fontSize+"pt'>&nbsp;</p>";
        return enTete;
//        editeur.resetDocument();
//        EditeurIO.read(editeur, new DataTexte(enTete));
//        editeur.setCaretPosition(editeur.getLength()-1);
//        editeur.getEditeurKit().reset();
//        editeur.requestFocus();
    }

    public boolean nouvelleEvaluation() {
        String titre = DialogueBloquant.input("dialog new test");

        if (titre==null) { return false; }
        if (titre.isEmpty()) { return nouvelleEvaluation(); }
        int index = cahier.getTitres().length;

        cahier.addChapitre(titre, genererNouveauContenu(titre, index+1));
        setElementCourant(index);
        setModified(true);
        
        startEvaluation();

        //règle les problèmes d'interface en cas de première évaluation
        creation.setBorder(null);
        if(ID==0) {activeContenu(true);}
        
        return true;
    }
    
    public class ActionNouvelleEvaluation extends ActionComplete {
        public ActionNouvelleEvaluation() {
            super("new test");
        }
        public void actionPerformed(ActionEvent e) {
            if (saveChanges()) {
                nouvelleEvaluation();
            }
        }

    }

    public void startEvaluation() {
        final DialogueComplet dialogue = new DialogueComplet("dialog test authorizations");
        dialogue.addDialogueListener(new DialogueListener() {
            @Override
            public void dialoguePerformed(DialogueEvent event) {
                authorizations.put("calculator", event.getInputBoolean("calculator"));
                authorizations.put("classtest", event.getInputBoolean("classtest"));
                authorizations.put("hometest", event.getInputBoolean("hometest"));
                Map<String, String> langue = dialogue.getInfoLangue();
                for(Entry<String, Boolean> entry : authorizations.entrySet()) {
                    if(entry.getValue()) {
                        JLabelText label = new JLabelText(langue.get(entry.getKey()), 12, Color.GRAY, false, true);
                        label.setEditable(false);label.setRemovable(false);
                        editeur.setCaretPosition(editeur.getHTMLdoc().getElement("rules").getStartOffset());
                        editeur.insererLabel(label);
                        new HTMLEditorKit.InsertBreakAction().actionPerformed(new ActionEvent(editeur, ID, "rules"));
                    }
                    activeActions(entry.getKey(), entry.getValue());
                }
            }
        });
    }

    private void activeActions(String key, boolean active) {
        switch(key) {
            case "calculator" : IHM.activeAction(IHM.ACTION.CALCULATRICE, active); break;
            case "hometest" : IHM.activeAction(IHM.ACTION.CONSULTATION, active); break;
            case "classtest" : if(active) {barreOutils.addBoutonOnRight(actionFinEvaluation = new ActionFinEvaluation());} break;
        }
    }

    private Action actionFinEvaluation = null;

    /** remet les action dans leur état précédent **/
    public void endEvaluation() {
        IHM.activeAction(IHM.ACTION.CALCULATRICE, PermissionManager.isCalculatriceAllowed());
        IHM.activeAction(IHM.ACTION.CONSULTATION, PermissionManager.isConsultationAllowed());
        String date = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault()).format(new Date());
        JLabelText dateFin = new JLabelText("Date fin : "+date, 14, Color.BLACK, false, false);
        dateFin.setEditable(false);
        dateFin.setRemovable(false);
        editeur.setCaretPosition(editeur.getHTMLdoc().getElement("fin").getStartOffset());
        editeur.insererLabel(dateFin);
        if(actionFinEvaluation!=null) {
            barreOutils.removeBouton(actionFinEvaluation);
            barreOutils.revalidate();
            barreOutils.repaint();
            actionFinEvaluation=null;
        }
    }

    private final class ActionFinEvaluation extends ActionComplete {
        private ActionFinEvaluation() {super("test finished");}
        @Override
        public void actionPerformed(ActionEvent e) {
            endEvaluation();
        }
    }

}
