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
// 1er nombre entré avec 5 entiers max et 2 chiffres après la virgule max (8 caractères au total)
// 2ème nombre entré avec 5 entiers max et 2 chiffres après la virgule max (8 caractères au total)

package bomehc.operations;

import bomehc.elements.ChangeModeListener;
import bomehc.utils.managers.ColorManager;
import java.awt.BasicStroke;    // Pour l'épaisseur des traits
import javax.swing.JPanel;      // Pour la gestion des JPanel
import java.awt.Color;          // Pour la gestion des couleurs des objets
import java.awt.Font;           // Gestion de la police d'écriture
import java.awt.Graphics;       // Création du type "Graphics"
import java.awt.Graphics2D;     //Création du type "Graphics2D"
import java.awt.Stroke;         // Pour changer l'épaisseur des traits
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JTextField;  // Créer un champ de texte
import java.awt.event.KeyEvent; // Nécessaire pour les listener clavier
import java.awt.event.KeyListener;  // Nécessaire pour les listener clavier
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Classe définissant un Panel pour mettre en forme une soustraction
 */

public class Soustraction extends OperationType implements ComponentListener, MouseListener, Serializable{

       // public static int dateCreation = 0;// JourMoisAnnée du jour de création du Panel
      //  public static int timeCreation = 0; // HeureMinuteSeconde de la création du Panel

        public int lPanel = 800; //Longueur du Panel de travail Addition (800 optimal)
        public int hPanel = 750-55; // Hauteur du Panel de travail Addition(695 optimal)

        private LimitedTextFieldOperations nb[] = {new LimitedTextFieldOperations(8), new LimitedTextFieldOperations(8)}; // Initialisation d'un tableau de 2 champs de texte
        private LimitedTextFieldOperations resultat[] = {null};   // Création du tableau des champs de résultat
        private JPanel panChiffre[][] = {null};    // Tableau des JPanel de mise en forme des chiffres
        private JLabel chiffre[][] = {null};    // Tableau des JLabel contenant les chiffre
        private LimitedTextFieldOperations retenu[] = {null};  // Tableau des champs de retenu
        private LimitedTextFieldOperations retenuDizaine[] = {null};  // Tableau des champs de dizaine
        public BoutonIcone modifierLigne[] = {null};// Tableau des boutons permettant de modifier une ligne
        private int maxY = 7;   // Désigne le nombre de chiffre maximal autorisé (avec maximum 5 avant la virgule, et 3 après la virgule)
        private boolean etat[] = {false,false}; // Tableau qui renvoie "false" si le nombre est à l'état de champ de texte et "true" s'il est mis en forme
        public int lChamp = 250;    // Largeur d'un champ de texte
        public int hChamp = 60;     // Hauteur d'un champ de texte
        public Font police = new Font("Arial", Font.BOLD, (hChamp - 10)); //Crée une police
        public int lengthNbMax[][] = {{0,0},{0,0}}; //Tableau qui renvoie la longueur de chaque entier et de la longueur de la partie décimal associée lorsque les nombres sont mis en forme
        public int cptNbLigne = 0;    // Compte le nombre de ligne mises en forme
        public JLabel virgule[] = {null};   // Tableau des virgules de chacun des chiffres d'opérations
        public JLabel virguleResultat = new JLabel(",");    // Label de la virgule du résultat


    /**
    * Crée le Panel de Soustraction, et sauvegarde la date de création
    * Remplit également les tableaux des éléments de Soustraction (cases de résultats, de retenues...)
    */

