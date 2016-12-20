/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import ancestris.view.Images;
import genj.gedcom.Context;
import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.view.AncestrisTopComponent;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;

/**
 *
 * @author daniel
 */
/**
 * Action - Close
 */
public class ActionClose extends AbstractAncestrisContextAction {

    private Context contextBeingClosed = null;

    /**
     * constructor
     */
    public ActionClose() {
        //FIXME: we must set tooltip accordingly to gedcom object selected 
        // (gobal Lookup listener?)
        // FIXME: we must enable and disable this action depending on curent selection
        setText(NbBundle.getMessage(ActionClose.class, "CTL_ActionClose"));
        setImage(Images.imgClose);
        setEnabled(enabled);
    }

    public ActionClose(Context context) {
        this();
        contextBeingClosed = context;
    }

    /**
     * run
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        // Make sure a gedcom top component is active in order to ensure that a contextChanged message will be send to all windows
        // (if focus is on the Welcome page, the contextChange message is not sent and menus remain enabled)
        for (AncestrisTopComponent aTC : AncestrisPlugin.lookupAll(AncestrisTopComponent.class)) {
            if (aTC.isOpen && aTC.isShowing()) {
                aTC.requestActive();
                break;
            }
        }
        
        if (contextBeingClosed != null) {
            GedcomDirectory.getDefault().closeGedcom(contextBeingClosed);
        } else {
            Context context = getContext();
            if (context != null) {
                GedcomDirectory.getDefault().closeGedcom(context);
                resultChanged(null);
            }
        }
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
    }

} //ActionClose

