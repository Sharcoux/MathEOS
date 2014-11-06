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

/**
 * Cette classe abstraite définit le modèle utilisé pour les barres d'outils des onglets
 * Celui-ci est composé de deux panneaux : gauche et droit
 */
@SuppressWarnings("serial")
public class BarreOutils extends JPanel {

    public static final boolean GAUCHE = BarreOutilsLayout.GAUCHE;
    public static final boolean DROIT = BarreOutilsLayout.DROITE;

    public static final Font POLICE_BOUTON = FontManager.get("font toolbar button");

    private HashMap<String, ButtonGroup> groupes;
    private final BarreOutilsLayout layout;

    /**
     * Crée le modèle de barre d'outils
     * Définit des GridLayout sur chaque panneau
     */
    public BarreOutils() {
        setBorder(BorderFactory.createMatteBorder(2, 0, 2, 0, Color.GRAY));

        //prépare la barre outil
        setLayout(layout = new BarreOutilsLayout(this));
        setBackground(ColorManager.get("color tool bar background"));
    }
    
    @Override
    public Dimension getPreferredSize() {
        return getParent()==null ? super.getPreferredSize() : new Dimension(getParent().getWidth(),getParent().getWidth()/30);
    }
    @Override
    public Dimension getMaximumSize() {
        return getParent()==null ? super.getMaximumSize() : new Dimension(getParent().getWidth(),getParent().getWidth()/30);
    }
    @Override
    public void setSize(int l, int h) {
        super.setSize(l, h);
    }
    @Override
    public void setSize(Dimension d) {setSize(d.width, d.height);}
    
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
        p.add(new JSeparator(JSeparator.VERTICAL));
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
//                w+=c.getPreferredSize().width;
                w+=c.getSize().width;
            }
            w+=(1+components.size())*GAP;
            return w;
        }
        
        private int getMinimumWidth(List<Component> components) {
            int w = 0;
            for(Component c : components) {
                if(!c.isVisible()) {continue;}
//                w+=c.getMinimumSize().width;
                w+=c.getSize().width;
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
            int totalWidth = getMinimumWidth(getComponents());
            double widthCoef = totalWidth>maxWidth ? maxWidth/totalWidth : 1;//On écrase les largeurs si ça dépasse
            
            for(Component c : getComponents()) {
                c.setSize((int) (c.getMinimumSize().getWidth()*widthCoef), parent.getHeight()-parent.getInsets().top-parent.getInsets().bottom);
            }
            
            int offset = GAP;
            for(Component c : gauche) {
                if(!c.isVisible()) {continue;}
                c.setLocation(offset, parent.getInsets().top);
                offset+=c.getWidth()+GAP;
            }
            offset = GAP;
            for(Component c : droit) {
                if(!c.isVisible()) {continue;}
                offset+=c.getWidth()+GAP;
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
        public void invalidateLayout(Container target) {
            layoutContainer(target);
        }
        
    }
}
