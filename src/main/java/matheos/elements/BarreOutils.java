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

import matheos.utils.boutons.ActionComplete;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.FontManager;
import matheos.utils.boutons.Bouton;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Cette classe abstraite définit le modèle utilisé pour les barres d'outils des onglets
 * Celui-ci est composé de deux panneaux : gauche et droit
 */
@SuppressWarnings("serial")
public class BarreOutils extends JPanel {

    public static final boolean GAUCHE = false;
    public static final boolean DROIT = true;

    public static final int LARGEUR_BOUTON = 45;
    public static final int HAUTEUR_BOUTON = 45;
    public static final Font POLICE_BOUTON = FontManager.get("font toolbar button");

    private final JPanel gauche = new JPanel();
    private final JPanel droit = new JPanel();

    private HashMap<String, ButtonGroup> groupes;

    /**
     * Crée le modèle de barre d'outils
     * Définit des GridLayout sur chaque panneau
     */
    public BarreOutils() {
        setBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, Color.GRAY));

        //prépare la barre outil
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(ColorManager.get("color tool bar background"));
//        IHM.addToSizeManager(this, 1366, HAUTEUR_BOUTON);


        //partie gauche
        gauche.setLayout(new BoxLayout(gauche, BoxLayout.X_AXIS));
        gauche.setBackground(ColorManager.get("color tool bar background"));
        gauche.setMaximumSize(new Dimension(683, HAUTEUR_BOUTON));
//        IHM.addToSizeManager(gauche, 683, HAUTEUR_BOUTON);
        add(gauche);

        //séparation
        add(Box.createHorizontalGlue());

        //partie droite
        droit.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        droit.setLayout(new BoxLayout(droit, BoxLayout.LINE_AXIS));
        droit.setBackground(ColorManager.get("color tool bar background"));
        droit.setMaximumSize(new Dimension(683, HAUTEUR_BOUTON));
//        IHM.addToSizeManager(droit, 683, HAUTEUR_BOUTON);
        add(droit);

        setPreferredSize(new Dimension(LARGEUR_BOUTON,HAUTEUR_BOUTON));
        setMaximumSize(new Dimension(LARGEUR_BOUTON,HAUTEUR_BOUTON));
    }

    private void addComponent(Component composant, JPanel panel) {
        panel.add(composant);
        panel.revalidate();
        panel.repaint();
    }

    public void addComponentOnLeft(Component composant) {
        addComponent(composant, gauche);
    }

    public void addComponentOnRight(Component composant) {
        addComponent(composant, droit);
    }

    public void removeComponent(Component composant) {
        gauche.remove(composant);
        droit.remove(composant);
    }
    
    public void removeBouton(Action a) {
        for(Component c : gauche.getComponents()) {
            if(c instanceof Bouton) {
                Bouton b = (Bouton) c;
                if(b.getButtonComponent().getAction()==a) {
                    gauche.remove(c);
                    gauche.repaint();
                }
            }
        }
        for(Component c : droit.getComponents()) {
            if(c instanceof Bouton) {
                Bouton b = (Bouton) c;
                if(b.getButtonComponent().getAction()==a) {
                    droit.remove(c);
                    droit.repaint();
                }
            }
        }
    }

    public Bouton addBouton(Action a, String aspect, boolean type, boolean side, String groupName) {
        Bouton bouton = new Bouton(a,aspect,type);
        addBouton(bouton, side, groupName);
        return bouton;
    }

    public void addBouton(Bouton bouton, boolean side, String groupName) {
        bouton.setFont(POLICE_BOUTON);
        int largeur = bouton.setSize(HAUTEUR_BOUTON);
//        IHM.addToSizeManager(bouton, largeur, HAUTEUR_BOUTON);

        if (side==GAUCHE) {addComponentOnLeft(bouton);} else {addComponentOnRight(bouton);}
        if(groupName!=null) {
            if(groupes==null) {groupes = new HashMap<>();}
            if(!groupes.containsKey(groupName)) groupes.put(groupName, new ButtonGroup());
            groupes.get(groupName).add(bouton.getButtonComponent());
        }
    }

    public Bouton addBouton(Action a, String aspect, boolean type, boolean side) {
        return this.addBouton(a, aspect, type, side, null);
    }

    public void addBoutonOnLeft(Bouton bouton) { addBouton(bouton, GAUCHE, null); }
    public Bouton addBoutonOnLeft(Action a, String aspect) { return addBouton(a,aspect,a instanceof ActionComplete.Toggle,GAUCHE); }
    public Bouton addBoutonOnLeft(Action a) { return addBoutonOnLeft(a,null); }

    public final void addBoutonOnRight(Bouton bouton) { addBouton(bouton, DROIT, null); }
    public Bouton addBoutonOnRight(Action a, String aspect, String groupe) { return addBouton(a,aspect,a instanceof ActionComplete.Toggle,DROIT); }
    public Bouton addBoutonOnRight(Action a, String aspect) { return addBouton(a,aspect,a instanceof ActionComplete.Toggle,DROIT); }
    public final Bouton addBoutonOnRight(Action a) { return addBoutonOnRight(a,null); }

    public Bouton addSwitchOnLeft(Action a, String aspect, String groupe) { return addBouton(a,aspect,Bouton.TOGGLE,GAUCHE,groupe); }
    public Bouton addSwitchOnLeft(Action a, String aspect) { return addBouton(a,aspect,Bouton.TOGGLE,GAUCHE); }
    public Bouton addSwitchOnLeft(Action a) { return addSwitchOnLeft(a,null); }

    public Bouton addSwitchOnRight(Action a, String aspect, String groupe) { return addBouton(a,aspect,Bouton.TOGGLE,DROIT,groupe); }
    public Bouton addSwitchOnRight(Action a, String aspect) { return addBouton(a,aspect,Bouton.TOGGLE,DROIT); }
    public Bouton addSwitchOnRight(Action a) { return addSwitchOnRight(a,null); }

    public void addSeparateur(boolean side) {
        JPanel cote = (side==GAUCHE ? gauche : droit);
        cote.add(Box.createHorizontalStrut(5));
        cote.add(new JSeparator(JSeparator.VERTICAL));
        cote.add(Box.createHorizontalStrut(5));
    }
    public void addSeparateurOnLeft() { addSeparateur(GAUCHE); }
    public void addSeparateurOnRight() { addSeparateur(DROIT); }

    public void clearSelection(String groupe) { if(groupes.containsKey(groupe)) groupes.get(groupe).clearSelection(); }

}
