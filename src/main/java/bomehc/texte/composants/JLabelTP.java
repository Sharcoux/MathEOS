/** «Copyright 2013 François Billioud»
 *
 * This file is part of Bomehc.
 *
 * Bomehc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bomehc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Bomehc. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Additional Terms according section 7 of GNU/GPL V3 :
 *
 * These additional terms refer to the source code of bomehc.
 *
 * According to GNU GPL v3, section 7 b) :
 * You should mention any contributor of the work as long as his/her contribution
 * is meaningful in a covered work. If you convey a source code using a part of the
 * source code of Bomehc, you should keep the original author in the resulting
 * source code. If you propagate a covered work with the same objectives as the
 * Program (help student to attend maths classes with an adapted software), you
 * should mention «Ludovic Faubourg», «Frédéric Marinoni» as author of the idea of
 * this software. In any case, if you propagate a covered work you have to mention
 * François Billioud and Guillaume Varoquaux as author of the realisation of Bomehc
 * software. The paternity of the authors have to appear in a legible, unobscured
 * manner, showing clearly their link to the covered work in any document,
 * web pages,... which describe the project or participate to the distribution of
 * the covered work.
 */


package bomehc.texte.composants;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import bomehc.IHM;
import bomehc.IHM.ONGLET_TP;
import bomehc.elements.Onglet;
import bomehc.json.Json;
import bomehc.sauvegarde.Data;
import bomehc.sauvegarde.DataTP;
import bomehc.texte.Editeur;
import bomehc.utils.managers.ColorManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class JLabelTP extends SVGComponent implements ComposantTexte.Image {

    /** Constante permettant d'identifier un JLabelTP **/
    public static final String JLABEL_TP = "labelTPComponent";
    
    private DataTP dataTP;
    private String nomTP;
    
    private boolean stroken = false;
    private Color strikeColor = Color.BLACK;
    public void setStroken(boolean b) {
        if(stroken==b) {return;}
        stroken=b;
        
        if(stroken) {
            //Ajoute la ligne au svg
            String strikeLine = "<line id=\"strike-line\" x1=\"0\" x2=\""+getSVG().attr("width")+"\" style=\"stroke:"+ColorManager.getRGBHexa(getStrikeColor())+";stroke-width:5;\" y1=\""+getSVG().attr("height")+"\" y2=\"0\" />";
            getSVG().append(strikeLine);
        } else {
            //supprime la ligne du svg
            getSVG().select("#strike-line").remove();
        }
        
        //recharge le svg
        updateSVG();
    }
    public boolean isStroken() {return stroken;}
    public void setStrikeColor(Color c) {strikeColor = c;}
    public Color getStrikeColor() {return strikeColor;}
    
    
    public JLabelTP(String image, DataTP data, String nomTP, int largeur, int hauteur) {
        super(image, largeur, hauteur);
        setDataTP(data);
        setNomTP(nomTP);
    }
    
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
     * @param svg la nouvelle image du JLabelTP
     */
    public void setParametres(DataTP data, String svg) {
//        setNomTP(nomTP);
        setDataTP(data);
        setSVGString(svg);
    }

    @Override
    public JLabelTP copy() {
        return creerJLabelTPFromHTML(getHTMLRepresentation(SVG_RENDERING.SVG, true), getDataTP());
    }

    @Override
    public String getHTMLRepresentation(SVG_RENDERING svgAllowed, boolean mathMLAllowed) {
        getSVG().attr("id", getId() + "").attr("width",getWidth()+"").attr("height",getHeight()+"");
        getSVG().attr("title", getNomTP());
        return super.getHTMLRepresentation(svgAllowed, mathMLAllowed);
    }

    public static JLabelTP creerJLabelTPFromHTML(String svg) {//si l'image est stockée en externe
        return creerJLabelTPFromHTML(svg, null);
    }
    public static JLabelTP creerJLabelTPFromHTML(String svg, Data data) {
        DataTP dataTP = (DataTP) data;
        Document d = Jsoup.parse(svg);d.outputSettings(new Document.OutputSettings().prettyPrint(false));
        Element svgElement = d.select("svg").first();
        String nom = svgElement.attr("title");
        String id = svgElement.attr("id");
        String widthString = svgElement.attr("width");
        String heightString = svgElement.attr("height");
        int width = widthString.isEmpty() ? 0 : Integer.parseInt(widthString);
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
        JLabelTP tp = new JLabelTP(svg, dataTP, nom, width, height);
        if(!id.isEmpty()) {
            try {
                tp.setId(Long.parseLong(svgElement.attr("id")));
            } catch (NumberFormatException e) {System.out.println("id du JLabelTP non trouvé : "+svgElement.outerHtml());}
        }
        return tp;
    }

    public static class TPListener extends MouseAdapter {
        private final Editeur editeur;
        public TPListener(Editeur editeur) { this.editeur = editeur; }
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getComponent() instanceof JLabelTP) {
                JLabelTP tp = (JLabelTP)e.getComponent();
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 2) {
                        editerTP(tp);
                    } else {
//                        Editeur editeur = (Editeur) SwingUtilities.getAncestorOfClass(Editeur.class, JLabelTP.this);
                        javax.swing.text.Element element = editeur.getHTMLdoc().getElement(Editeur.getSpanId(tp.getId()));
                        editeur.select(element.getStartOffset(),element.getEndOffset());
                    }
                } else {
                    if(!editeur.isEditable()) {return;}
                    JDialog dialogueImageTaille = new ComposantTexte.Image.ImageSizeEditor(tp);
                }
            }
        }
//        @Override
//        public void focusGained(FocusEvent e) {}
//        @Override
//        public void focusLost(FocusEvent e) {e.getComponent().setSelected(false);}
        /** Permet de charger un TP dans l'onglet approprié afin de le modifier. **/
        public void editerTP(JLabelTP tp) {
            if(tp.getDataTP()==null) {
                System.out.println("donnéesTP null : "+tp.getNomTP()+" : "+tp.getId()); return;
            }

            // On deselectionne le TP pour eviter qu'il soit supprimé a la prochaine insertion
            JTextComponent text = (JTextComponent) SwingUtilities.getAncestorOfClass(JTextComponent.class, tp);
            if(text!=null) {text.setSelectionStart(text.getSelectionEnd());}

            //On charge le TP dans le bon onglet
            Onglet.OngletTP onglet = ONGLET_TP.getInstance(tp.getNomTP());
            if(onglet == null) {return; }
            if(onglet.ecraserTP()) {
                IHM.setOngletActif(onglet);
                onglet.charger(/*getId(), */tp.getDataTP());
                if(!editeur.isEditable()) {onglet.setIdTP(0);}//Depuis un editeur non éditable, on ne doit pas pouvoir mettre à jour le tp
            }
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
            this.oldImage = tp.getSVGString();
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
