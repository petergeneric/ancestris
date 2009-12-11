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
import org.netbeans.api.javahelp.Help;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

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
        String id = "intro";
Help help = (Help)Lookup.getDefault().lookup(Help.class);
if (help != null && help.isValidID(id, true).booleanValue()) {
    help.showHelp(new HelpCtx(id));
} else {
//    Toolkit.getDefaultToolkit().beep();
}


//      if (windowManager.show("help"))
//        return;
//      windowManager.openWindow("help",resources.getString("cc.menu.help"),Images.imgHelp,new HelpWidget(),null,null);
//      // done
    }
  } //ActionHelp
