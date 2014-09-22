/** «Copyright 2012 François Billioud»
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

package matheos.patchs;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataProfil;

/**
 *
 * @author François Billioud
 */
public abstract class Patch {

    /**
     * cette valeur représente la version de MathEOS la plus ancienne
     * supportée par ce patch, càd la dernière version qui peut se dispenser
     * de ce patch. si la version est plus petite, il faut appliquer le patch.
     * Les numéros de version croissent de 1 à +∞.
     */
    protected abstract int getLastSupportedVersion();

    /**
     * renvoie true s'il est nécessaire d'appliquer le patch sur un fichier datant de cette version
     * @param version le numéro de version (croissant de 1 à +∞)
     * @return true si le patch doit être appliqué.
     */
    public boolean isNecessary(long version) {
        return version<getLastSupportedVersion();
    }

    /**
     * applique le patch au fichier dont l'adresse est passée en paramètre
     * @param version valeur de la version du fichier au lancement du patch
     * @param adresseFichier adresse absolue du fichier
     */
    private void patcher(DataProfil profil, long version) {
        if(this.isNecessary(version)) {
            Patch previous = previous();
            if(previous!=null) {previous.patcher(profil, version);}
            boolean succeed = this.apply(profil);
            if(succeed) {
                profil.updateVersion(getLastSupportedVersion());
                System.out.println(this.getClass().getName()+" successfully applied on file "+profil.getAdresseFichier());
            } else {
                System.out.println(this.getClass().getName()+" failed to apply on file "+profil.getAdresseFichier());
            }
        }
    }

    /**
     * applique le patch au fichier dont passé en paramètre
     * @param profil données du profil à modifier
     */
    protected abstract boolean apply(DataProfil profil);

    /**
     * Renvoie le précédent patch. Si l'application de ce patch présuppose
     * l'application d'un patch précédent, cette fonction doit renvoyer le
     * patch dépendant. null sinon
     *
     * @return le patch supposé avoir été appliqué ou null si aucun patch nécessaire
     */
    protected abstract Patch previous();

    /**
     * A la création d'un nouveau patch, ne pas oublier de modifier cette fonction
     */
    private static Patch getLastPatch() {return new Patch002();}
    
    public static DataProfil patcher(DataProfil data) {
        getLastPatch().patcher(data, data.getVersion());
        return data;
    }
    
    protected static <T extends Data> List<T> getAllElements(Class<T> classe, Data data) {
        List<T> L = new LinkedList<T>();
        for(Map.Entry<String, Data> entry : data.getDataEntries()) {
            L.addAll(getAllElements(classe, entry.getValue()));
        }
        if(classe.isInstance(data)) {L.add((T) data);}
        return L;
    }
}
