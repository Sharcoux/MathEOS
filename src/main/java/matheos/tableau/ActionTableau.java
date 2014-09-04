/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package matheos.tableau;

import static matheos.tableau.TableConstants.*;

/**
 * Classe permettant de définir les différentes actions qui peuvent affecter le
 * tableau (ajout de lignes, de colonnes, suppression, coloration, ...
 *
 * @author Guillaume
 */
public abstract class ActionTableau {

    public enum POSITION {

        ENTRE_CASE, MILIEU_CASE
    }

    public enum APPARENCE {

        FLECHE, CROIX, PALETTE_ADD, PALETTE_REMOVE, FLECHE_POSITIVE, FLECHE_NEGATIVE
    }

    public enum TYPE {

        NORMAL_BOUTON, FLECHE_BOUTON
    }

    /**
     * Renvoie le {@link TYPE} d'actions, c'est à dire s'il s'agit de boutons à
     * afficher, ou de flèches de proportionnalité.
     *
     * @return
     */
    public abstract TYPE getType();

    /**
     * Renvoie la {@link POSITION} des éléments composant le panel associé à cette action,
     * c'est à dire si les composants pour cette action sont situées entre les 
     * cases du tableau ou au milieu des cases.
     * @return 
     */
    public abstract POSITION getPosition();

    /**
     * Renvoie l'apparence par défaut des icones constituant les boutons de l'action en cours.
     * 
     * @return 
     */
    public abstract APPARENCE getDefaultApparence();

    /**
     * Renvoie l'apparence du bouton passé en paramètre pour cette action.
     * 
     * @param orientation l'orientation du panel support (HAUT, GAUHE, DROIT, BAS)
     * @param position la position du bouton sur le panel support
     * @return 
     */
    public abstract APPARENCE getApparence(ORIENTATIONS orientation, int position);

    /**
     * Renvoie le nombre de boutons qui doivent être affichés pour l'action en cours.
     * 
     * @param orientation l'orientation du panel support (HAUT, GAUHE, DROIT, BAS)
     * @return 
     */
    public abstract int calculerNombreBoutons(ORIENTATIONS orientation);

    /**
     * Méthode définissant l'action à effecteur en cas de clic sur un bouton.
     * 
     * @param orientation l'orientation du panel support (HAUT, GAUHE, DROIT, BAS)
     * @param cle l'objet définissant la position du composant sur lequel on a cliqué
     */
    public abstract void onClic(ORIENTATIONS orientation, Object cle);

    /**
     * Définit les différentes orientations de panel qui sont affectés par l'action en cours.
     * 
     * @param orientation l'orientation du panel support (HAUT, GAUHE, DROIT, BAS)
     * @param panel le panel support qui peut être affecté par l'action
     */
    public abstract void addBoutonPanel(ORIENTATIONS orientation, JBoutonsPanel panel);

    
    /**
     * Classe gérant l'ajout d'une ligne ou d'une colonne au tableau.
     */
    static class ActionAjouter extends ActionTableau {

        private final ControlleurTableau controlleur;
        private final JBoutonsPanel[] ecouteurs = new JBoutonsPanel[2];

        ActionAjouter(ControlleurTableau controlleur) {
            this.controlleur = controlleur;
        }

        @Override
        public TYPE getType() {
            return TYPE.NORMAL_BOUTON;
        }

        @Override
        public POSITION getPosition() {
            return POSITION.ENTRE_CASE;
        }

        @Override
        public APPARENCE getDefaultApparence() {
            return APPARENCE.FLECHE;
        }

        @Override
        public APPARENCE getApparence(ORIENTATIONS orientation, int position) {
            return getDefaultApparence();
        }

        @Override
        public int calculerNombreBoutons(ORIENTATIONS orientation) {
            switch (orientation) {
                case HAUT:
                    return controlleur.getTableau().getColumnCount() + 1;
                case GAUCHE:
                    return controlleur.getTableau().getRowCount() + 1;
                default :
                	throw new IllegalArgumentException("Il n'y a pas de bouton pour l'orientation choisie");
            }
        }

        @Override
        public void onClic(ORIENTATIONS orientation, Object cle) {
            int index = (Integer) cle;
            switch (orientation) {
                case HAUT:
                    if (controlleur.getTableau().getColumnCount() < NB_MAX_COLONNES) {
                        ecouteurs[orientation.getIndex()].ajouterBouton();
                        controlleur.ajouterColonne(index);
                    }
                    break;
                case GAUCHE:
                    if (controlleur.getTableau().getRowCount() < NB_MAX_LIGNES) {
                        ecouteurs[orientation.getIndex()].ajouterBouton();
                        controlleur.ajouterLigne(index);
                    }
                    break;
                default :
                	throw new IllegalArgumentException("Pas d'action possible pour cette orientation");
            }

        }

