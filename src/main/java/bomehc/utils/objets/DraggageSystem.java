/** «Copyright 2013 François Billioud»
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

package bomehc.utils.objets;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Cette classe permet de mettre en place un système de draggage pour le Component passé en paramètre
 * @author François Billioud
 */
public class DraggageSystem extends MouseAdapter {
    private int x0 = 0;//décalage pour le drag&drop
    private int y0 = 0;//décalage pour le drag&drop
    private final Component component;

    /** Permet de mettre en place un système de draggage pour le Component passé en paramètre **/
    public static void createDraggageSystem(Component c) {
        createDraggageSystem(c, c);
    }
    /** Permet de mettre en place un système de draggage pour le Component passé en paramètre.
     * Le composant déplacé peut être un fils du composant réellement déplacé, comme le panel d'une fenetre
     * @param toMove le composant qui subit physiquement le draggage
     * @param toListen le composant qui sera réellement déplacé
     **/
    public static void createDraggageSystem(Component toMove, Component toListen) {
        DraggageSystem ds = new DraggageSystem(toMove);
        toListen.addMouseListener(ds);
        toListen.addMouseMotionListener(ds);
    }
    /** Permet de mettre en place un système de draggage pour le Component passé en paramètre **/
    private DraggageSystem(Component c) {
        component = c;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = component.getX();
        int y = component.getY();
        component.setLocation(x + e.getX() - x0, y + e.getY() - y0);
    }
    @Override
    public void mousePressed(MouseEvent e) {
        x0 = e.getX();
        y0 = e.getY();
    }
}
