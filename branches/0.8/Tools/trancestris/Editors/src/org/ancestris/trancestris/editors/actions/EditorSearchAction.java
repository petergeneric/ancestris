/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.editors.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.util.actions.Presenter;

public final class EditorSearchAction implements ActionListener, Presenter.Toolbar {

    @Override
    public Component getToolbarPresenter() {
        return EditorSearchPanel.getInstance();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
