package ancestris.modules.releve.editor;

import ancestris.modules.releve.utils.Java2sAutoComboBox;
import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.Field;
import java.awt.BorderLayout;

/**
 * 
 * @author Michel
 */
public class BeanPlace extends Bean {

    private final Java2sAutoComboBox tfield;
    
    public BeanPlace(CompletionProvider.CompletionSource completionSource) {
        setLayout(new BorderLayout());
        tfield = new Java2sAutoComboBox(completionSource.getIncluded());
        tfield.setStrict(false);
        tfield.setCaseSensitive(false);
        tfield.setUpperAllFirstChar(true);
        tfield.setLocale(completionSource.getLocale());
        tfield.addChangeListener(changeSupport);

        add(tfield, BorderLayout.CENTER);
        defaultFocus = tfield;
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {
        tfield.getEditor().setItem(getFieldValue());
    }

    @Override
    protected void replaceValueImpl(Field field) {
        if (field == null) {
            tfield.getEditor().setItem("");
        } else {
            tfield.getEditor().setItem(field.toString());
        }
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        String value = tfield.getEditor().getItem().toString().trim();
        setFieldValue(value);
        tfield.getEditor().setItem(getFieldValue());
    }
    
}
