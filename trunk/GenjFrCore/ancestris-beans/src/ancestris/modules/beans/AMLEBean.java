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

import genj.edit.beans.MLEBean;
import genj.gedcom.Property;
import genj.gedcom.PropertyMultilineValue;

/**
 *
 * @author daniel
 */
public class AMLEBean extends MLEBean implements ABean {

    public AMLEBean() {
        super();
    }

    private String tag = "";

    /**
     * Get the value of tag
     *
     * @return the value of tag
     */
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
    }

    /**
     * set root gedcom property for this bean
     * @param property
     */
    @Override
    public AMLEBean setRoot(Property root) {
        super.setContext(root, tag);
        return this;
    }
}
