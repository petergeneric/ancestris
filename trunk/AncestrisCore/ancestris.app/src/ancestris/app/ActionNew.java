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

    /** execute callback */
    @Override
    public void actionPerformed(ActionEvent event) {
//        GedcomDirectory.getDefault().newGedcom();
        
        // TODO_FL : 2015-04-15 : peut-être ne pas appeler d'ici...
        ModifyGedcom wiz = Lookup.getDefault().lookup(ModifyGedcom.class);
        if (wiz != null){
            wiz.update();  // FIXME: ne pas oublier de remettre create une fois les tests terminés
        }
    }
} //ActionNew

