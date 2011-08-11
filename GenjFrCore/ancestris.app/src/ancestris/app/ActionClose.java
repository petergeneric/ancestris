/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.view.Images;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Resources;
import genj.util.swing.Action2;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomDirectory;
import genj.app.Workbench;
import java.awt.event.ActionEvent;

/**
 *
 * @author daniel
 */
/**
 * Action - Close
 */
public class ActionClose extends Action2 {

    private Resources RES = Resources.get(ActionClose.class);
    private Context contextBeingClosed = null;

    /** constructor */
    public ActionClose() {
        setText(RES, "CTL_ActionClose");
        setImage(Images.imgClose);
        setEnabled(enabled);
    }
    public ActionClose(Context context) {
        this();
        contextBeingClosed = context;
    }

    /** run */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (contextBeingClosed != null){
            Workbench.getInstance().closeGedcom(contextBeingClosed);
        } else {
            Context context = App.center.getSelectedContext(true);
            if (context != null)
                Workbench.getInstance().closeGedcom(context);
        }
    }
} //ActionClose

