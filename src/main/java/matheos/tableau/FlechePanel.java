/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package matheos.tableau;

import matheos.elements.ChangeModeListener;
import matheos.tableau.ActionTableau.APPARENCE;
import static matheos.tableau.TableConstants.FONT_NORMAL;
import static matheos.tableau.TableConstants.HAUTEUR_MIN_CHAMP;
import static matheos.tableau.TableConstants.LARGEUR_MIN_CHAMP;
import static matheos.tableau.TableConstants.TAILLE_ICONE;
import static matheos.tableau.TableConstants.TAILLE_ICONE_FLECHE;
import matheos.utils.boutons.BoutonPanel;
import matheos.utils.objets.Icone;
import matheos.utils.texte.EditeurIO;
import matheos.utils.texte.JLimitedMathTextPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

/**
 * Classe gérant le rendu visuel des flèches de proportionnalité du tableau.
 *
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class FlechePanel extends JPanel implements ResizableComponentTableau {

    private static final Icone flecheHautDebutGauche;
    private static final Icone flecheHautFinGauche;
    private static final Icone flecheHautDebutDroit;
    private static final Icone flecheHautFinDroit;
    private static final Icone flecheHautMilieu;
    private static final Icone flecheGaucheDebutBas;
    private static final Icone flecheGaucheFinBas;
    private static final Icone flecheGaucheDebutHaut;
    private static final Icone flecheGaucheFinHaut;
    private static final Icone flecheGaucheMilieu;
    private static final Icone flecheDroitDebutBas;
    private static final Icone flecheDroitFinBas;
    private static final Icone flecheDroitDebutHaut;
    private static final Icone flecheDroitFinHaut;
    private static final Icone flecheDroitMilieu;
    private static final Icone flecheBasDebutGauche;
    private static final Icone flecheBasFinGauche;
    private static final Icone flecheBasDebutDroit;
    private static final Icone flecheBasFinFroit;
    private static final Icone flecheBasMilieu;
    private static final Icone iconeCroix;
    private static final Icone iconeCroixRollover;
    private static final Icone iconeCroixSelected;

    static {
        flecheHautDebutGauche = new Icone("images/flechep_tableau_haut_debut_gauche.png");
        flecheHautFinGauche = new Icone("images/flechep_tableau_haut_fin_gauche.png");
        flecheHautDebutDroit = new Icone("images/flechep_tableau_haut_debut_droit.png");
        flecheHautFinDroit = new Icone("images/flechep_tableau_haut_fin_droit.png");
        flecheHautMilieu = new Icone("images/flechep_tableau_haut_milieu.png");

        flecheGaucheDebutBas = new Icone("images/flechep_tableau_gauche_debut_bas.png");
        flecheGaucheFinBas = new Icone("images/flechep_tableau_gauche_fin_bas.png");
        flecheGaucheDebutHaut = new Icone("images/flechep_tableau_gauche_debut_haut.png");
        flecheGaucheFinHaut = new Icone("images/flechep_tableau_gauche_fin_haut.png");
        flecheGaucheMilieu = new Icone("images/flechep_tableau_gauche_milieu.png");

        flecheDroitDebutBas = new Icone("images/flechep_tableau_droit_debut_bas.png");
        flecheDroitFinBas = new Icone("images/flechep_tableau_droit_fin_bas.png");
        flecheDroitDebutHaut = new Icone("images/flechep_tableau_droit_debut_haut.png");
        flecheDroitFinHaut = new Icone("images/flechep_tableau_droit_fin_haut.png");
        flecheDroitMilieu = new Icone("images/flechep_tableau_droit_milieu.png");

        flecheBasDebutGauche = new Icone("images/flechep_tableau_bas_debut_gauche.png");
        flecheBasFinGauche = new Icone("images/flechep_tableau_bas_fin_gauche.png");
        flecheBasDebutDroit = new Icone("images/flechep_tableau_bas_debut_droit.png");
        flecheBasFinFroit = new Icone("images/flechep_tableau_bas_fin_droit.png");
        flecheBasMilieu = new Icone("images/flechep_tableau_bas_milieu.png");

        iconeCroix = new Icone("images/croix_tableau_up.png");
        iconeCroixRollover = new Icone("images/croix_tableau_down_ombre.png");
        iconeCroixSelected = new Icone("images/croix_tableau_up_ombre.png");
    }

    public enum ETAT {
        TEXTE, SUPPRESSION
    }
    
    //les composants associés à la flèche
    private final TextPaneFleche textPane = new TextPaneFleche();
    private final PanelCroixAction panelCroix = new PanelCroixAction();
    
    //Données
    private final DataFleche dataFleche;
    
    private final SpringLayout layout;
    private transient ControlleurTableau.ChampTextListener champTextListener;
    private final PanelOrientation panelOrientation;
    private APPARENCE apparence;
    private ETAT etat;//indique si on affiche le JTextPane ou le bouton de suppression
    
    //les images permettant de dessiner la fleche
    private Icone flecheDebut;
    private Icone flecheMilieu;
    private Icone flecheFin;
    private JLabel labelDebut;
    private JLabel labelMilieu;
    private JLabel labelFin;

    public FlechePanel(ETAT etat, DataFleche dataFleche) {
        this.etat = etat;
        this.dataFleche = dataFleche;
        
        this.panelOrientation = new PanelOrientation(dataFleche.getOrientation());
        layout = new SpringLayout();
        this.setLayout(layout);
        changeEtat(etat);
        
        this.addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        this.setOpaque(false);
        setIcones(new Icone(), new Icone(), new Icone());
        
        textPane.charger(dataFleche.getData());
        setApparence();//Attribue les icones nécessaires au dessin de la flèche
    }
    
    private class TextPaneFleche extends JLimitedMathTextPane {
        private TextPaneFleche() {
            setLongueurMax(7);
            setMinimumSize(new Dimension(LARGEUR_MIN_CHAMP, HAUTEUR_MIN_CHAMP));
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setAlignmentCenter(true);
            setBackground(Color.WHITE);
            if (dataFleche != null) {
                textPane.charger(dataFleche.getData());
                textPane.getUndo().discardAllEdits();
                textPane.setModified(false);
            }
//            dimensionner();
//            addFocusListener();//c'est comme ça qu'on devrait faire mais c'est trop le bordel actuellement
            textPane.addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }
    }

    public DataFleche getModeleFleche() {
        dataFleche.setData(EditeurIO.write(textPane));
        return dataFleche;
    }

    /**
     * Change l'état du
     * <code>FlechePanel</code>, c'est à dire que le composant fonctionnel de la
     * flèche sera soit un champ de texte soit un bouton de suppression.
     *
     * @param etat le nouvel {@link ETAT} du composant
     */
    private void changeEtat(ETAT etat) {
        this.etat = etat;
        switch (etat) {
            case TEXTE:
                this.remove(panelCroix);
                this.add(textPane);
                break;
            case SUPPRESSION:
                this.remove(textPane);
                this.add(panelCroix);
                break;
        }
    }

    public ETAT getEtat() {
        return etat;
    }

    public JLimitedMathTextPane getTextPane() {
        return textPane;
    }

    public JLabel getLabelDebut() {
        return labelDebut;
    }

    public JLabel getLabelMilieu() {
        return labelMilieu;
    }

    public JLabel getLabelFin() {
        return labelFin;
    }

    public BoutonPanel getPanelCroix() {
        return panelCroix;
    }

    /**
     * Permet d'associer les différentes icones au bouton.
     *
     * @param icone
     * @param iconeRollover
     * @param iconeSelected
     */
    private void setIcones(Icone flecheDebut, Icone flecheMilieu, Icone flecheFin) {
        this.flecheDebut = flecheDebut;
        this.flecheMilieu = flecheMilieu;
        this.flecheFin = flecheFin;
        labelDebut = new JLabel(flecheDebut);
        labelMilieu = new JLabel(flecheMilieu);
        labelFin = new JLabel(flecheFin);
        this.add(labelDebut);
        this.add(labelMilieu);
        this.add(labelFin);

    }

    private void setApparence() {
        if (dataFleche.getIndexDepart() < dataFleche.getIndexArrivee()) {
            setApparence(APPARENCE.FLECHE_POSITIVE);
        } else {
            setApparence(APPARENCE.FLECHE_NEGATIVE);
        }
    }

    /**
     * Détermine l'apparence des différentes icones constituant la flèche et
     * effecte les icones correspondantes en conséquent.
     *
     * @param apparence l'apparence des icones à afficher
     */
    public void setApparence(APPARENCE apparence) {
        this.apparence = apparence;
        switch (dataFleche.getOrientation()) {
            case HAUT:
                switch (apparence) {
                    case FLECHE_POSITIVE:
                        setIcones(flecheHautDebutGauche, flecheHautMilieu, flecheHautFinGauche);
                        break;
                    case FLECHE_NEGATIVE:
                        setIcones(flecheHautDebutDroit, flecheHautMilieu, flecheHautFinDroit);
                        break;
                    default:
                        throw new IllegalArgumentException("L'apparence est incorrecte");
                }
                break;
            case GAUCHE:
                switch (apparence) {
                    case FLECHE_POSITIVE:
                        setIcones(flecheGaucheDebutBas, flecheGaucheMilieu, flecheGaucheFinBas);
                        break;
                    case FLECHE_NEGATIVE:
                        setIcones(flecheGaucheDebutHaut, flecheGaucheMilieu, flecheGaucheFinHaut);
                        break;
                    default:
                        throw new IllegalArgumentException("L'apparence est incorrecte");
                }
                break;
            case DROIT:
                switch (apparence) {
                    case FLECHE_POSITIVE:
                        setIcones(flecheDroitDebutBas, flecheDroitMilieu, flecheDroitFinBas);
                        break;
                    case FLECHE_NEGATIVE:
                        setIcones(flecheDroitDebutHaut, flecheDroitMilieu, flecheDroitFinHaut);
                        break;
                    default:
                        throw new IllegalArgumentException("L'apparence est incorrecte");
                }
                break;
            case BAS:
                switch (apparence) {
                    case FLECHE_POSITIVE:
                        setIcones(flecheBasDebutGauche, flecheBasMilieu, flecheBasFinGauche);
                        break;
                    case FLECHE_NEGATIVE:
                        setIcones(flecheBasDebutDroit, flecheBasMilieu, flecheBasFinFroit);
                        break;
                    default:
                        throw new IllegalArgumentException("L'apparence est incorrecte");
                }
                break;
        }
        this.revalidate();
        this.repaint();

    }

    public void adapterDimensions(double coef) {
        float coefficient = (float) coef;
        if (flecheDebut.getIconWidth() != (int) (TAILLE_ICONE_FLECHE * coef)) {
            flecheDebut.setSize((int) (TAILLE_ICONE_FLECHE * coef), (int) (TAILLE_ICONE_FLECHE * coef));
            flecheMilieu.setSize((int) (TAILLE_ICONE_FLECHE * coef), (int) (TAILLE_ICONE_FLECHE * coef));
            flecheFin.setSize((int) (TAILLE_ICONE_FLECHE * coef), (int) (TAILLE_ICONE_FLECHE * coef));
        }
        labelDebut.setIcon(flecheDebut.clone());
        labelMilieu.setIcon(flecheMilieu.clone());
        labelFin.setIcon(flecheFin.clone());
        labelDebut.setPreferredSize(new Dimension(flecheDebut.getIconWidth(), flecheDebut.getIconHeight()));
        labelMilieu.setPreferredSize(new Dimension(flecheMilieu.getIconWidth(), flecheMilieu.getIconHeight()));
        labelFin.setPreferredSize(new Dimension(flecheFin.getIconWidth(), flecheFin.getIconHeight()));

        switch (etat) {
            case TEXTE:
                textPane.setFontSize((int) (FONT_NORMAL * coef));
                textPane.setMinimumSize(new Dimension((int) (LARGEUR_MIN_CHAMP * coef), (int) (HAUTEUR_MIN_CHAMP * coef)));
//                textPane.dimensionner();
                break;
            case SUPPRESSION:
                iconeCroix.setSize((int) (TAILLE_ICONE * coef), (int) (TAILLE_ICONE * coef));
                iconeCroixSelected.setSize((int) (TAILLE_ICONE * coef), (int) (TAILLE_ICONE * coef));
                iconeCroixRollover.setSize((int) (TAILLE_ICONE * coef), (int) (TAILLE_ICONE * coef));
                panelCroix.setPreferredSize(new Dimension((int) (TAILLE_ICONE * coef), (int) (TAILLE_ICONE * coef)));
                break;
        }
        panelOrientation.positionnerInterieurPanelFleche(layout, this, coefficient);
        this.revalidate();
        this.repaint();
        validate();
        textPane.validate();
    }

    public void checkChampProportionnaliteListener(ControlleurTableau.ChampTextListener listener) {
        boolean hasAlreadyControlleurListener = false;
        for (PropertyChangeListener l : textPane.getListeners(PropertyChangeListener.class)) {
            if (l.equals(listener)) {
                hasAlreadyControlleurListener = true;
            }
        }
        if (!hasAlreadyControlleurListener) {
            champTextListener = listener;
            textPane.addPropertyChangeListener(listener);
            textPane.addFocusListener(listener);
        }
    }

    @Override
    public int getLargeurNormale() {
        return (int) panelOrientation.getNormalDimensionFlechePanel(this).getWidth();
    }

    @Override
    public int getHauteurNormale() {
        return (int) panelOrientation.getNormalDimensionFlechePanel(this).getHeight();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textPane.setEnabled(enabled);
        panelCroix.setEnabled(enabled);
        this.repaint();
    }

    public void addActionListener(ActionListener listener) {
        panelCroix.addActionListener(listener);
    }

    public void removeActionListener(ActionListener listener) {
        panelCroix.removeActionListener(listener);
    }

    /**
     * Classe permettant de dialoguer avec le panel support afin de lui renvoyer
     * la clé associé au {@link DataFleche} de ce panel.
     */
    public class PanelCroixAction extends BoutonPanel {

        public PanelCroixAction() {
            setOpaque(false);
            setIcon(iconeCroix);
            setSelectedIcon(iconeCroixSelected);
            setRolloverIcon(iconeCroixRollover);
            
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(champTextListener!=null) champTextListener.controlleur.supprimerFlecheProportionnalite(FlechePanel.this);
                }
            });
        }
    }
}
