/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.explorers.zipexplorer.actions;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.ancestris.trancestris.resources.ZipArchive;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class ZipExplorerOpenAction implements ActionListener {

    File zipFile = null;
    Locale fromLocale = null;
    Locale toLocale = null;
    ZipExplorerOpenActionPanel zipExplorerOpenActionPanel = new ZipExplorerOpenActionPanel();

    @Override
    public void actionPerformed(ActionEvent e) {

        class zipExplorerOpenActionPanelActionListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                zipFile = zipExplorerOpenActionPanel.zipFile;
                fromLocale = zipExplorerOpenActionPanel.fromLocale;
                toLocale = zipExplorerOpenActionPanel.toLocale;
            }
        }
        DialogDescriptor zipExplorerOpenActionDescriptor = new DialogDescriptor(
                zipExplorerOpenActionPanel,
                NbBundle.getMessage(ZipExplorerOpenActionPanel.class, "CTL_ZipExplorerOpenAction"),
                true,
                new zipExplorerOpenActionPanelActionListener());

        Dialog dialog = DialogDisplayer.getDefault().createDialog(zipExplorerOpenActionDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        if (zipExplorerOpenActionDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            Frame mainWindow = WindowManager.getDefault().getMainWindow();
            mainWindow.setTitle(NbBundle.getMessage(ZipExplorerOpenAction.class,"CTL_MainWindow_Title",fromLocale.getDisplayLanguage(), toLocale.getDisplayLanguage()));

            ZipArchive zipArchive = new ZipArchive(zipFile, fromLocale, toLocale);

            TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
            ((ZipExplorerTopComponent) tc).setBundles(zipArchive);
        }
    }
}
