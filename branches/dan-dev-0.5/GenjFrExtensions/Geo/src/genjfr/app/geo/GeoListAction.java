/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app.geo;

import genjfr.app.App;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author frederic
 */
public final class GeoListAction implements ActionListener {
    public void actionPerformed(ActionEvent e) {
        GeoListTopComponent tc = new GeoListTopComponent();
        tc.init(App.center.getSelectedGedcom(true));
        tc.open();
        tc.requestActive();
    }
}
