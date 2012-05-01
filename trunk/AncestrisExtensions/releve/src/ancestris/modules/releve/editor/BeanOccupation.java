package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldOccupation;

/**
 *
 * @author Michel
 */
public class BeanOccupation extends Bean {
    private Java2sAutoTextField cOccupation;
    
    public BeanOccupation(CompletionProvider completionProvider) {
        setLayout(new java.awt.BorderLayout());
        cOccupation = new Java2sAutoTextField(completionProvider.getOccupations());
        cOccupation.setStrict(false);
        cOccupation.setCaseSensitive(false);
        cOccupation.setUpperFirstChar(true);
        cOccupation.setLocale(completionProvider.getLocale());
        cOccupation.addChangeListener(changeSupport);
        
        // Layout the bean
        add(cOccupation, java.awt.BorderLayout.CENTER);

        // je fixe le focus par defaut
        defaultFocus = cOccupation;
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {

        final FieldOccupation occupationField = (FieldOccupation) getField();
        if (occupationField == null) {
            cOccupation.setText("");
        } else {
            cOccupation.setText(occupationField.toString());
        }        
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        String occupation = cOccupation.getText().trim();

        FieldOccupation fieldOccupation = (FieldOccupation) getField();
        fieldOccupation.setValue(occupation);
        // je rafraichi l'affichage du bean
        cOccupation.setText(occupation);
    }

    @Override
    protected void replaceValueImpl(Field field) {
        FieldOccupation occupationField = (FieldOccupation) field;
        if (occupationField == null) {
            cOccupation.setText("");
        } else {
            cOccupation.setText(occupationField.toString());
        }  
    }
}
