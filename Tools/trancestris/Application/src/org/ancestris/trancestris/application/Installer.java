/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application;

import java.awt.Dialog;
import org.ancestris.trancestris.application.utils.DownloadBundleWorker;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.ancestris.trancestris.application.actions.DownloadBundlePanel;
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.ancestris.trancestris.resources.ZipArchive;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    private Preferences modulePreferences = NbPreferences.forModule(Installer.class);
    private static final Logger logger = Logger.getLogger(Installer.class.getName());

    @Override
    public boolean closing() {
        TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
        ZipArchive zipArchive = ((ZipExplorerTopComponent) tc).getBundles();
        if (zipArchive != null) {
            if (((ZipExplorerTopComponent) tc).getBundles().isChange()) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(Installer.class, "Exit.confirm"), NotifyDescriptor.YES_NO_OPTION);
                DialogDisplayer.getDefault().notify(nd);
                if (nd.getValue() == DialogDescriptor.YES_OPTION) {
                    zipArchive.write();
                }
                return true;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                String UrlAddress = NbPreferences.forModule(Installer.class).get("Url.address", NbBundle.getMessage(DownloadBundlePanel.class, "DownloadBundlePanel.urlTextField.text"));
                String dirName = "";
                URL url;

                File bundleFile = null;

                // First Startup
                if ((dirName = modulePreferences.get("Dossier", "")).equals("") == true) {
                    DownloadBundlePanel downloadBundlePanel = new DownloadBundlePanel();
                    DialogDescriptor downloadActionDescriptor = new DialogDescriptor(
                            downloadBundlePanel,
                            NbBundle.getMessage(DownloadBundlePanel.class, "CTL_DownloadBundleAction"),
                            true,
                            null);
                    Dialog dialog = DialogDisplayer.getDefault().createDialog(downloadActionDescriptor);

                    dialog.setVisible(true);
                    dialog.toFront();
                    if (downloadActionDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
                        try {
                            url = new URL(downloadBundlePanel.getBundleUrl());
                            bundleFile = downloadBundlePanel.getLocalBundleFile();
                            NbPreferences.forModule(DownloadBundlePanel.class).put("Dossier", bundleFile.getParent());
                            NbPreferences.forModule(DownloadBundlePanel.class).put("Fichier", bundleFile.getName());
                            NbPreferences.forModule(DownloadBundlePanel.class).put("Url.address", url.toString());
                            NbPreferences.forModule(DownloadBundlePanel.class).put("fromLocale", downloadBundlePanel.getFromLocale().toString());
                            NbPreferences.forModule(DownloadBundlePanel.class).put("toLocale", downloadBundlePanel.getToLocale().toString());
                            Thread t = new Thread(new DownloadBundleWorker(url, bundleFile));
                            t.start();
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                } else {
                    NotifyDescriptor checkForNewFile = new NotifyDescriptor.Confirmation(NbBundle.getMessage(Installer.class, "Check-New-File-On-Server"), NotifyDescriptor.YES_NO_OPTION);
                    DialogDisplayer.getDefault().notify(checkForNewFile);
                    String fileName = modulePreferences.get("Fichier", "Ancestris_Bundles.zip");
                    bundleFile = new File(dirName + System.getProperty("file.separator") + fileName);

                    if (checkForNewFile.getValue() == DialogDescriptor.YES_OPTION) {
                        try {
                            url = new URL(UrlAddress);
                            URLConnection urlC = url.openConnection();

                            // log info about resource
                            Date date1 = new Date(urlC.getLastModified());
                            Date date2 = new Date(NbPreferences.forModule(Installer.class).getLong("Url.LastModified", 0L));

                            if (date1.after(date2)) {
                                logger.log(Level.INFO, "Server {0} local {1})", new Object[]{DateFormat.getInstance().format(date1), DateFormat.getInstance().format(date1)});
                                NotifyDescriptor downLoadNewFile = new NotifyDescriptor.Confirmation(NbBundle.getMessage(Installer.class, "New-File-On-Server"), NotifyDescriptor.YES_NO_OPTION);
                                DialogDisplayer.getDefault().notify(downLoadNewFile);
                                if (downLoadNewFile.getValue() == DialogDescriptor.YES_OPTION) {
                                    Thread t = new Thread(new DownloadBundleWorker(url, bundleFile));
                                    t.start();
                                } else {
                                    TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
                                    if (bundleFile != null) {
                                        if (bundleFile.exists()) {
                                            Locale fromLocale = getLocaleFromString(modulePreferences.get("fromLocale", ""));
                                            Locale toLocale = getLocaleFromString(modulePreferences.get("toLocale", ""));
                                            ((ZipExplorerTopComponent) tc).setBundles(bundleFile, fromLocale, toLocale);
                                        }
                                    }
                                }
                            } else {
                                NotifyDescriptor FileUpToDate = new NotifyDescriptor.Message(NbBundle.getMessage(Installer.class, "File-Up-To-Date"), NotifyDescriptor.INFORMATION_MESSAGE);
                                DialogDisplayer.getDefault().notify(FileUpToDate);
                                TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
                                if (bundleFile != null) {
                                    if (bundleFile.exists()) {
                                        Locale fromLocale = getLocaleFromString(modulePreferences.get("fromLocale", ""));
                                        Locale toLocale = getLocaleFromString(modulePreferences.get("toLocale", ""));
                                        ((ZipExplorerTopComponent) tc).setBundles(bundleFile, fromLocale, toLocale);
                                    }
                                }
                            }
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } else {
                        TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
                        if (bundleFile != null) {
                            if (bundleFile.exists()) {
                                Locale fromLocale = getLocaleFromString(modulePreferences.get("fromLocale", ""));
                                Locale toLocale = getLocaleFromString(modulePreferences.get("toLocale", ""));
                                ((ZipExplorerTopComponent) tc).setBundles(bundleFile, fromLocale, toLocale);
                            }
                        }
                    }
                }
            }
        });
    }

    private Locale getLocaleFromString(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        String locale[] = (str + "__").split("_", 3);

        return new Locale(locale[0], locale[1], locale[2]);
    }
}
