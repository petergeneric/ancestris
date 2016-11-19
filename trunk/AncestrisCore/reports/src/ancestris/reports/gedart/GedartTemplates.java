package ancestris.reports.gedart;

import genj.util.EnvironmentChecker;
import genj.util.PackageUtils;

import java.io.File;
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

        if (!dir.exists()) {
            dir.mkdirs();
        }
        // ... ensure templates resources files as a dir into it 
        File dirDefault = new File(dir + File.separator + "default");
        if (!dirDefault.exists()) {
            dirDefault.mkdirs();
        }
        File dest = null;
        final String PCKNAME = "ancestris.reports.gedart.templates.resources";
        try {
            for (String res : PackageUtils.findInPackage(PCKNAME, Pattern.compile(".*/[^/]*\\" + ".vm"))) {
                String name = res.substring(PCKNAME.length() + 1);
                URL inputUrl = GedartTemplates.class.getResource("/" + PCKNAME.replace('.', '/') + "/" + name);
                dest = new File(dirDefault + File.separator + name);
                FileUtils.copyURLToFile(inputUrl, dest);
            }
            for (String res : PackageUtils.findInPackage(PCKNAME, Pattern.compile(".*/[^/]*\\" + ".properties"))) {
                String name = res.substring(PCKNAME.length() + 1);
                URL inputUrl = GedartTemplates.class.getResource("/" + PCKNAME.replace('.', '/') + "/" + name);
                dest = new File(dirDefault + File.separator + name);
                FileUtils.copyURLToFile(inputUrl, dest);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        
        // ... then load content of directory
        putAll(new GedartTemplates(dir));

        
        
        // then look for templates in {user.home.ancestris}/gedart/templates
        dir = new File(EnvironmentChecker.getProperty(
                "user.home.ancestris/gedart/templates", "?",
                "Looking for gedart/templates"));

        if (!dir.exists()) {
            dir.mkdirs();
        }
        putAll(new GedartTemplates(dir));


    }

    public GedartTemplates(File dir) {
        if (dir.isDirectory()) {
            // loop over Templates
            File[] files = dir.listFiles();
            for (int b = 0; b < files.length; b++) {
                GedartTemplate t = GedartTemplate.create(files[b]);
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
        ArrayList<GedartTemplate> result = new ArrayList<GedartTemplate>(5);
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
