/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2021 Ancestris
 * 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.gedcom;

/**
 * Property Create.
 * Keep track of creation time.
 *
 * @author zurga
 */
public class PropertyCreate extends PropertyChange {

    public static String CREA = "CREA";

    public PropertyCreate() {
        super(CREA);
    }
    
    public PropertyCreate(String tag) {
        super(tag);
    }
    
}
