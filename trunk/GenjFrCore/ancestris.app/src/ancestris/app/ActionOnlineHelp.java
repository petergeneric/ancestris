/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.app;

import ancestris.view.Images;
import genj.io.FileAssociation;
import genj.util.Resources;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.util.NbBundle;

/**
 *
 * @author daniel
 */
  public final class ActionOnlineHelp extends Action2 {
    /** constructor */
    public ActionOnlineHelp() {
      setText(NbBundle.getMessage(this.getClass(), "CTL_ActionOnlineHelp"));
      setImage(Images.imgHelp);
    }
    /** run */
    @Override
  public void actionPerformed(ActionEvent event) {
      try {
        FileAssociation.open(new URL(NbBundle.getMessage(this.getClass(), "CTL_ActionOnlineHelp_url")), null);
      } catch (MalformedURLException e) {
        e.printStackTrace();
        }
      // done
    }
  } //ActionOnlineHelp
