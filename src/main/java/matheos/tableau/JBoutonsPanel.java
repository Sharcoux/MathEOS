package matheos.tableau;

import matheos.elements.ChangeModeListener;
import static matheos.tableau.ActionTableau.APPARENCE;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

/**
 * Classe servant de support aux actions autour du tableau (ajout ligne,
 * suppression, coloration, ...). Il contient soit les boutons d'actions, soit
 * les flèches de proportionnalité dans leur état de suppression. Il est présent
 * dans tous les modes du tableau autre que "normal".
 *
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class JBoutonsPanel extends JPanel implements ModeleFlechesListener {

    private final SpringLayout layout = new SpringLayout();
    private final TableConstants.ORIENTATIONS orientation;
    private final PanelOrientation panelOrientation;
    private final List<BoutonTableau> listeBoutons;
    private final List<FlechePanel> listeFleches;
    private ActionTableau actionTableau;
    private final ActionTableauListener actionTableauListener = new ActionTableauListener();
    private int nbBoutons = 0;

    public JBoutonsPanel(TableConstants.ORIENTATIONS orientation) {
        setLayout(layout);
        setBackground(Color.WHITE);
        setOpaque(false);
        this.orientation = orientation;
        this.panelOrientation = new PanelOrientation(orientation);
        listeBoutons = new ArrayList<>();
        listeFleches = new ArrayList<>();
        this.addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        this.validate();
        this.repaint();
    }

    public TableConstants.ORIENTATIONS getOrientation() {
        return orientation;
    }

    public void setActionTableau(ActionTableau actionTableau) {
        this.actionTableau = actionTableau;
        clear();
        if (actionTableau != null) {
            actionTableau.addBoutonPanel(orientation, this);
            afficher();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (BoutonTableau bouton : listeBoutons) {
            bouton.setEnabled(enabled);
        }
        for (FlechePanel panel : listeFleches) {
            panel.setEnabled(enabled);
        }
    }

    public void clear() {
        nbBoutons = 0;
        removeAll();
        revalidate();
        repaint();
    }

    private void afficher() {
        this.nbBoutons = actionTableau.calculerNombreBoutons(orientation);
        switch (actionTableau.getType()) {
            case NORMAL_BOUTON:
                creerNormalBoutons();
                break;
            case FLECHE_BOUTON:
                creerFlechesBouton();
                break;
        }
    }

    private void creerNormalBoutons() {
        creerBoutonsSiNecessaire();
        for (int i = 0; i < nbBoutons; i++) {
            afficherBouton(i);
        }
    }

    private void creerFlechesBouton() {
        for (FlechePanel flechePanel : listeFleches) {
            afficherPanelFleche(flechePanel);
        }
    }

    private BoutonTableau creerBoutonsSiNecessaire() {
        BoutonTableau bouton = null;
        while (listeBoutons.size() < nbBoutons) {
            bouton = new BoutonTableau();
            bouton.setApparence(orientation, actionTableau.getDefaultApparence());
            bouton.addActionListener(actionTableauListener);
            listeBoutons.add(bouton);
        }
        return bouton;
    }

    public void adapterDimensions(double coef) {
        layout.invalidateLayout(this);
        float coefficient = (float) coef;

        if (actionTableau != null) {
            switch (actionTableau.getType()) {
                case NORMAL_BOUTON:
                    while (nbBoutons != actionTableau.calculerNombreBoutons(orientation)) {
                        if (nbBoutons < actionTableau.calculerNombreBoutons(orientation)) {
                            ajouterBouton();
                        } else if (nbBoutons > actionTableau.calculerNombreBoutons(orientation)) {
                            supprimerBouton();
                        }
                    }
                    for (int i = 0; i < nbBoutons; i++) {
                        if (!listeBoutons.get(i).getApparence().equals(actionTableau.getApparence(orientation, i))) {
                            listeBoutons.get(i).setApparence(orientation, actionTableau.getApparence(orientation, i));
                        }
                        if (!this.equals(listeBoutons.get(i).getParent())) {
                            this.add(listeBoutons.get(i));
                        }
                        listeBoutons.get(i).adapterDimensions(coef);
                    }

                    panelOrientation.positionnerBoutons(layout, this, listeBoutons, actionTableau.getPosition(), coefficient);
                    break;
                case FLECHE_BOUTON:
                    for (FlechePanel fechePanel : listeFleches) {
                        fechePanel.adapterDimensions(coef);
                    }
                    panelOrientation.positionnerPanelFleches(layout, this, listeFleches, coefficient);
                    break;
            }
        }
        panelOrientation.orienterPositionSupportChild(layout, this, getComposantsAffiches(), coefficient);
        this.validate();
        this.revalidate();
        this.repaint();
    }

    private List<? extends ResizableComponentTableau> getComposantsAffiches() {
        if (actionTableau != null) {
            switch (actionTableau.getType()) {
                case NORMAL_BOUTON:
                    return listeBoutons.subList(0, nbBoutons);
                case FLECHE_BOUTON:
                    return listeFleches;
            }
        }
        return new LinkedList<>();
    }

    public int getLargeurNormale() {
        return panelOrientation.getLargeurNormaleSupportChild(this, getComposantsAffiches());
    }

    private void afficherPanelFleche(FlechePanel flechePanel) {
        this.add(flechePanel);
        flechePanel.removeActionListener(actionTableauListener);
        flechePanel.addActionListener(actionTableauListener);
    }

    private void afficherBouton(int position) {
        BoutonTableau bouton = listeBoutons.get(position);
        bouton.setApparence(orientation, actionTableau.getApparence(orientation, position));
        this.add(bouton);
    }

    public void ajouterBouton() {
        nbBoutons++;
        creerBoutonsSiNecessaire();
        this.afficherBouton(nbBoutons - 1);
        revalidate();
        repaint();
    }

    public void supprimerBouton() {
        this.remove(listeBoutons.get(nbBoutons - 1));
        nbBoutons--;
        revalidate();
        repaint();
    }

    public void supprimerBouton(int position) {
        BoutonTableau bouton = listeBoutons.get(position);
        this.remove(bouton);
        nbBoutons--;
        revalidate();
        repaint();
    }

    public void setBoutonApparence(int position, APPARENCE apparence) {
        listeBoutons.get(position).setApparence(orientation, apparence);
    }

    @Override
    public void modeleFlecheAdded(List<DataFleche> modeleFleches) {
        for (DataFleche fleche : modeleFleches) {
            if (fleche.getOrientation().equals(orientation)) {
                FlechePanel flechePanel = new FlechePanel(FlechePanel.ETAT.SUPPRESSION, fleche);
                listeFleches.add(flechePanel);
                if (actionTableau != null && actionTableau.getType().equals(ActionTableau.TYPE.FLECHE_BOUTON)) {
                    afficherPanelFleche(flechePanel);
                }
            }
        }
        this.revalidate();
        this.repaint();
    }

    @Override
    public void modeleFlecheRemoved(List<DataFleche> modeleFleches) {
        for (DataFleche fleche : modeleFleches) {
            Iterator<FlechePanel> it = listeFleches.iterator();
            while (it.hasNext()) {
                FlechePanel panel = it.next();
                if (fleche.getOrientation().equals(orientation)) {
                    if (panel.getModeleFleche().equals(fleche)) {
                        it.remove();
                        this.remove(panel);
                    }
                }
            }
        }
        this.revalidate();
        this.repaint();
    }

    @Override
    public void modeleFlechesChanged(List<DataFleche> modeleFleches) {
    }

    private class ActionTableauListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof BoutonTableau) {
                for (int i = 0; i < listeBoutons.size(); i++) {
                    if (e.getSource().equals(listeBoutons.get(i))) {
                        actionTableau.onClic(orientation, i);
                        return;
                    }
                }
            } else if (e.getSource() instanceof FlechePanel.PanelCroixAction) {
//                actionTableau.onClic(orientation, ((FlechePanel.PanelCroixAction) e.getSource()).getFlecheAssociee());
            }
        }
    }
}
