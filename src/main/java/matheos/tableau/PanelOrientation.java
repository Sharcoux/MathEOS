/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package matheos.tableau;

import matheos.tableau.ActionTableau.POSITION;
import matheos.tableau.TableConstants.ORIENTATIONS;
import static matheos.tableau.TableConstants.LARGEUR_PANEL_BOUTON;
import static matheos.tableau.TableConstants.ESPACE_FLECHE_CHAMP;
import static matheos.tableau.TableConstants.TAILLE_ICONE_FLECHE;
import static matheos.tableau.TableConstants.TAILLE_ICONE;
import static matheos.tableau.TableConstants.ESPACE_TABLEAU_BOUTON;
import matheos.utils.objets.Icone;
import matheos.utils.texte.JLimitedMathTextPane;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

/**
 * Classe gérant le positionnement des différents composants et des panels
 * autour du tableau en fonction de leur position relative au tableau (HAUT,
 * GAUCHE, DROITE, BAS). Un composant sera dimensionné de sorte que sa longueur
 * correspondra au côté le plus long de son parent, et sa largeur au côté le
 * moins long. Ainsi, pour un panel orienté HAUT, sa longueur sera sa longueur,
 * et sa largeur sera sa hauteur, alors que pour un composant orienté GAUCHE, sa
 * longueur sera sa hauteur et sa largeur sera sa largeur.
 *
 * @author Guillaume
 */
public class PanelOrientation {

    public enum DIMENSION {

        LARGEUR, HAUTEUR
    }
    private static Tableau tableau;
    private Orientable orientable;

    public static void setTableau(Tableau tableau) {
        PanelOrientation.tableau = tableau;
    }

    /**
     * Crée un PanelOrientation avec une orientation définie en paramètre (HAUT,
     * GAUCHE, DROITE, BAS)
     *
     * @param orientation
     */
    public PanelOrientation(ORIENTATIONS orientation) {
        switch (orientation) {
            case HAUT:
                orientable = new OrientationHaut();
                break;
            case GAUCHE:
                orientable = new OrientationGauche();
                break;
            case DROIT:
                orientable = new OrientationDroit();
                break;
            case BAS:
                orientable = new OrientationBas();
                break;
        }
    }

    public ORIENTATIONS getPosition() {
        return orientable.getOrientation();
    }

    /**
     * Permet de contraindre le panel support des panels d'actions. En
     * particulier, la largeur du panel support est contrainte à partir de celle
     * du panel fils.
     *
     * @param layout le layout du panel parent
     * @param supportParent le panel support, qui contient le child
     * @param child le panel fils du supportPanel
     */
    public void orienterPositionSupportParent(SpringLayout layout, Component supportParent, Component child) {
        layout.putConstraint(orientable.getLongueurNordOuest(), child, 0, orientable.getLongueurNordOuest(), supportParent);
        layout.putConstraint(orientable.getLongueurSudEst(), supportParent, 0, orientable.getLongueurSudEst(), child);
        layout.putConstraint(orientable.getLargeurNordOuest(), child, 0, orientable.getLargeurNordOuest(), supportParent);
        layout.putConstraint(orientable.getLargeurSudEst(), child, 0, orientable.getLargeurSudEst(), supportParent);
    }

