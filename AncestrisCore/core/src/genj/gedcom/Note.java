/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.gedcom;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Class for encapsulating a note
 */
public class Note extends Entity implements MultiLineProperty {

    /**
     * a delegate for keep the text data crammed in here by Gedcom grammar
     */
    private PropertyMultilineValue delegate;

    /**
     * need tag,id-arguments constructor for all entities
     */
    public Note(String tag, String id) {
        super(tag, id);
        assertTag(Gedcom.NOTE);
    }

    
    /**
     * Note is valid regardless of value
     * @return boolean
     */
    @Override
    public boolean isValid() {
        return true;
    }
    
    @Override
    public void moveEntityValue() {
        // do nothing
    }

    
    
    /**
     * Notification to entity that it has been added to a Gedcom
     */
    /*package*/ void addNotify(Gedcom ged) {

        // continue
        super.addNotify(ged);

        // create a delegate we're using for storing the 
        // note's multiline value
        if (delegate == null) {
            delegate = (PropertyMultilineValue) addProperty("NOTE", "");
            delegate.isTransient = true;
        }

        // done
    }

    /**
     * Returns the delegate multiline value property that actually contains this
     * note's content
     */
    public PropertyMultilineValue getDelegate() {
        // create a delegate we're using for storing the
        // note's multiline value
        if (delegate == null) {
            delegate = (PropertyMultilineValue) addProperty("NOTE", "");
            delegate.isTransient = true;
        }
        return delegate;
    }

    /**
     * Note ...
     */
    @Override
    protected String getToStringPrefix(boolean showIds) {
        return getDelegate().getDisplayValue();
    }

    /**
     * Returns a user-readable note title
     *
     * @return
     */
    @Override
    public String getDisplayTitle() {
        int maxLen = 30;
        String str = getDisplayValue().trim();
        if (!str.isEmpty() && maxLen != 0) {
            int len = str.length();
            if (len > maxLen) {
                int cut = str.indexOf(" ", maxLen);
                if (cut != -1) {
                    str = str.substring(0, cut);
                }
            }
        }
        if (str.isEmpty()) {
            str = getPropertyName();
        }
        return getId() + " - " + str;
    }

    /**
     * @see genj.gedcom.Entity#setValue(java.lang.String)
     */
    @Override
    public void setValue(String newValue) {
        // keep it in delegate
        getDelegate().setValue(newValue);
    }

    /**
     * @see genj.gedcom.Property#delProperty(genj.gedcom.Property)
     */
    @Override
    public void delProperty(Property which) {
        // ignore request unless not delegate
        if (which != getDelegate()) {
            super.delProperty(which);
        }
    }

    /**
     * @see genj.gedcom.Property#addProperty(genj.gedcom.Property, int)
     */
    @Override
    Property addProperty(Property which, int pos) {
        // concatenate value if PropertyMultilineValue NOTE added
        if (which instanceof PropertyMultilineValue && delegate != null && which.getTag().equals(this.getTag())) {
            delegate.setValue(delegate.getValue() + which.getValue());
            return delegate;
        }
        
        return super.addProperty(which, pos);
    }

    /**
     * @see genj.gedcom.Property#getValue()
     */
    public String getValue() {
        return getDelegate().getValue();
    }

    public List<Property> findProperties(Pattern tag, Pattern value) {
        // let super do its thing
        List<Property> result = super.findProperties(tag, value);
        // don't let 'this' in there
        result.remove(this);
        // done
        return result;
    }

    /**
     * @see genj.gedcom.MultiLineProperty#getLineIterator()
     */
    public Iterator getLineIterator() {
        return getDelegate().getLineIterator();
    }

    /**
     * @see genj.gedcom.MultiLineProperty#getLineCollector()
     */
    public Collector getLineCollector() {
        return getDelegate().getLineCollector();
    }

    /**
     * @see genj.gedcom.Property#isPrivate()
     */
    public boolean isPrivate() {
        return getDelegate().isPrivate();
    }

    /**
     * @see genj.gedcom.Property#setPrivate(boolean, boolean)
     */
    public void setPrivate(boolean set, boolean recursively) {
        getDelegate().setPrivate(set, recursively);
    }

} //Note
