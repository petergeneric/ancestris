/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.app.AboutWidget;
import genj.app.Images;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.window.WindowManager;

public final class ActionAbout extends Action2 {
      private Resources resources = Resources.get(genj.app.ControlCenter.class);
      private WindowManager windowManager = App.center.getWindowManager();
    /** constructor */
    public  ActionAbout() {
      setText(resources, "cc.menu.about");
      setImage(Images.imgAbout);
    }
    /** run */
    protected void execute() {
      if (windowManager.show("about"))
        return;
      windowManager.openDialog("about",resources.getString("cc.menu.about"),WindowManager.INFORMATION_MESSAGE,new AboutWidget(App.center.getViewManager()),Action2.okOnly(),App.center);
      // done
    }
  } //ActionAbout
