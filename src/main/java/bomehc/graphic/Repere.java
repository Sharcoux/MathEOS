/**
 * «Copyright 2013 François Billioud»
 *
 * This file is part of Bomehc.
 *
 * Bomehc is free software: you can redistribute it and/or modify under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * Bomehc is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY.
 *
 * You should have received a copy of the GNU General Public License along with
 * Bomehc. If not, see <http://www.gnu.org/licenses/>.
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
 */package bomehc.graphic;

import bomehc.graphic.composants.Droite;
import bomehc.graphic.composants.Point;
import bomehc.graphic.composants.Vecteur;
import bomehc.sauvegarde.Data;
import bomehc.sauvegarde.Data.Enregistrable;
import bomehc.sauvegarde.DataObject;
import bomehc.utils.managers.ColorManager;
import bomehc.utils.managers.FontManager;
import bomehc.utils.boutons.ActionComplete;
import bomehc.utils.dialogue.DialogueComplet;
import bomehc.utils.dialogue.DialogueEvent;
import bomehc.utils.dialogue.DialogueListener;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JPanel;
import static bomehc.graphic.composants.Composant.Legendable.LEGENDE_PROPERTY;
import bomehc.graphic.composants.Texte.Legende;
import bomehc.utils.managers.Traducteur;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class Repere implements Serializable, Enregistrable {

    private static final long serialVersionUID = 1L;
    /** efficacité du zoom. On est ici à 10% */
    private static final double ZOOM = 0.1;
    private static final int DECIMAL_LIMIT = 9;
    private final Color COULEUR_QUADRILLAGE = ColorManager.get("color secondary axes");
    /** Constante en-dessous de laquelle un résultat est considéré comme nul */
    public static final double ZERO_ABSOLU = 0.000001;//a multiplier par l'échelle
    /** police d'affichage des graduations du repère **/
    private final Font POLICE = FontManager.get("font repere");

    public static final boolean ABSCISSES = true;
    public static final boolean ORDONNEES = false;

    private transient JPanel espaceDessin = null;
    private transient final Droite axeAbscisses;
    private transient final Droite axeOrdonnees;
    private transient final PropertyChangeListener axeListener;
    {
        axeAbscisses = new DroiteAbscisse();
        axeOrdonnees = new DroiteOrdonnee();
        axeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //On ajoute/enlève les Légendes lors de leur création/suppression
                if(evt.getPropertyName().equals(LEGENDE_PROPERTY)) {
                    if(evt.getOldValue()!=null) {
                        if(getEspaceDessin()!=null) {getEspaceDessin().remove(((Legende) evt.getOldValue()).getTextComponent());}
                    }
                    if(evt.getNewValue()!=null) {
                        if(getEspaceDessin()!=null) {getEspaceDessin().add(((Legende) evt.getNewValue()).getTextComponent());}
                    }
                }
            }
        };
        axeAbscisses.addPropertyChangeListener(axeListener);
        axeOrdonnees.addPropertyChangeListener(axeListener);
    }

    //dimensions en pixel de l'espace dessin virtuel (valeurs utilisées lorsqu'aucun espaceDessin n'est affecté au repère)
    private int largeur = 1, hauteur = 1;

    //nom des données
    private static final String ORTHONORMAL = "orthonormal";
    private static final String MAGNETISME = "magnetisme";
    private static final String AFFICHER_GRADUACTIONS = "afficherGraduations";
    private static final String AFFICHER_QUADRILLAGE = "afficherQuadrillage";
    private static final String AFFICHER_AXE_ABSCISSES = "afficherAxeAbscisses";
    private static final String AFFICHER_AXE_ORDONNEES = "afficherAxeOrdonnees";
    private static final String NOM_AXE_X = "xName";
    private static final String NOM_AXE_Y = "yName";
    private static final String XMIN = "xmin";
    private static final String XMAX = "xmax";
    private static final String YMIN = "ymin";
    private static final String YMAX = "ymax";
    private static final String XECHELLE = "xEchelle";
    private static final String YECHELLE = "yEchelle";
    private static final String XMAGNET_PRECISION = "xMagnetPrecision";
    private static final String YMAGNET_PRECISION = "yMagnetPrecision";
    
    //Contient tous les paramètres du repère.
    private Data donneesRepere = new DataObject();
    /**
     * Crée un Repère permettant de convertir des dimensions réelles en dimensions en pixels et inversement
     * @param largeur la largeur en pixels de l'espace à convertir
     * @param hauteur la hauteur en pixels de l'espace à convertir
     */
    public Repere(int largeur, int hauteur) {
        definirRepere(-8, 8, -8, 8, 1, 1);
        definirProprietes(true, true, true, true, true, true);
        this.largeur = largeur;
        this.hauteur = hauteur;
    }

    /**
     * Crée un Repère permettant de convertir des dimensions réelles en dimensions en pixels et inversement sur l'espace passé en paramètre
     * @param dessin l'espaceDessin à représenter
     */
    public Repere(EspaceDessin dessin) {
        definirRepere(-8, 8, -8, 8, 1, 1);
        definirProprietes(true, true, true, true, true, true);
        if(dessin!=null) {setEspaceDessin(dessin);}
    }

    /**
     * Crée un Repère permettant de convertir des dimensions réelles en dimensions en pixels et inversement sur un espace à définir
     */
    public Repere() {
        definirRepere(-8, 8, -8, 8, 1, 1);
        definirProprietes(true, true, true, true, true, true);
    }

    /** Définit l'aire représentée par le repère. cette aire sera actualisée si le repère est orthonormal. Le pas du magnétisme sera aussi adapté **/
    public void setArea(double xm, double xM, double ym, double yM, double xE, double yE) {
        definirRepere(xm, xM, ym, yM, xE, yE);
        if(isOrthonormal()) { orthoNormal(); }
        if(espaceDessin!=null) {espaceDessin.repaint();}
    }

    /** Définit les propriétés du repère. Elle seront immédiatement appliquées **/
    public void setProperties(boolean afficherAxeAbscisses, boolean afficherAxeOrdonnees, boolean afficherGraduations, boolean afficherQuadrillage, boolean orthonormal, boolean magnetisme) {
        definirProprietes(afficherAxeAbscisses, afficherAxeOrdonnees, afficherGraduations, afficherQuadrillage, orthonormal, magnetisme);
    }

    /** Applique bêtement les paramètres sans aucune adaptation **/
    private void definirRepere(double xm, double xM, double ym, double yM, double xE, double yE) {
        setXMin(xm);
        setXMax(xM);
        setYMin(ym);
        setYMax(yM);
        setXEchelle(xE);
        setYEchelle(yE);
        setXMagnetPrecision(0.25*xE);
        setYMagnetPrecision(0.25*yE);
    }

    private void definirProprietes(boolean afficherAxeAbscisses, boolean afficherAxeOrdonnees, boolean afficherGraduations, boolean afficherQuadrillage, boolean orthonormal, boolean magnetisme) {
        afficherAxeAbscisses(afficherAxeAbscisses);
        afficherAxeOrdonnees(afficherAxeOrdonnees);
        afficherGraduations(afficherGraduations);
        afficherQuadrillage(afficherQuadrillage);
        setOrthonormal(orthonormal);
        setMagnetisme(magnetisme);
    }
    
    public double getXMin() {return Double.parseDouble(donneesRepere.getElement(XMIN));}
    public double getXMax() {return Double.parseDouble(donneesRepere.getElement(XMAX));}
    public double getYMin() {return Double.parseDouble(donneesRepere.getElement(YMIN));}
    public double getYMax() {return Double.parseDouble(donneesRepere.getElement(YMAX));}
    public double getXEchelle() {return Double.parseDouble(donneesRepere.getElement(XECHELLE));}
    public double getYEchelle() {return Double.parseDouble(donneesRepere.getElement(YECHELLE));}
    public double getXMagnetPrecision() { return Double.parseDouble(donneesRepere.getElement(XMAGNET_PRECISION)); }
    public double getYMagnetPrecision() { return Double.parseDouble(donneesRepere.getElement(YMAGNET_PRECISION)); }
    public String getNomAxeX() { return donneesRepere.getElement(NOM_AXE_X)!=null ? donneesRepere.getElement(NOM_AXE_X) : "x"; }
    public String getNomAxeY() { return donneesRepere.getElement(NOM_AXE_Y)!=null ? donneesRepere.getElement(NOM_AXE_Y) : "y"; }
    
    private void setXMin(double xmin) {donneesRepere.putElement(XMIN, xmin+"");}
    private void setXMax(double xmax) {donneesRepere.putElement(XMAX, xmax+"");}
    private void setYMin(double ymin) {donneesRepere.putElement(YMIN, ymin+"");}
    private void setYMax(double ymax) {donneesRepere.putElement(YMAX, ymax+"");}
    private void setXEchelle(double xEchelle) {donneesRepere.putElement(XECHELLE, xEchelle+"");}
    private void setYEchelle(double yEchelle) {donneesRepere.putElement(YECHELLE, yEchelle+"");}
    private void setXMagnetPrecision(double xMagnet) {donneesRepere.putElement(XMAGNET_PRECISION, xMagnet+"");}
    private void setYMagnetPrecision(double yMagnet) {donneesRepere.putElement(YMAGNET_PRECISION, yMagnet+"");}
    private void setNomAxeX(String x) { donneesRepere.putElement(NOM_AXE_X, x);axeAbscisses.setNom(x); }
    private void setNomAxeY(String y) { donneesRepere.putElement(NOM_AXE_Y, y);axeOrdonnees.setNom(y); }

    /** Lit l'aire et les propriétés du repère dans le composant de données **/
    public final void charger(Data r) {
        donneesRepere = r;
        afficherAxeAbscisses(isAfficherAxeAbscisses());
        afficherAxeOrdonnees(isAfficherAxeOrdonnees());
        afficherGraduations(isAfficherGraduations());
        afficherQuadrillage(isAfficherQuadrillage());
        setOrthonormal(isOrthonormal());
        setMagnetisme(isMagnetisme());
        espaceDessin.repaint();
    }

    public Data getDonnees() {return donneesRepere.clone();}
    
    /** Applique les changements au repère. Si le repère est orthonormé, ces valeurs seront adaptées **/
    public final void setArea(double xm, double xM, double ym, double yM) {
        setArea(xm, xM, ym, yM, getXEchelle()==0 ? 1 : getXEchelle(), getYEchelle()==0 ? 1 : getYEchelle());
    }

    //setter
    public void afficherGraduations(boolean b) { actionGraduations.setSelected(b);donneesRepere.putElement(AFFICHER_GRADUACTIONS, b+""); }
    public void afficherAxeAbscisses(boolean b) { actionAxeAbscisses.setSelected(b);donneesRepere.putElement(AFFICHER_AXE_ABSCISSES, b+""); }
    public void afficherAxeOrdonnees(boolean b) { actionAxeOrdonnees.setSelected(b);donneesRepere.putElement(AFFICHER_AXE_ORDONNEES, b+""); }
    public void afficherQuadrillage(boolean b) { actionQuadrillage.setSelected(b);donneesRepere.putElement(AFFICHER_QUADRILLAGE, b+""); }
    public void setMagnetisme(boolean b) { actionMagnetisme.setSelected(b); actionReglageMagnetisme.setEnabled(b);donneesRepere.putElement(MAGNETISME, b+""); }
    public void setOrthonormal(boolean b) { actionOrthonormal.setSelected(b); if(b) {orthoNormal();} donneesRepere.putElement(ORTHONORMAL, b+""); }
    public final void setEspaceDessin(JPanel dessin) {
        //on écoute les changements de taille du nouveau panel de dessin
        if(espaceDessin!=null) {
            espaceDessin.removeComponentListener(resizeListener);
            if(isAfficherAxeAbscisses() && axeAbscisses.getLegende()!=null) {espaceDessin.remove(axeAbscisses.getLegende().getTextComponent());}
            if(isAfficherAxeOrdonnees() && axeOrdonnees.getLegende()!=null) {espaceDessin.remove(axeOrdonnees.getLegende().getTextComponent());}
        }
        espaceDessin = dessin;
        if(dessin!=null) {
            dessin.addComponentListener(resizeListener);
            if(isOrthonormal()) {orthoNormal();}
            if(isAfficherAxeAbscisses() && axeAbscisses.getLegende()!=null) {espaceDessin.add(axeAbscisses.getLegende().getTextComponent());}
            if(isAfficherAxeOrdonnees() && axeOrdonnees.getLegende()!=null) {espaceDessin.add(axeOrdonnees.getLegende().getTextComponent());}
            dessin.repaint();
        }
    }

    private final ComponentAdapter resizeListener = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
            if(isOrthonormal()) {orthoNormal();}
            espaceDessin.repaint();
        }
    };

    //getter
    public Droite getAxeAbscisses() { return axeAbscisses; }
    public Droite getAxeOrdonnees() { return axeOrdonnees; }

    public boolean isOrthonormal() { return "true".equals(donneesRepere.getElement(ORTHONORMAL)); }
    public boolean isMagnetisme() { return "true".equals(donneesRepere.getElement(MAGNETISME)); }
    public boolean isAfficherAxeAbscisses() { return "true".equals(donneesRepere.getElement(AFFICHER_AXE_ABSCISSES)); }
    public boolean isAfficherAxeOrdonnees() { return "true".equals(donneesRepere.getElement(AFFICHER_AXE_ORDONNEES)); }
    public boolean isAfficherGraduations() { return "true".equals(donneesRepere.getElement(AFFICHER_GRADUACTIONS)); }
    public boolean isAfficherQuadrillage() { return "true".equals(donneesRepere.getElement(AFFICHER_QUADRILLAGE)); }
