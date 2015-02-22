/**
 * «Copyright 2012 François Billioud»
 *
 * This file is part of Bomehc.
 *
 * Bomehc is free software: you can redistribute it and/or modify under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * Bomehc is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY.
 *
 * You should have received a copy of the GNU General Public License along with
 * Bomehc. If not, see <http://www.gnu.org/licenses/>.
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
package bomehc.utils.boutons;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import bomehc.utils.interfaces.ProportionalComponent;
import bomehc.utils.managers.ColorManager;

/**
 * Cette class permet de créer indistinctement un JButton ou un JToogleButton.
 * Elle inclut aussi la gestion des <code>ActionComplete</code>, qui permettent
 * de gérer les images de rollover, de selection et l'affichage de l'arrière-plan.
 * @author François Billioud
 * @see ActionComplete
 */
@SuppressWarnings("serial")
public class Bouton extends JPanel implements ProportionalComponent {

    /** Constante représentant le type JButton **/
    public static boolean BOUTON = false;
    /** Constante représentant le type JToggleButton **/
    public static boolean TOGGLE = true;
    /** Le bouton que cette classe vient décorer **/
    private AbstractButton bouton = null;

    private static final int BUTTON_GAP_H = 0;
    private static final int BUTTON_GAP_V = 0;
    private static final int ICON_GAP_H = 15;
    private static final int ICON_GAP_V = 4;
    
    private int sizePolicy = 0;
    
    /**
     * Crée un bouton du type définit
     * @param type BOUTON pour un JButton, TOGGLE pour un JToggleButton
     * @see ActionComplete
     */
    public Bouton(boolean type) {
        setOpaque(false);
        setLayout(new LayoutManager() {
            @Override
            public void addLayoutComponent(String name, Component comp) {}
            @Override
            public void removeLayoutComponent(Component comp) {}
            @Override
            public Dimension preferredLayoutSize(Container parent) {return new Dimension(bouton.getPreferredSize().width+BUTTON_GAP_H, bouton.getPreferredSize().height+BUTTON_GAP_V);}
            @Override
            public Dimension minimumLayoutSize(Container parent) {return new Dimension(bouton.getMinimumSize().width+BUTTON_GAP_H, bouton.getMinimumSize().height+BUTTON_GAP_V);}
            @Override
            public void layoutContainer(Container parent) {
                bouton.setLocation(BUTTON_GAP_V/2, BUTTON_GAP_H/2);
                bouton.setSize(parent.getWidth()-BUTTON_GAP_H, parent.getHeight()-BUTTON_GAP_V);
            }
        });
        super.setFocusable(false);
        //Corrige la position des Tooltips pour les boutons dans la barre du bas
        bouton = (type ? new JToggleButton() {
            @Override
            public Point getToolTipLocation(MouseEvent event) {
                return positionnerTooltips(event);
            }
        } : new JButton() {
            @Override
            public Point getToolTipLocation(MouseEvent event) {
                return positionnerTooltips(event);
            }
        });
        bouton.setFocusable(false);
//        bouton.setBackground(ColorManager.get("color button background"));
        bouton.setForeground(ColorManager.get("color button foreground"));
        
        //active la touche enter pour les boutons
        bouton.registerKeyboardAction(
                bouton.getActionForKeyStroke(
                        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
                        JComponent.WHEN_FOCUSED);
        bouton.registerKeyboardAction(
                bouton.getActionForKeyStroke(
                        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                        JComponent.WHEN_FOCUSED);
        
        add(bouton);
        
        //Transfert les propertyChanges
        bouton.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Bouton.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        bouton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED) {
                    Bouton.this.repaint();
                }
            }
        });
        
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                bouton.requestFocusInWindow();
            }
        });
    }
    /**
     * Crée un bouton du type définit à partir d'une action et enrichit à l'aide d'un aspect
     * à lire dans les fichiers de thème et de langue
     * @param action l'action à partir de laquelle créer le bouton
     * @param aspect la balise sera recherchée dans les fichiers de langue et de thème pour enrichir l'action du bouton
     * @param type BOUTON pour un JButton, TOGGLE pour un JToggleButton
     * @see ActionComplete
     */
    public Bouton(Action action, String aspect, boolean type) {//ATTENTION : cas Action == null non géré
        this(type);
        if (aspect != null) {
            InfoBouton info = new InfoBouton(aspect);
            info.addParametersToAction(action);
        }
        setAction(action);
    }

    /**
     * Crée un JButton à partir d'une action et enrichit à l'aide d'un aspect
     * à lire dans les fichiers de thème et de langue
     * @param action l'action à partir de laquelle créer le bouton
     * @param aspect la balise sera recherchée dans les fichiers de langue et de thème pour enrichir l'action du bouton
     * @see ActionComplete
     */
    public Bouton(Action action, String aspect) {
        this(action, aspect, action instanceof ActionComplete.Toggle ? TOGGLE : BOUTON);
    }

    /**
     * Crée un bouton du type définit à partir d'une action
     * @param action l'action à partir de laquelle créer le bouton
     * @param type BOUTON pour un JButton, TOGGLE pour un JToggleButton
     * @see ActionComplete
     */
    public Bouton(Action action, boolean type) {
        this(action, null, type);
    }

    /**
     * Crée un JButton à partir d'une action. Gère les <code>ActionComplete</code>
     * @param action l'action à partir de laquelle créer le bouton
     * @see ActionComplete
     */
    public Bouton(Action action) {
        this(action, action instanceof ActionComplete.Toggle ? TOGGLE : BOUTON);
    }

    /** Permet de récupérer l'instance du bouton décoré par ce décorateur **/
    public AbstractButton getButtonComponent() {
        return bouton;
    }

    @Override
    public boolean requestFocusInWindow() {
        return bouton.requestFocusInWindow();
    }
    @Override
    public void requestFocus() {
        bouton.requestFocus();
    }
    @Override
    public void setFocusable(boolean b) {
        bouton.setFocusable(b);
    }
    @Override
    public boolean isFocusable() {
        return bouton.isFocusable();
    }
    @Override
    public void addKeyListener(KeyListener l) {
        bouton.addKeyListener(l);
    }
    @Override
    public void removeKeyListener(KeyListener l) {
        bouton.removeKeyListener(l);
    }
    
    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        if (bouton != null) {
            bouton.setEnabled(b);
        }
    }

    @Override
    public void setFont(Font f) {
        super.setFont(f);
        if (bouton != null) {
            bouton.setFont(f);
        }
    }
    
