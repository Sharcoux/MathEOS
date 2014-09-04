/**
 * «Copyright 2012,2013 François Billioud»
 *
 * This file is part of MathEOS.
 *
 * MathEOS is free software: you can redistribute it and/or modify under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * MathEOS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY.
 *
 * You should have received a copy of the GNU General Public License along with
 * MathEOS. If not, see <http://www.gnu.org/licenses/>.
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

import matheos.IHM.ONGLET;
import matheos.IHM.ONGLET_TEXTE;
import matheos.clavier.Clavier;
import matheos.clavier.ClavierCaractereSpeciaux;
import matheos.clavier.ClavierNumerique;
import matheos.elements.*;
import matheos.graphic.fonctions.OngletFonctions;
import matheos.graphic.geometrie.OngletGeometrie;
import matheos.operations.OngletOperations;
import matheos.sauvegarde.DataCahier;
import matheos.sauvegarde.DataProfil;
import matheos.table.OngletTable;
import matheos.tableau.OngletTableaux;
import matheos.texte.OngletCahierDEvaluation;
import matheos.texte.OngletCahierDExercice;
import matheos.texte.OngletCahierDeCours;
import matheos.utils.objets.Calculatrice;
import matheos.texte.Editeur;
import matheos.utils.objets.Loading;
import matheos.utils.managers.Traducteur;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.Bouton;
import matheos.utils.dialogue.DialogueAbout;
import matheos.utils.dialogue.DialogueBloquant;
import matheos.utils.dialogue.DialogueComplet;
import matheos.utils.dialogue.DialogueEvent;
import matheos.utils.dialogue.DialogueListener;
import matheos.utils.fichiers.Adresse;
import matheos.utils.managers.ImageManager;
import matheos.utils.managers.PermissionManager;
import matheos.utils.objets.Clock;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.Action;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

/**
 * Définit le modèle de la fenêtre principale. C'est une classe abstraite qui
 * sert de modèle pour les interfaces. Elle définit la structure fixe de la
 * fenêtre : le plein écran, le layout, la barre de menu, l'espace pour la barre
 * du haut, la barre du bas et l'écran partagé ainsi que la flèche permettant
 * les changements de dimension
 */
@SuppressWarnings("serial")
public final class IHM {
    private static volatile boolean interfaceReady = false;
    private static volatile boolean chargementEnAttente = false;
    
    private static final String SAUVEGARDE_AUTOMATIQUE = "sauvegarde auto";
    public static enum ACTION { CALCULATRICE, CONSULTATION, FONCTIONS, TRACER_FONCTION, CARACTERES_LITTERAUX, CARACTERES_COLLEGE, COMPARATEURS_SPECIAUX, RACINE_CARREE, CARACTERES_AVANCES, FIN_EVALUATION, DEMI_DROITE, POSITION_CURSEUR, PROPORTIONNALITE };

    public static enum ONGLET {

        TEXTE(ONGLET_TYPE.TEXTE, null,-1,null),
        COURS(ONGLET_TYPE.TEXTE, "notebook",0,OngletCahierDeCours.class),
        EXERCICE(ONGLET_TYPE.TEXTE, "workbook",1,OngletCahierDExercice.class),
        EVALUATION(ONGLET_TYPE.TEXTE, "evaluation",2,OngletCahierDEvaluation.class),
        TP(ONGLET_TYPE.TP, null,-1,null),
        OPERATIONS(ONGLET_TYPE.TP, "operation",0,OngletOperations.class),
//        TABLEAUX(ONGLET_TYPE.TP, "table",1,OngletTableaux.class),
        TABLEAUX(ONGLET_TYPE.TP, "table",1,OngletTable.class),
        GEOMETRIE(ONGLET_TYPE.TP, "geometry",2,OngletGeometrie.class),
        FONCTION(ONGLET_TYPE.TP, "function",3,OngletFonctions.class);
        
        private final String nom;
        private final ONGLET_TYPE type;
        private final int index;
        private Onglet instance;
        private final Class<?> classe;

        private ONGLET(ONGLET_TYPE type, String nom, int index, Class<?> classe) {
            this.type = type;
            this.nom = nom;
            this.index = index;
            this.classe = classe;
        }

        public void setInstance(Onglet onglet) {
            instance = onglet;
            onglet.setNom(this.getNom());
        }

        public String getNom() { return this.nom; }
        public ONGLET_TYPE getType() { return this.type; }
        public int getIndex() { return this.index; }
        public Onglet getInstance() { return this.instance; }
        public static ONGLET getOnglet(Onglet o) {
            for(ONGLET onglet : ONGLET.values()) { if(o==onglet.getInstance()) {return onglet;} }
            return null;
        }
    }

    public static enum ONGLET_TEXTE {
        COURS, EXERCICE, EVALUATION;
        public ONGLET toOnglet() {
            return ONGLET.valueOf(this.name());
        }
        public String getNom() {
            return this.toOnglet().getNom();
        }
        public Onglet.OngletCours getInstance() {
            return (Onglet.OngletCours) toOnglet().getInstance();
        }
    }

    public static enum ONGLET_TP {
        OPERATIONS, TABLEAUX, GEOMETRIE, FONCTION;
        public String getNom() {
            return this.toOnglet().getNom();
        }
        public ONGLET toOnglet() {
            return ONGLET.valueOf(this.name());
        }
        public Onglet.OngletTP getInstance() {
            return (Onglet.OngletTP) toOnglet().getInstance();
        }
    }

    public static enum ONGLET_TYPE {
        TEXTE, TP;
        public ONGLET toOnglet() {
            return ONGLET.valueOf(this.name());
        }
    }
    
