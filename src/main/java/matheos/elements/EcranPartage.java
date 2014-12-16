/** «Copyright 2011 François Billioud»
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

package matheos.elements;

import matheos.elements.Onglet.OngletCours;
import matheos.elements.Onglet.OngletTP;
import matheos.utils.managers.FontManager;
import matheos.utils.managers.LaFFixManager;
import matheos.utils.managers.Traducteur;

import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import matheos.utils.managers.ColorManager;

/**
 * Définit la partie centrale de la fenêtre.
 * un JSplitPane composé de la partie cours (gauche) et la partie TP (droite)
 */
@SuppressWarnings("serial")
public class EcranPartage extends JSplitPane {

    public static final boolean COURS = true;
    public static final boolean TP = false;

    public static final int MILIEU = 0;
    public static final int GAUCHE = 1;
    public static final int DROIT = 2;

    private final Font POLICE_ONGLET = FontManager.get("font tab");

    /** Constante mémorisant la position du séparateur (gauche, milieu, droite) **/
    private int taille;

    private boolean mode;

    private final TableAction partieCours = new TableAction();
    private final TableAction partieTP = new TableAction();

    /** Variable contenant la partie actuellement activée **/
    private TableAction partieActive = partieCours;


    /**
     * Crée la partie centrale de la fenêtre.
     * Place la séparation au milieu de l'écran au début
     * Demande que la barre de séparation se dirige plutôt vers le centre lors des redimensionnements.
     */
    public EcranPartage() {
        setBorder(BorderFactory.createMatteBorder(1,0,1,0,ColorManager.get("color splitpane border")));

        //met en place les composants
        miseEnPlace();

        partieCours.addMouseListener(new ChangeModeListener(COURS));
        partieTP.addMouseListener(new ChangeModeListener(TP));
        
        //adapte la position de la séparation en cas de resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {setTaille(taille);}
        });

    }

    private void miseEnPlace() {
        int separation = Toolkit.getDefaultToolkit().getScreenSize().width/2;

        setLeftComponent(partieCours);
        setRightComponent(partieTP);
        setOneTouchExpandable(false);
        setDividerLocation( separation  );
        setDividerSize(10);
        setResizeWeight(0.5);
        setContinuousLayout(false);
        setTaille(MILIEU);
    }

    public void setTaille(int taille) {
        this.taille = taille;
        switch (taille) {
            case GAUCHE : setDividerLocation(0.25); break;
            case MILIEU : setDividerLocation(0.5); break;
            case DROIT : setDividerLocation(0.75); break;
        }
        revalidate();
        repaint();
    }

    public void addOngletCours(String nom, Onglet.OngletCours onglet) {
        partieCours.addTab(Traducteur.traduire(nom),onglet);
    }

    public void addOngletTP(String nom, Onglet.OngletTP onglet) {
        partieTP.addTab(Traducteur.traduire(nom),onglet);
        partieTP.revalidate();
        partieTP.repaint();
        revalidate();
        repaint();
    }

    /** Force l'onglet à être actif. Change le mode si besoin **
    public void setOngletActif(Onglet onglet) {
        IHM.activeMode(partieCours.indexOfTabComponent(onglet)!=-1 ? COURS : TP);
        setOngletSelected(onglet);
    }*/

    /** définit l'onglet actif d'une partie **/
    public void setOngletSelected(Onglet.OngletTP onglet) {
        partieTP.setOngletSelected(onglet);
    }
    public void setOngletSelected(Onglet onglet) {
        try {setOngletSelected((Onglet.OngletTP)onglet);}
        catch (ClassCastException e) {
            setOngletSelected((Onglet.OngletCours)onglet);
        }
    }
    public void setOngletSelected(Onglet.OngletCours onglet) {
        partieCours.setOngletSelected(onglet);
    }

    public int getTaille() {
        return taille;
    }

    /** change la partie active et la réveille **/
    public void setMode(boolean mode) {
        //Lors de l'initialisation de l'interface, mode = COURS et this.partieActive = COURS
        partieTP.activePartie(mode==TP);
        partieCours.activePartie(mode==COURS);
        partieActive = (mode == COURS) ? partieCours : partieTP;
        this.mode = mode;
    }

    public JTabbedPane getPartie(boolean mode) {
        return mode==COURS ? partieCours : partieTP;
    }

    public OngletTP getOngletTPActif() { return (OngletTP)partieTP.getOngletActif(); }
    public OngletCours getOngletCoursActif() { return (OngletCours)partieCours.getOngletActif(); }
    public OngletCours getOngletCours(int index){return (OngletCours)partieCours.getOnglet(index);}
    public Onglet getOngletActif() { return partieActive.getOngletActif(); }
    public OngletTP getOngletTP(int index){return (OngletTP)partieTP.getOnglet(index);}
    public boolean getMode() {
        return mode;
    }

    private class TableAction extends JTabbedPane implements LaFFixManager.BackgroundTrouble {

        private TableAction() {
            super();
            setFont(POLICE_ONGLET);
            setFocusable(false);
            
            //HACK : problème de background avec Nimbus
//            LaFFixManager.fixBackground(this, Color.GREEN, true);

        }

        public Onglet getOngletActif() { return (Onglet)this.getSelectedComponent();}
        public Onglet getOnglet(int index){return (Onglet) this.getComponentAt(index);}
        public void setOngletSelected(Onglet c) {
            if(c==null || c==getSelectedComponent()) { return; }
            this.setSelectedComponent(c); //this.setBackgroundAt(this.getSelectedIndex(), Color.darkGray);
        }

        public void activePartie(boolean b) {
            //setEnabled(true);
            //getOngletActif().activer(); *Fait par l'InterfaceComplete
        }
        
        @Override
        public void addTab(String title, Component component) {
            super.addTab(title, component);
            if(getSelectedIndex() == -1) {setSelectedIndex(0);}
        }

        //FIXME : voir pourquoi le changement de couleur n'est pas fonctionnel
//        @Override
//        public void setSelectedIndex(int index) {
//            int oldIndex = getSelectedIndex();
//            super.setSelectedIndex(index);
//            if(index>-1) setBackgroundAt(index, Color.YELLOW);
//            if(oldIndex>-1) setBackgroundAt(oldIndex, Color.GREEN);
//        }

    }
    
}
