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

package matheos.utils.dialogue;

import matheos.IHM;
import matheos.utils.managers.Traducteur;
import matheos.utils.texte.JLimitedMathTextPane;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author François Billioud
 */
public class InfoDialogueComplet {

    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String BUTTONS = "buttons";
    
    public static final String INF_PROPERTY = "inf";
    public static final String SUP_PROPERTY = "sup";
    public static final String TYPE_PROPERTY = "type";
    public static final String EMPTY_ALLOWED_PROPERTY = "empty";
    public static final String TOOLTIP_MARK = " description";
    
    private static final String COMPONENT_PREFIX = "dialog component ";
    private static int lastComponentID = 0;

    /** le titre de la boite de dialogue **/
    public String titre;
    /** le message à afficher dans la boîte de dialogue **/
    public String description;
    /** l'id des input à demander à l'utilisateur **/
    public List<String> componentsIds;
    
    private Map<String, InfoComposant> infosComposants;
    /** les boutons à afficher en bas du dialogue **/
    public String[] buttons;

    public InfoComposant getInfosComposant(String id) {return infosComposants.get(id);}

    /** Construit une boite de dialogue en lisant ses propriétés dans les fichiers de langue et de thème **/
    public InfoDialogueComplet(String balise) {
        this(balise, readInfosLangue(balise));
    }
    
    /** Construit une boite de dialogue en précisant les composants à afficher pour l'input utilisateur **/
    public InfoDialogueComplet(String balise, List<? extends JComponent> components) {
        this(balise, components, null);
    }
    
    /** Construit une boite de dialogue en précisant les composants à afficher et leurs ids pour l'input utilisateur **/
    public InfoDialogueComplet(String balise, String[] ids, JComponent[] components) {
        this(balise, ids, components, null);
    }
    
    /** Construit une boite de dialogue en précisant les composants et les boutons à afficher
     * 
     * @param balise aspect du dialogue, permettra la lecture dans les fichiers de langue et de thème
     * @param components les composants à afficher, un par ligne. Le name servira d'id. Un label peut être affiché à côté s'il figure dans le fichier de langue
     * @param buttons tableau contenant les componentsIds des boutons à afficher. On détecte le bouton utilisé grâce à la méthode getCommand du DialogueEvent
     */
    public InfoDialogueComplet(String balise, List<? extends JComponent> components, String[] buttons) {
        this(balise, readInfosLangue(balise), components, buttons);
    }

    public InfoDialogueComplet(String balise, String[] ids, JComponent[] components, String[] buttons) {
        this(balise, readInfosLangue(balise), affectIdToComponents(ids, components), buttons);
    }
    
    /** On lit les infos de thème **/
    private InfoDialogueComplet(String balise, Map<String, String> langue, List<? extends JComponent> components, String[] buttons) {
        this(langue, new InfosTheme(balise, components, buttons, langue));
    }
    
    private InfoDialogueComplet(String balise, Map<String, String> langue) {
        this(langue, new InfosTheme(balise, langue));
    }
    
    private InfoDialogueComplet(Map<String, String> langue, InfosTheme infosTheme) {
        //information de langue
        this.titre = langue.get(TITLE);
        this.description = langue.get(DESCRIPTION);

        //informations de thème
        this.componentsIds = infosTheme.componentsIds;
        this.buttons = infosTheme.buttons;
        this.infosComposants = infosTheme.infosComposants;
        
        //nettoyage
        groupsMap.clear();
    }

