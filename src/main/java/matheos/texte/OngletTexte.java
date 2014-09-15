/**
 * «Copyright 2012,2013 François Billioud»
 *
 * This file is part of MathEOS.
 *
 * MathEOS is free software: you can redistribute it and/or modify under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * MathEOS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY.
 *
 * You should have received a copy of the GNU General Public License along with
 * MathEOS. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Additional Terms according section 7 of GNU/GPL V3 :
 *
 * These additional terms refer to the source code of matheos.
 *
 * According to GNU GPL v3, section 7 b) :
 * You should mention any contributor of the work as long as his/her contribution
 * is meaningful in a covered work. If you convey a source code using a part of the
 * source code of MathEOS, you should keep the original author in the resulting
 * source code. If you propagate a covered work with the same objectives as the
 * Program (help student to attend maths classes with an adapted software), you
 * should mention «Ludovic Faubourg», «Frédéric Marinoni» as author of the idea of
 * this software. In any case, if you propagate a covered work you have to mention
 * François Billioud and Guillaume Varoquaux as author of the realisation of MathEOS
 * software. The paternity of the authors have to appear in a legible, unobscured
 * manner, showing clearly their link to the covered work in any document,
 * web pages,... which describe the project or participate to the distribution of
 * the covered work.
 */
package matheos.texte;

import matheos.elements.ChangeModeListener;
import matheos.elements.Onglet.OngletCours;
import matheos.sauvegarde.Data;
import matheos.utils.objets.Blinking;
import matheos.sauvegarde.DataTP;
import matheos.sauvegarde.DataTexte;
import matheos.utils.boutons.Bouton;
import matheos.utils.texte.EditeurKit;
import matheos.texte.composants.JLabelTP;
import matheos.utils.managers.CursorManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public abstract class OngletTexte extends OngletCours {

    private static final float[] ZOOM = {1f, 1.7f, 3f};
    private int indexZoom = 0;
    Editeur editeur;
    JScrollPane scrollPane;
    protected Bouton creation;
    protected Blinking blinking;

    public OngletTexte() {
        editeur = new Editeur();
        editeur.addMouseListener(new ChangeModeListener(ChangeModeListener.COURS));
        scrollPane = new JScrollPane(editeur, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(100);
        scrollPane.setWheelScrollingEnabled(true);
        add(scrollPane, BorderLayout.CENTER);

        EditeurKit editeurKit = editeur.getEditeurKit();
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonBold());
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonItalic());
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonUnderline());
        barreOutils.addSeparateurOnLeft();

        barreOutils.addBoutonOnLeft(editeurKit.getBoutonLeftAlined());
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonCenterAlined());
        barreOutils.addSeparateurOnLeft();

