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

    /** constructor */
    public ActionClose() {
        setText(RES, "cc.menu.close");
        setImage(Images.imgClose);
        setEnabled(enabled);
    }

    /** run */
    @Override
    public void actionPerformed(ActionEvent event) {
        Context context = App.center.getSelectedContext();
        if (context != null)
            App.workbenchHelper.closeGedcom(context);
    }
} //ActionClose

