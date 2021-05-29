package ancestris.reports.gedart;

import ancestris.core.TextOptions;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAge;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.io.InputSource;
import genj.io.input.FileInput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.ListTool;
import org.apache.velocity.tools.generic.SortTool;
import org.openide.modules.Places;

// FIXME: refactor this class, remove @suppresswarning
@SuppressWarnings("unchecked")
public class DocReport {

    private VelocityContext context;
    private static VelocityEngine engine = new VelocityEngine();
    private Writer out;
    public Charset CHARSET;
    private static final File TEMPLATE_DIR = Places.getUserDirectory();

    DocReport(GedartTemplates template, String encoding) {
        try {
            CHARSET = Charset.forName(encoding);
        } catch (Exception e) {
            CHARSET = Charset.forName("ISO-8859-1");
        }

        try {
            engine.setProperty("resource.loader", "file,class");

            engine.setProperty("file.resource.loader.path", TEMPLATE_DIR.getPath());
            engine.setProperty("file.resource.loader.cache", "true");

            engine.setProperty("directive.set.null.allowed", "true");
            // TODO: pour ne pas interpoller {$v} ... il faudrait mettre false
            // TODO: Mais pour #parse("$TEMPLATE/...") il faudrait mettre true
            engine.setProperty("runtime.interpolate.string.literals", "true");
            engine.init();

        } catch (Exception e) {
            System.out.println("Problem initializing Velocity : " + e);
        }
        restart();
    }

    DocReport(File file, GedartTemplates template, String encoding) throws IOException {
        this(template, encoding);

        // open output stream
        out = new OutputStreamWriter(
                new FileOutputStream(file), CHARSET);
    }

    void restart() {
        context = new VelocityContext();
        context.put("gedcom", new Gedcom());
        context.put("list", new ListTool());
        context.put("sorter", new SortTool());
        context.put("date", new DateTool());
        context.put("docindex", new reportIndex());
        context.put("null", null);
        context.put("encoding", CHARSET);
    }

    void put(String key, Object o) {
        context.put(key, o);
    }

    void put(String key, Gedcom e) {
        context.put(key, new reportGedcom(e));
    }

    void put(String key, ReportGedart r) {
        put(key, new reportOptions(r));
    }
    // TODO: rf = reportProperty.create(f);

    void put(String key, Fam f) {
        reportFam rf = null;
        if (f != null) {
            rf = new reportFam(f);
        }
        put(key, rf);
    }

    void put(String key, Indi i) {
        context.put(key, new reportIndi(i));
    }

    void put(String key, Entity[] entities) {
        if (entities.length == 0) {
            put(key, new Object[0]);
        } else if (entities[0] instanceof Indi) {
            reportIndi[] reportIndis = new reportIndi[entities.length];
            for (int i = 0; i < entities.length; i++) {
                reportIndis[i] = new reportIndi((Indi) entities[i]);
            }
            put(key, reportIndis);
        } else if (entities[0] instanceof Fam) {
            reportFam[] reportFams = new reportFam[entities.length];
            for (int i = 0; i < entities.length; i++) {
                reportFams[i] = new reportFam((Fam) entities[i]);
            }
            put(key, reportFams);
        } else {
            put(key, (Object) null);
        }
    }

    void render(String template) {
        //StringWriter w = new StringWriter();
        try {
            engine.mergeTemplate(template, "ISO-8859-1",
                    context, out);
        } catch (Exception ee) {
        }
//		out.print( w.toString());
        restart();
    }

    void close() {
        try {
            out.flush();
            out.close();
        } catch (Exception e) {
        }
    }

    public class reportIndex {

        private Map index2primary2secondary2elements = new TreeMap();

        private String getKey(Entity e, String tagPath) {
            Property p;
            String tp = tagPath;
            boolean isFirst = false;
            boolean isLast = false;

            isFirst = tp.endsWith("NAME:FIRST");
            isLast = tp.endsWith("NAME:LAST");
            if (isFirst || isLast) {
                tp = tp.substring(0, tp.lastIndexOf(':'));
            }
            p = e.getPropertyByPath(e.getTag() + ":" + tp);
            if (p == null) {
                return "?";
            }
            String result;
            if (isFirst) {
                result = ((PropertyName) p).getFirstName().split(" ", 1)[0];
            } else if (isLast) {
                result = ((PropertyName) p).getLastName();
            } else {
                result = p.toString();
            }
            if (result.length() == 0) {
                return "?";
            }
            return result;
        }