        @Override
        public void addBoutonPanel(ORIENTATIONS orientation, JBoutonsPanel panel) {
            if (orientation.equals(ORIENTATIONS.HAUT) || orientation.equals(ORIENTATIONS.GAUCHE)) {
                ecouteurs[orientation.getIndex()] = panel;
            }
        }
    }

    /**
     * Classe gérant la suppression d'une ligne ou d'une colonne du tableau.
     */
    static class ActionSupprimer extends ActionTableau {

        private final ControlleurTableau controlleur;
        private final JBoutonsPanel[] ecouteurs = new JBoutonsPanel[2];

        ActionSupprimer(ControlleurTableau controlleur) {
            this.controlleur = controlleur;
        }

        @Override
        public TYPE getType() {
            return TYPE.NORMAL_BOUTON;
        }

        @Override
        public POSITION getPosition() {
            return POSITION.MILIEU_CASE;
        }

        @Override
        public APPARENCE getDefaultApparence() {
            return APPARENCE.CROIX;
        }

        @Override
        public APPARENCE getApparence(ORIENTATIONS orientation, int position) {
            return getDefaultApparence();
        }

        @Override
        public int calculerNombreBoutons(ORIENTATIONS orientation) {
            switch (orientation) {
                case HAUT:
                    return controlleur.getTableau().getColumnCount();
                case GAUCHE:
                    return controlleur.getTableau().getRowCount();
                default :
                	throw new IllegalArgumentException("Il n'y a pas de bouton pour l'orientation choisie");
            }
        }

        @Override
        public void onClic(ORIENTATIONS orientation, Object cle) {
            int index = (Integer) cle;
            switch (orientation) {
                case HAUT:
                    if (controlleur.getTableau().getColumnCount() > NB_MIN_COLONNES) {
                        ecouteurs[orientation.getIndex()].supprimerBouton();
                        controlleur.supprimerColonne(index);
                    }
                    break;
                case GAUCHE:
                    if (controlleur.getTableau().getRowCount() > NB_MIN_LIGNES) {
                        ecouteurs[orientation.getIndex()].supprimerBouton();
                        controlleur.supprimerLigne(index);
                    }
                    break;
                default :
                	throw new IllegalArgumentException("Pas d'action possible pour cette orientation");
            }

        }

        @Override
        public void addBoutonPanel(ORIENTATIONS orientation, JBoutonsPanel panel) {
            if (orientation.equals(ORIENTATIONS.HAUT) || orientation.equals(ORIENTATIONS.GAUCHE)) {
                ecouteurs[orientation.getIndex()] = panel;
            }
        }
    }

    /**
     * Classe gérant la coloration d'une ligne ou d'une colonne du tableau.
     */
    static class ActionColorer extends ActionTableau {

        private final ControlleurTableau controlleur;
        private final JBoutonsPanel[] ecouteurs = new JBoutonsPanel[2];

        ActionColorer(ControlleurTableau controlleur) {
            this.controlleur = controlleur;
        }

        @Override
        public TYPE getType() {
            return TYPE.NORMAL_BOUTON;
        }

        @Override
        public POSITION getPosition() {
            return POSITION.MILIEU_CASE;
        }

        @Override
        public APPARENCE getDefaultApparence() {
            return APPARENCE.PALETTE_ADD;
        }

        @Override
        public APPARENCE getApparence(ORIENTATIONS orientation, int position) {
            return controlleur.isColorated(orientation.getTypeCases(), position) ? APPARENCE.PALETTE_REMOVE : getDefaultApparence();
        }

        @Override
        public int calculerNombreBoutons(ORIENTATIONS orientation) {
            switch (orientation) {
                case HAUT:
                    return controlleur.getTableau().getColumnCount();
                case GAUCHE:
                    return controlleur.getTableau().getRowCount();
                default :
                	throw new IllegalArgumentException("Il n'y a pas de bouton pour l'orientation choisie");
            }
        }

