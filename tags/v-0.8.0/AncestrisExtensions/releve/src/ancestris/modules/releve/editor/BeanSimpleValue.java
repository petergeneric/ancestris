package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldSimpleValue;
import java.awt.BorderLayout;
import javax.swing.JTextField;

/**
 * Remarque : remplace genj.edit.beans.SimpleValueBean a laquelle il manque
 * dans le constructeur "defaultFocus = tfield;"
 * @author Michel
 */
public class BeanSimpleValue extends Bean {

    /** members */
    private JTextField tfield;
    
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

        final FieldSimpleValue property = (FieldSimpleValue) getField();
        removeAll();
        if (property == null) {
            tfield.setText("");
            add(BorderLayout.NORTH, tfield);
        } else {
            String txt = property.toString();
                tfield.setText(txt);
                add(BorderLayout.NORTH, tfield);
        }
        // not changed
        changeSupport.setChanged(false);
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        FieldSimpleValue p = (FieldSimpleValue) getField();
        String value = tfield.getText().trim();
        p.setValue(value);
        tfield.setText(value);
    }

    @Override
    protected void replaceValueImpl(Field field) {
        final FieldSimpleValue property = (FieldSimpleValue) field;
        if (property == null) {
            tfield.setText("");
        } else {
            String txt = property.toString();
            tfield.setText(txt);
        }
    }
}
