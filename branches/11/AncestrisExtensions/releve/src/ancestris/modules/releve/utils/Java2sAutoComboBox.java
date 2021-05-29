package ancestris.modules.releve.utils;

import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class Java2sAutoComboBox extends JComboBox<String> {

    private final AutoTextFieldEditor autoTextFieldEditor;    

    public Java2sAutoComboBox(List<String> stringList) {
        autoTextFieldEditor = new AutoTextFieldEditor(stringList, this);
        setEditable(true);
        setModel(new StringListModel(stringList));
        setEditor(autoTextFieldEditor);
        setPrototypeDisplayValue("AAAAAAAAAAA");
    }
    
    /**
     * Add change listener
     */
    public void addChangeListener(ChangeListener l) {
        autoTextFieldEditor.getAutoTextFieldEditor().addChangeListener(l);
    }

    /**
     * Remove change listener
     */
    public void removeChangeListener(ChangeListener l) {
        autoTextFieldEditor.getAutoTextFieldEditor().removeChangeListener(l);
    }

    /*
     * Java2sAutoComboBox Parameters 
     */

    public boolean isCaseSensitive() {
        return autoTextFieldEditor.getAutoTextFieldEditor().isCaseSensitive();
    }

    public boolean isStrict() {
        return autoTextFieldEditor.getAutoTextFieldEditor().isStrict();
    }

    public void setCaseSensitive(boolean flag) {
        autoTextFieldEditor.getAutoTextFieldEditor().setCaseSensitive(flag);
    }

    public void setUpperAllFirstChar(boolean flag) {
        autoTextFieldEditor.getAutoTextFieldEditor().setUpperAllFirstChar(flag);
    }

    public void setUpperAllChar(boolean flag) {
        autoTextFieldEditor.getAutoTextFieldEditor().setUpperAllChar(flag);
    }

    public void setStrict(boolean flag) {
        autoTextFieldEditor.getAutoTextFieldEditor().setStrict(flag);
    }

    protected void setSelectedValue(Object obj) {
        setSelectedItem(obj);
    }


    ////////////////////////////////////////////////////////////////////////////
    // class StringListModel
    ////////////////////////////////////////////////////////////////////////////
    
    static class StringListModel extends DefaultComboBoxModel<String> {

        private final List<String> strings;

        public StringListModel(List<String> stringList) {
            super();
            strings = stringList;
        }

        public String getSelectedString() {
            return (String) getSelectedItem();
        }

        @Override
        public String getElementAt(int index) {
            return strings.get(index);
        }

        @Override
        public int getSize() {
            return strings.size();
        }

        @Override
        public int getIndexOf(Object element) {
            return strings.indexOf(element);
        }

    } 
    
    ////////////////////////////////////////////////////////////////////////////
    // class AutoTextFieldEditor
    ////////////////////////////////////////////////////////////////////////////
    
    static private class AutoTextFieldEditor extends BasicComboBoxEditor {

        private Java2sAutoTextField getAutoTextFieldEditor() {
            return (Java2sAutoTextField) editor;
        }

        AutoTextFieldEditor(List<String> list,  Java2sAutoComboBox comboBox) {
            editor = new Java2sAutoTextField(list, comboBox);
        }

        @Override
        public void setItem(Object anObject) {
            if (anObject == null) {
                editor.setText("");
            } else {
                editor.setText(anObject.toString());
            }
        }
    }
}
