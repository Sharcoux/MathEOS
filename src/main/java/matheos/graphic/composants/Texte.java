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

package matheos.graphic.composants;

import matheos.graphic.Repere;
import matheos.graphic.composants.Composant.Draggable;
import matheos.sauvegarde.DataTexte;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.FontManager;
import matheos.utils.objets.DispatchMouseToParent;
import matheos.utils.texte.EditeurIO;
import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.texte.JMathTextPane;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import matheos.utils.managers.CursorManager;

/**
 *
 * @author François Billioud
 */
public class Texte extends ComposantGraphique implements Serializable, Cloneable, Draggable {
    private static final long serialVersionUID = 1L;

    private final Font POLICE = FontManager.get("font graphic text component");
    
    private transient final DispatchMouseToParent dispatcher = new DispatchMouseToParent();
    private transient final JLimitedMathTextPane textField;
    private double x;
    private double y;
    private Vecteur deplacement = new Vecteur(0,0);
    
    private transient String contenu;
//    private double rotation = 0;

//    private Texte() {}//pour le JSON

    public Texte(Point P) { this(P.x(),P.y()); }
    public Texte(Point P, String text) { this(P.x(),P.y(),text); }

    public Texte(double x, double y) {
        this(new JLimitedMathTextPane(1,true),x,y);
    }
    public Texte(double x, double y, String text) {
        this(new JLimitedMathTextPane(1,true),x,y,text);
    }
    public Texte(JLimitedMathTextPane textField, double x, double y) {
        this.x = x;
        this.y = y;
        this.textField = textField;
//        setCouleur(ColorManager.get("color text graph"));//pour donner une couleur spécifique aux composant texte
        initializeTextField(this.textField);
    }
    public Texte(JLimitedMathTextPane textField, double x, double y, String text) {
        this(textField,x,y);
        if(text!=null && !text.isEmpty()) {
            this.setText(text);
            textField.setBorder(null);
        }
    }

    public JMathTextPane getTextComponent() { return textField; }

