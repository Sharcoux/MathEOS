/**
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of Bomehc
 *
 * Bomehc is free software: you can redistribute it and/or modify
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
 *
 **/

package bomehc.utils.librairies;

import java.awt.Dimension;
import java.awt.Insets;

/**
 * Permet d'effectuer des opérations sur les dimensions.
 * @author François Billioud
 */
public class DimensionTools {
    
    public static class DimensionT extends Dimension {
        public DimensionT(int width, int height) {super(width, height);}
        public DimensionT(Dimension d) {super(d);}
        
        public DimensionT plus(Insets insets) {
            return new DimensionT(width+insets.left+insets.right,height+insets.bottom+insets.top);
        }
        public DimensionT plus(int width, int height) {
            return new DimensionT(width + this.width, height + this.height);
//            this.width += width; this.height += height;
//            return this;//pour le chainage
        }

        public DimensionT plus(Dimension d) {
            return new DimensionT(width + d.width, height + d.height);
//            width += d.width; height += d.height;
//            return this;//pour le chainage
        }

        public DimensionT moins(Dimension d) {
            return new DimensionT(width - d.width, height - d.height);
//            width -= d.width; height -= d.height;
//            return this;//pour le chainage
        }

        public DimensionT fois(double d) {
            return new DimensionT((int)(width * d),(int) (height * d));
//            width *= d; height *= d;
//            return this;//pour le chainage
        }

        public DimensionT max(Dimension d) {
            return new DimensionT(Math.max(width, d.width), Math.max(height, d.height));
//            width = Math.max(width, d.width); height = Math.max(height, d.height);
//            return this;//pour le chainage
        }

        public DimensionT min(Dimension d) {
            return new DimensionT(Math.min(width, d.width), Math.min(height, d.height));
//            width = Math.min(width, d.width); height = Math.min(height, d.height);
//            return this;//pour le chainage
        }
        
        @Override
        public String toString() {
            return "["+width+","+height+"]";
        }
    }

    public static DimensionT plus(Dimension d1, Dimension d2) {
        return new DimensionT(d1).plus(d2);
    }
    
    public static DimensionT moins(Dimension d1, Dimension d2) {
        return new DimensionT(d1).moins(d2);
    }
    
    public static DimensionT max(Dimension d1, Dimension d2) {
        return new DimensionT(d1).max(d2);
    }
    
    public static DimensionT min(Dimension d1, Dimension d2) {
        return new DimensionT(d1).min(d2);
    }
    
    public static DimensionT fois(Dimension d, double value) {
        return new DimensionT(d).fois(value);
    }
    
}
