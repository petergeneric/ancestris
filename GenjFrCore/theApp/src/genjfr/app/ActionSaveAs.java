/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genj.app.Workbench;
import genj.gedcom.Gedcom;
import genj.util.Resources;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;


  /**
   * Action - Save
   */
  public class ActionSaveAs extends Action2 {
    /** gedcom */
    protected Gedcom gedcomBeingSaved;
    private Resources RES = Resources.get("genj.app");

    /**
     * Constructor for saving gedcom
     */
    public ActionSaveAs() {
        setText(RES.getString("cc.menu.saveas"));
     setTip(RES, "cc.tip.save_file");
      // setup
      //setImage(Images.imgSave);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Workbench.getInstance().saveAsGedcom(App.center.getSelectedContext(true));
    }

  } // ActionSave
