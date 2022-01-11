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
package ancestris.gedcom;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.io.Filter;
import genj.util.Resources;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.DateWidget;
import genj.util.swing.TextFieldWidget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * A widget for setting save options (export)
 */
/*package*/ public class SaveOptionsWidget extends JTabbedPane {

    /**
     * components
     */
    private final JCheckBox[] checkEntities = new JCheckBox[Gedcom.ENTITIES.length];
    private JRadioButton checkEntityInclude;
    private JRadioButton checkEntityExclude;
    private TextFieldWidget textEntityTag;
    private JCheckBox[] checkFilters;
    private JTextField textTags, textValues;
    private TextFieldWidget textPassword;
    private JComboBox comboEncodings;
    private JCheckBox checkFilterEmpties, checkFilterLiving, checkMediaDirectory;
    private final Resources resources = Resources.get(this);
    private DateWidget dateEventsAfter, dateBirthsAfter;
    private boolean isGedcom;
    private JCheckBox sort;

    /**
     * filters
     */
    private Filter[] filters;

    /**
     * Constructor.
     *
     * @param gedcom Gedcom File
     */
    /*package*/ public SaveOptionsWidget(Gedcom gedcom) {
        this(gedcom, (Filter[]) null);
    }

    /*package*/ public SaveOptionsWidget(Gedcom gedcom, Collection<? extends Filter> filters) {
        this(gedcom, filters.toArray(new Filter[]{}));
    }

    /*package*/ public SaveOptionsWidget(Filter[] filters) {
        this(null, filters);
    }

    /*package*/ public SaveOptionsWidget(Gedcom gedcom, Filter[] filters) {
        isGedcom = false;
        // Entity filter    
        Box types = new Box(BoxLayout.Y_AXIS);
        for (int t = 0; t < Gedcom.ENTITIES.length; t++) {
            checkEntities[t] = new JCheckBox(Gedcom.getName(Gedcom.ENTITIES[t], true), true);
            types.add(checkEntities[t]);
        }
        types.add(new JLabel(" "));
        types.add(new JLabel(" "));
        ButtonGroup group = new ButtonGroup();
        checkEntityInclude = new JRadioButton(resources.getString("save.options.entities.include"));
        checkEntityInclude.setSelected(true);
        checkEntityExclude = new JRadioButton(resources.getString("save.options.entities.exclude"));
        group.add(checkEntityInclude);
        group.add(checkEntityExclude);
        types.add(checkEntityInclude);
        types.add(checkEntityExclude);
        types.add(new JLabel(resources.getString("save.options.entities.tag")));
        textEntityTag = new TextFieldWidget("");
        textEntityTag.setEditable(true);
        types.add(textEntityTag);

        // Property filter
        Box props = new Box(BoxLayout.Y_AXIS);
        props.add(new JLabel(resources.getString("save.options.exclude.tags")));
        textTags = new TextFieldWidget(resources.getString("save.options.exclude.tags.eg"), 10).setTemplate(true);
        props.add(textTags);
        props.add(new JLabel(resources.getString("save.options.exclude.values")));
        textValues = new TextFieldWidget(resources.getString("save.options.exclude.values.eg"), 10).setTemplate(true);
        props.add(textValues);
        props.add(new JLabel(resources.getString("save.options.exclude.events")));
        dateEventsAfter = new DateWidget();
        props.add(dateEventsAfter);
        props.add(new JLabel(resources.getString("save.options.exclude.indis")));
        dateBirthsAfter = new DateWidget();
        props.add(dateBirthsAfter);
        checkFilterLiving = new JCheckBox(resources.getString("save.options.exclude.living"));
        props.add(checkFilterLiving);
        checkFilterEmpties = new JCheckBox(resources.getString("save.options.exclude.empties"));
        props.add(checkFilterEmpties);

        // View filter
        Box others = new Box(BoxLayout.Y_AXIS);
        this.filters = filters;
        if (filters != null) {
            this.checkFilters = new JCheckBox[filters.length];
            for (int i = 0; i < checkFilters.length; i++) {
                checkFilters[i] = new JCheckBox(filters[i].getFilterName(), false);
                others.add(checkFilters[i]);
            }
        }

        // Allow for files moves to another directory
        int nbFiles = 0;
        Box directories = new Box(BoxLayout.Y_AXIS);
        if (gedcom != null) {
            List<PropertyFile> files = (List<PropertyFile>) gedcom.getPropertiesByClass(PropertyFile.class);
            nbFiles = files.size();
            directories.add(new JLabel(" "));
            checkMediaDirectory = new JCheckBox(resources.getString("save.options.files.check"));
            directories.add(checkMediaDirectory);
            directories.add(new JLabel(" "));
            directories.add(new JLabel(resources.getString("save.options.files.label")));
            directories.add(new JLabel(" "));
        }

        // Hide options tab if gedcom is null (used for other types of file)
        Box options = new Box(BoxLayout.Y_AXIS);
        if (gedcom != null) {
            isGedcom = true;
            // Options
            options.add(new JLabel(" "));
            options.add(new JLabel(resources.getString("save.options.encoding")));
            comboEncodings = new ChoiceWidget(Gedcom.ENCODINGS, Gedcom.UTF8);
            comboEncodings.setEditable(false);
            comboEncodings.setSelectedItem(gedcom.getEncoding());
            options.add(comboEncodings);
            options.add(new JLabel(" "));
            options.add(new JLabel(resources.getString("save.options.password")));
            textPassword = new TextFieldWidget(gedcom.hasPassword() ? gedcom.getPassword() : "", 10);
            textPassword.setEditable(gedcom.getPassword() != Gedcom.PASSWORD_UNKNOWN);
            options.add(textPassword);
            options.add(new JLabel(" "));
            sort = new JCheckBox(resources.getString("save.options.sort"));
            sort.setToolTipText(resources.getString("save.options.sort.tooltip"));
            options.add(sort);
        }

        // layout
        add(resources.getString("save.options.filter.entities"), types);
        add(resources.getString("save.options.filter.views"), new JScrollPane(others));
        add(resources.getString("save.options.filter.properties"), props);
        if (gedcom != null && nbFiles > 0) {
            add(resources.getString("save.options.files"), directories);
        }
        if (gedcom != null) {
            add(resources.getString("save.options"), options);
        }

        // done
    }

    /**
     * Getter.
     *
     * @return if Gedcom is defined
     */
    public boolean isIsGedcom() {
        return isGedcom;
    }

    /**
     * The selected media and source directory
     */
    public boolean areMediaToBeCopied() {
        return checkMediaDirectory.isSelected();
    }

    /**
     * The choosen password
     */
    public String getPassword() {
        return textPassword.getText();
    }

    /**
     * The choosen encoding
     */
    public String getEncoding() {
        return comboEncodings.getSelectedItem().toString();
    }

    /**
     * The choosen filters
     */
    public Collection<Filter> getFilters() {

        // Result
        List<Filter> result = new ArrayList<>(10);

        // create one for the types
        FilterByType fbt = FilterByType.get(checkEntities, checkEntityInclude.isSelected(), textEntityTag.getText().trim());
        if (fbt != null) {
            result.add(fbt);
        }

        // create one for the properties
        FilterProperties fp = FilterProperties.get(textTags.getText(), textValues.getText());
        if (fp != null) {
            result.add(fp);
        }

        // create one for events
        PointInTime eventsAfter = dateEventsAfter.getValue();
        if (eventsAfter != null && eventsAfter.isValid()) {
            result.add(new FilterEventsAfter(eventsAfter));
        }

        // create one for births
        PointInTime birthsAfter = dateBirthsAfter.getValue();
        if (birthsAfter != null && birthsAfter.isValid()) {
            result.add(new FilterIndividualsBornAfter(birthsAfter));
        }

        // create one for living
        if (checkFilterLiving.isSelected()) {
            result.add(new FilterLivingIndividuals());
        }

        // create one for empties
        if (checkFilterEmpties.isSelected()) {
            result.add(new FilterEmpties());
        }

        // create one for every other
        if (filters != null) {
            for (int f = 0; f < filters.length; f++) {
                if (checkFilters[f].isSelected()) {
                    result.add(filters[f]);
                }
            }
        }

        // done
        return result;
    }
    
    public boolean getSort(){
        if (sort != null) {
            return sort.isSelected();
        }
        return false;
    }

    /**
     * Filtering out empty properties
     */
    private static class FilterEmpties implements Filter {

        @Override
        public boolean veto(Property property) {
            for (int i = 0; i < property.getNoOfProperties(); i++) {
                if (!veto(property.getProperty(i))) {
                    return false;
                }
            }
            return property.getValue().trim().length() == 0;
        }

        @Override
        public String getFilterName() {
            return toString();
        }

        @Override
        public boolean veto(Entity entity) {
            return false;
        }

        @Override
        public boolean canApplyTo(Gedcom gedcom) {
            return true;
        }
    }

    /**
     * Filter individuals if born after pit
     */
    private static class FilterIndividualsBornAfter implements Filter {

        private final PointInTime after;

        /**
         * constructor
         */
        private FilterIndividualsBornAfter(PointInTime after) {
            this.after = after;
        }

        /**
         * callback
         */
        @Override
        public boolean veto(Entity entity) {
            if (entity instanceof Indi) {
                Indi indi = (Indi) entity;
                PropertyDate birth = indi.getBirthDate();
                if (birth != null) {
                    return birth.getStart().compareTo(after) >= 0;
                }
            }

            // fine
            return false;
        }

        @Override
        public String getFilterName() {
            return toString();
        }

        @Override
        public boolean veto(Property property) {
            return false;
        }

        @Override
        public boolean canApplyTo(Gedcom gedcom) {
            return true;
        }
    }

    /**
     * Filter not deceased individuals
     */
    private static class FilterLivingIndividuals implements Filter {

        private FilterLivingIndividuals() {
        }

        /**
         * callback
         */
        @Override
        public boolean veto(Entity indi) {
            if (indi instanceof Indi) {
                return !((Indi) indi).isDeceased();
            }

            // fine
            return false;
        }

        @Override
        public String getFilterName() {
            return toString();
        }

        @Override
        public boolean veto(Property property) {
            return false;
        }

        @Override
        public boolean canApplyTo(Gedcom gedcom) {
            return true;
        }
    }

    /**
     * Filter properties if concerning events after pit
     */
    private static class FilterEventsAfter implements Filter {

        private final PointInTime after;

        /**
         * constructor
         */
        private FilterEventsAfter(PointInTime after) {
            this.after = after;
        }

        /**
         * callback
         */
        @Override
        public boolean veto(Property property) {
            PropertyDate when = property.getWhen();
            return !(when == null || when.getStart().compareTo(after) <= 0);
        }

        @Override
        public String getFilterName() {
            return toString();
        }

        @Override
        public boolean veto(Entity entity) {
            return false;
        }

        @Override
        public boolean canApplyTo(Gedcom gedcom) {
            return true;
        }
    }

    /**
     * Filter property by tag/value
     */
    private static class FilterProperties implements Filter {

        /**
         * filter tags
         */
        private final Set tags;

        /**
         * filter paths
         */
        private final Set paths;

        /**
         * filter values
         */
        private final String[] values;

        /**
         * Constructor
         */
        private FilterProperties(Set tags, Set paths, List<String> values) {
            this.tags = tags;
            this.paths = paths;
            this.values = values.toArray(new String[0]);
            // done
        }

        /**
         * Get instance
         */
        protected static FilterProperties get(String sTags, String sValues) {

            // calculate tags
            Set<String> tags = new HashSet<>();
            Set<TagPath> paths = new HashSet<>();

            StringTokenizer tokens = new StringTokenizer(sTags, ",");
            while (tokens.hasMoreTokens()) {
                String s = tokens.nextToken().trim();
                if (s.indexOf(':') > 0) {
                    try {
                        paths.add(new TagPath(s));
                    } catch (IllegalArgumentException e) {
                    }
                } else {
                    tags.add(s);
                }
            }
            // calculate values
            List<String> values = new ArrayList<>();
            tokens = new StringTokenizer(sValues, ",");
            while (tokens.hasMoreTokens()) {
                values.add(tokens.nextToken().trim());
            }

            // done
            return (tags.isEmpty() && paths.isEmpty() && values.isEmpty()) ? null : new FilterProperties(tags, paths, values);
        }

        /**
         * @see genj.io.Filter#accept(genj.gedcom.Property)
         */
        public boolean veto(Property property) {
            // check if tag is applying
            if (tags.contains(property.getTag())) {
                return true;
            }
            // check if path is applying
            if (paths.contains(property.getPath())) {
                return true;
            }
            // simple
            return !accept(property.getValue());
        }

        @Override
        public boolean veto(Entity entity) {
            if (entity instanceof Note) {
                return !accept(entity.getValue());
            }
            return false;
        }

        /**
         * Whether we accept a value
         */
        private boolean accept(String value) {
            if (value == null) {
                return true;
            }
            for (String value1 : values) {
                if (value.contains(value1)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String getFilterName() {
            return toString();
        }

        @Override
        public boolean canApplyTo(Gedcom gedcom) {
            return true;
        }
    } //FilterProperty

    /**
     * Filter by type
     */
    private static class FilterByType implements Filter {

        /**
         * the enabled types
         */
        private Set<String> types = new HashSet<>();
        private String tag = new String();
        private boolean includes = true;
        private Set<Indi> indis = new HashSet<>();

        /**
         * Create an instance
         */
        protected static FilterByType get(JCheckBox[] checks, boolean includes, String tag) {

            FilterByType result = new FilterByType();

            for (int t = 0; t < checks.length; t++) {
                if (checks[t].isSelected()) {
                    result.types.add(Gedcom.ENTITIES[t]);
                }
            }
            result.includes = includes;
            result.tag = tag;
            return (result.types.size() < Gedcom.ENTITIES.length) || (!tag.isEmpty()) ? result : null;
        }

        /**
         * accepting all properties, limit to entities of parameterized types
         *
         * @see genj.io.Filter#accept(genj.gedcom.Property)
         */
        @Override
        public boolean veto(Entity ent) {
            // If entity tag is excluded, veto is true
            if (!types.contains(ent.getTag())) {
                return true;
            }
            // if a tag parameter is given, check if entity is linked to an individual with that tag
            if (!tag.isEmpty()) {
                // indi?
                if (ent instanceof Indi) {
                    return !keepIndi(ent);
                }

                // fam?
                if (ent instanceof Fam) {
                    Fam fam = (Fam) ent;
                    boolean father = keepIndi(fam.getHusband()),
                            mother = keepIndi(fam.getWife()),
                            child = false;
                    Indi[] children = fam.getChildren();
                    for (int i = 0; child == false && i < children.length; i++) {
                        if (keepIndi(children[i])) {
                            child = true;
                        }
                    }
                    // father and mother or parent and child
                    return !((father && mother) || (father && child) || (mother && child));
                }
                // let submitter through if it's THE one
                if (ent.getGedcom().getSubmitter() == ent) {
                    return !includes;
                }
                // maybe a referenced other type?
                List<Entity> allreadyVisited = new ArrayList<>();
                allreadyVisited.add(ent);
                if (includes != checkRecursiveRef(ent, allreadyVisited)) {
                    return !includes;
                }
                
                return true;
            }
            return false;
        }

        private boolean checkRecursiveRef(Entity root, List<Entity> allreadyVisited) {
            Entity[] refs = PropertyXRef.getReferences(root);
            final List<Entity> ents = new ArrayList<>(refs.length);
            for (Entity ref : refs) {
                // Check all Indis before recurse.
                if (ref instanceof Indi) {
                    if (keepIndi(ref)) {
                        return !includes;
                    }
                } else if (!allreadyVisited.contains(ref)){ //avoid to get same value
                    ents.add(ref);
                    allreadyVisited.add(ref);
                }
            }
            for (Entity ref : ents) {
                if (includes != checkRecursiveRef(ref, allreadyVisited)) {
                    return !includes;
                }
            }
            return includes;
        }

        @Override
        public String getFilterName() {
            return toString();
        }

        @Override
        public boolean veto(Property property) {
            return false;
        }

        @Override
        public boolean canApplyTo(Gedcom gedcom) {
            return true;
        }

        private boolean keepIndi(Entity ent) {
            if (ent == null) {
                return false;
            }
            boolean hasTag = !ent.getAllProperties(tag).isEmpty();
            return (hasTag && includes) || (!hasTag && !includes);
        }

    } //FilterByType

} //SaveOptionsWidget
