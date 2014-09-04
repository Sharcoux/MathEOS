package matheos.tableau;

import matheos.json.Json;
import static matheos.tableau.TableConstants.COULEUR_CASE;
import static matheos.tableau.TableConstants.DEFAULT_COULEUR_CASE;
import static matheos.tableau.ControlleurTableau.MODE.*;
import matheos.tableau.TableConstants.ETAT;
import matheos.tableau.TableConstants.FIRST_CASE;
import matheos.tableau.TableConstants.ORIENTATIONS;
import matheos.tableau.TableConstants.TYPE_CASES;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataObject;
import matheos.sauvegarde.DataTexte;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.ActionGroup;
import matheos.utils.interfaces.Undoable;
import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.texte.EditeurIO;
import matheos.utils.texte.JMathTextPane;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;

import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.undo.UndoableEdit;

public class ControlleurTableau implements CellEditorListener, TableModelListener, Undoable {

    //nom des données
    public static final String MODELE_TABLEAU = "modele tableau";
    public static final String MODELE_FLECHES = "modele fleches";

    private final OngletTableaux ongletTableaux;
    private static UndoManagerTableau edits;
    private double coef;
    private Tableau tableau;
    private SupportPanel supportPanel;
    private final ModeleFleches modeleFleches;
    private final ColorationCelluleListener colorationMouseListener = new ColorationCelluleListener();
    private ChampTextListener champTextListener;
    private boolean modified;

    public ControlleurTableau(OngletTableaux ongletTableaux) {
        this.ongletTableaux = ongletTableaux;
        modeleFleches = new ModeleFleches();
    }

    /**
     * @return the tableau
     */
    public Tableau getTableau() {
        return tableau;
    }

    public double getCoef() {
        return coef;
    }

    public void setCoef(double coef) {
        this.coef = coef;
    }

