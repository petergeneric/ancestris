package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.gedcom.utilities.PropertyTag2Name;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author dominique
 */
public class ConfidenceLevelComboBoxModel extends DefaultComboBoxModel<String> {

    public ConfidenceLevelComboBoxModel() {
        addElement("");
        
        addElement("High");
        addElement("Medium");
        addElement("Low");
    }
}
