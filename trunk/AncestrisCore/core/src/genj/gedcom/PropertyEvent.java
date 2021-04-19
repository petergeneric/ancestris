/**
 * GenJ - GenealogyJ
 * Ancestris
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2003 - 2021 Frederic Lapeyre <frederic@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.gedcom;

import genj.util.swing.ImageIcon;

/**
 * Gedcom Property : EVENT
 */
public class PropertyEvent extends PropertyEventDetails {

    public static ImageIcon IMG = Grammar.V55.getMeta(new TagPath("INDI:EVEN")).getImage();

    /**
     * whether the event is known to have happened
     */
    private boolean knownToHaveHappened;

    private String description;

    /**
     * need tag-argument constructor for all properties
     */
    public PropertyEvent(String tag) {
        super(tag);
    }

    /**
     * Returns the value of this property
     */
    public String getValue() {
        if (description != null && !description.isEmpty()) {
            return description;
        }
        return knownToHaveHappened ? "Y" : "";
    }

    /**
     * Returns the display value of this property
     */
    public String getDisplayValue() {
        if (description != null && !description.isEmpty() && !knownToHaveHappened) {
            return description;
        }
        return knownToHaveHappened ? resources.getString("prop.event.knwontohavehappened") : "";
    }

    /**
     * Sets the value of this property
     * @param value
     */
    public void setValue(String value) {
        setKnownToHaveHappened(value.toLowerCase().equals("y"));
        description = value;
    }

    /**
     * Returns the list of paths which identify PropertyEvents
     * @param gedcom
     * @return 
     */
    public static TagPath[] getTagPaths(Gedcom gedcom) {
        return gedcom.getGrammar().getAllPaths(null, PropertyEvent.class);
    }

    /**
     * Access - whether this event is known to have happened
     *
     * @return null if this attribute is not supported, true or false otherwise
     */
    public Boolean isKnownToHaveHappened() {
        // patch - no known EVEN
        if (getTag().equals("EVEN")) {
            return null;
        }
        return new Boolean(knownToHaveHappened);
    }

    /**
     * Access - whether this event is known to have happened
     */
    public void setKnownToHaveHappened(boolean set) {
        String old = getValue();
        knownToHaveHappened = set;
        if (set) {
            description = "Y";
        } else {
            description = "";
        }
        propagatePropertyChanged(this, old);
    }


} //PropertyEvent