//    @Override
//    public void reshape(int x, int y, int largeur, int hauteur) {
//        super.reshape(x, y, largeur, hauteur);
//        if(largeur<6 || hauteur<6) {return;}
//        setButtonSize(largeur - 1, hauteur - 1);
//    }
    
    @Override
    public void setSize(int largeur, int hauteur) {
        super.setSize(largeur, hauteur);
        if(largeur<ICON_GAP_H+BUTTON_GAP_H || hauteur<ICON_GAP_V+BUTTON_GAP_V) {return;}
        setButtonSize(largeur - BUTTON_GAP_H, hauteur - BUTTON_GAP_V);
    }
    
    /** Redimensionne le JButton (ou JToggleButton) lui-même **/
    private void setButtonSize(int largeur, int hauteur) {
        if (bouton != null) {
            if (bouton.getIcon() != null) {
                setIconSize(largeur-ICON_GAP_H, hauteur-ICON_GAP_V);
            }
            bouton.setSize(largeur, hauteur);
            bouton.repaint();
        }
    }

    /** Redimensionne les icones de ce bouton **/
    public void setIconSize(int largeur, int hauteur) {
        Action a = getAction();
        if(a!=null && a instanceof ActionComplete) {
//            ((ActionComplete)a).setSize(largeur, hauteur);
            ActionComplete action = (ActionComplete) a;
            switch(sizePolicy) {
                case SIZE_BY_HEIGHT : action.setSizeByHeight(hauteur); break;
                case SIZE_BY_WIDTH : action.setSizeByWidth(largeur); break;
                default : action.setSize(largeur, hauteur);
            }
            setIconsFromAction(a);
        } else {
            InfoBouton info = new InfoBouton(bouton.getActionCommand(), largeur, hauteur);
            info.setIconesBouton(bouton);
        }
    }

    @Override
    public void setPreferredSize(Dimension d) {
        super.setPreferredSize(d);
        if (bouton != null) {
            int width = d.width==Integer.MAX_VALUE||d.width==Integer.MIN_VALUE||d.width==0 ? d.width : (d.width-BUTTON_GAP_H);
            int height = d.height==Integer.MAX_VALUE||d.height==Integer.MIN_VALUE||d.height==0 ? d.height : (d.height-BUTTON_GAP_V);
            bouton.setPreferredSize(new Dimension(width, height));
        }
    }

    @Override
    public void setMaximumSize(Dimension d) {
        super.setMaximumSize(d);
        if (bouton != null) {
            int width = d.width==Integer.MAX_VALUE||d.width==Integer.MIN_VALUE||d.width==0 ? d.width : (d.width-BUTTON_GAP_H);
            int height = d.height==Integer.MAX_VALUE||d.height==Integer.MIN_VALUE||d.height==0 ? d.height : (d.height-BUTTON_GAP_V);
            bouton.setMaximumSize(new Dimension(width, height));
        }
    }
    
    @Override
    public void setMinimumSize(Dimension d) {
        super.setMinimumSize(d);
        if (bouton != null) {
            int width = d.width==Integer.MAX_VALUE||d.width==Integer.MIN_VALUE||d.width==0 ? d.width : (d.width-BUTTON_GAP_H);
            int height = d.height==Integer.MAX_VALUE||d.height==Integer.MIN_VALUE||d.height==0 ? d.height : (d.height-BUTTON_GAP_V);
            bouton.setMinimumSize(new Dimension(width, height));
        }
    }
    
    @Override
    public void setSizePolicy(int policy) {
        sizePolicy = policy;
    }

    private int calculerHauteur(int largeur) {
        if(initialHeight==0 || initialWidth==0) {return bouton.getPreferredSize().height+BUTTON_GAP_V;}
        double coef = initialHeight/(double)initialWidth;
        return (int) (coef*(largeur-ICON_GAP_H-BUTTON_GAP_H))+ICON_GAP_V+BUTTON_GAP_V;
    }
    private int calculerLargeur(int hauteur) {
        if(initialHeight==0 || initialWidth==0) {return bouton.getPreferredSize().width+BUTTON_GAP_H;}
        double coef = initialWidth/(double)initialHeight;
        return (int) (coef*(hauteur-ICON_GAP_V-BUTTON_GAP_V))+ICON_GAP_H+BUTTON_GAP_H;
    }
    
    /**
     * méthode qui redimensionne le bouton en calculant sa largeur à partir de la taille de son icone
     * @param largeur
     * @return renvoie la largeur ainsi calculée
     */
    @Override
    public int setSizeByHeight(int hauteur) {
        int largeur = calculerLargeur(hauteur);
        setSize(largeur,hauteur);
        return largeur;
    }

    /**
     * méthode qui redimensionne le bouton en calculant sa hauteur à partir de la taille de son icone
     * @param largeur
     * @return renvoie la hauteur ainsi calculée
     */
    @Override
    public int setSizeByWidth(int largeur) {
        int hauteur = calculerHauteur(largeur);
        setSize(largeur,hauteur);
        return hauteur;
    }
    
    private int initialWidth = 0;
    private int initialHeight = 0;
    @Override
    public Dimension getPreferredSize() {
        int height = getHeight(), width = getWidth();
        Dimension d = bouton.getPreferredSize();
        if(height==0) {height = d.height==Integer.MAX_VALUE||d.height==Integer.MIN_VALUE||d.height==0 ? d.height : (d.height+BUTTON_GAP_V);}
        if(width==0) {width = d.width==Integer.MAX_VALUE||d.width==Integer.MIN_VALUE||d.width==0 ? d.width : (d.width+BUTTON_GAP_H);}
        switch(sizePolicy) {
            case SIZE_BY_HEIGHT : width = calculerLargeur(height); break;
            case SIZE_BY_WIDTH : height = calculerHauteur(width); break;
            default: width = bouton.getPreferredSize().width+BUTTON_GAP_H; height = bouton.getPreferredSize().height+BUTTON_GAP_V; break;
        }
        return new Dimension(width, height);
    }
    
    @Override
    public Dimension getMinimumSize() {
//        return new DimensionT(bouton.getMinimumSize()).plus(1,1);
        int height = getHeight(), width = getWidth();
        Dimension d = bouton.getMinimumSize();
        if(height==0) {height = d.height==Integer.MAX_VALUE||d.height==Integer.MIN_VALUE||d.height==0 ? d.height : (d.height+BUTTON_GAP_V);}
        if(width==0) {width = d.width==Integer.MAX_VALUE||d.width==Integer.MIN_VALUE||d.width==0 ? d.width : (d.width+BUTTON_GAP_H);}
        switch(sizePolicy) {
            case SIZE_BY_HEIGHT : width = calculerLargeur(height); break;
            case SIZE_BY_WIDTH : height = calculerHauteur(width); break;
            default: width = bouton.getMinimumSize().width+BUTTON_GAP_H; height = bouton.getMinimumSize().height+BUTTON_GAP_V; break;
        }
        return new Dimension(width, height);
    }
    
    /** Selectionne ce bouton **/
    public void setSelected(boolean b) {
        if (bouton != null) {
            bouton.setSelected(b);
        }
    }

    /** Détermine si ce bouton est sélectionnée ou non **/
    public boolean isSelected() {
        if (bouton != null) {
            return bouton.isSelected();
        }
        return false;
    }
    
    private void setIconsFromAction(Action a) {
        setIconFromAction(a);
        setRolloverIconFromAction(a);
        setSelectedIconFromAction(a);
        setAcceleratorFromAction(a);
        setDrawBackgroundFromAction(a);
    }

    public Action getAction() {return bouton==null ? null : bouton.getAction();}
    
    /**
     * Affecte une action au bouton en gérant au passage le rollover, la selection et l'affichage de l'arrière-plan
     * @param a l'action à affecter
     * @see ActionComplete
     */
    public final void setAction(Action a) {
        if (bouton == null) { return; }
        
        bouton.setAction(a);
        setIconsFromAction(a);
        
        if(a.getValue(Action.NAME)!=null && a.getValue(Action.LARGE_ICON_KEY)!=null) {//dans le cas general on n affiche pas le nom ET l icone
            setHideActionText(true);
        }
        
        if(a.getValue(Action.LARGE_ICON_KEY)!=null) {
            Icon icon = (Icon) a.getValue(Action.LARGE_ICON_KEY);
            initialWidth = icon.getIconWidth();
            initialHeight = icon.getIconHeight();
        } else {
            initialWidth = initialHeight = 0;
        }
        revalidate();
        repaint();
    }

    /** charge l'icone du bouton depuis l'action **/
    protected void setIconFromAction(Action a) {
        Icon icon = null;
        if (a != null) {
            icon = (Icon)a.getValue(Action.LARGE_ICON_KEY);
            if (icon == null) {
                icon = (Icon)a.getValue(Action.SMALL_ICON);
            }
        }
        bouton.setIcon(icon);
    }
    
    /** permet de gérer le rollover via l'action **/
    protected void setRolloverIconFromAction(Action a) {
        Icon icone = (Icon) a.getValue(ActionComplete.ROLLOVER_ICON);
        if (icone != null) {
            bouton.setRolloverEnabled(true);
            bouton.setRolloverIcon(icone);
        }
    }

    /** permet de gérer la selection via l'action **/
    protected void setSelectedIconFromAction(Action a) {
        Icon icone = (Icon) a.getValue(ActionComplete.SELECTED_ICON);
        if (icone != null) {
            bouton.setSelectedIcon(icone);
        }
    }

    void setAcceleratorFromAction(Action a) {
        if(a.getValue(Action.ACCELERATOR_KEY)!=null) {
            Action action = (a instanceof ActionComplete.Toggle) ? new ToggleBindingAction((ActionComplete.Toggle) a) : a;
            bouton.registerKeyboardAction(action, (KeyStroke)a.getValue(Action.ACCELERATOR_KEY), WHEN_IN_FOCUSED_WINDOW);
//            bouton.getInputMap(WHEN_IN_FOCUSED_WINDOW).put((KeyStroke)a.getValue(Action.ACCELERATOR_KEY), "action");
//            bouton.getActionMap().put("action",a);
        }
    }

    /** permet de gérer l'affichage ou non de l'arrière-plan via l'action **/
    void setDrawBackgroundFromAction(Action a) {
        Boolean b = (Boolean) a.getValue(ActionComplete.DRAW_BACKGROUND);
        if(b!=null) { bouton.setContentAreaFilled(b); }
    }

    /**
     * Retourne la position du tooltip dans les coordonnées système du composant
     * qui reçoit le
     * <code>MouseEvent</code>. Si on retourne null, Swing choisira une position
     * par défaut. Ici, corrige la position du tooltip pour les boutons dans la
     * barre du bas, car le tooltips par défaut limite l'accès au bouton pour
     * cliquer dessus.
     *
     * @param event le <code>MouseEvent</code> qui entraine le
     * <code>ToolTipManager</code> à afficher le tooltip
     * @return la nouvelle position du tooltip pour les composants qui sont trop
     * en bas de l'écran; null sinon.
     */
    private Point positionnerTooltips(MouseEvent event) {
        AbstractButton button = null;
        if (event.getSource() instanceof AbstractButton) {
            button = ((AbstractButton) event.getSource());
        }
        if (button != null) {
            double screenBottomInsets = Toolkit.getDefaultToolkit().getScreenInsets(button.getGraphicsConfiguration()).bottom;
            double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
            double bottomButton = button.getLocationOnScreen().getY() + button.getHeight();
            if (screenHeight - screenBottomInsets - bottomButton < 20) {
                return new Point(0, -55);
            }
            return new Point(0, button.getHeight() + 15);
        }
        return null;
    }

    /**
     * Permet d'imposer que le bouton n'affiche pas le texte définit par l'action.
     * @param b true pour masquer le texte et ne garder que l'icone.
     */
    public final void setHideActionText(boolean b) {
        getButtonComponent().setHideActionText(b);
    }
    
    
    private static final int H_GAP = 2;
    private static final int V_GAP = 2;
    private static final int STROKE_SIZE = 2;
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(bouton.isSelected() && !bouton.isContentAreaFilled()) {
            Dimension arcs = new Dimension(15,15);
            int width = getWidth();
            int height = getHeight();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setStroke(new BasicStroke(STROKE_SIZE));
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            //Draws the rounded opaque panel with borders.
            graphics.setColor(getForeground());
            graphics.drawRoundRect(H_GAP, V_GAP, width-2*H_GAP-1, height-2*V_GAP-1, arcs.width, arcs.height);//paint border
            Container parent = getParent();
            while(parent!=null) {
                if(parent.isOpaque()) {
                    graphics.setColor(parent.getBackground());
                    graphics.fillRoundRect(H_GAP+STROKE_SIZE, V_GAP+STROKE_SIZE, width-1-2*(H_GAP+STROKE_SIZE), height-1-2*(V_GAP+STROKE_SIZE), arcs.width, arcs.height);//paint background
                    break;
                } else {
                    parent = parent.getParent();
                }
            }
        }
    }
    
    /**
     * Cette classe vient corriger un bug : lors de l'utilisation d'un raccourci
     * sur une action, l'état de celle-ci n'est pas modifié. On Corrige donc le
     * problème en modifiant l'état manuellement
     */
    private static class ToggleBindingAction extends AbstractAction {
        private final ActionComplete.Toggle toggle;
        private ToggleBindingAction(ActionComplete.Toggle toggle) {
            this.toggle = toggle;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            toggle.setSelected(!toggle.isSelected());
            toggle.actionPerformed(e);
        }
    }
}
