package ancestris.reports.gedart;

import genj.report.ReportLoader;
import genj.util.EnvironmentChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

class GedartTemplates extends TreeMap<String, GedartTemplate> {

	public GedartTemplates() {
		// first templates in report/gedart/templates
            // XXX: Does not work in ancestris, see SamplePrivider implementation
//		File gedartDir = new File(ReportLoader.getReportDirectory(),
//				"gedart/templates");
//		putAll(new GedartTemplates(gedartDir));

            
// XXX: copied from almanach:            
//        @Override
//        protected void loadFromResources(File[] loaded) {
//            Set<String> seen = new HashSet<String>();
//            for (File file:loaded){
//                seen.add(file.getName());
//            }
//            final String PCKNAME = "genj.almanac.resources";
//            try {
//                for (String res : PackageUtils.findInPackage(PCKNAME, Pattern.compile(".*/[^/]*\\.almanac"))) {
//                    String name = res.substring(PCKNAME.length() + 1);
//                    if (seen.contains(name))
//                        continue;
//                    try {
//                        load(new BufferedReader(
//                                new InputStreamReader(
//                                Almanac.class.getResourceAsStream("/" + PCKNAME.replace('.', '/') + "/" +name))));
//                    } catch (Exception ex) {
//                        LOG.log(Level.WARNING, "IO Problem reading " + res, ex);
//                        Exceptions.printStackTrace(ex);
//                    }
//                }
//            } catch (ClassNotFoundException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
            
            
            
            
            
		// second templates in {user.home.ancestris}/gedart/contrib-templates
		File dir = new File(EnvironmentChecker.getProperty(
				"user.home.ancestris/gedart/contrib-templates", "?",
				"Looking for gedart/contrib-templates"));

		if (!dir.exists()) {
			dir.mkdirs();
		}
		putAll(new GedartTemplates(dir));

		// then templates in {user.home.ancestris}/gedart/templates
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
				if (t == null)
					continue;
				put(t.getName(),t);
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
		  for (GedartTemplate ga: this.values()) {
			  if (ga.getDescription(ctx) == null) continue;
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
