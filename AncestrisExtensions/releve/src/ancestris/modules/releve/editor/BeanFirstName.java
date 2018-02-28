package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.CompletionListener;
import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.CompletionProvider.IncludeFilter;
import ancestris.modules.releve.model.Field;
import java.util.List;

/**
 *
 * @author Michel
 */
public class BeanFirstName extends Bean implements CompletionListener {
    private final Java2sAutoTextField cFirst;
    private final CompletionProvider completionProvider;
    
    public BeanFirstName(CompletionProvider completionProvider) {
        this.completionProvider = completionProvider;
        setLayout(new java.awt.BorderLayout());
        cFirst = new Java2sAutoTextField(completionProvider.getFirstNames(IncludeFilter.INCLUDED));
        cFirst.setStrict(false);
        cFirst.setCaseSensitive(false);
        cFirst.setUpperAllFirstChar(true);
        cFirst.setLocale(completionProvider.getLocale()); 
        cFirst.addChangeListener(changeSupport);
        
        // Layout the bean
        add(cFirst, java.awt.BorderLayout.CENTER);

        // je fixe le focus par defaut
        defaultFocus = cFirst;
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {
        cFirst.setText(getFieldValue());
    }

    @Override
    protected void replaceValueImpl(Field field) {
        if (field == null) {
            cFirst.setText("");
        } else {
            cFirst.setText(field.toString());
        }
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        String value = cFirst.getText().trim().replaceAll(",", "");
        
        if (!value.isEmpty()) {
            // première lettre en majuscules
            value = Character.toString(value.charAt(0)).toUpperCase() + value.substring(1);
        }
        setFieldValue(value);
        cFirst.setText(value);
    }

    /**
     * j'ajoute la declaration de listener
     * apres que l'objet soit créé
     */
    @Override
    public void addNotify() {
        super.addNotify();
        completionProvider.addFirstNamesListener(this);
    }
    
    /**
     * je supprime la declaration de listener
     * avant que l'objet ne soit detruit
     */
    @Override
    public void removeNotify() {
        completionProvider.removeFirstNamesListener(this);
        super.removeNotify();
    }

    /**
     * Implemente CompletionListener
     * copie la nouvelle liste de completion
     * @param keyList
     */
    @Override
    public void includedKeyUpdated(List<String> keyList) {
        cFirst.setDataList(keyList);        
    }
}
