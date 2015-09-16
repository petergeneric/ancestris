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
package ancestris.modules.treesharing.communication;

import java.io.Serializable;

/**
 *
 * @author frederic
 */
public class GedcomFam implements Serializable {
    public String gedcomName = "";
    public String entityID = "";
    
    public String husbID = "";
    public String husbLastName = "";
    public String husbFirstName = "";
    public String husbBirthDate = "";
    public String husbBirthPlace = "";
    public String husbDeathDate = "";
    public String husbDeathPlace = "";
    
    public String wifeID = "";
    public String wifeLastName = "";
    public String wifeFirstName = "";
    public String wifeBirthDate = "";
    public String wifeBirthPlace = "";
    public String wifeDeathDate = "";
    public String wifeDeathPlace = "";
    
    public String famMarrDate = "";
    public String famMarrPlace = "";
    
}
