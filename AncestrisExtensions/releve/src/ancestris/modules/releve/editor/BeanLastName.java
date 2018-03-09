package ancestris.modules.releve.editor;

import ancestris.modules.releve.utils.Java2sAutoTextField;
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
public class BeanLastName extends Bean implements CompletionListener {
    private final Java2sAutoTextField cLast;
    CompletionProvider completionProvider;

    public BeanLastName(CompletionProvider completionProvider) {
        this.completionProvider = completionProvider;
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
    
    /**
     * je supprime la declaration de listener
     * avant que l'objet ne soit detruit
     */
    @Override
    public void addNotify() {
        super.addNotify();
        completionProvider.addLastNamesListener(this);
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
    public void includedKeyUpdated(List<String> keyList) {
        cLast.setDataList(keyList);
    }
}
