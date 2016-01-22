/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.util;

import com.sun.javafx.tk.Toolkit;
import javafx.scene.control.Menu;

/**
 *
 * @author frederic
 */
public class MacMenu {
    
    public void MacMenu() {
        
        // Get the toolkit
        //MenuToolkit tk = MenuToolkit.toolkit();
		if (!Toolkit.getToolkit().getSystemMenu().isSupported()) {
            return;
        }

        try {
//            return new MenuToolkit(new TKSystemMenuAdapter(), new MacApplicationAdapter());
//            systemMenuAdapter = tkSystemMenuAdapter;
//            applicationAdapter = macApplicationAdapter;
        } catch (Exception e) {
        }
        

        // Create the default Application menu
//        Menu defaultApplicationMenu = tk.createDefaultApplicationMenu("test");

        // Update the existing Application menu
//        tk.setApplicationMenu(defaultApplicationMenu);

        // Since we now have a reference to the menu, we can rename items
//        defaultApplicationMenu.getItems().get(1).setText("Hide all the otters");
    }

}
