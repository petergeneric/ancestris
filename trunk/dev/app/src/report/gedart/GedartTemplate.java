package gedart;

import java.io.File;

import org.apache.commons.lang.builder.ToStringBuilder;

class GedartTemplate  {

	private String name ;
	private String description;
	private String path;
	public GedartTemplate(File dir) {
		name = dir.getName();
		description = name;
		path = dir.getAbsolutePath();
	}
	static GedartTemplate create(File dir) {
		if (!dir.isDirectory())
			return null;
		if (!new File(dir, "index.vm").exists())
			return null;
		return new GedartTemplate(dir);
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public String getPath() {
		return path;
	}
	public String toString(){return getDescription(); }
}
