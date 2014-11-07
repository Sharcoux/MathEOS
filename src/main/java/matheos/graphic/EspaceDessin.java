/**
 * «Copyright 2013 François Billioud»
 *
 * This file is part of MathEOS.
 *
 * MathEOS is free software: you can redistribute it and/or modify under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * MathEOS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY.
 *
 * You should have received a copy of the GNU General Public License along with
 * MathEOS. If not, see <http://www.gnu.org/licenses/>.
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
 */package matheos.graphic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import matheos.graphic.ListComposant.ListComposantListener;
import matheos.graphic.composants.Composant.Legendable;
import matheos.graphic.composants.ComposantGraphique;
import matheos.graphic.composants.Point;
import matheos.graphic.composants.Texte;
import matheos.graphic.composants.Vecteur;
import matheos.utils.interfaces.ComponentInsertionListener;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.FontManager;

/**
 *
 * @author François Billioud, Tristan
 */
@SuppressWarnings("serial")
public class EspaceDessin extends JPanel {

    /**
     * Constante en pixels indiquant l'imprécision de l'utilisateur
     */
    public static final int TOLERANCE = 25;//tolérance en pixels
    public static final int TOLERANCE_DRAG = 10;//tolérance du draggage en pixels
    public static final Font POLICE_MESSAGE = FontManager.get("font graphic messages");

    private Repere repere;
    private Point souris;
    public Point getSouris() {return souris;}
    
    /** Contient tous les ComposantGraphiques à dessiner **/
    private ListComposant elementsPermanents;
 
    /** Contient les ComposantGraphiques temporaires, non comptabilisés dans les actions annuler/refaire **/
    private ListComposant elementsTemporaires;
    
    /** Contient les messages à afficher sur l'espace dessin **/
    private List<String> messages = new LinkedList<>();
    
    /** Permet l'insertion automatique des Textes dans l'espace dessin **/
    private final ListComposantListener texteCatcher = new ListComposantTextCatcher();
    private final ListComposantListener temporaryTexteCatcher = new TemporaryListTextCatcher();
    private final ListComposantListener modificationListener = new ModificationListener();
    
    private EspaceDessin() {
        super();
        setOpaque(true);
        setBackground(Color.WHITE);
        setFocusable(false);
        
        addMouseEventListener();
    }
    

    public EspaceDessin(Repere repere) {
        this();
        setRepere(repere);
    }

    public String getSVG() {
        String s = "<svg height='"+getHeight()+"' width='"+getWidth()+"' viewBox='0 0 "+getWidth()+" "+getHeight()+"' />"+"\n";
        s+=getElementsPermanents().getSVGRepresentation(getRepere());
        s+="</svg>";
        return s;
    }
    
    public Graphics2D capturerImage(Graphics2D g) {
        if (this.getSize().width == 0 || this.getSize().height == 0) {
            return null;
        }
//        BufferedImage tamponSauvegarde = new BufferedImage(this.getSize().width, this.getSize().height, BufferedImage.TYPE_3BYTE_BGR);
//        Graphics g = tamponSauvegarde.createGraphics(); //On crée un Graphic que l'on insère dans tamponSauvegarde
//        g.setColor(Color.WHITE);
//        this.paint(g);
//        return tamponSauvegarde;

        setBackground(ColorManager.transparent());
        this.paint(g);
        setBackground(Color.WHITE);
        return g;
    }

    public Repere getRepere() {
        return repere;
    }

    public final void setRepere(Repere r) {
        repere = r;
        repere.setEspaceDessin(this);
        repaint();
    }

    public void setElementsPermanents(ListComposant L) {
        if(elementsPermanents!=null) {
            texteCatcher.clear(elementsPermanents, elementsPermanents);//On supprime les anciens Textes de l'espaceDessin
            elementsPermanents.removeListComposantListener(texteCatcher);
            elementsPermanents.removeListComposantListener(modificationListener);
        }
        elementsPermanents = L;
        texteCatcher.addAll(L, L);//On ajoute les nouveaux textes à l'espaceDessin
        elementsPermanents.addListComposantListener(texteCatcher);
        elementsPermanents.addListComposantListener(modificationListener);
        repaint();
    }
    public void setElementsTemporaires(ListComposant L) {
        if(elementsTemporaires!=null) {
            elementsTemporaires.clear();//On supprime les anciens Textes de l'espaceDessin
            elementsTemporaires.removeListComposantListener(temporaryTexteCatcher);
            elementsTemporaires.removeListComposantListener(modificationListener);
        }
        elementsTemporaires = L;
        temporaryTexteCatcher.addAll(L, L);//On ajoute les nouveaux textes à l'espaceDessin
        elementsTemporaires.addListComposantListener(temporaryTexteCatcher);
        elementsTemporaires.addListComposantListener(modificationListener);
        repaint();
    }
    public void setMessages(List<String> L) {
        messages = L;
        repaint();
    }
    public ListComposant getElementsPermanents() {return elementsPermanents;}
    public ListComposant getElementsTemporaires() {return elementsTemporaires;}
    public List<String> getMessages() {return messages;}
        
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintComponent(g2D);
        
