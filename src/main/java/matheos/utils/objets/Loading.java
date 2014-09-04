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

package matheos.utils.objets;

import matheos.utils.managers.ImageManager;
import matheos.utils.managers.Traducteur;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class Loading {

    private static LoadingWindow instance;
    public static void start() { if(instance==null) instance = new LoadingWindow(); Clock.start(); }
    public static void stop() { if(instance==null) {return;} instance.dispose(); Clock.stop(); instance=null;}
    public static void setValue(int n) {if(instance==null) {return;} instance.setValue(n);}

//    public static void setImage(Image img) {instance.image = img;}
//    private static final class LoadingThread extends Thread {
//        private LoadingThread() {super("loading");}
//        private final LoadingWindow l;
//        @Override
//        public void run() {
//            l = new LoadingWindow();
//        }
//        public void setValue(int i) {
//            synchronized(l.progressBar) {
//                l.progressBar.setValue(i);
//                l.progressBar.repaint();
//            }
//        }
//        public void dispose() {
//            l.dispose();
//        }
//    }
    
    static final class LoadingWindow extends JDialog {;
        public final JProgressBar progressBar = new JProgressBar();
        public final String message = Traducteur.traduire("loading");
        public BufferedImage image;
        public LoadingWindow() {
            setName("MathEOS");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//            setIconImage(ImageManager.ICON_APPLICATION);
//            setExtendedState(MAXIMIZED_BOTH);
//            setUndecorated(true); 
            setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
            //getContentPane().setLayout(new BorderLayout());
            //Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            //setLocation(d.width/2-150, d.height/2-50);
            //setSize(300,100);
            //setUndecorated(true);
            JPanel fond;
            try {
                image = ImageIO.read(new File("images/fond.png"));
                fond = new Fond(image);
            } catch (IOException ex) {
                Logger.getLogger(Loading.class.getName()).log(Level.SEVERE, null, ex);
                fond = new JPanel();
            }
            fond.add(new SupportMessage("<html><font size='20'>"+message+"</font></html>"));
            fond.add(Box.createGlue());
            fond.add(new SupportProgressBar(progressBar));
            fond.add(Box.createGlue());
            setContentPane(fond);
    //        paintAll(getGraphics());repaint();

            setVisible(true);

        }
        
        private void setValue(int i) {
            progressBar.setValue(i);
            progressBar.repaint();
        }

    //    @Override
    //    public void paint(Graphics g) {
    //        super.paint(g);
    //        g.drawImage(image, 0, 0, null);
    //    }

        private final class Fond extends JPanel {
            private final Image image;
            private Fond(Image image) {
                this.image = image;
                setOpaque(false);
                setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            }
            @Override
            public void paintComponent(Graphics g) {
    //            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    //            g.drawImage(image, 0, 0, d.width, d.height, null);
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
    //            super.paintComponent(g);
            }
        }

        private final class SupportMessage extends JPanel {
            private final JLabel chargement;

            private SupportMessage(String message) {
                setLayout(new BorderLayout());
                setOpaque(false);

                chargement = new JLabel(message);
                chargement.setHorizontalAlignment(JLabel.CENTER);
                add(chargement, BorderLayout.CENTER);

                enlight();
            }

            private final void enlight() {
                (new Thread("enlighter") {
                    @Override
                    public void run() {
                        int i = 0,j = 0, k = 0;
                        int ri=1,rj=2,rk=3;
                        while(instance!=null) {
                            chargement.setForeground(new Color(i,j,k));
                            chargement.repaint();
                            i+=ri;j+=rj;k+=rk;
                            if(i>250) {ri=-1;}
                            if(j>250) {rj=-2;}
                            if(k>250) {rk=-3;}
                            if(i<5) {ri=1;}
                            if(j<5) {rj=2;}
                            if(k<5) {rk=3;}
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Loading.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }).start();
            }
        }

        private final class SupportProgressBar extends JPanel {
            private final JProgressBar bar;
            public SupportProgressBar(JProgressBar bar) {
                this.bar = bar;
                setOpaque(false);
                setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
                bar.setMaximumSize(new Dimension((int)(Toolkit.getDefaultToolkit().getScreenSize().width*0.9),20));
                bar.setPreferredSize(new Dimension(200,20));
    //                setMaximumSize(new Dimension(200,20));
    //                setPreferredSize(new Dimension(200,20));
                add(Box.createGlue());
                add(bar);
                add(Box.createVerticalStrut(20));
            }

        }
    }

}
