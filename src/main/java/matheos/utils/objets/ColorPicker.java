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

package matheos.utils.objets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import matheos.IHM;
import matheos.utils.boutons.InfoBouton;
import matheos.utils.librairies.DimensionTools;
import matheos.utils.managers.ColorManager;

/**
 *
 * @author François Billioud
 */
public class ColorPicker extends JComboBox<Color> {

    private final Color[] colors;
    
    public ColorPicker(String balise) {
        setFocusable(false);
        setToolTipText(IHM.getThemeElement(balise+InfoBouton.DESCRIPTION));
        setActionCommand(balise);
        //récupère les couleurs de la liste depuis le thème
        colors = ColorManager.getListCouleurs(balise);
        setModel(new DefaultComboBoxModel<>(colors));
        setRenderer(new ColorRenderer());
        setSelectedIndex(0);
    }
    
    private class ColorRenderer implements ListCellRenderer<Color> {
        @Override
        public Component getListCellRendererComponent(JList list, final Color value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            JPanel panel = new JPanel(new BorderLayout()){
                @Override
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Rectangle r = this.getBounds();
                    Color previous = g.getColor();
                    if(isSelected) {
                        g.setColor(Color.DARK_GRAY);
                        g.fillRect(0, 0, r.width, r.height);
                    }
//                    g.setColor(isSelected ? ColorManager.enlight(value) : value);
                    g.setColor(value);
                    Rectangle painted = new Rectangle(r.width/10+3, r.height/20+3, r.width-r.width/5-6, r.height-r.height/10-6);
                    g.fillRect(painted.x, painted.y, painted.width, painted.height);
                    g.setColor(ColorManager.getColorFromHexa("#90aad3"));
                    g.drawRect(painted.x, painted.y, painted.width, painted.height);
                    g.drawRect(painted.x-2, painted.y-2, painted.width+4, painted.height+4);
                    g.setColor(ColorManager.getColorFromHexa("#b0d5fd"));
                    g.drawRect(painted.x-1, painted.y-1, painted.width+2, painted.height+2);
//                    g.fillRect(0, 0, r.width, r.height);
                    g.setColor(previous);
                }
            };
            panel.setOpaque(true);
//            panel.setBackground(value);
            panel.setPreferredSize(new Dimension(40,30));
//            panel.setBorder(BorderFactory.createBevelBorder(isSelected ? BevelBorder.LOWERED : BevelBorder.RAISED));
//            panel.setSize(new Dimension(40,20));
            return panel;
        }
    }
    
    public Color[] getAvailableColors() {return colors;}
    
    @Override
    public Color getSelectedItem() {
        return (Color)super.getSelectedItem();
    }
    
}
