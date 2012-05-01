package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldSex;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 *
 * @author Michel
 */
public class BeanSex extends Bean {

    private javax.swing.JComboBox jComboBox1;

    public BeanSex() {
        setLayout(new java.awt.BorderLayout());
        jComboBox1 = new javax.swing.JComboBox();

        // l'ordre des valeurs dans la combobox doit correspondre à l'ordre des
        // valeurs predefinies dans FieldSex
        //  UNKNOWN = 0;
        //  MALE = 1;
        //  FEMALE = 2;
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Inconnu", "Masculin", "Feminin" }));
        jComboBox1.addActionListener(changeSupport);
        add(jComboBox1, java.awt.BorderLayout.CENTER);
        defaultFocus = jComboBox1;
    }

    @Override
    protected void setFieldImpl() {
        final FieldSex sex = (FieldSex) getField();
        jComboBox1.setSelectedIndex(sex.getSex());

         // je configure le raccourci de la touche ESCAPE pour annuler la saisie en cours
        resetKeyboardActions();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, this);
        getActionMap().put(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // restaure la valeur
                jComboBox1.setSelectedIndex(sex.getSex());
            }
        });
    }

    @Override
    protected void commitImpl()  {
        FieldSex sex = (FieldSex)getField();
        sex.setSex(jComboBox1.getSelectedIndex());
    }

    @Override
    protected void replaceValueImpl(Field field) {
        final FieldSex sex = (FieldSex) field;
        jComboBox1.setSelectedIndex(sex.getSex());
    }
}
