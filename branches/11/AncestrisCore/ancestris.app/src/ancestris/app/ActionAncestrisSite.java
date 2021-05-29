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
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
  public final class ActionAncestrisSite extends AbstractAncestrisAction {
    /** constructor */
    public ActionAncestrisSite() {
      setText(NbBundle.getMessage(this.getClass(), "CTL_ActionAncestrisSite"));
      setImage(Images.imgAncestris);
    }
    /** run */
    @Override
  public void actionPerformed(ActionEvent event) {
      try {
        FileAssociation.open(new URL(NbBundle.getMessage(this.getClass(), "CTL_ActionAncestrisSite_url")), null);
      } catch (MalformedURLException e) {
        e.printStackTrace();
        }
      // done
    }
  } //ActionOnlineHelp
