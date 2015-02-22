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

package bomehc.table;

import bomehc.sauvegarde.Data;
import bomehc.sauvegarde.Data.Enregistrable;
import bomehc.sauvegarde.DataTexte;
import static bomehc.table.TableLayout.TableModel.ROW;
import bomehc.utils.interfaces.Editable;
import bomehc.utils.interfaces.Undoable;
import bomehc.utils.librairies.DimensionTools.DimensionT;
import bomehc.utils.managers.ColorManager;
import bomehc.utils.texte.EditeurKit;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

/**
 * Met en place les éléments du TableModel comme doit le faire un tableau.
 * @author François Billioud
 */
public class TableLayout implements LayoutManager {

    /** Vitesse de (dé)croissance du zoom **/
    private static final int PAS = 1;
    
    private final TableModel model;
    private final JComponent parent;
    private int[] rowMinimumSizes = new int[0];
    private int[] columnMinimumSizes = new int[0];
    private int[] rowPrefferedSizes = new int[0];
    private int[] columnPrefferedSizes = new int[0];
    private final TableModelListener modelListener = new TableModelListener() {
        @Override
        public void rowInserted(Cell[] cTab, int index) {
            updateSizes();
            Component[] T = parent.getComponents();
            List<Component> L = Arrays.asList(T);
            for(Cell c : cTab) {if(!L.contains(c)) {parent.add(c);}}
            layoutContainer(parent);
        }
        @Override
        public void columnInserted(Cell[] cTab, int index) {
            updateSizes();
            Component[] T = parent.getComponents();
            List<Component> L = Arrays.asList(T);
            for(Cell c : cTab) {if(!L.contains(c)) {parent.add(c);}}
            layoutContainer(parent);
        }
        @Override
        public void rowDeleted(Cell[] cTab, int index) {updateSizes();for(Cell c : cTab) {parent.remove(c);}layoutContainer(parent);}
        @Override
        public void columnDeleted(Cell[] cTab, int index) {updateSizes();for(Cell c : cTab) {parent.remove(c);}layoutContainer(parent);}
        @Override
        public void contentEdited(Cell c, Object newContent) {updateSizes();}
        @Override
        public void cellReplaced(Cell oldCell, Cell newCell) {
            parent.remove(oldCell);
            parent.add(newCell);
            updateSizes();
        }
        @Override
        public void cleared(Cell[][] table) {
            for(Cell[] row : table) {for(Cell c : row) {parent.remove(c);}}
            updateSizes();
            layoutContainer(parent);
        }
        @Override
        public void colorChanged(Color oldColor, Color newColor) {}
    };

    public TableLayout(TableModel model, JComponent parent) {
        this.model = model;
        model.addTableModelListener(modelListener);
        this.parent = parent;

        //Vérifie l'équivalence entre les composants du TableModel et ceux du Parent
        Component[] T = parent.getComponents();
        List<Component> L = Arrays.asList(T);
        int n = model.getRowCount();
        for(int i = 0; i<n; i++) {
            Cell[] row = model.get(ROW, i);
            for(Cell c : row) {if(!L.contains(c)) {parent.add(c);}}
        }
        
        //initialise les dimensions
        updateSizes();
    }
    
    private boolean modelChanged = false;
    private Dimension parentSize = null;
    
