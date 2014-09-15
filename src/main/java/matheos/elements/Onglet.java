/** «Copyright 2011 François Billioud»
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

package matheos.elements;

import matheos.IHM;
import matheos.json.Json;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.Data.Enregistrable;
import matheos.sauvegarde.DataCahier;
import matheos.sauvegarde.DataTP;
import matheos.sauvegarde.DataTexte;
import matheos.utils.dialogue.DialogueBloquant;
import matheos.utils.managers.Traducteur;
import matheos.utils.interfaces.Undoable;
import matheos.utils.objets.Icone;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public abstract class Onglet extends JPanel implements Undoable, Enregistrable {

    public static final String BARRE_OUTILS = "barreOutils";
    public static final String MENU_OPTIONS = "menuOptions";
    
    protected BarreOutils barreOutils;
    protected BarreMenu.Menu menuOptions = new IHM.MenuOptions();
    /** sert d'identifiant pour les opérations saveParameter et getParameter **/
    private final String ID_parameter = this.getClass().getSimpleName();

    public Onglet() {
        setLayout(new BorderLayout());
        //setFocusable(true);
    }

    public BarreOutils getBarreOutils() {
        return barreOutils;
    }

    public void setBarreOutils(BarreOutils barre) {
        if(barreOutils==barre) {return;}
        firePropertyChange(BARRE_OUTILS, barreOutils, barre);
//        IHM.changeBarreOutils(barre);
        barreOutils = barre;
    }

    public BarreMenu.Menu getMenuOptions() {
        return menuOptions;
    }

    public void setMenuOptions(BarreMenu.Menu options) {
        if(menuOptions==options) {return;}
        firePropertyChange(MENU_OPTIONS, menuOptions, options);
        menuOptions = options;
//        IHM.changeMenuOptions(options);
    }

    public void activer(boolean b) {
//        setBorder(b ? BorderFactory.createLineBorder(ColorManager.get("color active part"), 5) : null);
        if(b) requestFocus();
        activeContenu(b);
    }

    public abstract void setActionEnabled(int actionID, boolean b);

