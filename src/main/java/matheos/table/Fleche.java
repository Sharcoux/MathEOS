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

import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataObject;
import matheos.sauvegarde.DataTexte;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.Bouton;
import matheos.utils.librairies.DimensionTools;
import matheos.utils.managers.GeneralUndoManager;
import matheos.utils.managers.ImageManager;
import matheos.utils.objets.Icone;
import matheos.utils.texte.JLimitedMathTextPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author François Billioud
 */
public class Fleche extends JLayeredPane implements Data.Enregistrable {
    
    private static final String START_INDEX = "startIndex";
    private static final String END_INDEX = "endIndex";
    private static final String CONTENT = "content";
    
    private final DessinFleche dessin = new DessinFleche();
    private JComponent topComponent;
    private final JMathField field = new JMathField();
    private final Bouton boutonSupprimer = new BoutonSuppression();
    private GeneralUndoManager undo;
    
    private Model model;
    private Data data;//Contient les données
    
    private final SidePanel.ORIENTATION orientation;
    
    private int width;
    private int height;
    private int x;
    private int y;
    
    private final boolean hRev;
    private final boolean vRev;
    private final boolean rot;
    
    public Fleche(SidePanel.ORIENTATION orientation, Data data, Model model) {
        this.data = data;
        this.orientation = orientation;
        setModel(model);
        field.charger(getContent());
        
        flecheInit();
        
        hRev = getStartIndex()>getEndIndex();
        vRev = orientation==SidePanel.ORIENTATION.BAS || orientation==SidePanel.ORIENTATION.GAUCHE;
        rot = !orientation.isVertical();
    }
    public Fleche(SidePanel.ORIENTATION orientation, int start, int end, Model model) {
        this.data = new DataObject();
        this.orientation = orientation;
        setModel(model);
        setStartIndex(start);
        setEndIndex(end);
        
        flecheInit();
        
        hRev = start>end;
        vRev = orientation==SidePanel.ORIENTATION.BAS || orientation==SidePanel.ORIENTATION.GAUCHE;
        rot = !orientation.isVertical();
    }
    
    /** Détermine le model auquel est rattaché la flèche. Lors de la suppression, ce lien est rompu **/
    public void setModel(Model model) {
        if(this.model==model) {return;}
        if(this.model!=null) {this.model.removeTableModelListener(modelListener);}
        this.model = model;
        if(model!=null) {model.addTableModelListener(modelListener);}
    }
    
