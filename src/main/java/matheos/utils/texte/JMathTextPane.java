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
package matheos.utils.texte;

import matheos.clavier.ParticularKeyListener;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.Data.Enregistrable;
import matheos.sauvegarde.DataTexte;
import matheos.texte.Editeur;
import matheos.texte.composants.ComposantTexte;
import matheos.texte.composants.JLabelText;
import matheos.utils.interfaces.Editable;
import matheos.utils.interfaces.Undoable;
import matheos.utils.librairies.JsoupTools;
import matheos.utils.librairies.TransferableTools;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.CursorManager;
import matheos.utils.texte.MathTools.MathMouseListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import net.sourceforge.jeuclid.swing.JMathComponent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public abstract class JMathTextPane extends JTextPane implements Editable, Undoable, Enregistrable {

    /** Permet de convertir les tailles de texte HTML3 en unités "em" conformes CSS3 **/
    public static final Double[] FONT_CONVERSION_EM = {0.0, 0.7, 0.8, 1.0, 1.2, 1.5, 2.0, 3.0};
    /** Permet de convertir les tailles de texte HTML3 en unités "pt" conformes RTF **/
    public static final Integer[] FONT_CONVERSION_PT = {0, 8, 10, 12, 14, 18, 24, 36};
    
    public static final String COMPONENT_TYPE_ATTRIBUTE = "componentAttribute";
    public static final String COMPONENT_ID_ATTRIBUTE = "componentId";
    public static final String SPECIAL_COMPONENT = "special-math-component";
    public static final String FONT_SIZE_PROPERTY = "font size";
    protected final static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); // Presse papier pour le copier-coller
    
    protected EditeurKit editeurKit;   //permet aux classes utilisatrices du JLimitedTextPane de contrôler celui-ci.
    protected HTMLEditorKit editorKit;
    protected HTMLDocument htmlDoc;
    protected CompositeUndoManager undo;
    protected HashMap<String, Component> componentMap = new HashMap<>();

    public JMathTextPane() {
        setBorder(BorderFactory.createEmptyBorder());//HACK pour que les marges soient correctes
        setMargin(new Insets(1, 10, 3, 10));
        
        editorKit = new HTMLEditorKit();
        editorKit.setDefaultCursor(CursorManager.getCursor(Cursor.TEXT_CURSOR));
        setEditorKit(editorKit);
        
        htmlDoc = (HTMLDocument) editorKit.createDefaultDocument();
        htmlDoc.setPreservesUnknownTags(false);
        htmlDoc.putProperty("IgnoreCharsetDirective", true);
//        htmlDoc.addDocumentListener(new MathStyleChangeListener()); //adapte le style des JMathComponent au style du htmlDoc
        addPropertyChangeListener("caretColor",componentColorListener);//change la couleur des composants si la color du texte est modifiée
        addPropertyChangeListener("font",componentFontSizeListener);//change a taille des composants si le fontSize du texte est modifié
        
//        DefaultCaret caret = (DefaultCaret) getCaret();
//        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        this.setDocument(htmlDoc);
        this.setEditable(true);

//        this.setContentType("text/html");
        addCaretListener(new MiseAJourStyles());

        undo = new CompositeUndoManager();
        undo.addPropertyChangeListener(new UndoableStateListener());

        htmlDoc.setDocumentFilter(new Filtre());
        htmlDoc.addUndoableEditListener(undo);
        //htmlDoc.addUndoableEditListener(undo);

        //HACK car ces 3 combinaisons sont capturées par le JTextComponent
        //les combinaisons existantes se trouvent dans getInputMap().parent.parent.parent.arrayTable.table
        this.getActionMap().put("copy-to-clipboard", new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {copier();}});
        this.getActionMap().put("paste-from-clipboard", new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {coller();}});
        this.getActionMap().put("cut-to-clipboard", new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {couper();}});
        this.removeBinding(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        
        //Permet de changer la couleur des composants lorsqu'ils sont sélectionnés
        ComponentSelectionListener selectionListener = new ComponentSelectionListener(this);
        this.addMouseMotionListener(selectionListener);
        this.addCaretListener(selectionListener);
        this.addFocusListener(selectionListener);
        this.addMouseListener(selectionListener);

        //génère un JMathComponent à l'appuie sur ² ou *
        this.addParticularKeyListener(new ParticularKeyListener.EtoileKeyListener());
        this.addParticularKeyListener(new ParticularKeyListener.CarreKeyListener());
        
        removeHead();//HACK : Evite des bugs liés à la position initiale du body qui n'est pas toujours 0
        
        //debugger
        addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)) {
//                    PermissionManager.showPermissions();
//                    System.out.println(EditeurIO.write(JMathTextPane.this).contenuHTML);
                    System.out.println(getDonnees().getContenuHTML());
    //                StringWriter w = new StringWriter();
    //                try {
    //                    editeur.getEditorKit().write(w, editeur.getDocument(), 0, editeur.getLength());
    //                    System.out.println(w.toString());
    //                } catch (IOException ex) {
    //                    Logger.getLogger(OngletTexte.class.getName()).log(Level.SEVERE, null, ex);
    //                } catch (BadLocationException ex) {
    //                    Logger.getLogger(OngletTexte.class.getName()).log(Level.SEVERE, null, ex);
    //                }
                }
            }
        });
    }

    /**
     * Renvoie l'EditeurKit permettant d'obtenir facilement des composants Swing pour contrôler le TextPane
     * @return l'EditeurKit associé au TextPane
     */
    public EditeurKit getEditeurKit() {return editeurKit==null ? editeurKit=new EditeurKit() : editeurKit;}

    private final FocusListener focusedEditeurListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {//On s'assure que l'editeurKit s'intéresse à l'éditeur actif
                editeurKit.setTextEditor(JMathTextPane.this);
            }
        };
    /**
     * Par défaut le JLimitedTextPane possède un EditeurKit. Cependant, celui-ci
     * peut être remplacé si vous souhaitez utiliser un même EditeurKit pour plusieurs JLimitedTextPane
     */
    public void setEditeurKit(final EditeurKit editeurKit) {
        this.removeFocusListener(focusedEditeurListener);
        this.editeurKit = editeurKit;
        this.addFocusListener(focusedEditeurListener);
    }

    //public void setEditorKit(HTMLEditorKit editorKit) { this.editorKit = editorKit; }
    public HTMLDocument getHTMLdoc() { return htmlDoc; }

    public Map<String, Component> getComponentMap() { return componentMap; }

    public void insererJMathComponent(JMathComponent mathComponent) {
        insererJMathComponent(mathComponent, new SimpleAttributeSet());
    }

    protected void insererJMathComponent(JMathComponent mathComponent, AttributeSet attr) {
        if(mathComponent==null) {return;}
        removeHead();//HACK pour pouvoir insérer les composant en début de document
        
        MutableAttributeSet inputAttributes = (attr==null ? new SimpleAttributeSet() : new SimpleAttributeSet(attr));
        inputAttributes.addAttribute(COMPONENT_TYPE_ATTRIBUTE, MathTools.MATH_COMPONENT);

        //on prépare le JMathComponent soit avec le style passé en paramètre, soit avec le style courant de l'éditeur
        Color foreground = StyleConstants.getForeground(inputAttributes.getAttribute(StyleConstants.Foreground)!=null ? inputAttributes : getCharacterAttributes());
        int size = StyleConstants.getFontSize(inputAttributes.getAttribute(StyleConstants.FontSize)!=null ? inputAttributes : getCharacterAttributes());
        int fontSize = (int) Math.sqrt(size * this.getFont().getSize());
//        mathComponent.setForeground(foreground);
//        mathComponent.setFontSize(fontSize);

        insertComponent(mathComponent, inputAttributes, MathTools.getId(mathComponent), MathTools.MATH_COMPONENT);
        // On peut choisir ici si l'on préfère une édition par clic souris ou par PopupMenu
        // Commenter l'une des deux lignes suivantes selon le choix
        mathComponent.addMouseListener(new MathMouseListener(this));
        // math.setComponentPopupMenu(new MenuContextuelMathComponent(math));
        
        mathComponent.addPropertyChangeListener(MathTools.ALIGNMENT_Y_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                undo.validateAndAddEdit(new MathTools.MathAlignmentEdit((JMathComponent)evt.getSource(),(float)evt.getOldValue(),(float)evt.getNewValue()));
            }
        });

    }
    protected void insertComponent(Component c, AttributeSet attr, long id, String type) {
        if (this.getSelectedText() != null && this.getSelectedText().length()>0) {
            try {
                htmlDoc.remove(this.getSelectionStart(), this.getSelectedText().length());
            } catch (BadLocationException ex) {
                Logger.getLogger(Editeur.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        int position = getSelectionStart();
        String spanID = getSpanId(id);//afin de différencier l'id du span et l'id du contenu (cf id dans getHtmlRepresentation de JLabelImage)
        
        MutableAttributeSet inputAttributes = new SimpleAttributeSet();
        if(editeurKit!=null) {inputAttributes.addAttributes(editeurKit.getStyleAttributes());}  //ajoute les attributs des boutons de style
        if(attr!=null) inputAttributes.addAttributes(attr);                                     //ajoute les attributs spécifiques passés en paramètres
        StyleConstants.setComponent(inputAttributes, c);                                        //ajoute le composant
        inputAttributes.addAttribute(COMPONENT_ID_ATTRIBUTE, spanID);                           //ajoute l'ID du span du composant
        boolean succeeded = insertComponent(position, spanID, c, inputAttributes, type);
    }
    
    private boolean insertComponent(int position, String spanID, Component c, AttributeSet inputAttributes, String type) {
        activerFiltre(false);

        try {
            String toInsert = "<span id='"+spanID+"' class='"+SPECIAL_COMPONENT+" "+type+"'>&nbsp;</span>";
            if(type.equals(JLabelText.JLABEL_TEXTE)) {// || type.equals(MathTools.MATH_COMPONENT)) {//HACK pour insérer correctement les JLabelText
                htmlDoc.insertAfterEnd(htmlDoc.getCharacterElement(position),toInsert);
            } else {
                insererHTML(toInsert, position, Tag.SPAN);
            }
            componentMap.put(spanID, c);
            htmlDoc.setCharacterAttributes(position, 1, inputAttributes, false);
            //FIXME on est obligé d'ajouter/retirer un composant pour forcer le raffraichissement du charactère précédent
            htmlDoc.insertString(position+1, " ", null);
            htmlDoc.remove(position+1, 1);
            return true;
        } catch (IOException | BadLocationException ex) {
            Logger.getLogger(Editeur.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            activerFiltre(true);// On réactive le filtre à la fin de l'insertion
        }
        return false;
    }
    
    public DataTexte getSelectedDataTexte() {
        return EditeurIO.write(this, getSelectionStart(), getSelectionEnd());
    }

    public void insererHTML(String html, int pos, Tag firstTag) throws BadLocationException, IOException {
        int pushDepth=0, popDepth=0;
//        if(firstTag.isBlock()) {
//            popDepth = isEOL(pos-1) ? 0 : 1;
//            pushDepth= (isEOL(pos+1)||pos>=getLength()) ? 0 : 1;
//        } else {
//        javax.swing.text.Element e = htmlDoc.getParagraphElement(pos);
        pushDepth = isEOL(pos-1) ? 1 : 0;
//        while(!e.getParentElement().getName().equals("body") && e.getStartOffset()==pos) {
//            pushDepth++;
//            e = e.getParentElement();
//        }
        popDepth = pushDepth;
        try {
            editorKit.insertHTML(htmlDoc, pos, html, popDepth, pushDepth, firstTag);
        } catch(Exception e) {
            System.out.println("couldn't insert the normal way : "+html);
            popDepth = 1-popDepth; pushDepth = 1-pushDepth;//on inverse la valeur de profondeur (de 0 à 1 ou l'inverse)
            editorKit.insertHTML(htmlDoc, pos, html, popDepth, pushDepth, firstTag);
        }
//        editorKit.insertHTML(htmlDoc, pos, html, popDepth, pushDepth, firstTag);
        revalidate();
        repaint();
    }
    private boolean isEOL(int pos) throws BadLocationException {
        if(pos<0 || pos>getLength()-1) {return false;}
        String texte = this.getText(pos, 1);
        if(texte.equals(" ") || texte.equals("&nbsp;") && !isComponentPosition(pos)) {return isEOL(pos-1);}
        return texte.equals("\n");
    }
    private int getDocumentStart() {
        javax.swing.text.Element head = htmlDoc.getDefaultRootElement().getElement(0);
        if(head.getName().equals("head")) {return htmlDoc.getDefaultRootElement().getElement(1).getStartOffset();}
        return 0;
    }

    @Override
    public void insertComponent(Component c) {
        if(c instanceof JMathComponent) {insererJMathComponent((JMathComponent) c);}
        else {super.insertComponent(c);}
    }
    
    @Override
    public String getText() {
        try {
            return getHTMLdoc().getText(0, getLength());
        } catch (BadLocationException ex) {
            Logger.getLogger(JMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }
    
    public void annuler() {
        if (undo.peutAnnuler()) {
            undo.annuler();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
        this.requestFocus();
    }

    public void refaire() {
        if (undo.peutRefaire()) {
            undo.refaire();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
        this.requestFocus();
    }

    public boolean peutAnnuler() {
        return undo.peutAnnuler();
    }

    public boolean peutRefaire() {
        return undo.peutRefaire();
    }

    public boolean peutColler() {
        return clipboard.getContents(htmlDoc)!=null;
    }

    public boolean peutCopier() {
        return getSelectionStart()!=getSelectionEnd();
    }

    public boolean peutCouper() {
        return getSelectionStart()!=getSelectionEnd();
    }
    
    public CompositeUndoManager getUndo() {
        return undo;
    }

    public boolean isEmpty() {
        return htmlDoc.getLength() == 0;
    }

    public void setFontSize(int size) {
        int old = getFont().getSize();
        if(size==old) {return;}
        this.setFont(this.getFont().deriveFont((float) size));
        
        //change la taille des composants insérés
        for(Component c : componentMap.values()) {
            if(c instanceof JMathComponent) {((JMathComponent)c).setFontSize(size);}
            else if (c instanceof JLabelText) {((ComposantTexte)c).setFontSize(size);}
        }
        
        firePropertyChange(FONT_SIZE_PROPERTY, old, size);
    }

    /**
     * Retourne le numéro de la ligne correspondant à la position donnée en
     * paramètre. La première ligne sera d'index 1.
     *
     * @param pos la position dont on souhaite connaître la ligne
     * @return la ligne qui contient la position donnée, ou une -1 si l'index
     * passé en paramètre est incorrect
     */
    public int getLine(int pos) {
        if (pos < 0 || pos > htmlDoc.getLength()) {
            return -1;
        }
        try {
            double posY = this.modelToView(pos).getY();
            double lastY = this.modelToView(0).getY();
            int numeroLigne = 1;
            for (int i = 0; i <= htmlDoc.getLength(); i++) {
                double newPosY = this.modelToView(i).getY();
                if (newPosY != lastY) {
                    lastY = newPosY;
                    numeroLigne++;
                }
                if (newPosY == posY) {
                    return numeroLigne;
                }
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(JMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    /**
     * Retourne le numéro de la ligne correspondant à la position du caret. La
     * première ligne sera d'index 1.
     *
     * @return le numéro de la ligne qui contient le caret
     */
    public int getCaretLine() {
        return getLine(getCaretPosition());
    }

    /**
     * Retourne le nombre de ligne réel du composant. Comme la première ligne
     * est d'index 1, la méthode retourne également l'index de la dernière ligne
     * réel du composant.
     *
     * @return le nombre de ligne du composant
     */
    public int getNumberOfLines() {
        return getLine(htmlDoc.getLength());
    }

    /**
     * Méthode renvoyant le nombre de caractère contenu dans le document du
     * JMathTextPane.
     *
     * @return
     */
    public int getLength() {
        return htmlDoc.getLength();
    }

    /**
     * Permet de griser les JMathComponent lorsque le JMathTextPane devient
     * inactif.
     *
     * @param enabled true si le composant doit être actif, false sinon
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        activeComposants(enabled);
//            for (JMathComponent math : this.getMathComponents().values()) {
//                math.setForeground(ColorManager.get("color image disabled"));
//            }
//            majMathComponent();
    }
    
    protected void activeComposants(boolean active) {
        if (active) {
            for(Component c : componentMap.values()) {
                if(c instanceof JMathComponent) {c.setForeground(ColorManager.get("color image enabled"));}
            }
        } else {
            for(Component c : componentMap.values()) {
                if(c instanceof JMathComponent) {c.setForeground(ColorManager.get("color image disabled"));}
            }
        }
    }


    /**
     * Méthode récupérant tous les JMathComponent de l'Editeur et leur position.
     *
     * @return une Hashtable contenant comme clé la position des JMathComponent
     * dans l'éditeur et en valeur le JMathComponent.
     */
    public HashMap<Integer, JMathComponent> getMathComponents() {
        HashMap<Integer, JMathComponent> mathListe = new HashMap<Integer, JMathComponent>();
        if (htmlDoc == null) {
            return mathListe;
        }
        if (htmlDoc.getLength() <= 0) {
            return mathListe;
        }
        for (int i = 0; i < htmlDoc.getLength(); i++) {
            if (isMathComponentPosition(i)) {
                JMathComponent math = (JMathComponent) htmlDoc.getCharacterElement(i).getAttributes().getAttribute(StyleConstants.ComponentAttribute);
                mathListe.put(i, math);
            }
        }
        return mathListe;
    }

    /**
     * Méthode qui détermine s'il y a un composant quelconque sur le
     * JMathTextPane à l'emplacement placée en paramètre.
     *
     * @param pos l'emplacement où l'on souhaite faire le test.
     * @return true s'il y à un composant à cette position; false sinon.
     */
    public boolean isComponentPosition(int pos) {
        return htmlDoc.getCharacterElement(pos).getAttributes().getAttribute(COMPONENT_TYPE_ATTRIBUTE) != null;
    }
    
    /**
     * Renvoie le Component inséré à la position indiquée
     *
     * @param position l'emplacement où l'on souhaite faire le test.
     * @return le component s'il y en a un, null sinon
     */
    public Component getComponentAt(int position) {
        String spanID = (String) htmlDoc.getCharacterElement(position).getAttributes().getAttribute(COMPONENT_ID_ATTRIBUTE);
        if(spanID==null) {return null;}
        return componentMap.get(spanID);
    }

    /**
     * Méthode qui détermine s'il y a un JMathComponent sur le JMathTextPane à
     * l'emplacement placée en paramètre.
     *
     * @param pos l'emplacement où l'on souhaite faire le test.
     * @return true s'il y à un JMathComponent à cette position; false sinon.
     */
    public boolean isMathComponentPosition(int pos) {
//        return htmlDoc.getCharacterElement(pos).getAttributes().containsAttribute(COMPONENT_TYPE_ATTRIBUTE, MathTools.MATH_COMPONENT);
        return htmlDoc.getCharacterElement(pos).getAttributes().getAttribute(StyleConstants.ComponentAttribute) instanceof JMathComponent;
    }

    /**
     * Permet de récupérer un JMathComponent inséré dans un JMathTextPane à
     * partir de sa position dans le Document.
     *
     * @param pos la position où l'on souhaite récupérer le JMathComponent dans
     * le Document.
     * @return le JMathComponent inséré à la position pos ou null si aucun
     * JMathComponent n'a été inséré à cette position
     */
    public JMathComponent getMathComponent(int pos) {
        if (isMathComponentPosition(pos)) {
            return (JMathComponent) htmlDoc.getCharacterElement(pos).getAttributes().getAttribute(StyleConstants.ComponentAttribute);
        }
        return null;
    }

    public static String getSpanId(long id) {
        return id+"s";
    }
    
    /**
     * Permet de récupérer la position d'un JMathComponent dans le Document du
     * JMathTextPane.
     *
     * @param math le JMathComponent dont on souhaite déterminer la position
     * @return la position du JMathComponent sous forme d'Integer, ou -1 si le
     * JMathComponent n'est pas inséré dans le JMathTextPane
     */
    public int getMathPosition(JMathComponent math) {
        HashMap<Integer, JMathComponent> hash = getMathComponents();
        if (hash == null) {
            return -1;
        }
        for (Integer cle : hash.keySet()) {
            if (math == hash.get(cle)) {
                return cle;
            }
        }
        return -1;
    }

    public boolean hasBeenModified() { return undo.hasBeenModified(); }
    /** signale que le contenu a été modifié **/
    public void setModified(boolean b) {
        undo.setModified(b);
    }

    /**
     * Méthode calculant la largeur du contenu total
     **/
    public int getPreferredWidth() {
        try {
//            HTMLDocument.Iterator iter = htmlDoc.getIterator(HTML.Tag.BODY);
//            if(iter!=null && iter.isValid()) {
//                return getStringWidth(iter.getStartOffset(), iter.getEndOffset());
//            }
            return getStringWidth(0, getLength());
        } catch (BadLocationException ex) {
            Logger.getLogger(JMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    
    /**
     * Méthode calculant la hauteur du contenu total
     **/
    public int getPreferredHeight() {
        Element body = htmlDoc.getDefaultRootElement().getElement(htmlDoc.getDefaultRootElement().getElementCount()-1);
        return getStringHeight(body.getStartOffset(), body.getEndOffset()-1);
//        return getStringHeight(0, getLength());
    }
    
    /**
     * Méthode calculant la largeur d'une chaine de caractères contenant des
     * JMathComponents entre deux index spécifiés, en fonction de la Font passée
     * en paramètre.
     *
     * @param posStart l'index de départ de la chaine
     * @param posEnd l'index de fin de la chaine
     * @return la largeur de la chaine dans la FontMetrics spécifiés
     */
    public int getStringWidth(int posStart, int posEnd) throws BadLocationException {
        FontMetrics fm = getFontMetrics(getFont());
        int width = 0;
        // On calcule la largeur de la ligne
        for(int pos = posStart; pos<posEnd; pos++) {
            if(isComponentPosition(pos)) {
                Component c = getComponentAt(pos);
                width -= fm.stringWidth(" ");
                int cWidth = c.getWidth();
                if(cWidth==0) {cWidth = c.getPreferredSize().width;}//HACK car le composant peut mettre du temps à prendre sa taille définitive
                width += cWidth;
            } else {
                if(getText(pos, 1).equals("\n")) {
                    width += fm.stringWidth(getDocument().getText(posStart, pos - posStart));
                    return Math.max(width, getStringWidth(pos+1, posEnd));
                }
            }
        }
        return width+=fm.stringWidth(getDocument().getText(posStart, posEnd - posStart));
    }

    /**
     * Méthode calculant la hauteur maximale d'une chaine de caractères
     * contenant des JMathComponents entre deux index spécifiés, en fonction de
     * la Font passée en paramètre.
     *
     * @param posStart l'index de départ de la chaine
     * @param posEnd l'index de fin de la chaine
     * @return la hauteur de la chaine dans la FontMetrics spécifiés
     */
    public int getStringHeight(int posStart, int posEnd) {
        FontMetrics fm = getFontMetrics(getFont());
        // On initialise la hauteur sur ligne (getAscent()) et celle sous ligne (getDescent())
        double heightSup = fm.getMaxAscent(); //Hauteur sur ligne de chaque ligne du JLimitedMathTextPane
        double heightInf = fm.getMaxDescent(); //Hauteur sous ligne de chaque ligne du JLimitedMathTextPane
        double heightLead = fm.getLeading(); //Hauteur d'interligne de chaque ligne du JLimitedMathTextPane
        // S'il y a des JMathComponent, on repère leur position et on change les tableaux en conséquent

        for(int pos = posStart; pos<posEnd; pos++) {
            if(isComponentPosition(pos)) {
                Component c = getComponentAt(pos);
                int cHeight = c.getHeight();
                if(cHeight==0) {cHeight = c.getPreferredSize().height;}//HACK car le composant peut mettre du temps à prendre sa taille
                if (cHeight * c.getAlignmentY() > heightSup) {
                    heightSup = c.getHeight() * c.getAlignmentY();
                }
                if (cHeight * (1-c.getAlignmentY()) > heightInf) {
                    heightInf = c.getHeight() * (1-c.getAlignmentY());
                }
            } else {
                try {
                    if(getText(pos, 1).equals("\n")) {return (int) (heightSup + heightInf + heightLead) + getStringHeight(pos+1, posEnd);}
                } catch (BadLocationException ex) {
                    Logger.getLogger(JMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        // La hauteur finale est la somme des hauteurs sur ligne et des hauteurs sous ligne
        int height = (int) (heightSup + heightInf + heightLead);
        return height;
    }
    
    /**
     * Méthode calculant la Dimension du contenu total
     **/
    public Dimension getContentSize() {
        return new Dimension(getPreferredWidth(), getPreferredHeight());
    }
    
    /**
     * Méthode calculant la Dimension d'une chaine de caractères
     * contenant des JMathComponents entre deux index spécifiés, en fonction de
     * la Font passée en paramètre. La hauteur se calcule seulement sur une
     * seule ligne.
     *
     * @param posStart l'index de départ de la chaine
     * @param posEnd l'index de fin de la chaine
     * @return la hauteur de la chaine dans la FontMetrics spécifiés
     */
    public Dimension getStringSize(int posStart, int posEnd) {
        try {
            return new Dimension(getStringWidth(posStart, posEnd), getStringHeight(posStart, posEnd));
        } catch (BadLocationException ex) {
            Logger.getLogger(JMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Dimension(0,0);
    }
    
    /**
     * Méthode qui copie dans le presse papier la chaîne de caractère
     * sélectionnée ainsi que les composants, et coupe le texte initial.
     */
    public void couper() {
        if (this.getSelectionStart() == this.getSelectionEnd()) {
            return;
        }
        copier();
        try {
            htmlDoc.replace(this.getSelectionStart(), this.getSelectedText().length(), "", null);
            undo.valider();
        } catch (BadLocationException ex) {
            Logger.getLogger(JMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Méthode qui copie dans le presse papier la chaîne de caractère
     * sélectionnée ainsi que les composants.
     */
    public void copier() {
        if (this.getSelectionStart() == this.getSelectionEnd()) {
            return;
        }
        Transferable transfert = TransferableTools.createTransferableDataTexte(this, this.getSelectionStart(), this.getSelectedText().length());
        try {
            clipboard.setContents(transfert, null);
        } catch (IllegalStateException e1) {}
    }

    /**
     * Colle une copie du presse papier à l'emplacement passé en paramètres.
     */
    public void coller() {
        if (this.getSelectedText() != null && this.getSelectedText().length()>0) {
            undo.valider();
            try {
                htmlDoc.remove(this.getSelectionStart(), this.getSelectedText().length());
            } catch (BadLocationException ex) {
                Logger.getLogger(JMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try {
            // Cas d'un texte copié depuis MathEOS
            if(clipboard.isDataFlavorAvailable(TransferableTools.matheosFlavor)) {
                DataTexte data = (DataTexte) clipboard.getData(TransferableTools.matheosFlavor);
                EditeurIO.copy(this, data, getCaretPosition());
            // Cas d'un texte copié depuis un autre logiciel
            } else if(clipboard.isDataFlavorAvailable(TransferableTools.htmlFlavor)) {
                String html = (String) clipboard.getData(TransferableTools.htmlFlavor);
                //XXX vérifier que la régex est correct
                html = html.replaceAll("<o:p></o:p>", "");//supprime les bouses de msWord
                Document doc = Jsoup.parse(html);
                JsoupTools.removeComments(doc);
                EditeurIO.importHtml(this, doc.body().html(), getCaretPosition());
            // Cas d'un texte quelconque
            } else if(clipboard.isDataFlavorAvailable(TransferableTools.textFlavor)) {
                String content = (String) clipboard.getData(TransferableTools.textFlavor);
                htmlDoc.insertString(getCaretPosition(), content, getCharacterAttributes());
//                setCaretPosition(getCaretPosition() + content.length());//Déjà fait par insertString
            }
        } catch (UnsupportedFlavorException | IOException | BadLocationException ex) {
            Logger.getLogger(JMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Classe interne nécessaire pour régler les problèmes d'écriture avant et
     * après un JMathComponent. La classe filtre la saisie utilisateur et adapte
     * les attributs en fonction de l'état des boutons de la BarreOutils.
     */
    protected class Filtre extends DocumentFilter {
        
        private boolean flagInsert;
        private boolean flagRemove;
    
        private int lastPosition;
        
        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String str, AttributeSet attr) throws BadLocationException {
            if(!flagInsert || offset!=lastPosition) {undo.valider(); flagInsert=true;}
            flagRemove=false;
            
            MutableAttributeSet att;

            //HACK en cas d'écriture proche d'un Component. On va chercher un style qui convient
            int pos = offset;
            do {
                att = new SimpleAttributeSet(getHTMLdoc().getCharacterElement(pos).getAttributes());
                pos--;
            } while(pos>=0 && att.getAttribute(StyleConstants.ComponentAttribute)!=null);
            
            // Problème quand on souhaite écrire juste après ou juste avant un JMathComponent
//            if(att.getAttribute(StyleConstants.NameAttribute)==Tag.SPAN) {att.addAttribute(StyleConstants.NameAttribute, Tag.CONTENT);}
            
            att.removeAttribute(COMPONENT_ID_ATTRIBUTE);
            att.removeAttribute(COMPONENT_TYPE_ATTRIBUTE);
            att.removeAttribute(StyleConstants.ComponentAttribute);
            att.removeAttribute(StyleConstants.ComponentElementName);

            if(str.equals("\n") && editeurKit!=null) {//on réinitialise le style après un retour à le ligne.
                getEditeurKit().reset();
//                getInputAttributes().addAttributes(getEditeurKit().getStyleAttributes());
            }
            if(editeurKit!=null) {
                att.addAttributes(getEditeurKit().getStyleAttributes());
                StyleConstants.setForeground(att, getEditeurKit().getForeground());
            }
 
//            attrs = att.copyAttributes();

            //remplacerReturn(fb, offset, length, str, attrs);
//            remplacerEspaces(fb, offset, length, str, attrs);
            
            fb.insertString(offset, str, att);
            
            if(str.equals("\n")) {//on réinitialise le style après un retour à le ligne.
                new HTMLEditorKit.AlignmentAction("toLeft", StyleConstants.ALIGN_LEFT).actionPerformed(null);
                new HTMLEditorKit.ForegroundAction("toBlack", Color.BLACK).actionPerformed(null);
                undo.valider();
            }
            
            lastPosition = getCaretPosition();
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String str, AttributeSet attrs) throws BadLocationException {
            //XXX Attention : par défaut, dans Swing, insertString et remove appellent tous les deux replace, et non l'inverse
            if(str!=null && str.length()!=0) {//y aura-t-il une insertion ?
                if(length>0) {//il y aura aussi un remove
                    undo.valider();
                    flagRemove=true; remove(fb, offset, length);//remove sans valider le groupe d'edit
                    flagInsert=true; insertString(fb, offset, str, attrs);//insert sans valider le groupe d'edit
                    flagInsert=false; flagRemove=false;
                } else {//il s'agissait d'un insert déguisé
                    insertString(fb, offset, str, attrs);
                }
            } else {//il s'agissait d'un remove déguisé
                remove(fb, offset, length);
            }
        }
        
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if(!flagRemove || offset+length!=lastPosition) {undo.valider(); flagRemove=true;}
            flagInsert=false;
            
            for(int i = offset; i<offset+length; i++) {
                String spanID = (String) htmlDoc.getCharacterElement(i).getAttributes().getAttribute(COMPONENT_ID_ATTRIBUTE);
                if(spanID!=null) {componentMap.remove(spanID);}
            }
            super.remove(fb, offset, length);
            lastPosition = getCaretPosition();
        }

    }


    private DocumentFilter filtre = null;
    /**
     * Permet d'activer le Filtre du HTMLDocument du JMathTextPane. Si le Filtre
     * est null ou n'est pas une instance de {@link Filtre}, un nouveau est
     * créé. Si le Filtre est activée, chaque nouvel élément inséré dans le
     * HTMLDocument sera contrôlé au niveau de ses attributs.
     */
    protected void activerFiltre(boolean b) {
        if(b) {
            if(htmlDoc.getDocumentFilter()!=null) {return;}//filtre déjà installé
            if(filtre==null) {filtre = new Filtre();}
            htmlDoc.setDocumentFilter(filtre);
        } else {
            filtre = htmlDoc.getDocumentFilter();
            htmlDoc.setDocumentFilter(null);
        }
    }

    public void addParticularKeyListener(ParticularKeyListener particularKeyListener) {
        this.addKeyListener(particularKeyListener);
    }

    public void removeParticularKeyListener(ParticularKeyListener particularKeyListener) {
        this.removeKeyListener(particularKeyListener);
    }

    /** Ecoute les changement de couleur des composants **/
    private final PropertyChangeListener componentColorListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals("caretColor")) {
                for(int i=getSelectionStart(); i<getSelectionEnd(); i++) {
                    Component c = getComponentAt(i);
                    if(c!=null && c instanceof ComposantTexte) {
                        ComposantTexte label = (ComposantTexte) c;
                        label.setForeground((Color)evt.getNewValue());
                    }
                    //PENDING a supprimer après intégration des JMathComponent dans les ComposantTexte
                    else if(c!=null && c instanceof JMathComponent) {
                        JMathComponent math = (JMathComponent) c;
                        math.setForeground((Color)evt.getNewValue());
                    }
                }
            }
        }
    };
    
    /** Ecoute les changement de font-size des composants **/
    private final PropertyChangeListener componentFontSizeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals("font")) {
                for(int i=getSelectionStart(); i<getSelectionEnd(); i++) {
                    Font police = (Font)evt.getNewValue();
                    Component c = getComponentAt(i);
                    if(c!=null && c instanceof ComposantTexte) {
                        float fontSize = c.getFont().getSize2D();
                        ComposantTexte label = (ComposantTexte) c;
                        label.setFontSize(police.getSize()*fontSize);
                    }
                    //PENDING a supprimer après intégration des JMathComponent dans les ComposantTexte
                    else if(c!=null && c instanceof JMathComponent) {
                        float fontSize = c.getFont().getSize2D();
                        JMathComponent math = (JMathComponent) c;
                        math.setFontSize(fontSize * getFont().getSize());
                    }
                }
            }
        }
    };
    
//    /**
//     * Cette class permet de générer un objet qui corrigera le fontSize et la couleur des JMathComponent
//     * insérés dans le JMathTextPane lorsque les attributs du document sont modifiés.
//     */
//    public class MathStyleChangeListener implements DocumentListener {
//        public void insertUpdate(DocumentEvent e) {}
//        public void removeUpdate(DocumentEvent e) {}
//        public void changedUpdate(DocumentEvent e) {
//            for(int i=e.getOffset(); i<e.getOffset()+e.getLength(); i++) {
//                JMathComponent math = getMathComponent(i);
//                if(math!=null) {
//                    MutableAttributeSet attr = new SimpleAttributeSet(getCharacterAttributes());//XXX vérifier qu'il ne faut pas prendre les attributs du charactère i-1
//                    if(editeurKit!=null) {attr.addAttributes(editeurKit.getStyleAttributes());}
//                    if(math.getForeground() != StyleConstants.getForeground(attr)) {
//                        math.setForeground(StyleConstants.getForeground(attr));
//                    }
//                    int fontSize = StyleConstants.getFontSize(attr);
//                    float size = (float) Math.sqrt(fontSize * getFont().getSize());//XXX d'où sort cette formule ?
//                    if ((int) math.getFontSize() != size) { math.setFontSize(size); }
//                }
//            }
//            revalidate();
//            repaint();
//        }
//    }

    private class MiseAJourStyles implements CaretListener {
        private boolean peutCouper = false;
        private boolean peutCopier = false;

        @Override
        public void caretUpdate(CaretEvent e) {
            int referentCharacter = e.getDot();
            try {
                if(referentCharacter>0 && !getText(referentCharacter-1, 1).equals("\n")) {referentCharacter--;}
            } catch (BadLocationException ex) {
                Logger.getLogger(JMathTextPane.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            AttributeSet ast = htmlDoc.getCharacterElement(referentCharacter).getAttributes();
            AttributeSet astParagraphe = htmlDoc.getParagraphElement(e.getDot()).getAttributes();
            
            //met à jour la couleur du caret
            removePropertyChangeListener("caretColor", componentColorListener);
            setCaretColor(StyleConstants.getForeground(ast));
            addPropertyChangeListener("caretColor", componentColorListener);
            
            if(editeurKit!=null) {editeurKit.updateBoutons(astParagraphe, ast);}
            if(peutCouper!=peutCouper()) {
                peutCouper = peutCouper();
                JMathTextPane.this.firePropertyChange(Editable.PEUT_COUPER, !peutCouper, peutCouper);
            }
            if(peutCopier!=peutCopier()) {
                peutCopier = peutCopier();
                JMathTextPane.this.firePropertyChange(Editable.PEUT_COPIER, !peutCopier, peutCopier);
            }
            //IHM.activeCouperCopier(getSelectedText() != null);
        }
    }
    
    private class UndoableStateListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JMathTextPane.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    public boolean removeHead() {
        try {
            javax.swing.text.Element head = htmlDoc.getDefaultRootElement().getElement(0);
            if(head.getName().equals("head")) {htmlDoc.remove(0, head.getEndOffset());return true;}
        } catch (BadLocationException ex) {
            Logger.getLogger(EditeurIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private void removeBinding(int keyCode, int modifiers) {
        InputMap map = getInputMap();
        KeyStroke key = KeyStroke.getKeyStroke(keyCode, modifiers);
        while(map!=null && map.get(key)!=null) {
            map.remove(key);
            map = map.getParent();
        }
    }
    
    /**
     * Récupère les données Sérialisables nécessaires pour recréer le Traitement de texte.
     * @return un objet Sérializable Data contenant les informations à enregistrer
     */
    public DataTexte getDonnees() {
        return EditeurIO.write(this);
    }
    
    /**
     * Charge les données depuis le DataTexte et remplace le contenu actuel par celui lue dans le DataTexte.
     * @param data les données à charger
     */
    @Override
    public void charger(Data data) {
        clear();
        DataTexte dataTexte;
        if(data instanceof DataTexte) {dataTexte = (DataTexte) data;}
        else {dataTexte = new DataTexte("");dataTexte.putAll(data);}
        EditeurIO.read(this, dataTexte);
    }
    
    public void clear() {
        this.removeAll();
        componentMap.clear();
        try {
            htmlDoc.remove(0, htmlDoc.getLength());
        } catch (BadLocationException ex) {
            Logger.getLogger(Editeur.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void setBorder(Border border) {
        //HACK : mettre une border écrase les marges. La solution est d'utiliser une CompoundBorder
        Insets insets = getMargin();
        if(insets==null) {super.setBorder(border);return;}
        Border margin = BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right);
        super.setBorder(BorderFactory.createCompoundBorder(border, margin));
    }

}
