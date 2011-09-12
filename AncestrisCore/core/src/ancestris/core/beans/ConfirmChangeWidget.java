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

import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author daniel
 */
public class ConfirmChangeWidget extends JPanel implements ChangeListener {

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
        ok.setEnabled(changeStatus);
        cancel.setEnabled(changeStatus);
        if (changeStatus)
            setVisible(true);
        else if(hideIfUnchanged)
            setVisible(false);
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
            if (callback != null)
                callback.okCallBack(event);
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

            if (callback != null)
                callback.cancelCallBack(event);
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
