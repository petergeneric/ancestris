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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.edit.actions;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAlias;
import java.util.Collection;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.LookupEvent;

/**
 * Create an alias between records of two individuals indicating that the person is the same
 */
@ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateAlias")
@ActionRegistration(displayName = "#add.alias",
        lazy = false)
@ActionReferences(value = {
    @ActionReference(position=910,separatorBefore=900,path = "Ancestris/Actions/GedcomProperty/AddIndiOrFam")})
public class CreateAlias extends CreateRelationship {
  
  private Indi source;
  
  /** constructor */
    public CreateAlias() {
        this(null);
    }

  /** constructor */
  public CreateAlias(Indi source) {
        super(resources.getString("add.alias"), Gedcom.INDI);
        setContextProperties(source);
        contextChanged();
  }

    @Override
    public void resultChanged(LookupEvent ev) {
        source = null;
        Collection<? extends Property> props = lkpInfo.allInstances();
        if (!props.isEmpty()) {
            Property prop = props.iterator().next();
            if (prop instanceof Indi) {
                source = (Indi) prop;
            }
        }
        super.resultChanged(ev);
    }

    @Override
    protected final void contextChanged() {
        if (source != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }


    /** description of what this does */
    @Override
  public String getDescription() {
    return resources.getString("add.alias.of", source.toString() );
  }

  /** perform the change */
    @Override
  protected Property change(Entity target, boolean targetIsNew) throws GedcomException {
    
    // create ALIAs in entity
    PropertyAlias alias = (PropertyAlias)source.addProperty("ALIA", '@'+target.getEntity().getId()+'@');
    
    // link it
    try {
      alias.link();
    } catch (GedcomException e) {
      source.delProperty(alias);
      throw e;
    }
    
    // done - continue with alias link
    return alias;
  }

}
