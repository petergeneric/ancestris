/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Zurga (zurga@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.genealogyeditor.utilities;

import javax.swing.ComboBoxModel;

/**
 * Filter interface for Ariès Table Panel.
 * @author Zurga
 */


public interface AriesFilterPanel {
    
    /**
     * Filter method callback for the toolbar.
     * @param columnname Column to search
     * @param searchText value to search
     */
    void filter(int columnIndex, String searchText);
    
    /**
     * Get the comboboxModel to use.
     * @return The ComboBoxModel
     */
    ComboBoxModel<String> getComboBoxModel();
    
    /**
     * Method to allow to save settings.
     */
    void saveFilterSettings();
    
}
