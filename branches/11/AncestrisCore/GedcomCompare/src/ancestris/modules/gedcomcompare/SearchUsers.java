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
package ancestris.modules.gedcomcompare;

import ancestris.modules.gedcomcompare.tools.ConnectedUserFrame;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author frederic
 */
public class SearchUsers extends Thread {
    
    public static int SEARCH_TYPE_MAPS = 0;
    public static int SEARCH_TYPE_EVENTS = 1;
    
    private final GedcomCompareTopComponent owner;
    private volatile boolean stopRun;
    private int searchType;
    private ConnectedUserFrame user;
    
    public SearchUsers(GedcomCompareTopComponent tstc, int searchType, ConnectedUserFrame user) {
        this.owner = tstc;
        this.searchType = searchType;
        this.user = user;
    }
        
    @Override
    public void run() {
        stopRun = false;
        owner.setRotatingIcon(true);
        while (!stopRun) {
            owner.updateConnectedUsers(false);
            if (searchType == SEARCH_TYPE_MAPS) {
                getUsersMaps(user);
            }
            if (searchType == SEARCH_TYPE_EVENTS) {
                getUsersEvents(user);
            }
            stopGracefully();
        }
    }
    
    public void stopGracefully() {
        stopRun = true;
        owner.setRotatingIcon(false);
        owner.displaySearchedUser("");
    }
    
    
    /**
     * Search functions
     */
    private void getUsersMaps(ConnectedUserFrame user) {

        List<ConnectedUserFrame> copyOfUsers = null; 
        
        if (user == null){ 
            copyOfUsers = (List) ((ArrayList) owner.getConnectedUsers()).clone();     // Copy ancestris members to avoid concurrent access to the list while using it
        } else {
            copyOfUsers = new ArrayList<>();
            copyOfUsers.add(user);
        }

        // Loop on all users
        for (ConnectedUserFrame cuf : copyOfUsers) {
            
            // Skip if user not included or not active (btw: I am not mysef in the connected users)
            if (!cuf.isIncluded() || cuf.isComplete()) {
                continue;
            }
            if (!cuf.isActive()) {
                cuf.resetIcon(1);
                continue;
            }

            // Indicate which user is being probed
            owner.displaySearchedUser(cuf.getName());
            
            // Exchange Map : give Map and expect Map back throught the listen loop
            boolean ok = owner.getCommHandler().getMapCapsule(owner.getMapCapsule(), cuf);
            if (ok) {
                owner.addConnection();
            } else {
                cuf.resetIcon(1);
            }

            // Clear communication with user
            owner.getCommHandler().clearUserCommunication(cuf);
            
            

        }
    }
    
    /**
     * Search functions
     */
    private void getUsersEvents(ConnectedUserFrame user) {
        
            // Skip if user not included or not active (btw: I am not mysef in the connected users)
            if (!user.isIncluded() || !user.isActive()) {
                user.resetIcon(2);
                return;
            }

            // Indicate which cuf is being probed
            owner.displaySearchedUser(user.getName());
            
            // Exchange Map : give Map and expect Map back throught the listen loop
            boolean ok = owner.getCommHandler().getMapEventsCapsule(owner.getMapEventsCapsule(user), user);
            if (ok) {
                ok = owner.getCommHandler().getUserProfile(owner.getMyProfile(), user);
                if (ok) {
                    owner.addConnection();
                } else {
                    user.resetIcon(2);
                }
            } else {
                user.resetIcon(2);
            }
            
            // Clear communication with user
            owner.getCommHandler().clearUserCommunication(user);
            
    }
    
}
