/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genj.report.options;

import genj.option.OptionUI;
import genj.option.PropertyOption;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 * UI for use with AggregatorOption's multiple choice properties.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class MultipleChoiceUI extends JComboBox implements OptionUI
{

    private PropertyOption option;
    private String[] choices;

    /** constructor */
    public MultipleChoiceUI(PropertyOption option, String[] choices)
    {
        this.option = option;
        this.choices = choices;
        setModel(new DefaultComboBoxModel(choices));
        setSelectedIndex(getIndex());
    }

    /** component representation */
    public JComponent getComponentRepresentation()
    {
        return this;
    }

    /** text representation */
    public String getTextRepresentation()
    {
        int i = getIndex();
        return i == -1 ? "" : choices[i];
    }

    /** commit */
    public void endRepresentation()
    {
        option.setValue(new Integer(getSelectedIndex()));
    }

    /** getter for index */
    protected int getIndex()
    {
        int index = ((Integer)option.getValue()).intValue();
        if (index < 0 || index > choices.length - 1)
            index = -1;
        return index;
    }
}
