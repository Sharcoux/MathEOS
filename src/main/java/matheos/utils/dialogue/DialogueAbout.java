/** «Copyright 2014 Guillaume Varoquaux»
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
package matheos.utils.dialogue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import matheos.Configuration;
import matheos.IHM;
import matheos.utils.librairies.DimensionTools.DimensionT;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.CursorManager;
import matheos.utils.managers.FontManager;
import matheos.utils.managers.ImageManager;
import matheos.utils.managers.Traducteur;
import matheos.utils.objets.Icone;

/**
 *
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class DialogueAbout extends JDialog {

    private static final Font POLICE = FontManager.get("font about");
    private final Icone iconeMathEOS = ImageManager.getIcone("applicationIcon", 128, 128);//ImageManager.getIcone("about",64,64);
    private final Image imageMathEOS = iconeMathEOS.getImage();
            //IHM.ICON_APPLICATION.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
    
    public DialogueAbout() {
        super(IHM.getMainWindow(), Traducteur.traduire("about title"), true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setIconImage(imageMathEOS);
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        JPanel topPane = new JPanel();
        topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
        JPanel iconPane = new JPanel(new BorderLayout());
        JPanel messagePane = new JPanel(new BorderLayout());
        JPanel licensePane = new JPanel(new BorderLayout());
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        JLabel iconLabel = new AboutLabel(iconeMathEOS);
        JLabel descriptionMessage = new AboutLabel(String.format(Traducteur.traduireEnHTML("about message"),Configuration.getVersion()));
        JButton okButton = new JButton(Traducteur.traduire("ok"));okButton.setFont(POLICE);
        JButton licenseButton = new JButton(Traducteur.traduire("about license button"));licenseButton.setFont(POLICE);

        contentPane.add(topPane);
        contentPane.add(licensePane);
        contentPane.add(buttonPane);
        topPane.add(iconPane);iconPane.add(iconLabel, BorderLayout.CENTER);
        topPane.add(messagePane); messagePane.add(descriptionMessage, BorderLayout.CENTER);
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(licenseButton);
        buttonPane.add(Box.createHorizontalStrut(50));
        buttonPane.add(okButton);
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        getContentPane().add(contentPane, BorderLayout.CENTER);
        pack();
        JLabel licenseMessage = new AboutLabel(decoupe(Traducteur.traduireEnHTML("about license")));
        licensePane.add(licenseMessage);
        pack();
        DimensionT dimension = new DimensionT(IHM.getMainWindow().getSize()).moins(getSize()).fois(0.5);
        this.setLocation(dimension.width, dimension.height);
        
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogueAbout.this.dispose();
            }
        });
        licenseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(new File(Traducteur.traduire("about license file")));
                    DialogueAbout.this.dispose();
                } catch (IOException ex) {
                    Logger.getLogger(DialogueAbout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
    }
    
    private class AboutLabel extends JLabel {
        private AboutLabel(Icon icon) { super(icon);init(); }
        private AboutLabel(String text) { super(text);init(); }
        private void init() {
            this.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
            this.setFont(POLICE);
        }
    }
    
    private String decoupe(String s) {
        String[] T = s.split(" ");
        String resultat = "<HTML>";
        String ligne = "";
        FontMetrics fm = getFontMetrics(POLICE);
        for(String mot : T) {
            if(fm.stringWidth(ligne+" "+mot)<getPreferredSize().width) {
                ligne+=" "+mot;
            } else {
                resultat+=ligne+"<br/>";
                ligne = mot;
            }
        }
        return resultat + ligne + "</HTML>";
    }

        //Création des composants
//        JLinkLabel textLien = new JLinkLabel(adresseSite);

        //Dimensionnement des composants
//        logoMathEOS.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
//        leftPanel.setMinimumSize(new Dimension(imageMathEOS.getWidth(this) * 2, imageMathEOS.getHeight(this) * 2));
//        leftPanel.setPreferredSize(new Dimension(imageMathEOS.getWidth(this) * 2, imageMathEOS.getHeight(this) * 2));
//        ok.setMaximumSize(new DimensionT(ok.getPreferredSize()).plus(40, 0));
//        license.setMaximumSize(new DimensionT(ok.getPreferredSize()).plus(40, 0));
//        ok.setMaximumSize(new Dimension(100,50));
//        license.setMaximumSize(new Dimension(100,50));

        //Ajout des listeners sur le bouton OK

//        ok.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//                    close();
//                }
//            }
//        });

    /**
     * Classe permettant de créer un lien vers un navigateur
     *
     */
    private static class JLinkLabel extends JLabel implements MouseListener {

        private static final Color FOREGROUND_COLOR = ColorManager.get("color link foreground");
        private static final Color CLICKED_COLOR = ColorManager.get("color link clicked");
        private String link = "";
        private String onMouseEnteredLink;
        private String onMouseExitedLink;

        private JLinkLabel(String link) {
            super(link);
            this.link = link;
            this.setForeground(FOREGROUND_COLOR);
            createLink();
            this.addMouseListener(this);
            this.setCursor(CursorManager.getCursor(Cursor.TEXT_CURSOR));
        }

        private void createLink() {
            onMouseExitedLink = link;
            StringBuilder sb = new StringBuilder();
            sb.append("<html><u>").append(link).append("</u></html>");
            onMouseEnteredLink = sb.toString();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Desktop d = Desktop.getDesktop();
            try {
                d.browse(new URI(link));
            } catch (IOException | URISyntaxException e1) {
                DialogueBloquant.error(Traducteur.traduire("error"), Traducteur.traduire("no browser"));
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.setForeground(CLICKED_COLOR);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            this.setForeground(FOREGROUND_COLOR);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            //Le passage en html agrandit automatiquement le lien jusqu'au bout du Panel
            //On limite donc sa taille maximale afin qu'ils ne s'agrandissent pas
            this.setMaximumSize(new Dimension(this.getWidth(), this.getHeight()));
            this.setText(onMouseEnteredLink);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            this.setText(onMouseExitedLink);
        }
    }
}