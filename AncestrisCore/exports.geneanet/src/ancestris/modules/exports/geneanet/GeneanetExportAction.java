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

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.SaveOptionsWidget;
import ancestris.util.swing.FileChooserBuilder;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.io.Filter;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import javax.swing.JFrame;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

@ActionID(id = "ancestris.modules.exports.geneanet.GeneanetExportAction", category = "File")
@ActionRegistration(
        displayName = "#CTL_GeneanetExportAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/File/Export", name = "GeneanetExportAction", position = 100)
public final class GeneanetExportAction extends AbstractAncestrisContextAction {

    private Gedcom myGedcom = null;

    public GeneanetExportAction() {
        super();
        setImage("ancestris/modules/exports/geneanet/geneanet.png");
        setText(NbBundle.getMessage(GeneanetExportAction.class, "CTL_GeneanetExportAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        // Create the file chooser
        Context contextToOpen = getContext();

        if (contextToOpen != null) {
            myGedcom = contextToOpen.getGedcom();
            String gedcomName = removeExtension(myGedcom.getName());

            ArrayList<Filter> theFilters = new ArrayList<Filter>(5);
            for (Filter f : AncestrisPlugin.lookupAll(Filter.class)) {
                if (f.canApplyTo(contextToOpen.getGedcom())) {
                    theFilters.add(f);
                }
            }
            for (Filter f : Lookup.getDefault().lookupAll(Filter.class)) {
                if (f.canApplyTo(contextToOpen.getGedcom())) {
                    theFilters.add(f);
                }
            }

            SaveOptionsWidget options = new SaveOptionsWidget(theFilters.toArray(new Filter[]{}));

            File file = new FileChooserBuilder(GeneanetExportAction.class)
                    .setFilesOnly(true)
                    .setDefaultBadgeProvider()
                    .setTitle(NbBundle.getMessage(getClass(), "FileChooserTitle", myGedcom.getName()))
                    .setApproveText(NbBundle.getMessage(getClass(), "FileChooserOKButton"))
                    .setFileFilter(FileChooserBuilder.getGedcomFilter())
                    .setAcceptAllFileFilterUsed(false)
                    .setDefaultExtension(FileChooserBuilder.getGedcomFilter().getExtensions()[0])
                    .setAccessory(options)
                    .setFileHiding(true)
                    .setSelectedFile(new File(gedcomName + "-geneanet"))
                    .showSaveDialog();
            
            if (file != null) {
                GeneanetExport exportGeneanet = new GeneanetExport(myGedcom, file, options);
                showWaitCursor();
                String result = "";
                boolean b;
                if (b = exportGeneanet.execute()) {
                    result = NbBundle.getMessage(GeneanetExport.class, "GeneanetExportAction.End");
                } else {
                    result = NbBundle.getMessage(GeneanetExport.class, "GeneanetExportAction.Error");
                }
                hideWaitCursor();
                NotifyDescriptor nd = new NotifyDescriptor.Message(result, b ? NotifyDescriptor.INFORMATION_MESSAGE : NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);

                try {
                    String fileStr = "http://www.geneanet.org/creer-votre-arbre/gedcom";
                    URI uri = new URI(fileStr);
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(uri);
                    } else {
                    }
                } catch (Exception ex) {
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
