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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import matheos.elements.ChangeModeListener;
import matheos.sauvegarde.DataTexte;
import matheos.texte.Editeur;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.Traducteur;
import matheos.utils.texte.EditeurIO;
import matheos.utils.texte.EditeurKit;
import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.texte.JMathTextPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author François Billioud
 */
public class JHeader extends JPanel implements ComposantTexte {
    /** Constante permettant d'identifier un JHeader **/
    public static final String JHEADER = "headerComponent";
    
    private long id = System.currentTimeMillis();
    
    public JHeader() {
        setLayout(new BorderLayout());
        markPanel.setLayout(new BoxLayout(markPanel, BoxLayout.PAGE_AXIS));
        markPanel.add(noteLabel);
        markPanel.add(note);
        
        add(markPanel, BorderLayout.WEST);
        add(editeur, BorderLayout.CENTER);
        
        setBorder(BorderFactory.createLineBorder(Color.red));
        markPanel.setBorder(BorderFactory.createLineBorder(Color.red));
        markPanel.setBackground(Color.WHITE);
        note.setBackground(Color.WHITE);
        
        note.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                note.setSelected(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                note.setSelected(false);
            }
        });
    }
    
    public JHeader(String noteValue, String noteMax, DataTexte observations) {
        this();
        note.setValues(noteValue, noteMax);
        editeur.charger(observations);
    }
    
//    public Dimension getPreferredSize() {
//        return new Dimension(super.getPreferredSize().width, editeur.getPreferredSize().height);
//    }
    
    private final JPanel markPanel = new JPanel();
    private final JMathTextPane editeur = new JLimitedMathTextPane(10, true);
    private final JLabelNote note = new JLabelNote("", Traducteur.traduire("mark max value"), 100, 50);
    private final JLabel noteLabel = new JLabel(Traducteur.traduire("test mark")+" : ");

    @Override
    public String getHTMLRepresentation(SVG_RENDERING svgAllowed, boolean mathMLAllowed) {
        String cBord = ColorManager.getRGBHexa("color border test");
        String noteName = Traducteur.traduire("test mark");
//        String observations = Traducteur.traduire("test remark");
        int fontSize = EditeurKit.TAILLES_PT[0];
        String editeurContent = Jsoup.parse(EditeurIO.getDonnees(editeur, 0, editeur.getLength(), svgAllowed, mathMLAllowed).getContenuHTML()).body().html();
        return
//"                        <div id='table' "+REMOVABLE_PROPERTY+"="+isRemovable()+" style='padding-top:20px;'>"+
"                            <table id='"+getId()+"' "+REMOVABLE_PROPERTY+"='"+isRemovable()+"' style='border-collapse:collapse;text-align:center;color:#000000;' width='100%' cellspacing='0' cellpadding='1' align='center'>"+
"                                <tr style='text-align:center;vertical-align:top;height:20px;font-size:"+fontSize+"pt;' valign='top'>"+
"                                <td style='border:1px solid "+cBord+";vertical-align:text-top;width:15%;height:150px;'>"+
                                    "<p>"+noteName+" :"+"</p>"+
                                    "<p class='note-value' align='right'>"+
                                        note.getHTMLRepresentation(svgAllowed, mathMLAllowed)+
                                    "</p>"+
"                                </td>"+
"                                <td class='observations' style='border:1px solid "+cBord+";height:150px;'>"+
                                    editeurContent+
"                                </td></tr>"+
"                             </table>";
//"                        </div>";
    }

    public static JHeader creerJHeaderFromHTML(String html) {
        Document doc = Jsoup.parse(html);
        Element table = doc.select("table").first();
        JLabelNote note = JLabelNote.creerJLabelNoteFromHTML(table.select(".note-value").html());
        String observations = table.select(".observations").html();
        JHeader header = new JHeader(note.getNumerateur(), note.getDenominateur(), new DataTexte(observations));
        header.setRemovable(table.attr(REMOVABLE_PROPERTY).equals("true"));
        header.setId(Long.parseLong(table.id()));
        return header;
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
        return note.isSelected();
    }

    @Override
    public void setSelected(boolean b) {
        Color c = b ? couleurSelection : Color.WHITE;
        setBackground(c);
        editeur.setBackgroundColor(c, true);
        note.setSelected(b);
        markPanel.setBackground(c);
    }
    
    private Color couleurSelection = ColorManager.get("color disabled");
    public void setSelectionColor(Color selectionColor) {
        couleurSelection = selectionColor;
    }
    
    @Override
    public void setEnabled(boolean b) {
        setOpaque(b ? isSelected() : b);
        super.setEnabled(b);
        note.setEnabled(b);
        noteLabel.setEnabled(b);
        editeur.setEnabled(b);
        markPanel.setEnabled(b);
        markPanel.setOpaque(b ? isSelected() : b);
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

    
    private boolean removable = false;
    /** définit si l'on peut supprimer cet élément. vrai par défaut **/
    public void setRemovable(boolean b) {
        removable = b;
    }
    
    /** renvoie si l'on peut supprimer cet élément. vrai par défaut **/
    public boolean isRemovable() {return removable;}
    
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
        JHeader h = new JHeader(note.getNumerateur(), note.getDenominateur(), editeur.getDonnees());
        h.setRemovable(true);
        return h;
    }
    //FIXME: tout le passage ici me parait assez louche. Le but était de pouvoir ajouter un mouseListener sur tous les
    //éléments du header, mais également d'ajouter un noteListener uniquement sur la note. Il serait mieux de tout mettre
    //sur tous les éléments. De toute façon, le noteListener ne se déclenche pas sur un autre élément.
    public synchronized void addMouseListener(MouseListener l) {
        super.addMouseListener(l);
        if(l instanceof HeaderListener) {
            note.addMouseListener(((HeaderListener)l).noteListener);
        } else if(l instanceof ChangeModeListener) {
            note.addMouseListener(l);
            editeur.addMouseListener(l);
            markPanel.addMouseListener(l);
            noteLabel.addMouseListener(l);
        }
    }
    
    public synchronized void removeMouseListener(HeaderListener l) {
        super.removeMouseListener(l);
        note.removeMouseListener(l.noteListener);
    }
    
    public static class HeaderListener extends MouseAdapter {
        private final MouseListener noteListener;
        public HeaderListener(Editeur editeur) {
            this.noteListener = new JLabelNote.NoteListener(editeur);
        }
    }

}
