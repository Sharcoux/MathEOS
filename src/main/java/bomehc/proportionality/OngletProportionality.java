/**
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of Bomehc
 *
 * Bomehc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 *
 **/

package bomehc.proportionality;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.KeyStroke;
import bomehc.elements.Onglet;
import bomehc.sauvegarde.Data;
import bomehc.table.Table;
import bomehc.table.TableLayout.Cell;
import bomehc.utils.boutons.ActionComplete;
import bomehc.utils.boutons.ActionGroup;
import bomehc.utils.dialogue.DialogueComplet;
import bomehc.utils.dialogue.DialogueEvent;
import bomehc.utils.dialogue.DialogueListener;
import bomehc.utils.managers.ColorManager;
import bomehc.utils.managers.PermissionManager;
import static bomehc.utils.managers.PermissionManager.ACTION.PROPORTIONNALITE;
import bomehc.utils.texte.EditeurKit;

/**
 * OngletTP qui permet de mettre en place les tableaux de proportionnalité.
 * @author François Billioud
 */
public class OngletProportionality extends Onglet.OngletTP {
    
    public static final String MODE_PROPERTY = "mode";
    public static final String ORIENTATION_PROPERTY = "orientation";
    public static final int ACTION_PROPORTIONNALITE = 0;

    private final ProportionalityTable table;
    private final OngletProportionalityLayout layout;
    
    private int mode = NORMAL;
    public static final int NORMAL = 0;
    public static final int INSERTION = 1;
    public static final int COLORER = 2;
    public static final int SUPPRESSION = 3;
    public static final int CREATION_FLECHE = 4;
    public static final int SUPPRESSION_FLECHE = 5;
    
