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

import matheos.Main;
import matheos.texte.composants.JLabelTP;
import matheos.texte.composants.JLabelText;
import matheos.texte.composants.JLabelImage;
import matheos.texte.Editeur;
import matheos.sauvegarde.DataTexte;
import matheos.texte.composants.ComposantTexte;
import matheos.utils.librairies.JsoupTools;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.CursorManager;
import java.awt.Component;
import java.awt.Cursor;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import net.sourceforge.jeuclid.swing.JMathComponent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author François Billioud
 */
public abstract class EditeurIO {

    private static final String CLASS_MathEOS = JMathTextPane.MathEOS_COMPONENT;
    
    /** Transforme le contenu en un objet DataTexte permettant de reconstruire les données créées avec MathEOS.
     * Les composants sont convertis en html pur ou en mix html/donnée. Les données sont enregistrées dans une
     * map et identifiées par l'id du composant.
     */
    private static DataTexte genererDonneesMathEOS(String html, Map<String, Component> componentMap) {
        DataTexte donnees = new DataTexte(html);
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select("span."+CLASS_MathEOS);
        for(Element e : elements) {
            String id = e.attr("id");
            Component c = componentMap.get(id);
            String htmlElement;
            if(c instanceof JMathComponent) {htmlElement = MathTools.getHTMLRepresentation((JMathComponent)c); }
            else {
                if(c instanceof ComposantTexte) {
                    if(c instanceof JLabelImage) {donnees.putImage(id, ((JLabelImage)c).getImageInitiale());}//information impossible à insérer dans le html
                    if(c instanceof JLabelTP) {donnees.putTP(id, ((JLabelTP)c).getDataTP());}//XXX a supprimer si on choisit d'insérer les données dans le html
                    htmlElement = ((ComposantTexte)c).getHTMLRepresentation();
                }
                else {htmlElement = " ";System.out.println("composant non trouvé!");}
            }
            e.html(htmlElement);
        }
        donnees.setContenuHTML(isFullDocument(donnees.getContenuHTML()) ? doc.html() : doc.body().html());//corrigerAttributs(corrigerFont(doc.body().html()));
        //correction des styles :
//        donnees.contenuHTML = HTML3toHTML5Attributs(donnees.contenuHTML);
        return donnees;
    }
    
    /** renvoie une chaine html brute contenant du mathML. Les spans matheos sont supprimés et les JLabelImage sont changés en images. **/
    private static String convertirEnHtmlMathML(DataTexte dataTexte) {
        //on va simplement supprimer les tags span générés par matheos
        Document doc = Jsoup.parse(dataTexte.getContenuHTML());
        Elements spanTags = doc.select("span."+CLASS_MathEOS);
        for(Element e : spanTags) {e.unwrap();}
        return isFullDocument(dataTexte.getContenuHTML()) ? doc.html() : doc.body().html();
    }

    /** renvoie une chaine html conforme au html5. les composants deviennent des images **/
    private static String convertirEnHtml5(DataTexte dataTexte, Map<String, Component> componentMap) {
        //transforme tous les composants en image
        Document doc = Jsoup.parse(dataTexte.getContenuHTML());
        Elements elements = doc.select("span."+MathTools.MATH_COMPONENT+"."+CLASS_MathEOS);
        for(Element e : elements) {
            String id = e.attr("id");
            Component c = componentMap.get(id);
            if(c instanceof JMathComponent) {
                JMathComponent mathComp = (JMathComponent)c;
                JLabelImage labelImage = new JLabelImage(MathTools.capturerImage(mathComp), mathComp.getHeight());
                labelImage.setId(MathTools.getId(mathComp));
                e.html(labelImage.getHTMLRepresentation());
                e.unwrap();
            } else {
                System.out.println("composant non trouvé! "+e.outerHtml());
            }
        }
        Elements spanTags = doc.select("span."+CLASS_MathEOS);
        for(Element e : spanTags) {e.unwrap();}//supprime les spans
        String html = isFullDocument(dataTexte.getContenuHTML()) ? doc.html() : doc.body().html();

        //remplace les espaces
        html = html.replace("&nbsp;", " ");

        //remplace les balises font par leur équivalent HTML 5
        return HTML3toHTML5Attributs(html);
//        return html;
    }
    
    private static boolean isFullDocument(String html) {return html.contains("<body");}

