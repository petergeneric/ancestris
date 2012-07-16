/*
 * Copyright (C) 2012 lemovice
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ancestris.modules.gedcom.history;

import ancestris.app.App;
import ancestris.core.pluginservice.PluginInterface;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.modules.Places;
import org.openide.util.Lookup;

@ActionID(category = "Tools",
id = "ancestris.modules.gedcom.history.ClearHistoryAction")
@ActionRegistration(displayName = "#CTL_ClearHistoryAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools/Gedcom/History", position = 3333)
})
public final class ClearHistoryAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        for (PluginInterface pluginInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
            if (pluginInterface instanceof GedcomHistoryPlugin) {
                Context context = App.center.getSelectedContext(true);
                if (context != null) {
                    String gedcomName = context.getGedcom().getName().substring(0, context.getGedcom().getName().lastIndexOf(".") == -1 ? context.getGedcom().getName().length() : context.getGedcom().getName().lastIndexOf("."));
                    File cacheSubdirectory = Places.getCacheSubdirectory(GedcomHistoryPlugin.class.getCanonicalName());
                    File historyFile = new File(cacheSubdirectory.getAbsolutePath() + System.getProperty("file.separator") + gedcomName + ".hist");

                    ((GedcomHistoryPlugin) pluginInterface).getGedcomHistory(gedcomName).clear();
                    if (historyFile.exists() == true) {
                        historyFile.delete();
                    }
                }
                break;
            }
        }
    }
}
