/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.api.editor;

import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import javax.swing.JComponent;

/**
 *
 * @author daniel
 */
public interface PlaceEditor {
    
    /**
     * Setup editor to edit or create a PropertyPlace.
     * @param place if not null, set editor to modify this place property. If
     * null, set editor to create a new place property for parent property
     * @param parent used only if place is null
     * @return 
     */
    public PlaceEditor setup(Property parent,PropertyPlace place);

    /**
     * Returns the graphic component for this editor.
     * This can be used to display an editor in a dialog window
     * @return 
     */
    public JComponent getEditorPanel();
    
    /**
     * Fire editor and return PropertyPlace object created or modified.
     * If no modification has been done (Cancel button), return null.
     * Be aware that this does not commit changes to gedcom
     * @return 
     */
    public PropertyPlace edit();

    /**
     * Commit editor changes. Return PropertyPlace 
     * object on success, null either
     * @return 
     */
    public PropertyPlace commit();
    
}
