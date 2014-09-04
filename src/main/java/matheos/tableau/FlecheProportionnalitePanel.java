package matheos.tableau;

import matheos.elements.ChangeModeListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

/**
 * Classe servant de support à l'affichage des différentes flèches de proportionnalité.
 * Il est présent en mode "normal" du tableau.
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class FlecheProportionnalitePanel extends JPanel implements ModeleFlechesListener {

    private SpringLayout layout = new SpringLayout();
    private TableConstants.ORIENTATIONS orientation;
    private PanelOrientation panelOrientation;
    private List<FlechePanel> listeFlechesProp;
    private List<JPanel> liste;
    private List<ResizableComponentTableau> listeResizable;
    private ControlleurTableau.ChampTextListener textChangedListener;

    public FlecheProportionnalitePanel(TableConstants.ORIENTATIONS orientation) {
        setLayout(layout);
        setOpaque(false);
        this.orientation = orientation;
        this.panelOrientation = new PanelOrientation(orientation);
        liste = new ArrayList<JPanel>();
        listeResizable = new ArrayList<ResizableComponentTableau>();
        listeFlechesProp = new ArrayList<FlechePanel>();
        this.addMouseListener(new ChangeModeListener(ChangeModeListener.TP));
        this.validate();
        this.repaint();
    }
    
// NOT USED
//    private void clear() {
//        for (FlechePanel panel : listeFlechesProp) {
//            this.remove(panel);
//        }
//        liste.clear();
//        listeResizable.clear();
//        listeFlechesProp.clear();
//        this.revalidate();
//        this.repaint();
//    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (FlechePanel panel : listeFlechesProp) {
            panel.setEnabled(enabled);
        }
        this.repaint();
    }

    public void adapterDimensions(double coef) {
        float coefficient = (float) coef;

        for (FlechePanel panel : listeFlechesProp) {
            panel.adapterDimensions(coef);
        }
        if (listeFlechesProp != null && !listeFlechesProp.isEmpty()) {
            panelOrientation.positionnerPanelFleches(layout, this, listeFlechesProp, coefficient);
        }
        liste.clear();
        listeResizable.clear();
        liste.addAll(listeFlechesProp);
        listeResizable.addAll(listeFlechesProp);
        panelOrientation.orienterPositionSupportChild(layout, this, listeResizable, coefficient);

        this.revalidate();
        this.repaint();
    }

    public int getLargeurNormale() {
        listeResizable.clear();
        listeResizable.addAll(listeFlechesProp);
        return panelOrientation.getLargeurNormaleSupportChild(this, listeResizable);
    }

    public void checkChampProportionnaliteListener(ControlleurTableau.ChampTextListener listener) {
        textChangedListener = listener;
        for (FlechePanel panel : listeFlechesProp) {
            panel.checkChampProportionnaliteListener(textChangedListener);
        }
    }

    @Override
    public void modeleFlecheAdded(List<DataFleche> modeleFleches) {
        for (DataFleche fleche : modeleFleches) {
            if (fleche.getOrientation().equals(orientation)) {
                FlechePanel panel = new FlechePanel(FlechePanel.ETAT.TEXTE, fleche);
                listeFlechesProp.add(panel);
                this.add(panel);
                panel.checkChampProportionnaliteListener(textChangedListener);
            }
        }
        this.revalidate();
        this.repaint();
    }

    @Override
    public void modeleFlecheRemoved(List<DataFleche> modeleFleches) {
        for (DataFleche fleche : modeleFleches) {
            Iterator<FlechePanel> it = listeFlechesProp.iterator();
            while (it.hasNext()) {
                FlechePanel panel = it.next();
                if (fleche.getOrientation().equals(orientation)) {
                    if (panel.getModeleFleche().equals(fleche)) {
                        it.remove();
                        this.remove(panel);
                    }
                }
            }
        }
        this.revalidate();
        this.repaint();
    }

    @Override
    public void modeleFlechesChanged(List<DataFleche> modeleFleches) {
//        for (DataFleche fleche : modeleFleches) {
//            if (fleche.getOrientation().equals(orientation)) {
//                for (FlechePanel panel : listeFlechesProp) {
//                    if (panel.getModeleFleche().equals(fleche)) {
//                        try {
//                            panel.getTextPane().getDocument().remove(0, panel.getTextPane().getDocument().getLength());
//                        } catch (BadLocationException ex) {
//                            Logger.getLogger(FlecheProportionnalitePanel.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                        EditeurIO.read( panel.getTextPane(), fleche.getData());
//                    }
//                }
//            }
//        }
//        this.revalidate();
//        this.repaint();
    }
}
