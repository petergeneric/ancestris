package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldEventType;
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
    private Java2sAutoComboBox cListEventType;

    public BeanEventType(CompletionProvider completionProvider) {
        setLayout(new java.awt.BorderLayout());
        cListEventType = new Java2sAutoComboBox(completionProvider.getEventTypes());
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

        final FieldEventType eventType = (FieldEventType) getField();
        if (eventType == null) {
            // j'affiche le premier element de la liste pas defaut
            //cListEventType.setText(cListEventType.getDataList().get(0).toString());
            cListEventType.setSelectedValue(cListEventType.getDataList().get(0).toString());
        } else {
            //cListEventType.setText(eventType.toString());
            cListEventType.setSelectedValue(eventType.toString());
        }
        
        // je configure le raccourci de la touche ESCAPE pour annuler la saisie en cours
        resetKeyboardActions();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, this);
        getActionMap().put(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // restaure la valeur
                //cListEventType.setText(eventType.toString());
                cListEventType.setSelectedValue(eventType.toString());
            }
        });
    }

    @Override
    protected void replaceValueImpl(Field field) {
       final FieldEventType eventType = (FieldEventType) field;
        if (eventType == null) {
            // j'affiche le premier element de la liste pas defaut
            //cListEventType.setText(cListEventType.getDataList().get(0).toString());
            cListEventType.setSelectedValue(cListEventType.getDataList().get(0).toString());
        } else {
            //cListEventType.setText(eventType.toString());
            cListEventType.setSelectedValue(eventType.toString());
        }
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {

        FieldEventType fieldEventType = (FieldEventType) getField();

        // je supprime les espaces aux extremetes
        //String value = cListEventType.getText().trim();
        String value = cListEventType.getSelectedItem().toString().trim();
        // parse tag,name
        int separatorIndex = value.indexOf(' ');

        // j'enregistre les valeurs dans la variable field
        if ( separatorIndex == -1 ) {
            fieldEventType.setTag(value);
            fieldEventType.setName("");
        } else {
            fieldEventType.setTag(value.substring(0,separatorIndex));
            fieldEventType.setName(value.substring(separatorIndex+1,value.length()));
        }

        // j'affiche la valeur mise en forme
        cListEventType.setSelectedValue(fieldEventType.toString());
    }
}
