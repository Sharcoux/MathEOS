/**
 * Copyright (C) 2015 François Billioud
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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import bomehc.sauvegarde.DataTexte;
import bomehc.utils.interfaces.Editable;
import static bomehc.utils.interfaces.Editable.PEUT_COPIER;
import static bomehc.utils.interfaces.Editable.PEUT_COUPER;
import bomehc.utils.librairies.TransferableTools;

/**
 * Cette classe gère tout ce qui a trait à la sélection de cases dans le tableau.
 *
 * @author François Billioud
 */
public class Selection implements Editable {
    private int gauche;
    private int haut;
    private int droite;
    private int bas;
    private TableLayout.Coord depart = null;
    private TableLayout.Coord arrivee = null;
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); // Presse papier pour le copier-coller

    private final Table table;
    
    public Selection(Table table) {
        this.table = table;
    }

    private void ordonneColonnes(int x1, int x2) {
        if(x1<x2) {
            gauche = x1; droite = x2;
        } else {
            gauche = x2; droite = x1;
        }
    }
    private void ordonneLignes(int y1, int y2) {
        if(y1<y2) {
            haut = y1; bas = y2;
        } else {
            haut = y2; bas = y1;
        }
    }

    public boolean isMultiple() {return (gauche!=droite || haut!=bas);}

    public void set(TableLayout.Cell depart, TableLayout.Cell arrivee) {
        TableLayout.Coord origine = depart==null ? null : table.getCellCoordinates(depart);
        TableLayout.Coord destination = arrivee==null ? null : table.getCellCoordinates(arrivee);
        set(origine, destination);
    }

    public void set(TableLayout.Cell selectedCell) {
        TableLayout.Coord p = selectedCell==null ? null : table.getCellCoordinates(selectedCell);
        set(p,p);
    }

    private void set(TableLayout.Coord origine, TableLayout.Coord destination) {
        select(false);
        setDepart(origine);
        setArrivee(destination);
        select(true);
        table.repaint();//pour redessiner les lignes
    }

    private void setDepart(TableLayout.Coord p) {
        boolean isDepartNull = depart==null;
        if(p!=null) {
//                if(arrivee!=null) {//On n'est pas sencé positionner le départ après l'arrivée
//                    ordonneLignes(p.ligne, arrivee.ligne);
//                    ordonneColonnes(p.colonne, arrivee.colonne);
//                } else {
                gauche = p.colonne; droite = p.colonne; haut = p.ligne; bas = p.ligne;
//                }
        } else {
            gauche = droite = haut = bas = 0;
        }
        this.depart = p;
        if(isDepartNull != (depart==null)) {
            support.firePropertyChange(PEUT_COUPER, !isDepartNull, isDepartNull);
            support.firePropertyChange(PEUT_COPIER, !isDepartNull, isDepartNull);
        }
    }
    public void setDepart(TableLayout.Cell c) {
        select(false);
        TableLayout.Coord p = table.getCellCoordinates(c);
        setDepart(p);
        select(true);
        table.repaint();//pour redessiner les lignes
    }
    public TableLayout.Coord getDepart() {return depart;}
    public TableLayout.Coord getArrivee() {return arrivee;}

    private void setArrivee(TableLayout.Coord p) {
        if(p!=null) {//depart n'est pas null normalement. L'arrivée n'est pas sensé être définie avant le départ
            ordonneLignes(depart.ligne, p.ligne);
            ordonneColonnes(depart.colonne, p.colonne);
        } else {
            if(depart==null) {
                droite = gauche-1; bas = haut-1;//permet de ne pas passer dans les boucles for
            } else {
                droite = gauche; bas = haut;
            }
        }
        this.arrivee = p;
    }
    public void setArrivee(TableLayout.Cell c) {
        select(false);
        TableLayout.Coord p = table.getCellCoordinates(c);
        setArrivee(p);
        select(true);
        table.repaint();//pour redessiner les lignes
    }

    private void select(boolean b) {
        for(int i=haut; i<=bas; i++) {
            for(int j=gauche; j<=droite; j++) {
                if(!b || !table.isColoring()) table.getCell(i, j).setSelected(b);//En mode coloriage, le focus empeche de voir
            }
        }
    }

    public void clearSelection() {
        set(null);
    }

    public void clearContent() {
        DataTexte[][] previousContent = new DataTexte[bas-haut+1][droite-gauche+1];
        for(int i=haut; i<=bas; i++) {
            for(int j=gauche; j<=droite; j++) {
                TableLayout.Cell c = table.getCell(i, j);
                previousContent[i-haut][j-gauche] = c.getDonnees();
                c.clear();
                c.discardEdits();
            }
        }
        table.getUndoManager().addEdit(new TableEdits.ClearContentEdit(depart, previousContent, table));
    }

    @Override
    public void couper() {
        copier();
        clearContent();
    }
    @Override
    public void copier() {
        Transferable transfert;
        if(isMultiple()) {
            int n = bas-haut+1, m=droite-gauche+1;
            TableLayout.Cell[][] textPanes = new TableLayout.Cell[n][m];
            for(int i=0; i<n; i++) {
                for(int j=0; j<m; j++) {
                    textPanes[i][j] = table.getCell(i+haut,j+gauche);
                }
            }
            transfert = TransferableTools.createTransferableDataTexteArray(textPanes);
        } else {
            TableLayout.Cell c = table.getCell(depart.ligne, depart.colonne);
            transfert = TransferableTools.createTransferableDataTexte(c.getDonnees());
        }
        try {
            clipboard.setContents(transfert, null);
        } catch (IllegalStateException e1) {}
    }
    @Override
    public void coller() {
        try {
            if(clipboard.isDataFlavorAvailable(TransferableTools.bomehcArrayFlavor)) {//copie d'un tableau de cases
                DataTexte[][] data = (DataTexte[][]) clipboard.getData(TransferableTools.bomehcArrayFlavor);
                int n=data.length, m=data[0].length;
                int nMax=table.getRowCount(), mMax=table.getColumnCount();
                int nFinal = Math.min(nMax-depart.ligne, n), mFinal = Math.min(mMax-depart.colonne, m);
                DataTexte[][] oldData = new DataTexte[nFinal][mFinal];
                DataTexte[][] newData = new DataTexte[nFinal][mFinal];
                for(int i=0; i<nFinal; i++) {
                    for(int j=0; j<mFinal; j++) {
                        TableLayout.Cell c = table.getCell(i+depart.ligne, j+depart.colonne);
                        newData[i][j] = data[i][j];
                        oldData[i][j] = c.getDonnees();
                        c.charger(data[i][j]);
                    }
                }
                table.getUndoManager().addEdit(new TableEdits.ReplaceContentEdit(depart, oldData, newData, table));
            } else {//copie d'un contenu d'une case dans une succession d'autres
                for(int i=haut; i<=bas; i++) {
                    for(int j=gauche; j<=droite; j++) {
                        TableLayout.Cell c = table.getCell(i, j);
                        DataTexte oldData = c.getDonnees(), newData;
                        c.clear();
                        if(clipboard.isDataFlavorAvailable(TransferableTools.bomehcFlavor)) {
                            newData = (DataTexte) clipboard.getData(TransferableTools.bomehcFlavor);
                            c.charger(newData);
                        } else if(clipboard.isDataFlavorAvailable(TransferableTools.htmlFlavor)) {
                            String html = (String) clipboard.getData(TransferableTools.htmlFlavor);
                            c.charger(new DataTexte(html));
//                                EditeurIO.importHtml(c, html, 0);
                            newData = c.getDonnees();
                        } else if(clipboard.isDataFlavorAvailable(TransferableTools.textFlavor)) {
                            String text = (String) clipboard.getData(TransferableTools.textFlavor);
                            c.charger(new DataTexte(text));
                            newData = c.getDonnees();
                        } else {
                            newData = new DataTexte("");
                        }
                        table.getUndoManager().addEdit(new TableEdits.ContentEdit(c, oldData, newData));
                    }
                }
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean peutCouper() {return depart!=null;}
    @Override
    public boolean peutCopier() {return depart!=null;}
    @Override
    public boolean peutColler() {return depart!=null && !table.isEditing();}

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {support.addPropertyChangeListener(listener);}
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {support.removePropertyChangeListener(listener);}
}
