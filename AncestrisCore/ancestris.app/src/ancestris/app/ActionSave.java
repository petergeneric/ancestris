/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import ancestris.view.Images;
import genj.gedcom.Context;
import ancestris.core.actions.AbstractAncestrisAction;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Action - Save
 */
// FIXME: must be remove in flavour of NB Save capabilities (SaveCoockie?)
public class ActionSave extends AbstractAncestrisAction {

    /** gedcom */
    private Context contextBeingSaved = null;

    public ActionSave() {
        setText(NbBundle.getMessage(ActionSave.class,"CTL_ActionSave"));
        setTip(NbBundle.getMessage(ActionSave.class,"HINT_ActionSave"));
        // setup
        setImage(Images.imgSave);
    }

    public ActionSave(Context context) {
        this();
        contextBeingSaved = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (contextBeingSaved != null){
            GedcomDirectory.getDefault().saveGedcom(contextBeingSaved);
        } else {
            Context context = Utilities.actionsGlobalContext().lookup(Context.class);
            if (context != null)  {
                GedcomDirectory.getDefault().saveGedcom(context);
            }
        }
    }
} // ActionSave

