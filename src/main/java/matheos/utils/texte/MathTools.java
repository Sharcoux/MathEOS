/** «Copyright 2013 François Billioud, Guillaume Varoquaux»
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.sourceforge.jeuclid.context.Parameter;
import net.sourceforge.jeuclid.swing.JMathComponent;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import matheos.arevoir.MathAlignementHandler;
import matheos.utils.managers.ColorManager;
import matheos.utils.librairies.JsoupTools;
import matheos.utils.dialogue.DialogueMathAlignement;
import matheos.utils.dialogue.math.DialogueMath;
import matheos.utils.dialogue.math.DialogueMathChapeauAngle;
import matheos.utils.dialogue.math.DialogueMathEquation;
import matheos.utils.dialogue.math.DialogueMathExposant;
import matheos.utils.dialogue.math.DialogueMathFraction;
import matheos.utils.dialogue.math.DialogueMathIndice;
import matheos.utils.dialogue.math.DialogueMathParentheseLarge;
import matheos.utils.dialogue.math.DialogueMathRacineCarree;
import matheos.utils.dialogue.math.DialogueMathSysteme;
import matheos.utils.managers.CursorManager;
import matheos.utils.managers.FontManager;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Guillaume Varoquaux, François Billioud
 */
public abstract class MathTools {

    /** balise utilisée pour reconnaître les JMathComponent **/
    public static final String MATH_COMPONENT = "jMathComponent";

    public static final String ALIGNMENT_Y_PROPERTY = "alignementY";
    private static final Font POLICE = FontManager.get("font math component");