    private final PropertyChangeListener propertyListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case MODE_PROPERTY:
                    setMode((int)evt.getNewValue());
                    break;
                case ORIENTATION_PROPERTY:
                    int orientation = (int) evt.getNewValue();
                    layout.setOrientation(orientation);
                    break;
                case Table.EDITING_PROPERTY:
                    setEditingMode((boolean)evt.getNewValue());
                    break;
            }
            //Sinon, on transmet les events
            OngletProportionality.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    };
    
    private final ActionGroup modeGroupe = new ActionGroup();
    private final ActionModeNormal actionNormal = new ActionModeNormal();
    private final Action actionInsertion = new ActionModeInsertion();
    private final Action actionSuppression = new ActionModeSuppression();
    private final Action actionColorer = new ActionModeColorer();
    private final Action actionCreateArrow = new ActionModeCreateArrow();
    private final Action actionDeleteArrow = new ActionModeDeleteArrow();

    public OngletProportionality() {
        
        table = new ProportionalityTable(2, 2);
        table.addPropertyChangeListener(propertyListener);
        
        layout = new OngletProportionalityLayout(table, this);
        layout.addPropertyChangeListener(propertyListener);
        setLayout(layout);
        setBackground(ColorManager.get("color table"));
        
        EditeurKit kit = table.getEditeurKit();
        barreOutils.addBoutonOnLeft(kit.getBoutonBold());
        barreOutils.addBoutonOnLeft(kit.getBoutonItalic());
        barreOutils.addBoutonOnLeft(kit.getBoutonUnderline());
        barreOutils.addSeparateurOnLeft();
        barreOutils.addBoutonOnLeft(kit.getBoutonLeftAlined());
        barreOutils.addBoutonOnLeft(kit.getBoutonCenterAlined());
        barreOutils.addBoutonOnLeft(kit.getBoutonRightAlined());
        
        barreOutils.addSwitchOnRight(actionInsertion);
        barreOutils.addSwitchOnRight(actionSuppression);
        barreOutils.addSwitchOnRight(actionColorer);
        barreOutils.addSwitchOnRight(actionCreateArrow);
        barreOutils.addSwitchOnRight(actionDeleteArrow);
        
        //ajoute un changeModeListener sur les composants ajoutés à la table
        table.addMouseListener(getChangeModeListener());
        for(Cell c : table.getAllCells()) {
            c.addMouseListener(getChangeModeListener());
        }
        for(Fleche f : table.getAllArrows()) {
            f.addMouseListener(getChangeModeListener());
        }
        table.addTableModelListener(new TableChangeModeListener());
        
        //Raccourcis clavier
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "stop");
        getActionMap().put("stop",new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                retourModeNormal();
            }
        });
        
        addMouseListener(new MouseAdapter() {//Pour annuler par simple clic hors du tableau
            @Override
            public void mousePressed(MouseEvent e) {
                retourModeNormal();
            }
        });
    }
    
    private void retourModeNormal() {
        actionNormal.setSelected(true);
        actionNormal.actionPerformed(null);
    }
    
    private void setEditingMode(boolean b) {
        actionInsertion.setEnabled(!b);
        actionSuppression.setEnabled(!b);
        actionColorer.setEnabled(!b);
        actionCreateArrow.setEnabled(!b);
        actionDeleteArrow.setEnabled(!b);
    }
    
    private void setMode(int mode) {
        if(this.mode!=mode) {
            firePropertyChange(MODE_PROPERTY, this.mode, mode);
            this.mode = mode;
            if(mode==NORMAL) { actionNormal.setSelected(true); }
            table.setEditable(mode==NORMAL);
            table.setColoringMode(mode==COLORER);
            layout.setMode(mode);
            int orientation;
            switch(mode) {
                case INSERTION : orientation = OngletProportionalityLayout.HAUT + OngletProportionalityLayout.GAUCHE; break;
                case SUPPRESSION : orientation = OngletProportionalityLayout.HAUT + OngletProportionalityLayout.GAUCHE; break;
                case COLORER: orientation = OngletProportionalityLayout.HAUT + OngletProportionalityLayout.GAUCHE; break;
                default : orientation = OngletProportionalityLayout.ALL;
            }
            layout.setOrientation(orientation);//on réinitialise l'orientation
            repaint();
        }
    }
    
    @Override
    public Graphics2D capturerImage(Graphics2D g) {
        if(this.getSize().width == 0 || this.getSize().height == 0) {
            return g;
        }
        retourModeNormal();
        table.prepareTableForPicture();
        Color backGround = getBackground();
        setBackground(ColorManager.transparent());
//        BufferedImage tamponSauvegarde = new BufferedImage(this.getPreferredSize().width+1, this.getPreferredSize().height+1, BufferedImage.TYPE_3BYTE_BGR);
//        Graphics g = tamponSauvegarde.createGraphics(); //On crée un Graphic que l'on insère dans tamponSauvegarde
        this.paint(g);
        setBackground(backGround);
        return g;
    }

    @Override
    public Dimension getInsertionSize() {
        return layout.preferredLayoutSize(this);
    }    
    
    @Override
    protected Data getDonneesTP() {
        return table.getDonnees();
    }

    @Override
    protected void chargement(/*long id, */Data donnees) {
        table.charger(donnees);
    }

    @Override
    protected void nouveauTP() {
        DialogueComplet dialog = new DialogueComplet("dialog new table");
        dialog.addDialogueListener(new DialogueListener() {
            @Override
            public void dialoguePerformed(DialogueEvent event) {
                if(event.isConfirmButtonPressed()) {
                    retourModeNormal();
                    table.clear();
                    //FIXME : pas vraiment optimisé. Il vaudrait mieux utiliser la même table
                    chargement(new ProportionalityTable(event.getInputInteger("rows"), event.getInputInteger("columns")).getDonnees());
                }
            }
        });
    }

    @Override
    public void setActionEnabled(PermissionManager.ACTION actionID, boolean b) {
        switch(actionID) {
            case PROPORTIONNALITE :
                actionCreateArrow.setEnabled(b);
                actionDeleteArrow.setEnabled(b);
                break;
        }
    }

    @Override
    protected void activeContenu(boolean b) {
        setEnabled(b);
        setBackground(ColorManager.get(b ? "color table" : "color disabled"));
        table.setEnabled(b);
        layout.setEnabled(b);
    }
    
    @Override
    public void zoomP() {}
    @Override
    public void zoomM() {}
    @Override
    public void annuler() { table.annuler(); }
    @Override
    public void refaire() { table.refaire(); }
    @Override
    public boolean peutAnnuler() { return table.peutAnnuler(); }
    @Override
    public boolean peutRefaire() { return table.peutRefaire(); }
    @Override
    public boolean hasBeenModified() { return table.hasBeenModified(); }
    @Override
    public void setModified(boolean b) { table.setModified(b); }

    private abstract class ActionMode extends ActionComplete.Toggle {
        private final int mode;
        private ActionMode(String aspect, int mode) {
            super(aspect, false);
            this.mode = mode;
            modeGroupe.add(this);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            setMode(mode);
        }
    }
    
    private class ActionModeNormal extends ActionMode {
        private ActionModeNormal() {
            super("table mode normal", NORMAL);
        }
    }
    private class ActionModeInsertion extends ActionMode {
        private ActionModeInsertion() {
            super("table mode insertion", INSERTION);
        }
    }
    private class ActionModeSuppression extends ActionMode {
        private ActionModeSuppression() {
            super("table mode suppression", SUPPRESSION);
        }
    }
    private class ActionModeColorer extends ActionMode {
        private ActionModeColorer() {
            super("table mode paint", COLORER);
        }
    }
    private class ActionModeCreateArrow extends ActionMode {
        private ActionModeCreateArrow() {
            super("table mode arrow creation", CREATION_FLECHE);
        }
    }
    private class ActionModeDeleteArrow extends ActionMode {
        private ActionModeDeleteArrow() {
            super("table mode arrow suppression", SUPPRESSION_FLECHE);
        }
    }
    
    private class TableChangeModeListener implements ProportionalityTable.ModelListener {
        @Override
        public void arrowInserted(int direction, Fleche fleche) {fleche.addMouseListener(getChangeModeListener());}
        @Override
        public void arrowDeleted(int direction, Fleche fleche) {fleche.removeMouseListener(getChangeModeListener());}
        @Override
        public void rowInserted(Cell[] row, int index) {for(Cell c : row) {c.addMouseListener(getChangeModeListener());}}
        @Override
        public void columnInserted(Cell[] column, int index) {for(Cell c : column) {c.addMouseListener(getChangeModeListener());}}
        @Override
        public void rowDeleted(Cell[] row, int index) {for(Cell c : row) {c.removeMouseListener(getChangeModeListener());}}
        @Override
        public void columnDeleted(Cell[] column, int index) {for(Cell c : column) {c.removeMouseListener(getChangeModeListener());}}
        @Override
        public void contentEdited(Cell c, Object newContent) {}
        @Override
        public void cleared(Cell[][] table) {for(Cell[] C : table) {for(Cell c : C) {c.removeMouseListener(getChangeModeListener());}}}
        @Override
        public void cellReplaced(Cell oldCell, Cell newCell) {oldCell.removeMouseListener(getChangeModeListener());newCell.addMouseListener(getChangeModeListener());}
        @Override
        public void colorChanged(Color oldColor, Color newColor) {}
    }
    
}