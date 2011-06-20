package org.ancestris.trancestris.editors.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.ancestris.trancestris.editors.EditorTopComponent;
import org.ancestris.trancestris.resources.ResourceFile;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class EditorOpenAction implements ActionListener {

    File defaultBundleFile = null;
    Locale selectedLocale = null;
    EditorOpenActionPanel editorOpenActionPanel = new EditorOpenActionPanel();

    @Override
    public void actionPerformed(ActionEvent e) {

        class EditorOpenActionPanelActionListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                defaultBundleFile = editorOpenActionPanel.defaultBundleFile;
                selectedLocale = editorOpenActionPanel.selectedLocale;
            }
        }
        DialogDescriptor GeneanetExportActionDescriptor = new DialogDescriptor(
                editorOpenActionPanel,
                NbBundle.getMessage(EditorOpenActionPanel.class, "CTL_EditorOpenAction"),
                true,
                new EditorOpenActionPanelActionListener());

        Dialog dialog = DialogDisplayer.getDefault().createDialog(GeneanetExportActionDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        if (GeneanetExportActionDescriptor.getValue() == DialogDescriptor.OK_OPTION) {

            try {
                ResourceFile resourceFile = new ResourceFile(defaultBundleFile);

                String defaultBundleFileName = defaultBundleFile.getCanonicalPath();
                int LanguageExtIndex = defaultBundleFileName.lastIndexOf("_");
                int extensionIndex = defaultBundleFileName.lastIndexOf(".");
                String translatedBundleFileName = "";
                if (LanguageExtIndex > 0) {
                    translatedBundleFileName = defaultBundleFileName.substring(0, LanguageExtIndex);
                } else {
                    translatedBundleFileName = defaultBundleFileName.substring(0, extensionIndex);
                }
                translatedBundleFileName += "_" + selectedLocale.getLanguage();
                translatedBundleFileName += defaultBundleFileName.substring(extensionIndex, defaultBundleFileName.length());
                File translatedBundleFile = new File(translatedBundleFileName);
                if (translatedBundleFile.exists() == true) {
                    resourceFile.setTranslation(translatedBundleFile);
                }
                TopComponent tc = WindowManager.getDefault().findTopComponent("EditorTopComponent");
                ((EditorTopComponent) tc).setBundles(resourceFile, selectedLocale);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
