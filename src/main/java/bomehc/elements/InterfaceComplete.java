/** «Copyright 2011,2013 François Billioud»
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

package bomehc.elements;

import bomehc.IHM;
import static bomehc.elements.EcranPartage.COURS;
import static bomehc.elements.EcranPartage.TP;
import bomehc.elements.Onglet.OngletCours;
import bomehc.elements.Onglet.OngletTP;
import bomehc.utils.boutons.ActionComplete;
import bomehc.utils.interfaces.Editable;
import bomehc.utils.interfaces.Undoable;
import bomehc.utils.managers.ColorManager;
import bomehc.utils.managers.CursorManager;
import bomehc.utils.managers.FontManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.SwingPropertyChangeSupport;
import bomehc.texte.composants.JLabelTP;
import bomehc.utils.interfaces.ComponentInsertionListener;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class InterfaceComplete {

    //Constantes
    public static final int GAUCHE = EcranPartage.GAUCHE;
    public static final int MILIEU = EcranPartage.MILIEU;
    public static final int DROIT = EcranPartage.DROIT;
    
    //constantes utilises pour les changePropertyListeners
    public static final String UPDATE_AVAILABLE = "updateAvailable";
    public static final String SAVING_AVAILABLE = "savingAvailable";
    public static final String INSERT_AVAILABLE = "insertAvailable";

    //Constantes utilisées pour le Laf
    private final Font TOOLTIP_FONT = FontManager.get("font tooltip", Font.PLAIN);
    private final Font OPTION_PANE_MESSAGE_FONT = FontManager.get("font optionPane message", Font.PLAIN);
    private final Font OPTION_PANE_BUTTON_FONT = FontManager.get("font optionPane boutons", Font.PLAIN);

    /** Variable contenant la fenêtre complète **/
    private final Fenetre fenetre;

    //interface
    private final BarreMenu barreMenu;
    private BarreOutils barreOutils;
    private final BarreBas barreBas;
    private final EcranPartage ecran;
    
    /** Ecoute les changments d'onglet et adapte l'interface en conséquent **/
    private final ChangeListener ongletChangeListener = new OngletChangeListener();
    private Onglet ongletActif;
    private final InterfaceListener interfaceListener = new InterfaceListener();

    /** Prend une photo de l'ongletTP actif **/
    private String photoTP;
    public String getPhotoTP() {return photoTP;}

    /** Sauvegarde le dernier objet éditable ayant reçu le focus **/
    private Editable lastEditable = null;

    /** Sauvegarde le dernier objet éditable ayant reçu le focus **/
    private Undoable lastUndoable = null;

    /** Ecoute les changements parmi les actions possibles (undo, redo, annuler, refaire...) **/
    private final EditableStateListener editListener = new EditableStateListener();
    private final UndoableStateListener undoListener = new UndoableStateListener();
    
    //actions de contrôle globales
    private final Action annuler = new ActionAnnuler();
    private final Action refaire = new ActionRefaire();
    private final Action couper = new ActionCouper();
    private final Action copier = new ActionCopier();
    private final Action coller = new ActionColler();

    /**
     * Crée un interface de base en positionnant les éléments :
     * une barre menu, un panel au Nord, un EcranPartage (JSplitPane) au Centre
     * et une BarreBas (JPanel) au Sud
     */
    public InterfaceComplete(String laf) {
        chargerLaF(laf);
        fenetre = new Fenetre();

        barreMenu = new BarreMenu();
        barreOutils = new BarreOutils();
        barreBas = new BarreBas();
        ecran = new EcranPartage();

        miseEnPlaceElements();
        initKeyboardManager();  //écoute les changements de focus sur les éléments Editable et Undoable
        initClipboardListener();//écoute les changements de contenu du Clipboard
        changeCursors();
    }
    
    private void chargerLaF(String LaF) {

        //charge le L&F
        UIManager.LookAndFeelInfo plafinfo[] = UIManager.getInstalledLookAndFeels();
        boolean LaFfound=false;
        int LaFindex=0;

        for (int look = 0; look < plafinfo.length && !LaFfound; look++)
        {//récupère le look and feels lu dans le fichie de thème
            if(plafinfo[look].getClassName().toLowerCase().contains(LaF))
            {
                LaFfound=true;
                LaFindex=look;
            }
        }

        try {
            if(LaFfound) {
                UIManager.setLookAndFeel(plafinfo[LaFindex].getClassName());
            }
            else {UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());}
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e){
            Logger.getLogger(Fenetre.class.getName()).log(Level.SEVERE, null, e);
        }
        UIManager.getLookAndFeel().getDefaults().put("ToolTip.font", TOOLTIP_FONT);
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(2000);
        UIManager.put("ToolTip.font", TOOLTIP_FONT);
        UIManager.put("OptionPane.messageFont", OPTION_PANE_MESSAGE_FONT);
        UIManager.put("OptionPane.buttonFont", OPTION_PANE_BUTTON_FONT);
        String[] lafParameters = IHM.getThemeElementBloc("laf");
        for(String s : lafParameters) {
            String[] T = s.split("::");
            if(T.length==2) UIManager.put(T[0], ColorManager.getColorFromHexa(T[1]));
        }
