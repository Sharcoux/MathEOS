/*
 * «Copyright 2011 Guillaume Varoquaux»
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

package bomehc.operations;

import bomehc.elements.ChangeModeListener;
import bomehc.utils.managers.ColorManager;
import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;


/**
 * Classe définissant un Panel pour mettre en forme une division
 */

public class Division extends OperationType implements ActionListener, ComponentListener, KeyListener, MouseListener, Serializable {

    private LimitedTextFieldOperations nb[] = {new LimitedTextFieldOperations(6), new LimitedTextFieldOperations(5)}; // Dividende et Diviseur
    public Font police = new Font("Arial", Font.BOLD, (int) (50)); //Crée une police
    private int nbLigne = 1;     // Nombre de ligne total (dividende + lignes de champ de texte) de calcul pour la division
    private int nbDivid = 4;     // Nombre de chiffre au dividende
    private int nbDivis = 3;     // Nombre de chiffre au diviseur
    private int nbColonneAjoute = 0;     // Nombre de colonnes ajoutées avec le bouton "Plus"
    public List<LimitedTextFieldOperations> resultat = new ArrayList<LimitedTextFieldOperations>();   // Création du tableau des champs de résultat
    public List<List<JPanel>> panChiffre = new ArrayList<List<JPanel>>();    // Tableau des JPanel de mise en forme des chiffres
    public List<List<JLabel>> chiffre = new ArrayList<List<JLabel>>();    // Tableau des JLabel contenant les chiffre
    public List<List<LimitedTextFieldOperations>> fieldChiffre = new ArrayList<List<LimitedTextFieldOperations>>();    // Listes contenant les champs de chiffre de calcul
    public List<List<VirguleOperation>> panVirgule = new ArrayList<List<VirguleOperation>>();    // Panel pouvant recevoir la virgule du dividende (0) ou du diviseur (1) ou du résultat (2)
    public int positionVirgule[] = {-1, -1, -1}; // Tableau renvoyant le Panel de chiffre associé à la virgule pour le dividende (0) et le diviseur (1),  et le résultat (2) ou renvoie -1 si pas de virgule
    public JLabel virgule[] = new JLabel[3]; //Tableau de virgules au dividende (0) et au diviseur (1) et au résultat (2)
    public List<JButton> addSoustraction = new ArrayList<JButton>();    // Liste de boutons permettant de faire apparaitre ou de cacher les lignes de soustraction
    public List<JLabel> labAddSoustraction = new ArrayList<JLabel>();   // Tableau des labels contenus sur les boutons d'apparition de soustraction
    public int positionSoustraction = -1;   //Variable renvoyant l'indice de la ligne de soustraction active, ou -1 s'il y en a pas
    public List<List<OperationType.LimitedTextFieldOperations>> fieldSoustraction = new ArrayList<List<LimitedTextFieldOperations>>(); // Champs de chiffre pour les soustractions intermédiaires
    public JButton plus = new JButton();    // Bouton permettant d'ajouter une colonne
    public JButton moins = new JButton();   // Bouton permettant d'enlever une colonne
    public JLabel more = new JLabel("+");    // Label du symbole "+" pour ajouter une colonne
    public JLabel less = new JLabel("-");    //Label du symbole "-" pour enlever une colonne
    public BoutonIcone modifierLigne[] = {null}; // Bouton permettant de modifier le dividende (0) ou le diviseur (1)
    private boolean etat[] = {false, false}; // Tableau qui renvoie "false" si le nombre est à l'état de champ de texte et "true" s'il est mis en forme
    public float coef2 = 1;  // Proportionnalité par rapport au nombre de case contenue

  /**
  * Crée le Panel de Division et sauvegarde la date de création
  * Remplit également les listes des éléments de la Division (cases de résultats, de retenues...)
  */

