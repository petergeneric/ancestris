/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genj.app.Images;
import genj.gedcom.Gedcom;
import genj.util.Resources;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;

  /**
   * Action - Save
   */
  public class ActionSave extends Action2 {
    /** gedcom */
    protected Gedcom gedcomBeingSaved;
    private Resources RES = Resources.get("genj.app");

    public ActionSave(){
        setText(RES.getString("cc.menu.save"));
      setTip(RES, "cc.tip.save_file");
      // setup
      setImage(Images.imgSave);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        App.workbenchHelper.saveGedcom(App.center.getSelectedContext(false));
    }

  } // ActionSave
