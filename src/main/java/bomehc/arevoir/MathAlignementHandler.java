/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bomehc.arevoir;

import java.awt.FontMetrics;
import java.util.Arrays;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author Guillaume
 */
public class MathAlignementHandler {

    private static final Character[] CARACTERES_DESCENT = {'y', 'p', 'q', 'g', 'j'};
    private double alignement = 0.5;
    private FontMetrics fontMetrics;

    public MathAlignementHandler() {
    }

    public float getAlignement() {
        return (float) alignement;
    }

    public float parse(Document doc, FontMetrics fontMetrics) {
        this.fontMetrics = fontMetrics;
        alignement = premierNoeud(doc.getFirstChild());
        return (float) alignement;
    }

    public void parseur(Node noeud) {
        //afficherElement(noeud);
        if (noeud.hasChildNodes()) {
            for (int i = 0; i < noeud.getChildNodes().getLength(); i++) {
                parseur(noeud.getChildNodes().item(i));
            }
        }
    }

    public double premierNoeud(Node noeud) {
        double align = 1.0;
        String balise = noeud.getNodeName();

        if (balise.equals("mrow")) {
            align = traiterMROW(noeud);
        }
        if (balise.equals("mn")) {
            align = traiterTexte(noeud);
        }
        if (balise.equals("mi")) {
            align = traiterTexte(noeud);
        }
        if (balise.equals("mo")) {
            align = traiterTexte(noeud);
        }
        if (balise.equals("font")) {
            premierNoeud(noeud.getFirstChild());
        }
        if (balise.equals("mfrac")) {
            align = 0.1 + traiterMFRAC(noeud);
        }
        if (balise.equals("msqrt")) {
            align = traiterMSQRT(noeud);
        }
        if (balise.equals("msup")) {
            align = traiterMSUP(noeud);
        }
        if (balise.equals("msub")) {
            align = traiterMSUB(noeud);
        }
        if (balise.equals("mover")) {
            align = traiterTexte(noeud);
        }

        return align;
    }

    public double selectionNoeud(Node noeud) {
        double align = 1.0;
        String balise = noeud.getNodeName();

        if (balise.equals("mrow")) {
            align = traiterMROW(noeud);
        }
//        if(balise.equals("mn")){align = (double) 22/27;} // Pour les lettres en-dessous de la baseline
        if (balise.equals("mn")) {
            align = traiterTexte(noeud);
        }
        if (balise.equals("mtext")) {
            align = traiterTexte(noeud);
        }
        if (balise.equals("mi")) {
            align = traiterTexte(noeud);
        }
        if (balise.equals("mo")) {
            align = traiterTexte(noeud);
        }
        if (balise.equals("font")) {
            align = traiterMROW(noeud);
        }
        if (balise.equals("mfrac")) {
            align = traiterMFRAC(noeud);
        }
        if (balise.equals("msqrt")) {
            align = traiterMSQRT(noeud);
        }
        if (balise.equals("msup")) {
            align = traiterMSUP(noeud);
        }
        if (balise.equals("msub")) {
            align = traiterMSUB(noeud);
        }
        if (balise.equals("mfenced")) {
            align = traiterMFENCED(noeud);
        }
        if (balise.equals("mover")) {
            align = traiterTexte(noeud);
        } else {
            return align;
        }

        return align;
    }

    public double traiterTexte(Node noeud) {
        String content = noeud.getTextContent();
        if (fontMetrics != null) {
            for (char c : CARACTERES_DESCENT) {
                if (content.indexOf(c) != -1) {
                    double ascent = fontMetrics.getAscent();
                    double descent = fontMetrics.getDescent();
                    return ascent / (ascent + descent);
                }
            }
        }
        return 1.0;
    }

