/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;

public final class RunApp implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        EditTopComponent etp = new EditTopComponent();
        etp.open();
//        App.Startup s = new App.Startup();
//        s.run();
        // TODO implement action body
    }
}
