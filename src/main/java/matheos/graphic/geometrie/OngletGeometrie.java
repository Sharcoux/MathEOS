/**
 * «Copyright 2011,2012 François Billioud»
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
 */
package matheos.graphic.geometrie;

import matheos.IHM;
import matheos.elements.BarreOutils;
import matheos.graphic.Module;
import matheos.graphic.OngletGraph;
import static matheos.graphic.geometrie.Visionneuse.*;
import matheos.utils.boutons.ActionComplete;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import matheos.utils.managers.PermissionManager;
import static matheos.utils.managers.PermissionManager.ACTION.POSITION_CURSEUR;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class OngletGeometrie extends OngletGraph {
    
    public static final int ACTION_DEMI_DROITE = 0;
    public static final int ACTION_POSITION_CURSEUR = 1;

    protected final void setDefaultArea() {
        getEspaceDessin().getRepere().setArea(-10.0, 10.0, -10.0, 10.0, 1, 1);
    }
    
    private ModuleGeometrie moduleGeo;
    public OngletGeometrie() {
        super(new ModuleGeometrie());
        moduleGeo = (ModuleGeometrie) getModule();

        //prépare le repère
        getEspaceDessin().getRepere().setProperties(false, false, true, true, true, true);
        setDefaultArea();

        //Menu déroulant Options
        setMenuOptions(new OptionsGeometrie(moduleGeo));

        //contenu barre outils normale
        setBarreOutils(new BarreOutilsGeometrie(moduleGeo));
    }
    
    private class BarreOutilsGeometrie extends IHM.BarreOutilsTP {
        private BarreOutilsGeometrie(ModuleGeometrie module) {
            addBoutonOnLeft(new ActionVoirConstruction());
            addSeparateurOnRight();
            addSwitchOnRight(module.getToggleAction(ModuleGeometrie.ARC));
            addSwitchOnRight(module.getToggleAction(ModuleGeometrie.DROITE));
            addSwitchOnRight(module.getToggleAction(ModuleGeometrie.DEMI_DROITE));
            addSwitchOnRight(module.getToggleAction(ModuleGeometrie.SEGMENT));
            addSwitchOnRight(module.getToggleAction(ModuleGeometrie.POINT));
            addComponentOnRight(module.getColorPicker());
            addSeparateurOnRight();
            addSwitchOnRight(module.getToggleAction(ModuleGeometrie.RENOMMER));
            addSwitchOnRight(module.getToggleAction(ModuleGeometrie.SUPPRIMER));
            addSwitchOnRight(module.getToggleAction(ModuleGeometrie.TEXTE));
            addSwitchOnRight(module.getToggleAction(ModuleGeometrie.COLORER));
            addSwitchOnRight(module.getToggleAction(ModuleGeometrie.DRAGAGE));
       }
    }
    
    private class OptionsGeometrie extends OngletGraph.OptionsGraph {
        public OptionsGeometrie(ModuleGeometrie module) {
            super(getEspaceDessin().getRepere());
            addCheckBox(module.getToggleAction(ModuleGraph.ACTION_POINTILLES_LECTURE));
            addCheckBox(module.getToggleAction(ModuleGraph.ACTION_COORDONNEES_CURSEUR));
            addCheckBox(module.getToggleAction(ModuleGeometrie.POINTS_DE_CONSTRUCTION));
            addElement(module.getAction(ModuleGeometrie.MESURES));
        }
    }
    
    private class BarreOutilsVisionneuse extends BarreOutils {
        public BarreOutilsVisionneuse(Visionneuse visionneuse) {
            addBoutonOnLeft(visionneuse.getAction(ACTION_RESTART));
            addBoutonOnLeft(visionneuse.getAction(ACTION_PREVIOUS));
            addBoutonOnLeft(visionneuse.getAction(ACTION_NEXT));
            addBoutonOnLeft(visionneuse.getAction(ACTION_LAST));
            addBoutonOnRight(visionneuse.getAction(ACTION_CANCEL));
        }
    }

    private class OptionsVisionneuse extends IHM.MenuOptions {
        public OptionsVisionneuse(Visionneuse module) {
        }
    }

    @Override
    public void setActionEnabled(PermissionManager.ACTION actionID, boolean b) {
        switch(actionID) {
            case DEMI_DROITE : moduleGeo.getAction(ModuleGeometrie.DEMI_DROITE).setEnabled(b); break;
            case POSITION_CURSEUR : moduleGeo.getAction(ModuleGraph.ACTION_COORDONNEES_CURSEUR).setEnabled(b); break;
        }
    }
    

    private class ActionVoirConstruction extends ActionComplete {
        private ActionVoirConstruction() { super("geometry construction"); }

        public void actionPerformed(ActionEvent e) {
            moduleGeo.retourModeNormal();

            //Création du nouveau module
            Visionneuse visionneuse = new Visionneuse();
            visionneuse.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if(evt.getPropertyName().equals(Module.MODE_PROPERTY)) {
                        if(evt.getNewValue().equals(Visionneuse.MODE_NORMAL)) {
                            moduleGeo = new ModuleGeometrie();
                            setModule(moduleGeo, new BarreOutilsGeometrie(moduleGeo), new OptionsGeometrie(moduleGeo));
                        }
                    }
                }
            });
            setModule(visionneuse, new BarreOutilsVisionneuse(visionneuse), new OptionsVisionneuse(visionneuse));
        }
    }

}
