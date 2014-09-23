/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// 1er nombre entré avec 5 entiers max et 3 chiffres après la virgule max (9 caractères au total)
// 2ème nombre entré avec 4 chiffres max, et virgule possible (5 caractères au total)

package matheos.operations;

import matheos.elements.ChangeModeListener;
import matheos.utils.managers.ColorManager;
import java.awt.BasicStroke;    // Pour l'épaisseur des traits
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Classe définissant un Panel pour mettre en forme une Multiplication
 */

public class Multiplication extends OperationType implements ActionListener, ComponentListener, MouseListener, KeyListener, Serializable{

        public int lPanel = 800; //Longueur du Panel de travail Addition (800 optimal)
        public int hPanel = 750-55; // Hauteur du Panel de travail Addition(695 optimal)

        private LimitedTextFieldOperations nb[] = {new LimitedTextFieldOperations(9), new LimitedTextFieldOperations(5)};  // Création d'un tableau de champ de texte
        private LimitedTextFieldOperations resultat[] = {null};   // Création du tableau des champs de résultat
        private JPanel panChiffre[][] = {null};    // Tableau des JPanel de mise en forme des chiffres
        private JLabel chiffre[][] = {null};    // Tableau des JLabel contenant les chiffre
        private LimitedTextFieldOperations retenu[] ={null};  // Tableau des champs de retenu
        private LimitedTextFieldOperations retenuAdd[] ={null};  // Tableau des champs de retenu pour l'addition
        public BoutonIcone modifierLigne[] = {null};// Tableau des boutons permettant de modifier une ligne
        private LimitedTextFieldOperations fieldChiffre[][] = {null};    // Tableau des champs de textes pour les chiffres de l'addition intermédiaire
        public static int maxY = 8;   // Désigne le nombre de chiffre maximal autorisé (avec maximum 5 avant la virgule, et 3 après la virgule)
        private boolean etat[] = {false,false}; // Tableau qui renvoie "false" si le nombre est à l'état de champ de texte et "true" s'il est mis en forme
        private int nbLigne = 0;     // Nombre de ligne d'addition
        public int lChamp = 250;    // Largeur d'un champ de texte
        public int hChamp = 60;     // Hauteur d'un champ de texte
        public Font police = new Font("Arial", Font.BOLD, (hChamp - 10)); //Crée une police
        public int lengthNbMax[][] = {{0,0},{0,0}}; //Tableau qui renvoie la longueur de chaque entier et de la longueur de la partie décimal associée lorsque les nombres sont mis en forme
        public int cptNbLigne = 0;    // Compte le nombre de ligne mises en forme
        public JLabel virgule[] = {null};   // Tableau des virgules de chacun des chiffres d'opérations
        public VirguleOperation virguleResultat[] = {null};   // Panel pouvant recevoir la virgule au résultat
        public int positionVirguleResultat = -1; // Renvoie le Panel de chiffre associé à la virgule au résultat
        public JLabel labVirguleResultat = new JLabel(","); //Label de la virgule du résultat

        public BoutonIcone raz = new BoutonIcone("images/Remise_A_Zero_Down.png", "images/Remise_A_Zero_Up.png"); // Bouton de remise à "0" des retenues de multiplication pour le calcul suivant
        //public JLabel razLabel = new JLabel("0");    //JLabel du bouton Remise à zéro des retenus
        public int nbField = 0;
        public int d0=0;    // Déplacement des pannels, virgule et retenus pour la mise en forme
        public int d1=0;    // Déplacement des résultats, case d'addition, virgules résultats et retenus additions