//        barreOutils.addComponentOnLeft(editeurKit.getMenuTaille());
        barreOutils.addComponentOnLeft(editeurKit.getMenuCouleur());
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonTitle());
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonSubTitle());

        //on écoute les modifs de l'éditeur
        editeur.setFontSize(EditeurKit.TAILLES_PT[0]);
        editeur.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                if(evt.getPropertyName().equals(Editeur.TITLE_PROPERTY)) {//gère le changement de titre de chapitre
                    String newTitle = (String) evt.getNewValue();
                    String[] parts = newTitle.split(":");//HACK pour supprimer un éventuel préfix (chapitre i : xxx)
                    cahier.setTitre(cahier.getIndexCourant(), parts[parts.length-1]);
                } else if(evt.getPropertyName().equals(Editeur.FONT_SIZE_PROPERTY)) {
//                    editeur.setPreferredSize(OngletTexte.this.getSize());
//                    editeur.repaint();
                }
            }
        });
    }

    @Override
    public void setActionEnabled(int actionID, boolean b) {
    }
    
    @Override
    public void imprimer() {
        editeur.imprime();
    }

    @Override
    public void miseEnPage() {
        editeur.miseEnPage();
    }

    @Override
    public void apercu() {
        editeur.apercu();
    }

    @Override
    public void activeContenu(boolean b) {
        blinking.arreter();
        if(cahier.getIndexCourant()==-1) {
            blinking=new Blinking(creation);blinking.start();
            editeur.setEnabled(false);
        } else {
            this.setEnabled(b);
            editeur.activeContenu(b);
        }
    }

    /**
     * charge les donnés dans l'éditeur
     * @param dataTexte DataTexte contenant les données
     */
    protected void chargerEditeur(Data dataTexte) {
        editeur.charger(dataTexte);
    }

    @Override
    public void zoomP() {
        if (indexZoom < ZOOM.length - 1) {
            indexZoom++;
            updateFontSize();
        }
        ((HTMLEditorKit)editeur.getEditorKit()).setDefaultCursor(CursorManager.getCursor(indexZoom==ZOOM.length-1 ? CursorManager.TEXT_BIG_CURSOR : CursorManager.TEXT_MEDIUM_CURSOR));
    }

    @Override
    public void zoomM() {
        if (indexZoom > 0) {
            indexZoom--;
            updateFontSize();
        }
        ((HTMLEditorKit)editeur.getEditorKit()).setDefaultCursor(CursorManager.getCursor(indexZoom==0 ? CursorManager.TEXT_SMALL_CURSOR : CursorManager.TEXT_MEDIUM_CURSOR));
    }

    private void updateFontSize() {
        float size = EditeurKit.TAILLES_PT[0] * ZOOM[indexZoom];
        editeur.setFontSize((int)size);
    }
    
    public void export2Docx(File f) {
        editeur.export2Docx(f);
    }

    public boolean hasBeenModified() {return editeur.hasBeenModified();}
    public void setModified(boolean b) {editeur.setModified(b);}

    public void annuler() {
        editeur.annuler();
    }

    public void refaire() {
        editeur.refaire();
    }

    public boolean peutAnnuler() {
        return editeur.peutAnnuler();
    }

    public boolean peutRefaire() {
        return editeur.peutRefaire();
    }

    @Override
    public long insertion(long id, String nomTP, DataTP donnees, String image, int hauteur) {
        return id == 0 ? insererTP(image, donnees, nomTP, hauteur) : updateTP(id, nomTP, donnees, image);
    }
    
    /**
     * Méthode gérant l'insertion d'un TP dans la partie cours.
     *
     * @param image l'image du TP à insérer.
     * @param data les données du TP à insérer.
     * @param nomTP le nom du type de TP à insérer.
     * @param hauteur la taille initiale du TP
     * @return un long représentant le JLabelTP correspondant à l'onglet TP
     * inséré dans l'Editeur
     */
    private long insererTP(String im, DataTP data, String nomTP, int hauteur) {
        if (im != null) {
            JLabelTP tp = new JLabelTP(im, data, nomTP, hauteur);
            editeur.insererTP(tp);
            return tp.getId();
        }
        return 0;
    }

    /**
     * Méthode gérant l'insertion d'un TP dans la partie cours.
     *
     * @param image l'image du TP à insérer.
     * @param data les données du TP à insérer.
     * @param nomTP le nom du type de TP à insérer.
     * @param hauteur la taille initiale du TP
     * @return un long représentant le JLabelTP correspondant à l'onglet TP
     * inséré dans l'Editeur
     */
    private long insererTP(String im, DataTP data, String nomTP) {
        return insererTP(im, data, nomTP, 0);
    }
    
    /**
     * Méthode permettant de mettre à jour un tp à partir des nouvelles données
     * de l'onglet tp correspondant. Si le TP n'existe plus, on en crée un
     * nouveau.
     *
     * @param id l'id {@linkplain JLabelTP} à mettre à jour
     * @param nom le nouveau nom du TP
     * @param data la donnée Serializable caractérisant l'état du TP
     * @param imageTP l'image de la partie TP correspondant à la sauvegarde des
     * données
     * @return un long représentant l'id du JLabelTP mis à jour, ou un nouvel id
     * si l'ancien JLabelTP a été supprimé de l'Editeur par l'utilisateur
     */
    public long updateTP(long id, String nomTP, DataTP data, String imageTP) {
        JLabelTP oldTP = editeur.getTP(id);
        if (oldTP == null) {
            return insererTP(imageTP, data, nomTP);
        }
        editeur.updateTP(oldTP, data, imageTP);
        return id;
    }
    
    @Override
    public DataTexte getDonneesEditeur() {
        return editeur.getDonnees();
    }

}
