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

import java.util.List;

/**
 *
 * @author frederic
 */
public class Comm {
    
    /**
     * Register on Ancestris server that I am ready to share 
     * 
     * @param myName
     * @param myAccess
     * @return 
     */
    public boolean registerMe(String myName, String myAccess) {
        // Connect to Ancestris server
        
        // Provides myName and my access
        
        // Identifies if registration is successful (server is on, name does not already exist, etc.)
        
        // Close connection with server
        
        return true;
    }
    

    /**
     * Tell Ancestris server that I am no longer ready to share
     * 
     * @param myName
     * @return 
     */
    public boolean unregisterMe(String myName) {
        // Connect to Ancestris server
        
        // Request unregistration of myName
        
        // Identifies if unregistration is successful (server is on, name has been removed, etc.)
        
        // Close connection with server
        
        return true;
    }
    

    /**
     * Identify the list of currently sharing friends from the ancestris server (crypted communication)
     * 
     * @return all Ancestris friends sharing something
     */
    public List<AncestrisFriend> getAncestrisFriends() {
        // Connect to Ancestris server
        
        // Collect list of Ancestris friends (registered name and access)
        
        // Close connection with server
        
        // Return list
        
        return null;
    }
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Opens door allowing friends to connect to me
     * 
     * @return 
     */
    public boolean startListeningtoFriends() {
        return true;
    }


    /**
     * Closes door stopping friends from listening to me
     * 
     * @return 
     */
    public boolean stopListeningtoFriends() {
        return true;
    }

    
    /**
     * Establish a 1-to-1 communications with a sharing Ancestris friend (crypted communication)
     *      
     * @param friend
     * @return  true if connection established
     *          false if sharing criteria not matching request, or if connection is not working
     */
    public boolean connectToAncestrisFriend(AncestrisFriend friend) {
        // Connect to ancestris program of friend
        
        return true;
    }
    
    
    

}
