
package genj.gedcom;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Property Time in GEDCOM 7. 
 * @author zurga
 */
public class PropertyTime extends Property {
    
    private final static Logger LOGGER = Logger.getLogger("genj.gedcom.propertytime");
    
    public static String TIME = "TIME";
    
    private final static String TIME_PATTERN = "%02d:%02d:%02.0f";
    
    private int hour = 0;
    private int minute = 0;
    private float second = 0;
    
    private String value;
    private boolean isValid = false;

    public PropertyTime() {
        super(TIME);
    }
    
    public PropertyTime(String tag) {
        super(tag);
    }

    @Override
    public String getValue() {
        if (isValid) {
            return String.format(TIME_PATTERN, hour, minute, second);
        } else {
            return value;
        }
    }

    @Override
    public void setValue(String value) {
        final String old = this.value;
        this.value = value;
        try {
            StringTokenizer st = new StringTokenizer(value, ":");
            if (st.hasMoreTokens()) {
                hour = Integer.parseInt(st.nextToken());
            }
            if (st.hasMoreTokens()) {
                minute = Integer.parseInt(st.nextToken());
            }
            if (st.hasMoreTokens()) {
                second = Math.round(Float.parseFloat(st.nextToken()));
            }
            isValid = true;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.FINE, "Unable to parse time value : " + value, e);
        }
        
        propagatePropertyChanged(this, old);
    }
    
}
