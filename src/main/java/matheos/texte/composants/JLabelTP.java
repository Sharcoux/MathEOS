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

import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import matheos.IHM;
import matheos.IHM.ONGLET_TP;
import matheos.elements.Onglet;
import matheos.json.Json;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataTP;
import matheos.texte.Editeur;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class JLabelTP extends SVGPanel implements ComposantTexte.Image {

    /** Constante permettant d'identifier un JLabelTP **/
    public static final String JLABEL_TP = "labelTPComponent";
    
    private DataTP dataTP;
    private String nomTP;
    private String svg;
    private int largeurInitiale;
    private long id = System.currentTimeMillis();// L'id unique du JLabelImage permettant de l'identifier
    private final double coef;
    
    private int largeurMax = Integer.MAX_VALUE;
    @Override
    public int getLargeurMax() {
        Editeur editeur = (Editeur) SwingUtilities.getAncestorOfClass(Editeur.class, this);
        return Math.min(largeurMax, (editeur!=null && editeur.getWidth()!=0) ? editeur.getWidth() : Integer.MAX_VALUE);
    }
    public void setLargeurMax(int largeur) {largeurMax = largeur;}
    @Override
    public Dimension getMaximumSize() {int l = largeurInitiale/*getLargeurMax()*/;return new Dimension(l, (int) (l*coef));}

    public JLabelTP(String image, DataTP data, String nomTP, int largeur, int hauteur) {
        image = image.replaceAll("&times;", "&#x000d7;");//JMathComponent ne lit pas le HTML
        image = image.replaceAll("&divide;", "&#x000f7;");//JMathComponent ne lit pas le HTML
        image = image.replaceAll("&plusmn;", "&#177;");//JMathComponent ne lit pas le HTML
        image = image.replaceAll("xml:space=\"preserve\"", "xml:space=\"default\"");//JMathComponent ne lit pas les \n (JMathComponent c'est un peu de la merde...)
        this.svg = image;
        this.coef = hauteur/(double)largeur;
        
        SVGUniverse uni = new SVGUniverse();
        StringReader r = new StringReader(svg);
        uni.loadSVG(r,id+".svg");
        super.setSvgUniverse(uni);
        super.setSvgURI(uni.getStreamBuiltURI(id+".svg"));
        super.setScaleToFit(true);
        super.setAntiAlias(true);
        
        this.largeurInitiale = largeur;
        setSize(largeur, hauteur);
        setDataTP(data);
        setNomTP(nomTP);
        addMouseListener(new TPListener());
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(largeurInitiale, (int) (largeurInitiale*coef));
    }
//    public Dimension getMaximumSize() {return getPreferredSize();}

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
    public void setSVG(String svg) {
        this.svg = svg;
        getSvgUniverse().loadSVG(new StringReader(svg), "test2.svg");
        setSvgURI(getSvgUniverse().getStreamBuiltURI("test2.svg"));
    }

    /**
     * Méthode permettant de changer les paramètres d'un JLabelTP.
     * @param data les nouvelles données Serializable
     * @param svg la nouvelle image du JLabelTP
     */
    public void setParametres(DataTP data, String svg) {
//        setNomTP(nomTP);
        setDataTP(data);
        setSVG(svg);
    }

    @Override
    public JLabelTP copy() {
        return new JLabelTP(svg, dataTP, nomTP, getWidth(), getHeight());
    }

    @Override
    public String getHTMLRepresentation() {
        Document d = Jsoup.parse(this.svg);d.outputSettings(new Document.OutputSettings().prettyPrint(false));
        Element svgElement = d.select("svg").first();
        svgElement.attr("id", id + "").attr("width",getWidth()+"").attr("height",getHeight()+"");
        svgElement.attr("title", getNomTP());
        return svgElement.outerHtml();
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
    
    private Color couleurSelection;
    public void setCouleurSelection(Color couleurSelection) {
        this.couleurSelection = couleurSelection;
    }

    @Override
    public void selectionner() {
        this.setBackground(couleurSelection);
        this.repaint();
    }

    @Override
    public void deselectionner() {
        this.setBackground(Color.WHITE);
        this.repaint();
    }

    public void setSize(int largeur) {
        largeurInitiale = largeur;
        int l = Math.min(getLargeurMax(), largeur);
        this.setSize(largeur, (int) (largeur*coef));
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

    private final class TPListener extends MouseAdapter implements FocusListener {
        private TPListener() { super(); }
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (e.getComponent() instanceof JLabelTP) {
                    if (e.getClickCount() == 2) {
                        editerTP();
                    } else {
                        Editeur editeur = (Editeur) SwingUtilities.getAncestorOfClass(Editeur.class, JLabelTP.this);
                        javax.swing.text.Element element = editeur.getHTMLdoc().getElement(Editeur.getSpanId(getId()));
                        editeur.select(element.getStartOffset(),element.getEndOffset());
                    }
                }
            } else {
                JDialog dialogueImageTaille = new ComposantTexte.Image.ImageSizeEditor(JLabelTP.this);
            }
        }
        @Override
        public void focusGained(FocusEvent e) {}
        @Override
        public void focusLost(FocusEvent e) {deselectionner();}
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
