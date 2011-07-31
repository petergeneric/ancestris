/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.app.Images;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;
import org.netbeans.api.javahelp.Help;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class ActionHelp extends Action2 {


    /** constructor */
    public ActionHelp() {
        setText(NbBundle.getMessage(this.getClass(), "CTL_ActionHelp"));
        setImage(Images.imgHelp);
    }

    /** run */
    @Override
  public void actionPerformed(ActionEvent e) {
        String id = "ancestris.app.about";
        Help help = Lookup.getDefault().lookup(Help.class);
        if (help != null && help.isValidID(id, true).booleanValue()) {
            help.showHelp(new HelpCtx(id));
        } else {
//    Toolkit.getDefaultToolkit().beep();
        }
    }
} //ActionHelp

