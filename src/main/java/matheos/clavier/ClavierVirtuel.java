/**
 * Copyright (C) 2015 François Billioud
 *
 * This file is part of MathEOS
 *
 * MathEOS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Additional Terms according section 7 of GNU/GPL V3 :
 *
 * These additional terms refer to the source code of bomehc.
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
 *
 **/

package matheos.clavier;

import java.awt.AWTException;
import java.awt.GridLayout;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.Bouton;

/**
 *
 * @author François Billioud
 */
public class ClavierVirtuel extends Clavier {
    
    public static final int NOMBRE_BOUTON = 33;
    public static final int NOMBRE_LIGNES = 3;
    public static final int NOMBRE_COLONNES = 11;
    
    private static final String BACKSPACE = "backspace key";
    private static final String SPACE = "space key";
    private static final String ENTER = "enter key";
    private static final String SHIFT = "shift key";
    
    private final String[] lettres = {"a","b","c","d","e","f","g","h","i","j",BACKSPACE,
        "k","l","m","n","o","p","q","r","s","t",ENTER,
        SHIFT,"u","v","w","x","y","z",".","?","!",SPACE};
    private final String[] lettres_shifted = {"A","B","C","D","E","F","G","H","I","J",BACKSPACE,
        "K","L","M","N","O","P","Q","R","S","T",ENTER,
        SHIFT,"U","V","W","X","Y","Z",",","'",":",SPACE};
    
    public ClavierVirtuel() {
        super();
        panelClavier = new PanelVirtuel();
        this.setSize(panelClavier.getWidth(), panelClavier.getHeight());
        this.add(panelClavier);
    }
    private class PanelVirtuel extends Clavier.PanelClavier {

        private PanelVirtuel() {
            GridLayout grille = new GridLayout(NOMBRE_LIGNES, NOMBRE_COLONNES, 5, 5);
            this.setLayout(grille);
            this.setSize(580, 200);
            int buttonWidth = (580-5*11)/10;

            bouton = new BoutonClavier[NOMBRE_BOUTON];

            for(int i = 0; i<lettres.length; i++) {
                String s = lettres[i];
                if(s.length()>1) {
                    switch(s) {
                        case SHIFT: bouton[i] = new BoutonClavier(new ActionShift()); break;
                        case BACKSPACE: bouton[i] = new BoutonClavier(new ActionBackspace()); break;
                        case ENTER: bouton[i] = new BoutonClavier(new ActionEnter()); break;
                        case SPACE: bouton[i] = new BoutonClavier(new ActionSpace()); break;
                    }
                    
                    bouton[i].setSizePolicy(Bouton.SIZE_BY_WIDTH);
                    bouton[i].setSizeByWidth(buttonWidth);
                } else {
                    bouton[i] = new BoutonClavier(new ActionBoutonTexte(s));
                }
                add(bouton[i]);
            }
        }
    }
    
    private class ActionSpace extends ActionComplete {
        private ActionSpace() {super(SPACE);}
        @Override
        public void actionPerformed(ActionEvent e) {
            new ActionBoutonTexte(" ").actionPerformed(e);
        }
    }
    private class ActionBackspace extends ActionComplete {
        private ActionBackspace() {super(BACKSPACE);}
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_BACK_SPACE);
                robot.keyRelease(KeyEvent.VK_BACK_SPACE);
            } catch (AWTException ex) {
                Logger.getLogger(ClavierVirtuel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private class ActionEnter extends ActionComplete {
        private ActionEnter() {super(ENTER);}
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);
            } catch (AWTException ex) {
                Logger.getLogger(ClavierVirtuel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private class ActionShift extends ActionComplete.Toggle {
        private ActionShift() {
            super(SHIFT,false);
        }
        @Override
        public void actionPerformed(ActionEvent e) {shift();}
    }
    
    private boolean shifted = false;
    private void shift() {
        shifted = !shifted;
        String[] T = shifted ? lettres_shifted : lettres;
        for(int i=0; i<T.length; i++) {
            String s = T[i];
            if(s.length()>1) {
                
            } else {
                bouton[i].getAction().putValue(Action.NAME, s);
            }
        }
        repaint();
    }
}
