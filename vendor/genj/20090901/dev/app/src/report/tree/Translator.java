/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import genj.report.Report;

/**
 * Uses the report object to translate strings.
 * This class' purpose is to prevent passing around the report object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class Translator
{
    private Report report;

    public Translator(Report report)
    {
        this.report = report;
    }

    /**
     * Translates a string.
     *
     * @param key the key to lookup in [ReportName].properties
     */
    public final String translate(String key)
    {
        return report.translate(key);
    }

    /**
     * Translates a string.
     *
     * @param key the key to lookup in [ReportName].properties
     * @param value an integer value to replace %1 in value with
     */
    public final String translate(String key, int value)
    {
        return report.translate(key, value);
    }

    /**
     * Translates a string.
     *
     * @param key the key to lookup in [ReportName].properties
     * @param value an object value to replace %1 in value with
     */
    public final String translate(String key, Object value)
    {
        return report.translate(key, value);
    }

    /**
     * Translates a string.
     *
     * @param key the key to lookup in [ReportName].properties
     * @param values an array of values to replace %1, %2, ... in value with
     */
    public String translate(String key, Object[] values)
    {
        return report.translate(key, values);
    }
}