    static void setTheme(String newTheme) {
        if(newTheme.equals(Configuration.getTheme())) {return;}
        Configuration.setTheme(newTheme);
        if (interfaceMathEOS != null) { resetInterface(); }
    }

    /**
     * Renvoie la chaine lue dans le fichier de thème pour la balise spécifiée
     * @param balise clé à chercher dans le fichier de thème
     * @return la chaine lue, ou null si non trouvée
     */
    public static String getThemeElement(String balise) {
        return Configuration.getFichierTheme().getContenu(balise);
    }
    /**
     * Renvoie la map des valeurs lues dans le fichier de thème pour la balise spécifiée
     * @param balise clé à chercher dans le fichier de thème
     * @return la map des données correspondantes, ou null si non trouvée
     */
    public static String[] getThemeElementBloc(String balise) {
        return Configuration.getFichierTheme().getContenuBloc(balise);
    }
    /**
     * Récupère l'élément du thème correspondant à la balise spécifiée. Le booléen permet de préciser si la donnée est critique
     * pour le fonctionnement du logiciel.
     * @param balise chaine contenant la balise à retrouver
     * @param isCritical si l'élément est indispensable
     * @return la chaine lue, ou null si non trouvée
     */
    public static String getThemeElement(String balise,boolean isCritical) {
        return Configuration.getFichierTheme().getContenu(balise,isCritical);
    }
    //gère les langues
    static void setLangue(String newLangue) {
        if(newLangue.equals(Configuration.getLangue())) {return;}
        Configuration.setLangue(newLangue);
        if (interfaceMathEOS != null) { resetInterface(); }
    }

    private static DataProfil getProfil() {
        return Configuration.getProfil();
    }

//    //gère les profils
//    private static void setThemeNLanguage(String theme, String langue) {
//        if(theme.equals(Configuration.getTheme())&&langue.equals(Configuration.getLangue())) {return;}
////        Configuration.setTheme(theme);
////        Configuration.setLangue(langue);
////        Traducteur.setLangue(langue);
//        if (interfaceMathEOS != null) { resetInterface(); }
//    }
    /**
     * prépare l'interface pour le profil passé en paramètre (langue, thème et authorisations)
     */
    private static void changerProfil(DataProfil profil) {
        if (profil == null) {
            nouveauProfil();
            return;
        }
        boolean restartNeeded = !profil.getTheme().equals(Configuration.getTheme()) || !profil.getLangue().equals(Configuration.getLangue());
        Configuration.setProfil(profil);
        if(restartNeeded) {resetInterface();}
        if(!interfaceReady) {chargementEnAttente = true; return;}//cas du lancement du logiciel
        System.out.println("chargement du profil effectué par l'utilisateur");
        chargement();//charge les onglets depuis le nouveau profil
    }

    /**
     * Variable contenant la fenêtre complète *
     */
    private static InterfaceComplete interfaceMathEOS;
    
    private static void choisirProfilInitial() {
        final String[] options = {Traducteur.traduire("choose open profil"), Traducteur.traduire("choose create profil"), Traducteur.traduire("choose cancel")};
        final String message = Traducteur.traduire("choose open or create profil");
        final String title = Traducteur.traduire("choose open or create profil title");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int answer = JOptionPane.showOptionDialog(null, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
                switch (answer) {
                    case JOptionPane.YES_OPTION:
                        if(!ouvrirProfil()) {choisirProfilInitial();}//si aucun profil n'a été ouvert, on réaffiche les différents choix
                        break;
                    case JOptionPane.NO_OPTION:
                        nouveauProfil();
                        break;
                    default:
                        close();
                        break;
                }
            }
        });
    }

    /** cherche le profil à lancer **/
    static void lancer() {
        DataProfil p = getProfil();
        if (p == null) {//choix du profil à ouvrir
            choisirProfilInitial();
        } else {//profil lu dans la config utilisateur
            changerProfil(p);
            System.out.println("profil read from config");
        }
        startInterface();
        System.out.println("interface started");
    }

    /**
     * Crée un interface de base en positionnant les éléments : une barre menu,
     * un panel au Nord, un EcranPartage (JSplitPane) au Centre et une BarreBas
     * (JPanel) au Sud
     */
    static void startInterface() {
        interfaceMathEOS = new InterfaceComplete(getThemeElement("laf"));
        System.out.println("interfaceMathEOS created");
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(2000);
        initialize();
        System.out.println("interfaceMathEOS initialized");
    }

    static void resetInterface() {
        //interfaceMathEOS.sauvegarder(getCurrentProfile());
        interfaceMathEOS.masque();
        interfaceMathEOS.getFenetre().dispose();
        interfaceMathEOS=null;
        interfaceReady = false;
        chargementEnAttente = true;//On doit charger le profil à la fin du changement
        startInterface();
    }

    public static JFrame getMainWindow() {return interfaceMathEOS==null ? null : interfaceMathEOS.getFenetre();}
    
    /**
     * Ferme la fenêtre principale de MathEOS, et donc l'application
     */
    private static void close(){
        Loading.stop();
        try {
            interfaceMathEOS.getFenetre().dispose();
        } catch(NullPointerException ex) {}
        finally {
            System.exit(0);
        }
    }

//    public static void addToSizeManager(Component c, int largeur, int hauteur) {
        //FIXME
        //la ligne ci-dessous permet normalement de régler la taille des différents éléments de l'interface quelque soit la taille de la fenêtre du logiciel.
        //Cependant, le redimensionnement des éléments prends trop de temps et fait ramer le logiciel. Il faudrait essayer de lancer le redimensionnement dans un autre thread.
        //interfaceMathEOS.getSizeManager().addToSizeManager(c, largeur, hauteur);