        if(repere==null) {return;}

        //affiche le repère
        repere.dessine(g2D);

        //affiche les composants
        g2D.setStroke(new BasicStroke(3,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        if(elementsPermanents!=null) elementsPermanents.dessine(repere, g2D);
        g2D.setStroke(new BasicStroke(2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        if(elementsTemporaires!=null) elementsTemporaires.dessine(repere, g2D);
        
        if(messages!=null) afficherDonnees(g2D);
    }

    private void afficherDonnees(Graphics2D g2D) {
        g2D.setColor(ColorManager.get("color data text"));
        Font fontOrigine = g2D.getFont();
        g2D.setFont(POLICE_MESSAGE);
        for(int i=0;i<messages.size();i++) {
            g2D.drawString(messages.get(i), 1, g2D.getFontMetrics(POLICE_MESSAGE).getHeight()*(i+1));
        }
        g2D.setFont(fontOrigine);
    }

    public void deplacerRepere(Vecteur vecteur) {
        repere.deplacerRepere(vecteur);
        souris = souris.plus(vecteur);
        repaint();
    }
    
    public void zoomP() {
        repere.zoomP(souris);
        repaint();
    }

    public void zoomM() {
        repere.zoomM(souris);
        repaint();
    }

    /**
     * Sélectionne et renvoie l'élémentPermanent le plus proche de P respectant le filtre f
     * @param f le filtre à respecter
     * @param P le point de référence
     * @param priority l'ordre de priorité des éléments
     * @return l'élément le plus pertinent
     */
    public ComposantGraphique getSelectedComponent(Filtre f, Point P, List<Class<? extends ComposantGraphique>> priority) {
        return getElementsPermanents().selection(P, repere, f, priority);
    }
    
    /**
     * Sélectionne et renvoie l'élémentPermanent le plus proche de P respectant le filtre f
     * en suivant l'ordre de priorité par défaut
     * @param f le filtre à respecter
     * @param P le point de référence
     * @return l'élément le plus pertinent
     */
    public ComposantGraphique getSelectedComponent(Filtre f, Point P) {
        return getElementsPermanents().selection(P, repere, f, f.getClassesAcceptees());
    }

    @Override
    public void setEnabled(boolean b) {//On gèle la détection de la souris en cas de désactivation
        super.setEnabled(b);
        
        //On envoie un event pour simuler l'entrée ou la sortie de la souris lors de la réactivation ou la désactivation de l'espace dessin
        java.awt.Point P = new java.awt.Point(0,0);
        SwingUtilities.convertPointFromScreen(MouseInfo.getPointerInfo().getLocation(), this);
        MouseEvent e = new MouseEvent(this, 0, System.currentTimeMillis(), 0, P.x, P.y, 0, false);
        if(b) {
            addMouseEventListener();
            mouseListener.mouseEntered(e);
        } else {
            mouseListener.mouseExited(e);
            removeMouseEventListener();
        }
        for(Component c : getComponents()) {c.setEnabled(b);}
    }

    /** écoute les évènements envoyé par l'espaceDessin au sujet de la souris **/
    public static interface EspaceDessinListener {
        public void mouseIn(Point souris);
        public void mouseOut(Point souris);
        public void mouseDragged(Vecteur drag, Point origine, Point destination);
        public void mouseDragReleased(Vecteur drag, Point origine, Point destination);
        public void mouseMoved(Point origine, Point destination);
        public void mouseClicked(Point souris, int button);
        public void mousePressed(Point souris, int button);
        public void mouseReleased(Point souris, int button);
    }
    /** écoute les évènements envoyé par l'espaceDessin au sujet de la souris **/
    public static class EspaceDessinAdapter implements EspaceDessinListener {
        public void mouseIn(Point souris) {}
        public void mouseOut(Point souris) {}
        public void mouseDragged(Vecteur drag, Point origine, Point destination) {}
        public void mouseDragReleased(Vecteur drag, Point origine, Point destination) {}
        public void mouseMoved(Point origine, Point destination) {}
        public void mouseClicked(Point souris, int button) {}
        public void mousePressed(Point souris, int button) {}
        public void mouseReleased(Point souris, int button) {}
    }

    private final MouseEventListener mouseListener = new MouseEventListener();
    private void addMouseEventListener() {
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addMouseWheelListener(mouseListener);
    }
    private void removeMouseEventListener() {
        removeMouseListener(mouseListener);
        removeMouseMotionListener(mouseListener);
        removeMouseWheelListener(mouseListener);
    }
    
    private final List<EspaceDessinListener> mouseListeners = new LinkedList<>();
    public void addEspaceDessinListener(EspaceDessinListener listener) {
        mouseListeners.add(listener);
    }
    public void removeEspaceDessinListener(EspaceDessinListener listener) {
        mouseListeners.remove(listener);
    }
    private void fireMouseIn(Point souris) {
        for(EspaceDessinListener l : mouseListeners) { l.mouseIn(souris); }
    }
    private void fireMouseOut(Point souris) {
        for(EspaceDessinListener l : mouseListeners) { l.mouseOut(souris); }
    }
    private void fireMouseDragged(Vecteur drag, Point origine, Point destination) {
        for(EspaceDessinListener l : mouseListeners) { l.mouseDragged(drag, origine, destination); }
    }
    private void fireMouseDragReleased(Vecteur drag, Point origine, Point destination) {
        for(EspaceDessinListener l : mouseListeners) { l.mouseDragReleased(drag, origine, destination); }
    }
    private void fireMouseMoved(Point origine, Point destination) {
        for(EspaceDessinListener l : mouseListeners) { l.mouseMoved(origine, destination); }
    }
    private void fireMouseClicked(Point souris, int button) {
        for(EspaceDessinListener l : mouseListeners) { l.mouseClicked(souris, button); }
    }
    private void fireMousePressed(Point souris, int button) {
        for(EspaceDessinListener l : mouseListeners) { l.mousePressed(souris, button); }
    }
    private void fireMouseReleased(Point souris, int button) {
        for(EspaceDessinListener l : mouseListeners) { l.mouseReleased(souris, button); }
    }
        
    /**
     * Classe qui écoute les mouvements de souris sur l'EspaceDessin et interprète
     * les zoom+, zoom-, entrée et sortie sur l'EspaceDessin
     */
    private class MouseEventListener extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            if(repere==null) {return;}
            if(!EspaceDessin.this.contains(e.getPoint())) {return;}
            Point newPoint = repere.pointReel(e.getX(), e.getY());
            fireMouseIn(newPoint);
            fireMouseMoved(souris, newPoint);//On déplace la souris vers l'intérieur
            souris = newPoint;
        }
        @Override
        public void mouseExited(MouseEvent e) {
            if(repere==null) {return;}
//            java.awt.Point p = new java.awt.Point(e.getLocationOnScreen());
//            SwingUtilities.convertPointFromScreen(p, e.getComponent());
//            if(EspaceDessin.this.contains(p)) {return;}
            if(EspaceDessin.this.contains(e.getPoint())) {return;}
            Point newPoint = repere.pointReel(e.getX(), e.getY());
            fireMouseMoved(souris, newPoint);//On déplace la souris vers l'extérieur
            fireMouseOut(newPoint);
            souris = newPoint;
        }
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if(repere==null) {return;}
            if (e.getWheelRotation() > 0) {
                for (int i = 0; i < e.getWheelRotation(); i++) {
                    zoomM();
                    EspaceDessin.this.repaint();
                }
            }
            if (e.getWheelRotation() < 0) {
                for (int i = 0; i > e.getWheelRotation(); i--) {
                    zoomP();
                    EspaceDessin.this.repaint();
                }
            }
        }
        public void mouseMoved(MouseEvent e) {
            if(repere==null) {return;}
            Point newPoint = repere.pointReel(e.getX(), e.getY());
            fireMouseMoved(souris, newPoint);
            souris = newPoint;
        }
        
        private Vecteur deplacementDrag;
        public void mouseDragged(MouseEvent e) {
            if(repere==null) {return;}
            Point newPoint = repere.pointReel(e.getX(), e.getY());
            Vecteur deplacement = new Vecteur(souris, newPoint);
            if(deplacementDrag!=null) {//Sinon il s'agit d'un drag depuis l'extérieur
                deplacementDrag = deplacementDrag.plus(deplacement);
                if(SwingUtilities.isLeftMouseButton(e)) {fireMouseDragged(deplacement, souris, newPoint);}
            }
            souris = souris.plus(deplacement);//Attention : le repère a pu bouger
        }
        
        private boolean pressed = false;
        public void mousePressed(MouseEvent e) {
            if(repere==null) {return;}
            deplacementDrag = new Vecteur(0,0);
            fireMousePressed(souris, e.getButton());
            pressed = true;
        }
        public void mouseClicked(MouseEvent e) {
            if(!pressed) {return;}
            if(repere==null) {return;}
            if(repere.distance2Pixel(deplacementDrag)>TOLERANCE_DRAG) {}
            else {fireMouseClicked(souris, e.getButton());}
            pressed = false;
        }
        public void mouseReleased(MouseEvent e) {
            if(repere==null) {return;}
            souris = repere.pointReel(e.getX(), e.getY());
            if(deplacementDrag!=null && repere.distance2Pixel(deplacementDrag)>TOLERANCE_DRAG) {fireMouseDragReleased(deplacementDrag, souris.moins(deplacementDrag), souris);}
            else {fireMouseReleased(souris, e.getButton());}
        }
    }

