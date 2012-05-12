/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.ancestris.trancestris.resources.ZipArchive;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
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
                TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
                String dirName = "";
                if ((dirName = modulePreferences.get("Dossier", "")).equals("") != true) {
                    String fileName = modulePreferences.get("Fichier", "");
                    File tempfile = new File(dirName + System.getProperty("file.separator") + fileName);

                    if (tempfile.exists()) {
                        Locale fromLocale = getLocaleFromString(modulePreferences.get("fromLocale", ""));
                        Locale toLocale = getLocaleFromString(modulePreferences.get("toLocale", ""));
                        ((ZipExplorerTopComponent) tc).setBundles(tempfile, fromLocale, toLocale);
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
