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

import genj.edit.beans.ShortNameBean;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.io.Serializable;

/**
 *
 * @author daniel
 */
public class ANameBean extends ShortNameBean implements Serializable {

    public ANameBean() {
        super();
    }

    /**
     * set root gedcom property for this bean
     * @param property
     */
    public void setRoot(Property property) {
        setContext(property,new TagPath("NAME"),property.getProperty("NAME"));
    }

}
