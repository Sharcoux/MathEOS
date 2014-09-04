/*
 * Copyright (C) 2014 François Billioud
 * 
 * This file is part of MathEOS.
 *
 * MathEOS is a free software: you can redistribute it and/or modify
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

package matheos.utils.managers;

import matheos.IHM;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author François Billioud
 */
public abstract class CursorManager {
    
    private static Cursor CLIC_DROIT;
    private static Cursor HORIZONTAL;
    private static Cursor NORMAL;
    private static Cursor WAIT;
    private static Cursor TEXT_MEDIUM;
    private static Cursor TEXT_BIG;
    private static Cursor TEXT_SMALL;
    public static final int RIGHT_CLIC_CURSOR = Cursor.CUSTOM_CURSOR;
    public static final int TEXT_BIG_CURSOR = 14;
    public static final int TEXT_MEDIUM_CURSOR = 15;
    public static final int TEXT_SMALL_CURSOR = 16;
    static {
        try {
            CLIC_DROIT = Toolkit.getDefaultToolkit().createCustomCursor(ImageIO.read(new File(IHM.getThemeElement("cursor rightclic"))), new java.awt.Point(0,0), "right clic available");
            HORIZONTAL = Toolkit.getDefaultToolkit().createCustomCursor(ImageIO.read(new File(IHM.getThemeElement("cursor horizontal"))), new java.awt.Point(15,15), "horizontal resize");
            NORMAL = Toolkit.getDefaultToolkit().createCustomCursor(ImageIO.read(new File(IHM.getThemeElement("cursor"))), new java.awt.Point(0,0), "default");
            WAIT = Toolkit.getDefaultToolkit().createCustomCursor(ImageIO.read(new File(IHM.getThemeElement("cursor waiting"))), new java.awt.Point(0,0), "default");
            TEXT_MEDIUM = Toolkit.getDefaultToolkit().createCustomCursor(ImageIO.read(new File(IHM.getThemeElement("cursor text"))), new java.awt.Point(15,15), "text");
            TEXT_BIG = Toolkit.getDefaultToolkit().createCustomCursor(ImageIO.read(new File(IHM.getThemeElement("cursor text big"))), new java.awt.Point(15,15), "text big");
            TEXT_SMALL = Toolkit.getDefaultToolkit().createCustomCursor(ImageIO.read(new File(IHM.getThemeElement("cursor text small"))), new java.awt.Point(15,15), "text small");
        } catch (IOException ex) {
            Logger.getLogger(CursorManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Cursor getCursor(int i) {
        Cursor c;
        switch(i) {
            case Cursor.E_RESIZE_CURSOR:
            case Cursor.W_RESIZE_CURSOR:
                c = HORIZONTAL;
                break;
            case Cursor.DEFAULT_CURSOR: c = NORMAL; break;
            case Cursor.WAIT_CURSOR: c = WAIT; break;
            case Cursor.CUSTOM_CURSOR: c = CLIC_DROIT; break;
            case Cursor.TEXT_CURSOR: c = TEXT_SMALL; break;
            case TEXT_BIG_CURSOR: c = TEXT_BIG; break;
            case TEXT_MEDIUM_CURSOR: c = TEXT_MEDIUM; break;
            case TEXT_SMALL_CURSOR: c = TEXT_SMALL; break;
            default: c = Cursor.getPredefinedCursor(i);
        }
        return c!=null ? c : Cursor.getPredefinedCursor(i);
    }

}
