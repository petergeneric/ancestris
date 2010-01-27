/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genj.app.Images;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomDirectory;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.window.WindowManager;

/**
 *
 * @author daniel
 */
  /**
   * Action - Close
   */
  public class ActionClose extends Action2 {
      private Resources resources = Resources.get(genj.app.ControlCenter.class);
      private WindowManager windowManager = App.center.getWindowManager();
    /** constructor */
    public  ActionClose() {
        this(true);
    }
    protected ActionClose(boolean enabled) {
      setText(resources.getString("cc.menu.close"));
      setImage(Images.imgClose);
      setEnabled(enabled);
    }
    /** run */
    protected void execute() {

      // Current Gedcom
      final Gedcom gedcom = App.center.getSelectedGedcom();
      if (gedcom == null)
        return;

      // changes we should care about?
      if (gedcom.hasChanged()) {

        int rc = windowManager.openDialog(null,null,WindowManager.WARNING_MESSAGE,
            resources.getString("cc.savechanges?", gedcom.getName()),
            Action2.yesNoCancel(),App.center);
        // cancel everything?
        if (rc==2)
          return;
        // save now?
        if (rc==0) {
          // Remove it so the user won't change it while being saved
          GedcomDirectory.getInstance().unregisterGedcom(gedcom);
          // and save
          new ActionSave(gedcom) {
            protected void postExecute(boolean preExecuteResult) {
              // super first
              super.postExecute(preExecuteResult);
              // add back if still changed
              if (gedcomBeingSaved.hasChanged())
                GedcomDirectory.getInstance().registerGedcom(gedcomBeingSaved);
            }
          }.trigger();
          return;
        }
      }

      // Remove it
      GedcomDirectory.getInstance().unregisterGedcom(gedcom);

     for (GenjInterface gjvTc : GenjViewTopComponent.getMyLookup().lookupAll(GenjInterface.class)) {
          Gedcom ged = gjvTc.getGedcom();
          if (gedcom.equals(gjvTc.getGedcom()))
                  gjvTc.close();
      }

      // Done
    }
  } //ActionClose
