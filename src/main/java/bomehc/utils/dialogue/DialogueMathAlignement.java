/** «Copyright 2014 Guillaume Varoquaux»
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

package bomehc.utils.dialogue;

import bomehc.utils.managers.Traducteur;

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

import net.sourceforge.jeuclid.swing.JMathComponent;
import bomehc.utils.texte.JMathTextPane;
import bomehc.utils.texte.MathTools;
import bomehc.IHM;

/**
 *
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class DialogueMathAlignement extends JDialog implements ActionListener, ChangeListener{

    private float initialAlignment;
    private float finalAlignment;
    
    private JLabel valeurChoisie;
    private JMathTextPane parent;
    private JMathComponent math;
    private static final int SLIDER_MIN = 0;
    private static final int SLIDER_MAX = 100;
    
    public DialogueMathAlignement(JMathTextPane parent,JMathComponent math){
        super(IHM.getMainWindow(),Traducteur.traduire("dialog alignment"));
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setModal(true);
        
        this.parent = parent;
        this.math = math;
        this.initialAlignment = math.getAlignmentY();
        this.finalAlignment = initialAlignment;

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                DialogueMathAlignement.this.math.setAlignmentY(initialAlignment);
                DialogueMathAlignement.this.setVisible(false);
                DialogueMathAlignement.this.dispose();
            }
        });
        setSize(300, 150);
        setResizable(false);
        setLocation((int) math.getLocationOnScreen().getX() + (int) math.getPreferredSize().getWidth()+50,(int) math.getLocationOnScreen().getY()-100);
        setAlwaysOnTop(true);

        Container conteneur = getContentPane();
        conteneur.setLayout(new BorderLayout());
        int alignInitial = (int) (100*math.getAlignmentY());
        JLabel label = new JLabel("Ancien alignement : "+ alignInitial);
        label.setHorizontalAlignment(JLabel.CENTER);
        conteneur.add(label,BorderLayout.NORTH);
        JButton OK = new JButton(Traducteur.traduire("ok"));
        conteneur.add(OK,BorderLayout.SOUTH);
        OK.setActionCommand("OK");
        OK.addActionListener(this);

        JSlider slider = new JSlider(SLIDER_MIN,SLIDER_MAX, alignInitial);
        slider.setSize(100, 50);
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(5);
        slider.addChangeListener(this);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        
        valeurChoisie = new JLabel(Integer.toString(alignInitial));
        JPanel panel = new JPanel();
        slider.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    DialogueMathAlignement.this.setVisible(false);
                    DialogueMathAlignement.this.dispose();
                }
            }
        });
        panel.add(slider);
        panel.add(valeurChoisie);

        getContentPane().add(panel,BorderLayout.CENTER);

        int position = this.parent.getMathPosition(math);
        this.parent.setCaretPosition(position+1); // Force la déselection s'il y en a une

        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if(initialAlignment != finalAlignment) {math.firePropertyChange(MathTools.ALIGNMENT_Y_PROPERTY, initialAlignment, finalAlignment);}
        this.setVisible(false);
        this.dispose();
    }

    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        valeurChoisie.setText(Integer.toString(source.getValue()));
        finalAlignment = ((float)source.getValue())/SLIDER_MAX;
        math.setAlignmentY(finalAlignment);
        math.revalidate();
        parent.repaint();
    }
}
