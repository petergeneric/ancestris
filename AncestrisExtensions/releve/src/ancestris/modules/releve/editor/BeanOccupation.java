package ancestris.modules.releve.editor;

import ancestris.modules.releve.utils.Java2sAutoTextField;
import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.Field;

/**
 *
 * @author Michel
 */
public class BeanOccupation extends Bean {
    private final Java2sAutoTextField cOccupation;
    CompletionProvider completionProvider;

    public BeanOccupation(CompletionProvider.CompletionSource completionSource) {
        setLayout(new java.awt.BorderLayout());
        cOccupation = new Java2sAutoTextField(completionSource.getIncluded());
        cOccupation.setStrict(false);
        cOccupation.setCaseSensitive(false);
        cOccupation.setUpperFirstChar(true);
        cOccupation.setLocale(completionSource.getLocale());
        cOccupation.addChangeListener(changeSupport);
        
        add(cOccupation, java.awt.BorderLayout.CENTER);
        // je fixe le focus par defaut
        defaultFocus = cOccupation;
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {
        cOccupation.setText( getFieldValue() );
    }

    @Override
    protected void replaceValueImpl(Field field) {
        if (field == null) {
            cOccupation.setText("");
        } else {
            cOccupation.setText(field.toString());
        }
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        String occupation = cOccupation.getText().trim();

        setFieldValue(occupation);
        // je rafraichi l'affichage du bean
        cOccupation.setText(occupation);
    }

}
