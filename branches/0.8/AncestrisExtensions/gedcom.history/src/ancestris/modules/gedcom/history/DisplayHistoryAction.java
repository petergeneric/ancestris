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

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

@ActionID(category = "Tools",
id = "ancestris.modules.gedcom.history.DisplayHistoryAction")
@ActionRegistration(iconInMenu = true,
displayName = "#CTL_DisplayHistoryAction",
iconBase = "ancestris/modules/gedcom/history/DisplayHistoryIcon.png")
@ActionReferences({
    @ActionReference(path = "Menu/Tools/Gedcom/History", position = 3333)
})
public final class DisplayHistoryAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Context context = Utilities.actionsGlobalContext().lookup(Context.class);
        if (context != null) {
            Gedcom gedcom = context.getGedcom();
            GedcomHistoryTopComponent gedcomHistoryTopComponent = null;
            Set<TopComponent> openedTopComponent = TopComponent.getRegistry().getOpened();
            for (TopComponent topComponent : openedTopComponent) {
                if (topComponent instanceof GedcomHistoryTopComponent) {
                    if (((GedcomHistoryTopComponent) topComponent).getGedcom().equals(gedcom) == true) {
                        gedcomHistoryTopComponent = (GedcomHistoryTopComponent) topComponent;
                        return;
                    }
                }
            }
            if (gedcomHistoryTopComponent == null) {
                gedcomHistoryTopComponent = new GedcomHistoryTopComponent();
            }
            gedcomHistoryTopComponent.open();
            gedcomHistoryTopComponent.requestActive();
        }
    }
}
