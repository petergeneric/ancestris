/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genj.report.options;

import genj.option.MultipleChoiceOption;
import genj.option.OptionUI;
import genj.option.OptionsWidget;
import genj.option.PropertyOption;
import genj.report.Report;
import genj.util.Registry;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates several PropertyOption objects.
 * An object of this class controls all aggregated options and is an interface to set
 * the same value to all of them.
 * This is needed when an option is defined in more than one component. In this case
 * all components with this option have to be updated.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class AggregatorOption extends PropertyOption
{
    /**
     * The controlled options.
     */
    private List<PropertyOption> options = new ArrayList<PropertyOption>();

    /**
     * The related report. This is used for translation.
     */
    private Report report;

    /**
     * Initializes the object.
     * @param report  related report
     * @param property  property name
     */
    protected AggregatorOption(Report report, String property)
    {
        super(report, property);
        this.report = report;
    }

    /**
     * Adds option to be aggregated.
     */
    public void addOption(PropertyOption option)
    {
        options.add(option);
    }

    /**
     * Returns the value for this option.
     */
    @Override
    public Object getValue()
    {
        return getFirst().getValue();
    }

    @Override
    public void setName(String set)
    {
        getFirst().setName(set);
    }

    @Override
    public void setToolTip(String set)
    {
        getFirst().setToolTip(set);
    }

    /**
     * Sets the value of all aggregated options.
     */
    @Override
    public void setValue(Object set)
    {
        for (int i = 0; i < options.size(); i++)
            ((PropertyOption) options.get(i)).setValue(set);
    }

    @Override
    public String getName()
    {
        return getFirst().getName();
    }

    @Override
    public String getToolTip()
    {
        return getFirst().getToolTip();
    }

    /**
     * Provider a UI for this option
     */
    @Override
    public OptionUI getUI(OptionsWidget widget)
    {
        Class<? extends OptionUI> uiType = getFirst().getUI(null).getClass();
        // a font?
        if (uiType == FontUI.class)
            return new FontUI(this);
        // a boolean?
        if (uiType == BooleanUI.class)
            return new BooleanUI(this);
        // a file?
        if (uiType == FileUI.class)
            return new FileUI(this);
        // multiple choice?
        if (uiType == MultipleChoiceOption.UI.class)
        {
            Object[] choices = ((MultipleChoiceOption)getFirst()).getChoices();
            String[] translatedChoices = translate(choices);
            return new MultipleChoiceUI(this, translatedChoices);
        }
        // all else
        return new SimpleUI(this);
    }

    /**
     * Returns the class of the first contained option.
     */
    public Class<? extends PropertyOption> getType()
    {
        return getFirst().getClass();
    }

    /**
     * Returns the choices of a multiple choice property.
     * If the contained property is not a MultipleChoiceOption, the method will fail.
     */
    public String[] getChoices()
    {
        Object[] choices = ((MultipleChoiceOption)getFirst()).getChoices();
        return translate(choices);
    }

    /**
     * Translates an array of choices.
     * @return array with translated strings
     */
    private String[] translate(Object[] choices)
    {
        String[] result = new String[choices.length];
        for (int i = 0; i < choices.length; i++)
        {
            String choice = choices[i].toString();
            String key = getProperty() + "." + choice;
            result[i] = report.translate(key);
            if (result[i].equals(key))
                result[i] = report.translate(choice);
        }
        return result;
    }

    /**
     * Restore option values from registry
     */
    @Override
    public void restore(Registry registry)
    {
        String value = registry.get(getPropertyKey(), (String)null);
        if (value != null)
            setValue(value);
    }

    /**
     * Persist option values to registry
     */
    @Override
    public void persist(Registry registry)
    {
        Object value = getValue();
        if (value != null)
            registry.put(getPropertyKey(), value.toString());
    }

    /**
     * Returns the registry key for this property.
     */
    private String getPropertyKey()
    {
        return instance.getClass().getName() + '.' + getProperty();
    }

    /**
     * Returns the first aggregated option. This is used for accessing the properties of the property.
     * @return
     */
    private PropertyOption getFirst()
    {
        return (PropertyOption)options.get(0);
    }
}
