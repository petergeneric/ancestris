/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.editors.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

public final class EditorSearchAction implements ActionListener {

    Dialog createDialog = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (createDialog == null) {
            EditorSearchPanel zipExplorerSearchPanel = new EditorSearchPanel();
            DialogDescriptor dd = new DialogDescriptor(zipExplorerSearchPanel, NbBundle.getMessage(this.getClass(), "EditorSearchPanel.title.text"));
            dd.setModal(false);
            dd.setOptions(new Object[]{DialogDescriptor.CLOSED_OPTION});
            createDialog = DialogDisplayer.getDefault().createDialog(dd);
        }
        createDialog.setVisible(true);

    }
}
