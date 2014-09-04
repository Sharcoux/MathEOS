/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package matheos.tableau;

import matheos.utils.librairies.ImageTools;
import matheos.utils.texte.JLimitedMathTextPane;
import matheos.utils.texte.EditeurIO;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeCellEditor;

/**
 *
 * @author Guillaume
 */
@SuppressWarnings("serial")
public class TableauEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor {

    public enum TYPE_EDITOR {

        NORMAL, SEPARATE
    }
    /**
     * The Swing component being edited.
     */
    protected JComponent supportComponent;
    protected JComponent editorComponent;
    /**
     * The delegate class which handles all methods sent from the
     * <code>CellEditor</code>.
     */
    protected EditorDelegate delegate;
    /**
     * An integer specifying the number of clicks needed to start editing. Even
     * if
     * <code>clickCountToStart</code> is defined as zero, it will not initiate
     * until a click occurs.
     */
    protected int clickCountToStart = 2;
    protected static final int NB_LIGNES = 2;
//    private boolean isChanged;
    private ModeleCellule modeleCellule;
    private transient UndoManagerTableau.ContentEdit contentEdit;

    /**
     * Returns a reference to the editor component.
     *
     * @return the editor <code>Component</code>
     */
    public Component getComponent() {
        return editorComponent;
    }

    private ControlleurTableau controlleur;
    public static TableauEditor createTableauEditor(TYPE_EDITOR typeEditor, ControlleurTableau controlleur) {
        switch (typeEditor) {
            case SEPARATE:
                CaseSeparee caseSeparee = new CaseSeparee();
                return new TableauEditor(caseSeparee, controlleur);
            default:
                JLimitedMathTextPane editor = new JLimitedMathTextPane();
                return new TableauEditor(editor, controlleur);
        }
    }

