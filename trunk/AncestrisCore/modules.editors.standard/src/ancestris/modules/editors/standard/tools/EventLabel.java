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

import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.util.WordBuffer;
import javax.swing.JLabel;

/**
 *
 * @author frederic
 */
public class EventLabel extends JLabel {

    private String tag = "";
    private String shortLabel = "";
    private String longLabel = "";
    
    public EventLabel(Property property) {
        this.tag = property.getTag();
        
        String str = property.getPropertyName();
        if (str.contains(" ")) {
            this.shortLabel = str.substring(0, str.indexOf(" ")); // only take first word
        } else {
            this.shortLabel = str;
        }
        setText(shortLabel);
        
        WordBuffer buffer = new WordBuffer(" - ");

        buffer.append(shortLabel);

        Property date = property.getProperty("DATE");
        if (date != null) {
            PropertyDate pDate = (PropertyDate) date;
            buffer.append(pDate.getStart().getValue());
        }

        Property place = property.getProperty("PLAC");
        if (place != null) {
            PropertyPlace pPlace = (PropertyPlace) place;
            buffer.append(pPlace.getCity());
        }
        
        longLabel = buffer.toString();
    }

    public String getTag() {
        return tag;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public String getLongLabel() {
        return longLabel;
    }
}
