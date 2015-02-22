/** «Copyright 2011 François Billioud»
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

package bomehc.elements;

import bomehc.utils.boutons.ActionComplete;
import bomehc.utils.managers.ColorManager;
import bomehc.utils.managers.FontManager;
import bomehc.utils.boutons.Bouton;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager2;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractButton;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import bomehc.utils.interfaces.ProportionalComponent;

/**
 * Cette classe abstraite définit le modèle utilisé pour les barres d'outils des onglets
 * Celui-ci est composé de deux panneaux : gauche et droit
 */
@SuppressWarnings("serial")
public class BarreOutils extends JPanel {

    public static final boolean GAUCHE = BarreOutilsLayout.GAUCHE;
    public static final boolean DROIT = BarreOutilsLayout.DROITE;

    public final Font POLICE_BOUTON = FontManager.get("font toolbar button");
    private final int TOOLBAR_HEIGHT = 48;

    private HashMap<String, ButtonGroup> groupes;

    /**
     * Crée le modèle de barre d'outils
     * Définit des GridLayout sur chaque panneau
     */
    public BarreOutils() {
        setBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, ColorManager.get("color tool bar border")));

        //prépare la barre outil
        setLayout(new BarreOutilsLayout(this));
        setBackground(ColorManager.get("color tool bar background"));
        setPreferredSize(new Dimension(Integer.MAX_VALUE, TOOLBAR_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, TOOLBAR_HEIGHT));
    }
    
    public void addComponent(Component composant, boolean side) {
        add(composant, side);
    }

    public void addComponentOnLeft(Component composant) {
        addComponent(composant, GAUCHE);
    }

    public void addComponentOnRight(Component composant) {
        addComponent(composant, DROIT);
    }

    public void removeComponent(Component composant) {
        if(composant==null) {return;}
        remove(composant);
    }
    
    public void removeBouton(Action a) {
        for(Component c : getComponents()) {
            if(c instanceof Bouton && ((Bouton)c).getAction()==a) {remove(c);}
            else if(c instanceof AbstractButton && ((AbstractButton)c).getAction()==a) {remove(c);}
        }
    }

    public Bouton addBouton(Action a, String aspect, boolean type, boolean side, String groupName) {
        Bouton bouton = new Bouton(a,aspect,type);
        addBouton(bouton, side, groupName);
        return bouton;
    }

    public void addBouton(Bouton bouton, boolean side, String groupName) {
        bouton.setFont(POLICE_BOUTON);
        addComponent(bouton, side);
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

    public JPanel addSeparateur(boolean side) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalStrut(5));
        JSeparator separateur = new JSeparator(JSeparator.VERTICAL);
        separateur.setBackground(ColorManager.get("color separator"));
