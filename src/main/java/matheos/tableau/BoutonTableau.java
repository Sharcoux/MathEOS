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

import static matheos.tableau.TableConstants.ORIENTATIONS;
import static matheos.tableau.TableConstants.TAILLE_ICONE;
import static matheos.tableau.ActionTableau.APPARENCE;
import matheos.utils.boutons.BoutonPanel;
import matheos.utils.objets.Icone;

import java.awt.Dimension;

/**
 * Classe qui représente les boutons situés autour du tableau modifier le
 * tableau.
 *
 * @author Tristan
 */
@SuppressWarnings("serial")
public class BoutonTableau extends BoutonPanel implements ResizableComponentTableau {

	private static Icone iconeFlecheHaut;
    private static Icone iconeFlecheHautRollover;
    private static Icone iconeFlecheHautSelected;
    private static Icone iconeFlecheGauche;
    private static Icone iconeFlecheGaucheRollover;
    private static Icone iconeFlecheGaucheSelected;
    private static Icone iconeFlecheDroit;
    private static Icone iconeFlecheDroitRollover;
    private static Icone iconeFlecheDroitSelected;
    private static Icone iconeFlecheBas;
    private static Icone iconeFlecheBasRollover;
    private static Icone iconeFlecheBasSelected;
    private static Icone iconeCroix;
    private static Icone iconeCroixRollover;
    private static Icone iconeCroixSelected;
    private static Icone iconePaletteAddHaut;
    private static Icone iconePaletteAddHautRollover;
    private static Icone iconePaletteAddHautSelected;
    private static Icone iconePaletteAddGauche;
    private static Icone iconePaletteAddGaucheRollover;
    private static Icone iconePaletteAddGaucheSelected;
    private static Icone iconePaletteRemove;
    private static Icone iconePaletteRemoveRollover;
    private static Icone iconePaletteRemoveSelected;

    static {
        iconeFlecheHaut = new Icone("images/fleche_tableau_haut_up.png");
        iconeFlecheHautRollover = new Icone("images/fleche_tableau_haut_down_ombre.png");
        iconeFlecheHautSelected = new Icone("images/fleche_tableau_haut_up_ombre.png");
        iconeFlecheGauche = new Icone("images/fleche_tableau_gauche_up.png");
        iconeFlecheGaucheRollover = new Icone("images/fleche_tableau_gauche_down_ombre.png");
        iconeFlecheGaucheSelected = new Icone("images/fleche_tableau_gauche_up_ombre.png");
        iconeFlecheDroit = new Icone("images/fleche_tableau_droit_up.png");
        iconeFlecheDroitRollover = new Icone("images/fleche_tableau_droit_down_ombre.png");
        iconeFlecheDroitSelected = new Icone("images/fleche_tableau_droit_up_ombre.png");
        iconeFlecheBas = new Icone("images/fleche_tableau_bas_up.png");
        iconeFlecheBasRollover = new Icone("images/fleche_tableau_bas_down_ombre.png");
        iconeFlecheBasSelected = new Icone("images/fleche_tableau_bas_up_ombre.png");
        iconeCroix = new Icone("images/croix_tableau_up.png");
        iconeCroixRollover = new Icone("images/croix_tableau_down_ombre.png");
        iconeCroixSelected = new Icone("images/croix_tableau_up_ombre.png");
        iconePaletteAddHaut = new Icone("images/paletteHaut_tableau_up.png");
        iconePaletteAddHautRollover = new Icone("images/paletteHaut_tableau_down_ombre.png");
        iconePaletteAddHautSelected = new Icone("images/paletteHaut_tableau_up_ombre.png");
        iconePaletteAddGauche = new Icone("images/paletteGauche_tableau_up.png");
        iconePaletteAddGaucheRollover = new Icone("images/paletteGauche_tableau_down_ombre.png");
        iconePaletteAddGaucheSelected = new Icone("images/paletteGauche_tableau_up_ombre.png");
        iconePaletteRemove = new Icone("images/paletteCroix_tableau_up.png");
        iconePaletteRemoveRollover = new Icone("images/paletteCroix_tableau_down_ombre.png");
        iconePaletteRemoveSelected = new Icone("images/paletteCroix_tableau_up_ombre.png");
    }
    private ORIENTATIONS orientation;
    private APPARENCE apparence;
    private Icone icone;
    private Icone iconeRollover;
    private Icone iconeSelected;

