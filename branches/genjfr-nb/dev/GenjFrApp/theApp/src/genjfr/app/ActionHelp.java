/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.app.HelpWidget;
import genj.app.Images;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.window.WindowManager;

public final class ActionHelp extends Action2 {
      private Resources resources = Resources.get(genj.app.ControlCenter.class);
      private WindowManager windowManager = App.center.getWindowManager();
    /** constructor */
    public ActionHelp() {
      setText(resources, "cc.menu.contents");
      setImage(Images.imgHelp);
    }
    /** run */
    protected void execute() {
      if (windowManager.show("help"))
        return;
      windowManager.openWindow("help",resources.getString("cc.menu.help"),Images.imgHelp,new HelpWidget(),null,null);
      // done
    }
  } //ActionHelp