    /**
    * Crée le Panel de mulitplication, et sauvegarde la date de création
    * Remplit également les tableaux des éléments d'Addition (cases de résultats, de retenues...)
    */
    public Multiplication(){

        this.addComponentListener(this);    // Ajout du pannel à la liste des écouteurs pour le redimenssionnement

        // On repère la date de création du Panel
        Calendar date = GregorianCalendar.getInstance();
        dateCreation = date.get(Calendar.DAY_OF_MONTH)*1000000 + (date.get(Calendar.MONTH)+1)*10000+date.get(Calendar.YEAR);
        timeCreation = date.get(Calendar.HOUR_OF_DAY)*10000 + date.get(Calendar.MINUTE)*100+ date.get(Calendar.SECOND);

        // Création du Panel de travail
        
        this.setBackground(ColorManager.get("color panel operation"));
        //this.setBackground(Color.CYAN);
        this.setLayout(null);


        // Création des champs de texte

        int i=0;
        for (i=0; i<=1; i++)
            nb[i].addKeyListener(this);   // Ajoute le champ de texte à une méthoee d'écoute de clavier
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

        for (i=0; i<= 1; i++)
        {    virgule[i] = new JLabel(",");
             virgule[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }
 

        // Initialisation du tableau de résultats

        resultat = new LimitedTextFieldOperations[maxY+1];

        for (i=0; i<= maxY; i++)
        {   resultat[i] = new LimitedTextFieldOperations(1);
            resultat[i].setHorizontalAlignment(JTextField.CENTER);
            resultat[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }


        // Création du tableau virguleResultat

        virguleResultat = new VirguleOperation[maxY];

        for (i=0; i<= maxY-1; i++)
        {   virguleResultat[i] = new VirguleOperation();
            virguleResultat[i].setBackground(ColorManager.get("color chiffre background"));
            virguleResultat[i].addKeyListener(this);
            virguleResultat[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
            virguleResultat[i].addMouseListener(this);
//            virguleResultat[i].addMouseListener(new MouseAdapter(){
//                @Override
//                public void mousePressed(MouseEvent e) {
//                    for (int i = 0; i <= maxY-1; i++) {
//                        if (e.getSource() == virguleResultat[i]) // Clic sur une virgule
//                        {
//                            if (positionVirguleResultat == i)    // Cas où il y a déjà une virgule sur le Panel cliqué
//                            {   virguleResultat[i].remove(labVirguleResultat);    // On enlève alors la virgule
//                                virguleResultat[i].remove(VirguleOperation.virgule);
//                                positionVirguleResultat = -1;
//                                repaint();
//                            }
//                            else    // Dans cas cas, il faut ajouter une virgule, et effacer la précédente s'il y en a une
//                            {   if (positionVirguleResultat != -1)   // Une virgule était déjà présente, on l'enlève
//                                {    virguleResultat[i].remove(labVirguleResultat);
//                                }
//                                virguleResultat[i].add(labVirguleResultat);   // on ajoute la nouvelle virgule
//                                positionVirguleResultat = i;
//                                repaint();
//                            }
//                        }
//                    }
//                }
//            });
        }



        // Initialisation du tableau de retenu de multiplication

        retenu = new LimitedTextFieldOperations[maxY-1];
       
        for (i=0; i<= maxY-2; i++)
        {   retenu[i] = new LimitedTextFieldOperations(1);
            retenu[i].setHorizontalAlignment(JTextField.CENTER);
            retenu[i].setForeground(ColorManager.get("color retenu foreground"));
            retenu[i].setBackground(ColorManager.get("color retenu background"));
            retenu[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }


          // Initialisation du tableau fieldChiffre[][] avec les Panel LimitedTextField

        fieldChiffre = new LimitedTextFieldOperations[4][maxY+1];

        for (i=0; i<=3; i++)
        {   for (j=0; j<=maxY; j++)
            {   fieldChiffre[i][j] = new LimitedTextFieldOperations(1);
                fieldChiffre[i][j].setHorizontalAlignment(JTextField.CENTER);
                fieldChiffre[i][j].setEnabled(false);
                fieldChiffre[i][j].setOpaque(false);
                fieldChiffre[i][j].setFocusable(false);
                fieldChiffre[i][j].addKeyListener(this);
                fieldChiffre[i][j].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
            }
        }



        // Initialisation du tableau de retenu d'addition

        retenuAdd = new LimitedTextFieldOperations[maxY];
     
        for (i=0; i<= maxY-1; i++)
        {   retenuAdd[i] = new LimitedTextFieldOperations(1);
            retenuAdd[i].setHorizontalAlignment(JTextField.CENTER);
            retenuAdd[i].setForeground(ColorManager.get("color retenu foreground"));
            retenuAdd[i].setBackground(ColorManager.get("color retenu background"));
            retenuAdd[i].setEnabled(false);
            retenuAdd[i].setOpaque(false);
            retenuAdd[i].setFocusable(false);
            retenuAdd[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }



        // Création du bouton modifier nombre

          modifierLigne = new BoutonIcone[2];
        //modif = new JLabel[maxX];

        for (i=0; i<= 1; i++)
        {   modifierLigne[i] = new BoutonIcone("images/Modifier_Down.png", "images/Modifier_Up.png");
            modifierLigne[i].addMouseListener(this);
            modifierLigne[i].addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        }


        // Création du bouton de remise à zéro des retenus

       // raz.setLayout(null);
        //raz.add(razLabel);
        raz.addMouseListener(this);
        raz.addMouseListener(new ChangeModeListener(ChangeModeListener.TP));


    }

   /**
   * Crée un nouveau Panel de multiplication
   * @return Nouveau Panel de multiplication
   */

    @Override
    public Multiplication nouveau() {
        return new Multiplication();
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

            if (e.getKeyCode() == 10 && (e.getSource() == nb[0] || e.getSource() == nb[1]))   // Touche "entrée" pressée et que l'on est dans un des champs pour saisir un nombre
            {   if (nb[0].hasFocus() == true) // Curseur dans champ de texte 0
                    i = 0;
                if (nb[1].hasFocus() == true)
                    i = 1;

                if (nb[i].testDecimal() == true )    // Le nombre saisi est correct
                if ((i == 0 && testFormat0() == true) || (i==1 && testFormat1() == true))   // Le nombre est bien au format autorisé et la somme des deux nombres saisis n'est pas trop longue
                {
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
                        {   chiffre[i][maxY-3-avVirgule.length()+j].setText(""+ avVirgule.charAt(j));
                            panChiffre[i][maxY-3-avVirgule.length()+j].add(chiffre[i][maxY-3-avVirgule.length()+j]);
                            add(panChiffre[i][maxY-3-avVirgule.length()+j]);
                        }

                        for (j = 0; j <= apVirgule.length()-1; j++)
                        {   chiffre[i][maxY-3+j].setText(""+ apVirgule.charAt(j));
                            panChiffre[i][maxY-3+j].add(chiffre[i][maxY-3+j]);
                            add(panChiffre[i][maxY-3+j]);
                        }
                        lengthNbMax[i][0] = avVirgule.length();
                        lengthNbMax[i][1] = apVirgule.length();

                    }

                    if (isDecimal == false)   //Cas où le nombre n'est pas à virgule
                    {   remove(nb[i]);
                        for (j = nb[i].getText().length()-1; j>=0; j--)
                        {   chiffre[i][maxY-3-nb[i].getText().length()+j].setText(""+ nb[i].getText().charAt(j));
                            panChiffre[i][maxY-3-nb[i].getText().length()+j].add(chiffre[i][maxY-3-nb[i].getText().length()+j]);
                            add(panChiffre[i][maxY-3-nb[i].getText().length()+j]);
                         }
                        lengthNbMax[i][0] = nb[i].getText().length();
                    }

                    etat[i] = true;
                    cptNbLigne++;
                    add(modifierLigne[i]);


                }

                nbLigne = lengthNbMax[1][0] + lengthNbMax[1][1];
                nbField = (lengthNbMax[0][0] + lengthNbMax[0][1]) + (lengthNbMax[1][0] + lengthNbMax[1][1]);

                if (etat[0] == true && etat[1] == true) // Les deux nombres sont mis en forme
                {   if ((nbField - lengthDecimalMax())>6 || lengthDecimalMax()!=0)   // Déplacement de la partie "multiplication"
                    {   d0 = (3 - lengthDecimalMax())*77;
                        d1 = 0;
                    }
                    else    // Déplacement de la partie "addition" 
                       d1 = (lengthDecimalMax()-3)*77;
                    if (nbLigne >=1)
                    {   fieldChiffre[0][maxY].setEnabled(true);
                        fieldChiffre[0][maxY].setOpaque(true);
                        fieldChiffre[0][maxY].setFocusable(true);
                    }

                    positionComponent();    // On positionne (les virgules, les panels de chiffres, les retenus)[cas if] ainsi que (les retenus d'addition et les champs d'addition)[cas else]
                    afficherResultat();
                }

                // On détermine qui a le focus après la mise en forme
                if(nbLigne <= 1)
                   resultat[maxY].requestFocus();
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
            
    }

    /**
     * Vérifie qu'on ne tape pas autre chose qu'une virgule
     * dans le champ de virgule au résultat
     * @param e KeyEvent
     */
   
    public void keyTyped(KeyEvent e) {
        int i=0;
        for (i=0;i<= maxY-1;i++)
                if (e.getSource() == virguleResultat[i])
                   if (e.getKeyChar() != ',')
                   {}//e.consume();
     }

    /**
     * Méthode qui affiche progressivement les cases d'additions intermédiaires.
     * Une nouvelle case s'affiche lorsqu'on rentre un caractère dans la case précédente.
     * La case de la ligne inférieur s'active lorsque la première case de la ligne au-dessus est remplie.
     * On affiche les retenues d'addition lorsque les dernières cases de la dernière ligne se remplissent.
     * @param e KeyEvent
     */
    public void keyReleased(KeyEvent e) {
        for(int i=0;i<=nbLigne-1; i++)
            for (int j=0; j<=maxY;j++)
                if(e.getComponent() == fieldChiffre[i][j])
                {    if (j != 0 && fieldChiffre[i][j].getText().length() ==1 ) //Si on est pas au début de la ligne
                                                                               //et qu'il y a un caractère saisi dans la case
                     {  if (j==maxY && i!=nbLigne-1)
                        {   fieldChiffre[i+1][j].setEnabled(true);
                            fieldChiffre[i+1][j].setOpaque(true);
                            fieldChiffre[i+1][j].setFocusable(true);
                        }
                        fieldChiffre[i][j-1].setEnabled(true);
                        fieldChiffre[i][j-1].setOpaque(true);
                        fieldChiffre[i][j-1].setFocusable(true);
                        if (i==nbLigne-1)
                        {   retenuAdd[j-1].setEnabled(true);
                            retenuAdd[j-1].setOpaque(true);
                            retenuAdd[j-1].setFocusable(true);
                        }
                     }
                }
   }



    public void actionPerformed(ActionEvent arg0) {}

    /**
    * Gère les boutons "ModifierLigne" et le bouton "raz" de remise à zéro des retenues.
    * ModifierLigne : Permet de modifier une ligne d'addition.
    * Raz : Efface le contenu des cases de retenu.
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
                    for (int j=0; j<=maxY-1; j++)
                        retenuAdd[j].setText("");
                    for (int j=0; j<=maxY-2; j++)
                        retenu[j].setText("");
                    for (int j=0; j<=maxY; j++)
                        resultat[j].setText("");
                    for (int k=0; k<=3; k++)
                        for (int j=0; j<=maxY; j++)
                            fieldChiffre[k][j].setText("");

            
                    this.add(nb[i]);
                    //nb[i].requestFocus();
                    nbLigne--;
                    nbField--;
                    d0=0;

                    afficherResultat();  // On met en forme de résultat et les retenues
                    positionComponent();  // On remet en place les éléments qui ont été mis en forme (virgule, panChiffre, retenu)
                }

          if(e.getSource() == raz)    // Si le bouton "raz" est activé, on remet la valeur des retenus à zéro
            for (int j=0; j<=6; j++)
                retenu[j].setText("");
    }

  /**
     * Méthode permettant de modifier un chiffre en cliquant dessus.
     * Cela ouvre une boîte de dialogue qui demander d'entrer le nouveau chiffre.
     * @param e MouseEvent
     */

    public void mousePressed(MouseEvent e) {
        for (int i=0; i<=panChiffre.length-1; i++){
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
        
        for (int i = 0; i <= maxY-1; i++) {
            if (e.getSource() == virguleResultat[i]) // Clic sur une virgule
            {
                if (positionVirguleResultat == i)    // Cas où il y a déjà une virgule sur le Panel cliqué
                {   virguleResultat[i].remove(labVirguleResultat);    // On enlève alors la virgule
                    virguleResultat[i].remove(VirguleOperation.virgule);
                    positionVirguleResultat = -1;
                    repaint();
                }
                else    // Dans cas cas, il faut ajouter une virgule, et effacer la précédente s'il y en a une
                {   if (positionVirguleResultat != -1)   // Une virgule était déjà présente, on l'enlève
                    {    virguleResultat[i].remove(labVirguleResultat);
                    }
                    virguleResultat[i].add(labVirguleResultat);   // on ajoute la nouvelle virgule
                    positionVirguleResultat = i;
                    repaint();
                }
            }
        }
    }


    public void mouseReleased(MouseEvent e) { }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}



    /**
     * Gère la position des "*" et de la barre de multiplication, de la barre d'addition et des "+" intermédiaires
     * en fonction du nombre de ligne d'opération et de la taille de la fenêtre
     * @param g Le Graphics
     */
    @Override
    public void paintComponent(Graphics  g){
            int i=2;
            int lengthMaxInt = 0;    // Longueur maximale des parties entières
            int lengthMaxDec = 0;    // Longueur maximale des parties décimale

            Graphics2D g2d = (Graphics2D)g; // Crée un Graphique de type 2d
            super.paintComponent(g); // Redessine le Panel avant d'ajouter les composants 
            Stroke epaisseur = new BasicStroke((float) (Math.max(1, 6*coef)));    // Crée une variable contenant l'épaisseur du trait (ici : 6)
            g2d.setStroke(epaisseur);    // Donne au trait son épaisseur

            // Tracée de la ligne de "x"
            if (cptNbLigne !=2 || lengthNbMax[1][0] + lengthNbMax[1][1] <2)
            {   if (lengthIntMax()>4 || (cptNbLigne == 2 &&lengthNbMax[1][0] + lengthNbMax[1][1] <2))   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                    lengthMaxInt = lengthIntMax();
                else
                    lengthMaxInt = 4;

                if (lengthDecimalMax() != 0)     //Si le nombre est décimal
                {    lengthMaxDec = lengthDecimalMax();
                    g2d.drawLine((int) (x0 + (d0 + 380 - (lengthMaxInt-1)*77)*coef), (int) (240*coef),(int) (x0 + (d0 + 505 + lengthMaxDec*77)*coef), (int) (240*coef)); // Trace le trait en fonction des (x,y) de deux points
                }
                else      //Si le nombre n'est pas décimal
                    g2d.drawLine((int) (x0 + (d0 + 380 - (lengthMaxInt-1)*77)*coef), (int) (240*coef),(int) (x0 + (d0 + 500)*coef), (int) (240*coef)); // Trace le trait en fonction des (x,y) de deux points
            }
            else
            {   lengthMaxInt = lengthIntMax();
                if (lengthDecimalMax() != 0)     //Si le nombre est décimal
                {    lengthMaxDec = lengthDecimalMax();
                    g2d.drawLine((int) (x0 + (d0 + 380 - (lengthMaxInt-1)*77)*coef), (int) (220*coef),(int) (x0 + (d0 + 505 + lengthMaxDec*77)*coef), (int) (220*coef)); // Trace le trait en fonction des (x,y) de deux points
                }
                else      //Si le nombre n'est pas décimal
                    g2d.drawLine((int) (x0 + (d0 + 380 - (lengthMaxInt-1)*77)*coef),(int) (220*coef),(int) (x0 + (d0 + 500)*coef), (int) (220*coef)); // Trace le trait en fonction des (x,y) de deux points
            }


           // Tracée de la ligne de "+"
           if (cptNbLigne ==2 &&  lengthNbMax[1][0] + lengthNbMax[1][1] >=2)    // On affiche l'addition intermédiaire que s'il y a au moins deux lignes
           {    lengthMaxInt = nbField;
                g2d.drawLine((int) (x0 + (d1 + 745 - lengthMaxInt*77)*coef),(int)((265 + 75*nbLigne)*coef),(int) (x0 + (d1 +  735)*coef), (int) ((265 + 75*nbLigne)*coef));
           }



            // Tracée du symbole "x"
            if (cptNbLigne ==2 || lengthIntMax()>4)   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                lengthMaxInt = lengthIntMax();
            else
                lengthMaxInt = 4;   // Par défaut, on se place à la distance correspondant à un champ de texte non mis en forme

            g2d.drawLine((int) (x0 + (d0 + 380 - (lengthMaxInt-1)*77)*coef),(int) (170*coef),(int) (x0 + (d0 + 410 - (lengthMaxInt-1)*77)*coef),(int) (200*coef));  // Trait vertical
            g2d.drawLine((int) (x0 + (d0 + 380 - (lengthMaxInt-1)*77)*coef),(int) (200*coef),(int) (x0 + (d0 + 410 - (lengthMaxInt-1)*77)*coef),(int) (170*coef));   //Trait horizontal





            // Tracée du symbole "+"
           if (cptNbLigne ==2 && lengthNbMax[1][0] + lengthNbMax[1][1] >=2)    // On affiche l'addition intermédiaire que s'il y a au moins deux lignes
           {    lengthMaxInt = (lengthNbMax[0][0] + lengthNbMax[0][1])+ (lengthNbMax[1][0] + lengthNbMax[1][1]);

                for (i=0; i<=nbLigne-1; i++)
                {   g2d.drawLine((int) (x0 + (d1 + 565 - (lengthMaxInt-2)*77)*coef),(int) ((290 + 75*i)*coef),(int) (x0 + (d1 + 565 - (lengthMaxInt-2)*77)*coef),(int) ((320 + 75*i)*coef));  // Trait vertical
                    g2d.drawLine((int) (x0 + (d1 + 550 - (lengthMaxInt-2)*77)*coef),(int) ((305 + 75*i)*coef),(int) (x0 + (d1 + 580 - (lengthMaxInt-2)*77)*coef),(int) ((305 + 75*i)*coef));   //Trait horizontal
                }
            }
            g2d.setStroke(new BasicStroke(1));
     }


    /**
    * Méthode qui met en forme les cases de résultats, les retenues et les cases d'addition intermédiaires
    * en fonction du nombre de chiffres dans la multuplication
    */
    public void afficherResultat(){
            int lengthMax =0;   // Renvoie la longueur maximale d'un nombre mis en forme
            int i=0;
            int j=0;

            // On réinitialise les cases de résultats ou de retenus
            for (j=0; j<=maxY;j++)
                this.remove(resultat[j]);
            for (j=0; j<=maxY-2;j++)
                 this.remove(retenu[j]);

            this.remove(raz);

            for(i=0;i<=3;i++)
                for(j=maxY;j>=0;j--)
                    this.remove(fieldChiffre[i][j]);

             for (j=0;j<=maxY-1; j++)
             {   this.remove(virguleResultat[j]);
                 this.remove(retenuAdd[j]);
             }

            if(positionVirguleResultat != -1)
            {   virguleResultat[positionVirguleResultat].remove(labVirguleResultat);
                virguleResultat[positionVirguleResultat].repaint();
                positionVirguleResultat = -1;
            }

            // On bloque les cases d'additions intermédiaires à part la dernière de la première ligne
            for (i=0; i<=nbLigne-1;i++)
                for (j=0; j<=maxY;j++)
                {   if (i!=0 || j!= maxY)
                    {   fieldChiffre[i][j].setEnabled(false);
                        fieldChiffre[i][j].setOpaque(false);
                        fieldChiffre[i][j].setFocusable(false);
                    }
                    if (i==nbLigne-1 && j!= maxY)
                    {   retenuAdd[j].setEnabled(false);
                        retenuAdd[j].setOpaque(false);
                        retenuAdd[j].setFocusable(false);
                    }
                 }

            if (etat[0] == true && etat[1] == true)
            {   // On réaffiche les cases de résultat et la virgule
                lengthMax = lengthIntMax();

                for(j=maxY;j>= maxY-nbField+1;j--)
                    this.add(resultat[j]);  // On affiche les cases de résultat


                 // Affichage du tableau de virgule au résultat
                lengthMax = nbField;
                if (lengthDecimalMax() !=0)
                {    if (lengthNbMax[1][0] == 1 && lengthNbMax[1][1] ==0)   // Cas où il n'y a aucun ligne d'addition (le 2nd nombre n'a pas plus d'un chiffre
                    {   for (j=7;j>=maxY-lengthMax+1; j--)
                            this.add(virguleResultat[j]);
                    }
                     else
                     {    for (j=7;j>=maxY-lengthMax+1; j--)    // Cas où il y a plusieurs lignes d'addition (le 2nd nombre a plus d'un chiffre)
                             this.add(virguleResultat[j]);
                     }
                }


                // Affichage des cases de retenu de multiplication
                lengthMax = lengthNbMax[0][0];
                if (lengthNbMax[0][1]!= 0)      // Le nombre est décimal
                {   for (j=0;j<=lengthMax-1; j++)   // On affiche les cases de retenu sur tous les entiers
                        add(retenu[maxY-4-j]);

                    lengthMax = lengthNbMax[0][1];
                    for (j=1;j<=lengthMax-1; j++)  // On affiche les cases de retenu sur tous les décimal sauf le dernier
                        add(retenu[maxY-4+j]);
                    this.add(raz);
                }
                else        // Le nombre est seulement un entier
                {   if (lengthMax != 0 && lengthMax != 1)
                    {   for (j=1;j<=lengthMax-1; j++)   // On affiche les cases de retenu sur tous les chiffres, sauf celui des unités
                            add(retenu[maxY-4-j]);
                        this.add(raz);
                    }
                }


                // Affichage des Champs de texte
                if (lengthNbMax[1][0] + lengthNbMax[1][1] >=2)  // On affiche l'addition intermédiaire que s'il y a au moins deux lignes
                {   for(i=0;i<=nbLigne-1;i++)
                        for(j=maxY;j>= maxY-nbField+1;j--)
                            this.add(fieldChiffre[i][j]);
                }


                // Affichage des cases de retenu d'addition
                if (lengthNbMax[1][0] + lengthNbMax[1][1] >= 2)
                {   lengthMax = nbField;
                    if (lengthMax != 0 && lengthMax != 1)
                        for (j=7;j>=maxY-lengthMax+1; j--)   // On affiche les cases de retenu sur tous les chiffres, sauf celui des unités
                            this.add(retenuAdd[j]);
                }

                positionComponent();    // Positionnement des cases de résultats, des virgules de résultats
                fieldChiffre[0][maxY].requestFocus();
            }
  } 


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
     * Vérifie que le 1er champ saisi est bien au format 12345,678 ou 12345
     * @return true si le 1er champ est au format correct; false sinon
     */

    public boolean testFormat0(){
            String str = nb[0].getText();
            int cptVirgule = 0;
            int cptNb = 0;
            int i=0;
            for (i = 0; i<= str.length()-1;i++)
            {   int codeAscii = (int) str.charAt(i);
                if (codeAscii == 44)     // Le caractère pointé est une virgule
                {   cptVirgule = 1;
                    if (i>5)
                    {   JOptionPane.showMessageDialog(this,  "Il y a trop de chiffres avant la virgule !",  "Attention", JOptionPane.INFORMATION_MESSAGE);
                        return false;
                    }
                    if (str.length()-1-i>3)
                    {   JOptionPane.showMessageDialog(this,  "Il y a trop de chiffres après la virgule !",  "Attention", JOptionPane.INFORMATION_MESSAGE);
                        return false;
                    }
                }
            }
            if (cptVirgule == 0)
                cptNb = str.length();
            else
                cptNb = str.length()-1;

            if (cptVirgule == 0 && str.length() > 5)
            {   JOptionPane.showMessageDialog(this,  "L'entier doit être limité à 5 chiffres !",  "Attention", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }

            if (etat[1] == true && (cptNb + lengthNbMax[1][0] + lengthNbMax[1][1])>9)
            {   JOptionPane.showMessageDialog(this,  "L'opération ne peut pas comporter plus de 9 chiffres au total",  "Attention", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }

            return true;
    }

        /**
     * Vérifie que le 2nd champ saisi est bien au format 1234 ou 123,4 ou 12,34 ou 1,234
     * @return true si le 2nd champ est au format correct; false sinon
     */

    public boolean testFormat1(){
            String str = nb[1].getText();
            int cptVirgule = 0;
            int cptNb = 0;
            int i=0;
            for (i = 0; i<= str.length()-1;i++)
            {   int codeAscii = (int) str.charAt(i);
                if (codeAscii == 44)     // Le caractère pointé est une virgule
                {   cptVirgule = 1;
                    if (i>4)
                    {   JOptionPane.showMessageDialog(this,  "Il y a trop de chiffres avant la virgule !",  "Attention", JOptionPane.INFORMATION_MESSAGE);
                        return false;
                    }
                }
            }
             if (cptVirgule == 0)
                cptNb = str.length();
            else
                cptNb = str.length()-1;

            if (cptVirgule == 0 && str.length() > 4)
            {   JOptionPane.showMessageDialog(this,  "L'entier doit être limité à 4 chiffres !",  "Attention", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }

            if (etat[0] == true && (lengthNbMax[0][0] + lengthNbMax[0][1] + cptNb)>9)
            {   JOptionPane.showMessageDialog(this,  "L'opération ne peut pas comporter plus de 9 chiffres au total",  "Attention", JOptionPane.INFORMATION_MESSAGE);
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
     * Méthode qui positionne l'ensemble des composants du JPanel Mutliplication.
     * Ce positionnement s'effectue en fonction d'un coefficient "coef", qui est calculé ici,
     * et qui s'adapte en fonction de la taille de la fenêtre.
     */

    @Override
       protected void positionComponent(){

            int i = 0;
            float l = super.getSize().width;
            float h = super.getSize().height;

            coef = 1;
            if (super.getSize().width >=784)
                l = 784;
            if ((super.getSize().height) >=657)
                h = 657;

            if (l<784 || h<657)     // On définit le coef de proportionnalité
            {   l =  l/784;
                h = h/657;
                if (l < h)
                {   coef = l;
                    x0 = (int)((super.getSize().width - 784*coef)/2);
                }
                else
                {   coef = h;
                    x0 = (int)((super.getSize().width - 784*coef)/2);
                }
            }
            else
                x0 = (int)((super.getSize().width - l)/2);

            police = new Font("Arial", Font.BOLD, (int) (50*coef));


            int x = 0;
            int y = 0;
            for (x=0; x<=1; x++)
            {   for (y=0; y<=maxY-1; y++)
                    panChiffre[x][y].setBounds((int) (x0 + (d0 + 135 + 77*y)*coef),(int) ((70 + 80*x)*coef),(int) (60*coef),(int) (60*coef));
            }


            for (x=0; x<=1; x++)
            {   for (y=0; y<=maxY-1; y++)
                {   chiffre[x][y].setFont(police);
                    chiffre[x][y].setBounds((int) (15*coef),(int) (2*coef),(int) (60*coef),(int) (60*coef));
                }
            }

            for (i=0; i<= 1; i++)
            {   modifierLigne[i].setBounds((int) (x0 + 745*coef),(int) ((80 + 80*i)*coef), (int) (37*coef), (int) (45*coef));
                if(coef>0) {modifierLigne[i].setSized((int) (0*coef), (int) (3*coef),(int) (40*coef),(int) (40*coef));}
                nb[i].setFont(police);
                nb[i].setBounds((int) (x0 + 250*coef),(int) ((70 + 80*i)*coef), (int) (lChamp*coef), (int) (hChamp*coef));
                virgule[i].setFont(police);
                virgule[i].setBounds((int) (x0 + (d0 + 505)*coef),(int) ((80+80*i)*coef), (int) (50*coef), (int) (50*coef));
            }
                
            if (lengthNbMax[1][0] + lengthNbMax[1][1] >= 2)
                for (i=0; i<= maxY; i++) // On déplace le résultat à la bonne ligne
                {   resultat[i].setFont(police);
                    resultat[i].setBounds((int) (x0 + (d1 + 58+77*i)*coef),(int) ((280 + 75*nbLigne)*coef), (int) (60*coef), (int) (60*coef));
                }
            else
                for (i=0; i<= maxY; i++)
                {   resultat[i].setFont(police);
                    resultat[i].setBounds((int) (x0 + (d1 + 58+77*i)*coef),(int) (260*coef), (int) (60*coef), (int) (60*coef));
                }

            if (lengthNbMax[1][0] == 1 && lengthNbMax[1][1] ==0)   // Cas où il n'y a aucune ligne d'addition (le 2nd nombre n'a pas plus d'un chiffre
            {   for (i=7;i>=maxY-nbField+1; i--)
                {  virguleResultat[i].setBounds((int) (x0 + (d1 + 118+77*i)*coef),(int) ((215 + 75*(nbLigne))*coef), (int) (15*coef), (int) (30*coef)); // On positionne les cases de virgule pour le résultat à la bonne ligne
                }
            }
            else
            {   for (i=7;i>=maxY-nbField+1; i--)    // Cas où il y a plusieurs lignes d'addition (le 2nd nombre a plus d'un chiffre)
                {   virguleResultat[i].setBounds((int) (x0 + (d1 + 118+77*i)*coef),(int) ((320 + 75*(nbLigne))*coef), (int) (15*coef), (int) (30*coef));  // On positionne les cases de virgule pour le résultat à la bonne ligne
                }
            }
             
            // Positionnement des virgules réelles
            labVirguleResultat.setFont(new Font("Arial", Font.PLAIN, (int) (60 * coef)));
            labVirguleResultat.setBounds((int) (0 * coef), (int) (-51 * coef), (int) (20 * coef), (int) (90 * coef));

            // Positionnement des virgules virtuelles
            VirguleOperation.virgule.setFont(new Font("Arial", Font.PLAIN, (int) (60 * coef)));
            VirguleOperation.virgule.setBounds((int) (0 * coef), (int) (-51 * coef), (int) (20 * coef), (int) (90 * coef));

            Font policeRetenu = new Font("Arial", Font.BOLD, (int) (40*coef));
            for (i=0; i<= maxY-2; i++)
            {   retenu[i].setFont(policeRetenu);
                retenu[i].setBounds((int) (x0 + (d0 + 145+77*i)*coef),(int) (10*coef), (int) (50*coef), (int) (50*coef));
            }

            for (x=0; x<=3; x++)
            {   for (y=0; y<=maxY; y++)
                {   fieldChiffre[x][y].setFont(police);
                    fieldChiffre[x][y].setBounds((int) (x0 + (d1 + 58 + 77*y)*coef),(int) ((275 + 75*x)*coef),(int) (60*coef),(int) (60*coef));
                }
            }

            Font policeRetenuAdd = new Font("Arial", Font.BOLD, ((int) (30*coef))); //Création d'un police particulière pour les retenus
            for (i=0; i<= maxY-1; i++)
            {   retenuAdd[i].setFont(policeRetenuAdd);
                retenuAdd[i].setBounds((int) (x0 + (d1 + 78+77*i)*coef),(int) (230*coef), (int) (40*coef), (int) (40*coef));
            }

            //razLabel.setBounds((int) (12*coef),(int) (-12*coef), (int) (60*coef), (int) (60*coef));
            //razLabel.setFont(new Font("Arial", Font.PLAIN, (int) (20*coef)));
            raz.setBounds((int) (x0 + 745*coef),(int) (25*coef), (int) (40*coef), (int) (40*coef));
            if(coef>0) {raz.setSized((int) (0*coef), (int) (3*coef),(int) (40*coef),(int) (40*coef));}
            this.repaint();
       }

     /**
     * Méthode qui copie l'ensemble des éléments du JPanel Multiplication
     * afin de reconstruire un JPanel Impression mis en forme.
     * @return Un JPanel, copie du JPanel Multiplication principal, et mis en forme.
     */

    @Override
    public JPanel impression(){

        Impression panImpression = new Impression();

        if (cptNbLigne == 2)
        {
            panImpression.setLayout(null);
            panImpression.setBounds(0,0,Multiplication.super.getSize().width, Multiplication.super.getSize().height);
            panImpression.setBackground(ColorManager.transparent());

            // Copie des chiffres d'opération
            JLabel copyChiffre[][] = {null};
            copyChiffre = Copie.copie(chiffre, 2, maxY);
            for (int x=0; x<=1; x++)
            {   for (int y=0; y<=maxY-1; y++)
                {   panImpression.add(copyChiffre[x][y]);
                    copyChiffre[x][y].setBounds((int) (x0 + (d0 + 135 + 77*y)*coef),(int) ((70 + 80*x)*coef),(int) (60*coef),(int) (60*coef));
                }
            }

            // Copie de la virgule de résultat
            JLabel copyVirguleResultat = Copie.copie(labVirguleResultat);
            copyVirguleResultat.setText(",");
            if (lengthDecimalMax() !=0)
            {
               if (lengthNbMax[1][0] == 1 && lengthNbMax[1][1] ==0)
                {      panImpression.add(copyVirguleResultat);
                       copyVirguleResultat.setBounds((int) (x0 + (d1 + 108+77*positionVirguleResultat)*coef),(int) ((160 + 75*(nbLigne))*coef), (int) (50*coef), (int) (90*coef)); // On positionne les cases de virgule pour le résultat à la bonne ligne
               }
                else
                {   // Cas où il y a plusieurs lignes d'addition (le 2nd nombre a plus d'un chiffre)
                    panImpression.add(copyVirguleResultat);
                    copyVirguleResultat.setBounds((int) (x0 + (d1 + 108+77*positionVirguleResultat)*coef),(int) ((255 + 75*(nbLigne))*coef), (int) (50*coef), (int) (90*coef));  // On positionne les cases de virgule pour le résultat à la bonne ligne
                    //copyVirguleResultat.setLocation((int) (x0 + (d1 + 108+77*positionVirguleResultat)*coef),(int) ((185 + 75*(-1))*coef));
                }
             }
               
            // Copie des virgules de ligne d'opération
            JLabel copyVirgule[] = {null};
            copyVirgule = Copie.copie(virgule, 2);
            for (int i=0; i<= 1; i++)
            {   if(lengthNbMax[i][1] !=0)
                    panImpression.add(copyVirgule[i]);
                copyVirgule[i].setBounds((int) (x0 + (d0 + 495)*coef),(int) ((80+80*i)*coef), (int) (50*coef), (int) (50*coef));
            }

            // Copie des Label du résultat
            JLabel copyResultat[] = {null};
            copyResultat = Copie.copie(resultat, maxY+1);
            if (lengthNbMax[1][0] + lengthNbMax[1][1] >= 2)
            {   for (int i=0; i<= maxY; i++)
                {   panImpression.add(copyResultat[i]);
                    copyResultat[i].setBounds((int) (x0 + (d1 + 58+77*i)*coef),(int) ((280 + 75*nbLigne)*coef), (int) (60*coef), (int) (60*coef));
                }
            }
            else
            {   for (int i=0; i<= maxY; i++)
                {   panImpression.add(copyResultat[i]);
                    copyResultat[i].setBounds((int) (x0 + (d1 + 58+77*i)*coef),(int) (260*coef), (int) (60*coef), (int) (60*coef));
                }
            }
                         
            // Copie des Label de retenue
            JLabel copyRetenu[] = {null};
            copyRetenu = Copie.copie(retenu, maxY-1);
            for (int i=0; i<= maxY-2; i++)
            {   panImpression.add(copyRetenu[i]);
                copyRetenu[i].setBounds((int) (x0 + (d0 + 145+77*i)*coef),(int) (10*coef), (int) (50*coef), (int) (50*coef));
            }
            
             // Copie des Label de champs de chiffre d'addition
            JLabel copyFieldChiffre[][] = {null};
            copyFieldChiffre = Copie.copie(fieldChiffre, 4, maxY+1);
            for (int x=0; x<=3; x++)
            {   for (int y=0; y<=maxY; y++)
                {   panImpression.add(copyFieldChiffre[x][y]);
                    copyFieldChiffre[x][y].setBounds((int) (x0 + (d1 + 58 + 77*y)*coef),(int) ((275 + 75*x)*coef),(int) (60*coef),(int) (60*coef));
                }
            }

            // Copie des Label de retenu d'addition
            JLabel copyRetenuAdd[] = {null};
            copyRetenuAdd = Copie.copie(retenuAdd, maxY);
            for (int i=0; i<= maxY-1; i++)
            {   panImpression.add(copyRetenuAdd[i]);
                copyRetenuAdd[i].setBounds((int) (x0 + (d1 + 78+77*i)*coef),(int) (230*coef), (int) (40*coef), (int) (40*coef));
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
            int i=2;
            int lengthMaxInt = 0;    // Longueur maximale des parties entières
            int lengthMaxDec = 0;    // Longueur maximale des parties décimale

            Graphics2D g2d = (Graphics2D)g; // Crée un Graphique de type 2d
            super.paintComponent(g); // Redessine le Panel avant d'ajouter les composants 
            Stroke epaisseur = new BasicStroke((int) (6*coef));    // Crée une variable contenant l'épaisseur du trait (ici : 6)
            g2d.setStroke(epaisseur);    // Donne au trait son épaisseur

            // Tracée de la ligne de "x"
            if (cptNbLigne !=2 || lengthNbMax[1][0] + lengthNbMax[1][1] <2)
            {   if (lengthIntMax()>4 || (cptNbLigne == 2 &&lengthNbMax[1][0] + lengthNbMax[1][1] <2))   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                    lengthMaxInt = lengthIntMax();
                else
                    lengthMaxInt = 4;

                if (lengthDecimalMax() != 0)     //Si le nombre est décimal
                {   lengthMaxDec = lengthDecimalMax();
                    g2d.drawLine((int) (x0 + (d0 + 380 - (lengthMaxInt-1)*77)*coef), (int) (240*coef),(int) (x0 + (d0 + 505 + lengthMaxDec*77)*coef), (int) (240*coef)); // Trace le trait en fonction des (x,y) de deux points
                }
                else      //Si le nombre n'est pas décimal
                    g2d.drawLine((int) (x0 + (d0 + 380 - (lengthMaxInt-1)*77)*coef), (int) (240*coef),(int) (x0 + (d0 + 500)*coef), (int) (240*coef)); // Trace le trait en fonction des (x,y) de deux points
            }
            else
            {   lengthMaxInt = lengthIntMax();
                if (lengthDecimalMax() != 0)     //Si le nombre est décimal
                {    lengthMaxDec = lengthDecimalMax();
                    g2d.drawLine((int) (x0 + (d0 + 380 - (lengthMaxInt-1)*77)*coef), (int) (220*coef),(int) (x0 + (d0 + 505 + lengthMaxDec*77)*coef), (int) (220*coef)); // Trace le trait en fonction des (x,y) de deux points
                }
                else      //Si le nombre n'est pas décimal
                    g2d.drawLine((int) (x0 + (d0 + 380 - (lengthMaxInt-1)*77)*coef),(int) (220*coef),(int) (x0 + (d0 + 500)*coef), (int) (220*coef)); // Trace le trait en fonction des (x,y) de deux points
            }


           // Tracée de la ligne de "+"
           if (cptNbLigne ==2 &&  lengthNbMax[1][0] + lengthNbMax[1][1] >=2)    // On affiche l'addition intermédiaire que s'il y a au moins deux lignes
           {    lengthMaxInt = nbField;
                g2d.drawLine((int) (x0 + (d1 + 745 - lengthMaxInt*77)*coef),(int)((265 + 75*nbLigne)*coef),(int) (x0 + (d1 +  735)*coef), (int) ((265 + 75*nbLigne)*coef));
           }

            // Tracée du symbole "x"
            if (cptNbLigne ==2 || lengthIntMax()>4)   // Si toutes les lignes sont mises en forme ou qu'une ligne est longue
                lengthMaxInt = lengthIntMax();
            else
                lengthMaxInt = 4;   // Par défaut, on se place à la distance correspondant à un champ de texte non mis en forme

            g2d.drawLine((int) (x0 + (d0 + 380 - (lengthMaxInt-1)*77)*coef),(int) (170*coef),(int) (x0 + (d0 + 410 - (lengthMaxInt-1)*77)*coef),(int) (200*coef));  // Trait vertical
            g2d.drawLine((int) (x0 + (d0 + 380 - (lengthMaxInt-1)*77)*coef),(int) (200*coef),(int) (x0 + (d0 + 410 - (lengthMaxInt-1)*77)*coef),(int) (170*coef));   //Trait horizontal

            // Tracée du symbole "+"
           if (cptNbLigne ==2 && lengthNbMax[1][0] + lengthNbMax[1][1] >=2)    // On affiche l'addition intermédiaire que s'il y a au moins deux lignes
           {    lengthMaxInt = (lengthNbMax[0][0] + lengthNbMax[0][1])+ (lengthNbMax[1][0] + lengthNbMax[1][1]);

                for (i=0; i<=nbLigne-1; i++)
                {   g2d.drawLine((int) (x0 + (d1 + 565 - (lengthMaxInt-2)*77)*coef),(int) ((290 + 75*i)*coef),(int) (x0 + (d1 + 565 - (lengthMaxInt-2)*77)*coef),(int) ((320 + 75*i)*coef));  // Trait vertical
                    g2d.drawLine((int) (x0 + (d1 + 550 - (lengthMaxInt-2)*77)*coef),(int) ((305 + 75*i)*coef),(int) (x0 + (d1 + 580 - (lengthMaxInt-2)*77)*coef),(int) ((305 + 75*i)*coef));   //Trait horizontal
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
    public int tailleX0(){
        if (lengthNbMax[1][0] + lengthNbMax[1][1] >=2) // Il faut copier les additions intermédiaires
                return (int) (x0 + (d1 + 540 - (lengthNbMax[0][0] + lengthNbMax[0][1]+ lengthNbMax[1][0] + lengthNbMax[1][1]-2)*77)*coef);
            else    // Il n'y a pas d'addition intermédiaire
                return (int) (x0 + (d0 + 360 - (lengthIntMax()-1)*77)*coef);
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
        return (int) (x0 + (d1 +  745)*coef);
    }

     /**
     * Méthode qui permet de déterminer les dimensions du JPanel à copier dans la partie texte
     * @return La coordonnée en y de la fin de l'opération
     */

    @Override
    public int tailleY1(){
        if (lengthNbMax[1][0] + lengthNbMax[1][1] >= 2)
               return (int) ((350 + 75*nbLigne)*coef);
            else
               return (int) (330*coef);
    }
    
} 