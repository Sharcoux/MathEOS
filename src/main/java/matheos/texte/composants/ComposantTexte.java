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

package matheos.texte.composants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import matheos.Configuration;
import matheos.IHM;
import matheos.utils.librairies.JsoupTools;
import matheos.utils.managers.Traducteur;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.DOMImplementation;

/**
 *
 * @author François Billioud
 */
public interface ComposantTexte {
    public static final String REMOVABLE_PROPERTY = "removable";
    public static enum SVG_RENDERING {SVG, EMBED_SVG, PNG};
    
    /**
     * Renvoie la représentation HTML du composant
     * @param svgAllowed Indique si le contenu renvoyé peut contenir du svg
     * @param mathMLAllowed Indique si le contenu renvoyé peut contenir du MathML
     * @return la traduction du composant sous forme de chaîne HTML
     */
    public String getHTMLRepresentation(SVG_RENDERING svgAllowed, boolean mathMLAllowed);
    
    /**
     * Renvoie l'identifiant du composant. Il sert d'attribut ID dans la balise la plus large
     * du HTML renvoyé par getHTMLRepresentation()
     * @return un entier long permettant d'identifier le composant.
     */
    public long getId();
    
    /**
     * Permet de spécifier manuellement l'id d'un composant texte. Utile notamment lors du chargement
     * @param id l'id du composant
     */
    public void setId(long id);
    
    /**
     * Indique si le composant est sélectionné ou déselectionné
     */
    public boolean isSelected();

    /**
     * Modifie le composant pour qu'il prenne un aspect "sélectionné" ou "désélectionné"
     */
    public void setSelected(boolean b);
    
    /**
     * Défini la couleur à utiliser pour sélectionner le composant
     * @param selectionColor la couleur de sélection
     */
    public void setSelectionColor(Color selectionColor);
    /**
     * Appelé lors de la désactivation de l'onglet Texte,
     * Modifie le composant pour lui donner un aspect désactivé.
     */
//    public void desactiver();
    
    
    /**
     * Définit la couleur d'avant-plan du composant
     * @param foreground couleur d'avant-plan
     */
    public void setForeground(Color foreground);
    
    /**
     * Renvoie la couleur d'avant-plan du composant
     * @return La couleur d'avant-plan
     */
    public Color getForeground();
    
    /**
     * Définit la couleur d'arrière plan du composant
     * @param background couleur d'arrière-plan
     */
    public void setBackground(Color background);
    
    /**
     * Renvoie la couleur d'arrière plan du composant
     * @return la couleur d'arrière-plan
     */
    public Color getBackground();
    
    /**
     * Définit la font-size à appliquer au composant.
     * Ceci permet le fonctionnement des boutons "zoom"
     * @param size la nouvelle taille de référence
     */
    public void setFontSize(float size);
    
    /**
     * Récupère le font-size à appliquer au composant.
     * Ce font-size sert de taille de référence pour la taille du composant.
     * @return Le font-size de référence
     */
    public float getFontSize();
    
    /**
     * Définit l'état barré ou non du composant
     * @param b true si barré, false sinon
     */
    public void setStroken(boolean b);
    
    /**
     * Indique si le composant est barré ou non
     * @return true si le composant est barré, non sinon
     */
    public boolean isStroken();
    
    /**
     * Définit la couleur qui sera utilisée pour barrer le composant
     * @param c la couleur en question
     */
    public void setStrikeColor(Color c);
    
    /**
     * Renvoie la couleur utilisée pour barrer le texte
     * @return la couleur en question
     */
    public Color getStrikeColor();
    
    public Object copy();
    
    public static interface Image extends ComposantTexte {
        public void setSize(int largeur);

        public int getWidth();

        public Point getLocationOnScreen();

        public Dimension getPreferredSize();

        public void firePropertyChange(String SIZE_PROPERTY, int largeurInitiale, int largeurFinale);

        public int getLargeurMax();

        public void repaint();
        
        
        class ImageSizeEditor extends JDialog {
            private static final int MINIMUM_SCALE = 5;
            private final Image image;
            private final int largeurInitiale;
            private int currentValue;
            /** becomes true if ok action has been executed. It is used to know if the window has been manually or programmatically closed **/
            private boolean confirmed = false;
            
