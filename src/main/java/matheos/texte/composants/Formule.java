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

import matheos.utils.managers.ColorManager;
import matheos.utils.librairies.JsoupTools;
import matheos.utils.managers.CursorManager;
import matheos.utils.managers.FontManager;
import matheos.utils.objets.DispatchMouseToParent;
import static matheos.utils.texte.MathTools.calculateMathAlignment;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.sourceforge.jeuclid.context.Parameter;
import net.sourceforge.jeuclid.swing.JMathComponent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author François Billioud
 */
public class Formule extends JPanel implements ComposantTexte {
    
    /** balise utilisée pour reconnaître les JMathComponent **/
    public static final String MATH_COMPONENT = "jMathComponent";

    public static final String ALIGNMENT_Y_PROPERTY = "alignementY";
    private final Font POLICE = FontManager.get("font math component");
    
    private JMathComponent mathComponent;
    private long id;
    private Color foregroundColor;
    private boolean selected = false;

    public Formule(String htmlRepresentation) {
        //identification de la chaine mathML
        Document doc = Jsoup.parse(htmlRepresentation);
        Element mathElement = doc.select("span."+MATH_COMPONENT).first();//cas d'un MathML créé par MathEOS
        if(mathElement==null) { mathElement = doc.select("math").first(); }//cas d'un MathML quelconque
        if(mathElement==null) { System.out.println("impossible de charger le mathML : "+htmlRepresentation);return;}//échec
        
        //On corrige la chaine entrée
        String chaine = corrigerInput(mathElement.html());
        
        //Lecture des attributs
        //Lit les attributs depuis le html de la chaine. Cela suppose que les attributs css doivent être transformés de cette
        //manière avant l'appel à creerFromHTML, ou alors, il faut utiliser les attributeSet lors du parsing ou qqc comme ça
        String widthAttr = JsoupTools.getStyle(mathElement, "width").replaceAll("[a-zA-Z]", "");//TODO gérer les unités au lieu de les supprimer
        String heightAttr = JsoupTools.getStyle(mathElement, "height").replaceAll("[a-zA-Z]", "");
        String fontSizeAttr = JsoupTools.getStyle(mathElement, "font-size").replaceAll("[a-zA-Z]", "");
        String yAlignAttr = JsoupTools.getStyle(mathElement, "y-align").replaceAll("[a-zA-Z]", "");
        String idAttr = mathElement.attr("id");
        
        //on initialise le JMathComponent
        mathComponent = new JMathComponent();
        mathComponent.setParameter(Parameter.MFRAC_KEEP_SCRIPTLEVEL, true);
        mathComponent.setContent(chaine);
        mathComponent.setBackground(ColorManager.transparent());
        mathComponent.setCursor(CursorManager.getCursor(Cursor.TEXT_CURSOR));

        id = System.currentTimeMillis();
        foregroundColor = mathComponent.getForeground();

        //On ajoute les attributs lus dans le html
        if(!widthAttr.isEmpty() && !heightAttr.isEmpty()) {setSize(new Dimension(Integer.parseInt(widthAttr),Integer.parseInt(heightAttr)));}
        Color color = JsoupTools.getColor(mathElement);
        if(color!=null) {setForeground(color);}
        if(!fontSizeAttr.isEmpty()) {mathComponent.setFontSize(Float.parseFloat(fontSizeAttr));}
        if(!yAlignAttr.isEmpty()) {mathComponent.setAlignmentY(Float.parseFloat(yAlignAttr));} else {
            //on calcule le bon alignementY
            FontMetrics fm = mathComponent.getFontMetrics(mathComponent.getFont());
            mathComponent.setAlignmentY(calculateMathAlignment(chaine, fm));
        }
        if(!idAttr.isEmpty()) {id = Long.parseLong(idAttr);}

        //On prépare le panel comme un contenair
        setLayout(new BorderLayout());
        add(mathComponent, BorderLayout.CENTER);
        DispatchMouseToParent dispatcher = new DispatchMouseToParent();
        mathComponent.addMouseListener(dispatcher);
        mathComponent.addMouseMotionListener(dispatcher);
        mathComponent.addMouseWheelListener(dispatcher);
        
//        addMouseListener(new FormuleListener());
        
    }
    
    private String corrigerInput(String mathMLInput) {
        String chaine = mathMLInput.replaceAll("&times;", "&#x000d7;");//JMathComponent ne lit pas le HTML
        chaine = chaine.replaceAll("&divide;", "&#x000f7;");//JMathComponent ne lit pas le HTML
        chaine = chaine.replaceAll("\n", "");//JMathComponent ne lit pas les \n (JMathComponent c'est un peu de la merde...)
        chaine = chaine.replaceAll("<?xml.*?>", "").replaceAll("<math>|</math>","");//On supprime les balises inutiles pouvant perturber le JMathComponent
        return chaine;
    }

    public String getContent() {
        return mathComponent.getContent();
    }
    
    public long getId() {return id;}
    public void setId(long id) {this.id = id;}