    /**
     * Permet de contraindre le panel support des composants d'actions (flèches
     * de proportionnalité, icones d'ajouts ou de suppression de ligne, ...) En
     * particulier, la largeur du supportChild dépend de la largeur du composant
     * le plus large qu'il contient.
     *
     * @param layout le layout du supportChild
     * @param supportChild le JPanel contenant les children
     * @param children la liste des ResizableComponentTableau contenu sur le
     * supportChild
     * @param coef le coef de proportionnalité lié à la taille de l'onglet
     */
    public void orienterPositionSupportChild(SpringLayout layout, Component supportChild, List<? extends ResizableComponentTableau> children, float coef) {
        ResizableComponentTableau cMax = getMaxComponentLargeur(children);
        layout.getConstraints(supportChild).setConstraint(orientable.getLongueurSudEst(), null);
        if (cMax == null) {
            SpringLayout.Constraints constParent = layout.getConstraints(supportChild);
            setLargeur(constParent, Spring.scale(Spring.constant(orientable.getLargeurMin()), coef));
        } else if (cMax == null || getPreferredLargeur(cMax) <= LARGEUR_PANEL_BOUTON) {
            SpringLayout.Constraints constParent = layout.getConstraints(supportChild);
            setLargeur(constParent, Spring.scale(Spring.constant(LARGEUR_PANEL_BOUTON), coef));
        } else {
            Component child = (Component) cMax;
            layout.putConstraint(orientable.getLongueurNordOuest(), child, 0, orientable.getLongueurNordOuest(), supportChild);
            layout.putConstraint(orientable.getLongueurSudEst(), supportChild, 0, orientable.getLongueurSudEst(), child);
        }
    }

    /**
     * Renvoie le composant ayant la largeur normale la plus élevé parmi la
     * liste passée en paramètre.
     *
     * @param children la liste des composants que l'on souhaite comparer
     * @return le {@link ResizableComponentTableau} ayant la largeur normale la
     * plus élevée
     */
    public ResizableComponentTableau getMaxComponentLargeur(List<? extends ResizableComponentTableau> children) {
        ResizableComponentTableau cMax = trouverElementLePlusLarge(orientable.getOrientation(), children);
        if (cMax != null) {
            return cMax;
        }
        return null;
    }

    /**
     * Renvoie la largeur normal du panel action support des composants
     * d'actions (flèches de proportionnalités, boutons ajout de ligne,
     * suppression, ...) La largeur normale correspond à la largeur, au sens du
     * PanelOrientation, sans l'application du coef de proportionnalité lié à la
     * taille de l'onglet.
     *
     * @param supportChild le panel support dont on cherche la dimension normale
     * @param children l'ensemble des composants contenu sur le supportChild
     * @return la largeur normale du supportChild
     */
    public int getLargeurNormaleSupportChild(Component supportChild, List<? extends ResizableComponentTableau> children) {
        if (children == null || children.isEmpty()) {
            return orientable.getLargeurMin();
        }
        int largeur = LARGEUR_PANEL_BOUTON;
        if (children != null && !children.isEmpty()) {
            ResizableComponentTableau cMax = trouverElementLePlusLarge(orientable.getOrientation(), children);
            int largeurChildren = getPreferredLargeur(cMax);
            largeur = Math.max(largeurChildren, largeur);
        }
        return largeur;
    }

    /**
     * Positionne les boutons-icones (ajout de ligne, suppression, coloration,
     * ...) sur le panel parent.
     *
     * @param layout le layout du parent
     * @param parent le panel parent contenant les boutons
     * @param children l'ensemble des boutons présent sur le parent
     * @param position la {@link POSITION} des boutons par rapport au tableau
     * (entre les cases ou au milieu des cases)
     * @param coef le coef de proportionnalité lié à la taille de l'onglet
     */
    public void positionnerBoutons(SpringLayout layout, Component parent, List<? extends Component> children, POSITION position, float coef) {
        int largeur = 0;
        switch (position) {
            case ENTRE_CASE:
//                    for(int i = 0; i < tableau.getColumnCount(); i++){
//                        layout.putConstraint(SpringLayout.WEST, children.get(i), largeur, SpringLayout.WEST, tableau);
//                        largeur += tableau.getColumnModel().getColumn(i).getPreferredWidth();
//                    }
//                    layout.putConstraint(SpringLayout.WEST, children.get(tableau.getColumnCount()), largeur, SpringLayout.WEST, tableau);

                layout.putConstraint(orientable.getLargeurNordOuest(), children.get(0), 0, orientable.getLargeurNordOuest(), parent);
                putConstraint(layout, orientable.getBordExterieur(), parent, 0, orientable.getBordExterieur(), children.get(0));
                for (int i = 0; i < getCasesCount(); i++) {
                    largeur = getLargeurCaseTableau(i);
                    layout.putConstraint(getPositionCenter(), children.get(i + 1), largeur, getPositionCenter(), children.get(i));
                    putConstraint(layout, orientable.getBordExterieur(), parent, 0, orientable.getBordExterieur(), children.get(i + 1));
                }
                break;
            case MILIEU_CASE:
                int ajout = (int) (getLargeurCaseTableau(0) / 2);
                for (int i = 0; i < getCasesCount(); i++) {
                    layout.putConstraint(orientable.getLargeurNordOuest(), children.get(i), largeur + ajout, orientable.getLargeurNordOuest(), parent);
                    putConstraint(layout, orientable.getBordExterieur(), parent, 0, orientable.getBordExterieur(), children.get(i));
                    if (i < getCasesCount() - 1) {
                        largeur += getLargeurCaseTableau(i);
                        ajout = (int) (getLargeurCaseTableau(i + 1) / 2);
                    }
                }
                break;
        }
    }

