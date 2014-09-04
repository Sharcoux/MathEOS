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

import matheos.utils.managers.ColorManager;
import matheos.utils.managers.CursorManager;
import matheos.utils.managers.FontManager;
import matheos.utils.objets.Icone;
import matheos.utils.managers.ImageManager;
import matheos.utils.managers.Traducteur;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class DialogueAbout extends JDialog {

    private final int largeur = 370;
    private final int hauteur = 180;
    private static final Font POLICE = FontManager.get("font about");
    private final Icone iconeMathEOS = ImageManager.getIcone("applicationIcon");//ImageManager.getIcone("about",64,64);
    private final Image imageMathEOS = iconeMathEOS.getImage();
            //IHM.ICON_APPLICATION.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
    private final String version;
    private final String adresseSite;

    public DialogueAbout(Frame owner, String version, String adresseSite) {
        super(owner, Traducteur.traduire("about title"), true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.version = version;
        this.adresseSite = adresseSite;
        this.setSize(largeur, hauteur);
        this.setResizable(false);
        setIconImage(imageMathEOS);
        setLocationRelativeTo(owner);
        initComponent();
    }

    private void initComponent() {
        //Création des JPanel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

        JPanel backPanel = new JPanel();
        backPanel.setLayout(new BoxLayout(backPanel, BoxLayout.X_AXIS));

        JPanel okPanel = new JPanel();

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        //Création des composants
        JLabel logoMathEOS = new JLabel(iconeMathEOS);
        JLabel textMathEOS = new JLabel(Traducteur.traduire("about version") + version);
        JLabel textMathEOSDescription = new JLabel(Traducteur.traduireEnHTML("about message"));
        JLinkLabel textLien = new JLinkLabel(adresseSite);

        JButton ok = new JButton(Traducteur.traduire("ok"));

        textMathEOS.setFont(POLICE);
        textMathEOSDescription.setFont(POLICE);
        textLien.setFont(POLICE);
        ok.setFont(POLICE);

        //Ajout des composants aux jPanel
        contentPanel.add(backPanel, BorderLayout.CENTER);
        contentPanel.add(okPanel, BorderLayout.SOUTH);
        backPanel.add(leftPanel);
        backPanel.add(rightPanel);

        okPanel.add(ok, BorderLayout.CENTER);

        leftPanel.add(logoMathEOS);
        rightPanel.add(textMathEOS);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(textMathEOSDescription);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(textLien);

        //Dimensionnement des composants
        leftPanel.setMinimumSize(new Dimension(imageMathEOS.getWidth(this) * 2, imageMathEOS.getHeight(this) * 2));
        ok.setMaximumSize(new Dimension(50, 20));

        //Ajout des listeners sur le bouton OK
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        ok.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    close();
                }
            }
        });
        this.setContentPane(contentPanel);
    }

    private void close() {
        DialogueAbout.this.setVisible(false);
        DialogueAbout.this.dispose();
    }

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