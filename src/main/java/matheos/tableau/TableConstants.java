package matheos.tableau;

import matheos.utils.managers.ColorManager;
import java.awt.Color;

public interface TableConstants {

    float[] ZOOM = {1.2f, 1.5f};
    Color DEFAULT_COULEUR_CASE = ColorManager.get("color cell background");
    Color COULEUR_CASE = ColorManager.get("color cell special");
    int SIZE_BORDER = 5;
    int NB_MIN_LIGNES = 2;
    int NB_MAX_LIGNES = 20;
    int NB_MIN_COLONNES = 2;
    int NB_MAX_COLONNES = 30;
    int LARGEUR_MIN_CELLULE = 60;
    int HAUTEUR_MIN_CELLULE = 40;
    int TAILLE_ICONE = 35;
    int TAILLE_ICONE_FLECHE = 25;
    int ESPACE_TABLEAU_BOUTON = 10;
    int LARGEUR_PANEL_BOUTON = TAILLE_ICONE + ESPACE_TABLEAU_BOUTON;
    int ESPACE_FLECHE_CHAMP = 5;
    int FONT_SIZE_TABLEAU = 20;
    int FONT_NORMAL = 20;
    int LARGEUR_MIN_CHAMP = 2 * FONT_NORMAL;
    int HAUTEUR_MIN_CHAMP = FONT_NORMAL + 10;
    int LARGEUR_CELLULE_SEPAREE = 90;
    int HAUTEUR_CELLULE_SEPAREE = 60;

    public static enum TYPE_CASES {

        ROW, COLUMN
    }

    public static enum ORIENTATIONS {

        HAUT(0, TYPE_CASES.COLUMN), GAUCHE(1, TYPE_CASES.ROW), DROIT(2, TYPE_CASES.ROW), BAS(3, TYPE_CASES.COLUMN);
        private final int index;
        private final TYPE_CASES typeCases;

        private ORIENTATIONS(int index, TYPE_CASES typeCases) {
            this.index = index;
            this.typeCases = typeCases;
        }

        public int getIndex() {
            return index;
        }

        public TYPE_CASES getTypeCases() {
            return typeCases;
        }
    }

    public static enum ETAT {

        NORMAL, AJOUTER, SUPPRIMER, COLORER, AJOUTER_FLECHES, SUPPRIMER_FLECHES
    }
    
    public static enum FIRST_CASE{
        NORMAL, NOT_VISIBLE, SEPARATE
    }
}
