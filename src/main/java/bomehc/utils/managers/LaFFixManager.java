/**
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of Bomehc
 *
 * Bomehc is free software: you can redistribute it and/or modify
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
 *
 **/

package bomehc.utils.managers;

import bomehc.IHM;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.AbstractRegionPainter;

/**
 *
 * @author François Billioud
 */
public abstract class LaFFixManager {

    private static class Painter extends javax.swing.plaf.nimbus.AbstractRegionPainter {
        private final Color couleur;

        private Painter(Color couleur) {
            this.couleur = couleur;
        }
        @Override
        protected AbstractRegionPainter.PaintContext getPaintContext() {
            return new AbstractRegionPainter.PaintContext(null, null, false);
        }

        @Override
        protected void doPaint(Graphics2D g, JComponent c, 
                int width, int height, Object[] extendedCacheKeys) {
            g.setColor(c.isEnabled() ? c.getBackground() : couleur);
            g.fillRect(0, 0, width, height);
        }
    }
    
    public static void fixBackground(BackgroundTrouble c, Color couleur, boolean enabled) {
//        couleur = new Color(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), 0);
        if(IHM.getThemeElement("laf").equals("nimbus")) {
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            Painter painter = new Painter(couleur);
            String key;
            if(c instanceof JTextPane) {
                key = "TextPane["+(enabled ? "Enabled" : "Disabled")+"].backgroundPainter";
                defaults.put(key, painter);
            } else if(c instanceof JTabbedPane) {
                key = "TabbedPane["+(enabled ? "Enabled" : "Disabled")+"].backgroundPainter";
                defaults.put(key, couleur);
//                defaults.put("TabbedPane.background", couleur);
//                c.setBackground(couleur);
            } else {return;}

            //HACK : l'inputMap est modifiée
//            InputMap parentInputMap = c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).getParent();
//            InputMap parentInputMap2 = c.getInputMap(JComponent.WHEN_FOCUSED).getParent();
            
            //Il faut d'abord remettre à zéro pour pouvoir changer de couleur plusieurs fois
//            UIDefaults defaultsInitial = new UIDefaults();
//            Object painterDefault = UIManager.get(key);
//            defaultsInitial.put(key, painterDefault);
//            c.putClientProperty("Nimbus.Overrides", defaultsInitial);
//            c.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
//            c.setBackground(null);
            
            c.putClientProperty("Nimbus.Overrides", defaults);
            c.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
            if(enabled) c.setBackground(couleur);
            
//            c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).setParent(parentInputMap);
//            c.getInputMap(JComponent.WHEN_FOCUSED).setParent(parentInputMap2);
        }
    }
    
    /** Interface servant à marquer les objets ayant des problèmes de background **/
    public interface BackgroundTrouble {
        public void setBackground(Color c);
        public void putClientProperty(Object key, Object value);
    }

    private LaFFixManager() {throw new AssertionError("trying to instanciate utilitary class");}

}
