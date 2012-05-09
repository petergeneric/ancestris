/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.explorers.zipexplorer.actions;

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
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class ZipExplorerDownloadAction implements ActionListener {

    private static final Logger logger = Logger.getLogger(ZipExplorerDownloadAction.class.getName());

    class zipExplorerDownloadActionPanelActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }
    ZipExplorerDownloadActionPanel zipExplorerDownloadActionPanel = new ZipExplorerDownloadActionPanel();

    @Override
    public void actionPerformed(ActionEvent e) {

        DialogDescriptor zipExplorerDownloadActionDescriptor = new DialogDescriptor(
                zipExplorerDownloadActionPanel,
                NbBundle.getMessage(ZipExplorerOpenActionPanel.class, "CTL_ZipExplorerDownloadAction"),
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
            String localFile = null;
            // Get only file name
            StringTokenizer st = new StringTokenizer(url.getFile(), "/");
            while (st.hasMoreTokens()) {
                localFile = st.nextToken();
            }
            fos = new FileOutputStream(localFile);

            int oneChar, count = 0;
            while ((oneChar = is.read()) != -1) {
                fos.write(oneChar);
                count++;
            }
            is.close();
            fos.close();
            logger.log(Level.INFO, " {0} byte(s) copied", count);
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, e.toString());
            System.err.println();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }
}
