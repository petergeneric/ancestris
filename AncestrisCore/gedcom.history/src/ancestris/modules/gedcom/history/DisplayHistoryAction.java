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
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@ActionID(id = "ancestris.modules.gedcom.history.DisplayHistoryAction", category = "Edit")
@ActionRegistration(
        displayName = "#CTL_DisplayHistoryAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Edit", name = "DisplayHistoryAction", position = 2900)
public final class DisplayHistoryAction extends AbstractAncestrisContextAction {

    public DisplayHistoryAction() {
        super();
        setImage("ancestris/modules/gedcom/history/DisplayHistoryIcon.png");
        setText(NbBundle.getMessage(DisplayHistoryAction.class, "CTL_DisplayHistoryAction"));
    }
    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        Context contextToOpen = getContext();
        if (contextToOpen != null) {
            Gedcom gedcom = contextToOpen.getGedcom();
            GedcomHistoryTopComponent gedcomHistoryTopComponent = null;
            //Set<TopComponent> openedTopComponent = TopComponent.getRegistry().getOpened();
            //for (TopComponent topComponent : openedTopComponent) {
            for (AncestrisViewInterface topComponent : Lookup.getDefault().lookupAll(AncestrisViewInterface.class)) {
                if (topComponent instanceof GedcomHistoryTopComponent) {
                    if (((GedcomHistoryTopComponent) topComponent).getGedcom().equals(gedcom) == true) {
                        gedcomHistoryTopComponent = (GedcomHistoryTopComponent) topComponent;
                        break;
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
