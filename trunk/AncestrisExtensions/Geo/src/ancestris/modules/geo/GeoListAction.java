/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.modules.geo;

import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.util.Utilities;

/**
 *
 * @author frederic
 */
public final class GeoListAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
        GeoListTopComponent tc = new GeoListTopComponent();
        Context c = Utilities.actionsGlobalContext().lookup(Context.class);
        if (c==null){
            return;
        }
        tc.init(c);
        tc.open();
        tc.requestActive();
    }
}