    /** lis les données MathEOS contenues dans le DataTexte et restitue le contenu de l'éditeur **/
    private static void insererDonneesMathEOS(JMathTextPane jtp, DataTexte donnees, int offset, boolean copie) {
        int tailleInitiale = jtp.getLength();
        long tempId = System.currentTimeMillis();//va servir pour les éléments créés durant le processus
        boolean isFullDocument = isFullDocument(donnees.getContenuHTML());//définit le type d'import à effectuer : total ou partiel
        
        //analyse des styles afin de forcer leur assimilation correcte par le jtp
//        donnees.contenuHTML = HTML5toHTML3Attributs(donnees.contenuHTML);
        
        //parsing du html
        Document doc = Jsoup.parse(donnees.getContenuHTML());
        
        //capture des composants matheos en vue de leur insertion
        Elements elements = doc.select("span."+CLASS_MathEOS);
        
        //HACK : on enregistre le innerHTML des spans matheos car le HTMLDocument a tendance à
        //transformer <span id='X'><span id='Y'>content</span></span> en <span id='Y'>content</span>
        HashMap<String, String> componentHTML = new HashMap<>();
        for(Element e : elements) {
            //en cas de copie, on doit donner un nouvel id à ces éléments et enregistrer l'ancien pour retrouver les données
            if(copie) {
                long newId = tempId++;
                e.attr("oldId",e.attr("id"));
                e.attr("id",newId+"s");
                Element innerSpan = e.children().first();
                if(innerSpan==null) {System.out.println("element vide : "+e.outerHtml());}
                else {innerSpan.attr("id", newId+"");}
            }
            componentHTML.put(e.attr("id"),e.html());
            e.html("&nbsp;");//le noeud ne doit pas être vide sous peine d'être supprimé par le HTMLDocument
        }
        
        //HACK : on supprime les title des paragraphes sinon ils sont dupliqués
        doc.select("p").removeAttr("id");
        
        //HACK : on enregistre les styles dans une map car sinon ils ne sont pas modifiables
        HashMap<String, String> stylesMap = new HashMap<>();
        for(Element e : doc.getAllElements()) {
            
            //Les objets MathEOS ont un traitement séparé qui est fait par ailleurs
            if(e.hasClass(CLASS_MathEOS)) {continue;}
            
            if(copie) {e.removeAttr("id");}//Pour ne pas dupliquer les ids
            String size = JsoupTools.getStyle(e, "size");
            String color = JsoupTools.getStyle(e, "color");
            String align = JsoupTools.getStyle(e, "text-align");
            //si aucun style posant problème, on laisse tomber
            String stylesToCorrect = "";
            if(!size.isEmpty()) {stylesToCorrect+="size:"+size+";";}
            if(!color.isEmpty()) {stylesToCorrect+="color:"+color+";";}
            if(!align.isEmpty()) {stylesToCorrect+="text-align:"+align+";";}
            if(stylesToCorrect.isEmpty()) {continue;}
            //sinon, on enregistre l'élément
            String id = e.attr("id");
            if(id.isEmpty()) {
                id="temp"+(tempId++);
                e.attr("id",id);
            }
            stylesMap.put(id,stylesToCorrect);
            //on efface les styles
            JsoupTools.setStyleAttribute(e, "size", null);
            JsoupTools.setStyleAttribute(e, "color", null);
            JsoupTools.setStyleAttribute(e, "text-align", null);
        }
        
        //insert le html
        jtp.removeHead();//HACK permettant d'assurer que le body commence à la position 0
        
        if(copie) {copyHTMLContent(jtp, isFullDocument ? doc.html() : doc.body().html(), offset);}
        else {insertHTMLContent(jtp, isFullDocument ? doc.html() : doc.body().html(), offset);}
        
        //fait le lien entre le html, le htmlDoc, et les composants
        for(Element e : elements) {
            String id = e.attr("id");//l'id de l'élément considéré
            String sourceId = id;//en cas de copie, l'id de l'élément source
            if(copie) {sourceId = e.attr("oldId");e.removeAttr("oldId");}
            javax.swing.text.Element element = jtp.getHTMLdoc().getElement(id);
            
            if(element==null) {System.out.println("le chargement du component : "+id+" a échoué");Main.erreurDetectee(null, id);}
            else {
                //On simule un insert classique. Les anciennes balises sont supprimées
                int position = element.getStartOffset();
                jtp.getHTMLdoc().removeElement(element);
                jtp.setCaretPosition(position);
                
                //Si le jtp accepte les labels, on insère le composant
                if(Editeur.class.isInstance(jtp)) {
                    Editeur editeur = (Editeur) jtp;
                    if(e.hasClass(JLabelImage.JLABEL_IMAGE)) {
                        try {
                            JLabelImage image = JLabelImage.creerJLabelImageFromHTML(componentHTML.get(id), donnees.getImage(sourceId));
//                            if(copie) {image.setId(tempId++);}
                            editeur.insererImage(image);
                        } catch(Exception ex) {Main.erreurDetectee(ex, id);e.unwrap();}
                    } else {
                        if(e.hasClass(JLabelTP.JLABEL_TP)) {
                            try {
                                //XXX Envisager de charger le tp en tant qu'image en cas de pb de chargement
                                JLabelTP tp = JLabelTP.creerJLabelTPFromHTML(componentHTML.get(id), donnees.getTP(sourceId), donnees.getImage(sourceId));
//                                if(copie) {tp.setId(tempId++);}
                                editeur.insererTP(tp);
                            } catch(Exception ex) {Main.erreurDetectee(ex, id);e.unwrap();}
                        } else {
                            if(e.hasClass(JLabelText.JLABEL_TEXTE)) {
                                try {
                                    JLabelText text = JLabelText.creerJLabelTextFromHTML(componentHTML.get(id));
                                    if(copie) {
//                                        text.setId(tempId++);
                                        text.setRemovable(true);//HACK pour éviter de copier des éléments non suppressibles
                                    }
                                    editeur.insererLabel(text);
                                } catch(Exception ex) {Main.erreurDetectee(ex, id);e.unwrap();}
                            }
                        }
                    }
                } else {//Sinon on supprime le noeud
                    //XXX pas idéal comme façon de déterminer si le neud est de type Label
                    //TODO ajouter un label global pour les JLabel
                    if(e.hasClass(JLabelImage.JLABEL_IMAGE) || e.hasClass(JLabelTP.JLABEL_TP) || e.hasClass(JLabelText.JLABEL_TEXTE)) {
                        e.remove();
                    }
                }
                //s'il s'agit d'un JMathComponent :
                if(e.hasClass(MathTools.MATH_COMPONENT)) {
                    try {
                        JMathComponent math = MathTools.creerJMathComponentFromHTML(componentHTML.get(id));
//                        if(copie) {MathTools.setId(math, tempId++);}
                        jtp.insererJMathComponent(math);
                    } catch(Exception ex) {Main.erreurDetectee(ex, id);e.unwrap();}
                }
            }
        }
        jtp.removeHead();//HACK permettant d'assurer que le body commence à la position 0
        
        //HACK suite : applique les styles manuellement
        HTMLDocument htmlDoc = jtp.getHTMLdoc();
        for(Entry<String, String> styleCorrectionEntry : stylesMap.entrySet()) {
            //prépare les styles à ajouter
            String id = styleCorrectionEntry.getKey();
            String styleCorrection = styleCorrectionEntry.getValue();//la chaine du type 'size:5;color:#000000;'
            javax.swing.text.Element elt = htmlDoc.getElement(id);
            if(elt==null) {
                Element e = doc.getElementById(id);
                //le JTextPane rajoute parfois des balises vides inutiles. On ne fait pas remonter ce problème.
                if(!e.tagName().equals("font")) {
                    System.out.println("empty element : "+e.outerHtml());
                }
                continue;
            }
            Map<String, String> stylesElt = JsoupTools.getStyleMap(styleCorrection);
            MutableAttributeSet attributeSet = new SimpleAttributeSet();
            if(stylesElt.containsKey("size")) {StyleConstants.setFontSize(attributeSet, JsoupTools.convertFontSize2PT(stylesElt.get("size")));}
            if(stylesElt.containsKey("text-align")) {StyleConstants.setAlignment(attributeSet, (int)JsoupTools.CSSToJavaValue.get(stylesElt.get("text-align")));}
            if(stylesElt.containsKey("color")) {StyleConstants.setForeground(attributeSet, ColorManager.getColorFromHexa(stylesElt.get("color")));}
            
            if(elt.getName().equals("p")) {
                htmlDoc.setParagraphAttributes(elt.getStartOffset(), elt.getEndOffset()-elt.getStartOffset()-1, attributeSet, false);
            } else {
                htmlDoc.setCharacterAttributes(elt.getStartOffset(), elt.getEndOffset()-elt.getStartOffset(), attributeSet, false);
            }
        }
        
        //spécifie les font-size des éléments du jtp
//        applyFontSizeCorrection(jtp.getHTMLdoc().getDefaultRootElement(), jtp.getHTMLdoc(), 12);//EditeurKit.TAILLES_PT[0]);
        
        //place le caret à la fin de l'insertion
        int tailleModifiee = jtp.getLength();
        jtp.setCaretPosition(offset+tailleModifiee-tailleInitiale);
    }
    
