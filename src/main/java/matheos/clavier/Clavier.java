/** «Copyright 2013 François Billioud»
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

package matheos.clavier;

import matheos.IHM;
import matheos.utils.objets.DraggageSystem;
import matheos.utils.managers.FontManager;
import matheos.utils.objets.Icone;
import matheos.utils.objets.DispatchMouseToParent;
import matheos.utils.texte.JMathTextPane;
import matheos.utils.texte.MathTools;

import java.awt.Color;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML.Tag;
import matheos.utils.managers.ColorManager;

/**
 *
 * @author Francois Billioud
 */
@SuppressWarnings("serial")
public abstract class Clavier extends JDialog {

    protected BoutonClavier bouton[];
    protected PanelClavier panelClavier;
    protected static final int EPAISSEUR_BORDURE = 4;
    protected final Font POLICE = FontManager.get("font keyboard button",Font.BOLD);
    protected final Color FOREGROUND = ColorManager.get("color button foreground");
    
    /** Méthode static qui sert seulement à forcer l'instanciation de la classe Clavier afin que focusedText et focusedMathListener se mettent à jour **/
    public static void listenTextPanes() {}

    protected Clavier() {
        super(IHM.getMainWindow());
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setUndecorated(true);
        this.setFocusableWindowState(false);
        DraggageSystem.createDraggageSystem(this, getContentPane());
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if(b) {
            updateMathActionsEnabledState();
            updateStringActionsEnabledState();
        }
    }
    
    public void activerBouton(int buttonID, boolean b) {
        bouton[buttonID].setVisible(b);
    }

