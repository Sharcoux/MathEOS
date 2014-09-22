/** «Copyright 2013 François Billioud»
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

import matheos.utils.fichiers.Adresse;
import matheos.utils.managers.Traducteur;
import java.io.Serializable;

/**
 *
 * @author François Billioud
 */
public class DataProfil extends DataObject implements Serializable {
    private static final long serialVersionUID = 5L;

    //nom des données
    private static final String VERSION = "version";
    private static final String THEME = "theme";
    private static final String LANGUE = "langue";
    private static final String NOM = "nom";
    private static final String PRENOM = "prenom";
    private static final String NIVEAU = "niveau";
    private static final String CLASSE_ID = "classeID";
    private static final String ADRESSE = "adresse";
    
    public long getVersion() {return Long.parseLong(getElement(VERSION));}
    public String getTheme() {return getElement(THEME);}
    public String getLangue() {return getElement(LANGUE);}
    public String getNom() {return getElement(NOM);}
    public String getPrenom() {return getElement(PRENOM);}
    public int getNiveau() {return Integer.parseInt(getElement(NIVEAU));}
    public String getClasseID() {return getElement(CLASSE_ID);}
    
    private void setVersion(long version) {putElement(VERSION, version+"");}
    public void setTheme(String theme) {putElement(THEME, theme);}
    public void setLangue(String langue) {putElement(LANGUE, langue);}
    //paramètres

    public DataProfil(String nom, String prenom, int classe, String langue, String theme, String classeID) {
        putElement(NOM,nom);
        putElement(PRENOM,prenom);
        putElement(NIVEAU,classe+"");
        putElement(CLASSE_ID,classeID);

        putElement(LANGUE,langue);
        putElement(THEME, theme);
        
        putElement(VERSION, serialVersionUID+"");
    }

    /** Renvoie l'adresse du fichier ou null si non encore définit **/
    public String getAdresseFichier() {
        return getElement(ADRESSE);
    }

    public void setAdresseProfil(String adresseProfil) {
        putElement(ADRESSE, adresseProfil);
    }
    
    public void sauvegarder() {
        if(getAdresseFichier()!=null) { new Adresse(getAdresseFichier()).sauvegarde(this); }
    }

    public DataCahier getCahier(String nomCahier) {
        Data donnees = getData(nomCahier);
        if(donnees instanceof DataCahier) {return (DataCahier)donnees;}
        else {
            DataCahier d = new DataCahier();
            d.putAll(donnees);
            return d;
        }
    }
    
    public void setCahier(String nomCahier, DataCahier cahier) {
        putData(nomCahier, cahier);
    }
    
    public String[] getTitres(String cahier) {
        return getCahier(cahier).getTitres();
    }
    
    public void setTitreElement(String cahier, int i, String titre) {
        getCahier(cahier).setTitre(i, titre);
    }
    
    public String getClasse() {
        return Traducteur.getListeClasses()[getNiveau()];
    }
    
    public void updateVersion(int i) {
        if(getVersion()<i) {setVersion(i);}
    }
    
    public int nbChapitres(String cahier) {
        return getCahier(cahier).nbChapitres();
    }
}