    private static void applyFontSizeCorrection(javax.swing.text.Element e, HTMLDocument doc, int size) {
        int definitiveSize = (e.getAttributes().getAttribute(StyleConstants.FontSize)!=null) ? (int)e.getAttributes().getAttribute(StyleConstants.FontSize) : size;
        
        if(e.isLeaf()) {
            MutableAttributeSet set = new SimpleAttributeSet();
            StyleConstants.setFontSize(set, definitiveSize);
            doc.setCharacterAttributes(e.getStartOffset(), e.getEndOffset()-e.getStartOffset(), set, false);
        } else {
            for(int i = 0; i<e.getElementCount(); i++) {
                applyFontSizeCorrection(e.getElement(i),doc,definitiveSize);
            }
        }
    }
    
    
    /** Permet de lire les données depuis un logiciel quelconque, ou depuis le web. Charge les images et les objets MathML **/
    public static void importHtmlMathML(JMathTextPane jtp, String html, int offset) {
        //lecture du html et capture des composants
        Document doc = Jsoup.parse(html);
        DataTexte donnees = new DataTexte("");
        
        //on supprime les balises matheos qui pourraient gêner si elles étaient présentes
        for(Element e : doc.select("span."+CLASS_MathEOS)) { e.unwrap(); }
        
        //repère les images si le jtp est de type Editeur
        if(Editeur.class.isInstance(jtp)) {
            for(Element e : doc.select("img")) {
                JLabelImage image = JLabelImage.creerJLabelImageFromHTML(e.outerHtml());
                String spanID = JMathTextPane.getSpanId(image.getId());
                donnees.putImage(spanID, image.getImageInitiale());
//                e.attr("id", image.getId()+"");
                e.html("<span id='"+spanID+"' class='"+JLabelImage.JLABEL_IMAGE+" "+CLASS_MathEOS+"'>"+image.getHTMLRepresentation()+"</span>");
                e.unwrap();
            }
        } else {
            for(Element e : doc.select("img")) {e.unwrap();}//on enlève les images
        }
        
        //repère le mathML
        for(Element elt : doc.select("math")) {
            Element e = elt;//Evite de modifier l'objet de boucle
            if(e.parent().hasClass(MathTools.MATH_COMPONENT)) {e = e.parent();}
            JMathComponent math = MathTools.creerJMathComponentFromHTML(e.outerHtml());
            String spanID = JMathTextPane.getSpanId(MathTools.getId(math));
//            e.attr("id", MathTools.getId(math)+"");
            e.html("<span id='"+spanID+"' class='"+MathTools.MATH_COMPONENT+" "+CLASS_MathEOS+"'>"+MathTools.getHTMLRepresentation(math)+"</span>");
            e.unwrap();
        }
        
        //insert le html mis en forme
        donnees.setContenuHTML(isFullDocument(html) ? doc.html() : doc.body().html());
        
        //on traite à présent le problème comme un import matheos
        read(jtp, donnees, offset);
    }
    
