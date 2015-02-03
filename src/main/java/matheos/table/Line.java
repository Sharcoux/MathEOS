/**
 * Copyright (C) 2015 François Billioud
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
 * These additional terms refer to the source code of bomehc.
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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.html.HTMLEditorKit;
import matheos.json.Json;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.Data.Enregistrable;
import matheos.sauvegarde.DataObject;
import matheos.table.TableLayout.Cell;

/**
 *
 * @author François Billioud
 */
public abstract class Line extends ArrayList<Cell> implements Enregistrable {
    
    public static final String BACKGROUND_COLOR = "background";
    public static final String FOREGROUND_COLOR = "foreground";
    public static final String BOLD = "bold";
    public static final String ITALIC = "italic";
    public static final String UNDERLINED = "undelined";
    public static final String ALIGNMENT = "alignment";
    
    private final Data data;
    @Override
    public Data getDonnees() {return data.clone();}
    @Override
    public void charger(Data data) {this.data.clear();this.data.putAll(data);}
    public Color getBackground() {
        if(data.getElement(BACKGROUND_COLOR)==null) {return null;}
        Color c = null;
        try {
            c = (Color) Json.toJava(data.getElement(BACKGROUND_COLOR),Color.class);
        } catch (IOException ex) {
            Logger.getLogger(Line.class.getName()).log(Level.SEVERE, null, ex);
        }
        return c;
    }
    public void setBackground(Color couleur) {
        Color old = getBackground();
        if(couleur==null) {
            data.removeElementByKey(BACKGROUND_COLOR);
            for(Cell c : this) {c.setColor(c.BACKGROUND);}
        } else {
            try {
                data.putElement(BACKGROUND_COLOR, Json.toJson(couleur));
                // for(Cell c : this) {c.setBackgroundManual(couleur);}//A utiliser pour donner une couleur différente aux cellules
                for(Cell c : this) {c.setColor(couleur);}
            } catch (IOException ex) {
                Logger.getLogger(Line.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public Color getForeground() {
        if(data.getElement(FOREGROUND_COLOR)==null) {return null;}
        Color c = null;
        try {
            c = (Color) Json.toJava(data.getElement(FOREGROUND_COLOR),Color.class);
        } catch (IOException ex) {
            Logger.getLogger(Line.class.getName()).log(Level.SEVERE, null, ex);
        }
        return c;
    }
    public void setForeground(Color couleur) {
        try {
                data.putElement(FOREGROUND_COLOR, Json.toJson(couleur));
        } catch (IOException ex) {
            Logger.getLogger(Line.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void setBold(boolean bold) { data.putElement(BOLD, bold+""); }
    public void setItalic(boolean italic) { data.putElement(ITALIC, italic+""); }
    public void setUnderlined(boolean underlined) { data.putElement(UNDERLINED, underlined+""); }
    public void setAlignment(int alignment) { data.putElement(ALIGNMENT, alignment+""); }
    public boolean isBold() {
        String s = data.getElement(BOLD);
        return s!=null && Boolean.parseBoolean(s);
    }
    public boolean isItalic() {
        String s = data.getElement(ITALIC);
        return s!=null && Boolean.parseBoolean(s);
    }
    public boolean isUnderlined() {
        String s = data.getElement(UNDERLINED);
        return s!=null && Boolean.parseBoolean(s);
    }
    public int getAlignment() {
        String s = data.getElement(ALIGNMENT);
        return s==null ? 0 : Integer.parseInt(s);
    }
    /**
    * Crée une ligne ou colonne en initialisant les données des flèches et des styles. les cellules ne sont pas concernées
    * @param data contient les informations de style propres à la ligne, et les indices des flèches
    */
    private Line() {data = new DataObject();}
    public void applyStyleToCell(Cell c) {
        Color color = getBackground();
        if(color!=null) { c.setColor(color); }
        ActionEvent e = new ActionEvent(c, ActionEvent.ACTION_PERFORMED, "newCell");
        color = getForeground();
        if(color!=null) {new HTMLEditorKit.ForegroundAction("foreground", color).actionPerformed(e);}
        if(isBold()) {new HTMLEditorKit.BoldAction().actionPerformed(e);}
        if(isItalic()) {new HTMLEditorKit.ItalicAction().actionPerformed(e);}
        if(isUnderlined()) {new HTMLEditorKit.UnderlineAction().actionPerformed(e);}
        new HTMLEditorKit.AlignmentAction("alignment", getAlignment()).actionPerformed(e);
    }
    
    public static class Row extends Line {}
    public static class Column extends Line {}
}
