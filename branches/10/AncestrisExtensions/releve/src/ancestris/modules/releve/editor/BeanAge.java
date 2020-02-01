package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldAge;
import genj.gedcom.time.Delta;
import java.awt.BorderLayout;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;
import org.openide.util.NbBundle;

/**
 *
 */
public class BeanAge extends Bean {

    //private JTextField tfield;
    static final String yearLabel  = NbBundle.getMessage(BeanAge.class,"BeanAge.YearInitial");
    static final String monthLabel = NbBundle.getMessage(BeanAge.class,"BeanAge.MonthInitial");
    static final String dayLabel   = NbBundle.getMessage(BeanAge.class,"BeanAge.DayInitial");
    static final String valueFormat = String.format("%%3s%s  %%2s%s  %%2s%s", yearLabel, monthLabel, dayLabel);
    static final String maskFormat = String.format("***%s  **%s  **%s", yearLabel, monthLabel, dayLabel);
    static final Pattern pattern = Pattern.compile(
            String.format("^([0-9\\p{javaWhitespace}]{3})%s  ([0-9\\p{javaWhitespace}]{2})%s  ([0-9\\p{javaWhitespace}]{2})%s?$",
            yearLabel, monthLabel, dayLabel));

    private final JFormattedTextField tfield;

    public BeanAge() {

        MaskFormatter mask = null;
        try {

            mask = new MaskFormatter(maskFormat);
            mask.setPlaceholderCharacter(' ');
            mask.setValidCharacters("0123456789 ");
        } catch (ParseException e) {
        }

        tfield = new JFormattedTextField(mask);
        tfield.getDocument().addDocumentListener(changeSupport);
        
        setLayout(new BorderLayout());
        add(tfield, BorderLayout.CENTER);
        defaultFocus = tfield;
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {

        final FieldAge property = (FieldAge) getField();
        if (property == null) {
            tfield.setText("");
        } else {
            String txt = String.format(valueFormat,
                    property.getDelta().getYears() == 0 ? "" : String.valueOf(property.getDelta().getYears()),
                    property.getDelta().getMonths() == 0 ? "" : String.valueOf(property.getDelta().getMonths()),
                    property.getDelta().getDays() == 0 ? "" : String.valueOf(property.getDelta().getDays())
                    );
            tfield.setText(txt);
        }
        // je place le focus au début du champ
        tfield.setCaretPosition(0);
        
        // not changed
        changeSupport.setChanged(false);
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        Matcher matcher = pattern.matcher(tfield.getText());
        if (matcher.matches()) {
            String deltaValue = 
                  (matcher.group(1).trim().isEmpty() ? "" : matcher.group(1).trim()+"y ")
                + (matcher.group(2).trim().isEmpty() ? "" : matcher.group(2).trim()+"m " ) 
                + (matcher.group(3).trim().isEmpty() ? "" : matcher.group(3).trim()+"d" );
            setFieldValue(deltaValue);
        }
    }

    @Override
    protected void replaceValueImpl(Field field) {
        final FieldAge fieldAge = (FieldAge) field;
        if (fieldAge != null && fieldAge instanceof  FieldAge) {
            final FieldAge property = (FieldAge) field;
            String txt = String.format(valueFormat,
                    property.getDelta().getYears() == 0 ? "" : String.valueOf(property.getDelta().getYears()) ,
                    property.getDelta().getMonths() == 0 ? "" : String.valueOf(property.getDelta().getMonths()),
                    property.getDelta().getDays() == 0 ? "" : String.valueOf(property.getDelta().getDays())
            );
            tfield.setText(txt);
        } else {
            String txt = String.format(valueFormat, "   ", "  " , "  ");
            tfield.setText(txt);
        }
    }
}
