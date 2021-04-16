/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015-2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.gedcomcompare.communication;

import java.io.Serializable;

/**
 *
 * @author frederic
 */
public class UserProfile implements Serializable {
    
    // Info
    public String name = "";
    public String email = "";
    public String city = "";
    public String country = "";
    public byte[] photoBytes = null;   // size 155x186
    public String privacy = "";

    // Connection info
    public String pseudo = "";
    public String ipAddress;
    public String portAddress;
    public String pipAddress;
    public String pportAddress;
    public boolean usePrivateIP;


    public UserProfile() {
    }
    
}
