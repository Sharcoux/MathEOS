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



// Nombres entrés avec 5 entiers max et 3 chiffres après la virgule max (9 caractères au total)


package bomehc.operations;

import bomehc.elements.ChangeModeListener;
import bomehc.utils.managers.ColorManager;
import java.awt.BasicStroke;    // Pour l'épaisseur des traits
import java.awt.event.MouseEvent;
import javax.swing.JPanel;      // Pour la gestion des JPanel
import java.awt.Color;          // Pour la gestion des couleurs des objets
import java.awt.Font;           // Gestion de la police d'écriture
import java.awt.Graphics;       // Création du type "Graphics"
import java.awt.Graphics2D;     //Création du type "Graphics2D"
import java.awt.Stroke;         // Pour changer l'épaisseur des traits
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JTextField;  // Créer un champ de texte
import java.awt.event.KeyEvent; // Nécessaire pour les listener clavier
import java.awt.event.KeyListener;  // Nécessaire pour les listener clavier
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;         // Création et estion de boutons
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Classe définissant un Panel pour mettre en forme une addition
 */

public class Addition extends OperationType implements ActionListener, ComponentListener, KeyListener, MouseListener{

        public int lPanel = 800; //Longueur du Panel de travail Addition (800 optimal)
        public int hPanel = 750-55; // Hauteur du Panel de travail Addition(695 optimal)

        private LimitedTextFieldOperations nb[] = {new LimitedTextFieldOperations(9), new LimitedTextFieldOperations(9),new LimitedTextFieldOperations(9),new LimitedTextFieldOperations(9),new LimitedTextFieldOperations(9),new LimitedTextFieldOperations(9)};  // Création d'un tableau de champ de texte
        private LimitedTextFieldOperations resultat[] = {null};   // Création du tableau des champs de résultat
        private JPanel panChiffre[][] = {null};    // Tableau des JPanel de mise en forme des chiffres
        private JLabel chiffre[][] = {null};    // Tableau des JLabel contenant les chiffre
        private LimitedTextFieldOperations retenu[] ={null};  // Tableau des champs de retenu
        //private JButton modifierLigne[] = {null};   // Tableau des boutons permettant de modifier une ligne
        public BoutonIcone modifierLigne[] = {null};// Tableau des boutons permettant de modifier une ligne
        private int maxX = 6;   // Désigne le nombre de ligne maximale d'opération
        private int maxY = 8;   // Désigne le nombre de chiffre maximal autorisé (avec maximum 5 avant la virgule, et 3 après la virgule)
        private JButton plus = new JButton();    // Bouton permettant d'ajouter une ligne
        private JButton moins = new JButton();   // Bouton permettant d'enlever une ligne
        private boolean etat[] = {false,false,false,false,false,false}; // Tableau qui renvoie "false" si le nombre est à l'état de champ de texte et "true" s'il est mis en forme
        private int nbLigne = 2;     // Nombre de ligne d'opération présente à l'écran (sans compter le résultat)
        public int lChamp = 250;    // Largeur d'un champ de texte
        public int hChamp = 60;     // Hauteur d'un champ de texte
        public Font police = new Font("Arial", Font.BOLD, (int) (hChamp - 10)); //Crée une police
        public int lengthNbMax[][] = {{0,0},{0,0},{0,0},{0,0},{0,0},{0,0}}; //Tableau qui renvoie la longueur de chaque entier et de la longueur de la partie décimal associée lorsque les nombres sont mis en forme
        public int cptNbLigne = 0;    // Compte le nombre de ligne mises en forme
        public JLabel virgule[] = {null};   // Tableau des virgules de chacun des chiffres d'opérations
        public JLabel virguleResultat = new JLabel(",");    // Label de la virgule du résultat
        public JLabel more = new JLabel("+");    // Label du symbole "+" pour ajouter une ligne
        public JLabel less = new JLabel("-");    //Label du symbole "-" pour enlever une ligne



  /**
  * Crée le Panel d'Addition, et sauvegarde la date de création
  * Remplit également les tableaux des éléments d'Addition (cases de résultats, de retenues...)
  */

