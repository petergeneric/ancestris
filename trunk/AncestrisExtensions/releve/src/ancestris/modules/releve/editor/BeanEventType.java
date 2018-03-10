package ancestris.modules.releve.editor;

import ancestris.modules.releve.utils.Java2sAutoComboBox;
import ancestris.modules.releve.model.CompletionProvider.CompletionSource;
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
public class BeanEventType extends Bean {
    //private Java2sAutoTextField cListEventType;
    private final Java2sAutoComboBox cListEventType;

    public BeanEventType(CompletionSource completionSource) {
        setLayout(new java.awt.BorderLayout());
        cListEventType = new Java2sAutoComboBox(completionSource.getIncluded());
        cListEventType.setStrict(false);        
        cListEventType.addChangeListener(changeSupport);
        add(cListEventType, java.awt.BorderLayout.CENTER);
        defaultFocus = cListEventType;
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {
        final String value = getFieldValue();
        cListEventType.getEditor().setItem(value);
        
        // je configure le raccourci de la touche ESCAPE pour annuler la saisie en cours
        resetKeyboardActions();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, this);
        getActionMap().put(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // restaure la valeur
                cListEventType.getEditor().setItem(value);
            }
        });
    }

    @Override
    protected void replaceValueImpl(Field field) {
        if (field == null) {
            cListEventType.getEditor().setItem("");
        } else {
            cListEventType.getEditor().setItem(field.toString());
        }
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        // je supprime les espaces aux extremetes
        String value = cListEventType.getEditor().getItem().toString().trim();
        // j'enregistre les valeurs dans la variable field
         setFieldValue(value);  
        // j'affiche la valeur mise en forme
        cListEventType.getEditor().setItem(value);
    }

 }
