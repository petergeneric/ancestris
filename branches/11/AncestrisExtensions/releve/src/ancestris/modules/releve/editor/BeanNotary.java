package ancestris.modules.releve.editor;

import ancestris.modules.releve.utils.Java2sAutoComboBox;
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
public class BeanNotary extends Bean{
    private final Java2sAutoComboBox jCombobox;
    
    public BeanNotary(CompletionProvider.CompletionSource completionSource) {
        setLayout(new java.awt.BorderLayout());
        jCombobox = new Java2sAutoComboBox(completionSource.getIncluded());
        jCombobox.setStrict(false);
        jCombobox.addChangeListener(changeSupport);
        add(jCombobox, java.awt.BorderLayout.CENTER);
        defaultFocus = jCombobox;
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {

        
        jCombobox.getEditor().setItem( getFieldValue() );
        
        // je configure le raccourci de la touche ESCAPE pour annuler la saisie en cours
        resetKeyboardActions();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, this);
        getActionMap().put(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // restaure la valeur
                jCombobox.getEditor().setItem( getFieldValue() );
            }
        });
    }

    @Override
    protected void replaceValueImpl(Field field) {
        if (field == null) {
            jCombobox.getEditor().setItem("");
        } else {
            jCombobox.getEditor().setItem(field.toString());
        }
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {

        // je supprime les espaces aux extremetes
        String value = jCombobox.getEditor().getItem().toString().trim();

        // j'enregistre les valeurs dans la variable field
        setFieldValue(value);

        // j'affiche la valeur mise en forme
        jCombobox.getEditor().setItem(value);
    }

}