  public Addition(){

        this.addComponentListener(this);    // Ajout du pannel à la liste des écouteurs pour le redimenssionnement

        // Définition de l'espace de travail pour l'opération

        // Création du Panel de travail
        this.setBackground(ColorManager.get("color panel operation"));
        this.setLayout(null);


        // Création des champs de texte
        int i=0;
        for (i=0; i<=maxX-1; i++)
            nb[i].addKeyListener(this);
        this.add(nb[0]);    // Ajout de ce champ de texte à la fenêtre
        this.add(nb[1]);
        nb[0].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        nb[1].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));

        // Création du bouton d'ajout de ligne de calcul et de suppression
        plus.setLayout(null);
        plus.add(more);
        this.add(plus);
        plus.addActionListener(this);
        plus.addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        moins.setLayout(null);
        moins.add(less);
        moins.addActionListener(this);
        moins.addMouseListener(new ChangeModeListener(ChangeModeListener.TP));


        // Initialisation du tableau panChiffre[][]
        // Initialisation du tableau chiffre[][] avec les Label
        panChiffre = new JPanel[maxX][maxY];
        chiffre = new JLabel[maxX][maxY];
        
        int j = 0;
        for (i=0; i<=maxX-1; i++)
        {   for (j=0; j<=maxY-1; j++)
            {   panChiffre[i][j] = new JPanel();
                chiffre[i][j] = new JLabel();
                if (i ==0 || i ==2 || i==4)
                    chiffre[i][j].setForeground(ColorManager.get("color chiffre line1"));
                else
                    chiffre[i][j].setForeground(ColorManager.get("color chiffre line2"));
                panChiffre[i][j].setLayout(null);
                panChiffre[i][j].setBackground(ColorManager.get("color chiffre background"));
                panChiffre[i][j].add(chiffre[i][j]);
                panChiffre[i][j].addMouseListener(this);
                panChiffre[i][j].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
            }
        }

    

        // Initialisation du tableau de virgule
        virgule = new JLabel[maxX];

        for (i=0; i<= maxX-1; i++)
        {    virgule[i] = new JLabel(",");
             virgule[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }
        

        // Initialisation du tableau de résultats
        resultat = new LimitedTextFieldOperations[maxY+1];

        for (i=0; i<= maxY; i++)
        {    resultat[i] = new LimitedTextFieldOperations(1);
             resultat[i].setHorizontalAlignment(JTextField.CENTER);
             resultat[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }


        // Initialisation du tableau de retenu
        retenu = new LimitedTextFieldOperations[maxY-1];
       
        for (i=0; i<= maxY-2; i++)
        {   retenu[i] = new LimitedTextFieldOperations(1);
            retenu[i].setHorizontalAlignment(JTextField.CENTER);
            retenu[i].setBackground(ColorManager.get("color retenu background"));
            retenu[i].setForeground(ColorManager.get("color retenu foreground"));
            retenu[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }


        // Création du bouton modifier nombre
        modifierLigne = new BoutonIcone[maxX];

        for (i=0; i<= maxX-1; i++)
        {   modifierLigne[i] = new BoutonIcone("images/Modifier_Down.png", "images/Modifier_Up.png");
            modifierLigne[i].addMouseListener(this);
            modifierLigne[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }


    }

  /**
   * Crée un nouveau Panel d'Addition
   * @return Nouveau Panel d'Addition
   */
  
    @Override
    public Addition nouveau() {
        return new Addition();
    }


  /**
  * Méthode qui vérifie sur le nombre saisi est correct
  * et qui met en forme les nombres pour l'opération
  * en effectuant les modifications nécessaires
  * Est appelé à l'appui de la touche "Entrée" et
  * concerne la ligne dont le champ de texte contient le curseur
  */

    public void keyPressed(KeyEvent e) {        // Pour détecter quand un nombre est tapé dans le champ de texte
            int i=0;
            int j=0;
            String avVirgule = null;    // Chaine qui contiendra la partie entière du nombre
            String apVirgule = null;    // Chaine qui contiendra la partie décimale du nombre
            boolean isDecimal = false;  // Booléen qui renvoie false si le nombre est entier et true s'il est décimal

            if (e.getKeyCode() == 10)   // Touche "entrée" pressée
            {   if (nb[0].hasFocus() == true) // Curseur dans chemp de texte 0
                    i = 0;
                if (nb[1].hasFocus() == true)
                    i = 1;
                if (nb[2].hasFocus() == true)
                    i = 2;
                if (nb[3].hasFocus() == true)
                    i = 3;
                if (nb[4].hasFocus() == true)
                    i = 4;
                if (nb[5].hasFocus() == true)
                    i = 5;

                if (nb[i].testDecimal() == true && testFormat(nb[i]) == true)    // Le nombre saisi est correct
                {
                    // On teste la présence d'une virgule dans le nombre saisi pour l'afficher ou non
                    for (j = 0; j<= nb[i].getText().length()-1;j++)
                    {   int codeAscii = (int) nb[i].getText().charAt(j);
                        if (codeAscii == 44)     // Il y a une virgule dans le nombre
                        {   isDecimal = true;
                            this.add(virgule[i]); // On affiche la virgule sur le Panel
                            apVirgule = nb[i].getText().substring(j+1, nb[i].getText().length());   // On crée deux sous-chaine
                            avVirgule = nb[i].getText().substring(0, j);
                        }
                    }


                    if (isDecimal == true)   //Cas où le nombre est à virgule
                    {   this.remove(nb[i]);
                        for (j = avVirgule.length()-1; j>=0; j--)
                        {   chiffre[i][maxY-3-avVirgule.length()+j].setText(""+ avVirgule.charAt(j));
                            panChiffre[i][maxY-3-avVirgule.length()+j].add(chiffre[i][maxY-3-avVirgule.length()+j]);
                            Addition.this.add(panChiffre[i][maxY-3-avVirgule.length()+j]);
                        }

                        for (j = 0; j <= apVirgule.length()-1; j++)
                        {   chiffre[i][maxY-3+j].setText(""+ apVirgule.charAt(j));
                            panChiffre[i][maxY-3+j].add(chiffre[i][maxY-3+j]);
                            Addition.this.add(panChiffre[i][maxY-3+j]);
                        }
                        lengthNbMax[i][0] = avVirgule.length();
                        lengthNbMax[i][1] = apVirgule.length();

                    }

                    if (isDecimal == false)   //Cas où le nombre n'est pas à virgule
                    {   this.remove(nb[i]);
                        for (j = nb[i].getText().length()-1; j>=0; j--)
                        {   chiffre[i][maxY-3-nb[i].getText().length()+j].setText(""+ nb[i].getText().charAt(j));
                            panChiffre[i][maxY-3-nb[i].getText().length()+j].add(chiffre[i][maxY-3-nb[i].getText().length()+j]);
                           this.add(panChiffre[i][maxY-3-nb[i].getText().length()+j]);
                         }
                        lengthNbMax[i][0] = nb[i].getText().length();
                    }

                    etat[i] = true;
                    cptNbLigne++;


                    this.add(modifierLigne[i]);
                    //if(i==nbLigne-1) {resultat[5].requestFocus();}
                    // Affichage des Panels de résultat
                    afficherResultat();     // On affiche les résultats et on repositionne le bouton "plus" et le bouton "moins" à la bonne ligne par l'appel à positionComponent()

                    // On détermine qui a le focus après la mise en forme
                    if(lengthDecimalMax() == 0)
                        resultat[5].requestFocus();
                    else
                        resultat[5+lengthDecimalMax()].requestFocus();
                    int indice=0;
                    while(indice<nbLigne)
                    {   if(etat[indice] == false)
                        {   nb[indice].requestFocus();
                            break;
                        }
                        indice++;
                    }

                }
            }

    }

    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}


/**
 * Concerne le bouton "+", le bouton "-"
 * Gère l'apparition, la disparition et l'effet de ces boutons
 * Bouton "+" Ajoute une nouvelle ligne d'addition
 * Bouton "-" Supprimer la dernière ligne d'addition
 * @param arg0 ActionEvent
 */

    public void actionPerformed(ActionEvent arg0) {
            int j = 0;
            int i=2;

            if(arg0.getSource() == plus && nbLigne <= 5)    // Si le bouton "+" est cliqué et qu'il y a encore de le la place pour une ligne supplémentaire
            {   nbLigne++;      // On incrémente la valeur du nombre de ligne d'opération

                // Selon la valeur du nombre de ligne, on ajoute le bon nombre de champ de texte
                if (nbLigne ==3)
                {   if (etat[2] == false)
                        this.add(nb[2]);
                    this.add(moins);
                }

                if (nbLigne ==4)
                {   if (etat[2] == false)
                        this.add(nb[2]);
                    if (etat[3] == false)
                        this.add(nb[3]);
                }

                if (nbLigne ==5)
                {   if (etat[2] == false)
                        this.add(nb[2]);
                    if (etat[3] == false)
                        this.add(nb[3]);
                    if (etat[4] == false)
                        this.add(nb[4]);
                }

                if (nbLigne ==6)
                {   if (etat[2] == false)
                        this.add(nb[2]);
                    if (etat[3] == false)
                        this.add(nb[3]);
                    if (etat[4] == false)
                        this.add(nb[4]);
                    if (etat[5] == false)
                        this.add(nb[5]);
                    this.remove(plus);
                }

            }

            if(arg0.getSource() == moins && nbLigne >= 3)    // Si le bouton "-" est cliqué et qu'il y a plus de 2 lignes d'opération
            {   nbLigne--;      // On décrémente la valeur du nombre de ligne d'opération
                if (etat[i] == true)
                    cptNbLigne--;
                // Selon la valeur du nombre de ligne, on enlèvee le bon nombre de champ de texte
                if (nbLigne ==2)
                {   for (i=2; i<= maxX-1;i++)
                    {   this.remove(nb[i]);
                        for (j = 0; j<=maxY-1;j++ )
                        {    panChiffre[i][j].remove(chiffre[i][j]);
                            this.remove(panChiffre[i][j]);
                            chiffre[i][j].setText("");
                        }
                        etat[i] = false;
                        this.remove(virgule[i]);
                    }
                    this.remove(modifierLigne[2]);
                    lengthNbMax[2][0] = 0;
                    lengthNbMax[2][1] = 0;
                    this.remove(moins);
                }

                if (nbLigne ==3)
                {   for (i=3; i<= maxX-1;i++)
                    {   this.remove(nb[i]);
                        for (j =0; j<=maxY-1;j++ )
                        {   panChiffre[i][j].remove(chiffre[i][j]);
                            this.remove(panChiffre[i][j]);
                            chiffre[i][j].setText("");
                        }
                        etat[i] = false;
                        this.remove(virgule[i]);
                    }
                    this.remove(modifierLigne[3]);
                    lengthNbMax[3][0] = 0;
                    lengthNbMax[3][1] = 0;
                }

                if (nbLigne ==4)
                {   for (i=4; i<= maxX-1;i++)
                    {   this.remove(nb[i]);
                        for (j =0; j<=maxY-1;j++ )
                        {   panChiffre[i][j].remove(chiffre[i][j]);
                            this.remove(panChiffre[i][j]);
                            chiffre[i][j].setText("");
                        }
                        etat[i] = false;
                        this.remove(virgule[i]);
                    }
                    this.remove(modifierLigne[4]);
                    lengthNbMax[4][0] = 0;
                    lengthNbMax[4][1] = 0;
                }

                if (nbLigne ==5)
                {   i = 5;
                    this.remove(nb[5]);
                    for (j =0; j<=maxY-1;j++ )
                    {   panChiffre[i][j].remove(chiffre[5][j]);
                        this.remove(panChiffre[5][j]);
                        chiffre[i][j].setText("");
                    }
                    etat[5] = false;
                    this.remove(modifierLigne[5]);
                    this.remove(virgule[5]);
                    this.add(plus);
                    lengthNbMax[5][0] = 0;
                    lengthNbMax[5][1] = 0;
                }

            }


            afficherResultat();  // On met en forme de résultat et les retenues
            positionComponent(); // On repositionne le bouton "plus" et le bouton "moins" à la bonne ligne
     }

    /**
    * Gère les boutons "ModifierLigne"
    * Permet de modifier une ligne d'addition
    * @param e MouseEvent
    */

    public void mouseClicked(MouseEvent e) {
        // Caractérise les boutons de modification de ligne
        for (int i=0; i<=maxX-1; i++)    // On écoute chaque ligne
                if(e.getSource() == modifierLigne[i])    // Si le bouton "modifierLigne" d'une ligne est activé, on réinitialise tous les paramètres
                {   cptNbLigne--;
                    // On écrit le nombre dans le champ de texte concerné
                    String chiffreChamp = "";
                    for (int j=0; j<=maxY-1;j++)
                        chiffreChamp = chiffreChamp + chiffre[i][j].getText();

                    if (lengthNbMax[i][1] !=0)
                    {   chiffreChamp = chiffreChamp.substring(0, lengthNbMax[i][0])+","+chiffreChamp.substring(lengthNbMax[i][0], lengthNbMax[i][0]+lengthNbMax[i][1]);
                    }
                    nb[i].setText(chiffreChamp);

                    this.remove(modifierLigne[i]);
                    this.remove(virgule[i]);
                    lengthNbMax[i][0] = 0;
                    lengthNbMax[i][1] = 0;
                    etat[i] = false;
                    for (int j=0; j<=maxY-1; j++)
                    {   chiffre[i][j].setText("");
                        panChiffre[i][j].remove(chiffre[i][j]);
                        this.remove(panChiffre[i][j]);
                    }
                    for (int j=0; j<= maxY; j++)
                        resultat[j].setText("");
                    for (int j=0; j<= maxY-2; j++)
                        retenu[j].setText("");


                    this.add(nb[i]);
                    nb[i].requestFocus();
                }
         afficherResultat();  // On met en forme de résultat et les retenues
         positionComponent();
    }

    /**
     * Méthode permettant de modifier un chiffre en cliquant dessus.
     * Cela ouvre une boîte de dialogue qui demander d'entrer le nouveau chiffre.
     * @param e MouseEvent
     */

    public void mousePressed(MouseEvent e) {
        for (int i=0; i<=panChiffre.length-1; i++)
            for (int j=0; j<=panChiffre[i].length-1; j++)
                if (e.getSource() == panChiffre[i][j])
                {   panChiffre[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                    String nouveauChiffre = JOptionPane.showInputDialog(this, "Nouveau chiffre :", "" );
                    panChiffre[i][j].setBorder(null);
                    if (nouveauChiffre != null && !nouveauChiffre.equals(""))
                    {   int codeAscii = (int) nouveauChiffre.charAt(0);
                        if (codeAscii <= 57 && codeAscii >=48)
                        {   panChiffre[i][j].remove(chiffre[i][j]);
                            chiffre[i][j].setText(nouveauChiffre.substring(0, 1));
                            panChiffre[i][j].add(chiffre[i][j]);
                            positionComponent();
                        }
                    }

                }
    }


    public void mouseReleased(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}


    /**
     * Gère la position des "+" et de la barre d'addition en fonction du nombre de ligne d'opération et de la taille de la fenêtre
     * @param g Le Graphics
     */

    @Override
    public void paintComponent(Graphics  g){
            int i=2;
            int lengthMaxInt = 0;    // Longueur maximale des parties entières
            int lengthMaxDec = 0;    // Longueur maximale des parties décimale
            CalculCoef();
            Graphics2D g2d = (Graphics2D)g; // Crée un Graphique de type 2d
            super.paintComponent(g); // Redessine le Panel avant d'ajouter les composants

            Stroke epaisseur = new BasicStroke((int) (6*coef));    // Crée une variable contenant l'épaisseur du trait (ici : 6)
            g2d.setStroke(epaisseur); // Donne au trait son épaisseur

            // Tracée de la ligne
            if (cptNbLigne == nbLigne || lengthIntMax()>4)   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                lengthMaxInt = lengthIntMax();
            else
                lengthMaxInt = 4;

            if (lengthDecimalMax() != 0)     //Si le nombre est décimal
            {   lengthMaxDec = lengthDecimalMax();
                g2d.drawLine((int) (x0 + (380 - (lengthMaxInt-1)*65)*coef), (int)((240 + 80*(nbLigne-2))*coef), (int) (x0 + (525 + lengthMaxDec*65)*coef), (int) ((240 + 80*(nbLigne-2))*coef)); // Trace le trait en fonction des (x,y) de deux points
            }
            else      //Si le nombre n'est pas décimal
                g2d.drawLine((int) (x0 + (380 - (lengthMaxInt-1)*65)*coef),(int) ((240 + 80*(nbLigne-2))*coef), (int) (x0 + 500*coef), (int) ((240 + 80*(nbLigne-2))*coef)); // Trace le trait en fonction des (x,y) de deux points


            // Tracée du symbole "+"
            if (cptNbLigne == nbLigne || lengthIntMax()>4)   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                lengthMaxInt = lengthIntMax();
            else
                lengthMaxInt = 4;   // Par défaut, on se place à la distance correspondant à un champ de texte non mis en forme

            for (i=2; i<=nbLigne; i++)
            {   g2d.drawLine((int) (x0 + (395 - (lengthMaxInt-1)*65)*coef),(int) ((160 + 80*(i-2))*coef),(int) (x0 + (395 - (lengthMaxInt-1)*65)*coef), (int) ((200 + 80*(i-2))*coef));  // Trait vertical
                g2d.drawLine((int) (x0 + (375 - (lengthMaxInt-1)*65)*coef) ,(int) ((180 + 80*(i-2))*coef),(int) (x0 + (415 - (lengthMaxInt-1)*65)*coef),(int) ((180 + 80*(i-2))*coef));   //Trait horizontal
            }
            g2d.setStroke(new BasicStroke(1));
      }


    /**
    * Méthode qui met en forme les cases de résultats et les retenues en fonction
    * du nombre de chiffres dans l'addition
    */

    public void afficherResultat(){
            int lengthMax =0;   // Renvoie la longueur maximale d'un nombre mis en forme
            int i=0;

            // On réinitialise les cases de résultats ou de retenus
           for (i=0; i<=maxY;i++)
                this.remove(resultat[i]);
            for (i=0; i<=maxY-2;i++)
                this.remove(retenu[i]);
            this.remove(virguleResultat);

            // On réaffiche les cases de résultat et la virgule
            lengthMax = lengthIntMax();
            if (lengthMax !=0)      // Si le nombre n'est pas vide
                for (i=0;i<=lengthMax; i++)
                    this.add(resultat[maxY-3-i]);  // On affiche les cases d'entier

            if (lengthMax !=0)  // Si le nombre n'a pas de partie entière, il n'a pas non plus de partie décimal
            {   lengthMax = lengthDecimalMax();
                for (i=0;i<=lengthMax-1; i++)
                    this.add(resultat[maxY-2+i]);   // On affiche les cases de décimal
                if (lengthDecimalMax() !=0)
                    this.add(virguleResultat);
            }

            // Affichage des cases de retenu
            lengthMax = lengthIntMax();
            if (lengthDecimalMax()!= 0)      // Le nombre est décimal
            {   for (i=0;i<=lengthMax-1; i++)   // On affiche les cases de retenu sur tous les entiers
                    add(retenu[maxY-4-i]);

                 lengthMax = lengthDecimalMax();
                 for (i=1;i<=lengthMax-1; i++)  // On affiche les cases de retenu sur tous les décimal sauf le dernier
                    add(retenu[maxY-4+i]);
            }
            else        // Le nombre est seulement un entier
            {   if (lengthMax != 0 && lengthMax != 1)
                    for (i=1;i<=lengthMax-1; i++)   // On affiche les cases de retenu sur tous les chiffres, sauf celui des unités
                        add(retenu[maxY-4-i]);
            }

            positionComponent();    // On positionne les cases de résultats et la virgule du résultat
    }    // Fin de la méthode "afficherResultat"


    /**
     * Méthode qui renvoie le maximum des longueurs de la partie entière des nombres de chaque ligne
     * @return La longueur de la ligne ayant la plus longue partie entière
     */

    public int lengthIntMax(){
            int lengthMax = 0;   // Renvoie la longueur maximale d'un nombre mis en forme
            int i=0;
            for (i=0;i<=nbLigne-1; i++)       // On définit la valeur du maximum en tant qu'entier
                if (lengthNbMax[i][0]>=lengthMax)
                    lengthMax = lengthNbMax[i][0];
            return lengthMax;
    }

     /**
     * Méthode qui renvoie le maximum des longueurs de la partie décimale des nombres de chaque ligne
     * Permet également de savoir si le nombre est décimale ou non
     * Si lengthMax = 0, le nombre n'a pas de chiffre après la virgule
     * @return La longueur de la ligne ayant la plus longue partie décimale
     */

    public int lengthDecimalMax(){
            int lengthMax = 0;   // Renvoie la longueur maximale d'un nombre mis en forme
            int i=0;
            for (i=0;i<=nbLigne-1; i++)       // On définit la valeur du maximum en tant que décimal
                if (lengthNbMax[i][1]>lengthMax)
                    lengthMax = lengthNbMax[i][1];
            return lengthMax;
    }

    /**
     * Vérifie que le nombre saisi est bien au format 12345,678 ou 12345
     * @param nb champ à tester
     * @return true si le nombre est au format correct; false sinon
     */

    public boolean testFormat(LimitedTextFieldOperations nb){
       String str = nb.getText();
       int cptVirgule = 0;
       int i=0;
       for (i = 0; i<= str.length()-1;i++)
        {   int codeAscii = (int) str.charAt(i);
            if (codeAscii == 44)     // Le caractère pointé est une virgule
            {   cptVirgule = 1;
                if (i>5)
                {  JOptionPane.showMessageDialog(this,  "Il y a trop de chiffres avant la virgule !",  "Attention", JOptionPane.INFORMATION_MESSAGE);
                   return false;
                }
                if (str.length()-1-i>3)
                {   JOptionPane.showMessageDialog(this,  "Il y a trop de chiffres après la virgule !",  "Attention", JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
            }
        }
        if (cptVirgule == 0 && str.length() > 5)
        {   JOptionPane.showMessageDialog(this,  "L'entier doit être limité à 5 chiffres !",  "Attention", JOptionPane.INFORMATION_MESSAGE);
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
     * Méthode qui positionne l'ensemble des composants du JPanel Addition.
     * Ce positionnement s'effectue en fonction d'un coefficient "coef", qui est calculé ici,
     * et qui s'adapte en fonction de la taille de la fenêtre.
     */

    @Override
    protected void positionComponent(){

            int i = 0;
            int lengthMaxInt = 0;
            CalculCoef();
//            float l = super.getSize().width;
//            float h = super.getSize().height;
//
//
//
            if (cptNbLigne == nbLigne || lengthIntMax()>4)   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                lengthMaxInt = lengthIntMax();
            else
                lengthMaxInt = 4;
//
//            coef = 1;
//            if (super.getSize().width >=784)
//                l = 784;
//            if ((super.getSize().height) >=657)
//                h = 657;
//
//            if (l<784 || h<657)     // On définit le coef de proportionnalité
//            {   l =  l/784;
//                h = h/657;
//
//                if (l < h)
//                {   coef = l;
//                    x0 = (int)((super.getSize().width - 784*coef)/2);
//                }
//                else
//                {   coef = h;
//                    x0 = (int)((super.getSize().width - 784*coef)/2);
//                }
//            }
//            else
//                x0 = (int)((super.getSize().width - l)/2);

            police = new Font("Arial", Font.BOLD, (int) (50*coef));
            more.setBounds((int) (6*coef), (int) (-13*coef), (int) (60*coef), (int) (60*coef));
            more.setFont(new Font("Arial", Font.PLAIN, (int) (40*coef)));
            plus.setBounds((int) (x0 + (315 - (lengthMaxInt-1)*65)*coef),(int) ((210 + 80* (nbLigne-2))*coef),(int) (35*coef),(int) (35*coef));
            less.setBounds((int) (11*coef), (int) (-16*coef), (int) (60*coef), (int) (60*coef));
            less.setFont(new Font("Arial", Font.PLAIN, (int) (40*coef)));
            moins.setBounds((int) (x0 + (260 - (lengthMaxInt-1)*65)*coef),(int) ((210 + 80* (nbLigne-2))*coef),(int) (35*coef),(int) (35*coef));

            virguleResultat.setFont(police);
            virguleResultat.setBounds((int) (x0 + 510*coef),(int) ((270+80*(nbLigne-2))*coef),(int) (50*coef), (int) (50*coef));




            int x = 0;
            int y = 0;
            for (x=0; x<=maxX-1; x++)
            {   for (y=0; y<=maxY-1; y++)
                {   if (y<=4)
                        panChiffre[x][y].setBounds((int) (x0 + (180 + 65*y)*coef),(int) ((70 + 80*x)*coef),(int) (60*coef),(int) (60*coef));
                    else
                        panChiffre[x][y].setBounds((int) (x0 + (205 + 65*y)*coef),(int) ((70 + 80*x)*coef),(int) (60*coef),(int) (60*coef));
                }
            }


            for (x=0; x<=maxX-1; x++)
            {   for (y=0; y<=maxY-1; y++)
                {   chiffre[x][y].setFont(police);
                    chiffre[x][y].setBounds((int) (15*coef),(int) (2*coef),(int) (60*coef),(int) (60*coef));
                }
            }



            for (i=0; i<= maxX-1; i++)
            {
                modifierLigne[i].setBounds((int) Math.round(x0 + 730*coef),(int) Math.round((80 + 80*i)*coef),(int) Math.round(37*coef), (int) Math.round(45*coef));
                modifierLigne[i].setSized((int) Math.round(0*coef), (int) Math.round(3*coef),(int) Math.round(40*coef),(int) Math.round(40*coef));
                nb[i].setFont(police);
                nb[i].setBounds((int) (x0 + 250*coef),(int) ((70 + 80*i)*coef), (int) (lChamp*coef), (int) (hChamp*coef));
                virgule[i].setFont(police);
                virgule[i].setBounds((int) (x0 + 510*coef),(int) ((80+80*i)*coef), (int) (50*coef), (int) (50*coef));
            }

            for (i=0; i<= maxY; i++)
            {   resultat[i].setFont(police);
                if (i<=5)
                    resultat[i].setBounds((int) (x0 + (115+65*i)*coef),(int) ((100+80*nbLigne)*coef), (int) (60*coef), (int) (60*coef));
                else
                    resultat[i].setBounds((int) (x0 + (140+65*i)*coef),(int) ((100+80*nbLigne)*coef), (int) (60*coef), (int) (60*coef));
            }

            Font policeRetenu = new Font("Arial", Font.BOLD, (int) (40*coef)); //Création d'un police particulière pour les retenus
            for (i=0; i<= maxY-2; i++)
            {   retenu[i].setFont(policeRetenu);
                if (i<=4)
                    retenu[i].setBounds((int) (x0 + (190+65*i)*coef),(int) (10*coef),(int) (50*coef),(int) (50*coef));
                else
                    retenu[i].setBounds((int) (x0 + (215+65*i)*coef),(int) (10*coef),(int) (50*coef),(int) (50*coef));
            }
            this.repaint();  // on réactualise le pannel, pour permettre de mettre à jour le paintComponent
            this.validate();
    }




    /**
     * Méthode qui copie l'ensemble des éléments du JPanel d'Addition
     * afin de reconstruire un JPanel Impression mis en forme.
     * @return Un JPanel, copie du JPanel d'Addition principal, et mis en forme.
     */

    @Override
    public JPanel impression(){

        Impression panImpression = new Impression();

        if (cptNbLigne == nbLigne)
        {
            panImpression.setLayout(null);
            panImpression.setBounds(0,0,Addition.super.getSize().width, Addition.super.getSize().height);
            panImpression.setBackground(ColorManager.transparent());

            // Copie des chiffres d'opération
            JLabel copyChiffre[][] = {null};
            copyChiffre = Copie.copie(chiffre, maxX, maxY);
            for (int x=0; x<=maxX-1; x++)
            {   for (int y=0; y<=maxY-1; y++)
                {   panImpression.add(copyChiffre[x][y]);
                    if (y<=4)
                       copyChiffre[x][y].setBounds((int) (x0 + (195 + 65*y)*coef),(int) ((72 + 80*x)*coef),(int) (60*coef),(int) (60*coef));
                    else
                       copyChiffre[x][y].setBounds((int) (x0 + (220 + 65*y)*coef),(int) ((72 + 80*x)*coef),(int) (60*coef),(int) (60*coef));
                }
            }

            // Copie de la virgule de résultat
            if (lengthDecimalMax() !=0)
            {   JLabel copyVirguleResultat =  Copie.copie(virguleResultat);
                copyVirguleResultat.setBounds((int) (x0 + 510*coef),(int) ((270+80*(nbLigne-2))*coef),(int) (50*coef), (int) (50*coef));
                panImpression.add(copyVirguleResultat);
            }

            // Copie des virgules de ligne d'opération
            JLabel copyVirgule[] = {null};
            copyVirgule = Copie.copie(virgule, maxX);
            for (int i=0; i<= maxX-1; i++)
            {   if(lengthNbMax[i][1] !=0)
                    panImpression.add(copyVirgule[i]);
                copyVirgule[i].setBounds((int) (x0 + 510*coef),(int) ((80+80*i)*coef), (int) (50*coef), (int) (50*coef));
            }

            // Copie des Label du résultat
            JLabel copyResultat[] = {null};
            copyResultat = Copie.copie(resultat, maxY+1);
            for (int i=0; i<= maxY; i++)
            {   panImpression.add(copyResultat[i]);
                if (i<=5)
                    copyResultat[i].setBounds((int) (x0 + (130+65*i)*coef),(int) ((100+80*nbLigne)*coef), (int) (60*coef), (int) (60*coef));
                else
                    copyResultat[i].setBounds((int) (x0 + (155+65*i)*coef),(int) ((100+80*nbLigne)*coef), (int) (60*coef), (int) (60*coef));
            }

            // Copie des Label de retenue
            JLabel copyRetenu[] = {null};
            copyRetenu = Copie.copie(retenu, maxY-1);
            for (int i=0; i<= maxY-2; i++)
            {   panImpression.add(copyRetenu[i]);
               if (i<=4)
                    copyRetenu[i].setBounds((int) (x0 + (205+65*i)*coef),(int) (10*coef),(int) (50*coef),(int) (50*coef));
                else
                    copyRetenu[i].setBounds((int) (x0 + (230+65*i)*coef),(int) (10*coef),(int) (50*coef),(int) (50*coef));
            }
        }
        return panImpression;
      }


    /**
    * Classe de JPanel qui copie le JPanel principal
    */

    public class Impression extends JPanel{

        public Impression(){}

        /**
         * Copie de la méthode de la classe mère pour le JPanel d'Impression.
         * @param g Le Graphics
         */

        @Override
        public void paintComponent(Graphics  g){
                int i=2;
                int lengthMaxInt = 0;    // Longueur maximale des parties entières
                int lengthMaxDec = 0;    // Longueur maximale des parties décimale

                Graphics2D g2d = (Graphics2D)g; // Crée un Graphique de type 2d
                super.paintComponent(g); // Redessine le Panel avant d'ajouter les composants

                Stroke epaisseur = new BasicStroke((int) (6*coef));    // Crée une variable contenant l'épaisseur du trait (ici : 6)
                g2d.setStroke(epaisseur); // Donne au trait son épaisseur

                // Tracée de la ligne
                if (cptNbLigne == nbLigne || lengthIntMax()>4)   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                    lengthMaxInt = lengthIntMax();
                else
                    lengthMaxInt = 4;

                if (lengthDecimalMax() != 0)     //Si le nombre est décimal
                {   lengthMaxDec = lengthDecimalMax();
                    g2d.drawLine((int) (x0 + (380 - (lengthMaxInt-1)*65)*coef), (int)((240 + 80*(nbLigne-2))*coef), (int) (x0 + (525 + lengthMaxDec*65)*coef), (int) ((240 + 80*(nbLigne-2))*coef)); // Trace le trait en fonction des (x,y) de deux points
                }
                else      //Si le nombre n'est pas décimal
                    g2d.drawLine((int) (x0 + (380 - (lengthMaxInt-1)*65)*coef),(int) ((240 + 80*(nbLigne-2))*coef), (int) (x0 + 500*coef), (int) ((240 + 80*(nbLigne-2))*coef)); // Trace le trait en fonction des (x,y) de deux points

                // Tracée du symbole "+"
                if (cptNbLigne == nbLigne || lengthIntMax()>4)   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                    lengthMaxInt = lengthIntMax();
                else
                    lengthMaxInt = 4;   // Par défaut, on se place à la distance correspondant à un champ de texte non mis en forme

                for (i=2; i<=nbLigne; i++)
                {   g2d.drawLine((int) (x0 + (395 - (lengthMaxInt-1)*65)*coef),(int) ((160 + 80*(i-2))*coef),(int) (x0 + (395 - (lengthMaxInt-1)*65)*coef), (int) ((200 + 80*(i-2))*coef));  // Trait vertical
                    g2d.drawLine((int) (x0 + (375 - (lengthMaxInt-1)*65)*coef) ,(int) ((180 + 80*(i-2))*coef),(int) (x0 + (415 - (lengthMaxInt-1)*65)*coef),(int) ((180 + 80*(i-2))*coef));   //Trait horizontal
                }
                g2d.setStroke(new BasicStroke(1));
        }
    }


    /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en x du début de l'opération
     */

    @Override
    public int tailleX0(){
            return (int) (x0 + (370 - (lengthIntMax()-1)*65)*coef);
    }

    /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en y du début de l'opération
     */

    @Override
    public int tailleY0(){
            return (int) (10*coef);
    }

     /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en x de la fin de l'opération
     */

    @Override
    public int tailleX1(){
            if (lengthDecimalMax() != 0)     //Si le nombre est décimal
                return (int) (x0 + (535 +  lengthDecimalMax()*65)*coef);
            else      //Si le nombre n'est pas décimal
                return (int) (x0 + 510*coef);
    }

     /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en y de la fin de l'opération
     */

    @Override
    public int tailleY1(){
            return (int) ((170+80*nbLigne)*coef);
    }


}