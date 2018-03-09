package ancestris.modules.releve.editor;

import ancestris.modules.releve.utils.Java2sAutoComboBox;
import ancestris.modules.releve.model.CompletionListener;
import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.CompletionProvider.IncludeFilter;
import ancestris.modules.releve.model.Field;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 *
 * @author Michel
 */
public class BeanEventType extends Bean implements CompletionListener {
    //private Java2sAutoTextField cListEventType;
    private final Java2sAutoComboBox cListEventType;
    private final CompletionProvider completionProvider;

    public BeanEventType(CompletionProvider completionProvider) {
        this.completionProvider = completionProvider;
        completionProvider.addEventTypesListener(this);
        setLayout(new java.awt.BorderLayout());
        cListEventType = new Java2sAutoComboBox(completionProvider.getEventTypes(IncludeFilter.INCLUDED));
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

    /**
     * je supprime la declaration de listener
     * avant que l'objet ne soit detruit
     */
    @Override
    public void removeNotify() {
        completionProvider.removeEventTypesListener(this);
        super.removeNotify();
    }

    /**
     * Implemente CompletionListener
     * copie la nouvelle liste de completion
     * @param keyList
     */
    @Override
    public void includedKeyUpdated(List<String> keyList) {
        cListEventType.setDataList(keyList);
    }

}
