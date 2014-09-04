/** «Copyright 2013 François Billioud»
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


package matheos.texte.composants;

import matheos.IHM;
import matheos.IHM.ONGLET_TP;
import matheos.elements.Onglet;
import matheos.json.Json;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataTP;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class JLabelTP extends JLabelImage implements ComposantTexte {

    /** Constante permettant d'identifier un JLabelTP **/
    public static final String JLABEL_TP = "labelTPComponent";
    
    private DataTP dataTP;
    private String nomTP;

//    public JLabelTP(BufferedImage image, DataTP data, String nomTP, Color couleur, int hauteurInitiale, long id) {
//        this(image, data, nomTP, couleur, hauteurInitiale);
//        setId(id);
//    }

    public JLabelTP(BufferedImage image, DataTP data, String nomTP, int hauteurInitiale) {
        super(image, hauteurInitiale);
        setDataTP(data);
        setNomTP(nomTP);
        addMouseListener(new TPDoubleClicListener());//écoute les double-clic sur le composant
    }

    public JLabelTP(BufferedImage image, DataTP data, String nomTP) {
        this(image, data, nomTP, 0);
    }

//    public JLabelTP(AttributeSet attributes) {
//        super(attributes);
//        setDataTP((DataTP) attributes.getAttribute(DATA_TP));
//        setNomTP((String) attributes.getAttribute(NOM_TP));
//    }

    public DataTP getDataTP() {
        return dataTP;
    }

    /** attribut les données du TP représentées par ce JLabel. les données reçoivent l'id du JLabel pour assurer la cohérence **/
    public final void setDataTP(DataTP data) {
        if(data==null) {return;}
        data.setId(getId());
        this.dataTP = data;
    }
    
    /** Attribue l'id du JLabel. Le dataTP est modifié en conséquence pour assurer la cohérence **/
    @Override
    public void setId(long id) {
        super.setId(id);
        if(dataTP!=null) {
            getDataTP().setId(id);
        }
    }

    public String getNomTP() {
        return nomTP;
    }

    public final void setNomTP(String nomTP) {
        this.nomTP = nomTP;
    }

    /**
     * Méthode permettant de changer les paramètres d'un JLabelTP.
     * @param data les nouvelles données Serializable
     * @param tp la nouvelle image du JLabelTP
     */
    public void setParametres(DataTP data, BufferedImage tp) {
//        setNomTP(nomTP);
        setDataTP(data);
       	changeImageInitiale(tp);
    }

/*    @Override
    public AttributeSet imageToModel() {
        MutableAttributeSet attributes = new SimpleAttributeSet();
        attributes.addAttributes(super.imageToModel());
        attributes.addAttribute(DATA_TP, dataTP);
        attributes.addAttribute(NOM_TP, nomTP);
        attributes.addAttribute(JLabelTools.TYPE_LABEL, JLABEL_TP);
        return attributes;
    }
*/
/*    static JLabelTP modelToImage(AttributeSet attributes) {
        BufferedImage im = ImageTools.getImageFromArray((byte[]) attributes.getAttribute(IMAGE_INITIALE));
        Color selectionColor = (Color) attributes.getAttribute(SELECTION_COLOR);
        DataTP dataCopie = (DataTP) attributes.getAttribute(DATA_TP);
        String nomTPCopie = (String) attributes.getAttribute(NOM_TP);
        int hauteurInitiale = (Integer) attributes.getAttribute(HAUTEUR_IMAGE);
        return new JLabelTP(im, dataCopie, nomTPCopie, selectionColor, hauteurInitiale);
    }
*/
    @Override
    public JLabelTP copyImage() {
        return new JLabelTP(imageInitiale, dataTP, nomTP, getIcon().getIconHeight());
    }

    @Override
    public String getHTMLRepresentation() {
        Element img = Jsoup.parse(super.getHTMLRepresentation()).select("img").first();
//        try {
            img.attr("title", getNomTP())
//                    .attr("data-tp", JsonWriter.objectToJson(dataTP))//insert les données directement dans le html (pas recommandé)
                    ;
//        } catch (IOException ex) {
//            Logger.getLogger(JLabelTP.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return img.outerHtml();
    }

    public static JLabelTP creerJLabelTPFromHTML(String html) {//si l'image est stockée en externe
        return creerJLabelTPFromHTML(html, null);
    }
    public static JLabelTP creerJLabelTPFromHTML(String html, BufferedImage image) {//si on a choisit d'insérer les dataTP dans le html
        return creerJLabelTPFromHTML(html, null, image);
    }
    public static JLabelTP creerJLabelTPFromHTML(String html, Data data, BufferedImage image) {
        DataTP dataTP = (DataTP) data;
        Element img = Jsoup.parse(html).select("img").first();
        String nom = img.attr("title");
        String id = img.attr("id");
        if(dataTP==null) {
            try {//on essaye de lire les données directement depuis le HTML
                dataTP = (DataTP) Json.toJava(img.attr("data-tp"), DataTP.class);
            } catch (IOException ex) {
                Logger.getLogger(JLabelTP.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("impossible de lire le tp depuis le html");
//                return null;
            }
        }
//        data.id = label.getId();//on assure la cohérence des données. Le dataTP doit toujours prendre son id du JLabel
        JLabelTP tp = new JLabelTP(image, dataTP, nom, image.getHeight());
        if(!id.isEmpty()) {
            try {
                tp.setId(Long.parseLong(img.attr("id")));
            } catch (NumberFormatException e) {System.out.println("id du JLabelTP non trouvé : "+img.outerHtml());}
        }
        return tp;
    }
    
    private final class TPDoubleClicListener extends MouseAdapter {
        private TPDoubleClicListener() { super(); }
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (e.getComponent() instanceof JLabelTP) {
                    if (e.getClickCount() == 2) {
                        editerTP();
                    } else {
//                        super.mouseClicked(e);//le listener est déjà ajouté
                    }
                }
            } else {
//                super.mouseClicked(e);//le listener est déjà ajouté
            }
        }
    }
    
    /** Permet de charger un TP dans l'onglet approprié afin de le modifier. **/
    public void editerTP() {
        if(getDataTP()==null) {
            System.out.println("donnéesTP null : "+getNomTP()+" : "+getId()); return;
        }
        
        // On deselectionne le TP pour eviter qu'il soit supprimé a la prochaine insertion
        JTextComponent text = (JTextComponent) SwingUtilities.getAncestorOfClass(JTextComponent.class, this);
        if(text!=null) {text.setSelectionStart(text.getSelectionEnd());}
        
        //On charge le TP dans le bon onglet
        Onglet.OngletTP onglet = null;
        for(ONGLET_TP o : ONGLET_TP.values()) {
            if(getNomTP().equals(o.getNom())) {
                onglet = o.getInstance();
            }
        }
        if(onglet == null) {return; }
        if (onglet.ecraserTP()) {
            IHM.setOngletActif(onglet);
            onglet.charger(/*getId(), */getDataTP());
        }
    }


    public static final class TPEdit extends AbstractUndoableEdit {
        private final JLabelTP tp;
        private final DataTP oldData;
        private final DataTP newData;
        private final BufferedImage oldImage;
        private final BufferedImage newImage;
        
        public TPEdit(JLabelTP tp, DataTP newData, BufferedImage newImage) {
            this.tp = tp;
            this.oldData = tp.getDataTP();
            this.newData = newData;
            this.oldImage = tp.getImageInitiale();
            this.newImage = newImage;
        }
        
        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            tp.setParametres(newData, newImage);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            tp.setParametres(oldData, oldImage);
        }
    }

}
