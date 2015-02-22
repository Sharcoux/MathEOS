/** «Copyright 2013 François Billioud»
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
import java.awt.Color;

/**
 * Cette classe a pour but de permettre la lecture des couleurs depuis le fichier de thème
 * A terme, il devra être possible de spécifier des particularités comme l'augmentation des contrastes
 * permettant de prendre en compte certains handicaps supplémentaires.
 * @author François Billioud
 */
public abstract class ColorManager {

    public static Color get(String balise) {
        String s = IHM.getThemeElement(balise,true);
        int intValue = (int) Long.decode(s).intValue();
        return new Color(intValue, true);
    }
    
    /** Transforme une chaîne hexa #XXXXXX en une couleur **/
    public static Color getColorFromHexa(String hexa) {
        return new Color((int) Long.decode(hexa).intValue());
    }

    /** retourne une couleur en hexadécimal sous la forme #XXXXXX **/
    public static String getRGBHexa(Color color) {
        return String.format("#%06X", (0xFFFFFF & color.getRGB()));
    }
    
    /** retourne une couleur en hexadécimal sous la forme #XXXXXX **/
    public static String getRGBHexa(String balise) {
        String color = IHM.getThemeElement(balise);
        return "#"+color.substring(color.length()==8?2:4);//transforme 0xXXXXXXXX ou 0xXXXXXX en #XXXXXX pour le html
    }

    public static Color transparent() {
        return new Color(0,0,0,0);
    }

    public static Color reverse(Color c) {
        return new Color(255-c.getRed(),255-c.getGreen(),255-c.getBlue(),255);
    }

}
