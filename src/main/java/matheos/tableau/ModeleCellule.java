package matheos.tableau;

import matheos.sauvegarde.Data;
import matheos.sauvegarde.DataObject;
import matheos.sauvegarde.DataTexte;
import matheos.utils.managers.ColorManager;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;

public class ModeleCellule extends DataObject implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    //nom des données
    private static final String CONTENU_1 = "contenu";
    private static final String CONTENU_2 = "contenu2";//Pour les cellules divisées
    private static final String BACKGROUND_COLOR = "backgroundColor";
    
    private transient BufferedImage renderer;
    
    public DataTexte getContent() {
        Data donnee = getData(CONTENU_1);
        if(donnee==null) {return new DataTexte("");}
        if(donnee instanceof DataTexte) {return (DataTexte)donnee;}
        else {
            DataTexte text = new DataTexte("");
            text.putAll(donnee);
            return text;
        }
    }

    public DataTexte getContent2() {
        Data data = getData(CONTENU_2);
        if(data==null) {return new DataTexte("");}
        if(data instanceof DataTexte) {return (DataTexte)data;}
        else {
            DataTexte text = new DataTexte("");
            text.putAll(data);
            return text;
        }
    }
    
    public void setContent(Data data) {
        putData(CONTENU_1, data==null ? new DataTexte("") : data);
    }
    
    public void setContent2(Data data) {
        putData(CONTENU_2, data==null ? new DataTexte("") : data);
    }
    
    public Color getBackgroundColor() {
        String color = getElement(BACKGROUND_COLOR);
        if(color==null) {return ColorManager.get("color cell background");}
        return ColorManager.getColorFromHexa(getElement(BACKGROUND_COLOR));
    }
    public void setBackgroundColor(Color c) {
        putElement(BACKGROUND_COLOR, ColorManager.getRGBHexa(c));
    }

    public ModeleCellule() {
        putData(CONTENU_1, new DataTexte(""));
    }

    /**
     * @return the renderer
     */
    public BufferedImage getRenderer() {
        return renderer;
    }

    /**
     * @param renderer the renderer to set
     */
    public void setRenderer(BufferedImage renderer) {
        this.renderer = renderer;
    }

}
