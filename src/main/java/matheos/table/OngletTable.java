/**
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of MathEOS
 *
 * MathEOS is free software: you can redistribute it and/or modify
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
 *
 **/

package matheos.table;

import matheos.elements.Onglet;
import matheos.sauvegarde.Data;
import matheos.table.TableSideLayout.ORIENTATION;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.ActionGroup;
import matheos.utils.dialogue.DialogueComplet;
import matheos.utils.dialogue.DialogueEvent;
import matheos.utils.dialogue.DialogueListener;
import matheos.utils.librairies.DimensionTools.DimensionT;
import matheos.utils.managers.GeneralUndoManager;
import matheos.utils.texte.EditeurKit;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import matheos.utils.managers.ColorManager;

/**
 * OngletTP qui permet de mettre en place les tableaux de proportionnalité.
 * @author François Billioud
 */
public class OngletTable extends Onglet.OngletTP {
    
    public static final String MODE_PROPERTY = "mode";
    public static final String ORIENTATION_PROPERTY = "orientation";
    public static final int ACTION_PROPORTIONNALITE = 0;

    private final Table table;
    private final SideTableLayout layout;
    
    private final GeneralUndoManager undo = new GeneralUndoManager();
    
    private int mode = NORMAL;
    public static final int NORMAL = 0;
    public static final int INSERTION = 1;
    public static final int COLORER = 2;
    public static final int SUPPRESSION = 3;
    public static final int CREATION_FLECHE = 4;
    public static final int SUPPRESSION_FLECHE = 5;
    
    private final PropertyChangeListener changeModeListener = new PropertyChangeListener() {
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
            OngletTable.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    };
    
    private final ActionGroup modeGroupe = new ActionGroup();
    private final ActionModeNormal actionNormal = new ActionModeNormal();
    private final Action actionInsertion = new ActionModeInsertion();
    private final Action actionSuppression = new ActionModeSuppression();
    private final Action actionColorer = new ActionModeColorer();
    private final Action actionCreateArrow = new ActionModeCreateArrow();
    private final Action actionDeleteArrow = new ActionModeDeleteArrow();

    public OngletTable() {
        
        table = new Table(2, 2);
        table.setUndoManager(undo);
        table.addPropertyChangeListener(changeModeListener);
        
        layout = new SideTableLayout(table, this);
        setLayout(layout);
        layout.addPropertyChangeListener(changeModeListener);
        
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
                case INSERTION : orientation = TableSideLayout.HAUT + TableSideLayout.GAUCHE; break;
                case SUPPRESSION : orientation = TableSideLayout.HAUT + TableSideLayout.GAUCHE; break;
                case COLORER: orientation = TableSideLayout.HAUT + TableSideLayout.GAUCHE; break;
                default : orientation = TableSideLayout.ALL;
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
        return table.getTableModel().getDonnees();
    }

