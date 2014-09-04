/** «Copyright 2013,2014 François Billioud, Guillaume Varoquaux»
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
package matheos.texte;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

/**
 * Classe pemettant de gérer l'impression d'un {@link Editeur}.
 *
 * @author gvaroquaux
 *
 */
public final class Formatter {

    private final Editeur editeur;
    private final PrinterJob imprimeur = PrinterJob.getPrinterJob();
    private final PrintRequestAttributeSet attributs = new HashPrintRequestAttributeSet();
    private PageFormat page = imprimeur.defaultPage();
    private List<Page> pages;
    private Book book = new Book();

    public Formatter(Editeur editeur) {
        this.editeur = editeur;
    }

    public JTextPane getEditeur() {
        return editeur;
    }

    /**
     * Méthode créant une boîte de dialogue pour définir les paramètres de mise
     * en page de l'éditeur, puis d'enregistrer les nouveaux paramètres.
     */
    public void miseEnPage() {
        PageFormat miseEnPage = imprimeur.pageDialog(attributs);
        if (miseEnPage != null) {
            page = miseEnPage;
        }
    }

    /**
     * Méthode créant une boîte de dialogue permettant d'afficher les
     * différentes pages mises en forme avant leur impression.
     */
    public void apercu() {
        book = new Book();
        pages = decouperEnPages();
        book.append(editeur, page, pages.size());
        new Apercu(book).setVisible(true);
    }

