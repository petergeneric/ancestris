/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.app;

import ancestris.view.Images;
import genj.io.FileAssociation;
import ancestris.core.actions.AbstractAncestrisAction;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author daniel
 */
@ActionID(id = "ancestris.app.ActionOnlineHelp", category = "Help")
@ActionRegistration(iconBase = "ancestris/view/images/Help.png", displayName = "#CTL_ActionHelp", iconInMenu = true)
@ActionReference(path = "Menu/Help", position = 100)
  public final class ActionOnlineHelp extends AbstractAncestrisAction {
    /** constructor */
    public ActionOnlineHelp() {
      setText(NbBundle.getMessage(this.getClass(), "CTL_ActionOnlineHelp"));
      setImage(Images.imgOnlineHelp);
    }
    /** run */
    @Override
  public void actionPerformed(ActionEvent event) {
        try {
            FileAssociation.getDefault().execute(new URL(NbBundle.getMessage(this.getClass(), "CTL_ActionOnlineHelp_url")));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
      // done
    }
  } //ActionOnlineHelp
