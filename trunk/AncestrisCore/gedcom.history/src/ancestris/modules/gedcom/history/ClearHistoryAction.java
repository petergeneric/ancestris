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

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.core.pluginservice.PluginInterface;
import genj.gedcom.Context;
import java.awt.event.ActionEvent;
import java.io.File;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

//@ActionID(id = "ancestris.modules.gedcom.history.ClearHistoryAction", category = "Edit")
//@ActionRegistration(
//        displayName = "#CTL_ClearHistoryAction",
//        iconInMenu = true,
//        lazy = false)
//@ActionReference(path = "Menu/Edit", name = "ClearHistoryAction", position = 2510)
public final class ClearHistoryAction extends AbstractAncestrisContextAction {

    public ClearHistoryAction() {
        super();
        setImage("ancestris/modules/gedcom/history/ClearHistoryIcon.png");
        setText(NbBundle.getMessage(DisplayHistoryAction.class, "CTL_ClearHistoryAction"));
    }
    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        for (PluginInterface pluginInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
            if (pluginInterface instanceof GedcomHistoryPlugin) {
                Context context = getContext();
                if (context != null) {
                    String gedcomName = context.getGedcom().getName().substring(0, context.getGedcom().getName().lastIndexOf(".") == -1 ? context.getGedcom().getName().length() : context.getGedcom().getName().lastIndexOf("."));
                    File cacheSubdirectory = Places.getCacheSubdirectory(GedcomHistoryPlugin.class.getCanonicalName());
                    File historyFile = new File(cacheSubdirectory.getAbsolutePath() + System.getProperty("file.separator") + gedcomName + ".hist");

                    ((GedcomHistoryPlugin) pluginInterface).getGedcomHistory(context.getGedcom()).clear();
                    if (historyFile.exists() == true) {
                        historyFile.delete();
                    }
                }
                break;
            }
        }
    }
}
