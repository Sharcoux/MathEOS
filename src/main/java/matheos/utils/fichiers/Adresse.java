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
package matheos.utils.fichiers;


import matheos.json.JsonMathEOSReader;
import matheos.json.JsonMathEOSWriter;
import matheos.json.JsonReader;
import matheos.utils.dialogue.DialogueBloquant;
import matheos.utils.managers.Traducteur;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author François Billioud
 */
@SuppressWarnings("serial")
public class Adresse extends File {

    public static final String EXTENSION_DOCX = "docx";
    public static final String EXTENSION_PDF = "pdf";
    public static final String EXTENSION_LANGUE = "lang";
    public static final String EXTENSION_MathEOS = "bmc";
    public static final String EXTENSION_MathEOS_EXPORT_FILE = "mef";
    public static final String EXTENSION_THEME = "th";

    public Adresse(String adresse) {
        super(adresse);
    }

    public Adresse(File adresse) {
        super(adresse.toString());
    }

    public Adresse(File adresse, String fileName) {
        super(adresse, fileName);
    }

    /**
     * Charge les noms des fichiers de l'extension passée en paramètre.
     * L'extension est ignorée dans le résultat renvoyé
     * @param extension extension des fichiers à observer
     * @return tableau contenant les noms des fichiers trouvés, sans leur extension
     */
    public String[] listeNomFichiers(String extension) {
        String[] listeFichiers = list();
        if (listeFichiers == null) {
            return null;
        }
        LinkedList<String> L = new LinkedList<>();
        for (String listeFichier : listeFichiers) {
            //ne garde que les noms, sans l'extension
            if (listeFichier.endsWith("." + extension)) {
                L.add(listeFichier.split("\\." + extension)[0]);
            }
        }
        return L.toArray(new String[L.size()]);
    }

    /**
     * Charge les noms des fichiers de l'extension passée en paramètre.
     * L'extension est incluse dans le résultat renvoyé
     * @param extension extension des fichiers à observer
     * @return tableau contenant les noms des fichiers trouvés, ou null
     * si l'adresse spécifiée n'existe pas.
     */
    public String[] listeFichiers(String extension) {
        if (!this.exists()) {
            return null;
        }
        String[] listeFichiers = list();
        LinkedList<String> L = new LinkedList<>();
        for (String listeFichier : listeFichiers) {
            //ne garde que les noms, sans l'extension
            if (listeFichier.endsWith("." + extension)) {
                L.add(listeFichier);
            }
        }
        return L.toArray(new String[L.size()]);
    }

    /**
     * Charge les noms des dossiers à l'adresse indiquée.
     * @return tableau contenant les noms des dossiers trouvés
     */
    public String[] listeDossiers() {
        String[] listeDossiers = list();
        LinkedList<String> L = new LinkedList<>();
        for (String dossier : listeDossiers) {
            if(new File(this.getAbsoluteFile()+separator+dossier).isDirectory()){L.add(dossier);}
        }
        return L.toArray(new String[L.size()]);
    }

    /**
     * renvoie le nom du fichier privé de son extension
     * @return le nom du fichier privé de son extension
     */
    public String getNom() {
        if (isFile()) {
            String nom = getName();
            String[] T = getName().split("\\.");
            int n = T.length;
            return nom.split("\\." + T[n - 1])[0];//retire l'extension
        } else {
            return null;
        }
    }

