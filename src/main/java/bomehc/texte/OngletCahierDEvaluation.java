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

import bomehc.Configuration;
import bomehc.IHM;
import bomehc.texte.composants.JLabelText;
import bomehc.utils.managers.ColorManager;
import bomehc.utils.managers.PermissionManager;
import bomehc.utils.managers.Traducteur;
import bomehc.utils.boutons.ActionComplete;
import bomehc.utils.dialogue.DialogueBloquant;
import bomehc.utils.dialogue.DialogueComplet;
import bomehc.utils.dialogue.DialogueEvent;
import bomehc.utils.dialogue.DialogueListener;
import bomehc.utils.texte.EditeurKit;
import bomehc.utils.texte.JMathTextPane;
import java.awt.Color;
import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
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
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import bomehc.sauvegarde.Data;
import bomehc.sauvegarde.DataCahier;
import bomehc.sauvegarde.DataFile;
import bomehc.sauvegarde.DataTexte;
import bomehc.texte.composants.ComposantTexte;
import bomehc.texte.composants.JHeader;
import bomehc.utils.boutons.Bouton;
import bomehc.utils.fichiers.Adresse;
import bomehc.utils.managers.ImageManager;
import bomehc.utils.objets.DataTexteDisplayer;
import bomehc.utils.texte.EditeurIO;


