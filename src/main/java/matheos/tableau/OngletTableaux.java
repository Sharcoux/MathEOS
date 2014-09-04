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

import static matheos.tableau.TableConstants.*;
import matheos.elements.ChangeModeListener;
import matheos.elements.Onglet.*;
import matheos.utils.managers.ColorManager;
import matheos.sauvegarde.Data;
import matheos.utils.dialogue.DialogueComplet;

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import matheos.utils.dialogue.DialogueEvent;
import matheos.utils.dialogue.DialogueListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JScrollPane;

/**
 * Panel contenant tous les composants: les panelsBoutons et le tableau.
 *
 * @author Tristan
 */
@SuppressWarnings("serial")
public class OngletTableaux extends OngletTP implements MouseListener, ComponentListener {

    private final ControlleurTableau controlleur;
    private SupportPanel supportPanel;
    private SpringLayout layout = new SpringLayout();
    double coef = 1;
    private final JPanel pan;
    private final JScrollPane scroll;


    /**
     * Crée le panelTableau, crée tous les panelsBoutons et le tableau avec un
     * nombre de lignes et de colonnes prédéfini, puis affiche uniquement le
     * tableau. Rien n'est placé ou dimensionné, ce sera fait automatiquement
     * avec le resize qui va lancer la méthode adapterDimensions.
     */
    public OngletTableaux() {
        addComponentListener(this);
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        pan = new JPanel();
        pan.setBackground(Color.WHITE);
        scroll = new JScrollPane(pan, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.add(scroll, BorderLayout.CENTER);

        pan.addMouseListener(this);
        pan.addMouseListener(new ChangeModeListener(ChangeModeListener.TP));

        controlleur = new ControlleurTableau(this);
        controlleur.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });

        barreOutils.addSwitchOnRight(controlleur.getActionSupprimerFleches());
        barreOutils.addSwitchOnRight(controlleur.getActionAjouterFleches());
        barreOutils.addSwitchOnRight(controlleur.getActionColorer());
        barreOutils.addSwitchOnRight(controlleur.getActionSupprimer());
        barreOutils.addSwitchOnRight(controlleur.getActionAjouter());
        
        nouveau(NB_MIN_LIGNES, NB_MIN_COLONNES);