//        UIManager.put("nimbusFocus", Color.RED);//couleur du cadre autour de l'élément porteur du focus
//        UIManager.put("nimbusInfoBlue", Color.RED);
//        UIManager.put("control", new Color(255,128,0));//couleur de l'arriere du splitPane
//        UIManager.put("nimbusBase", new Color(200,130,0));//Couleur générale des JTabbedPane
//        UIManager.put("nimbusBlueGrey", new Color((float)0.5,(float)0.5,(float)1.,(float)0.5));//Couleur générale des fonds
//        UIManager.put("nimbusBlueGrey", new Color(255,255,255));//Couleur générale des fonds
//        UIManager.put("nimbusSelection", new Color(200,0,00));//couleur du menu selectionné
//        UIManager.put("nimbusSelectionBackground", new Color(200,200,00));//Couleur de l'item comboBox survolé
//        UIManager.put("nimbusLightBackground", new Color(0,200,00));//Couleur du fond des champs de texte
//        UIManager.put("textForeground", new Color(0,200,00));//Couleur des traits
//        UIManager.put("TabbedPane.background", new Color(0,200,00));
    }
    
    private void changeCursors() {
        fenetre.setCursor(CursorManager.getCursor(Cursor.DEFAULT_CURSOR));
        ecran.getComponent(0).setCursor(CursorManager.getCursor(Cursor.E_RESIZE_CURSOR));
    }

    public Fenetre getFenetre() {
        return fenetre;
    }

    private void miseEnPlaceElements() {
        //prépare la barre de menu
        fenetre.setJMenuBar(barreMenu);//ajoute la barre de menu à la fenêtre

        //ajoute les éléments de l'interface
        fenetre.getContentPane().add(barreOutils,BorderLayout.NORTH);//ajoute la barre outils du haut à la fenêtre
        fenetre.getContentPane().add(barreBas,BorderLayout.SOUTH);//ajoute la barre outils du bas à la fenêtre
        fenetre.getContentPane().add(ecran,BorderLayout.CENTER);//ajoute toute la partie centrale
        barreOutils.revalidate();

        //gère les changement d'onglet
        ecran.getPartie(EcranPartage.COURS).addChangeListener(ongletChangeListener);
        ecran.getPartie(EcranPartage.COURS).addChangeListener(new ChangeListener() {
            //Met à jour l'état des boutons update et insertion si nécessaire
            @Override
            public void stateChanged(ChangeEvent e) {
                OngletTP ongletTP = getOngletTPActif();
                OngletCours ongletCours = (OngletCours)(((JTabbedPane)e.getSource()).getSelectedComponent());
                if(ongletTP==null || ongletCours==null) {return;}
                setUpdateAvailable(ongletTP.hasBeenModified() && ongletCours.getTP(ongletTP.getIdTP())!=null);//On vérifie que le tp est bien dans l'éditeur pour l'update
                setInsertAvailable(!ongletCours.isNouveauCahier());//On empêche les insertions dans un cahier vierge (ie sans chapitre)
            }
        });
        ecran.getPartie(EcranPartage.TP).addChangeListener(ongletChangeListener);
        ecran.getPartie(EcranPartage.TP).addChangeListener(new ChangeListener() {
            //Met à jour l'état des boutons update et insertion si nécessaire
            @Override
            public void stateChanged(ChangeEvent e) {
                OngletTP ongletTP = (OngletTP)(((JTabbedPane)e.getSource()).getSelectedComponent());
                OngletCours ongletCours = getOngletCoursActif();
                if(ongletTP==null || ongletCours==null) {return;}
                setUpdateAvailable(ongletTP.hasBeenModified() && ongletCours.getTP(ongletTP.getIdTP())!=null);//On vérifie que le tp est bien dans l'éditeur pour l'update
                //INSERTION_STATE : Décommenter la ligne ci-dessous pour désactiver l'insertion lorsqu'aucune modification n'a été faite
                //setInsertAvailable(onglet.hasBeenModified());
            }
        });

        //boutons
        barreMenu.addBouton(couper);
        barreMenu.addBouton(copier);
        barreMenu.addBouton(coller);
        barreMenu.addBouton(annuler);
        barreMenu.addBouton(refaire);
        //edition
        barreMenu.addElement(couper, BarreMenu.EDITION);
        barreMenu.addElement(copier, BarreMenu.EDITION);
        barreMenu.addElement(coller, BarreMenu.EDITION);
        barreMenu.addElement(annuler, BarreMenu.EDITION);
        barreMenu.addElement(refaire, BarreMenu.EDITION);

        fenetre.validate();
        fenetre.repaint();
    }
    
    private void changeBarreOutils(BarreOutils barre) {
        //change la barre outils
        fenetre.getContentPane().remove(barreOutils);
        fenetre.getContentPane().add(barre,BorderLayout.NORTH);

        barreOutils = barre;
    }

    /** redéfinit l'interface pour l'onglet spécifié **/
    private void activeOnglet(Onglet o) {
        if(ongletActif==o) {return;}
        if(ongletActif!=null) {
            ongletActif.removePropertyChangeListener(interfaceListener);
            ongletActif.activer(false);
        }
        changeBarreOutils(o.getBarreOutils());
        changeMenuOptions(o.getMenuOptions());
        activeAnnuler(o.peutAnnuler());
        activeRefaire(o.peutRefaire());
        if(o instanceof OngletCours) { setInsertAvailable(!((OngletCours)o).isNouveauCahier()); }
        
        ongletActif = o;
        ongletActif.activer(true);
        ongletActif.addPropertyChangeListener(interfaceListener);
        
    }

    private void changeMenuOptions(BarreMenu.Menu menuOptions) {
        barreMenu.changeMenuOptions(menuOptions);
    }

    /** gère complètement un changement de mode **/
    //TODO : passer par des listener plutôt que l'IHM, comme pour le changement d'onglet
    public void activeMode(boolean newMode) {
        if(newMode!=getMode()) {
            if(getOngletActif() instanceof OngletTP) { photoTP = ((OngletTP)getOngletActif()).capturerImage(); }
            ecran.setMode(newMode);
            barreBas.setMode(newMode);
            activeOnglet(ecran.getOngletActif());
            setTaille(MILIEU);
        }
    }

    /** force un onglet à devenir actif. Change le mode si nécessaire **/
    public void setOngletActif(Onglet onglet) {
        if (!isFromSameMode(onglet, getMode())) {
            activeMode(!getMode());
        }
        ecran.setOngletSelected(onglet);
        activeOnglet(onglet);
    }

    private boolean isFromSameMode(Onglet onglet, boolean mode) {
        return (mode == COURS ? OngletCours.class.isInstance(onglet) : OngletTP.class.isInstance(onglet));
    }
    
    public void setOngletCoursEnabled(int index, boolean enable) {
        ecran.getPartie(COURS).setEnabledAt(index, enable);
    }
    
    public void setOngletTPEnabled(int index, boolean enable) {
        ecran.getPartie(TP).setEnabledAt(index, enable);
    }
    
    /** Change la position de la barre de séparation centrale (GAUCHE, DROIT, MILIEU) **/
    public void setTaille(int taille) {
        barreBas.removeBouton(BarreBas.CORRESPONDANCES.get(getTaille()));
        boolean coteFleche;
        switch(taille) {
            case GAUCHE: coteFleche = ActionFleche.DROITE; break;
            case DROIT: coteFleche = ActionFleche.GAUCHE; break;
            case MILIEU: coteFleche = (getMode()==EcranPartage.COURS ? ActionFleche.DROITE : ActionFleche.GAUCHE); break;
            default: coteFleche = ActionFleche.DROITE;
        }
        barreBas.addBouton(new ActionFleche(coteFleche),BarreBas.CORRESPONDANCES.get(taille));
        ecran.setTaille(taille);
    }

    private int getTaille() { return ecran.getTaille(); }
    public boolean getMode() { return ecran.getMode(); }
    public BarreOutils getBarreOutils() { return barreOutils; }
    public BarreBas getBarreBas() { return barreBas; }
    public BarreMenu getBarreMenu() { return barreMenu; }
    public OngletCours getOngletCoursActif() { return ecran.getOngletCoursActif(); }
    public OngletCours getOngletCours(int index) { return ecran.getOngletCours(index); }
    public OngletTP getOngletTPActif() { return ecran.getOngletTPActif(); }
    public OngletTP getOngletTP(int index) { return ecran.getOngletTP(index); }
    public Onglet getOngletActif() { return ongletActif; }