/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class OngletCahierDEvaluation extends OngletTexte {

    public static final String CORRIGE = "corrigé";
    public static final String CORRECTED = "corrected";
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
    private final Bouton exporterDevoir;
    private Bouton importerDevoir;
    private Bouton importerCorrections;
    private Bouton choixDossierEleves;
    private Bouton listeEleves;
    private Component separateur;
    private ActionComplete.Toggle ajoutAutoCorrige;
    
    public OngletCahierDEvaluation() {
        creation = getBarreOutils().addBoutonOnRight(new ActionNouvelleEvaluation());
        getBarreOutils().addBoutonOnRight(special = new Bouton(Bouton.BOUTON));
        getBarreOutils().addSeparateurOnRight();
        exporterDevoir = getBarreOutils().addBoutonOnRight(new ActionExporterDevoir());
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
    protected void setCahierViergeState(boolean b) {
        if(isNouveauCahier()==b) {return;}
        super.setCahierViergeState(b);
        special.setVisible(!b);
        exporterDevoir.setVisible(!b);
        
        if(Configuration.isTeacher()) {
            if(commentaires!=null) commentaires.setVisible(!b);
            if(choixDossierEleves!=null) choixDossierEleves.setVisible(!b);
        } else {
            if(importerCorrections!=null) {importerCorrections.setVisible(!b);}
        }
    }
    
    @Override
    protected void chargerEditeur(Data dataTexte) {
        //On remet les actions dans leur état par défaut
        setMode(MODE_NORMAL);
        
        //Met en place les bons boutons selon l'utilisateur et la présence ou non d'un corrigé
        boolean isCorrigePresent = isCorrigePresent();
        boolean isDossierElevesSet = isDossierElevesSet();
        if(Configuration.isTeacher()) {
            exporterCorrige.setVisible(isCorrigePresent);
            supprimerCorrige.setVisible(isCorrigePresent);
            special.setAction(isCorrigePresent ? new ActionEditerCorrige() : new ActionCreerCorrige());
            listeEleves.setVisible(isDossierElevesSet);
        } else {
            importerDevoir.setVisible(true);
            importerCorrections.setVisible(!isCorrected()&&!isNouveauCahier());
            special.setAction(isCorrigePresent ? new ActionConsulterCorrige() : new ActionAttacherCorrige());
        }
        
        super.chargerEditeur(dataTexte);
        
        //Si on a chargé un fichier qui ne contient pas de bandeau élève, il s'agit probablement d'une évaluation donnée par l'enseignant.
        if(!isNouveauCahier() && editeur.getHTMLdoc().getElement("header")==null && !Configuration.isTeacher()) {
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
            DialogueBloquant.CHOICE choix = DialogueBloquant.dialogueBloquant("dialog suspicious import", DialogueBloquant.MESSAGE_TYPE.WARNING, DialogueBloquant.OPTION.OK_CANCEL, ImageManager.getIcone("test import statement"));
            if(choix!=DialogueBloquant.CHOICE.OK) {return;}
        }
        int index = cahier.getIndex(file.getTitre());
        if("true".equals(file.getElement(CORRIGE))) {//Le fichier est en fait un corrigé
            if(index==-1) {
                DialogueBloquant.error("dialog solution load error");
                return;
            }
            if(!saveChanges()) {return;}
            cahier.getDataChapitre(index).putData(CORRIGE, file.getContenu());
            cahier.setIndexCourant(index);
            chargerEditeur(cahier.getContenuCourant());
            setModified(true);
        } else {
            super.importer(file, newChapter);
            //Si le corrigé a été automatiquement attaché au fichier, on le charge
            if(file.containsDataKey(CORRIGE)) {
                DataTexte corrige;
                Data data = file.getData(CORRIGE);
                if(data!=null && data instanceof DataTexte) {corrige = (DataTexte) data;} else {corrige = new DataTexte("");corrige.putAll(data);}
                setCorrige(corrige);
                if(!Configuration.isTeacher()) {
                    special.setAction(new ActionConsulterCorrige());
                }
            }
        }
    }
    
    @Override//Si on ne fait pas ça, chaque sauvegarde va remplacer l'évaluation par son corrigé lorsqu'on est en mode édition
    public DataCahier getDonnees() {
        DataCahier data;
        switch(mode) {
            case MODE_EDITION_CORRIGE :
                setCorrige(getDonneesEditeur());
                setModified(false);
                data = getCahier();
                break;
            case MODE_CORRECTION_ELEVE :
                sauvegarderFichierEleve();
                setModified(false);
                data = getCahier();
                break;
            default :
                data = super.getDonnees();
                //Insert la date de dernière modification
                String date = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault()).format(new Date());
                String labelLastMod = Traducteur.traduire("test header last modified");
                Component c = editeur.getComponentMap().get("12s");
                if(c!=null) {
                    JLabelText lastMod = (JLabelText)c;
                    lastMod.setText(labelLastMod + " : "+date);
                }
        }
        return data;
    }
    
    /** Utilisée pour savoir quand on a fait le tour des élèves à corriger **/
    private String firstFile = "";
    /** Contient l'adresse du fichier élève en cours de correction, ou null **/
    private Adresse correctingFile = null;
    /** Contient le fichier élève (DataFile) en cours de correction, ou null **/
    private DataFile correctingDoc = null;
    private Adresse getCorrectingFile() {return correctingFile;}
    private DataFile getCorrectingDocument() {return correctingDoc;}
    private boolean isDossierElevesSet() {return getCahier().getDataChapitre(getId()).containsElementKey(FICHIERS_ELEVES);}
    private Adresse getDossierCopiesEleves() {return isDossierElevesSet() ? new Adresse(getCahier().getDataChapitre(getId()).getElement(FICHIERS_ELEVES)) : null;}
    private void setCorrectingFile(Adresse a) {
        correctingFile = a;
        correctingDoc = (DataFile) a.chargement();
        setMode(MODE_CORRECTION_ELEVE);
        editeur.charger(getCorrectingDocument().getContenu());
    }
    private void sauvegarderFichierEleve() {
        getCorrectingDocument().setContenu(getDonneesEditeur());
        getCorrectingDocument().putElement(CORRECTED, "true");
        if(isCorrigePresent() && ajoutAutoCorrige.isSelected()) {getCorrectingDocument().putData(CORRIGE, getCorrige());}//On attache automatiquement le corrigé au fichier élève
        getCorrectingFile().sauvegarde(getCorrectingDocument());
    }
    
    private int getMode() {return mode;}
    
    /** Adapte l'interface au mode désiré. Attention : ne change pas le contenu de l'éditeur **/
    private void setMode(int mode) {
        if(this.mode==mode) {return;}
        this.mode = mode;
        switch(mode) {
            case MODE_NORMAL :
                if(Configuration.isTeacher()) {
                    special.setAction(isCorrigePresent() ? new ActionEditerCorrige() : new ActionCreerCorrige());
                }
                setModeCorrectionEnabled(false);
                break;
            case MODE_EDITION_CORRIGE :
                special.setAction(new ActionRetourRedaction());
                setModeCorrectionEnabled(true);
                break;
            case MODE_CORRECTION_ELEVE :
                special.setAction(new ActionSaveAndNext());
                setModeCorrectionEnabled(true);
                break;
        }
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
                //Outils profs
                exporterCorrige = getBarreOutils().addBoutonOnRight(new ActionExporterCorrige());
                supprimerCorrige = getBarreOutils().addBoutonOnRight(new ActionSupprimerCorrige());
                separateur = getBarreOutils().addSeparateurOnRight();
                commentaires = getBarreOutils().addBoutonOnRight(new ActionCommentaires());
                choixDossierEleves = getBarreOutils().addBoutonOnRight(new ActionChoixDossierEleves());
                listeEleves = getBarreOutils().addBoutonOnRight(new ActionOuvrirListeEleves());
                ajoutAutoCorrige = new ActionAjoutAutoCorrige(true);
                JMenuItem item = getMenuOptions().addCheckBox(ajoutAutoCorrige);
                item.setToolTipText((String) ajoutAutoCorrige.getValue(Action.SHORT_DESCRIPTION));//On force l'apparition du tooltip
            } else {
                importerDevoir = getBarreOutils().addBoutonOnRight(new ActionImporterDevoir());
                importerCorrections = getBarreOutils().addBoutonOnRight(new ActionImporterCorrections());
            }
            
            if(!outilsInitialises) {outilsInitialises = true;} else {
                //nettoyage
                if(activer) {
                    getBarreOutils().removeComponent(importerCorrections);
                    importerCorrections = null;
                    getBarreOutils().removeComponent(importerDevoir);
                    importerDevoir = null;
                } else {
                    getBarreOutils().removeComponent(exporterCorrige);
                    exporterCorrige = null;
                    getBarreOutils().removeComponent(supprimerCorrige);
                    supprimerCorrige = null;
                    getBarreOutils().removeComponent(separateur);
                    separateur = null;
                    getBarreOutils().removeComponent(commentaires);
                    commentaires = null;
                    getBarreOutils().removeComponent(choixDossierEleves);
                    choixDossierEleves = null;
                    getBarreOutils().removeComponent(listeEleves);
                    listeEleves = null;
                    getMenuOptions().remove(ajoutAutoCorrige);
                    ajoutAutoCorrige = null;
                }
            }
            getBarreOutils().revalidate();
        }
    }
    
    private boolean isCorrected() {
        return getCahier().getDataChapitre(getId()).containsElementKey(CORRIGE);
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
        String labelName = Traducteur.traduire("test header name");
        String labelClass = Traducteur.traduire("test header classroom");
        String labelStart = Traducteur.traduire("test header start");
        String labelLastMod = Traducteur.traduire("test header last modified");
        
        String nom = Configuration.getNomUtilisateur();
        String classe = Configuration.getClasse();
        int fontSize = EditeurKit.TAILLES_PT[0];
        String date = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault()).format(new Date());
        return "	<div id='header' style='font-size:"+fontSize+"pt;'>"+
