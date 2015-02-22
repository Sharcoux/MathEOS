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

package bomehc.table.cells;

import bomehc.json.Json;
import bomehc.sauvegarde.Data;
import bomehc.sauvegarde.DataTexte;
import bomehc.table.Table;
import static bomehc.table.TableLayout.Cell.BACKGROUND_COLOR;
import bomehc.utils.librairies.DimensionTools.DimensionT;
import bomehc.utils.texte.JLimitedMathTextPane;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Cellule classique contenant un unique textPane.
 * @author François Billioud
 */
public class BasicCell extends MultipleTextPaneCell {
    private final JLimitedMathTextPane cellTextPane;
    private BasicCell(JLimitedMathTextPane txt, Table tableOwner) {
        super(tableOwner, txt);
        cellTextPane = txt;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        txt.setAlignmentX(LEFT_ALIGNMENT);
        add(Box.createVerticalGlue());
        add(txt);
        add(Box.createVerticalGlue());
        setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
        txt.setMaximumSize(txt.getMinimumSize());
        txt.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {updateMaxSize();}
            @Override
            public void removeUpdate(DocumentEvent e) {updateMaxSize();}
            @Override
            public void changedUpdate(DocumentEvent e) {updateMaxSize();}
            private void updateMaxSize() {
                DimensionT dim = new DimensionT(BasicCell.this.getWidth(),cellTextPane.getPreferredHeight());
                cellTextPane.setMaximumSize(dim.max(cellTextPane.getMinimumSize()));
            }
        });
    }
    public BasicCell(Table c) { this(new CellTextPane(c), c); }

    @Override
    public void charger(Data data) {
        cellTextPane.charger(data);

        //lit la couleur
        String s = data.getElement(BACKGROUND_COLOR);
        if(s!=null) {
            try {
                Color c = (Color) Json.toJava(s, Color.class);
                setColor(c);
            } catch (IOException ex) {
                Logger.getLogger(CellTextPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public DataTexte getDonnees() {
        DataTexte data = cellTextPane.getDonnees();
        Color c = getColor();
        if(c==null) {return data;}
        try {
            data.putElement(BACKGROUND_COLOR, Json.toJson(getColor()));
        } catch (IOException ex) {
            Logger.getLogger(CellTextPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
    @Override
    public DimensionT getPreferredSize() {
        return cellTextPane.getPreferredSize().plus(getBorder().getBorderInsets(this));
//        return cellTextPane.getPreferredSize().plus(0,1);
    }
    @Override
    public DimensionT getMinimumSize() {
        return cellTextPane.getMinimumSize().plus(getBorder().getBorderInsets(this)).plus(cellTextPane.getMargin());
//        return cellTextPane.getMinimumSize().plus(0,1);
    }
    
    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
        cellTextPane.setSize(cellTextPane.getPreferredSize());
    }
}
