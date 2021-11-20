/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application;

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.ancestris.trancestris.application.actions.DownloadBundlePanel;
import org.ancestris.trancestris.application.actions.SendTranslationAction;
import org.ancestris.trancestris.application.actions.SendTranslationPanel;
import org.ancestris.trancestris.application.utils.DownloadBundleWorker;
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.ancestris.trancestris.resources.ZipArchive;
import org.ancestris.trancestris.tipoftheday.TipOfTheDay;
import org.netbeans.api.options.OptionsDisplayer;
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
    private final static String SEND = NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.button.send");
    String toLocale = "";
    String fromLocale = "";

    @Override
    public boolean closing() {
        TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
        ZipArchive zipArchive = ((ZipExplorerTopComponent) tc).getBundles();
        SendTranslationPanel sendTranslationPanel = new SendTranslationPanel();
        String archiveName = "";
        String filePath = "";
        String prefix = "";
        String suffix = "";
        File zipOutputFile = null;

        if (zipArchive != null) {
            if (((ZipExplorerTopComponent) tc).getBundles().isChange()) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(Installer.class, "Exit.confirm"), NotifyDescriptor.YES_NO_OPTION);
                DialogDisplayer.getDefault().notify(nd);
                if (nd.getValue() == DialogDescriptor.YES_OPTION) {
                    zipArchive.write();
                }
            }
            return true;

        } else {
            return true;
        }
    }

    @Override
    public void validate() throws IllegalStateException {
        /*
         * 29/08/2012 Update properties according our new ancestris website
         * Should be removed in next trancestris major version
         */

        // Update center
        Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate/org_ancestris_trancestris_application_update_center");
        p.put("originalUrl", "http://dl.ancestris.org/trancestris/nbm/core/updates.xml");
        try {
            p.flush();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }

        // bundles file
        Preferences p1 = NbPreferences.root().node("/org/ancestris/trancestris/application");
        p1.put("Url.address", "https://www.dl.ancestris.org/trancestris/bundles/Ancestris_Bundles.zip");
        try {
            p1.flush();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                String UrlAddress = NbPreferences.forModule(Installer.class).get("Url.address", NbBundle.getMessage(DownloadBundlePanel.class, "DownloadBundlePanel.urlTextField.text"));
                String dirName = "";
                String fileName = "";
                File bundleFile = null;
                URL url;

                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                dirName = modulePreferences.get("Dossier", System.getProperty("user.dir"));
                fileName = modulePreferences.get("Fichier", "Ancestris_Bundles.zip");
                bundleFile = new File(dirName + System.getProperty("file.separator") + fileName);

                // No local Bundle file available 
                if (bundleFile.exists() == false) {
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
                } else {
                    TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");

                    // Open the current bundle
                    Locale fromLocale = getLocaleFromString(modulePreferences.get("fromLocale", Locale.ENGLISH.toString()));
                    Locale toLocale = getLocaleFromString(modulePreferences.get("toLocale", Locale.getDefault().toString()));
                    ((ZipExplorerTopComponent) tc).setBundles(bundleFile, fromLocale, toLocale);

                    if (((ZipExplorerTopComponent) tc).getBundles().hasTranslation() == false && modulePreferences.getBoolean("Check-New-File-On-Server", true) == true) {
                        NotifyDescriptor checkForNewFile = new NotifyDescriptor.Confirmation(NbBundle.getMessage(Installer.class, "Check-New-File-On-Server"), NotifyDescriptor.YES_NO_OPTION);
                        DialogDisplayer.getDefault().notify(checkForNewFile);
                        if (checkForNewFile.getValue() == NotifyDescriptor.YES_OPTION) {
                            try {
                                url = new URL(UrlAddress);
                                URLConnection urlC = url.openConnection();
                                logger.log(Level.INFO, "Use URL: {0}", urlC.getURL());

                                // log info about resource
                                Date date1 = new Date(urlC.getLastModified());
                                Date date2 = new Date(NbPreferences.forModule(Installer.class).getLong("Url.LastModified", 0L));
                                logger.log(Level.INFO, "Server date {0} local date {1})", new Object[]{DateFormat.getInstance().format(date1), DateFormat.getInstance().format(date1)});

                                if (date1.after(date2)) {
                                    NotifyDescriptor downLoadNewFile = new NotifyDescriptor.Confirmation(NbBundle.getMessage(Installer.class, "New-File-On-Server"), NotifyDescriptor.YES_NO_OPTION);
                                    DialogDisplayer.getDefault().notify(downLoadNewFile);
                                    if (downLoadNewFile.getValue() == DialogDescriptor.YES_OPTION) {
                                        Thread t = new Thread(new DownloadBundleWorker(url, bundleFile));
                                        t.start();
                                    }
                                } else {
                                    NotifyDescriptor FileUpToDate = new NotifyDescriptor.Message(NbBundle.getMessage(Installer.class, "File-Up-To-Date"), NotifyDescriptor.INFORMATION_MESSAGE);
                                    DialogDisplayer.getDefault().notify(FileUpToDate);
                                }
                            } catch (MalformedURLException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    } else if (((ZipExplorerTopComponent) tc).getBundles().hasTranslation()) {
                        NotifyDescriptor info = new NotifyDescriptor.Message(NbBundle.getMessage(Installer.class, "Translation_pending"), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(info);
                    }
                }

                // set mail properties
                if (modulePreferences.get("mail.host", "").equals("")) {
                    NotifyDescriptor nd1 = new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationPanel.class, "SendTranslationPanel.msg.setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd1);
                    OptionsDisplayer.getDefault().open("SendTranslation");
                }

                //Load the tips into the tip loader:
                String tipsFileName = "/org/ancestris/trancestris/application/resources/Bundle_" + Locale.getDefault().getLanguage() + ".properties";
                logger.log(Level.INFO, "selected tips {0}", tipsFileName);
                InputStream propertiesIn = getClass().getResourceAsStream(tipsFileName);
                if (propertiesIn == null) {
                    tipsFileName = "/org/ancestris/trancestris/application/resources/Bundle.properties";
                    propertiesIn = getClass().getResourceAsStream(tipsFileName);
                    if (propertiesIn == null) {
                        logger.log(Level.INFO, "default tips {0} not found", tipsFileName);
                    } else {
                        logger.log(Level.INFO, "Local tip not found. Selected default tips {0}", tipsFileName);
                    }
                }
                new TipOfTheDay(propertiesIn);
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

    private void setDefaultValues(SendTranslationPanel sendTranslationPanel) {
//        sendTranslationPanel.setMailToFormattedTextField(modulePreferences.get("mailto.address", "frederic@ancestris.org"));
        sendTranslationPanel.setMailToFormattedTextField("frederic@ancestris.org");
        sendTranslationPanel.setNameFormattedTextField(modulePreferences.get("mail.name", ""));
        sendTranslationPanel.setEmailFormattedTextField(modulePreferences.get("mail.address", ""));
        String TS = new SimpleDateFormat(NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.subject.date")).format(new Date());
        String subject = "[" + NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.subject.tag", fromLocale, toLocale) + " " + TS + "] ";
        sendTranslationPanel.setSubjectFormattedTextField(subject);
    }

    private void saveValues(SendTranslationPanel sendTranslationPanel) {
        modulePreferences.put("mail.name", sendTranslationPanel.getNameFormattedTextField());
        modulePreferences.put("mail.address", sendTranslationPanel.getEmailFormattedTextField());
        modulePreferences.put("mailto.address", sendTranslationPanel.getMailToFormattedTextField());
    }
}