    /**
     * Méthode créant une boîte de dialogue permettant à l'utilisateur de
     * choisir les paramètres d'impression du document. Si l'utilisateur valide,
     * la méthode déclenche l'impression du document. Sinon, rien ne se passe
     */
    public void imprime() {
        try {
            book = new Book();
            pages = decouperEnPages();
            book.append(editeur, page, pages.size());
            imprimeur.setPageable(book);
            imprimeur.setPrintable(editeur);
            if (imprimeur.printDialog(attributs)) {
                imprimeur.print(attributs);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(editeur, ex);
        }
    }

    /**
     * Méthode permettant de calculer un coefficient de proportionnalité qui
     * gère le découpage des pages. Ce coefficient est calculé à partir de la
     * largeur réel de l'éditeur.
     *
     * @return un coefficient de proportionnalité pour la mise en page de
     * l'éditeur. S'il est > 1, la page sera agrandie. S'il est < 1, le contenu
     * sera réduit pour tenir en largeur sur la page.
     */
    private double calculerProportionPage() {
        double panelWidth = editeur.getSize().getWidth()+15; //width of the editor + 15 pour la vertical Scrollbar
        double pageWidth = page.getImageableWidth(); //width of printer page

        double scale = pageWidth / (panelWidth);
        return scale;
    }
    
    /**
     * Méthode permettant de récupérer la liste des pages qui seront imprimées.
     * Une page est constituée d'une position de départ et d'une position de
     * fin, représentant les positions des lignes de début et de fin qui seront
     * présentes sur la page.
     *
     * @return une liste des pages qui seront imprimées
     */
    private List<Page> decouperEnPages() {
        List<Page> listePages = new ArrayList<>();

        double pageHeight = page.getImageableHeight() / calculerProportionPage();//hauteur du jtp qui tient dans une page

        String[] lignes = editeur.getText().split("\n");//
        int[] indexLignes = new int[lignes.length];
        indexLignes[0] = 0;
        for(int i=0; i<lignes.length-1; i++) {indexLignes[i+1] = indexLignes[i]+lignes[i].length()+1;}
            
        try {
            int indexLineDebut,indexLineFin;
            double yDebut = 0;//editeur.modelToView(posDebut).getY();
            double yFin = yDebut+pageHeight;
            System.out.println(yFin);
            
            for(int i=0; i<lignes.length; i++) {
                double y = editeur.modelToView(indexLignes[i]).getY();
                System.out.println(i+" : "+y);
                if(y>yFin) {
                    indexLineFin = i>1 ? i-2 : i-1;
                    yFin = getLineBottomY(indexLignes[indexLineFin],indexLignes[indexLineFin]+lignes[indexLineFin].length());
                    System.out.println("fin :"+indexLineFin+" : "+yFin);
                    listePages.add(new Page(yDebut,yFin));
//                    listePages.add(new Page(y,editeur.modelToView(posFin).getY()));
                    indexLineDebut = indexLineFin + 1;
                    yDebut = getLineTopY(indexLignes[indexLineDebut],indexLignes[indexLineDebut]+lignes[indexLineDebut].length());
                    yFin = yDebut+pageHeight;
                }
            }
            listePages.add(new Page(yDebut, editeur.getHeight()));//on ajoute la dernière page

    /*        try {
                double y = 0;
                double somme = editeur.modelToView(0).getY();

                for (int i = 0; i < editeur.getDocument().getEndPosition().getOffset(); i++) {
                    if (y != editeur.modelToView(i).getY()) {
                        y = editeur.modelToView(i).getY();
                        somme += editeur.modelToView(i).getHeight();
                        lignes.add(somme);
                    }
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            //On calcule le découpage des pages (position de début et position de fin)
            int indexFin = 0;
            double positionInitiale = 0;
            double positionFinale = 0;
            while (positionInitiale < lignes.get(lignes.size() - 1)) { //Tant qu'il y a du contenu à afficher
                positionFinale = positionInitiale + pageHeight;//on crée une nouvelle page
                int i = indexFin;		//On identifie la dernière ligne affichable entièrement sur cette page
                do {
                    indexFin = i;
                    i++;
                } while (i < lignes.size() && positionFinale > lignes.get(i));
                positionFinale = Math.min(lignes.get(indexFin), positionFinale); // Min au cas où la ligne est plus haute que la hauteur de la page
                listePages.add(new Page(positionInitiale, positionFinale));
                positionInitiale = positionFinale;
            }
            return listePages;*/
        } catch (BadLocationException ex) {
            Logger.getLogger(Formatter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listePages;
    }
    
    private double getLineTopY(int posDebut, int posEnd) throws BadLocationException {
        double top = Integer.MAX_VALUE;
        //cas particulier des lignes sans caractères
        if(posDebut==posEnd) {
            Rectangle r = editeur.modelToView(posDebut);
            return r.getY();
        }
        //on parcourt la ligne pour chercher le plus gros caractère
        for(int i = posDebut; i<posEnd; i++) {
            int y = editeur.modelToView(i).y;
            if(y<top) {top = y;}
        }
        return top;
    }
    private double getLineBottomY(int posDebut, int posEnd) throws BadLocationException {
        double bottom = 0;
        //cas particulier des lignes sans caractères
        if(posDebut==posEnd) {
            Rectangle r = editeur.modelToView(posDebut);
            return r.getY() + r.getHeight();
        }
        //on parcourt la ligne pour chercher le plus gros caractère
        for(int i = posDebut; i<posEnd; i++) {
            Rectangle r = editeur.modelToView(i);
            double y = r.getY() + r.getHeight();
            if(y>bottom) {bottom = y;}
        }
        return bottom;
    }

    /**
     * Méthode appelée lors de l'impression du document afin de de découper et
     * d'afficher les différentes pages.
     *
     * @param g le graphic représentant l'editeur
     * @param page le {@link PageFormat} de la page actuelle
     * @param numero le numéro de la page actuelle à imprimer
     * @return 0 si tout s'est bien passée; 1 si le numéro de la page dépasse le
     * nombre maximum de page à imprimer
     * @throws PrinterException
     */
    public int print(Graphics g, PageFormat page, int numero) throws PrinterException {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.black);

        //calcul le nb de pages du document
//        int totalNumPages = pages.size();

        //make sure not print empty pages
//        if (numero >= totalNumPages) {
        if (numero >= book.getNumberOfPages()) {
            return Printable.NO_SUCH_PAGE;
        }

        //shift Graphic to line up with beginning of print-imageable region
        g2.translate(page.getImageableX(), page.getImageableY());

        //scale the page so the width fits...
        double scale = calculerProportionPage();
        g2.scale(scale, scale);

        //shift Graphic to line up with beginning of next page to print
        Page p = pages.get(numero);
        System.out.println(numero);
        System.out.println(p.getPositionDebut());        
        System.out.println(p.getPositionFin());        
        g2.translate(0d, -p.getPositionDebut());
        g2.clipRect(0, 0, (int) (page.getImageableWidth()/scale), (int) p.getPositionFin()); //On coupe la page après cette ligne (p.getPositionFin()) afin de ne pas couper en deux la ligne suivante

        editeur.paint(g2); //repaint the page for printing

        return Printable.PAGE_EXISTS;
    }

    /**
     * Classe qui représente une page. Ses attributs permettent d'identifier la
     * position en pixel du début de la page sur l'ensemble de l'éditeur ainsi
     * que la position en pixel de la fin de la page.
     *
     * @author gvaroquaux
     *
     */
    private final class Page {

        private final double positionDebut;
        private final double positionFin;

        Page(double yDebut, double yFin) {
            positionDebut = yDebut;
            positionFin = yFin;
        }

        public double getPositionDebut() {
            return positionDebut;
        }

        public double getPositionFin() {
            return positionFin;
        }
    }
}
