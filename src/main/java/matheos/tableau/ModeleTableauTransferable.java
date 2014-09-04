/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package matheos.tableau;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;

/**
 * Classe définissant le format {@link Transferable} d'un {@link ModeleTableau}
 * Le format d'échange est de type <code>ModeleTableau</code>, et représente donc
 * un sous modèle du <code>ModeleTableau</code> principal.
 * 
 * @author Guillaume
 */
public class ModeleTableauTransferable implements Serializable, Transferable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static DataFlavor tableauFlavor = new DataFlavor(ModeleTableau.class, "tableauFlavor");
    private final static DataFlavor[] supportedFlavors = {tableauFlavor};
    private ModeleTableau modeleTableau;

    public ModeleTableauTransferable(ModeleTableau modeleTableau) {
    	this.modeleTableau = modeleTableau;
    }

    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (!tableauFlavor.equals(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return modeleTableau;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return supportedFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
    	return tableauFlavor.equals(flavor);
    }
   
    
}
