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
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.ancestris.trancestris.application.actions.DownloadBundlePanel;
import org.ancestris.trancestris.application.actions.TipOfTheDayAction;
import org.ancestris.trancestris.application.utils.DownloadBundleWorker;
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.ancestris.trancestris.resources.ZipArchive;
import org.jdesktop.swingx.JXTipOfTheDay;
import org.jdesktop.swingx.tips.TipLoader;
import org.jdesktop.swingx.tips.TipOfTheDayModel;
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
        p1.put("Url.address", "http://www.dl.ancestris.org/trancestris/bundles/Ancestris_Bundles.zip");
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
                    NotifyDescriptor checkForNewFile = new NotifyDescriptor.Confirmation(NbBundle.getMessage(Installer.class, "Check-New-File-On-Server"), NotifyDescriptor.YES_NO_OPTION);
                    DialogDisplayer.getDefault().notify(checkForNewFile);

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
                                Locale fromLocale = getLocaleFromString(modulePreferences.get("fromLocale", Locale.ENGLISH.toString()));
                                Locale toLocale = getLocaleFromString(modulePreferences.get("toLocale", Locale.getDefault().toString()));
                                ((ZipExplorerTopComponent) tc).setBundles(bundleFile, fromLocale, toLocale);
                            }
                        }
                    }
                }

                try {
                    final JXTipOfTheDay jXTipOfTheDay = new JXTipOfTheDay(loadModel());
                    jXTipOfTheDay.setCurrentTip(getStartingTipLocation());
                    jXTipOfTheDay.showDialog(null, new JXTipOfTheDay.ShowOnStartupChoice() {
                        @Override
                        public boolean isShowingOnStartup() {
                            return isStartupChoiceOption();
                        }

                        @Override
                        public void setShowingOnStartup(boolean showOnStartup) {
                            setStartupChoiceOption(showOnStartup);
                            setNextStartingTipLocation(jXTipOfTheDay.getCurrentTip(), jXTipOfTheDay.getModel().getTipCount());
                        }
                    });
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
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

    private TipOfTheDayModel loadModel() throws Exception {
        //Load the tips into the tip loader:
        InputStream propertiesIn = getClass().getResourceAsStream("tips.properties");
        Properties properties = new Properties();
        properties.load(propertiesIn);
        return TipLoader.load(properties);
    }

    //Return whether the tip dialog should be shown at start up:
    private static boolean isStartupChoiceOption() {
        Preferences pref = NbPreferences.forModule(TipOfTheDayAction.class);
        boolean s = pref.getBoolean("StartUpPref", true);
        return s;
    }

    //Store whether the tip dialog should be shown at start up:
    private static void setStartupChoiceOption(boolean val) {
        NbPreferences.forModule(TipOfTheDayAction.class).putBoolean("StartUpPref", val);
        System.out.println("Show Tips on Startup: " + val);
    }

    //Get the tip to be shown,
    //via the NbPreferences API:
    private static int getStartingTipLocation() {
        Preferences pref = NbPreferences.forModule(TipOfTheDayAction.class);
        //Return the first tip if pref is null:
        if (pref == null) {
            return 0;
            //Otherwise, return the tip found via NbPreferences API,
            //with '0' as the default:    
        } else {
            int s = pref.getInt("StartTipPref", 0);
            return s;
        }
    }

    //Set the tip that will be shown next time,
    //we receive the current tip and the total tips:
    private static void setNextStartingTipLocation(int loc, int tot) {

        int nextTip = 0;
        //Back to zero if the maximum is reached:
        if (loc + 1 == tot) {
            nextTip = 0;
            //Otherwise find the next tip and store it:
        } else {
            nextTip = loc + 1;
        }

        //Store the tip, via the NbPreferences API,
        //so that it will be stored in the NetBeans user directory:
        NbPreferences.forModule(TipOfTheDayAction.class).putInt("StartTipPref", nextTip);

        System.out.println("Total tips: " + tot);
        System.out.println("Current tip location: " + loc);
        System.out.println("Future tip location: " + nextTip);
    }
}
