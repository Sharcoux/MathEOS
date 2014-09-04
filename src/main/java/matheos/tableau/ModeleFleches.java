package matheos.tableau;

import matheos.sauvegarde.DataTexte;
import static matheos.tableau.TableConstants.ORIENTATIONS;
import static matheos.tableau.TableConstants.TYPE_CASES;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.EventListenerList;

public class ModeleFleches extends LinkedList<DataFleche> implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    private final transient EventListenerList listenerList = new EventListenerList();

    public void addModeleFlechesListener(ModeleFlechesListener l) {
        listenerList.add(ModeleFlechesListener.class, l);
    }

    public void removeModeleFlechesListener(ModeleFlechesListener l) {
            listenerList.remove(ModeleFlechesListener.class, l);
    }

    protected void fireAddReponse(List<DataFleche> modeleFleches) {
        for (ModeleFlechesListener l : listenerList.getListeners(ModeleFlechesListener.class)) {
            l.modeleFlecheAdded(modeleFleches);
        }
    }

    protected void fireRemoveReponse(List<DataFleche> modeleFleches) {
        for (ModeleFlechesListener l : listenerList.getListeners(ModeleFlechesListener.class)) {
            l.modeleFlecheRemoved(modeleFleches);
        }
    }

    protected void fireChangedReponse(List<DataFleche> modeleFleches) {
        for (ModeleFlechesListener l : listenerList.getListeners(ModeleFlechesListener.class)) {
            l.modeleFlechesChanged(modeleFleches);
        }
    }

    /**
     * Permet d'ajouter un {@link DataFleche} à la liste. Si le
     * <code>ModeleFleche</code> existe déjà dans la liste, il n'est pas ajouté.
     *
     * @param modeleFleche le ModeleFleche a ajouter à la liste
     * @return true si l'ajout a bien eu lieu; false sinon
     */
    @Override
    public boolean add(DataFleche modeleFleche) {
        if (!this.contains(modeleFleche)) {
            super.add(modeleFleche);
            fireAddReponse(new ListeFleches(modeleFleche));
            return true;
        }
        return false;
    }
    
    private static class ListeFleches extends LinkedList<DataFleche> {
        private ListeFleches(DataFleche... fleches) {
            addAll(Arrays.asList(fleches));
        }
    }
    
    /**
     * Permet d'enlever de la liste le {@link DataFleche} passé en paramètre.
     *
     * @param modeleFleche le {@link DataFleche} a retirer de la liste
     */
    public void remove(DataFleche modeleFleche) {
        if(!contains(modeleFleche)) {return;}
        remove(modeleFleche);
        fireRemoveReponse(new ListeFleches(modeleFleche));
    }


    /**
     * Méthode permettant d'affecter un texte à une flèche de proportionnalité.
     * Si le
     * <code>ModeleFleche</code> contenait déjà un texte, celui-ci est remplacé
     * par le nouveau.
     *
     * @param cle la {@link Cle} permettant d'identifier
     * le <code>ModeleFleche</code> auquel affecter le texte
     * @param data la {@link DataTexte} représentant le
     * texte à affecter à cette flèche ou une <code>NullPointerException</code>
     * aucune flèche n'est associée à la clé passé en paramètre
     */
	public void setText(DataFleche cle, DataTexte data) {
		DataFleche fleche = cle;
		if (fleche == null) {
			throw new NullPointerException("Aucune flèche trouvée pour la clé : " + cle);
		}
		fleche.setData(data);
                fireChangedReponse(new ListeFleches(fleche));
	}

	/**
	 * Renvoie l'ensemble des flèches pour l'orientation donnée.
	 *
	 * @param orientation l'orientation des flèches
	 * @return Un ModeleFleches contenant une copie des flèches concernées
	 */
	public ModeleFleches getFleches(ORIENTATIONS orientation) {
		ModeleFleches liste = new ModeleFleches();
		for (DataFleche fleche : this) {
			if (fleche.getOrientation().equals(orientation)) {
				liste.add(fleche);
			}
		}
		return liste;
	}

	/**
	 * Renvoie l'ensemble des flèches pour un type donnée. Le type peut-être
	 * soit TYPE_CASES.ROW, soit TYPE_CASES.COLUMN. Si le type est
	 * TYPE_CASES.ROW, la méthode renvoir l'ensemble des flèches ayant
	 * l'<link>Orientation<link> EAST ou WEST
	 *
	 * @param typeCase
	 * @return
	 */
	private ModeleFleches getFlechesDeType(TYPE_CASES typeCase) {
		ModeleFleches liste = new ModeleFleches();
		for (DataFleche fleche : this) {
                    if(fleche.getOrientation().getTypeCases().equals(typeCase)) {
                        liste.add(fleche);
                    }
		}
		return liste;
	}

	/**
	 * Renvoie l'ensemble des flèches pour l'orientation donnée, qui concerne
	 * l'index passé en paramètre, c'est à dire les flèches qui commencent à cet
	 * index, ou qui arrivent à cet index.
	 *
	 * @param typeCases le type de la flèche, c'est à dire si c'est uen flèche
	 * en ligne ou en colonne
	 * @param index l'index de la ligne ou la colonne dont on veut connaître les
	 * flèches arrivant ou partant.
	 * @return Un ModeleFleches contenant une copie des flèches concernées
	 */
	public ModeleFleches getFlechesLieesAUneCase(TYPE_CASES typeCases, int index) {
            ModeleFleches liste = new ModeleFleches();
            for (DataFleche fleche : getFlechesDeType(typeCases)) {
                if (fleche.getIndexDepart() == index || fleche.getIndexArrivee() == index) {
                        liste.add(fleche);
                }
            }
            return liste;
	}

	/**
	 * Méthode permettant d'ajouter d'un coup plusieurs {@link DataFleche} à
	 * ce ModeleFleches.
	 *
	 * @param modeleFleches le <code>ModeleFleches</code> contentant les
	 * <code>modeleFleche</code> à ajouter
	 */
	public void setFleches(ModeleFleches modeleFleches) {
            List<DataFleche> flechesAdded = new ArrayList<>();
            for (DataFleche dataFleche : modeleFleches) {
                DataFleche fleche = dataFleche;
                if (!this.contains(fleche)) {
                    this.add(fleche);
                    flechesAdded.add(fleche);
                    }
            }
            fireAddReponse(flechesAdded);
	}

	/**
	 * Permet d'adapter la position des flèches de proportionnalité lorsqu'une
	 * ligne ou une colonne est ajoutée au tableau. Les flèches peuvent ainsi se
	 * déplacer, s'agrandir ou se réduire en fonction de leur position par
	 * rapport aux cases ajoutées.
	 *
	 * @param typeCase le {@link TYPE_CASES} correspondant au type d'ajout
	 * (ligne ou colonne)
	 * @param position la position à laquelle ont été ajoutées les cases
	 */
	public void ajouterCases(TYPE_CASES typeCase, int position) {
            ModeleFleches fleches = getFlechesDeType(typeCase);
		for (DataFleche fleche : fleches) {
                    if (fleche.getIndexDepart() >= position) {
                        fleche.setIndexDepart(fleche.getIndexDepart() + 1);
                    }
                    if (fleche.getIndexArrivee() >= position) {
                        fleche.setIndexArrivee(fleche.getIndexArrivee() + 1);
                    }
		}
	}

	/**
	 * Permet d'adapter la position des flèches de proportionnalité lorsqu'une
	 * ligne ou une colonne est supprimée du tableau. Les flèches peuvent ainsi
	 * se déplacer, s'agrandir ou se réduire en fonction de leur position par
	 * rapport aux cases supprimées.
	 *
	 * @param typeCase le {@link TYPE_CASES} correspondant au type de
	 * suppression (ligne ou colonne)
	 * @param position la position à laquelle ont été supprimées les cases
	 */
	public ModeleFleches supprimerCases(TYPE_CASES typeCase, int position) {
            ModeleFleches fleches = getFlechesDeType(typeCase);
            ModeleFleches flechesRemoved = new ModeleFleches();
            for (DataFleche fleche : fleches) {
                if (fleche.getIndexDepart() == position || fleche.getIndexArrivee() == position) {
                    flechesRemoved.add(fleche);
                    this.remove(fleche);
                } else {
                    if (fleche.getIndexDepart() > position) {
                        fleche.setIndexDepart(fleche.getIndexDepart() - 1);
                    }
                    if (fleche.getIndexArrivee() > position) {
                        fleche.setIndexArrivee(fleche.getIndexArrivee() - 1);
                    }
                }
            }
            fireRemoveReponse(flechesRemoved);
            return flechesRemoved;
	}

	@Override
	public ModeleFleches clone() {
            ModeleFleches copy = new ModeleFleches();
            for (DataFleche modeleFleche : this) {
                copy.add((DataFleche) modeleFleche.clone());
            }
            return copy;
	}

}
