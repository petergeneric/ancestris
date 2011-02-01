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

import genj.edit.beans.ChoiceBean;
import genj.gedcom.Property;
import java.io.Serializable;

/**
 *
 * @author daniel
 */
//public class APlaceBean extends PlaceBean implements Serializable {
public class APlaceBean extends ChoiceBean implements Serializable {

    private static final String PATH = "PLAC";

    public APlaceBean() {
        super();
    }

    /**
     * set root gedcom property for this bean
     * @param property
     */
    @Override
    public APlaceBean setContext(Property root, String tag) {
        super.setContext(root, tag + ":" + PATH);
        return this;
    }
}
