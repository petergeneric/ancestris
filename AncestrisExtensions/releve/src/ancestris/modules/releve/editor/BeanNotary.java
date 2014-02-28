package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.CompletionListener;
import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.CompletionProvider.IncludeFilter;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldNotary;
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
public class BeanNotary extends Bean implements CompletionListener {
    private Java2sAutoComboBox jCombobox;
    private CompletionProvider completionProvider;

    public BeanNotary(CompletionProvider completionProvider) {
        this.completionProvider = completionProvider;
        completionProvider.addNotariesListener(this);
        setLayout(new java.awt.BorderLayout());
        jCombobox = new Java2sAutoComboBox(completionProvider.getNotaries(IncludeFilter.INCLUDED));
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

        final FieldNotary notaryField = (FieldNotary) getField();
        if (notaryField == null) {
            // je n'affiche rien
            jCombobox.getEditor().setItem("");
        } else {
            jCombobox.getEditor().setItem(notaryField.toString());
        }
        
        // je configure le raccourci de la touche ESCAPE pour annuler la saisie en cours
        resetKeyboardActions();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, this);
        getActionMap().put(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // restaure la valeur
                jCombobox.getEditor().setItem(notaryField.toString());
            }
        });
    }

    @Override
    protected void replaceValueImpl(Field field) {
       final FieldNotary notaryField = (FieldNotary) field;
        if (notaryField == null) {
            // je n'affiche rien
            jCombobox.getEditor().setItem("");
        } else {
            jCombobox.getEditor().setItem(notaryField.toString());
        }
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {

        FieldNotary fieldNotary = (FieldNotary) getField();

        // je supprime les espaces aux extremetes
        String value = jCombobox.getEditor().getItem().toString().trim();

        // j'enregistre les valeurs dans la variable field
        fieldNotary.setValue(value.trim());

        // j'affiche la valeur mise en forme
        jCombobox.getEditor().setItem(fieldNotary.toString());
    }

    /**
     * je supprime la declaration de listener
     * avant que l'objet ne soit detruit
     */
    @Override
    public void removeNotify() {
        completionProvider.removeNotariesListener(this);
        super.removeNotify();
    }

    /**
     * Implemente CompletionListener
     * copie la nouvelle liste de completion
     * @param keyList
     */
    @Override
    public void includedKeyUpdated(List<String> keyList) {
        jCombobox.setDataList(keyList);
    }

}
