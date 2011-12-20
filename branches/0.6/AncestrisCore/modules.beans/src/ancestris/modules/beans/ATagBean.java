/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.beans;

import genj.gedcom.Gedcom;
import java.io.Serializable;
import javax.swing.JLabel;

/**
 *
 * @author daniel
 */
public class ATagBean extends JLabel implements Serializable {

    private String tag;

    public ATagBean() {
        super();
    }

    public String getTag() {
        return tag;
    }

    /**
     * Set the value of tag
     *
     * @param tag new value of tag
     */
    public void setTag(String tag) {
        this.tag = tag;
        setText(Gedcom.getName(tag));
    }
}