    public boolean hasBeenModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        if(this.modified==modified) {return;}
        firePropertyChange(Undoable.MODIFIED, this.modified, modified);
        this.modified = modified;
    }

    public SupportPanel nouveau(int nbLigne, int nbColonne) {
        setMode(NORMAL);
        
        modeleFleches.clear();
        champTextListener = new ChampTextListener();
        setModified(false);

        tableau = new Tableau(this, nbLigne, nbColonne);
        edits = new UndoManagerTableau(tableau.getModel(), modeleFleches);
        PanelOrientation.setTableau(tableau);

        supportPanel = new SupportPanel(tableau);
        supportPanel.addModeleFlechesListener(modeleFleches);
        return supportPanel;
    }

    public void addEdit(UndoableEdit anEdit) {
        if(!peutAnnuler()) { firePropertyChange(Undoable.PEUT_ANNULER, false, true); }
        edits.addEdit(anEdit);
        setModified(true);
    }

    public void stopEdition() {
        supportPanel.changeMode(ETAT.NORMAL, null);
    }

    public void ajouterLigne(int index) {
        tableau.getModel().ajouterLigne(index);
        modeleFleches.ajouterCases(TYPE_CASES.ROW, index);
        tableau.clearSelection();
        addEdit(new UndoManagerTableau.AddEdit(TYPE_CASES.ROW, index));
        //adapterDimensions();
    }

    public void ajouterColonne(int index) {
        tableau.getModel().ajouterColonne(index);
        modeleFleches.ajouterCases(TYPE_CASES.COLUMN, index);
        tableau.clearSelection();
        addEdit(new UndoManagerTableau.AddEdit(TYPE_CASES.COLUMN, index));
        //adapterDimensions();
    }

    public void supprimerLigne(int index) {
        List<ModeleCellule> celluleRemoved = tableau.getModel().supprimerLigne(index);
        ModeleFleches flechesRemoved = modeleFleches.supprimerCases(TYPE_CASES.ROW, index);
        addEdit(new UndoManagerTableau.RemoveEdit(TYPE_CASES.ROW, index, celluleRemoved, flechesRemoved));
        tableau.clearSelection();
        //adapterDimensions();
    }

    public void supprimerColonne(int index) {
        List<ModeleCellule> celluleRemoved = tableau.getModel().supprimerColonne(index);
        ModeleFleches flechesRemoved = modeleFleches.supprimerCases(TYPE_CASES.COLUMN, index);
        addEdit(new UndoManagerTableau.RemoveEdit(TYPE_CASES.COLUMN, index, celluleRemoved, flechesRemoved));
        tableau.clearSelection();
        //adapterDimensions();
    }

    public void colorerCases(TYPE_CASES typeCases, int position) {
        List<Color> oldCouleurs = tableau.getModel().getCouleursCases(typeCases, position);
        switch (typeCases) {
            case COLUMN:
                tableau.getModel().colorerColonne(position, COULEUR_CASE);
                addEdit(new UndoManagerTableau.ColorateEdit(TYPE_CASES.COLUMN, position, COULEUR_CASE, oldCouleurs));
                break;
            case ROW:
                tableau.getModel().colorerLigne(position, COULEUR_CASE);
                addEdit(new UndoManagerTableau.ColorateEdit(TYPE_CASES.ROW, position, COULEUR_CASE, oldCouleurs));
                break;
        }
        tableau.clearSelection();
        //adapterDimensions();
    }

    public void decolorerCases(TYPE_CASES typeCases, int position) {
        Color oldCouleur;
        switch (typeCases) {
            case COLUMN:
                if (tableau.getFirstCase().equals(FIRST_CASE.NOT_VISIBLE)) {
                    oldCouleur = tableau.getModel().getCouleurCase(1, position);
                } else {
                    oldCouleur = tableau.getModel().getCouleurCase(0, position);
                }
                tableau.getModel().decolorerColonne(position);
                addEdit(new UndoManagerTableau.DecolorateEdit(TYPE_CASES.COLUMN, position, oldCouleur));
                break;
            case ROW:
                if (tableau.getFirstCase().equals(FIRST_CASE.NOT_VISIBLE)) {
                    oldCouleur = tableau.getModel().getCouleurCase(position, 1);
                } else {
                    oldCouleur = tableau.getModel().getCouleurCase(position, 0);
                }
                tableau.getModel().decolorerLigne(position);
                addEdit(new UndoManagerTableau.DecolorateEdit(TYPE_CASES.ROW, position, oldCouleur));
                break;
        }
        tableau.clearSelection();
        //adapterDimensions();
    }

    public boolean isColorated(TYPE_CASES typeCases, int position) {
        switch (typeCases) {
            case COLUMN:
                return tableau.getModel().isColoratedColonne(position);
            case ROW:
                return tableau.getModel().isColoratedLigne(position);
        }
        return false;
    }

    public void creerFlecheProportionnalite(ORIENTATIONS orientation, int indexDebut, int indexFin) {
        DataFleche modeleFleche = new DataFleche(orientation, indexDebut, indexFin);
        modeleFleches.add(modeleFleche);
        addEdit(new UndoManagerTableau.AddFlecheEdit(modeleFleche));
        supportPanel.checkChampProportionnaliteListener(champTextListener);
        adapterDimensions();
    }

    public void supprimerFlecheProportionnalite(FlechePanel cle) {
        DataFleche modeleFleche = cle.getModeleFleche();
        modeleFleches.remove(modeleFleche);
        addEdit(new UndoManagerTableau.RemoveFlecheEdit(modeleFleche));
        adapterDimensions();
    }

    public ModeleFleches getFleches(ORIENTATIONS orientation) {
        return modeleFleches.getFleches(orientation);
    }

    public ModeleFleches getFlechesLieesAUneCase(TYPE_CASES typeCase, int index) {
        return modeleFleches.getFlechesLieesAUneCase(typeCase, index);
    }

    private void normalMode() {
        actionNormal.setSelected(true);
        supportPanel.changeMode(ETAT.NORMAL, null);
        //tableau.clearSelection();
        tableau.removeMouseListener(colorationMouseListener);
        adapterDimensions();
    }

    /**
     * Passe en mode "ajouter" : les panelsBoutons disparaissent, et seuls les
     * panelsBoutonsAjouter s'affichent.
     */
    private void ajouterMode() {
        actionAjouter.setSelected(true);
        tableau.clearSelection();
        tableau.removeMouseListener(colorationMouseListener);
        ActionTableau ajout = new ActionTableau.ActionAjouter(this);
        supportPanel.changeMode(ETAT.AJOUTER, ajout);
        adapterDimensions();
    }

    /**
     * Passe en mode "supprimer" : les panelsBoutons disparaissent, et seuls les
     * panelsBoutonsSupprimer s'affichent.
     */
    private void supprimerMode() {
        actionSupprimer.setSelected(true);
        tableau.clearSelection();
        tableau.removeMouseListener(colorationMouseListener);
        ActionTableau suppression = new ActionTableau.ActionSupprimer(this);
        supportPanel.changeMode(ETAT.SUPPRIMER, suppression);
        adapterDimensions();
    }

    /**
     * Passe en mode "colorer" : les panelsBoutons disparaissent, et seuls les
     * panelsBoutonsColorer s'affichent.
     */
    private void colorerMode() {
        actionColorer.setSelected(true);
        tableau.clearSelection();
        tableau.addMouseListener(colorationMouseListener);
        ActionTableau colorer = new ActionTableau.ActionColorer(this);
        supportPanel.changeMode(ETAT.COLORER, colorer);
        adapterDimensions();
    }

    private void ajouterFlechesMode() {
        actionAjouterFleches.setSelected(true);
        tableau.clearSelection();
        tableau.removeMouseListener(colorationMouseListener);
        ActionTableau ajoutFleche = new ActionTableau.ActionAjouterFleche(this);
        supportPanel.changeMode(ETAT.AJOUTER_FLECHES, ajoutFleche);
        adapterDimensions();
    }

    private void supprimerFlechesMode() {
        actionSupprimerFleches.setSelected(true);
        tableau.clearSelection();
        tableau.removeMouseListener(colorationMouseListener);
        ActionTableau supprimerFleche = new ActionTableau.ActionSupprimerFleche(this);
        supportPanel.changeMode(ETAT.SUPPRIMER_FLECHES, supprimerFleche);
        adapterDimensions();
    }

    public void adapterDimensions() {
        this.coef = calculCoef();
        ongletTableaux.adapterDimensions(this.coef);
        tableau.clearSelection();
    }

    private double calculCoef() {
        return ongletTableaux.calculCoef();
    }

    /**
     * Permet d'adapter les dimensions de l'Onglet lorsque le tableau change,
     * c'est à dire lorsque l'on ajoute ou supprime une ligne ou une colonne, ou
     * lorsqu'on effectue un couper/coller
     *
     * @param e
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        adapterDimensions();
    }

    /**
     * Permet de passer en mode normal et d'adapter les dimensions de l'Onglet
     * lorsqu'on a terminé l'édition d'une case du tableau.
     *
     * @param e
     */
    @Override
    public void editingStopped(ChangeEvent e) {
        setMode(NORMAL);
        ongletTableaux.requestFocusInWindow();
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
    }

    public void annuler() {
        if (peutAnnuler()) {
            if(!peutRefaire()) { firePropertyChange(Undoable.PEUT_REFAIRE, false, true); }
            edits.undo();
            setModified(true);
            if(!peutAnnuler()) { firePropertyChange(Undoable.PEUT_ANNULER, true, false); }
            adapterDimensions();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public void refaire() {
        if (peutRefaire()) {
            if(!peutAnnuler()) { firePropertyChange(Undoable.PEUT_ANNULER, false, true); }
            edits.redo();
            setModified(true);
            if(!peutRefaire()) { firePropertyChange(Undoable.PEUT_REFAIRE, true, false); }
            adapterDimensions();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public boolean peutAnnuler() {
        return edits.canUndo();
    }

    public boolean peutRefaire() {
        return edits.canRedo();
    }
    
    PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    private void firePropertyChange(String property, boolean oldValue, boolean newValue) {
        changeSupport.firePropertyChange(property, oldValue, newValue);
    }
    
    private void firePropertyChange(String property, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(property, oldValue, newValue);
    }
    
    /**
     * Permet de gérer la coloration d'une case individuel lorsqu'on clique
     * dessus dans le mode "Couleur".
     */
    private class ColorationCelluleListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                return;
            }
            java.awt.Point p = e.getPoint();
            int indRow = tableau.rowAtPoint(p);
            int indCol = tableau.columnAtPoint(p);
            if (indRow == 0 && indCol == 0 && tableau.getFirstCase().equals(FIRST_CASE.NOT_VISIBLE)) {
                return;
            }
            Color couleurActuel = tableau.getModel().getCouleurCase(indRow, indCol);
            Color newColor = couleurActuel.equals(COULEUR_CASE) ? DEFAULT_COULEUR_CASE : COULEUR_CASE;
            if (!couleurActuel.equals(newColor)) {
                tableau.getModel().colorerCase(indRow, indCol, newColor);
                addEdit(new UndoManagerTableau.ColorateCelluleEdit(indRow, indCol, couleurActuel, newColor));
            }
            tableau.clearSelection();
            adapterDimensions();
        }
    }

    /**
     * Classe permettant de dialoguer avec les flèches de proportionnalité. Elle
     * permet de créer un {@link Edit} lorsqu'un champ de texte a été modifié,
     * et de récupérer le contenu du champ lorsque celui-ci perd le focus afin
     * de modifier le {@link ModeleFleches} en conséquent.
     */
    public class ChampTextListener implements PropertyChangeListener, FocusListener {

        private DataTexte oldData = new DataTexte("");
        public ControlleurTableau controlleur = ControlleurTableau.this;//HACK pour remettre un peu d'ordre dans ce foutoir

        @Override
        public void focusGained(FocusEvent e) {
            JMathTextPane txt = (JMathTextPane) e.getComponent();
            if(!txt.hasBeenModified()) {oldData = EditeurIO.write(txt);}
        }

        @Override
        public void focusLost(FocusEvent e) {//vérifier que le contenu du modèle est bien mis à jour
            JLimitedMathTextPane text = (JLimitedMathTextPane) e.getComponent();
            if(!text.hasBeenModified()) {return;}
            addEdit(new UndoManagerTableau.ContentFlecheEdit(text, oldData, EditeurIO.write(text)));
            
            text.setModified(false);
//            if (modeleFlecheFocused != null && fleches.contains(modeleFlecheFocused.getCle())) {
//                DataFleche oldModele = fleches.get(modeleFlecheFocused.getCle());
//                if (createEdit || !oldModele.getData().equals(modeleFlecheFocused.getData())) {
//                    modeleFlecheFocused.setData(EditeurIO.write((JLimitedMathTextPane) e.getComponent()));
//                    addEdit(new UndoManagerTableau.ContentFlecheEdit(modeleFlecheFocused.getCle(), oldModele.getData(), modeleFlecheFocused.getData()));
//                    fleches.remove(oldModele);
//                    fleches.add(modeleFlecheFocused.clone());
//                }
//            }
//            createEdit = false;
//            modeleFlecheFocused = null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(Undoable.MODIFIED) && evt.getNewValue().equals(Boolean.TRUE)) {
                modified = true;
            }
        }
    }

    public Data getDonneesTP() {
        Data data = new DataObject();
        try {
            String fleches = Json.toJson(modeleFleches);
            String table = Json.toJson(tableau.getModel());
            data.putElement(MODELE_FLECHES, fleches);
            data.putElement(MODELE_TABLEAU, table);
            return data;
        } catch(IOException ex) {
            System.out.println("Erreur lors de l'écriture du tableau");
            Logger.getLogger(ControlleurTableau.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Renvoie un {@link Rectangle} définissant le visuel fonctionnel du
     * tableau, c'est à dire le tableau ainsi que ses flèches de
     * proportionnalité.
     *
     * @return un {@link Rectangle} définissant le visuel fonctionnel de
     * l'OngletTableau
     */
    public Rectangle getVisibleBounds() {
        if (tableau.getWidth() == 0 || tableau.getHeight() == 0) {
            return null;
        }

        int x0 = supportPanel.getX() + tableau.getX();
        int y0 = supportPanel.getY() + tableau.getY();
        int width = tableau.getWidth();
        int height = tableau.getHeight();

        if (!modeleFleches.getFleches(ORIENTATIONS.HAUT).isEmpty()) {
            y0 = supportPanel.getY();
            height += tableau.getY();
        }
        if (!modeleFleches.getFleches(ORIENTATIONS.GAUCHE).isEmpty()) {
            x0 = supportPanel.getX();
            width += tableau.getX();
        }
        if (!modeleFleches.getFleches(ORIENTATIONS.DROIT).isEmpty()) {
            width = width + supportPanel.getWidth() - tableau.getX() - tableau.getWidth();
        }
        if (!modeleFleches.getFleches(ORIENTATIONS.BAS).isEmpty()) {
            height = height + supportPanel.getHeight() - tableau.getY() - tableau.getHeight();
        }

        return new Rectangle(x0, y0, width, height);
    }

    public void chargement(Data dataControlleur) {
        ModeleFleches fleches;
        ModeleTableau modeleTableau;
        try {
            fleches = (ModeleFleches) Json.toJava(dataControlleur.getElement(MODELE_FLECHES),ModeleFleches.class);
            modeleTableau = (ModeleTableau) Json.toJava(dataControlleur.getElement(MODELE_TABLEAU),ModeleTableau.class);
        } catch (IOException ex) {
            System.out.println("erreur lors de la lecture du tableau");
            Logger.getLogger(ControlleurTableau.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        int nbLigne = modeleTableau.getRowCount();
        int nbColonne = modeleTableau.getColumnCount();
        ongletTableaux.nouveau(nbLigne, nbColonne);
        tableau.setModeleTableau(this, modeleTableau);
        modeleFleches.setFleches(fleches);

        edits = new UndoManagerTableau(tableau.getModel(), modeleFleches);
        supportPanel.checkChampProportionnaliteListener(champTextListener);
        tableau.clearSelection();
        setModified(false);
        tableau.validate();
        tableau.revalidate();
        tableau.repaint();
    }

    
    //************************************************************************************
    //********************** Gestion des changement de mode ******************************
    public static final String MODE_PROPERTY = "mode";
    public static enum MODE {
        NORMAL, AJOUTER, SUPPRIMER, AJOUTER_FLECHES, SUPPRIMER_FLECHES, COLORER;
    }
    private MODE mode = NORMAL;
    
    public MODE getMode() { return mode; }

    public void setMode(MODE newMode) {
        MODE oldMode = mode;
        if(oldMode==newMode) {return;}
        mode = newMode;
        switch(newMode) {
            case NORMAL:normalMode();break;
            case COLORER:colorerMode();break;
            case AJOUTER:ajouterMode();break;
            case SUPPRIMER:supprimerMode();break;
            case AJOUTER_FLECHES:ajouterFlechesMode();break;
            case SUPPRIMER_FLECHES:supprimerFlechesMode();break;
            default:;
        }

        //notifie les listeners
        firePropertyChange(MODE_PROPERTY, oldMode, newMode);
    }
    

    //************************************************************************************
    //********************** Gestion des actions *****************************************
    ActionGroup actions = new ActionGroup();
    ActionNormal actionNormal = new ActionNormal();
    ActionColorer actionColorer = new ActionColorer();
    ActionAjouter actionAjouter = new ActionAjouter();
    ActionSupprimer actionSupprimer = new ActionSupprimer();
    ActionAjouterFleches actionAjouterFleches = new ActionAjouterFleches();
    ActionSupprimerFleches actionSupprimerFleches = new ActionSupprimerFleches();
    {
        actions.add(actionNormal);
        actions.add(actionColorer);
        actions.add(actionAjouter);
        actions.add(actionSupprimer);
        actions.add(actionAjouterFleches);
        actions.add(actionSupprimerFleches);
    }
    
    public Action getActionNormal() { return actionNormal; }
    public Action getActionColorer() { return actionColorer; }
    public Action getActionAjouter() { return actionAjouter; }
    public Action getActionSupprimer() { return actionSupprimer; }
    public Action getActionAjouterFleches() { return actionAjouterFleches; }
    public Action getActionSupprimerFleches() { return actionSupprimerFleches; }
    
    private class ActionNormal extends ActionComplete.Toggle {
        private ActionNormal() { super("table normal", true); }
        public void actionPerformed(ActionEvent e) { setMode(NORMAL); }
    }
    private class ActionAjouter extends ActionComplete.Toggle {
        private ActionAjouter() { super("table add", false); }
        public void actionPerformed(ActionEvent e) { setMode(AJOUTER); }
    }

    private class ActionSupprimer extends ActionComplete.Toggle {
        private ActionSupprimer() { super("table remove", false); }
        public void actionPerformed(ActionEvent e) { setMode(SUPPRIMER); }
    }

    private class ActionAjouterFleches extends ActionComplete.Toggle {
        private ActionAjouterFleches() { super("table add arrow", false); }
        public void actionPerformed(ActionEvent e) { setMode(AJOUTER_FLECHES); }
    }

    private class ActionSupprimerFleches extends ActionComplete.Toggle {
        private ActionSupprimerFleches() { super("table remove arrow", false); }
        public void actionPerformed(ActionEvent e) { setMode(SUPPRIMER_FLECHES); }
    }

    private class ActionColorer extends ActionComplete.Toggle {
        private ActionColorer() { super("table color", false); }
        public void actionPerformed(ActionEvent e) { setMode(COLORER); }
    }
        
}
