/** «Copyright 2013,2014 François Billioud»
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
package bomehc.texte;

import bomehc.IHM;
import bomehc.utils.managers.ImageManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.print.Book;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JToolBar;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class Apercu extends JDialog {

    private int pageCourante = 0;
    private final Panneau panneau;
    private final JToolBar barre = new JToolBar();
    private String libellé = "Page {0}/{1}";
    private JTextField numero;
    private final Book pages;

    public Apercu(final Book pages) {
        super(IHM.getMainWindow(), "Aperçu avant impression");
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setModal(true);
        this.setIconImage(ImageManager.getIcone("applicationIcon").getImage());
        this.pages = pages;
        numero = new JTextField(MessageFormat.format(libellé, pageCourante + 1, pages.getNumberOfPages()));
        panneau = new Panneau();
        add(panneau);
        barre.setFloatable(false);
        barre.add(new AbstractAction("Précédente") {

            public void actionPerformed(ActionEvent e) {
                if (pageCourante > 0) {
                    pageCourante--;
                    numero.setText(MessageFormat.format(libellé, pageCourante + 1, pages.getNumberOfPages()));
                    repaint();
                }
            }
        });
        barre.add(new AbstractAction("Suivante") {

            public void actionPerformed(ActionEvent e) {
                if (pageCourante < pages.getNumberOfPages() - 1) {
                    pageCourante++;
                    numero.setText(MessageFormat.format(libellé, pageCourante + 1, pages.getNumberOfPages()));
                    repaint();
                }
            }
        });
        numero.setEditable(false);
        numero.setHorizontalAlignment(JTextField.CENTER);
        barre.add(numero);
        add(barre, BorderLayout.SOUTH);
        setSize(480, 680);
        //setResizable(false);
        setLocation(100, 50);
    }

    private class Panneau extends JComponent {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D surface = (Graphics2D) g;

            double px = pages.getPageFormat(pageCourante).getWidth();
            double py = pages.getPageFormat(pageCourante).getHeight();
            double sx = getWidth() - 1;
            double sy = getHeight() - 1;
            double xoff, yoff;
            double échelle;
            if (px / py < sx / sy) { // centrer horizontalement
                échelle = sy / py;
                xoff = 0.5 * (sx - échelle * px);
                yoff = 0;
            } else { // centrer verticalement
                échelle = sx / px;
                xoff = 0;
                yoff = 0.5 * (sy - échelle * py);
            }
            surface.translate(xoff, yoff);
            surface.scale(échelle, échelle);
            Rectangle2D contour = new Rectangle2D.Double(0, 0, px, py);
            surface.setPaint(Color.WHITE);
            surface.fill(contour);
            surface.setPaint(Color.BLACK);
            surface.draw(contour);
            Printable aperçu = pages.getPrintable(pageCourante);
            try {
                //Définit la surface où l'on peut dessiner la page
                int xSurface = (int) pages.getPageFormat(pageCourante).getImageableX();
                int ySurface = (int) pages.getPageFormat(pageCourante).getImageableY();
                int widthSurface = (int) pages.getPageFormat(pageCourante).getImageableWidth();
                int heightSurface = (int) pages.getPageFormat(pageCourante).getImageableHeight();
                surface.setClip(xSurface, ySurface, widthSurface, heightSurface);
                aperçu.print(surface, pages.getPageFormat(pageCourante), pageCourante);
            } catch (PrinterException ex) {
                surface.draw(new Line2D.Double(0, 0, px, py));
                surface.draw(new Line2D.Double(0, px, 0, py));
            }
        }
    }
}
