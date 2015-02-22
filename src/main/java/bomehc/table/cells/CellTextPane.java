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

import bomehc.table.Table;
import bomehc.texte.composants.ComposantTexte;
import bomehc.texte.composants.JLabelText;
import bomehc.utils.managers.CursorManager;
import bomehc.utils.texte.JLimitedMathTextPane;
import java.awt.Component;
import java.awt.Cursor;
import bomehc.utils.texte.MathTools;
import net.sourceforge.jeuclid.swing.JMathComponent;

/**
     * Le JMathTextPane qui sert pour les cellules du tableau.
 * @author François Billioud
 */
public class CellTextPane extends JLimitedMathTextPane {

    private final Table parent;//La table qui contient cette cellule
    public CellTextPane(Table parent) {
        super(1,true);
        this.parent = parent;
        setForcageLigne(true);
        setOpaque(false);
//        setFont(getFont().deriveFont(20));
    }

    @Override
    public void setEditable(boolean b) {
        if(b==isEditable()) {return;}
        setFocusable(b);
        super.setEditable(b);
        setCursor(CursorManager.getCursor(b ? Cursor.TEXT_CURSOR : Cursor.DEFAULT_CURSOR));
        if(getCaret()!=null) {getCaret().setVisible(isFocusOwner());}
    }

    @Override
    public void setFontSize(int size) {
        int old = getFont().getSize();
        if(size==old) {return;}
        this.setFont(this.getFont().deriveFont((float) size));
        
        //change la taille des composants insérés
        for(Component c : componentMap.values()) {
            if(c instanceof JMathComponent) {MathTools.setFontSize((JMathComponent)c, size);}
            else if (c instanceof JLabelText) {((ComposantTexte)c).setFontSize(size);}
        }
    }
    
    @Override
    public void annuler() { if(isEditable()) {super.annuler();} else {parent.annuler();} }
    @Override
    public void refaire() { if(isEditable()) {super.refaire();} else {parent.refaire();} }
    @Override
    public boolean peutAnnuler() { return isEditable() ? super.peutAnnuler() : parent.peutAnnuler(); }
    @Override
    public boolean peutRefaire() { return isEditable() ? super.peutRefaire() : parent.peutRefaire(); }
    @Override
    public void copier() { if(isEditable()) {super.copier();} else {parent.copier();} }
    @Override
    public void coller() { if(isEditable()) {super.coller();} else {parent.coller();} }
    @Override
    public void couper() { if(isEditable()) {super.couper();} else {parent.couper();} }
    @Override
    public boolean peutCopier() { return isEditable() ? super.peutCopier() : parent.peutCopier(); }
    @Override
    public boolean peutColler() { return isEditable() ? super.peutColler() : parent.peutColler(); }
    @Override
    public boolean peutCouper() { return isEditable() ? super.peutCouper() : parent.peutCouper(); }
    @Override
    public boolean hasBeenModified() { return isEditable() ? super.hasBeenModified() : parent.hasBeenModified(); }

}

