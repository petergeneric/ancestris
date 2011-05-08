/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.editorstd.beans;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author frederic
 */
public class AutoCompleteCombo extends JComboBox implements JComboBox.KeySelectionManager {

    private String searchFor;
    private long lap;

    public class CBDocument extends PlainDocument {

        @Override
        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            if (str == null) {
                return;
            }
            super.insertString(offset, str, a);
            if (!isPopupVisible() && str.length() != 0) {
                fireActionEvent();
            }
        }
    }

    public AutoCompleteCombo(Object[] items) {
        super(items);
        lap = new java.util.Date().getTime();
        setKeySelectionManager(this);
        JTextField tf;
        if (getEditor() != null) {
            tf = (JTextField) getEditor().getEditorComponent();
            if (tf != null) {
                tf.setDocument(new CBDocument());
                addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        JTextField tf = (JTextField) getEditor().getEditorComponent();
                        String text = tf.getText();
                        ComboBoxModel aModel = getModel();
                        String current;
                        for (int i = 0; i < aModel.getSize(); i++) {
                            current = aModel.getElementAt(i).toString();
                            if (current.toLowerCase().startsWith(text.toLowerCase())) {
                                tf.setText(current);
                                tf.setSelectionStart(text.length());
                                tf.setSelectionEnd(current.length());
                                break;
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public int selectionForKey(char aKey, ComboBoxModel aModel) {
        long now = new java.util.Date().getTime();
        if (searchFor != null && aKey == KeyEvent.VK_BACK_SPACE && searchFor.length() > 0) {
            searchFor = searchFor.substring(0, searchFor.length() - 1);
        } else {
            if (lap + 1000 < now) {
                searchFor = "" + aKey;
            } else {
                searchFor = searchFor + aKey;
            }
        }
        lap = now;
        String current;
        for (int i = 0; i < aModel.getSize(); i++) {
            current = aModel.getElementAt(i).toString().toLowerCase();
            if (current.toLowerCase().startsWith(searchFor.toLowerCase())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void fireActionEvent() {
        super.fireActionEvent();
    }
}
