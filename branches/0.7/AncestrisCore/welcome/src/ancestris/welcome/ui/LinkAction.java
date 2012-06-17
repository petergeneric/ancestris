package ancestris.welcome.ui;

import ancestris.welcome.content.Utils;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;

class LinkAction extends AbstractAction {

    private DataObject dob;

    public LinkAction(DataObject dob) {
        super(dob.getNodeDelegate().getDisplayName());
        this.dob = dob;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String url = (String) dob.getPrimaryFile().getAttribute("url");
        if (null != url) {
            Utils.showURL(url);
            return;
        }
        OpenCookie oc = dob.getCookie(OpenCookie.class);
        if (null != oc) {
            oc.open();
        }
    }
}
