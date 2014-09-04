/*
 * «Copyright 2011 Tristan Coulange»
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
 */
package matheos.tableau;

import static matheos.tableau.TableConstants.DEFAULT_COULEUR_CASE;
import matheos.tableau.TableConstants.FIRST_CASE;
import matheos.utils.managers.ColorManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import matheos.utils.librairies.ImageTools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.border.Border;

/**
 * Classe qui représente le rendu d'une cellule.
 *
 * @author Tristan
 */
@SuppressWarnings("serial")
public class TableauRenderer extends DefaultTableCellRenderer {

    private final ControlleurTableau controlleur;
    private static final Color SELECTED_COLOR = ColorManager.get("color disabled");

    public TableauRenderer(ControlleurTableau controlleur) {
        this.controlleur = controlleur;
    }

    /**
     * Colore le fond de la cellule (row,column) en bleu si elle est
     * sélectionnée, et dans le cas contraire en la couleur de la case, centre
     * le texte dans la cellule, met des bordures nécessaire; puis renvoie la
     * cellule.
     *
     *
     * @param table
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @return
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setBackground(chooseBackground(table, value, isSelected));
        setBorder(createBorder(table, row, column));
        this.setHorizontalAlignment(JLabel.CENTER);
        if (table instanceof Tableau && row == 0 && column == 0 && ((Tableau) table).getFirstCase().equals(FIRST_CASE.SEPARATE)) {
            createSeparateRenderer((Tableau) table, value, isSelected);
        } else {
            createRenderer(value, isSelected);
        }
        return this;
    }

    /**
     * Détermine la bordure en fonction de la position de la cellule.
     *
     * @param row
     * @param column
     * @return
     */
    private Border createBorder(JTable table, int row, int column) {
        Border border;
        if (table instanceof Tableau) {
            if (row == 0 && column == 0 && ((Tableau) table).getFirstCase().equals(FIRST_CASE.NOT_VISIBLE)) {
                return BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK);
            }
        }
        int top = (row == 0 ? 1 : 0);
        int left = (column == 0 ? 1 : 0);
        border = BorderFactory.createMatteBorder(top, left, 1, 1, Color.BLACK);
        return border;
    }

    /**
     * Détermine la couleur de fond en fonction de l'état sélectionné ou non de
     * la cellule, et de sa couleur de fond propre.
     *
     * @param table
     * @param value
     * @param isSelected
     * @return
     */
    private Color chooseBackground(JTable table, Object value, boolean isSelected) {
        Color backgroundColor = DEFAULT_COULEUR_CASE;
        if (isSelected) {
            backgroundColor = SELECTED_COLOR;
        } else if (table.isEnabled()) {
            if (value instanceof ModeleCellule) {
                backgroundColor = ((ModeleCellule) value).getBackgroundColor();
            }
        } else {
            backgroundColor = ColorManager.get("color cell disabled");
        }
        return backgroundColor;
    }

    /**
     * Crée le visuel du renderer en fonction de l'objet contenu dans la Cellule
     *
     * @param value
     * @param isSelected
     */
    private void createRenderer(Object value, boolean isSelected) {
        if (value instanceof ModeleCellule) {
            this.setValue("");
            Image im = ((ModeleCellule) value).getRenderer();
            if (im != null) {
                im = ImageTools.changeColorToTransparent(im, Color.WHITE, 55);
                if (isSelected) {
                    im = ImageTools.changeToNegative(im);
                }
                double width0 = im.getWidth(this);
                double height0 = im.getHeight(this);
                double coef = controlleur.getCoef();
                this.setIcon(new ImageIcon(im.getScaledInstance((int) (width0 * coef), (int) (height0 * coef), Image.SCALE_SMOOTH)));
            } else {
                this.setIcon(null);
                String chaine = ((ModeleCellule)value).getContent().getContenuHTML();//"<html><p style='text-align:center'>" + ((ModeleCellule) value).getContent() + "</p></html>";
                //chaine = chaine.replaceAll("\n", "<br/>");
                this.setValue(chaine);
            }
        } else {
            this.setIcon(null);
        }
    }

    private void createSeparateRenderer(Tableau table, Object value, boolean isSelected) {
        if (value instanceof ModeleCellule) {
            this.setValue("");
            int width = table.getColumnModel().getColumn(0).getWidth();
            int height = table.getRowHeight(0);
            BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = buffered.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.BLACK);
            BufferedImage im = ((ModeleCellule) value).getRenderer();
            if (im != null) {
                double width0 = im.getWidth(this);
                double height0 = im.getHeight(this);
                double coef = controlleur.getCoef();
                im = ImageTools.getScaledInstance(im, (int) (width0 * coef), (int) (height0 * coef), ImageTools.Quality.AUTO, ImageTools.AUTOMATIC);
                g.drawImage(im, (int) (width / 2 - im.getWidth(this) / 2), (int) (height / 2 - im.getHeight(this) / 2), this);
            }
            g.drawLine(0, 0, width, height);
            Image img = ImageTools.changeColorToTransparent(buffered, Color.WHITE, 55);
            if (isSelected) {
                img = ImageTools.changeToNegative(img);
            }
            this.setIcon(new ImageIcon(img));
        }
    }
}