    protected static class PanelClavier extends JPanel {
        protected PanelClavier() {
            this.setFocusable(false);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK, EPAISSEUR_BORDURE));
            DispatchMouseToParent dispatcher = new DispatchMouseToParent();
            this.addMouseListener(dispatcher);
            this.addMouseMotionListener(dispatcher);
        }
    }
    protected class BoutonClavier extends JButton {
        protected BoutonClavier(AbstractAction action) {
            super(action);
            this.setFocusable(false);
            this.setFont(POLICE);
            this.setForeground(ColorManager.get("color button foreground"));
            DispatchMouseToParent dispatcher = new DispatchMouseToParent();
            this.addMouseListener(dispatcher);
            this.addMouseMotionListener(dispatcher);
            //this.setMargin(new Insets(0, 0, 0, 0));
            //this.setLayout(new BorderLayout());
        }

        //les boutons contenant du html ne deviennent pas gris en étant désactivé. Ceci corrige le problème
        @Override
        public void setEnabled(boolean b) {
            super.setEnabled(b);
            this.setForeground(b?FOREGROUND:Color.GRAY);
        }
/*        @Override
        public void setEnabled(boolean b) {
            boolean avant = this.isEnabled();
            super.setEnabled(b);
            if (avant != b) {
                if (this.getComponents().length != 0) {
                    JLabel icone = (JLabel) this.getComponent(0);
                    if(icone.getIcon()==null) {return;}
                    Image im = ((Icone) icone.getIcon()).getImage();
                    if (b == true) {
                        Image image = ImageTools.changeColorToOther(im, ColorManager.get("color image disabled"), ColorManager.get("color image enabled"));
                        icone.setIcon(new Icone(image));
                    } else {
                        Image image = ImageTools.changeColorToOther(im, ColorManager.get("color image enabled"), ColorManager.get("color image disabled"));
                        icone.setIcon(new Icone(image));
                    }
                    repaint();
                } else if (this.getText().contains("<html>")) {
                    String chaine = this.getText();
                    if (b == true) {
                        String contenu = this.getText().substring(chaine.indexOf("'>") + 2, chaine.indexOf("</html>"));
                        this.setText("<html>" + contenu + "</html>");
                    } else {
                        String contenu = this.getText().substring(chaine.indexOf("<html>") + 6, chaine.indexOf("</html>"));
                        this.setText("<html><font color='gray'>" + contenu + "</font></html>");
                    }
                    repaint();
                }
            }
        }*/
    }

    private static JTextComponent focusedText = null;
    protected static JTextComponent getFocusedText() {return (focusedText!=null && focusedText.isShowing()) ? focusedText : null;}
    private static List<ActionBoutonTexte> listeActionTexte = new LinkedList<>();
    //XXX on peut envisager aussi de laisser les boutons toujours enabled et d'utiliser (JTextComponent)AppContext.getAppContext().get(FOCUSED_COMPONENT);
    static {//active les boutons de texte lorsqu'un textComponent a le focus
        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener(
            new PropertyChangeListener() {
                public synchronized void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("focusOwner") /*&& (e.getNewValue() != null)*/) {
                        if(focusedText==e.getNewValue()) {return;}
//                        JTextComponent oldTextComponent = focusedText;
                        JTextComponent oldFocusedText = focusedText;
                        focusedText = (e.getNewValue() instanceof JTextComponent) ? (JTextComponent) e.getNewValue() : null;
                        if((oldFocusedText==null) != (focusedText==null)) { updateStringActionsEnabledState(); }//SSI l'un est null et pas l'autre
                    }
                }
            }
        );
    }

    private static JMathTextPane focusedMathListener = null;
    protected static JMathTextPane getFocusedMathListener() {return (focusedMathListener!=null && focusedMathListener.isShowing()) ? focusedMathListener : null;}
    private static List<ActionBoutonMathML> listeActionMathML = new LinkedList<>();
    static {//active les boutons de mathML lorsqu'un composant pouvant recevoir des JMathComponent a le focus
        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener(
            new PropertyChangeListener() {
                public synchronized void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("focusOwner") /*&& (e.getNewValue() != null)*/) {
                        if(focusedMathListener==e.getNewValue()) {return;}
                        JMathTextPane oldTextPane = focusedMathListener;
                        focusedMathListener = (e.getNewValue() instanceof JMathTextPane) ? (JMathTextPane) e.getNewValue() : null;
                        if((oldTextPane==null) != (focusedMathListener==null)) { updateMathActionsEnabledState(); }//SSI l'un est null et pas l'autre
                    }
                }
            }
        );
    }
    
    private static void updateStringActionsEnabledState() {
        SwingUtilities.invokeLater(new Runnable() {//nouveau thread pour ne pas perturber l'utilisateur
            @Override
            public void run() {
                for(ActionBoutonTexte action : listeActionTexte) {
                    if(action!=null) action.setEnabled(focusedText!=null && focusedText.isShowing());
                }
            }
        });
    }
    private static void updateMathActionsEnabledState() {
        SwingUtilities.invokeLater(new Runnable() {//nouveau thread pour ne pas perturber l'utilisateur
            @Override
            public void run() {
                for(ActionBoutonMathML action : listeActionMathML) {
                    if(action!=null) action.setEnabled(focusedMathListener!=null && focusedMathListener.isShowing());
                }
            }
        });
    }
    
    /** Action qui insert un texte brut dans le textComponent qui a le focus **/
    protected static class ActionBoutonTexte extends AbstractAction {
        protected ActionBoutonTexte(String name) {
            super(name);
            listeActionTexte.add(this);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            //XXX revoir cette méthode en cas de problème de style
            if(focusedText!=null) {
                focusedText.replaceSelection((String) this.getValue(Action.NAME));
            }
        }

    }
    /** Action qui s'applique au JMathTextPane qui a le focus **/
    protected static abstract class ActionBoutonMathML extends AbstractAction {
        protected ActionBoutonMathML(String name) {
            super(name);
            listeActionMathML.add(this);
        }
        protected ActionBoutonMathML(Icone icone) {
            super(null,icone);
            listeActionMathML.add(this);
        }
    }
    /** Action qui insert un texte mathML dans le JMathTextPane qui a le focus **/
    protected static class ActionBoutonMathMLString extends ActionBoutonMathML {
        String mathML;
        protected ActionBoutonMathMLString(String apparence,String mathML) {
            super(apparence);
            this.mathML = mathML;
        }
        public void actionPerformed(ActionEvent e) {
            focusedMathListener.insererJMathComponent(MathTools.creerMathComponent(mathML));
        }
    }
    /** Action qui insert un texte html dans le JMathTextPane qui a le focus **/
    protected static class ActionBoutonHTMLString extends ActionBoutonMathML {
        String html;
        protected ActionBoutonHTMLString(String html) {
            super(html);
            this.html = html.replaceAll("<html>", "<span>").replaceAll("</html>", "</span>");
        }
        public void actionPerformed(ActionEvent e) {
            try {
                focusedMathListener.insererHTML(html, focusedMathListener.getCaretPosition(), Tag.SPAN);
            } catch (BadLocationException | IOException ex) {
                Logger.getLogger(Clavier.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
