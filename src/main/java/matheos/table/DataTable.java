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

import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataObject;
import matheos.sauvegarde.DataTexte;
import static matheos.table.TableLayout.TableModel.ROW;

/**
 * Stock les données d'une table de la façon suivante :
 * Un Data rows et un Data columns qui contiennent les infos propres à une ligne ou une colonne
 * rowCount et columnCount : respectivement le nombre de lignes et de colonnes
 * i,j des DataTexte représentant le contenu d'une cellule
 * @author François Billioud
 */
public class DataTable extends DataObject {

    public static final String ROWS = "rows";
    public static final String COLUMNS = "columns";
    public static final String ROW_COUNT = "rowCount";
    public static final String COLUMN_COUNT = "columnCount";

    public DataTable() {}

    public Data getDataLine(boolean direction, int index) {
        return getData(direction==ROW ? ROWS : COLUMNS).getData(index+"");
    }
    public DataTexte getDataCell(int i, int j) {
        Data donnees = getData(i+","+j);
        if(donnees instanceof DataTexte) {return (DataTexte)donnees;}
        else {
            DataTexte dataTexte = new DataTexte("");
            dataTexte.putAll(donnees);
            return dataTexte;
        }
    }
    public int getRowCount() {return Integer.parseInt(getElement(ROW_COUNT));}
    public int getColumnCount() {return Integer.parseInt(getElement(COLUMN_COUNT));}

    public DataTexte[] getRowCellContent(int i) {
        int m = getColumnCount();
        DataTexte[] T = new DataTexte[m];
        for(int j=0;j<m;j++) {
            T[j] = getDataCell(i, j);
        }
        return T;
    }
    
}
