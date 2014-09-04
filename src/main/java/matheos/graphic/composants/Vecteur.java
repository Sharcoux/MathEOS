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

import matheos.graphic.Repere;
import java.io.Serializable;

/**
 *
 * @author François Billioud
 */
public class Vecteur implements Serializable {
    private static final long serialVersionUID = 1L;

    private double x=0;
    private double y=0;

    public Vecteur(double xCoord, double yCoord ) {
        x=xCoord;
        y=yCoord;
    }

    public Vecteur(Point A, Point B) {
        x = B.x() - A.x();
        y = B.y() - A.y();
    }

    public Vecteur vecteurOrthogonal() {
        return new Vecteur(-y,x);
    }

    public boolean estParallele(Vecteur u) {
        return (Math.abs(x*u.y() - y*u.x()) < Repere.ZERO_ABSOLU);
    }

    public boolean estDeMemeSigne(Vecteur u) {
        return prodScal(u)>=0;
    }

    public double coefProp(Vecteur u) {
        if(Math.abs(x)<Repere.ZERO_ABSOLU) {
            if(Math.abs(y)<Repere.ZERO_ABSOLU) {return 0;}
            else {return u.y()/y;}
        }
        else {
            return u.x()/x;
        }
    }

    public double longueur() {
        return Math.sqrt(x*x+y*y);
    }

    public double prodScal(Vecteur u) {
        return x*u.x() + y*u.y();
    }

    public Vecteur plus(Vecteur v) {
        return new Vecteur(x+v.x(),y+v.y());
    }

    public Vecteur moins(Vecteur v) {
        return new Vecteur(x-v.x(),y-v.y());
    }

    public Vecteur fois(double c) {
        return new Vecteur(c*x,c*y);
    }

    public Vecteur unitaire() {
        double l = this.longueur();
        if(Math.abs(l)>Repere.ZERO_ABSOLU) {return this.fois(1/l);}
        else {return this;}
    }

    /**
     * Effectue une rotation du vecteur de la valeur de l'angle passé en paramètre
     * @param angle l'angle en radian
     * @return le nouveau vecteur
     */
    public Vecteur rotation(double angle) {
        double nx = x*Math.cos(angle) - y*Math.sin(angle);
        double ny = y*Math.cos(angle) + x*Math.sin(angle);
        return new Vecteur(nx,ny);
    }

    /**
     * Renvoie l'angle que fait le vecteur avec l'axe horizontal (Ox)\n
     * L'angle est compris entre -PI et PI
     * @return : angle en radian
     */
    public double orientation() {
        double angle = Math.acos(x/Math.sqrt(x*x+y*y));
        if(y>0) {return angle;}
            else {return -angle;}
    }
	
    public double x() {return x;}
    public double y() {return y;}
    
    public boolean isNull() {
        return Math.abs(x)<Repere.ZERO_ABSOLU && Math.abs(y)<Repere.ZERO_ABSOLU;
    }
}
