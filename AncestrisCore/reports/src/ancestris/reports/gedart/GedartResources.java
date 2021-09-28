package ancestris.reports.gedart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to manage ResourceBundle without java class in specific directory.
 *
 * @author Zurga
 */
public class GedartResources {

    private static final Logger LOG = Logger.getLogger("ancestris.util");
    /**
     * language we're trying to use
     */
    private final static String DEFAULT_LANG = Locale.getDefault().getLanguage();
    private final static String BUNDLE_NAME = "Bundle";
    private final static String BUNDLE_EXTENSION = ".properties";
    private ResourceBundle defaultResource;
    private ResourceBundle localizedResource;

    public GedartResources(String path, String lang) {
        String langue = lang != null ? lang : DEFAULT_LANG;
        try (FileInputStream fisDefault = new FileInputStream(new File(path + "/" + BUNDLE_NAME + BUNDLE_EXTENSION))) {
            defaultResource = new PropertyResourceBundle(fisDefault);
        } catch (IOException e) {
            LOG.log(Level.FINEST, "Unable to find resources.", e);
            defaultResource = null;
        }
        try (FileInputStream fisLocalized = new FileInputStream(new File(path + "/" + BUNDLE_NAME + "_" + langue + BUNDLE_EXTENSION))) {
            localizedResource = new PropertyResourceBundle(fisLocalized);
        } catch (IOException e) {
            LOG.log(Level.FINEST, "Unable to find resources.", e);
            localizedResource = null;
        }

    }

    /**
     * Sub-classes that are accompanied by a [ReportName].properties file
     * containing simple key=value pairs can lookup internationalized
     * text-values with this method.
     *
     * @param key the key to lookup in [ReportName].properties
     */
    public final String translate(String key) {
        return translate(key, (Object[]) null);
    }

    /**
     * Sub-classes that are accompanied by a [ReportName].properties file
     * containing simple key=value pairs can lookup internationalized
     * text-values with this method.
     *
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
     *
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
     *
     * @param key the key to lookup in [ReportName].properties
     * @param values an array of values to replace %1, %2, ... in value with
     */
    public String translate(String key, Object[] values) {
        return translate(key, values, true);
    }

    public String translate(String key, Object[] values, boolean notnull) {

        if (defaultResource == null && localizedResource == null) {
            return notnull ? key : "";
        }

        // look it up in language
        String result = null;
        if (localizedResource != null && localizedResource.containsKey(key)) {
            result = localizedResource.getString(key);
        }
        // fallback if necessary
        if (result == null && defaultResource != null && defaultResource.containsKey(key)) {
            result = defaultResource.getString(key);
        }

        if (result != null) {
            result = MessageFormat.format(result, values);
        }
        if (result == null && notnull) {
            result = key;
        }
        // done
        return result;
    }

}
