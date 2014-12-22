/**
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of MathEOS
 *
 * MathEOS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Additional Terms according section 7 of GNU/GPL V3 :
 *
 * These additional terms refer to the source code of bomehc.
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
 *
 **/

package matheos.utils.objets;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import matheos.IHM;
import matheos.sauvegarde.DataTexte;
import matheos.texte.Editeur;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.managers.CursorManager;
import matheos.utils.managers.ImageManager;

/**
 *
 * @author François Billioud
 */
public class DataTexteDisplayer extends JDialog {
    
    private final Editeur editeur;
    public Editeur getEditeur() {return editeur;}

    public DataTexteDisplayer(DataTexte data, String title) {
        this(data);
        setTitle(title);
    }
    public DataTexteDisplayer(DataTexte data) {
        super(IHM.getMainWindow(), ModalityType.MODELESS);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        //chargement du chapitre
        editeur = new Editeur();
        editeur.charger(data);
        editeur.setEditable(false);
        editeur.setCursor(CursorManager.getCursor(Cursor.TEXT_CURSOR));
        editeur.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)) {
                    if(listActionsClicDroit.isEmpty()) {return;}
                    MenuContextuel menu = new MenuContextuel(listActionsClicDroit, e);
                }
            }
        });
        
        editeur.getActionMap().put("undo", new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {editeur.getUndo().undo();}});
        editeur.getActionMap().put("redo", new AbstractAction() {@Override public void actionPerformed(ActionEvent e) {editeur.getUndo().redo();}});
        editeur.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo");
        editeur.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redo");

        //conteneur ScrollPane
        JScrollPane container = new JScrollPane(editeur, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        container.getVerticalScrollBar().setUnitIncrement(100);

        //Fenetre d'affichage
        setIconImage(ImageManager.getIcone("applicationIcon").getImage());
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(d.width/2, 20, d.width/2, d.height-40);
        setContentPane(container);
        setAlwaysOnTop(true);
    }
    
    public void setEditable(boolean b) {editeur.setEditable(b);}
    public boolean isEditable() {return editeur.isEditable();}
    
    private List<Action> listActionsClicDroit = new LinkedList<>();
    public void addActionClicDroit(Action a) {
        listActionsClicDroit.add(a);
    }
    
    public static void display(DataTexte data, String title) {
        DataTexteDisplayer displayer = new DataTexteDisplayer(data, title);
        displayer.setVisible(true);
    }
    public static void display(DataTexte data) {
        DataTexteDisplayer displayer = new DataTexteDisplayer(data);
        displayer.setVisible(true);
    }
    
    public abstract class ActionClicDroit extends ActionComplete {
        protected Editeur editeur = DataTexteDisplayer.this.editeur;
        public ActionClicDroit(String aspect) {super(aspect);}
    }

}