//    }
    public static void activeAction(ACTION actionName, boolean active) {
        switch(actionName) {
            case PROPORTIONNALITE : ONGLET_TP.TABLEAUX.getInstance().setActionEnabled(OngletTableaux.ACTION_PROPORTIONNALITE, active); break;
            case POSITION_CURSEUR : ONGLET_TP.GEOMETRIE.getInstance().setActionEnabled(OngletGeometrie.ACTION_POSITION_CURSEUR, active); break;
            case DEMI_DROITE : ONGLET_TP.GEOMETRIE.getInstance().setActionEnabled(OngletGeometrie.ACTION_DEMI_DROITE, active); break;
            case TRACER_FONCTION : ONGLET_TP.FONCTION.getInstance().setActionEnabled(OngletFonctions.ACTION_TRACE, active); break;
            case FONCTIONS : 
                interfaceMathEOS.setOngletTPEnabled(ONGLET.FONCTION.getIndex(), active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_FLECHE_FONCTION, active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_INFINI, active);
                break;
            case CALCULATRICE : actionCalculatrice.setEnabled(active); break;
            case CONSULTATION : 
                actionConsultation.setEnabled(active);
                interfaceMathEOS.setOngletCoursEnabled(ONGLET.COURS.getIndex(), active);
                interfaceMathEOS.setOngletCoursEnabled(ONGLET.EXERCICE.getIndex(), active);
                break;
            case CARACTERES_LITTERAUX :
                actionClavierNumerique.clavier.activerBouton(ClavierNumerique.BOUTON_X, active);
                actionClavierNumerique.clavier.activerBouton(ClavierNumerique.BOUTON_Y, active);
                actionClavierNumerique.clavier.activerBouton(ClavierNumerique.BOUTON_A, active);
                break;
            case CARACTERES_COLLEGE :
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_APPARTIENT, active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_CHAPEAU_ANGLE, active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_DIFFERENT, active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_ENV_EGAL, active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_EXPOSANT, active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_INDICE, active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_N_APPARTIENT_PAS, active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_PARALLELE, active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_PERPENDICULAIRE, active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_PI, active);
                break;
            case COMPARATEURS_SPECIAUX :
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_INF_EGAL, active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_SUP_EGAL, active);
                break;
            case RACINE_CARREE :
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_RACINE_CARREE, active);
                break;
            case CARACTERES_AVANCES :
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_EQUATION, active);
                actionClavierSpecial.clavier.activerBouton(ClavierCaractereSpeciaux.BOUTON_SYSTEME, active);
                break;
        }
    }

    /**
     * gère complètement un changement de mode *
     */
    public static void activeMode(boolean newMode) {
        interfaceMathEOS.activeMode(newMode);
    }

    /**
     * force un onglet à devenir actif. Change de mode si nécessaire
     */
    public static void setOngletActif(Onglet onglet) {
        interfaceMathEOS.setOngletActif(onglet);
    }

    public static void setTaille(int taille) {
        interfaceMathEOS.setTaille(taille);
    }

    /** Crée une BarreOutils préconfigurée pour les Cours avec les boutons gérés par l'IHM **/
    public static class BarreOutilsCours extends BarreOutils {
        public BarreOutilsCours() {//Place les boutons contrôlés par l'IHM
            this.addSwitchOnRight(actionClavierSpecial);
            this.addBoutonOnRight(actionInsertion);
        }
    }

    /** Crée une BarreOutils préconfigurée pour les TP avec les boutons gérés par l'IHM **/
    public static class BarreOutilsTP extends BarreOutils {
        public BarreOutilsTP() {//Place les boutons contrôlés par l'IHM
            this.addSwitchOnRight(actionClavierSpecial);
            this.addBoutonOnRight(actionInsertion);
            this.addBoutonOnRight(actionUpdateTP);
            this.addBoutonOnRight(actionNouveauTP);
        }
    }

    public static class MenuOptions extends BarreMenu.Menu {
        public MenuOptions() {
            super(BarreMenu.MENU_OPTION);
            //Place les bouton contrôlés par l'IHM
            addElement(actionLangue);
            addElement(actionTheme);
        }
    }

    public static boolean getMode() {
        return interfaceMathEOS.getMode();
    }

    public static Onglet getOngletActif() {
        return interfaceMathEOS.getOngletActif();
    }

    private static Onglet.OngletCours getOngletCoursActif() {
        return interfaceMathEOS.getOngletCoursActif();
    }

    private static Onglet.OngletTP getOngletTPActif() {
        return interfaceMathEOS.getOngletTPActif();
    }

//    private static Onglet.OngletTP getOngletTP(int index) {
//        return interfaceMathEOS.getOngletTP(index);
//    }
//
//    private static Onglet.OngletCours getOngletCours(int index) {
//        return interfaceMathEOS.getOngletCours(index);
//    }

