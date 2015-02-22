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
 */

package bomehc.arevoir.inutilise;

import bomehc.utils.texte.JMathTextPane;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.JTextComponent;

/**
 *
 * @author François Billioud
 */
public abstract class FocusManager {

    /** démarre l'écoute des Components **/
    public static void active() {
        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("focusOwner") && (e.getNewValue() != null)) {
                        setLastMathListener(null);
                        setLastTextComponent(null);
                        if(e.getNewValue() instanceof JMathTextPane) { setLastMathListener((JMathTextPane) e.getNewValue()); }
                        if(e.getNewValue() instanceof JTextComponent) { setLastTextComponent((JTextComponent) e.getNewValue()); }
                    }
                }
            }
        );
    }

    private static JMathTextPane lastMathListener = null;
    private static void setLastMathListener(JMathTextPane mathInputListener) {
        lastMathListener = mathInputListener;
    }
    public static JMathTextPane getLastMathListener() {return lastMathListener;}

    private static JTextComponent lastTextComponent = null;
    private static void setLastTextComponent(JTextComponent jTextComponent) {
        lastTextComponent = jTextComponent;
    }
    public static JTextComponent getLastTextComponent() {return lastTextComponent;}
}
