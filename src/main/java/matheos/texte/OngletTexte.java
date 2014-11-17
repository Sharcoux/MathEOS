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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTMLEditorKit;
import matheos.elements.Onglet.OngletCours;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataTP;
import matheos.sauvegarde.DataTexte;
import matheos.texte.composants.JLabelNote;
import matheos.texte.composants.JLabelTP;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.Bouton;
import matheos.utils.dialogue.DialogueComplet;
import matheos.utils.dialogue.DialogueEvent;
import matheos.utils.dialogue.DialogueListener;
import matheos.utils.interfaces.ComponentInsertionListener;
import matheos.utils.managers.CursorManager;
import matheos.utils.managers.PermissionManager;
import matheos.utils.objets.Blinking;
import matheos.utils.texte.EditeurKit;
import matheos.utils.texte.JMathTextPane;

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
    private Blinking blinking;
    private final ActionCorrection actionCorriger = new ActionCorrection();

    public OngletTexte() {
        editeur = new Editeur();
        scrollPane = new JScrollPane(editeur, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(100);
        scrollPane.setWheelScrollingEnabled(true);
        add(scrollPane, BorderLayout.CENTER);

        EditeurKit editeurKit = editeur.getEditeurKit();
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonBold());
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonItalic());
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonUnderline());
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonStrike());
        barreOutils.addSeparateurOnLeft();

        barreOutils.addBoutonOnLeft(editeurKit.getBoutonLeftAlined());
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonCenterAlined());
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonRightAlined());
        barreOutils.addSeparateurOnLeft();