    protected void fireComponentInsertion(Component c) {
        ComponentInsertionListener[] L = listenerList.getListeners(ComponentInsertionListener.class);
        for(ComponentInsertionListener l : L) {l.componentInserted(c);}
    }
    
    protected void fireComponentRemoval(Component c) {
        ComponentInsertionListener[] L = listenerList.getListeners(ComponentInsertionListener.class);
        for(ComponentInsertionListener l : L) {l.componentRemoved(c);}
    }
    
    public void addComponentInsertionListener(ComponentInsertionListener e) {
        listenerList.add(ComponentInsertionListener.class, e);
    }
    
    public void removeComponentInsertionListener(ComponentInsertionListener e) {
        listenerList.remove(ComponentInsertionListener.class, e);
    }
    
    private class ListComposantTextCatcher implements ListComposantListener {
        protected Texte getTexte(ComposantGraphique cg) {
            if(cg instanceof Texte) {
                return (Texte) cg;
            } else if(cg instanceof Legendable) {
                Legendable l = (Legendable) cg;
                return l.getLegende();
            } else {
                return null;
            }
        }
        protected void addTexte(Texte texte) {
            EspaceDessin.this.add(texte.getTextComponent());
            fireComponentInsertion(texte.getTextComponent());
        }
        protected void removeTexte(Texte texte) {
            EspaceDessin.this.remove(texte.getTextComponent());
            fireComponentRemoval(texte.getTextComponent());
        }
        protected void addComposant(ComposantGraphique cg) {
            Texte texte = getTexte(cg);
            if(texte!=null) {addTexte(texte);}
        }
        protected void removeComposant(ComposantGraphique cg) {
            Texte texte = getTexte(cg);
            if(texte!=null) {removeTexte(texte);}
        }

