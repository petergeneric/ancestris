package genjreports.gedart;

import genj.report.ReportLoader;
import genj.util.EnvironmentChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

class GedartTemplates extends TreeMap<String, GedartTemplate> {

	public GedartTemplates() {
		// first templates in report/gedart/templates
		File gedartDir = new File(ReportLoader.getReportDirectory(),
				"gedart/templates");
		putAll(new GedartTemplates(gedartDir));

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
