package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.BeanField;
import ancestris.modules.releve.model.Field;
import genj.util.ChangeSupport;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Michel
 */
public abstract class Bean extends JPanel {
    protected BeanField beanField;

    protected JComponent defaultFocus = null;
    protected ChangeSupport changeSupport = new ChangeSupport(this);


     /** constructor */
    protected Bean() {
        setOpaque(false);
    }

    /**
     * 
     * set property to look at
     */
    public final Bean setContext(BeanField beanField) {
        this.beanField = beanField;

        setFieldImpl();
        changeSupport.setChanged(false);

        // je configure le raccourci de la touche ESCAPE pour annuler la saisie en cours
        resetKeyboardActions();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, this);
        getActionMap().put(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // restaure la valeur
                setFieldImpl();
            }
        });

        return this;
    }

    protected abstract void setFieldImpl();

    protected void replaceValue(Field field) {
        String oldValue = beanField.getField().toString();
        if ( field!= null && !field.toString().equals(oldValue)) {
            replaceValueImpl(field);
            changeSupport.setChanged(true);
        }
    }

    protected abstract void replaceValueImpl(Field field);

    /**
     * Commit any changes made by the user
     */
    public final void commit() {
        if (!hasChanged()) {
            return;
        }

        commitImpl();
        // clear changed
        changeSupport.setChanged(false);
    }

    protected abstract void commitImpl() ;

    /**
     * Current Property
     */
    public final Field getField() {
        return beanField.getField();
    }

    /**
     * Current Property
     */
    public final BeanField getBeanField() {
        return beanField;
    }

    /**
     * Whether the bean has changed since first listener was attached
     */
    public boolean hasChanged() {
        return changeSupport.hasChanged();
    }

    /**
     * Listener
     */
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    /**
     * Listener
     */
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    /**
     * overridden requestFocusInWindow()
     */
    @Override
    public boolean requestFocusInWindow() {
        // delegate to default focus
        if (defaultFocus != null) {
            return defaultFocus.requestFocusInWindow();
        }
        return false;
    }



}