    /** On regarde si la taille des colonnes ou des lignes a changée.
     * Si oui, on met à jour les tableaux contenant les tailles min et pref
     * des lignes et des colonnes
     **/
    private void updateSizes() {
        int n = model.getRowCount();
        int m = model.getColumnCount();
        boolean rowNbChanged, colNbChanged;
        if(rowNbChanged = (n!=rowPrefferedSizes.length)) {modelChanged = true;}//si le nb de lignes a changé
        if(colNbChanged = (m!=columnPrefferedSizes.length)) {modelChanged = true;}//si le nb de colonne a changé
        
        final int[] minRow = new int[n];
        final int[] prefRow = new int[n];
        final int[] minColumn = new int[m];
        final int[] prefColumn = new int[m];
        for(int i=0; i<n; i++) {
            Cell[] row = model.get(ROW,i);
            for(int j=0; j<m; j++) {
                Dimension pref = row[j].getPreferredSize();
                Dimension min = row[j].getMinimumSize();
                if(pref.height>prefRow[i]) {//On cherche la taille la plus grande. Quand on trouve,
                    prefRow[i] = pref.height;//On met à jour le nouveau tableau
                    if(!rowNbChanged && prefRow[i]!=rowPrefferedSizes[i]) {modelChanged = true;}//Si la nouvelle taille est différente de l'ancienne, on devra raffraichir l'affichage
                }
                if(min.height>minRow[i]) {
                    minRow[i] = min.height;
                    if(!rowNbChanged && minRow[i]!=rowMinimumSizes[i]) {modelChanged = true;}
                }
                if(pref.width>prefColumn[j]) {
                    prefColumn[j] = pref.width;
                    if(!colNbChanged && prefColumn[j]!=columnPrefferedSizes[j]) {modelChanged = true;}
                }
                if(min.width>minColumn[j]) {
                    minColumn[j] = min.width;
                    if(!colNbChanged && minColumn[j]!=columnMinimumSizes[j]) {modelChanged = true;}
                }
            }
        }
        rowMinimumSizes = minRow;
        rowPrefferedSizes = prefRow;
        columnMinimumSizes = minColumn;
        columnPrefferedSizes = prefColumn;
    }
    
    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Insets insets = parent.getInsets();
        double prefWidth = 0, prefHeight = 0;
        for(int d : rowPrefferedSizes) {prefHeight+=d;}
        for(int d : columnPrefferedSizes) {prefWidth+=d;}
        int width = insets.left + insets.right + (int)prefWidth;
        int height = insets.top + insets.bottom + (int)prefHeight;
        return new Dimension(width, height);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Insets insets = parent.getInsets();
        double minWidth = 0, minHeight = 0;
        for(int d : rowMinimumSizes) {minHeight+=d;}
        for(int d : columnMinimumSizes) {minWidth+=d;}
        int width = insets.left + insets.right + (int)minWidth;
        int height = insets.top + insets.bottom + (int)minHeight;
        return new Dimension(width, height);
    }

    private int rowSize(int i) {
        return Math.max(rowPrefferedSizes[i], rowMinimumSizes[i]);
    }
    private int columnSize(int j) {
        return Math.max(columnPrefferedSizes[j], columnMinimumSizes[j]);
    }
    
    public DimensionT getBestSize() {
        int width = 0, height = 0;
        int n = model.getRowCount(), m = model.getColumnCount();
        for(int i=0; i<n; i++) {height+=rowSize(i);}
        for(int j=0; j<m; j++) {width+=columnSize(j);}
        return new DimensionT(width, height);
    }
    
    @Override
    public void layoutContainer(Container parent) {
        boolean sizeChanged = parent.getParent()!=null && !parent.getParent().getSize().equals(parentSize);
        if(!modelChanged && !sizeChanged) {return;}//aucune modification à effectuer
        
        Dimension max = parent.getMaximumSize();
//        Dimension pref = preferredLayoutSize(parent);
        Dimension pref = getBestSize();
        if(pref.width==0 || pref.height==0) {return;}
        changeFontSize(max, pref, !(pref.width>max.width || pref.height>max.height));

        
        int n = model.getRowCount(), m = model.getColumnCount();
        Insets insets = parent.getInsets();
        int posY = insets.top;
        for(int i = 0; i<n; i++) {
            Cell[] row = model.get(ROW, i);
            int height = rowSize(i);
            int posX = insets.left;
            for(int j = 0; j<m; j++) {
                int width = columnSize(j);
                Cell c = row[j];
                c.setLocation(posX,posY);
                c.setSize(width,height);
                posX+=width;
            }
            posY+=height;
        }
        
        modelChanged=false; parentSize = parent.getSize();
        if(parent.getParent()!=null) {parentSize = parent.getParent().getSize();}
//        parent.repaint();
    }
    
    private int approximeSize(int max, int current, int f) {
        return max*f/current;
    }
    
    private void changeFontSize(Dimension max, Dimension pref, boolean increase) {
        int prefW = pref.width;
        int maxW = max.width;
        int n = model.getRowCount(), m = model.getColumnCount();
        int fontSize = model.getMaxFontSize();
        if(n>0 && m>0) {fontSize=model.getCell(0, 0).getFontSize();}
        
        int newFontSize;
        if(increase) {
            newFontSize = Math.min(model.getMaxFontSize(), approximeSize(maxW, prefW, fontSize));
            if(fontSize>=newFontSize) {return;}
        } else {
            newFontSize = fontSize-PAS;
            if(newFontSize<model.getMinFontSize()) {return;}
        }
        
        for(int i = 0; i<n; i++) {
            Cell[] row = model.get(ROW,i);
            for(int j = 0; j<m; j++) {
                Cell c = row[j];
                c.setFontSize(newFontSize);
            }
        }
        updateSizes();
//        pref = preferredLayoutSize(parent);
        pref = getBestSize();
        max = parent.getMaximumSize();
        if(pref.width>max.width || pref.height>max.height) {changeFontSize(max, pref, false);}
    }
    
    @Override
    public String toString() {
        return "TableLayout : model="+model.toString();
    }
    
    public void drawLines(Container parent, Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        int n = model.getRowCount(), m = model.getColumnCount();
        Insets insets = parent.getInsets();
        
        //verticales
        final int yStart = insets.top, yEnd = yStart+height();
        final int xStart = insets.left, xEnd = xStart+width();
        int x = xStart, y = yStart;
        g2D.drawLine(xStart, y, xEnd, y);
        for(int i = 0; i<n; i++) {
            y+=rowSize(i);
            g2D.drawLine(xStart, y, xEnd, y);
        }
        g2D.drawLine(x, yStart, x, yEnd);
        for(int j = 0; j<m; j++) {
            x+=columnSize(j);
            g2D.drawLine(x, yStart, x, yEnd);
        }
    }
    
    /** somme des hauteurs des lignes. ne compte pas les insets du parent **/
    public int height() {
        int n = model.getRowCount();
        int sum = 0;
        for(int i=0; i<n; i++) {
            sum+=rowSize(i);
        }
        return sum;
    }
    
    /** somme des largeurs des colonnes. ne compte pas les insets du parent **/
    public int width() {
        int m = model.getColumnCount();
        int sum = 0;
        for(int j=0; j<m; j++) {
            sum+=columnSize(j);
        }
        return sum;
    }
    
    public static interface TableModel extends Enregistrable {
        public static final boolean ROW = true;
        public static final boolean COLUMN = false;
        public Cell[] getAllCells();
        public Cell[] get(boolean line, int index);
        public Cell getCell(int row, int column);
        public int getRowCount();
        public int getColumnCount();
        public int getCount(boolean line);
        /** Crée une nouvelle ligne à l'index spécifié. Les lignes suivantes sont décalées **/
        public List<Cell> insert(boolean line, int i);
        public List<Cell> delete(boolean line, int i);
        public void addTableModelListener(TableModelListener l);
        public void removeTableModelListener(TableModelListener l);
        public int getMaxFontSize();
        public int getMinFontSize();
        public void clear();
        public Coord getCellCoordinates(Cell c);
        public Cell replaceCell(int row, int column, Cell c);
    }
    
    public static interface TableModelListener extends EventListener {
        public void rowInserted(Cell[] row, int index);
        public void columnInserted(Cell[] column, int index);
        public void rowDeleted(Cell[] row, int index);
        public void columnDeleted(Cell[] column, int index);
        public void contentEdited(Cell c, Object newContent);
        public void cleared(Cell[][] table);
        public void cellReplaced(Cell oldCell, Cell newCell);
        public void colorChanged(Color oldColor, Color newColor);
    }
    
    public static class Coord {
        public int ligne;
        public int colonne;
        public Coord(int ligne, int colonne) {
            this.ligne = ligne;this.colonne = colonne;
        }
        public Coord() {};
    }
    
    public abstract static class Cell extends JPanel implements Undoable, Editable, Enregistrable {
        /** propriété de changement de couleur de fond pour les cellules **/
        public static final String BACKGROUND_COLOR = "backgroundColorChanged";//attention : background est certainement utilisé par le système
        /** couleur du fond des cellules en temps normal **/
        public final Color BACKGROUND = ColorManager.get("color cell");
        /** couleur du fond des cellules sélectionnées **/
        public final Color FOCUSED_COLOR = ColorManager.get("color cell focused");
        /** couleur des cellules mises en valeur **/
        public final Color COLOR_1 = ColorManager.get("color cell color1");//TODO : ajouter d'autres couleurs
        /** proprité de changement de statut pour la cellule **/
        public static final String SELECTED_STATE = "cellSelected";//selected est probablement utilisé par le système
        /** proprité de changement de statut pour la cellule **/
        public static final String EDITING_STATE = "cellEditing";//selected est probablement utilisé par le système

        public abstract void setFontSize(int f);
        public abstract int getFontSize();
        public abstract boolean isEditing();
        public abstract void setEditing(boolean b);
        /** Renvoie la couleur enregistrée pour la cellule. **/
        public abstract Color getColor();
        /** définit la couleur enregistrée pour la cellule et la met en application si possible. **/
        public abstract void setColor(Color c);
        /** Applique la couleur sans changer la couleur enregistrée pour la cellule. **/
        public abstract void setBackgroundColor(Color c);
        public abstract boolean isSelected();
        /** Définit l'état de sélection. Un PropertyChangeEvent est envoyé après la modification **/
        public abstract void setSelected(boolean b);
        /** gets the textComponent currently used for editing **/
        public abstract JTextComponent getCellEditor();
        /** Définit l'éditeur permettant de modifier les styles pour la cellule **/
        public abstract void setEditeurKit(EditeurKit editeur);
        /** clears cell content **/
        public abstract void clear();
        /** discards the internals edits of this cell **/
        public abstract void discardEdits();
        
        public abstract void addContentEditListener(ContentEditListener listener);
        public abstract void removeContentEditListener(ContentEditListener listener);
        
        @Override
        public abstract DataTexte getDonnees();
    }
    
    public static interface ContentEditListener extends EventListener {
        public void contentEdited(Cell source);
    }
    
}
