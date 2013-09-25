/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//XXX: we must redesign this class and DialogManager in a more NB integrated manner
package ancestris.util.swing;

import genj.util.Registry;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author daniel
 */
public class DialogManager {

    /** message types */
    public static final int ERROR_MESSAGE = NotifyDescriptor.ERROR_MESSAGE,
            INFORMATION_MESSAGE = NotifyDescriptor.INFORMATION_MESSAGE,
            WARNING_MESSAGE = NotifyDescriptor.WARNING_MESSAGE,
            QUESTION_MESSAGE = NotifyDescriptor.QUESTION_MESSAGE,
            PLAIN_MESSAGE = NotifyDescriptor.PLAIN_MESSAGE;
    public static final int OK_CANCEL_OPTION = DialogDescriptor.OK_CANCEL_OPTION;
    /** Return value if OK is chosen. */
    public static final Object OK_OPTION = DialogDescriptor.OK_OPTION;
    public static final Object CANCEL_OPTION = DialogDescriptor.CANCEL_OPTION;
    private static DialogManager instance = null;
    public static DialogManager getInstance() {
        if (instance == null) {
            instance = new DialogManager();
        }
        return instance;
    }

    public Object show(String title, int messageType, String txt, Object[] options) {
        NotifyDescriptor d = new NotifyDescriptor(txt, title, NotifyDescriptor.DEFAULT_OPTION, messageType, options, null);
        return DialogDisplayer.getDefault().notify(d);
    }

    // Wrapper pour convertir les dialogues gnj en dialogue NB
    //TODO: ce qui n'est pas fait: la possibilite de desactiver des boutons par le caller
    // p.ex via action[0].setEnable(...)
    //

    public String show(String title, int messageType, String txt, String value) {
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(txt, title, NotifyDescriptor.OK_CANCEL_OPTION, messageType);
        d.setInputText(value);
        // analyze
        if (NotifyDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
            return d.getInputText();
        }
        return null;
    }
    
    public static ADialog create(String title, JComponent content){
        return new ADialog(title, content);
    }
    
    public static class ADialog{
        private DialogDescriptor dd;
        private String dialogId = null;

        public ADialog(String title, JComponent content) {
            dd = new DialogDescriptor(content, title);
            dd.setMessageType(DialogDescriptor.QUESTION_MESSAGE);
            dd.setOptions(new Object[]{ DialogDescriptor.OK_OPTION});
        }

        public ADialog setOptions(Object[] newOptions) {
            dd.setOptions(newOptions);
            return this;
        }

        public ADialog setOptionType(int newType) {
            dd.setOptions(null);
            dd.setOptionType(newType);
            return this;
        }

        public ADialog setMessageType(int newType) {
            dd.setMessageType(newType);
            return this;
        }        

        public ADialog setDialogId(String id) {
            dialogId = id;
            return this;
        }

        public final ADialog setValid(boolean newValid) {
            dd.setValid(newValid);
            return this;
        }
        
    public Object show() {
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        // restore bounds
        if (dialogId != null) {
            final Registry registry = Registry.get(DialogManager.class);
            Dimension bounds = registry.get(dialogId+".dialog", (Dimension) null);
            if (bounds != null) {
                Rectangle prev = dialog.getBounds();
                prev.grow((bounds.width - prev.width) / 2, (bounds.height - prev.height) / 2);
                dialog.setBounds(prev);
            }
            dialog.setVisible(true);
            registry.put(dialogId+".dialog", dialog.getSize());
        } else {
            dialog.setVisible(true);
        }
        return dd.getValue();
    }
    }
}