        @Override
        public boolean add(ListComposant source, ComposantGraphique cg) {
            addComposant(cg);
            return false;
        }

        @Override
        public boolean addAll(ListComposant source, Collection<? extends ComposantGraphique> L) {
            for(ComposantGraphique cg : L) { addComposant(cg); }
            return false;
        }

        @Override
        public boolean remove(ListComposant source, ComposantGraphique cg) {
            removeComposant(cg);
            return false;
        }

        @Override
        public boolean removeAll(ListComposant source, Collection<? extends ComposantGraphique> L) {
            for(ComposantGraphique cg : L) { removeComposant(cg); }
            return false;
        }

        @Override
        public boolean clear(ListComposant source, Collection<? extends ComposantGraphique> L) {
            for(ComposantGraphique cg : L) { removeComposant(cg); }
            return false;
        }
    }
    private class TemporaryListTextCatcher extends ListComposantTextCatcher {
        private final ListComposant listeTextes = new ListComposant();
        @Override
        protected void addComposant(ComposantGraphique cg) {
            Texte texte = getTexte(cg);
            if(texte==null) {return;}
            //HACK pour gérer l'ajout temporaire d'un élément présent dans la liste permanente
            if(Arrays.asList(EspaceDessin.this.getComponents()).contains(texte.getTextComponent())) {listeTextes.add(texte);return;}
            addTexte(texte);
        }
        @Override
        protected void removeComposant(ComposantGraphique cg) {
            Texte texte = getTexte(cg);
            if(texte==null) {return;}
            //HACK pour gérer la suppression temporaire d'un élément présent dans les 2 listes (temporaire et permanente)
            if(listeTextes.contient(texte)) {listeTextes.remove(texte);return;}
            removeTexte(texte);
        }
    }
    private class ModificationListener implements ListComposantListener {
        @Override
        public boolean add(ListComposant source, ComposantGraphique cg) {
            repaint();return true;
        }
        @Override
        public boolean addAll(ListComposant source, Collection<? extends ComposantGraphique> L) {
            repaint();return true;
        }
        @Override
        public boolean remove(ListComposant source, ComposantGraphique cg) {
            repaint();return true;
        }
        @Override
        public boolean removeAll(ListComposant source, Collection<? extends ComposantGraphique> L) {
            repaint();return true;
        }
        @Override
        public boolean clear(ListComposant source, Collection<? extends ComposantGraphique> L) {
            repaint();return true;
        }
    }
}
