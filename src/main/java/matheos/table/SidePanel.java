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

import static matheos.table.OngletTable.*;
import matheos.table.TableLayout.Cell;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.Bouton;
import matheos.utils.managers.GeneralUndoManager;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Panel qui permet d'afficher les boutons autour du tableau (flèches, boutons, etc).
 * @author François Billioud
 */
public class SidePanel {
    
    private static final boolean VERTICAL = true;
    private static final boolean HORIZONTAL = false;
    
    private GeneralUndoManager undo;
    
    public enum ORIENTATION {
        HAUT(VERTICAL," up", Model.HAUT), GAUCHE(HORIZONTAL," left", Model.GAUCHE), BAS(VERTICAL," down", Model.BAS), DROITE(HORIZONTAL," right", Model.DROITE);
        private final boolean direction; private final String name; private final int orientationID;
        ORIENTATION(boolean b, String s, int i) {direction=b; name=s; orientationID=i;}
        private boolean getDirection() {return direction;}
        private String getBalise(String base) {return base+name;}
        public int getOrientationId() {return orientationID;}
        public boolean isVertical() {return direction==VERTICAL;}
    }
    
    private final ORIENTATION orientation;
    private final Model model;
    private final JPanel support;                                           //support sur lequel seront  dessinés la table et les composants
    private final LinkedList<JComponent> components = new LinkedList<>();   //liste des composants dont cet objet a la charge
    
    private final PropertyChangeSupport changePropertyBean = new PropertyChangeSupport(this);
    
    private int mode = NORMAL;
    private ORIENTATION currentTableOrientation = null;//Permet de limiter l'affichage à un seul coté. Le principe est encore à améliorer
    
    public SidePanel(ORIENTATION orientation, final Model model, JPanel support) {
        this.orientation = orientation;
        this.model = model;
        this.support = support;
        
        model.addTableModelListener(new Model.ModelListener() {//TODO remplacer par un adapter
            @Override
            public void arrowInserted(int direction, Fleche fleche) {applyMode(mode);SidePanel.this.support.repaint();}
            @Override
            public void arrowDeleted(int direction, Fleche fleche) {remove(fleche);SidePanel.this.support.repaint();}
            @Override
            public void rowInserted(Cell[] row, int index) {applyMode(mode);}
            @Override
            public void columnInserted(Cell[] column, int index) {applyMode(mode);}
            @Override
            public void rowDeleted(Cell[] row, int index) {applyMode(mode);}
            @Override
            public void columnDeleted(Cell[] column, int index) {applyMode(mode);}
            @Override
            public void contentEdited(Cell c, Object newContent) {}
            @Override
            public void cleared(Cell[][] table) {applyMode(mode);}
            @Override
            public void cellReplaced(Cell oldCell, Cell newCell) {}
        });
    }

    public void setUndoManager(GeneralUndoManager undo) {
        this.undo = undo;
    }
    
    public void setMode(int mode) {
        if(this.mode==mode) {return;}
        int old = this.mode;
        this.mode = mode;
        
        applyMode(mode);
        
        changePropertyBean.firePropertyChange(MODE_PROPERTY, old, mode);
        startIndex = -1;
    }
    
    /** permet de redessiner les éléments du mode. Utile lorsque les éléments nécessitent une mise à jour **/
    private void applyMode(int mode) {
        switch(mode) {
            case NORMAL : setModeNormal(); break;
            case INSERTION : setModeInsertion(); break;
            case SUPPRESSION : setModeSuppression(); break;
            case COLORER : setModeColorer(); break;
            case CREATION_FLECHE : setModeCreationFleches(); break;
            case SUPPRESSION_FLECHE : setModeSuppressionFleches(); break;
        }
    }
    