    /** Permet de lire les données depuis un logiciel quelconque, ou depuis le web. Charge les images et les objets MathML **/
    public static void importHtml(JMathTextPane jtp, String html, int offset) {
        //TODO envisager d'autre format pour les objets mathématiques et les convertir ici en MathML
        importHtmlMathML(jtp, html, offset);
    }
    
    /**
     * Permet de charger le contenu d'un élément DataTexte dans un JMathTextPane
     * @param jtp l'éditeur de texte cible
     * @param donnees les données à charger
     */
    public static void read(JMathTextPane jtp, DataTexte donnees) {
        read(jtp, donnees, 0);
    }
    
    /**
     * Permet d'insérer le contenu d'un élément DataTexte dans un JMathTextPane
     * @param jtp l'éditeur de texte cible
     * @param donnees les données à charger
     * @param offset la position où insérer le contenu
     */
    public static void read(JMathTextPane jtp, DataTexte donnees, int offset) {
        Cursor previous = jtp.getCursor();
        jtp.setCursor(CursorManager.getCursor(Cursor.WAIT_CURSOR));
        insererDonneesMathEOS(jtp, donnees, offset, false);
        jtp.setCursor(previous==null ? CursorManager.getCursor(Cursor.TEXT_CURSOR) : previous);
        jtp.repaint();
    }