    /**
     * Renvoie le JMathComponent associé à cette Formule
     * @return le JMathComponent qui traduit la formule
     */
    public JMathComponent getMathComponent() {
        return mathComponent;
    }
    
    public float getFontSize() {
        return mathComponent.getFontSize();
    }

    public Color getForeground() {
        return mathComponent.getForeground();
    }

    public int getHorizontalAlignment() {
        return mathComponent.getHorizontalAlignment();
    }

    public Dimension getPreferredSize() {
        return mathComponent.getPreferredSize();
    }

    public int getVerticalAlignment() {
        return mathComponent.getVerticalAlignment();
    }
    
    public void setBackground(Color color) {
        mathComponent.setBackground(color);
    }

    public void setContent(String string) {
        mathComponent.setContent(string);
    }

    public void setFontSize(float f) {
        mathComponent.setFontSize(f);
    }

    @Override
    public final void setForeground(Color color) {
        mathComponent.setForeground(color);
    }

    public void setHorizontalAlignment(int i) {
        mathComponent.setHorizontalAlignment(i);
    }

    @Override
    public void setOpaque(boolean bln) {
        mathComponent.setOpaque(bln);
    }

    public void setVerticalAlignment(int i) {
        mathComponent.setVerticalAlignment(i);
    }
    
    @Override
    public boolean isSelected() {return selected;}
    
    @Override
    public void setSelected(boolean b) {
        if(selected==b) {return;}
        selected = b;
        if(selected) {
            mathComponent.requestFocusInWindow();
            mathComponent.setBackground(ColorManager.get("color disabled"));
            mathComponent.setForeground(Color.WHITE);
        } else {
            mathComponent.setForeground(foregroundColor);//on rend au composant son ancienne couleur
            mathComponent.setBackground(ColorManager.transparent());
        }
        mathComponent.repaint();
    }

/*    @Override
    public void addMouseListener(MouseListener listener) {
        super.addMouseListener(listener);
        mathComponent.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.setSource(Formule.this);
                for(MouseListener l : getListeners(MouseListener.class)) {l.mouseClicked(e);}
            }

            @Override
            public void mousePressed(MouseEvent e) {
                e.setSource(Formule.this);
                for(MouseListener l : getListeners(MouseListener.class)) {l.mousePressed(e);}
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                e.setSource(Formule.this);
                for(MouseListener l : getListeners(MouseListener.class)) {l.mouseReleased(e);}
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                e.setSource(Formule.this);
                for(MouseListener l : getListeners(MouseListener.class)) {l.mouseEntered(e);}
            }

            @Override
            public void mouseExited(MouseEvent e) {
                e.setSource(Formule.this);
                for(MouseListener l : getListeners(MouseListener.class)) {l.mouseExited(e);}
            }
        });
    }*/
    
    @Override
    public void setSize(Dimension d) {
        this.setSize(d.width, d.height);
    }
    
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        mathComponent.setSize(width-1, height-1);
    }
    
    public void editAlignment() {
        //TODO : On devrait simplement générer une boite de dialogue permettant l'édit de
        //notre formule. Un changeEvent serait finalement envoyé pour prévenir le JMathTextPane
//        new DialogueMathAlignement(mathComponent);
    }
    
    public void edit() {
        //TODO : On devrait simplement générer une boite de dialogue permettant l'édit de
        //notre formule. Un changeEvent serait finalement envoyé pour prévenir le JMathTextPane
//        new DialogueMathEdit();
    }

    @Override
    public Object copy() {
        return new Formule(getHTMLRepresentation());
    }

    @Override
    public void setStroken(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isStroken() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setStrikeColor(Color c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Color getStrikeColor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static class FormuleListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() instanceof Formule) {
                Formule f = (Formule) e.getSource();
                if (SwingUtilities.isLeftMouseButton(e)) {
                    f.setSelected(true);
                    f.edit();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    f.editAlignment();
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            Formule f = (Formule) SwingUtilities.getAncestorOfClass(Formule.class, e.getComponent());
            if (f!=null) {
                f.setBorder(BorderFactory.createLineBorder(Color.black));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Formule f = (Formule) SwingUtilities.getAncestorOfClass(Formule.class, e.getComponent());
            if (f!=null) {
                f.setBorder(BorderFactory.createLineBorder(Color.black));
            }
        }
    }
    
    public String getHTMLRepresentation() {
        String html = "<math>"+getContent().replaceAll("<math>|</math>","")+"</math>";
        Document doc = Jsoup.parse("<span class='"+MATH_COMPONENT+"'></span>");
        doc.select("span").first().attr("id", getId()+"")
                .attr("style", "font-size:"+getFontSize()+";color:"+ColorManager.getRGBHexa(getForeground())+";")
                .attr("height", ""+getSize().height)
                .attr("width", ""+getSize().width)
                .attr("y-align",""+getAlignmentY())
                .html(html);
        JsoupTools.removeComments(doc);//on enlève l'instruction de version (xml version 1.0 encoding etc)
        return doc.body().html();
    }
    
}
