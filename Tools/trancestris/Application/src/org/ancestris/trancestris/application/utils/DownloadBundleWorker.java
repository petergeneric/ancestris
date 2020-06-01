/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.ancestris.trancestris.application.Installer;
import org.ancestris.trancestris.application.actions.DownloadBundleAction;
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.ancestris.trancestris.resources.ZipArchive;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author dominique
 */
public class DownloadBundleWorker implements Runnable {

    private static final Logger logger = Logger.getLogger(DownloadBundleAction.class.getName());
    private final Preferences modulePreferences = NbPreferences.forModule(Installer.class);
    private final URL url;
    private final File bundleFile;

    public DownloadBundleWorker(URL url, File bundleFile) {
        this.url = url;
        this.bundleFile = bundleFile;
    }

    @Override
    public void run() {
        try {
            logger.log(Level.INFO, "Opening connection to {0} ...", url.getFile());
            ProgressHandle progressHandle = ProgressHandle.createHandle(NbBundle.getMessage(DownloadBundleAction.class, "DownloadBundleAction.DownloadProgress"));
            URLConnection urlC = url.openConnection();

            // log info about resource
            // Copy resource to local file, use remote file
            // if no local file name specified
            try (InputStream is = url.openStream()) {
                // log info about resource
                Date date = new Date(urlC.getLastModified());
                logger.log(Level.INFO, "Copying resource (type: {0}, modified on: {1})", new Object[]{urlC.getContentType(), DateFormat.getInstance().format(date)});
                NbPreferences.forModule(DownloadBundleWorker.class).putLong("Url.LastModified", date.getTime());
                try (FileOutputStream fos = new FileOutputStream(bundleFile)) {
                    progressHandle.start();
                    int oneChar, count = 0;
                    while ((oneChar = is.read()) != -1) {
                        fos.write(oneChar);
                        count++;
                    }
                    progressHandle.finish();
                    logger.log(Level.INFO, " {0} byte(s) copied", count);
                }
            }
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(DownloadBundleAction.class, "DownloadBundleAction.completed"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
                    ZipArchive bundles = ((ZipExplorerTopComponent) tc).getBundles();
                    if (bundles != null) {
                        ((ZipExplorerTopComponent) tc).setBundles(bundleFile, bundles.getFromLocale(), bundles.getToLocale());
                    } else {
                        if (modulePreferences.get("fromLocale", "").isEmpty() == false) {
                            Locale fromLocale = getLocaleFromString(modulePreferences.get("fromLocale", Locale.ENGLISH.toString()));
                            Locale toLocale = getLocaleFromString(modulePreferences.get("toLocale", Locale.getDefault().toString()));
                            ((ZipExplorerTopComponent) tc).setBundles(bundleFile, fromLocale, toLocale);
                        }
                    }
                }
            });
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, e.toString());
            System.err.println();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    private Locale getLocaleFromString(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        String locale[] = (str + "__").split("_", 3);

        return new Locale(locale[0], locale[1], locale[2]);
    }
}