    /**
     * Crée un bouton
     */
    public BoutonTableau() {
        this.setOpaque(false);
        setActionReleased();
        setIcones(new Icone(), new Icone(), new Icone());
    }

    /**
     * Permet d'associer les différentes icones au bouton.
     *
     * @param icone
     * @param iconeRollover
     * @param iconeSelected
     */
    private void setIcones(Icone icone, Icone iconeRollover, Icone iconeSelected) {
        this.icone = icone;
        this.iconeRollover = iconeRollover;
        this.iconeSelected = iconeSelected;
        this.setIcon(icone);
        this.setSelectedIcon(iconeSelected);
        this.setRolloverIcon(iconeRollover);
    }

    public APPARENCE getApparence() {
        return apparence;
    }

    /**
     * Détermine l'apparence d'un bouton en fonction de l'orientation de son
     * panel parent, et effecte les icones correspondantes en conséquent.
     *
     * @param orientation l'orientation du panel support (HAUT, GAUHE, DROIT,
     * BAS)
     * @param apparence l'apparence de l'icone à afficher
     */
    public void setApparence(ORIENTATIONS orientation, APPARENCE apparence) {
        if (!apparence.equals(this.apparence) || !orientation.equals(this.orientation)) {
            this.apparence = apparence;
            this.orientation = orientation;
            switch (apparence) {
                case FLECHE:
                    switch (orientation) {
                        case HAUT:
                            setIcones(iconeFlecheHaut, iconeFlecheHautRollover, iconeFlecheHautSelected);
                            break;
                        case GAUCHE:
                            setIcones(iconeFlecheGauche, iconeFlecheGaucheRollover, iconeFlecheGaucheSelected);
                            break;
                        case DROIT:
                            setIcones(iconeFlecheDroit, iconeFlecheDroitRollover, iconeFlecheDroitSelected);
                            break;
                        case BAS:
                            setIcones(iconeFlecheBas, iconeFlecheBasRollover, iconeFlecheBasSelected);
                            break;
                        default :
                        	throw new IllegalArgumentException("L'orientation est incorrecte");
                    }
                    break;
                case CROIX:
                    setIcones(iconeCroix, iconeCroixRollover, iconeCroixSelected);
                    break;
                case PALETTE_ADD:
                    switch (orientation) {
                        case HAUT:
                            setIcones(iconePaletteAddHaut, iconePaletteAddHautRollover, iconePaletteAddHautSelected);
                            break;
                        case GAUCHE:
                            setIcones(iconePaletteAddGauche, iconePaletteAddGaucheRollover, iconePaletteAddGaucheSelected);
                        break;
                        default:
                            throw new IllegalArgumentException("Il n'y a pas de bouton pour cette orientation");
                    }
                    break;
                case PALETTE_REMOVE:
                    setIcones(iconePaletteRemove, iconePaletteRemoveRollover, iconePaletteRemoveSelected);
                    break;
                default:
                    throw new IllegalArgumentException("Cette apparence n'existe pas");
            }
            this.revalidate();
            this.repaint();
        }
    }

    /**
     * Adapte la taille du bouton en fonction du coefficient (toutes les
     * dimensions sont multipliées par coef)
     *
     * @param coef coefficient par lequel toutes les dimensions sont multipliées
     */
    public void adapterDimensions(double coef) {
        if (icone.getIconWidth() != (int) (TAILLE_ICONE * coef)) {
            icone.setSize((int) (TAILLE_ICONE * coef), (int) (TAILLE_ICONE * coef));
            iconeRollover.setSize((int) (TAILLE_ICONE * coef), (int) (TAILLE_ICONE * coef));
            iconeSelected.setSize((int) (TAILLE_ICONE * coef), (int) (TAILLE_ICONE * coef));
        }
        this.setPreferredSize(new Dimension((int) (TAILLE_ICONE * coef), (int) (TAILLE_ICONE * coef)));
    }

    @Override
    public int getLargeurNormale() {
        return TAILLE_ICONE;
    }

    @Override
    public int getHauteurNormale() {
        return TAILLE_ICONE;
    }
}