    public Soustraction(){

        this.addComponentListener(this);    // Ajout du pannel à la liste des écouteurs pour le redimenssionnement


        // Création du Panel de travail
        this.setBackground(ColorManager.get("color panel operation"));
        this.setLayout(null);


        // Création des champs de texte
        int i=0;
        for (i=0; i<=1; i++)
            nb[i].addKeyListener(new ClavierListener());   // Ajoute le champ de texte à une méthoee d'écoute de clavier
        this.add(nb[0]);    // Ajout de ce champ de texte à la fenêtre
        this.add(nb[1]);
        nb[0].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        nb[1].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));

        // Initialisation du tableau panChiffre[][] 
        // Initialisation du tableau chiffre[][] avec les Label
        panChiffre = new JPanel[2][maxY];
        chiffre = new JLabel[2][maxY];
      
        int j = 0;
        for (i=0; i<=1; i++)
        {   for (j=0; j<=maxY-1; j++)
            {   panChiffre[i][j] = new JPanel();
                chiffre[i][j] = new JLabel();
                if (i ==0)
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
        virgule = new JLabel[2];

        for (i = 0; i <= 1; i++)
        {    virgule[i] = new JLabel(",");
             virgule[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }
        


        // Initialisation du tableau de résultats
        resultat = new LimitedTextFieldOperations[maxY];

        for (i=0; i<= maxY-1; i++)
        {   resultat[i] = new LimitedTextFieldOperations(1);
            resultat[i].setHorizontalAlignment(JTextField.CENTER);
            resultat[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }


        // Initialisation du tableau de retenu
        retenu = new LimitedTextFieldOperations[maxY-1];
        
        for (i=0; i<= maxY-2; i++)
        {   retenu[i] = new LimitedTextFieldOperations(1);
            retenu[i].setHorizontalAlignment(JTextField.CENTER);
            retenu[i].setForeground(ColorManager.get("color retenu foreground"));
            retenu[i].setBackground(ColorManager.get("color retenu background"));
            retenu[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }

         // Initialisation du tableau de retenu de dizaine
           retenuDizaine = new LimitedTextFieldOperations[maxY-1];
          
        for (i=0; i<= maxY-2; i++)
        {   retenuDizaine[i] = new LimitedTextFieldOperations(1);
            retenuDizaine[i].setHorizontalAlignment(JTextField.CENTER);
            retenuDizaine[i].setForeground(ColorManager.get("color retenu dizaine foreground"));
            retenuDizaine[i].setBackground(ColorManager.get("color retenu dizaine background"));
            retenuDizaine[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }


        // Création du bouton modifier nombre
         modifierLigne = new BoutonIcone[2];

        for (i=0; i<= 1; i++)
        {   modifierLigne[i] = new BoutonIcone("images/Modifier_Down.png", "images/Modifier_Up.png");
            modifierLigne[i].addMouseListener(this);
            modifierLigne[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }

    }

  /**
   * Crée un nouveau Panel de Soustraction
   * @return Nouveau Panel de Soustraction
   */

    @Override
    public Soustraction nouveau() {
        return new Soustraction();
    }



  /**
  * Méthode qui vérifie sur le nombre saisi est correct
  * et qui met en forme les nombres pour l'opération
  * en effectuant les modifications nécessaires
  * Est appelé à l'appui de la touche "Entrée" et
  * concerne la ligne dont le champ de texte contient le curseur
  */

  public class ClavierListener implements KeyListener{


    public void keyPressed(KeyEvent e) {        // Pour détecter quand un nombre est tapé dans le champ de texte
            int i=0;
            int j=0;
            String avVirgule = null;    // Chaine qui contiendra la partie entière du nombre
            String apVirgule = null;    // Chaine qui contiendra la partie décimale du nombre

            boolean isDecimal = false;  // Booléen qui renvoie false si le nombre est entier et true s'il est décimal

            if (e.getKeyCode() == 10 && (e.getSource() == nb[0] || e.getSource() == nb[1]))  // Touche "entrée" pressée
            {   if (nb[0].hasFocus()) // Curseur dans chemp de texte 0
                    i = 0;
                if (nb[1].hasFocus())
                    i = 1;

                if (nb[i].testDecimal() == true && testFormat(nb[i]) == true)    // Le nombre saisi est correct
                {   etat[i] = true;

                    // On teste la présence d'une virgule dans le nombre saisi pour l'afficher ou non
                    for (j = 0; j<= nb[i].getText().length()-1;j++)
                    {   int codeAscii = (int) nb[i].getText().charAt(j);
                        if (codeAscii == 44)     // Il y a une virgule dans le nombre
                        {   isDecimal = true;
                            add(virgule[i]); // On affiche la virgule sur le Panel
                            apVirgule = nb[i].getText().substring(j+1, nb[i].getText().length());   // On crée deux sous-chaine
                            avVirgule = nb[i].getText().substring(0, j);
                        }
                    }


                    if (isDecimal == true)   //Cas où le nombre est à virgule
                    {   remove(nb[i]);
                        for (j = avVirgule.length()-1; j>=0; j--)
                        {   chiffre[i][maxY-2-avVirgule.length()+j].setText(""+ avVirgule.charAt(j));
                            panChiffre[i][maxY-2-avVirgule.length()+j].add(chiffre[i][maxY-2-avVirgule.length()+j]);
                            add(panChiffre[i][maxY-2-avVirgule.length()+j]);

                        }
                        
                        for (j = 0; j <= apVirgule.length()-1; j++)
                        {   chiffre[i][maxY-2+j].setText(""+ apVirgule.charAt(j));
                            panChiffre[i][maxY-2+j].add(chiffre[i][maxY-2+j]);
                            add(panChiffre[i][maxY-2+j]);
                         }
                          if (i == 0)
                          {      for(j= avVirgule.length()-2;j>=0; j--)
                                    add(retenuDizaine[maxY-2-avVirgule.length()+j]);
                                for(j = apVirgule.length()-1;j>=0; j--)
                                    add(retenuDizaine[maxY-3+j]);
                          }
                        

                        lengthNbMax[i][0] = avVirgule.length();
                        lengthNbMax[i][1] = apVirgule.length();
                    }

                    if (isDecimal == false)   //Cas où le nombre n'est pas à virgule
                    {   remove(nb[i]);
                        for (j = nb[i].getText().length()-1; j>=0; j--)
                        {   chiffre[i][maxY-2-nb[i].getText().length()+j].setText(""+ nb[i].getText().charAt(j));
                            panChiffre[i][maxY-2-nb[i].getText().length()+j].add(chiffre[i][maxY-2-nb[i].getText().length()+j]);
                            add(panChiffre[i][maxY-2-nb[i].getText().length()+j]);
                        }
                        if (i == 0)
                            for(j= nb[i].getText().length()-2;j>=0; j--)
                                add(retenuDizaine[maxY-2-nb[i].getText().length()+j]);

                        lengthNbMax[i][0] = nb[i].getText().length();
                    }

                    cptNbLigne++;
                    // Affichage des Panels de résultat
                    afficherResultat();
                    add(modifierLigne[i]);

                }
            }
            // On détermine qui a le focus après la mise en forme
            if(lengthDecimalMax() == 0)
                resultat[4].requestFocus();
            else
                resultat[4+lengthDecimalMax()].requestFocus();
            int indice=0;
            while(indice<2)
            {   if(etat[indice] == false)
                {   nb[indice].requestFocus();
                    break;
                }
                indice++;
            }

            repaint();
    }

    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

  }

   public void actionPerformed(ActionEvent e) {}

    /**
    * Gère les boutons "ModifierLigne"
    * Permet de modifier une ligne de soustraction
    * @param e MouseEvent
    */

    public void mouseClicked(MouseEvent e) {
        // Caractérise les boutons de modification de ligne
        for (int i=0; i<=1; i++)    // On écoute chaque ligne
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
                     for (int j=0; j<= maxY-1; j++)
                        resultat[j].setText("");
                    for (int j=0; j<= maxY-2; j++)
                        retenu[j].setText("");
                    for (int j=0; j<=maxY-2;j++)
                        retenuDizaine[j].setText("");
                    if (i == 0)
                        for (int j=0; j<=maxY-2;j++)    // On enlève les retenuDizaine
                            this.remove(retenuDizaine[j]);

                    this.add(nb[i]);
                    //nb[i].requestFocus();
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
     * Gère la position des "-" et de la barre de soustraction en fonction du nombre de ligne d'opération et de la taille de la fenêtre
     * @param g Le Graphics
     */
    @Override
    public void paintComponent(Graphics  g){

            int lengthMaxInt = 0;    // Longueur maximale des parties entières
            int lengthMaxDec = 0;    // Longueur maximale des parties décimale

            CalculCoef();
            Graphics2D g2d = (Graphics2D)g; // Crée un Graphique de type 2d
            super.paintComponent(g); // Redessine le Panel avant d'ajouter les composants 
            Stroke epaisseur = new BasicStroke(6*coef);    // Crée une variable contenant l'épaisseur du trait (ici : 6)
            g2d.setStroke(epaisseur);    // Donne au trait son épaisseur

            // Tracée de la ligne

            if (cptNbLigne == 2|| lengthIntMax()>3)   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                lengthMaxInt = lengthIntMax();
            else
                lengthMaxInt = 3;
            if (cptNbLigne == 2)
            {   if (lengthDecimalMax() != 0)     //Si le nombre est décimal
                {    lengthMaxDec = lengthDecimalMax();
                     g2d.drawLine((int) (x0+(380 - (lengthMaxInt-1)*90)*coef), (int) (290*coef), (int) (x0 + (525 + lengthMaxDec*90)*coef), (int) (290*coef)); // Trace le trait en fonction des (x,y) de deux points
                }
                else      //Si le nombre n'est pas décimal
                    g2d.drawLine((int) (x0 +(380 - (lengthMaxInt-1)*90)*coef), (int) (290*coef), (int) (x0 + 500*coef), (int) (290*coef)); // Trace le trait en fonction des (x,y) de deux points
            }
            else
                g2d.drawLine((int) (x0+(380 - (lengthMaxInt-1)*90)*coef), (int) (240*coef), (int) (x0 + 500*coef), (int) (240*coef));

            // Tracée du symbole "-"
            if (cptNbLigne == 2 || lengthIntMax()>3)   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                lengthMaxInt = lengthIntMax();
            else
                lengthMaxInt = 3;   // Par défaut, on se place à la distance correspondant à un champ de texte non mis en forme

            g2d.drawLine((int) (x0 + (375 - (lengthMaxInt-1)*90)*coef),(int) (180*coef),(int) (x0+(410 - (lengthMaxInt-1)*90)*coef),(int) (180*coef));   //Trait horizontal
            g2d.setStroke(new BasicStroke(1));
     }


    /**
    * Méthode qui met en forme les cases de résultats et les retenues en fonction
    * du nombre de chiffres dans la soustraction
    */
    public void afficherResultat(){
            int lengthMax =0;   // Renvoie la longueur maximale d'un nombre mis en forme
            int i=0;

            // On réinitialise les cases de résultats ou de retenus
            for (i=0; i<=maxY-1;i++)
                this.remove(resultat[i]);
            for (i=0; i<=maxY-2;i++)
                this.remove(retenu[i]);
            this.remove(virguleResultat);

            if (etat[0] == true && etat[1] == true)
            {   // On réaffiche les cases de résultat et la virgule
                lengthMax = lengthIntMax();
                if (lengthMax !=0)      // Si le nombre n'est pas vide
                    for (i=0;i<=lengthMax-1; i++)
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
                        add(retenu[maxY-3-i]);

                    lengthMax = lengthDecimalMax();
                    for (i=1;i<=lengthMax-1; i++)  // On affiche les cases de retenu sur tous les décimal sauf le dernier
                        add(retenu[maxY-3+i]);
                }
                else        // Le nombre est seulement un entier
                {   if (lengthMax != 0 && lengthMax != 1)
                        for (i=1;i<=lengthMax-1; i++)   // On affiche les cases de retenu sur tous les chiffres, sauf celui des unités
                            add(retenu[maxY-3-i]);
                }
            }
            
            positionComponent();    // On positionne la virgule du résultat
            resultat[4].requestFocus();
    }    // Fin de la méthode "afficherResultat"


    /**
     * Méthode qui renvoie le maximum des longueurs de la partie entière des nombres de chaque ligne
     * @return La longueur de la ligne ayant la plus longue partie entière
     */

    public int lengthIntMax(){
            int lengthMax = 0;   // Renvoie la longueur maximale d'un nombre mis en forme
            int i=0;
            for (i=0;i<=1; i++)       // On définit la valeur du maximum en tant qu'entier
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
            for (i=0;i<=1; i++)       // On définit la valeur du maximum en tant que décimal
                if (lengthNbMax[i][1]>lengthMax)
                    lengthMax = lengthNbMax[i][1];
            return lengthMax;
    }


     /**
     * Vérifie que le nombre saisi est bien au format 12345,67 ou 12345
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
                    {   JOptionPane.showMessageDialog(this,  "Il y a trop de chiffres avant la virgule !",  "Attention", JOptionPane.INFORMATION_MESSAGE);
                        return false;
                    }
                    if (str.length()-1-i>2)
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
     * Méthode qui positionne l'ensemble des composants du JPanel Soustraction.
     * Ce positionnement s'effectue en fonction d'un coefficient "coef", qui est calculé ici,
     * et qui s'adapte en fonction de la taille de la fenêtre.
     */

    @Override
     protected void positionComponent(){

            int i = 0;

            CalculCoef();


            police = new Font("Arial", Font.BOLD, (int) (50*coef));
            virguleResultat.setFont(police);
            virguleResultat.setBounds((int) (x0 + 510*coef),(int) (320*coef), (int) (50*coef), (int) (50*coef));


            int x = 0;
            int y = 0;
            for (x=0; x<=1; x++)
            {   for (y=0; y<=maxY-1; y++)
                {   if (y<=4)
                        panChiffre[x][y].setBounds((int) (x0 + (75 + 90*y)*coef),(int) ((70 + 80*x)*coef),(int) (60*coef),(int) (60*coef));
                    else
                        panChiffre[x][y].setBounds((int) (x0 +(100 + 90*y)*coef),(int) ((70 + 80*x)*coef),(int) (60*coef),(int) (60*coef));
                }
            }


            for (x=0; x<=1; x++)
            {   for (y=0; y<=maxY-1; y++)
                {   chiffre[x][y].setFont(police);
                    chiffre[x][y].setBounds((int) (15*coef),(int) (2*coef),(int) (60*coef),(int) (60*coef));
                }
            }




            for (i=0; i<= 1; i++)
            {   modifierLigne[i].setBounds((int) (x0 + 730*coef),(int) ((80 + 80*i)*coef),(int) (37*coef), (int) (45*coef));
                modifierLigne[i].setSized((int) (0*coef), (int) (3*coef),(int) (40*coef),(int) (40*coef));
                nb[i].setFont(police);
                nb[i].setBounds((int) (x0 + 250*coef),(int) ((70 + 80*i)*coef), (int) (lChamp*coef), (int) (hChamp*coef));
                virgule[i].setFont(police);
                virgule[i].setBounds((int) (x0 + 510*coef),(int) ((80+80*i)*coef), (int) (50*coef), (int) (50*coef));
            }

            for (i=0; i<= maxY-1; i++)
            {   resultat[i].setFont(police);
                if (i<=4)
                    resultat[i].setBounds((int) (x0 +(75 + 90*i)*coef), (int) (310*coef), (int) (60*coef), (int) (60*coef));    // On positionne cette virgule à la bonne ligne
                else
                    resultat[i].setBounds((int) (x0+(100 + 90*i)*coef), (int) (310*coef), (int) (60*coef), (int) (60*coef));
                resultat[i].setHorizontalAlignment(JTextField.CENTER);
            }


            Font policeRetenu = new Font("Arial", Font.BOLD, (int) (40*coef));
            for (i=0; i<= maxY-2; i++)
            {   retenu[i].setFont(policeRetenu);
                if (i<=4)
                    retenu[i].setBounds((int) (x0 + (85+90*i)*coef),(int) (220*coef), (int) (50*coef), (int) (50*coef));    // On positionne cette virgule à la bonne ligne
                else
                    retenu[i].setBounds((int) (x0 + (110+90*i)*coef),(int) (220*coef), (int) (50*coef), (int) (50*coef));
            }

            Font policeRetenuDizaine = new Font("Arial", Font.BOLD, (int)(30*coef));
            for (i=0; i<= maxY-2; i++)
            {   retenuDizaine[i].setFont(policeRetenuDizaine);
                if (i<=3)
                    retenuDizaine[i].setBounds((int) (x0+ (140 + 90*i)*coef),(int) (105*coef), (int) (25*coef), (int) (40*coef));    // On positionne cette virgule à la bonne ligne
                else
                    retenuDizaine[i].setBounds((int) (x0 + (165 + 90*i)*coef),(int) (105*coef), (int) (25*coef), (int) (40*coef));
            }

            this.repaint();
     }

     
    
    /**
     * Méthode qui copie l'ensemble des éléments du JPanel Soustraction
     * afin de reconstruire un JPanel Impression mis en forme.
     * @return Un JPanel, copie du JPanel de Soustraction principal, et mis en forme.
     */

    @Override
     public JPanel impression(){

        Impression panImpression = new Impression();

        if (cptNbLigne == 2)
        {
            panImpression.setLayout(null);
            panImpression.setBounds(0,0,Soustraction.super.getSize().width, Soustraction.super.getSize().height);
            panImpression.setBackground(ColorManager.transparent());

            // Copie des chiffres d'opération
            JLabel copyChiffre[][] = {null};
            copyChiffre = Copie.copie(chiffre, 2, maxY);
            for (int x=0; x<=1; x++)
            {   for (int y=0; y<=maxY-1; y++)
                {   panImpression.add(copyChiffre[x][y]);
                    if (y<=4)
                        copyChiffre[x][y].setBounds((int) (x0 + (75 + 90*y)*coef),(int) ((70 + 80*x)*coef),(int) (60*coef),(int) (60*coef));
                    else
                        copyChiffre[x][y].setBounds((int) (x0 +(100 + 90*y)*coef),(int) ((70 + 80*x)*coef),(int) (60*coef),(int) (60*coef));
               }
            }

            // Copie de la virgule de résultat
            if (lengthDecimalMax() !=0)
            {   JLabel copyVirguleResultat =  Copie.copie(virguleResultat);
                copyVirguleResultat.setBounds((int) (x0 + 510*coef),(int) (320*coef), (int) (50*coef), (int) (50*coef));
                panImpression.add(copyVirguleResultat);
            }

            // Copie des virgules de ligne d'opération
            JLabel copyVirgule[] = {null};
            copyVirgule = Copie.copie(virgule, 2);
            for (int i=0; i<= 1; i++)
            {   if(lengthNbMax[i][1] !=0)
                    panImpression.add(copyVirgule[i]);
                copyVirgule[i].setBounds((int) (x0 + 510*coef),(int) ((80+80*i)*coef), (int) (50*coef), (int) (50*coef));
            }

            // Copie des Label du résultat
            JLabel copyResultat[] = {null};
            copyResultat = Copie.copie(resultat, maxY);
            for (int i=0; i<= maxY-1; i++)
            {   panImpression.add(copyResultat[i]);
                if (i<=4)
                    copyResultat[i].setBounds((int) (x0 +(75 + 90*i)*coef), (int) (310*coef), (int) (60*coef), (int) (60*coef));    // On positionne cette virgule à la bonne ligne
                else
                    copyResultat[i].setBounds((int) (x0+(100 + 90*i)*coef), (int) (310*coef), (int) (60*coef), (int) (60*coef));
            }

            // Copie des Label de retenue
            JLabel copyRetenu[] = {null};
            copyRetenu = Copie.copie(retenu, maxY-1);
            for (int i=0; i<= maxY-2; i++)
            {   panImpression.add(copyRetenu[i]);
                if (i<=4)
                    copyRetenu[i].setBounds((int) (x0 + (85+90*i)*coef),(int) (220*coef), (int) (50*coef), (int) (50*coef));    // On positionne cette virgule à la bonne ligne
                else
                    copyRetenu[i].setBounds((int) (x0 + (110+90*i)*coef),(int) (220*coef), (int) (50*coef), (int) (50*coef));
           }

            // Copie des Label de retenue des dizaines
            JLabel copyRetenuDizaine[] = {null};
            copyRetenuDizaine = Copie.copie(retenuDizaine, maxY-1);
            for (int i=0; i<= maxY-2; i++)
            {   panImpression.add(copyRetenuDizaine[i]);
                if (i<=3)
                    copyRetenuDizaine[i].setBounds((int) (x0+ (140 + 90*i)*coef),(int) (105*coef), (int) (25*coef), (int) (40*coef));    // On positionne cette virgule à la bonne ligne
                else
                    copyRetenuDizaine[i].setBounds((int) (x0 + (165 + 90*i)*coef),(int) (105*coef), (int) (25*coef), (int) (40*coef));
            }

        }
        return panImpression;
      }

    /**
    * Classe de JPanel qui copie le JPanel principal
    */

      public class Impression extends JPanel{

        public Impression(){}

        // Méthode qui s'occupe de la position des "+" et de la barre d'addition
        @Override
        public void paintComponent(Graphics  g){
               int lengthMaxInt = 0;    // Longueur maximale des parties entières
            int lengthMaxDec = 0;    // Longueur maximale des parties décimale

            Graphics2D g2d = (Graphics2D)g; // Crée un Graphique de type 2d
            super.paintComponent(g); // Redessine le Panel avant d'ajouter les composants 
            Stroke epaisseur = new BasicStroke(6*coef);    // Crée une variable contenant l'épaisseur du trait (ici : 6)
            g2d.setStroke(epaisseur);    // Donne au trait son épaisseur

            // Tracée de la ligne

            if (cptNbLigne == 2|| lengthIntMax()>3)   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                lengthMaxInt = lengthIntMax();
            else
                lengthMaxInt = 3;
            if (cptNbLigne == 2)
            {   if (lengthDecimalMax() != 0)     //Si le nombre est décimal
                {    lengthMaxDec = lengthDecimalMax();
                     g2d.drawLine((int) (x0+(380 - (lengthMaxInt-1)*90)*coef), (int) (290*coef), (int) (x0 + (525 + lengthMaxDec*90)*coef), (int) (290*coef)); // Trace le trait en fonction des (x,y) de deux points
                }
                else      //Si le nombre n'est pas décimal
                    g2d.drawLine((int) (x0 +(380 - (lengthMaxInt-1)*90)*coef), (int) (290*coef), (int) (x0 + 500*coef), (int) (290*coef)); // Trace le trait en fonction des (x,y) de deux points
            }
            else
                g2d.drawLine((int) (x0+(380 - (lengthMaxInt-1)*90)*coef), (int) (240*coef), (int) (x0 + 500*coef), (int) (240*coef));

            // Tracée du symbole "-"
            if (cptNbLigne == 2 || lengthIntMax()>3)   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                lengthMaxInt = lengthIntMax();
            else
                lengthMaxInt = 3;   // Par défaut, on se place à la distance correspondant à un champ de texte non mis en forme

            g2d.drawLine((int) (x0 + (375 - (lengthMaxInt-1)*90)*coef),(int) (180*coef),(int) (x0+(410 - (lengthMaxInt-1)*90)*coef),(int) (180*coef));   //Trait horizontal
            g2d.setStroke(new BasicStroke(1));
        }
    }

    /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en x du début de l'opération
     */

    @Override
    public int tailleX0(){
            return (int) (x0 + (365 - (lengthIntMax()-1)*90)*coef);
    }

    /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en y du début de l'opération
     */

    @Override
    public int tailleY0(){
            return (int) (70*coef);

    }

     /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en x de la fin de l'opération
     */

    @Override
    public int tailleX1(){
            if (lengthDecimalMax() != 0)     //Si le nombre est décimal
                return (int) (x0 + (525 + lengthDecimalMax()*90)*coef);

            else      //Si le nombre n'est pas décimal
                return (int) (x0 + 540*coef);
    }

     /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en y de la fin de l'opération
     */

    @Override
    public int tailleY1(){
            return (int) (380*coef);
    }


}



