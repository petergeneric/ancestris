/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


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
        GedcomDirectory.getDefault().saveAsGedcom(Utilities.actionsGlobalContext().lookup(Context.class));
    }

  } // ActionSave
