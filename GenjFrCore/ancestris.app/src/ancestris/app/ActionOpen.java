/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.view.Images;
import genj.app.Workbench;
import genj.util.Resources;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;
import java.net.URL;

public class ActionOpen extends Action2 {

    private Resources RES = Resources.get(ActionOpen.class);
    private URL url = null;;

    /** constructor - good for button or menu item */
    public ActionOpen() {
        setTip(RES, "HINT_ActionOpen");
        setText(RES, "CTL_ActionOpen");
        setImage(Images.imgOpen);
    }

    /** constructor - good for button or menu item */
    public ActionOpen(URL url) {
        this.url = url;
        setTip(RES.getString("HINT_ActionOpen_file",url.getFile()));
        setText(RES.getString("CTL_ActionOpen_file",url.getFile()));
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

