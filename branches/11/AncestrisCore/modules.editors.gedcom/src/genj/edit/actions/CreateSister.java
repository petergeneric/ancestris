/**
 * Ancestris
 *
 * Copyright (C) 2018 Frederic Lapeyre <frederic@ancestris.org>
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

import genj.gedcom.PropertySex;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 * Create a child of a family or person
 */
@ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateSister")
@ActionRegistration(displayName = "#add.sister",
        lazy = false)
@ActionReferences(value = {
    @ActionReference(position=305,path = "Ancestris/Actions/GedcomProperty/AddIndiOrFam")})
public class CreateSister extends CreateSibling {

    /** constructor */
    public CreateSister() {
        super(null, PropertySex.FEMALE);
    }

}