        public Map buildIndexes(reportEntity[] entities, String tagpathPrimary, String tagpathSecondary) {
            index2primary2secondary2elements = new TreeMap();
            return addIndexes(entities, tagpathPrimary, tagpathSecondary);
        }

        public Map addIndexes(reportEntity[] entities, String tagpathPrimary, String tagpathSecondary) {
            for (int i = 0; i < entities.length; i++) {
                Entity e = (Entity) entities[i].property;
                String prim = getKey(e, tagpathPrimary);
                String sec = getKey(e, tagpathSecondary);
                addIndexTerm(prim.substring(0, 1), prim, sec, e);
            }
            return index2primary2secondary2elements;
        }

        /**
         * Add an index entry
         */
        public void addIndexTerm(String index, String primary, Entity entity) {
            addIndexTerm(index, primary, "", entity);
        }

        /**
         * Add an index entry
         */
        public void addIndexTerm(String index, String primary, String secondary, Entity entity) {

            // check index
            if (index == null) {
                return;
            }
            index = index.trim();
            if (index.length() == 0) {
                return;
            }

            // check primary - ignore indexterm if empty
            primary = trimIndexTerm(primary);
            if (primary.length() == 0) {
                return;
            }

            // check secondary
            secondary = trimIndexTerm(secondary);
            if (secondary.length() == 0) {
                return;
            }

            // remember
            Map primary2secondary2elements = (Map) index2primary2secondary2elements.get(index);
            if (primary2secondary2elements == null) {
                primary2secondary2elements = new TreeMap();
                index2primary2secondary2elements.put(index, primary2secondary2elements);
            }
            Map secondary2elements = (Map) primary2secondary2elements.get(primary);
            if (secondary2elements == null) {
                secondary2elements = new TreeMap();
                primary2secondary2elements.put(primary, secondary2elements);
            }
            List elements = (List) secondary2elements.get(secondary);
            if (elements == null) {
                elements = new ArrayList();
                secondary2elements.put(secondary, elements);
            }

            // remember the element for primary+secondary if the element isn't in there already
            if (!elements.contains(entity)) {
                elements.add(entity);
            }
        }

        private String trimIndexTerm(String term) {
            // null?
            if (term == null) {
                return "";
            }
            // remove anything after (
            int bracket = term.indexOf('(');
            if (bracket >= 0) {
                term = term.substring(0, bracket);
            }
            // remove anything after ,
            int comma = term.indexOf('(');
            if (comma >= 0) {
                term = term.substring(0, comma);
            }
            // trim
            return term.trim();
        }

    }

    public class reportFam extends reportEntity {

        private Indi refIndi = null;

        reportFam(Fam f, Indi indi) {
            this(f);
            refIndi = indi;
        }

        reportFam(Fam f) {
            super(f);
        }

        public reportProperty getOtherSpouse() {
            if (refIndi == null) {
                return null;
            }
            Indi i = ((Fam) property).getOtherSpouse(refIndi);
            return create(i);
        }

        public String getShortValue() {
            return toString();
        }

        public String toString() {
            // Might be null
            if (property == null) {
                return "";
            }
            return property.toString() + ((Entity) property).format("MARR", "{ le $D}{ � $P}");
        }

        public reportProperty[] getChildren() {
            Indi[] ch = ((Fam) property).getChildren();
            reportProperty[] result = new reportProperty[ch.length];
            for (int c = 0; c < ch.length; c++) {
                result[c] = create(ch[c]);
            }
            return result;
        }

        public reportProperty getHusband() {
            Indi i = ((Fam) property).getHusband();
            return create(i);
        }

        public reportProperty getWife() {
            Indi i = ((Fam) property).getWife();
            return create(i);
        }
    }

    public class reportIndi extends reportEntity {

        reportIndi(Indi i) {
            super(i);
        }

        public String getShortValue() {
            return toString();
        }

//TODO: a inclure
// N'est utilise que dans Fam.toString ou Indi.toString
//		private String cleanID(String s) {
//			if (!showID)
//				s.replaceAll(" \\([IF]\\d+\\)", "");
//			return s;
//		}
        public String toString() {
            // Might be null
            if (property == null) {
                return "";
            }
            String birth = ((Entity) property).format("BIRT", OPTIONS.getBirthSymbol() + " {$V }{$D}{ $P}");
            String death = ((Entity) property).format("DEAT", OPTIONS.getDeathSymbol() + " {$V }{$D}{ $P}");
            return property.toString() + " " + birth + " " + death;
        }

