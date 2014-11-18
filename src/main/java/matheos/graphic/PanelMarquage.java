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

package matheos.graphic;

import matheos.graphic.composants.*;
import matheos.graphic.fonctions.Fonction;
import matheos.utils.managers.Traducteur;
import matheos.utils.boutons.ActionComplete;
import matheos.utils.boutons.Bouton;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.NAME;
import javax.swing.JDialog;

import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Cette classe sert à offrir un choix limité et pertinent pour le marquage et
 * le nommage des Points, des Droite et des Arcs
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class PanelMarquage extends JDialog {
    public static String renommer(ComposantGraphique cg) {
        PanelMarquage instance = new PanelMarquage(cg);
        if(cg instanceof Fonction) {return instance.renommer((Fonction)cg);}
        if(cg instanceof Point) {return instance.renommer((Point)cg);}
        if(cg instanceof Droite) {return instance.renommer((Droite)cg);}
        if(cg instanceof Arc) {return instance.renommer((Arc)cg);}
        return null;
    }

    public static void marquer(ComposantGraphique cg) {
        PanelMarquage instance = new PanelMarquage(cg);
        if(cg instanceof Segment) {instance.marquer((Segment)cg);}
        if(cg instanceof Arc) {instance.marquer((Arc)cg);}
    }
    
    private String newValue = null;
    private final ComposantGraphique composant;
    private PanelMarquage(ComposantGraphique cg) {
        composant = cg;
        initFenetre();
    }
    
    public static Filtre getFiltreRenommer() {return new Filtre(Point.class, Droite.class, Arc.class, Fonction.class);}
    public static Filtre getFiltreMarquer() {return new Filtre(Segment.class, Arc.class);}

    private void initFenetre() {
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setSize(300, 200);
//        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setTitle(Traducteur.traduire("point name"));
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setModalityType(ModalityType.APPLICATION_MODAL);
    }
    private String renomme(PanelMarquage.PanelLettres panel) {
        setContentPane(panel);
        setVisible(true);
        return newValue;
    }
    
    private String renommer(Fonction f) {
        return renomme(new PanelLettresFonction());
    }
    private String renommer(Point P) {
        return renomme(new PanelLettresPoint());
    }
    private String renommer(Droite d) {
        return renomme(new PanelLettresDroite());
    }
    private String renommer(Arc c) {
        return renomme(new PanelLettresCercle());
    }
    
    private String marquer(JPanel panel) {
        setContentPane(panel);
        setVisible(true);
        return newValue;
    }
    private String marquer(Segment AB) {
        return marquer(new PanelMarquesSegment());
    }
    private String marquer(Arc c) {
        return marquer(new PanelMarquesArc());
    }

    private class PanelLettres extends JPanel {
        private PanelLettres(int lignes, int colonnes) {
            setLayout(new GridLayout(lignes,colonnes));
        }
        protected void setLettres(String[] lettres) {
            for (int i=0; i<lettres.length; i++) {
                add(new Bouton(new ActionLettreRenommer(lettres[i])));
            }
        }
    }
    
    private class PanelLettresFonction extends PanelLettres {
        private final String[] LETTRES = {"f","g","h","","f1","f2","f3","f4","F","f'","f''","f'''","g1","g2","g3","g4","h1","h2","h3","h4"};
        private PanelLettresFonction() {
            super(5,4);
            setLettres(LETTRES);
        }
    }
    private class PanelLettresPoint2 extends PanelLettres {
        private final String[] LETTRES = {"R","T","U","V","W","X","Y","Z","M'","N'","O'","S'","M''","N''","O''","S''"};
        private PanelLettresPoint2() {
            super(4,4);
            setLettres(LETTRES);
        }
    }
    private class PanelLettresPoint extends PanelLettres {
        private final String[] LETTRES = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","S",""};
        private PanelLettresPoint() {
            super(4,5);
            setLettres(LETTRES);
            add(new Bouton(new ActionAutresLettresRenommer()));
        }
    }
    private class PanelLettresDroite extends PanelLettres {
        private final String[] LETTRES = {"d","D","","d1","d2","d3","d'","d''","d'''"};
        private PanelLettresDroite() {
            super(3,3);
            setLettres(LETTRES);
        }
    }
    private class PanelLettresCercle extends PanelLettres {
        private final String[] LETTRES = {"C","C'","C''","C1","C2","C3","c","C0",""};
        private PanelLettresCercle() {
            super(3,3);
            setLettres(LETTRES);
        }
    }

    private class PanelMarquesArc extends JPanel {
        private final String[] MARQUES = {"","/","//","X"};
        private PanelMarquesArc() {
            setLayout(new GridLayout(2,2));
            for (int i=0; i<MARQUES.length; i++) {
                add(new Bouton(new ActionLettreMarquer(MARQUES[i])));
            }
        }
    }

    private class PanelMarquesSegment extends JPanel {
        private final String[] MARQUES = {"","/","//","X"};
        private PanelMarquesSegment() {
            setLayout(new GridLayout(2,2));
            for (int i=0; i<MARQUES.length; i++) {
                add(new Bouton(new ActionLettreMarquer(MARQUES[i])));
            }
        }
    }

    private final class ActionLettreRenommer extends ActionComplete {
        private ActionLettreRenommer(String valeur) {
            this.putValue(NAME, valeur);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(valeur));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            composant.setNom((String)getValue(NAME));
            newValue = (String)getValue(NAME);
            dispose();
        }
    }

    private class ActionAutresLettresRenommer extends ActionComplete {
        public ActionAutresLettresRenommer() {
            this.putValue(NAME, "...");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('.'));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            PanelMarquage.this.setContentPane(new PanelLettresPoint2());
            PanelMarquage.this.revalidate();
            PanelMarquage.this.repaint();
        }
    }
    
    private final class ActionLettreMarquer extends ActionComplete {
        private ActionLettreMarquer(String valeur) {
            this.putValue(NAME, valeur);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(composant instanceof Segment) {
                ((Segment)composant).setMarque((String)getValue(NAME));
                newValue = (String)getValue(NAME);
            } else if(composant instanceof Arc) {
                ((Arc) composant).setMarque((String) getValue(NAME));
                newValue = (String)getValue(NAME);
            }
            dispose();
        }
    }

}