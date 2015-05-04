/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.api.newgedcom.ModifyGedcom;
import ancestris.view.Images;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.gedcom.GedcomDirectory;
import java.awt.event.ActionEvent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class ActionNew extends AbstractAncestrisAction {

    /** constructor */
    public ActionNew() {
//      setAccelerator(ACC_NEW);
        setText(NbBundle.getMessage(ActionNew.class,"CTL_ActionNew"));
        setTip(NbBundle.getMessage(ActionNew.class,"HINT_ActionNew"));
        setImage(Images.imgNew);
    }

    /** execute callback
     * @param event */
    @Override
    public void actionPerformed(ActionEvent event) {
        for (ModifyGedcom wiz:Lookup.getDefault().lookupAll(ModifyGedcom.class)){
            if (wiz.isReady()) {
                wiz.create();
                return;
            }
        }
        // Fallback to newGedcom
        GedcomDirectory.getDefault().newGedcom();

    }
} //ActionNew

