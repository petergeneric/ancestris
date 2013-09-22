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
    /**
     *
     * @param title
     * param messageType
     * param content
     * param options
     * param dialogId Unique key to store dialog dimension. If null
     * no dimension persistence is done
     *
     * @return
     */
    public Object show(String title, int messageType, JComponent content, Object[] options, String dialogId) {
        DialogDescriptor d = new DialogDescriptor(content, title);
        d.setMessageType(messageType);
        d.setOptions(options);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(d);
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

        return d.getValue();
    }

    public String show(String title, int messageType, String txt, String value) {
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(txt, title, NotifyDescriptor.OK_CANCEL_OPTION, messageType);
        d.setInputText(value);
        // analyze
        if (NotifyDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
            return d.getInputText();
        }
        return null;
    }
}
