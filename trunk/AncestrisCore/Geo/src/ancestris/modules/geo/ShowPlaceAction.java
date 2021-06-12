/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ancestris.modules.geo;

import ancestris.api.place.ShowPlace;
import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.core.pluginservice.AncestrisPlugin;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.util.Resources;
import java.awt.event.ActionEvent;
import java.util.Collection;
import org.jxmapviewer.viewer.GeoPosition;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.LookupEvent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * External action
 */
@ActionID(category = "View", id = "ancestris.modules.geo.ShowPlace")
@ActionRegistration(displayName = "Show Place",lazy = false)
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty", position = 745)})
public class ShowPlaceAction extends AbstractAncestrisContextAction {

    /** the wrapped file */
    private PropertyPlace pPlace;
    private final static Resources RESOURCES = Resources.get(ShowPlaceAction.class);

    public ShowPlaceAction() {
        super();
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        // valid only for context aware action
        setEnabled(false);
        if (lkpInfo != null) {
            pPlace = null;
            for (Property prop : lkpInfo.allInstances()) {
                if (prop instanceof PropertyPlace) {
                    pPlace = (PropertyPlace) prop;
                }
            }
            super.resultChanged(ev);
        }
    }

    @Override
    protected void contextChanged() {
        super.contextChanged();
        setImage(PropertyPlace.IMAGE);
        setText(RESOURCES.getString("ACTION_ShowPlace").replaceAll("&", ""));
        setTip(RESOURCES.getString("ACTION_ShowPlace.tip"));
        setEnabled(pPlace != null && pPlace.getGeoPosition() != null);
    }

    @Override
    public boolean isDefault(Property prop) {
        return prop instanceof PropertyPlace;
    }
    
    
    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        if (pPlace != null) {
            GeoPosition gp = pPlace.getGeoPosition();
            Collection<? extends ShowPlace> showers = AncestrisPlugin.lookupAll(ShowPlace.class);
            if (showers.isEmpty()) {
                getMapTopComponent(pPlace.getGedcom());
                showers = AncestrisPlugin.lookupAll(ShowPlace.class);
            }
            if (!showers.isEmpty()) {
                for (ShowPlace shower : showers) {
                    shower.showPlace(gp);
                }
            }
            return;
        }
    }

    private GeoMapTopComponent getMapTopComponent(Gedcom gedcom) {
        GeoMapTopComponent theList = null;
        // get map top component
        for (TopComponent tc : WindowManager.getDefault().getRegistry().getOpened()) {
            if (tc instanceof GeoMapTopComponent) {
                GeoMapTopComponent gmtc = (GeoMapTopComponent) tc;
                if (gmtc.getGedcom() == gedcom) {
                    theList = gmtc;
                    break;
                }
            }
        }
        if (theList == null) {
            theList = new GeoMapTopComponent();
            theList.init(new Context(gedcom));
            theList.open();
        }
        return theList;
    }

    
} //RunExternal
