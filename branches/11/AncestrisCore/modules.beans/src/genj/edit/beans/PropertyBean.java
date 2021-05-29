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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.edit.beans;

import ancestris.util.TimingUtility;
import genj.gedcom.Entity;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyAge;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyChoiceValue;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMultilineValue;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyQuality;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.renderer.BlueprintManager;
import genj.renderer.BlueprintRenderer;
import genj.util.ChangeSupport;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.ViewContext;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

/**
 * Beans allow the user to edit gedcom properties
 */
public abstract class PropertyBean extends JPanel {

    protected final static Resources RESOURCES = Resources.get(PropertyBean.class);
    protected final static Logger LOG = Logger.getLogger("ancestris.edit.beans");
    protected final static Registry REGISTRY = Registry.get(PropertyBean.class);
    private final static Class<?>[] PROPERTY2BEANTYPE = { // TODO beans could be resolved dynamically to allow plugin overrides
        Entity.class, EntityBean.class,
        PropertyQuality.class, QualityBean.class,
        PropertyPlace.class, PlaceBean.class, // before choice!
        PropertyAge.class, AgeBean.class,
        PropertyChoiceValue.class, ChoiceBean.class,
        PropertyDate.class, DateBean.class,
        PropertyEvent.class, EventBean.class,
        PropertyFile.class, FileBean.class,
        PropertyBlob.class, FileBean.class,
        PropertyMultilineValue.class, MLEBean.class,
        PropertyName.class, NameBean.class,
        PropertySex.class, SexBean.class,
        PropertyXRef.class, XRefBean.class,
        Property.class, SimpleValueBean.class // last!
    };
    /** the context to edit */
    protected Property root;
    protected TagPath path;
    protected Property property;
    protected List<? extends PropertyBean> session;
    /** the default focus */
    protected JComponent defaultFocus = null;
    /** change support */
    protected ChangeSupport changeSupport = new ChangeSupport(this);

    /* Default tag value is null if not explicitly set */
    private String tag = null;

    /**
     * Lookup
     */
    @SuppressWarnings("unchecked")
    public static PropertyBean getBean(Class<? extends Property> property) {

        for (int i = 0; i < PROPERTY2BEANTYPE.length; i += 2) {
            if (PROPERTY2BEANTYPE[i] != null && PROPERTY2BEANTYPE[i].isAssignableFrom(property)) {
                return getBeanImpl((Class<? extends PropertyBean>) PROPERTY2BEANTYPE[i + 1]);
            }
        }

        LOG.log(Level.WARNING, "Can''t find declared bean for property type {0})", property.getName());
        return getBeanImpl(SimpleValueBean.class);
    }

    @SuppressWarnings("unchecked")
    public static PropertyBean getBean(String bean) {
        try {
            return getBeanImpl((Class<? extends PropertyBean>) Class.forName(bean));
        } catch (ClassNotFoundException e) {
            LOG.log(Level.FINE, "Can't find desired bean " + bean, e);
            return getBeanImpl(SimpleValueBean.class);
        }
    }

    private static PropertyBean getBeanImpl(Class<? extends PropertyBean> clazz) {
        try {
            return ((PropertyBean) clazz.newInstance());
        } catch (Throwable t) {
            LOG.log(Level.FINE, "Problem with bean lookup " + clazz.getName(), t);
            return new SimpleValueBean();
        }
    }

    /**
     * recycle an unused bean
     */
    public static void recycle(PropertyBean bean) {

        // safety check - still in use?
        if (bean.getParent() != null) {
            throw new IllegalArgumentException("bean still has parent");
        }

        // clear state (gc)
        bean.root = null;
        bean.path = null;
        bean.property = null;
        bean.session = null;
    }

    /** constructor */
    protected PropertyBean() {
        setOpaque(false);
    }

    /**
     * tell bean to prefer the horizontal instead of the vertical
     */
    public void setPreferHorizontal(boolean set) {
        // bean dependent
    }

    /**
     * Get default TAG for this bean
     * if no default tag: returns null
     */
    public String getTag() {
        return tag;
    }

    /**
     * Sets default ag value
     * @param tag
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * set property to look at
     */
    public final PropertyBean setContext(Property property) {
        return setContext(property, new TagPath("."), property, new ArrayList<PropertyBean>());
    }

    /**
     * set property to look at (equivalent to setContext(root,path,root.getProperty(path))
     * @param root the root property. Must not be null
     * @param path the path of the property to look at. If null, the default path is used when available.
     * @see genj.edit.beans.PropertyBean#setContext(genj.gedcom.Property, genj.gedcom.TagPath, genj.gedcom.Property)
     */
    public final PropertyBean setContext(Property root, TagPath path) {
        if (path == null && getTag() != null) {
            path = TagPath.valueOf(".:" + getTag());
        }

        if (root == null || path == null) {
            throw new IllegalArgumentException("root and path cannot be null");
        }

        return setContext(root, path, root.getProperty(path));
    }