    public String getContenu() {
        if(contenu==null||textField.hasBeenModified()) {
            contenu = EditeurIO.getDonnees(textField).getContenuHTML();
            textField.setModified(false);
        }
        return contenu;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    /** Renvoie le Vecteur traduisant les déplacements depuis que l'objet a été créé **/
    public Vecteur getDeplacement() { return deplacement; }

    @Override
    public final void setCouleur(Color couleur) {
        super.setCouleur(couleur);
        JMathTextPane txt = getTextComponent();
        for(Component c : txt.getComponents()) {c.setForeground(couleur);}
        txt.setForeground(couleur);
        txt.repaint();
    }

    @Override
    public void drag(Vecteur v) {deplacement = deplacement.plus(v);}
    
    @Override
    public void setPosition(double x, double y) {
        if(Math.abs(this.x-x)<Repere.ZERO_ABSOLU && Math.abs(this.y-y)<Repere.ZERO_ABSOLU) {return;}
        Point old = new Point.XY(getX(), getY());
        Point P = new Point.XY(x, y);
        this.x = x; this.y = y;
        if(this.x!=x || this.y!=y) {firePropertyChange(COORDINATE_PROPERTY, old, P);}
    }
    @Override
    public void setPosition(Point P) {this.setPosition(P.x(), P.y());}

    @Override
    protected void dessineComposant(Repere repere, Graphics2D g2D) {
        java.awt.Point point = new java.awt.Point(repere.reel2Pixel(new Point(x,y).plus(deplacement)));//Attention : si le repère bouge, le texte bouge !
        if(!getTextComponent().getLocation().equals(point)) {
            getTextComponent().setLocation(point);
            getTextComponent().repaint();
        }
    }

    @Override
    public int distance2Pixel(Point point, Repere repere) {
        Rectangle r = getTextComponent().getBounds();
        if(r.contains(repere.reel2Pixel(point))) {return 0;}
        int deltaX = compare(repere.xReel2Pixel(point.x()), (int)r.getMinX(), (int)r.getMaxX());
        int deltaY = compare(repere.yReel2Pixel(point.y()), (int)r.getMinY(), (int)r.getMaxY());
        return (int)new Vecteur(deltaX, deltaY).longueur();
    }
    private int compare(int xPixel, int xMinPixel, int xMaxPixel) {
        if(xPixel>xMinPixel) {
            if(xPixel<xMaxPixel) {return 0;}
            else {return xPixel-xMaxPixel;}
        } else {
            return xMinPixel-xPixel;
        }
    }

    @Override
    public boolean estEgalA(Composant cg) {
        try {
            Texte texte = (Texte)cg;
            return texte.getX()==this.getX() && texte.getY()==this.getY() && texte.getContenu().equals(this.getContenu());
        } catch(Exception e) {return false;}
    }

    @Override
    public List<Point> pointsSupplementaires() {
        return new LinkedList<>();
    }

    public final void setText(String text) {
        if(!getTextComponent().isEditable()) {//HACK : si le textPane n'est pas éditable, le setText ne va pas fonctionner
            getTextComponent().setEditable(true);
            textField.charger(new DataTexte(text));
            getTextComponent().setEditable(false);
        } else {
            textField.charger(new DataTexte(text));
        }
//        EditeurIO.importHtmlMathML(textField, text, 0);
//        textField.setMinimumSize(new Dimension(metrics.stringWidth(textField.getText()+"a"),metrics.getHeight()+5));
//        textField.setSize(new Dimension(metrics.stringWidth(textField.getText()+"a"),metrics.getHeight()+5));
//        textField.dimensionner();
        textField.revalidate();
    }

    private void initializeTextField(final JLimitedMathTextPane textField){
        textField.setFont(POLICE);
//        FontMetrics metrics = textField.getFontMetrics(textField.getFont());
        textField.addFocusListener(focusListener);
        for(Component c : textField.getComponents()) {c.setForeground(getCouleur());}
        textField.setForeground(getCouleur());
        //pour que le fond soit transparent, ajouter la ligne suivante
        textField.setBackgroundColor(ColorManager.transparent(), true);
//        textField.setBackgroundManual(ColorManager.get("color text graph background"));
        textField.setOpaque(false);
        setEditable(true);
//        textField.dimensionner();
        textField.revalidate();
    }
    
    /** Si mis a vrai, le texte n'est plus editable **/
    @Override public void passif(boolean b) {
        super.passif(b);
        if(b) setEditable(false);
    }
    
    private final FocusListener focusListener = new FocusListener() {//gère les bordures en cas de sélection du Texte
        public void focusGained(FocusEvent e) {
            textField.setBorder(BorderFactory.createLineBorder(ColorManager.get("color focused")));
            
            //on enlève le dispatcher : le dessin n'a pas besoin d'être prévenu des mouvements
            setDispatcher(false);
        }

        public void focusLost(FocusEvent e) {
            if (textField.isEmpty()) {
                textField.setBorder(BorderFactory.createLineBorder(ColorManager.get("color text graph unfocused")));
            } else { textField.setBorder(null); }
            
            //on avertit l'espace dessin des mouvements dans le JTextField
            setDispatcher(true);
        }
    };
    
    private transient boolean isDispatching = false;//Permet de savoir si le dispatcheur est présent pour ne pas l'ajouter 2 fois
    private void setDispatcher(boolean b) {
        if(isDispatching==b) {return;}
        isDispatching = b;
        if(b) {
            for(Component c : textField.getComponents()) {
                c.addMouseMotionListener(dispatcher);
                c.addMouseListener(dispatcher);
                c.addMouseWheelListener(dispatcher);
            }
            textField.addMouseMotionListener(dispatcher);
            textField.addMouseListener(dispatcher);
            textField.addMouseWheelListener(dispatcher);
        } else {
            for(Component c : textField.getComponents()) {
                c.removeMouseMotionListener(dispatcher);
                c.removeMouseListener(dispatcher);
                c.removeMouseWheelListener(dispatcher);
            }
            textField.removeMouseMotionListener(dispatcher);
            textField.removeMouseListener(dispatcher);
            textField.removeMouseWheelListener(dispatcher);
        }
    }
    
    public void setEditable(boolean b) {
        textField.setFocusable(b);
        textField.setEditable(b);
        textField.getCaret().setSelectionVisible(b);
        setDispatcher(!b);
        textField.getHTMLEditorKit().setDefaultCursor(CursorManager.getCursor(b ? Cursor.TEXT_CURSOR : Cursor.DEFAULT_CURSOR));
        textField.setCursor(CursorManager.getCursor(b ? Cursor.TEXT_CURSOR : Cursor.DEFAULT_CURSOR));
    }


//    @Override
//    public Texte clone() {
//        Texte clone = null;
//        try {
//            clone = (Texte) super.clone();
//        } catch (CloneNotSupportedException ex) {
//            Logger.getLogger(Texte.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        clone = new Texte(new JLimitedMathTextPane(),this.x,this.y);
//        clone.texteField.setSdoc(this.texteField.getSdoc());
//        return clone;
//    }

    @Override
    public boolean dependsOn(ComposantGraphique cg) {
        return false;
    }

    @Override
    public String getSVGRepresentation(Repere repere) {
        return "<text x='"+repere.xReel2Pixel(getX())+"' y='"+repere.yReel2Pixel(getY())+"' fill='"+ColorManager.getRGBHexa(getCouleur())+"'>"+getContenu()+"</text>";
    }

    public static class Legende extends Texte {
        private Legendable dependance = null;

        public Legende(String text) {
            super(0, 0, text);//le composant sera positionné par la source
            passif(true);
        }
        /** Définit le texte comme dépendant du composant passé en paramètre. **/
        public void setDependance(Legendable cg) {dependance = cg;}
        public Legendable getDependance() {return dependance;}
        @Override
        public boolean dependsOn(ComposantGraphique cg) {
            return cg.estEgalA(dependance);
        }
        @Override
        public boolean estEgalA(Composant cg) {
            try {
                Legende legende = (Legende)cg;
                return ((legende.getDependance()==null && this.getDependance()==null) || legende.getDependance().estEgalA(this.getDependance())) && legende.getContenu().equals(this.getContenu());
            } catch(Exception e) {return false;}
        }

    }
}
