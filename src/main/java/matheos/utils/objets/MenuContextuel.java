/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package matheos.utils.objets;

import matheos.utils.boutons.ActionComplete;
import matheos.utils.managers.FontManager;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

/**
 *
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class MenuContextuel extends JPopupMenu {

    private final Font POLICE = FontManager.get("font popup menu");

    public MenuContextuel() {
        setFocusable(false);
        //this.setSize(200, 250);
        setFont(POLICE);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getActionMap().put("close", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { MenuContextuel.this.setVisible(false); }
        });
    }

    public MenuContextuel(List<Action> listeAction) {
        this();
        for (Action a : listeAction) {
            this.add(a instanceof ActionComplete.Toggle ? new JCheckBoxMenuItem(a) : new JMenuItem(a));
        }
    }

    public MenuContextuel(List<Action> listeAction, MouseEvent e) {
        this(listeAction);
        show(e.getComponent(), e.getX(), e.getY());
    }
    
    public void addJMenuItems(List<JMenuItem> items) {
        for (JMenuItem menuItem : items) {
            this.add(menuItem);
        }
    }

    public JMenuItem addCheckBox(Action a) {
        return addCheckBox(a, false);
    }

    public JMenuItem addItem(Action a) {
        return add(new JMenuItem(a));
    }

    public JCheckBoxMenuItem addCheckBox(Action a, boolean isSelected) {
        final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(a);
        menuItem.setSelected(isSelected);
        add(menuItem);
        return menuItem;
    }

    @Override
    public final JMenuItem add(JMenuItem menuItem) {
        setFont(menuItem);
        return super.add(menuItem);
    }

    private void setFont(JMenuItem menuItem) {
        menuItem.setFont(this.getFont());
    }

    @Override
    public final void setFont(Font font) {
        super.setFont(font);
        for(Component c : getComponents()){
            c.setFont(font);
        }
    }
    
    public boolean isEmpty() {return getComponentCount()==0;}
    
}
