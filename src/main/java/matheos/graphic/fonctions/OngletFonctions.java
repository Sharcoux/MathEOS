/** «Copyright 2014 François Billioud»
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

package matheos.graphic.fonctions;

import java.awt.Color;
import matheos.graphic.Module;
import matheos.graphic.OngletGraph;
import matheos.graphic.Repere;
import static matheos.graphic.fonctions.ModuleFonctions.*;
import matheos.utils.boutons.MenuDeroulant;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import matheos.utils.managers.PermissionManager;
import static matheos.utils.managers.PermissionManager.ACTION.TRACER_FONCTION;

/**
 *
 * @author François Billioud
 */
public class OngletFonctions extends OngletGraph {
    
    public static final int ACTION_TRACE = ModuleFonctions.TRACE;

    private class Couleur extends MenuDeroulant {
        private void setSelectedColor(Color c) {setSelectedIndex(Arrays.asList(Module.COULEURS).indexOf(c));}
        private Couleur() {
            super(Module.REF_COULEURS, "graphic color");
            this.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    int index = OngletFonctions.Couleur.this.getSelectedIndex();
                    getController().setCouleur(Module.COULEURS[index]);
                }
            });
        }
    }

    public OngletFonctions() {
        super(new ModuleFonctions());
        Module module = getModule();
        
        //parametrage du repère pour l'espace dessin
        getEspaceDessin().getRepere().setProperties(true, true, true, true, true, true);
        setDefaultArea();
        
        barreOutils.addSeparateurOnRight();
//        barreOutilsFonction.addBoutonOnRight(new ActionNouveau());
        barreOutils.addBoutonOnRight(module.getAction(TRACE));
        barreOutils.addSwitchOnRight(module.getAction(DROITE));
        barreOutils.addSwitchOnRight(module.getAction(SEGMENTS));
        barreOutils.addSwitchOnRight(module.getAction(POINT));
        barreOutils.addComponentOnRight(new Couleur());
        barreOutils.addSeparateurOnRight();
        barreOutils.addSwitchOnRight(module.getAction(RENOMMER));
        barreOutils.addSwitchOnRight(module.getAction(SUPPRIMER));
        barreOutils.addSwitchOnRight(module.getAction(TEXTE));
        barreOutils.addSwitchOnRight(module.getAction(COLORER));
        barreOutils.addSwitchOnRight(module.getToggleAction(DRAGAGE));

        //Menu déroulant Options
        setMenuOptions(new OptionsFonctions(getEspaceDessin().getRepere()));

    }
    
    private class OptionsFonctions extends OngletGraph.OptionsGraph {
        private OptionsFonctions(Repere repere) {
            super(repere);
            addCheckBox(getModule().getAction(ACTION_POINTILLES_LECTURE));
            addCheckBox(getModule().getAction(ACTION_COORDONNEES_CURSEUR));
            addCheckBox(getModule().getAction(XPLUS));
        }
    }

    @Override
    public void setActionEnabled(PermissionManager.ACTION actionID, boolean b) {
        switch(actionID) {
            case TRACER_FONCTION : getModule().getAction(TRACE).setEnabled(b); break;
        }
    }
    
    @Override
    protected final void setDefaultArea() {
        getEspaceDessin().getRepere().setArea(-2.0, 8.0, -2.0, 8.0, 1, 1);
    }

}
