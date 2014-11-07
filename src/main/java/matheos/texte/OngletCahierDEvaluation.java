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
import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataCahier;
import matheos.sauvegarde.DataFile;
import matheos.sauvegarde.DataTexte;
import matheos.utils.boutons.Bouton;
import matheos.utils.fichiers.Adresse;
import matheos.utils.objets.DataTexteDisplayer;


/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class OngletCahierDEvaluation extends OngletTexte {

    public static final String CORRIGE = "corrigé";
    public static final String COMMENTAIRES = "comments";
    public static final String FICHIERS_ELEVES = "studentFiles";
    
    private static final int MODE_NORMAL = 0;
    private static final int MODE_EDITION_CORRIGE = 1;
    private static final int MODE_CORRECTION_ELEVE = 2;
    private int mode = MODE_NORMAL;
    
    private final Map<String, Boolean> authorizations = new HashMap<>();
    
    private final Bouton special;
    private Bouton commentaires;
    private Bouton supprimerCorrige;
    private Bouton exporterCorrige;
    private Component separateur;
    
    public OngletCahierDEvaluation() {
        creation = getBarreOutils().addBoutonOnRight(new ActionNouvelleEvaluation());
        blinking = new Blinking(creation);
        getBarreOutils().addBoutonOnRight(special = new Bouton(Bouton.BOUTON));
        getBarreOutils().addSeparateurOnRight();
        getBarreOutils().addBoutonOnRight(new ActionExporterDevoir());
    }

    @Override
    protected String[] getTitres() {
        String[] titres = cahier.getTitres();
        for(int i = 0; i<titres.length; i++) {
            titres[i] = Traducteur.traduire("test")+" "+(i+1)+" : "+titres[i];
        }
        return titres;
    } 
    
    @Override
    protected void chargerEditeur(Data dataTexte) {
        //On remet les actions dans leur état par défaut
        setMode(MODE_NORMAL);
        
        //Met en place les bons boutons selon l'utilisateur et la présence ou non d'un corrigé
        boolean isCorrigePresent = isCorrigePresent();
        if(Configuration.isTeacher()) {
            exporterCorrige.setVisible(isCorrigePresent);
            supprimerCorrige.setVisible(isCorrigePresent);
            special.setAction(isCorrigePresent ? new ActionEditerCorrige() : new ActionCreerCorrige());
        } else {
            special.setAction(isCorrigePresent ? new ActionConsulterCorrige() : new ActionAttacherCorrige());
        }
        
        super.chargerEditeur(dataTexte);
        
        //Si on a chargé un fichier qui ne contient pas de bandeau élève, il s'agit probablement d'une évaluation donnée par l'enseignant.
        if(editeur.getHTMLdoc().getElement("header")==null && !Configuration.isTeacher()) {
            startEvaluation();
        }
        
    }
    
    @Override
    public DataFile exporter() {
        DataFile file;
        switch(mode) {
            case MODE_EDITION_CORRIGE :
                file = super.exporter();
                file.putElement(CORRIGE, "true");//On indique que le fichier est un corrigé
                break;
            case MODE_CORRECTION_ELEVE :
                getCorrectingDocument().setContenu(getDonneesEditeur());
                file = getCorrectingDocument();
                break;
            default : file = super.exporter();
        }
        return file;
    }
    @Override
    public void importer(DataFile file, boolean newChapter) {
        if(Configuration.isTeacher() && !Traducteur.traduire("teacher").equals(Traducteur.getListeClasses()[file.getNiveau()])) {
            DialogueBloquant.CHOICE choix = DialogueBloquant.dialogueBloquant("dialog suspicious import", DialogueBloquant.MESSAGE_TYPE.WARNING, DialogueBloquant.OPTION.OK_CANCEL);
            if(choix!=DialogueBloquant.CHOICE.OK) {return;}
        }
        if("true".equals(file.getElement(CORRIGE))) {
            if(cahier.getContenuChapitre(file.getIndice())==null) {
                DialogueBloquant.error("dialog solution load error");
                return;
            }
            if(!saveChanges()) {return;}
            cahier.getDataChapitre(file.getIndice()).putData(CORRIGE, file.getContenu());
            cahier.setIndexCourant(file.getIndice());
            chargerEditeur(cahier.getContenuCourant());
            setModified(true);
        } else {
            super.importer(file, newChapter);
        }
    }
    
    @Override//Si on ne fait pas ça, chaque sauvegarde va remplacer l'évaluation par son corriger lorsqu'on est en mode édition
    public DataCahier getDonnees() {
        DataCahier data;
        switch(mode) {
            case MODE_EDITION_CORRIGE :
                setCorrige(getDonneesEditeur());
                editeur.setModified(false);
                data = getCahier();
                break;
            case MODE_CORRECTION_ELEVE :
                getCorrectingFile().sauvegarde(getCorrectingDocument());
                editeur.setModified(false);
                data = getCahier();
                break;
            default : data = super.getDonnees();
        }
        return data;
    }
    
    private Adresse correctingFile = null;
    private DataFile correctingDoc = null;
    private Adresse getCorrectingFile() {return correctingFile;}
    private DataFile getCorrectingDocument() {return correctingDoc;}
    private Adresse getDossierCopiesEleves() {return new Adresse(getCahier().getDataChapitre(getId()).getElement(FICHIERS_ELEVES));}
    private void setCorrectingFile(Adresse a) {
        correctingFile = a;
        correctingDoc = (DataFile) a.chargement();
        if(mode==MODE_CORRECTION_ELEVE) {
            editeur.charger(correctingDoc.getContenu());
        } else {
            setMode(MODE_CORRECTION_ELEVE);
        }
    }
    
    private int getMode() {return mode;}
    
    private void setMode(int mode) {
        if(this.mode==mode) {return;}
        this.mode = mode;
        switch(mode) {
            case MODE_NORMAL :
                if(Configuration.isTeacher()) {
                    special.setAction(isCorrigePresent() ? new ActionEditerCorrige() : new ActionCreerCorrige());
                }
                editeur.charger(getCahier().getContenuCourant());
                setModeCorrectionEnabled(false);
                break;
            case MODE_EDITION_CORRIGE :
                special.setAction(new ActionRetourRedaction());
                editeur.charger(getCorrige());
                setModeCorrectionEnabled(true);
                break;
            case MODE_CORRECTION_ELEVE :
                special.setAction(new ActionSaveAndNext());
                editeur.charger(getCorrectingDocument().getContenu());
                setModeCorrectionEnabled(true);
                break;
        }
    }
    
    @Override
    public void setActionEnabled(PermissionManager.ACTION actionID, boolean activer) {
        if(actionID==PermissionManager.ACTION.OUTILS_PROF) {
            if(activer) {
                //Outils profs
                exporterCorrige = getBarreOutils().addBoutonOnRight(new ActionExporterCorrige());
                supprimerCorrige = getBarreOutils().addBoutonOnRight(new ActionSupprimerCorrige());
                separateur = getBarreOutils().addSeparateurOnRight();
                commentaires = getBarreOutils().addBoutonOnRight(new ActionCommentaires());
            } else {
                //On détruit les boutons inutiles
                if(exporterCorrige!=null) {
                    getBarreOutils().removeComponent(exporterCorrige);
                    exporterCorrige = null;
                }
                if(supprimerCorrige!=null) {
                    getBarreOutils().removeComponent(supprimerCorrige);
                    supprimerCorrige = null;
                }
                if(separateur!=null) {
                    getBarreOutils().removeComponent(separateur);
                    separateur = null;
                }
                if(commentaires!=null) {
                    getBarreOutils().removeComponent(commentaires);
                    commentaires = null;
                }
            }
        }
    }
    
    private boolean isCorrigePresent() {
        return getCahier().getDataChapitre(getId()).containsDataKey(CORRIGE);
    }
    private void setCorrige(DataTexte data) {
        cahier.getDataChapitre(getId()).putData(CORRIGE, data);
    }
    /**
     * Renvoie le corrigé ou un nouveau DataTexte si non trouvé.
     * @return Le corrigé ou un nouveau DataTexte. Jamais null
     */
    private DataTexte getCorrige() {
        DataTexte corrige;
        Data d = getCahier().getDataChapitre(getId()).getData(CORRIGE);
        if(d!=null && d instanceof DataTexte) {corrige = (DataTexte) d;} else {corrige=new DataTexte("");corrige.putAll(d);}
        return corrige;
    }
    
    private boolean isCommentairesPresent() {
        return getCahier().getDataChapitre(getId()).containsDataKey(COMMENTAIRES);
    }
    private void setCommentaires(DataTexte data) {
        cahier.getDataChapitre(getId()).putData(COMMENTAIRES, data);
    }
    /**
     * Renvoie les commentaires ou un nouveau DataTexte si non trouvé.
     * @return Les commentaires ou un nouveau DataTexte. Jamais null.
     */
    private DataTexte getCommentaires() {
        DataTexte corrige;
        Data d = getCahier().getDataChapitre(getId()).getData(COMMENTAIRES);
        if(d!=null && d instanceof DataTexte) {corrige = (DataTexte) d;} else {corrige=new DataTexte("");corrige.putAll(d);}
        return corrige;
    }
    
    private String studentHeader() {
        String nom = Configuration.getNomUtilisateur();
        String classe = Configuration.getClasse();
        int fontSize = EditeurKit.TAILLES_PT[0];
        String date = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault()).format(new Date());
        return "	<div id='header' style='font-size:"+fontSize+"pt;'>"+
"                            <div class=headerGauche style='padding-left:20px'>"+
                                "<p><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='9s'><span id='9' editable='false' removable='false' style='color:#000000;font-size:"+fontSize+";'>Nom, Prénom : "+nom+"</span></span></p>"+
                                "<p><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='10s'><span id='10' editable='false' removable='false' style='color:#000000;font-size:"+fontSize+";'>Classe : "+classe+"</span></span></p>"+
                            "</div>"+
"                            <div style='text-align:right;"/*position:absolute;top:20px;right:50px;*/+"'>"+
                                "<span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='11s'><span id='11' editable='false' removable='false' style='color:#000000;font-size:"+fontSize+";'>Date début : "+date+"</span></span>"+
                            "</div>"+
"                            <div id='fin' style='text-align:right;'>&nbsp;</div>"+
"                        </div>";
    }

    public String creerEnTete(String sujet, int numero) {
        String cBord = ColorManager.getRGBHexa("color border test");
        String cTitre = ColorManager.getRGBHexa("color text title");
        String cSujet = ColorManager.getRGBHexa("color text subtitle");
//        int numero = cahier.getIndexCourant();
//        String sujet = cahier.getTitres()[numero];
        String controle = Traducteur.traduire("test title");//+" "+numero;
        String note = Traducteur.traduire("test mark");
        String observations = Traducteur.traduire("test remark");
//        String signature = Traducteur.traduire("test signature");
        int fontSize = EditeurKit.TAILLES_PT[0];
        int fontSizeTitle = EditeurKit.TAILLES_PT[2];
        int fontSizeMedium = EditeurKit.TAILLES_PT[1];
        long id = System.currentTimeMillis();
        String enTete = ""+
"		<div id='layout'>";

                if(!Configuration.isTeacher()) {enTete+=studentHeader();}
                
                enTete+=
//"                        <div id='titre' style='text-align:center;'>"+
//"                            <div style='font-size:"+fontSizeTitle+"pt'>"+controle+"</div>"+
"                            <p id='titre1' style='text-align:center;color:"+cTitre+/*";font-size:"+fontSizeTitle+*/";'><font color='"+cTitre+"'><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='"+id+"'><span id='"+id+"' editable='true' removable='false' style='text-decoration:underline;color:"+cTitre+";font-size:"+fontSizeTitle+";font-weight:bold;'>"+controle+"</span></span></font></p>"+
//"                            <div style='font-size:"+fontSizeMedium+"pt'>"+sujet+"</div>"+
"                            <p id='titre2' style='text-align:center;color:"+cSujet+/*";font-size:"+fontSizeMedium+*/";'><font color='"+cSujet+"'><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='1s'><span id='1' editable='true' removable='false' style='text-decoration:underline;color:"+cSujet+";font-size:"+fontSizeMedium+";'>"+sujet+"</span></span></font></p>"+
//"                        </div>"+

"                        <div id='rules' style='text-align:left;'>&nbsp</div>"+
                
"                        <div id='table' style='padding-top:20px;'>"+
"                            <table id='cadre' style='border-collapse:collapse;text-align:center;color:#000000;' cellspacing='0' cellpadding='1' align='center' width='100%' height='150px'>"+
"                                <tr style='text-align:center;vertical-align:top;height:20px;font-size:"+fontSize+"pt;' valign='top'>"+
"                                <td style='border:1px solid "+cBord+";vertical-align:text-top;width:15%;height:150px;'>"+
                                    "<p>"+note+" :"+"</p>"+
                                    "<p align='right'>"+
                                        "<span id=\"3s\" class=\"special-math-component markComponent\"><svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" width=\"50\" height=\"50\" viewBox=\"0 0 100 100\" style=\"stroke:#000000; fill:#000000; font-weight:normal; font-size:40;\" id=\"3\">\n" +
                                            "<line x1=\"0\" x2=\"100\" y1=\"100\" y2=\"0\" style=\"stroke-width:3\" />\n" +
                                            "<text id=\"numerator\" x=\"25\" y=\"40\" style=\"font-size:45;\"></text>\n" +
                                            "<text id=\"denominator\" x=\"45\" y=\"90\" style=\"font-size:35;\">"+Traducteur.traduire("mark max value")+"</text>\n" +
                                        "</svg></span>"+
                                    "</p>"+
"                                </td>"+
"                                <td style='border:1px solid "+cBord+";height:150px;'>"+
                                    observations+" :"+
"                                </td></tr>"+
"                             </table>"+
"                        </div>"+
"               </div>"+
                "<p style='text-align:left;color:#000000;font-size:"+fontSize+"pt'>&nbsp;</p>";
        return enTete;
    }

    public boolean nouvelleEvaluation() {
        String titre = DialogueBloquant.input("dialog new test");

        if (titre==null) { return false; }
        if (titre.isEmpty()) { return nouvelleEvaluation(); }
        int index = cahier.nbChapitres();

        cahier.addChapitre(titre, genererNouveauContenu(titre, index+1));
        setElementCourant(index);
        setModified(true);
        
        if(!Configuration.isTeacher()) {startEvaluation();}

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
//                        try {
//                            editeur.getHTMLdoc().insertString(editeur.getCaretPosition(), "\n", null);
//                        } catch (BadLocationException ex) {
//                            Logger.getLogger(OngletCahierDEvaluation.class.getName()).log(Level.SEVERE, null, ex);
//                        }
                        new HTMLEditorKit.InsertBreakAction().actionPerformed(new ActionEvent(editeur, ActionEvent.ACTION_PERFORMED, "rules"));
                    }
                    activeActions(entry.getKey(), entry.getValue());
                }
                
                //insert le header s'il n'est pas présent
                if(editeur.getHTMLdoc().getElement("header")==null) {
                    try {
                        editeur.getHTMLdoc().insertAfterStart(editeur.getHTMLdoc().getElement("layout"), studentHeader());
                    } catch (BadLocationException | IOException ex) {
                        Logger.getLogger(OngletCahierDEvaluation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    private void activeActions(String key, boolean active) {
        switch(key) {
            case "calculator" : IHM.activeAction(PermissionManager.ACTION.CALCULATRICE, active); break;
            case "hometest" : IHM.activeAction(PermissionManager.ACTION.CONSULTATION, active); break;
            case "classtest" : if(active) {special.setAction(new ActionFinEvaluation());} break;
        }
    }

    /** remet les action dans leur état précédent **/
    public void endEvaluation() {
        IHM.activeAction(PermissionManager.ACTION.CALCULATRICE, PermissionManager.isCalculatriceAllowed());
        IHM.activeAction(PermissionManager.ACTION.CONSULTATION, PermissionManager.isConsultationAllowed());
        String date = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault()).format(new Date());
        JLabelText dateFin = new JLabelText("Date fin : "+date, 14, Color.BLACK, false, false);
        dateFin.setEditable(false);
        dateFin.setRemovable(false);
        editeur.setCaretPosition(editeur.getHTMLdoc().getElement("fin").getStartOffset());
        editeur.insererLabel(dateFin);
        special.setAction(new ActionAttacherCorrige());
    }
    
    private final class ActionFinEvaluation extends ActionComplete {
        private ActionFinEvaluation() {super("test finished");}
        @Override
        public void actionPerformed(ActionEvent e) {
            endEvaluation();
        }
    }
    private final class ActionAttacherCorrige extends ActionComplete {
        private ActionAttacherCorrige() {super("test add solution");}
        @Override
        public void actionPerformed(ActionEvent e) {
            DataFile data = IHM.choixFichierImport();
            if(data!=null) {
                setCorrige(data.getContenu());
                Action consulter = new ActionConsulterCorrige();
                special.setAction(consulter);
                consulter.actionPerformed(e);
            }
        }
    }
        
    private final class ActionConsulterCorrige extends ActionComplete {
        private ActionConsulterCorrige() {super("test check solution");}
        @Override
        public void actionPerformed(ActionEvent e) {
            final DataTexteDisplayer displayer = new DataTexteDisplayer(getCorrige(), Traducteur.traduire("test solution"));
            
            //Permet de changer le corrigé par clic droit
            displayer.addActionClicDroit(displayer.new ActionClicDroit("test change solution") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    displayer.setVisible(false);
                    DataFile corrige = IHM.choixFichierImport();
                    if(corrige==null) {return;}
                    displayer.getEditeur().charger(corrige.getContenu());
                    setCorrige(corrige.getContenu());
                    displayer.setVisible(true);
                }
            });
            
            displayer.setVisible(true);
        }
    }
    
    private class ActionCommentaires extends ActionComplete.Toggle {
        DataTexteDisplayer displayer = null;
        private ActionCommentaires() {super("test comments",false);}
        @Override
        public void actionPerformed(ActionEvent e) {
            if(displayer==null) {
                displayer = new DataTexteDisplayer(getCommentaires(), Traducteur.traduire("comments")+" : "+getTitres()[getId()]);
                displayer.setSize(500, 400);
                displayer.setEditable(true);
                displayer.setVisible(true);
                displayer.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        setCommentaires(displayer.getEditeur().getDonnees());
                        ActionCommentaires.this.setSelected(false);
                        displayer = null;
                    }
                });
                if(getId()==0 && !isCommentairesPresent()) {
                    displayer.setAlwaysOnTop(false);
                    DialogueBloquant.dialogueBloquant("dialog help comments", DialogueBloquant.MESSAGE_TYPE.INFORMATION, DialogueBloquant.OPTION.DEFAULT);
                    displayer.setAlwaysOnTop(true);
                }
            } else {
                setCommentaires(displayer.getEditeur().getDonnees());
                displayer.setVisible(false);
                displayer.dispose();
                displayer = null;
            }
            
        }
    }
    
    private class ActionEditerCorrige extends ActionComplete {
        private ActionEditerCorrige() {super("test edit solution");}
        @Override
        public void actionPerformed(ActionEvent e) {
            if(saveChanges()) {setMode(MODE_EDITION_CORRIGE);}
            DialogueBloquant.dialogueBloquant("dialog help solution", DialogueBloquant.MESSAGE_TYPE.INFORMATION, DialogueBloquant.OPTION.DEFAULT);
        }
    }
    
    private class ActionCreerCorrige extends ActionEditerCorrige {
        @Override
        public void actionPerformed(ActionEvent e) {
            DataTexte data = getDonneesEditeur();//On initialise le corrigé avec les données de l'éditeur
            setCorrige(data);
            supprimerCorrige.setVisible(true);
            exporterCorrige.setVisible(true);
            super.actionPerformed(e);
        }
    }
    
    private class ActionSupprimerCorrige extends ActionComplete {
        private ActionSupprimerCorrige() {super("test remove solution");}
        @Override
        public void actionPerformed(ActionEvent e) {
            //On supprime le corrigé et les boutons associés
            cahier.getDataChapitre(getId()).removeDataByKey(CORRIGE);
            supprimerCorrige.setVisible(false);
            exporterCorrige.setVisible(false);
            special.setAction(new ActionCreerCorrige());
            //On retourne en mode normal si ce n'était pas le cas
            setMode(MODE_NORMAL);
        }
    }

    private class ActionRetourRedaction extends ActionComplete {
        private ActionRetourRedaction() {super("test edit test");}
        @Override
        public void actionPerformed(ActionEvent e) {
            if(saveChanges()) {setMode(MODE_NORMAL);}
        }
    }
    
    private class ActionExporterDevoir extends ActionComplete {
        private ActionExporterDevoir() {super("test export test");}
        @Override
        public void actionPerformed(ActionEvent e) {
            IHM.choixFichierExport(new DataFile(null, OngletCahierDEvaluation.this.getName(), getId(), getCahier().getTitreCourant(), getCahier().getContenuCourant()));
        }
    }
    private class ActionExporterCorrige extends ActionComplete {
        private ActionExporterCorrige() {super("test export solution");}
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!isCorrigePresent() || !saveChanges()) {return;}
            IHM.choixFichierExport(new DataFile(null, OngletCahierDEvaluation.this.getName(), getId(), getCahier().getTitreCourant(), getCorrige()));
        }
    }
    
    private class ActionRepertoiresCopiesEleves extends ActionComplete {
        private ActionRepertoiresCopiesEleves() {super();}
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser(Configuration.getDossierCourant());
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setFileFilter(new Adresse.SingleFileFilter());
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int choix = fc.showOpenDialog(IHM.getMainWindow());
            if(choix==JFileChooser.APPROVE_OPTION) {
                Adresse a = new Adresse(fc.getSelectedFile());
                String[] files = a.listeFichiers(Adresse.EXTENSION_MathEOS_EXPORT_FILE);
                if(files.length==0) {DialogueBloquant.error("dialog empty directory");}
                else {
                    cahier.putElement(FICHIERS_ELEVES, a.getPath());
                }
            }
        }
    }
    
    private class ActionOuvrirCopieEleve extends ActionComplete {
        private Adresse fichierEleve;
        private ActionOuvrirCopieEleve(Adresse a) {super();}
        @Override
        public void actionPerformed(ActionEvent e) {
            setCorrectingFile(fichierEleve);
        }
    }
    
    private class ActionAfficherListeEleves extends ActionComplete {
        private ActionAfficherListeEleves() {super();}
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] fichiers = getDossierCopiesEleves().listeNomFichiers(Adresse.EXTENSION_MathEOS_EXPORT_FILE);
            String title = Traducteur.traduire("dialog student files list title");
            String message = Traducteur.traduire("dialog student files list message");
            String fichier = (String) JOptionPane.showInputDialog(null, title,
                message, JOptionPane.QUESTION_MESSAGE, null,
                fichiers, // Array of choices
                getCorrectingFile().getNom()); // Initial choice
            if(fichier!=null) {
                setCorrectingFile(new Adresse(getDossierCopiesEleves().getAbsolutePath()+Adresse.separator+fichier+"."+Adresse.EXTENSION_MathEOS_EXPORT_FILE));
            }
        }
    }
    
    private class ActionSaveAndNext extends ActionComplete {
        private ActionSaveAndNext() {super();}
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] fichiers = getDossierCopiesEleves().listeFichiers(Adresse.EXTENSION_MathEOS_EXPORT_FILE);
            for(int i=0; i<fichiers.length; i++) {
                String s = fichiers[i];
                if(s.equals(getCorrectingFile().getName())) {
                    setCorrectingFile(new Adresse(getDossierCopiesEleves()+Adresse.separator+fichiers[(i==fichiers.length-1 ? 0 : i+1)]));
                }
            }
        }
    }
}
