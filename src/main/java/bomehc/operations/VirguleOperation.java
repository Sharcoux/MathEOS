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

import bomehc.utils.managers.ColorManager;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Classe qui définit les JPanel de Virgules pour les opérations.
 * Lorsque la souris passe dessus, on fait apparaitre une virgule.
 * Lorsque la souris ressort, on efface la virgule.
 * @author Guillaume
 */
public class VirguleOperation extends JPanel implements MouseListener, Serializable{

    public static JLabel virgule = new JLabel(","); // Le JLabel content la virgule si le JPanel est survolé par la souris

    public VirguleOperation(){

        this.addMouseListener(this);
        this.setBackground(ColorManager.get("color chiffre background"));
        this.setLayout(null);
        this.setBorder(BorderFactory.createLineBorder(Color.black));
        this.setFocusable(false);
        virgule.setFocusable(false);
        repaint();

    }


    public void mouseClicked(MouseEvent e) {}
  
    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    /**
     * Méthode qui affiche la virgule si la souris survole le JPanel.
     * @param e MouseEvent
     */

    public void mouseEntered(MouseEvent e) {
            this.add(virgule);
            repaint();
     }

    /**
     * Méthode qui efface la virgule si la souris sort du JPanel.
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {
            this.remove(virgule);
            repaint();
      }



}
