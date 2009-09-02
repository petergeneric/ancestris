/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genj.report.options;

import genj.option.Option;
import genj.option.PropertyOption;
import genj.report.Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class adds the possibility to add options to the report configuration screen from
 * components used by a report. This way, not only options visible in the report class will be displayed
 * but also options from components used by the report.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class ComponentReport extends Report
{
    /**
     * Report options. This list is generated from the components.
     */
    private List<PropertyOption> options = null;

    /**
     * Configurable components in categories.
     */
    private Map<String, List<Object>> configurables = new LinkedHashMap<String, List<Object>>();

    /**
     * Properties with forced categories.
     */
    private Map<String, String> forcedCategories = new HashMap<String, String>();

    /**
     * Adds a component to be configured to the default category.
     * If it is a ComponentContainer, add its contents instead.
     * @param category
     * @param component
     */
    protected void addOptions(Object component)
    {
        addOptions(component, getName());
    }

    /**
     * Adds a component to be configured to the specified category.
     * If it is a ComponentContainer, add its contents instead.
     * @param category
     * @param component
     */
    protected void addOptions(Object component, String category)
    {
        List<Object> comps = configurables.get(category);
        if (comps == null)
        {
            comps = new ArrayList<Object>();
            configurables.put(category, comps);
        }
        addComponent(comps, component);
    }

    /**
     * Adds a component to be configured to the given list.
     * If it is a ComponentContainer, add its contents instead.
     * @param category
     * @param component
     */
    private void addComponent(List<Object> comps, Object component)
    {
        if (component instanceof ComponentContainer)
        {
            for (Object o : ((ComponentContainer)component).getComponents())
                if (o == component)
                    comps.add(o);
                else
                    addComponent(comps, o);
        }
        else
            comps.add(component);
    }

    /**
     * Forces a property to have given category.
     */
    protected void setCategory(String property, String category)
    {
        forcedCategories.put(property, category);
    }

    /**
     * Generates the option list for this report.
     */
    @Override
    public List<PropertyOption> getOptions()
    {
        if (options != null)
            return options;

        Map<String, AggregatorOption> optionsCache = new LinkedHashMap<String, AggregatorOption>();

        addOptions(optionsCache, super.getOptions(), getName());

        for (Map.Entry<String, List<Object>> entry : configurables.entrySet())
            for (Object component : entry.getValue())
                addOptions(optionsCache, PropertyOption.introspect(component), entry.getKey());

        options = new ArrayList<PropertyOption>(optionsCache.values());
        // restore options values
        for (PropertyOption option : options)
        {
            // restore old value
            option.restore(registry);
            // options do try to localize the name and tool tip based on a properties file
            // in the same package as the instance - problem is that this
            // won't work with our special way of resolving i18n in reports
            // so we have to do that manually
            String oname = translate(option.getProperty());
            if (oname.length() > 0)
                option.setName(oname);
            String toolTipKey = option.getProperty() + ".tip";
            String toolTip = translate(toolTipKey);
            if (toolTip.length() > 0 && !toolTip.equals(toolTipKey))
                option.setToolTip(toolTip);
        }
        return options;
    }

    /**
     * Adds options to the options cache.
     * @param optionsCache
     * @param options
     * @param category
     */
    private void addOptions(Map<String, AggregatorOption> optionsCache, List<PropertyOption> options, String category)
    {
        for (PropertyOption option : options)
        {
            String property = option.getProperty();
            AggregatorOption aggregator = optionsCache.get(property);
            if (aggregator == null)
            {
                aggregator = new AggregatorOption(this, property);
                optionsCache.put(property, aggregator);
                String cat = forcedCategories.get(property);
                if (cat == null)
                    cat = category;
                aggregator.setCategory(translate(cat));
            }
            aggregator.addOption(option);
        }
    }

    /**
     * Store report's options
     */
    @Override
    public void saveOptions()
    {
        // if known
        if (options == null)
            return;
        // save 'em
        for (Option option : options)
            option.persist(registry);
        // done
    }
}
