package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldPlace;
import genj.util.swing.TextFieldWidget;
import java.awt.BorderLayout;

/**
 * Remarque : remplace genj.edit.beans.SimpleValueBean a laquelle il manque
 * dans le constructeur "defaultFocus = tfield;"
 * @author Michel
 */
public class BeanPlace extends Bean {

    /** members */
    private TextFieldWidget tfield;
    
    public BeanPlace() {
        tfield = new TextFieldWidget("", 8);
        tfield.addChangeListener(changeSupport);

        setLayout(new BorderLayout());
        add(tfield, BorderLayout.CENTER);
        defaultFocus = tfield;
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {

        final FieldPlace placeField = (FieldPlace) getField();
        removeAll();
        if (placeField == null) {
            tfield.setText("");
            tfield.setEditable(true);
            tfield.setVisible(true);
            add(BorderLayout.NORTH, tfield);
        } else {
            String txt = placeField.toString();
                tfield.setText(txt);
                tfield.setEditable(true);
                tfield.setVisible(true);
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
        FieldPlace placeField = (FieldPlace) getField();
        String value = tfield.getText().trim();
        placeField.setValue(value);
        tfield.setText(value);
    }

    @Override
    protected void replaceValueImpl(Field field) {
        FieldPlace placeField = (FieldPlace) field;
        if (placeField == null) {
            tfield.setText("");
          } else {
            tfield.setText(placeField.toString());
        }
    }
}
