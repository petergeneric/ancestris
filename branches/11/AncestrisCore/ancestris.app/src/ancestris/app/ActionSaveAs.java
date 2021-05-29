/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import genj.gedcom.Gedcom;
import ancestris.core.actions.AbstractAncestrisContextAction;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;


  /**
   * Action - Save
   */
  public class ActionSaveAs extends AbstractAncestrisContextAction {
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
        GedcomDirectory.getDefault().saveAsGedcom(getContext(), null);
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
    }

  } // ActionSave
