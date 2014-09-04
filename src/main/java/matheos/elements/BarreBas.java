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

import matheos.utils.managers.ColorManager;
import matheos.utils.managers.FontManager;
import matheos.utils.boutons.Bouton;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

/**
 * Classe définissant la barre du bas
 * La barre du bas est un panel contenant 13 cellules en ligne
 * Ces cellules sont remplies avec des boutons et des panels vides.
 * Ces derniers peuvent recevoir à leur tour des boutons
 */

@SuppressWarnings("serial")
public class BarreBas extends JPanel {

//    public enum BOUTONS {
//        Sommaire(new IHM.ActionSommaire(), 0), ZoomP(new IHM.ActionZoomP(), 1), ZoomM(new IHM.ActionZoomM(), 2), Calculatrice(new IHM.ActionCalculatrice(), 11), Clavier(new IHM.ActionClavierNumerique(), 12);
//        private Action action; private int index; private BOUTONS(Action a, int i) {action=a;index=i;}
//        public Action getAction() { return action; } public int getIndex() { return index; }
//    }
    /** nombre d'emplacements pour des boutons **/
    private static final int CASES = 13;

    public static final int HAUTEUR_BOUTON = 45;
    public static final int LARGEUR_BOUTON = Toolkit.getDefaultToolkit().getScreenSize().width/CASES;
    
    public static final Font POLICE_BOUTON = FontManager.get("font bottom toolbar");

    //position des boutons et des flèches
    public static final int SOMMAIRE = 0;
    public static final int ZOOM_P = 1;
    public static final int ZOOM_M = 2;
    public static final int FLECHE_GAUCHE = 3;
    public static final int CONSULTATION = 4;
    public static final int FLECHE_MILIEU = 6;
    public static final int FLECHE_DROITE = 9;
    public static final int CALCULATRICE = 11;
    public static final int CLAVIER_NUMERIQUE = 12;
    public static final HashMap<Integer,Integer> CORRESPONDANCES = new HashMap<>();
    static {
        CORRESPONDANCES.put(EcranPartage.GAUCHE, FLECHE_GAUCHE);
        CORRESPONDANCES.put(EcranPartage.MILIEU, FLECHE_MILIEU);
        CORRESPONDANCES.put(EcranPartage.DROIT, FLECHE_DROITE);
    }

    /** panels des emplacements **/
    private final Emplacement[] emplacement = new Emplacement[CASES];

    /**
     * Crée la barre du bas
     * Crée les icônes, les panneaux vides et positionne le tout dans une ligne GridLayout
     */
    public BarreBas() {
        //aspect
        setLayout(new GridLayout(1,CASES));
        setOpaque(true);
        setBackground(ColorManager.get("color down bar"));
        setBorder(BorderFactory.createMatteBorder(2,0,0,0,ColorManager.get("color down bar border")));
        setPreferredSize(new Dimension(LARGEUR_BOUTON,HAUTEUR_BOUTON));
        setMaximumSize(new Dimension(LARGEUR_BOUTON,HAUTEUR_BOUTON));

        for (int i = 0; i<CASES; i++) {
            emplacement[i] = new Emplacement();
            add(emplacement[i]);
        }
//        for (BOUTONS bouton : BOUTONS.values()) {
//            addBouton(bouton.getAction(), bouton.getIndex());
//        }
    }

    public void addComposant(JComponent composant, int position) {
        emplacement[position].addComposant(composant);
    }

    public void removeComposant(int position) {
        emplacement[position].removeComposant();
    }

    public Bouton addBouton(Action a, int position) {
        return addBouton(a,null,position);
    }

    public Bouton addBouton(Action a, String aspect, int position) {
        return addBouton(new Bouton(a,aspect),position);
    }

    public Bouton addBouton(Action a, String aspect, int position, boolean type) {
        return addBouton(new Bouton(a,aspect,type),position);
    }
    
    public Bouton addBouton(Action a, int position, boolean type) {
        return addBouton(new Bouton(a,type),position);
    }

    public Bouton addBouton(Bouton bouton, int position) {
        bouton.setFont(POLICE_BOUTON);
        int largeur = bouton.setSize(HAUTEUR_BOUTON);
//        IHM.addToSizeManager(bouton, largeur, HAUTEUR_BOUTON);

        addComposant(bouton,position);
        return bouton;
    }

    public void removeBouton(int position) {
        removeComposant(position);
    }

    public void setMode(boolean mode) {

    }

    private static class Emplacement extends JPanel {
        private Emplacement() {
            setLayout(new BorderLayout());
            setSize(LARGEUR_BOUTON,HAUTEUR_BOUTON);
            setOpaque(false);
        }
        public void addComposant(Component c) {
            removeAll();
            add(c);
            revalidate();
            repaint();
        }
        public void removeComposant() {
            removeAll();
            revalidate();
            repaint();
        }
    }

}
