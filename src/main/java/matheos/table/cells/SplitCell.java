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

package matheos.table.cells;

import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataTexte;
import matheos.table.Table;
import matheos.utils.librairies.DimensionTools.DimensionT;
import matheos.utils.managers.ColorManager;
import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.texte.JMathTextPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.SpringLayout;
import javax.swing.border.Border;

/**
 * Cellule divisée par un diagonale et qui contient 2 textPanes.
 * @author François Billioud
 */
public class SplitCell extends MultipleTextPaneCell {
    public static final String SECOND_TEXT = "secondText";

    private final JLimitedMathTextPane texteHaut;
    private final JLimitedMathTextPane texteBas;

    private SplitCell(Table tableOwner, JLimitedMathTextPane texteHaut, JLimitedMathTextPane texteBas) {
        super(tableOwner, texteHaut, texteBas);
        this.texteHaut = texteHaut;
        this.texteBas = texteBas;

        texteHaut.addFocusListener(borderAdapter);
        texteBas.addFocusListener(borderAdapter);

        SpringLayout layout = new SpringLayout();
        setLayout(layout);
        add(texteHaut);
        add(texteBas);
        layout.putConstraint(SpringLayout.NORTH, texteHaut, 2, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, texteHaut, -15, SpringLayout.EAST, this);
        layout.putConstraint(SpringLayout.WEST, texteBas, 2, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.SOUTH, texteBas, 2, SpringLayout.SOUTH, this);

        //aligne à droite le texte du haut
//                texteHaut.getHTMLdoc().removeUndoableEditListener(texteHaut.getUndo());
//                StyledEditorKit.AlignmentAction alignementCentre = new HTMLEditorKit.AlignmentAction("right", StyleConstants.ALIGN_RIGHT);
//                alignementCentre.actionPerformed(new ActionEvent(this, 0, ""));
//                texteHaut.getHTMLdoc().addUndoableEditListener(texteHaut.getUndo());

    }
    public SplitCell(Table tableOwner) {
        this(tableOwner, new CellTextPane(tableOwner), new CellTextPane(tableOwner));
    }

    private final FocusAdapter borderAdapter = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            setCorrectBorder((JMathTextPane) e.getComponent());
        }
        @Override
        public void focusLost(FocusEvent e) {
            setCorrectBorder((JMathTextPane) e.getComponent());
        }
    };
    private void setCorrectBorder(JMathTextPane txt) {
        if(txt==null) {return;}
        Border b = null;
        if(txt.getLength()==0) {b = BorderFactory.createLineBorder(Color.BLACK, 1);}
        if(isEditing() && txt.isFocusOwner()) {b = BorderFactory.createLineBorder(ColorManager.get("color cell editing border"), 2);}
        txt.setBorder(b);
    }

    @Override
    public DimensionT getMinimumSize() {
        DimensionT haut = texteHaut.getMinimumSize(), bas = texteBas.getMinimumSize();
        return new DimensionT(Math.max(haut.width+texteHaut.getX(), bas.width+texteBas.getX()),(int)((haut.height+bas.height)*1.5));
    }
    @Override
    public DimensionT getPreferredSize() {
        DimensionT haut = texteHaut.getPreferredSize(), bas = texteBas.getPreferredSize();
        int positionHaut = (int) Math.max(texteHaut.getX(), getWidth()*0.2);
        return new DimensionT(Math.max(haut.width+positionHaut, bas.width+texteBas.getX()),(int) ((haut.height+bas.height)*1.5));
    }
    @Override
    public void setFontSize(int size) {
        super.setFontSize((int) (size*0.7));
    }
    @Override
    public int getFontSize() {
        return (int) (super.getFontSize()/0.7);
    }
    @Override
    public Dimension getMaximumSize() {
        return texteBas.getMaximumSize();
    }

    //Dessine la séparation diagonale
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawLine(0, 0, getWidth(), getHeight());
    }

    @Override
    public void setEditing(boolean b) {
//            setFocusable(!b);
        super.setEditing(b);
        setCorrectBorder(texteHaut);
        setCorrectBorder(texteBas);
    }

    @Override
    public DataTexte getDonnees() {
        DataTexte data = texteHaut.getDonnees();
        data.putData(SECOND_TEXT, texteBas.getDonnees());
        return data;
    }

    @Override
    public void charger(Data data) {
        DataTexte dataTexte;
        if(data instanceof DataTexte) {dataTexte = (DataTexte) data;}
        else {dataTexte = new DataTexte("");dataTexte.putAll(data);}

        Data dataTexteBas = dataTexte.getData(SECOND_TEXT);
        texteBas.charger(dataTexteBas);
        dataTexte.removeDataByKey(SECOND_TEXT);
        texteHaut.charger(dataTexte);
    }

}
