package gedart;

import genj.report.ReportLoader;
import genj.util.EnvironmentChecker;

import java.io.File;
import java.util.TreeMap;

class GedartTemplates extends TreeMap<String, String> {

	public GedartTemplates() {
		File gedartDir = new File(ReportLoader.getReportDirectory(),
				"gedart/templates");
		putAll(new GedartTemplates(gedartDir));

		gedartDir = new File(ReportLoader.getReportDirectory(),
		"gedartcustom/templates");
		putAll(new GedartTemplates(gedartDir));

// exists?
		File dir = new File(EnvironmentChecker.getProperty(this,
				"user.home.genj/gedart/templates", "?",
				"Looking for gedart/templates"));

		if (!dir.exists()) {
			dir.mkdir();
		}
		putAll(new GedartTemplates(dir));
	}

	public GedartTemplates(File dir) {
		if (dir.isDirectory()) {

			// loop over Templatess
			File[] files = dir.listFiles();
			for (int b = 0; b < files.length; b++) {
				File file = files[b];
				if (!file.isDirectory())
					continue;
				if (!new File(file, "index.vm").exists())
					continue;
				put(file.getName(), file.getAbsolutePath());
			}
		}
	}
}
