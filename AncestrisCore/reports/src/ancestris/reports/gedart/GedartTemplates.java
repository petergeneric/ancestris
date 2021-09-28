package ancestris.reports.gedart;

import genj.util.EnvironmentChecker;
import genj.util.PackageUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.openide.util.Exceptions;

class GedartTemplates extends TreeMap<String, GedartTemplate> {

    public GedartTemplates() {

        // First templates in {user.home.ancestris}/gedart/contrib-templates
        File dir = new File(EnvironmentChecker.getProperty(
                "user.home.ancestris/gedart/contrib-templates", "?",
                "Looking for gedart/contrib-templates"));

        if (dir.exists()) {
            deleteFolder(dir);
            dir.mkdirs();
        } else {
            dir.mkdirs();
        }
        // Copy all templates to contrib-templates
        File dest = null;
        final String PCKNAME = "ancestris.reports.gedart.templates";
        try {
            for (String res : PackageUtils.findInPackage(PCKNAME, Pattern.compile("([^\\s]+((\\.(?i)(txt|properties|vm|jpg))$|\\/$))"))) {
                String name = res.substring(PCKNAME.length() + 1);
                if (name.endsWith(".")) { // we have a template directory, create it if it deos not exist
                    File subdir = new File(dir + File.separator + name.substring(0, name.length()-1));
                    if (!subdir.exists()) {
                        subdir.mkdirs();
                    }
                    continue;
                }
                // name = "modele.file.ext"
                // in name, replace first "." by "/"
                // dest = dir + name
                name = name.replaceFirst("\\.", "\\/");
                URL inputUrl = GedartTemplates.class.getResource("/" + PCKNAME.replace('.', '/') + "/" + name);
                dest = new File(dir + File.separator + name);
                FileUtils.copyURLToFile(inputUrl, dest);
            }
        } catch (IOException | ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        // ... then load content of directories
        putAll(new GedartTemplates(dir));

        
        
        // then look for templates in {user.home.ancestris}/gedart/templates
        dir = new File(EnvironmentChecker.getProperty(
                "user.home.ancestris/gedart/templates", "?",
                "Looking for gedart/templates"));

        if (!dir.exists()) {
            dir.mkdirs();
        }
        // ... then load content of directories
        putAll(new GedartTemplates(dir));


    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }    
    
    public GedartTemplates(File dir) {
        if (dir.isDirectory()) {
            // loop over Templates
            File[] files = dir.listFiles();
            for (File file : files) {
                GedartTemplate t = GedartTemplate.create(file);
                if (t == null) {
                    continue;
                }
                put(t.getName(), t);
            }
        }
    }

    /**
     * Convert collection of templates into array
     */
    public GedartTemplate[] toArray() {
        return (values().toArray(new GedartTemplate[0]));
    }

    public GedartTemplate[] toArray(Object context) {
        ArrayList<GedartTemplate> result = new ArrayList<>(5);
        String ctx = context.getClass().getSimpleName();
        for (GedartTemplate ga : this.values()) {
            if (ga.getDescription(ctx) == null) {
                continue;
            }
            GedartTemplate _ga;
            try {
                _ga = ga.clone();
                _ga.setDescription(ga.getDescription(ctx));
                result.add(_ga);
            } catch (CloneNotSupportedException e) {
            }
        }
        return (result.toArray(new GedartTemplate[0]));
    }


}
