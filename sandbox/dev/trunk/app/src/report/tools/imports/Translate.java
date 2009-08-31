package tools.imports;

import java.util.Locale;

import genj.util.Resources;

public class Translate {

	private Resources resources;

	/** language we're trying to use */
	  private final static String lang = Locale.getDefault().getLanguage();

	  Translate(Object object) {
		    Class clazz = object instanceof Class ? (Class)object : object.getClass();
		    String name = clazz.getName();
		    while (name.indexOf('.') >= 0)
			      name = name.substring(name.indexOf('.')+1);

		      resources = new Resources(getClass().getResourceAsStream(name+".properties"));
		  }
	  
	  /**
	   * Type name (name without packages)
	   */
	  private String getTypeName() {
	    String rtype = getClass().getName();
	    while (rtype.indexOf('.') >= 0)
	      rtype = rtype.substring(rtype.indexOf('.')+1);
	    return rtype;
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

	    if (resources==null)
	      return key;

	    // look it up in language
	    String result = null;
	    if (lang!=null)
	      result = resources.getString(key+'.'+lang, values, false);

	    // fallback if necessary
	    if (result==null)
	      result = resources.getString(key, values, true);

	    // done
	    return result;
	  }
}
