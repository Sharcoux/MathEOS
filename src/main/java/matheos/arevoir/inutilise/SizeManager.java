/** «Copyright 2011 François Billioud»
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

package matheos.arevoir.inutilise;

import java.awt.Component;
import java.awt.Toolkit;
import java.util.LinkedList;

/**
 *
 * @author François Billioud
 */
public class SizeManager {

    //taille de l'écran du développeur
    public static final int LARGEUR_ECRAN = Toolkit.getDefaultToolkit().getScreenSize().width;//1366;
    public static final int HAUTEUR_ECRAN = Toolkit.getDefaultToolkit().getScreenSize().height;//768;


    private LinkedList<ComponentInformation> ComponentList = new LinkedList<ComponentInformation>();
    private Convertisseur largeurConvert = new Convertisseur(LARGEUR_ECRAN);
    private Convertisseur hauteurConvert = new Convertisseur(HAUTEUR_ECRAN);
    private Convertisseur fontConvert = new Convertisseur(HAUTEUR_ECRAN);

    /**
     * Gère la taille d'affichage des éléments qu'il collectionne par rapport
     * à la taille d'un composant de référence
     * @param c Composant de référence
     * @param largeurRef largeur supposée de ce composant
     * @param hauteurRef hauteur supposée de ce composant
     */
    public SizeManager(Component c, int largeurRef, int hauteurRef) {
        //FIXME
        //les lignes ci-dessous permettent d'ajouter un componentListener aux éléments inscrits sur le SizeManager.
        //Cependant, ce système est trop gourmand. Il faudrait essayer de lancer le redimensionnement dans un autre thread
        /*c.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                Dimension d = e.getComponent().getSize();       //taille composant de référence
                for (ComponentInformation ci : ComponentList) { //étudie les composants de la liste
                    //resize component
                    int newLargeur = largeurConvert.prop(d.width, ci.largeur);
                    int newHauteur = hauteurConvert.prop(d.height, ci.hauteur);
                    ci.composant.setSize(newLargeur,newHauteur);

                    //resize font
                    int newFontSize = hauteurConvert.prop(d.height, ci.fontSize);
                    Font f = ci.composant.getFont().deriveFont(Font.PLAIN,newFontSize);
                    ci.composant.setFont(f);
                }
            }
            public void componentMoved(ComponentEvent e) {}
            public void componentShown(ComponentEvent e) {}
            public void componentHidden(ComponentEvent e) {}
        });*/
    }

    public SizeManager(Component c) {
        this(c, LARGEUR_ECRAN, HAUTEUR_ECRAN);
    }

    public void addToSizeManager(Component c, int largeur, int hauteur) {
        ComponentList.add(new ComponentInformation(c, largeur, hauteur));
    }



    private static class ComponentInformation {
        public Component composant;
        public int largeur;
        public int hauteur;
        public int fontSize;
        /**
         * Rescence les informations liées au composant
         * @param c Le composant à dimensionner
         * @param largeur Largeur désirée dans le cas de référence
         * @param hauteur Hauteur désirée dans le cas de référence
         */
        private ComponentInformation(Component c, int largeur, int hauteur) {
            this.composant = c;
            this.largeur = largeur;
            this.hauteur = hauteur;
            this.fontSize = c.getFont().getSize();
        }
    }

    private class Convertisseur {

        /** valeur du paramètre de référence pour les conversions **/
        private int parametreReference;

        /** le paramètre secondaire permet de faire des conversions de ration fixe **/
        private int parametreSecondaire;

        /** ratio de conversion **/
        private double ratio = 1;

        /**
         * Crée un convertisseur d'entier qui fait des règles de 3
         * @param parametreReference Paramètre de référence pour les conversions
         */
        public Convertisseur(int parametreReference) {
            this.parametreReference = parametreReference;
            this.ratio = 1;
        }

        /**
         * Crée un convertisseur d'entier qui fait des règles de 3
         * @param parametreReference Paramètre de référence pour les conversions
         */
        public Convertisseur(int parametreReference, int parametreSecondaire) {
            this.parametreReference = parametreReference;
            this.parametreSecondaire = parametreSecondaire;
            this.ratio = (double)parametreSecondaire / (double)parametreReference;
        }


        /**
         * converti la valeur de base passée en argument en fonction de la valeur courante du paramètre par une règle de 3
         * @param valeurCourante Valeur du paramètre
         * @param valeurDeBase Valeur souhaitée dans le cas de référence
         * @return valeur entière convertie par la règle de 3
         */
        public int prop(int valeurCourante, int valeurDeBase) {
            float valeurTheorique = (float)valeurCourante / (float)parametreReference * (float)valeurDeBase;
            return Math.round(valeurTheorique);
        }


        /**
         * converti la valeur de base passée en argument en fonction de la valeur courante du paramètre par une règle de 3
         * @param valeurDeBase Valeur souhaitée dans le cas de référence
         * @return valeur entière convertie par la règle de 3
         */
        public int prop(int valeurDeBase) {
            double valeurTheorique = ratio * (double)valeurDeBase;
            return (int) Math.round(valeurTheorique);
        }

        /**
         * converti la valeur passée en argument selon le ratio définit à la création du convertisseur
         * @param valeurX Nouvelle valeur selon le premier axe
         * @return valeur entière résultat
         */
        public int propY(int valeurX) {
            double valeurTheorique = ratio * (double)valeurX;
            return (int) Math.round(valeurTheorique);
        }

        /**
         * converti la valeur passée en argument selon le ratio inverse définit à la création du convertisseur
         * @param valeurY Nouvelle valeur selon le second axe
         * @return valeur entière résultat
         */
        public int propX(int valeurY) {
            double valeurTheorique = (double)valeurY / ratio;
            return (int) Math.round(valeurTheorique);
        }
    }

}
