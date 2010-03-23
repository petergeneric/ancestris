/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.tools.merge;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class MenuAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        // TODO implement action body

        MergeTopComponent tc = new MergeTopComponent();
        tc.open();
        tc.requestActive();

    }
}