//    public void updateUndoRedo() {
//        IHM.updateUndoRedo();
//    }

    /** Permet à un onglet d'enregistrer des préférences ou des données particulières dans le profil **/
    public void sauvegarderDonnees(String nomData, Data data) {
        try {
            IHM.saveParameter(nomData, Json.toJson(data), ID_parameter);
        } catch (IOException ex) {
            Logger.getLogger(Onglet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** Permet à un onglet de charger des préférences ou des données particulières précédemment enregistrées dans le profile. null sinon **/
    public Data chargerDonnees(String nomData) {
        try {
            String json = IHM.getParameter(nomData, ID_parameter);
            if(json==null) {return null;}
            return (Data) Json.toJava(json, Data.class);
        } catch (IOException ex) {
            Logger.getLogger(Onglet.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
//    public void setModified(boolean b) {
//        if(b==hasBeenModified()) {return;}
//        firePropertyChange(Undoable.MODIFIED, !b, b);
//        ongletModified(b);
//    }
    
    protected abstract void activeContenu(boolean b);

    public abstract void zoomP();

    public abstract void zoomM();
    
    public abstract void setNom(String nom);

    public static abstract class OngletCours extends Onglet {

        protected int ID = -1; //contient le numéro du chapitre ou de l'évaluation. Sert d'ID pour le contenu
        protected DataCahier cahier = new DataCahier();//Contient le contenu du cahier

        public OngletCours() {
            super();
            addMouseListener(new ChangeModeListener(ChangeModeListener.COURS));
            barreOutils = new IHM.BarreOutilsCours();
        }

        public abstract void imprimer();

        public abstract void miseEnPage();

        public abstract void apercu();

        public abstract long insertion(long id, String nomTP, DataTP donnees, String image, int hauteur);

        protected abstract String[] getTitres();
        
        protected abstract String creerEnTete(String sujet, int index);
        
        @Override
        public void setNom(String nom) {setName(nom);}

        /**
         * Crée le dataTexte d'un nouveau chapitre ou nouvelle évaluation
         * @param sujet le nom de l'élément
         * @param index le numéro de l'élément
         * @return le nouveau dataTexte
         */
        public DataTexte genererNouveauContenu(String sujet, int index) {
            return new DataTexte("<html><head></head><body>"+creerEnTete(sujet, index)+"</body></html>");
        }
        
        /**
         * Propose à l'utilisateur de choisir un chapitre ou une évaluation à ouvrir
         * @return le numéro de l'élément à ouvrir, ou -1 si aucun élément sélectionné
         */
        public int sommaire() {
            //cas où aucun chapitre n'a encore été créé
            if(cahier.getIndexCourant()==-1) {
                DialogueBloquant.warning(Traducteur.traduire("warning"), Traducteur.traduire("no chapter"));
                return -1;
            }

            //met en forme les titres
            JList<String> listeChoix = new JList<>(getTitres());
            listeChoix.setSelectedIndex(ID);
            String title = Traducteur.traduire("dialog contents");
            int choix = JOptionPane.showConfirmDialog(this, listeChoix, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, new Icone(IHM.getThemeElement("contents")));
            return choix==JOptionPane.OK_OPTION ? listeChoix.getSelectedIndex() : -1;
        }

        /**
         * Propose à l'utilisateur d'enregistrer ses modifications avant d'écraser son travail
         * @return true si l'utilisateur accepte la modification, false sinon
         */
        public boolean saveChanges() {
            if(cahier.getIndexCourant()==-1 || !hasBeenModified()) {return true;}//aucun changement à prendre en compte
            DialogueBloquant.CHOICE choix = DialogueBloquant.dialogueBloquant("dialog confirm save", DialogueBloquant.MESSAGE_TYPE.WARNING, DialogueBloquant.OPTION.YES_NO_CANCEL);
            System.out.println(choix.toString());
            if(choix==DialogueBloquant.CHOICE.YES) {IHM.sauvegarde();}
            return choix==DialogueBloquant.CHOICE.YES || choix==DialogueBloquant.CHOICE.NO;
        }
        
        /** récupère les données à sauvegarder **/
        protected abstract DataTexte getDonneesEditeur();

        public abstract void export2Docx(File destination);

        /** Système de sauvegarde par défaut. Enregistre le contenu dans le profil. **/
        public DataCahier getDonnees() {
            cahier.setContenu(getDonneesEditeur());
            setModified(false);
            return cahier;
        }

        /** Système de chargement par défaut. Charge les données telles que définies dans le statut du profil.
         *  Attention : l'ancien cahier sera écrasé. Charge à l'IHM de sauvegarder les données avant
         **/
        public void charger(Data cahier) {
            if(cahier==null) {System.out.println("le cahier était null");return;}
            if(cahier instanceof DataCahier) {this.cahier = (DataCahier) cahier;}
            else {(this.cahier = new DataCahier()).putAll(cahier);}
            ID = this.cahier.getIndexCourant();
            chargerEditeur(ID==-1 ? new DataTexte("") : this.cahier.getChapitre(ID));
        }
        
        /** charge le contenu d'un chapitre ou d'une évaluation dans l'éditeur **/
        protected abstract void chargerEditeur(Data element);

        /** Change l'indice de l'élément en cours et charge le document correspondant.
         *  Attention : le travail actuel sera écrasé. Charge à l'appelant de sauvegarder les données avant
         **/
        public void setElementCourant(int index) {
            cahier.setIndexCourant(index);
            ID = index;
            chargerEditeur(cahier.getChapitre(index));
        }
//        /**
//         * Charge un document dans l'éditeur
//         * @param ID identifiant du chapitre ou de l'évaluation
//         * @param donnees contenu
//         */
//        public void chargementOngletTexte(int ID, Data donnees) {
//            chargement(ID, donnees);
//        }
    }

    public static abstract class OngletTP extends Onglet implements TPFactory {

        protected static final String NOM = "nom";
        protected static final String ID_TP = "idTP";
        private String TPactuel = "auto";
        private long idTP = 0;

        public OngletTP() {
            super();
            barreOutils = new IHM.BarreOutilsTP();
            addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }

        public long getIdTP() {
            return idTP;
        }

        public void setIdTP(long idTP) {
            this.idTP = idTP;
        }
        
        /** définit la taille des TP lors de leur insertion. 0 par défaut pour laisser le JLabelTP gérer la taille **/
        public int preferredInsertionSize() {
            return 0;
        }

        /**
         * vérifie que l'utilisateur ne va pas écraser son travail dans la partie TP
         * et lui permet de l'insérer auparavant dans la partie cours.
         * @param ongletTP
         * @return true si le TP est écrasé
         */
        public boolean ecraserTP() {
            if (this.hasBeenModified()) {
                DialogueBloquant.CHOICE choix = DialogueBloquant.dialogueBloquant("dialog save old tp", DialogueBloquant.MESSAGE_TYPE.QUESTION, DialogueBloquant.OPTION.YES_NO_CANCEL);
                if (choix == DialogueBloquant.CHOICE.CANCEL || choix == DialogueBloquant.CHOICE.CLOSED) {
                    return false;
                }
                if (choix == DialogueBloquant.CHOICE.YES) {
                    setModified(false);
                    IHM.insererTP();
                }
            }
            return true;
        }

        public String capturerImage() {
            // Get a DOMImplementation.
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

            // Create an instance of org.w3c.dom.Document.
            String svgNS = "http://www.w3.org/2000/svg";
            Document document = domImpl.createDocument(svgNS, "svg", null);

            // Create an instance of the SVG Generator.
            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
            Writer w = new StringWriter();
            svgGenerator = (SVGGraphics2D) capturerImage(svgGenerator);
            try {
                svgGenerator.stream(w);
                return w.toString();
            } catch (SVGGraphics2DIOException ex) {
                Logger.getLogger(Onglet.class.getName()).log(Level.SEVERE, null, ex);
            }
            return w.toString();
        }
        
        protected abstract Graphics2D capturerImage(Graphics2D g);

        public String getNomTP() {
            return TPactuel;
        }

        @Override
        public void setNom(String nom) {
            setNomTP(nom);
        }
        
        protected void setNomTP(String nom) {
            TPactuel = nom;
        }
        
        @Override
        public DataTP getDonnees() {
            Data data = getDonneesTP();
            return new DataTP(getNomTP(), getIdTP(), data);
        }
        
        protected abstract Data getDonneesTP();

        protected abstract void chargement(/*long id, */Data donnees);

        @Override
        public void setBarreOutils(BarreOutils barre) {
            super.setBarreOutils(barre);
        }

        /**
         * Charge un document dans la zone TP
         * @param id identifiant du TP
         * @param donnees contenu. Si null, créera un nouveau TP
         */
        @Override
        public void charger(/*long id, */Data data) {
            if (data == null) {
                vider();
            } else {
                DataTP donnees;
                if(data instanceof DataTP) {donnees = (DataTP) data;}
                else {donnees = new DataTP("", 0, null);donnees.putAll(data);}
                try {
//                    DataTP data = (DataTP) JsonReader.jsonToJava(donnees);
                    idTP = donnees.getId();
                    TPactuel = donnees.getNom();
                    chargement(/*id, */donnees.getContenuTP());
                    setModified(false);
                } catch(Exception e) {
                    System.out.println("erreur lors du chargement du TP");
                    Logger.getLogger(OngletTP.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
        
        private void vider() {
            nouveau();
        }
        
        public void nouveau() {
            setModified(false);
            setIdTP(0);
            nouveauTP();
        }
        
        protected abstract void nouveauTP();

    }

    public interface TPFactory {

        public void charger(/*long id, */Data donnees);

        public String capturerImage();
    }

}
