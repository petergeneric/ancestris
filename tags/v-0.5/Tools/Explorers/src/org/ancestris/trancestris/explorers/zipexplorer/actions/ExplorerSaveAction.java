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

public final class ExplorerSaveAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
            ZipArchive zipArchive = ((ZipExplorerTopComponent) tc).getBundles();
            File file = new File("test.zip");
            if (file.exists() == false) {
                file.createNewFile();
            }
            zipArchive.write(file, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
