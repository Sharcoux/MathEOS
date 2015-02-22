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

import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Classe abstraite qui copie des tableaux et des listes dans une nouvelle variable.
 * Copie l'ensemble des paramètres : taille, contenu, font, ...
 * @author Guillaume
 */
public abstract class Copie {

    /**
     * Méthode de copie d'un JLabel.
     * @param lab le JLabel a copier.
     * @return Un JLabel, copie de celui placé en paramètre.
     */

    public static JLabel copie(JLabel lab){
            JLabel copyLab  = new JLabel();
            copyLab.setText(lab.getText());
            copyLab.setFont(lab.getFont());
            return copyLab;
    }

    /**
     * Méthode de copie d'un tableau de JLabel.
     * @param lab le tableau de JLabel a copier.
     * @param maxX la longueur du tableau.
     * @return Un tableau de JLabel, copie de celui placé en paramètre.
     */

    public static JLabel[] copie(JLabel lab[], int maxX){
            JLabel copyLab[]  = {null};
            copyLab = new JLabel[lab.length];

            for (int x=0; x<=lab.length-1; x++)
            {   copyLab[x] = new JLabel();
                copyLab[x].setText(lab[x].getText());
                copyLab[x].setFont(lab[x].getFont());
                copyLab[x].setForeground(lab[x].getForeground());
            }
            return copyLab;
    }

    /**
     * Méthode de copie d'un tableau de tableaux de JLabel.
     * @param lab le tableau de tableaux de JLabel a copier.
     * @param maxX la longueur du 1er tableau.
     * @param maxY la longueur des 2nds tableaux.
     * @return Un tableau de tableaux de JLabel, copie de celui placé en paramètre.
     */

    public static JLabel[][] copie(JLabel lab[][], int maxX, int maxY){
            JLabel copyLab[][]  = {null};
            copyLab = new JLabel[maxX][maxY];

            for (int x=0; x<=maxX-1; x++)
            {   for (int y=0; y<=maxY-1; y++)
                {   copyLab[x][y] = new JLabel();
                    copyLab[x][y].setText(lab[x][y].getText());
                    copyLab[x][y].setFont(lab[x][y].getFont());
                    copyLab[x][y].setForeground(lab[x][y].getForeground());
                }
            }
            return copyLab;
    }

    /**
     * Méthode qui copie le contenu d'un tableau de JTextField.
     * @param txt la tableau de JTextField à copier.
     * @param maxX la longueur du tableau de JTextField.
     * @return Un tableau de JLabel des contenus des JTextField.
     */

     public static JLabel[] copie(JTextField txt[], int maxX){
            JLabel copyTxt[];
            copyTxt = new JLabel[maxX];

            for (int x=0; x<=maxX-1; x++)
            {   copyTxt[x] = new JLabel();
                copyTxt[x].setText(txt[x].getText());
                copyTxt[x].setFont(txt[x].getFont());
                copyTxt[x].setForeground(txt[x].getForeground());
            }
            return copyTxt;
    }

    /**
    * Méthode qui copie le contenu d'un tableau de tableaux de JTextField.
    * @param txt le tableau de tableaux de JTextField à copier.
    * @param maxX la longueur du 1er tableau de JTextField.
    * @param maxY la longueur des 2nds tableaux de JTextField.
    * @return Un tableau de tableaux de JLabel des contenus des JTextField.
    */

     public static JLabel[][] copie(JTextField txt[][], int maxX, int maxY){
            JLabel copyTxt[][]  = {null};
            copyTxt = new JLabel[maxX][maxY];

            for (int x=0; x<=maxX-1; x++)
            {   for (int y=0; y<=maxY-1; y++)
                {   copyTxt[x][y] = new JLabel();
                    copyTxt[x][y].setText(txt[x][y].getText());
                    copyTxt[x][y].setFont(txt[x][y].getFont());
                    copyTxt[x][y].setForeground(txt[x][y].getForeground());
                }
            }
            return copyTxt;
    }

    /**
     * Méthode qui copie une liste de listes de JLabel.
     * @param lab La liste de listes à copier.
     * @return Une liste de listes de JLabel, copie de celle placée en paramètre.
     */

    public static List<List<JLabel>> copieDoubleListLabel(List<List<JLabel>> lab){
            List<List<JLabel>> copyLab  = new ArrayList<List<JLabel>>();
            copyLab.add(new ArrayList<JLabel>());
            copyLab.add(new ArrayList<JLabel>());

            for (int x=0; x<=lab.size()-1; x++)
            {   for (int y=0; y<=lab.get(x).size()-1; y++)
                {   copyLab.get(x).add (y, new JLabel());
                    copyLab.get(x).get(y).setText(lab.get(x).get(y).getText());
                    copyLab.get(x).get(y).setFont(lab.get(x).get(y).getFont());
                    copyLab.get(x).get(y).setForeground(lab.get(x).get(y).getForeground());
                }
            }
            return copyLab;
    }

    /**
     * Méthode qui copie une liste de LimitedTextField.
     * @param txt La liste de JTextField à copier.
     * @return Une liste de JLabel contenant les contenues des LimitedTextField en paramètre.
     */
    public static List<JLabel> copieListTextField(List<? extends JTextField> txt){
            List<JLabel> copyTxt  = new ArrayList<JLabel>();
            copyTxt.add(new JLabel());

            for (int x=0; x<=txt.size()-1; x++)
            {   copyTxt.add (x, new JLabel());
                copyTxt.get(x).setText(txt.get(x).getText());
                copyTxt.get(x).setFont(txt.get(x).getFont());
                copyTxt.get(x).setForeground(txt.get(x).getForeground());
            }
            return copyTxt;
    }

    /**
     * Méthode qui copie une liste de listes de LimitedTextField.
     * @param txt a liste de liste de LimitedTextField à copier.
     * @return Une liste de listes de JLabel contenant les contenues des LimitedTextField en paramètre.
     */
     public static List<List<JLabel>> copieDoubleListTextField(List<List<OperationType.LimitedTextFieldOperations>> txt){
            List<List<JLabel>> copyTxt  = new ArrayList<List<JLabel>>();

            for (int x=0; x<=txt.size()-1; x++)
            {   copyTxt.add(x, new ArrayList<JLabel>());
                for (int y=0; y<=txt.get(x).size()-1; y++)
                {   copyTxt.get(x).add (y, new JLabel());
                    copyTxt.get(x).get(y).setText(txt.get(x).get(y).getText());
                    copyTxt.get(x).get(y).setFont(txt.get(x).get(y).getFont());
                    copyTxt.get(x).get(y).setForeground(txt.get(x).get(y).getForeground());
                }
            }
            return copyTxt;
    }

}
