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
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class DownloadBundleAction implements ActionListener {

    private static final Logger logger = Logger.getLogger(DownloadBundleAction.class.getName());

    class zipExplorerDownloadActionPanelActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }
    DownloadBundlePanel zipExplorerDownloadActionPanel = new DownloadBundlePanel();

    @Override
    public void actionPerformed(ActionEvent e) {

        DialogDescriptor zipExplorerDownloadActionDescriptor = new DialogDescriptor(
                zipExplorerDownloadActionPanel,
                NbBundle.getMessage(OpenZipBundlePanel.class, "CTL_DownloadBundleAction"),
                true,
                new zipExplorerDownloadActionPanelActionListener());
        Dialog dialog = DialogDisplayer.getDefault().createDialog(zipExplorerDownloadActionDescriptor);

        dialog.setVisible(true);
        dialog.toFront();
        if (zipExplorerDownloadActionDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            try {
                download(new URL(zipExplorerDownloadActionPanel.getBundleUrl()), zipExplorerDownloadActionPanel.getLocalBundleFile());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    void download(URL url, File bundleFile) {
        try {
            logger.log(Level.INFO, "Opening connection to {0} ...", url.getFile());

            URLConnection urlC = url.openConnection();

            // Copy resource to local file, use remote file
            // if no local file name specified
            InputStream is = url.openStream();
            // log info about resource
            Date date = new Date(urlC.getLastModified());
            logger.log(Level.INFO, "Copying resource (type: {0}, modified on: {1})", new Object[]{urlC.getContentType(), DateFormat.getInstance().format(date)});

            FileOutputStream fos = null;
            fos = new FileOutputStream(bundleFile);

            int oneChar, count = 0;
            while ((oneChar = is.read()) != -1) {
                fos.write(oneChar);
                count++;
            }
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
