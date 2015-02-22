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

package bomehc.sauvegarde;

import bomehc.json.Json;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author François Billioud
 */
public class DataObject implements Data {
    public final DataMap<String> elements = new DataMap<>();
    public final DataMap<Data> data = new DataMap<>();
    
    private static final long serialVersionUID = 1L;
    
    @Override
    public String removeElementByValue(String value) {
        return elements.removeValue(value);
    }
    @Override
    public String removeData(Data value) {
        return data.removeValue(value);
    }
    @Override
    public String removeElementByKey(String key) {
        return elements.removeKey(key);
    }
    @Override
    public Data removeDataByKey(String key) {
        return data.removeKey(key);
    }
    
    @Override
    public String putElement(String key, String value) {
        return elements.put(key, value);
    }
    @Override
    public Data putData(String key, Data value) {
        return data.put(key, value);
    }
    @Override
    public void putAll(Data data) {
        if(data==null) {return;}
        for(Map.Entry<String,Data> e : data.getDataEntries()) {
            putData(e.getKey(), e.getValue());
        }
        for(Map.Entry<String, String> e : data.getElementEntries()) {
            putElement(e.getKey(), e.getValue());
        }
    }

    @Override
    public boolean containsElementKey(String key) {
        return elements.containsKey(key);
    }
    @Override
    public boolean containsElementValue(String value) {
        return elements.containsValue(value);
    }
    @Override
    public boolean containsDataKey(String key) {
        return data.containsKey(key);
    }
    @Override
    public boolean containsDataValue(Data value) {
        return data.containsValue(value);
    }
    /**
     * Cherche une valeur dans les paramètres optionnels de l'objet.
     * @param key la clé à chercher
     * @return la valeur correspondant à la clé ou null si la valeur n'existe pas
     */
    @Override
    public String getElement(String key) {
        return elements.get(key);
    }
    @Override
    public Data getData(String key) {
        return data.get(key);
    }
    
    @Override
    public Data clone() {
        try {
            return (Data) Json.jsonCloning(this);
        } catch (IOException ex) {
            Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
            try {
                return (Data) super.clone();
            } catch (CloneNotSupportedException e) {
                Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return this;//XXX a discuter
    }
    
    @Override
    public void clear() {
        elements.clear();
        data.clear();
    }

    @Override
    public Set<Map.Entry<String, Data>> getDataEntries() {
        return data.entrySet();
    }

    @Override
    public Set<Map.Entry<String, String>> getElementEntries() {
        return elements.entrySet();
    }

    @Override
    public Set<String> getDataKeys() {
        return data.keySet();
    }

    @Override
    public Set<String> getElementKeys() {
        return elements.keySet();
    }

}