    /**
     * Constructs a
     * <code>DefaultCellEditor</code> that uses a
     * <code>JLimitedMathTextPane</code>.
     *
     * @param textPane a <code>pane</code> object
     */
    private TableauEditor(final JLimitedMathTextPane textPane, ControlleurTableau controlleur) {
        textPane.setMaxLines(NB_LIGNES);
        editorComponent = textPane;
        JScrollPane scroll = new JScrollPane(textPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        supportComponent = scroll;
        this.controlleur = controlleur;
//        textPane.setEditorKit(new WrapEditorKit());
        textPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        textPane.setBackground(Color.WHITE);
        textPane.setMinimumSize(new Dimension(40, 30));
//        textPane.addTextChangedListener(new DocumentChangeListener());

        this.clickCountToStart = 2;

        delegate = new EditorDelegate() {
            @Override
            public void setValue(Object value) {
                if(value instanceof ModeleCellule) modeleCellule = (ModeleCellule) value;
                textPane.setBorder(BorderFactory.createLineBorder(Color.black));
                if (value != null) {
                    textPane.clear();
                    if (value instanceof ModeleCellule) {
                        EditeurIO.read(textPane, ((ModeleCellule) value).getContent());
//                        textPane.dimensionner();
                    }
                    textPane.requestFocusInWindow();
                    textPane.getUndo().discardAllEdits();
                }
            }

            @Override
            public Object getCellEditorValue() {
                textPane.setBorder(null);
                if(!textPane.hasBeenModified()) {return modeleCellule;}

                ModeleCellule newModeleCellule = new ModeleCellule();
                newModeleCellule.setBackgroundColor(modeleCellule.getBackgroundColor());
                newModeleCellule.setContent(EditeurIO.write(textPane));
                BufferedImage image = null;
                if (textPane.getHTMLdoc().getLength() > 0 && textPane.getMathComponents() != null) {
                    JLimitedMathTextPane txt = new JLimitedMathTextPane(NB_LIGNES);
                    txt.setFontSize(TableConstants.FONT_SIZE_TABLEAU);
                    txt.setAlignmentCenter(true);
                    EditeurIO.read(txt, EditeurIO.write(textPane));
//                    txt.majMathComponent();
                    textPane.add(txt);
//                    txt.dimensionner();
                    image = ImageTools.getImageFromComponent(txt);
                    textPane.remove(txt);

                    // textPane.setFont(textPane.getFont().deriveFont((float)
                    // TableConstants.FONT_SIZE_TABLEAU));
                    // textPane.dimensionner();
                    // textPane.setCaretPosition(0);
                    // textPane.getCaret().setSelectionVisible(false);
                    // textPane.getCaret().setVisible(false);
                    // textPane.select(0, 0);
                    // image = MathTools.getImage(textPane);
                }
                newModeleCellule.setRenderer(image);

                textPane.clear();
                addEdit(modeleCellule, newModeleCellule);
                return newModeleCellule;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                if (table instanceof Tableau) {
                    if (row == 0 && column == 0 && ((Tableau) table).getFirstCase().equals(TableConstants.FIRST_CASE.NOT_VISIBLE)) {
                        return null;
                    }
                }
                contentEdit = new UndoManagerTableau.ContentEdit(row, column);
                editorComponent.setFont(table.getFont());
                delegate.setValue(value);
                return supportComponent;
            }
        };
    }

    /**
     * Crée un TableauEditor qui utilise un
     * <code>CaseSeparee</code> comme éditeur.
     *
     * @param caseSeparee
     */
    private TableauEditor(final CaseSeparee caseSeparee, ControlleurTableau controlleur) {
        supportComponent = caseSeparee;
        editorComponent = caseSeparee;
        this.controlleur = controlleur;

        this.clickCountToStart = 2;

        delegate = new EditorDelegate() {
            @Override
            public void setValue(Object value) {
                if(value instanceof ModeleCellule) modeleCellule = (ModeleCellule) value;
                caseSeparee.setBorder(BorderFactory.createLineBorder(Color.black));
                if (value != null) {
                    caseSeparee.getTexteHaut().clear();
                    caseSeparee.getTexteBas().clear();

                    if (value instanceof ModeleCellule) {
                        EditeurIO.read(caseSeparee.getTexteHaut(), ((ModeleCellule) value).getContent());
                        EditeurIO.read(caseSeparee.getTexteBas(), ((ModeleCellule) value).getContent2());
                    }
                    caseSeparee.dimensionner();
                    caseSeparee.requestFocusInWindow();
                    caseSeparee.getTexteHaut().getUndo().discardAllEdits();
                    caseSeparee.getTexteBas().getUndo().discardAllEdits();
                }
            }

            @Override
            public Object getCellEditorValue() {
                if (!caseSeparee.hasbeenModified()) {
                    return modeleCellule;
                }

                ModeleCellule newModeleCellule = new ModeleCellule();
                if (modeleCellule instanceof ModeleCellule) {
                    newModeleCellule.setBackgroundColor(modeleCellule.getBackgroundColor());
                }
                newModeleCellule.setContent(EditeurIO.write(caseSeparee.getTexteHaut()));
                newModeleCellule.setContent2(EditeurIO.write(caseSeparee.getTexteBas()));
                BufferedImage image = caseSeparee.prendreImage();
                newModeleCellule.setRenderer(image);

                caseSeparee.getTexteHaut().clear();
                caseSeparee.getTexteBas().clear();
                addEdit(modeleCellule, newModeleCellule);
                return newModeleCellule;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                if (table instanceof Tableau) {
                    if (row == 0 && column == 0 && ((Tableau) table).getFirstCase().equals(TableConstants.FIRST_CASE.NOT_VISIBLE)) {
                        return null;
                    }
                }
                contentEdit = new UndoManagerTableau.ContentEdit(row, column);
                editorComponent.setPreferredSize(new Dimension(table.getColumnModel().getColumn(column).getWidth(), table.getRowHeight(row)));
                ((CaseSeparee) editorComponent).getTexteHaut().setFont(table.getFont());
                ((CaseSeparee) editorComponent).getTexteBas().setFont(table.getFont());
                delegate.setValue(value);
                return editorComponent;
            }
        };
    }

    /**
     * Finalise la création du contentEdit en ajoutant l'ancien objet et le
     * nouvel objet. Ajoute le contentEdit concernant la modification de cellule
     * à la liste du ControlleurTableau et réinitialise ce contentEdit pour une
     * nouvelle utilisation.
     *
     * @param oldObject l'ancienne valeur de la cellule
     * @param newObject la nouvelle valeur de la cellule
     */
    private void addEdit(Object oldObject, Object newObject) {
        if (oldObject instanceof ModeleCellule) {
            contentEdit.setOldModeleCellule((ModeleCellule) oldObject);
        } else {
            contentEdit.setOldModeleCellule(new ModeleCellule());
        }
        if (newObject instanceof ModeleCellule) {
            contentEdit.setNewModeleCellule((ModeleCellule) newObject);
        } else {
            contentEdit.setNewModeleCellule(new ModeleCellule());
        }
        controlleur.addEdit(contentEdit);
        contentEdit = null;
    }

//    public boolean isChanged() {
//        return isChanged;
//    }

    /**
     * Specifies the number of clicks needed to start editing.
     *
     * @param count an int specifying the number of clicks needed to start
     * editing
     * @see #getClickCountToStart
     */
    public void setClickCountToStart(int count) {
        clickCountToStart = count;
    }

    /**
     * Returns the number of clicks needed to start editing.
     *
     * @return the number of clicks needed to start editing
     */
    public int getClickCountToStart() {
        return clickCountToStart;
    }

    //
    // Override the implementations of the superclass, forwarding all methods
    // from the CellEditor interface to our delegate.
    //
    /**
     * Forwards the message from the
     * <code>CellEditor</code> to the
     * <code>delegate</code>.
     *
     * @see EditorDelegate#getCellEditorValue
     */
    @Override
    public Object getCellEditorValue() {
        return delegate.getCellEditorValue();
    }

    /**
     * Forwards the message from the
     * <code>CellEditor</code> to the
     * <code>delegate</code>.
     *
     * @see EditorDelegate#isCellEditable(EventObject)
     */
    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return delegate.isCellEditable(anEvent);
    }