        validate();
        repaint();
    }

    /**
     * Permet de créer un nouveau TP Tableau.
     *
     * @param nbreLignes le nombre de ligne initial du {@link Tableau}
     * @param nbreColonnes le nombre de colonne initial du {@link Tableau}
     */
    public final void nouveau(int nbreLignes, int nbreColonnes) {
        pan.removeAll();
        layout = new SpringLayout();

        supportPanel = controlleur.nouveau(nbreLignes, nbreColonnes);
        
        pan.setLayout(layout);
        pan.add(supportPanel);        
        
        adapterDimensions(calculCoef());
        revalidate();
        repaint();

        //updateUndoRedo();
    }
    
    public static final int ACTION_PROPORTIONNALITE = 0;
    @Override
    public void setActionEnabled(int actionID, boolean b) {
        switch(actionID) {
            case ACTION_PROPORTIONNALITE :
                controlleur.getActionAjouterFleches().setEnabled(b);
                controlleur.getActionSupprimerFleches().setEnabled(b);
                break;
        }
    }

    /**
     * Calcule le coefficient par lequel il faut multiplier toutes les pour que
     * le tableau rentre dans la fenêtre.
     *
     * @return
     */
    public double calculCoef() {

        if (this.coef > 1) {
            return coef;
        }
        int largeurTableau = supportPanel.getNormalWidth() + 1;
        int hauteurTableau = supportPanel.getNormalHeight() + 1;
        int largeurPanel = (int) this.getSize().getWidth() - 4 * SIZE_BORDER;
        int hauteurPanel = (int) this.getSize().getHeight() - 4 * SIZE_BORDER;
        double coeficient = 1;
        if (largeurTableau != 0 && hauteurTableau != 0) {
            double lcoef = 1;
            double hcoef = 1;
            if (largeurTableau > largeurPanel) {
                lcoef = ((double) (largeurPanel)) / ((double) (largeurTableau));
            }
            if (hauteurTableau > hauteurPanel) {
                hcoef = ((double) (hauteurPanel)) / ((double) (hauteurTableau));
            }
            coeficient = Math.min(lcoef, hcoef);
        }
        if (coeficient < 0.2) {
            coeficient = 0.2;
        }
        return coeficient;
    }

    /**
     * Adapte toutes les dimensions au coefficient, elles sont multipliées par
     * coeff.
     *
     * @param coef coefficient par lequel toutes les dimensions doivent être
     * multipliées
     */
    public void adapterDimensions(double coef) {
        this.coef = coef;
        controlleur.setCoef(coef);
        float coefficient = (float) coef;
        Spring espace = Spring.scale(Spring.constant(SIZE_BORDER), coefficient);

        supportPanel.adapterDimensions(coef);

        layout.putConstraint(SpringLayout.NORTH, supportPanel, espace, SpringLayout.NORTH, pan);
        layout.putConstraint(SpringLayout.WEST, supportPanel, espace, SpringLayout.WEST, pan);

        pan.setPreferredSize(new Dimension((int) (supportPanel.getPreferredSize().getWidth() + 2 * SIZE_BORDER * coefficient), (int) (supportPanel.getPreferredSize().getHeight() + 2 * SIZE_BORDER * coefficient)));
        pan.revalidate();
        pan.repaint();
        revalidate();
        repaint();
        validate();
    }

    /**
     * Fait apparaitre une boîte de dialogue qui demande le nombre de lignes et
     * de colonnes que l'on veut pour le nouveau tableau.
     */
    @Override
    public void nouveau() {
        DialogueComplet dialogue = new DialogueComplet("dialog new table");
        dialogue.addDialogueListener(new DialogueListener() {
            @Override
            public void dialoguePerformed(DialogueEvent event) {
                if(!event.isConfirmButtonPressed()) {return;}
                int nbreLignes = event.getInputInteger("rows");
                int nbreColonnes = event.getInputInteger("columns");
                if (nbreLignes > NB_MAX_LIGNES) {
                    nbreLignes = NB_MAX_LIGNES;
                } else {
                    nbreLignes = Math.max(nbreLignes, NB_MIN_LIGNES);
                }

                if (nbreColonnes > NB_MAX_COLONNES) {
                    nbreColonnes = NB_MAX_COLONNES;
                } else {
                    nbreColonnes = Math.max(nbreColonnes, NB_MIN_COLONNES);
                }
                nouveau(nbreLignes, nbreColonnes);
                setIdTP(0);
                setModified(false);
            }
        });
    }

    protected void nouveauTP() {
        nouveau(NB_MIN_LIGNES, NB_MIN_COLONNES);
    }



    public BufferedImage sauverJPanelDansFileSelonZone() {
        this.requestFocusInWindow();
        
        controlleur.setMode(ControlleurTableau.MODE.NORMAL);

        Rectangle rect = controlleur.getVisibleBounds();

        if (rect != null) {
            int x0 = (int) rect.getX() - SIZE_BORDER;
            int y0 = (int) rect.getY() - SIZE_BORDER;
            int x1 = (int) (rect.getX() + rect.getWidth()) + SIZE_BORDER;
            int y1 = (int) (rect.getY() + rect.getHeight()) + SIZE_BORDER;

            BufferedImage tamponSauvegarde = new BufferedImage(x1 - x0, y1 - y0, BufferedImage.TYPE_3BYTE_BGR);
            Graphics g = tamponSauvegarde.createGraphics();
            g.setColor(Color.WHITE);
            g.translate(-x0, -y0);
            pan.paint(g);
            return tamponSauvegarde;
        }
        return null;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            pan.setBackground(Color.WHITE);
            this.setBackground(Color.WHITE);
        } else {
            pan.setBackground(ColorManager.get("color disabled"));
            this.setBackground(ColorManager.get("color disabled"));
        }
        supportPanel.setEnabled(enabled);
        repaint();
    }

    /**
     * Lorsque la fenêtre est réduite les dimensions sont automatiquement
     * adaptées.
     *
     * @param e
     */
    public void componentResized(ComponentEvent e) {
        double coeficient = calculCoef();
        if (coeficient != this.coef) {
            adapterDimensions(coeficient);
        }
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    /**
     * Passe en mode "normal" : fait disparaitre les panelsBoutons.
     *
     * @param e
     */
    public void mousePressed(MouseEvent e) {
        controlleur.setMode(ControlleurTableau.MODE.NORMAL);
        this.requestFocusInWindow();
        validate();
        repaint();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    @Override
    public BufferedImage capturerImage() {
        return this.sauverJPanelDansFileSelonZone();
    }
    @Override
    public int preferredInsertionSize() {
        return 200;
    }

    @Override
    public void activeContenu(boolean b) {
        if(!b) controlleur.setMode(ControlleurTableau.MODE.NORMAL);
        setEnabled(b);
    }

    @Override
    public void zoomP() {
        boolean zoomChange = false;
        for (int i = 0; i < ZOOM.length; i++) {
            if (this.coef == ZOOM[i] && i != ZOOM.length - 1) {
                this.coef = ZOOM[i + 1];
                zoomChange = true;
                break;
            }
        }
        if (this.coef < ZOOM[0]) {
            this.coef = ZOOM[0];
            zoomChange = true;
        }
        if (zoomChange) {
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.getVerticalScrollBar().setUnitIncrement(50);
            scroll.getHorizontalScrollBar().setUnitIncrement(50);
            adapterDimensions(this.coef);
        }
    }

    @Override
    public void zoomM() {
        if (this.coef == ZOOM[0]) {
            this.coef = 1.0;
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            adapterDimensions(calculCoef());
        } else {
            for (int i = ZOOM.length - 1; i >= 0; i--) {
                if (this.coef == ZOOM[i] && i != 0) {
                    this.coef = ZOOM[i - 1];
                    adapterDimensions(this.coef);
                    break;
                }
            }
        }
    }

    @Override
    public void annuler() {
        controlleur.annuler();
    }

    @Override
    public void refaire() {
        controlleur.refaire();
    }

    @Override
    public boolean peutAnnuler() {
        return controlleur.peutAnnuler();
    }

    @Override
    public boolean peutRefaire() {
        return controlleur.peutRefaire();
    }


//    private class ActionNouveau extends ActionComplete {
//
//        private ActionNouveau() {
//            super("table new");
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            if(ecraserTP()) {
//                creationTableau();
//            }
//        }
//    }

    @Override
    public boolean hasBeenModified() {
        return controlleur.hasBeenModified();
    }

    @Override
    public void setModified(boolean b) {
        controlleur.setModified(b);
    }

    @Override
    public void chargement(/*long id, */Data data) {
        controlleur.chargement(data);
        adapterDimensions(calculCoef());
        //updateUndoRedo();
    }

    @Override
    public Data getDonneesTP() {
        return controlleur.getDonneesTP();
    }
}

/*class Data implements Serializable {

    String nom;
    long idTP;
    DataControlleur dataControlleur;

    Data(String nom, long id, DataControlleur dataControlleur) {
        this.nom = nom;
        this.idTP = id;
        this.dataControlleur = dataControlleur;
    }
}*/