    /**
     * Permet de dimensionner et positionner les panels support des flèches de
     * proportionnalité sur leur parent.
     *
     * @param layout le layout du panel support des {@link FlechePanel}
     * @param panelFleches le panel support des {@link FlechePanel}
     * @param children l'ensemble des {@link FlechePanel} contenus sur le
     * panelFleches
     * @param coef le coef de proportionnalité lié à la taille de l'onglet
     */
    public void positionnerPanelFleches(SpringLayout layout, Component panelFleches, List<FlechePanel> children, float coef) {
        Component cMax = (Component) getMaxComponentLargeur(children);
        for (FlechePanel child : children) {
            DataFleche modeleFleche = child.getModeleFleche();
            Component labelMilieu = child.getLabelMilieu();
            int indexDepart = Math.min(modeleFleche.getIndexDepart(), modeleFleche.getIndexArrivee());
            int largeur = 0;
            int index = 0;
            while (index <= indexDepart) {
                largeur += getLargeurCaseTableau(index);
                index++;
            }
            Spring positionDebut = Spring.constant(largeur + getPreferredLongueur(labelMilieu) / 2 + (int) (TAILLE_ICONE / 2 * coef));
            layout.putConstraint(getPositionCenter(), child, positionDebut, orientable.getLargeurNordOuest(), tableau);
            layout.getConstraints(child).setConstraint(orientable.getBordInterieur(), null);
            layout.getConstraints(child).setConstraint(orientable.getBordExterieur(), null);
            if (!child.equals(cMax)) {
                layout.putConstraint(orientable.getBordInterieur(), child, 0, orientable.getBordInterieur(), panelFleches);
            }
        }
    }