//        barreOutils.addComponentOnLeft(editeurKit.getMenuTaille());
        barreOutils.addComponentOnLeft(editeurKit.getMenuCouleur());
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonTitle());
        barreOutils.addBoutonOnLeft(editeurKit.getBoutonSubTitle());
        
        barreOutils.addSeparateurOnRight();
        barreOutils.addBoutonOnLeft(actionCorriger);
        barreOutils.addComponentOnRight(getBoutonInsertionNote());
        
        //boutons à afficher uniquement en mode correction
        editeur.getEditeurKit().getBoutonStrike().setVisible(false);
        getBoutonInsertionNote().setVisible(false);
        editeur.getEditeurKit().getBoutonRightAlined().setVisible(false);
        
        //on écoute les modifs de l'éditeur
        editeur.setFontSize(EditeurKit.TAILLES_PT[0]);
        editeur.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                switch (evt.getPropertyName()) {
                    case Editeur.TITLE_PROPERTY:
                        //gère le changement de titre de chapitre
                        String newTitle = (String) evt.getNewValue();
                        String[] parts = newTitle.split(":");//HACK pour supprimer un éventuel préfix (chapitre i : xxx)
                        cahier.setTitre(cahier.getIndexCourant(), parts[parts.length-1].trim());
                        break;
                    case Editeur.FONT_SIZE_PROPERTY:
                        break;
                }
            }
        });
        editeur.addMouseListener(getChangeModeListener());
        //ajoute un changeModeListener sur les composants de l'editeur
        editeur.addComponentInsertionListener(new ComponentInsertionListener() {
            @Override
            public void componentInserted(Component c) {
                c.addMouseListener(OngletTexte.this.getChangeModeListener());
                fireComponentInsertion(c);
            }
            @Override
            public void componentRemoved(Component c) {
                c.removeMouseListener(OngletTexte.this.getChangeModeListener());
                fireComponentRemoval(c);
            }
        });
    }

    @Override
    public void setActionEnabled(PermissionManager.ACTION actionID, boolean b) {
    }
    
    @Override
    public JLabelTP getTP(long id) {
        return editeur.getTP(id);
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

    /**
     * Méthode gérant la remise en couleur des Composants lorsque l'éditeur
     * redevient actif.
     */
    @Override
    public void activeContenu(boolean b) {
        this.setEnabled(b);
        editeur.setEnabled(b);
        if(b && editeur.isShowing()) {editeur.requestFocusInWindow();}
    }

    @Override
    protected void setCahierViergeState(boolean b) {
        if(isNouveauCahier()==b) {return;}
        super.setCahierViergeState(b);
        editeur.getEditeurKit().getBoutonBold().setEnabled(!b);
        editeur.getEditeurKit().getBoutonItalic().setEnabled(!b);
        editeur.getEditeurKit().getBoutonUnderline().setEnabled(!b);
        editeur.getEditeurKit().getBoutonStrike().setEnabled(!b);
        editeur.getEditeurKit().getBoutonCenterAlined().setEnabled(!b);
        editeur.getEditeurKit().getBoutonLeftAlined().setEnabled(!b);
        editeur.getEditeurKit().getBoutonRightAlined().setEnabled(!b);
        editeur.getEditeurKit().getMenuCouleur().setEnabled(!b);
        editeur.getEditeurKit().getBoutonTitle().setEnabled(!b);
        editeur.getEditeurKit().getBoutonSubTitle().setEnabled(!b);
        getBoutonInsertionNote().setEnabled(!b);
        actionCorriger.setEnabled(!b);
        
        //on gère l'état du bouton de création
//        creation.setEnabled(true);//On active le bouton de création uniquement
        if(b) {
            blinking = new Blinking(creation);blinking.start();
        } else {
            blinking.arreter();
            creation.setBorder(null);
        }
    }
        
    /**
     * charge les donnés dans l'éditeur
     * @param dataTexte DataTexte contenant les données
     */
    @Override
    protected void chargerEditeur(Data dataTexte) {
        editeur.charger(dataTexte);
        setModeCorrectionEnabled(false);
    }
    
    @Override
    protected void chargerCahierVierge() {
        editeur.charger(new DataTexte(""));
    }

    @Override
    public void zoomP() {
        if (indexZoom < ZOOM.length - 1) {
            indexZoom++;
            updateFontSize();
        }
        editeur.getHTMLEditorKit().setDefaultCursor(CursorManager.getCursor(indexZoom==ZOOM.length-1 ? CursorManager.TEXT_BIG_CURSOR : CursorManager.TEXT_MEDIUM_CURSOR));
    }

    @Override
    public void zoomM() {
        if (indexZoom > 0) {
            indexZoom--;
            updateFontSize();
        }
        editeur.getHTMLEditorKit().setDefaultCursor(CursorManager.getCursor(indexZoom==0 ? CursorManager.TEXT_SMALL_CURSOR : CursorManager.TEXT_MEDIUM_CURSOR));
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
    public long insertion(long id, String nomTP, DataTP donnees, String image, int largeur, int hauteur) {
        return id == 0 ? insererTP(image, donnees, nomTP, largeur, hauteur) : updateTP(id, nomTP, donnees, image, largeur, hauteur);
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
    private long insererTP(String im, DataTP data, String nomTP, int largeur, int hauteur) {
        if (im != null) {
            JLabelTP tp = new JLabelTP(im, data, nomTP, largeur, hauteur);
            editeur.insererTP(tp);
            return tp.getId();
        }
        return 0;
    }
    
    /**
     * Méthode gérant l'insertion d'une note ou d'un barème dans la partie cours.
     *
     * @param note le JLabelNote à insérer
     * @return un long représentant le JLabelNote
     */
    private long insererNote(JLabelNote note) {
        editeur.insererNote(note);
        return note.getId();
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
    public long updateTP(long id, String nomTP, DataTP data, String imageTP, int largeur, int hauteur) {
        JLabelTP oldTP = editeur.getTP(id);
        if (oldTP == null) {
            return insererTP(imageTP, data, nomTP, largeur, hauteur);
        }
        editeur.updateTP(oldTP, data, imageTP);
        return id;
    }
    
    @Override
    public DataTexte getDonneesEditeur() {
        return editeur.getDonnees();
    }
    
    private class ActionCorrection extends ActionComplete.Toggle {
        private ActionCorrection() {super("text correct", false);}
        @Override
        public void actionPerformed(ActionEvent e) {
            setModeCorrectionEnabled(isSelected());
        }
    }
    
    private boolean correctionEnabled = false;
    private final CaretListener strikeStyleListener = new StrikeStyleUpdater();
    private Bouton boutonInsertionNote;
    private Bouton getBoutonInsertionNote() {return boutonInsertionNote==null ? boutonInsertionNote = new Bouton(new ActionInsertionNote()) : boutonInsertionNote;}
    protected void setModeCorrectionEnabled(boolean b) {//Active le mode où l'élève ou l'enseignant peut corriger une copie
        if(b==correctionEnabled) {return;}
        correctionEnabled = b;
        actionCorriger.setSelected(b);//Assure la cohérence entre correctionEnabled et actionCorriger
        
        //boutons à afficher en mode correction
        editeur.getEditeurKit().getBoutonStrike().setVisible(b);
        getBoutonInsertionNote().setVisible(b);
        editeur.getEditeurKit().getBoutonRightAlined().setVisible(b);
        
        //boutons à afficher en mode normal
        creation.setVisible(!b);
        editeur.getEditeurKit().getBoutonSubTitle().setVisible(!b);
        editeur.getEditeurKit().getBoutonTitle().setVisible(!b);
        
        //dispositions spéciales
        editeur.setStyleUpdatingEnabled(!b);//le caret doit garder sa couleur en toutes circonstances en mode correction
        editeur.setCaretPosition(editeur.getSelectionStart());//Changer de mode avec une sélection en cours peut amener des confusions
        if(correctionEnabled) {
            editeur.getEditeurKit().getMenuCouleur().setSelectedCouleur(EditeurKit.COULEURS[3]);
            editeur.addCaretListener(strikeStyleListener);
        } else {
            editeur.removeCaretListener(strikeStyleListener);
            editeur.getEditeurKit().getMenuCouleur().setSelectedCouleur(EditeurKit.COULEURS[0]);
        }
        
        getBarreOutils().revalidate();
        getBarreOutils().repaint();
    }

    
    /** CaretListener qui met à jour le style pour qu'il corresponde au texte où se situe le caret **/
    private class StrikeStyleUpdater implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            int referentCharacter = e.getDot();
            try {
                if(referentCharacter>0 && !editeur.getText(referentCharacter-1, 1).equals("\n")) {referentCharacter--;}
            } catch (BadLocationException ex) {
                Logger.getLogger(JMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
            }
            AttributeSet ast = editeur.getHTMLdoc().getCharacterElement(referentCharacter).getAttributes();
            Object textDecorationValue = ast.getAttribute(CSS.Attribute.TEXT_DECORATION);
            editeur.getEditeurKit().getBoutonStrike().setSelected(textDecorationValue!=null && textDecorationValue.toString().equals("line-through"));
        }
    }
    
    private class ActionInsertionNote extends ActionComplete {
        private ActionInsertionNote() { super("text insert mark"); }
        @Override
        public void actionPerformed(ActionEvent e) {
            DialogueComplet d = new DialogueComplet("dialog mark scale");
            d.addDialogueListener(new DialogueListener() {
                @Override
                public void dialoguePerformed(DialogueEvent event) {
                    if(!event.isConfirmButtonPressed()) {return;}
                    JLabelNote note = new JLabelNote(event.getInputString("numerator"), event.getInputString("denominator"), 50, 50);
                    try {
                        editeur.getHTMLdoc().insertString(editeur.getCaretPosition(), "\n", null);
                        new HTMLEditorKit.AlignmentAction("align right", StyleConstants.ALIGN_RIGHT).actionPerformed(new ActionEvent(editeur, ActionEvent.ACTION_PERFORMED, "align right"));
                        insererNote(note);
                        editeur.getHTMLdoc().insertString(editeur.getCaretPosition(), "\n", null);
                        new HTMLEditorKit.AlignmentAction("align left", StyleConstants.ALIGN_LEFT).actionPerformed(new ActionEvent(editeur, ActionEvent.ACTION_PERFORMED, "align left"));
                    } catch (BadLocationException ex) {
                        Logger.getLogger(OngletTexte.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        }
    }
    
}
