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

package matheos.utils.librairies;

import java.awt.Component;
import matheos.sauvegarde.DataTexte;
import matheos.table.TableLayout.Cell;
import matheos.utils.texte.EditeurIO;
import matheos.texte.composants.JLabelImage;
import matheos.utils.texte.JMathTextPane;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import matheos.texte.composants.ComposantTexte;
import org.jsoup.Jsoup;

/**
 *
 * @author François Billioud
 */
public abstract class TransferableTools {

    public static final DataFlavor matheosArrayFlavor;
    public static final DataFlavor matheosFlavor;
    public static final DataFlavor xmlFlavor;
    public static final DataFlavor htmlFlavor;
    public static final DataFlavor imageFlavor;
    public static final DataFlavor textFlavor;
    public static final DataFlavor msFlavor;
    public static final DataFlavor htmlInputStreamFlavor;
    public static final DataFlavor rtfInputStreamFlavor;
    static {
        matheosArrayFlavor = getFlavor("application/matheos;class=\""+DataTexte[].class.getName() + "\"");
        matheosFlavor = getFlavor("application/matheos;class="+DataTexte.class.getName());
        xmlFlavor = getFlavor("text/xml;class=java.lang.String");
        htmlFlavor = getFlavor("text/html;class=java.lang.String");
        msFlavor = getFlavor("application/msword;class=java.io.InputStream");
        htmlInputStreamFlavor = getFlavor("text/html;class=java.io.InputStream");
        rtfInputStreamFlavor = getFlavor("text/rtf;class=java.io.InputStream");
        imageFlavor = DataFlavor.imageFlavor;
        textFlavor = DataFlavor.stringFlavor;
    }
    