    /**
     * Positionne et dimensionner l'intérieur des {@link FlechePanel}, c'est à
     * dire les icones constituant la flèche de proportionnalité, ainsi que le
     * champ de texte ou la croix de suppression.
     *
     * @param layout le layout du panel contenant les différents éléments
     * onstituant la flèche de proportionnalité
     * @param flechePanel le {@link FlechePanel} dont on souhaite positionner
     * les composants
     * @param coef le coef de proportionnalité lié à la taille de l'onglet
     */
    public void positionnerInterieurPanelFleche(SpringLayout layout, FlechePanel flechePanel, float coef) {
        DataFleche modeleFleche = flechePanel.getModeleFleche();
        int indexDebut = modeleFleche.getIndexDepart();
        int indexArrivee = modeleFleche.getIndexArrivee();
        FlechePanel.ETAT etat = flechePanel.getEtat();
        Component componentEtat = null;
        Component labelDebut = flechePanel.getLabelDebut();
        JLabel labelMilieu = flechePanel.getLabelMilieu();
        Component labelFin = flechePanel.getLabelFin();

        Spring espaceFlecheChamp = Spring.scale(Spring.constant(ESPACE_FLECHE_CHAMP), coef);

        //En fonction de l'etat, on va afficher le texte ou la croix de suppression
        switch (etat) {
            case TEXTE:
                componentEtat = flechePanel.getTextPane();
                break;
            case SUPPRESSION:
                componentEtat = flechePanel.getPanelCroix();
                break;
        }
        //On réinitialise car sinon les labelDebut et labelFin gardent leur taille précédente
        //car rien ne vient les redimensionner par la suite
        layout.getConstraints(labelDebut).setConstraint(orientable.getBordExterieur(), null);
        layout.getConstraints(labelFin).setConstraint(orientable.getBordExterieur(), null);

        //On dimensionne la largeur
        putConstraint(layout, orientable.getBordExterieur(), componentEtat, 0, orientable.getBordExterieur(), flechePanel);

        putConstraint(layout, orientable.getBordExterieur(), labelDebut, espaceFlecheChamp, orientable.getBordInterieur(), componentEtat);
        putConstraint(layout, orientable.getBordExterieur(), labelMilieu, espaceFlecheChamp, orientable.getBordInterieur(), componentEtat);
        putConstraint(layout, orientable.getBordExterieur(), labelFin, espaceFlecheChamp, orientable.getBordInterieur(), componentEtat);

        int longueurNormaleFlecheMilieu = calculerLongueurFlecheMilieu(modeleFleche);
        Dimension dimensionLabelMilieu = getDimensionsAdaptees((int) (longueurNormaleFlecheMilieu * coef + 1), (int) (TAILLE_ICONE_FLECHE * coef));
        ((Icone) labelMilieu.getIcon()).setSize((int) dimensionLabelMilieu.getWidth(), (int) dimensionLabelMilieu.getHeight());
        setPreferredSize(labelMilieu, (int) (longueurNormaleFlecheMilieu * coef + 1), (int) (TAILLE_ICONE_FLECHE * coef));

        putConstraint(layout, orientable.getBordInterieur(), flechePanel, 0, orientable.getBordInterieur(), labelMilieu);

        //On dimensionne la longueur
        layout.putConstraint(getPositionCenter(), componentEtat, 0, getPositionCenter(), flechePanel);

        if (indexDebut < indexArrivee) {
            layout.putConstraint(orientable.getLargeurSudEst(), labelDebut, 0, orientable.getLargeurNordOuest(), labelMilieu);
            layout.putConstraint(orientable.getLargeurNordOuest(), labelFin, 0, orientable.getLargeurSudEst(), labelMilieu);
        } else {
            layout.putConstraint(orientable.getLargeurSudEst(), labelFin, 0, orientable.getLargeurNordOuest(), labelMilieu);
            layout.putConstraint(orientable.getLargeurNordOuest(), labelDebut, 0, orientable.getLargeurSudEst(), labelMilieu);
        }

        layout.putConstraint(getPositionCenter(), labelMilieu, 0, getPositionCenter(), flechePanel);

        int largeur = (int) Math.max((2 * TAILLE_ICONE_FLECHE + longueurNormaleFlecheMilieu) * coef, getPreferredLongueur(componentEtat)) + 1;
        Spring largeurParent = Spring.constant(largeur);
        SpringLayout.Constraints constParent = layout.getConstraints(flechePanel);
        setLongueur(constParent, largeurParent);

//        int largeur = (int) Math.max((2 * TAILLE_ICONE_FLECHE + longueurNormaleFlecheMilieu) * coef, getPreferredWidth(componentEtat));
//        int hauteur = (int) Math.max(LARGEUR_PANEL_BOUTON * coef, TAILLE_ICONE_FLECHE * coef + ESPACE_FLECHE_CHAMP * coef + getPreferredHeight(componentEtat));
//        Spring largeurParent = Spring.constant(largeur);
//        Spring hauteurParent = Spring.constant(hauteur);
//        SpringLayout.Constraints constParent = layout.getConstraints(flechePanel);
//        setLongueur(constParent, largeurParent);
//        setLargeur(constParent, hauteurParent);
    }

