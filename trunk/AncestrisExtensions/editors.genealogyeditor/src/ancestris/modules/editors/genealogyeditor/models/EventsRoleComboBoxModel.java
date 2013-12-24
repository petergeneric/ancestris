package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.gedcom.utilities.PropertyTag2Name;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author dominique
 */
public class EventsRoleComboBoxModel extends DefaultComboBoxModel<String> {

    // ROLE_IN_EVENT:=
    // [ CHIL | HUSB | WIFE | MOTH | FATH | SPOU | (<ROLE_DESCRIPTOR>) ]    
    public EventsRoleComboBoxModel() {
        addElement("");
        
        addElement(PropertyTag2Name.getTagName("CHIL"));
        addElement(PropertyTag2Name.getTagName("HUSB"));
        addElement(PropertyTag2Name.getTagName("WIFE"));
        addElement(PropertyTag2Name.getTagName("MOTH"));
        addElement(PropertyTag2Name.getTagName("FATH"));
        addElement(PropertyTag2Name.getTagName("SPOU"));
    }
}
