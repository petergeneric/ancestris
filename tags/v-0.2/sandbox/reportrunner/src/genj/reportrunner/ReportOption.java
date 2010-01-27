package genj.reportrunner;

import genj.option.MultipleChoiceOption;
import genj.option.PropertyOption;
import genj.report.options.AggregatorOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a single report option.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 * @version $Id: ReportOption.java,v 1.2 2009/04/30 16:58:26 pewu Exp $
 */
public class ReportOption
{
	/**
	 * Original report property object.
	 */
    private PropertyOption property;

    /**
     * Default option value.
     */
    private Object defaultValue;

    /**
     * Available choices for multi-choice options.
     */
    private String[] choices;

    /**
     * Translated option description.
     */
    private String description;

    /**
     * Creates the object.
     * @param property  report property
     * @param description  translated option description
     */
    public ReportOption(PropertyOption property, String description)
    {
        this.property = property;
        this.description = description;
        defaultValue = property.getValue();

        Object[] optionChoices = null;
        if (property instanceof MultipleChoiceOption)
            optionChoices = ((MultipleChoiceOption)property).getChoices();
        else if (property instanceof AggregatorOption)
        {
            AggregatorOption aggregator = (AggregatorOption)property;
            if (MultipleChoiceOption.class.isAssignableFrom(aggregator.getType()))
                optionChoices = aggregator.getChoices();
        }
        if (optionChoices != null)
        {
            List<String> list = new ArrayList<String>();
            for (Object choice : optionChoices)
            {
                String choiceStr = choice.toString();
                int i = choiceStr.lastIndexOf('.');
                if (i != -1)
                    choiceStr = choiceStr.substring(i + 1);
                list.add(choiceStr);
            }
            choices = list.toArray(new String[0]);
        }
    }

    /**
     * Returns option name.
     */
    public String getName()
    {
        return property.getProperty();
    }

    /**
     * Sets value of the option.
     */
    public void setValue(String value)
    {
        if (choices != null)
        {
            for (int i = 0; i < choices.length; i++)
                if (choices[i].equals(value))
                {
                    property.setValue(i);
                    break;
                }
        }
        else
        {
            Class<?> clazz = defaultValue.getClass();
            if (clazz == Integer.class)
                property.setValue(Integer.valueOf(value));
            else if (clazz == Boolean.class)
                property.setValue(Boolean.valueOf(value));
            else // assume String
                property.setValue(value);
        }
    }

    /**
     * Returns available choices.
     */
    public String[] getChoices()
    {
        return choices;
    }

    /**
     * Resets option to default value.
     */
    public void reset()
    {
        property.setValue(defaultValue);
    }

    /**
     * Returns the type of option or available choices.
     */
    public String getType()
    {
        if (choices != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (String choice : choices)
                sb.append(choice).append("|");
            sb.deleteCharAt(sb.length() - 1);
            sb.append(")");
            return sb.toString();
        }
        if (defaultValue.getClass() == String.class)
            return "string";
        if (defaultValue.getClass() == Integer.class)
            return "int";
        if (defaultValue.getClass() == Boolean.class)
            return "(true|false)";
        return "unknown";
    }

    /**
     * Returns the default option value.
     */
    public String getDefaultValue()
    {
        if (choices != null)
            return choices[(Integer)defaultValue];
        return defaultValue.toString();
    }

    /**
     * Returns option description.
     */
    public String getDescription()
    {
        return description;
    }
}
