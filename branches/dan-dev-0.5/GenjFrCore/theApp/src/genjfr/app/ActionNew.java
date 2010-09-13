/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.app.Images;
import genj.util.Resources;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;
import org.openide.util.NbPreferences;

public final class ActionNew extends Action2 {

    private Resources RES = Resources.get("genj.app");

    /** constructor */
    public ActionNew() {
//      setAccelerator(ACC_NEW);
        setText(RES, "cc.menu.new");
        setTip(RES, "cc.tip.create_file");
        setImage(Images.imgNew);
    }

    /** execute callback */
    public void actionPerformed(ActionEvent event) {
        App.workbenchHelper.newGedcom();
    }
} //ActionNew

