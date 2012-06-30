package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.CompletionListener;
import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldEventType;
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
    private Java2sAutoComboBox cListEventType;
    private CompletionProvider completionProvider;

    public BeanEventType(CompletionProvider completionProvider) {
        this.completionProvider = completionProvider;
        completionProvider.addEventTypesListener(this);
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
            cListEventType.getEditor().setItem(cListEventType.getDataList().get(0).toString());
        } else {
            //cListEventType.setText(eventType.toString());
            cListEventType.getEditor().setItem(eventType.toString());
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
                cListEventType.getEditor().setItem(eventType.toString());
            }
        });
    }

    @Override
    protected void replaceValueImpl(Field field) {
       final FieldEventType eventType = (FieldEventType) field;
        if (eventType == null) {
            // j'affiche le premier element de la liste pas defaut
            //cListEventType.setText(cListEventType.getDataList().get(0).toString());
            cListEventType.getEditor().setItem(cListEventType.getDataList().get(0).toString());
        } else {
            //cListEventType.setText(eventType.toString());
            cListEventType.getEditor().setItem(eventType.toString());
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
        String value = cListEventType.getEditor().getItem().toString().trim();
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
        cListEventType.getEditor().setItem(fieldEventType.toString());
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
    public void keyUpdated(List<String> keyList) {
        cListEventType.setDataList(keyList);
    }

}
