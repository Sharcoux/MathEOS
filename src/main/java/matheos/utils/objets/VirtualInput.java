/**
 * Copyright (C) 2015 François Billioud
 *
 * This file is part of MathEOS
 *
 * MathEOS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Additional Terms according section 7 of GNU/GPL V3 :
 *
 * These additional terms refer to the source code of bomehc.
 *
 * According to GNU GPL v3, section 7 b) : You should mention any contributor of
 * the work as long as his/her contribution is meaningful in a covered work. If
 * you convey a source code using a part of the source code of MathEOS, you
 * should keep the original author in the resulting source code. If you
 * propagate a covered work with the same objectives as the Program (help
 * student to attend maths classes with an adapted software), you should mention
 * «Ludovic Faubourg», «Frédéric Marinoni» as author of the idea of this
 * software. In any case, if you propagate a covered work you have to mention
 * François Billioud and Guillaume Varoquaux as author of the realisation of
 * MathEOS software. The paternity of the authors have to appear in a legible,
 * unobscured manner, showing clearly their link to the covered work in any
 * document, web pages,... which describe the project or participate to the
 * distribution of the covered work.
 *
 *
 */
package matheos.utils.objets;

import java.awt.Event;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.NAME;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.Bouton;
import matheos.utils.managers.Traducteur;

/**
 * Affiche un clavier virtuel 
 * @author François Billioud
 */
public class VirtualInput extends JDialog {

    private static final String TITLE_MARK = " title";
    private static final String BUTTONS_MARK = " buttons";
    private PanelBoutons panel;
    
    public Map<String, Bouton> boutons = new HashMap<>();

    public VirtualInput(String aspect, int rowCount, int colCount) {
        this(aspect, rowCount, colCount, Traducteur.traduire(aspect+BUTTONS_MARK).split(" "), true);
    }
    public VirtualInput(String aspect, int rowCount, int colCount, String[] buttonNames) {
        this(aspect, rowCount, colCount, buttonNames, false);
    }
    private VirtualInput(String aspect, int rowCount, int colCount, String[] buttonData, boolean byAspect) {
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setSize(300, 200);
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setTitle(Traducteur.traduire(aspect+TITLE_MARK));
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        panel = new PanelBoutons(rowCount, colCount, buttonData, byAspect);
        panel.setLayout(new GridLayout(rowCount, colCount));
        setContentPane(panel);
    }

    private class PanelBoutons extends JPanel {
        private PanelBoutons(int lignes, int colonnes, String[] buttonData, boolean byAspects) {
            setLayout(new GridLayout(lignes, colonnes));
            for (String data : buttonData) {
                Bouton bouton = new Bouton(byAspects ? new ActionBouton(data,true) : new ActionBouton(data));
                add(bouton);
                boutons.put(data, bouton);
            }
        }
    }

    private class ActionBouton extends ActionComplete {

        private ActionBouton(String aspect, boolean byAspect) {
            super(aspect);
        }
        private ActionBouton(String valeur) {
            this.putValue(NAME, valeur);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(valeur));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireInput((String) getValue(NAME));
            dispose();
        }
    }

    private void fireInput(String input) {
        for (ActionListener l : list.getListeners(ActionListener.class)) {
            l.actionPerformed(new ActionEvent(this, Event.ACTION_EVENT, input));
        }
    }
    private final EventListenerList list = new EventListenerList();

    public void addActionListener(ActionListener l) {
        list.add(ActionListener.class, l);
    }

    public void removeActionListener(ActionListener l) {
        list.remove(ActionListener.class, l);
    }
}
