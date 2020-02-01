/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.renderer.velocity;

import ancestris.core.TextOptions;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAge;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;

/**
 *
 * @author daniel
 */
public class PropertyWrapper extends Object implements Comparable<PropertyWrapper> {
    Property property;
    static final TextOptions OPTIONS = TextOptions.getInstance();


    PropertyWrapper(Property prop) {
        property = prop;
    }

    // Factory for PropertyWrapper
    static PropertyWrapper create(Property p) {
        if (p == null) {
            return null;
        }
        if (p instanceof Indi) {
            return new IndiWrapper((Indi) p);
        }
        if (p instanceof Fam) {
            return new FamWrapper((Fam) p);
        }
        if (p instanceof PropertyAge) {
            return new PropertyAgeWrapper((PropertyAge) p);
        }
        if (p instanceof PropertyXRef) {
            return new PropertyXRefWrapper((PropertyXRef) p);
        }
        if (p instanceof PropertyPlace) {
            return new PropertyPlaceWrapper((PropertyPlace) p);
        }
        if (p instanceof PropertyFile) {
            return new PropertyFileWrapper((PropertyFile) p);
        }
        if (p instanceof PropertyName) {
            return new PropertyNameWrapper((PropertyName) p);
        }
        return new PropertyWrapper(p);
    }

    @Override
    public int compareTo(PropertyWrapper o) {
        return property.compareTo(o.property);
    }
    
    public PropertyWrapper getProperty(String tagPath) {
        if (property == null) {
            return null;
        }
        Property subProp = property.getPropertyByPath(property.getTag() + ":" + tagPath);
        return create(subProp);
    }

    public PropertyWrapper getParent(){
            if (property == null) return null;
        return create(property.getParent());
    }

    // Shortcut for getProperty so that $indi.name is equivalent to $indi.getProperty("NAME")
    public Object get(String tag) {
        return getProperty(tag.toUpperCase());
    }

    public PropertyWrapper[] getProperties(String tagPath) {
        Property[] props = property.getProperties(new TagPath(property.getTag() + ":" + tagPath));
        if (props.length == 0) {
            return null;
        }
        PropertyWrapper[] reportProps = new PropertyWrapper[props.length];
        for (int i = 0; i < props.length; i++) {
            reportProps[i] = create(props[i]);
        }
        return reportProps;
    }

    public String getPath() {
        return property.getPath().toString();
    }

    public String getDate() {
        return format("{$D}");
    }

    public String getValue() {
        // Don't use format to get DisplayValue as format may be overriden (see PropertyPlace)
        // FIXME: We should specify that format({$v} must always return the DisplayValue
        return (property == null) ? "" : property.getDisplayValue();
    }

    public String getName() {
        String str = property.getPropertyName();
        int i = str.indexOf(' ');
        if (i>0) {
            return str.substring(0, i);
        } else {
            return str;
        }
    }

    public String getPlace() {
        return format("{$P}");
    }

    public String toString() {
        return getValue();
    }

    /**
     * @param fmtstr
     * {$t} property tag (doesn't count as matched)
     * {$T} property name(doesn't count as matched)
     * {$D} date as fully localized string
     * {$y} year
     * {$p} place (city)
     * {$P} place (all jurisdictions)
     * {$V} value
     * {$v} display value
     *
     * @return
     */
    public String format(String fmtstr) {
        return (property == null) ? "" : property.format(fmtstr);
    }

}
