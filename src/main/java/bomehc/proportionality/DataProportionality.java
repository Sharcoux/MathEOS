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

package bomehc.proportionality;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import bomehc.json.Json;
import bomehc.sauvegarde.Data;
import bomehc.table.DataTable;
import static bomehc.table.Table.BAS;
import static bomehc.table.Table.DROITE;
import static bomehc.table.Table.GAUCHE;
import static bomehc.table.Table.HAUT;

/**
 *
 * @author François Billioud
 */
public class DataProportionality extends DataTable {
    public static final String TOP_ARROW = "topArrow";
    public static final String BOTTOM_ARROW = "bottomArrow";
    public static final String RIGHT_ARROW = "rightArrow";
    public static final String LEFT_ARROW = "leftArrow";

    public Set<Fleche> getListeFleches(int orientation) {
        Set<Fleche> fleches = null;
        switch(orientation) {
            case HAUT : fleches = readFleches(TOP_ARROW, Fleche.ORIENTATION.HAUT); break;
            case BAS : fleches = readFleches(BOTTOM_ARROW, Fleche.ORIENTATION.BAS); break;
            case DROITE : fleches = readFleches(RIGHT_ARROW, Fleche.ORIENTATION.DROITE); break;
            case GAUCHE : fleches = readFleches(LEFT_ARROW, Fleche.ORIENTATION.GAUCHE); break;
        }
        if(fleches==null) {return new HashSet<>();}
        return fleches;
    }

    public Set<Fleche> readFleches(String key, Fleche.ORIENTATION orientation) {
        Set<Fleche> set = new HashSet<>();
        if(getElement(key)!=null) {
            try {
                Set<Data> donnees = (Set) Json.toJava(getElement(key),HashSet.class);
                for(Data donnee : donnees) {
                    set.add(new Fleche(orientation, donnee, null));
                }
            } catch (IOException ex) {
                Logger.getLogger(DataProportionality.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return set;
    }
    public String writeFleches(Set<Fleche> set, String orientationKey) {
        Set<Data> donnees = new HashSet<>();
        for(Fleche f : set) { donnees.add(f.getDonnees()); }
        try {
            String s = Json.toJson(donnees);
            putElement(orientationKey, s);
        } catch (IOException ex) {
            Logger.getLogger(DataProportionality.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