//        separateur.setForeground(Color.red);
        separateur.setOpaque(true);
        p.add(separateur);
        p.add(Box.createHorizontalStrut(5));
        add(p,side);
        return p;
    }
    public JPanel addSeparateurOnLeft() { return addSeparateur(GAUCHE); }
    public JPanel addSeparateurOnRight() { return addSeparateur(DROIT); }

    public void clearSelection(String groupe) { if(groupes.containsKey(groupe)) groupes.get(groupe).clearSelection(); }

    
    private static class BarreOutilsLayout implements LayoutManager2 {

        private static final int GAP = 2;
        
        private final List<Component> gauche = new LinkedList<>();
        private final List<Component> droit = new LinkedList<>();
        private final Container parent;
        
        private static final boolean DROITE = true;
        private static final boolean GAUCHE = false;
        
        private BarreOutilsLayout(Container parent) {
            this.parent = parent;
        }
        
        @Override
        public void addLayoutComponent(String name, Component comp) {
            gauche.add(comp);
        }
        @Override
        public void removeLayoutComponent(Component comp) {gauche.remove(comp);droit.remove(comp);}
        
        private List<Component> getComponents() {
            return Arrays.asList(parent.getComponents());
        }
        
        private int getPreferredWidth(List<Component> components) {
            int w = 0;
            for(Component c : components) {
                if(!c.isVisible()) {continue;}
                w+=c.getPreferredSize().width;
//                w+=c.getSize().width;
            }
            w+=(1+components.size())*GAP;
            return w;
        }
        
        private int getMinimumWidth(List<Component> components) {
            int w = 0;
            for(Component c : components) {
                if(!c.isVisible()) {continue;}
                w+=c.getMinimumSize().width;
//                w+=c.getSize().width;
            }
            w+=(1+components.size())*GAP;
            return w;
        }
        
        private int getWidth(List<Component> components) {
            int w = 0;
            for(Component c : components) {
                if(!c.isVisible()) {continue;}
                w+=c.getSize().width;
            }
            w+=(1+components.size())*GAP;
            return w;
        }
        
        private int getPreferredHeight(List<Component> components) {
            int h = 0;
            for(Component c : components) {
//                h = Math.max(h, c.getPreferredSize().height);
                if(!c.isVisible()) {continue;}
                h = Math.max(h, c.getSize().height);
            }
            return h;
        }
        
        private int getMinimumHeight(List<Component> components) {
            int h = 0;
            for(Component c : components) {
//                h = Math.max(h, c.getMinimumSize().height);
                if(!c.isVisible()) {continue;}
                h = Math.max(h, c.getSize().height);
            }
            return h;
        }
        
        private int getHeight(List<Component> components) {
            int h = 0;
            for(Component c : components) {
                if(!c.isVisible()) {continue;}
                h = Math.max(h, c.getSize().height);
            }
            return h;
        }
        
        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(getPreferredWidth(getComponents()), getPreferredHeight(getComponents()));
        }
        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(getMinimumWidth(getComponents()), getMinimumHeight(getComponents()));
        }
        
        @Override
        public void layoutContainer(Container parent) {
            if(parent.getParent()==null) {return;}
            int n = getComponents().size();
            if(n==0) {return;}
            int maxWidth = parent.getWidth(), maxHeight = parent.getHeight()-parent.getInsets().top-parent.getInsets().bottom;
            
            //On met à l'échelle les éléments proportionnels
            for(Component c : getComponents()) {
                if(c instanceof ProportionalComponent) {((ProportionalComponent)c).setSizeByHeight(maxHeight);}
            }
            
            int totalWidth = getMinimumWidth(getComponents())+GAP;
            double widthCoef = totalWidth>maxWidth ? maxWidth/(double)totalWidth : 1;//On écrase les largeurs si ça dépasse
            int adaptedGap = (int) (GAP*widthCoef);
            
            for(Component c : getComponents()) {
                c.setSize((int) (c.getMinimumSize().getWidth()*widthCoef), maxHeight);
            }
            
            int offset = adaptedGap;
            for(Component c : gauche) {
                if(!c.isVisible()) {continue;}
                c.setLocation(offset, parent.getInsets().top);
                offset+=c.getWidth()+adaptedGap;
            }
            offset = adaptedGap;
            for(Component c : droit) {
                if(!c.isVisible()) {continue;}
                offset+=c.getWidth()+adaptedGap;
                c.setLocation(parent.getWidth()-offset, parent.getInsets().top);
                c.repaint();
            }
            parent.repaint();
            
        }

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
            if(Boolean.valueOf(DROIT).equals(constraints)) {droit.add(comp);} else {gauche.add(comp);}
        }

        @Override
        public Dimension maximumLayoutSize(Container target) {
            if(target.getParent()!=null) {
                return new Dimension(target.getParent().getWidth(),target.getParent().getWidth()/30);
            } else {
                return target.getMaximumSize();
            }
        }

        @Override
        public float getLayoutAlignmentX(Container target) {return 0;}

        @Override
        public float getLayoutAlignmentY(Container target) {return 0;}

        @Override
        public void invalidateLayout(Container target) {}
        
    }
}