/*    private static class OngletInitializer extends Thread {
        private final ONGLET onglet;
        private OngletInitializer(ONGLET onglet) {this.onglet = onglet;}
        @Override
        public void run() {
            switch(onglet.getType()) {
                case TEXTE:
                    Onglet.OngletCours o = null;
                    try {
                        o = (Onglet.OngletCours) onglet.classe.newInstance();
                    } catch (InstantiationException ex) {
                        Logger.getLogger(IHM.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(IHM.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    onglet.setInstance(o);
                    if(o!=null) { interfaceMathEOS.addOngletCours(onglet.getNom(), o); }
                    break;
                case TP:
                    Onglet.OngletTP p = null;
                    try {
                        p = (Onglet.OngletTP) onglet.classe.newInstance();
                    } catch (InstantiationException ex) {
                        Logger.getLogger(IHM.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(IHM.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    onglet.setInstance(p);
                    if(p!=null) { interfaceMathEOS.addOngletTP(onglet.getNom(), p); }
                    break;
            }
        }
    }
*/
    private static void initializeBarreMenu() {
        BarreMenu menu = interfaceMathEOS.getBarreMenu();

        //fenetre
        menu.addElement(new ActionNouveau(), BarreMenu.FICHIER);
        menu.addElement(new ActionOuvrir(), BarreMenu.FICHIER);
        menu.addElement(actionSauvegarde, BarreMenu.FICHIER);
        menu.addElement(new ActionDocx(), BarreMenu.FICHIER);
        menu.addElement(new ActionMiseEnPage(), BarreMenu.FICHIER);
        menu.addElement(new ActionApercu(), BarreMenu.FICHIER);
        menu.addElement(new ActionImprimer(), BarreMenu.FICHIER);

        menu.addElement(new ActionAPropos(), BarreMenu.AIDE);
        
        menu.addBouton(actionSauvegarde);

        /*
         * /boutons menu.add(interfaceMathEOS.boutonCouper); menu.add(interfaceMathEOS.boutonCopier); menu.add(interfaceMathEOS.boutonColler); menu.add(interfaceMathEOS.boutonAnnuler); menu.add(interfaceMathEOS.boutonRefaire);
         */

    }

    /**
     * Ouvre une fenêtre permettant de créer un nouveau profil, puis de le
     * charger dans l'application.
     *
     * @param opening boolean permettant de savoir si c'est le premier
     * profil créé à la première ouverture du logiciel, où s'il s'agit d'un
     * nouveau profil créé par la suite
     */
    static void nouveauProfil() {
        final DialogueComplet dialogue = new DialogueComplet("dialog new profile");
        dialogue.addDialogueListener(new DialogueListener() {
            @Override
            public void dialoguePerformed(DialogueEvent event) {
                //Si aucun profil n'existe actuellement, il faut absolument en choisir un
                if((event.isCloseButtonPressed() || event.isCancelButtonPressed()) && getProfil()==null) {
                    choisirProfilInitial();
                }
                else if(event.isConfirmButtonPressed()) {
                    String prenom = event.getInputString("firstname");
                    String nom = event.getInputString("lastname");
                    String classeID = event.getInputString("classroom");
                    int niveau = ((JComboBox)dialogue.getInputComponent("level")).getSelectedIndex();
                    DataProfil profil = new DataProfil(nom, prenom, niveau, Configuration.getLangue(), Configuration.getTheme(), classeID);
                    
                    //crée les cahiers
                    for(ONGLET_TEXTE onglet : ONGLET_TEXTE.values()) {
                        profil.setCahier(onglet.getNom(), new DataCahier());
                    }
                    definirAdresseFichier(profil);
                    profil.sauvegarder();
                    //charge le profil dans l'interface
                    changerProfil(profil);
                    dialogue.dispose();
                }
            }
        });
    }
    
    private static void definirAdresseFichier(DataProfil profil) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new Adresse.MathEOSFileFilter());
        String adresseParDefaut = profil.getNom()+ "_" + profil.getPrenom()+ "_" + profil.getClasse()+ "." + Adresse.EXTENSION_MathEOS;
        fc.setSelectedFile(new File(adresseParDefaut));
        fc.setCurrentDirectory(new File(Configuration.getDossierCourant()));
        System.out.println(Configuration.getDossierCourant());
        int choix = fc.showSaveDialog(null);
        if(choix==JFileChooser.APPROVE_OPTION) {
            File fichier = fc.getSelectedFile();
            if(!fichier.getPath().endsWith(Adresse.EXTENSION_MathEOS)) {fichier = new File(fichier.getAbsolutePath()+"."+Adresse.EXTENSION_MathEOS);}
            //cas où on écrase un fichier
            if(fichier.exists()) {
                DialogueBloquant.CHOICE decision = DialogueBloquant.dialogueBloquant("dialog file already exists", DialogueBloquant.MESSAGE_TYPE.WARNING, DialogueBloquant.OPTION.YES_NO);
                if(decision!=DialogueBloquant.CHOICE.YES) { definirAdresseFichier(profil); return; }//on recommence
                fichier.delete();
            }
            if(getProfil()==profil) {//Si le profil est le profil actuel
                Configuration.setAdresseFichierCourant(fichier.getAbsolutePath());
            } else {//Si le profil n'est pas le profil actuel
                profil.setAdresseProfil(fichier.getAbsolutePath());
            }
        }
    }

    /**
     * Ouvre un JFileChooser pour demandaer de choisir un profil, puis tente de l'ouvrir
     * @return true si le profil a été ouvert correctement, false sinon
     */
    static boolean ouvrirProfil() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new Adresse.MathEOSFileFilter());
        fc.setCurrentDirectory(new File(Configuration.getDossierCourant()));
        int choix = fc.showOpenDialog(null);
        if(choix==JFileChooser.APPROVE_OPTION) {
            String adresse = fc.getSelectedFile().getAbsolutePath();
            DataProfil p = (DataProfil) new Adresse(adresse).chargement();
            if(p!=null) {
                p.setAdresseProfil(adresse);//au cas où le fichier s'est déplacé
                changerProfil(p);
            }
            return p!=null;
        } else {
            return false;
        }
    }

