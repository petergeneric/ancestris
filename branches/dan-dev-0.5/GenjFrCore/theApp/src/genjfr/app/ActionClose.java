/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.app.Images;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Resources;
import genj.util.swing.Action2;
import genjfr.app.pluginservice.GenjFrPlugin;
import genjfr.util.GedcomDirectory;
import java.awt.event.ActionEvent;

/**
 *
 * @author daniel
 */
/**
 * Action - Close
 */
public class ActionClose extends Action2 {

    private Resources RES = Resources.get("genj.app");
    private Context contextBeingClosed = null;

    /** constructor */
    public ActionClose() {
        setText(RES, "cc.menu.close");
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
            App.workbenchHelper.closeGedcom(contextBeingClosed);
        } else {
            Context context = App.center.getSelectedContext(true);
            if (context != null)
                App.workbenchHelper.closeGedcom(context);
        }
    }
} //ActionClose