//    public SizeManager getSizeManager() {return fenetre.getSizeManager(); }

    public void addOngletCours(String nom, OngletCours onglet) {
        ecran.getPartie(EcranPartage.COURS).removeChangeListener(ongletChangeListener);
        ecran.addOngletCours(nom, onglet);
        onglet.activer(false);//onglets désactivés par défaut
        ecran.getPartie(EcranPartage.COURS).addChangeListener(ongletChangeListener);

        //Gestion du bouton de sauvegarde de tous les onglets
        onglet.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                switch (evt.getPropertyName()) {
                    case Undoable.MODIFIED:
                        //On permet l'enregistrement global dès qu'un des onglets est modifié
                        boolean modified = (boolean)evt.getNewValue();
                        setSavingAvailable(modified || isAnyChange());
                        break;
                    case OngletCours.NOUVEAU_CAHIER:
                        if(((OngletCours)evt.getSource())==getOngletCoursActif()) {setInsertAvailable(!(boolean)evt.getNewValue());}
                        break;
                }
            }
        });
        //Gestion du bouton de mise à jour TP lors de la suppression d'un tp depuis l'éditeur
        onglet.addComponentInsertionListener(new ComponentInsertionListener() {
            @Override
            public void componentInserted(Component c) {}
            @Override
            public void componentRemoved(Component c) {
                if(!(c instanceof JLabelTP)) {return;}
                JLabelTP tp = (JLabelTP) c;
                if(tp.getId()==getOngletTPActif().getIdTP()) {
                    setUpdateAvailable(false);
                }
            }
        });
    }

    /** Pour le bouton saveAll, permet de vérifier si l'un des onglets présente des modifs **/
    private boolean isAnyChange() {
        for(Component onglet : ecran.getPartie(COURS).getComponents()) {
            try {
                if(((OngletCours)onglet).hasBeenModified()) {return true;}
            } catch(ClassCastException e) {
                System.out.println(onglet);
            }
        }
        return false;
    }

    public void addOngletTP(String nom, final OngletTP onglet) {
        ecran.getPartie(EcranPartage.TP).removeChangeListener(ongletChangeListener);
        ecran.addOngletTP(nom, onglet);
        onglet.activer(false);//onglets désactivés par défaut
        ecran.getPartie(EcranPartage.TP).addChangeListener(ongletChangeListener);
        
        //Gestion des boutons updateTP et insertion
        onglet.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(Undoable.MODIFIED)) {
                    //INSERTION_STATE : Décommenter la ligne ci-dessous pour désactiver l'insertion lorsqu'aucune modification n'a été faite
                    //setInsertAvailable((boolean)evt.getNewValue());//insertion available
                    setUpdateAvailable((boolean)evt.getNewValue() && onglet.getIdTP()!=0);//update available
                }
            }
        });
    }

    public void affiche() { fenetre.setVisible(true); }
    public void masque() { fenetre.setVisible(false); }

    private void initKeyboardManager() {
        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("focusOwner") && (e.getNewValue() != null)) {
                        if(e.getNewValue() instanceof Editable) { setActiveEditable((Editable) e.getNewValue()); }
                        if(e.getNewValue() instanceof Undoable) { setActiveUndoable((Undoable) e.getNewValue()); }
                    }
                }
            }
        );
    }

    private void initClipboardListener() {
        Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(new FlavorListener() {
            @Override
            synchronized public void flavorsChanged(FlavorEvent e) {
                try {
                    DataFlavor[] T = Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors();
                    activeColler(T.length>0);
                } catch (HeadlessException ex) {
                    try {//HACK Le clipboard peut parfois prendre quelque millisecondes pour être prêt. 200ms de pause semblent suffir pour régler le pb.
                        wait(200);
                        DataFlavor[] T = Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors();
                        activeColler(T.length>0);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(InterfaceComplete.class.getName()).log(Level.SEVERE, null, ex1);
                    } catch (IllegalStateException ex2) {
                        System.out.println("Impossible de lire le clipboard");
                        Logger.getLogger(InterfaceComplete.class.getName()).log(Level.SEVERE, null, ex2);
                    }
                }
                //activeColler(TransferableTools.isContentAccepted(T));
            }
        });
    }
    
    /** définit le composant Editable actif pour l'interface */
    private void setActiveEditable(Editable editable) {
        if(lastEditable!=null) {lastEditable.removePropertyChangeListener(editListener);}
        lastEditable = editable;
        activeCouper(editable.peutCouper());
        activeCopier(editable.peutCopier());
        activeColler(editable.peutColler());
        lastEditable.addPropertyChangeListener(editListener);
    }
    /** définit le composant Undoable actif pour l'interface */
    private void setActiveUndoable(Undoable undoable) {
        if(lastUndoable!=null) {lastUndoable.removePropertyChangeListener(undoListener);}
        lastUndoable = undoable;
        activeAnnuler(undoable.peutAnnuler());
        activeRefaire(undoable.peutRefaire());
        lastUndoable.addPropertyChangeListener(undoListener);
    }

    /** Ecoute les changements de barre outils et de menu options et adapte l'interface si besoin **/
    private class InterfaceListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case Onglet.BARRE_OUTILS:
                    InterfaceComplete.this.changeBarreOutils((BarreOutils) evt.getNewValue());
                    break;
                case Onglet.MENU_OPTIONS:
                    InterfaceComplete.this.changeMenuOptions((BarreMenu.Menu) evt.getNewValue());
                    break;
            }
        }
    }
    
    /** Assure la cohérence entre l'onglet actif de l'interface et l'onglet
     * actif dans la partie centrale
     */
    private class OngletChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            setOngletActif((Onglet)((JTabbedPane)e.getSource()).getSelectedComponent());
        }
    }

    private class ActionFleche extends ActionComplete {
        public static final boolean GAUCHE = true;
        public static final boolean DROITE = false;

        private final boolean sens;
        private ActionFleche(boolean sens) {
            super(sens==GAUCHE ? "left arrow" : "right arrow");
            this.sens = sens;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            setTaille(getTaille()!=MILIEU ? MILIEU : (sens==GAUCHE ? InterfaceComplete.GAUCHE : InterfaceComplete.DROIT));
        }
    }

    private class ActionAnnuler extends ActionComplete {
        private ActionAnnuler() {super("undo");}
        @Override
        public void actionPerformed(ActionEvent e) {
            if(lastUndoable==null) {return;}
            if(lastUndoable instanceof Component) {((Component)lastUndoable).requestFocus();}
            lastUndoable.annuler();
            activeRefaire(true);
            if(!lastUndoable.peutAnnuler()) { activeAnnuler(false); }
        }
    }
    private class ActionRefaire extends ActionComplete {
        private ActionRefaire() {super("redo");}
        @Override
        public void actionPerformed(ActionEvent e) {
            if(lastUndoable==null) {return;}
            if(lastUndoable instanceof Component) {((Component)lastUndoable).requestFocus();}
            lastUndoable.refaire();
            activeAnnuler(true);
            if(!lastUndoable.peutRefaire()) { activeRefaire(false); }
        }
    }

    private class ActionCouper extends ActionComplete {
        private ActionCouper() {super("cut");}
        @Override
        public void actionPerformed(ActionEvent e) {
            if(lastEditable==null) {return;}
            if(lastEditable instanceof Component) {((Component)lastUndoable).requestFocus();}
            lastEditable.couper();
            activeColler(true);
        }
    }
    private class ActionCopier extends ActionComplete {
        private ActionCopier() {super("copy");}
        @Override
        public void actionPerformed(ActionEvent e) {
            if(lastEditable==null) {return;}
            if(lastEditable instanceof Component) {((Component)lastUndoable).requestFocus();}
            lastEditable.copier();
            activeColler(true);
        }
    }
    private class ActionColler extends ActionComplete {
        private ActionColler() {super("paste");}
        @Override
        public void actionPerformed(ActionEvent e) {
            if(lastEditable==null) {return;}
            if(lastEditable instanceof Component) {((Component)lastUndoable).requestFocus();}
            lastEditable.coller();
        }
    }

    private void activeAnnuler(boolean b) {
        annuler.setEnabled(b);
    }
    private void activeRefaire(boolean b) {
        refaire.setEnabled(b);
    }
    private void activeCouper(boolean b) {
        couper.setEnabled(b);
    }
    private void activeCopier(boolean b) {
        copier.setEnabled(b);
    }
    private void activeColler(boolean b) {
        coller.setEnabled(b);
    }
    
    /** Ecoute les changements de propriété des objets Editable (peutCouper, peutCopier, peutColler) **/
    private class EditableStateListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case Editable.PEUT_COUPER:
                    activeCouper((Boolean)evt.getNewValue());
                    break;
                case Editable.PEUT_COPIER:
                    activeCopier((Boolean)evt.getNewValue());
                    break;
                case Editable.PEUT_COLLER:
                    activeColler((Boolean)evt.getNewValue());
                    break;
            }
        }
    }
    
    /** Ecoute les changements de propriété des objets Undoable (peutAnnuler, peutRefaire, hasBeenModified) **/
    private class UndoableStateListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case Undoable.PEUT_ANNULER:
                    activeAnnuler((Boolean)evt.getNewValue());
                    break;
                case Undoable.PEUT_REFAIRE:
                    activeRefaire((Boolean)evt.getNewValue());
                    break;
                case Undoable.MODIFIED:
                    
                    break;
            }
        }
    }

    private void updateORinsertTP(long idTP) {
        Onglet.OngletTP onglet = getOngletTPActif();
        if(getMode()==EcranPartage.TP) {photoTP = onglet.capturerImage();}//permet d'éviter que l'image soit grisée
        Dimension d = onglet.getInsertionSize();
        long id = getOngletCoursActif().insertion(idTP, onglet.getNomTP(), onglet.getDonnees(), photoTP, (int)(d.getWidth()*0.7), (int)(d.getHeight()*0.7));//la taille originale est trop grande
        if (id != 0) {//Si l'insertion s'est bien déroulée, on prévient l'ongletTP du nouvel id qui lui a été attribué
            onglet.setIdTP(id);
            onglet.setModified(false);
        }
    }
    /**
     * Méthode qui récupère les infos du TP actifs pour les donner au Cours actif afin de faire une insertion
     */
    public void insererTP() {
        updateORinsertTP(0);
    }
    /**
     * Méthode qui récupère les infos du TP actifs pour les donner au Cours actif afin de mettre à jour le TP
     */
    public void updateTP() {
        Onglet.OngletTP onglet = getOngletTPActif();
        //PENDING
        //A supprimer lorsque le clonage des Opérations sera fonctionel
        // Cela sert seulement à empêcher que la même opération soit insérée deux fois
//        if (onglet.getNomTP().equals("operation") && onglet.getIdTP() != 0) {
//            JOptionPane.showMessageDialog(getFenetre(), "Cette opération a déjà été insérée");
//            return;
//        }
        //Fin suppression
        updateORinsertTP(onglet.getIdTP());
    }
    
    //Support des propertyChange
    private final SwingPropertyChangeSupport changeSupport = new SwingPropertyChangeSupport(this, true);
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    private boolean updateAvailable = false;
    private boolean savingAvailable = false;
    private boolean insertAvailable = true;
    private void setUpdateAvailable(boolean b) {
        if(updateAvailable==b) {return;}
        firePropertyChange(UPDATE_AVAILABLE, !b, b);
        updateAvailable = b;
    }
    private void setSavingAvailable(boolean b) {
        if(savingAvailable==b) {return;}
        firePropertyChange(SAVING_AVAILABLE, !b, b);
        savingAvailable = b;
    }
    private void setInsertAvailable(boolean b) {
        if(insertAvailable==b) {return;}
        firePropertyChange(INSERT_AVAILABLE, !b, b);
        insertAvailable = b;
    }
}
