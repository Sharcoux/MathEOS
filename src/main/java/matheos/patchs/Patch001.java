/**
 * Copyright (C) 2014 François Billioud
 *
 * This file is part of MathEOS
 *
 * MathEOS is free software: you can redistribute it and/or modify
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
 *
 **/

package matheos.patchs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import matheos.json.Json;
import matheos.sauvegarde.DataProfil;

/**
 * Ce patch rend compatible les fichiers MathEOS et Bomehc
 * @author François Billioud
 */
public class Patch001 extends Patch {

    @Override
    protected int getLastSupportedVersion() {
        //retourne la plus vieille version n'ayant pas besoin de ce patch.
        return 4;
    }

    @Override
    protected boolean apply(DataProfil profil) {
        try {
            String content = Json.toJson(profil);
            content = content.replaceAll("bomehc-component", "special-math-component");
            content = content.replaceAll("matheos-component", "special-math-component");
            DataProfil pBis = (DataProfil) Json.toJava(content,DataProfil.class);
            profil.putAll(pBis);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Patch001.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    protected Patch previous() {
        //retourne le précédent patch qui doit être appliqué avant celui-ci. null si aucun patch nécessaire
        return null;
    }
}
