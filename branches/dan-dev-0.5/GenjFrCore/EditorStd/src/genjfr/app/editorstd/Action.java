/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app.editorstd;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class Action implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        EditorStdTopComponent.updateInstances();
    }
}
