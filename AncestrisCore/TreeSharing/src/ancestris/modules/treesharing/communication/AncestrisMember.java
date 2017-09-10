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
    private final String pipAddress;
    private final String pportAddress;
    private boolean usePrivate;

    /**
     * Constructor
     */
    public AncestrisMember(String name, String ipAddress, String portAddress, String pipAddress, String pportAddress) {
        this.allowed = true;
        this.name = name;
        this.ipAddress = ipAddress;
        this.portAddress = portAddress;
        this.pipAddress = pipAddress;
        this.pportAddress = pportAddress;
        this.usePrivate = false;
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
        return usePrivate ? pipAddress : ipAddress;
    }

    public String getPortAddress() {
        return usePrivate ? pportAddress : portAddress;
    }

    public String getxIPAddress() {
        return ipAddress;
    }

    public String getxPortAddress() {
        return portAddress;
    }

    public String getpIPAddress() {
        return pipAddress;
    }

    public String getpPortAddress() {
        return pportAddress;
    }

    public boolean getUsePrivate() {
        return usePrivate;
    }

    public void setUsePrivate(boolean flag) {
        usePrivate = flag;
    }

}
