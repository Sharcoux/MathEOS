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

package matheos.texte.composants;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import matheos.sauvegarde.DataTexte;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.Traducteur;
import matheos.utils.texte.EditeurIO;
import matheos.utils.texte.EditeurKit;
import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.texte.JMathTextPane;
import org.jsoup.Jsoup;

/**
 *
 * @author François Billioud
 */
public class JHeader extends JPanel implements ComposantTexte {
    
    private long id = System.currentTimeMillis();
    
    public JHeader() {
        setLayout(new BorderLayout());
        markPanel.add(noteLabel);
        markPanel.add(note);
        editeur.setText("Observations :");
        setBorder(BorderFactory.createLineBorder(Color.red));
    }
    
    public JHeader(String noteValue, String noteMax, DataTexte observations) {
        this();
        note.setValues(noteValue, noteMax);
        editeur.charger(observations);
    }
    
    private final JPanel markPanel = new JPanel();
    private final JMathTextPane editeur = new JLimitedMathTextPane(10, false);
    private final JLabelNote note = new JLabelNote("", Traducteur.traduire("mark max value"), 40, 40);
    private final JLabel noteLabel = new JLabel(Traducteur.traduire("test mark")+" : ");

    @Override
    public String getHTMLRepresentation() {
        String cBord = ColorManager.getRGBHexa("color border test");
        String noteName = Traducteur.traduire("test mark");
//        String observations = Traducteur.traduire("test remark");
        int fontSize = EditeurKit.TAILLES_PT[0];
        return
"                        <div id='table' style='padding-top:20px;'>"+
"                            <table id='cadre' style='border-collapse:collapse;text-align:center;color:#000000;' cellspacing='0' cellpadding='1' align='center' width='100%' height='150px'>"+
"                                <tr style='text-align:center;vertical-align:top;height:20px;font-size:"+fontSize+"pt;' valign='top'>"+
"                                <td style='border:1px solid "+cBord+";vertical-align:text-top;width:15%;height:150px;'>"+
                                    "<p>"+noteName+" :"+"</p>"+
                                    "<p align='right'>"+
                                        note.getHTMLRepresentation()+
                                    "</p>"+
"                                </td>"+
"                                <td style='border:1px solid "+cBord+";height:150px;'>"+
                                    Jsoup.parse(EditeurIO.export2htmlMathML(editeur.getDonnees())).body()+
"                                </td></tr>"+
"                             </table>"+
"                        </div>";
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean isSelected() {
        return false;
    }

    @Override
    public void setSelected(boolean b) {
    }

    @Override
    public void setFontSize(float size) {
        note.setFontSize(size);
        noteLabel.setFont(noteLabel.getFont().deriveFont(size));
        editeur.setFontSize((int) size);
    }

    @Override
    public float getFontSize() {
        return note.getFontSize();
    }

    @Override
    public void setStroken(boolean b) {
    }

    @Override
    public boolean isStroken() {
        return false;
    }

    @Override
    public void setStrikeColor(Color c) {
    }

    @Override
    public Color getStrikeColor() {
        return Color.BLACK;
    }

    @Override
    public JHeader copy() {
        return new JHeader(note.getNumerateur(), note.getDenominateur(), editeur.getDonnees());
    }

}
