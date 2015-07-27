/** «Copyright 2013,2014 François Billioud»
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
package matheos.texte;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.print.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataTP;
import matheos.texte.composants.ComposantTexte;
import matheos.texte.composants.JHeader;
import matheos.texte.composants.JLabelImage;
import matheos.texte.composants.JLabelNote;
import matheos.texte.composants.JLabelTP;
import matheos.texte.composants.JLabelText;
import matheos.utils.librairies.ImageTools;
import matheos.utils.librairies.TransferableTools;
import matheos.utils.texte.EditeurKit;
import matheos.utils.texte.JMathTextPane;


/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class Editeur extends JMathTextPane implements Printable {

    private Formatter formatter;
    public static final String TITLE_PROPERTY = "title";

    public Formatter getFormatter() {return (formatter==null ? formatter=new Formatter(this) : formatter);}
    
    private final JLabelTP.TPListener tpListener = new JLabelTP.TPListener(this);
    private final JLabelImage.ImageMouseListener imageListener = new JLabelImage.ImageMouseListener(this);
    private final JLabelText.LabelTextMouseListener labelListener = new JLabelText.LabelTextMouseListener(this);
    private final JLabelNote.NoteListener noteListener = new JLabelNote.NoteListener(this);
    private final JHeader.HeaderListener headerListener = new JHeader.HeaderListener(this);
    private final PropertyChangeListener labelSizeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            undo.validateAndAddEdit(new JLabelImage.TailleEdit((ComposantTexte.Image)evt.getSource(), (int)evt.getOldValue(), (int)evt.getNewValue()));
        }
    };
    private final PropertyChangeListener titleChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JLabelText label = (JLabelText)evt.getSource();
            if(label.getId()==1L) {Editeur.this.firePropertyChange(TITLE_PROPERTY, evt.getOldValue(), evt.getNewValue());}//permet de détecter les changements de nom de titre de chapitre
            undo.validateAndAddEdit(new JLabelText.LabelChangeEdit((JLabelText)evt.getSource(), (String)evt.getOldValue(), (String)evt.getNewValue()));//permet d'annuler un changement de titre
        }
    };
    
    public Editeur() {
        super();
        setAutoscrolls(true);
        setEditeurKit(new EditeurKit(this));//définit cet éditeur comme le seul concerné par les objets de l'EditeurKit
        getHTMLdoc().setDocumentFilter(new EditeurFiltre());

        //HACK : ces actions sont capturées par le JTextComponent
        this.getActionMap().put("next-link-action", editeurKit.getBoutonSubTitle().getAction());
        this.getActionMap().put("previous-link-action", editeurKit.getBoutonTitle().getAction());

        //HACK : problème de background avec Nimbus
//        LaFFixManager.fix(this, ColorManager.get("color disabled"));
    }
    
    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
    }
    
    /** Empêche le retrait de certains JLabel **/
    private class EditeurFiltre extends Filtre {
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if(!isEditable()) {return;}
            for(int i = offset; i<offset+length; i++) {
                Component c = getComponentAt(i);
                if(c instanceof JLabelText) {
                    if(!((JLabelText)c).isRemovable()) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                } else if(c instanceof JHeader) {
                    if(!((JHeader)c).isRemovable()) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
            }
            super.remove(fb, offset, length);
        }
    }

    //on ajoute la possibilité de coller une image
    @Override
    public void coller() {
//        desactiverFiltre();

        //TODO : remplacer les balises img par des JLabelImage. corriger les tailles de police avec ChoixTaille.closestSize
//        int posStart = Math.min(this.getCaret().getDot(), this.getCaret().getMark());
        if (this.getSelectedText() != null && this.getSelectedText().length()>0) {
            undo.valider();
            try {
                htmlDoc.remove(this.getSelectionStart(), this.getSelectedText().length());
            } catch (BadLocationException ex) {
                Logger.getLogger(JMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            Transferable clipboardContent = clipboard.getContents(this);
            // Cas d'un texte copié depuis MathEOS
            if(clipboardContent.isDataFlavorSupported(TransferableTools.matheosFlavor)) {
                super.coller();
            // Cas d'un texte copié depuis msword
//            } else if(clipboardContent.isDataFlavorSupported(TransferableTools.htmlInputStreamFlavor)) {
//                new HTMLEditorKit().read((InputStream) clipboard.getData(TransferableTools.htmlInputStreamFlavor), htmlDoc, getCaretPosition());
            // Cas d'un texte copié depuis un autre logiciel
            } else if(clipboardContent.isDataFlavorSupported(TransferableTools.htmlFlavor)) {
                super.coller();
//                String html = (String) clipboardContent.getTransferData(TransferableTools.htmlFlavor);
////                html = html.replaceAll("<o:p></o:p>", "").replaceAll("<!--.*.-->", "");//supprime les bouses de msWord
//                html = html.replaceAll("<o:p></o:p>", "");//supprime les bouses de msWord
//                Document doc = Jsoup.parse(html);
//                JsoupTools.removeComments(doc);
//                new HTMLEditorKit().insertHTML(htmlDoc, getCaretPosition(), "<div>"+doc.body().html()+"</div>", 0, 0, Tag.DIV);
            // Cas d'un texte copié depuis msword
//            } else if(clipboardContent.isDataFlavorSupported(TransferableTools.htmlInputStreamFlavor)) {
//                new StyledEditorKit().read((InputStream) clipboard.getData(TransferableTools.htmlInputStreamFlavor), htmlDoc, getCaretPosition());
            // Cas de la copie d'une image
            } else if(clipboardContent.isDataFlavorSupported(TransferableTools.imageFlavor)) {
                Image image = (Image) clipboardContent.getTransferData(TransferableTools.imageFlavor);
                insererImage(image);
                setCaretPosition(getCaretPosition()+1);
            // Cas de la copie d'un fichier
            } else if(clipboardContent.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                List<File> files = (List<File>)clipboardContent.getTransferData(DataFlavor.javaFileListFlavor);
                for(File f : files) {
                    if(f.exists()) {
                        try {
                            Image im = ImageIO.read(f);
                            if(im==null) throw new IOException();
                            insererImage(im);
                        } catch(IOException ex) {
                            super.coller();
                        }
                    } else {
                        super.coller();
                    }
                }
            // Cas d'un texte quelconque
            } else if(clipboardContent.isDataFlavorSupported(TransferableTools.textFlavor)) {
                super.coller();
            }
        } catch (UnsupportedFlavorException | IOException ex) {
            Logger.getLogger(JMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
        }

//        activerFiltre();
    }

    /**
     * Surcharge de la méthode d'insertion des composants pour gérer l'insertion
     * des JLabel et JMathComponent et la mise à jour des Attributs pour écrire autour.
     *
     * @param c le composant à insérer.
     */
    @Override
    public void insertComponent(Component c) {
        if(c instanceof JLabelTP) {insererTP((JLabelTP) c);}
        else if(c instanceof JLabelImage) {insererImage((JLabelImage) c);}
        else if(c instanceof JLabelText) {insererLabel((JLabelText) c);}
        else {super.insertComponent(c);}
    }

    @Override
    public void clearComponent(Component c) {
        if(c instanceof JLabelTP) {clearTP((JLabelTP) c);}
        else if(c instanceof JHeader) {clearHeader((JHeader) c);}
        else if(c instanceof JLabelNote) {clearNote((JLabelNote) c);}
        else if(c instanceof JLabelImage) {clearImage((JLabelImage) c);}
        else if(c instanceof JLabelText) {clearLabel((JLabelText) c);}
        else {super.clearComponent(c);}
    }

    /**
     * Remplace les données d'un TP précédemment insérer. La vérification
     * de l'existance du tp dans le JTextPane est à la charge de l'appelant
     * @param oldTP
     * @param newData
     * @param newImage 
     */
    public void updateTP(JLabelTP oldTP, DataTP newData, String newImage) {
        undo.validateAndAddEdit(new JLabelTP.TPEdit(oldTP, newData, newImage));//(tp, tp.getNomTP(), nomTP, tp.getDataTP(), data, tp.getImageInitiale(), imageTP));
        oldTP.setParametres(newData, newImage);
        this.repaint();
    }

    public void clearTP(JLabelTP label) {
        label.removePropertyChangeListener(labelSizeListener);
        label.removeMouseListener(tpListener);
    }
    public void insererTP(JLabelTP label) {
        MutableAttributeSet inputAttributes = new SimpleAttributeSet(); // this.getInputAttributes();
//        inputAttributes.addAttributes(EditeurKit.getStyleAttributes());

        inputAttributes.addAttribute(COMPONENT_TYPE_ATTRIBUTE, JLabelImage.JLABEL_IMAGE);
        long id = label.getId();
        String type = JLabelTP.JLABEL_TP;
        insertComponent(label, inputAttributes, id, type);
//        label.addMouseListener(new JLabelTP.TPDoubleClicListener(this));//Déjà ajouté à la création
        label.setSelectionColor(getSelectionColor());
        label.addPropertyChangeListener(JLabelImage.SIZE_PROPERTY, labelSizeListener);
        label.addMouseListener(tpListener);
        label.setSelected(true);
        label.setSelected(false);//Hack pour donner la bonne couleur au composant au départ
    }
    
    public void clearNote(JLabelNote label) {
        label.removePropertyChangeListener(labelSizeListener);
        label.removeMouseListener(noteListener);
    }
    public void insererNote(JLabelNote label) {
        MutableAttributeSet inputAttributes = new SimpleAttributeSet(); // this.getInputAttributes();
//        inputAttributes.addAttributes(EditeurKit.getStyleAttributes());

        inputAttributes.addAttribute(COMPONENT_TYPE_ATTRIBUTE, JLabelImage.JLABEL_IMAGE);
        long id = label.getId();
        String type = JLabelNote.JLABEL_NOTE;
        insertComponent(label, inputAttributes, id, type);
//        label.addMouseListener(new JLabelTP.TPDoubleClicListener(this));//Déjà ajouté à la création
        label.setSelectionColor(getSelectionColor());
        label.addPropertyChangeListener(JLabelImage.SIZE_PROPERTY, labelSizeListener);
        label.addMouseListener(noteListener);
        label.setSelected(true);
        label.setSelected(false);//Hack pour donner la bonne couleur au composant au départ
    }

    public void clearHeader(JHeader header) {
        header.removeMouseListener(headerListener);
    }
    public void insererHeader(JHeader header) {
        MutableAttributeSet inputAttributes = new SimpleAttributeSet(); // this.getInputAttributes();

        inputAttributes.addAttribute(COMPONENT_TYPE_ATTRIBUTE, JHeader.JHEADER);
        long id = header.getId();
        String type = JHeader.JHEADER;
        insertComponent(header, inputAttributes, id, type);
//        Element e = getHTMLdoc().getElement(JMathTextPane.getSpanId(id));
//        try {
//            getHTMLEditorKit().insertHTML(getHTMLdoc(), getCaretPosition(), "<p>&nbsp;</p>", 2, 0, HTML.Tag.P);
//        } catch (BadLocationException ex) {
//            Logger.getLogger(Editeur.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(Editeur.class.getName()).log(Level.SEVERE, null, ex);
//        }
        header.addMouseListener(headerListener);
    }

    public void clearImage(JLabelImage label) {
        label.removePropertyChangeListener(labelSizeListener);
        label.removeMouseListener(imageListener);
    }
    public void insererImage(final JLabelImage label) {
        MutableAttributeSet inputAttributes = new SimpleAttributeSet(); // this.getInputAttributes();
//        inputAttributes.addAttributes(EditeurKit.getStyleAttributes());

        inputAttributes.addAttribute(COMPONENT_TYPE_ATTRIBUTE, JLabelImage.JLABEL_IMAGE);
        long id = label.getId();
        String type = JLabelImage.JLABEL_IMAGE;
        insertComponent(label, inputAttributes, id, type);
//        label.addMouseListener(new JLabelImage.ImageMouseListener(this));//déjà ajouté à la création
        label.setSelectionColor(getSelectionColor());
        label.addMouseListener(imageListener);//écoute les clics sur le composant
        label.addPropertyChangeListener(labelSizeListener);
    }
    
    public void clearLabel(JLabelText label) {
        removePropertyChangeListener(titleChangeListener);
    }
    public void insererLabel(JLabelText label) {
        label.addPropertyChangeListener(JLabelText.CONTENT_PROPERTY, titleChangeListener);
        
        MutableAttributeSet inputAttributes = new SimpleAttributeSet(); // this.getInputAttributes();
//        inputAttributes.addAttributes(EditeurKit.getStyleAttributes());

        inputAttributes.addAttribute(COMPONENT_TYPE_ATTRIBUTE, JLabelText.JLABEL_TEXTE);
        StyleConstants.setForeground(inputAttributes, label.getForeground());
        long id = label.getId();
        String type = JLabelText.JLABEL_TEXTE;
        insertComponent(label, inputAttributes, id, type);
        label.addMouseListener(new JLabelText.LabelTextMouseListener(this));
        label.setSelectionColor(getSelectionColor());
    }

    public void insererImage(Image image) {
        insererImage(new JLabelImage(ImageTools.imageToBufferedImage(image),image.getHeight(this)));
    }


    /**
     * Méthode chargeant le HTMLDocument de l'Editeur, et
     * réinitialisant ainsi l'Editeur avec son contenu.
     *
     * @param donnees DataTexte qui contient les données nécessaires pour charger un(e) cours/exercice/evaluation
     */
    @Override
    public void charger(Data donnees) {
        resetDocument();
        if(donnees!=null) {
            super.charger(donnees);
            undo.discardAllEdits();
        }
        this.requestFocusInWindow();
        this.setCaretPosition(htmlDoc.getLength());
    }

    @Override
    public int print(Graphics g, PageFormat page, int numero) throws PrinterException {
        return getFormatter().print(g, page, numero);
    }

    public void imprime() {
        getFormatter().imprime();
    }

    public void miseEnPage() {
        getFormatter().miseEnPage();
    }

    public void apercu() {
        getFormatter().apercu();
    }

    @Override
    protected void activeComposants(boolean active) {
        super.activeComposants(active);
    }

    /**
     * Méthode permettant de réinitialiser l'Editeur.
     */
    public void resetDocument() {
        //RàZ du style
//        if(editeurKit!=null) {editeurKit.reset();}
        StyleConstants.setAlignment(getInputAttributes(),StyleConstants.ALIGN_LEFT);
        StyleConstants.setForeground(getInputAttributes(),Color.BLACK);
        
        DocumentFilter f = htmlDoc.getDocumentFilter();
        htmlDoc.setDocumentFilter(null);
        clear();
        htmlDoc.setDocumentFilter(f);
        
        undo.discardAllEdits();
    }

    
    /**
     * Méthode qui liste l'ensemble des label image insérés dans l'Editeur.
     *
     * @return Une Hashmap contenant en clé la position de l'image et en valeur
     * le label image lui-même.
     */
    public HashMap<Integer, JLabelImage> getImageComponents() {
        HashMap<Integer, JLabelImage> imageListe = new HashMap<>();
        if (htmlDoc == null) {
            return null;
        }
        if (htmlDoc.getLength() < 1) {
            return null;
        }
        for (int i = 0; i < htmlDoc.getLength(); i++) {
            if (htmlDoc.getCharacterElement(i).getAttributes().getAttribute(StyleConstants.ComponentAttribute) instanceof JLabelImage) {
                JLabelImage labelImage = (JLabelImage) htmlDoc.getCharacterElement(i).getAttributes().getAttribute(StyleConstants.ComponentAttribute);
                imageListe.put(i, labelImage);
            }
        }
        if (imageListe.isEmpty()) {
            return null;
        }
        return imageListe;
    }
    
    /**
     * Récupère un JLabelTP dans le HTMLDocument de l'Editeur à partir de son id
     * unique.
     *
     * @param id l'id du JLabelTP
     * @return le JLabelTP correspondant à l'id passé en paramètre ou null s'il
     * n'est pas présent dans l'Editeur
     */
    public JLabelTP getTP(long id) {
        Component c = componentMap.get(getSpanId(id));
        return (c instanceof JLabelTP) ? (JLabelTP)c : null;
    }

    @Override
    public void annuler() {
        super.annuler();
    }

    @Override
    public void refaire() {
        super.refaire();
    }
    
    @Override
    public int getPreferredHeight() {
        if(this.getWidth()>0 && this.getHeight()>0) {
            try {
                Rectangle r = modelToView(getLength());
                return r.y+r.height;
            } catch (BadLocationException ex) {
                Logger.getLogger(Editeur.class.getName()).log(Level.SEVERE, null, ex);
                return super.getPreferredHeight();
            }
        } else {
            return super.getPreferredHeight();
        }
    }
    
    @Override
    public int getStringHeight(int posStart, int posEnd) throws BadLocationException {
        if(this.getWidth()>0 && this.getHeight()>0) {
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            for(int pos = posStart; pos<=posEnd; pos++) {
                Rectangle r = modelToView(pos);
                if(r==null) continue;
                if(r.y<minY) minY = r.y;
                if(r.y+r.height>maxY) maxY = r.y+r.height;
            }
            return maxY - minY;
        } else {
            return super.getStringHeight(posStart, posEnd);
        }
    }
    
    @Override
    public int getPreferredWidth() {
        if(this.getWidth()>0 && this.getHeight()>0) {
            int maxX = Integer.MIN_VALUE;
            for(int pos = 0; pos<=getLength(); pos++) {
                Rectangle r;
                try {
                    r = modelToView(pos);
                    if(r.x+r.width>maxX) maxX = r.x+r.width;
                } catch (BadLocationException ex) {
                    Logger.getLogger(Editeur.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return maxX;
        } else {
            return super.getPreferredWidth();
        }
    }
    
    @Override
    public int getStringWidth(int posStart, int posEnd) throws BadLocationException {
        if(this.getWidth()>0 && this.getHeight()>0) {
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            for(int pos = posStart; pos<=posEnd; pos++) {
                Rectangle r = modelToView(pos);
                if(r==null) continue;
                if(r.x<minX) minX = r.x;
                if(r.x+r.width>maxX) maxX = r.x+r.width;
            }
            return maxX - minX;
        } else {
            return super.getStringWidth(posStart, posEnd);
        }
    }
    

//    /**
//     * Cette class permet de générer un objet qui corrigera le fontSize et la couleur des JMathComponent
//     * insérés dans le JMathTextPane lorsque les attributs du document sont modifiés.
//     */
//    DocumentListener labelStyleChangeListener = new DocumentListener() {
//        @Override
//        public void changedUpdate(DocumentEvent e) {
//            for(int i=e.getOffset(); i<e.getOffset()+e.getLength(); i++) {
//                Component c = getComponentAt(i);
//                if(c!=null && c instanceof ComposantTexte) {
//                    ComposantTexte label = (ComposantTexte) c;
//                    MutableAttributeSet attr = new SimpleAttributeSet(htmlDoc.getCharacterElement(i).getAttributes());//XXX vérifier qu'il ne faut pas prendre les attributs du charactère i-1
////                    if(editeurKit!=null) {attr.addAttributes(editeurKit.getStyleAttributes());}
//                    if(label.getForeground() != StyleConstants.getForeground(attr)) {
//                        if(StyleConstants.getForeground(attr)!=Color.BLACK){//HACK pour éviter les retours au noir indésirables
//                            label.setForeground(StyleConstants.getForeground(attr));
//                        }
//                    }
//                    int fontSize = StyleConstants.getFontSize(attr);
//                    float size = (float) Math.sqrt(fontSize * getFont().getSize());//XXX d'où sort cette formule ?
//                    if ((int) label.getFontSize() != size) { label.setFontSize(size); }
//                }
//            }
//        }
//        public void insertUpdate(DocumentEvent e) {}
//        public void removeUpdate(DocumentEvent e) {}
//    };
}
