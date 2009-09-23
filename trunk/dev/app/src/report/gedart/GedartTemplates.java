package gedart;

import genj.report.ReportLoader;
import genj.util.EnvironmentChecker;

import java.io.File;
import java.util.TreeMap;

class GedartTemplates extends TreeMap<String, GedartTemplate> {

	public GedartTemplates() {
		// first templates in report/gedart/templates
		File gedartDir = new File(ReportLoader.getReportDirectory(),
				"gedart/templates");
		putAll(new GedartTemplates(gedartDir));

//		// second templates in report/gedartcustom/templates
//		gedartDir = new File(ReportLoader.getReportDirectory(),
//		"gedartcustom/templates");
//		putAll(new GedartTemplates(gedartDir));

		// second templates in {user.home.genj}/gedart/contrib-templates
		File dir = new File(EnvironmentChecker.getProperty(this,
				"user.home.genj/gedart/contrib-templates", "?",
				"Looking for gedart/contrib-templates"));

		if (!dir.exists()) {
			dir.mkdirs();
		}
		putAll(new GedartTemplates(dir));

		// then templates in {user.home.genj}/gedart/templates
		dir = new File(EnvironmentChecker.getProperty(this,
				"user.home.genj/gedart/templates", "?",
				"Looking for gedart/templates"));

		if (!dir.exists()) {
			dir.mkdirs();
		}
		putAll(new GedartTemplates(dir));
	}

	public GedartTemplates(File dir) {
		if (dir.isDirectory()) {
			// loop over Templatess
			File[] files = dir.listFiles();
			for (int b = 0; b < files.length; b++) {
				GedartTemplate t = GedartTemplate.create(files[b]);
				if (t == null)
					continue;
				put(t.getName(),t);
			}
		}
	}
}
