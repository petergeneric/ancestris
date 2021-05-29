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

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.gedcom.Context;
import org.openide.modules.ModuleInstall;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    GedcomHistoryPlugin gedcomHistoryPlugin;

    @Override
    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                AncestrisPlugin.register(gedcomHistoryPlugin = new GedcomHistoryPlugin());
                Context context = Utilities.actionsGlobalContext().lookup(Context.class);
                // On first install when a gedcom is already open
                // we need to open the history file.
                if (context != null && context.getGedcom() != null) {
                    gedcomHistoryPlugin.gedcomOpened (context.getGedcom());
                }
            }
        });
    }

    @Override
    public boolean closing() {
        return true;
    }

    @Override
    public void uninstalled() {
        AncestrisPlugin.register(gedcomHistoryPlugin);
    }
}
