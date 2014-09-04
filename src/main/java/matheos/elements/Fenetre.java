/** «Copyright 2011 François Billioud»
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

package matheos.elements;

import matheos.arevoir.inutilise.SizeManager;
import matheos.utils.librairies.DimensionTools;
import matheos.utils.managers.ImageManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class Fenetre extends JFrame {

    /** Elément qui va gérer la taille des composants du logiciel **/
    private SizeManager sizeManager;
    
    public Fenetre() {
        super("MathEOS");//appelle le constructeur JFrame("MathEOS")
        prepareWindow();
        setIconImage(ImageManager.getIcone("applicationIcon").getImage());

        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
    }

    private void prepareWindow() {
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        sizeManager = new SizeManager(this);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        setMinimumSize(new Dimension((int)(screenSize.getWidth()*0.8), (int)(screenSize.getHeight()*0.8)));//permet de réduire la fenêtre
        setPreferredSize(DimensionTools.fois(screenSize,0.8));//permet de réduire la fenêtre
        setExtendedState(MAXIMIZED_BOTH);//ouvre en plein écran
        
        //Ce code permet de faire un vrai mode plein écran exclusif, mais la fenêtre perd les icônes de fermeture/réduction/agrandissement
//        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice device = env.getDefaultScreenDevice();
//        device.setFullScreenWindow(this);
    }

    public SizeManager getSizeManager() { return sizeManager; }
}
