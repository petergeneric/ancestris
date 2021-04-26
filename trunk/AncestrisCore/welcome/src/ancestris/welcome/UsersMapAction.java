/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.welcome;

import genj.io.FileAssociation;
import ancestris.core.actions.AbstractAncestrisAction;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public final class UsersMapAction extends AbstractAncestrisAction {

    public UsersMapAction() {
        setText(NbBundle.getMessage(this.getClass(), "CTL_UsersMapAction"));
        setImage("ancestris/welcome/resources/ico_map_16.png");
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            FileAssociation.open(new URL(NbBundle.getMessage(this.getClass(), "CTL_UsersMapAction_url")), null);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