    public boolean sauvegarde(Object o) {
        if (this.getParentFile() != null && !this.getParentFile().exists()) {
            this.getParentFile().mkdirs();
        }
        
        IOMethod m;
        m = new JsonIO();
//        m = new GsonIO();
//        m = new Jackson();
        try {
            File security = new File(this.getParent()+Adresse.separatorChar+"."+this.getName());
            m.sauvegarde(security, o);
            if(!this.exists()) {this.createNewFile();}
            try {
                Files.move(security.toPath(), this.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch(AtomicMoveNotSupportedException ex) {
                Files.move(security.toPath(), this.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            if(!security.delete()) {security.deleteOnExit();}
        } catch(Exception e) {
            Logger.getLogger(Adresse.class.getName()).log(Level.SEVERE, "adresse : "+this.getAbsolutePath(), e);
            DialogueBloquant.error("dialog file not saved");
            return false;
        }
        return true;
    }
    
    public Object chargement() {
        IOMethod m;
        m = new JsonIO();
//        m = new GsonIO();
//        m = new Jackson();
        
        Object o = m.chargement(this);

        return o;
    }

    @Override
    public boolean delete() {
        if (this.isDirectory()) {
            for (File f : this.listFiles()) {
                new Adresse(f.toString()).delete();
            }
        }
        return super.delete();
    }

    @Override
    public Adresse getParentFile() {
        File parent = super.getParentFile();
        return parent==null ? null : new Adresse(super.getParentFile());
    }

    public static class MathEOSFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.getPath().endsWith(EXTENSION_MathEOS) || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return Traducteur.traduire("matheos file");
        }
    }

    public static class DocxFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.getPath().endsWith(EXTENSION_DOCX) || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return Traducteur.traduire("docx file");
        }
    }
    public static class PdfFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.getPath().endsWith(EXTENSION_PDF) || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return Traducteur.traduire("pdf file");
        }
    }
    
    public static class SingleFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.getPath().endsWith(EXTENSION_MathEOS_EXPORT_FILE) || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return Traducteur.traduire("matheos export file");
        }
    }
    
    private static interface IOMethod {
        public Object chargement(File adresse);
        public void sauvegarde(File adresse, Object o);
    }
    
    private static class JsonIO implements IOMethod {

        @Override
        public Object chargement(File file) {
            Object o = null;
            FileInputStream readStream = null;
            JsonReader jr;
            if (file.exists()) {
                try {
                    readStream = new FileInputStream(file);
                    o = JsonMathEOSReader.jsonToJava(readStream);
                } catch (IOException ex) {
                    Logger.getLogger(file.getClass().getName()).log(Level.SEVERE, "adresse : "+file.getAbsolutePath(), ex);
                    DialogueBloquant.error("dialog file input error",file.getAbsolutePath());
                } finally {
                    if (readStream != null) {
                        try {
                            readStream.close();
                        } catch (IOException x) {
                            Logger.getLogger(file.getClass().getName()).log(Level.SEVERE, "adresse : "+file.getAbsolutePath(), x);
                        }
                    }
                }
            } else {
                System.out.println("fichier " + file + " non trouvé");
            }
            return o;
        }

        @Override
        public void sauvegarde(File file, Object o) {
            FileOutputStream writtenStream = null;
//            JsonWriter jw;
//            FileWriter fw = null;
            try {
//                fw = new FileWriter(file);
//                fw.write(JsonMathEOSWriter.objectToJson(o));
                writtenStream = new FileOutputStream(file);
                writtenStream.write(JsonMathEOSWriter.objectToJson(o).getBytes(Charset.forName("UTF-8")));
//                jw = new JsonWriter(writtenStream);
//                jw.write(o);
            } catch (IOException ex) {
                Logger.getLogger(Adresse.class.getName()).log(Level.SEVERE, null, ex);
                DialogueBloquant.error("dialog file not saved");
            } finally {
                if (writtenStream != null) {
//                if (fw != null) {
                    try {
                        writtenStream.close();
//                        fw.close();
                    } catch (IOException x) {
                        Logger.getLogger(Adresse.class.getName()).log(Level.SEVERE, null, x);
                    }
                }
            }
        }
        
    }
    private static class Jackson implements IOMethod {

        @Override
        public Object chargement(File file) {
            Object o = null;
//            ObjectMapper mapper = new ObjectMapper();
//            try {
//                o = mapper.readValue(file, DataProfil.class);
//            } catch (IOException ex) {
//                Logger.getLogger(Adresse.class.getName()).log(Level.SEVERE, null, ex);
//            }
            return o;
        }

        @Override
        public void sauvegarde(File file, Object o) {
//            ObjectMapper mapper = new ObjectMapper();
//            try {
//                mapper.writeValue(file, o);
//            } catch (IOException ex) {
//                Logger.getLogger(Adresse.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
        
    }
    private static class GsonIO implements IOMethod {

        @Override
        public Object chargement(File file) {
            Object o = null;
//            FileReader fr;
//            BufferedReader br = null;
//            String texte = "";
//            try {
//                fr = new FileReader(file);
//                br = new BufferedReader(fr);
//                String ligne;
//                while((ligne=br.readLine())!=null) {
//                    texte += ligne;
//                }
//            } catch(IOException ex){
//                Logger.getLogger(file.getClass().getName()).log(Level.SEVERE, "adresse : "+file.getAbsolutePath(), ex);
//            } finally {
//                if(br!=null) try {
//                    br.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(Adresse.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            o = new Gson().fromJson(texte, DataProfil.class);
            return o;
        }

        @Override
        public void sauvegarde(File file, Object o) {
//            FileWriter fileW = null;
//            try {
//                fileW = new FileWriter(file);
//                fileW.write(Data.toJson(o));
//            } catch (IOException ex) {
//                Logger.getLogger(Adresse.class.getName()).log(Level.SEVERE, null, ex);
//            } finally {
//                if(fileW!=null) {
//                    try {
//                        fileW.close();
//                    } catch (IOException ex) {
//                        Logger.getLogger(Adresse.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }
        }
    }
}
