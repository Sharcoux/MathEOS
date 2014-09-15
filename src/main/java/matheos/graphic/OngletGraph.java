/*
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of matheos.
 *
 * matheos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

import matheos.IHM;
import matheos.elements.BarreMenu;
import matheos.elements.BarreOutils;
import matheos.elements.ChangeModeListener;
import matheos.elements.Onglet;
import matheos.sauvegarde.Data;
import matheos.utils.managers.ColorManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

/**
 *
 * @author François Billioud
 */
public abstract class OngletGraph extends Onglet.OngletTP {

    private final GraphController controller;
    private final EspaceDessin dessin;
    
    public OngletGraph(Module module) {
        Repere repere = new Repere();
        dessin = new EspaceDessin(repere);
        controller = new GraphController(dessin);
        controller.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });
        
        controller.setModule(module);
        
        dessin.addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        dessin.setFocusable(true);
        this.add(dessin, BorderLayout.CENTER);
        
        //Raccourcis clavier
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "stop");
        getActionMap().put("stop",new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {getController().getModule().retourModeNormal();}
        });
    }
    
    protected static class OptionsGraph extends IHM.MenuOptions {
        public OptionsGraph(Repere repere) {
            addCheckBox(repere.getActionQuadrillage());
            addCheckBox(repere.getActionAxeAbscisses());
            addCheckBox(repere.getActionAxeOrdonnees());
            addCheckBox(repere.getActionMagnetisme());
            addCheckBox(repere.getActionOrthonormal());
            addElement(repere.getActionReglageEchelle());
            addElement(repere.getActionReglageMagnetisme());
        }
    }

    @Override
    public int preferredInsertionSize() {
        return 400;
    }
    
    protected GraphController getController() {
        return controller;
    }
    
    protected EspaceDessin getEspaceDessin() {
        return dessin;
    }
    
    protected Module getModule() {
        return controller.getModule();
    }
    protected void setModule(Module m, BarreOutils barre, BarreMenu.Menu options) {
        controller.setModule(m);
        setBarreOutils(barre);
        setMenuOptions(options);
    }

    @Override
    public void activeContenu(boolean b) {
        setEnabled(b);
        dessin.setBackground(b ? Color.WHITE : ColorManager.get("color disabled"));
        dessin.repaint();
        dessin.setEnabled(b);
    }

    @Override
    public Data getDonneesTP() {
        return controller.getDonnees();
    }
    
    @Override
    public void chargement(/*long id, */Data donnees) {
        controller.charger(donnees);
    }
    
    //fonctions dues à l'interface
    @Override
    public void zoomP() {
        dessin.zoomP();
    }

    @Override
    public void zoomM() {
        dessin.zoomM();
    }

    @Override
    public Graphics2D capturerImage(Graphics2D g) {
        return dessin.capturerImage(g);
    }

    public void annuler() {
        controller.annuler();
    }

    public void refaire() {
        controller.refaire();
    }

    public boolean peutAnnuler() {
        return controller.peutAnnuler();
    }

    public boolean peutRefaire() {
        return controller.peutRefaire();
    }

    protected void nouveauTP() {
        controller.getModule().retourModeNormal();
        controller.getListeObjetsConstruits().charger(new ListComposant());
        setDefaultArea();
    }
    
    protected abstract void setDefaultArea();
    
    @Override
    public boolean hasBeenModified() {
        return controller.hasBeenModified();
    }

    @Override
    public void setModified(boolean b) {
        controller.setModified(b);
    }
    
}
