/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genj.app.Images;
import genj.io.FileAssociation;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.window.WindowManager;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author daniel
 */
  public final class ActionOnlineHelp extends Action2 {
      private Resources resources = Resources.get(genj.app.ControlCenter.class);
      private WindowManager windowManager = App.center.getWindowManager();
    /** constructor */
    public ActionOnlineHelp() {
      setText(resources, "cc.menu.online");
      setImage(Images.imgHelp);
    }
    /** run */
    protected void execute() {
      try {
        FileAssociation.open(new URL(resources.getString("cc.menu.onlineurl")), null);
      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }
      // done
    }
  } //ActionOnlineHelp
