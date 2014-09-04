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
package matheos.tableau;

import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataObject;
import matheos.sauvegarde.DataTexte;
import matheos.tableau.TableConstants.ORIENTATIONS;

import java.io.Serializable;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class DataFleche extends DataObject implements Serializable {

    //nom des données
    private static final String ORIENTATION = "orientation";
    private static final String INDEX_DEPART = "depart";
    private static final String INDEX_ARRIVEE = "arrivee";
    private static final String DONNEES_TEXTE = "donneesTexte";//contenu du texte de la fleche

    public DataFleche(ORIENTATIONS orientation, int indexDepart, int indexArrivee) {
        putElement(ORIENTATION, orientation+"");
        putElement(INDEX_DEPART, indexDepart+"");
        putElement(INDEX_ARRIVEE, indexArrivee+"");
        putData(DONNEES_TEXTE, new DataTexte(""));
    }

    public void setParameters(DataFleche cle) {
        setOrientation(cle.getOrientation());
        setIndexDepart(cle.getIndexDepart());
        setIndexArrivee(cle.getIndexArrivee());
    }

    public void setOrientation(ORIENTATIONS orientation) {
        putElement(ORIENTATION, orientation+"");
    }
    
    public TableConstants.ORIENTATIONS getOrientation() {
        return ORIENTATIONS.valueOf(getElement(ORIENTATION));
    }

    public int getIndexArrivee() {
        return Integer.parseInt(getElement(INDEX_ARRIVEE));
    }

    public void setIndexArrivee(int indexArrivee) {
        putElement(INDEX_ARRIVEE, indexArrivee+"");
    }

    public int getIndexDepart() {
        return Integer.parseInt(getElement(INDEX_ARRIVEE));
    }

    public void setIndexDepart(int indexDepart) {
        putElement(INDEX_DEPART, indexDepart+"");
    }

    public DataTexte getData() {
        Data data = getData(DONNEES_TEXTE);
        if(data instanceof DataTexte) {return (DataTexte)data;}
        else {
            DataTexte text = new DataTexte("");
            text.putAll(data);
            return text;
        }
    }

    public void setData(Data data) {
        putData(DONNEES_TEXTE, data);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (getOrientation() != null ? getOrientation().hashCode() : 0);
        hash = 53 * hash + getIndexDepart();
        hash = 53 * hash + getIndexArrivee();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataFleche other = (DataFleche) obj;
        return this.getOrientation()==other.getOrientation() && this.getIndexDepart()==other.getIndexDepart() && this.getIndexArrivee()==other.getIndexArrivee();
    }

}
