///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package bomehc.arevoir;
//
//import bomehc.utils.managers.ColorManager;
//import java.awt.Color;
//import java.io.IOException;
//import java.io.Reader;
//import java.io.StringReader;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.swing.text.MutableAttributeSet;
//import javax.swing.text.StyleConstants;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import net.sourceforge.jeuclid.swing.JMathComponent;
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//import bomehc.utils.texte.JLimitedMathTextPane;
//import bomehc.utils.texte.MathTools;
//import bomehc.utils.dialogue.DialogueEvent;
//import bomehc.utils.dialogue.DialogueListener;
//import bomehc.utils.dialogue.math.DialogueMathChapeauAngle;
//import bomehc.utils.dialogue.math.DialogueMathEquation;
//import bomehc.utils.dialogue.math.DialogueMathExposant;
//import bomehc.utils.dialogue.math.DialogueMathFraction;
//import bomehc.utils.dialogue.math.DialogueMathIndice;
//import bomehc.utils.dialogue.math.DialogueMathParentheseLarge;
//import bomehc.utils.dialogue.math.DialogueMathRacineCarree;
//import bomehc.utils.dialogue.math.DialogueMathSysteme;
//import bomehc.utils.texte.JMathTextPane;
//
///**
// *
// * @author Guillaume
// */
//public class EditMathManager implements DialogueListener {
//
//    private JMathTextPane parent;
//    private JMathComponent oldMath;
//    private JMathComponent newMath;
//    private static final String COLOR_BLACK = "black";
//    private static final String COLOR_RED = "red";
//    private static final String COLOR_BLUE = "blue";
//    private static final String COLOR_GREEN = "#009600";
//
//    public EditMathManager(JMathComponent math, JMathTextPane parent) {
//        this.parent = parent;
//        this.oldMath = math;
//        this.newMath = MathTools.copyMathComponent(oldMath);
//    }
//
//    public void editMath() {
//        String content = oldMath.getContent();
//        if (content.contains("<?xml") && content.contains("?>")) {
//            String chaineAux = content.substring(content.indexOf("<?xml") + 5, content.indexOf("?>"));
//            chaineAux = "<\\?xml" + chaineAux + "\\?>";
//            if (chaineAux != null) {
//                content = content.replaceAll(chaineAux, "");
//            }
//        }
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder parseur;
//        Document doc = null;
//        try {
//            parseur = factory.newDocumentBuilder();
//            Reader reader = new StringReader(content);
//            InputSource is = new InputSource(reader);
//            doc = parseur.parse(is);
//        } catch (SAXException ex) {
//            Logger.getLogger(EditMathManager.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(EditMathManager.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ParserConfigurationException ex) {
//            Logger.getLogger(EditMathManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        if (doc == null) {
//            return;
//        }
//
//        definirType(doc.getFirstChild());
//    }
//
//    public void definirType(Node noeud) {
//        String nodeName = noeud.getNodeName();
//
//        if (nodeName.equals("mrow")) {
//            typeMrow(noeud);
//            return;
//        }   //Système d'équations, Equation, Parenthèse large
//        if (nodeName.equals("mfrac")) {
//            typeMfrac(noeud);
//            return;
//        }  //Fraction
//        if (nodeName.equals("msqrt")) {
//            typeMsqrt(noeud);
//            return;
//        }  // Racine carrée
//        if (nodeName.equals("msup")) {
//            typeMsup(noeud);
//            return;
//        }   // Exposant
//        if (nodeName.equals("msub")) {
//            typeMsub(noeud);
//            return;
//        }   //Indice
//        if (nodeName.equals("mover")) {
//            typeMover(noeud);
//            return;
//        }  //Chapeau Angle
//    }
//
//    public void typeMrow(Node noeud) {
//        if (noeud.hasChildNodes()) {
//            if (noeud.getChildNodes().getLength() == 1) // Cas parenthèse large à tester
//            {
//                if (noeud.getFirstChild().getNodeName().equals("mfenced")) {
//                    typeMfenced(noeud.getFirstChild());
//                    return;
//                }
//            } else if (noeud.getChildNodes().getLength() == 2) // Cas Système d'équation à tester
//            {
//                Node firstNode = noeud.getFirstChild();
//                Node secondNode = noeud.getChildNodes().item(1);
//                if (firstNode.getNodeName().equals("mo") && secondNode.getNodeName().equals("mrow")) {
//                    if (firstNode.getTextContent().equals("{") && secondNode.getChildNodes().getLength() == 1) {
//                        Node secondBisNode = secondNode.getFirstChild();
//                        if (secondBisNode.getNodeName().equals("mtable")) {
//                            typeMtable(secondBisNode);
//                            return;
//                        }
//                    }
//                }
//            }
//        }
//        typeEquation(noeud);
//    }
//
//    public void typeMfrac(Node noeud) {
//        DialogueMathFraction dialogue = new DialogueMathFraction(parent);
//        Node numNode = noeud.getChildNodes().item(0);
//        Node denNode = noeud.getChildNodes().item(1);
//        JLimitedMathTextPane num = dialogue.getNumerateur();
//        JLimitedMathTextPane den = dialogue.getDenominateur();
//        parserPourRemplirChamps(numNode, num);
//        parserPourRemplirChamps(denNode, den);
//        num.getUndo().discardAllEdits();
//        den.getUndo().discardAllEdits();
//        dialogue.addDialogueListener(this);
//        num.setCaretPosition(num.getHTMLdoc().getLength());
//        dialogue.setVisible(true);
//    }
//
//    public void typeMsqrt(Node noeud) {
//        DialogueMathRacineCarree dialogue = new DialogueMathRacineCarree(parent);
//        Node champNode = noeud.getChildNodes().item(0);
//        JLimitedMathTextPane champ = dialogue.getChamp();
//        parserPourRemplirChamps(champNode, champ);
//        champ.getUndo().discardAllEdits();
//        dialogue.addDialogueListener(this);
//        champ.setCaretPosition(champ.getHTMLdoc().getLength());
//        dialogue.setVisible(true);
//    }
//
//    public void typeMsup(Node noeud) {
//        DialogueMathExposant dialogue = new DialogueMathExposant(parent);
//        Node champNode = noeud.getChildNodes().item(0);
//        Node exposantNode = noeud.getChildNodes().item(1);
//        JLimitedMathTextPane champ = dialogue.getChamp();
//        JLimitedMathTextPane exposant = dialogue.getExposant();
//        parserPourRemplirChamps(champNode, champ);
//        parserPourRemplirChamps(exposantNode, exposant);
//        champ.getUndo().discardAllEdits();
//        exposant.getUndo().discardAllEdits();
//        dialogue.addDialogueListener(this);
//        champ.setCaretPosition(champ.getHTMLdoc().getLength());
//        dialogue.setVisible(true);
//    }
//
//    public void typeMsub(Node noeud) {
//        DialogueMathIndice dialogue = new DialogueMathIndice(parent);
//        Node champNode = noeud.getChildNodes().item(0);
//        Node indiceNode = noeud.getChildNodes().item(1);
//        JLimitedMathTextPane champ = dialogue.getChamp();
//        JLimitedMathTextPane indice = dialogue.getIndice();
//        parserPourRemplirChamps(champNode, champ);
//        parserPourRemplirChamps(indiceNode, indice);
//        champ.getUndo().discardAllEdits();
//        indice.getUndo().discardAllEdits();
//        dialogue.addDialogueListener(this);
//        champ.setCaretPosition(champ.getHTMLdoc().getLength());
//        dialogue.setVisible(true);
//    }
//
//    public void typeMover(Node noeud) {
//        DialogueMathChapeauAngle dialogue = new DialogueMathChapeauAngle(parent);
//        Node champNode = noeud.getChildNodes().item(0);
//        JLimitedMathTextPane champ = dialogue.getChamp();
//        parserPourRemplirChamps(champNode, champ);
//        champ.getUndo().discardAllEdits();
//        dialogue.addDialogueListener(this);
//        champ.setCaretPosition(champ.getHTMLdoc().getLength());
//        dialogue.setVisible(true);
//    }
//
//    public void typeMfenced(Node noeud) {
//        DialogueMathParentheseLarge dialogue = new DialogueMathParentheseLarge(parent);
//        Node champNode = noeud.getChildNodes().item(0);
//        JLimitedMathTextPane champ = dialogue.getChamp();
//        parserPourRemplirChamps(champNode, champ);
//        champ.getUndo().discardAllEdits();
//        dialogue.addDialogueListener(this);
//        champ.setCaretPosition(champ.getHTMLdoc().getLength());
//        dialogue.setVisible(true);
//    }
//
//    public void typeMtable(Node noeud) {
//        DialogueMathSysteme dialogue = new DialogueMathSysteme(parent);
//        Node premiereLigneNode = noeud.getChildNodes().item(0).getFirstChild().getFirstChild();
//        Node deuxiemeLigneNode = noeud.getChildNodes().item(1).getFirstChild().getFirstChild();
//        JLimitedMathTextPane premiereLigne = dialogue.getPremiereEquation();
//        JLimitedMathTextPane deuxiemeLigne = dialogue.getDeuxiemeEquation();
//        parserPourRemplirChamps(premiereLigneNode, premiereLigne);
//        parserPourRemplirChamps(deuxiemeLigneNode, deuxiemeLigne);
//        premiereLigne.getUndo().discardAllEdits();
//        deuxiemeLigne.getUndo().discardAllEdits();
//        dialogue.addDialogueListener(this);
//        premiereLigne.setCaretPosition(premiereLigne.getHTMLdoc().getLength());
//        dialogue.setVisible(true);
//    }
//
//    public void typeEquation(Node noeud) {
//        DialogueMathEquation dialogue = new DialogueMathEquation(parent);
//        Node champNode = noeud;
//        JLimitedMathTextPane champ = dialogue.getChamp();
//        parserPourRemplirChamps(champNode, champ);
//        champ.getUndo().discardAllEdits();
//        dialogue.addDialogueListener(this);
//        champ.setCaretPosition(champ.getHTMLdoc().getLength());
//        dialogue.setVisible(true);
//    }
//
//    private void parserPourRemplirChamps(Node noeud, JLimitedMathTextPane texte) {
//        int pos = 0;
//        if (noeud.getChildNodes().getLength() == 0) {
//            return;
//
//        }
//        for (int i = 0; i < noeud.getChildNodes().getLength(); i++) {
//            texte.setCaretPosition(pos);
//            Node noeudFils = noeud.getChildNodes().item(i);
//            if (noeudFils.getNodeName().equals("font")) {
//                for (int j = 0; j < noeudFils.getChildNodes().getLength(); j++) {
//                    traiterNoeud(noeudFils.getChildNodes().item(j), texte);
//                    MutableAttributeSet inputAttribute = texte.getInputAttributes();
//                    inputAttribute.removeAttribute(inputAttribute);
//                    inputAttribute.addAttributes(texte.getHTMLdoc().getCharacterElement(pos).getAttributes());
//                    StyleConstants.setForeground(inputAttribute, getColor(noeudFils));
//                    texte.getHTMLdoc().setCharacterAttributes(pos, 1, inputAttribute, true);
////                    texte.majMathComponent();
//                    pos = texte.getHTMLdoc().getLength();
//                }
//            } else {
//                traiterNoeud(noeudFils, texte);
//                MutableAttributeSet inputAttribute = texte.getInputAttributes();
//                inputAttribute.removeAttribute(inputAttribute);
//                inputAttribute.addAttributes(texte.getHTMLdoc().getCharacterElement(pos).getAttributes());
//                StyleConstants.setForeground(inputAttribute, ColorManager.get("color ink1"));
//                texte.getHTMLdoc().setCharacterAttributes(pos, 1, inputAttribute, true);
////                texte.majMathComponent();
//                pos = texte.getHTMLdoc().getLength();
//            }
//        }
//    }
//
//    private void traiterNoeud(Node noeud, JLimitedMathTextPane texte) {
//        if (noeud.getNodeName().equals("mn")) {
//            texte.replaceSelection(noeud.getTextContent());
//        } else {
//            String content = "";
//
//            content = creerContent(noeud, content);
//            JMathComponent math = MathTools.creerMathComponent(content);
//            texte.insererJMathComponent(math);
//        }
//
//    }
//
//    private String creerContent(Node noeud, String content) {
//        if (noeud.getNodeValue() != null) {
//            if (noeud.getTextContent().equals(" ")) {
//                content = content + "&#xA0;";
//            } else if (noeud.getTextContent().equals("&")) {
//                content = content + "&#38;";
//            } else if (noeud.getTextContent().equals("<")) {
//                content = content + "&#60;";
//            } else if (noeud.getTextContent().equals(">")) {
//                content = content + "&#62;";
//            } else {
//                content = content + noeud.getTextContent();
//            }
//        } else {
//            content = content + "<" + noeud.getNodeName();
//            if (noeud.hasAttributes()) // On ajoute les attributs d'un noeud s'il y en a
//            {
//                for (int i = 0; i < noeud.getAttributes().getLength(); i++) {
//                    content = content + " " + noeud.getAttributes().item(i);
//                }
//            }
//            content = content + ">";
//            for (int i = 0; i < noeud.getChildNodes().getLength(); i++) {
//                content = creerContent(noeud.getChildNodes().item(i), content);
//            }
//            content = content + "</" + noeud.getNodeName() + ">";
//        }
//        return content;
//    }
//
//    public Color getColor(Node noeud) {
//        if (noeud.hasAttributes()) {
//            if (noeud.getAttributes().getNamedItem("color") != null) {
//                String couleur = noeud.getAttributes().getNamedItem("color").getNodeValue();
//                if (couleur.equals(COLOR_BLACK)) {
//                    return ColorManager.get("color ink1");
//                }
//                if (couleur.equals(COLOR_RED)) {
//                    return ColorManager.get("color ink2");
//                }
//                if (couleur.equals(COLOR_BLUE)) {
//                    return ColorManager.get("color ink3");
//                }
//                if (couleur.equals(COLOR_GREEN)) {
//                    return ColorManager.get("color ink4");
//                }
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public void dialoguePerformed(DialogueEvent event) {
//        if (event.getReponsesString() == null) {
//            return;
//        }
//        if (event.getReponsesString()[0].equals("")) {
//            return;
//        }
//        String chaine = event.getReponsesString()[0];
//        if (chaine.contains("<?xml") && chaine.contains("?>")) {
//            String chaineAux = chaine.substring(chaine.indexOf("<?xml") + 5, chaine.indexOf("?>"));
//            chaineAux = "<\\?xml" + chaineAux + "\\?>";
//            if (chaineAux != null) {
//                chaine = chaine.replaceAll(chaineAux, "");
//            }
//        }
//        newMath.setContent(chaine);
//        newMath.setAlignmentY(MathTools.calculateMathAlignment(chaine, oldMath.getFontMetrics(oldMath.getFont())));
//
//        if (parent != null) {
//            parent.requestFocus();
//            parent.updateMathComp(oldMath, newMath);
//        }
//    }
//}
