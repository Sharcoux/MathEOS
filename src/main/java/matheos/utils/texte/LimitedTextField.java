/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package matheos.utils.texte;


import java.awt.Toolkit;
import java.io.Serializable;

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * TextField that can be limited in size (max number of characters typed in)
 * @author oma
 *
 */
@SuppressWarnings("serial")
public class LimitedTextField extends JTextField {

    public LimitedTextField(int maxLength) {
        super();
        AbstractDocument doc = (AbstractDocument) getDocument();
        doc.setDocumentFilter(new TextLimiter(maxLength));
    }

    /**
     * Text limiter used to limit the number of characters of text fields
     * Should be used this way :
     *
     * AbstractDocument doc = (AbstractDocument) myTextComponent.getDocument();
    doc.setDocumentFilter(new TextLimiter(maxLength));
     *
     * @author oma
     *
     */
    private class TextLimiter extends DocumentFilter implements Serializable {
        private static final long serialVersionUID = 1L;

        private int max;

        public TextLimiter(int max) {
            this.max = max;
        }

        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset, String str, AttributeSet attr) throws BadLocationException {

            replace(fb, offset, 0, str, attr);
        }

        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String str, AttributeSet attrs) throws BadLocationException {
            int newLength = fb.getDocument().getLength() - length + str.length();

            if (newLength <= max) {
                fb.replace(offset, length, str, attrs);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }

        }
    }

}
