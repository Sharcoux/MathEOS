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

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import matheos.Configuration;
import matheos.IHM;
import matheos.IHM.ONGLET_TP;
import matheos.elements.Onglet;
import matheos.json.Json;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataTP;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class JLabelTP extends JSVGCanvas implements ComposantTexte {

    /** Constante permettant d'identifier un JLabelTP **/
    public static final String JLABEL_TP = "labelTPComponent";
    
    private DataTP dataTP;
    private String nomTP;
    private String svg;
    private long id = System.currentTimeMillis();// L'id unique du JLabelImage permettant de l'identifier

//    public JLabelTP(BufferedImage image, DataTP data, String nomTP, Color couleur, int hauteurInitiale, long id) {
//        this(image, data, nomTP, couleur, hauteurInitiale);
//        setId(id);
//    }

    public JLabelTP(String image, DataTP data, String nomTP, int hauteurInitiale) {
        this.svg = image;
        
        // parse String into DOM Document
        StringReader reader = new StringReader(image);
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        try {
            SVGDocument doc = f.createSVGDocument(Configuration.getURLDossierImagesTemp(),reader);
            setSVGDocument(doc);
        } catch (IOException ex) {
            Logger.getLogger(JLabelTP.class.getName()).log(Level.SEVERE, null, ex);
        }

        setDataTP(data);
        setNomTP(nomTP);
        addMouseListener(new TPDoubleClicListener());//écoute les double-clic sur le composant
    }

    public JLabelTP(String image, DataTP data, String nomTP) {
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
        this.id = id;
        if(dataTP!=null) {
            getDataTP().setId(id);
        }
    }

    @Override
    public long getId() {
        return id;
    }
    
    public String getNomTP() {
        return nomTP;
    }

    public final void setNomTP(String nomTP) {
        this.nomTP = nomTP;
    }
    
    public String getSVG() {return svg;}
    public void setSVG(String svg) {this.svg = svg;}

    /**
     * Méthode permettant de changer les paramètres d'un JLabelTP.
     * @param data les nouvelles données Serializable
     * @param tp la nouvelle image du JLabelTP
     */
    public void setParametres(DataTP data, String tp) {
//        setNomTP(nomTP);
        setDataTP(data);
        svg = tp;
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
    public JLabelTP copy() {
        return new JLabelTP(svg, dataTP, nomTP, getHeight());
    }

    @Override
    public String getHTMLRepresentation() {
        Element svgElement = Jsoup.parse(this.svg).select("svg").first();
        svgElement.attr("id", id + "").attr("width",getWidth()+"").attr("height",getHeight()+"");
        svgElement.attr("title", getNomTP());
        return svgElement.outerHtml();
    }

    public static JLabelTP creerJLabelTPFromHTML(String svg) {//si l'image est stockée en externe
        return creerJLabelTPFromHTML(svg, null);
    }
    public static JLabelTP creerJLabelTPFromHTML(String svg, Data data) {
        DataTP dataTP = (DataTP) data;
        Element svgElement = Jsoup.parse(svg).select("svg").first();
        String nom = svgElement.attr("title");
        String id = svgElement.attr("id");
        String heightString = svgElement.attr("height");
        int height = heightString.isEmpty() ? 0 : Integer.parseInt(heightString);
        if(dataTP==null) {
            try {//on essaye de lire les données directement depuis le HTML
                dataTP = (DataTP) Json.toJava(svgElement.attr("data-tp"), DataTP.class);
            } catch (IOException ex) {
                Logger.getLogger(JLabelTP.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("impossible de lire le tp depuis le html");
//                return null;
            }
        }
        JLabelTP tp = new JLabelTP(svg, dataTP, nom, height);
        if(!id.isEmpty()) {
            try {
                tp.setId(Long.parseLong(svgElement.attr("id")));
            } catch (NumberFormatException e) {System.out.println("id du JLabelTP non trouvé : "+svgElement.outerHtml());}
        }
        return tp;
    }
    
    private Color couleurSelection;
    public void setCouleurSelection(Color couleurSelection) {
        this.couleurSelection = couleurSelection;
    }

    @Override
    public void selectionner() {
        this.setBackground(couleurSelection);
    }

    @Override
    public void deselectionner() {
        this.setBackground(Color.WHITE);
    }

    public void setSize(int hauteur) {
        this.setSize(getWidth()/getHeight()*hauteur, hauteur);
    }    
    @Override
    public void setFontSize(float size) {
        setSize(Math.round(size/getFont().getSize2D()*getHeight()));
        setFont(getFont().deriveFont(size));
    }

    @Override
    public float getFontSize() {
        return getFont().getSize2D();
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
        private final String oldImage;
        private final String newImage;
        
        public TPEdit(JLabelTP tp, DataTP newData, String newImage) {
            this.tp = tp;
            this.oldData = tp.getDataTP();
            this.newData = newData;
            this.oldImage = tp.getSVG();
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
