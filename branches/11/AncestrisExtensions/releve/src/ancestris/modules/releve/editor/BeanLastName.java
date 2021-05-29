package ancestris.modules.releve.editor;

import ancestris.modules.releve.utils.Java2sAutoTextField;
import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.Field;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 *
 * @author Michel
 */
public class BeanLastName extends Bean  {
    private final Java2sAutoTextField cLast;
    
    public BeanLastName(CompletionProvider.CompletionSource completionSource) {
        setLayout(new java.awt.BorderLayout());
        cLast = new Java2sAutoTextField(completionSource.getIncluded());
        cLast.setStrict(false);        
        cLast.setCaseSensitive(false);
        cLast.setUpperAllChar(true);
        cLast.setLocale(completionSource.getLocale()); //Locale.UK
        cLast.addChangeListener(changeSupport);
        // Layout the bean
        add(cLast, java.awt.BorderLayout.CENTER);
        defaultFocus = cLast;
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {

        cLast.setText(getFieldValue());
        
        // je configure le raccourci de la touche ESCAPE pour annuler la saisie en cours
        resetKeyboardActions();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, this);
        getActionMap().put(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // restaure la valeur
                cLast.setText(getFieldValue());
            }
        });
    }

    @Override
    protected void replaceValueImpl(Field field) {
        if (field == null) {
            cLast.setText("");
        } else {
            cLast.setText(field.toString());
        }
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {

        // je supprime les espaces aux extremites
        String value = cLast.getText().trim();

        //last = last.toUpperCase();
        cLast.setText(value);

        // j'enregistre la nouvelle valeur
        setFieldValue(value);
    }
    
}
