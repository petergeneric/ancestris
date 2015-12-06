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

import javax.swing.JLabel;

/**
 *
 * @author frederic
 */
public class EventLabel extends JLabel {

    private String tag = "";
    
    public EventLabel(String tag, String string) {
        this.tag = tag;
        setText(string);
    }

    public String getTag() {
        return tag;
    }
}
