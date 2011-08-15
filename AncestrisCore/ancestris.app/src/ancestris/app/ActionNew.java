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

    private Resources RES = Resources.get(ActionNew.class);

    /** constructor */
    public ActionNew() {
//      setAccelerator(ACC_NEW);
        setText(RES, "CTL_ActionNew");
        setTip(RES, "HINT_ActionNew");
        setImage(Images.imgNew);
    }

    /** execute callback */
    public void actionPerformed(ActionEvent event) {
        Workbench.getInstance().newGedcom();
    }
} //ActionNew

