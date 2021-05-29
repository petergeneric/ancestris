/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.ancestris.trancestris.resources.ZipArchive;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class SearchAction implements ActionListener {

    Dialog createDialog = null;

    @Override
    public void actionPerformed(ActionEvent e) {

        TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
        ZipArchive zipArchive = ((ZipExplorerTopComponent) tc).getBundles();
        if (zipArchive != null) {
//            if (createDialog == null) {
                SearchPanel zipExplorerSearchPanel = new SearchPanel(zipArchive);
                DialogDescriptor dd = new DialogDescriptor(zipExplorerSearchPanel, NbBundle.getMessage(this.getClass(), "SearchPanel.title.text"));
                dd.setModal(false);
                dd.setOptions(new Object[]{DialogDescriptor.CLOSED_OPTION});
                createDialog = DialogDisplayer.getDefault().createDialog(dd);
//            }
            createDialog.setVisible(true);
        }
    }
}