    public void setOrientation(ORIENTATION tableOrientation) {
        if(tableOrientation==currentTableOrientation) {return;}
        ORIENTATION old = currentTableOrientation;
        if(tableOrientation==this.orientation || tableOrientation==null) {applyMode(mode);}//Le panel est concerné. On le remet en place
        else {removeAll();}//sinon, on enlève tout.
        currentTableOrientation = tableOrientation;
        changePropertyBean.firePropertyChange(ORIENTATION_PROPERTY, old, tableOrientation);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        changePropertyBean.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changePropertyBean.removePropertyChangeListener(l);
    }
    
    private boolean isVertical() {return orientation.getDirection()==VERTICAL;}
    

    private void removeAll() {
        while(!components.isEmpty()) {
            support.remove(components.poll());
        }
        support.revalidate();
        support.repaint();
    }
    
    private void add(JComponent c) {
        support.add(c);
        components.add(c);
    }
    
    private void remove(JComponent c) {
        support.remove(c);
        components.remove(c);
    }
    
    private void replace(JComponent old, JComponent newComp) {
        support.remove(old);
        int index = components.indexOf(old);
        components.set(index, newComp);
        support.add(newComp);
        support.revalidate();
        support.repaint();
    }

    public void positionComponent(Cell[] line, int buttonSize) {
        switch(mode) {
            case NORMAL : positionNormal(line,buttonSize); break;
            case INSERTION : positionInsertion(line,buttonSize); break;
            case SUPPRESSION : positionSuppression(line,buttonSize); break;
            case COLORER : positionColorer(line,buttonSize); break;
            case CREATION_FLECHE : positionCreationFleche(line,buttonSize); break;
            case SUPPRESSION_FLECHE : positionSuppressionFleche(line,buttonSize); break;
        }
    }
    
    public void positionNormal(Cell[] line, int buttonSize) {
        for(JComponent c : components) {
            final Fleche f = (Fleche) c;
            f.setModeEdition();
            f.setHeight(buttonSize);
            f.positionComponent();
            f.repaint();
        }
    }
    
    private static final int GAP = 0;
    private Point getOrientationTranslationPoint(JComponent c, JComponent ref, Point p0) {
        Point p1 = isVertical() ? new Point(-c.getWidth()/2,0) : new Point(0, -c.getHeight()/2);
        
        switch(orientation) {
            case HAUT : p1.translate(0, -p0.y-GAP-c.getHeight()); break;
            case BAS : p1.translate(0, -p0.y+GAP+ref.getHeight()); break;
            case GAUCHE : p1.translate(-GAP-c.getWidth()-p0.x,0); break;
            case DROITE : p1.translate(ref.getWidth()+GAP-p0.x,0); break;
        }
        return p1;
    }
    private Point getModeTranslationPoint(JComponent ref) {
        Point p = new Point(0,0);
        switch(mode) {
            case INSERTION : break;
            case SUPPRESSION : p = new Point(ref.getWidth()/2,ref.getHeight()/2); break;
            case COLORER : p = new Point(ref.getWidth()/2,ref.getHeight()/2); break;
            case CREATION_FLECHE : p = new Point(ref.getWidth()/2,ref.getHeight()/2); break;
            case SUPPRESSION_FLECHE : p = new Point(ref.getWidth()/2,ref.getHeight()/2); break;
        }
        return p;
    }

    private void positionComponentOverCells(Cell[] line, int buttonSize) {
        if(currentTableOrientation!=null && currentTableOrientation!=orientation) {return;}
        if(components.size()<line.length) {applyMode(mode);}
        Iterator<JComponent> componentIterator = components.iterator();
        for(int i=0; i<line.length; i++) {
            if(!componentIterator.hasNext()) {
                Logger.getLogger(SidePanel.class.getName()).log(Level.SEVERE, "no such element", "");
                return;
            }
            JComponent c = componentIterator.next();
            c.setSize(buttonSize, buttonSize);
            
            JComponent ref = line[i];
            Point p0 = getModeTranslationPoint(ref);
            Point p1 = getOrientationTranslationPoint(c, ref, p0);
            
            Point p = SwingUtilities.convertPoint(ref, p0, support);
            p.translate(p1.x, p1.y);
            c.setLocation(p.x,p.y);
        }
        if(componentIterator.hasNext()) {
            JComponent c = componentIterator.next();
            c.setSize(buttonSize, buttonSize);
            
            JComponent ref = line[line.length-1];
            Point p0 = getModeTranslationPoint(ref);
            Point p1 = getOrientationTranslationPoint(c, ref, p0);
            
            Point p = SwingUtilities.convertPoint(ref, p0, support);
            p.translate(p0.x+p1.x+(isVertical() ? ref.getWidth() : 0), p0.y+p1.y+(isVertical() ? 0 : ref.getHeight()));
            c.setLocation(p.x,p.y);
        }
    }
    