//    public static void sauvegardeAuto() {
//
//        for(ONGLET_TEXTE onglet : ONGLET_TEXTE.values()) {
//            if(onglet.getInstance()!=null) {
//                onglet.getInstance().sauvegarderDonnees(SAUVEGARDE_AUTOMATIQUE, onglet.getInstance().getDonneesTexte());
//                //TODO rajouter la sauvegade définitive des données des onglets de cours
//            }
//        }
//        for(ONGLET_TP onglet : ONGLET_TP.values()) {
//            if(onglet.getInstance()!=null && onglet!=ONGLET_TP.OPERATIONS && onglet!=ONGLET_TP.TABLEAUX) {
//                onglet.getInstance().sauvegarderDonnees(SAUVEGARDE_AUTOMATIQUE, onglet.getInstance().getDonneesTP());
//            }
//        }
//    }
//    public static void chargementAuto() {
//        for(ONGLET_TP onglet : ONGLET_TP.values()) {
//            //TODO trouver un moyen de récupérer l'id du TP
//            if(onglet.getInstance()!=null) {
//                onglet.getInstance().chargementTP(0, onglet.getInstance().chargerDonnees(SAUVEGARDE_AUTOMATIQUE));
//            }
//        }
//    }

    /** lance le processus de sauvegarde de tous les onglets de cours dans le profil **/
    public static void sauvegarde() {
        for(ONGLET_TEXTE onglet : ONGLET_TEXTE.values()) {
            if(onglet.getInstance()!=null) {
                getProfil().setCahier(onglet.getNom(), onglet.getInstance().getDonnees());
            }
        }
        if(getProfil().getAdresseFichier()==null) {definirAdresseFichier(getProfil());}
        getProfil().sauvegarder();
//        actionSauvegarde.setEnabled(false);
    }

    /** lance le chargement de tous les onglets de cours depuis le profil **/
    private static void chargement() {
        for(ONGLET_TEXTE onglet : ONGLET_TEXTE.values()) {
            if(onglet.getInstance()!=null) {
                System.out.println("trying to load "+onglet.getNom());
                onglet.getInstance().charger(getProfil().getCahier(onglet.getNom()));
                System.out.println("onglet "+onglet.getNom()+" loaded");
            }
        }
        //permet de mettre à jour l'affichage après le chargement d'un profil.
        getOngletActif().activer(true);
        
        //Selectionne l'onglet cours
        setOngletActif(ONGLET_TEXTE.COURS.getInstance());
        
        //Affiche le nom de l'élève dans le titre de la fenêtre
        interfaceMathEOS.getFenetre().setTitle("MathEOS - "+getProfil().getPrenom()+" "+getProfil().getNom()+" "+getProfil().getClasse());
        
        //Charge les permissions
        PermissionManager.readPermissions(getProfil().getClasse());
    }
    
    /** Enregistre un paramètre dans le fichier profil **/
    public static void saveParameter(String dataName, String data, String ID) {
        getProfil().putElement(ID+"*"+dataName, data);
    }
    /** Renvoie le paramètre lu, ou null si non trouvé **/
    public static String getParameter(String dataName, String ID) {
        return getProfil().getElement(ID+"*"+dataName);
    }

    /** renvoie vrai ssi les données de l'utilisateur ne sont plus menacées **/
    private static boolean saveChanges() {
        if(!actionSauvegarde.isEnabled()) {return true;}//rien à sauvegarder
        DialogueBloquant.CHOICE choix = DialogueBloquant.dialogueBloquant("dialog confirm save", DialogueBloquant.MESSAGE_TYPE.WARNING, DialogueBloquant.OPTION.YES_NO_CANCEL);
        if(choix==DialogueBloquant.CHOICE.YES) {sauvegarde();}
        return choix==DialogueBloquant.CHOICE.YES || choix==DialogueBloquant.CHOICE.NO;
    }
