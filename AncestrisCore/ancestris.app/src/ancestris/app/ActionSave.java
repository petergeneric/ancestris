/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.view.Images;
import genj.app.Workbench;
import genj.gedcom.Context;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;

/**
 * Action - Save
 */
// FIXME: must be remove in flavour of NB Save capabilities (SaveCoockie?)
public class ActionSave extends Action2 {

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
            Workbench.getInstance().saveGedcom(contextBeingSaved);
        } else {
            Context context = App.center.getSelectedContext(true);
            if (context != null)  {
                Workbench.getInstance().saveGedcom(context);
            }
        }
    }
} // ActionSave

