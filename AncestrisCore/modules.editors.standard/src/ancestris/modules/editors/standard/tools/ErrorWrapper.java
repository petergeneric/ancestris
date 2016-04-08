/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.editors.standard.tools;

import genj.gedcom.Property;

/**
 *
 * @author frederic
 */
public class ErrorWrapper {
    
    private Property propertyInError = null;
    private String message = "";
    
    
    public ErrorWrapper(Property property, String msg) {
        this.propertyInError = property;
        this.message = msg;
    }
    
    public String getMessage() {
        return message;

    }

}
