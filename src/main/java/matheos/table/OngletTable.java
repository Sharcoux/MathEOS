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
import matheos.table.SidePanel.ORIENTATION;
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
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * OngletTP qui permet de mettre en place les tableaux de proportionnalité.
 * @author François Billioud
 */
public class OngletTable extends Onglet.OngletTP {
    
    public static final String MODE_PROPERTY = "mode";
    public static final String ORIENTATION_PROPERTY = "orientation";

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
            if(evt.getPropertyName().equals(MODE_PROPERTY)) {
                setMode((int)evt.getNewValue());
            } else if(evt.getPropertyName().equals(ORIENTATION_PROPERTY)) {
                SidePanel.ORIENTATION orientation = (SidePanel.ORIENTATION) evt.getNewValue();
                layout.setOrientation(orientation);
            }
            //Sinon, on transmet les events
            OngletTable.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    };
    
    public OngletTable() {
        
        table = new Table(2, 2);
        table.setUndoManager(undo);
        table.addPropertyChangeListener(changeModeListener);
        
        setLayout(layout = new SideTableLayout(table, this));
        layout.addPropertyChangeListener(changeModeListener);
        
        EditeurKit kit = table.getEditeurKit();
        barreOutils.addBoutonOnLeft(kit.getBoutonBold());
        barreOutils.addBoutonOnLeft(kit.getBoutonItalic());
        barreOutils.addBoutonOnLeft(kit.getBoutonUnderline());
        barreOutils.addSeparateurOnLeft();
        barreOutils.addBoutonOnLeft(kit.getBoutonLeftAlined());
        barreOutils.addBoutonOnLeft(kit.getBoutonCenterAlined());
        barreOutils.addBoutonOnLeft(kit.getBoutonRightAlined());
        
        barreOutils.addSwitchOnRight(new ActionModeInsertion());
        barreOutils.addSwitchOnRight(new ActionModeSuppression());
        barreOutils.addSwitchOnRight(new ActionModeColorer());
        barreOutils.addSwitchOnRight(new ActionModeCreateArrow());
        barreOutils.addSwitchOnRight(new ActionModeDeleteArrow());
        actionNormal = new ActionModeNormal();
        
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
    
    private final ActionComplete.Toggle actionNormal;

    private void setMode(int mode) {
        if(this.mode!=mode) {
            firePropertyChange(MODE_PROPERTY, this.mode, mode);
            this.mode = mode;
            if(mode==NORMAL) { actionNormal.setSelected(true); }
            table.setEditable(mode==NORMAL);
            table.setColoringMode(mode==COLORER);
            layout.setMode(mode);
            layout.setOrientation(null);//on réinitialise l'orientation
            repaint();
        }
    }
    
    @Override
    public BufferedImage capturerImage() {
        if(this.getSize().width == 0 || this.getSize().height == 0) {
            return null;
        }
        retourModeNormal();
        table.prepareTableForPicture();
        Color backGround = getBackground();
        setBackground(Color.WHITE);
        BufferedImage tamponSauvegarde = new BufferedImage(this.getPreferredSize().width+1, this.getPreferredSize().height+1, BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = tamponSauvegarde.createGraphics(); //On crée un Graphic que l'on insère dans tamponSauvegarde
        g.setColor(Color.WHITE);
        this.paint(g);
        setBackground(backGround);
        return tamponSauvegarde;
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
                    table.clear();
                    chargement(new Model(table, event.getInputInteger("rows"), event.getInputInteger("columns")).getDonnees());
                }
            }
        });
    }

    @Override
    public void setActionEnabled(int actionID, boolean b) {
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

    private final ActionGroup modeGroupe = new ActionGroup();
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
        private final JComponent parent;
        
        private final SidePanel supportFlechesBas;
        private final SidePanel supportFlechesHaut;
        private final SidePanel supportFlechesGauche;
        private final SidePanel supportFlechesDroite;

        SideTableLayout(Table table, JPanel parent) {
            this.table = table;
            this.parent = parent;
            
            Model model = table.getTableModel();
            supportFlechesBas = new SidePanel(ORIENTATION.BAS, model, parent);
            supportFlechesHaut = new SidePanel(ORIENTATION.HAUT, model, parent);
            supportFlechesGauche = new SidePanel(ORIENTATION.GAUCHE, model, parent);
            supportFlechesDroite = new SidePanel(ORIENTATION.DROITE, model, parent);
            
            supportFlechesBas.setUndoManager(table.getUndoManager());
            supportFlechesHaut.setUndoManager(table.getUndoManager());
            supportFlechesGauche.setUndoManager(table.getUndoManager());
            supportFlechesDroite.setUndoManager(table.getUndoManager());
            
            parent.add(table);
        }
        
        void addPropertyChangeListener(PropertyChangeListener l) {
            supportFlechesGauche.addPropertyChangeListener(l);
            supportFlechesHaut.addPropertyChangeListener(l);
            supportFlechesBas.addPropertyChangeListener(l);
            supportFlechesDroite.addPropertyChangeListener(l);
        }
        
        void removePropertyChangeListener(PropertyChangeListener l) {
            supportFlechesGauche.removePropertyChangeListener(l);
            supportFlechesHaut.removePropertyChangeListener(l);
            supportFlechesBas.removePropertyChangeListener(l);
            supportFlechesDroite.removePropertyChangeListener(l);
        }
        
        void setMode(int mode) {
            supportFlechesGauche.setMode(mode);
            supportFlechesHaut.setMode(mode);
            supportFlechesBas.setMode(mode);
            supportFlechesDroite.setMode(mode);
        }
        
        void setOrientation(ORIENTATION orientation) {
            supportFlechesGauche.setOrientation(orientation);
            supportFlechesHaut.setOrientation(orientation);
            supportFlechesBas.setOrientation(orientation);
            supportFlechesDroite.setOrientation(orientation);
        }
        
        @Override
        public void addLayoutComponent(String name, Component comp) {}
        @Override
        public void removeLayoutComponent(Component comp) {}
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Dimension minCell = table.getMinimumCellSize(), prefTable = table.getPreferredSize();
            int min = Math.min(minCell.width, minCell.height);
            return new DimensionT(min,min).fois(4).plus(prefTable);
        }
        
        @Override
        public Dimension minimumLayoutSize(Container parent) {
            Dimension minCell = table.getMinimumCellSize(), minTable = table.getMinimumSize();
            int min = Math.min(minCell.width, minCell.height);
            return new DimensionT(min,min).fois(4).plus(minTable);
        }

        @Override
        public void layoutContainer(Container parent) {
            Dimension minCell = table.getMinimumCellSize();
            int min = Math.min(minCell.width, minCell.height);
            table.setLocation(min*2,min*2);
            
            Dimension size = parent.getSize();
            if(size.width==0 || size.height==0) {size = parent.getPreferredSize();}
            Dimension max = new Dimension(size.width-min*4, size.height-min*4);
            table.setMaximumSize(max);
            
            Dimension mini = table.getMinimumSize();
            DimensionT pref = new DimensionT(table.getPreferredSize()).min(max).max(mini);
//            table.setSize(pref);
            table.setSize(pref);
            table.revalidate();
            try {//HACK pour le moment, le système étant instable, il est plus sur de mettre de coté cette méthode
                positionComponents();
            } catch(Exception ex) {
                Logger.getLogger(OngletTable.class.getName()).log(Level.SEVERE, "no such element", ex);
            }
        }
        
        private void positionComponents() {
            if(table.isEmpty()) {return;}
            Dimension min = table.getMinimumCellSize();
            int mini = Math.min(min.width, min.height);
            supportFlechesGauche.positionComponent(table.getTableModel().get(Model.COLUMN, 0), mini);
            supportFlechesHaut.positionComponent(table.getTableModel().get(Model.ROW, 0), mini);
            supportFlechesBas.positionComponent(table.getTableModel().get(Model.ROW, table.getTableModel().getRowCount()-1), mini);
            supportFlechesDroite.positionComponent(table.getTableModel().get(Model.COLUMN, table.getTableModel().getColumnCount()-1), mini);
        }
    }
}