"                            <div class=headerGauche style='padding-left:20px'>"+
                                "<p><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='9s'><span id='9' editable='false' removable='false' style='color:#000000;font-size:"+fontSize+";'>"+labelName+" : "+nom+"</span></span></p>"+
                                "<p><span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='10s'><span id='10' editable='false' removable='false' style='color:#000000;font-size:"+fontSize+";'>"+labelClass+" : "+classe+"</span></span></p>"+
                            "</div>"+
"                            <p style='text-align:right;"/*position:absolute;top:20px;right:50px;*/+"'>"+
                                "<span class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='11s'><span id='11' editable='false' removable='false' style='color:#000000;font-size:"+fontSize+";'>"+labelStart+" : "+date+"</span></span>"+
                            "</p>"+
"                            <p style='text-align:right;'><span id='fin'>&nbsp;</span></p>"+
"                            <p style='text-align:right;"/*position:absolute;top:20px;right:50px;*/+"'>"+
                                "<span id='last-modified' class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JLabelText.JLABEL_TEXTE+"' id='12s'><span id='12' editable='false' removable='false' style='color:#000000;font-size:"+fontSize+";'>"+labelLastMod+" : "+date+"</span></span>"+
                            "</p>"+
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
        JHeader header = new JHeader("", Traducteur.traduire("mark max value"), new DataTexte("<p>"+observations+" : </p><p>&nbsp;</p>"));
        long id = header.getId()+1;
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
"                    <div id='"+JMathTextPane.getSpanId(header.getId())+"style='width:100%;' class='"+JMathTextPane.SPECIAL_COMPONENT+" "+JHeader.JHEADER+"'>"+
                        header.getHTMLRepresentation(ComposantTexte.SVG_RENDERING.SVG, true)+
