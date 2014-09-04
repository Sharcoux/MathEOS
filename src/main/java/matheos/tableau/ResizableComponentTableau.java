/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package matheos.tableau;

/**
 *
 * @author Guillaume
 */
public interface ResizableComponentTableau {

    /**
     * Renvoie la largeur normale d'un composant, c'est à dire sa largeur sans
     * l'application d'un coefficient de proportionnalité.
     *
     * @return
     */
    int getLargeurNormale();

    /**
     * Renvoie la hauteur normale d'un composant, c'est à dire sa hauteur sans
     * l'application d'un coefficient de proportionnalité.
     *
     * @return
     */
    int getHauteurNormale();
}
