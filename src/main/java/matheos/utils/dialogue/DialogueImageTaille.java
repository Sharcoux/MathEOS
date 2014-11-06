/** «Copyright 2014 Guillaume Varoquaux»
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
package matheos.utils.dialogue;

import matheos.IHM;
import matheos.texte.Editeur;
import matheos.texte.composants.JLabelImage;
import matheos.utils.managers.Traducteur;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import matheos.texte.composants.ComposantTexte;

/**
 *
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class DialogueImageTaille extends JDialog implements ActionListener, ChangeListener {

    private final JButton OK;
    private final JLabel valeurInitiale;
    private final Editeur editeur;
    private ComposantTexte.Image labelImage;
    private int largeurInitiale;
    private int largeurFinale;
    private static final int SLIDER_MIN = 10;
    private static final int SLIDER_MAX = 100;

    public DialogueImageTaille(Editeur editeur, ComposantTexte.Image labelImage) {
        super(IHM.getMainWindow());
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setModal(true);
        this.labelImage = labelImage;
        this.editeur = editeur;
        this.largeurInitiale = labelImage.getWidth();
        this.setTitle(Traducteur.traduire("image dimensions title"));

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosing(e);
                DialogueImageTaille.this.labelImage.setSize(largeurInitiale);
            }
        });
        setSize(300, 150);
        setResizable(false);
        setLocation(Math.max((int) labelImage.getLocationOnScreen().getX() + (int) labelImage.getPreferredSize().getWidth() + 50,0), Math.max((int) labelImage.getLocationOnScreen().getY() - 100,0));
        setAlwaysOnTop(true);

        Container conteneur = getContentPane();
        conteneur.setLayout(new BorderLayout());
        int valeurInitial = (int) Math.max(Math.round(SLIDER_MAX * labelImage.getWidth()/ (double)labelImage.getLargeurMax()), SLIDER_MIN);
        JLabel label = new JLabel(Traducteur.traduire("image dimensions old") + " : " + valeurInitial);
        label.setHorizontalAlignment(JLabel.CENTER);
        conteneur.add(label, BorderLayout.NORTH);
        OK = new JButton(Traducteur.traduire("ok"));
        conteneur.add(OK, BorderLayout.SOUTH);
        OK.setActionCommand("OK");
        OK.addActionListener(this);

        JSlider slider = new JSlider(SLIDER_MIN, SLIDER_MAX, valeurInitial);
        slider.setSize(100, 50);
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(5);
        slider.addChangeListener(this);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        valeurInitiale = new JLabel(Integer.toString(valeurInitial));
        JPanel panel = new JPanel();
        slider.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    DialogueImageTaille.this.setVisible(false);
                    DialogueImageTaille.this.dispose();
                }
            }
        });
        panel.add(slider);
        panel.add(valeurInitiale);

        getContentPane().add(panel, BorderLayout.CENTER);

        this.editeur.setCaretPosition(editeur.getSelectionEnd()); // Force la déselection s'il y en a une

        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (largeurInitiale != largeurFinale) {
            labelImage.firePropertyChange(JLabelImage.SIZE_PROPERTY, largeurInitiale, largeurFinale);
            largeurInitiale = largeurFinale;
        }
        this.setVisible(false);
        this.dispose();
    }

    public void stateChanged(ChangeEvent e) {
        //On attends de vérifier que le slider est imobilisé
        JSlider source = (JSlider) e.getSource();
        valeurInitiale.setText(Integer.toString(source.getValue()));
        largeurFinale = (int) Math.round(((double) source.getValue() / SLIDER_MAX) * labelImage.getLargeurMax());
        labelImage.setSize(largeurFinale);
//        labelImage.repaint();
        editeur.repaint();
    }
}
