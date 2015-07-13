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

/**
 *
 * @author frederic
 */
public class AncestrisMember {
    
    private boolean allowed;
    private final String name;
    private final String access;

    /**
     * Constructor
     */
    public AncestrisMember(String name, String access) {
        this.allowed = true;
        this.name = name;
        this.access = access;
    }
    

    public String getMemberName() {
        return name;
    }

    public String getAccess() {
        return access;
    }

    public boolean isAllowed() {
        return allowed;
    }
    
    public void setAllowed(boolean b) {
        allowed = b;
    }
    
}