    private static class InfosTheme {
        private List<String> componentsIds = new LinkedList<>();
        private Map<String, InfoComposant> infosComposants = new HashMap<>();
        private String[] buttons;
        /** Lit les composants depuis le fichier de thème **/
        private InfosTheme(String balise, Map<String, String> langue) {
            String[] infoTheme = IHM.getThemeElementBloc(balise);
            if(infoTheme==null) return;
            for(String info : infoTheme) {
                String[] infoTab = splitAndTrim(info,"::");
                String id = infoTab[0], parametres = infoTab[1];
                if(infoTab[0].equals(BUTTONS)) {this.buttons = splitAndTrim(infoTab[1],",");}
                else {
                    InfoComposant infoComp = new InfoComposant(id, langue.get(id), parametres);
                    String tooltip = langue.get(id+TOOLTIP_MARK);
                    if(tooltip!=null) {infoComp.composant.setToolTipText(tooltip);}
                    this.componentsIds.add(infoComp.aspect);
                    this.infosComposants.put(infoComp.aspect, infoComp);
                }
            }
        }
        /** Utilise les composants fournis en paramètre **/
        private InfosTheme(String balise, List<? extends JComponent> composants, String[] buttons, Map<String, String> langue) {
            this(balise, langue);
            for(JComponent c : composants) {
                String id = c.getName();
                if(id == null || id.isEmpty()) {id = COMPONENT_PREFIX+(lastComponentID++);}//Crée un nom fictif qui servira d'id
                else {
                    String tooltip = langue.get(id+TOOLTIP_MARK);
                    if(tooltip!=null) {c.setToolTipText(tooltip);}
                }
                boolean alreadyRegistered = infosComposants.put(id, new InfoComposant(id, langue.get(id), c))!=null;
                if(!alreadyRegistered) {componentsIds.add(id);}
            }
            this.buttons = buttons;
        }
    }

    /** Lit les informations du dialogue dans le fichier langue **/
    static Map<String, String> readInfosLangue(String balise) {
        Map<String, String> langueData = new HashMap<>();
        String[] infoLangue = Traducteur.getInfoDialogue(balise);
        for(String info : infoLangue) {
            String[] T = splitAndTrim(info,"::");
            if(T.length>0) {langueData.put(T[0], T[1]);}
        }
        return langueData;
    }

    /** affecte les componentsIds passés en paramètre aux composants **/
    private static List<JComponent> affectIdToComponents(String[] aspects, JComponent[] components) {
        int n = Math.max(aspects.length, components.length);
        List<JComponent> reponse = new LinkedList<>();
        for(int i = 0; i<n; i++) {
            components[i].setName(aspects[i]);
            reponse.add(components[i]);
        }
        return reponse;
    }

    public static class InfoComposant {
        public final String aspect;
        public final String label;
        public final JComponent composant;
        private InfoComposant(String aspect, String label, JComponent composant) {
            this.aspect = aspect;
            this.label = label;
            this.composant = composant;
        }
        private InfoComposant(String aspect, String label, String parameters) {
            this(aspect, label, createComponentFromParameters(parameters));
        }
    }
    
    private static Map<String, ButtonGroup> groupsMap = new HashMap<>();//TODO : cette map static est vraiment pas top
        
    private static JComponent createComponentFromParameters(String parameters) {
        String[] parametersTab = splitAndTrim(parameters,";");
        String classe = parametersTab[0];
        Map<String, String> parametersMap = new HashMap<>();
        for(int i = 1; i<parametersTab.length; i++) {
            String[] key_value = splitAndTrim(parametersTab[i],"=");
            parametersMap.put(key_value[0],key_value[1]);
        }
        
        JComponent component;
        switch(classe) {
            case "JMathTextPane" : component = new JLimitedMathTextPane(Integer.parseInt(parametersMap.get("width"))); break;
            case "JTextField" :
                component = new JTextField(Integer.parseInt(parametersMap.get("width")));
                if(parametersMap.get("type")!=null) {}
                break;
            case "JComboBox" : component = new JComboBox(Traducteur.getInfoDialogue(parametersMap.get("content"))); break;
            case "JCheckBox" : component = new JCheckBox((String)null, Boolean.parseBoolean(parametersMap.get("check"))); break;//PENDING renommer getInfoDialogue
            case "JRadioButton" :
                boolean initialState = parametersMap.get("check") == null ? false : Boolean.parseBoolean(parametersMap.get("check"));
                component = new JRadioButton((String)null, initialState);
                ButtonGroup group = groupsMap.get(parametersMap.get("group"));
                if(group==null) {group = new ButtonGroup(); groupsMap.put(parametersMap.get("group"), group);}
                group.add((AbstractButton)component);
                break;
            default : return null;
        }
        String[] basicProperties = {TYPE_PROPERTY,  EMPTY_ALLOWED_PROPERTY, INF_PROPERTY, SUP_PROPERTY};
        for(String property : basicProperties) {
            if(parametersMap.containsKey(property)) {component.putClientProperty(property, parametersMap.get(property));}
        }
        return component;
    }

    private static String[] splitAndTrim(String toSplit, String regex) {
        String[] T = toSplit.split(regex);
        for(int i = 0; i<T.length; i++) {T[i] = T[i].trim();}
        return T;
    }
}
