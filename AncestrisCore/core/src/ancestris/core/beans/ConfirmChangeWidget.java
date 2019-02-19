/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.core.beans;

import ancestris.core.AncestrisCorePlugin;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.util.swing.DialogManager;
import genj.util.Registry;
import genj.util.swing.ButtonHelper;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;

/**
 *
 * @author daniel
 */
public class ConfirmChangeWidget extends JPanel implements ChangeListener {

    private final static ResourceBundle BUNDLE = NbBundle.getBundle(AncestrisCorePlugin.class);
    private boolean changed = false;
    private ConfirmChangeCallBack callback;
    private OK ok = new OK();
    private Cancel cancel = new Cancel();
    public boolean hideIfUnchanged = true;

    private KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    private KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

    public ConfirmChangeWidget(ConfirmChangeCallBack callback) {
        super(new FlowLayout(FlowLayout.RIGHT));
        ButtonHelper bh = new ButtonHelper().setInsets(0).setContainer(this);
        bh.create(ok).setFocusable(false);
        bh.create(cancel).setFocusable(false);
        this.callback = callback;
        setBorder(BorderFactory.createEtchedBorder());
        
        registerKeyboardAction(ok, enterStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(cancel, escStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /*
     * Preferences settings
     */
    /** option - whether changes are auto commit */
    private static final String AUTO_COMMIT = "auto.commit";         // NOI18N

    public static void setAutoCommit(boolean autoCommit) {
        Registry.get(AncestrisCorePlugin.class).put(AUTO_COMMIT, autoCommit);
    }

    public static boolean getAutoCommit() {
        // Default Autocommit to false
        return Registry.get(AncestrisCorePlugin.class).get(AUTO_COMMIT, false);
    }

    /**
     * Ask the user whether he wants to commit changes
     */
    public boolean isCommitChanges() {
        // check for auto commit
        if (getAutoCommit()) {
            return true;
        }

        JCheckBox auto = new JCheckBox(BUNDLE.getString("confirm.autocomit"));
        auto.setFocusable(false);

        if (DialogManager.create(
                BUNDLE.getString("confirm.keep.title"),
                new JComponent[]{
                    new JLabel(BUNDLE.getString("confirm.keep.changes")),
                    new JLabel(" "),
                    auto
                })
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(DialogManager.YES_NO_OPTION)
                .show() != DialogManager.YES_OPTION){
            return false;
        }

        setAutoCommit(auto.isSelected());
        return true;

    }

    /**
     * Change event handler
     * @param e
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (callback != null && e.getSource() instanceof Boolean) {
            Boolean b = (Boolean) e.getSource();
            if (b) {
                callback.okCallBack(null);
            } else {
                callback.cancelCallBack(null);
            }
            return;
        }
        setChanged(true);
        revalidate();
    }

    /**
     * Set change status. If changeStatus is false the panel whill be hidden in
     * hideIfUnchanged is set to true
     * @param changeStatus
     */
    public void setChanged(boolean changeStatus) {
        changed = changeStatus;
        // don't show widget if autocommit is true
        if (getAutoCommit()) {
            ok.setEnabled(false);
            cancel.setEnabled(false);
            setVisible(false);
            return;
        }
        ok.setEnabled(changeStatus);
        cancel.setEnabled(changeStatus);
        if (changeStatus) {
            setVisible(true);
        } else if (hideIfUnchanged) {
            setVisible(false);
        }
    }

    /**
     * return the change status
     * @return
     */
    public boolean hasChanged() {
        return changed;
    }

    /**
     * A ok action
     */
    private class OK extends AbstractAncestrisAction {

        /** constructor */
        private OK() {
            setText(AbstractAncestrisAction.TXT_OK);
            setEnabled(false);
        }

        /** cancel current proxy */
        @Override
        public void actionPerformed(ActionEvent event) {
            if (callback != null) {
                callback.okCallBack(event);
            }
        }
    } //OK

    /**
     * A cancel action
     */
    private class Cancel extends AbstractAncestrisAction {

        /** constructor */
        private Cancel() {
            setText(AbstractAncestrisAction.TXT_CANCEL);
            setEnabled(false);
        }

        /** cancel current proxy */
        @Override
        public void actionPerformed(ActionEvent event) {
            // disable ok&cancel
            setChanged(false);

            if (callback != null) {
                callback.cancelCallBack(event);
            }
        }
    } //Cancel

    /**
     * CallBack interface to handle ok and cancel actions of the two
     * buttons.
     */
    public interface ConfirmChangeCallBack {

        public void okCallBack(ActionEvent event);

        public void cancelCallBack(ActionEvent event);
        
        public void commit(boolean ask);
    }
}
