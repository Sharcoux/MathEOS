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

package matheos.utils.dialogue;

import matheos.IHM;
import matheos.utils.managers.Traducteur;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JOptionPane;

/**
 *
 * @author François Billioud
 */
public abstract class DialogueBloquant {

    private static final String TITLE_MARK = " title";
    private static final String EXPLANATION_MARK = " message";
    
    public static enum MESSAGE_TYPE {ERROR(JOptionPane.ERROR_MESSAGE),
                        INFORMATION(JOptionPane.INFORMATION_MESSAGE),
                        PLAIN(JOptionPane.PLAIN_MESSAGE),
                        QUESTION(JOptionPane.QUESTION_MESSAGE),
                        WARNING(JOptionPane.WARNING_MESSAGE);
        private final int message;
        MESSAGE_TYPE(int message) {
            this.message = message;
        }
        int getMessage() {return message;}
    };
    
    public static enum CHOICE {OK(JOptionPane.OK_OPTION),
                        NO(JOptionPane.NO_OPTION),
                        YES(JOptionPane.YES_OPTION),
                        CANCEL(JOptionPane.CANCEL_OPTION),
                        CLOSED(JOptionPane.CLOSED_OPTION);
        private final int choice;
        CHOICE(int choice) {
            this.choice = choice;
        }
        int getChoice() {return choice;}
    };
    
    public static enum OPTION {OK_CANCEL(JOptionPane.OK_CANCEL_OPTION),
                        DEFAULT(JOptionPane.DEFAULT_OPTION),
                        YES_NO(JOptionPane.YES_NO_OPTION),
                        YES_NO_CANCEL(JOptionPane.YES_NO_CANCEL_OPTION);
        private int option;
        OPTION(int option) {
            this.option = option;
        }
        int getOption() {return option;}
    };

    /** Convertit un résultat JOptionPane en résultat DialogueBloquant **/
    private static CHOICE getChoice(int option, OPTION options) {
        List<CHOICE> toConsider = new LinkedList<CHOICE>();
        if(options.toString().contains("OK")) {toConsider.add(CHOICE.OK);}
        if(options.toString().contains("YES")) {toConsider.add(CHOICE.YES);}
        if(options.toString().contains("NO")) {toConsider.add(CHOICE.NO);}
        if(options.toString().contains("CANCEL")) {toConsider.add(CHOICE.CANCEL);}
        toConsider.add(CHOICE.CLOSED);
        for(CHOICE c : toConsider) {
            if(option==c.getChoice()) {return c;}
        }
        return null;
    }
    
    public static CHOICE dialogueBloquant(String aspect, Object... parametres) {
        return dialogueBloquant(aspect, MESSAGE_TYPE.PLAIN ,OPTION.DEFAULT);
    }
    public static CHOICE dialogueBloquant(String aspect, MESSAGE_TYPE messageType, OPTION options, Object... parametres) {
        return dialogueBloquant(aspect, messageType, options, null, parametres);
    }
    public static CHOICE dialogueBloquant(String aspect, MESSAGE_TYPE messageType, OPTION options, Icon icon, Object... parametres) {
        String title = Traducteur.traduire(aspect+TITLE_MARK);
        String explanation = Traducteur.traduire(aspect+EXPLANATION_MARK);
        if(parametres.length!=0) {
            explanation = String.format(explanation, parametres);
        }
        return dialogueBloquant(title, explanation, messageType, options, icon);
    }
    public static CHOICE dialogueBloquant(String title, String explanation, MESSAGE_TYPE messageType, OPTION options) {
        return dialogueBloquant(title, explanation, messageType, options, null);
    }
    public static CHOICE dialogueBloquant(String title, String explanation, MESSAGE_TYPE messageType, OPTION options, Icon icon) {
        int result = JOptionPane.showConfirmDialog(IHM.getMainWindow(), explanation, title, options.getOption(), messageType.getMessage(), icon);
        return getChoice(result, options);
    }
    
    public static void error(String aspect, Object... parametres) {
        dialogueBloquant(aspect, MESSAGE_TYPE.ERROR, OPTION.DEFAULT, parametres);
    }
    public static void error(String title, String message) {
        dialogueBloquant(title, message, MESSAGE_TYPE.ERROR, OPTION.DEFAULT);
    }
    
    public static void warning(String aspect, Object... parametres) {
        dialogueBloquant(aspect, MESSAGE_TYPE.WARNING, OPTION.DEFAULT, parametres);
    }
    public static void warning(String title, String message) {
        dialogueBloquant(title, message, MESSAGE_TYPE.WARNING, OPTION.DEFAULT);
    }
    
    public static String input(String aspect, Object... parametres) {
        String message = Traducteur.traduire(aspect+EXPLANATION_MARK);
        if(parametres.length!=0) {message = String.format(message, parametres);}
        return JOptionPane.showInputDialog(IHM.getMainWindow(), message, Traducteur.traduire(aspect+TITLE_MARK), MESSAGE_TYPE.QUESTION.getMessage());
    }
    public static String input(String aspect, String initialValue, Object... parametres) {
        return JOptionPane.showInputDialog(IHM.getMainWindow(), Traducteur.traduire(aspect+EXPLANATION_MARK), initialValue);
    }

    private DialogueBloquant() { throw new AssertionError("instantiating utilitary class"); }
}
