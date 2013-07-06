package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.CompletionListener;
import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.CompletionProvider.IncludeFilter;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldPlace;
import java.awt.BorderLayout;
import java.util.List;

/**
 * 
 * @author Michel
 */
public class BeanPlace extends Bean implements CompletionListener {

    //private Java2sAutoTextField tfield;
    private Java2sAutoComboBox tfield;
    CompletionProvider completionProvider;
    
    public BeanPlace(CompletionProvider completionProvider) {
        this.completionProvider = completionProvider;
        //tfield = new Java2sAutoTextField(completionProvider.getPlaces());
        tfield = new Java2sAutoComboBox(completionProvider.getPlaces(IncludeFilter.INCLUDED));
        completionProvider.addPlacesListener(this);

        tfield.setStrict(false);
        tfield.setCaseSensitive(false);
        tfield.setUpperAllFirstChar(true);
        tfield.setLocale(completionProvider.getLocale());
        tfield.addChangeListener(changeSupport);

        setLayout(new BorderLayout());
        add(tfield, BorderLayout.CENTER);
        defaultFocus = tfield;
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {

        final FieldPlace placeField = (FieldPlace) getField();
        if (placeField == null) {
            //tfield.setText("");
            tfield.getEditor().setItem("");
        } else {
            //tfield.setText(placeField.toString());
            tfield.getEditor().setItem(placeField.toString());
        }
    }

    @Override
    protected void replaceValueImpl(Field field) {
        final FieldPlace placeField = (FieldPlace) getField();
        if (placeField == null) {
            //tfield.setText("");
            tfield.getEditor().setItem("");
        } else {
            //tfield.setText(placeField.toString());
            tfield.getEditor().setItem(placeField.toString());
        }
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        FieldPlace placeField = (FieldPlace) getField();
        String oldValue = placeField.toString();
        String value = tfield.getEditor().getItem().toString().trim();
        placeField.setValue(value);
        // je mets a jour la liste de completion
        completionProvider.updatePlaces(placeField, oldValue);
        tfield.getEditor().setItem(placeField.toString());
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
    public void keyUpdated(List<String> keyList) {
        tfield.setDataList(keyList);
    }


}
