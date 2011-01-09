/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.app.Images;
import genj.util.Resources;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;
import java.net.URL;

public class ActionOpen extends Action2 {

    private Resources RES = Resources.get("genj.app");
    private URL url = null;;

    /** constructor - good for button or menu item */
    public ActionOpen() {
        setTip(RES, "cc.tip.open_file");
        setText(RES, "cc.menu.open");
        setImage(Images.imgOpen);
    }

    /** constructor - good for button or menu item */
    public ActionOpen(URL url) {
        this.url = url;
        setTip(RES.getString("cc.tip.open.file",url.getFile()));
        setText(RES.getString("cc.menu.open.file",url.getFile()));
        setImage(Images.imgOpen);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (url != null) {
            App.workbenchHelper.openGedcom(url);
        } else {
            App.workbenchHelper.openGedcom();
        }
    }

} // ActionOpen