    @Override
    protected void chargement(/*long id, */Data donnees) {
        table.charger(donnees);
        undo.discardAllEdits();
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
                    chargement(new Model(table, event.getInputInteger("rows"), event.getInputInteger("columns")).getDonnees());
                }
            }
        });
    }

    @Override
    public void setActionEnabled(int actionID, boolean b) {
        if(actionID==ACTION_PROPORTIONNALITE) {
            actionCreateArrow.setEnabled(b);
            actionDeleteArrow.setEnabled(b);
        }
    }

    @Override
    protected void activeContenu(boolean b) {
        setEnabled(b);
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
    
    static class SideTableLayout implements LayoutManager {

        private final Table table;
        
        private final Map<ORIENTATION, TableSideLayout> sidePanels = new HashMap<>();

        SideTableLayout(Table table, JPanel parent) {
            this.table = table;
            
            sidePanels.put(ORIENTATION.HAUT, new TableSideLayout(ORIENTATION.HAUT, table, parent));
            sidePanels.put(ORIENTATION.BAS, new TableSideLayout(ORIENTATION.BAS, table, parent));
            sidePanels.put(ORIENTATION.GAUCHE, new TableSideLayout(ORIENTATION.GAUCHE, table, parent));
            sidePanels.put(ORIENTATION.DROITE, new TableSideLayout(ORIENTATION.DROITE, table, parent));
            
            parent.add(table);
        }
        
        void addPropertyChangeListener(PropertyChangeListener l) {
            for(TableSideLayout side : sidePanels.values()) {
                side.addPropertyChangeListener(l);
            }
        }
        
        void removePropertyChangeListener(PropertyChangeListener l) {
            for(TableSideLayout side : sidePanels.values()) {
                side.removePropertyChangeListener(l);
            }
        }
        
        void setMode(int mode) {
            for(TableSideLayout side : sidePanels.values()) {
                side.setMode(mode);
            }
        }
        
        void setOrientation(int orientation) {
            sidePanels.get(ORIENTATION.HAUT).setEnabled(orientation%2==1);
            sidePanels.get(ORIENTATION.GAUCHE).setEnabled(orientation/2%2==1);
            sidePanels.get(ORIENTATION.BAS).setEnabled(orientation/4%2==1);
            sidePanels.get(ORIENTATION.DROITE).setEnabled(orientation/8%2==1);
        }
        
        @Override
        public void addLayoutComponent(String name, Component comp) {}
        @Override
        public void removeLayoutComponent(Component comp) {}
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Dimension minCell = table.getMinimumCellSize(), prefTable = table.getPreferredSize();
            int min = Math.min(minCell.width, minCell.height);
            return new DimensionT(min,min).plus(Fleche.MARGIN_LATERAL, Fleche.MARGIN_VERTICAL).fois(2).plus(prefTable);
        }
        
        @Override
        public Dimension minimumLayoutSize(Container parent) {
            Dimension minCell = table.getMinimumCellSize(), minTable = table.getMinimumSize();
            int min = Math.min(minCell.width, minCell.height);
            return new DimensionT(min,min).plus(Fleche.MARGIN_LATERAL, Fleche.MARGIN_VERTICAL).fois(2).plus(minTable);
        }

        @Override
        public void layoutContainer(Container parent) {
            //La table est localisée à +offsetGauche, +offsetHaut, où les offsets sont l'espace laissé pour dessiner le boutons d'interaction utilisateur
            Dimension minCell = table.getMinimumCellSize();
            int min = Math.min(minCell.width, minCell.height);
            table.setLocation(min+Fleche.MARGIN_LATERAL,min+Fleche.MARGIN_VERTICAL);
            
            //La taille de la table est la preferredSize, si possible. Mais on fixe la taille max afin d'adapter la font-size pour que tout tienne tjs à l'écran
            Dimension size = parent.getSize();
            if(size.width==0 || size.height==0) {size = parent.getPreferredSize();}
            Dimension max = new Dimension(size.width-(min+Fleche.MARGIN_LATERAL)*2, size.height-(min+Fleche.MARGIN_VERTICAL)*2);
            table.setMaximumSize(max);
            
            //On calcul à présent la taille réelle de la table
            Dimension mini = table.getMinimumSize();
            DimensionT pref = new DimensionT(table.getPreferredSize()).min(max).max(mini);
            table.setSize(pref);
            table.revalidate();
            
            //Enfin, on positionne les objets sur les côtés de la table
            try {//HACK pour le moment, le système étant instable, il est plus sûr de mettre de coté cette méthode
                if(table.isEmpty()) {return;}
                Dimension miniSize = table.getMinimumCellSize();
                int miniDimension = Math.min(miniSize.width, miniSize.height);
                for(TableSideLayout side : sidePanels.values()) {
                    if(side.isEnabled()) {side.positionComponent(miniDimension);}
                }
            } catch(Exception ex) {
                Logger.getLogger(OngletTable.class.getName()).log(Level.SEVERE, "no such element", ex);
            }
        }
        
    }
}
