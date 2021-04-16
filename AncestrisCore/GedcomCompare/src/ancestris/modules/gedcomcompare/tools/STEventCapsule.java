/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcomcompare.tools;

import java.io.Serializable;

/**
 *
 * @author frederic
 */
public class STEventCapsule implements Serializable {

    public String type;
    public String city;
    public double lat;
    public double lon;
    public String[] lastnames;
    public int year;
    
    public String entity;
    public String propertyName;
    public String propertyDate;
    public String propertyPlace;
    public String propertyString;
    

    public STEventCapsule() {
    }
}