//    public static Image recreateImageFromData(String nomTP, Data donnees) {
//        //TODO créer une factory plus adaptée que l'onglet complet
//        for(ONGLET_TP o : ONGLET_TP.values()) {
//            if(nomTP.equals(o.toOnglet().getNom())) {
//                Onglet.TPFactory factory = (Onglet.TPFactory) o.toOnglet().getInstance();
//                factory.chargementTP(0, donnees);
//                return factory.capturerImage();
//            }
//        }
//        return null;
//    }

    private static abstract class ActionClavier extends ActionComplete.Toggle /*implements WindowListener, WindowStateListener*/ {
        /** Contient la fenêtre contenant les boutons du clavier **/
        protected Clavier clavier;
        private ActionClavier(String aspect, Clavier c) {
            super(aspect, false);
            this.clavier = c;
            //On désactive le bouton si le clavier est fermé
            c.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {ActionClavier.this.setSelected(false);}
            });
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            clavier.setVisible(isSelected());//L'état semble modifié après
        }
        public Clavier getClavierWindow() {return clavier;}
    }

    private static ActionClavierNumerique actionClavierNumerique;
    public static class ActionClavierNumerique extends ActionClavier {
        public ActionClavierNumerique() {
            super("numeric keyboard", new ClavierNumerique());
        }
    }

    private static ActionClavierCaracteresSpeciaux actionClavierSpecial;
    private static final class ActionClavierCaracteresSpeciaux extends ActionClavier {
        private ActionClavierCaracteresSpeciaux() {
            super("special character",new ClavierCaractereSpeciaux());
        }
    }

    private static final class ActionZoomP extends ActionComplete {
        private ActionZoomP() { super("zoom+"); }
        @Override
        public void actionPerformed(ActionEvent e) {
            getOngletActif().zoomP();
        }
    }

    private static final class ActionZoomM extends ActionComplete {
        private ActionZoomM() { super("zoom-"); }
        @Override
        public void actionPerformed(ActionEvent e) {
            getOngletActif().zoomM();
        }
    }

    private static Action actionCalculatrice;
    private static final class ActionCalculatrice extends ActionComplete {
        private ActionCalculatrice() {
            super("calculator");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
//            if(!PermissionManager.isCalculatriceAllowed()) {
//                JOptionPane.showMessageDialog(interfaceMathEOS.getFenetre(), "L'utilisation de la calculatrice n'est pas autorisée", "Operation refusée", JOptionPane.WARNING_MESSAGE);
//                return;
//            }
            try {
                Runtime.getRuntime().exec("calc.exe");
            } catch (Exception ex) {
                Calculatrice.getInstance().setVisible(true);
            }
        }
    }

    private static Action actionNouveauTP;
    private static final class ActionNouveauTP extends ActionComplete {

        private ActionNouveauTP() {
            super("new tp");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Onglet.OngletTP o = getOngletTPActif();
            if(!o.hasBeenModified() || o.ecraserTP()) {o.nouveau();}
        }
    }

    private static final class ActionNouveau extends ActionComplete {

        private ActionNouveau() {
            super("new profile");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //Propose la sauvegarde des données avant de changer de profil
            saveChanges();
            nouveauProfil();
        }
    }

    private static final class ActionOuvrir extends ActionComplete {

        private ActionOuvrir() {
            super("open profile");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            saveChanges();
            ouvrirProfil();
        }
    }

    /** En cas de modification du contenu de la partie cours, remet en fonctionnement le bouton sauvegarde **/
