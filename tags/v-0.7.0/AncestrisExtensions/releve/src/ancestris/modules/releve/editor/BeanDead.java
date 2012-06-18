package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldDead;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 *
 * @author Michel
 */
public class BeanDead extends Bean {

    private javax.swing.JCheckBox jCheckBoxDead;

    public BeanDead() {
        setLayout(new java.awt.BorderLayout());
        jCheckBoxDead = new javax.swing.JCheckBox();
        jCheckBoxDead.addActionListener(changeSupport);
        jCheckBoxDead.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jCheckBoxDead.setBackground(new Color(255, 255, 255));
        add(jCheckBoxDead, java.awt.BorderLayout.CENTER);
        //jCheckBoxDead.setBackground(new Color(200, 255, 255));
        defaultFocus = jCheckBoxDead;
    }

    @Override
    protected void setFieldImpl() {
        final FieldDead dead = (FieldDead) getField();
        jCheckBoxDead.setSelected(dead.getState());

         // je configure le raccourci de la touche ESCAPE pour annuler la saisie en cours
        resetKeyboardActions();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, this);
        getActionMap().put(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // restaure la valeur
                jCheckBoxDead.setSelected(dead.getState());
            }
        });
    }

    @Override
    protected void replaceValueImpl(Field field) {
        final FieldDead dead = (FieldDead) field;
        jCheckBoxDead.setSelected(dead.getState());
    }

    @Override
    protected void commitImpl()  {
        FieldDead dead = (FieldDead)getField();
        dead.setState(jCheckBoxDead.isSelected());
    }

}
