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

package matheos.proportionality;

import java.awt.BasicStroke;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataObject;
import matheos.sauvegarde.DataTexte;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.Bouton;
import matheos.utils.librairies.DimensionTools;
import matheos.utils.objets.GeneralUndoManager;
import matheos.utils.texte.JLimitedMathTextPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import matheos.table.TableLayout;
import matheos.utils.objets.GlobalDispatcher;

/**
 *
 * @author François Billioud
 */
public class Fleche extends JLayeredPane implements Data.Enregistrable {
    
    private static final String START_INDEX = "startIndex";
    private static final String END_INDEX = "endIndex";
    private static final String CONTENT = "content";
    public static final int FONT_SIZE = 20;//La font-size des textFields des flèches
    public static final int MARGIN_LATERAL = 50;//Espace laissé sur les côtés pour dessiner les textFields des flèches à gauche et à droite
    public static final int MARGIN_VERTICAL = 20;//Espace laissé en haut et en bas pour dessiner les textFields des flèches du haut et du bas
    
    private final DessinFleche dessin = new DessinFleche();
    private JComponent topComponent;
    private final JMathField field = new JMathField();
    private final Bouton boutonSupprimer = new BoutonSuppression();
    private GeneralUndoManager undo;
    
    private ProportionalityTable table;
    private Data data;//Contient les données
    
    private static final boolean VERTICAL = true;
    private static final boolean HORIZONTAL = false;
    
//    private GeneralUndoManager undo;
    
    public enum ORIENTATION {
        HAUT(VERTICAL," up", ProportionalityTable.HAUT), GAUCHE(HORIZONTAL," left", ProportionalityTable.GAUCHE), BAS(VERTICAL," down", ProportionalityTable.BAS), DROITE(HORIZONTAL," right", ProportionalityTable.DROITE);
        private final boolean direction; private final String name; private final int orientationID;
        ORIENTATION(boolean b, String s, int i) {direction=b; name=s; orientationID=i;}
        public boolean getDirection() {return direction;}
        public String getBalise(String base) {return base+name;}
        public int getOrientationId() {return orientationID;}
        public boolean isVertical() {return direction==VERTICAL;}
    }
    
    private final ORIENTATION orientation;
    
    private int width;
    private int height;
    private int x;
    private int y;
    
    private final boolean hRev;
    private final boolean vRev;
    private final boolean rot;
    
    public Fleche(ORIENTATION orientation, Data data, ProportionalityTable table) {
        this.data = data;
        this.orientation = orientation;
        setTable(table);
        field.charger(getContent());
        
        flecheInit();
        
        hRev = getStartIndex()>getEndIndex();
        vRev = orientation==ORIENTATION.BAS || orientation==ORIENTATION.GAUCHE;
        rot = !orientation.isVertical();
    }
    public Fleche(ORIENTATION orientation, int start, int end, ProportionalityTable model) {
        this.data = new DataObject();
        this.orientation = orientation;
        setTable(model);
        setStartIndex(start);
        setEndIndex(end);
        
        flecheInit();
        
        hRev = start>end;
        vRev = orientation==ORIENTATION.BAS || orientation==ORIENTATION.GAUCHE;
        rot = !orientation.isVertical();
    }
    