    /**
     * Set property to look at. The property is defined as:
     * parent.getProperty(path) where parent is root.getProperty(parentPath)
     * if parent is null, the two paths are concatained and the result is equivalent to
     * setContext(Property root, TagPath parentPath+path, null);
     * <br/>If parent is not null, result is equivalent to
     * setContext(Property parent, TagPath path, property);
     * @param root
     * @param parentPath
     * @param parent
     * @param path: property path from parent or default tag returned by getTag if null
     * @return
     */
    public final PropertyBean setContext(Property root, TagPath parentPath, Property parent, String relativePath) {
        if (relativePath == null) {
            relativePath = getTag();
        }
        if (parent == null) {
            if (relativePath == null || parentPath == null) {
                throw new IllegalArgumentException("path cannot be null");
            }
            return setContext(root, TagPath.valueOf(parentPath.toString() + ":" + relativePath), null);
        } else {
            return setContext(parent, TagPath.valueOf(".:" + relativePath));
        }
    }

    /**
     * set property to look at. This method may be overiden.
     * @param root the root property. Must not be null
     * @param path the path of the property to look at. If null, the default path is used when available.
     * This case must be handled by overiding class. If not defined or no overiding class is defined,
     * a null path is not allowed.
     * @param property the property for this bean. if null, a new property will be created on commit
     * @see genj.edit.beans.PropertyBean#setContext(genj.gedcom.Property, genj.gedcom.TagPath, genj.gedcom.Property)
     */
    public final PropertyBean setContext(Property root, TagPath path, Property property) {
        return setContext(root, path, property, new ArrayList<PropertyBean>());
    }

    /**
     * // FIXME: I don't see where session is used. Should we remove it?
     * set property to look at
     */
    public final PropertyBean setContext(Property root, TagPath path, Property property, List<PropertyBean> session) {

        if (path == null && getTag() != null) {
            path = TagPath.valueOf(".:" + getTag());
        }

        if (root == null || path == null) {
            throw new IllegalArgumentException("root and path cannot be null");
        }

        this.root = root;
        this.path = path;
        this.property = property;
        this.session = session;

        LOG.log(Level.FINER, "{0}: setPropertyImpl {1}", new Object[]{TimingUtility.getInstance().getTime(),this.getClass().getCanonicalName()});
        setPropertyImpl(property);

        changeSupport.setChanged(false);

        return this;
    }

    protected abstract void setPropertyImpl(Property prop);

    /**
     * ContextProvider callback
     */
    public ViewContext getContext() {
        // ok, this is tricky since some beans might not
        // want to expose a property (is null) and the one
        // we're looking at might actually not be part of
        // an entity yet - no context in those cases
        // (otherwise other code that relies on properties being
        // part of an entity might break)
        return property == null || property.getEntity() == null ? null : new ViewContext(property);
    }

    /**
     * Current Root
     */
    public final Property getRoot() {
        return root;
    }

    /**
     * Current Path
     */
    public final TagPath getPath() {
        return path;
    }

    /**
     * Current Property
     */
    public final Property getProperty() {
        return property;
    }

    /**
     * Whether the bean is valid and can be committed as of current state
     */
    public boolean isCommittable() {
        return true;
    }

    /**
     * Whether the bean has changed since first listener was attached
     */
    public boolean hasChanged() {
        return changeSupport.hasChanged();
    }

    /**
     * Listener
     */
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    /**
     * Listener
     */
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    /**
     * Commit any changes made by the user
     */
    public final void commit() throws GedcomException {
        if (!hasChanged()) {
            return;
        }
        // still need target?
        if (property == null) {
            property = root.setValue(path, "");
        }
        // let impl do its thing
        commitImpl(property);
        // clear changed
        changeSupport.setChanged(false);
        // nothing more
    }

    protected abstract void commitImpl(Property property) throws GedcomException;

    /**
     * Editable? default is yes
     */
    public boolean isEditable() {
        return true;
    }

    /**
     * overridden requestFocusInWindow()
     */
    @Override
    public boolean requestFocusInWindow() {
        // delegate to default focus
        if (defaultFocus != null) {
            return defaultFocus.requestFocusInWindow();
        }
        return false;
    }

    /**
     * overridden requestFocus()
     */
    @Override
    public void requestFocus() {
        // delegate to default focus
        if (defaultFocus != null) {
            defaultFocus.requestFocus();
        } else {
            super.requestFocus();
        }
    }

    /**
     * Provide available actions
     */
    public List<? extends Action> getActions() {
        return new ArrayList<Action>();
    }

    /**
     * A preview component using EntityRenderer for an entity
     */
    public static class Preview extends JComponent {

        /** entity */
        private Entity entity;
        /** the blueprint renderer we're using */
        private BlueprintRenderer renderer;

        /**
         * Constructor
         */
        protected Preview() {
            setBorder(new EmptyBorder(4, 4, 4, 4));
        }

        /**
         * @see genj.edit.ProxyXRef.Content#paintComponent(java.awt.Graphics)
         */
        @Override
        protected void paintComponent(Graphics g) {
            Insets insets = getInsets();
            Rectangle box = new Rectangle(insets.left, insets.top, getWidth() - insets.left - insets.right, getHeight() - insets.top - insets.bottom);
            // clear background
            g.setColor(Color.WHITE);
            g.fillRect(box.x, box.y, box.width, box.height);
            // render entity
            if (renderer != null && entity != null) {
                renderer.render(g, entity, box);
            }
            // done
        }

        protected void setEntity(Entity ent) {
            entity = ent;
            if (entity != null) {
                renderer = new BlueprintRenderer(BlueprintManager.getInstance().getBlueprint(entity.getTag(), REGISTRY.get("blueprint.entity" + entity.getTag(), "")));
            }
            repaint();
        }

        protected Entity getEntity() {
            return entity;
        }
    } //Preview
} //Proxy

