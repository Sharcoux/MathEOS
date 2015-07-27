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

package matheos.utils.managers;

import matheos.IHM;
import java.awt.Color;

/**
 * Cette classe a pour but de permettre la lecture des couleurs depuis le fichier de thème
 * A terme, il devra être possible de spécifier des particularités comme l'augmentation des contrastes
 * permettant de prendre en compte certains handicaps supplémentaires.
 * @author François Billioud
 */
public abstract class ColorManager {

    public static final String COLOR_PREFIX = "color ";
    
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

    public static Color[] getListCouleurs(String balise) {
        String[] colorsHexa = IHM.getThemeElementBloc(COLOR_PREFIX+balise);
        Color[] colors = new Color[colorsHexa.length];
        for(int i = 0; i<colorsHexa.length; i++) {
            colors[i] = ColorManager.getColorFromHexa(colorsHexa[i]);
        }
        return colors;
    }

    public static Color transparent() {
        return new Color(0,0,0,0);
    }

    public static Color reverse(Color c) {
        return new Color(255-c.getRed(),255-c.getGreen(),255-c.getBlue(),255);
    }
    
    public static Color enlight(Color c) {
        int r = c.getRed(), g = c.getGreen(), b = c.getBlue(), a = c.getAlpha();
        return new Color(moveLight(r),moveLight(g),moveLight(b),a);
    }
    private static int moveLight(int value) {
        int d = 30+Math.abs(127-value)/2;
        return value + (value>127?-20:d);
    }

}