    /**
     * Renvoie les dimensions normal d'un {@link FlechePanel}, c'est à dire ses
     * dimensions sans l'application d'un coefficient de proportionnalité.
     *
     * @param flechePanel le {@link FlechePanel} dont on souhaite connaître les
     * dimensions normales
     * @return la {@link Dimension} normale du panel de flèche passé en paramètre
     */
    public Dimension getNormalDimensionFlechePanel(FlechePanel flechePanel) {
        Dimension dimension = new Dimension();
        DataFleche modeleFleche = flechePanel.getModeleFleche();
        JLimitedMathTextPane text = flechePanel.getTextPane();
        int longueurIconeMilieu = calculerLongueurFlecheMilieu(modeleFleche);
//        int longueurChampTexte = Math.max(MathTools.getStringWidth(text, 0, text.getHTMLdoc().getLength() + 1, JLimitedMathTextPane.FONT.deriveFont(FONT_NORMAL + FONT_NORMAL / 5)) + FONT_NORMAL / 2, LARGEUR_MIN_CHAMP);
//        int hauteurChampTexte = Math.max(MathTools.getStringHeight(text, 0, text.getHTMLdoc().getLength() + 1, JLimitedMathTextPane.FONT.deriveFont(FONT_NORMAL + FONT_NORMAL / 5)) + FONT_NORMAL / 5, HAUTEUR_MIN_CHAMP);
//        Dimension dimensionTexte = getDimensionsAdaptees(longueurChampTexte, hauteurChampTexte);
        Dimension dimensionTexte = text.getContentSize();
        int longueur = Math.max(2 * TAILLE_ICONE_FLECHE + longueurIconeMilieu, (int) dimensionTexte.getWidth());
        int largeur = Math.max(LARGEUR_PANEL_BOUTON, TAILLE_ICONE_FLECHE + ESPACE_FLECHE_CHAMP + (int) dimensionTexte.getHeight());
        dimension.setSize(getDimensionsAdaptees(longueur, largeur));
        return dimension;
    }

    /**
     * Calcule la longueur normale de la flèche de proportionalité (c'est à dire la longueur
     * de l'icone du milieu), sans tenir compre du coefficient de proportionnalité.
     * La longueur se calcule par la somme des tailles (largeur ou hauteur) des
     * cases du tableau entre le début et la fin de la flèche de proportionnalité.
     * 
     * @param modeleFleche le {@link FlechePanel} dont on cherche la longueur de la
     * flèche milieu
     * @return la longueur normale de la flèche milieu
     */
    public int calculerLongueurFlecheMilieu(DataFleche modeleFleche) {
        int indexDepart = Math.min(modeleFleche.getIndexDepart(), modeleFleche.getIndexArrivee());
        int indexArrivee = Math.max(modeleFleche.getIndexDepart(), modeleFleche.getIndexArrivee());
        int longueurIconeMilieu = 0;
        int index = indexDepart + 1;
        while (index < indexArrivee) {
            longueurIconeMilieu += getLargeurCasesTableau(index);
            index++;
        }
        return longueurIconeMilieu;
    }

    /**
     * Permet de déterminer l'élément le plus large d'une liste d'élément.
     * 
     * @param orientation l'orientation de la liste (HAUT, GAUCHE, DROIT, BAS)
     * @param children la liste des composants dont on veut connaitre le plus large
     * @return le {@link ResizableComponentTableau} ayant la largeur normale la plus
     * importante ou null si la liste est null ou vide
     */
    private ResizableComponentTableau trouverElementLePlusLarge(ORIENTATIONS orientation, List<? extends ResizableComponentTableau> children) {
        if (children == null || children.isEmpty()) {
            return null;
        }
        ResizableComponentTableau cMax = Collections.max(children, new ComparatorComponent(orientation));
        return cMax;
    }