    /**
     * Méthode permettant de créer un nouveau JMathComponent à partir de la
     * chaine MathMl.
     *
     * @param chaine la chaine MathMl du nouveau JMathComponent
     * @return Un nouveau JMathComponent construit à partir de la chaine placée
     * en paramètre.
     */
    private static JMathComponent creerNouveauMathComponent(String chaine) {
        final JMathComponent math = new JMathComponent();
        math.setParameter(Parameter.MFRAC_KEEP_SCRIPTLEVEL, true);
        System.out.println("chaine : "+chaine);
        math.setContent(chaine);
        math.setBackground(ColorManager.transparent());
        math.setCursor(CursorManager.getCursor(Cursor.TEXT_CURSOR));

        math.setFont(POLICE);

        math.setName(""+System.currentTimeMillis());//sert d'ID pour le MathComponent
        //permet de changer la couleur d'un composant sélectionné
        math.addPropertyChangeListener(Parameter.MATHCOLOR.name(), new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(Parameter.MATHCOLOR.name()) && isSelected(math)) {
                    foregroundColors.put(math, (Color) evt.getNewValue());
                }
            }
        });
        return math;
    }

    public static JMathComponent creerMathComponent(String chaineMathML) {
        String chaine = chaineMathML;
        /**if (chaine.contains("<?xml") && chaine.contains("?>")) {
            String chaineAux = chaine.substring(chaine.indexOf("<?xml") + 5, chaine.indexOf("?>"));
            chaineAux = "<\\?xml" + chaineAux + "\\?>";
            if (chaineAux != null) {
                chaine = chaine.replaceAll(chaineAux, "");
            }
        }**/
        //On corrige la chaine
//        chaine = chaine.replaceAll("&times;", "<mo>&#x000d7;</mo>");//JMathComponent ne lit pas le HTML
//        chaine = chaine.replaceAll("&divide;", "<mo>&#x000f7;</mo>");//JMathComponent ne lit pas le HTML
        chaine = chaine.replaceAll("&times;", "&#x000d7;");//JMathComponent ne lit pas le HTML
        chaine = chaine.replaceAll("&divide;", "&#x000f7;");//JMathComponent ne lit pas le HTML
        chaine = chaine.replaceAll("&plusmn;", "&#177;");//JMathComponent ne lit pas le HTML
        chaine = chaine.replaceAll("\n", "");//JMathComponent ne lit pas les \n (JMathComponent c'est un peu de la merde...)
        System.out.println(chaine.replaceAll("<\\?xml(.)*\\?>", "").replaceAll("<math>|</math>",""));
        JMathComponent math = creerNouveauMathComponent(chaine.replaceAll("<\\?xml(.)*\\?>", "").replaceAll("<math>|</math>",""));
        
        FontMetrics fm = math.getFontMetrics(math.getFont());
        math.setAlignmentY(calculateMathAlignment(chaine, fm));

        return math;
    }
    
    /**
     * Méthode calculant l'alignement d'un JMathComponent à partir de sa chaine.
     * Il s'agit de l'alignement correct que doit avoir le JMathComponent dans
     * le JTextPane qui le contiendra. Le paramètre retourné doit donc être
     * placé en paramètre de la méthode setAlignementY() du JMathComponent.
     *
     * @param content la chaine dont on veut déterminer l'alignement.
     * @return Un float ayant pour valeur l'alignement Y déterminé.
     */
    public static float calculateMathAlignment(String content, FontMetrics fontMetrics) {
        float alignement = 1;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parseur;
        try {
            parseur = factory.newDocumentBuilder();
            Reader reader = new StringReader(content);
            InputSource is = new InputSource(reader);
            MathAlignementHandler mah = new MathAlignementHandler();
            org.w3c.dom.Document doc = parseur.parse(is);
            alignement = mah.parse(doc, fontMetrics);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Logger.getLogger(MathTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return alignement;
    }

    /**
     * Méthode permettant de déterminer le parent du JMathComponent s'il y en a
     * un.
     *
     * @param math le JMathComponent donc on veut déterminer le parent.
     * @return Le Container parent du JmathComponent ou null s'il n'en a pas.
     */
//    public static Container getParent(JMathComponent math) {
//        if (math.getParent() != null) {
//            if (math.getParent().getParent() != null) {
//                return math.getParent().getParent();
//            }
//        }
//        return null;
//    }

    /**
     * Méthode permettant de créer une copie paramètre par paramètre du
     * JMathComponent placé en paramètre.
     *
     * @param math le JMathComponent à copier.
     * @return Un nouveau JMathComponent, copie de celui placé en paramètre.
     */
    public static JMathComponent copyMathComponent(JMathComponent math) {
        JMathComponent nouveauMath = creerJMathComponentFromHTML(getHTMLRepresentation(math));
        if(isSelected(math)) {
            nouveauMath.setForeground(foregroundColors.get(math));
        } else {
            nouveauMath.setForeground(math.getForeground());
        }
        return nouveauMath;
    }

//    public static AttributeSet mathToModel(JMathComponent math) {
//        MutableAttributeSet attributes = new SimpleAttributeSet();
//        attributes.addAttribute(MATH_CONTENT, math.getContent());
//        attributes.addAttribute(MATH_SIZE, math.getFontSize());
//        attributes.addAttribute(MATH_ALIGNMENT_X, math.getAlignmentX());
//        attributes.addAttribute(MATH_ALIGNMENT_Y, math.getAlignmentY());
//        StyleConstants.setForeground(attributes,math.getForeground());
//        return attributes;
//    }
//
//    public static JMathComponent modelToMath(AttributeSet attributes) {
//        JMathComponent nouveauMath = creerNouveauMathComponent((String) attributes.getAttribute(MATH_CONTENT));
//        nouveauMath.setForeground((Color) StyleConstants.getForeground(attributes));
//        nouveauMath.setFontSize((Float) attributes.getAttribute(MATH_SIZE));
//        nouveauMath.setAlignmentX((Float) attributes.getAttribute(MATH_ALIGNMENT_X));
//        nouveauMath.setAlignmentY((Float) attributes.getAttribute(MATH_ALIGNMENT_Y));
//        return nouveauMath;
//    }

    /**
     * Méthode convertissant le contenu d'un champ de texte au format MathML.
     *
     * @param txt le champ de texte dont on veut transformer le contenu en
     * MathML
     * @return la chaine contenu dans le JLimitedMathTextPane au format MathML
     */
//    public static String convertToMathMLString(JLimitedMathTextPane txt) {
//        return convertToMathMLString(txt, 0, txt.getHTMLdoc().getLength());
//    }

    /**
     *
     * @param txt le champs de texte dont on veut transformer le contenu en
     * MathML
     * @return la chaine contenu dans le JLimitedMathTextPane au format MathML
     */
    /**
     * Méthode convertissant le contenu d'un champ de texte entre les positions
     * indiqué au format MathML.
     *
     * @param txt le champ de texte dont on veut transformer le contenu en
     * MathML
     * @param posStart la position à partir de laquelle on souhaite effectuer la
     * conversion
     * @param posEnd la position jusqu'à laquell on souhaite effectuer la
     * conversion
     * @return la chaine contenu entre les deux index dans le
     * JLimitedMathTextPane au format MathML
     */
//    public static String convertToMathMLString(JMathTextPane txt, int posStart, int posEnd) {
//        int start = Math.min(posStart, posEnd);
//        int end = Math.max(posStart, posEnd);
//        HTMLDocument htmlDoc = txt.getHTMLdoc();
//        String chaineParsee = "";
//        
//        //on convertit les JMathComponent :
//        boolean wordFlag=false, numericFlag=false;
//        for(int i = start; i<end; i++) {
//            if(txt.isMathComponentPosition(i)) {
//                JMathComponent math = txt.getMathComponent(i);
//                chaineParsee += math.getContent().replaceAll("<\\?xml.*\\?>", "").replaceAll("<math>|</math>","");
////                if(wordFlag) { wordFlag = false; chaineParsee += "</mi>"; }
//                if(numericFlag) { numericFlag = false; chaineParsee += "</mn>"; }
//            } else {
//                try {
//                    String caractere = htmlDoc.getText(i, 1);
//                    //traitement caractères spéciaux
//                    if (caractere.equals("&")) {//XXX ??? Quand est-ce que ça peut arriver ?
////                        caractere = "&#38;";
//                    } else if (caractere.equals("<")) {
//                        caractere = "&#60;";
//                    } else if (caractere.equals(">")) {
//                        caractere = "&#62;";
//                    }
//                    //traitement des lettres (math identifiers)
////                    if(caractere.matches("[a-zA-Zàáâãäåçèéêëìíîïðòóôõöùúûüýÿ]")) {
////                        if(numericFlag) { numericFlag = false; chaineParsee += "</mn>"; }
////                        if(!wordFlag) { wordFlag = true; chaineParsee+="<mi>"; }
////                    } else {
////                        if(wordFlag) { wordFlag = false; chaineParsee += "</mi>"; }
////                    }
//                    //traitement des nombres (numeric)
////                    if(caractere.matches("[0-9.,]")) {
//                    else 
////                        if(wordFlag) { wordFlag = false; chaineParsee += "</mi>"; }
//                        if(!numericFlag) { numericFlag = true; chaineParsee+="<mn>"; }
////                    } else {
////                        if(numericFlag) { numericFlag = false; chaineParsee += "</mn>"; }
////                    }
//                    chaineParsee += caractere;
//                } catch (BadLocationException ex) {
//                    Logger.getLogger(MathTools.class.getName()).log(Level.SEVERE, null, ex);
//                    return "";
//                }
//            }
//        }
//        //ferme les balises ouvertes :
////        if(wordFlag) { chaineParsee += "</mi>"; }
//        if(numericFlag) { chaineParsee += "</mn>"; }
        
        //gère le style :
//        String couleurHexa = ColorManager.getRGBHexa(StyleConstants.getForeground(htmlDoc.getCharacterElement(posEnd).getAttributes()));
//        chaineParsee = "<font color='#"+couleurHexa+"'>" + chaineParsee + "</font>";

//        for (int i = start; i < end; i++) {   // Cas initial
//            if (i == start) {
//                if (!getColor(htmlDoc.getCharacterElement(i)).equals(COLOR_BLACK)) {
//                    chaineParsee = chaineParsee + "<font color='" + getColor(htmlDoc.getCharacterElement(i)) + "'>";
//                }
//                if (txt.isMathComponentPosition(i)) {
//                    JMathComponent math = txt.getMathComponent(i);
//                    chaineParsee = chaineParsee + math.getContent().replaceAll("<?xml.*?>", "").replaceAll("<math>|</math>","");
//                } else {
//                    try {
//                        String caractere = htmlDoc.getText(i, 1);
//                        if (caractere.equals("&")) {
//                            caractere = "&#38;";
//                        }
//                        if (caractere.equals("<")) {
//                            caractere = "&#60;";
//                        }
//                        if (caractere.equals(">")) {
//                            caractere = "&#62;";
//                        }
//                        chaineParsee = chaineParsee + "<mn>" + caractere + "</mn>";
//                    } catch (BadLocationException ex) {
//                        Logger.getLogger(MathTools.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            } // Autres cas
//            else {   // Gestion des couleurs
//                if (!getColor(htmlDoc.getCharacterElement(i)).equals(getColor(htmlDoc.getCharacterElement(i - 1)))) {
//                    if (getColor(htmlDoc.getCharacterElement(i)).equals(COLOR_BLACK)) {
//                        chaineParsee = chaineParsee + "</font>";
//                    } else {
//                        if (!getColor(htmlDoc.getCharacterElement(i - 1)).equals(COLOR_BLACK)) {
//                            chaineParsee = chaineParsee + "</font>";
//                        }
//                        chaineParsee = chaineParsee + "<font color='" + getColor(htmlDoc.getCharacterElement(i)) + "'>";
//                    }
//                }
//
//                // Gestion du contenu
//                if (txt.isMathComponentPosition(i)) // MathComponent
//                {
//                    JMathComponent math = txt.getMathComponent(i);
//                    chaineParsee = chaineParsee + math.getContent();
//                } else // Texte normal
//                {
//                    try {
//                         String caractere = htmlDoc.getText(i, 1);
//                        if (caractere.equals("&")) {
//                            caractere = "&#38;";
//                        }
//                        if (caractere.equals("<")) {
//                            caractere = "&#60;";
//                        }
//                        if (caractere.equals(">")) {
//                            caractere = "&#62;";
//                        }
//                        if (caractere.equals(" ")) {
//                            caractere = "&#xA0;";
//                        }
//                        chaineParsee = chaineParsee + "<mn>" + caractere + "</mn>";
//                    } catch (BadLocationException ex) {
//                        Logger.getLogger(MathTools.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }
//        }
//        if (!getColor(htmlDoc.getCharacterElement(htmlDoc.getLength() - 1)).equals(COLOR_BLACK)) {
//            chaineParsee = chaineParsee + "</font>";
//        }
//        System.out.println(chaineParsee);
//        return chaineParsee;
//    }

//    private static String getColor(javax.swing.text.Element e) {
//        Color couleur = StyleConstants.getForeground(e.getAttributes());
//        Color noire = ColorManager.get("color ink1");
//        Color rouge = ColorManager.get("color ink2");
//        Color bleue = ColorManager.get("color ink3");
//        Color verte = ColorManager.get("color ink4");
//        if (couleur.getRGB() == noire.getRGB()) {
//            return COLOR_BLACK;
//        }
//        if (couleur.getRGB() == rouge.getRGB()) {
//            return COLOR_RED;
//        }
//        if (couleur.getRGB() == bleue.getRGB()) {
//            return COLOR_BLUE;
//        }
//        if (couleur.getRGB() == verte.getRGB()) {
//            return COLOR_GREEN;
//        }
//        return COLOR_BLACK;
//    }

/*    public static boolean isContentTooLong(JMathTextPane text) {
        if (text.getSelectedText() == null) {
            return false;
        } else {
            return MathTools.getStringWidth(text, text.getSelectionStart(), text.getSelectionEnd(), JLimitedMathTextPane.FONT_MAX) >= JLimitedMathTextPane.LARGEUR_MAX_TOTAL;
        }
    }

    public static boolean isContentTooLong(JMathTextPane text, boolean enterAsSeparator) {
        if(!enterAsSeparator){
            return isContentTooLong(text);
        }
        if (text.getSelectedText() == null) {
            return false;
        } else {
            String[] chaines = text.getSelectedText().split("\n");
            int index = text.getSelectionStart();
            for(String contenu : chaines){
                if(MathTools.getStringWidth(text, index, index + contenu.length(), JLimitedMathTextPane.FONT_MAX) >= JLimitedMathTextPane.LARGEUR_MAX_TOTAL){
                    return true;
                }else{
                    index = index + contenu.length() + 1;
                }
            }
            return false;
        }
    }
*/
    /**
     * Méthode mettant au carré la sélection contenu dans un JMathTextPane.
     *
     * @param text le JMathTextPane contenant le texte à transofmrer en sélection
     * @return la chaine MathML mise au carré
     */
//    public static JMathComponent putInSquareString(JMathTextPane text) {
//        if (text instanceof JLimitedMathTextPane && JLimitedMathTextPane.isContentTooLong(text)) {
//            DialogueBloquant.error("dialog selection too long");
//            return null;
//        }
//        String chaine = convertToMathMLString(text, text.getSelectionStart(), text.getSelectionEnd());
//        chaine = putInSquareMathMLString(chaine);
//        return creerMathComponent(chaine);
//    }

//    /**
//     * Méthode mettant au carré une chaine de caractères MathML.
//     *
//     * @param mathMLString la chaine au format MathML
//     * @return la chaine MathML mise au carré
//     */
//    public static String putInSquareMathMLString(String mathMLString) {
//        String chaineRenvoyee = "<msup><mrow>" + mathMLString + "</mrow><mrow><mn>2</mn></mrow></msup>";
//        return chaineRenvoyee;
//    }

//    /**
//     * Méthode mettant au carré une chaine de caractère MathML.
//     *
//     * @param textPane le JMathTextPane à observer
//     * @param posStart le début de la sélection
//     * @param posEnd la fin de la selection
//     * @return le mathComponent correspondant
//     */
//    public static JMathComponent putSelectionInBracket(JMathTextPane textPane, int posStart, int posEnd) {
//        int start = Math.min(posStart, posEnd);
//        int end = Math.max(posStart, posEnd);
//        String chaine = "<mrow><mfenced><mrow>" + convertToMathMLString(textPane, start, end) + "</mrow></mfenced></mrow>";
//        return creerMathComponent(chaine);
//    }
    
//    /**
//     * Méthode mettant sous une racine carrée une chaine de caractère MathML.
//     *
//     * @param textPane le JMathTextPane à observer
//     * @param posStart le début de la sélection
//     * @param posEnd la fin de la selection
//     * @return le mathComponent correspondant
//     */
//    public static JMathComponent putSelectionInSquareRoot(JMathTextPane textPane, int posStart, int posEnd){
//        int start = Math.min(posStart, posEnd);
//        int end = Math.max(posStart, posEnd);
//        String chaine = "<msqrt><mrow>"+convertToMathMLString(textPane, start, end)+"</mrow></msqrt>";
//        return creerMathComponent(chaine);
//    }
//    
//    /**
//     * Méthode mettant en système d'équation un chaine de caractère MathML.
//     *
//     * @param premiereLigne la première ligne du système
//     * @param deuxiemeLigne la seconde ligne du système
//     * @return la chaine MathML mise en système d'équation
//     */
//    public static String putSelectionInEquationSystem(String premiereLigne, String deuxiemeLigne){
//        String chaineRenvoyee = "<mrow><mo>{</mo><mrow><mtable columnalign='left'><mtr><mtd><mrow>"+premiereLigne+"</mrow></mtd></mtr><mtr><mtd><mrow>"+deuxiemeLigne+"</mrow></mtd></mtr></mtable></mrow></mrow>";
//        return chaineRenvoyee;
//    }
    
//    public static String putInEquationSystemMathMLString(String[] lignes){
//        StringBuilder sb = new StringBuilder().append("<mrow><mo>{</mo><mrow><mtable columnalign='left'>");
//        for(String ligne : lignes){
//            sb.append("<mtr><mtd><mrow>").append(ligne).append("</mrow></mtd></mtr>");
//        }
//        sb.append("</mtable></mrow></mrow>");
//        return sb.toString();
//    }

//    /**
//     * Méthode mettant en système d'équation un chaine de caractère MathML.
//     *
//     * @param textPane le JMathTextPane à observer
//     * @param posStart le début de la sélection
//     * @param posEnd la fin de la selection
//     * @return le JMathComponent correspondant
//     */
//    public static JMathComponent putSelectionInEquationSystem(JMathTextPane textPane, int posStart, int posEnd) {
//        int start = Math.min(posStart, posEnd);
//        int end = Math.max(posStart, posEnd);
//        textPane.setSelectionStart(posStart);
//        textPane.setSelectionEnd(posEnd);
//        int nbLignes = textPane.getSelectedText().split("\n").length;
//
//        int indexDepart = start;
//        int indexFin;
//        String[] chaines = new String[nbLignes];
//        for(int i = 0; i < nbLignes; i++){
//            int index = textPane.getSelectedText().indexOf("\n", indexDepart);
//            if(index == -1){
//                index = textPane.getSelectedText().length();
//            }
//            indexFin = textPane.getSelectionStart() + index;
//            chaines[i] = indexDepart != indexFin ? MathTools.convertToMathMLString(textPane, indexDepart, indexFin) : "";
//            indexDepart = indexFin + 1;
//        }
//        String chaine = putInEquationSystemMathMLString(chaines);
//        return creerMathComponent(chaine);
//    }


    public static long getId(JMathComponent mathComponent) {
        return Long.parseLong(mathComponent.getName());
    }

    public static void setId(JMathComponent mathComponent, long id) {
        mathComponent.setName(id+"");
    }

    private static final HashMap<JMathComponent,Color> foregroundColors = new HashMap<>();
    
    public static void selectionner(JMathComponent math) {
        if(isSelected(math)) {return;}
        Color oldColor = math.getForeground();
        math.setBackground(ColorManager.get("color disabled"));
        math.setForeground(Color.WHITE);
        foregroundColors.put(math, oldColor);
        math.repaint();
    }

    public static void deselectionner(JMathComponent math) {
        if(!isSelected(math)) {return;}
        Color oldColor = foregroundColors.get(math);
        foregroundColors.remove(math);
        if(oldColor==null) {return;}
        math.setForeground(oldColor);//on rend au composant son ancienne couleur
        math.setBackground(ColorManager.transparent());
        math.repaint();
    }
    
    public static boolean isSelected(JMathComponent math) {
        return foregroundColors.get(math)!=null;
    }
    
    public static String getHTMLRepresentation(JMathComponent math) {
        boolean selected = isSelected(math);
        if(selected) {deselectionner(math);}
        String html = "<math>"+math.getContent().replaceAll("<math>|</math>","")+"</math>";
        Element span = Jsoup.parse("<span class='"+MATH_COMPONENT+"'></span>").body().child(0);
        HashMap<String, String> styles = new HashMap<>();
        styles.put("font-size",""+math.getFontSize());
        styles.put("color",ColorManager.getRGBHexa(math.getForeground()));
        styles.put("height",""+math.getSize().height);
        styles.put("width",""+math.getSize().width);
//        styles.put("vertical-align",""+math.getAlignmentY());
        span.attr("id", getId(math)+"")
//                .attr("style", "font-size:"+math.getFontSize()+";color:#"+ColorManager.getRGBHexa(math.getForeground())+";")
//                .attr("height", ""+math.getSize().height)
//                .attr("width", ""+math.getSize().width)
                .attr("y-align",""+math.getAlignmentY())
                .html(html);
        JsoupTools.setStyle(span, styles);
        JsoupTools.removeComments(span);//on enlève l'instruction de version (xml version 1.0 encoding etc)
        if(selected) {selectionner(math);}
        return span.outerHtml();
    }

    public static JMathComponent creerJMathComponentFromHTML(String htmlRepresentation) {
        Document doc = Jsoup.parse(htmlRepresentation);
        Element mathElement = doc.select("span."+MATH_COMPONENT).first();//cas d'un MathML créé par MathEOS
        if(mathElement==null) { mathElement = doc.select("math").first(); }//cas d'un MathML quelconque
        if(mathElement==null) { System.out.println("impossible de charger le mathML : "+htmlRepresentation);return null;}//échec
        
        Dimension size = null;
        Color color;
        Float yAlign = null;
        Long id = null;
        double widthAttr = JsoupTools.getSizedStyle(mathElement, "width");//TODO gérer les unités au lieu de les supprimer
        double heightAttr = JsoupTools.getSizedStyle(mathElement, "height");
        float fontSize = JsoupTools.getFontSize(mathElement);
        String yAlignAttr = mathElement.attr("y-align").replaceAll("[a-zA-Z]", "");
        String idAttr = mathElement.attr("id");
        if(widthAttr!=0 && heightAttr!=0) {size = new Dimension((int)widthAttr,(int)heightAttr);}
        color = JsoupTools.getColor(mathElement);
        if(!yAlignAttr.isEmpty()) {yAlign = Float.parseFloat(yAlignAttr);}
        if(!idAttr.isEmpty()) {id = Long.parseLong(idAttr);}
        
        return chargerMathComponent(mathElement.html(), color, size, fontSize, yAlign, id);
    }

    private static JMathComponent chargerMathComponent(String contenuHTML, Color foreground, Dimension size, Float fontSize, Float alignmentY, Long id) {
        JMathComponent mathComponent = creerMathComponent(contenuHTML);
        if(foreground!=null) mathComponent.setForeground(foreground);
        if(size!=null) mathComponent.setSize(size);
        if(fontSize!=null) mathComponent.setFontSize(fontSize);
        if(alignmentY!=null) mathComponent.setAlignmentY(alignmentY);
        if(id!=null) setId(mathComponent,id);
        return mathComponent;
    }

    public static BufferedImage capturerImage(JMathComponent mathComponent) {
        int width = mathComponent.getSize().width, height = mathComponent.getSize().height;
        if (width == 0 || height == 0) {
            return null;
        }
        BufferedImage tamponSauvegarde = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = tamponSauvegarde.createGraphics(); //On crée un Graphic que l'on insère dans tamponSauvegarde
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        if(isSelected(mathComponent)) {
            deselectionner(mathComponent);
            mathComponent.paint(g);
            selectionner(mathComponent);
        } else {
            mathComponent.paint(g);
        }
        return tamponSauvegarde;
    }

    public static void setFontSize(JMathComponent c, float size) {
        c.setFontSize(size);
    }
    public static void setContent(JMathComponent component, String content) {
        String newContent = content;
        newContent = newContent.replaceAll("&times;", "&#x000d7;");//JMathComponent ne lit pas le HTML
        newContent = newContent.replaceAll("&divide;", "&#x000f7;");//JMathComponent ne lit pas le HTML
        newContent = newContent.replaceAll("\n", "");//JMathComponent ne lit pas les \n (JMathComponent c'est un peu de la merde...)
        newContent = newContent.replaceAll("<\\?xml(.)*\\?>", "");
        component.setContent(newContent);
    }

    public static class MathAlignmentEdit extends AbstractUndoableEdit {
        private final float oldAlignment;
        private final float newAlignment;
        private final JMathComponent mathComponent;
        public MathAlignmentEdit(JMathComponent math, float oldAlignmentY, float newAlignemntY) {
            this.mathComponent = math;
            this.oldAlignment = oldAlignmentY;
            this.newAlignment = newAlignemntY;
        }
        public void undo() throws CannotUndoException {
            super.undo();
            mathComponent.setAlignmentY(oldAlignment);
            mathComponent.repaint();
        }
        public void redo() throws CannotUndoException {
            super.redo();
            mathComponent.setAlignmentY(newAlignment);
            mathComponent.repaint();
        }
    }
    public static class MathEdit extends AbstractUndoableEdit {
        private final String oldMathML;
        private final String newMathML;
        private final JMathComponent mathComponent;
        public MathEdit(JMathComponent math, String oldMathML, String newMathML) {
            this.mathComponent = math;
            this.oldMathML = oldMathML;
            this.newMathML = newMathML;
        }
        public void undo() throws CannotUndoException {
            super.undo();
            MathTools.setContent(mathComponent, oldMathML);
            mathComponent.repaint();
        }
        public void redo() throws CannotUndoException {
            super.redo();
            MathTools.setContent(mathComponent, newMathML);
            mathComponent.repaint();
        }
    }
    
    public static class MathMouseListener implements MouseListener {
        private final JMathTextPane jtp;
        public MathMouseListener(JMathTextPane jtp) {this.jtp = jtp;}
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (e.getComponent() instanceof JMathComponent) {
                    JMathComponent math = (JMathComponent) e.getComponent();
                    if (jtp!=null) {
    //                    if (!ClavierManager.getInstance().hasChild(parent) && ClavierManager.getInstance().isSelectedArea(parent)) {
                        jtp.requestFocusInWindow();
                        jtp.setCaretPosition(jtp.getMathPosition(math) + 1); // Force la déselection
//                        EditMathManager mathManager = new EditMathManager(math, jtp);
//                        mathManager.editMath();
                        edit(math, jtp);
    //                    } else if (parent instanceof JMathTextPane) { //Si le composant n'a pas le focus, on le lui donne e on change le caret de position
    //                        JMathTextPane mathParent = (JMathTextPane) parent;
    //                        if(mathParent.isEnabled() && mathParent.isFocusable()){
    //                            mathParent.setCaretPosition(((JMathTextPane) parent).getMathPosition(math));
    //                            mathParent.requestFocusInWindow();
    //                        }
    //                    }
                    }

                }
            }
            if (SwingUtilities.isRightMouseButton(e)) {
                if (e.getComponent() instanceof JMathComponent) {
                    JMathComponent math = (JMathComponent) e.getComponent();
    //                    if (!ClavierManager.getInstance().hasChild(parent) && parent.isFocusOwner()) {
                    jtp.setCaretPosition(jtp.getMathPosition(math) + 1);
                    DialogueMathAlignement dialogueMathAlignement = new DialogueMathAlignement(jtp, math);
                    //                    }
                }

            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if(e.getSource() instanceof JMathComponent)
            {   JMathComponent math = (JMathComponent) e.getComponent();
                math.setBorder(BorderFactory.createLineBorder(Color.black));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if(e.getSource() instanceof JMathComponent)
            {   JMathComponent math = (JMathComponent) e.getComponent();
                math.setBorder(null);
            }
        }
    }
    
    public static void edit(JMathComponent mathComponent, JMathTextPane parent) {
        EditLibrary.edit(mathComponent, parent);
    }
    
    /** sert simplement à séparer les fonctions relatives à l'édition des JMathComponent des autres fonctions **/
    private static abstract class EditLibrary {
        public static void edit(final JMathComponent mathComponent, final JMathTextPane parent) {
            Element masterElement = selectMasterElement(mathComponent);
            System.out.println("contenu lu : "+masterElement.outerHtml());

            switch(masterElement.tagName()) {
                case "mover" : handle_over(mathComponent, parent, masterElement); break;
                case "msup" : handle_sup(mathComponent, parent, masterElement); break;
                case "msub" : handle_sub(mathComponent, parent, masterElement); break;
                case "mfrac" : handle_frac(mathComponent, parent, masterElement); break;
                case "mtable" : handle_table(mathComponent, parent, masterElement); break;
                case "msqrt" : handle_sqrt(mathComponent, parent, masterElement); break;
                case "mfenced" : handle_fenced(mathComponent, parent, masterElement); break;
                case "mrow" : handle_row(mathComponent, parent, masterElement); break;
                default :
                    //TODO : Il faut alors regarder le premier sibling
            }
        }

        private static Element selectMasterElement(JMathComponent c) {
//            Element mathElement = Jsoup.parse(c.getContent()).body().child(0);
            Element masterElement = Jsoup.parse(MathTools.getHTMLRepresentation(c)).select("math").first().child(0);
            if(masterElement.tagName().equals("mrow")) {
                Element table = masterElement.select("mtable").first();
                if(table!=null) {masterElement = table;}
            }
            return masterElement;
        }

        private static void handle_over(JMathComponent c, JMathTextPane textParent, Element masterElement) {
//            String angle = DialogueBloquant.input("dialog angle mark", masterElement.html());
//            String oldContent = c.getContent();
//
//            Document doc = Jsoup.parse(c.getContent());
//            doc.select("mn").first().html(angle);
//            c.setContent(doc.body().html());
//
//            String newContent = c.getContent();
//            textParent.getUndo().addEdit(new MathTools.MathEdit(c, oldContent, newContent));//TODO éviter de cette façon de faire
            String angle = masterElement.child(0).outerHtml();
            DialogueMath dialogue = new DialogueMathChapeauAngle(textParent, angle);
            dialogue.addDialogueMathListener(dialogue.new EditListener(c));
        }
        private static void handle_sup(JMathComponent c, JMathTextPane textParent, Element masterElement) {
            String mantice = masterElement.child(0).outerHtml();
            String exposant = masterElement.child(1).outerHtml();
            DialogueMath dialogue = new DialogueMathExposant(textParent, mantice, exposant);
            dialogue.addDialogueMathListener(dialogue.new EditListener(c));
        }
        private static void handle_sub(JMathComponent c, JMathTextPane textParent, Element masterElement) {
            String valeur = masterElement.child(0).outerHtml();
            String indice = masterElement.child(1).outerHtml();
            DialogueMath dialogue = new DialogueMathIndice(textParent, valeur, indice);
            dialogue.addDialogueMathListener(dialogue.new EditListener(c));
        }
        private static void handle_frac(JMathComponent c, JMathTextPane textParent, Element masterElement) {
            String numerateur = masterElement.child(0).outerHtml();
            String denominateur = masterElement.child(1).outerHtml();
            DialogueMath dialogue = new DialogueMathFraction(textParent, numerateur, denominateur);
            dialogue.addDialogueMathListener(dialogue.new EditListener(c));
        }
        private static void handle_table(JMathComponent c, JMathTextPane textParent, Element masterElement) {
            String equation1 = masterElement.select("mrow").first().outerHtml();
            String equation2 = masterElement.select("mrow").get(1).outerHtml();
            DialogueMath dialogue = new DialogueMathSysteme(textParent, equation1, equation2);
            dialogue.addDialogueMathListener(dialogue.new EditListener(c));
        }
        private static void handle_sqrt(JMathComponent c, JMathTextPane textParent, Element masterElement) {
            String valeur = masterElement.child(0).outerHtml();
            DialogueMath dialogue = new DialogueMathRacineCarree(textParent, valeur);
            dialogue.addDialogueMathListener(dialogue.new EditListener(c));
        }
        private static void handle_fenced(JMathComponent c, JMathTextPane textParent, Element masterElement) {
            String valeur = masterElement.child(0).outerHtml();
            DialogueMath dialogue = new DialogueMathParentheseLarge(textParent, valeur);
            dialogue.addDialogueMathListener(dialogue.new EditListener(c));
        }
        private static void handle_row(JMathComponent c, JMathTextPane textParent, Element masterElement) {
            String valeur = masterElement.outerHtml();
            DialogueMath dialogue = new DialogueMathEquation(textParent, valeur);
            dialogue.addDialogueMathListener(dialogue.new EditListener(c));
        }

        private EditLibrary() {throw new AssertionError("instanciating utilitary class");}
    }

    private MathTools() {throw new AssertionError("instanciating utilitary class");}
    
}
