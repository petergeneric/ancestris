/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.view.Images;
import genj.app.Workbench;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;

public final class ActionNew extends Action2 {

    /** constructor */
    public ActionNew() {
//      setAccelerator(ACC_NEW);
        setText(NbBundle.getMessage(ActionNew.class,"CTL_ActionNew"));
        setTip(NbBundle.getMessage(ActionNew.class,"HINT_ActionNew"));
        setImage(Images.imgNew);
    }

    /** execute callback */
    @Override
    public void actionPerformed(ActionEvent event) {
        Workbench.getInstance().newGedcom();
    }
} //ActionNew

