/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class OpenZipBundleAction implements ActionListener {

    File zipFile = null;
    Locale fromLocale = null;
    Locale toLocale = null;
    OpenZipBundlePanel openZipBundlePanel = new OpenZipBundlePanel(true);

    @Override
    public void actionPerformed(ActionEvent e) {

        DialogDescriptor zipExplorerOpenActionDescriptor = new DialogDescriptor(
                openZipBundlePanel,
                NbBundle.getMessage(OpenZipBundlePanel.class, "CTL_OpenZipBundleAction"),
                true,
                null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(zipExplorerOpenActionDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        if (zipExplorerOpenActionDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            zipFile = openZipBundlePanel.zipFile;
            fromLocale = openZipBundlePanel.fromLocale;
            toLocale = openZipBundlePanel.toLocale;
            NbPreferences.forModule(OpenZipBundlePanel.class).put("Dossier", zipFile.getParent());
            NbPreferences.forModule(OpenZipBundlePanel.class).put("Fichier", zipFile.getName());
            NbPreferences.forModule(OpenZipBundlePanel.class).put("fromLocale", fromLocale.toString());
            NbPreferences.forModule(OpenZipBundlePanel.class).put("toLocale", toLocale.toString());
            TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
            if (zipFile != null && zipFile.exists()) {
                ((ZipExplorerTopComponent) tc).setBundles(zipFile, fromLocale, toLocale);
            } else {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(OpenZipBundlePanel.class, "OpenZipBundleAction.FileNotFound.text"), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
    }
}