"                    </div>"+
                
"               </div>"+
                "<p style='text-align:left;color:#000000;font-size:"+fontSize+"pt'>&nbsp;</p>";
        return enTete;
    }

    public boolean nouvelleEvaluation() {
        String titre = DialogueBloquant.input("dialog new test", "", ImageManager.getIcone("new test"));

        if (titre==null) { return false; }
        if (titre.isEmpty()) { return nouvelleEvaluation(); }
        
        addChapitre(titre, genererNouveauContenu(titre, cahier.nbChapitres()+1));
        
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
        importerDevoir.setVisible(false);
        importerCorrections.setVisible(false);
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
                        int position = editeur.getHTMLdoc().getElement("rules").getStartOffset();
                        try {
                            editeur.getHTMLEditorKit().insertHTML(
                                    editeur.getHTMLdoc(),
                                    position,
                                    "<p>&nbsp;</p>",
                                    1, 1,
                                    HTML.Tag.P);
                            editeur.setCaretPosition(position+1);
                            editeur.insererLabel(label);
//                        editeur.insererLabel(label);
//                        new HTMLEditorKit.InsertBreakAction().actionPerformed(new ActionEvent(editeur, ActionEvent.ACTION_PERFORMED, "rules"));
                        } catch (BadLocationException ex) {
                            Logger.getLogger(OngletCahierDEvaluation.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(OngletCahierDEvaluation.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    activeActions(entry.getKey(), entry.getValue());
                }
                
                //insert le header s'il n'est pas présent
                if(editeur.getHTMLdoc().getElement("header")==null) {
//                        editeur.getHTMLdoc().insertAfterStart(editeur.getHTMLdoc().getElement("layout"), studentHeader());
                        EditeurIO.charger(editeur, new DataTexte(studentHeader()), editeur.getHTMLdoc().getElement("layout").getStartOffset());
                }
                
                //rend la date éditable en cas de devoir maison
                if(authorizations.get("hometest").equals(Boolean.TRUE)) {
                    ((JLabelText)editeur.getComponentMap().get("11s")).setEditable(true);
                    ((JLabelText)editeur.getComponentMap().get("12s")).setEditable(true);
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
        
        //Insert la date de fin
        String date = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault()).format(new Date());
        String labelEnd = Traducteur.traduire("test header end");
        JLabelText dateFin = new JLabelText(labelEnd+" : "+date, 14, Color.BLACK, false, false);
        dateFin.setEditable(false);
        dateFin.setRemovable(false);
        Element e = editeur.getHTMLdoc().getElement("fin");
        editeur.setCaretPosition(e.getStartOffset());
        editeur.getHTMLdoc().removeElement(e);
        editeur.insererLabel(dateFin);
        
        special.setAction(new ActionAttacherCorrige());
        importerDevoir.setVisible(true);
        importerCorrections.setVisible(true);
    }
    
    private final class ActionFinEvaluation extends ActionComplete {
        private ActionFinEvaluation() {super("test finished");}
        @Override
        public void actionPerformed(ActionEvent e) {
            endEvaluation();
            IHM.sauvegarde();//On sauvegarde les données
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
                        if(displayer.getEditeur().hasBeenModified()) {setModified(true);}
                        displayer = null;
                    }
                });
                if(getId()==0 && !isCommentairesPresent()) {
                    displayer.setAlwaysOnTop(false);
                    DialogueBloquant.dialogueBloquant("dialog help comments", DialogueBloquant.MESSAGE_TYPE.INFORMATION, DialogueBloquant.OPTION.DEFAULT, ImageManager.getIcone("test comments"));
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
            if(saveChanges()) {
                setMode(MODE_EDITION_CORRIGE);
                editeur.charger(getCorrige());
                DialogueBloquant.dialogueBloquant("dialog help solution", DialogueBloquant.MESSAGE_TYPE.INFORMATION, DialogueBloquant.OPTION.DEFAULT);
            }
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
            DialogueBloquant.CHOICE choix = DialogueBloquant.dialogueBloquant("test confirm solution removal", DialogueBloquant.MESSAGE_TYPE.WARNING, DialogueBloquant.OPTION.YES_NO, ImageManager.getIcone("test remove solution"));
            if(choix!=DialogueBloquant.CHOICE.YES) {return;}
            //On supprime le corrigé et les boutons associés
            cahier.getDataChapitre(getId()).removeDataByKey(CORRIGE);
            supprimerCorrige.setVisible(false);
            exporterCorrige.setVisible(false);
            special.setAction(new ActionCreerCorrige());
            //On retourne en mode normal si on était en train d'éditer le corrigé
            if(getMode()==MODE_EDITION_CORRIGE) {
                setMode(MODE_NORMAL);
                editeur.charger(getCahier().getContenuCourant());
            }
        }
    }

    private class ActionRetourRedaction extends ActionComplete {
        private ActionRetourRedaction() {super("test edit test");}
        @Override
        public void actionPerformed(ActionEvent e) {
            if(saveChanges()) {
                setMode(MODE_NORMAL);
                editeur.charger(getCahier().getContenuCourant());
            }
        }
    }
    
    private class ActionImporterDevoir extends ActionComplete {
        private ActionImporterDevoir() {super("test import statement");}
        @Override
        public void actionPerformed(ActionEvent e) {
            DataFile f = IHM.choixFichierImport();
            if(f==null) {return;}
            importer(f, !f.getTitre().equals(getCahier().getTitreCourant()));
        }
    }
    private class ActionImporterCorrections extends ActionComplete {
        private ActionImporterCorrections() {super("test import corrections");}
        @Override
        public void actionPerformed(ActionEvent e) {
            DataFile f = IHM.choixFichierImport();
            if(f==null) {return;}
            if(!f.getAuteur().equals(Configuration.getNomUtilisateur())) {
                DialogueBloquant.error("test author not matching");
                return;
            }
            if(f.getTitre().equals(getCahier().getTitreCourant())) {
                importer(f, false);
            } else {
                DialogueBloquant.CHOICE choix = DialogueBloquant.dialogueBloquant("test import not match", DialogueBloquant.MESSAGE_TYPE.WARNING, DialogueBloquant.OPTION.OK_CANCEL, ImageManager.getIcone("test import correction"));
                if(choix==DialogueBloquant.CHOICE.OK) {
                    cahier.setContenu(f.getContenu());
                    cahier.setTitre(getId(), f.getTitre());
                    chargerEditeur(f.getContenu());
                    setModified(true);
                }
            }
        }
    }
    private class ActionExporterDevoir extends ActionComplete {
        private ActionExporterDevoir() {super("test export test");}
        @Override
        public void actionPerformed(ActionEvent e) {
            String nomFichier = (Configuration.isTeacher() ? "" : (Configuration.getNomUtilisateur()+"_"+Configuration.getClasse()).replaceAll(" ", "_")+"_") + getCahier().getTitreCourant();
            IHM.choixFichierExport(new DataFile(null, OngletCahierDEvaluation.this.getName(), getId(), getCahier().getTitreCourant(), getCahier().getContenuCourant()), nomFichier);
        }
    }
    private class ActionExporterCorrige extends ActionComplete {
        private ActionExporterCorrige() {super("test export solution");}
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!isCorrigePresent() || !saveChanges()) {return;}
            String nomFichier = getCahier().getTitreCourant()+"_"+Traducteur.traduire("test solution");
            IHM.choixFichierExport(new DataFile(null, OngletCahierDEvaluation.this.getName(), getId(), getCahier().getTitreCourant(), getCorrige()), nomFichier);
        }
    }
    
    private class ActionChoixDossierEleves extends ActionComplete {
        private ActionChoixDossierEleves() {super("test select test folder");}
        @Override
        public void actionPerformed(ActionEvent e) {
            final JFileChooser fc = new JFileChooser(Configuration.getDossierCourant()) {
                @Override
                public void approveSelection() {
                    if (getSelectedFile()!=null && getSelectedFile().isFile()) {
                        super.setSelectedFile(getSelectedFile().getParentFile());
                    }
                    super.approveSelection();
                }
                @Override
                public void setSelectedFile(File file) {
                    if (file.isFile()) {
                        super.setSelectedFile(file.getParentFile());
                    } else {
                        super.setSelectedFile(file);
                    }
                }
            };
            if (System.getProperty("os.name").startsWith("Mac OS X")) {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            } else {
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            }
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setFileFilter(new Adresse.SingleFileFilter());
            fc.setApproveButtonText(Traducteur.traduire("choose directory"));
            int choix = fc.showOpenDialog(IHM.getMainWindow());
            if(choix==JFileChooser.APPROVE_OPTION) {
                Adresse a = new Adresse(fc.getSelectedFile());
                String[] files = a.listeFichiers(Adresse.EXTENSION_Bomehc_EXPORT_FILE);
                if(files.length==0) {DialogueBloquant.error("dialog empty directory");}
                else {
                    cahier.getDataChapitre(getId()).putElement(FICHIERS_ELEVES, a.getPath());
                    listeEleves.setVisible(true);
                    setModified(true);
                }
            }
        }
    }
    
