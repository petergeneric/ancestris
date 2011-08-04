/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.view.Images;
import genj.app.Workbench;
import genj.util.Resources;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;

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
        Workbench.getInstance().newGedcom();
    }
} //ActionNew