    /**
     * Classe permettant de comparer des {@link ResizableComponentTableau} sur 
     * leur largeur.
     */
    private static class ComparatorComponent implements Comparator<ResizableComponentTableau> {

        private ORIENTATIONS orientation;

        private ComparatorComponent(ORIENTATIONS orientation) {
            this.orientation = orientation;
        }

        @Override
        public int compare(ResizableComponentTableau o1, ResizableComponentTableau o2) {
            switch (orientation) {
                case HAUT:
                    return new Integer(o1.getHauteurNormale()).compareTo(o2.getHauteurNormale());
                case GAUCHE:
                    return new Integer(o1.getLargeurNormale()).compareTo(o2.getLargeurNormale());
                case DROIT:
                    return new Integer(o1.getLargeurNormale()).compareTo(o2.getLargeurNormale());
                case BAS:
                    return new Integer(o1.getHauteurNormale()).compareTo(o2.getHauteurNormale());
            }
            return 0;
        }
    }

    private void setPreferredSize(Component c, int longueur, int largeur) {
        switch (orientable.getLongueur()) {
            case LARGEUR:
                c.setPreferredSize(new Dimension(longueur, largeur));
                break;
            case HAUTEUR:
                c.setPreferredSize(new Dimension(largeur, longueur));
                break;
        }
    }

    private void setLargeur(SpringLayout.Constraints springConstraint, Spring value) {
        switch (orientable.getLargeur()) {
            case LARGEUR:
                springConstraint.setWidth(value);
                break;
            case HAUTEUR:
                springConstraint.setHeight(value);
                break;
        }
    }

    private void setLongueur(SpringLayout.Constraints springConstraint, Spring value) {
        switch (orientable.getLongueur()) {
            case LARGEUR:
                springConstraint.setWidth(value);
                break;
            case HAUTEUR:
                springConstraint.setHeight(value);
                break;
        }
    }

    // NOT USED
//    private int getPreferredLongueur(ResizableComponentTableau c) {
//        switch (orientable.getLongueur()) {
//            case LARGEUR:
//                return c.getLargeurNormale();
//            case HAUTEUR:
//                return c.getHauteurNormale();
//            default:
//            	return 0;
//        }
//        
//    }

    private int getPreferredLargeur(ResizableComponentTableau c) {
        switch (orientable.getLargeur()) {
            case LARGEUR:
                return c.getLargeurNormale();
            case HAUTEUR:
                return c.getHauteurNormale();
        }
        return 0;
    }

    private int getPreferredLongueur(Component c) {
        switch (orientable.getLongueur()) {
            case LARGEUR:
                return (int) c.getPreferredSize().getWidth();
            case HAUTEUR:
                return (int) c.getPreferredSize().getHeight();
        }
        return 0;
    }

    // NOT USED
//    private int getPreferredLargeur(Component c) {
//        switch (orientable.getLargeur()) {
//            case LARGEUR:
//                return (int) c.getPreferredSize().getWidth();
//            case HAUTEUR:
//                return (int) c.getPreferredSize().getHeight();
//            default :
//            	return 0;
//        }
//    }
//
//    private int getPreferredHeight(Component c) {
//        return (int) c.getPreferredSize().getHeight();
//    }
//
//    private int getPreferredWidth(Component c) {
//        return (int) c.getPreferredSize().getWidth();
//    }

    private int getCasesCount() {
        switch (orientable.getOrientation().getTypeCases()) {
            case COLUMN:
                return tableau.getColumnCount();
            case ROW:
                return tableau.getRowCount();
        }
        return 0;
    }

    private int getLargeurCaseTableau(int index) {
        switch (orientable.getOrientation().getTypeCases()) {
            case COLUMN:
                return tableau.getColumnModel().getColumn(index).getPreferredWidth();
            case ROW:
                return tableau.getRowHeight(index);
        }
        return 0;
    }

