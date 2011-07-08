
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.resources;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author dominique
 */
public abstract class ResourceItem {

    /** Property change support */
    private transient PropertyChangeSupport support = new PropertyChangeSupport(this);

    protected ResourceItem() {
    }

    protected final void firePropertyChange(String name, Object o, Object n) {
        support.firePropertyChange(name, o, n);
    }

    /** Adds property listener */
    public void addPropertyChangeListener(PropertyChangeListener changeListener) {
        support.addPropertyChangeListener(changeListener);
    }

    /** Removes property listener */
    public void removePropertyChangeListener(PropertyChangeListener changeListener) {
        support.removePropertyChangeListener(changeListener);
    }

    /** General class for basic elements, which contain value directly. */
    private static abstract class Basic extends ResourceItem {

        /* Parsed value of the element */
        protected String value;

        public Basic(String value) {
            super();
            this.value = value;
        }

        /*
         * Get a value of the element.
         * @return the Java string (no escaping)
         */
        public String getValue() {
            return value;
        }

        /*
         * Sets the value. Does not check if the value has changed.
         * The value is immediately propadated in text Document possibly
         * triggering DocumentEvents.
         * @param value Java string (no escaping)
         */
        public void setValue(String value) {
            this.value = value;
        }

        /** Get a string representation of the key for printing. Treats the '=' sign as a part of the key
         * @return the string
         */
        abstract public String getStringValue();
    }

    /** Class representing key element in properties file. */
    public static class PropertyKey extends Basic {

        public PropertyKey(String value) {
            super(value);
        }

        /** Get a string representation of the key for printing. Treats the '=' sign as a part of the key
         * @return the string
         */
        @Override
        public String getStringValue() {
            return value + "=";                     //NOI18N
        }
    }

    /** Class representing value element in properties files. */
    public static class PropertyValue extends Basic {

        public PropertyValue(String value) {
            super(value);
        }

        @Override
        public String getStringValue() {
            return value;
        }
    }

    public static class PropertyComment extends Basic {

        public PropertyComment(String value) {
            super(value);
        }

        @Override
        public String getStringValue() {
            String comment = "";
            for (String line : value.split( "\\r?\\n")) {
                comment += "# " + line + "\n";
            }
            return comment;
        }
    }

    public static class ResourceLine extends ResourceItem {

        /** Name of the Key property */
        public static final String PROP_ITEM_KEY = "key"; // NOI18N
        /** Name of the Value property */
        public static final String PROP_ITEM_VALUE = "value"; // NOI18N
        /** Name of the Comment property */
        public static final String PROP_ITEM_COMMENT = "comment"; // NOI18N
        /* Key element.  */
        private PropertyKey key = null;
        /* Value element. */
        private PropertyValue value = null;
        /* Comment element. */
        private PropertyComment comment = null;

        public ResourceLine(PropertyKey key, PropertyValue value, PropertyComment comment) {
            this.key = key;
            this.value = value;
            this.comment = comment;
        }

        /** Get a value string of the element.
         * @return the string
         */
        public String getResourceLine() {
            return (comment == null) ? "" : comment.getStringValue() + "\n" + // NOI18N
                    ((key == null) ? "" : key.getStringValue()) + // NOI18N
                    ((value == null) ? "" : value.getStringValue()) + "\n"; // NOI18N
        }

        /** Returns the key element for this item. */
        public PropertyKey getPropertyKey() {
            return key;
        }

        /** Get a key by which to identify this record
         * @return nonescaped key
         */
        public String getKey() {
            return (key == null) ? null : key.getValue();
        }

        /** Returns the value element for this item. */
        public PropertyValue getPropertyValue() {
            return value;
        }

        /** Get the value of this item */
        public String getValue() {
            return (value == null) ? null : value.getValue();
        }

        /** Returns the comment element for this item. */
        public PropertyComment getPropertyComment() {
            return comment;
        }

        /** Get the comment for this item */
        public String getComment() {
            return (comment == null) ? null : comment.getStringValue ();
        }
    }
}