        public String getString(String male, String female, String unknown) {
            if (((Indi) property).getSex() == PropertySex.MALE) {
                return male;
            }
            if (((Indi) property).getSex() == PropertySex.FEMALE) {
                return female;
            }
            return unknown;
        }

        public String getString(String male, String female) {
            return getString(male, female, male);
        }

        public reportProperty getFamc() {
            // Parents
            Fam famc = ((Indi) property).getFamilyWhereBiologicalChild();
            return create(famc);
        }

//		// And we loop through its families
        public reportFam[] getFams() {
            Fam[] fams = ((Indi) property).getFamiliesWhereSpouse();

            reportFam[] reportFams = new reportFam[fams.length];
            for (int f = 0; f < fams.length; f++) {
                // .. here's the fam and spouse
                reportFams[f] = new reportFam(fams[f], (Indi) property);
            }
            return reportFams;
        }
    }

    public class reportEntity extends reportProperty {

        TextOptions OPTIONS = TextOptions.getInstance();

        reportEntity(Entity e) {
            super(e);
        }

        public String getId() {
            return ((Entity) property).getId();
        }
    }

    public class reportGedcom extends Object {

        Gedcom theGedcom;

        reportGedcom(Gedcom g) {
            theGedcom = g;
        }

        public reportEntity getSubmitter() {
            return new reportEntity(theGedcom.getSubmitter());
        }

        public String getFileName() {
            return theGedcom.getOrigin().getFileName();
        }

        public String getProperty(String tagPath) {
            return theGedcom.getFirstEntity("HEAD").getPropertyByPath("HEAD:" + tagPath).getDisplayValue();
        }
    }

    public class reportPropertyAge extends reportProperty {

        reportPropertyAge(PropertyAge p) {
            super(p);
        }

        public String get(String what) {
            Matcher temp = Pattern.compile("(([0-9]+)y *)?(([0-9]+)m *)?(([0-9]+)d)?").matcher(getValue());
            if (temp.matches()) {
                if (what.equals("Years")) {
                    return temp.group(2);
                }
                if (what.equals("Months")) {
                    return temp.group(4);
                }
                if (what.equals("Days")) {
                    return temp.group(6);
                }
                return null;
            }
            return null;

        }

        public String toString() {
            return get("Years");
        }

    }

    public class reportPropertyXRef extends reportProperty {

        reportPropertyXRef(PropertyXRef p) {
            super(p);
        }

        public reportEntity getTarget() {
            Entity e = ((PropertyXRef) property).getTargetEntity();
            return (reportEntity) create(e);
        }
    }

    public class reportPropertyPlace extends reportProperty {

        reportPropertyPlace(PropertyPlace p) {
            super(p);
        }

        public String[] getHierarchy() {
            String hierarchy = ((PropertyPlace) property).getFormatAsString();
            return hierarchy.split(",");
        }

        public String getJuridiction(int level) {
            return ((PropertyPlace) property).getJurisdiction(level);
        }

        public String getFirstAvailableJurisdiction() {
            return ((PropertyPlace) property).getFirstAvailableJurisdiction();
        }

        public String getCity() {
            return ((PropertyPlace) property).getCity();
        }

        public String getJuridiction(String f) {
            String[] values = ((PropertyPlace) property).getValue().split(",");
            String[] hierarchy = getHierarchy();
            for (int i = 0; i < hierarchy.length; i++) {
                if (hierarchy[i].trim().equalsIgnoreCase(f.trim())) {
                    return values[i];
                }
            }
            return null;
        }
    }

    public class reportPropertyFile extends reportProperty {

        reportPropertyFile(PropertyFile p) {
            super(p);
        }

        public String getAbsolutePath() {
            try {
                return ((PropertyFile) property).getInput().get().getLocation();
            } catch (Exception e) {
                return null;
            }
        }
    }

    public class reportPropertyName extends reportProperty {

        reportPropertyName(PropertyName p) {
            super(p);
        }

        public String getLastName() {
            return ((PropertyName) property).getLastName();
        }

        public String getFirstName() {
            return ((PropertyName) property).getFirstName();
        }
    }
    
    public class reportPropertyMedia extends reportProperty {
        reportPropertyMedia(Property p) {
             super(p);
            
            if (p instanceof PropertyMedia) {
                property = ((PropertyMedia) p).getTargetEntity();
            } 
        }
        
