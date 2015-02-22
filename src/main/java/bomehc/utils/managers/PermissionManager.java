/** «Copyright 2013,2014 François Billioud»
 *
 * This file is part of Bomehc.
 *
 * Bomehc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bomehc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Bomehc. If not, see <http://www.gnu.org/licenses/>.
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
 */

package bomehc.utils.managers;

import bomehc.IHM;
import bomehc.utils.boutons.ActionComplete;
import bomehc.utils.dialogue.DialogueComplet;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;

import javax.swing.JCheckBox;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public abstract class PermissionManager {
    
    public static enum ACTION { CALCULATRICE, CONSULTATION, FONCTIONS, TRACER_FONCTION, CARACTERES_LITTERAUX, CARACTERES_COLLEGE, COMPARATEURS_SPECIAUX, RACINE_CARREE, CARACTERES_AVANCES, FIN_EVALUATION, DEMI_DROITE, POSITION_CURSEUR, PROPORTIONNALITE, OUTILS_PROF };

    private static final ActionCalculatrice actionCalculatrice = new ActionCalculatrice(true);
    private static final ActionConsultation actionConsultation = new ActionConsultation(true);
    private static final ActionCaracteresLitteraux actionCaracteresLitteraux = new ActionCaracteresLitteraux(true);
    private static final ActionCaracteresCollege actionCaracteresCollege = new ActionCaracteresCollege(true);
    private static final ActionComparateursSpeciaux actionComparateursSpeciaux = new ActionComparateursSpeciaux(true);
    private static final ActionRacineCarree actionRacineCarree = new ActionRacineCarree(true);
    private static final ActionTracerMilieu actionTracerMilieu = new ActionTracerMilieu(true);
    private static final ActionTracerParallele actionTracerParallele = new ActionTracerParallele(true);
    private static final ActionTracerMarquage actionTracerMarquage = new ActionTracerMarquage(true);
    private static final ActionAfficherAngles actionAfficherAngles = new ActionAfficherAngles(true);
    private static final ActionPositionCurseur actionPositionCurseur = new ActionPositionCurseur(true);
    private static final ActionFonctions actionFonctions = new ActionFonctions(true);
    private static final ActionTracerFonction actionTracerFonction = new ActionTracerFonction(true);
    private static final ActionCaracteresAvances actionCaracteresAvances = new ActionCaracteresAvances(true);
    private static final ActionDemiDroite actionDemiDroite = new ActionDemiDroite(true);
    private static final ActionProportionnalite actionProportionnalite = new ActionProportionnalite(true);
    private static final ActionOutilsProf actionOutilsProf = new ActionOutilsProf(true);

    private static List<ActionComplete.Toggle> getListActions() {
        List<ActionComplete.Toggle> listeActions = new LinkedList<ActionComplete.Toggle>();
        listeActions.add(actionCalculatrice);
        listeActions.add(actionConsultation);
        listeActions.add(actionCaracteresLitteraux);
        listeActions.add(actionCaracteresCollege);
        listeActions.add(actionComparateursSpeciaux);
        listeActions.add(actionRacineCarree);
        listeActions.add(actionTracerMilieu);
        listeActions.add(actionTracerParallele);
        listeActions.add(actionTracerMarquage);
        listeActions.add(actionAfficherAngles);
        listeActions.add(actionPositionCurseur);
        listeActions.add(actionFonctions);
        listeActions.add(actionTracerFonction);
        listeActions.add(actionCaracteresAvances);
        listeActions.add(actionDemiDroite);
        listeActions.add(actionProportionnalite);
        listeActions.add(actionOutilsProf);
        return listeActions;
    }
        
    public static boolean isCalculatriceAllowed() {return actionCalculatrice.isSelected();}
    public static boolean isConsultationAllowed() {return actionConsultation.isSelected();}
    public static boolean isCaracteresLitterauxAllowed() {return actionCaracteresLitteraux.isSelected();}
    public static boolean isCaracteresCollegeAllowed() {return actionCaracteresCollege.isSelected();}
    public static boolean isComparateursSpeciauxAllowed() {return actionComparateursSpeciaux.isSelected();}
    public static boolean isRacineCarreeAllowed() {return actionRacineCarree.isSelected();}
    public static boolean isTracerMilieuAllowed() {return actionTracerMilieu.isSelected();}
    public static boolean isTracerParalleleAllowed() {return actionTracerParallele.isSelected();}
    public static boolean isTracerMarquageAllowed() {return actionTracerMarquage.isSelected();}
    public static boolean isAfficherAnglesAllowed() {return actionAfficherAngles.isSelected();}
    public static boolean isPositionCurseurAllowed() {return actionPositionCurseur.isSelected();}
    public static boolean isFonctionsAllowed() {return actionFonctions.isSelected();}
    public static boolean isTracerFonctionAllowed() {return actionTracerFonction.isSelected();}
    public static boolean isCaracteresAvancesAllowed() {return actionCaracteresAvances.isSelected();}
    public static boolean isDemiDroiteAllowed() {return actionDemiDroite.isSelected();}
    public static boolean isProportionnaliteAllowed() {return actionProportionnalite.isSelected();}
    public static boolean isOutilsProfAllowed() {return actionOutilsProf.isSelected();}
    
    public static void setCalculatriceAllowed(boolean allowed) {actionCalculatrice.setSelected(allowed);}
    public static void setConsultationAllowed(boolean allowed) {actionConsultation.setSelected(allowed);}
    public static void setCaracteresLitterauxAllowed(boolean allowed) {actionCaracteresLitteraux.setSelected(allowed);}
    public static void setCaracteresCollegeAllowed(boolean allowed) {actionCaracteresCollege.setSelected(allowed);}
    public static void setComparateursSpeciauxAllowed(boolean allowed) {actionComparateursSpeciaux.setSelected(allowed);}
    public static void setRacineCarreeAllowed(boolean allowed) {actionRacineCarree.setSelected(allowed);}
    public static void setTracerMilieuAllowed(boolean allowed) {actionTracerMilieu.setSelected(allowed);}
    public static void setTracerParalleleAllowed(boolean allowed) {actionTracerParallele.setSelected(allowed);}
    public static void setTracerMarquageAllowed(boolean allowed) {actionTracerMarquage.setSelected(allowed);}
    public static void setAfficherAnglesAllowed(boolean allowed) {actionAfficherAngles.setSelected(allowed);}
    public static void setPositionCurseurAllowed(boolean allowed) {actionPositionCurseur.setSelected(allowed);}
    public static void setFonctionsAllowed(boolean allowed) {actionFonctions.setSelected(allowed);}
    public static void setTracerFonctionAllowed(boolean allowed) {actionTracerFonction.setSelected(allowed);}
    public static void setCaracteresAvancesAllowed(boolean allowed) {actionCaracteresAvances.setSelected(allowed);}
    public static void setDemiDroiteAllowed(boolean allowed) {actionDemiDroite.setSelected(allowed);}
    public static void setProportionnaliteAllowed(boolean allowed) {actionProportionnalite.setSelected(allowed);}
    public static void setOutilsProfAllowed(boolean allowed) {actionOutilsProf.setSelected(allowed);}
    
    public static void showPermissions() {
        List<JCheckBox> options = new LinkedList<JCheckBox>();
        for(ActionComplete action : getListActions()) {
            options.add(new JCheckBox(action));
        }
        DialogueComplet dialogue = new DialogueComplet("authorization", options);
    }
    
    public static void readPermissions(String classe) {
        List<ActionComplete.Toggle> liste = getListActions();
        String[] restrictions = Traducteur.getInfoDialogue(classe);
        restriction:
        for(ActionComplete.Toggle action : liste) {
            boolean isRestricted = Arrays.asList(restrictions).contains((String)action.getValue(Action.ACTION_COMMAND_KEY));
            action.setSelected(!isRestricted);
            action.actionPerformed(null);
        }
    }


    private static class ActionCalculatrice extends ActionComplete.Toggle {
        public ActionCalculatrice(boolean b) {super("authorization calculator", b);}
        @Override
        public void actionPerformed(ActionEvent e) {
            IHM.activeAction(ACTION.CALCULATRICE, isSelected());}
    }
    private static class ActionConsultation extends ActionComplete.Toggle {
        public ActionConsultation(boolean b) {super("authorization consultation", b);}
        public void actionPerformed(ActionEvent e) {IHM.activeAction(ACTION.CONSULTATION, isSelected());}
    }
    private static class ActionCaracteresLitteraux extends ActionComplete.Toggle {
        public ActionCaracteresLitteraux(boolean b) {super("authorization litteral characters", b);}
        public void actionPerformed(ActionEvent e) {IHM.activeAction(ACTION.CARACTERES_LITTERAUX, isSelected());}
    }
    private static class ActionCaracteresCollege extends ActionComplete.Toggle {
        public ActionCaracteresCollege(boolean b) {super("authorization standard characters", b);}
        public void actionPerformed(ActionEvent e) {IHM.activeAction(ACTION.CARACTERES_COLLEGE, isSelected());}
    }
    private static class ActionComparateursSpeciaux extends ActionComplete.Toggle {
        public ActionComparateursSpeciaux(boolean b) {super("authorization special comparators", b);}
        public void actionPerformed(ActionEvent e) {IHM.activeAction(ACTION.COMPARATEURS_SPECIAUX, isSelected());}
    }
    private static class ActionRacineCarree extends ActionComplete.Toggle {
        public ActionRacineCarree(boolean b) {super("authorization square root", b);}
        public void actionPerformed(ActionEvent e) {IHM.activeAction(ACTION.RACINE_CARREE, isSelected());}
    }
    private static class ActionCaracteresAvances extends ActionComplete.Toggle {
        public ActionCaracteresAvances(boolean b) {super("authorization advanced characters", b);}
        public void actionPerformed(ActionEvent e) {IHM.activeAction(ACTION.CARACTERES_AVANCES, isSelected());}
    }
    private static class ActionFonctions extends ActionComplete.Toggle {
        public ActionFonctions(boolean b) {super("authorization functions", b);}
        public void actionPerformed(ActionEvent e) {IHM.activeAction(ACTION.FONCTIONS, isSelected());}
    }
    private static class ActionTracerFonction extends ActionComplete.Toggle {
        public ActionTracerFonction(boolean b) {super("authorization function trace", b);}
        public void actionPerformed(ActionEvent e) {IHM.activeAction(ACTION.TRACER_FONCTION, isSelected());}
    }
    private static class ActionDemiDroite extends ActionComplete.Toggle {
        public ActionDemiDroite(boolean b) {super("authorization half-line", b);}
        public void actionPerformed(ActionEvent e) {IHM.activeAction(ACTION.DEMI_DROITE, isSelected());}
    }
    private static class ActionProportionnalite extends ActionComplete.Toggle {
        public ActionProportionnalite(boolean b) {super("authorization proportionality", b);}
        public void actionPerformed(ActionEvent e) {IHM.activeAction(ACTION.PROPORTIONNALITE, isSelected());}
    }
    private static class ActionOutilsProf extends ActionComplete.Toggle {
        public ActionOutilsProf(boolean b) {super("authorization teacher tools", b);}
        public void actionPerformed(ActionEvent e) {IHM.activeAction(ACTION.OUTILS_PROF, isSelected());}
    }
    private static class ActionTracerMilieu extends ActionComplete.Toggle {
        public ActionTracerMilieu(boolean b) {super("authorization middle", b);}
        public void actionPerformed(ActionEvent e) {}
    }
    private static class ActionTracerParallele extends ActionComplete.Toggle {
        public ActionTracerParallele(boolean b) {super("authorization parallel", b);}
        public void actionPerformed(ActionEvent e) {}
    }
    private static class ActionTracerMarquage extends ActionComplete.Toggle {
        public ActionTracerMarquage(boolean b) {super("authorization notation", b);}
        public void actionPerformed(ActionEvent e) {}
    }
    private static class ActionAfficherAngles extends ActionComplete.Toggle {
        public ActionAfficherAngles(boolean b) {super("authorization display angle", b);}
        public void actionPerformed(ActionEvent e) {}
    }
    private static class ActionPositionCurseur extends ActionComplete.Toggle {
        public ActionPositionCurseur(boolean b) {super("authorization display cursor position", b);}
        public void actionPerformed(ActionEvent e) {IHM.activeAction(ACTION.POSITION_CURSEUR, isSelected());}
    }
    
    private PermissionManager() {throw new AssertionError("instanciating utilitary class");}
}