    public void positionInsertion(Cell[] line, int buttonSize) {
        if(orientation==ORIENTATION.DROITE || orientation==ORIENTATION.BAS) {return;}
        positionComponentOverCells(line, buttonSize);
    }
    public void positionSuppression(Cell[] line, int buttonSize) {
        if(orientation==ORIENTATION.DROITE || orientation==ORIENTATION.BAS) {return;}
        positionComponentOverCells(line, buttonSize);
    }
    public void positionColorer(Cell[] line, int buttonSize) {
        if(orientation==ORIENTATION.DROITE || orientation==ORIENTATION.BAS) {return;}
        positionComponentOverCells(line, buttonSize);
    }
    public void positionCreationFleche(Cell[] line, int buttonSize) {
        positionComponentOverCells(line, buttonSize);
    }
    public void positionSuppressionFleche(Cell[] line, int buttonSize) {
        positionNormal(line, buttonSize);
    }
    
    private void createButtons(Action action, int nbElements) {
        removeAll();
        for(int i=0; i<nbElements; i++) {
            //On utilise les clientProperty pour ne créer qu'une action pour plusieurs boutons (très important lors des resize)
            Bouton bouton = new Bouton(action);
            bouton.getButtonComponent().putClientProperty("index", i);
            add(bouton);
        }
        support.revalidate();
        support.repaint();
    }
    private void setModeNormal() {
        removeAll();
        Set<Fleche> fleches = model.getArrows(orientation.getOrientationId());
        for(Fleche f : fleches) {
            add(f);
            f.setUndoManager(undo);
            f.setModeEdition();
        }
        
        support.revalidate();
        support.repaint();
    }
    private void setModeInsertion() {
        if(orientation==ORIENTATION.DROITE || orientation==ORIENTATION.BAS) {removeAll();return;}
        Action action = new ActionInsertion();
        int n = model.getRowCount(), m = model.getColumnCount();
        int nbElements = (isVertical() ? m : n)+1;
        createButtons(action, nbElements);
    }
    private void setModeSuppression() {
        if(orientation==ORIENTATION.DROITE || orientation==ORIENTATION.BAS) {removeAll();return;}
        Action action = new ActionSuppression();
        int n = model.getRowCount(), m = model.getColumnCount();
        int nbElements = (isVertical() ? m : n);
        createButtons(action, nbElements);
    }
    private void setModeColorer() {
        if(orientation==ORIENTATION.DROITE || orientation==ORIENTATION.BAS) {removeAll();return;}
        final Action actionPaint = new ActionCouleur(true);
        final Action actionUnpaint = new ActionCouleur(false);
        int n = model.getRowCount(), m = model.getColumnCount();
        int nbElements = (isVertical() ? m : n);
        
        removeAll();
        for(int i=0; i<nbElements; i++) {
            boolean isAlreadyColored = model.getColor(isVertical()==Model.COLUMN,i)!=null;
            final Bouton bouton = new BoutonDouble(actionPaint, actionUnpaint, !isAlreadyColored);
            bouton.getButtonComponent().putClientProperty("index", i);
            add(bouton);
        }
        support.revalidate();
        support.repaint();
    }
    
