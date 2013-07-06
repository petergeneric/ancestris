package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.CompletionListener;
import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.CompletionProvider.IncludeFilter;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldSimpleValue;
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
public class BeanLastName extends Bean implements CompletionListener {
    private Java2sAutoTextField cLast;
    CompletionProvider completionProvider;

    public BeanLastName(CompletionProvider completionProvider) {
        this.completionProvider = completionProvider;
        completionProvider.addLastNamesListener(this);
        setLayout(new java.awt.BorderLayout());
        cLast = new Java2sAutoTextField(completionProvider.getLastNames(IncludeFilter.INCLUDED));
        cLast.setStrict(false);        
        cLast.setCaseSensitive(false);
        cLast.setUpperAllChar(true);
        cLast.setLocale(completionProvider.getLocale()); //Locale.UK
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

        final FieldSimpleValue name = (FieldSimpleValue) getField();
        if (name == null) {
            cLast.setText("");
        } else {
            cLast.setText(name.toString());
        }
        
        // je configure le raccourci de la touche ESCAPE pour annuler la saisie en cours
        resetKeyboardActions();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, this);
        getActionMap().put(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // restaure la valeur
                cLast.setText(name.toString());
            }
        });
    }

    @Override
    protected void replaceValueImpl(Field field) {
       final FieldSimpleValue name = (FieldSimpleValue) field;
        if (name == null) {
            cLast.setText("");
        } else {
            cLast.setText(name.toString());
        }
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {

        FieldSimpleValue fieldName = (FieldSimpleValue) getField();

        // je supprime les espaces aux extremites
        String lastName = cLast.getText().trim();

        //last = last.toUpperCase();
        cLast.setText(lastName);

        // j'enregistre la nouvelle valeur
        fieldName.setValue( lastName);
    }

    /**
     * je supprime la declaration de listener
     * avant que l'objet ne soit detruit
     */
    @Override
    public void removeNotify() {
        completionProvider.removeLastNamesListener(this);
        super.removeNotify();
    }

    /**
     * Implemente CompletionListener
     * copie la nouvelle liste de completion
     * @param keyList
     */
    @Override
    public void keyUpdated(List<String> keyList) {
        cLast.setDataList(keyList);
    }
}
