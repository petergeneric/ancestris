/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.api.newgedcom.ModifyGedcom;
import ancestris.view.Images;
import ancestris.core.actions.AbstractAncestrisAction;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
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
//        GedcomDirectory.getDefault().newGedcom();

        // TODO_FL : 2015-04-15 : peut-être ne pas appeler d'ici...
        Collection list = Lookup.getDefault().lookupAll(ModifyGedcom.class);
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            ModifyGedcom wiz = (ModifyGedcom) iterator.next();
            if (wiz.isReady()) {
                wiz.update();   // ne pas oublier de mettre create() après les test, une fois le bouton modify créé
                return;
            }
        }
    }
} //ActionNew

