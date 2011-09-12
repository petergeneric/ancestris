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
import genj.util.Registry;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.DialogHelper;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

    public ConfirmChangeWidget(ConfirmChangeCallBack callback) {
        super(new FlowLayout(FlowLayout.RIGHT));
        ButtonHelper bh = new ButtonHelper().setInsets(0).setContainer(this);
        bh.create(ok).setFocusable(false);
        bh.create(cancel).setFocusable(false);
        this.callback = callback;
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

        int rc = DialogHelper.openDialog(BUNDLE.getString("confirm.keep.changes"),
                DialogHelper.QUESTION_MESSAGE, new JComponent[]{
                    new JLabel(BUNDLE.getString("confirm.keep.changes")),
                    auto
                },
                Action2.yesNo(),
                this);

        if (rc != 0) {
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
    private class OK extends Action2 {

        /** constructor */
        private OK() {
            setText(Action2.TXT_OK);
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
    private class Cancel extends Action2 {

        /** constructor */
        private Cancel() {
            setText(Action2.TXT_CANCEL);
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
    }
}
