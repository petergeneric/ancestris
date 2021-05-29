package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldSex;
import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 *
 * @author Michel
 */
public class BeanSex extends Bean {

    private final MyCombobox jComboBox1;

    public BeanSex() {
        setLayout(new java.awt.BorderLayout());
        jComboBox1 = new MyCombobox();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[]{FieldSex.unknownLabel, FieldSex.maleLabel, FieldSex.femaleLabel }));
        jComboBox1.addActionListener(changeSupport);

        add(jComboBox1, java.awt.BorderLayout.CENTER);
        defaultFocus = jComboBox1;

    }

    @Override
    protected void setFieldImpl() {
        final FieldSex fieldSex = (FieldSex) getField();
        if(fieldSex != null ) {
            jComboBox1.setSelectedItem(fieldSex.toString());        
        } else {
            jComboBox1.setSelectedItem(FieldSex.unknownLabel);
        }
                
         // je configure le raccourci de la touche ESCAPE pour annuler la saisie en cours
        resetKeyboardActions();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, this);
        getActionMap().put(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // restaure la valeur
                if (fieldSex != null) {
                    jComboBox1.setSelectedItem(fieldSex.toString());
                } else {
                    jComboBox1.setSelectedItem(FieldSex.unknownLabel);
                }
            }
        });
    }

    @Override
    protected void replaceValueImpl(Field field) {
        if(field != null ) {
            jComboBox1.setSelectedItem(field.toString());        
        } else {
            jComboBox1.setSelectedItem(FieldSex.unknownLabel);
        }        
    }

    @Override
    protected void commitImpl()  {
        setFieldValue(jComboBox1.getSelectedItem().toString());
    }

    
    /**
     * cette combobox ignore les combinaisons de touche ALT-VK_DOWN et ALT-VK_UP
     */
    private class MyCombobox extends JComboBox<String> {

        @Override
        public void processKeyEvent(KeyEvent e) {
            if ((e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) && (e.getModifiers() & InputEvent.ALT_MASK) != 0 && !super.isPopupVisible()) {
                getParent().dispatchEvent((AWTEvent) e);
                return; //don't process the event
            }
            super.processKeyEvent(e);
        }
    }
}
