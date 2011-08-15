/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app.geo;

import ancestris.app.App;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class GeoMapAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
        GeoMapTopComponent tc = new GeoMapTopComponent();
        tc.init(App.center.getSelectedContext(true));
        tc.open();
        tc.requestActive();
    }
}
