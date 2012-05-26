/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.view.Images;
import genj.app.Workbench;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.net.URLDecoder;
import org.openide.util.NbBundle;

public class ActionOpen extends Action2 {

    private URL url = null;;

    /** constructor - good for button or menu item */
    public ActionOpen() {
        setTip(NbBundle.getMessage(ActionOpen.class,"HINT_ActionOpen"));
        setText(NbBundle.getMessage(ActionOpen.class,"CTL_ActionOpen"));
        setImage(Images.imgOpen);
    }

    /** constructor - good for button or menu item */
    @SuppressWarnings("deprecation")
    public ActionOpen(URL url) {
        this.url = url;
        setTip(NbBundle.getMessage(ActionOpen.class,"HINT_ActionOpen_file",URLDecoder.decode(url.getFile())));
        setText(NbBundle.getMessage(ActionOpen.class,"CTL_ActionOpen_file",URLDecoder.decode(url.getFile())));
        setImage(Images.imgOpen);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (url != null) {
            Workbench.getInstance().openGedcom(url);
        } else {
            Workbench.getInstance().openGedcom();
        }
    }

} // ActionOpen

