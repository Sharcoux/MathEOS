/** «Copyright 2011 François Billioud»
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
package bomehc.utils.boutons;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class BoutonPanel extends PanelAction implements MouseListener {

    ImageIcon icone;
    ImageIcon iconePassage;
    ImageIcon iconeClic;
    String texte;

    JLabel iconeAffichee = null;
    JLabel texteAffiche = null;
    boolean rollover = false;
    boolean clic = false;
    boolean actionPressed = true;
    boolean actionReleased = false;

    public BoutonPanel() {
        this(null,null);
    }

    public BoutonPanel(String s) {
        this(s,null);
    }

    public BoutonPanel(ImageIcon i) {
        this(null,i);
    }
    
    public BoutonPanel(String texte, ImageIcon i) {
        super();
        init(texte,i);
    }

    final void init(String texte, ImageIcon i) {
        if(texte != null) { setText(texte);}
        if(i != null) { setIcon(i);}
        if(texte == null && i == null) {setText("");}
    }

    public void setText(String texte) {
        this.texte = texte;
        texteAffiche = new JLabel(texte);
        add(texteAffiche);
        revalidate();
        repaint();
    }

    public void setIcon(String s) {
        icone = new ImageIcon(s);
        setIcon(icone);
    }

    public final void setIcon(ImageIcon i) {
        icone = i;
        if(iconeAffichee!=null) {remove(iconeAffichee);}
        iconeAffichee = new JLabel(icone);
        add(iconeAffichee,BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void setRolloverIcon(String s) {
        iconePassage = new ImageIcon(s);
        rollover = true;
    }

    public void setRolloverIcon(ImageIcon i) {
        iconePassage = i;
        rollover = true;
    }

    public void setSelectedIcon(String s) {
        iconeClic = new ImageIcon(s);
        clic = true;
    }

    public void setSelectedIcon(ImageIcon i) {
        iconeClic = i;
        clic = true;
    }

    public void setRolloverEnabled(boolean b) {
        rollover = b;
    }

    public void setSelectionEnabled(boolean b) {
        clic = b;
    }

    public void setActionPressed(){
        actionPressed = true;
        actionReleased = false;
    }

    public void setActionReleased(){
        actionPressed = false;
        actionReleased = true;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(clic) {
            affiche(iconeClic);
        }
        if(actionPressed) {
            ActionEvent event = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"ActionPerformed");
            fireActionPerformed(event);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(clic) {
            affiche(icone);
        }
        if(actionReleased) {
            ActionEvent event = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"ActionPerformed");
            fireActionPerformed(event);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if(rollover) {
            affiche(iconePassage);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if(rollover) {
            affiche(icone);
        }
    }

    void affiche(ImageIcon i) {
        remove(iconeAffichee);
        iconeAffichee = new JLabel(i);
        add(iconeAffichee);
        revalidate();
        repaint();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        iconeAffichee.setEnabled(enabled);
        texteAffiche.setEnabled(enabled); 
    }
    
    

}
