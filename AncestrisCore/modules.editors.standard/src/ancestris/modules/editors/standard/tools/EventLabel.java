/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.editors.standard.tools;

import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.util.WordBuffer;
import javax.swing.JLabel;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class EventLabel extends JLabel {

    private String tag = "";
    private String tableLabel = "";  // default label of the JLabel, the one which appears in the table
    private String shortLabel = "";
    private String longLabel = "";
    
    public EventLabel(Property property) {
        this.tag = property.getTag();
        
        if (property instanceof Indi) {
            this.shortLabel = NbBundle.getMessage(getClass(), "IndiEventNameShort");
            this.longLabel = NbBundle.getMessage(getClass(), "IndiEventNameLong");
            this.tableLabel = this.shortLabel;
        } else {
            String str = property.getPropertyName();
            
            // Short label : only take first word or property name
            if (str.contains(" ")) {
                str = str.substring(0, str.indexOf(" ")); 
            }
            this.shortLabel = str;

            // Table label : replace shortlabel by type if even has a informed type
            if (tag.equals("EVEN")) {
                String type = property.getPropertyValue("TYPE");
                str = type.isEmpty() ? str : type;
            }
            this.tableLabel = str;

            // Long label
            WordBuffer buffer = new WordBuffer(" - ");
            buffer.append(shortLabel);

            Property date = property.getProperty("DATE");
            if (date != null) {
                PropertyDate pDate = (PropertyDate) date;
                buffer.append(pDate.getDisplayValue());
            }

            Property place = property.getProperty("PLAC");
            if (place != null) {
                PropertyPlace pPlace = (PropertyPlace) place;
                buffer.append(pPlace.getCity());
            }
            this.longLabel = buffer.toString();
        }
        
        setText(shortLabel);
    }

    public String getTag() {
        return tag;
    }

    public String getTableLabel() {
        return tableLabel;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public String getLongLabel() {
        return longLabel;
    }
}
