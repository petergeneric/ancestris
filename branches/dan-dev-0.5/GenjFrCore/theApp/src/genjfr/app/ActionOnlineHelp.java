/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genj.app.Images;
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
    private Resources RES = Resources.get("genj.app");
    /** constructor */
    public ActionOnlineHelp() {
      setText(NbBundle.getMessage(this.getClass(), "CTL_ActionOnlineHelp"));
      setImage(Images.imgHelp);
    }
    /** run */
    @Override
  public void actionPerformed(ActionEvent event) {
      try {
        FileAssociation.open(new URL(RES.getString("cc.menu.onlineurl")), null);
      } catch (MalformedURLException e) {
        e.printStackTrace();
        }
      // done
    }
  } //ActionOnlineHelp