        public String getForm(){
            return property.getPropertyValue("FORM");
        }
        
        public String getURL() {
            PropertyFile pFile = (PropertyFile) property.getProperty("FILE");
            if (pFile == null) {
                return "";
            }
            Optional<InputSource> oInput = pFile.getInput();
            if (!oInput.isPresent()) {
                return "";
            }
            InputSource input = oInput.get();
            if (input instanceof FileInput) {
                return "file:///"+input.getLocation();
            } else {
                return input.getLocation();
            }
        }
        
    }

    public class reportProperty extends Object implements Comparable<reportProperty> {

        Property property;

        reportProperty(Property prop) {
            property = prop;
            if (prop == null) {
                return;
            }
            // TODO: doc.addText("Notes :");
            // TODO: outputNotes( "", prop, todos, doc); // Note should be emphasized
        }

        // Factory for reportProperty
        reportProperty create(Property p) {
            if (p == null) {
                return null;
            }
            if (p instanceof Indi) {
                return new reportIndi((Indi) p);
            }
            if (p instanceof Fam) {
                return new reportFam((Fam) p);
            }
            if (p instanceof PropertyAge) {
                return new reportPropertyAge((PropertyAge) p);
            }
            if (p instanceof PropertyMedia || "OBJE".equals(p.getTag())) {
                return new reportPropertyMedia(p);
            }
            if (p instanceof PropertyXRef) {
                return new reportPropertyXRef((PropertyXRef) p);
            }
            if (p instanceof PropertyPlace) {
                return new reportPropertyPlace((PropertyPlace) p);
            }
            if (p instanceof PropertyFile) {
                return new reportPropertyFile((PropertyFile) p);
            }
            if (p instanceof PropertyName) {
                return new reportPropertyName((PropertyName) p);
            }
            return new reportProperty(p);
        }

        @Override
        public int compareTo(reportProperty that) {
            return property.compareTo(that.property);
        }

        public reportProperty getProperty(String tagPath) {
            if (property == null) {
                return null;
            }
            Property subProp = property.getPropertyByPath(property.getTag() + ":" + tagPath);
            return create(subProp);
        }

        public reportProperty getParent() {
            if (property == null) {
                return null;
            }
            return create(property.getParent());
        }

        // Shortcut for getProperty so that $indi.name is equivalent to $indi.getProperty("NAME")
        public Object get(String tag) {
            return getProperty(tag.toUpperCase());
        }

        public reportProperty[] getProperties(String tagPath) {
            Property props[] = property.getProperties(new TagPath(property.getTag() + ":" + tagPath));
            if (props.length == 0) {
                return null;
            }

            reportProperty[] reportProps = new reportProperty[props.length];

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
//		public String getValue() 	{return format("{$v}");} // return display value

        public String getValue() {
            // Don't use format to get DisplayValue as format may be overriden (see PropertyPlace)
            // FIXME: We should specify that format({$v} must always return the DisplayValue
            return (property == null) ? "" : property.getDisplayValue();
        }

        public String getName() {
            return format("{$T}");
        }

        public String getPlace() {
            return format("{$P}");
        }

        public String toString() {
            return getValue();
        }

        /**
         * @param fmtstr {$t} property tag (doesn't count as matched) {$T}
         * property name(doesn't count as matched) {$D} date as fully localized
         * string {$y} year {$p} place (city) {$P} place (all jurisdictions)
         * {$V} value {$v} display value
         * @return
         */
        public String format(String fmtstr) {
            return (property == null) ? "" : property.format(fmtstr);
        }
    }

    public class reportOptions {

        private ReportGedart report;
        private TextOptions OPTIONS = TextOptions.getInstance();

        // TODO: use reflexion/introspection in future?
        reportOptions(ReportGedart rep) {
            report = rep;
        }

        public boolean getShowBlankCells() {
            return report.includeBlankCells;
        }

        public boolean getShowID() {
            return report.showID;
        }

        public boolean getShowTOC() {
            return report.includeTOC;
        }

        public boolean getShowIndex() {
            return report.includeIndex;
        }

        public boolean getShowIndis() {
            return report.includeIndi;
        }

        public boolean getShowFams() {
            return report.includeFam;
        }

        public String getLocale() {
            return OPTIONS.getOutputLocale().getLanguage();
        }
    }
}
