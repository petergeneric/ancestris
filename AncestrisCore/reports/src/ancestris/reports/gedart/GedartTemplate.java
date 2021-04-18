package ancestris.reports.gedart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.TreeMap;
import org.openide.modules.Places;

class GedartTemplate extends Object implements Cloneable {

    private String name;
    private String description;
    private String path;
    private String format;
    private GedartResources resources;
    private TreeMap<String, String> context = new TreeMap<>();

    public GedartTemplate(File dir) {
        // Default values
        name = dir.getName();
        path = dir.getAbsolutePath();

        int index = name.lastIndexOf('.');
        if (index > 0) {
            format = name.substring(index + 1);
            description = name.substring(0, index);
        }
        if (format != null) {
            description += " (" + format + ")";
        }

        // May be overriden by .properties file
        try {
            resources = new GedartResources(new FileInputStream(new File(dir, "resources.properties")));
        } catch (FileNotFoundException e) {
        }
        if (resources != null) {
            String value = resources.translate("name");
            if (value != null) {
                description = value;
            }
            setContext("Indi");
            setContext("Indi[]");
            setContext("Fam");
            setContext("Fam[]");
            setContext("Gedcom");
        }
    }

    static GedartTemplate create(File dir) {
        if (!dir.isDirectory()) {
            return null;
        }
        if (!new File(dir, "index.vm").exists()) {
            return null;
        }
        return new GedartTemplate(dir);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDescription(String ctx) {
        if (context.isEmpty()) {
            return getDescription();
        }
        return context.get(ctx);
    }

    public String getPath() {
                //XXX: this is a quick fix. Gedart will use velocity renderer
        // Make template path relative to userdir
        final String base = Places.getUserDirectory().getAbsolutePath();
        return path.substring(base.length() + 1);
    }

    public String getFormat() {
        return format;
    }

    public void setContext(String tag) {
        String value = resources.translate(tag, null, false);
        if (value != null) {
            context.put(tag, value);
        }
    }

    public String toString() {
        return getDescription();
    }

    public static String[] getDescription(GedartTemplate[] templates) {
        String[] descriptions = new String[templates.length];
        for (int i = 0; i < templates.length; i++) {
            descriptions[i] = templates[i].getDescription();
        }
        return descriptions;
    }

    protected GedartTemplate clone() throws CloneNotSupportedException {
        return (GedartTemplate) super.clone();
    }

    public void setDescription(String description2) {
        description = description2;
    }
}
