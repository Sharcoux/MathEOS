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
package bomehc.utils.objets;

/**
 *
 * @author François Billioud
 */
import bomehc.IHM;
import bomehc.utils.managers.FontManager;
import bomehc.utils.boutons.ActionComplete;
import bomehc.utils.boutons.Bouton;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import bomehc.utils.managers.Traducteur;

@SuppressWarnings("serial")
public class Calculatrice extends JDialog {

    /**
     * La précision des calculs exprimée en nombre de décimales *
     */
    public final int PRECISION = 6;
    //constantes représentant les opérateurs
    private final String PLUS = "+";
    private final String MOINS = "-";
    private final String FOIS = "*";
    private final String DIVISE = "/";
    private final String MARQUEUR_DECIMAL = Traducteur.traduire("decimal point");
    //paramètres d'affichage
    private final Font POLICE_BOUTON = FontManager.get("font calculator");
    private final Dimension DIMENSION_CHIFFRE = new Dimension(50, 40);
    private final Dimension DIMENSION_OPERATION = new Dimension(45, 31);
    /**
     * contient la liste des éléments à afficher *
     */
    private final String[] ELEMENTS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", MARQUEUR_DECIMAL, "0", PLUS, MOINS, FOIS, DIVISE};
    
    //mémoire calculatrice
    private double chiffre1;                //Valeur du premier nombre rentré
    private double contenu;                 //Valeur du contenu actuel de la calculatrice
    private String operateur = "";          //Dernier opérateur qui a été saisi
    private boolean clicOperateur = false;  //Est activé quand le dernier élément saisi est un opérateur
    private boolean update = false;         //Est activé quand le dernier élément saisi n'est pas un chiffre
    private boolean virgule = false;        //Est activé quand une virgule figure dans le nombre
    
    private final Container container;      //conteneur de la fenêtre

    //String[] tab_string = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ".", "=", "C", "+", "-", "*", "/"};
    private Calculatrice() {
        super(IHM.getMainWindow());
        this.setSize(240, 260);
        this.setTitle("Calculette");
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setAlwaysOnTop(true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        container = new Container();
        this.setContentPane(container);
    }
    
    private static Calculatrice instance;
    public static Calculatrice getInstance() {
        return instance!=null ? instance : (instance=new Calculatrice());
    }

    public void reinit() {
        container.ecran.setText("0");
        contenu = 0;
        virgule = false;
    }

    public String getDisplayedFormatedText() {
        String text = getEcran().getText();
        return MARQUEUR_DECIMAL.equals(".") ? text : text.replace(MARQUEUR_DECIMAL, ".");//ATTENTION au point dans les régex
    }
    public String getDisplayedText() { return getEcran().getText(); }
    public void displayText(String texte) { getEcran().setText(texte); }

    private Container.EcranDAffichage getEcran() { return container.ecran; }

    private class Container extends JPanel {
        private final JPanel panelOperateurs = new JPanel();        //contient les opérations
        private final JPanel panelChiffres = new JPanel();          //contient les chiffres
        private final JPanel panelEcran = new JPanel();             //contient l'écran d'affichage
        private EcranDAffichage ecran = new EcranDAffichage("0");

        private Container() {
            panelOperateurs.setPreferredSize(new Dimension(55, 225));
            panelChiffres.setPreferredSize(new Dimension(165, 225));
            panelEcran.setPreferredSize(new Dimension(220, 30));

            Bouton bouton;
            //crée les boutons du panelChiffres
            for (int i = 0; i < 12; i++) {
                bouton = new Bouton(i == 11 ? new ActionReset() : new ActionChiffre(ELEMENTS[i]));
                bouton.setPreferredSize(DIMENSION_CHIFFRE);
                panelChiffres.add(bouton);
            }

            //crée les boutons des opérations
            for (int i = 11; i < 16; i++) {
                bouton = new Bouton(i == 15 ? new ActionEgal() : new ActionOperateur(ELEMENTS[i]));
                bouton.setPreferredSize(DIMENSION_OPERATION);
                panelOperateurs.add(bouton);
            }

            //prépare l'écran d'affichage
            panelEcran.add(ecran);
            panelEcran.setBorder(BorderFactory.createLineBorder(Color.black));

            //remplit le container
            this.add(panelEcran, BorderLayout.NORTH);
            this.add(panelChiffres, BorderLayout.CENTER);
            this.add(panelOperateurs, BorderLayout.EAST);

        }

        private class EcranDAffichage extends JLabel {

            private EcranDAffichage(String label) {
                super(label);
                setFont(POLICE_BOUTON);
                setHorizontalAlignment(JLabel.RIGHT);
                setPreferredSize(new Dimension(220, 20));
                setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            }

            @Override
            public void setText(String texte) {
                super.setText(texte.replace("\\.", MARQUEUR_DECIMAL));
            }
        }
    }
    
    private void calcul() {

        if (operateur.equals(PLUS)) {
            chiffre1 += Double.parseDouble(getDisplayedFormatedText());
        }

        if (operateur.equals(MOINS)) {
            chiffre1 -= Double.parseDouble(getDisplayedFormatedText());
        }

        if (operateur.equals(FOIS)) {
            chiffre1 *= Double.parseDouble(getDisplayedFormatedText());
        }

        if (operateur.equals(DIVISE)) {
            chiffre1 /= Double.parseDouble(getDisplayedFormatedText());
        }

        try {
            displayText(String.valueOf(approxime(chiffre1)));
            contenu = chiffre1;
        } catch (ArithmeticException e) {
            reinit();
        }

    }

    public double approxime(double d) {
        return Math.round(d * Math.pow(10, PRECISION)) / Math.pow(10, PRECISION);
    }

    private final class ActionChiffre extends ActionComplete {

        private ActionChiffre(String valeur) {
            this.putValue(NAME, valeur);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //On affiche le nouveau chiffre dans le label
            String str = (String) getValue(NAME);
            if (!str.equals(MARQUEUR_DECIMAL) || !virgule) {
                if (update) {
                    update = false;
                    if (str.equals(MARQUEUR_DECIMAL)){
                        str = "0" + MARQUEUR_DECIMAL;
                        virgule = true;
                    }
                } else {
                    String ancienContenu = getDisplayedText();
                    if (str.equals(MARQUEUR_DECIMAL)) {
                        virgule = true;
                    } else {
                        if (ancienContenu.equals("0")) {
                            ancienContenu = "";
                        }
                    }
                    str = ancienContenu + str;
                }
                displayText(str);
                contenu = Double.parseDouble(getDisplayedFormatedText());    //Attention au marqueur décimal
            }
        }
    }

    private final class ActionOperateur extends ActionComplete {

        private final String operation;

        private ActionOperateur(String operation) {
            super(operation);
            this.operation = operation;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!clicOperateur) { //première opération
                chiffre1 = contenu;
                clicOperateur = true;
            } else {
                calcul();
            }
            operateur = this.operation;
            update = true;
            virgule = false;
        }
    }

    private final class ActionReset extends ActionComplete {

        private ActionReset() {
            this.putValue(NAME, "C");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            clicOperateur = false;
            update = false;
            chiffre1 = 0;
            operateur = "";
            reinit();
        }
    }

    private final class ActionEgal extends ActionComplete {

        ActionEgal() {
            this.putValue(NAME, "=");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            calcul();
            update = true;
            clicOperateur = false;
            virgule = false;
        }
    }
}
