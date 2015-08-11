/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2012-2013 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
//XXX: we must redesign this class and DialogManager in a more NB integrated manner
package ancestris.util.swing;

import genj.util.Registry;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author daniel
 */
//FIXME:: write doc and default values for options ans message type
public abstract class DialogManager {

    /** message types */
    public static final int ERROR_MESSAGE = NotifyDescriptor.ERROR_MESSAGE,
            INFORMATION_MESSAGE = NotifyDescriptor.INFORMATION_MESSAGE,
            WARNING_MESSAGE = NotifyDescriptor.WARNING_MESSAGE,
            QUESTION_MESSAGE = NotifyDescriptor.QUESTION_MESSAGE,
            PLAIN_MESSAGE = NotifyDescriptor.PLAIN_MESSAGE;
    public static final int OK_CANCEL_OPTION = DialogDescriptor.OK_CANCEL_OPTION,
            YES_NO_OPTION = NotifyDescriptor.YES_NO_OPTION,
            YES_NO_CANCEL_OPTION = NotifyDescriptor.YES_NO_CANCEL_OPTION,
            OK_ONLY_OPTION = 10;
    ;
    
    /** Return value if OK is chosen. */
    public static final Object OK_OPTION = DialogDescriptor.OK_OPTION;
    public static final Object CANCEL_OPTION = DialogDescriptor.CANCEL_OPTION;
    public static final Object YES_OPTION = DialogDescriptor.YES_OPTION;
    public static final Object CLOSED_OPTION = DialogDescriptor.CLOSED_OPTION;

    public static Object show(String title, int messageType, String txt, Object[] options) {
        NotifyDescriptor d = new NotifyDescriptor(txt, title, NotifyDescriptor.DEFAULT_OPTION, messageType, options, null);
        return DialogDisplayer.getDefault().notify(d);
    }

    // Wrapper pour convertir les dialogues gnj en dialogue NB
    //TODO: ce qui n'est pas fait: la possibilite de desactiver des boutons par le caller
    // p.ex via action[0].setEnable(...)
    //
    public static ADialog create(String title, JComponent content) {
        return new ADialog(title, content);
    }

    public static ADialog create(String title, JComponent[] content) {
        // assemble content into Box (don't use Box here because
        // Box extends Container in pre JDK 1.4)
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        for (int i = 0; i < content.length; i++) {
            if (content[i] == null) {
                continue;
            }
            box.add(content[i]);
            content[i].setAlignmentX(0F);

        }
        return create(title, box);
    }

    public static InputLine create(String title, String text, String value) {
        return new InputLine(title, text, value);
    }

    /**
     * Creates a simple error dialog message with only an ok button.
     *
     * @param title
     * @param text
     *
     * @return
     */
    //XXX: rename to createOk, default type is ERROR
    public static DialogManager createError(String title, String text) {
        return new Message(title, text).setMessageType(ERROR_MESSAGE);
    }

    public static DialogManager create(String title, String text) {
        return new Message(title, text);
    }

    /**
     * Creates a simple question dialog message with only a yes and no button.
     * Message type defaults to QUESTION_MESSAGE
     *
     * @param title
param text
     *
     * @return
     */
    public static DialogManager createYesNo(String title, String text) {
        return new Message(title, text).setOptionType(NotifyDescriptor.YES_NO_OPTION).setMessageType(QUESTION_MESSAGE);
    }

    // see http://wiki.netbeans.org/DevFaqDialogControlOKButton
    /**
     * custom version of {@link NotifyDescriptor.InputLine} that enable or disable
     * ok button depending on presence of data in input buffer
     * This behaviour can be disabled using {@link setEnabler}
     */
    private static class MyInputLine extends NotifyDescriptor.InputLine {

        private boolean isAutoEnabler = true;

        public MyInputLine(String text, String title) {
            super(text, title);
            doEnablement();
        }

        @Override
        public void setInputText(String text) {
            super.setInputText(text);
            doEnablement();
        }

        public void setEnabler(boolean set) {
            isAutoEnabler = set;
            doEnablement();
        }

        @Override
        protected void initialize() {
            super.initialize();
            textField.getDocument().addDocumentListener(new DocumentListener() {

                public void insertUpdate(DocumentEvent e) {
                    doEnablement();
                }

                public void removeUpdate(DocumentEvent e) {
                    doEnablement();
                }

                public void changedUpdate(DocumentEvent e) {
                    doEnablement();
                }
            });

        }

