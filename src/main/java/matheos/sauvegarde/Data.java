/** «Copyright 2012 François Billioud»
 *
 * This file is part of MathEOS.
 *
 * MathEOS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MathEOS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MathEOS. If not, see <http://www.gnu.org/licenses/>.
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
 */

package matheos.sauvegarde;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author François Billioud
 */
//public class Data extends HashMap<String, Serializable> implements Serializable {
public interface Data extends Serializable, Cloneable {

    public String removeElementByValue(String value);
    public String removeData(Data value);
    public String removeElementByKey(String key);
    public Data removeDataByKey(String key);
    
    public String putElement(String key, String value);
    public Data putData(String key, Data value);
    public void putAll(Data data);
    public boolean containsElementKey(String key);
    public boolean containsElementValue(String value);
    public boolean containsDataKey(String key);
    public boolean containsDataValue(Data value);
    /**
     * Cherche une valeur dans les paramètres optionnels de l'objet.
     * @param key la clé à chercher
     * @return la valeur correspondant à la clé ou null si la valeur n'existe pas
     */
    public String getElement(String key);
    public Data getData(String key);
    public Set<Map.Entry<String, Data>> getDataEntries();
    public Set<Map.Entry<String, String>> getElementEntries();
    public Set<String> getDataKeys();
    public Set<String> getElementKeys();
    
    public void clear();
    public Data clone();
    
    public static interface Enregistrable {
        /**
         * Charge les données passées en paramètre.
         * @param data données au format Data, à charger
         * @see getDonnees()
         */
        public void charger(Data data);
        /**
         * Récupère les données au format Data. Ces données peuvent être
         * sérialisées au format Json, ou autre, puis rechargées.
         * @return un Data
         * @see charger(Data data)
         */
        public Data getDonnees();
    }
}