    private int getLargeurCasesTableau(int index) {
        switch (orientable.getOrientation().getTypeCases()) {
            case COLUMN:
                return tableau.calculerLargeurColonne(index);
            case ROW:
                return tableau.calculerLargeurLigne(index);
        }
        return 0;
    }

    private String getPositionCenter() {
        switch (orientable.getOrientation().getTypeCases()) {
            case COLUMN:
                return SpringLayout.HORIZONTAL_CENTER;
            case ROW:
                return SpringLayout.VERTICAL_CENTER;
        }
        return null;
    }

    private Dimension getDimensionsAdaptees(int longueur, int largeur) {
        Dimension dimension = new Dimension();
        switch (orientable.getOrientation().getTypeCases()) {
            case COLUMN:
                dimension.setSize(longueur, largeur);
                break;
            case ROW:
                dimension.setSize(largeur, longueur);
                break;
        }
        return dimension;
    }

    private void putConstraint(SpringLayout layout, String e1, Component c1, Spring s, String e2, Component c2) {
        if (orientable.getLongueurNordOuest().equals(orientable.getBordExterieur())) {
            layout.putConstraint(e1, c1, s, e2, c2);
        } else {
            layout.putConstraint(e2, c2, s, e1, c1);
        }
    }

    private void putConstraint(SpringLayout layout, String e1, Component c1, int pad, String e2, Component c2) {
        putConstraint(layout, e1, c1, Spring.constant(pad), e2, c2);
    }

    /**
     * Interface définissant les méthodes qui permettent de paramétrer les
     * 4 orientations (HAUT, GAUCHE, DROIT, BAS)
     */
    private interface Orientable {

        /**
         * Renvoie l'orientation du Panel en question.
         * @return 
         */
        ORIENTATIONS getOrientation();

        /**
         * Renvoie le {@Link SpringLayout} associé à la longueur NORTH OU WEST
         * en fonction du Panel.
         * 
         * @return 
         */
        String getLongueurNordOuest();

        /**
         * Renvoie le {@Link SpringLayout} associé à la largeur WEST OU NORTH
         * en fonction du Panel.
         * 
         * @return 
         */
        String getLargeurNordOuest();

        /**
         * Renvoie le {@Link SpringLayout} associé à la largeur EAST OU SOUTH
         * en fonction du Panel.
         * 
         * @return 
         */
        String getLargeurSudEst();

        /**
         * Renvoie le {@Link SpringLayout} associé à la longueur SOUTH OU EAST
         * en fonction du Panel.
         * 
         * @return 
         */
        String getLongueurSudEst();

        /**
         * Renvoie le {@Link SpringLayout} associé au bord le plus éloigné du tableau.
         * 
         * @return 
         */
        String getBordExterieur();

        /**
         * Renvoie le {@Link SpringLayout} associé au bord le plus proche du tableau.
         * 
         * @return 
         */
        String getBordInterieur();

        /**
         * Détermine si la largeur d'un <code>Orientable</code> se définit par
         * sa largeur réelle ou par sa hauteur réelle.
         * 
         * @return 
         */
        DIMENSION getLargeur();

        /**
         * Détermine si la longueur d'un <code>Orientable</code> se définit par
         * sa longueur réelle ou par sa largeur réelle.
         * 
         * @return 
         */
        DIMENSION getLongueur();

        /**
         * Renvoie la largeur minimum d'un <code>Orientable</code>, c'est à dire
         * la largeur qu'aura cet <code>Orientable</code> s'il ne contient aucun
         * autre composant ou s'ils sont tous moins large que cette valeur.
         * 
         * @return 
         */
        int getLargeurMin();
    }

    private class OrientationHaut implements Orientable {

        @Override
        public ORIENTATIONS getOrientation() {
            return ORIENTATIONS.HAUT;
        }

        @Override
        public String getLongueurNordOuest() {
            return SpringLayout.NORTH;
        }