    private final TableLayout.TableModelListener modelListener = new TableLayout.TableModelListener() {
        @Override
        public void rowInserted(TableLayout.Cell[] row, int index) {
            if(index<=getStartIndex()) {increase();}
            else if(index<=getEndIndex()) {increseEnd();}
        }
        @Override
        public void columnInserted(TableLayout.Cell[] column, int index) {
            if(index<=getStartIndex()) {increase();}
            else if(index<=getEndIndex()) {increseEnd();}
        }
        @Override
        public void rowDeleted(TableLayout.Cell[] row, int index) {
            if(index==getStartIndex()||index==getEndIndex()) {supprimer();}
            else if(index<getStartIndex()) {decrease();}
            else if(index<getEndIndex()) {decreaseEnd();}
        }
        @Override
        public void columnDeleted(TableLayout.Cell[] column, int index) {
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
        model.deleteArrow(orientation.getOrientationId(), Fleche.this);
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
        return orientation.getOrientationId()+29*((this.getStartIndex() + this.getEndIndex())*29+7);
    }
    
    void positionComponent() {
        if(getParent()==null || model==null) {return;}
        Model.Coord coordDepart, coordArrivee;
        if(orientation.isVertical()) {
            int row = orientation==SidePanel.ORIENTATION.HAUT ? 0 : (model.getRowCount()-1);
            coordDepart = new Model.Coord(row, getStartIndex());
            coordArrivee = new Model.Coord(row, getEndIndex());
        } else {
            int column = orientation==SidePanel.ORIENTATION.GAUCHE ? 0 : (model.getColumnCount()-1);
            coordDepart = new Model.Coord(getStartIndex(), column);
            coordArrivee = new Model.Coord(getEndIndex(), column);
        }
        
        TableLayout.Cell depart = model.getCell(coordDepart.ligne, coordDepart.colonne);
        TableLayout.Cell arrivee = model.getCell(coordArrivee.ligne, coordArrivee.colonne);
        
        Point p1 = SwingUtilities.convertPoint(depart, depart.getWidth()/2, depart.getHeight()/2, getParent());
        Point p2 = SwingUtilities.convertPoint(arrivee, arrivee.getWidth()/2, arrivee.getHeight()/2, getParent());
        Point translation;
        switch(orientation) {
            case HAUT : translation = new Point(0,-depart.getHeight()/2-height); break;
            case BAS : translation = new Point(0,depart.getHeight()/2); break;
            case GAUCHE : translation = new Point(-depart.getWidth()/2-height,0); break;
            case DROITE : translation = new Point(depart.getWidth()/2,0); break;
            default:translation = new Point(0,0);
        }
        p1.translate(translation.x, translation.y);
        p2.translate(translation.x, translation.y);
        //p1 et p2 sont les points au centre au-dessus de la cellule cible. Il new reste plus qu'à dessiner
        int xMin = Math.min(p1.x, p2.x), yMin = Math.min(p1.y, p2.y);
        int xMax = Math.max(p1.x, p2.x), yMax = Math.max(p1.y, p2.y);
        width = orientation.isVertical() ? xMax - xMin : yMax - yMin;
        width+=height;//RAPPEL : height correspond à la largeur des extrémités de la fleche. Ceci permet donc de centrer les fleches en faisant dépasser le dessin
        
        x = xMin; y=yMin;
        if(orientation.isVertical()) {x-=height/2;} else {y-=height/2;}//Ceci permet donc de centrer les fleches en faisant dépasser le dessin
        if(rot) {super.setSize(2*height, width);dessin.setSize(height, width);} else {super.setSize(width, 2*height);dessin.setSize(width,height);}
        Point offset = positionDessinAboveSupport();
        
        //Position the topComponent
        topComponent.setSize(topComponent.getPreferredSize());
        Point p = dessin.getPositionForComponents(topComponent);
        p.translate(offset.x, offset.y);
        topComponent.setLocation(p);
        
//        repaint();
    }
    
    private Point positionDessinAboveSupport() {
        Point offset = new Point(0,0);
        switch(orientation) {
            case HAUT : setLocation(x, y-height);dessin.setLocation(0, height);offset.translate(0, height); break;
            case BAS : setLocation(x, y);dessin.setLocation(0, 0); break;
            case GAUCHE : setLocation(x-height, y);dessin.setLocation(height, 0);offset.translate(height, 0); break;
            case DROITE : setLocation(x, y);dessin.setLocation(0, 0); break;
        }
        return offset;
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
        return data;
    }
    
    public void setUndoManager(GeneralUndoManager undo) {
        if(undo==this.undo) {return;}
        this.undo = undo;
        field.getDocument().addUndoableEditListener(undo);
    }
    
    private class BoutonSuppression extends Bouton {
        private BoutonSuppression() {super(new ActionComplete("table suppression") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Model m = Fleche.this.model;//Le model sera retiré par l'instruction suivante
                supprimer();
                undo.addEdit(new TableEdits.ArrowDeletedEdit(Fleche.this, m));
            }
        });}
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
        modeEdition = true;
    }
    
    void setModeEdition() {
        if(modeEdition) {return;}
        remove(topComponent);
        topComponent = field;
        add(topComponent,JLayeredPane.MODAL_LAYER);
        revalidate();
        modeEdition = false;
    }
    
    private class JMathField extends JLimitedMathTextPane {
        private JMathField() {
            super(1, 5);
            setFontSize(20);
            setSize(getMinimumSize());
            setAlignmentCenter(true);
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        }
        @Override
        public DimensionTools.DimensionT getMaximumSize() {
            return getPreferredSize().max(getMinimumSize());
        }
    }

    private class DessinFleche extends JPanel {
        private Icone debutIcone = (ImageManager.getIcone("table prop arrow start"));
        private Icone milieuIcone = (ImageManager.getIcone("table prop arrow middle"));
        private Icone finIcone = (ImageManager.getIcone("table prop arrow end"));

        DessinFleche() {
            setOpaque(false);
        }
        
        Point getPositionForComponents(JComponent c) {
            Point p = new Point(0,0);
            switch(orientation) {
                case HAUT : p = new Point(width/2-c.getWidth()/2,-c.getHeight()/2); break;
                case BAS : p = new Point(width/2-c.getWidth()/2,height-c.getHeight()/2); break;
                case GAUCHE : p = new Point(-c.getWidth()/2,width/2-c.getHeight()/2); break;
                case DROITE : p = new Point(height-c.getWidth()/2,width/2-c.getHeight()/2); break;
            }
            return p;
        }
        
        private Dimension previousSize = new Dimension(0,0);
        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D) g;
            boolean showMiddle = width-2*height>0;
            if(!previousSize.equals(getSize())) {
                debutIcone = ImageManager.getIcone("table prop arrow start", height, height);
                if(showMiddle) milieuIcone = ImageManager.getIcone("table prop arrow middle", width-2*height, height);
                finIcone = ImageManager.getIcone("table prop arrow end", height, height);
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
            g2D.drawImage(debutIcone.getImage(), 0, 0, this);
            if(showMiddle) g2D.drawImage(milieuIcone.getImage(), height, 0, this);
            g2D.drawImage(finIcone.getImage(), width-height, 0, this);
            super.paintComponent(g);
        }
    }
}