    private static DataFlavor getFlavor(String flavor) {
        try {
            return new DataFlavor(flavor);
        } catch (ClassNotFoundException e) {
            Logger.getLogger(TransferableTools.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
    
    public static boolean isContentAccepted(DataFlavor[] T) {
        List L = Arrays.asList(T);
        if(L.contains(matheosFlavor)) {return true;}
        if(L.contains(htmlFlavor)) {return true;}
        if(L.contains(htmlInputStreamFlavor)) {return true;}
        if(L.contains(imageFlavor)) {return true;}
        if(L.contains(msFlavor)) {return true;}
        if(L.contains(rtfInputStreamFlavor)) {return true;}
        if(L.contains(textFlavor)) {return true;}
        return L.contains(xmlFlavor);
    }

    public static Transferable createTransferableDataTexte(DataTexte data) {
        return new DataTransferable(data);
    }
    
    public static Transferable createTransferableDataTexte(JMathTextPane jtp) {
        return new DataTransferable(jtp);
    }
    
    public static Transferable createTransferableDataTexte(JMathTextPane jtp, int startOffset, int length) {
        return new DataTransferable(jtp, startOffset, length);
    }

    public static Transferable createTransferableImage(BufferedImage image) {
        return new ImageTransferable(image);
    }
    
    public static Transferable createTransferableDataTexteArray(Cell[][] textPanes) {
        return new DataArrayTransferable(textPanes);
    }

    private static class ImageTransferable implements Transferable {
        private final BufferedImage image;

        private ImageTransferable(BufferedImage image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] T = {matheosFlavor, imageFlavor, htmlFlavor, textFlavor};
            return T;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return Arrays.asList(getTransferDataFlavors()).contains(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if(flavor.equals(matheosFlavor)) {
                return new JLabelImage(image, image.getHeight());
            }
            if(flavor.equals(htmlFlavor)) {
                return new JLabelImage(image, image.getHeight()).getHTMLRepresentation(ComposantTexte.SVG_RENDERING.PNG, false);
            }
            if(flavor.equals(imageFlavor)) {
                return image;
            }
            return "image";
        }

    }

    private static class DataTransferable implements Transferable {

        private final DataTexte dataTexte;
        private final String html5;
        private final String htmlMathML;
        
        private final ArrayList<DataFlavor> supportedFlavours = new ArrayList<>();
        {
            supportedFlavours.add(matheosFlavor);
            supportedFlavours.add(htmlFlavor);
            supportedFlavours.add(htmlInputStreamFlavor);
            supportedFlavours.add(imageFlavor);
            supportedFlavours.add(textFlavor);
        }
        
//        private final HTMLDocument htmlDoc;
        private final String text;

        private DataTransferable(DataTexte data) {//Idéalement, il faudrait pouvoir calculer les html5 et le htmlMathML
            dataTexte = data;
            String html = dataTexte.getContenuHTML();
            html5 = EditeurIO.toHTML5(html);
            htmlMathML = html5;
            text = Jsoup.parse(html).text();
        }
        private DataTransferable(JMathTextPane jtp) {
            String htmlBrut = EditeurIO.getHTMLContent(jtp.getHTMLdoc());
            final Map<String, Component> map = new HashMap<>(jtp.getComponentMap());
            dataTexte = EditeurIO.getDonnees(htmlBrut, map);
            html5 = EditeurIO.export2html5(htmlBrut, map);
            htmlMathML = EditeurIO.export2htmlMathML(htmlBrut, map);
            text = Jsoup.parse(htmlBrut).text();
        }
        private DataTransferable(JMathTextPane jtp, int startOffset, int length) {
            String htmlBrut = EditeurIO.getHTMLContent(jtp.getHTMLdoc(), startOffset, length);
            final Map<String, Component> map = new HashMap<>(jtp.getComponentMap());
            this.dataTexte = EditeurIO.getDonnees(htmlBrut, map);
            this.html5 = EditeurIO.export2html5(htmlBrut, map);
            this.htmlMathML = EditeurIO.export2htmlMathML(htmlBrut, map);
            
            HTMLDocument htmlDoc = jtp.getHTMLdoc();
            String s;
            try {
                s = htmlDoc.getText(startOffset, length);
            } catch (BadLocationException ex) {
                Logger.getLogger(TransferableTools.class.getName()).log(Level.SEVERE, null, ex);
                s = "";
            }
            this.text = s;
            if(dataTexte.imageIds().isEmpty()) {supportedFlavours.remove(imageFlavor);}
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return supportedFlavours.toArray(new DataFlavor[supportedFlavours.size()]);
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return supportedFlavours.contains(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if(flavor.equals(matheosFlavor)) {
                return dataTexte;
            }
//            if(flavor.equals(xmlFlavor)) {
//                return htmlMathML;
//            }
            if(flavor.equals(htmlFlavor)) {
                return html5;
            }
            if(flavor.equals(imageFlavor)) {
                Iterator<String> iter = dataTexte.imageIds().iterator();
                if(!iter.hasNext()) { 
                    return null;
                }
                return dataTexte.getImage(iter.next());
            }
//            if(flavor.equals(msFlavor)) {
//                return EditeurIO.export2Docx(htmlDoc, componentMap, startOffset, length);
//            }
//            if(flavor.equals(msFlavor)) {
//                ByteArrayOutputStream output = new ByteArrayOutputStream();
//                OutputStreamWriter outputWriter = new OutputStreamWriter(output);
//                outputWriter.write(EditeurIO.export2html(htmlDoc, componentMap, startOffset, length));
//                byte[] Tbyte = output.toByteArray();
//                outputWriter.close();output.close();
//                return new ByteArrayInputStream(Tbyte);
//            }
            if(flavor.equals(htmlInputStreamFlavor)) {
                byte[] Tbyte;
                try (ByteArrayOutputStream output = new ByteArrayOutputStream(); OutputStreamWriter outputWriter = new OutputStreamWriter(output)) {
                    outputWriter.write(html5);
                    Tbyte = output.toByteArray();
                }
                return new ByteArrayInputStream(Tbyte);
            }
//            if(flavor.equals(rtfInputStreamFlavor)) {
//                byte[] Tbyte;
//                try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
//                    try {
//                        (new StyledEditorKit()).write(output, htmlDoc, startOffset, length);
//                    } catch (BadLocationException ex) {
//                        Logger.getLogger(TransferableTools.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    Tbyte = output.toByteArray();
//                }
//                return new ByteArrayInputStream(Tbyte);
//            }
            if(flavor.equals(textFlavor)) { return text; }
            throw new UnsupportedFlavorException(flavor);
        }

    }

    private TransferableTools() {throw new AssertionError("instantiating utility class");}

    private static class DataArrayTransferable implements Transferable {

        private final DataTexte[][] donnees;
        private final DataTransferable secours;

        private DataArrayTransferable(Cell[][] textPanes) {
            secours = new DataTransferable(textPanes[0][0].getDonnees());
            int n=textPanes.length, m = textPanes[0].length;
            donnees = new DataTexte[n][m];
            for(int i=0; i<n; i++) {
                for(int j=0; j<m; j++) {
                    donnees[i][j] = textPanes[i][j].getDonnees();
                }
            }
        }
        
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] T = {matheosArrayFlavor, matheosFlavor, htmlFlavor, textFlavor};
            return T;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return Arrays.asList(getTransferDataFlavors()).contains(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if(flavor.equals(matheosArrayFlavor)) {
                return donnees;
            } else {
                return secours.getTransferData(flavor);
            }
        }
        
    }
    
}