//    public static void notifyEdit() {actionSauvegarde.setEnabled(true);}
    private static Action actionSauvegarde;
    private static final class ActionSauvegarde extends ActionComplete {

        private ActionSauvegarde() {
            super("save all");setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) { //interfaceMathEOS.sauvegarder(getCurrentProfile());
            sauvegarde();
        }
    }

    private static final class ActionImprimer extends ActionComplete {

        private ActionImprimer() {
            super("print");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getOngletCoursActif().imprimer();
        }
    }

    private static final class ActionMiseEnPage extends ActionComplete {

        private ActionMiseEnPage() {
            super("layout");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getOngletCoursActif().miseEnPage();
        }
    }

    private static final class ActionApercu extends ActionComplete {

        private ActionApercu() {
            super("quick view");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getOngletCoursActif().apercu();
        }
    }
    
    /**
     * Méthode qui récupère les infos du TP actifs pour les donner au Cours actif afin de faire une insertion
     */
    public static void insererTP() {
        interfaceMathEOS.insererTP();
    }
    /**
     * Méthode qui récupère les infos du TP actifs pour les donner au Cours actif afin de mettre à jour le TP
     */
    public static void updateTP() {
        interfaceMathEOS.updateTP();
    }

    private static Action actionInsertion;
    private static final class ActionInsertion extends ActionComplete {

        private ActionInsertion() {
            //INSERTION_STATE : passer a false pour n afficher l insertion qu apres modification
            super("insert");setEnabled(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            insererTP();
        }
    }
    
    private static Action actionUpdateTP;
    private static final class ActionUpdateTP extends ActionComplete {

        private ActionUpdateTP() {
            super("edit tp");setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            updateTP();
        }
    }

//    private static void updateORinsertTP(long idTP) {
//            Onglet.OngletTP onglet = getOngletTPActif();
//            //PENDING
//            //A supprimer lorsque le clonage des Opérations sera fonctionel
//            // Cela sert seulement à empêcher que la même opération soit insérée deux fois
//            if (onglet.getNomTP().equals(IHM.ONGLET.OPERATIONS.name()) && getOngletTPActif().getIdTP() != 0) {
//                JOptionPane.showMessageDialog(interfaceMathEOS.getFenetre(), "Cette opération a déjà été insérée");
//                return;
//            }
//            //Fin suppression
//            BufferedImage photoTP = getMode()==EcranPartage.COURS ? interfaceMathEOS.getPhotoTP() : onglet.capturerImage();//permet d'éviter que l'image soit grisée
//            long id = getOngletCoursActif().insertion(idTP, onglet.getNomTP(), onglet.getDonneesTP(), photoTP, onglet.preferredInsertionSize());
//            if (id != 0) { onglet.tpInserted(id); }
//    }
//    /**
//     * Méthode qui récupère les infos du TP actifs pour les donner au Cours actif afin de faire une insertion
//     */
//    public static void insererTP() {
//        updateORinsertTP(0);
//    }

//    public static void setBoutonUpdateTPEnabled(boolean b) {
//        actionUpdateTP.setEnabled(b);
//    }
    /** permet d'assurer la cohérence du chapitre cours avec le chapitre exercice **/
    public static void changerChapitre(int chapitre) {
        Onglet.OngletCours cours = ONGLET_TEXTE.COURS.getInstance();
        Onglet.OngletCours exercices = ONGLET_TEXTE.EXERCICE.getInstance();
        boolean operationCanceled = false;
        if(cours.hasBeenModified()) {operationCanceled = !cours.saveChanges();}
        else if(exercices.hasBeenModified()) {operationCanceled = !exercices.saveChanges();}
        if(operationCanceled) {return;}
        cours.setElementCourant(chapitre);
        exercices.setElementCourant(chapitre);
    }

    /** permet d'assurer la cohérence du chapitre cours avec le chapitre exercice **/
    public static void nouveauChapitre(String titre) {
        Onglet.OngletCours cours = ONGLET_TEXTE.COURS.getInstance();
        Onglet.OngletCours exercices = ONGLET_TEXTE.EXERCICE.getInstance();
        if(cours.saveChanges() && exercices.saveChanges()) {
            DataCahier cahierCours = getProfil().getCahier(ONGLET_TEXTE.COURS.getNom());
            int index = cahierCours.getTitres().length;
            cahierCours.addChapitre(titre, cours.genererNouveauContenu(titre, index+1));// +1 car l'index vaut 0 pour le 1er chapitre
            DataCahier cahierExercices = getProfil().getCahier(ONGLET_TEXTE.EXERCICE.getNom());
            cahierExercices.addChapitre(titre, exercices.genererNouveauContenu(titre, index+1));

            cours.setElementCourant(index);
            exercices.setElementCourant(index);
            cours.setModified(true);exercices.setModified(true);
            setOngletActif(getOngletCoursActif());
        }
    }

    private static final class ActionAPropos extends ActionComplete {

        private ActionAPropos() {
            super("about");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DialogueAbout about = new DialogueAbout(interfaceMathEOS.getFenetre(), Configuration.getVersion(), Configuration.getAdresseSite());
            about.setVisible(true);
        }
    }

    private static Action actionLangue;
    private static final class ActionLangue extends ActionComplete {

        private ActionLangue() {
            super("language");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String[] langues = Traducteur.getListeLangues();
            String title = Traducteur.traduire("dialog language title");
            String message = Traducteur.traduire("dialog language message");
            String langue = (String) JOptionPane.showInputDialog(null, title,
                message, JOptionPane.QUESTION_MESSAGE, null,
                langues, // Array of choices
                Configuration.getLangue()); // Initial choice
            if(langue!=null) setLangue(langue);
        }
    }

    private static Action actionTheme;
    private static final class ActionTheme extends ActionComplete {

        private ActionTheme() {
            super("theme");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String[] themes = (new Adresse(Configuration.getDossierThemes())).listeNomFichiers(Adresse.EXTENSION_THEME);
            String title = Traducteur.traduire("dialog theme title");
            String message = Traducteur.traduire("dialog theme message");
            String theme = (String) JOptionPane.showInputDialog(null, title,
                message, JOptionPane.QUESTION_MESSAGE, null,
                themes, // Array of choices
                Configuration.getTheme()); // Initial choice
            if(theme!=null) setTheme(theme);
        }
    }

    private static final class ActionSommaire extends ActionComplete {
        private ActionSommaire() {
            super("contents");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            Onglet.OngletCours onglet = getOngletCoursActif();
            int i = onglet.sommaire();
            if(i>=0) {
                //On synchronise les cahiers cours et exercices
                if(onglet==ONGLET.COURS.getInstance() || onglet==ONGLET.EXERCICE.getInstance()) {
                    changerChapitre(i);
                } else { onglet.setElementCourant(i); }
            }
        }
    }

    private static Action actionConsultation;
    private static final class ActionConsultation extends ActionComplete {
        private ActionConsultation() { super("consultation"); }
        @Override
        public void actionPerformed(ActionEvent e) {
            Onglet.OngletCours onglet = ONGLET_TEXTE.COURS.getInstance();
            int i = onglet.sommaire();
            if(i<0) {return;}
            //chargement du chapitre
            Editeur editeur = new Editeur();
            editeur.charger(getProfil().getCahier(ONGLET_TEXTE.COURS.getNom()).getChapitre(i));
            editeur.setEditable(false);

            //conteneur ScrollPane
            JScrollPane container = new JScrollPane(editeur, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            container.getVerticalScrollBar().setUnitIncrement(100);

            //Fenetre d'affichage
            JFrame consultation = new JFrame();
            consultation.setIconImage(ImageManager.getIcone("applicationIcon").getImage());
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            consultation.setBounds(d.width/2, 20, d.width/2, d.height-40);
            consultation.setContentPane(container);
            consultation.setAlwaysOnTop(true);
            consultation.setVisible(true);
        }
    }

    private static final class ActionDocx extends ActionComplete {
        private ActionDocx() {
            super("export docx");
        }
        private String getDefaultName() {
            return Configuration.getDossierCourant()+File.separatorChar+Traducteur.traduire("chapter")+"."+Adresse.EXTENSION_DOCX;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            //choix du fichier de destination
            File fichier;
            JFileChooser fc = new JFileChooser();
            fc.addChoosableFileFilter(new Adresse.DocxFileFilter());
            fc.setSelectedFile(new File(getDefaultName()));
            int choix = fc.showSaveDialog(interfaceMathEOS.getFenetre());
            if(choix==JFileChooser.APPROVE_OPTION) {
                fichier = fc.getSelectedFile();
                if(!fichier.getPath().endsWith(Adresse.EXTENSION_DOCX)) {fichier = new File(fichier.getAbsolutePath()+"."+Adresse.EXTENSION_DOCX);}
                if(fichier.exists()) {
                    DialogueBloquant.CHOICE decision = DialogueBloquant.dialogueBloquant("dialog file already exists", DialogueBloquant.MESSAGE_TYPE.WARNING, DialogueBloquant.OPTION.YES_NO);
                    if(decision!=DialogueBloquant.CHOICE.YES) { actionPerformed(e); return; }//on recommence
                    fichier.delete();
                }
            } else { return; }
            getOngletCoursActif().export2Docx(fichier);
        }

    }

    private static void interfaceReady() {
        interfaceMathEOS.affiche();
        System.out.println("interface displayed");
        Loading.stop();
        System.out.println("loading stopped");
        interfaceReady = true;
        if(chargementEnAttente) {
            System.out.println("profil changes pending... try to load profil");
            chargementEnAttente = false;
            chargement();
            System.out.println("chargement du profil effectué par l'interface");
        }
    }
    private static void initialize() {
        Loading.setValue(10);

        //prépare les actions
        initializeActions();
        System.out.println("actions initialized");
        
        //écoute l'interface pour mettre à jour les actions
        interfaceMathEOS.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(InterfaceComplete.SAVING_AVAILABLE)) System.out.println("saving available IHM "+evt);
                switch(evt.getPropertyName()) {
                    case InterfaceComplete.INSERT_AVAILABLE : actionInsertion.setEnabled((boolean)evt.getNewValue()); break;
                    case InterfaceComplete.SAVING_AVAILABLE : actionSauvegarde.setEnabled((boolean)evt.getNewValue()); break;
                    case InterfaceComplete.UPDATE_AVAILABLE : actionUpdateTP.setEnabled((boolean)evt.getNewValue()); break;
                }
            }
        });

        //barre outils du bas
        Clock c = new Clock("barre bas");
        initializeBarreBas();c.time();
        System.out.println("barre bas initialized");
        Loading.setValue(20);

        //les parties gauche et droite de l'interface
        c = new Clock("PartieTPCours");
        initializePartieTPCours();c.time();
        System.out.println("partieTPCours initialized");

        //la barre de  menu en haut de l'interface
        c = new Clock("BarreMenu");
        initializeBarreMenu();c.time();
        System.out.println("barre menu initialized");
        Loading.setValue(95);

        //sauvegarde automatique
        interfaceMathEOS.getFenetre().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(saveChanges()) {close();}
