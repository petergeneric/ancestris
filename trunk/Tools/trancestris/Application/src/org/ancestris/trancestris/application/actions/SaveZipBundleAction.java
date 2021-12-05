package org.ancestris.trancestris.application.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.ancestris.trancestris.resources.ZipArchive;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class SaveZipBundleAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

        TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
        ZipArchive zipArchive = ((ZipExplorerTopComponent) tc).getBundles();
        if (zipArchive != null) {
            zipArchive.write();
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationAction.class, "SaveZipBundleAction.msg.ok"), 
                    NotifyDescriptor.INFORMATION_MESSAGE));
        }

    }
}
