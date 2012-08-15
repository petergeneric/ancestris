/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.modules.geo;

import ancestris.gedcom.GedcomDirectory;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.util.Utilities;

public final class GeoMapAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
        GeoMapTopComponent tc = new GeoMapTopComponent();
        Context c = Utilities.actionsGlobalContext().lookup(Context.class);
        if (c == null){
            return;
        }
        tc.init(c);
        tc.open();
        tc.requestActive();
    }
}
