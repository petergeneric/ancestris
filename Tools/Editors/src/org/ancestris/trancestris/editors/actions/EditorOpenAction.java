package org.ancestris.trancestris.editors.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.ancestris.trancestris.editors.EditorTopComponent;
import org.ancestris.trancestris.resources.ResourceFile;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class EditorOpenAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        final FileNameExtensionFilter filter = new FileNameExtensionFilter("Properties files", "properties");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            Locale defaultLocale = Locale.getDefault();
            File defaultBundleFile = fileChooser.getSelectedFile();
            String defaultBundleFileName = defaultBundleFile.getName();
            int extensionIndex = defaultBundleFileName.lastIndexOf(".");
            String translatedBundleFileName = "";
            try {
                translatedBundleFileName = fileChooser.getCurrentDirectory().getCanonicalPath() + System.getProperty("file.separator");
                translatedBundleFileName += defaultBundleFileName.substring(0, extensionIndex);
                translatedBundleFileName += "_" + defaultLocale.getLanguage();
                translatedBundleFileName += defaultBundleFileName.substring(extensionIndex, defaultBundleFileName.length());
                File translatedBundleFile = new File (translatedBundleFileName);
                if (translatedBundleFile.exists() == false) {
                    translatedBundleFile.createNewFile();
                }
                ResourceFile resourceFile = new ResourceFile(defaultBundleFile, false);
                resourceFile.setTranslation(translatedBundleFile, false);
                TopComponent tc = WindowManager.getDefault().findTopComponent("EditorTopComponent");
                ((EditorTopComponent) tc).setBundles(resourceFile);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
