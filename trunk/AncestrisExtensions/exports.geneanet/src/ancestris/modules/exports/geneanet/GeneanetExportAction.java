/*
 *  Copyright (C) 2011 lemovice
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * GeneanetExportAction.java
 *
 * Created on 23 mai 2011, 21:34:49
 */
package ancestris.modules.exports.geneanet;

import genj.gedcom.Context;
import ancestris.app.App;
import genj.gedcom.Gedcom;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

public final class GeneanetExportAction implements ActionListener {

    JFileChooser fc = new JFileChooser() {

        @Override
        public void approveSelection() {
            File f = getSelectedFile();
            if (f.exists() && getDialogType() == SAVE_DIALOG) {
                int result = JOptionPane.showConfirmDialog(this, NbBundle.getMessage(GeneanetExport.class, "GeneanetExportAction.Overwrite.Text"), NbBundle.getMessage(GeneanetExport.class, "GeneanetExportAction.Overwrite.Title"), JOptionPane.YES_NO_CANCEL_OPTION);
                switch (result) {
                    case JOptionPane.YES_OPTION:
                        super.approveSelection();
                        return;
                    case JOptionPane.NO_OPTION:
                        return;
                    case JOptionPane.CANCEL_OPTION:
                        super.cancelSelection();
                        return;
                }
            } else {
                if (filter.accept(f) == false) {
                    setSelectedFile(new File(f.getName() + ".gw"));
                }
            }
            super.approveSelection();
        }
    };
    private Gedcom myGedcom = null;
    final FileNameExtensionFilter filter = new FileNameExtensionFilter(NbBundle.getMessage(GeneanetExport.class, "GeneanetExportAction.fileType"), "gw");
    String exportDirName = "";
    String exportFileName = "";

    @Override
    public void actionPerformed(ActionEvent e) {
        // Create the file chooser
        Context context;

        if ((context = App.center.getSelectedContext(true)) != null) {
            myGedcom = context.getGedcom();
            Preferences modulePreferences = NbPreferences.forModule(GeneanetExport.class);
            if (modulePreferences.getInt("RestricitionDuration", -1) == -1) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(GeneanetExport.class, "GeneanetExportAction.setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                OptionsDisplayer.getDefault().open("Extensions/GeneanetExport");
            } else {

                String gedcomName = removeExtension(myGedcom.getName());

                exportDirName = modulePreferences.get("Dossier-Export-" + gedcomName, "");
                exportFileName = modulePreferences.get("Fichier-Export-" + gedcomName, gedcomName + ".gw");

                if (exportDirName.length() > 0) {
                    // Set the current directory
                    fc.setCurrentDirectory(new File(exportDirName));
                }

                fc.setFileFilter(filter);
                fc.setAcceptAllFileFilterUsed(false);
                fc.setSelectedFile(new File(exportFileName));

                if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File exportFile = fc.getSelectedFile();
                    modulePreferences.put("Dossier-Export-" + gedcomName, exportFile.getPath());
                    modulePreferences.put("Fichier-Export-" + gedcomName, exportFile.getName());

                    GeneanetExport exportGeneanet = new GeneanetExport(myGedcom, exportFile);
                    showWaitCursor();
                    exportGeneanet.start();
                    hideWaitCursor();
                    NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(GeneanetExport.class, "GeneanetExportAction.End"), NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        }
    }

    private String removeExtension(String filename) {

        String separator = System.getProperty("file.separator");

        // Remove the path upto the filename.
        int lastSeparatorIndex = filename.lastIndexOf(separator);
        if (lastSeparatorIndex != -1) {
            filename = filename.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1) {
            return filename;
        }

        return filename.substring(0, extensionIndex);
    }

    private static void showWaitCursor() {
        Mutex.EVENT.readAccess(new Runnable() {

            @Override
            public void run() {
                JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                mainWindow.getGlassPane().setVisible(true);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(GeneanetExportAction.class, "GeneanetExportAction.Start"));
            }
        });
    }

    private static void hideWaitCursor() {
        Mutex.EVENT.readAccess(new Runnable() {

            @Override
            public void run() {
                StatusDisplayer.getDefault().setStatusText("");  //NOI18N
                JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                mainWindow.getGlassPane().setVisible(false);
                mainWindow.getGlassPane().setCursor(null);
            }
        });
    }
}