    public Division() {

        this.addComponentListener(this);    // Ajout du pannel à la liste des écouteurs pour le redimenssionnement

        // Définition de l'espace de travail pour l'opération

        // Création du Panel de travail
        this.setBackground(ColorManager.get("color panel operation"));
        this.setLayout(null);

        // Création des champs de texte
        for (int i = 0; i <= 1; i++) {
            nb[i].addKeyListener(this);
            this.add(nb[i]);    // Ajout de ce champ de texte à la fenêtre
            nb[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }

        // Création des Labels de virgule
        for (int i = 0; i <= 2; i++) {
            virgule[i] = new JLabel(",");
        }

        // Création du bouton d'ajout de ligne de calcul et de suppression
        plus.setLayout(null);
        plus.add(more);
        plus.addActionListener(this);
        plus.addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        plus.setFocusable(false);
        moins.setLayout(null);
        moins.add(less);
        moins.addActionListener(this);
        moins.addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        moins.setFocusable(false);


        // Création des boutons de modification du dividende et du diviseur
         modifierLigne = new BoutonIcone[2];

        for (int i=0; i<= 1; i++)
        {   modifierLigne[i] = new BoutonIcone("images/Modifier_Down.png", "images/Modifier_Up.png");
            modifierLigne[i].addMouseListener(this);
            modifierLigne[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }

        
        // On initialise les listes contenant les listes de Panel du dividende (liste 0) et du diviseur (liste 1)
        panChiffre.add(new ArrayList<JPanel>());
        panChiffre.add(new ArrayList<JPanel>());
        chiffre.add(new ArrayList<JLabel>());
        chiffre.add(new ArrayList<JLabel>());
        panVirgule.add(new ArrayList<VirguleOperation>());
        panVirgule.add(new ArrayList<VirguleOperation>());
        panVirgule.add(new ArrayList<VirguleOperation>());

    }

   /**
   * Crée un nouveau Panel de Division
   * @return Nouveau Panel de Division
   */

    @Override
    public Division nouveau() {
        return new Division();
    }

  /**
  * Méthode qui vérifie sur le nombre saisi est correct
  * et qui met en forme les nombres pour l'opération
  * en effectuant les modifications nécessaires
  * Est appelé à l'appui de la touche "Entrée" et
  * concerne la ligne dont le champ de texte contient le curseur
  */

    public void keyPressed(KeyEvent e) {        // Pour détecter quand un nombre est tapé dans le champ de texte

        int i = 0;
        int j = 0;
        String avVirgule = null;    // Chaine qui contiendra la partie entière du nombre
        String apVirgule = null;    // Chaine qui contiendra la partie décimale du nombre
        String nbSaisi = null;

        if (e.getKeyCode() == 10 && (e.getComponent() == nb[0] || e.getComponent() == nb[1])) // Touche "entrée" pressée
        {
            if (nb[0].hasFocus() == true) // Curseur dans chemp de texte Dividende
            {
                i = 0;
            }
            if (nb[1].hasFocus() == true) {
                i = 1;
            }

            if (nb[i].testDecimal() == true) // Le nombre saisi est correct
            {
                if ((i == 0 && testFormat0() == true) || (i == 1 && testFormat1() == true)) // Le nombre est bien au format autorisé et la somme des deux nombres saisis n'est pas trop longue
                {
                    // On teste la présence d'une virgule dans le nombre saisi pour stocker sa position
                    for (j = 0; j <= nb[i].getText().length() - 1; j++) {
                        int codeAscii = (int) nb[i].getText().charAt(j);
                        if (codeAscii == 44) // Il y a une virgule dans le nombre
                        {
                            positionVirgule[i] = j;
                            apVirgule = nb[i].getText().substring(j + 1, nb[i].getText().length());   // On crée deux sous-chaine
                            avVirgule = nb[i].getText().substring(0, j);
                            nbSaisi = avVirgule.concat(apVirgule);

                        }
                    }
                    if (nbSaisi == null) {
                        nbSaisi = nb[i].getText();
                    }
                    if (i == 0) {
                        nbDivid = nbSaisi.length();
                    }
                    if (i == 1) {
                        nbDivis = nbSaisi.length();
                    }

                    remove(nb[i]);
                    for (j = 0; j <= nbSaisi.length() - 1; j++) {
                        JPanel panelChiffre = new JPanel();
                        panelChiffre.setLayout(null);
                        panelChiffre.setBackground(ColorManager.get("color chiffre background"));
                        JLabel numero = new JLabel("" + nbSaisi.charAt(j));
                        numero.setText("" + nbSaisi.charAt(j));
                        if (j >= 1) // On crée un Panel pour la virgule que si le nombre a plus d'un chiffre
                        {
                            VirguleOperation virguleChiffre = new VirguleOperation();
                            virguleChiffre.addMouseListener(this);
                            if (positionVirgule[i] == j) {
                                virguleChiffre.add(virgule[i]);
                            }

                            panVirgule.get(i).add(j - 1, virguleChiffre);
                            panVirgule.get(i).get(j - 1).addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
                            this.add(panVirgule.get(i).get(j - 1));
                        }
                        chiffre.get(i).add(j, numero);
                         if (i ==0)
                            chiffre.get(i).get(j).setForeground(ColorManager.get("color chiffre line1"));
                        else
                            chiffre.get(i).get(j).setForeground(ColorManager.get("color chiffre line2"));
                        panelChiffre.add(numero);
                        panChiffre.get(i).add(j, panelChiffre);
                        this.add(panChiffre.get(i).get(j));
                        panChiffre.get(i).get(j).addMouseListener(this);
                        panChiffre.get(i).get(j).addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
                    }

                    etat[i] = true;
                    add(modifierLigne[i]);

                } 
            } 
            
            if (etat[0] == true && etat[1] == true) // Les deux nombres sont mis en forme
            {
                if (nbDivid - nbDivis >= -1) {
                    nbLigne = nbDivid - nbDivis + 2;
                } else {
                    nbLigne = 1;
                }
                afficherResultat();
            }
            // On détermine qui a le focus après la mise en forme
            int indice=0;
            while(indice<2)
            {   if(etat[indice] == false)
                {   nb[indice].requestFocus();
                    break;
                }
                indice++;
            }
            positionComponent();
        }
    }

    public void keyTyped(KeyEvent e) {}

    /**
     * Met à jour la ligne de soustraction en cours dans le cas où on modifie la ligne au-dessus
     * (ajout ou suppression de chiffre)
     * @param e KeyEvent
     */

    public void keyReleased(KeyEvent e) {
        // Cas où l'on ajoute un chiffre sur la ligne au-dessus
        if (positionSoustraction !=-1)
        {   AfficherSoustraction(positionSoustraction);
            positionComponent();
            // Cas où l'on enlève un chiffre sur la ligne au-dessus
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyChar()== KeyEvent.VK_DELETE) // On tape sur la touche "Backspace" ou "Suppr"
                for (int i = 0 ; i<= fieldChiffre.size()-1 ; i++)
                    for (int j = 0 ; j<= fieldChiffre.get(i).size()-1 ; j++)
                        if(e.getComponent() == fieldChiffre.get(i).get(j))
                        {   EffacerSoustraction(positionSoustraction);
                        }
        }
     repaint();
    }

    /**
     * Concerne le bouton "+", le bouton "-", et les boutons d'ajout de soustraction. <br/>
     * Le bouton "+" ajouter une colonne pour les zéro de la division.
     * Le bouton "-" supprimer une colonne de zéro de la division.
     * Le bouton d'ajout de soustraction permet d'afficher une soustraction intermédiaire
     * et de fermer celle qui était active s'il y en avait une. Cela appelle alors la méthode
     * "AfficherSoustraction". Ce bouton devient également un bouton de suppression de soustraction
     * si la soustraction à la ligne concernée es déjà affichée.
     * @param e ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == plus) {
            nbColonneAjoute++;
            nbDivid++;
            nbLigne++;

            if (nbColonneAjoute == 1) {
                this.add(moins);
            }
            panChiffre.get(0).add(nbDivid - 1, new JPanel());
            panChiffre.get(0).get(nbDivid - 1).setLayout(null);
            chiffre.get(0).add(new JLabel("0"));
            panChiffre.get(0).get(nbDivid - 1).add(chiffre.get(0).get(nbDivid - 1));
            this.add(panChiffre.get(0).get(nbDivid - 1));
            // On ajoute un champ de soustraction en-dessous du premier "0" rajouté par le bouton "+"
            if (fieldSoustraction != null && !fieldSoustraction.isEmpty())
                if(fieldSoustraction.get(0).size()-1 < nbDivis)
                {   LimitedTextFieldOperations champ = new LimitedTextFieldOperations(1);
                    champ.setHorizontalAlignment(LimitedTextFieldOperations.CENTER);
                    champ.setBackground(ColorManager.get("color optional field background"));
                    fieldSoustraction.get(0).add(nbDivis, champ);
                    this.add(fieldSoustraction.get(0).get(nbDivis));
                }

            fieldChiffre.add(new ArrayList<LimitedTextFieldOperations>());
            for (int i = 0; i <= nbLigne - 3; i++) {
                LimitedTextFieldOperations champ = new LimitedTextFieldOperations(1);
                champ.setHorizontalAlignment(LimitedTextFieldOperations.CENTER);
                champ.addKeyListener(this);
                fieldChiffre.get(i).add(nbDivid - 1 - i, champ);
                this.add(fieldChiffre.get(i).get(nbDivid - 1 - i));
            }
            for (int j = 0; j <= nbDivis - 1; j++) {
                LimitedTextFieldOperations champ = new LimitedTextFieldOperations(1);
                champ.setHorizontalAlignment(LimitedTextFieldOperations.CENTER);
                champ.addKeyListener(this);
                fieldChiffre.get(nbLigne - 2).add(j, champ);
                this.add(fieldChiffre.get(nbLigne - 2).get(j));
            }

            LimitedTextFieldOperations champ = new LimitedTextFieldOperations(1);
            champ.setHorizontalAlignment(LimitedTextFieldOperations.CENTER);
            resultat.add(nbDivid - 1, champ);
            this.add(resultat.get(nbDivid - 1));

            panVirgule.get(0).add(nbDivid - 2, new VirguleOperation());
            panVirgule.get(0).get(nbDivid - 2).addMouseListener(this);
            this.add(panVirgule.get(0).get(nbDivid - 2));

            panVirgule.get(2).add(nbDivid - 2, new VirguleOperation());
            panVirgule.get(2).get(nbDivid - 2).addMouseListener(this);
            this.add(panVirgule.get(2).get(nbDivid - 2));

            JLabel lab = new JLabel("+");
            JButton bouton = new JButton();
            bouton.setLayout(null);
            bouton.addActionListener(this);
            labAddSoustraction.add(lab);
            bouton.add(lab);
            addSoustraction.add(nbLigne - 2, bouton);
            this.add(addSoustraction.get(nbLigne - 2));
        }

        if (e.getSource() == moins) {
            nbColonneAjoute--;
            nbDivid--;
            nbLigne--;

            if (nbColonneAjoute == 0) {
                this.remove(moins);
            }

            this.remove(panChiffre.get(0).get(nbDivid));
            panChiffre.get(0).remove(nbDivid);
            chiffre.get(0).remove(nbDivid);
            
            // On efface la dernière colonne de la division avant la barre verticale
            for (int i = 0; i <= nbLigne - 2; i++) {
                this.remove(fieldChiffre.get(i).get(nbDivid - i));
                if (positionSoustraction !=-1)
                    EffacerSoustraction(positionSoustraction);
                fieldChiffre.get(i).remove(nbDivid - i);
            }

            // On efface la dernière ligne de la division
            for (int j = nbDivis - 1; j >= 0; j--) {
                this.remove(fieldChiffre.get(nbLigne - 1).get(j));
                fieldChiffre.get(nbLigne - 1).remove(j);
            }

            // Si la soustraction est active et en dernière position, il faut l'effacer
            if (positionSoustraction == nbLigne-1)
               for(int j=0;j<=fieldSoustraction.get(nbLigne-1).size()-1;j++)
                {   this.remove(fieldSoustraction.get(nbLigne-1).get(j)); 
                    fieldSoustraction.get(nbLigne-1).get(j).setText("");
                    positionSoustraction =-1;   // Il n'y a alors plus de soustraction affichée
                }
           
            
            if (positionVirgule[0] == nbDivid) {
                positionVirgule[0] = -1;
            }

            if (positionVirgule[2] == nbDivid) {
                positionVirgule[2] = -1;
            }

            this.remove(resultat.get(nbDivid));
            resultat.remove(nbDivid);

            this.remove(panVirgule.get(0).get(nbDivid - 1));
            panVirgule.get(0).remove(nbDivid - 1);

            this.remove(panVirgule.get(2).get(nbDivid - 1));
            panVirgule.get(2).remove(nbDivid - 1);

            // On enlève le bouton de soustraction de la ligne correspondante
            this.remove(addSoustraction.get(nbLigne - 1));
            labAddSoustraction.remove(nbLigne - 1);
            addSoustraction.remove(nbLigne - 1);
        }


        for (int i = 0; i <= addSoustraction.size() - 1; i++) {
            if (e.getSource() == addSoustraction.get(i)) {
                if (labAddSoustraction.get(i).getText().equals("+")) // Le bouton est un ajout de ligne de soustraction
                {   boolean affichageSoustraction = false; // Booleen qui définit si on affiche les soustractions (true) ou s'il n'y a rien à afficher (false)
                    if (i !=0)
                    {    for (int j=0; j<=fieldChiffre.get(i-1).size()-1; j++)    // On regarde quelles sont les champs non vide
                            if (!fieldChiffre.get(i-1).get(j).getText().equals(""))
                            {   j = fieldChiffre.get(i-1).size();
                                affichageSoustraction = true;
                            }
                    }
                    else
                        affichageSoustraction = true;

                    if (affichageSoustraction == true)  // Il y a quelque chose à afficher, on l'affiche
                    {   
                        // On remplace la Label "+" par un Label "-"
                        addSoustraction.get(i).remove(labAddSoustraction.get(i));
                        labAddSoustraction.get(i).setText("-");
                        addSoustraction.get(i).add(labAddSoustraction.get(i));
                        // On donne à la variable "positionSoustraction" l'indice de la ligne sélectionnée
                        if (positionSoustraction == -1) {   //Cas où il n'y avait pas de ligne activé
                            positionSoustraction = i;
                        } else {    // Cas où il y avait déjà une ligne activée, on la masque
                            addSoustraction.get(positionSoustraction).remove(labAddSoustraction.get(positionSoustraction));
                            labAddSoustraction.get(positionSoustraction).setText("+");
                            addSoustraction.get(positionSoustraction).add(labAddSoustraction.get(positionSoustraction));
                            if (positionSoustraction != 0) {
                                for (int j = 0; j <= fieldSoustraction.get(positionSoustraction).size()-1; j++) {
                                    this.remove(fieldSoustraction.get(positionSoustraction).get(j));
                                }
                            } else {
                                for (int j = 0; j <= fieldSoustraction.get(positionSoustraction).size()-1; j++) {
                                    this.remove(fieldSoustraction.get(positionSoustraction).get(j));
                                }
                            }   // Une fois la ligne masquée, on réaffecte la valeur de la nouvelle ligne active
                            positionSoustraction = i;
                        }
                        AfficherSoustraction(i);
                    }
                }

                else // Le bouton est une suppression de ligne de soustraction
                {   addSoustraction.get(i).remove(labAddSoustraction.get(i));
                    labAddSoustraction.get(i).setText("+");
                    addSoustraction.get(i).add(labAddSoustraction.get(i));
                    if (i != 0) {
                        for (int j = 0; j <= fieldSoustraction.get(i).size()-1; j++) {
                            this.remove(fieldSoustraction.get(i).get(j));
                        }
                    } else if (nbDivid > nbDivis) {
                        for (int j = 0; j <= nbDivis; j++) {
                            this.remove(fieldSoustraction.get(i).get(j));
                        }
                    } else {
                        for (int j = 0; j <= nbDivis - 1; j++) {
                            this.remove(fieldSoustraction.get(i).get(j));
                        }
                    }

                   positionSoustraction = -1;
                }
            }
        }
        positionComponent();
    }

    /**
     * Méthode qui détermine quelles seront les cases de soustraction à afficher.
     * On affiche une case de soustraction sous chaque case de la ligne précédente qui est remplie.
     * On ajoute également éventullement une case sur la ligne du dessous pour d'éventuelles retenues.
     * @param i ligne ou l'on souhaite afficher la soustraction; la première ligne est 0.
     */

    public void AfficherSoustraction(int i){

                        // Création des champs pour la soustraction
                        for (int j = fieldSoustraction.size(); j <= positionSoustraction; j++)    // On crée les listes vides pour ajouter les cases de soustraction
                            fieldSoustraction.add(new ArrayList<LimitedTextFieldOperations>());

                        if (i != 0) {   //Cas où la ligne sélectionnée n'est pas la première ligne
                            for (int j=0; j<=fieldChiffre.get(positionSoustraction-1).size()-1; j++)    // On regarde quelles sont les champs non vide
                                if (!fieldChiffre.get(positionSoustraction-1).get(j).getText().equals("") )  // On ajoute une case pour la soustraction en-dessous des cases non vides ou en-dessous de la case qui a le caret (et donc qui vient d'être remplie)
                                {   for (int k=fieldSoustraction.get(i).size(); k<=j+1; k++)
                                    {   LimitedTextFieldOperations champ = new LimitedTextFieldOperations(1);
                                        champ.setHorizontalAlignment(LimitedTextFieldOperations.CENTER);
                                        champ.setBackground(ColorManager.get("color optional field background"));
                                        fieldSoustraction.get(i).add(champ);
                                        fieldSoustraction.get(i).get(k).addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
                                    }
                                    if (j==0)   // On ajoute une case supplémentaire sur la ligne en-dessous
                                    {   this.add(fieldSoustraction.get(i).get(0));
                                        this.add(fieldSoustraction.get(i).get(1));
                                        fieldSoustraction.get(i).get(0).addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
                                        fieldSoustraction.get(i).get(1).addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
                                    }
                                    else    // On affiche les cases sur la ligne de soustraction
                                    {    this.add(fieldSoustraction.get(i).get(j+1));
                                         fieldSoustraction.get(i).get(j+1).addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
                                    }
                                 }

                        }   // Cas où il s'agit de la 1ère ligne de soustraction
                        else if (nbDivid > nbDivis) {
                            for (int j = 0; j <= nbDivis; j++) {
                                LimitedTextFieldOperations champ = new LimitedTextFieldOperations(1);
                                champ.setHorizontalAlignment(LimitedTextFieldOperations.CENTER);
                                champ.setBackground(ColorManager.get("color optional field background"));
                                fieldSoustraction.get(i).add(champ);
                                fieldSoustraction.get(i).get(j).addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
                                this.add(fieldSoustraction.get(i).get(j));
                            }
                        } else {
                            for (int j = 0; j <= nbDivis - 1; j++) {
                                LimitedTextFieldOperations champ = new LimitedTextFieldOperations(1);
                                champ.setHorizontalAlignment(LimitedTextFieldOperations.CENTER);
                                champ.setBackground(ColorManager.get("color optional field background"));
                                fieldSoustraction.get(i).add(champ);
                                fieldSoustraction.get(i).get(j).addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
                                this.add(fieldSoustraction.get(i).get(j));
                            }
                        }
    }

    /**
     * Méthode qui supprimes les cases de soustraction s'il n'y a plus de chiffre dans la case associée
     * de la ligne au-dessus.
     * @param i :  ligne ou l'on souhaite effacer la soustraction; la première ligne est 0.
     */

    public void EffacerSoustraction(int i){
        boolean ligneVide = true;   // Booleen qui détermine s'il y a encore une case de soustraction (false) ou pas (true)
        if(i==0)
        {   for (int j=0; j<= fieldSoustraction.get(0).size()-1;j++ )
            {   this.remove(fieldSoustraction.get(i).get(j));
                fieldSoustraction.get(i).get(j).setText("");
            }
        }
        else
        {
            for (int j=0; j<= fieldChiffre.get(i-1).size()-1;j++ )
            {   if (fieldChiffre.get(i-1).get(j).hasFocus() || (!fieldChiffre.get(i-1).get(j).isShowing() &&!fieldChiffre.get(i-1).get(j).getText().equals("")) )
                {   this.remove(fieldSoustraction.get(i).get(j+1));   // On enlève le champ de soustraction correspondant à la case qui a le caret et qui vient donc d'être vidée
                    fieldSoustraction.get(i).get(j+1).setText("");
                    //fieldSoustraction.get(i).clear();
                    if (j==0)   // Si c'est le 1er champ qui a le caret, on retire également la case de soustraction de la ligne en dessous
                    {    this.remove(fieldSoustraction.get(i).get(0));
                         fieldSoustraction.get(i).get(0).setText("");
                    }
                }
                else    // Pour toutes les autres cases, on vérifie si elles sont non vides
                    if ( !fieldChiffre.get(i-1).get(j).getText().equals(""))
                    {   ligneVide = false;  // Si une des case au moins est non vide, on passe "ligneVide" à false
                    }
            }
        }
        if (ligneVide == true)  // Il n'y a plus de case de soustraction, on referme la ligne
        {   addSoustraction.get(i).remove(labAddSoustraction.get(i));
            labAddSoustraction.get(i).setText("+");
            addSoustraction.get(i).add(labAddSoustraction.get(i));
            positionSoustraction = -1;
            positionComponent();
        
        }

    }

    /**
    * Gère les boutons "ModifierLigne"
    * Permet de modifier une ligne de division
    * @param e MouseEvent
    */

    public void mouseClicked(MouseEvent e) {
        // Caractérise les boutons de modification de ligne
          for (int i=0; i<=1; i++)    // On écoute chaque ligne
                if(e.getSource() == modifierLigne[i])    // Si le bouton "modifierLigne" d'une ligne est activé, on réinitialise tous les paramètres
                {    // On écrit le nombre dans le champ de texte concerné
                    String chiffreChamp = "";
                    for (int j=0; j<panChiffre.get(i).size()-nbColonneAjoute;j++)
                        chiffreChamp = chiffreChamp + chiffre.get(i).get(j).getText();

                    if (positionVirgule[i]!=-1 && positionVirgule[i]<(panChiffre.get(i).size()-nbColonneAjoute))
                    {   chiffreChamp = chiffreChamp.substring(0, positionVirgule[i])+","+chiffreChamp.substring(positionVirgule[i],panChiffre.get(i).size()-nbColonneAjoute);
                    }
                    nb[i].setText(chiffreChamp);

                    this.remove(modifierLigne[i]);
                    this.remove(virgule[i]);
                    this.remove(virgule[2]);    // On supprime la virgule au résultat
                    etat[i] = false;
                    for (int j=0; j<=chiffre.get(i).size()-1; j++)
                    {   panChiffre.get(i).get(j).remove(chiffre.get(i).get(j));
                        this.remove(panChiffre.get(i).get(j));
                    }
                    panChiffre.get(i).clear();
                    chiffre.get(i).clear();
                    for (int j=0; j<=panVirgule.get(i).size()-1; j++)
                    {   panVirgule.get(i).get(j).remove(virgule[i]);
                        this.remove(panVirgule.get(i).get(j));
                    }
                    // On supprime les Labels "+" et "-"
                    for (int j=0; j<=addSoustraction.size()-1; j++)
                    {   this.remove(addSoustraction.get(j));
                    }
                    labAddSoustraction.clear();
                    addSoustraction.clear();
                    // On vide le contenu des champs de soustraction
                    if ( positionSoustraction != -1)// Si positionSoutraction est différent de -1, on supprime les cases
                        for (int j=0; j<=fieldSoustraction.get(positionSoustraction).size()-1; j++)
                            this.remove(fieldSoustraction.get(positionSoustraction).get(j));
                    fieldSoustraction.clear();
                    this.remove(moins);
                    this.remove(plus);
                    // On réinitialise les paramètres initiaux
                    if (i==0)
                        nbDivid = 4;
                    else
                        nbDivis = 3;
                    this.add(nb[i]);
                    //nb[i].requestFocus();
                    nbLigne =1;
                    positionSoustraction = -1;
                        // On réinitialise les paramètres de virgule de "i" et du résultat
                    positionVirgule[i] = -1;
                    positionVirgule[2] = -1;  
                    if (i==0)
                    nbColonneAjoute = 0;

                    afficherResultat();  // On met en forme de résultat et les retenues
                    positionComponent();  // On remet en place les éléments qui ont été mis en forme (virgule, panChiffre, retenu)
                }
    }

     /**
     * Méthode qui gère le clic sur les Panels de Virgules et sur les boutons de modification de ligne
     * Si on clic sur un Panel de Virgule, soit il y avait déjà une virgule sur ce Panel, et on l'enlève.
     * Soit il n'y avait pas de virgule, dans ce cas on l'affiche et on efface la virgule déjà affichée
     * s'il y en avait une. <br/>
     * Change l'affichage des boutons "ModifierLigne" lorsqu'ils sont appuyés
     * @param e MouseEvent
     */
    public void mousePressed(MouseEvent e) {
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= panVirgule.get(i).size() - 1; j++) {
                if (e.getSource() == panVirgule.get(i).get(j)) // Clic sur une virgule
                {
                    if (positionVirgule[i] == j + 1)    // Cas où il y a déjà une virgule sur le Panel cliqué
                    {   panVirgule.get(i).get(j).remove(virgule[i]);    // On enlève alors la virgule
                        panVirgule.get(i).get(j).remove(VirguleOperation.virgule);
                        positionVirgule[i] = -1;
                        repaint();
                    }
                    else    // Dans cas cas, il faut ajouter une virgule, et effacer la précédente s'il y en a une
                    {   if (positionVirgule[i] != -1)   // Une virgule était déjà présente, on l'enlève
                        {    panVirgule.get(i).get(positionVirgule[i] - 1).remove(virgule[i]);
                        }
                        panVirgule.get(i).get(j).add(virgule[i]);   // on ajoute la nouvelle virgule
                        positionVirgule[i] = j + 1;
                        repaint();
                    }
                }
            }
        }