//                else {e.getWindow().setVisible(true);}
//                sauvegardeAuto();
            }
        });

        setTaille(EcranPartage.MILIEU);
        interfaceReady();
        System.out.println("interface ready");
    }
    private static void initializeActions() {
        actionClavierNumerique = new ActionClavierNumerique();
        actionClavierSpecial = new ActionClavierCaracteresSpeciaux();
        actionInsertion = new ActionInsertion();
        actionTheme = new ActionTheme();
        actionLangue = new ActionLangue();
        actionSauvegarde = new ActionSauvegarde();
        actionUpdateTP = new ActionUpdateTP();
        actionNouveauTP = new ActionNouveauTP();
        actionCalculatrice = new ActionCalculatrice();
        actionConsultation = new ActionConsultation();
    }
    private static void initializeBarreBas() {
        BarreBas bas = interfaceMathEOS.getBarreBas();
        bas.addBouton(new ActionSommaire(), BarreBas.SOMMAIRE);
        bas.addBouton(new ActionZoomP(), BarreBas.ZOOM_P);
        bas.addBouton(new ActionZoomM(), BarreBas.ZOOM_M);
        bas.addBouton(actionConsultation, BarreBas.CONSULTATION);
        bas.addBouton(actionCalculatrice, BarreBas.CALCULATRICE);
        Bouton b = bas.addBouton(actionClavierNumerique, BarreBas.CLAVIER_NUMERIQUE);
        ((ClavierNumerique)actionClavierNumerique.getClavierWindow()).setPositionReferenceComponent(b);
    }

    private static void initializePartieTPCours() {
        int avancement = 20;
        for(ONGLET_TEXTE onglet : ONGLET_TEXTE.values()) {
            Clock c = new Clock(onglet.name());
            Onglet.OngletCours o;
            switch(onglet) {
                case COURS:o = new OngletCahierDeCours();break;
                case EXERCICE:o = new OngletCahierDExercice();break;
                case EVALUATION:o = new OngletCahierDEvaluation();break;
                default:o=null;
            }
            if(o!=null) {
                onglet.toOnglet().setInstance(o);
                interfaceMathEOS.addOngletCours(onglet.getNom(), o);
                c.time();
            }
            Loading.setValue(avancement+=8);
        }

        for(ONGLET_TP onglet : ONGLET_TP.values()) {
            Clock c = new Clock(onglet.name());
            Onglet.OngletTP o;
            System.out.println(onglet);
            switch(onglet) {
                case FONCTION:o = new OngletFonctions();break;
//                case TABLEAUX:o = new OngletTableaux();break;
                case TABLEAUX:o = new OngletTable();break;
                case OPERATIONS:o = new OngletOperations();break;
                case GEOMETRIE:o = new OngletGeometrie();break;
                default:o=null;
            }
            if(o!=null) {
                onglet.toOnglet().setInstance(o);
                interfaceMathEOS.addOngletTP(onglet.getNom(), o);
                c.time();
            }
            Loading.setValue(avancement+=8);
        }
/*
        //FIXME revoir la gestion manuelle de l'avancement
        int avancement = 20;
        for(ONGLET onglet : ONGLET.values()) {
            Clock c = new Clock(onglet.nom);
            if(onglet.nom!=null) {
                if(onglet != onglet.TABLEAUX){
                if(onglet.getIndex()!=0) {
                    new OngletInitializer(onglet).run();//les onglets non visibles sont chargés en fond
                } else {
                    new OngletInitializer(onglet).run();
                }}
            }
            Loading.setValue(avancement+=8);
            c.time();
        }
 */
    }

    private IHM() {throw new AssertionError("instantiting utilitary class");}
    
}