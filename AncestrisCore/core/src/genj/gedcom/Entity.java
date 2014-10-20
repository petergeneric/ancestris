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
package genj.gedcom;

/**
 * Abstract base type for all Entities - don't make abstract since we actually
 * instantiate this for entities we don't know
 */
public class Entity extends Property {

    /** the containing gedcom */
    private Gedcom gedcom;

    /** the id */
    private String id;

    /** just in case someone's using a value */
    private String value;

    /**
     * need tag,id-arguments constructor for all entities
     */
    protected Entity(String tag, String id) {
        super(tag);
        this.id = id;
    }

    /**
     * Lifecycle - callback after being added to Gedcom
     */
    /*package*/ void addNotify(Gedcom ged) {
        // remember
        gedcom = ged;
        // propagate change (see Property.addNotify() for motivation why propagate is here)
        ged.propagateEntityAdded(this);
        // done    
    }

    /**
     * Lifecycle - callback before being removed from Gedcom
     */
    /*package*/
    @Override
    void beforeDelNotify() {

        // delete children
        delProperties();

        // propagate change (see addNotify() for motivation why propagate is here)
        gedcom.propagateEntityDeleted(this);

        // forget gedcom
        gedcom = null;

        // done    
    }

    /**
     * Return the last change of this entity (might be null)
     */
    public PropertyChange getLastChange() {
        return (PropertyChange) getProperty("CHAN");
    }

    /**
     * Gedcom this entity's in
     *
     * @return containing Gedcom
     */
    @Override
    public Gedcom getGedcom() {
        return gedcom;
    }

    /**
     * @see genj.gedcom.Property#getEntity()
     */
    @Override
    public Entity getEntity() {
        return this;
    }

    public boolean isConnected() {
        for (PropertyXRef xref : getProperties(PropertyXRef.class)) {
            if (xref.isValid()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Changes an entity's ID
     */
    public void setId(String set) throws GedcomException {

        // change it
        String old = id;
        id = set;

        // tell Gedcom about it
        if (gedcom != null) {
            try {
                gedcom.propagateEntityIDChanged(this, old);
            } catch (Throwable t) {
                id = old;
            }
        }

        // done
    }

    /**
     * Returns entity's id
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * return "unique" id string (may be used as link target or anchor)
     */
    public String getAnchor() {
        return getTag() + "_" + getId();
    }

    /**
     * @see genj.gedcom.Property#toString()
     */
    @Override
    public final String toString() {
        return toString(true);
    }

    public final String toString(boolean showIds) {

        StringBuilder buf = new StringBuilder();
        buf.append(getToStringPrefix(showIds));
        if (buf.length() == 0) {
            buf.append(getPropertyName());
        }
        if (showIds) {
            buf.append(" (");
            buf.append(getId());
            buf.append(')');
        }
        return buf.toString();
    }

    protected String getToStringPrefix(boolean showIds) {
        return getPropertyName();
    }

    /**
     * @see genj.gedcom.Property#getValue()
     */
    @Override
    public String getValue() {
        return value != null ? value : "";
    }

    /**
     * @see genj.gedcom.Property#setValue(java.lang.String)
     */
    @Override
    public void setValue(String set) {
        value = set;
    }

    /**
     * Returns a comparable id
     */
    private int getID() throws NumberFormatException {

        int start = 0;
        int end;

        while (start < id.length() && !Character.isDigit(id.charAt(start))) {
            start++;
        }
        end = start;
        while (end < id.length() && Character.isDigit(id.charAt(end))) {
            end++;
        }

        if (end < start) {
            throw new NumberFormatException();
        }

        return Integer.parseInt(id.substring(start, end));
    }

    /**
     * Format a sub-property of this entity
     *
     * @see Property#format(String)
     */
    public String format(String propertyTag, String format) {
        Property p = getProperty(propertyTag);
        return p != null ? p.format(format) : "";
    }

    /**
     * Propagate changed property
     */
    @Override
    void propagateXRefLinked(PropertyXRef property1, PropertyXRef property2) {
        if (gedcom != null) {
            gedcom.propagateXRefLinked(property1, property2);
        }
    }

    @Override
    void propagateXRefUnlinked(PropertyXRef property1, PropertyXRef property2) {
        if (gedcom != null) {
            gedcom.propagateXRefUnlinked(property1, property2);
        }
    }

    @Override
    void propagatePropertyAdded(Property container, int pos, Property added) {
        if (gedcom != null) {
            gedcom.propagatePropertyAdded(this, container, pos, added);
        }
    }

    @Override
    void propagatePropertyDeleted(Property container, int pos, Property deleted) {
        if (gedcom != null) {
            gedcom.propagatePropertyDeleted(this, container, pos, deleted);
        }
    }

    @Override
    void propagatePropertyChanged(Property property, String oldValue) {
        if (gedcom != null) {
            gedcom.propagatePropertyChanged(this, property, oldValue);
        }
    }

    @Override
    void propagatePropertyMoved(Property property, Property moved, int from, int to) {
        if (gedcom != null) {
            gedcom.propagatePropertyMoved(property, moved, from, to);
        }
    }

    /**
     * return natural ordering comparator for entities.
     * The natural order for entities is by ID;
     *
     * @return
     */
    @Override
    public PropertyComparator2 getComparator() {
        return ENTITYComparator.getInstance();
    }

    /**
     * Entity comparator: compare by id.
     */
    private static class ENTITYComparator extends PropertyComparator2.Default<Entity> {

        private static final ENTITYComparator INSTANCE = new ENTITYComparator();

        public static PropertyComparator2 getInstance() {
            return INSTANCE;
        }

        @Override
        public int compare(Entity i1, Entity i2) {
            int r = compareNull(i1, i2);
            if (r == Integer.MAX_VALUE) {
                try {
                    r = i1.getID() - i2.getID();
                } catch (NumberFormatException e) {
                    r = i1.id.compareToIgnoreCase(i2.id);
                }
            }
            return r;
        }

        @Override
        public String getSortGroup(Entity p) {
            return "";
        }
    }
} //Entity
