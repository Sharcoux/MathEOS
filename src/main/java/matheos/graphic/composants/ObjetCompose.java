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

package matheos.graphic.composants;

import matheos.graphic.ListComposant;
import matheos.graphic.Repere;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class ObjetCompose extends ComposantGraphique implements Composant.Intersectable {

    ListComposant composants;

    public ObjetCompose(ListComposant L) {
        composants = L;
    }

    @Override
    public void setCouleur(Color couleur) {
        super.setCouleur(couleur);
        for(ComposantGraphique cg : composants) cg.setCouleur(couleur);
    }

    @Override
    public void passif(boolean b) {
        super.passif(b);
        for(ComposantGraphique cg : composants) cg.passif(b);
    }

    @Override
    protected void dessineComposant(Repere repere, Graphics2D g2D) {
        composants.dessine(repere, g2D);
    }
    
    @Override
    public String getSVGRepresentation(Repere repere) {
        String s = "";
        for(ComposantGraphique cg : composants) {
            s+=cg.getSVGRepresentation(repere)+"\n";
        }
        return s;
    }

    @Override
    public int distance2Pixel(Point point, Repere repere) {
        if(composants==null || composants.get(0)==null) {System.out.println("composants null in distance2Pixel. Class ObjetCompose");return 0;}
        int min = Integer.MAX_VALUE;
        for(ComposantGraphique cg : composants) {
            int d = cg.distance2Pixel(point, repere);
            if(d<min) min = d;
        }
        return min;
    }

    @Override
    public boolean estEgalA(Composant cg) {
        if(cg instanceof ObjetCompose) {
            for(ComposantGraphique c : ((ObjetCompose)cg).composants) {if(!composants.contient(c)) return false;}
            return true;
        } else {
            return composants.size()==1 && cg.estEgalA(composants.get(0));
        }
    }

    @Override
    public List<Point> pointsSupplementaires() {
        List<Point> L = new LinkedList<>();
        ListIterator<ComposantGraphique> iterator = composants.listIterator();
        while(iterator.hasNext()) {
            L.addAll(iterator.next().pointsSupplementaires());
        }
        return L;
    }

    @Override
    public List<Point> pointsDIntersection(Intersectable cg) {
        List<Point> L = new LinkedList<>();
        for(ComposantGraphique composant : composants) {
            if(composant instanceof Intersectable) {
                Intersectable trait = (Intersectable) composant;
                List<Point> LTemp = trait.pointsDIntersection(cg);
                L.addAll(LTemp);
            }
        }
        return L;
    }

    @Override
    public boolean dependsOn(ComposantGraphique cg) {
        return composants.contient(cg);
    }

}
