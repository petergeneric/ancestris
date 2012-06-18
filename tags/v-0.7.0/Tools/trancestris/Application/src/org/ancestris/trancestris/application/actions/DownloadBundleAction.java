/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JOptionPane;
import org.ancestris.trancestris.application.utils.DownloadBundleWorker;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class DownloadBundleAction implements ActionListener {

    class DownloadBundlePanelListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }
    DownloadBundlePanel downloadBundlePanel = new DownloadBundlePanel();

    @Override
    public void actionPerformed(ActionEvent e) {

        DialogDescriptor downloadActionDescriptor = new DialogDescriptor(
                downloadBundlePanel,
                NbBundle.getMessage(DownloadBundleAction.class, "CTL_DownloadBundleAction"),
                true,
                new DownloadBundlePanelListener());
        Dialog dialog = DialogDisplayer.getDefault().createDialog(downloadActionDescriptor);

        dialog.setVisible(true);
        dialog.toFront();
        if (downloadActionDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            try {
                URL url = new URL(downloadBundlePanel.getBundleUrl());
                File bundleFile = downloadBundlePanel.getLocalBundleFile();
                NbPreferences.forModule(DownloadBundleAction.class).put("Dossier", bundleFile.getParent());
                NbPreferences.forModule(DownloadBundleAction.class).put("Fichier", bundleFile.getName());
                NbPreferences.forModule(DownloadBundleAction.class).put("Url.address", url.toString());
                if (bundleFile.exists()) {
                    int result = JOptionPane.showConfirmDialog(null, NbBundle.getMessage(DownloadBundlePanel.class, "DownloadBundlePanel.Overwrite.Text"), NbBundle.getMessage(DownloadBundlePanel.class, "DownloadBundlePanel.Overwrite.Title"), JOptionPane.YES_NO_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            Thread t = new Thread(new DownloadBundleWorker(url, bundleFile));
                            t.start();
                            return;
                        case JOptionPane.NO_OPTION:
                        case JOptionPane.CANCEL_OPTION:
                            return;
                    }
                } else {
                    Thread t = new Thread(new DownloadBundleWorker(url, bundleFile));
                    t.start();
                }

            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