        @Override
        public void onClic(ORIENTATIONS orientation, Object cle) {
            int index = (Integer) cle;
            if (controlleur.isColorated(orientation.getTypeCases(), index)) {
                ecouteurs[orientation.getIndex()].setBoutonApparence(index, APPARENCE.PALETTE_ADD);
                controlleur.decolorerCases(orientation.getTypeCases(), index);
            } else {
                ecouteurs[orientation.getIndex()].setBoutonApparence(index, APPARENCE.PALETTE_REMOVE);
                controlleur.colorerCases(orientation.getTypeCases(), index);
            }
        }

        @Override
        public void addBoutonPanel(ORIENTATIONS orientation, JBoutonsPanel panel) {
            if (orientation.equals(ORIENTATIONS.HAUT) || orientation.equals(ORIENTATIONS.GAUCHE)) {
                ecouteurs[orientation.getIndex()] = panel;
            }
        }
    }

    /**
     * Classe gérant l'ajout d'une flèche de proportionnalité au tableau.
     */
    static class ActionAjouterFleche extends ActionTableau {

        private final ControlleurTableau controlleur;
        private final JBoutonsPanel[] ecouteurs = new JBoutonsPanel[4];
        private int nbClic = 0;
        private int indexDebut = -1;
        private int indexFin = -1;

        ActionAjouterFleche(ControlleurTableau controlleur) {
            this.controlleur = controlleur;
        }

        @Override
        public TYPE getType() {
            return TYPE.NORMAL_BOUTON;
        }

        @Override
        public POSITION getPosition() {
            return POSITION.MILIEU_CASE;
        }

        @Override
        public APPARENCE getDefaultApparence() {
            return APPARENCE.FLECHE;
        }

        @Override
        public APPARENCE getApparence(ORIENTATIONS orientation, int position) {
            return getDefaultApparence();
        }

        @Override
        public int calculerNombreBoutons(ORIENTATIONS orientation) {
            switch (orientation) {
                case HAUT:
                    return controlleur.getTableau().getColumnCount();
                case GAUCHE:
                    return controlleur.getTableau().getRowCount();
                case DROIT:
                    return controlleur.getTableau().getRowCount();
                case BAS:
                    return controlleur.getTableau().getColumnCount();
            }
            throw new IllegalArgumentException("L'orientation ne correspond pas au mode 'Ajouter Fleche'");
        }

        @Override
        public void onClic(ORIENTATIONS orientation, Object cle) {
            int index = (Integer) cle;
            if (nbClic == 0) {
                nbClic = 1;
                indexDebut = index;
                for (JBoutonsPanel panel : ecouteurs) {
                    if (!panel.getOrientation().equals(orientation)) {
                        panel.clear();
                    } else {
                        ModeleFleches fleches = controlleur.getFlechesLieesAUneCase(orientation.getTypeCases(), index);
                        panel.supprimerBouton(index);
                        for (DataFleche fleche : fleches) {
                            if (fleche.getOrientation().equals(orientation) && fleche.getIndexDepart() == index) {
                                panel.supprimerBouton(fleche.getIndexArrivee());
                            }
                        }
                    }
                }
            } else if (nbClic == 1) {
                indexFin = index;
                controlleur.creerFlecheProportionnalite(orientation, indexDebut, indexFin);
                controlleur.setMode(ControlleurTableau.MODE.NORMAL);
            }
        }

        @Override
        public void addBoutonPanel(ORIENTATIONS orientation, JBoutonsPanel panel) {
            ecouteurs[orientation.getIndex()] = panel;
        }
    }

    /**
     * Classe gérant la suppression d'une flèche de proportionnalité autour du tableau.
     */
    static class ActionSupprimerFleche extends ActionTableau {

        private final ControlleurTableau controlleur;

        ActionSupprimerFleche(ControlleurTableau controlleur) {
            this.controlleur = controlleur;
        }

        @Override
        public TYPE getType() {
            return TYPE.FLECHE_BOUTON;
        }

        @Override
        public POSITION getPosition() {
            return null;
        }

        @Override
        public APPARENCE getDefaultApparence() {
            return null;
        }

        @Override
        public APPARENCE getApparence(ORIENTATIONS orientation, int position) {
            return getDefaultApparence();
        }

        @Override
        public int calculerNombreBoutons(ORIENTATIONS orientation) {
            return controlleur.getFleches(orientation).size();
        }

        @Override
        public void onClic(ORIENTATIONS orientation, Object cle) {}

        @Override
        public void addBoutonPanel(ORIENTATIONS orientation, JBoutonsPanel panel) {}
    }
}
