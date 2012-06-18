/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.app;

import genj.app.Workbench;
import genj.gedcom.Gedcom;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;


  /**
   * Action - Save
   */
  public class ActionSaveAs extends Action2 {
    /** gedcom */
    protected Gedcom gedcomBeingSaved;

    /**
     * Constructor for saving gedcom
     */
    public ActionSaveAs() {
        setText(NbBundle.getMessage(ActionSaveAs.class,"CTL_ActionSaveAs"));
     setTip(NbBundle.getMessage(ActionSaveAs.class,"HINT_ActionSave"));
      // setup
      //setImage(Images.imgSave);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Workbench.getInstance().saveAsGedcom(App.center.getSelectedContext(true));
    }

  } // ActionSave