        @Override
        public String getLargeurNordOuest() {
            return SpringLayout.WEST;
        }

        @Override
        public String getLargeurSudEst() {
            return SpringLayout.EAST;
        }

        @Override
        public String getLongueurSudEst() {
            return SpringLayout.SOUTH;
        }

        @Override
        public String getBordExterieur() {
            return SpringLayout.NORTH;
        }

        @Override
        public String getBordInterieur() {
            return SpringLayout.SOUTH;
        }

        @Override
        public DIMENSION getLargeur() {
            return DIMENSION.HAUTEUR;
        }

        @Override
        public DIMENSION getLongueur() {
            return DIMENSION.LARGEUR;
        }

        @Override
        public int getLargeurMin() {
            return LARGEUR_PANEL_BOUTON;
        }
    }

    private class OrientationGauche implements Orientable {

        @Override
        public ORIENTATIONS getOrientation() {
            return ORIENTATIONS.GAUCHE;
        }

        @Override
        public String getLongueurNordOuest() {
            return SpringLayout.WEST;
        }

        @Override
        public String getLargeurNordOuest() {
            return SpringLayout.NORTH;
        }

        @Override
        public String getLargeurSudEst() {
            return SpringLayout.SOUTH;
        }

        @Override
        public String getLongueurSudEst() {
            return SpringLayout.EAST;
        }

        @Override
        public String getBordExterieur() {
            return SpringLayout.WEST;
        }

        @Override
        public String getBordInterieur() {
            return SpringLayout.EAST;
        }

        @Override
        public DIMENSION getLargeur() {
            return DIMENSION.LARGEUR;
        }

        @Override
        public DIMENSION getLongueur() {
            return DIMENSION.HAUTEUR;
        }

        @Override
        public int getLargeurMin() {
            return LARGEUR_PANEL_BOUTON;
        }
    }

    private class OrientationDroit implements Orientable {

        @Override
        public ORIENTATIONS getOrientation() {
            return ORIENTATIONS.DROIT;
        }

        @Override
        public String getLongueurNordOuest() {
            return SpringLayout.WEST;
        }

        @Override
        public String getLargeurNordOuest() {
            return SpringLayout.NORTH;
        }

        @Override
        public String getLargeurSudEst() {
            return SpringLayout.SOUTH;
        }

        @Override
        public String getLongueurSudEst() {
            return SpringLayout.EAST;
        }

        @Override
        public String getBordExterieur() {
            return SpringLayout.EAST;
        }

        @Override
        public String getBordInterieur() {
            return SpringLayout.WEST;
        }

        @Override
        public DIMENSION getLargeur() {
            return DIMENSION.LARGEUR;
        }

        @Override
        public DIMENSION getLongueur() {
            return DIMENSION.HAUTEUR;
        }

        @Override
        public int getLargeurMin() {
            return ESPACE_TABLEAU_BOUTON;
        }
    }

    private class OrientationBas implements Orientable {

        @Override
        public ORIENTATIONS getOrientation() {
            return ORIENTATIONS.BAS;
        }

        @Override
        public String getLongueurNordOuest() {
            return SpringLayout.NORTH;
        }

        @Override
        public String getLargeurNordOuest() {
            return SpringLayout.WEST;
        }

        @Override
        public String getLargeurSudEst() {
            return SpringLayout.EAST;
        }

        @Override
        public String getLongueurSudEst() {
            return SpringLayout.SOUTH;
        }

        @Override
        public String getBordExterieur() {
            return SpringLayout.SOUTH;
        }

        @Override
        public String getBordInterieur() {
            return SpringLayout.NORTH;
        }

        @Override
        public DIMENSION getLargeur() {
            return DIMENSION.HAUTEUR;
        }

        @Override
        public DIMENSION getLongueur() {
            return DIMENSION.LARGEUR;
        }

        @Override
        public int getLargeurMin() {
            return ESPACE_TABLEAU_BOUTON;
        }
    }
}
