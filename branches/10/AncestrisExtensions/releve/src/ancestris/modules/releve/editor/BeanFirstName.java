package ancestris.modules.releve.editor;

import ancestris.modules.releve.utils.Java2sAutoTextField;
import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.Field;

/**
 *
 * @author Michel
 */
public class BeanFirstName extends Bean {
    private final Java2sAutoTextField cFirst;
    
    public BeanFirstName(CompletionProvider.CompletionSource completionSource) {
        setLayout(new java.awt.BorderLayout());
        cFirst = new Java2sAutoTextField(completionSource.getIncluded());
        cFirst.setStrict(false);
        cFirst.setCaseSensitive(false);
        cFirst.setUpperAllFirstChar(true);
        cFirst.setLocale(completionSource.getLocale()); 
        cFirst.addChangeListener(changeSupport);
        
        // Layout the bean
        add(cFirst, java.awt.BorderLayout.CENTER);

        // je fixe le focus par defaut
        defaultFocus = cFirst;
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {
        cFirst.setText(getFieldValue());
    }

    @Override
    protected void replaceValueImpl(Field field) {
        if (field == null) {
            cFirst.setText("");
        } else {
            cFirst.setText(field.toString());
        }
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        String value = cFirst.getText().trim().replaceAll(",", "");
        
        if (!value.isEmpty()) {
            // première lettre en majuscules
            value = Character.toString(value.charAt(0)).toUpperCase() + value.substring(1);
        }
        setFieldValue(value);
        cFirst.setText(value);
    }

}
