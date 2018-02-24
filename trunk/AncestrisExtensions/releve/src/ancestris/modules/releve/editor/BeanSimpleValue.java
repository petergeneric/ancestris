package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.Field;
import java.awt.BorderLayout;
import javax.swing.JTextField;

/**
 * Remarque : remplace genj.edit.beans.SimpleValueBean a laquelle il manque
 * dans le constructeur "defaultFocus = tfield;"
 * @author Michel
 */
public class BeanSimpleValue extends Bean {

    /** members */
    private final JTextField tfield;
    
    public BeanSimpleValue() {

        tfield = new JTextField("", 8);
        tfield.getDocument().addDocumentListener(changeSupport);

        setLayout(new BorderLayout());
        add(tfield, BorderLayout.CENTER);
        defaultFocus = tfield;
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {

        removeAll();
        tfield.setText(getFieldValue());
        add(BorderLayout.NORTH, tfield);
        // not changed
        changeSupport.setChanged(false);
    }

    @Override
    protected void replaceValueImpl(Field field) {
        if (field == null) {
            tfield.setText("");
        } else {
            tfield.setText(field.toString());
        }
    }
    
    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        String value = tfield.getText().trim();
        setFieldValue(value);
        tfield.setText(value);
    }

}
