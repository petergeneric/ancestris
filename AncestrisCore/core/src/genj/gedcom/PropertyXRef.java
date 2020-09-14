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

import genj.util.swing.ImageIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Gedcom Property : ABC
 * Class for encapsulating a property that describes a Reference to an entity
 *
 * @author Nils Meier
 * @version 0.1 04/29/98
 */
public abstract class PropertyXRef extends Property {

    /** the target property that this xref references */
    private PropertyXRef target = null;

    /** the value for a broken xref */
    private String value = "";

    /**
     * Empty Constructor
     */
    protected PropertyXRef(String tag) {
        super(tag);
    }

    /**
     * Method for notifying being removed from another parent
     */
    @Override
    /*package*/ void beforeDelNotify() {

        // are we referencing something that points back?
        if (target != null) {
            PropertyXRef other = target;
            Property pother = other.getParent();
            unlink();

            // delete target as well
            pother.delProperty(other);
        }

        // Let it through
        super.beforeDelNotify();

        // done
    }

    /**
     * Returns the entity this reference points to
     *
     * @return entity this property links to
     */
    public Entity getTargetEntity() {
        return target == null ? null : target.getEntity();
    }

    /**
     * Returns the parent
     */
    public Property getTargetParent() {
        return target == null ? null : target.getParent();
    }

    /**
     * Tries to find candidate entity to link to
     */
    protected Entity getCandidate() throws GedcomException {
        // no good if already linked
        if (target != null) {
            throw new IllegalArgumentException("Already linked");
        }

        Entity entity = getGedcom().getEntity(getTargetType(), value);
        if (entity == null) // Can't find {0} {1} ({2} in {3})
        {
            for (Entity ents : getGedcom().getEntities()) {
                if (value.equals(ents.getId())) {
                    return ents;
                }
            }
            throw new GedcomException(resources.getString("error.notfound", Gedcom.getName(getTargetType()), value));
        }
        return entity;
    }

    /**
     * Checks whether an entity is a candidate to link to
     */
    protected boolean isCandidate(Entity entity) {
        // can't be linked yet
        if (target != null) {
            return false;
        }
        // if it's an empty id or the entity's id matches
        return value.length() == 0 || entity.getId().equals(value);
    }

    /**
     * Returns the value of this property as string.
     *
     * @return value of this property as <code>String</code>
     */
    @Override
    public String getValue() {
        return target != null ? '@' + target.getEntity().getId() + '@' : '@' + value + '@';
    }

    /**
     * Returns <b>true</b> if this property is valid
     *
     * @return <code>boolean</code> true or false
     */
    @Override
    public boolean isValid() {
        return target != null;
    }

    /**
     * Links reference to entity (if not already done)
     *
     * @exception GedcomException when processing link would result in inconsistent state
     */
    public abstract void link() throws GedcomException;

    /**
     * links to other xref
     */
    protected void link(PropertyXRef target) {
        if (this.target != null) {
            throw new IllegalArgumentException("can't link while target!=null");
        }
        if (target == null) {
            throw new IllegalArgumentException("can't link to targe null");
        }
        this.target = target;
        target.target = this;
        propagateXRefLinked(this, target);
    }

    /**
     * Unlinks from other xref
     */
    public void unlink() {
        if (target == null) {
            throw new IllegalArgumentException("can't unlink without target");
        }
        PropertyXRef old = target;
        target.target = null;
        target = null;
        propagateXRefUnlinked(this, old);
    }

    /**
     * @see genj.gedcom.Property#getDisplayValue()
     */
    @Override
    public String getDisplayValue() {
        if (target == null) {
            return getValue();
        }
        return target.getEntity().toString();
    }

    /**
     * Returns a display value to be shown on the foreign end - this
     * does really apply only to references that don't come with an
     * explicit back-reference like ASSO, NOTE and OBJE.
     * In case like those the PropertyForeignXRef will ask back here
     * to display something meaningful (e.g. nicely overwritten in
     * PropertyAssociation "Witness: John Doe")
     */
    protected String getForeignDisplayValue() {
        Entity entity = getEntity();
        Property parent = getParent();
        String by = parent != entity ? entity.toString() + " - " + parent.getPropertyName() : entity.toString();
        return resources.getString("foreign.xref", by);
    }

