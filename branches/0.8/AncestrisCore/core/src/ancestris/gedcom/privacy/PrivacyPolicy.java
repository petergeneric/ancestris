/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2011 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.gedcom.privacy;

import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Delta;
import org.openide.util.Lookup;

/**
 * A filtering scheme applying privacy to Gedcom properties
 * Default to a very simplistic privacy policy where every property
 * is public except those tagged with _PRIV and events within the last 100 years.
 * Is that case the DisplayedValue is "..."
 */
public abstract class PrivacyPolicy {

    private static PrivacyPolicy defaultInstance = null;

    /** filter a value */
    public String getDisplayValue(Property prop) {
        return isPrivate(prop) ? getPrivateMask() : prop.getDisplayValue();
    }

    /** filter a value */
    public String getDisplayValue(Property prop, String tag) {
        if (prop == null) {
            return "";
        }
        prop = prop.getProperty(tag);
        return prop == null ? "" : getDisplayValue(prop);
    }

    /** check for privacy */
    public abstract boolean isPrivate(Property prop);

    /** All props are public accessor */
    public abstract PrivacyPolicy getAllPublic();

    /** All props are private accessor */
    public abstract PrivacyPolicy getAllPrivate();

    /** returns privacy mask text */
    public abstract String getPrivateMask();

    /** Open preference panel for this privacy policy */
    public void openPreferences() {
        // Do nothing
    }

    /** Singleton instance accessor method for privacy manager
     *
     * @return instance of privacy manager installed in the system
     */
    public static PrivacyPolicy getDefault() {
        PrivacyPolicy instance = Lookup.getDefault().lookup(PrivacyPolicy.class);

        return (instance != null) ? instance : getDefaultInstance();
    }

    private static synchronized PrivacyPolicy getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new DefaultPrivacyPolicy();
        }

        return defaultInstance;
    }

    private static class DefaultPrivacyPolicy extends PrivacyPolicy {

        private int yearsInfoIsPrivate = 100;
        private String tagMarkingPrivate = "_PRIV";
        private String mask = "...";
//        private Map<Property, Boolean> isPrivate;
        private static final PrivacyPolicy PUBLIC = new DefaultPrivacyPolicy() {

            @Override
            public boolean isPrivate(Property prop) {
                return false;
            }
        };
        private static final PrivacyPolicy PRIVATE = new DefaultPrivacyPolicy() {

            @Override
            public boolean isPrivate(Property prop) {
                return true;
            }
        };

        @Override
        public PrivacyPolicy getAllPublic() {
            return PUBLIC;
        }

        @Override
        public PrivacyPolicy getAllPrivate() {
            return PRIVATE;
        }

        /**
         * private constructor
         */
        private DefaultPrivacyPolicy() {
        }

        @Override
        public String getPrivateMask() {
            return mask;
        }

        /** check for privacy */
        @Override
        public boolean isPrivate(Property prop) {
            return isPrivate(prop, true);
        }

        /*
         * All optimizing stuff and relatives calculation have been removed
         */
        private boolean isPrivate(Property prop, boolean checkSpouses) {
            if (prop == null) {
                return false;
            }
//            if (isPrivate.containsKey(prop)) {
//                return isPrivate.get(prop);
//            }

            // maybe prop is tagged?
            if (tagMarkingPrivate != null && hasTagMarkingPrivate(prop)) {
                return cacheProp(prop, true);
            }
            // maybe because it's recent?
            if (yearsInfoIsPrivate > 0 && isWithinPrivateYears(prop)) {
                return cacheProp(prop, true);
            }

            // maybe parent is private?
            boolean result = isPrivate(prop.getParent());
            if (result) {
                return cacheProp(prop, true);
            }
//            // maybe ancestors or spouse
//            if (prop instanceof Indi) {
//                if (checkSpouses) {
//                    for (Indi spouse : ((Indi) prop).getPartners()) {
//                        boolean priv = isPrivate(spouse, false);
//                        if (priv) {
//                            return cacheProp(prop, true);
//                        }
//                    }
//                }
//
//                for (Indi parent : ((Indi) prop).getParents()) {
//                    boolean priv = isPrivate(parent);
//                    if (priv) {
//                        return cacheProp(prop, true);
//                    }
//                }
//            }
//            if (prop instanceof Fam) {
//                for (Indi parent : ((Fam) prop).getSpouses()) {
//                    boolean priv = isPrivate(parent);
//                    if (priv) {
//                        return cacheProp(prop, true);
//                    }
//                }
//            }
            return cacheProp(prop, false);
        }

        private boolean cacheProp(Property prop, boolean priv) {
//            isPrivate.put(prop, priv);
            return priv;
        }

//        /** check whether a property belongs to deceased individuals only */
//        private boolean isInfoOfDeceased(Property prop) {
//            // contained in indi? check death-date
//            Entity e = prop.getEntity();
//            if (e instanceof Indi) {
//                return ((Indi) e).isDeceased();
//            }
//
//            // contained in Fam? check husband and wife
//            if (e instanceof Fam) {
//                Indi husband = ((Fam) e).getHusband();
//                if (husband != null && !husband.isDeceased()) {
//                    return false;
//                }
//                Indi wife = ((Fam) e).getWife();
//                return wife != null && wife.isDeceased();
//            }
//
//            // dunno
//            return false;
//        }
        /** check for marked with tag */
        private boolean hasTagMarkingPrivate(Property prop) {
            return getPropertyFor(prop, tagMarkingPrivate, Property.class) != null;
        }

        /** whether a prop is still within the privat years' - only if it has a date sub-property */
        private boolean isWithinPrivateYears(Property prop) {
//            // skip DEAT tag
//            if ("DEAT".equalsIgnoreCase(prop.getTag())) {
//                return false;
//            }
            // check date
            PropertyDate date = (PropertyDate) getPropertyFor(prop, "DATE", PropertyDate.class);
            if (date == null) {
                return false;
            }
            // check anniversary of property's date
            Delta anniversary = date.getAnniversary();
            return anniversary != null && anniversary.getYears() < yearsInfoIsPrivate;
        }

        /** find a sub-property by tag and type */
        private Property getPropertyFor(Property prop, String tag, Class<? extends Property> type) {
            // check children
            for (int i = 0, j = prop.getNoOfProperties(); i < j; i++) {
                Property child = prop.getProperty(i);
                if (is(child, tag, type)) {
                    return child;
                }
            }
            return null;
        }

        private boolean is(Property prop, String tag, Class<? extends Property> type) {
            return prop.getTag().equals(tag) && type.isAssignableFrom(prop.getClass());
        }
    }
}//PrivacyFilter