//    public boolean isOrthonormal() { return actionOrthonormal.isSelected(); }
//    public boolean isMagnetisme() { return actionMagnetisme.isSelected(); }
//    public boolean isAfficherAxeAbscisses() { return actionAxeAbscisses.isSelected(); }
//    public boolean isAfficherAxeOrdonnees() { return actionAxeOrdonnees.isSelected(); }
//    public boolean isAfficherGraduations() { return actionGraduations.isSelected(); }
//    public boolean isAfficherQuadrillage() { return actionQuadrillage.isSelected(); }


    public JPanel getEspaceDessin() {return espaceDessin;}
    public int largeur() { return espaceDessin==null||espaceDessin.getWidth()==0 ? largeur : espaceDessin.getWidth(); }
    public int hauteur() { return espaceDessin==null||espaceDessin.getHeight()==0 ? hauteur : espaceDessin.getHeight(); }

    /** a utiliser à la place de ZERO_ABSOLU pour régler les problèmes en cas de travail avec de très petites échelles **/
    public double zeroRelatif() {
        return ZERO_ABSOLU * Math.min(getXEchelle(), getYEchelle());
    }//TODO remplacer les appels à ZERO_ABSOLU

    /** Convertit une abscisse réelle en coordonnée pixels **/
    public int xReel2Pixel(double xReel) {
        double coef = largeur() / (getXMax() - getXMin());
        return (int) (coef * (xReel - getXMin()));
    }

    /** Convertit une ordonnée réelle en coordonnée pixels **/
    public int yReel2Pixel(double yReel) {
        double coef = hauteur() / (getYMax() - getYMin());
        return (int) (coef * (getYMax() - yReel));
    }

    /** Convertit une abscisse pixel en coordonnée réelle **/
    public double xPixel2Reel(int xPixel) {
        double coef = (getXMax() - getXMin()) / largeur();
        return xPixel * coef + getXMin();
    }
    
    /** Convertit un point pixel en réel **/
    public Point pixel2Reel(java.awt.Point P) {
        return new Point.XY(xPixel2Reel(P.x),yPixel2Reel(P.y));
    }
    
    /** Convertit un point réel en pixel **/
    public java.awt.Point reel2Pixel(Point P) {
        return new java.awt.Point(xReel2Pixel(P.x()),yReel2Pixel(P.y()));
    }

    /** Convertit une ordonnée pixel en coordonnée réelle **/
    public double yPixel2Reel(int yPixel) {
        double coef = (getYMax() - getYMin()) / hauteur();
        return getYMax() - yPixel * coef;
    }

    public double xPixel2ReelMagnet(int xPixel) {
        double coef = (getXMax() - getXMin()) / largeur();
        double xMagnetPrecision = getXMagnetPrecision();
        return Math.floor((xPixel * coef + getXMin()) / xMagnetPrecision) * xMagnetPrecision + xMagnetPrecision / 2;
    }

    /** Convertit une abscisse pixel en coordonnée réelle arrondie **/
    public double yPixel2ReelMagnet(int yPixel) {
        double coef = (getYMax() - getYMin()) / hauteur();
        double yMagnetPrecision = getYMagnetPrecision();
        return Math.floor((getYMax() - yPixel * coef) / yMagnetPrecision) * yMagnetPrecision + yMagnetPrecision / 2;
    }

    /** Convertit une distance réelle en distance en pixels sur l'axe des abscisses **/
    public int xDistance2Pixel(double xReel) {
        double coef = largeur() / (getXMax() - getXMin());
        return (int) (coef * xReel);

    }

    /** Convertit une distance réelle en distance en pixels sur l'axe des ordonnées **/
    public int yDistance2Pixel(double yReel) {
        double coef = hauteur() / (getYMax() - getYMin());
        return (int) (coef * yReel);
    }

    /** Convertit une distance en pixels en distance réelle sur l'axe des abscisses **/
    public double xDistance2Reel(int xPixel) {
        double coef = (getXMax() - getXMin()) / largeur();
        return xPixel * coef;
    }

    /** Convertit une distance en pixels en distance réelle sur l'axe des ordonnées **/
    public double yDistance2Reel(int yPixel) {
        double coef = (getYMax() - getYMin()) / hauteur();
        return yPixel * coef;
    }

    /** Convertit une distance réelle sur deux dimensions en distance en pixels **/
    public int distance2Pixel(Vecteur v) {
        double x = xDistance2Pixel(v.x());
        double y = yDistance2Pixel(v.y());
        return (int) Math.sqrt((x * x) + (y * y));
    }

    /** calcul une distance de n pixels dans la direction v **/
    public Vecteur distance2Reel(int n, Vecteur v) {
        Vecteur u = v.unitaire().fois(n);
        return new Vecteur(xDistance2Reel((int)u.x()),yDistance2Reel((int)u.y()));
    }

    /** Rend le repère orthonormal, sur la base de l'échelle verticale */
    public final void orthoNormal() {
        double newXMin = ((getYMax() - getYMin()) / hauteur()) * getXMin() / ((getXMax() - getXMin())) * largeur() * getXEchelle() / getYEchelle();//XXX vérifier cette formule
        double newXMax = ((getYMax() - getYMin()) / hauteur()) * getXMax() / ((getXMax() - getXMin())) * largeur() * getXEchelle() / getYEchelle();
        setXMin(newXMin);
        setXMax(newXMax);
    }

    /** renvoie l'image réel d'un point du panel */
    public Point pointReel(int xPixel, int yPixel) {
        double xReel = xPixel2Reel(xPixel);
        double yReel = yPixel2Reel(yPixel);
        return new Point.XY(xReel, yReel);
    }

    /** renvoie la position d'un point après application du magnétisme */
    public Point pointMagnetique(Point P) {
        if (isMagnetisme()) {
            double xMagnetPrecision = getXMagnetPrecision();
            double yMagnetPrecision = getYMagnetPrecision();
            double xMagnet = Math.floor((P.x() + xMagnetPrecision / 2) / xMagnetPrecision) * xMagnetPrecision;//+xMagnetPrecision/2;
            double yMagnet = Math.floor((P.y() + yMagnetPrecision / 2) / yMagnetPrecision) * yMagnetPrecision;//+yMagnetPrecision/2;
            return new Point.XY(xMagnet, yMagnet);
        } else {
            return P;
        }
    }

    public Droite getAxe(boolean b) {
        return (b == ABSCISSES) ? axeAbscisses : axeOrdonnees;
    }
    
    private static double arrondi(double d, double precision) {
        return Math.round(d/precision)*precision;
    }

    public static String afficheNb(double d) {
        DecimalFormat df = new  DecimalFormat();
        df.setMaximumFractionDigits(DECIMAL_LIMIT);
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        return df.format(d).replace("\\.", Traducteur.traduire("decimal point"));
    }
    public static String afficheNb(double d, double precision) {
        return afficheNb(arrondi(d, precision));
    }

    private void changerRepere() {
        DialogueComplet dialogue = new DialogueComplet("dialog change mark");
        double precisionX = getXEchelle();
        double precisionY = getYEchelle();
        //pré-remplis les champs
        dialogue.setInitialValue("xmin", afficheNb(getXMin(), precisionX));
        dialogue.setInitialValue("xmax", afficheNb(getXMax(), precisionX));
        dialogue.setInitialValue("ymin", afficheNb(getYMin(), precisionY));
        dialogue.setInitialValue("ymax", afficheNb(getYMax(), precisionY));
        dialogue.setInitialValue("xscale", afficheNb(getXEchelle()));
        dialogue.setInitialValue("yscale", afficheNb(getYEchelle()));
        dialogue.setInitialValue("x", getNomAxeX());
        dialogue.setInitialValue("y", getNomAxeY());

        dialogue.addDialogueListener(new DialogueListener() {
            @Override
            public void dialoguePerformed(DialogueEvent event) {
                if(event.isConfirmButtonPressed()) {
                    double xmin = event.getInputDouble("xmin");
                    double xmax = event.getInputDouble("xmax");
                    double ymin = event.getInputDouble("ymin");
                    double ymax = event.getInputDouble("ymax");
                    if (xmin < xmax && ymin < ymax) {
                        setArea(xmin, xmax, ymin, ymax, event.getInputDouble("xscale"), event.getInputDouble("yscale"));
                    }
                    setNomAxeX(event.getInputString("x"));
                    setNomAxeY(event.getInputString("y"));
                }
            }
        });
    }

    private void changerMagnetisme() {
        DialogueComplet dialogue = new DialogueComplet("dialog change magnetism");
        //pré-remplis les champs
        dialogue.setInitialValue("x", afficheNb(getXMagnetPrecision()));
        dialogue.setInitialValue("y", afficheNb(getXMagnetPrecision()));

        dialogue.addDialogueListener(new DialogueListener() {
            @Override
            public void dialoguePerformed(DialogueEvent event) {
                if(event.isConfirmButtonPressed()) {
                    setXMagnetPrecision(event.getInputDouble("x"));
                    setYMagnetPrecision(event.getInputDouble("y"));
                }
            }
        });
    }
    
    public void dessine(Graphics2D g2D) {
        // quadrillage
        if (isAfficherQuadrillage()) {
            g2D.setStroke(new BasicStroke(1));
            g2D.setColor(COULEUR_QUADRILLAGE);

            for (double x = 0.; x < getXMax(); x += getXEchelle()) {
                Droite droite = axeOrdonnees.droiteParallele(new Point.XY(x, 0));
                droite.setCouleur(COULEUR_QUADRILLAGE);
                droite.dessine(this, g2D);
            }
            for (double x = -getXEchelle(); x > getXMin(); x -= getXEchelle()) {
                Droite droite = axeOrdonnees.droiteParallele(new Point.XY(x, 0));
                droite.setCouleur(COULEUR_QUADRILLAGE);
                droite.dessine(this, g2D);
            }
            for (double y = 0.; y < getYMax(); y += getYEchelle()) {
                Droite droite = axeAbscisses.droiteParallele(new Point.XY(0, y));
                droite.setCouleur(COULEUR_QUADRILLAGE);
                droite.dessine(this, g2D);
            }
            for (double y = -getYEchelle(); y > getYMin(); y -= getYEchelle()) {
                Droite droite = axeAbscisses.droiteParallele(new Point.XY(0, y));
                droite.setCouleur(COULEUR_QUADRILLAGE);
                droite.dessine(this, g2D);
            }
        }

        //axes
        if (isAfficherAxeAbscisses()) {
            axeAbscisses.dessine(this, g2D);
        }

        if (isAfficherAxeOrdonnees()) {
            axeOrdonnees.dessine(this, g2D);
        }

        if (isAfficherAxeAbscisses() || isAfficherAxeOrdonnees()) {
            g2D.drawString("0", xReel2Pixel(0) - 7, yReel2Pixel(0) + 11);
        }

    }

    public HashMap<String, Double> getParametres() {
        HashMap<String, Double> map = new HashMap<>();
        map.put("xMin", getXMin());
        map.put("xMax", getXMax());
        map.put("yMin", getYMin());
        map.put("yMax", getYMax());
        map.put("xEchelle", getXEchelle());
        map.put("yEchelle", getYEchelle());
        return map;
    }

    public void zoomP(Point curseur) {
        int xPixel = xReel2Pixel(curseur.x());
        int yPixel = yReel2Pixel(curseur.y());
        zoomP();
        Point newCurseur = new Point.XY(xPixel2Reel(xPixel),yPixel2Reel(yPixel));
        deplacerRepere(new Vecteur(newCurseur, curseur));
    }
    public void zoomP() {
        double xMinTemp = getXMin() + (getXMax() - getXMin()) * ZOOM;
        setXMax(getXMax() - (getXMax() - getXMin()) * ZOOM);
        setXMin(xMinTemp);

        double yMinTemp = getYMin() + (getYMax() - getYMin()) * ZOOM;
        setYMax(getYMax() - (getYMax() - getYMin()) * ZOOM);
        setYMin(yMinTemp);

        if (isOrthonormal()) { orthoNormal(); }
        espaceDessin.repaint();
    }

    public void zoomM(Point curseur) {
        int xPixel = xReel2Pixel(curseur.x());
        int yPixel = yReel2Pixel(curseur.y());
        zoomM();
        Point newCurseur = new Point.XY(xPixel2Reel(xPixel),yPixel2Reel(yPixel));
        deplacerRepere(new Vecteur(newCurseur, curseur));
    }
    public void zoomM() {
        double xMinTemp = getXMin() - (getXMax() - getXMin()) / (1 - ZOOM) * ZOOM;
        setXMax(getXMax() + (getXMax() - getXMin()) / (1 - ZOOM) * ZOOM);
        setXMin(xMinTemp);

        double yMinTemp = getYMin() - (getYMax() - getYMin()) / (1 - ZOOM) * ZOOM;
        setYMax(getYMax() + (getYMax() - getYMin()) / (1 - ZOOM) * ZOOM);
        setYMin(yMinTemp);

        if (isOrthonormal()) { orthoNormal(); }
        espaceDessin.repaint();
    }

    public void deplacerRepere(Vecteur vecteur) {
        //definirRepere(xMin + vecteur.x(), xMax + vecteur.x(), yMin + vecteur.y(), yMax + vecteur.y());
        setXMin(getXMin() + vecteur.x());
        setXMax(getXMax() + vecteur.x());
        setYMin(getYMin() + vecteur.y());
        setYMax(getYMax() + vecteur.y());
    }

    private final ActionAffichageGraduations actionGraduations      = new ActionAffichageGraduations(true);
    private final ActionAffichageAxeAbscisses actionAxeAbscisses    = new ActionAffichageAxeAbscisses(true);
    private final ActionAffichageAxeOrdonnees actionAxeOrdonnees    = new ActionAffichageAxeOrdonnees(true);
    private final ActionAffichageQuadrillage actionQuadrillage      = new ActionAffichageQuadrillage(false);
    private final ActionMagnetisme actionMagnetisme                 = new ActionMagnetisme(true);
    private final ActionOrthonormal actionOrthonormal               = new ActionOrthonormal(true);
    private final ActionReglageMagnetisme actionReglageMagnetisme   = new ActionReglageMagnetisme();
    private final ActionReglageEchelle actionReglageEchelle         = new ActionReglageEchelle();

    public final Action getActionGraduations() {return actionGraduations;}
    public final Action getActionAxeAbscisses() {return actionAxeAbscisses;}
    public final Action getActionAxeOrdonnees() {return actionAxeOrdonnees;}
    public final Action getActionMagnetisme() {return actionMagnetisme;}
    public final Action getActionOrthonormal() {return actionOrthonormal;}
    public final Action getActionQuadrillage() {return actionQuadrillage;}
    public final Action getActionReglageMagnetisme() {return actionReglageMagnetisme;}
    public final Action getActionReglageEchelle() {return actionReglageEchelle;}
    
    private abstract class ActionToggleRepere extends ActionComplete.Toggle {
        private final String property;
        private ActionToggleRepere(String aspect, boolean etat, String property) {
            super(aspect, etat);
            this.property = property;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            donneesRepere.putElement(property, isSelected()+"");
            if(espaceDessin!=null) {espaceDessin.repaint();}
        }
    }

    private class ActionAffichageGraduations extends ActionToggleRepere {
        private ActionAffichageGraduations(boolean etatInitial) {
            super("graphic display graduation", etatInitial, AFFICHER_GRADUACTIONS);
        }
    }

    private class ActionAffichageQuadrillage extends ActionToggleRepere {
        private ActionAffichageQuadrillage(boolean etatInitial) {
            super("graphic display grid", etatInitial, AFFICHER_QUADRILLAGE);
        }
    }

    private abstract class ActionAffichageAxe extends ActionToggleRepere {
        private final Droite axe;
        private ActionAffichageAxe(String aspect, boolean etat, String property, Droite axe) {
            super(aspect, etat, property);
            this.axe = axe;
            addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if(evt.getPropertyName().equals(Action.SELECTED_KEY)) {
                        boolean b = (boolean) evt.getNewValue();
                        if(b) {getEspaceDessin().add(ActionAffichageAxe.this.axe.getLegende().getTextComponent());}
                        else {getEspaceDessin().remove(ActionAffichageAxe.this.axe.getLegende().getTextComponent());}
                    }
                }
            });
        }
    }
    private class ActionAffichageAxeAbscisses extends ActionAffichageAxe {
        private ActionAffichageAxeAbscisses(boolean etatInitial) {
            super("graphic display axe x", etatInitial, AFFICHER_AXE_ABSCISSES, axeAbscisses);
        }
    }

    private class ActionAffichageAxeOrdonnees extends ActionAffichageAxe {
        private ActionAffichageAxeOrdonnees(boolean etatInitial) {
            super("graphic display axe y", etatInitial, AFFICHER_AXE_ORDONNEES, axeOrdonnees);
        }
    }

    private class ActionMagnetisme extends ActionToggleRepere {
        private ActionMagnetisme(boolean etatInitial) {
            super("graphic magnetism", etatInitial, MAGNETISME);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            actionReglageMagnetisme.setEnabled(isSelected());
        }
    }

    private class ActionOrthonormal extends ActionToggleRepere {
        private ActionOrthonormal(boolean etatInitial) {
            super("graphic orthonormal mark", etatInitial, ORTHONORMAL);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(isSelected()) {orthoNormal();}
            super.actionPerformed(e);
        }
    }

    private class ActionReglageEchelle extends ActionComplete {
        private ActionReglageEchelle() {
            super("graphic settle scale");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            changerRepere();
        }
    }

    private class ActionReglageMagnetisme extends ActionComplete {
        private ActionReglageMagnetisme() {
            super("graphic settle magnetism");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            changerMagnetisme();
        }
    }

    private abstract static class DroiteGraduee extends Droite {

        /** Longueur en pixel des traits de graduation */
        protected static final int L_GRADUATION = 4;
        protected final Color COULEUR_AXES = ColorManager.get("color axes");
        private static final Point O = new Point.XY(0, 0);
        private static final Vecteur Ox = new Vecteur(1, 0);
        private static final Vecteur Oy = new Vecteur(0, 1);

        private DroiteGraduee(Vecteur v) {
            super(O, v);
            setCouleur(COULEUR_AXES);
        }

        protected abstract void tracerGraduations(Repere repere, Graphics2D g2D);

//        protected abstract void tracerNomAxe(Repere repere, Graphics2D g2D);

        @Override
        public void dessine(Repere repere, Graphics2D g2D) {
            g2D.setStroke(new BasicStroke(3));
            super.dessine(repere, g2D);

            g2D.setStroke(new BasicStroke(1.5f));
            tracerGraduations(repere, g2D);

//            Font fontOrigine = g2D.getFont();
//            Font fontNouveau = POLICE;
//            g2D.setFont(fontNouveau);
//            tracerNomAxe(repere, g2D);
//            g2D.setFont(fontOrigine);
        }
    }

    private static class DroiteAbscisse extends DroiteGraduee {

        private DroiteAbscisse() {
            super(DroiteGraduee.Ox);
            setNom("x");
        }

        //Le placement par défat se fait en plein sur les graduations. On préfère donc redéfinir le placement de façon plus adaptée
        @Override
        public java.awt.Point getDefaultLegendeCoord(Repere repere) {
            double xMax = repere.getXMax();
            Rectangle textArea = getLegende().getTextComponent().getBounds();
            return new java.awt.Point(repere.xReel2Pixel(xMax)-5-textArea.width,repere.yReel2Pixel(0)-5-textArea.height);
        }
        
        @Override
        protected void tracerGraduations(Repere repere, Graphics2D g2D) {
            //graduations :
            //x+
            for (double x = repere.getXEchelle(); x < repere.getXMax(); x += repere.getXEchelle()) {
                g2D.drawLine(repere.xReel2Pixel(x), repere.yReel2Pixel(0) + L_GRADUATION, repere.xReel2Pixel(x), repere.yReel2Pixel(0) - L_GRADUATION);
                if (repere.isAfficherGraduations()) {
                    g2D.drawString(afficheNb(x, repere.getXEchelle()), repere.xReel2Pixel(x) - 5, repere.yReel2Pixel(0) + 20);
                }
            }

            //x-
            for (double x = -repere.getXEchelle(); x > repere.getXMin(); x -= repere.getXEchelle()) {
                g2D.drawLine(repere.xReel2Pixel(x), repere.yReel2Pixel(0) + L_GRADUATION, repere.xReel2Pixel(x), repere.yReel2Pixel(0) - L_GRADUATION);
                if (repere.isAfficherGraduations()) {
                    g2D.drawString(afficheNb(x, repere.getXEchelle()), repere.xReel2Pixel(x) - 12, repere.yReel2Pixel(0) + 20);
                }
            }

        }
//
//        @Override
//        protected void tracerNomAxe(Repere repere, Graphics2D g2D) {
//            g2D.drawString(getNom(), repere.xReel2Pixel(repere.getXMax()) - 15, repere.yReel2Pixel(0) - 5);
//        }
    }

    private static class DroiteOrdonnee extends DroiteGraduee {

        private DroiteOrdonnee() {
            super(DroiteGraduee.Oy);
            setNom("y");
        }

        @Override
        protected void tracerGraduations(Repere repere, Graphics2D g2D) {
            //graduations :
            //y+
            for (double y = repere.getYEchelle(); y < repere.getYMax(); y += repere.getYEchelle()) {
                g2D.drawLine(repere.xReel2Pixel(0) + L_GRADUATION, repere.yReel2Pixel(y), repere.xReel2Pixel(0) - L_GRADUATION, repere.yReel2Pixel(y));
                if (repere.isAfficherGraduations()) {
                    String nb = afficheNb(y, repere.getYEchelle());
                    g2D.drawString(nb, repere.xReel2Pixel(0) - (10+10*nb.length()), repere.yReel2Pixel(y) + 5);
                }
            }

            //y-
            for (double y = -repere.getYEchelle(); y > repere.getYMin(); y -= repere.getYEchelle()) {
                g2D.drawLine(repere.xReel2Pixel(0) + L_GRADUATION, repere.yReel2Pixel(y), repere.xReel2Pixel(0) - L_GRADUATION, repere.yReel2Pixel(y));
                if (repere.isAfficherGraduations()) {
                    String nb = afficheNb(y, repere.getYEchelle());
                    g2D.drawString(nb, repere.xReel2Pixel(0) - (2+10*nb.length()), repere.yReel2Pixel(y) + 5);
                }
            }
        }
//
//        @Override
//        protected void tracerNomAxe(Repere repere, Graphics2D g2D) {
//            g2D.drawString(getNom(), repere.xReel2Pixel(0) + 5, repere.yReel2Pixel(repere.getYMax()) + 15);
//        }
    }

}
