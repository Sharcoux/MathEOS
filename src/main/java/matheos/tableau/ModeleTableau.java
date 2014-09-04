/*
 * «Copyright 2011 Tristan Coulange»
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
 */
package matheos.tableau;

import static matheos.tableau.TableConstants.DEFAULT_COULEUR_CASE;
import matheos.tableau.TableConstants.FIRST_CASE;
import matheos.tableau.TableConstants.TYPE_CASES;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Classe qui représente le modèle du tableau. Elle contient par définition deux
 * attributs, la matrice données qui contient les éléments de chaque cellule du
 * tableau, et l'entête qui contient le titre de chaque colonne.
 *
 * @author Tristan
 */
public class ModeleTableau extends AbstractTableModel implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    private List<LigneModeleTableau> donnees;
    private List<Color> couleurLignes;
    private List<Color> couleurColonnes;
    private FIRST_CASE firstCase;

    private ModeleTableau() {
        firstCase = FIRST_CASE.NORMAL;
    }

    /**
     * Crée le modèle du tableau avec le nombre de lignes et de colonnes définis
     * dans les paramètres.
     *
     * @param nbreLignes nombre de lignes du tableau
     * @param nbreColonnes nombre de colonnes du tableau
     */
    public ModeleTableau(int nbreLignes, int nbreColonnes) {
        this();
        donnees = new ArrayList<>();
        couleurLignes = new ArrayList<>();
        couleurColonnes = new ArrayList<>();

        for (int i = 0; i < nbreLignes; i++) {
            donnees.add(new LigneModeleTableau(nbreColonnes));
            couleurLignes.add(DEFAULT_COULEUR_CASE);
        }
        for (int i = 0; i < nbreColonnes; i++) {
            couleurColonnes.add(DEFAULT_COULEUR_CASE);
        }
    }

    private List<Color> getCouleurLignes() {
        return couleurLignes;
    }

    private void setCouleurLignes(List<Color> couleurLignes) {
        this.couleurLignes = couleurLignes;
    }

    private List<Color> getCouleurColonnes() {
        return couleurColonnes;
    }

    private void setCouleurColonnes(List<Color> couleurColonnes) {
        this.couleurColonnes = couleurColonnes;
    }

    private void setDonnees(List<LigneModeleTableau> donnees) {
        this.donnees = donnees;
    }

    private List<LigneModeleTableau> getDonnees() {
        return donnees;
    }

    public FIRST_CASE getFirstCase() {
        return firstCase;
    }

    /**
     * Permet de changer le type de la première case du tableau, et d'effectuer
     * les modifications sur cette dernière en conséquent.
     *
     * @param firstCase le type FIRST_CASE que l'on souhaite donner à la
     * première case du tableau
     */
    public void setFirstCase(FIRST_CASE firstCase) {
        if (this.firstCase.equals(firstCase)) {
            return;
        }
        this.firstCase = firstCase;

        if (firstCase.equals(FIRST_CASE.NORMAL)) {
            Object o = getValueAt(0, 0);
            if (o instanceof ModeleCellule) {
                ModeleCellule cellule = (ModeleCellule) o;
                cellule.setRenderer(null);
                cellule.setContent(null);
                cellule.setContent2(null);
            }
        }
        if(firstCase.equals(FIRST_CASE.NOT_VISIBLE)){
            Object o = getValueAt(0, 0);
            if (o instanceof ModeleCellule) {
                ModeleCellule cellule = (ModeleCellule) o;
                cellule.setRenderer(null);
                cellule.setContent(null);
                cellule.setContent2(null);
            }
            colorerCase(0, 0, DEFAULT_COULEUR_CASE);
        }
//        if (firstCase.equals(FIRST_CASE.SEPARATE)) {
//            Object o = getValueAt(0, 0);
//            if (o instanceof ModeleCellule) {
//                if (((ModeleCellule) o).getRenderer() == null) {
//                    ((ModeleCellule) o).setRenderer(CaseSeparee.createDefaultRenderer());
//                }
//            }
//        }
        fireTableDataChanged();
    }

    // NOT USED
