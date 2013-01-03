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

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextFieldWidget;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.JPanel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.LookupEvent;

/**
 * Set the place hierarchy used in a gedcom file
 */
@ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.SetPlaceHierarchy")
@ActionRegistration(displayName = "#place.hierarchy")
@ActionReferences(value = {
    @ActionReference(path = "Ancestris/Actions/GedcomProperty")})
public class SetPlaceHierarchy extends AbstractChange {

    private static final ImageIcon IMG = Grammar.V55.getMeta(TagPath.valueOf("INDI:BIRT:PLAC")).getImage();
    /** the place to use as the global example */
    private PropertyPlace place;
    /** textfield for hierarchy */
    private TextFieldWidget hierarchy;

    public SetPlaceHierarchy() {
        super();
        setImageText(IMG, resources.getString("place.hierarchy"));
    }

    /**
     * Constructor
     */
    public SetPlaceHierarchy(PropertyPlace place) {
        this();
        this.place = place;
        contextChanged();
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends Property> props = lkpInfo.allInstances();
        if (props.size() != 1) {
            place = null;
        } else {
            Property p = props.iterator().next();
            if (p instanceof PropertyPlace) {
                place = (PropertyPlace) p;
            } else {
                place = null;
            }
        }
        super.resultChanged(ev);
    }

    @Override
    protected final void contextChanged() {
        if (place != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

    /**
     * no confirmation message needed
     */
    @Override
    protected String getConfirmMessage() {
        return resources.getString("place.hierarchy.msg", place.getGedcom().getName());
    }

    /**
     * Override content components to show to user
     */
    @Override
    protected JPanel getDialogContent() {

        JPanel result = new JPanel(new NestedBlockLayout("<col><confirm wx=\"1\" wy=\"1\"/><enter wx=\"1\"/></col>"));

        // prepare textfield for formar
        hierarchy = new TextFieldWidget(place.getFormatAsString());

        result.add(getConfirmComponent());
        result.add(hierarchy);

        // done
        return result;
    }

    /**
     * set the submitter
     */
    @Override
    protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {
        place.setFormatAsString(true, hierarchy.getText().trim());
        return null;
    }
} //SetPlaceFormat