        private void doEnablement() {
            if (isAutoEnabler) {
                if (textField.getText().isEmpty()) {
                    setValid(false);
                } else {
                    setValid(true);
                }
            } else {
                setValid(true);
            }
        }
    }

    protected abstract NotifyDescriptor getDescriptor();
    protected String dialogId = null;

    public DialogManager setOptions(Object[] newOptions) {
        getDescriptor().setOptions(newOptions);
        return this;
    }

    public DialogManager setAdditionalOptions(Object[] newOptions) {
        getDescriptor().setAdditionalOptions(newOptions);
        return this;
    }

    public DialogManager setOptionType(int newType) {
        if (newType == OK_ONLY_OPTION) {
            return setOptions(new Object[]{OK_OPTION});
        } else {
            getDescriptor().setOptions(null);
            getDescriptor().setOptionType(newType);
            return this;
        }
    }

    public DialogManager setMessageType(int newType) {
        getDescriptor().setMessageType(newType);
        return this;
    }

    public DialogManager setDialogId(Class type) {
        return setDialogId(type.getName());
    }

    public DialogManager setDialogId(String id) {
        dialogId = id;
        return this;
    }

    public final DialogManager setValid(boolean newValid) {
        getDescriptor().setValid(newValid);
        return this;
    }

    public abstract Object show();

    public static class Message extends DialogManager {

        private NotifyDescriptor descriptor;

        public Message(String title, String message) {
            super();
            descriptor = new NotifyDescriptor(
                    message,
                    title,
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.INFORMATION_MESSAGE,
                    new Object[]{NotifyDescriptor.OK_OPTION},
                    OK_OPTION);
        }

        @Override
        protected NotifyDescriptor getDescriptor() {
            return descriptor;
        }

        @Override
        public Object show() {
            return DialogDisplayer.getDefault().notify(descriptor);
        }
    }

    public static class InputLine extends DialogManager {

        private NotifyDescriptor.InputLine descriptor;

        public InputLine(String title, String text, String value) {
            super();
            descriptor = new MyInputLine(text, title);
            descriptor.setInputText(value);
            setOptionType(OK_CANCEL_OPTION);
        }

        @Override
        protected NotifyDescriptor getDescriptor() {
            return descriptor;
        }

        @Override
        public String show() {
            // analyze
            if (NotifyDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(descriptor)) {
                return descriptor.getInputText();
            }
            return null;
        }
    }

    public static class ADialog extends DialogManager {

        protected DialogDescriptor descriptor;
        private Dialog dialog;

        public ADialog(String title, JComponent content) {
            super();
            descriptor = new DialogDescriptor(content, title);
        }

        @Override
        protected NotifyDescriptor getDescriptor() {
            return descriptor;
        }

        public Object show() {
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            // restore bounds
            if (dialogId != null) {
                final Registry registry = Registry.get(DialogManager.class);
                Dimension bounds = registry.get(dialogId + ".dialog", (Dimension) null);
                if (bounds != null) {
                    Rectangle prev = dialog.getBounds();
                    prev.grow((bounds.width - prev.width) / 2, (bounds.height - prev.height) / 2);
                    dialog.setBounds(prev);
                }
                dialog.setVisible(true);
                registry.put(dialogId + ".dialog", dialog.getSize());
            } else {
                dialog.setVisible(true);
            }
            return descriptor.getValue();
        }

        public void cancel() {
            if (dialog == null) {
                throw new IllegalStateException("not showing");
            }
            dialog.dispose();
        }
    }
    //FIXME: from old DialogHelper. See if this is necessary
    /**
     * scan for JTabbedPanes and make their contained components opaque
     */
//    private static void patchOpaque(Component component, boolean set) {
//
//        if (component instanceof JTabbedPane) {
//            set = false;
//        }
//
//        if (component instanceof JComponent && !(component instanceof JTextField) && !(component instanceof JScrollPane)) {
//            if (!set) {
//                ((JComponent) component).setOpaque(set);
//            }
//        }
//
//        if (component instanceof Container && !(component instanceof JScrollPane)) {
//            for (Component c : ((Container) component).getComponents()) {
//                patchOpaque(c, set);
//            }
//        }
//
//    }
}
