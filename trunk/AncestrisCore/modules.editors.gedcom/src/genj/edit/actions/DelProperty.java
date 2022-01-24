/**
 * Ancestris - http://www.ancestris.org (Formerly GenJ - GenealogyJ)
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2010 - 2013 Ancestris
 * Author: Daniel Andre <daniel@ancestris.org>
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
package genj.edit.actions;

import ancestris.core.resources.Images;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.LookupEvent;

/**
 * PDelete - delete a property
 */
@ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.DelProperty")
@ActionRegistration(displayName = "#delete",
        lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Ancestris/Actions/GedcomProperty", position = 670)})
public class DelProperty extends AbstractChange {

    /** the candidates to delete */
    private Set<Property> candidates = new HashSet<>();

    public DelProperty() {
        this(new ArrayList<Property>());
    }

    public DelProperty(List<Property> props) {
        super();
        setContextProperties(props);
        setImageText(Images.imgDel, resources.getString("delete") + "...");
        contextChanged();
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        candidates.clear();
        candidates.addAll(lkpInfo.allInstances());
        super.resultChanged(ev);
    }

    @Override
    protected final void contextChanged() {
        candidates.clear();
        candidates.addAll(contextProperties);
        setEnabled(!candidates.isEmpty());

        String result = "";
        if (candidates != null && !candidates.isEmpty()) {
            if (candidates.size() > 1) {
                result = Property.getPropertyNames(candidates, 5) + " (" + candidates.size() + ")";
            } else {
                result = candidates.iterator().next().toString();
            }
        }
        setTip(resources.getString("delete.tip", result));
    }

    //XXX: for Entity this was: 
//  protected String getConfirmMessage() {
//    // You are about to delete {0} of type {1} from {2}! Deleting this ...
//    return resources.getString("confirm.del", getCandidate().toString(), Gedcom.getName(getCandidate().getTag(),false), getGedcom().getName() );
//  }
    @Override
    protected String getConfirmMessage() {
        StringBuilder txt = new StringBuilder();
        txt.append(resources.getString("confirm.del.props", candidates.size()));
        txt.append("<br><br>");
        int i = 0;
        for (Property prop : candidates) {
            if (i++ > 16) {
                txt.append("...");
                break;
            }
            txt.append("&nbsp;&nbsp;&nbsp;&nbsp;&Bull;&nbsp;&nbsp;" + prop.toString());
            txt.append("<br>");
        }
        return txt.toString();
    }

    /**
     * Perform the delete
     */
    @Override
    protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {

        // leaving an orphan?
        Set<Entity> orphans = new HashSet<>();
        Property parent = null;
        
        for (Property prop : new HashSet<>(candidates)) {
            if (prop instanceof Entity) {
                gedcom.deleteEntity(((Entity) prop));
                continue;
            }

            if (prop instanceof PropertyXRef && prop.isValid()) {
                orphans.add(((PropertyXRef) prop).getTargetEntity());
                orphans.add(prop.getEntity());
            }
            parent = prop.getParent();
            parent.delProperty(prop);
        }

        // check for and delete orphans
        for (Entity orphan : orphans) {
            if (!(orphan instanceof Indi || orphan.isConnected())) {
                gedcom.deleteEntity(orphan);
            }
        }
        if (parent != null) {
            SelectionDispatcher.fireSelection(new Context(parent));
        }
        // nothing to go to
        return null;
    }
} //DelProperty

