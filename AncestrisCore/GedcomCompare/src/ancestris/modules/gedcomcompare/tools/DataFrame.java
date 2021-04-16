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

import ancestris.modules.gedcomcompare.GedcomCompareTopComponent;
import java.awt.Color;
import javax.swing.JInternalFrame;

/**
 *
 * @author frederic
 */
public class DataFrame extends JInternalFrame {

    public static int GEDCOM_TYPE_LOCAL_MAIN = 0;
    public static int GEDCOM_TYPE_LOCAL_OTHER = 1;
    public static int GEDCOM_TYPE_REMOTE = 2;
    public static int GEDCOM_TYPE_REMOTE_INACTIVE = 3;

    private static Color bgcolors[] = new Color[] { new Color(205,226,255), new Color(161,255,165), new Color(255,199,205), new Color(200,200,200) };  // pastels: blue, green, red, light grey
    
    public GedcomCompareTopComponent owner;
    public int type;
    
    public DataFrame(String name) {
        super(name);
    }
    
    public void updateColor() {
        getContentPane().setBackground(bgcolors[type]);
    }
    
    public boolean isMain() {
        return type == GEDCOM_TYPE_LOCAL_MAIN;
    }

    public void setMain(boolean set) {
        if (set) {
            owner.checkSharingisOff();
            for (LocalGedcomFrame gedcom : owner.getLocalGedcoms()) {
                if (this == gedcom) {
                    type = GEDCOM_TYPE_LOCAL_MAIN;
                } else {
                    gedcom.setMain(false);
                }
            }
            owner.createComparisonFrames();
            owner.rearrangeWindows(false);
        } else {
            type = GEDCOM_TYPE_LOCAL_OTHER;
        }
        
        updateColor();
    }

    public boolean isRemote() {
        return type == GEDCOM_TYPE_REMOTE;
    }
    
    public void focusOther() {
        owner.setFocusToComparisonFrame(this);
    }

    
    
}