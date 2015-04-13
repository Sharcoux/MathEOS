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
import matheos.utils.boutons.ActionComplete;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import matheos.utils.objets.VirtualInput;

/**
 * Cette classe sert à offrir un choix limité et pertinent pour le marquage et
 * le nommage des Points, des Droite et des Arcs
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class PanelMarquage {
    public static Filtre getFiltreRenommer() {return new Filtre(Point.class, Droite.class, Arc.class, Fonction.class);}

    private static class RenameInput extends VirtualInput {
        private final ComposantGraphique cg;
        private RenameInput(int rowCount, int colCount, String[] letters, ComposantGraphique cg) {
            super("rename", rowCount, colCount, letters);
            this.cg = cg;
            addVirtualInputListener(new VirtualInputListener() {
                @Override
                public void inputSent(String value) {
                    RenameInput.this.cg.setNom(value);
                }
            });
        }
    }
    public static void renommer(ComposantGraphique cg) {
        if(cg instanceof Fonction) {renommer((Fonction)cg);}
        else if(cg instanceof Point) {renommer((Point)cg);}
        else if(cg instanceof Droite) {renommer((Droite)cg);}
        else if(cg instanceof Arc) {renommer((Arc)cg);}
    }

    private static class MarkInput extends VirtualInput {
        private final Composant.Identificable cg;
        private MarkInput(int rowCount, int colCount, String[] letters, Composant.Identificable cg) {
            super("mark", rowCount, colCount, letters);
            this.cg = cg;
            addVirtualInputListener(new VirtualInput.VirtualInputListener() {
                @Override
                public void inputSent(String value) {
                    MarkInput.this.cg.setMarque(value);
                }
            });
        }
    }
    
    public static void marquer(Composant.Identificable cg) {
        String[] MARQUES = {"","/","//","X"};
        MarkInput input = new MarkInput(2, 2, MARQUES, cg);
        input.boutons.get("").getAction().putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(' '));
        input.setVisible(true);
    }
    
    private static void renommer(final Fonction f) {
        String[] LETTRES = {"f","g","h","","f1","f2","f3","f4","F","f'","f''","f'''","g1","g2","g3","g4","h1","h2","h3","h4"};
        RenameInput input = new RenameInput(5, 4, LETTRES, f);
        input.boutons.get("").getAction().putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(' '));
        input.setVisible(true);
    }
    private static void renommer(final Point P) {
        String[] LETTRES = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","S","","..."};
        final RenameInput input = new RenameInput(4, 5, LETTRES, P);
        input.boutons.get("...").setAction(new ActionComplete() {
            {
                putValue(NAME, "...");
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('.'));
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                input.dispose();
                renommer2(P);
            }
        });
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                try {
                    int i = Integer.parseInt(e.getKeyChar()+"");
                    input.dispose();
                    input.cg.setNom("P"+i);
                } catch(NumberFormatException ex) {}
            }
        });
        input.boutons.get("").getAction().putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(' '));
        input.setVisible(true);
    }
    private static void renommer2(final Point P) {
        String[] LETTRES2 = {"R","T","U","V","W","X","Y","Z","M'","N'","O'","S'","M''","N''","O''","S''"};
        RenameInput input2 = new RenameInput(4, 4, LETTRES2, P);
        input2.setVisible(true);
    }
    private static void renommer(Droite d) {
        String[] LETTRES = {"d","D","","d1","d2","d3","d'","d''","d'''"};
        final RenameInput input = new RenameInput(3, 3, LETTRES, d);
        input.boutons.get("").getAction().putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(' '));
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                try {
                    int i = Integer.parseInt(e.getKeyChar()+"");
                    input.dispose();
                    input.cg.setNom("d"+i);
                } catch(NumberFormatException ex) {}
            }
        });
        input.setVisible(true);
    }
    private static void renommer(Arc d) {
        String[] LETTRES = {"C","C'","C''","C1","C2","C3","c","C0",""};
        final RenameInput input = new RenameInput(3, 3, LETTRES, d);
        input.boutons.get("").getAction().putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(' '));
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                try {
                    int i = Integer.parseInt(e.getKeyChar()+"");
                    input.dispose();
                    input.cg.setNom("C"+i);
                } catch(NumberFormatException ex) {}
            }
        });
        input.setVisible(true);
    }

    private PanelMarquage() {throw new AssertionError("instantiting utilitary class");}
    
}