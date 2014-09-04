/** «Copyright 2014 François Billioud»
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

package matheos.graphic.fonctions;

import matheos.utils.dialogue.DialogueBloquant;
import matheos.utils.dialogue.DialogueEvent;
import matheos.utils.dialogue.Validator;
import matheos.utils.managers.FontManager;
import matheos.utils.managers.Traducteur;
import matheos.utils.objets.Navigation;
import matheos.utils.texte.MathTools;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.sourceforge.jeuclid.swing.JMathComponent;

/**
 *
 * @author François Billioud
 */
public class SaisieCoefficients extends JPanel {
    public LinkedList<JTextField> champs = new LinkedList<JTextField>();
    private LinkedList<JMathComponent> coefs = new LinkedList<JMathComponent>();
    private LinkedList<JLabel> plus = new LinkedList<JLabel>();
    private LinkedList<Component> espaces = new LinkedList<Component>();
    private JButton boutonPlus = new BoutonPlus();
    public JTextField nom;
    private JLabel affectation;
//    private Component debut = Box.createHorizontalStrut(30);
    private Navigation navigation = null;

    public SaisieCoefficients() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setName("input");
        setFocusable(false);
        
        //coefficient b
        JTextField b = new TextField(2);
        champs.add(b);
        
        //label d'affectation : x->
        affectation = new JLabel("<html>&nbsp;:&nbsp;<i>x</i>&nbsp;&#x021a6;&nbsp;");
        affectation.setFont(FontManager.get("font dialog math"));
        
        //nom de la fonction
        nom = new TextField(2);

        add(Box.createHorizontalStrut(30));
        creerEspace();
        creerEspace();
        add(new BoutonPlus());
        add(Box.createHorizontalStrut(30));
        add(b);
        add(affectation);
        add(nom);
        creerEspace();
    }
    private void addField() {
        int coef = champs.size();
        JTextField champ = new TextField(2);
        JMathComponent comp = MathTools.creerMathComponent(coef==1 ? "<mrow><mn>x</mn></mrow>" : "<msup><mrow><mn>x</mn></mrow><mrow><mn>"+coef+"</mn></mrow></msup>");
        comp.setAlignmentY(0.5f);
        JLabel p = new JLabel(" + ");

        champs.push(champ);
        coefs.push(comp);
        plus.add(p);

        //ajoute après l'affectation
//        this.remove(debut);
        if(!espaces.isEmpty()) {remove(espaces.pop());}
        this.remove(nom);
        this.remove(affectation);
        this.add(p);
        this.add(comp);
        this.add(champ);
        this.add(affectation);
        this.add(nom);

        revalidate();
        repaint();
        Window parent = (Window) SwingUtilities.getAncestorOfClass(Window.class, this);
        parent.pack();
        champ.requestFocus();
    }
    private void removeField() {
        if(coefs.isEmpty()) {return;}
        this.remove(plus.pop());
        this.add(coefs.pop());
        this.add(champs.pop());
        revalidate();
        repaint();
    }

    public String getNom() {return nom.getText();}
    
    public LinkedList<Double> getEntries() {
        LinkedList<Double> coefficients = new LinkedList<Double>();
        for(JTextField champ : champs) {
            String text = champ.getText();
            if(text.equals("")) {
                coefficients.addFirst(0d);
            } else {
                try {coefficients.addFirst(Double.parseDouble(text));}
                catch(NumberFormatException ex) {
                    champ.setBorder(BorderFactory.createLineBorder(Color.red));
                    champ.requestFocus();
                    DialogueBloquant.error(Traducteur.traduire("error"), Traducteur.traduire("not decimal"));
                    return null;
                }
            }
        }
        return coefficients;
    }
    
    /** permet de spécifier le système de navigation à utiliser **/
    public void setNavigation(Navigation navigation) {
        this.navigation = navigation;
        for(JTextField c : champs) {
            navigation.addComponent(c);
        }
        navigation.addComponent(boutonPlus);
    }

    private void creerEspace() {
        Component c = Box.createHorizontalStrut(40);
        espaces.push(c);
        add(c);
    }
    
    private class TextField extends JTextField {
        private TextField(int i) {
            super(i);
            setFont(FontManager.get("font dialog"));
            setMaximumSize(getPreferredSize());
            if(navigation!=null) {navigation.addComponent(this);}
        }
    }
    private class BoutonPlus extends JButton {
        private BoutonPlus() {
            super(" + ");
            setMaximumSize(getPreferredSize());
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addField();
                }
            });
        }
    }
    
    public class ValidationCoefficients implements Validator {
        @Override
        public boolean validate(DialogueEvent event) {
            return getEntries()!=null;
        }
        @Override
        public boolean validationFails() {return true;}
    }
}
    