/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class DownloadBundleAction implements ActionListener {

    class DownloadBundlePanelListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    private class DownloadBundleWorker implements Runnable {

        @Override
        public void run() {
            try {
                logger.log(Level.INFO, "Opening connection to {0} ...", url.getFile());
                ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DownloadBundleAction.class, "DownloadBundleAction.DownloadProgress"));
                URLConnection urlC = url.openConnection();

                // Copy resource to local file, use remote file
                // if no local file name specified
                InputStream is = url.openStream();

                // log info about resource
                Date date = new Date(urlC.getLastModified());
                logger.log(Level.INFO, "Copying resource (type: {0}, modified on: {1})", new Object[]{urlC.getContentType(), DateFormat.getInstance().format(date)});
                NbPreferences.forModule(OpenZipBundlePanel.class).put("Url.LastModified", DateFormat.getInstance().format(date));

                FileOutputStream fos = null;
                fos = new FileOutputStream(bundleFile);

                progressHandle.start();
                int oneChar, count = 0;
                while ((oneChar = is.read()) != -1) {
                    fos.write(oneChar);
                    count++;
                }
                progressHandle.finish();

                is.close();
                fos.close();
                logger.log(Level.INFO, " {0} byte(s) copied", count);
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(DownloadBundleAction.class, "DownloadBundleAction.completed"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            } catch (MalformedURLException e) {
                logger.log(Level.SEVERE, e.toString());
                System.err.println();
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.toString());
            }
        }
    }
    private static final Logger logger = Logger.getLogger(DownloadBundleAction.class.getName());
    DownloadBundlePanel zipExplorerDownloadActionPanel = new DownloadBundlePanel();
    private URL url;
    private File bundleFile;

    @Override
    public void actionPerformed(ActionEvent e) {

        DialogDescriptor downloadActionDescriptor = new DialogDescriptor(
                zipExplorerDownloadActionPanel,
                NbBundle.getMessage(OpenZipBundlePanel.class, "CTL_DownloadBundleAction"),
                true,
                new DownloadBundlePanelListener());
        Dialog dialog = DialogDisplayer.getDefault().createDialog(downloadActionDescriptor);

        dialog.setVisible(true);
        dialog.toFront();
        if (downloadActionDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            try {
                url = new URL(zipExplorerDownloadActionPanel.getBundleUrl());
                bundleFile = zipExplorerDownloadActionPanel.getLocalBundleFile();
                NbPreferences.forModule(OpenZipBundlePanel.class).put("Dossier", bundleFile.getParent());
                NbPreferences.forModule(OpenZipBundlePanel.class).put("Fichier", bundleFile.getName());
                NbPreferences.forModule(OpenZipBundlePanel.class).put("Url.address", url.toString());

                Thread t = new Thread(new DownloadBundleWorker());
                t.start();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