  for (int i=0; i<=panChiffre.size()-1; i++)
            for (int j=0; j<=panChiffre.get(i).size()-1; j++)
                if (e.getSource() == panChiffre.get(i).get(j))
                {   panChiffre.get(i).get(j).setBorder(BorderFactory.createLineBorder(Color.black));
                    String nouveauChiffre = JOptionPane.showInputDialog(this, "Nouveau chiffre :", "" );
                   panChiffre.get(i).get(j).setBorder(null);
                    if (nouveauChiffre != null && !nouveauChiffre.equals(""))
                    {   int codeAscii = (int) nouveauChiffre.charAt(0);
                        if (codeAscii <= 57 && codeAscii >=48)
                        {   panChiffre.get(i).get(j).remove(chiffre.get(i).get(j));
                            chiffre.get(i).get(j).setText(nouveauChiffre.substring(0, 1));
                            panChiffre.get(i).get(j).add(chiffre.get(i).get(j));
                            positionComponent();
                        }
                    }

                }
    }

     /**
     * Change l'affichage des boutons "ModifierLigne" lorsqu'ils sont relachés
     * @param e MouseEvent
     */

    public void mouseReleased(MouseEvent e) {
//        for (int i=0; i<=1; i++)    // On écoute chaque ligne
//                if(e.getSource() == modifierLigne[i])    // Si le Panel "modifierLigne" d'une ligne est activé
//                {   modifierLigne[i].setIcon();
//                     positionComponent();
//                }
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    /**
    * Gère la position des des barres de division en fonction du nombre de ligne d'opération et de la taille de la fenêtre.
    * Gère également la position des éléments de soustraction intermédiaire,
    * le "-" et la barre de soustraction.
    * @param g Le Graphics
    */

    @Override
    public void paintComponent(Graphics g) {

        CalculCoef();
        Graphics2D g2d = (Graphics2D) g; // Crée un Graphique de type 2d
        super.paintComponent(g); // Redessine le Panel avant d'ajouter les composants 

        Stroke epaisseur = new BasicStroke((int) (6 * coef * coef2));    // Crée une variable contenant l'épaisseur du trait (ici : 6)
        g2d.setStroke(epaisseur); // Donne au trait son épaisseur


        // Tracée du trait vertical
        g2d.drawLine((int) (x0 + 400 * coef), (int) (70 * coef), (int) (x0 + 400 * coef), (int) ((70 + 80 * (nbLigne + 1) * coef2) * coef));

        // Tracée du trait horizontal
        g2d.drawLine((int) (x0 + 400 * coef), (int) ((70 + 80 * coef2) * coef), (int) (x0 + (400 + 77 * nbDivis * coef2) * coef), (int) ((70 + 80 * coef2) * coef));

        // Tracée des éléments de la soustraction

        if (positionSoustraction != -1) {
            if (positionSoustraction != 0) {   // Tracée du trait de soustraction
                int lastIndice = 0;     //Dernière case de soustraction pour tracer le trait
                /// On identifie la valeur de lastIndice selon les cases de soustractions ajoutées
                for (int j=0; j<=fieldChiffre.get(positionSoustraction-1).size()-1; j++)    // On regarde quelles sont les champs non vide
                        if (!fieldChiffre.get(positionSoustraction-1).get(j).getText().equals(""))
                            lastIndice = j;
                        
                g2d.drawLine((int) (x0 + (397 - 77 * (nbDivid - positionSoustraction + 1) * coef2) * coef), (int) ((70 + (70 + 80 * (positionSoustraction + 1)) * coef2) * coef), (int) (x0 + (397 - (10 + 77 * (nbDivid - positionSoustraction + 1 - lastIndice-1)) * coef2) * coef), (int) ((70 + (70 + 80 * (positionSoustraction + 1)) * coef2) * coef));
                // Tracée du "-" de la soustraction
                g2d.drawLine((int) (x0 + (397 - (50 + 77 * (nbDivid - positionSoustraction + 1)) * coef2) * coef), (int) ((70 + (30 + 80 * (positionSoustraction + 1)) * coef2) * coef), (int) (x0 + (397 - (20 + 77 * (nbDivid - positionSoustraction + 1)) * coef2) * coef), (int) ((70 + (30 + 80 * (positionSoustraction + 1)) * coef2) * coef));
            } else {    // Tracée du trait de soustraction
                if (nbDivid > nbDivis) {
                    g2d.drawLine((int) (x0 + (397 - 77 * (nbDivid - positionSoustraction) * coef2) * coef), (int) ((70 + (70 + 80 * (positionSoustraction + 1)) * coef2) * coef), (int) (x0 + (397 - (10 + 77 * (nbDivid - positionSoustraction - (nbDivis + 1))) * coef2) * coef), (int) ((70 + (70 + 80 * (positionSoustraction + 1)) * coef2) * coef));
                } else {
                    g2d.drawLine((int) (x0 + (397 - 77 * (nbDivid - positionSoustraction) * coef2) * coef), (int) ((70 + (70 + 80 * (positionSoustraction + 1)) * coef2) * coef), (int) (x0 + (397 - (10 + 77 * (nbDivid - positionSoustraction - (nbDivis))) * coef2) * coef), (int) ((70 + (70 + 80 * (positionSoustraction + 1)) * coef2) * coef));
                }
                // Tracée du "-" de la soustraction
                g2d.drawLine((int) (x0 + (397 - (50 + 77 * (nbDivid - positionSoustraction)) * coef2) * coef), (int) ((70 + (30 + 80 * (positionSoustraction + 1)) * coef2) * coef), (int) (x0 + (397 - (20 + 77 * (nbDivid - positionSoustraction)) * coef2) * coef), (int) ((70 + (30 + 80 * (positionSoustraction + 1)) * coef2) * coef));
            }
        }

        g2d.setStroke(new BasicStroke(1));
    }

    /**
    * Méthode qui met en forme les cases de résultats et les retenues en fonction
    * du nombre de chiffres dans la division
    */

    public void afficherResultat() {

        int i = 0;
        int j = 0;

        // On réinitialise les cases de résultats ou de retenus
        for (j = 0; j <= resultat.size() - 1; j++) {
            this.remove(resultat.get(j));
        }
        resultat.clear();
        
        for (i = 0; i <= fieldChiffre.size() - 1; i++) {
            for (j = 0; j <= fieldChiffre.get(i).size() - 1; j++) {
                this.remove(fieldChiffre.get(i).get(j));
            }
        }
        fieldChiffre.clear();

        // On supprime les Panels de Virgules au résultat
        for (j = 0; j <= panVirgule.get(2).size() - 1; j++) {
            this.remove(panVirgule.get(2).get(j));
        }
        panVirgule.get(2).clear();
        


        if (etat[0] == true && etat[1] == true) {   // On réaffiche les cases de résultat et la virgule
            // On remplit la liste de Champ au résultat

            for (j = 0; j <= nbDivid - 1; j++) {
                LimitedTextFieldOperations champ = new LimitedTextFieldOperations(1);
                champ.setHorizontalAlignment(LimitedTextFieldOperations.CENTER);
                resultat.add(champ);
                resultat.get(j).addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
                this.add(resultat.get(j));  // On affiche les cases de résultat

                if (j >= 1) // On crée un Panel pour la virgule que si le nombre a plus d'un chiffre
                {
                    VirguleOperation virguleChiffre = new VirguleOperation();
                    virguleChiffre.addMouseListener(this);
                    panVirgule.get(2).add(j - 1, virguleChiffre);
                    panVirgule.get(2).get(j-1).addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
                    this.add(panVirgule.get(2).get(j - 1));
                }
            }


            // Affichage des Champs de texte
  
            for (i = 0; i <= nbLigne - 2; i++) {
                fieldChiffre.add(i, new ArrayList<LimitedTextFieldOperations>());
                JLabel lab = new JLabel("+");
                JButton bouton = new JButton();
                bouton.setLayout(null);
                bouton.addActionListener(this);
                labAddSoustraction.add(i, lab);
                bouton.add(lab);
                addSoustraction.add(i, bouton);
                addSoustraction.get(i).addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
                this.add(addSoustraction.get(i));
                
                for (j = 0; j <= nbDivid - 1 - i; j++) {
                    LimitedTextFieldOperations champ = new LimitedTextFieldOperations(1);
                    champ.setHorizontalAlignment(LimitedTextFieldOperations.CENTER);
                    champ.addKeyListener(this);
                    fieldChiffre.get(i).add(j, champ);
                    fieldChiffre.get(i).get(j).addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
                    this.add(fieldChiffre.get(i).get(j));  // On affiche les cases de résultat
                }
            }

            // Affichage des boutons "Plus" et "Moins"
            this.add(plus);
            if (nbColonneAjoute >=1)
                this.add(moins);
            positionComponent();    // Positionnement des cases de résultats, des virgules de résultats

            resultat.get(0).requestFocus();
        }
    }

    /**
    * Vérifie que le nombre saisi au dividende est bien au format 12345 ou
    * avec une virgule en plus (soit 5 entiers ou 5 entiers et une virgule)
    * @return true si le nombre est au format correct; false sinon
    */

    public boolean testFormat0() {
        String str = nb[0].getText();
        int cptVirgule = 0;
        int i = 0;
        for (i = 0; i <= str.length() - 1; i++) {
            int codeAscii = (int) str.charAt(i);
            if (codeAscii == 44) // Le caractère pointé est une virgule
            {
                cptVirgule = 1;
                if (i > 5) {
                    JOptionPane.showMessageDialog(this, "Il y a trop de chiffres avant la virgule !", "Attention", JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
                if (str.length() - 1 - i > 3) {
                    JOptionPane.showMessageDialog(this, "Il y a trop de chiffres après la virgule !", "Attention", JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
            }
        }

        if (cptVirgule == 0 && str.length() > 5) {
            JOptionPane.showMessageDialog(this, "L'entier doit être limité à 5 chiffres !", "Attention", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

    /**
    * Vérifie que le nombre saisi au diviseur est bien au format 1234 ou
    * avec une virgule en plus (soit 4 entiers ou 4 entiers et une virgule)
    * @return true si le nombre est au format correct; false sinon
    */

    public boolean testFormat1() {
        String str = nb[1].getText();
        int cptVirgule = 0;
        int i = 0;
        for (i = 0; i <= str.length() - 1; i++) {
            int codeAscii = (int) str.charAt(i);
            if (codeAscii == 44) // Le caractère pointé est une virgule
            {
                cptVirgule = 1;
                if (i > 4) {
                    JOptionPane.showMessageDialog(this, "Il y a trop de chiffres avant la virgule !", "Attention", JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
            }
        }

        if (cptVirgule == 0 && str.length() > 4) {
            JOptionPane.showMessageDialog(this, "L'entier doit être limité à 4 chiffres !", "Attention", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

     /**
     * Méthode appelée lorsque la fenêtre est redimensionnée
     * Appelle la méthode "positionComponent()"
     * @param e ComponentEvent
     */

    public void componentResized(ComponentEvent e) {
            try{positionComponent();}
            catch(IllegalArgumentException ex){}
    }

    public void componentMoved(ComponentEvent e) {}

    public void componentShown(ComponentEvent e) {}

    public void componentHidden(ComponentEvent e) {}

     /**
     * Méthode qui positionne l'ensemble des composants du JPanel Division.
     * Ce positionnement s'effectue en fonction d'un coefficient "coef", qui est calculé ici,
     * et qui s'adapte en fonction de la taille de la fenêtre.
     */

    @Override
    protected void positionComponent() {

        int i = 0;

        CalculCoef();


        coef2 = 1;
        if (nbDivid <= 4) {
            coef2 = 1;
        } else {
            coef2 = (float) 5 / (nbDivid + 1);
        }


        police = new Font("Arial", Font.BOLD, (int) (50 * coef * coef2));

        nb[0].setFont(police);
        nb[1].setFont(police);

        // Positionnement des champs de texte initiaux
        nb[0].setBounds((int) (x0 + 130 * coef), (int) (70 *coef), (int) (250 *coef2*  coef), (int) (60 *coef2*  coef));
        nb[1].setBounds((int) (x0 + 420 * coef), (int) (70 * coef), (int) (150 *coef2*  coef), (int) (60 *coef2*  coef));
        
        // Positionnement des boutons modifier au dividende et au diviseur
        modifierLigne[0].setBounds((int) (x0 + 5*coef),(int) (10*coef), (int) (37*coef), (int) (45*coef));
        modifierLigne[0].setSized((int) Math.round(0*coef), (int) Math.round(3*coef),(int) Math.round(40*coef),(int) Math.round(40*coef));
        modifierLigne[1].setBounds((int) (x0 + 745*coef),(int) (10*coef), (int) (37*coef), (int) (45*coef));
        modifierLigne[1].setSized((int) Math.round(0*coef), (int) Math.round(3*coef),(int) Math.round(40*coef),(int) Math.round(40*coef));

        // Positionnement des Panel du Dividende
        // Positionnement des panels de Virgules au Dividende
        if (!panChiffre.isEmpty()) {
            for (int j = 0; j <= panChiffre.get(0).size() - 1; j++) {
                int xPanel = (int) (x0 + (397 - 77 * coef2 * (nbDivid - j)) * coef);
                int yPanel = (int) (70 * coef);
                int cotePanel = (int) (60 * coef * coef2);
                panChiffre.get(0).get(j).setBounds(xPanel, yPanel, cotePanel, cotePanel);
                if (j <= panVirgule.get(0).size() - 1) {
                    panVirgule.get(0).get(j).setBounds((int) (xPanel + cotePanel + 2 * coef2 * coef), (int) (yPanel + cotePanel - 20 * coef2 * coef), (int) (15 * coef * coef2), (int) (30 * coef * coef2));
                }
            }
        }

        // Positionnement des Panels au Diviseur
        // Positionnement des panels de Virgules au Diviseur
        if (!panChiffre.isEmpty()) {
            for (int j = 0; j <= panChiffre.get(1).size() - 1; j++) {
                int xPanel = (int) (x0 + (400 + 20 * coef2 + 77 * j * coef2) * coef);
                int yPanel = (int) (70 * coef);
                int cotePanel = (int) (60 * coef * coef2);
                panChiffre.get(1).get(j).setBounds(xPanel, yPanel, cotePanel, cotePanel);
                if (j <= panVirgule.get(1).size() - 1) {
                    panVirgule.get(1).get(j).setBounds((int) (xPanel + cotePanel + 2 * coef2 * coef), (int) (yPanel + cotePanel - 20 * coef2 * coef), (int) (15 * coef * coef2), (int) (30 * coef * coef2));
                }
            }
        }


        // Positionnement des virgules réelles
        for (i = 0; i <= 2; i++) {
            virgule[i].setFont(new Font("Arial", Font.PLAIN, (int) (60 * coef * coef2)));
            virgule[i].setBounds((int) (0 * coef), (int) (-51 * coef * coef2), (int) (20 * coef * coef2), (int) (90 * coef * coef2));
        }

        // Positionnement des virgules virtuelles
        VirguleOperation.virgule.setFont(new Font("Arial", Font.PLAIN, (int) (60 * coef * coef2)));
        VirguleOperation.virgule.setBounds((int) (0 * coef), (int) (-51 * coef * coef2), (int) (20 * coef * coef2), (int) (90 * coef * coef2));

        // Positionnement des label de chiffres
        if (!chiffre.isEmpty()) {
            for (i = 0; i <= chiffre.size() - 1; i++) {
                for (int j = 0; j <= chiffre.get(i).size() - 1; j++) {
                    chiffre.get(i).get(j).setFont(police);
                    chiffre.get(i).get(j).setBounds((int) (15 * coef * coef2), (int) (2 * coef * coef2), (int) (60 * coef * coef2), (int) (60 * coef * coef2));
                }
            }
        }

        // Positionnement des cases de Resultat
        // Positionnement des panels de Virgules au Resultat
        if (!resultat.isEmpty()) {
            for (int j = 0; j <= resultat.size() - 1; j++) {
                int xPanel = (int) (x0 + (400 + 20 * coef2 + 77 * j * coef2) * coef);
                int yPanel = (int) ((70 + 90 * coef2) * coef);
                int cotePanel = (int) (60 * coef * coef2);
                resultat.get(j).setFont(police);
                resultat.get(j).setBounds(xPanel, yPanel, cotePanel, cotePanel);
                if (j <= panVirgule.get(2).size() - 1) {
                    panVirgule.get(2).get(j).setBounds((int) (xPanel + cotePanel + 2 * coef2 * coef), (int) (yPanel + cotePanel - 20 * coef2 * coef), (int) (15 * coef * coef2), (int) (30 * coef * coef2));
                }
            }
        }


        // Positionnement des champs pour les chiffres
        if (!fieldChiffre.isEmpty()) {
            for (i = 0; i <= nbLigne - 2; i++) {
                for (int j = 0; j <= nbDivid - 1 - i; j++) {
                    int cotePanel = (int) (60 * coef * coef2);
                    int xPanel = (int) (x0 + (397 - 77 * (nbDivid - j - i) * coef2) * coef);
                    int yPanel = (int) ((70 + (80 + 80 * i) * coef2) * coef);

                    fieldChiffre.get(i).get(j).setFont(police);
                    if (i < positionSoustraction || positionSoustraction == -1) {
                        fieldChiffre.get(i).get(j).setBounds(xPanel, yPanel, cotePanel, cotePanel);
                    } else {
                        fieldChiffre.get(i).get(j).setBounds(xPanel, (int) (yPanel + 80 * coef * coef2), cotePanel, cotePanel);
                    }
                }
            }
           
        }

        // Positionnement des boutons de soustraction
        // Positionnement des Label de soustraction
        for (i = 0; i <= addSoustraction.size() - 1; i++) {
            if (i != 0) {
                if (i < positionSoustraction + 1 || positionSoustraction == -1) {
                    if (nbDivid < 4) {
                        addSoustraction.get(i).setBounds((int) (x0 + ((40 + 77 * (4 - nbDivid) + 77 * (i - 1)) * coef)), (int) ((70 + (80 * (i + 1) - 20) * coef2) * coef), (int) (25 * coef * coef2), (int) (25 * coef * coef2));
                    } else {
                        addSoustraction.get(i).setBounds((int) (x0 + ((40 + 77 * (i - 1)) * coef2 * coef)), (int) ((70 + (80 * (i + 1) - 20) * coef2) * coef), (int) (25 * coef * coef2), (int) (25 * coef * coef2));
                    }
                } else {
                    if (nbDivid < 4) {
                        addSoustraction.get(i).setBounds((int) (x0 + ((40 + 77 * (4 - nbDivid) + 77 * (i - 1)) * coef)), (int) ((70 + (80 * (i + 1) - 20 + 80) * coef2) * coef), (int) (25 * coef * coef2), (int) (25 * coef * coef2));
                    } else {
                        addSoustraction.get(i).setBounds((int) (x0 + ((40 + 77 * (i - 1)) * coef2 * coef)), (int) ((70 + (80 * (i + 1) - 20 + 80) * coef2) * coef), (int) (25 * coef * coef2), (int) (25 * coef * coef2));
                    }
                }
            } else if (nbDivid < 4) {
                addSoustraction.get(i).setBounds((int) (x0 + ((40 + 77 * (4 - nbDivid) + 77 * i) * coef)), (int) ((70 + (80 * (i + 1) - 20) * coef2) * coef), (int) (25 * coef * coef2), (int) (25 * coef * coef2));
            } else {
                addSoustraction.get(i).setBounds((int) (x0 + ((40 + 77 * i) * coef2 * coef)), (int) ((70 + (80 * (i + 1) - 20) * coef2) * coef), (int) (25 * coef * coef2), (int) (25 * coef * coef2));
            }

            labAddSoustraction.get(i).setFont(new Font("Arial", Font.PLAIN, (int) (29 * coef * coef2)));
            labAddSoustraction.get(i).setBounds((int) (4 * coef * coef2), (int) (-9 * coef * coef2), (int) (43 * coef * coef2), (int) (43 * coef * coef2));
        }

        // Positionnement des champs pour les chiffres de la soustraction à la position : positionSoustraction
        if (!fieldSoustraction.isEmpty() && positionSoustraction != -1) {
            i = positionSoustraction;
            if (i != 0) {
                for (int j = 0; j <= fieldSoustraction.get(i).size()-1; j++)
                {   int cotePanel = (int) (60 * coef * coef2);
                    int xPanel = (int) (x0 + (397 - 77 * (nbDivid - j - i + 2) * coef2) * coef);
                    int yPanel = (int) ((70 + (80 + 80 * i) * coef2) * coef);
                    fieldSoustraction.get(i).get(j).setFont(police);

                    if (j == 0)
                    {   fieldSoustraction.get(i).get(j).setBounds((int) (x0 + (397 - 77 * (nbDivid - i + 1) * coef2) * coef), (int) (yPanel + 80 * coef2 * coef), cotePanel, cotePanel);
                    }
                    else
                    {   fieldSoustraction.get(i).get(j).setBounds(xPanel, (int) (yPanel - 10 * coef * coef2), cotePanel, cotePanel);
                    }

                }
            } else if (nbDivid > nbDivis) {
                for (int j = 0; j <= nbDivis; j++) {
                    int cotePanel = (int) (60 * coef * coef2);
                    int xPanel = (int) (x0 + (397 - 77 * (nbDivid - j - i) * coef2) * coef);
                    int yPanel = (int) ((70 + 80 * coef2) * coef);

                    fieldSoustraction.get(i).get(j).setFont(police);
                    fieldSoustraction.get(i).get(j).setBounds(xPanel, (int) (yPanel - 10 * coef * coef2), cotePanel, cotePanel);
                }
            } else {
                for (int j = 0; j <= nbDivis - 1; j++) {
                    int cotePanel = (int) (60 * coef * coef2);
                    int xPanel = (int) (x0 + (397 - 77 * (nbDivid - j - i) * coef2) * coef);
                    int yPanel = (int) ((70 + 80 * coef2) * coef);

                    fieldSoustraction.get(i).get(j).setFont(police);
                    fieldSoustraction.get(i).get(j).setBounds(xPanel, (int) (yPanel - 10 * coef * coef2), cotePanel, cotePanel);
                }
            }

        }


        // Positionnement des boutons
        more.setBounds((int) (6 * coef), (int) (-13 * coef), (int) (60 * coef), (int) (60 * coef));
        more.setFont(new Font("Arial", Font.PLAIN, (int) (40 * coef)));
        plus.setBounds((int) (x0 + (390 * coef)), (int) (10 * coef), (int) (35 * coef), (int) (35 * coef));
        less.setBounds((int) (11 * coef), (int) (-16 * coef), (int) (60 * coef), (int) (60 * coef));
        less.setFont(new Font("Arial", Font.PLAIN, (int) (40 * coef)));
        moins.setBounds((int) (x0 + (350 * coef)), (int) (10 * coef), (int) (35 * coef), (int) (35 * coef));



        this.repaint();  // on réactualise le pannel, pour permettre de mettre à jour le paintComponent
        this.validate();
    }



     /**
     * Méthode qui copie l'ensemble des éléments du JPanel de Division.
     * afin de reconstruire un JPanel Impression mis en forme.
     * @return Un JPanel, copie du JPanel d'Addition principal, et mis en forme.
     */


    @Override
    public JPanel impression() {

        Impression panImpression = new Impression();

        if (etat[0] == true && etat[1] == true) {
            panImpression.setLayout(null);
            panImpression.setBounds(0, 0, Division.super.getSize().width, Division.super.getSize().height);
            panImpression.setBackground(ColorManager.transparent());


            // Copie des chiffres d'opération
            List<List<JLabel>> copyChiffre = new ArrayList<List<JLabel>>();
            copyChiffre = Copie.copieDoubleListLabel(chiffre);

            // Positionnement au Dividende
            // Copie de la virgule du dividende
            JLabel copyVirgule[] = new JLabel[3];

            for (int j = 0; j <= panChiffre.get(0).size() - 1; j++) {
                panImpression.add(copyChiffre.get(0).get(j));
                int xPanel = (int) (x0 + (397 - 77 * coef2 * (nbDivid - j)) * coef);
                int yPanel = (int) (70 * coef);
                int cotePanel = (int) (60 * coef * coef2);
                copyChiffre.get(0).get(j).setBounds(xPanel, yPanel, cotePanel, cotePanel);
                if (j == positionVirgule[0] - 1) {
                    copyVirgule[0] = Copie.copie(virgule[0]);
                    panImpression.add(copyVirgule[0]);
                    copyVirgule[0].setBounds((int) (xPanel + cotePanel - 8 * coef2 * coef), (int) (yPanel), (int) (50 * coef * coef2), (int) (60 * coef * coef2));
                }
            }
            // Positionnement au Diviseur
            // Copie de la virgule du diviseur

            for (int j = 0; j <= panChiffre.get(1).size() - 1; j++) {
                panImpression.add(copyChiffre.get(1).get(j));
                int xPanel = (int) (x0 + (400 + 20 * coef2 + 77 * j * coef2) * coef);
                int yPanel = (int) (70 * coef);
                int cotePanel = (int) (60 * coef * coef2);
                copyChiffre.get(1).get(j).setBounds(xPanel, yPanel, cotePanel, cotePanel);
                if (j == positionVirgule[1] - 1) {
                    copyVirgule[1] = Copie.copie(virgule[1]);
                    panImpression.add(copyVirgule[1]);
                    copyVirgule[1].setBounds((int) (xPanel + cotePanel - 8 * coef2 * coef), (int) (yPanel), (int) (50 * coef * coef2), (int) (60 * coef * coef2));
                }
            }


            // Copie des labels de résultats
            // Copie de la virgule du résultat
            List<JLabel> copyResultat = new ArrayList<JLabel>();
            copyResultat = Copie.copieListTextField(resultat);
            for (int j = 0; j <= resultat.size() - 1; j++) {
                panImpression.add(copyResultat.get(j));
                int xPanel = (int) (x0 + (400 + 20 * coef2 + 77 * j * coef2) * coef);
                int yPanel = (int) ((70 + 90 * coef2) * coef);
                int cotePanel = (int) (60 * coef * coef2);
                copyResultat.get(j).setBounds(xPanel, yPanel, cotePanel, cotePanel);
                if (j == positionVirgule[2] - 1) {
                    copyVirgule[2] = Copie.copie(virgule[2]);
                    panImpression.add(copyVirgule[2]);
                    copyVirgule[2].setBounds((int) (xPanel + cotePanel - 8 * coef2 * coef), (int) (yPanel), (int) (50 * coef * coef2), (int) (60 * coef * coef2));
                }
            }

            // Copie des label de chiffres de calcul
            List<List<JLabel>> copyFieldChiffre = new ArrayList<List<JLabel>>();
            copyFieldChiffre = Copie.copieDoubleListTextField(fieldChiffre);
            for (int i = 0; i <= nbLigne-2; i++)
            {    for (int j = 0; j <=  nbDivid-1-i; j++)
                {   panImpression.add(copyFieldChiffre.get(i).get(j));
                    int cotePanel = (int) (60 * coef * coef2);
                    int xPanel = (int) (x0 + (397 - 77 * (nbDivid - j - i) * coef2) * coef);
                    int yPanel = (int) ((70 + (80 + 80 * i) * coef2) * coef);
                    if (i < positionSoustraction || positionSoustraction == -1)
                        copyFieldChiffre.get(i).get(j).setBounds(xPanel, yPanel, cotePanel, cotePanel);
                    else
                        copyFieldChiffre.get(i).get(j).setBounds(xPanel, (int) (yPanel + 80 * coef * coef2), cotePanel, cotePanel);
                }
            }

         // Copie des champs de soustractions intermédiaires
        List<List<JLabel>> copyFieldSoustraction = new ArrayList<List<JLabel>>();
        copyFieldSoustraction = Copie.copieDoubleListTextField(fieldSoustraction);
        if (!fieldSoustraction.isEmpty() && positionSoustraction != -1) {
            int i = positionSoustraction;
            if (i != 0) {
                for (int j = 0; j <= fieldSoustraction.get(i).size()-1; j++) {
                    int cotePanel = (int) (60 * coef * coef2);
                    int xPanel = (int) (x0 + (397 - 77 * (nbDivid - j - i + 2) * coef2) * coef);
                    int yPanel = (int) ((70 + (80 + 80 * i) * coef2) * coef);
                    panImpression.add(copyFieldSoustraction.get(i).get(j));
                    copyFieldSoustraction.get(i).get(j).setForeground(ColorManager.get("color optional field foreground"));
                    if (j == 0)
                    {   copyFieldSoustraction.get(i).get(j).setBounds((int) (x0 + (397 - 77 * (nbDivid - i + 1) * coef2) * coef), (int) (yPanel + 80 * coef2 * coef), cotePanel, cotePanel);
                    }
                    else
                    {   copyFieldSoustraction.get(i).get(j).setBounds(xPanel, (int) (yPanel - 10 * coef * coef2), cotePanel, cotePanel);
                    }
                }
           } else if (nbDivid > nbDivis) {
                for (int j = 0; j <= nbDivis; j++) {
                    int cotePanel = (int) (60 * coef * coef2);
                    int xPanel = (int) (x0 + (397 - 77 * (nbDivid - j - i) * coef2) * coef);
                    int yPanel = (int) ((70 + 80 * coef2) * coef);
                    panImpression.add(copyFieldSoustraction.get(i).get(j));
                    copyFieldSoustraction.get(i).get(j).setForeground(ColorManager.get("color optional field foreground"));
                    copyFieldSoustraction.get(i).get(j).setBounds(xPanel, (int) (yPanel - 10 * coef * coef2), cotePanel, cotePanel);
                }
            } else {
                for (int j = 0; j <= nbDivis - 1; j++) {
                    int cotePanel = (int) (60 * coef * coef2);
                    int xPanel = (int) (x0 + (397 - 77 * (nbDivid - j - i) * coef2) * coef);
                    int yPanel = (int) ((70 + 80 * coef2) * coef);
                    panImpression.add(copyFieldSoustraction.get(i).get(j));
                    copyFieldSoustraction.get(i).get(j).setForeground(ColorManager.get("color optional field foreground"));
                    copyFieldSoustraction.get(i).get(j).setBounds(xPanel, (int) (yPanel - 10 * coef * coef2), cotePanel, cotePanel);
                }
            }

        }

          
        }
        return panImpression;
    }

    /**
    * Classe de JPanel qui copie le JPanel principal
    */

    public class Impression extends JPanel {

        public Impression() {
        }

        /**
         * Copie de la méthode de la classe mère pour le JPanel d'Impression.
         * @param g Le Graphics
         */

        @Override
        public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g; // Crée un Graphique de type 2d
        super.paintComponent(g); // Redessine le Panel avant d'ajouter les composants 

        Stroke epaisseur = new BasicStroke((int) (6 * coef * coef2));    // Crée une variable contenant l'épaisseur du trait (ici : 6)
        g2d.setStroke(epaisseur); // Donne au trait son épaisseur


        // Tracée du trait vertical
        g2d.drawLine((int) (x0 + 400 * coef), (int) (70 * coef), (int) (x0 + 400 * coef), (int) ((70 + 80 * (nbLigne + 1) * coef2) * coef));

        // Tracée du trait horizontal
        g2d.drawLine((int) (x0 + 400 * coef), (int) ((70 + 80 * coef2) * coef), (int) (x0 + (400 + 77 * nbDivis * coef2) * coef), (int) ((70 + 80 * coef2) * coef));

        // Tracée des éléments de la soustraction

        if (positionSoustraction != -1) {
            if (positionSoustraction != 0) {   // Tracée du trait de soustraction
                int lastIndice = 0;     //Dernière case de soustraction pour tracer le trait
                /// On identifie la valeur de lastIndice selon les cases de soustractions ajoutées
                for (int j=0; j<=fieldChiffre.get(positionSoustraction-1).size()-1; j++)    // On regarde quelles sont les champs non vide
                        if (!fieldChiffre.get(positionSoustraction-1).get(j).getText().equals(""))
                            lastIndice = j;

                g2d.drawLine((int) (x0 + (397 - 77 * (nbDivid - positionSoustraction + 1) * coef2) * coef), (int) ((70 + (70 + 80 * (positionSoustraction + 1)) * coef2) * coef), (int) (x0 + (397 - (10 + 77 * (nbDivid - positionSoustraction + 1 - lastIndice-1)) * coef2) * coef), (int) ((70 + (70 + 80 * (positionSoustraction + 1)) * coef2) * coef));
                // Tracée du "-" de la soustraction
                g2d.drawLine((int) (x0 + (397 - (50 + 77 * (nbDivid - positionSoustraction + 1)) * coef2) * coef), (int) ((70 + (30 + 80 * (positionSoustraction + 1)) * coef2) * coef), (int) (x0 + (397 - (20 + 77 * (nbDivid - positionSoustraction + 1)) * coef2) * coef), (int) ((70 + (30 + 80 * (positionSoustraction + 1)) * coef2) * coef));
            } else {    // Tracée du trait de soustraction
                if (nbDivid > nbDivis) {
                    g2d.drawLine((int) (x0 + (397 - 77 * (nbDivid - positionSoustraction) * coef2) * coef), (int) ((70 + (70 + 80 * (positionSoustraction + 1)) * coef2) * coef), (int) (x0 + (397 - (10 + 77 * (nbDivid - positionSoustraction - (nbDivis + 1))) * coef2) * coef), (int) ((70 + (70 + 80 * (positionSoustraction + 1)) * coef2) * coef));
                } else {
                    g2d.drawLine((int) (x0 + (397 - 77 * (nbDivid - positionSoustraction) * coef2) * coef), (int) ((70 + (70 + 80 * (positionSoustraction + 1)) * coef2) * coef), (int) (x0 + (397 - (10 + 77 * (nbDivid - positionSoustraction - (nbDivis))) * coef2) * coef), (int) ((70 + (70 + 80 * (positionSoustraction + 1)) * coef2) * coef));
                }
                // Tracée du "-" de la soustraction
                g2d.drawLine((int) (x0 + (397 - (50 + 77 * (nbDivid - positionSoustraction)) * coef2) * coef), (int) ((70 + (30 + 80 * (positionSoustraction + 1)) * coef2) * coef), (int) (x0 + (397 - (20 + 77 * (nbDivid - positionSoustraction)) * coef2) * coef), (int) ((70 + (30 + 80 * (positionSoustraction + 1)) * coef2) * coef));
            }
        }

        g2d.setStroke(new BasicStroke(1));
        }
    }

    /**
    * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
    * @return La coordonnée en x du début de l'opération
    */

    @Override
    public int tailleX0() {
        if (positionSoustraction != -1)
        {    if (positionSoustraction != 0)
                return Math.min((int)(x0 + (387 - 77 * nbDivid * coef2) * coef),(int) (x0 + (387 - (50 + 77 * (nbDivid - positionSoustraction+1)) * coef2) * coef));    // (x0 + (397 - (50 + 77 * (nbDivid - positionSoustraction + 1)) * coef2)
            else
                return Math.min((int)(x0 + (387 - 77 * nbDivid * coef2) * coef),(int) (x0 + (387 - (50 + 77 * (nbDivid - positionSoustraction)) * coef2) * coef));    // (x0 + (397 - (50 + 77 * (nbDivid - positionSoustraction + 1)) * coef2)

                
        }
        else
            return (int) (x0 + (387 - 77 * nbDivid * coef2) * coef);
    }

    /**
    * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
    * @return La coordonnée en y du début de l'opération
    */

    @Override
    public int tailleY0() {
        return (int) (60 * coef);

    }

     /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en x de la fin de l'opération
     */

    @Override
    public int tailleX1() {
        if (nbDivid >= nbDivis) {
            return (int) (x0 + (400 + 20*coef2+(77*(resultat.size()- 1)+60)*coef2)*coef);
        } else {
            return (int) (x0 + (400 + 77 * nbDivis * coef2) * coef);
        }
    }

     /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en y de la fin de l'opération
     */

    @Override
    public int tailleY1() {
        return (int) ((80 + 80 * (nbLigne + 1) * coef2) * coef);
    }
} 

