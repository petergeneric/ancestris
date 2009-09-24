package gedart;

import java.io.InputStream;
import java.util.Locale;

import genj.util.Resources;

public class GedartResources extends Resources {
	  /** language we're trying to use */
	  private final static String lang = Locale.getDefault().getLanguage();

	  public GedartResources(InputStream in) {
		super(in);
	}

	/**
	   * Sub-classes that are accompanied by a [ReportName].properties file
	   * containing simple key=value pairs can lookup internationalized
	   * text-values with this method.
	   * @param key the key to lookup in [ReportName].properties
	   */
	  public final String translate(String key) {
	    return translate(key, (Object[])null);
	  }

	  /**
	   * Sub-classes that are accompanied by a [ReportName].properties file
	   * containing simple key=value pairs can lookup internationalized
	   * text-values with this method.
	   * @param key the key to lookup in [ReportName].properties
	   * @param value an integer value to replace %1 in value with
	   */
	  public final String translate(String key, int value) {
	    return translate(key, new Integer(value));
	  }

	  /**
	   * Sub-classes that are accompanied by a [ReportName].properties file
	   * containing simple key=value pairs can lookup internationalized
	   * text-values with this method.
	   * @param key the key to lookup in [ReportName].properties
	   * @param value an object value to replace %1 in value with
	   */
	  public final String translate(String key, Object value) {
	    return translate(key, new Object[]{value});
	  }

	  /**
	   * Sub-classes that are accompanied by a [ReportName].properties file
	   * containing simple key=value pairs can lookup internationalized
	   * text-values with this method.
	   * @param key the key to lookup in [ReportName].properties
	   * @param values an array of values to replace %1, %2, ... in value with
	   */
	  public String translate(String key, Object[] values) {

	    // look it up in language
	    String result = null;
	    if (lang!=null)
	      result = this.getString(key+'.'+lang, values, false);

	    // fallback if necessary
	    if (result==null)
	      result = this.getString(key, values, true);

	    // done
	    return result;
	  }

}
