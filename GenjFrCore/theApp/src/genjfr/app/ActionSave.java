/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import ancestris.view.Images;
import genj.app.Workbench;
import genj.gedcom.Context;
import genj.util.Resources;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;

/**
 * Action - Save
 */
public class ActionSave extends Action2 {

    /** gedcom */
    private Context contextBeingSaved = null;
    private Resources RES = Resources.get("genj.app");

    public ActionSave() {
        setText(RES.getString("cc.menu.save"));
        setTip(RES, "cc.tip.save_file");
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