    /**
     * Permet de copier le contenu d'un élément DataTexte dans un JMathTextPane
     * @param jtp l'éditeur de texte cible
     * @param donnees les données à copier
     * @param offset la position où insérer le contenu
     */
    public static void copy(JMathTextPane jtp, DataTexte donnees, int offset) {
        insererDonneesMathEOS(jtp, donnees, offset, true);
    }

    /**
     * Renvoie le contenu du htmlDoc au format html. Les Components insérés dans le document ne sont pas convertis.
     * @param htmlDoc le HTMLdocument à lire
     * @return le html du document
     */
    private static String getHTMLContent(HTMLDocument htmlDoc) {
        return getHTMLContent(htmlDoc, 0, htmlDoc.getLength());
    }

    private static void copyHTMLContent(JMathTextPane jtp, String html, int offset) {
        try {
            boolean start = offset>0 ? !jtp.getText(offset-1, 1).equals("\n") : false;
            Position p = jtp.getHTMLdoc().createPosition(offset);
            insertHTMLContent(jtp, html, offset);
            if(start) {jtp.getHTMLdoc().remove(offset, 1);}
            if(offset>0) {jtp.getHTMLdoc().remove(p.getOffset()-1, 1);}
        } catch (BadLocationException ex) {
            Logger.getLogger(EditeurIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Insert le contenu html dans le htmlDoc à la position indiquée. Les Components insérés dans le document ne sont pas convertis.
     * @param htmlDoc le HTMLdocument où écrire
     */
    private static void insertHTMLContent(JMathTextPane jtp, String html, int offset) {
        HTMLEditorKit editorKit = new HTMLEditorKit();
        try {
            editorKit.read(new StringReader(html), jtp.getHTMLdoc(), offset);
        } catch (IOException | BadLocationException ex) {
            Logger.getLogger(EditeurIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Renvoie le contenu du htmlDoc entre les positions startOffset et startOffset+length au format html.
     * Les Components insérés dans le document ne sont pas convertis.
     * Les espaces consécutifs sont remplacés à ce moment là par des &nbsp; entre span.
     * @param htmlDoc le HTMLdocument à lire
     * @return le html du document
     */
    private static String getHTMLContent(HTMLDocument htmlDoc, int startOffset, int length) {
        StringWriter writer = new StringWriter();
        try {
//            new HTMLPerfectWriter(writer, htmlDoc, startOffset, length).write();
            new HTMLEditorKit().write(writer, htmlDoc, startOffset, length);
        } catch (IOException | BadLocationException ex) {
            Logger.getLogger(Editeur.class.getName()).log(Level.SEVERE, null, ex);
        }
        //retourne le document complet ou la partie copiée
        String html = writer.toString();
//        System.out.println(html);
        html = html.replaceAll("\t", "&#9;");
        Document doc = Jsoup.parse(html);
        return (startOffset==0 && htmlDoc.getLength()==length) ? doc.html() : doc.body().html();
    }
    
    /**
     * Transforme le contenu du HTMLDocument en données MathEOS
     * Transforme le contenu du JMathTextPane en données MathEOS
     * @param jtp le JMathTextPane à convertir
     * @return un objet DataTexte permettant de reconstituer le contenu du HTMLDocument
     */
    public static DataTexte write(JMathTextPane jtp) {
        return write(jtp, 0, jtp.getLength());
    }
    
    /**
     * Transforme le contenu du JMathTextPane en données MathEOS
     * @param jtp le JMathTextPane à convertir
     * @param startOffset le point de départ de la conversion
     * @param length la longueur de la chaîne à convertir
     * @return un objet DataTexte permettant de reconstituer le contenu du HTMLDocument
     */
    public static DataTexte write(JMathTextPane jtp, int startOffset, int length) {
        Cursor previous = jtp.getCursor();
        jtp.setCursor(CursorManager.getCursor(Cursor.WAIT_CURSOR));
        String html = getHTMLContent(jtp.getHTMLdoc(), startOffset, length);
        DataTexte data = genererDonneesMathEOS(html, jtp.getComponentMap());
        jtp.setCursor(previous==null ? CursorManager.getCursor(Cursor.TEXT_CURSOR) : previous);
        return data;
    }
    
    private static String HTML5toHTML3Attributs(String html) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getAllElements();
        for(Element elt : elements) {
            if(elt.hasClass("font-correction")) {
                if(elt.tagName().equals("span")) {elt.tagName("font");}
                else {
                    Element font = Jsoup.parse("<font></font>").body().children().first();
                    font.attr("size", elt.attr("font-size"));
                    font.attr("color", elt.attr("color"));
                    elt.html(font.html(elt.html()).outerHtml());
                }
                elt.removeClass("font-correction");
            }
            JsoupTools.convertStylesToHTMLAttribute(elt);
        }
        return doc.html();
    }
    private static String HTML3toHTML5Attributs(String html) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.getAllElements();
        allElements:
        for(Element elt : elements) {
            Element toCorrect = elt;
            if(elt.tagName().equals("font")) {
                Element parent = elt.parent();
                if(elt.children().size()==1) {
                    toCorrect = elt.child(0);
                } else if(parent.tagName().equals("p") && parent.children().size()==1) {
                    toCorrect = parent;
                } else {elt.tagName("span");elt.addClass("font-correction");}//on transforme le font en span
                if(elt!=toCorrect) {
                    if(toCorrect.attr("size").isEmpty() && !elt.attr("size").isEmpty() && toCorrect!=parent) {toCorrect.attr("size",elt.attr("size"));}//le font-size sera corrigé plus tard
                    if(toCorrect.attr("color").isEmpty() && !elt.attr("color").isEmpty()) {toCorrect.attr("color",elt.attr("color"));}
                    elt.unwrap();
                }
            }
            JsoupTools.convertAttributesToCSS(toCorrect);
        }
        return isFullDocument(html) ? doc.html() : doc.body().html();
    }
    
//    private static List<Object> convertToWord(HTMLDocument htmlDoc, Map<String, Component> componentMap, int startOffset, int length, File fichierDeDestination) {
//        //sert juste à éviter des alertes inutiles
//        System.setProperty("log4j.defaultInitOverride","tr ue");
//        LogManager.resetConfiguration();
//        LogManager.getRootLogger().addAppender(new NullAppender());
//
//        //Remplace les Composants par leurs images
//        String html = exporterEnHtml5(getHTMLContent(htmlDoc, startOffset, length), componentMap);
//
//        //génération du .docx
//        WordprocessingMLPackage wordMLPackage;
//        List<Object> msWordResult = new LinkedList<Object>();
//        try {
//            wordMLPackage = WordprocessingMLPackage.createPackage();
//
//            NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
//            wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
//            ndp.unmarshalDefaultNumbering();
//
//            msWordResult = XHTMLImporter.convert( html, null, wordMLPackage);
//            wordMLPackage.getMainDocumentPart().getContent().addAll( msWordResult );
//            if(fichierDeDestination!=null) {wordMLPackage.save(fichierDeDestination);}
//        } catch (Docx4JException ex) {
//            Logger.getLogger(IHM.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (JAXBException ex) {
//            Logger.getLogger(IHM.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return msWordResult;
//    }

    /** enregistre le contenu du htmlDoc dans un fichier .docx
     * Les Composants sont convertis en images
     * @param htmlDoc le document à convertir
     * @param componentMap la map des composants par id contenus dans le document
     * @param fichierDeDestination le fichier .docx où enregistrer les données
     */
//    public static void export2Docx(HTMLDocument htmlDoc, Map<String, Component> componentMap, File fichierDeDestination) {
//        convertToWord(htmlDoc, componentMap, 0, htmlDoc.getLength(), fichierDeDestination);
//    }

    /** convertit la sélection au format html5 pour un éventuel copier/coller **/
    public static String export2html5(DataTexte dataTexte, Map<String, Component> componentMap) {
        return convertirEnHtml5(dataTexte, componentMap);
    }
    
    public static String toHTML5(DataTexte dataTexte) {
        return HTML3toHTML5Attributs(dataTexte.getContenuHTML());
    }

    /** convertit la sélection au format html + mathML pour un éventuel copier/coller **/
    public static String export2htmlMathML(DataTexte dataTexte) {
        return convertirEnHtmlMathML(dataTexte);
    }
    
    /** convertit la sélection au format html + mathML pour un éventuel copier/coller **/
    public static String export2htmlMathML(JMathTextPane jtp, int startOffset, int length) {
        DataTexte data = write(jtp, startOffset, length);
        return convertirEnHtmlMathML(data);
    }

//    /** convertit la sélection au format word pour un éventuel copier/coller **/
//    public static List<Object> export2Docx(HTMLDocument htmlDoc, Map<String, Component> componentMap, int startOffset, int length) {
//        return convertToWord(htmlDoc, componentMap, startOffset, length, null);
//    }

    private EditeurIO() { throw new AssertionError("Instanciating utility class");}

}