            private final InitialValue label;
            private final JSlider slider;
            private final JButton ok;
            private final JLabel currentValueLabel;
            
            ImageSizeEditor(Image im) {
                super(IHM.getMainWindow(), Traducteur.traduire("image dimensions title"), ModalityType.APPLICATION_MODAL);
                setSize(300, 150);
                setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                setResizable(false);
                setLocation((int) im.getLocationOnScreen().getX() + (int) im.getPreferredSize().getWidth() + 50, (int) im.getLocationOnScreen().getY() - 100);
                
                this.image = im;
                this.largeurInitiale = im.getWidth();
                final int initialPercentValue = currentValue = (int) Math.max(Math.round(100 * largeurInitiale/ (double)im.getLargeurMax()), MINIMUM_SCALE);
                
                setLayout(new BorderLayout());
                add(label = new InitialValue(initialPercentValue), BorderLayout.NORTH);
                add(ok = new OK(), BorderLayout.SOUTH);
                add(new CenterPane(slider = new Slider(initialPercentValue), currentValueLabel = new JLabel(initialPercentValue+"")));
                
                //Empêche le slider de  changer de taille quand la valeur passe de 99 à 100
                currentValueLabel.setPreferredSize(new Dimension(getFontMetrics(currentValueLabel.getFont()).stringWidth("100"),currentValueLabel.getHeight()));
                
                slider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        currentValue = slider.getValue();
                        currentValueLabel.setText(currentValue+"");
                        image.setSize((int) Math.round(((double) currentValue / 100) * image.getLargeurMax()));
                        image.repaint();
                    }
                });
                
