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
package ancestris.modules.treesharing;

import javax.swing.ImageIcon;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class SearchSharedTrees extends Thread {
    
    private final TreeSharingTopComponent owner;
    private volatile boolean stopRun;
    private final ImageIcon ROTATING_ICON_PATH = new ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/rotating24.gif"));

    public SearchSharedTrees(TreeSharingTopComponent tstc) {
        this.owner = tstc;
    }
        
        
    @Override
    public void run() {
        stopRun = false;
        owner.setRotatingIcon(ROTATING_ICON_PATH, NbBundle.getMessage(SearchSharedTrees.class, "TIP_SearchInProgress"));
        while (!stopRun) {
            // do something long
        }
    }

    public void stopGracefully() {
        stopRun = true;
        owner.setRotatingIcon(null, null);
    }

}