    private class BoutonDouble extends Bouton implements ActionListener {
        private final Action action1, action2;
        private boolean currentAction1;
        private BoutonDouble(Action action1, Action action2, boolean action1Initiale) {
            super(action1Initiale ? action1 : action2);
            this.action1 = action1;
            this.action2 = action2;
            this.currentAction1 = action1Initiale;
            
            getButtonComponent().addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BoutonDouble.this.setAction(currentAction1 ? BoutonDouble.this.action2 : BoutonDouble.this.action1);
            currentAction1 = !currentAction1;
        }
    }
    
    private void setModeCreationFleches() {
        Action action = new ActionFlecheCreation();
        int n = model.getRowCount(), m = model.getColumnCount();
        int nbElements = (isVertical() ? m : n);
        createButtons(action, nbElements);
    }
    private void setModeSuppressionFleches() {
        setModeNormal();
        for(JComponent c : components) {
            final Fleche f = (Fleche) c;
            f.setModeSuppression();
        }
    }
    
    private class ActionInsertion extends ActionComplete {
        private ActionInsertion() {
            super(orientation.getBalise("table arrow"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            AbstractButton b = (AbstractButton) source;
            int i = (int) b.getClientProperty("index");
            boolean line = isVertical()==Model.COLUMN;
            
            Bouton bouton = new Bouton(b.getAction());
            bouton.getButtonComponent().putClientProperty("index", model.getCount(line));
            add(bouton);
            
            model.insert(line, i);
            undo.addEdit(new TableEdits.LineChangeEdit.InsertionEdit(i, model, line));
        }
    }
    
    private class ActionSuppression extends ActionComplete {
        private ActionSuppression() {
            super("table suppression");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            JComponent c = (JComponent) source;
            int i = (int) c.getClientProperty("index");
            boolean line = isVertical()==Model.COLUMN;
            List<Fleche> arrowsToRemove = getFlechesInvolving(i, model.getArrows(orientation.getOrientationId()));//les flèches qui seront impactées par cette suppression
            ArrayList<Cell> L = model.delete(line, i);
            undo.addEdit(new TableEdits.LineChangeEdit.SuppressionEdit(i, model, line, L.toArray(new Cell[L.size()]), arrowsToRemove));
            remove(components.getLast());
            support.revalidate();
            support.repaint();
        }
    }
    
    private class ActionCouleur extends ActionComplete {
        private final boolean colorer;
        private static final String COLORER = "table paint";
        private static final String EFFACER = "table unpaint";
        private ActionCouleur(boolean colorer) {
            super(orientation.getBalise(colorer ? COLORER : EFFACER));
            this.colorer = colorer;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            JComponent c = (JComponent) source;
            int i = (int) c.getClientProperty("index");
            boolean line = isVertical()==Model.COLUMN;
            Color oldColor = model.getColor(line, i), newColor;
            model.setColor(line, i, newColor = colorer ? Cell.COLOR_1 : Cell.BACKGROUND);
            undo.addEdit(new TableEdits.LineColorEdit(i, line, oldColor, newColor, model));
        }
    }
    
    private int startIndex = -1;
    private class ActionFlecheCreation extends ActionComplete {
        private ActionFlecheCreation() {
            super(orientation.getBalise("table arrow"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            JComponent c = (JComponent) source;
            int i = (int) c.getClientProperty("index");
            if(startIndex==-1) {
                startIndex = i;
                setOrientation(orientation);
                components.get(i).setVisible(false);
                Set<Fleche> fleches = model.getArrows(orientation.getOrientationId());
                List<Fleche> L = getFlechesInvolving(i, fleches);
                for(Fleche f : L) {components.get(f.getOtherSide(i)).setVisible(false);}
            }
            else {
                Fleche f = new Fleche(orientation, startIndex, i, model);
                model.insertArrow(orientation.getOrientationId(), f);
                undo.addEdit(new TableEdits.ArrowInsertedEdit(f, model));
                setMode(NORMAL);
            }
        }
    }
    private List<Fleche> getFlechesInvolving(int i, Collection<Fleche> col) {
        LinkedList<Fleche> L = new LinkedList<>();
        for(Fleche f : col) {if(f.getStartIndex()==i || f.getEndIndex()==i) {L.add(f);}}
        return L;
    }
}