    /**
     * Forwards the message from the
     * <code>CellEditor</code> to the
     * <code>delegate</code>.
     *
     * @see EditorDelegate#shouldSelectCell(EventObject)
     */
    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return delegate.shouldSelectCell(anEvent);
    }

    /**
     * Forwards the message from the
     * <code>CellEditor</code> to the
     * <code>delegate</code>.
     *
     * @see EditorDelegate#stopCellEditing
     */
    @Override
    public boolean stopCellEditing() {
        return delegate.stopCellEditing();
    }

    /**
     * Forwards the message from the
     * <code>CellEditor</code> to the
     * <code>delegate</code>.
     *
     * @see EditorDelegate#cancelCellEditing
     */
    @Override
    public void cancelCellEditing() {
        delegate.cancelCellEditing();
    }

    //
    // Implementing the TreeCellEditor Interface
    //
    /**
     * Implements the
     * <code>TreeCellEditor</code> interface.
     */
    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {

        delegate.setValue(value);
        return supportComponent;
    }

    //
    // Implementing the CellEditor Interface
    //
    /**
     * Implements the
     * <code>TableCellEditor</code> interface.
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        return delegate.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    /**
     * The protected
     * <code>EditorDelegate</code> class.
     */
    protected class EditorDelegate implements ActionListener, ItemListener, Serializable {

        /**
         * The value of this cell.
         */
        protected Object value;

        /**
         * Returns the value of this cell.
         *
         * @return the value of this cell
         */
        public Object getCellEditorValue() {
            return value;
        }

        /**
         * Sets the value of this cell.
         *
         * @param value the new value of this cell
         */
        public void setValue(Object value) {
            this.value = value;
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            setValue(value);
            return supportComponent;
        }

        /**
         * Returns true if
         * <code>anEvent</code> is <b>not</b> a
         * <code>MouseEvent</code>. Otherwise, it returns true if the necessary
         * number of clicks have occurred, and returns false otherwise.
         *
         * @param anEvent the event
         * @return true if cell is ready for editing, false otherwise
         * @see #setClickCountToStart
         * @see #shouldSelectCell
         */
        public boolean isCellEditable(EventObject anEvent) {
            if (anEvent instanceof MouseEvent) {
                return ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
            }
            return true;
        }

        /**
         * Returns true to indicate that the editing cell may be selected.
         *
         * @param anEvent the event
         * @return true
         * @see #isCellEditable
         */
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        /**
         * Returns true to indicate that editing has begun.
         *
         * @param anEvent the event
         */
        public boolean startCellEditing(EventObject anEvent) {
            return true;
        }

        /**
         * Stops editing and returns true to indicate that editing has stopped.
         * This method calls
         * <code>fireEditingStopped</code>.
         *
         * @return true
         */
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }

        /**
         * Cancels editing. This method calls
         * <code>fireEditingCanceled</code>.
         */
        public void cancelCellEditing() {
            fireEditingCanceled();
        }

        /**
         * When an action is performed, editing is ended.
         *
         * @param e the action event
         * @see #stopCellEditing
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            TableauEditor.this.stopCellEditing();
        }

        /**
         * When an item's state changes, editing is ended.
         *
         * @param e the action event
         * @see #stopCellEditing
         */
        @Override
        public void itemStateChanged(ItemEvent e) {
            TableauEditor.this.stopCellEditing();
        }
    }

//    /**
//     * Classe permettant d'associer à un éditeur de texte un layout de longueur
//     * "infinie" afin que le texte ne revienne pas à la ligne automatiquement.
//     */
//    private static class WrapEditorKit extends HTMLEditorKit {
//
//        private ViewFactory defaultFactory = new WrapColumnFactory();
//
//        @Override
//        public ViewFactory getViewFactory() {
//            return defaultFactory;
//        }
//
//        private class WrapColumnFactory extends HTMLFactory {
//
//            @Override
//            public View create(Element elem) {
//                View vue = super.create(elem);
//                if (vue instanceof javax.swing.text.html.ParagraphView) {
//                    return new NoWrapParagraphView(elem);
//                }
//                return vue;
//            }
//        }
//    }
//
//    /**
//     * Classe gérant le non retour à la ligne automatique d'un éditeur de texte.
//     */
//    private static class NoWrapParagraphView extends ParagraphView {
//
//        public NoWrapParagraphView(Element elem) {
//            super(elem);
//        }
//
//        @Override
//        public void layout(int width, int height) {
//            super.layout(Short.MAX_VALUE, height);
//        }
//
//        @Override
//        public float getMinimumSpan(int axis) {
//            return super.getPreferredSpan(axis);
//        }
//    }

//    /**
//     * Permet de déterminer si le champ de texte contenu dans une cellule du
//     * tableau a changé ou non.
//     */
//    private class DocumentChangeListener implements TextChangedListener {
//
//        @Override
//        public void textChanged(TextChangedEvent e) {
//            if (e.getComponent() != null && !e.getEventType().equals(DocumentEvent.EventType.CHANGE)) {
//                return;
//            }
//            if (e.getEventType().equals(DocumentEvent.EventType.CHANGE) && !((Component) e.getSource()).isFocusOwner()) {
//                return;
//            }
//            if (isChanged == false) {
//                isChanged = true;
//            }
//        }
//    }
}
