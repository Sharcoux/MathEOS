/** «Copyright 2013,2014 François Billioud»
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

package matheos.utils.dialogue.math;

import java.awt.BasicStroke;
import matheos.IHM;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.Bouton;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.FontManager;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;

import matheos.utils.objets.Navigation;
import matheos.utils.managers.Traducteur;
import matheos.utils.texte.EditeurIO;
import matheos.utils.texte.EditeurKit;
import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.texte.JMathTextPane;
import matheos.utils.texte.MathTools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.StyleConstants;
import javax.swing.undo.UndoableEdit;
import matheos.utils.objets.maps.BidiMap;
import net.sourceforge.jeuclid.swing.JMathComponent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public abstract class DialogueMath extends JDialog {
    //TODO : Réfléchir à un parent commun entre DialogueMath et DialogueComplet
    //ou plutôt à un moyen de rendre le dialoguemath bloquant, à l'instar de JOptionPane.showInputDialog qui renvoie une String
    public final Font POLICE = FontManager.get("font dialog math");
    
    private static final Color defaultColor = Color.BLACK;//Cette couleur ne sera pas marqué dans le html

    /** Le JMathTextPane qui recevra l'objet créé à partir de cette boîte de dialogue **/
    private final JMathTextPane texteParent;
    private final List<DialogueMathListener> listenerList = new LinkedList<>();
    protected final Map<String, JLimitedMathTextPane> champs = new HashMap<>();
    protected final EditeurKit editeurKit = new EditeurKit();
    protected final Navigation navigation = new Navigation();
    
    public DialogueMath(String title, final JMathTextPane texteParent) {
//        super(SwingUtilities.getWindowAncestor(texteParent), Traducteur.traduire(title));
        super(SwingUtilities.getWindowAncestor(texteParent)==null ? IHM.getMainWindow() : SwingUtilities.getWindowAncestor(texteParent), Traducteur.traduire(title));
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.texteParent = texteParent;
//        setModal(true);

        //On ferme cette boîte de dialogue si une fenêtre parente est fermée
//        windowParent = texteParent==null ? null : SwingUtilities.getWindowAncestor(texteParent);

        //On donne le focus aux enfants s'il y en a, ou au premier textPane sinon.
        addWindowFocusListener(new WindowMathListener());
        //On rend le focus aux parents en cas de fermeture de la fenetre
        addWindowListener(new WindowMathListener());
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(new SidePane(), BorderLayout.NORTH);
        contentPane.add(new ButtonPane(), BorderLayout.SOUTH);
        JPanel panel = getCenterPane();
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //HACK : on doit redéfinir la position parce que sinon, pour une raison inconnue la fenêtre se déplace...
                Point p = getPreferredLocation();
                Dimension d = getPreferredSize();
                setBounds(p.x, p.y, d.width, d.height);
            }
        });
        contentPane.add(panel, BorderLayout.CENTER);
        setContentPane(contentPane);

        //On place la boîte de dialogue près du textPane auquel elle se rapporte
        setLocation(getPreferredLocation());
        
        contentPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        contentPane.getActionMap().put("close", new ActionCancel());
        
        pack();
        setResizable(false);
        setVisible(true);
    }
    
    private Point getPreferredLocation() {
        Point p = texteParent.getLocationOnScreen();
        Dimension d = getPreferredSize();
        int x, y;
        int xDroite = p.x+texteParent.getWidth()+10, xGauche = p.x-d.width-10;
        int yBas = p.y, yHaut = p.y-d.height;
        x = xDroite+d.width>Toolkit.getDefaultToolkit().getScreenSize().width ? xGauche : xDroite;
        y = yBas+d.height>Toolkit.getDefaultToolkit().getScreenSize().height ? yHaut : yBas;
        return new Point(x,y);
    }
    
    private class WindowMathListener extends WindowAdapter {
        @Override
        public void windowGainedFocus(WindowEvent e) {
            Window[] owned = DialogueMath.this.getOwnedWindows();
            if(owned.length==0) {champs.values().iterator().next().requestFocus();}
            else {for(Window dialog : owned) {if(dialog instanceof DialogueMath) {
                dialog.requestFocus();
                return;
            }}}
        }
        @Override
        public void windowClosing(WindowEvent e) {
            if(getOwner()!=null) getOwner().requestFocus();
            if(texteParent!=null) texteParent.requestFocusInWindow();
        }
    }
    
    public Map<String, String> getInputs() {
        Map<String, String> result = new HashMap<>();
        for(Entry<String, JLimitedMathTextPane> e : champs.entrySet()) {
            JMathTextPane text = e.getValue();
            result.put(e.getKey(), EditeurIO.export2htmlMathML(text, 0, text.getLength()));
        }
        return result;
    }
    
    private class ActionOK extends ActionComplete {
        public ActionOK() {super("ok");}
        @Override
        public void actionPerformed(ActionEvent e) {
            okAction();
        }
    }
    
    /** Action exécutée lors du clic sur ok **/
    protected void okAction() {
        if(verifications()) {
            fireAnswer();
            dispose();
        }
    }
    
    /** envoie la chaine générée par "createMathMLString" aux listeners de ce dialogue **/
    protected void fireAnswer() {
        for (DialogueMathListener l : listenerList) {
            System.out.println("created : "+createMathMLString());
            l.handleMathString(createMathMLString());
        }
    }
    
    /** renvoie vrai si les champs sont remplis correctement **/
    protected boolean verifications() {
        for(JMathTextPane textMath : champs.values()) {
            if(textMath.isEmpty()) {
                JOptionPane.showConfirmDialog(textMath, Traducteur.traduire("not empty"), Traducteur.traduire("error"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
                textMath.setBorder(BorderFactory.createLineBorder(Color.red));
                textMath.requestFocusInWindow();
                textMath.repaint();
                return false;
            }
        }
        return true;
    }
    
    private class ActionCancel extends ActionComplete {
        public ActionCancel() {super("cancel");}
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }
    
    protected class JMathTextField extends JLimitedMathTextPane {
        protected JMathTextField(String id) {
            super(1, true);
            champs.put(id, this);
            navigation.addComponent(this);
            this.setEditeurKit(DialogueMath.this.editeurKit);

        //        this.setSize(new Dimension(80, 30));
//            this.setBackground(ColorManager.get("color math fields"));
            this.setOpaque(true);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.setFont(POLICE);
            this.setAlignmentCenter(true);
        }
        
    }
    
    private class SidePane extends JPanel {
        SidePane() {
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            
            add(Box.createHorizontalGlue());
            
            JComboBox colors = editeurKit.getMenuCouleur();
            colors.setMaximumSize(colors.getPreferredSize());
            add(colors);
            
//            add(Box.createVerticalGlue());
        }
    }
    private class ButtonPane extends JPanel {
        ButtonPane() {
            setLayout(new BorderLayout());
            Bouton ok = new Bouton(new ActionOK());
            addButton(ok);
            add(ok, BorderLayout.CENTER);
//            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
//            add(Box.createHorizontalGlue());
//            addButton(new Bouton(new ActionOK()));
//            add(Box.createHorizontalStrut(20));
//            addButton(new Bouton(new ActionCancel()));
//            add(Box.createHorizontalGlue());
//            setBorder(BorderFactory.createEmptyBorder(20, 20, 5, 20));
        }
        
        private void addButton(Bouton bouton) {
            navigation.addComponent(bouton);
//            bouton.setMaximumSize(new Dimension(250, 30));
            bouton.setMaximumSize(new Dimension(100, 30));
//            bouton.setFont(POLICE);
            bouton.setFocusable(true);
            add(bouton);
        }
            
    }
    
    public JMathTextPane getTexteParent() {
        return texteParent;
    }

    public JMathTextPane getInput(String id) {
        return champs.get(id);
    }
    
    public final void setInputInitialValue(String inputID, String htmlContent) {
        System.out.println("contenu récupéré : "+htmlContent);
        String initialValue = readMathMLString(htmlContent);
        EditeurIO.importHtmlMathML(champs.get(inputID), initialValue, 0);
        champs.get(inputID).setAlignmentCenter(true);
    }

    protected abstract String getMasterTag();
    protected abstract String createMathMLString();
    protected abstract JPanel getCenterPane();
    
    /** Analyse le contenu d'un champs JMathTextField pour le transformer en mathML à utiliser dans un "mrow" **/
    protected String writeMathMLString(JMathTextPane text) {
        String mathML = "";
        char[] textBrut = text.getText().toCharArray();
//        boolean componentFlag = false;
        Color alphaNumericFlag = null;//On change de flag à chaque changement de couleur
        for(int i = 0; i<text.getLength(); i++) {
            Color characterColor = StyleConstants.getForeground(text.getHTMLdoc().getCharacterElement(i).getAttributes());
            if(text.isMathComponentPosition(i)) {//si c'est un component
                if(alphaNumericFlag!=null) {//on ferme la balise mn si ouverte
                    mathML+="</mn>";
                    alphaNumericFlag = null;
                }
                Element mathComp = Jsoup.parse(MathTools.getHTMLRepresentation(text.getMathComponent(i))).select("math").first();
                Color fg = text.getMathComponent(i).getForeground();
                if(!defaultColor.equals(fg)) {
                    mathComp.child(0).attr("color",ColorManager.getRGBHexa(fg));
                }
                mathML+=mathComp.html();
            } else {//si on lit une lettre
                if(alphaNumericFlag==null) {//première d'une série, on ouvre la balise mn
                    mathML+="<mn "+(characterColor.equals(defaultColor) ? "" : ("color='"+ColorManager.getRGBHexa(characterColor)+"'"))+" >";
                    alphaNumericFlag=characterColor;
                } else {
                    if(alphaNumericFlag!=characterColor) {//si on change de couleur, on ferme la balise précédente et on ouvre une autre
                        mathML+="</mn><mn "+(characterColor.equals(defaultColor) ? "" : ("color='"+ColorManager.getRGBHexa(characterColor)+"'"))+" >";
                        alphaNumericFlag=characterColor;
                    }
                }
                    
                mathML+=textBrut[i];
//                componentFlag = false;
            }
        }
        if(alphaNumericFlag!=null) {mathML+="</mn>";}//ferme la dernière balise
        
        return mathML;
    }
    
    /** Prend une chaine "mrow" de mathML et la transforme en une chaine html composée de texte et d'éléments mathML prête à insérer dans un JMathTextField **/
    protected String readMathMLString(String mathPart) {
        Element body = Jsoup.parse(mathPart).outputSettings(new Document.OutputSettings().prettyPrint(false)).body();
        if(body.children().isEmpty()) {return "";}
        Element masterElement = body.child(0);
        int nbChilds = masterElement.children().size();
        for(int i = 0; i<nbChilds; i++) {//Impossible d'utiliser le foreach : ConcurrentModification
            Element n = masterElement.child(i);
            if(n.nodeName().equals("mn") || n.nodeName().equals("mo") || n.nodeName().equals("font")) {
                n.html(n.html().trim());
                if(n.nodeName().equals("font")) {//des mathComponent peuvent se cacher dans la balise font
                    for(Element child : n.children()) {if(child.tagName().startsWith("m")) {child.wrap("<math></math>");}}
                } else {
                    n.tagName("font");
                }
            } else {
                if(n.hasAttr("color")) {
                    n.wrap("<math color='"+n.attr("color")+"'></math>");
                } else {
                    n.wrap("<math></math>");
                }
            }
        }
        System.out.println("formated : "+masterElement.outerHtml());
//        return masterElement.tagName("p").outerHtml();
        return masterElement.html();
    }
    
    public void addDialogueMathListener(DialogueMathListener d) {
        listenerList.add(d);
    }

    public void removeDialogueMathListener(DialogueMathListener l) {
        listenerList.remove(l);
    }

    public interface DialogueMathListener {
        public void handleMathString(String answer);
    }
    
    //écoute la boite de dialogue lors de la création d'un mathComponent
    public class CreateListener implements DialogueMath.DialogueMathListener {
        private final JMathTextPane textPane;
        public CreateListener(JMathTextPane textPane) {
            this.textPane = textPane;
        }

        @Override
        public void handleMathString(String answer) {
            textPane.insererJMathComponent(MathTools.creerJMathComponentFromHTML(answer));
        }
    }
    
    //écoute la boite de dialogue lors de la modification d'un mathComponent
    public class EditListener implements DialogueMath.DialogueMathListener {

        private final JMathComponent component;
        public EditListener(JMathComponent comp) {
            component = comp;
        }
        
        @Override
        public void handleMathString(String answer) {
            String oldContent = MathTools.getHTMLRepresentation(component);
            String toInsert = Jsoup.parse(answer).select(getMasterTag()).first().html();
            Document componentDocument = Jsoup.parse(oldContent);
            componentDocument.select(getMasterTag()).first().html(toInsert);
            
            String newContent = componentDocument.select("math").first().outerHtml();
            MathTools.setContent(component, newContent);
            UndoableEdit edit = new MathTools.MathEdit(component, oldContent, newContent);
            texteParent.getUndo().validateAndAddEdit(edit);//TODO éviter cette façon de faire
        }
    
    }
    
    static abstract class MathLayout implements LayoutManager {
        private static final int MIN_WIDTH = 300, MIN_HEIGHT = 100;
        protected static final int MARGIN = 20;
        
        private final BidiMap<String, Component> components = new BidiMap<>();
        
        //On redessine les lignes en cas de changement de taille de l'un des éléments (racine, fractions, etc)
        private static final ComponentListener resizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if(e.getComponent()!=null && e.getComponent().getParent()!=null) {
                    e.getComponent().getParent().repaint();
                }
            }
        };
        
        @Override
        public void addLayoutComponent(String name, Component comp) {components.put(name, comp);comp.addComponentListener(resizeListener);}
        @Override
        public void removeLayoutComponent(Component comp) {components.removeValue(comp);comp.removeComponentListener(resizeListener);}
        protected Component getComponent(String name) {return components.get(name);}
        @Override
        public Dimension minimumLayoutSize(Container parent) {return new Dimension(MIN_WIDTH,MIN_HEIGHT);}
    }
    
    abstract class MathPanel extends JPanel {
        private static final int EPAISSEUR = 2;//épaisseur des traits
        
        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            super.paintComponent(g); // Redessine le Panel avant d'ajouter les composants
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Stroke old = g2d.getStroke();
            g2d.setStroke(new BasicStroke(EPAISSEUR));
            
            dessiner(g2d);

            g2d.setStroke(old);
        }
        protected abstract void dessiner(Graphics2D g2D);
    }
}
