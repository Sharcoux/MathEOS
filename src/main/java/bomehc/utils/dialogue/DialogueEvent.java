/** «Copyright 2014 François Billioud»
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

package bomehc.utils.dialogue;

import java.util.EventObject;
import java.util.Map;
import javax.swing.JDialog;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class DialogueEvent extends EventObject {

    public static final String CLOSE_BUTTON = "closed";
    public static final String OK_BUTTON = "ok";
    public static final String CANCEL_BUTTON = "cancel";
    public static final String YES_BUTTON = "yes";
    public static final String NO_BUTTON = "no";
    
    private String command;
    private Map<String, ? extends Object> inputs;
    
    public DialogueEvent(JDialog source, String buttonCommand, Map<String, ? extends Object> inputs) {
        super(source);
        this.command = buttonCommand;
        this.inputs = inputs;
    }
    
    public boolean isConfirmButtonPressed() {return command.equals(OK_BUTTON) || command.equals(YES_BUTTON);}
    public boolean isNoButtonPressed() {return command.equals(NO_BUTTON);}
    public boolean isCancelButtonPressed() {return command.equals(CANCEL_BUTTON);}
    public boolean isCloseButtonPressed() {return command.equals(CLOSE_BUTTON);}

    @Override
    public JDialog getSource() {return (JDialog)super.getSource();}
    public String getCommand() {return command;}
    
    public Map<String, ? extends Object> getInputs() {return inputs;}
    
    public String getInputString(String id) {
        Object o = inputs.get(id);
        if(o instanceof String) {return (String)o;}
        return null;
    }
    
    public Double getInputDouble(String id) {
        Object o = inputs.get(id);
        if(o instanceof String) {
            try {
                double d = Double.parseDouble(((String)o).replace(',', '.'));
                return d;
            }
            catch(NumberFormatException ex) {return null;}
        } else if(o instanceof Double) {
            return (Double)o;
        }
        return null;
    }
    
    public Integer getInputInteger(String id) {
        Object o = inputs.get(id);
        if(o instanceof String) {
            try {
                int i = Integer.parseInt((String)o);
                return i;
            }
            catch(NumberFormatException ex) {return null;}
        } else if(o instanceof Integer) {
            return (Integer)o;
        }
        return null;
    }
    
    public Boolean getInputBoolean(String id) {
        Object o = inputs.get(id);
        if(o instanceof String) {
            try {
                boolean b = Boolean.parseBoolean((String)o);
                return b;
            }
            catch(NumberFormatException ex) {return null;}
        } else if(o instanceof Boolean) {
            return (Boolean)o;
        }
        return null;
    }

    public Object getInputObject(String id) {
        return inputs.get(id);
    }

}
