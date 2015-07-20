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
    private final String ipAddress;
    private final String portAddress;

    /**
     * Constructor
     */
    public AncestrisMember(String name, String ipAddress, String portAddress) {
        this.allowed = true;
        this.name = name;
        this.ipAddress = ipAddress;
        this.portAddress = portAddress;
    }
    

    
    
    public void setAllowed(boolean b) {
        allowed = b;
    }
    
    public boolean isAllowed() {
        return allowed;
    }
    

    
    
    public String getMemberName() {
        return name;
    }

    public String getIPAddress() {
        return ipAddress;
    }

    public String getPortAddress() {
        return portAddress;
    }

}
