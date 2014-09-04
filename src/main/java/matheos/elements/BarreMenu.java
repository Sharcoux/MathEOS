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
import matheos.utils.managers.ImageManager;
import matheos.utils.managers.Traducteur;
import matheos.utils.boutons.Bouton;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class BarreMenu extends JMenuBar {

    //options d'affichage
    public static final Font POLICE_MENU = FontManager.get("font menu menubar");
    public static final Font POLICE_ITEM = FontManager.get("font item menubar");
    public static final int LARGEUR_BOUTON = 70;
    public static final int HAUTEUR_BOUTON = 45;
    public static final int LARGEUR_MENU = 150;
    public static final int HAUTEUR_MENU = 40;
    protected static final int TAILLE_ICONES_ITEMS = 24;
    
    public static final String MENU_OPTION = "option";

    //types d'éléments
    public static final boolean ITEM = false;
    public static final boolean CHECK_BOX = true;

    public static enum Categorie { fichier(0,"file"), edition(1,"edit"), options(2,MENU_OPTION), aide(3,"help");
        private final int index;
        private final String balise;
        private Menu menu;
        private Categorie(int i,String s) { index = i; balise = s; }
    }
    public static final Categorie FICHIER = Categorie.fichier;
    public static final Categorie EDITION = Categorie.edition;
    public static final Categorie OPTIONS = Categorie.options;
    public static final Categorie AIDE = Categorie.aide;



    public BarreMenu() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setOpaque(true);
        setBackground(ColorManager.get("color menu bar background"));
        setPreferredSize(new Dimension(LARGEUR_BOUTON,HAUTEUR_BOUTON-5));
        setMaximumSize(new Dimension(LARGEUR_BOUTON,HAUTEUR_BOUTON-5));

        initialize();
    }

    private void initialize() {
        for(Categorie c : Categorie.values()) {
            c.menu = new Menu(c.balise);
            add(c.menu, c.index);
        }
    }
    
    public void addElement(Action a, Categorie menuCategorie) { menuCategorie.menu.addElement(a); }
    public void addCheckBox(Action a, Categorie menuCategorie) { menuCategorie.menu.addCheckBox(a); }

    public void addElement(Component c, Categorie menuCategorie) {
        menuCategorie.menu.add(c);
    }

    public void changeMenuOptions(Menu menuOptions) {
        remove(OPTIONS.menu);
        add(menuOptions,OPTIONS.index);
        OPTIONS.menu = menuOptions;
        revalidate(); repaint();
    }

    public static Menu creerMenuOptions() {
        return new Menu(MENU_OPTION);
    }

    public Bouton addBouton(final Action action) {
        BoutonMenu bouton = new BoutonMenu(action);
        add(bouton);
        return bouton;
    }

    public static class Menu extends JMenu {
        public Menu(String nom) {
            super("  "+Traducteur.traduire(nom));//HACK pour faire un décalage en X
            setFont(POLICE_MENU);
            setMaximumSize(new Dimension(LARGEUR_MENU,HAUTEUR_MENU));
//            setMinimumSize(new Dimension(LARGEUR_MENU,HAUTEUR_MENU));
            setMargin(new Insets(5, 30, 5, 30));//FIXME : Pk ça marche pas ça ?
            setBorderPainted(true);
            setBorder(BorderFactory.createLineBorder(ColorManager.get("color menu border")));
            setBackground(ColorManager.get("color menu background"));

            addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {}
                public void mousePressed(MouseEvent e) {}
                public void mouseReleased(MouseEvent e) {}
                public void mouseEntered(MouseEvent e) {
                    Menu.this.setOpaque(true);
                    Menu.this.repaint();
                }
                public void mouseExited(MouseEvent e) {
                    Menu.this.setOpaque(false);
                    Menu.this.repaint();
                }
            });
        }

        /**
         * ajoute un élément au menu
         * @param a l'action à associer au menu
         */
        public void addElement(Action a) { add(new Element(a)); }
        /**
         * ajoute une checkbox au menu
         * @param a l'action à associer au menu
         */
        public void addCheckBox(Action a) { add(new CheckBoxElement(a)); }
    }

    public static class Element extends JMenuItem {
        public Element(Action a) {
            setAction(a);
            setIcon(ImageManager.getIcone((String)a.getValue(Action.ACTION_COMMAND_KEY),TAILLE_ICONES_ITEMS,TAILLE_ICONES_ITEMS));
            setFont(POLICE_ITEM);
            setToolTipText(null);
        }
    }

    public static class CheckBoxElement extends JCheckBoxMenuItem {
        public CheckBoxElement(Action a) {
            super(a);
            setFont(POLICE_ITEM);
            setToolTipText(null);
        }
    }

    public static class BoutonMenu extends Bouton {
        public BoutonMenu(Action a) {
            super(a);
            setMaximumSize(new Dimension(LARGEUR_BOUTON,HAUTEUR_BOUTON));
            setPreferredSize(new Dimension(LARGEUR_BOUTON,HAUTEUR_BOUTON));
            setSize(HAUTEUR_BOUTON);
            getButtonComponent().setContentAreaFilled(false);
        }
    }

}
