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

package matheos.texte.composants;

import matheos.Configuration;
import matheos.IHM;
import matheos.elements.EcranPartage;
import matheos.texte.Editeur;
import matheos.utils.dialogue.DialogueImageTaille;
import matheos.utils.librairies.ImageTools;
import matheos.utils.librairies.JsoupTools;
import matheos.utils.managers.ColorManager;
import matheos.utils.managers.CursorManager;
import matheos.utils.objets.Icone;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class JLabelImage extends JLabel implements ComposantTexte {

    /** Constante permettant d'identifier un JLabelImage **/
    public static final String JLABEL_IMAGE = "imageComponent";
    
    public static final String SIZE_PROPERTY = "taille";

    protected Color couleurSelection = ColorManager.get("color disabled"); // Couleur de sélection de l'image
    
    //2 images sont nécessaires pour une rendu de qualité, malheureusement, car le redimensionnement doit se faire avec
    //l'image brute pour l'iconeSelection, et l'image transparente pour l'iconeNormale
    protected BufferedImage imageInitiale; //Image initiale brute non transparente
    protected BufferedImage imageTransparente; //Copie transparente de l'image initiale afin de gagner en qualité
    private Icone iconeNormale;//Image non sélectionnée
    private Icone iconeSelection; //Image sélectionnée
    private double coefIcone = 1.0; //Proportion de l'image largeur/hauteur

    private long id = System.currentTimeMillis();// L'id unique du JLabelImage permettant de l'identifier
    public static final int PREFERRED_HEIGHT = 200; //Hauteur du label dans le cas général
    public static final int MAX_HEIGHT = 400; // Hauteur maximale autorisée
    public static final int MAX_WIDTH = 500; // Largeur maximale autorisée
    private int hauteurMax; // Hauteur maximale acceptable par le label (au cas où le label est très large)

    protected JLabelImage(BufferedImage image) {
        this(image, 0);
    }

//    protected JLabelImage(AttributeSet attributes) {
//        this(ImageTools.getImageFromArray((byte[]) attributes.getAttribute(IMAGE_INITIALE)),
//                (Color) attributes.getAttribute(SELECTION_COLOR),
//                (Integer) attributes.getAttribute(HAUTEUR_IMAGE));
//        id = (Long) attributes.getAttribute(ID);
//    }

    public JLabelImage(BufferedImage image, int hauteurInitiale) {
        super();
        iconeNormale = new Icone();
        iconeSelection = new Icone();
        setImageInitiale(image, hauteurInitiale);
        this.setCursor(CursorManager.getCursor(Cursor.TEXT_CURSOR));
        addMouseListener(new ImageMouseListener());//écoute les clics sur le composant
    }

    public JLabelImage(String source) throws IOException {
        this(ImageIO.read(new File(source)));
    }

//    private volatile Thread changeSizeTask;//On lance les changeSize dans un Thread séparé pour ne pas ralentir le logiciel
    private volatile ChangeSizeWorker changeSizeTask = null;//On lance les changeSize dans un Thread séparé pour ne pas ralentir le logiciel

    public void setSize(int hauteur) {
        final int hauteurImage = (hauteur <= hauteurMax) ? hauteur : hauteurMax;
//        if(changeSizeTask!=null && changeSizeTask.isAlive()) {
//            try {
//                changeSizeTask.join(200);
//            } catch (InterruptedException ex) {}
//        }
//        changeSizeTask = new Thread(new ChangeSize(hauteurImage));
//        changeSizeTask.start();
        if(changeSizeTask!=null && !changeSizeTask.isDone()) {
            changeSizeTask.addTask(hauteur);
        } else {
            changeSizeTask = new ChangeSizeWorker();
            changeSizeTask.addTask(hauteurImage);
            changeSizeTask.execute();
        }

//        super.setSize((int) (hauteur * coefIcone), hauteur);
    }
    
    private void setScaledImage(BufferedImage imageNormale, BufferedImage imageSelection) {
        this.iconeNormale = new Icone(imageNormale);//new Icone(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(imageNormale.getSource(), new ImageTransp())));
        this.iconeSelection = new Icone(imageSelection);//new Icone(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(imageNormale.getSource(), new ImageTransp(couleurSelection))));
        this.setIcon(iconeNormale);
        this.repaint();
    }

    @Override
    public void setFontSize(float size) {
        setSize(Math.round(size/getFont().getSize2D()*getHeight()));
        setFont(getFont().deriveFont(size));
    }

    @Override
    public float getFontSize() {
        return getFont().getSize2D();
    }

    
    
    private class ChangeSize implements Runnable {
        private int hauteur=getHauteur();
        private ChangeSize(int hauteur) {this.hauteur = hauteur;}
        @Override
        public void run() {
//            try{
                BufferedImage imageNormale = ImageTools.getScaledInstance(imageTransparente, (int)(hauteur*coefIcone), hauteur, ImageTools.Quality.OPTIMAL, ImageTools.AUTOMATIC);//Résultat mauvais en partant de l'image brute
                BufferedImage imageGrise = ImageTools.imageToBufferedImage(ImageTools.changeColorToOther(imageInitiale, Color.WHITE, couleurSelection, 35));
                BufferedImage imageSelection = ImageTools.getScaledInstance(imageGrise, (int)(hauteur*coefIcone), hauteur, ImageTools.Quality.FAST, ImageTools.AUTOMATIC);//résultats trop mauvais en travaillant avec l'image transparente
                setScaledImage(imageNormale, imageSelection);
//            } catch(InterruptedException e) {}
        }
    }
    
    /*        public JLabelImage(URL source) throws IOException {
    this(ImageIO.read(source));
    this.source = source.getFile();
    }
     */

    public Icone getIconeNormale() {
        return iconeNormale;
    }

    public BufferedImage getImageInitiale() {
        return imageInitiale;
    }

    public int getHauteur() {
        return getIcon().getIconHeight();
//        return getIconeImage().getIconHeight();
    }

    public void setCouleurSelection(Color couleurSelection) {
        this.iconeSelection.setImage(ImageTools.changeColorToOther(this.iconeSelection.getImage(), this.couleurSelection, couleurSelection, 35));
        this.couleurSelection = couleurSelection;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    /**
     * Prépare le JLabel à recevoir l'image passée en paramètre
     *
     * @param imageInitiale la nouvelle image à afficher par le JLabelImage
     */
    private void setImageInitiale(BufferedImage imageInitiale) {
        setImageInitiale(imageInitiale, 0);
    }
    /**
     * Méthode permettant de changer l'image affichée par le JLabelImage. La
     * hauteur de l'ancienne image est conservée.
     *
     * @param newImage la nouvelle image à afficher par le JLabelImage
     */
    public void changeImageInitiale(BufferedImage newImage) {
        setImageInitiale(newImage, getHauteur());
    }
    protected void setImageInitiale(BufferedImage imageInitiale, int hauteurInitiale) {
        this.imageInitiale = imageInitiale;
        int hauteur = hauteurInitiale;
        coefIcone = imageInitiale.getWidth(this) / (double) imageInitiale.getHeight(this);
        hauteurMax = Math.min((int) (MAX_WIDTH / coefIcone), MAX_HEIGHT);
        
        if(hauteurInitiale>hauteurMax) {hauteur=hauteurMax;}
        if (hauteurInitiale == 0) {
            if (coefIcone > 2) {
                hauteur = (int) (MAX_WIDTH / coefIcone);
            } else {
                hauteur = PREFERRED_HEIGHT;
            }
        }
        final BufferedImage imageNormale = ImageTools.getScaledInstance(imageInitiale, (int)(hauteur*coefIcone), hauteur, ImageTools.Quality.OPTIMAL, ImageTools.AUTOMATIC);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                imageTransparente = ImageTools.imageToBufferedImage(ImageTools.changeColorToTransparent(imageNormale, Color.WHITE, 55));//On rend l'image transparente pour une meilleure qualité lors des rescale
                BufferedImage imageSelection = ImageTools.imageToBufferedImage(ImageTools.changeColorToOther(imageNormale, Color.WHITE, couleurSelection, 55));
//                BufferedImage imageNormale = ImageTools.getScaledInstance(imageTransparente, (int)(h*coefIcone), h, ImageTools.Quality.OPTIMAL);
//                imageSelection = ImageTools.getScaledInstance(imageSelection, (int)(h*coefIcone), h, ImageTools.Quality.FAST);
                setScaledImage(imageNormale, imageSelection);
                repaint();
            }
        });
        setScaledImage(imageNormale, imageNormale);
    }

    /**
     * Récupère la hauteur maximale autorisée pour ce JLabelImage.
     *
     * @return la hauteur maximale que peut avoir le JLabelImage
     */
    public int getHauteurMax() {
        return hauteurMax;
    }

    /**
     * Méthode permettant de sélectionner visuellement un JLabelImage.
     */
    @Override
    public void selectionner() {
//        iconeImage = iconeSelection;
        this.setIcon(iconeSelection);
        //setDimension(this.getHeight());
        this.revalidate();
        this.repaint();
    }

    /**
     * Méthode permettant de déselectionner visuellement un JLabelImage.
     */
    @Override
    public void deselectionner() {
//        iconeImage = iconeNormale;
        this.setIcon(iconeNormale);
        //setDimension(this.getHeight());
        this.repaint();
    }

