/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.explorers.zipexplorer.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.ancestris.trancestris.resources.ZipArchive;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class SaveTranslationAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
        ZipArchive zipArchive = ((ZipExplorerTopComponent) tc).getBundles();
        String archiveName = zipArchive.getName();
        String filePath = zipArchive.getZipFile().getParent();
        String prefix = archiveName.substring(0, archiveName.indexOf('.'));
        String suffix = archiveName.substring(archiveName.indexOf('.') + 1);
        String locale = zipArchive.getTranslatedLocale().getLanguage();


        File zipOutputFile = new File(filePath + File.separator + prefix + "_" + locale + "." + suffix);
        if (!zipOutputFile.exists()) {
            try {
                zipOutputFile.createNewFile();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        zipArchive.saveTranslation(zipOutputFile);
    }
}
