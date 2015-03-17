/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import ancestris.view.Images;
import genj.gedcom.Context;
import ancestris.core.actions.AbstractAncestrisAction;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author daniel
 */
/**
 * Action - Close
 */
public class ActionClose extends AbstractAncestrisAction {

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
        if (contextBeingClosed != null) {
            GedcomDirectory.getDefault().closeGedcom(contextBeingClosed);
        } else {
            Context context = Utilities.actionsGlobalContext().lookup(Context.class);
            if (context != null) {
                GedcomDirectory.getDefault().closeGedcom(context);
            }
        }
    }
} //ActionClose