                ok.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (initialPercentValue != currentValue) {
                            image.firePropertyChange(JLabelImage.SIZE_PROPERTY, initialPercentValue, currentValue);
                        }
                        ImageSizeEditor.this.dispose();
                    }
                });
                
                addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        if(!confirmed) {image.setSize(largeurInitiale);}//window manually closed
                    }
                });
                
                pack();
                setVisible(true);
            }
            
            private class InitialValue extends JLabel {
                private InitialValue(int value) {
                    super(Traducteur.traduire("image dimensions old") + " : " + value);
                    setHorizontalAlignment(JLabel.CENTER);
                }
            }
            
            private class OK extends JButton {
                private OK() {
                    super(new AbstractAction(Traducteur.traduire("ok")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (largeurInitiale != image.getWidth()) {
                                image.firePropertyChange(JLabelImage.SIZE_PROPERTY, largeurInitiale, image.getWidth());
                            }
                            confirmed = true;
                            ImageSizeEditor.this.dispose();
                        }
                    });
                    //Racourci clavier
                    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ok");
                    getActionMap().put("ok", getAction());
                }
            }
            
            private class Slider extends JSlider {
                private Slider(int value) {
                    super(MINIMUM_SCALE, 100, value);
                    setSize(200, 50);
                    setPreferredSize(getSize());
                    setMajorTickSpacing(20);
                    setMinorTickSpacing(5);
                    setPaintTicks(true);
                    setPaintLabels(true);
                }
            }
            
            private class CenterPane extends JPanel {
                private CenterPane(JSlider slider, JLabel currentValueLabel) {
                    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
                    add(Box.createHorizontalStrut(20));
                    add(slider);
                    add(Box.createHorizontalStrut(20));
                    add(currentValueLabel);
                    add(Box.createHorizontalStrut(20));
                }
            }
            
        }
    }
    
    static class Composant2ImgConvertor {
        /**
         * Convertit un ComposantTexte en image png
         * @param textComp le composant à convertir
         * @return le html sous la forme -img id=... src=....png /-
         */
        public static String getImageHtmlPng(ComposantTexte textComp) {
            Component c = (Component)textComp;
            Element img = Jsoup.parse("<img />").select("img").first();

            HashMap<String, String> styles = JsoupTools.getStyleMap(img.attr("style"));
            styles.put("width", c.getWidth()+"");
            styles.put("height", c.getHeight()+"");
            styles.put("border", "none");

            img.attr("id", textComp.getId()+"").attr("alt", "image not found");
            JsoupTools.setStyle(img, styles);

            //on enregistre l'image dans les fichiers temporaires
            BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if(textComp.isSelected()) {
                textComp.setSelected(false);
                c.paint(g);
                textComp.setSelected(true);
            } else {
                c.paint(g);
            }
            try {
                ImageIO.write(image, "PNG", new File(Configuration.getDossierTemp()+textComp.getId()+"_PNG.png"));
                img.attr("src", Configuration.getURLDossierImagesTemp()+textComp.getId()+"_PNG.png");
            } catch (IOException ex) {
                Logger.getLogger(SVGComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
            return img.outerHtml();
        }
        /**
         * Convertit un ComposantTexte en image svg
         * @param textComp le composant à convertir
         * @return le html sous la forme -svg id=... /-
         */
        public static String getImageSvg(ComposantTexte textComp) {
            Component c = (Component)textComp;
            int width = c.getSize().width, height = c.getSize().height;
            if (width == 0 || height == 0) {
                return null;
            }
            // Get a DOMImplementation.
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

            // Create an instance of org.w3c.dom.Document.
            String svgNS = "http://www.w3.org/2000/svg";
            org.w3c.dom.Document document = domImpl.createDocument(svgNS, "svg", null);

            // Create an instance of the SVG Generator.
            SVGGraphics2D g = new SVGGraphics2D(document);
            g.setSVGCanvasSize(new Dimension(width, height));

            if(textComp.isSelected()) {
                textComp.setSelected(false);
                c.paint(g);
                textComp.setSelected(true);
            } else {
                c.paint(g);
            }
            try (StringWriter w = new StringWriter()) {
                g.stream(w,true);
                Element svg = Jsoup.parse(w.toString()).outputSettings(new Document.OutputSettings().prettyPrint(false)).select("svg").first();
                svg.attr("id", textComp.getId()+"");
                return SVGComponent.correctionSvg(svg.outerHtml());
            } catch (IOException ex) {
                Logger.getLogger(ComposantTexte.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
        /**
         * Convertit un ComposantTexte en image svg incluse dans une balise img
         * @param textComp le composant à convertir
         * @return le html sous la forme -img id=... src=....svg /-
         */
        public static String getImageHtmlSvg(ComposantTexte textComp) {
            return Svg2ImgConvertor.getImageHtmlSvg(getImageSvg(textComp));
        }
    }
    
    static class Svg2ImgConvertor {
        /**
         * Enregistre un svg dans un fichier et renvoie la balise img correspondante
         * @param svgString le svg à enregistrer
         * @return le html sous la forme -img id=... src=....svg /-
         */
        public static String getImageHtmlSvg(String svgString) {
            Element img = Jsoup.parse("<img />").select("img").first();
            Element svg = Jsoup.parse(svgString).outputSettings(new Document.OutputSettings().prettyPrint(false)).select("svg").first();

            HashMap<String, String> styles = JsoupTools.getStyleMap(img.attr("style"));
            styles.put("height", JsoupTools.getStyle(svg, "height"));
            styles.put("width", JsoupTools.getStyle(svg, "width"));
            styles.put("border", "none");

            String id = JsoupTools.getStyle(svg, "id");
            img.attr("id", id).attr("src", Configuration.getURLDossierImagesTemp() + id + ".svg")
                    .attr("alt", "image not found");
            JsoupTools.setStyle(img, styles);

            //on enregistre l'image dans les fichiers temporaires
            File imageTemp = new File(Configuration.getDossierTemp() + id + ".svg");
            try (FileWriter w = new FileWriter(imageTemp)) {
                w.write("<?xml version=\"1.0\"?>" + System.lineSeparator() + System.lineSeparator() +
                        "<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.0//EN' 'http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd'>"+System.lineSeparator());
                w.write(JsoupTools.corriger(svg.outerHtml()));
                imageTemp.deleteOnExit();
            } catch (IOException ex) {
                Logger.getLogger(JLabelImage.class.getName()).log(Level.SEVERE, null, ex);
            }
            return img.outerHtml();
        }
        
    }
}