/*    public AttributeSet imageToModel() {
        MutableAttributeSet attributes = new SimpleAttributeSet();
        BufferedImage im = imageInitiale;
        attributes.addAttribute(IMAGE_INITIALE, ImageTools.getArrayFromImage(im));
        attributes.addAttribute(SELECTION_COLOR, couleurSelection);
        attributes.addAttribute(HAUTEUR_IMAGE, this.getHeight());
        attributes.addAttribute(ID, this.id);
        attributes.addAttribute(JLabelTools.TYPE_LABEL, JLABEL_IMAGE);
        return attributes;
    }
*/
    /*	static JLabelImage modelToImage(AttributeSet attributes) {
    BufferedImage im = ImageTools.getImageFromArray((byte[]) attributes.getAttribute(IMAGE_INITIALE));
    Color selectionColor = (Color) attributes.getAttribute(SELECTION_COLOR);
    int hauteurInitiale = (Integer) attributes.getAttribute(HAUTEUR_IMAGE);
    JLabelImage labelImage = new JLabelImage(im, selectionColor, hauteurInitiale);
    return labelImage;
    }
     */
    /**
     * Méthode permettant de créer une copie du JLabelImage.
     *
     * @return un JLabelImage avec les mêmes paramètres
     */
    public JLabelImage copyImage() {
        return new JLabelImage(imageInitiale, getIcon().getIconHeight());
    }

    public byte[] getImageTbyte() {return ImageTools.getArrayFromImage(imageInitiale);}

    /**
     * Récupère la représentation pur HTML de ce label.
     * Attention l'appel à cette fonction aura pour conséquence d'écrire l'image dans le
     * dossier images temporaire
     * @return la représentation html de l'objet sous la forme d'une balise "<img />"
     */
    @Override
    public String getHTMLRepresentation() {
        Element img = Jsoup.parse("<img />").select("img").first();
        
        HashMap<String, String> styles = JsoupTools.getStyleMap(img.attr("style"));
        styles.put("height", getIcon().getIconHeight() + "");
        styles.put("width", getIcon().getIconWidth()+ "");
        
        img.attr("id", id + "").attr("src", Configuration.getURLDossierImagesTemp() + id + ".png")
                .attr("alt", "image not found");
        JsoupTools.setStyle(img, styles);
        
        try {//on enregistre l'image dans les fichiers temporaires
            File imageTemp = new File(Configuration.getDossierTemp() + id + ".png");
            ImageIO.write(imageInitiale, "PNG", imageTemp);
            imageTemp.deleteOnExit();
        } catch (IOException ex) {
            Logger.getLogger(JLabelImage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return img.outerHtml();
    }


    public static JLabelImage creerJLabelImageFromHTML(String html) {
        return creerJLabelImageFromHTML(html, null);
    }

    public static JLabelImage creerJLabelImageFromHTML(String html, BufferedImage image) {
        Element img = Jsoup.parse(html).select("img").first();
        BufferedImage im = image;
        JLabelImage label;
        int hauteur;
        String id = img.attr("id");
        if(image==null) {
            try {//on essaie de lire l'image
                im = ImageIO.read(new File(img.attr("src")));//en local
            } catch (IOException e) {
                try {
                    im = ImageIO.read(new URL(img.attr("src")));//sur internet
                } catch(IllegalArgumentException | IOException ex) {
                    Logger.getLogger(JLabelImage.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("image introuvable");
                    return null;
                }
            }
        }
        hauteur = (int) JsoupTools.getSizedStyle(img, "height");//TODO prendre en compte différentes unités
        label = new JLabelImage(im, hauteur);
        if(!id.isEmpty()) {
            try {
                label.setId(Long.parseLong(img.attr("id")));
            } catch (NumberFormatException e) {System.out.println("id du JLabelImage non trouvé : "+img.outerHtml());}
        }
        return label;
    }

    protected class ImageMouseListener extends MouseAdapter {
        protected ImageMouseListener() {}
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getComponent() instanceof JLabelImage) {
                IHM.activeMode(EcranPartage.COURS);
                JLabelImage image = (JLabelImage) e.getComponent();
                
                Editeur editeur = (Editeur) SwingUtilities.getAncestorOfClass(Editeur.class, JLabelImage.this);
                if(editeur!=null) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        //On sélectionne le JLabel
                        javax.swing.text.Element element = editeur.getHTMLdoc().getElement(Editeur.getSpanId(image.getId()));
                        editeur.select(element.getStartOffset(),element.getEndOffset());
                    }
                    if (SwingUtilities.isRightMouseButton(e)) {
                        DialogueImageTaille dialogueImageTaille = new DialogueImageTaille(editeur, image);
                    }
                }
            }
        }
    }
    
    public class ChangeSizeWorker extends SwingWorker<Void, DoubleImage> {
        private final LinkedList<Integer> toDo = new LinkedList<>();
        public void addTask(int newHeight) {toDo.push(newHeight);}
        @Override
        protected Void doInBackground() throws Exception {
            while(!toDo.isEmpty()) {
                Integer h;
                synchronized(toDo) {
                    h = toDo.pop();
                    toDo.clear();
                }
                BufferedImage imageNormale = ImageTools.getScaledInstance(imageTransparente, (int)(h*coefIcone), h, ImageTools.Quality.OPTIMAL, ImageTools.AUTOMATIC);//Résultat mauvais en partant de l'image brute
                BufferedImage imageGrise = ImageTools.imageToBufferedImage(ImageTools.changeColorToOther(imageInitiale, Color.WHITE, couleurSelection, 35));
                BufferedImage imageSelection = ImageTools.getScaledInstance(imageGrise, (int)(h*coefIcone), h, ImageTools.Quality.FAST, ImageTools.AUTOMATIC);//résultats trop mauvais en travaillant avec l'image transparente
                publish(new DoubleImage(imageNormale, imageSelection));
            }
            return null;
        }
        @Override
        protected void process(List<DoubleImage> chunks) {
            DoubleImage d = chunks.get(chunks.size()-1);
            setScaledImage(d.normal, d.selection);
        }
        @Override
        protected void done() {
            changeSizeTask = null;
        }
    }
    protected class DoubleImage {
        private BufferedImage normal;
        private BufferedImage selection;
        private DoubleImage(BufferedImage imageNormale, BufferedImage imageSelection) {
            normal = imageNormale;
            selection = imageSelection;
        }
    }
    
    public static final class TailleEdit extends AbstractUndoableEdit {
        private final JLabelImage image;
        private final int hauteurInitiale;
        private final int hauteurFinale;
        
        public TailleEdit(JLabelImage tp, int hauteurInitiale, int hauteurFinale) {
            this.image = tp;
            this.hauteurInitiale = hauteurInitiale;
            this.hauteurFinale = hauteurFinale;
        }
        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            image.setSize(hauteurFinale);
            image.repaint();
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            image.setSize(hauteurInitiale);
            image.repaint();
        }
    }
}
