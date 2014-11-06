/**
 * «Copyright 2011 François Billioud»
 *
 * This file is part of MathEOS.
 *
 * MathEOS is free software: you can redistribute it and/or modify under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * MathEOS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY.
 *
 * You should have received a copy of the GNU General Public License along with
 * MathEOS. If not, see <http://www.gnu.org/licenses/>.
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
package matheos.elements;

import java.awt.event.MouseAdapter;
import matheos.IHM;
import java.awt.event.MouseEvent;

/**
 *
 * @author François Billioud
 */
public class ChangeModeListener extends MouseAdapter {

    public static final boolean COURS = EcranPartage.COURS;
    public static final boolean TP = EcranPartage.TP;
    private final boolean mode;

    /**
     * Détecte les clics sur les éléments qui entrainent les changements de
     * partie.
     * FIXME : Il serait plus judicieux d'utiliser un Event lancé à travers Swing jusqu'à InterfaceComplete
     * @param mode le mode à activer lors du clic sur l'élément
     */
    public ChangeModeListener(boolean mode) {
        this.mode = mode;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        IHM.activeMode(mode);
    }
}
