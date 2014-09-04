package matheos.tableau;

import matheos.elements.ChangeModeListener;
import static matheos.tableau.TableConstants.*;
import matheos.utils.managers.ColorManager;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

/**
 * Panel support de l'ensemble de la partie fonctionnelle de l'OngletTableau.
 * Il contient donc le tableau ainsi que les panels d'actions et de flèches
 * de proportionnalité autour de celui-ci.
 * 
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class SupportPanel extends JPanel {

    private SpringLayout layout;
    private JSupportActionsPanel supportPanelHaut;
    private JSupportActionsPanel supportPanelGauche;
    private JSupportActionsPanel supportPanelDroit;
    private JSupportActionsPanel supportPanelBas;
    private Tableau tableau;

    public SupportPanel(Tableau tableau) {
        layout = new SpringLayout();
        this.setLayout(layout);
        this.tableau = tableau;
        setOpaque(false);
        creerComposants();
        this.addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
    }

    private void creerComposants() {

        supportPanelHaut = new JSupportActionsPanel(ORIENTATIONS.HAUT);
        supportPanelGauche = new JSupportActionsPanel(ORIENTATIONS.GAUCHE);
        supportPanelDroit = new JSupportActionsPanel(ORIENTATIONS.DROIT);
        supportPanelBas = new JSupportActionsPanel(ORIENTATIONS.BAS);

        this.add(supportPanelHaut);
        this.add(supportPanelBas);
        this.add(supportPanelGauche);
        this.add(supportPanelDroit);
        this.add(tableau);
        revalidate();
        repaint();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setBackground(Color.WHITE);
        } else {
            setBackground(ColorManager.get("color disabled"));
        }
        tableau.setEnabled(enabled);
        supportPanelHaut.setEnabled(enabled);
        supportPanelBas.setEnabled(enabled);
        supportPanelGauche.setEnabled(enabled);
        supportPanelDroit.setEnabled(enabled);
        repaint();
    }

    public void changeMode(ETAT etat, ActionTableau action) {
        switch (etat) {
            case NORMAL:
                supportPanelHaut.changeMode(etat, null);
                supportPanelGauche.changeMode(etat, null);
                supportPanelDroit.changeMode(etat, null);
                supportPanelBas.changeMode(etat, null);
                break;
            case AJOUTER:
                supportPanelHaut.changeMode(etat, action);
                supportPanelGauche.changeMode(etat, action);
                supportPanelDroit.changeMode(etat, null);
                supportPanelBas.changeMode(etat, null);
                break;
            case SUPPRIMER:
                supportPanelHaut.changeMode(etat, action);
                supportPanelGauche.changeMode(etat, action);
                supportPanelDroit.changeMode(etat, null);
                supportPanelBas.changeMode(etat, null);
                break;
            case COLORER:
                supportPanelHaut.changeMode(etat, action);
                supportPanelGauche.changeMode(etat, action);
                supportPanelDroit.changeMode(etat, null);
                supportPanelBas.changeMode(etat, null);
                break;
            case AJOUTER_FLECHES:
                supportPanelHaut.changeMode(etat, action);
                supportPanelGauche.changeMode(etat, action);
                supportPanelDroit.changeMode(etat, action);
                supportPanelBas.changeMode(etat, action);
                break;
            case SUPPRIMER_FLECHES:
                supportPanelHaut.changeMode(etat, action);
                supportPanelGauche.changeMode(etat, action);
                supportPanelDroit.changeMode(etat, action);
                supportPanelBas.changeMode(etat, action);
                break;
        }
    }

    public void checkChampProportionnaliteListener(ControlleurTableau.ChampTextListener listener) {
        supportPanelHaut.checkChampProportionnaliteListener(listener);
        supportPanelGauche.checkChampProportionnaliteListener(listener);
        supportPanelDroit.checkChampProportionnaliteListener(listener);
        supportPanelBas.checkChampProportionnaliteListener(listener);
    }

    public void addModeleFlechesListener(ModeleFleches modeleFleches) {
        supportPanelHaut.addModeleFlechesListener(modeleFleches);
        supportPanelGauche.addModeleFlechesListener(modeleFleches);
        supportPanelDroit.addModeleFlechesListener(modeleFleches);
        supportPanelBas.addModeleFlechesListener(modeleFleches);
    }

    public void removeModeleFlechesListener(ModeleFleches modeleFleches) {
        supportPanelHaut.removeModeleFlechesListener(modeleFleches);
        supportPanelGauche.removeModeleFlechesListener(modeleFleches);
        supportPanelDroit.removeModeleFlechesListener(modeleFleches);
        supportPanelBas.removeModeleFlechesListener(modeleFleches);
    }

    public int getNormalWidth() {
        return tableau.calculerLargeurNormaliseeTableau() + supportPanelGauche.getLargeurNormale() + supportPanelDroit.getLargeurNormale();
    }

    public int getNormalHeight() {
        return tableau.calculerHauteurNormaliseeTableau() + supportPanelHaut.getLargeurNormale() + supportPanelBas.getLargeurNormale();
    }

    public void adapterDimensions(double coef) {
        tableau.adapterDimensions(coef);
        float coefficient = (float) coef;

        Spring largeurDemiIconeMax = Spring.scale(Spring.constant(TAILLE_ICONE / 2), coefficient);

        //Contraintes sur la longueur des panels
        layout.putConstraint(SpringLayout.WEST, supportPanelHaut, -largeurDemiIconeMax.getPreferredValue(), SpringLayout.WEST, tableau);
        layout.putConstraint(SpringLayout.EAST, supportPanelHaut, largeurDemiIconeMax, SpringLayout.EAST, tableau);
        layout.putConstraint(SpringLayout.NORTH, supportPanelGauche, -largeurDemiIconeMax.getPreferredValue(), SpringLayout.NORTH, tableau);
        layout.putConstraint(SpringLayout.SOUTH, supportPanelGauche, largeurDemiIconeMax, SpringLayout.SOUTH, tableau);
        layout.putConstraint(SpringLayout.NORTH, supportPanelDroit, -largeurDemiIconeMax.getPreferredValue(), SpringLayout.NORTH, tableau);
        layout.putConstraint(SpringLayout.SOUTH, supportPanelDroit, largeurDemiIconeMax, SpringLayout.SOUTH, tableau);
        layout.putConstraint(SpringLayout.WEST, supportPanelBas, -largeurDemiIconeMax.getPreferredValue(), SpringLayout.WEST, tableau);
        layout.putConstraint(SpringLayout.EAST, supportPanelBas, largeurDemiIconeMax, SpringLayout.EAST, tableau);

        supportPanelHaut.adapterDimensions(coef);
        supportPanelGauche.adapterDimensions(coef);
        supportPanelDroit.adapterDimensions(coef);
        supportPanelBas.adapterDimensions(coef);

        //Contraintes sur les positions des différents composants
        layout.putConstraint(SpringLayout.NORTH, supportPanelHaut, 0, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, supportPanelGauche, 0, SpringLayout.WEST, this);

        layout.putConstraint(SpringLayout.NORTH, tableau, 0, SpringLayout.SOUTH, supportPanelHaut);
        layout.putConstraint(SpringLayout.WEST, tableau, 0, SpringLayout.EAST, supportPanelGauche);

        layout.putConstraint(SpringLayout.NORTH, supportPanelBas, 0, SpringLayout.SOUTH, tableau);
        layout.putConstraint(SpringLayout.WEST, supportPanelDroit, 0, SpringLayout.EAST, tableau);

        layout.putConstraint(SpringLayout.EAST, this, 0, SpringLayout.EAST, supportPanelDroit);
        layout.putConstraint(SpringLayout.SOUTH, this, 0, SpringLayout.SOUTH, supportPanelBas);

        revalidate();
        repaint();
    }

    /**
     * Panel servant de support aux panels autour du Tableau. Il contient soit
     * le panel de boutons d'actions (mode autre que "normal") soit le panel
     * de flèche de proportionnalité (mode "normal"). 
     * Il possède un {@link PanelOrientation} qui définit sa position relative
     * par rapport au tableau (HAUT, GAUCHE, DROIT, BAS), et qui s'occupe de 
     * le dimensionner et de le positionner en conséquent.
     */
    private class JSupportActionsPanel extends JPanel {

        private final JBoutonsPanel boutonsPanel;
        private final FlecheProportionnalitePanel flechesPanel;
        private final PanelOrientation orientation;
        private ETAT etat = ETAT.NORMAL;
        private SpringLayout layout = new SpringLayout();

        private JSupportActionsPanel(ORIENTATIONS orientation) {
            this.setLayout(layout);
            setOpaque(false);
            this.boutonsPanel = new JBoutonsPanel(orientation);
            this.flechesPanel = new FlecheProportionnalitePanel(orientation);
            this.orientation = new PanelOrientation(orientation);
        }

        private void changeMode(ETAT etat, ActionTableau action) {
            if (this.etat == etat) {
                return;
            }
            this.etat = etat;
            switch (etat) {
                case NORMAL:
                    setNormalEtat();
                    break;
                case AJOUTER:
                    setAjouterEtat(action);
                    break;
                case SUPPRIMER:
                    setSupprimerEtat(action);
                    break;
                case COLORER:
                    setColorerEtat(action);
                    break;
                case AJOUTER_FLECHES:
                    setAjouterFlechesEtat(action);
                    break;
                case SUPPRIMER_FLECHES:
                    setSupprimerFlechesEtat(action);
                    break;
            }
            this.validate();
            this.revalidate();
            this.repaint();
        }

        private void setNormalEtat() {
            this.remove(boutonsPanel);
            this.add(flechesPanel);
        }

        private void setAjouterEtat(ActionTableau action) {
            this.remove(flechesPanel);
            this.add(boutonsPanel);
            boutonsPanel.setActionTableau(action);
        }

        private void setSupprimerEtat(ActionTableau action) {
            this.remove(flechesPanel);
            this.add(boutonsPanel);
            boutonsPanel.setActionTableau(action);
        }

        private void setColorerEtat(ActionTableau action) {
            this.remove(flechesPanel);
            this.add(boutonsPanel);
            boutonsPanel.setActionTableau(action);
        }

        private void setAjouterFlechesEtat(ActionTableau action) {
            this.remove(flechesPanel);
            this.add(boutonsPanel);
            boutonsPanel.setActionTableau(action);
        }

        private void setSupprimerFlechesEtat(ActionTableau action) {
            this.remove(flechesPanel);
            this.add(boutonsPanel);
            boutonsPanel.setActionTableau(action);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            boutonsPanel.setEnabled(enabled);
            flechesPanel.setEnabled(enabled);
            repaint();
        }

        public void adapterDimensions(double coef) {
            float coefficient = (float) coef;
            switch (etat) {
                case NORMAL:
                    adapterDimensionModeNormale(coefficient);
                    break;
                default:
                    adapterDimensionModeBouton(coefficient);
            }
            this.revalidate();
            this.repaint();
        }

        private void adapterDimensionModeNormale(float coef) {
            flechesPanel.adapterDimensions(coef);
            orientation.orienterPositionSupportParent(layout, this, flechesPanel);
        }

        private void adapterDimensionModeBouton(float coef) {
            boutonsPanel.adapterDimensions(coef);
            orientation.orienterPositionSupportParent(layout, this, boutonsPanel);
        }

        public int getLargeurNormale() {
            switch (etat) {
                case NORMAL:
                    return flechesPanel.getLargeurNormale();
                default:
                    return boutonsPanel.getLargeurNormale();
            }
        }

        public void addModeleFlechesListener(ModeleFleches modeleFleches) {
            modeleFleches.addModeleFlechesListener(flechesPanel);
            modeleFleches.addModeleFlechesListener(boutonsPanel);
        }

        public void removeModeleFlechesListener(ModeleFleches modeleFleches) {
            modeleFleches.removeModeleFlechesListener(flechesPanel);
            modeleFleches.removeModeleFlechesListener(boutonsPanel);
        }

        public void checkChampProportionnaliteListener(ControlleurTableau.ChampTextListener listener) {
            flechesPanel.checkChampProportionnaliteListener(listener);
        }
    }
}
