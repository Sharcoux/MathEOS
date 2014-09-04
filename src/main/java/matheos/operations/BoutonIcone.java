/*
 * «Copyright 2011 Guillaume Varoquaux»
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
package matheos.operations;

import matheos.utils.managers.ColorManager;
import matheos.utils.objets.Icone;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Classe qui crée l'apparence des boutons de modification de ligne d'opérations.
 * @author Guillaume
 */
public class BoutonIcone extends JPanel implements MouseListener, Serializable{

    JLabel image = new JLabel();    // Variable qui contient l'icone actuelle du bouton
    public Icone iconeDown = null;  // Icone si le bouton est appuyé
    public Icone iconeUp = null;  // Icone si le bouton est relaché
    public String idIcone = "Up";   // Variable qui définit l'icone présente sur le bouton. Prend deux valeurs :  "Up" ou "Down".

    public BoutonIcone(String down, String up){
        
        this.setBackground(ColorManager.get("color button background"));
        image = new JLabel(new Icone(up));
        this.add(image);
        this.setLayout(null);
        iconeDown = new Icone(down);
        iconeUp = new Icone(up);
        this.addMouseListener(this);
        this.revalidate();
        this.repaint();

    }

    /**
     * Méthode qui change l'affichage de l'icone du bouton. <br/>
     * Si celle-ci était à "Up", elle passe à "Down" et inversement.<br/>
     * Elle est appelée lors du clic sur le bouton.
     */
    public void setIcon(){
        this.remove(image);
        if (idIcone.equals("Up"))
        {   idIcone = "Down";
            image = new JLabel(iconeDown);
        }
        else
        {   idIcone = "Up";
            image = new JLabel(iconeUp);
        }
        this.add(image);
        image.setBounds(0, 0, this.getWidth(), this.getHeight());
    }

    /**
     * Méthode qui récupère l'icone du bouton, "Up" ou "Down"
     * @return L'icone actuelle du bouton.
     */
    public Icone getIcon(){
      Icone icone = null;
      if (idIcone.equals("Down"))
          icone = iconeDown;
      else
          icone = iconeUp;
        return icone;
    }

    /**
    * Méthode qui positionne et dimensionne les icones sur leur Panel.
    * @param x la position en x du bouton.
    * @param y la position en y du bouton.
    * @param l la largeur du bouton.
    * @param h la hauteur du bouton.
    */

    public void setSized(int x, int y, int l, int h){
        this.remove(image);
        iconeDown = new Icone(iconeDown.getDescription(),l, h);
        iconeUp = new Icone(iconeUp.getDescription(), l, h);

        if (idIcone.equals("Down"))
            image = new JLabel(iconeDown);
        else
            image = new JLabel(iconeUp);
        image.setBounds(x, y, l, h);
        this.add(image);

    }

    /**
    * Méthode qui gère le changement d'icone au clic sur le bouton.
    * mousePressed : on passe à "Down"
    *  mouseReleased : on passe à "Up"
    * @param e : MouseEvent
    */

    public void mouseClicked(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        this.setIcon();
    }

    public void mouseReleased(MouseEvent e) {
        this.setIcon();
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

   
    
}
