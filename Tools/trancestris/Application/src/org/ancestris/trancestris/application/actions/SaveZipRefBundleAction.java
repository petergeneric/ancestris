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

public final class SaveZipRefBundleAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

        TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
        ZipArchive zipArchive = ((ZipExplorerTopComponent) tc).getBundles();
        if (zipArchive != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(SendTranslationAction.class, "SaveZipRefBundleAction.msg.askconfirmation"), 
                                                                    NbBundle.getMessage(SendTranslationAction.class, "SaveZipRefBundleAction.msg.askconfirmationtitle"),
                                                                    NotifyDescriptor.WARNING_MESSAGE,
                                                                    NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
                zipArchive.writeRef();
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationAction.class, "SaveZipRefBundleAction.msg.ok"), 
                        NotifyDescriptor.INFORMATION_MESSAGE));
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationAction.class, "SaveZipRefBundleAction.msg.cancelled"), 
                        NotifyDescriptor.INFORMATION_MESSAGE));
            }
            
        }

    }
}