    /** Détermine la table à laquelle est rattaché la flèche. Lors de la suppression, ce lien est rompu **/
    public void setTable(ProportionalityTable table) {
        if(this.table==table) {return;}
        if(this.table!=null) {this.table.removeTableModelListener(modelListener);}
        this.table = table;
        if(table!=null) {table.addTableModelListener(modelListener);}
    }
    
    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        field.setEnabled(b);
    }
    
    private final TableLayout.TableModelListener modelListener = new TableLayout.TableModelListener() {
        @Override
        public void rowInserted(TableLayout.Cell[] row, int index) {
            if(orientation.isVertical()) {return;}
            if(index<=getStartIndex()) {increase();}
            else if(index<=getEndIndex()) {increseEnd();}
        }
        @Override
        public void columnInserted(TableLayout.Cell[] column, int index) {
            if(!orientation.isVertical()) {return;}
            if(index<=getStartIndex()) {increase();}
            else if(index<=getEndIndex()) {increseEnd();}
        }
        @Override
        public void rowDeleted(TableLayout.Cell[] row, int index) {
            if(orientation.isVertical()) {return;}
            if(index==getStartIndex()||index==getEndIndex()) {supprimer();}
            else if(index<getStartIndex()) {decrease();}
            else if(index<getEndIndex()) {decreaseEnd();}
        }
        @Override
        public void columnDeleted(TableLayout.Cell[] column, int index) {
            if(!orientation.isVertical()) {return;}
            if(index==getStartIndex()||index==getEndIndex()) {supprimer();}
            else if(index<getStartIndex()) {decrease();}
            else if(index<getEndIndex()) {decreaseEnd();}
        }
        @Override
        public void contentEdited(TableLayout.Cell c, Object newContent) {}
        @Override
        public void cleared(TableLayout.Cell[][] table) {supprimer();}
        @Override
        public void cellReplaced(TableLayout.Cell oldCell, TableLayout.Cell newCell) {}
        @Override
        public void colorChanged(Color oldColor, Color newColor) {}
    };
    
    private void flecheInit() {
        setOpaque(false);
        add(dessin, JLayeredPane.PALETTE_LAYER);
        add(topComponent = field, JLayeredPane.MODAL_LAYER);
    }
                
    private void increase() {
        setStartIndex(getStartIndex()+1);
        increseEnd();
    }
    private void decrease() {
        setStartIndex(getStartIndex()-1);
        decreaseEnd();
    }
    private void decreaseEnd() {
        setEndIndex(getEndIndex()-1);
    }
    private void increseEnd() {
        setEndIndex(getEndIndex()+1);
    }
                
    private void supprimer() {
        table.deleteArrow(orientation.getOrientationId(), Fleche.this);
    }
    
    public int getOrientation() {return orientation.getOrientationId();}
    public int getStartIndex() {return Integer.parseInt(data.getElement(START_INDEX));}
    public int getEndIndex() {return Integer.parseInt(data.getElement(END_INDEX));}
    private DataTexte getContent() {
        Data content = data.getData(CONTENT);
        if(content instanceof DataTexte) {return (DataTexte)content;}
        else {DataTexte dataTexte = new DataTexte("");dataTexte.putAll(content);return dataTexte;}
    }
    
    private void setStartIndex(int i) {data.putElement(START_INDEX, i+"");}
    private void setEndIndex(int j) {data.putElement(END_INDEX, j+"");}
    
    public void setHeight(int height) {
        this.height = height;
        repaint();
    }
    
    public int getOtherSide(int extremite) {return extremite==getStartIndex() ? getEndIndex() : getStartIndex();}
    @Override
    public boolean equals(Object o) {
        if(o instanceof Fleche) {
            Fleche f = (Fleche)o;
            if(f.getOrientation()!=getOrientation()) {return false;}
            return (f.getStartIndex()==this.getStartIndex() && f.getEndIndex()==this.getEndIndex())
                    || (f.getStartIndex()==this.getEndIndex() && f.getEndIndex()==this.getStartIndex());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return orientation.getOrientationId();
    }
    
    void positionComponent() {
        if(getParent()==null || table==null) {return;}
        TableLayout.Coord coordDepart, coordArrivee;
        if(orientation.isVertical()) {
            int row = orientation==ORIENTATION.HAUT ? 0 : (table.getRowCount()-1);
            coordDepart = new TableLayout.Coord(row, getStartIndex());
            coordArrivee = new TableLayout.Coord(row, getEndIndex());
        } else {
            int column = orientation==ORIENTATION.GAUCHE ? 0 : (table.getColumnCount()-1);
            coordDepart = new TableLayout.Coord(getStartIndex(), column);
            coordArrivee = new TableLayout.Coord(getEndIndex(), column);
        }
        
        TableLayout.Cell depart = table.getCell(coordDepart.ligne, coordDepart.colonne);
        TableLayout.Cell arrivee = table.getCell(coordArrivee.ligne, coordArrivee.colonne);
        
        Point p1 = SwingUtilities.convertPoint(depart, depart.getWidth()/2, depart.getHeight()/2, getParent());
        Point p2 = SwingUtilities.convertPoint(arrivee, arrivee.getWidth()/2, arrivee.getHeight()/2, getParent());
        //p1 et p2 désignent maintenant le centre des 2 cellules
        Point translation;
        switch(orientation) {
            case HAUT : translation = new Point(0,-depart.getHeight()/2-height-MARGIN_VERTICAL); break;
            case BAS : translation = new Point(0,depart.getHeight()/2); break;
            case GAUCHE : translation = new Point(-depart.getWidth()/2-height-MARGIN_LATERAL,0); break;
            case DROITE : translation = new Point(depart.getWidth()/2,0); break;
            default:translation = new Point(0,0);
        }
        p1.translate(translation.x, translation.y);
        p2.translate(translation.x, translation.y);
        //p1 et p2 sont maintenant les points au centre au-dessus de la cellule cible. Il new reste plus qu'à dessiner
        int xMin = Math.min(p1.x, p2.x), yMin = Math.min(p1.y, p2.y);
        int xMax = Math.max(p1.x, p2.x), yMax = Math.max(p1.y, p2.y);
        width = orientation.isVertical() ? xMax - xMin : yMax - yMin;
        width+=height;//RAPPEL : height correspond à la largeur des extrémités de la fleche. Ceci permet donc de centrer les fleches en faisant dépasser le dessin
        
        x = xMin; y=yMin;
        if(orientation.isVertical()) {x-=height/2;} else {y-=height/2;}//Ceci permet donc de centrer les fleches en faisant dépasser le dessin
        if(rot) {super.setSize(height+MARGIN_LATERAL, width);dessin.setSize(height, width);} else {super.setSize(width, height+MARGIN_VERTICAL);dessin.setSize(width,height);}
        setLocation(x, y);

        topComponent.setSize(topComponent.getPreferredSize());
        positionneDessin();
        positionneTopComponent();
    }
    
    private void positionneDessin() {
        Point offset = new Point(0,0);
        switch(orientation) {
            case HAUT : offset.translate(0, MARGIN_VERTICAL); break;
            case GAUCHE : offset.translate(MARGIN_LATERAL, 0); break;
        }
        dessin.setLocation(offset);
    }

    private void positionneTopComponent() {
        JComponent c = topComponent;
        switch(orientation) {
            case HAUT : c.setLocation(width/2-c.getWidth()/2,MARGIN_VERTICAL/2); break;
            case BAS : c.setLocation(width/2-c.getWidth()/2,height+MARGIN_VERTICAL/2-c.getHeight()); break;
            case GAUCHE : c.setLocation(Math.max(0, height+MARGIN_LATERAL/2-c.getWidth()),width/2-c.getHeight()/2); break;
            case DROITE : c.setLocation(height-MARGIN_LATERAL/2,width/2-c.getHeight()/2); break;
        }
    }
        
    @Override
    public void setSize(int i, int j) {
        super.setSize(i, j);
        dessin.setSize(i, j);
        width = i; height = j;
        revalidate();
    }

    @Override
    public void charger(Data data) {
        this.data = data;
        field.charger(data);
    }

    @Override
    public Data getDonnees() {
        data.putData(CONTENT, field.getDonnees());
        return data.clone();
    }
    
    public void setUndoManager(GeneralUndoManager undo) {
        if(undo==this.undo) {return;}
        this.undo = undo;
        field.getDocument().addUndoableEditListener(undo);
    }
    
    private class BoutonSuppression extends Bouton {
        private BoutonSuppression() {
            super(new ActionComplete("table suppression") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ProportionalityTable m = Fleche.this.table;//Le model sera retiré par l'instruction suivante
                    supprimer();
                    undo.addEdit(m.new ArrowDeletedEdit(Fleche.this));
                }
            });
            addMouseListener(new GlobalDispatcher(Fleche.this) {
                @Override
                public boolean canDispatch(ComponentEvent e) {return true;}
            });
        }
        @Override
        public Dimension getPreferredSize() {return new Dimension(height, height);}
    }
    
    private boolean modeEdition = true;
    void setModeSuppression() {
        if(!modeEdition) {return;}
        remove(topComponent);
        topComponent = boutonSupprimer;
        boutonSupprimer.setSize(40, 40);
        add(topComponent,JLayeredPane.MODAL_LAYER);
        revalidate();
        modeEdition = false;
    }
    
    void setModeEdition() {
        if(modeEdition) {return;}
        remove(topComponent);
        topComponent = field;
        add(topComponent,JLayeredPane.MODAL_LAYER);
        revalidate();
        modeEdition = true;
    }
    
    private class JMathField extends JLimitedMathTextPane {
        private JMathField() {
            super(1, 5);
            setFontSize(FONT_SIZE);
            setSize(getMinimumSize());
            setAlignmentCenter(true);
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            addMouseListener(new GlobalDispatcher(Fleche.this) {
                @Override
                public boolean canDispatch(ComponentEvent e) {return true;}
            });
        }
        @Override
        public DimensionTools.DimensionT getMaximumSize() {
            return getPreferredSize().max(getMinimumSize());
        }
    }

    private class DessinFleche extends JPanel {

        DessinFleche() {
            setOpaque(false);
            addMouseListener(new GlobalDispatcher(Fleche.this) {
                @Override
                public boolean canDispatch(ComponentEvent e) {return true;}
            });
        }
        
        private Dimension previousSize = new Dimension(0,0);
        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D) g;
            boolean showMiddle = width-height>0;
            if(!previousSize.equals(getSize())) {
                previousSize = getSize();
            }
            if(rot) {
                g2D.rotate(Math.PI/2);
                g2D.translate(0, -height);
            }
            if(hRev) {
                g2D.translate(width, 0);
                g2D.scale(-1, 1);
            }
            if(vRev) {
                g2D.translate(0, height);
                g2D.scale(1, -1);
            }
            int start = height/4;
            int hauteur = height*2/3;
            int largeur = height-start;
            int yCoordTop = height-hauteur;
            g2D.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2D.drawArc(start, height-hauteur, largeur*2, 2*hauteur, 180, -90);
            if(showMiddle) {g2D.drawLine(start+largeur, height-hauteur, width-height, height-hauteur);}
            g2D.drawArc(width-start-2*largeur, height-hauteur, largeur*2, 2*hauteur, 90, -90);
            g2D.drawLine(width-start, height, width-start+height/6, height*3/4);
            g2D.drawLine(width-start, height, width-start-height/4, height*3/4);
            super.paintComponent(g);
        }
    }
}