//    private class ActionOuvrirCopieEleve extends ActionComplete {
//        private Adresse fichierEleve;
//        private ActionOuvrirCopieEleve(Adresse a) {super();}
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            setCorrectingFile(fichierEleve);
//        }
//    }
    
    private class ActionOuvrirListeEleves extends ActionComplete {
        private ActionOuvrirListeEleves() {super("test open student list");}
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] fichiers = getDossierCopiesEleves().listeNomFichiers(Adresse.EXTENSION_Bomehc_EXPORT_FILE);
            String title = Traducteur.traduire("dialog student files list title");
            String message = Traducteur.traduire("dialog student files list message");
            String fichier = (String) JOptionPane.showInputDialog(null, message,
                title, JOptionPane.QUESTION_MESSAGE, null,
                fichiers, // Array of choices
                getCorrectingFile()==null ? null : getCorrectingFile().getNom()); // Initial choice
            if(fichier!=null) {
                firstFile = getDossierCopiesEleves().getAbsolutePath()+Adresse.separator+fichier+"."+Adresse.EXTENSION_Bomehc_EXPORT_FILE;
                setCorrectingFile(new Adresse(firstFile));
            }
        }
    }
    
    private class ActionSaveAndNext extends ActionComplete {
        private ActionSaveAndNext() {super("test save and next");}
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] fichiers = getDossierCopiesEleves().listeFichiers(Adresse.EXTENSION_Bomehc_EXPORT_FILE);
            for(int i=0; i<fichiers.length; i++) {
                String s = fichiers[i];
                if(s.equals(getCorrectingFile().getName())) {//On repère le fichier actuel,
                    sauvegarderFichierEleve();
                    String next = getDossierCopiesEleves()+Adresse.separator+fichiers[(i==fichiers.length-1 ? 0 : i+1)];
                    if(!next.equals(firstFile)) {//On ouvre le fichier suivant
                        setCorrectingFile(new Adresse(next));
                    } else {//On retourne à l'éditeur
                        DialogueBloquant.dialogueBloquant("test correction over", DialogueBloquant.MESSAGE_TYPE.INFORMATION, DialogueBloquant.OPTION.DEFAULT, ImageManager.getIcone("test edit test"));
                        setMode(MODE_NORMAL);
                        editeur.charger(getCahier().getContenuCourant());
                        firstFile = "";
                    }
                    return;
                }
            }
        }
    }
    
    private static class ActionAjoutAutoCorrige extends ActionComplete.Toggle {
        private ActionAjoutAutoCorrige(boolean b) {super("test add auto solution", b);}
        @Override
        public void actionPerformed(ActionEvent e) {}
    }
    
}