    /**
     * Returns this reference's target
     *
     * @return target or null
     */
    public PropertyXRef getTarget() {
        return target;
    }

    /**
     * Sets this property's value as string.
     */
    @Override
    public void setValue(String set) {

        // ignore if linked
        if (target != null) {
            return;
        }

        // 20070128 don't bother with calculating old if this is happening in init()
        String old = getParent() == null ? null : getValue();

        // remember value
        value = set.replace('@', ' ').trim();

        // remember change
        if (old != null) {
            propagatePropertyChanged(this, old);
        }

        // done
    }

    /**
     * This property as a verbose string
     */
    @Override
    public String toString() {
        Entity e = getTargetEntity();
        if (e == null) {
            return super.toString();
        }
        return e.toString();
    }

    /**
     * The expected referenced type
     */
    public abstract String getTargetType();

    /**
     * @see genj.gedcom.Property#getDeleteVeto()
     */
    @Override
    public String getDeleteVeto() {
        // warn if linked
        if (getTargetEntity() == null) {
            return null;
        }
        // a specialized message?
        String key = "prop." + getTag().toLowerCase() + ".veto";
        String result = resources.getString(key, false);
        // fallback to default
        if (result == null) {
            result = resources.getString("prop.xref.veto");
        }
        return result;
    }

    /**
     * Return all entities that are connected to the given entity through
     * a PropertyXRef
     */
    public static Entity[] getReferences(Entity ent) {
        List<Entity> result = new ArrayList<Entity>(10);
        // loop through pxrefs
        List<PropertyXRef> ps = ent.getProperties(PropertyXRef.class);
        for (PropertyXRef px : ps) {
            Property target = px.getTarget();
            if (target != null) {
                result.add(target.getEntity());
            }
        }
        // done
        return result.toArray(new Entity[result.size()]);
    }

    /**
     * Final impl for image of xrefs
     *
     * @see genj.gedcom.Property#getImage(boolean)
     */
    @Override
    public ImageIcon getImage(boolean checkValid) {
        return overlay(super.getImage(false));
    }

    /**
     * Overlay image with current status
     */
    protected ImageIcon overlay(ImageIcon img) {
        ImageIcon overlay = target != null ? MetaProperty.IMG_LINK : MetaProperty.IMG_ERROR;
        return img.getOverLayed(overlay);
    }

    /**
     * Patched to now allow private - would require that
     * the opposite link is made private, too
     *
     * @see genj.gedcom.Property#setPrivate(boolean, boolean)
     */
    @Override
    public void setPrivate(boolean set, boolean recursively) {
        // ignored
    }

    /**
     * Comparison based on target
     */
    @Override
    public PropertyComparator2 getComparator() {
        return XREFComparator.getInstance();
    }

    private static class XREFComparator extends PropertyComparator2.Default<PropertyXRef> {

        private static final XREFComparator INSTANCE = new XREFComparator();

        public static PropertyComparator2 getInstance() {
            return INSTANCE;
        }

        @Override
        public String getSortGroup(PropertyXRef p) {
            Entity e = p.getTargetEntity();
            if (e == null) {
                return "";
            } else {
                return e.getDisplayComparator().getSortGroup(e);
            }
        }

        /**
         * Compare target entities.
         *
         * @param p1
         * @param p2
         *
         * @return
         */
        @Override
        public int compare(PropertyXRef p1, PropertyXRef p2) {
            int r = compareNull(p1, p2);
            if (r != Integer.MAX_VALUE) {
                return r;
            }
            Entity e1 = p1.getTargetEntity(), e2 = p2.getTargetEntity();
            r = compareNull(e1, e2);
            if (r != Integer.MAX_VALUE) {
                return r;
            }
            if (e1.getClass() != e2.getClass()) {
                return e1.toString().compareTo(e2.toString());
            }
            return e1.getDisplayComparator().compare(e1, e2);
        }
    }

} //PropertyXRef