    public double traiterMROW(Node noeud) {
        double align = 1.0;
        if (!noeud.hasChildNodes()) {
            return traiterTexte(noeud);
        }
        if (noeud.getChildNodes().getLength() == 1) {
            return selectionNoeud(noeud.getFirstChild());
        } // Système d'équation
        else {
            if (noeud.getChildNodes().getLength() == 2) {
                Node premierFils = noeud.getFirstChild();
                Node secondFils = noeud.getChildNodes().item(1);
                if (premierFils.getNodeName().equals("mo") && premierFils.getTextContent().equals("{") && secondFils.getNodeName().equals("mrow")) {
                    return traiterSYSTEME(secondFils.getFirstChild());
                }
            }
            if (noeud.getChildNodes().getLength() >= 2) {
                int nbFils = noeud.getChildNodes().getLength();
                Double[] listeAlignement = new Double[nbFils];
                for (int i = 0; i < nbFils; i++) {
                    listeAlignement[i] = selectionNoeud(noeud.getChildNodes().item(i));
                }
                Arrays.sort(listeAlignement);
                if (listeAlignement[0] == 1.0 || listeAlignement[0] == listeAlignement[nbFils - 1]) {
                    return listeAlignement[0];
                } else {
                    double alignementMax = listeAlignement[0];
                    int i = 1;
                    while(i < listeAlignement.length){
                        if(listeAlignement[i] != 1.0){
                            alignementMax = listeAlignement[i];
                        }
                        i++;
                    }
                    return (listeAlignement[0] + alignementMax) / 2;
                }
            }
        }
        return align;
    }

    public double traiterMFRAC(Node noeud) {
        double align = 0.5;
        double num = 1 + proportionMFRAC(noeud.getChildNodes().item(0));
        double den = 1 + proportionMFRAC(noeud.getChildNodes().item(1));
        align = num / (num + den);
        return align;
    }

    private double proportionMFRAC(Node noeud) {
        double valeur = 0.0;
        valeur = valeur + valeurProportionMFRAC(noeud);
        if (noeud.hasChildNodes()) {
            int nbFils = noeud.getChildNodes().getLength();
            Double[] tableauValeur = new Double[nbFils];
            for (int i = 0; i < nbFils; i++) {
                tableauValeur[i] = proportionMFRAC(noeud.getChildNodes().item(i));
            }
            Arrays.sort(tableauValeur);
            valeur = valeur + tableauValeur[nbFils - 1];
        }
        if (noeud.getNodeName().equals("msup")) {
            double valeurExposant = proportionMFRAC(noeud.getChildNodes().item(1));
            valeur = valeur - valeurExposant * 0.5 + 1 - Math.pow(1 / 4, valeurExposant);

        }
        return valeur;
    }

    private double valeurProportionMFRAC(Node noeud) {
        double valeur = 0.0;
        String balise = noeud.getNodeName();

        if (balise.equals("mrow")) {
            valeur = 0;
        }
        if (balise.equals("mn")) {
            valeur = 0;
        }
        if (balise.equals("mtext")) {
            valeur = 0;
        }
        if (balise.equals("mi")) {
            valeur = 0;
        }
        if (balise.equals("mo")) {
            valeur = 0;
        }
        if (balise.equals("font")) {
            valeur = 0;
        }
        if (balise.equals("mfrac")) {
            valeur = 1.0;
        }
        if (balise.equals("msqrt")) {
            valeur = 0.0;
        }
        if (balise.equals("msup")) {
            valeur = 0.0;
        }
        if (balise.equals("msub")) {
            valeur = 0.5;
        }
        if (balise.equals("mfenced")) {
            valeur = 0;
        }
        if (balise.equals("mover")) {
            valeur = 0;
        }
        return valeur;

    }

    public double traiterMSQRT(Node noeud) {
        if (!noeud.hasChildNodes()) {
            return 0.9;
        }
        double align = traiterMROW(noeud.getFirstChild());
        return align >= 0.8 ? align == 1.0 ? align - 0.1 : align : align + 0.1;
    }

    public double traiterMSUP(Node noeud) {
        if (!noeud.hasChildNodes()) {
            return 1.0;
        }
        double align = traiterMROW(noeud.getFirstChild());
        return align >= 0.8 ? align : align + 0.08;
    }

    public double traiterMSUB(Node noeud) {
        double align = traiterMROW(noeud) - 0.1;
        return align;
    }

    public double traiterSYSTEME(Node noeud) {
        double align = 0.6;
        return align;
    }

    public double traiterMFENCED(Node noeud) {
        double align = 1.0;
        align = traiterMROW(noeud);
        return align >= 0.9 ? align == 1.0 ? 0.8 : align - 0.1 : align + 0.1;
    }
}
