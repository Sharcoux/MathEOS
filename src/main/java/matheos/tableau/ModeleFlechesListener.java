/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package matheos.tableau;

import java.util.EventListener;
import java.util.List;

/**
 * Classe gérant les interactions entre le modèle des flèches de proportionnalité
 * autour du tableau ({@link ModeleFleches}) et un écouteur. 
 * L'écouteur est prévenu lorsqu'une flèche de proportionnalité est ajouté ou
 * supprimé du modèle, ou lorsque le texte qu'elle contient a changé.
 * 
 * @author Guillaume
 */
public interface ModeleFlechesListener extends EventListener {

    /**
     * Méthode appelée lorsqu'une nouvelle flèche de proportionnalité a été
     * ajoutée au tableau.
     *
     * @param modeleFleches la liste des {@code ModeleFleche} ajoutés
     */
    void modeleFlecheAdded(List<DataFleche> modeleFleches);

    /**
     * Méthode appelée lorsqu'une flèche de proportionnalité a été retirée du
     * tableau.
     *
     * @param modeleFleches la liste des {@code ModeleFleche} retirés
     */
    void modeleFlecheRemoved(List<DataFleche> modeleFleches);

    /**
     * Méthode appelée lorsque le texte contenu dans un champ d'une des flèches
     * de proportionnalité présente autour du tableau a changé.
     *
     * @param modeleFleches la liste des {@code ModeleFleche} dont le contenu du
     * champ de texte a changé
     */
    void modeleFlechesChanged(List<DataFleche> modeleFleches);
}
