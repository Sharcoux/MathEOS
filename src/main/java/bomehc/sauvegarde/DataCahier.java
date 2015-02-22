/** «Copyright 2012 François Billioud»
 *
 * This file is part of Bomehc.
 *
 * Bomehc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bomehc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Bomehc. If not, see <http://www.gnu.org/licenses/>.
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
 */

package bomehc.sauvegarde;

import java.util.Arrays;

/**
 *
 * @author François Billioud
 */
public class DataCahier extends DataObject {

    //nom des données
    private static final String INDEX_COURANT = "indexCourant";
    private static final String SIZE = "size";
    
    public DataCahier() {
        putElement(INDEX_COURANT, -1+"");
        putElement(SIZE, 0+"");
    }
    
    /**
     * Permet de récupérer un élément du cahier au format DataTexte
     * @param i l'index de l'élément à récupérer
     * @return le contenu au format DataTexte
     */
    public DataTexte getContenuChapitre(int i) {
        return getDataChapitre(i).getContenu();
    }
    
    /**
     * Permet de récupérer un élément du cahier au format DataTexte
     * @param i l'index de l'élément à récupérer
     * @return le contenu au format DataTexte
     */
    public DataTexte getContenuCourant() {
        return getDataChapitre(getIndexCourant()).getContenu();
    }
    
    public String getTitre(int i) {return getDataChapitre(i).getTitre();}
    
    public String getTitreCourant() {return getDataChapitre(getIndexCourant()).getTitre();}
    
    public DataChapitre getDataChapitre(int i) {
        Data contenu = getData(i+"");
        if(contenu instanceof DataChapitre) {return (DataChapitre)contenu;}
        else {DataChapitre dataChapitre = new DataChapitre("",null);dataChapitre.putAll(contenu);return dataChapitre;}
    }
    
    /**
     * Renvoie un tablau contenant les titres des éléments du cahier
     * @return Le tableau des titres, ou un tableau vide si aucun élément
     */
    public String[] getTitres() {
        int n = nbChapitres();
        String[] T = new String[n];
        for(int i = 0; i<n; i++) {
            T[i] = getDataChapitre(i).getTitre();
        }
        return T;
    }

    /** Modifie le titre d'un élément **/
    public void setTitre(int index, String titre) throws IndexOutOfBoundsException {
        if(index<0 || index>=nbChapitres()) {throw new IndexOutOfBoundsException("index ! "+index+" , size : "+nbChapitres());}
        getDataChapitre(index).setTitre(titre);
    }
    
    public int nbChapitres() {return Integer.parseInt(getElement(SIZE));}
    
    /**
     * Modifie le contenu de l'élément courant
     * @param donnees les données à enregistrer dans l'emplacement courant
     */
    public void setContenu(DataTexte donnees) {
        if(getIndexCourant()<0) {return;}//cahier non initialisé
        getDataChapitre(getIndexCourant()).setContenu(donnees);
    }
    
    /**
     * Ajoute un nouvel élément dans ce cahier, comme un nouveau chapitre par exemple.
     * le chapitre est ajouté automatiquement à la suite des autres quelque soit le chapitre actuellement ouvert.
     * @param titre le titre permettant à l'utilisateur d'identifier l'élément
     * @param donnees le contenu de l'élément
     */
    public void addChapitre(String titre, DataTexte donnees) {
        int n = nbChapitres();
        putData(n+"", new DataChapitre(titre, donnees));
        putElement(SIZE, (n+1)+"");
    }
    
    public void setIndexCourant(int index) throws IndexOutOfBoundsException {
        if(index<0 || index>=nbChapitres()) {throw new IndexOutOfBoundsException("try to access index : "+index+" of "+nbChapitres());}
        putElement(INDEX_COURANT, index+"");
    }
    
    public int getIndexCourant() {return Integer.parseInt(getElement(INDEX_COURANT));}
    
    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>titre.equals(get(i))</tt>, or -1 if there is no such index.
     *
     * @param titre element to search for
     * @return the index of the last occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     */
    public int getIndex(String titre) {
        String[] T = getTitres();
        for(int i=T.length-1; i>=0; i--) {
            if(T[i].equals(titre)) {return i;}
        }
        return -1;
    }
    
    public static class DataChapitre extends DataObject {
        private static final String TITRE = "title";
        private static final String CONTENU = "content";
        public DataChapitre(String titre, DataTexte contenu) {
            putElement(TITRE, titre);
            putData(CONTENU, contenu);
        }
        public String getTitre() {return getElement(TITRE);}
        public DataTexte getContenu() {
            Data content = getData(CONTENU);
            if(content instanceof DataTexte) {return (DataTexte) content;}
            DataTexte dataTexte = new DataTexte("");
            dataTexte.putAll(content);
            return dataTexte;
        }
        public String setTitre(String titre) {return putElement(TITRE, titre);}
        public void setContenu(DataTexte contenu) {putData(CONTENU, contenu);}
    }
    
}
