package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.CompletionListener;
import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.CompletionProvider.IncludeFilter;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldPlace;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

/**
 * 
 * @author Michel
 */
public class BeanPlace extends Bean implements CompletionListener {

    private final Java2sAutoComboBox tfield;
    private final CompletionProvider completionProvider;
    
    public BeanPlace(CompletionProvider completionProvider) {
        this.completionProvider = completionProvider;
        completionProvider.addPlacesListener(this);
        setLayout(new BorderLayout());
        tfield = new Java2sAutoComboBox(completionProvider.getPlaces(IncludeFilter.INCLUDED));
        tfield.setStrict(false);
        tfield.setCaseSensitive(false);
        tfield.setUpperAllFirstChar(true);
        tfield.setLocale(completionProvider.getLocale());
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
        String oldValue = getFieldValue();
        String value = tfield.getEditor().getItem().toString().trim();
        setFieldValue(value);
        // je mets a jour la liste de completion
        completionProvider.updatePlaces(getFieldValue(), oldValue);
        tfield.getEditor().setItem(getFieldValue());
    }

    
    /**
     * je supprime la declaration de listener
     * avant que l'objet ne soit detruit
     */
    @Override
    public void removeNotify() {
        completionProvider.removePlacesListener(this);
        super.removeNotify();
    }

    /**
     * Implemente CompletionListener
     * copie la nouvelle liste de completion
     * @param keyList
     */
    @Override
    public void includedKeyUpdated(List<String> keyList) {
        //if( keyList != null && tfield != null) {
            tfield.setDataList(keyList);
        //} else {
        //    return;
        //}
        
    }


}