//    private void setRowDonnees(List<ModeleCellule> listeCellules, int index) {
//        for (int i = 0; i < listeCellules.size(); i++) {
//            setValueAt(listeCellules.get(i), index, i);
//        }
//    }
//
//    private void setColumnDonnees(List<ModeleCellule> listeCellules, int index) {
//        for (int i = 0; i < donnees.size(); i++) {
//            setValueAt(listeCellules.get(i), i, index);
//        }
//    }

    private List<ModeleCellule> getRowDonnees(int row) {
        List<ModeleCellule> liste = new ArrayList<>();
        for (ModeleCellule cellule : donnees.get(row)) {
            liste.add(cellule);
        }
        return liste;
    }

    private List<ModeleCellule> getColumnDonnees(int column) {
        List<ModeleCellule> liste = new ArrayList<>();
        for (LigneModeleTableau ligne : donnees) {
            liste.add(ligne.get(column));
        }
        return liste;
    }

    /**
     * Ajoute une ligne dans le modèle du tableau à la position donnée.
     *
     * @param position position de la ligne à ajouter
     */
    public void ajouterLigne(int position) {
        donnees.add(position, new LigneModeleTableau(this.getColumnCount()));
        for (int i = 0; i < donnees.get(position).size(); i++) {
            donnees.get(position).get(i).setBackgroundColor(couleurColonnes.get(i));
        }
        couleurLignes.add(position, DEFAULT_COULEUR_CASE);
        verifierColorationLigne(position, getCouleurCase(position, 0));
        this.fireTableDataChanged();
    }

    /**
     * Ajoute une ligne dans le modèle du tableau à la position donnée, et la
     * remplie avec les {@link ModeleCellule} passées en paramètre. Si la taille
     * de la liste des
     * <code>ModeleCellule</code> est différente du nombre de colonne du
     * <code>ModeleTableau</code>, une ligne vide est ajoutée.
     *
     * @param position position de la ligne à ajouter
     * @param cellules la liste contenant les <code>ModeleCellule</code> à
     * ajouter à la nouvelle ligne créée
     */
    public void ajouterLigne(int position, List<ModeleCellule> cellules) {
        if (cellules == null || cellules.size() != this.getColumnCount()) {
            ajouterLigne(position);
            return;
        }
        donnees.add(position, new LigneModeleTableau(this.getColumnCount()));
        boolean isColorated = true;
        Color couleur = cellules.get(0).getBackgroundColor();
        if (couleur.equals(DEFAULT_COULEUR_CASE)) {
            isColorated = false;
        }
        for (int i = 0; i < cellules.size(); i++) {
            setValueAt(cellules.get(i), position, i);
            if (cellules.get(i).getBackgroundColor() != couleur) {
                isColorated = false;
            }
        }
        couleurLignes.add(position, isColorated ? couleur : DEFAULT_COULEUR_CASE);
        this.fireTableDataChanged();
    }

    /**
     * Supprime la ligne située à la position donnée dans le modèle du tableau.
     *
     * @param position position de la ligne à supprimer
     */
    public List<ModeleCellule> supprimerLigne(int position) {
        List<ModeleCellule> liste = getRowDonnees(position);
        donnees.remove(position);
        couleurLignes.remove(position);
        this.fireTableDataChanged();
        return liste;
    }

    /**
     * Ajoute une colonne dans le modèle du tableau à la position donnée.
     *
     * @param position position de la colonne à ajouter
     */
    public void ajouterColonne(int position) {
        for (int i = 0; i < this.getRowCount(); i++) {
            ModeleCellule cellule = new ModeleCellule();
            donnees.get(i).add(position, cellule);
            cellule.setBackgroundColor(couleurLignes.get(i));
        }
        couleurColonnes.add(position, DEFAULT_COULEUR_CASE);
        verifierColorationColonne(position, getCouleurCase(0, position));
        this.fireTableStructureChanged();
    }

    /**
     * Ajoute une colonne dans le modèle du tableau à la position donnée, et la
     * remplie avec les {@link ModeleCellule} passées en paramètre. Si la taille
     * de la liste des
     * <code>ModeleCellule</code> est différente du nombre de ligne du
     * <code>ModeleTableau</code>, une colonne vide est ajoutée.
     *
     * @param position position de la colonne à ajouter
     * @param cellules la liste contenant les <code>ModeleCellule</code> à
     * ajouter à la nouvelle colonne créée
     */
    public void ajouterColonne(int position, List<ModeleCellule> cellules) {
        if (cellules == null || cellules.size() != this.getRowCount()) {
            ajouterColonne(position);
            return;
        }
        boolean isColorated = true;
        Color couleur = cellules.get(0).getBackgroundColor();
        if (couleur.equals(DEFAULT_COULEUR_CASE)) {
            isColorated = false;
        }
        for (int i = 0; i < this.getRowCount(); i++) {
            donnees.get(i).add(position, cellules.get(i));
            if (cellules.get(i).getBackgroundColor() != couleur) {
                isColorated = false;
            }
        }
        couleurColonnes.add(position, isColorated ? couleur : DEFAULT_COULEUR_CASE);
        this.fireTableStructureChanged();
    }

    /**
     * Supprime une colonne dans le modèle du tableau à la position donnée.
     *
     * @param position position de la colonne à supprimer
     */
    public List<ModeleCellule> supprimerColonne(int position) {
        List<ModeleCellule> liste = getColumnDonnees(position);
        for (int i = 0; i < this.getRowCount(); i++) {
            donnees.get(i).remove(position);
        }
        couleurColonnes.remove(position);
        this.fireTableStructureChanged();
        return liste;
    }

    /**
     * Méthode permettant de colorer l'intégralité d'une ligne.
     *
     * @param position la position de la ligne à colorer
     * @param couleur la couleur de coloration de la ligne
     */
    public void colorerLigne(int position, Color couleur) {
        couleurLignes.set(position, couleur);
        for (int j = 0; j < donnees.get(position).size(); j++) {
            if (j == 0 && position == 0 && firstCase.equals(FIRST_CASE.NOT_VISIBLE)) {
                continue;
            }
            donnees.get(position).get(j).setBackgroundColor(couleur);
            verifierColorationColonne(j, couleur);
        }
        this.fireTableDataChanged();
    }

    /**
     * Méthode permettant de décolorer l'intégralité d'une ligne.
     *
     * @param position la position de la ligne à décolorer
     */
    public void decolorerLigne(int position) {
        couleurLignes.set(position, DEFAULT_COULEUR_CASE);
        for (int j = 0; j < donnees.get(position).size(); j++) {
            if (j == 0 && position == 0 && firstCase.equals(FIRST_CASE.NOT_VISIBLE)) {
                donnees.get(j).get(position).setBackgroundColor(DEFAULT_COULEUR_CASE);
            } else {
                donnees.get(position).get(j).setBackgroundColor(couleurColonnes.get(j));
            }
        }
        this.fireTableDataChanged();
    }

    /**
     * Méthode permettant de colorer l'intégralité d'une colonne.
     *
     * @param position la position de la colonne à colorer
     * @param couleur la couleur de coloration de la colonne
     */
    public void colorerColonne(int position, Color couleur) {
        couleurColonnes.set(position, couleur);
        for (int i = 0; i < donnees.size(); i++) {
            if (i == 0 && position == 0 && firstCase.equals(FIRST_CASE.NOT_VISIBLE)) {
                continue;
            }
            donnees.get(i).get(position).setBackgroundColor(couleur);
            verifierColorationLigne(i, couleur);
        }
        this.fireTableDataChanged();
    }

    /**
     * Méthode permettant de décolorer l'intégralité d'une colonne.
     *
     * @param position la position de la colonne à décolorer
     */
    public void decolorerColonne(int position) {
        couleurColonnes.set(position, DEFAULT_COULEUR_CASE);
        for (int i = 0; i < donnees.size(); i++) {
            if (i == 0 && position == 0 && firstCase.equals(FIRST_CASE.NOT_VISIBLE)) {
                donnees.get(i).get(position).setBackgroundColor(DEFAULT_COULEUR_CASE);
            } else {
                donnees.get(i).get(position).setBackgroundColor(couleurLignes.get(i));
            }
        }
        this.fireTableDataChanged();
    }

    public boolean isColoratedLigne(int position) {
        return !couleurLignes.get(position).equals(DEFAULT_COULEUR_CASE);
    }

    public boolean isColoratedColonne(int position) {
        return !couleurColonnes.get(position).equals(DEFAULT_COULEUR_CASE);
    }

    /**
     * Renvoie la couleur de la case dont la position est donnée par la ligne et
     * la colonne passées en paramètre.
     *
     * @param row l'index de la ligne où se situe la case
     * @param column l'index de la colonne où se situe la case
     * @return la couleur de la case
     */
    public Color getCouleurCase(int row, int column) {
        return donnees.get(row).get(column).getBackgroundColor();
    }

    /**
     * Renvoie la liste des couleurs des cases pour une ligne ou une colonne
     * donnée.
     *
     * @param typeCase le {@link TYPE_CASES} dont on veut connaître la couleur
     * (ligne ou colonne)
     * @param position la position de la ligne ou de la colonne
     * @return une liste des couleurs de chaque case contenant la ligne ou la
     * colonne passée en paramètre
     */
    public List<Color> getCouleursCases(TYPE_CASES typeCase, int position) {
        List<Color> couleurs = new ArrayList<>();
        switch (typeCase) {
            case COLUMN:
                for (LigneModeleTableau donnee : donnees) {
                    couleurs.add(donnee.get(position).getBackgroundColor());
                }
                break;
            case ROW:
                for (int i = 0; i < donnees.get(position).size(); i++) {
                    couleurs.add(donnees.get(position).get(i).getBackgroundColor());
                }
                break;
        }
        return couleurs;
    }

    /**
     * Permet de colorer une case d'une certaine couleur. Cela met également à
     * jour les colorations des lignes et des colonnes si la coloration de cette
     * case devient identique à celle des cellules voisines.
     *
     * @param row l'index de la ligne où se situe la case
     * @param column l'index de la colonne où se situe la case
     * @param couleur la couleur avec laquelle on souhaite colorer la case
     */
    public void colorerCase(int row, int column, Color couleur) {
        donnees.get(row).get(column).setBackgroundColor(couleur);
        if (row != 0 || column != 0 || !firstCase.equals(FIRST_CASE.NOT_VISIBLE)) {
            verifierColorationLigne(row, couleur);
            verifierColorationColonne(column, couleur);
        }
        this.fireTableDataChanged();
    }

    /**
     * Permet de colorer l'intégralité d'une ligne ou d'une colonne avec les
     * couleurs passées en paramètre. Si la taille de la liste ne correspond par
     * à la taille de la ligne ou de la colonne à colorer, une exception sera
     * levée.
     *
     * @param typeCase le {@link TYPE_CASES} à colorer (ligne ou colonne)
     * @param position la position de la ligne ou de la colonne
     * @param couleurs la liste des couleurs avec laquelle colorer
     * successivement chacune des cases de la ligne ou de la colonne
     */
    public void colorerCases(TYPE_CASES typeCase, int position, List<Color> couleurs) {
        switch (typeCase) {
            case COLUMN:
                for (int i = 0; i < donnees.size(); i++) {
                    donnees.get(i).get(position).setBackgroundColor(couleurs.get(i));
                    verifierColorationLigne(i, couleurs.get(i));
                }
                verifierColorationColonne(position, couleurs.get(0));
                break;
            case ROW:
                for (int i = 0; i < donnees.get(position).size(); i++) {
                    donnees.get(position).get(i).setBackgroundColor(couleurs.get(i));
                    verifierColorationColonne(i, couleurs.get(i));
                }
                verifierColorationLigne(position, couleurs.get(0));
                break;
        }
        this.fireTableDataChanged();
    }

    /**
     * Vérifie que toutes les cases d'une ligne ont la couleur passée en
     * paramètre. Si c'est le cas, la méthode change le modèle associé (true si
     * toutes les cases ont la couleur passée en paramètre, false sinon).
     *
     * @param row la ligne que l'on souhaite vérifier
     * @param couleur la couleur de référence avec laquelle comparer celle des
     * autre cases de la ligne
     */
    private void verifierColorationLigne(int row, Color couleur) {
        if (row != 0 && !firstCase.equals(FIRST_CASE.NOT_VISIBLE) && couleur.equals(DEFAULT_COULEUR_CASE)) {
            couleurLignes.set(row, DEFAULT_COULEUR_CASE);
            return;
        }
        boolean isColoratedLigne = true;
        for (int j = 0; j < donnees.get(row).size(); j++) {
            if (row == 0 && j == 0 && firstCase.equals(FIRST_CASE.NOT_VISIBLE)) {
                continue;
            }
            if (!donnees.get(row).get(j).getBackgroundColor().equals(couleur)) {
                isColoratedLigne = false;
            }
        }
        couleurLignes.set(row, isColoratedLigne ? couleur : DEFAULT_COULEUR_CASE);
    }

    /**
     * Vérifie que toutes les cases d'une colonne ont la couleur passée en
     * paramètre. Si c'est le cas, la méthode change le modèle associé (true si
     * toutes les cases ont la couleur passée en paramètre, false sinon).
     *
     * @param column la colonne que l'on souhaite vérifier
     * @param couleur la couleur de référence avec laquelle comparer celle des
     * autre cases de la colonne
     */
    private void verifierColorationColonne(int column, Color couleur) {
        if (couleur.equals(DEFAULT_COULEUR_CASE)) {
            couleurColonnes.set(column, DEFAULT_COULEUR_CASE);
            return;
        }
        boolean isColoratedColonne = true;
        for (int i = 0; i < donnees.size(); i++) {
            if (i == 0 && column == 0 && firstCase.equals(FIRST_CASE.NOT_VISIBLE)) {
                continue;
            }
            if (!donnees.get(i).get(column).getBackgroundColor().equals(couleur)) {
                isColoratedColonne = false;
            }
        }
        couleurColonnes.set(column, isColoratedColonne ? couleur : DEFAULT_COULEUR_CASE);
    }

    /**
     * Méthode permettant d'extraire de ce ModeleTableau un ModeleTableau
     * comportant les cases successives passées en paramètre. Si le nombre de
     * lignes ou le nombre de colonnes implique des cases qui n'existent pas
     * dans le tableau d'origine, le ModeleTableau retourné sera coupé au nombre
     * maximum de cases possible.
     *
     * @param firstRow l'index de la ligne de la première case à copier
     * @param firstColumn l'index de la colonne de la première case à copier
     * @param nbRow le nombre de lignes maximum à copier
     * @param nbColumn le nombre de colonne maximum à copier
     * @return un ModeleTableau comportant les cases demandées en paramètres ou
     * null si l'index de la première case n'existe pas
     */
    public ModeleTableau extractModeleTableau(int firstRow, int firstColumn, int rowCount, int colCount) {
        int nbRow = rowCount, nbColumn = colCount;
        if (firstRow >= getRowCount() || firstColumn >= getColumnCount()) {
            return null;
        }
        nbRow = Math.min(nbRow, getRowCount() - firstRow);
        nbColumn = Math.min(nbColumn, getColumnCount() - firstColumn);
        ModeleTableau modeleTableauExtracted = new ModeleTableau(nbRow, nbColumn);
        if (firstRow == 0 && firstColumn == 0) {
            modeleTableauExtracted.setFirstCase(firstCase);
        }
        for (int i = 0; i < nbRow; i++) {
            for (int j = 0; j < nbColumn; j++) {
                modeleTableauExtracted.setValueAt(getValueAt(i + firstRow, j + firstColumn), i, j);
            }
        }
        return modeleTableauExtracted;
    }

    /**
     * Permet de coller un ModeleTableau passé en paramètre dans le
     * ModeleTableau d'origine, c'est à dire que les cases du ModeleTableau en
     * paramètre vont remplacer les cases du ModeleTableau d'origine à partir de
     * la case désigné par les paramètres firstRow et firstColumn. Si le
     * ModeleTableau en paramètre implique un coller qui sorte des marges du
     * ModeleTableau d'origine, les cases en trop seront ignorées.
     *
     * @param firstRow l'index de la ligne de la première case à remplacer
     * @param firstColumn l'index de la colonne de la première case à remplacer
     * @param modeleTableauToPaste le ModeleTableau contenant les cases que l'on
     * souhaite substituer aux cases actuelles
     */
    public void pasteModeleTableau(int firstRow, int firstColumn, ModeleTableau modeleTableauToPaste) {
        int nbRow = Math.min(modeleTableauToPaste.getRowCount(), getRowCount() - firstRow);
        int nbColumn = Math.min(modeleTableauToPaste.getColumnCount(), getColumnCount() - firstColumn);
        for (int i = 0; i < nbRow; i++) {
            for (int j = 0; j < nbColumn; j++) {
                if (i == 0 && j == 0) {
                    switch (modeleTableauToPaste.getFirstCase()) {
                        case SEPARATE:
                            if (firstRow == 0 && firstColumn == 0) {
                                setFirstCase(modeleTableauToPaste.getFirstCase());
                            } else {
                                Object o = modeleTableauToPaste.getValueAt(i, j);
                                if (o instanceof ModeleCellule) {
                                    ((ModeleCellule) o).setRenderer(null);
                                    ((ModeleCellule) o).setContent2(null);
                                }
                            }
                            break;
                        default:
                            if (firstRow == 0 && firstColumn == 0){
                                setFirstCase(modeleTableauToPaste.getFirstCase());
                            }
                            break;
                    }
                }
                setValueAt(modeleTableauToPaste.getValueAt(i, j), i + firstRow, j + firstColumn);
            }
        }
        fireTableDataChanged();
    }

    /**
     * Renvoie le nombre de lignes du modèle.
     *
     * @return
     */
    @Override
    public int getRowCount() {
        return donnees.size();
    }

    /**
     * Renvoie le nombre de colonnes du modèle.
     *
     * @return
     */
    @Override
    public int getColumnCount() {
        return donnees.get(0).size();
    }

    /**
     * Méthode qui est censée renvoyer l'entête d'une colonne, comme l'entête
     * n'est pas utilisé la méthode renvoie "".
     *
     * @param columnIndex position de la colonne dont on veut l'entête
     * @return
     */
    @Override
    public String getColumnName(int columnIndex) {
        return "";
    }

    /**
     * Renvoie le String contenu dans la cellule (rowIndex,columnIndex).
     *
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return donnees.get(rowIndex).get(columnIndex);
    }

    /**
     * Renvoie true si la cellule est éditable, ici elle renvoit toujours true.
     *
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true; //Toutes les cellules éditables
    }

    /**
     * Remplit une cellule du modèle du tableau, ET ajoute l'action "ecrire" à
     * la listeAction qui contient toutes les actions effectuées. Cette méthode
     * est utilisée automatiquement lorsque l'on écrit dans le tableau.
     *
     * @param object texte écrit dans la cellule
     * @param rowIndex
     * @param columnIndex
     */
    @Override
    public void setValueAt(Object object, int rowIndex, int columnIndex) {
        donnees.get(rowIndex).remove(columnIndex);
        if (object instanceof ModeleCellule) {
            ModeleCellule modeleCellule = (ModeleCellule) object;
            donnees.get(rowIndex).add(columnIndex, modeleCellule);
            verifierColorationLigne(rowIndex, modeleCellule.getBackgroundColor());
            verifierColorationColonne(columnIndex, modeleCellule.getBackgroundColor());
        }
    }

    @Override
    public ModeleTableau clone() {
        ModeleTableau clone = new ModeleTableau();
        clone.setDonnees(new ArrayList<LigneModeleTableau>());
        for (LigneModeleTableau ligne : donnees) {
            clone.getDonnees().add(ligne.clone());
        }
        clone.setCouleurLignes(new ArrayList<Color>());
        for (Color couleurLigne : couleurLignes) {
            clone.getCouleurLignes().add(couleurLigne);
        }
        clone.setCouleurColonnes(new ArrayList<Color>());
        for (Color couleurColonne : couleurColonnes) {
            clone.getCouleurColonnes().add(couleurColonne);
        }
        clone.setFirstCase(firstCase);
        return clone;
    }

    public static class LigneModeleTableau extends ArrayList<ModeleCellule> implements Cloneable, Serializable {

        private static final long serialVersionUID = 1L;

        public LigneModeleTableau() {
        }

        public LigneModeleTableau(int nbreColonnes) {
            for (int i = 0; i < nbreColonnes; i++) {
                this.add(new ModeleCellule());
            }
        }

        @Override
        public LigneModeleTableau clone() {
            LigneModeleTableau clone = new LigneModeleTableau();
            for (ModeleCellule cellule : this) {
                clone.add((ModeleCellule)cellule.clone());
            }
            return clone;
        }
    }
}