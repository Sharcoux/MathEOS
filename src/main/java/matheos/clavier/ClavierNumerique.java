/** «Copyright 2011,2013 François Billioud, Guillaume Varoquaux»
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

package matheos.clavier;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import matheos.utils.managers.PermissionManager;

/**
 *
 * @author François Billioud, Guillaume Varoquaux
 */
@SuppressWarnings("serial")
public final class ClavierNumerique extends Clavier {

    public static final int BOUTON_X = 0;
    public static final int BOUTON_Y = 1;
    public static final int BOUTON_A = 2;
    public static final int BOUTON_RETOUR = 3;
    public static final int BOUTON_7 = 4;
    public static final int BOUTON_8 = 5;
    public static final int BOUTON_9 = 6;
    public static final int BOUTON_DIVISE = 7;
    public static final int BOUTON_4 = 8;
    public static final int BOUTON_5 = 9;
    public static final int BOUTON_6 = 10;
    public static final int BOUTON_MULTIPLIE = 11;
    public static final int BOUTON_1 = 12;
    public static final int BOUTON_2 = 13;
    public static final int BOUTON_3 = 14;
    public static final int BOUTON_MOINS = 15;
    public static final int BOUTON_0 = 16;
    public static final int BOUTON_VIRGULE = 17;
    public static final int BOUTON_PLUS = 18;
    public static final int BOUTON_EGAL = 19;
    public static final int NOMBRE_BOUTON = 20;

    public ClavierNumerique() {
        super();
        panelClavier = new PanelNumerique();
        this.setSize(panelClavier.getWidth(), panelClavier.getHeight());
        this.add(panelClavier);
        
        updateBoutonsAutorises();
        
        repaint();
    }
    
    public void updateBoutonsAutorises() {
        activerBouton(BOUTON_X, PermissionManager.isCaracteresLitterauxAllowed());
        activerBouton(BOUTON_Y, PermissionManager.isCaracteresLitterauxAllowed());
        activerBouton(BOUTON_A, PermissionManager.isCaracteresLitterauxAllowed());
    }

    private class PanelNumerique extends PanelClavier {

        private PanelNumerique() {
            GridLayout grille = new GridLayout(5, 5, 5, 5);
            this.setLayout(grille);
            this.setSize(230, 245);

            bouton = new BoutonClavier[NOMBRE_BOUTON];

            bouton[BOUTON_X] = new BoutonClavier(new ActionBoutonX());
            bouton[BOUTON_Y] = new BoutonClavier(new ActionBoutonY());
            bouton[BOUTON_A] = new BoutonClavier(new ActionBoutonA());
            bouton[BOUTON_RETOUR] = new BoutonClavier(new ActionBoutonRETOUR());
            bouton[BOUTON_7] = new BoutonClavier(new ActionBoutonTexte("7"));
            bouton[BOUTON_8] = new BoutonClavier(new ActionBoutonTexte("8"));
            bouton[BOUTON_9] = new BoutonClavier(new ActionBoutonTexte("9"));
            bouton[BOUTON_DIVISE] = new BoutonClavier(new ActionBoutonDIVISE());
            bouton[BOUTON_4] = new BoutonClavier(new ActionBoutonTexte("4"));
            bouton[BOUTON_5] = new BoutonClavier(new ActionBoutonTexte("5"));
            bouton[BOUTON_6] = new BoutonClavier(new ActionBoutonTexte("6"));
            bouton[BOUTON_MULTIPLIE] = new BoutonClavier(new ActionBoutonMULTIPLIE());
            bouton[BOUTON_1] = new BoutonClavier(new ActionBoutonTexte("1"));
            bouton[BOUTON_2] = new BoutonClavier(new ActionBoutonTexte("2"));
            bouton[BOUTON_3] = new BoutonClavier(new ActionBoutonTexte("3"));
            bouton[BOUTON_MOINS] = new BoutonClavier(new ActionBoutonTexte("-"));
            bouton[BOUTON_0] = new BoutonClavier(new ActionBoutonTexte("0"));
            bouton[BOUTON_VIRGULE] = new BoutonClavier(new ActionBoutonTexte(","));
            bouton[BOUTON_PLUS] = new BoutonClavier(new ActionBoutonTexte("+"));
            bouton[BOUTON_EGAL] = new BoutonClavier(new ActionBoutonTexte("="));

            for(BoutonClavier b : bouton) add(b);
        }
    }

    private class ActionBoutonX extends ActionBoutonMathMLString {
        ActionBoutonX() {
            //super(ImageManager.getIcone("numeric x", 23, 23));
            super("<html><i>x</i></html>","<mi>x</mi>");
        }
    }

    private class ActionBoutonY extends ActionBoutonMathMLString {
        ActionBoutonY() {
            //super(ImageManager.getIcone("numeric y", 23, 23));
            super("<html><i>y</i></html>","<mi>y</mi>");
        }
    }

    private class ActionBoutonA extends ActionBoutonMathMLString {
        ActionBoutonA() {
            //super(ImageManager.getIcone("numeric a", 23, 23));
            super("<html><i>a</i></html>","<mi>a</mi>");
        }
    }

    private class ActionBoutonRETOUR extends ActionBoutonTexte {
        ActionBoutonRETOUR() {
            super("<html>&larr;</html>");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(getFocusedText().getSelectedText()==null) {
                getFocusedText().setSelectionStart(getFocusedText().getCaretPosition()-1);
                getFocusedText().setSelectionEnd(getFocusedText().getCaretPosition());
            }
            getFocusedText().replaceSelection("");
        }
    }

    private class ActionBoutonDIVISE extends ActionBoutonHTMLString {
        ActionBoutonDIVISE() {
            super("<html>&divide;</html>");
        }
    }

    private class ActionBoutonMULTIPLIE extends ActionBoutonHTMLString {
        ActionBoutonMULTIPLIE() {
            super("<html>&times;</html>");
        }
    }

}
