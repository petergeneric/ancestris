package ancestris.modules.editors.genealogyeditor.models;

import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class EventsRoleComboBoxModel extends DefaultComboBoxModel<String> {

    // ROLE_IN_EVENT:=
    // [ CHIL | HUSB | WIFE | MOTH | FATH | SPOU | (<ROLE_DESCRIPTOR>) ]    
    public EventsRoleComboBoxModel() {
        addElement(NbBundle.getMessage(EventsRoleComboBoxModel.class, "EventsRoleComboBoxModel.roleInEvent.USER"));
        addElement(NbBundle.getMessage(EventsRoleComboBoxModel.class, "EventsRoleComboBoxModel.roleInEvent.CHIL"));
        addElement(NbBundle.getMessage(EventsRoleComboBoxModel.class, "EventsRoleComboBoxModel.roleInEvent.HUSB"));
        addElement(NbBundle.getMessage(EventsRoleComboBoxModel.class, "EventsRoleComboBoxModel.roleInEvent.WIFE"));
        addElement(NbBundle.getMessage(EventsRoleComboBoxModel.class, "EventsRoleComboBoxModel.roleInEvent.MOTH"));
        addElement(NbBundle.getMessage(EventsRoleComboBoxModel.class, "EventsRoleComboBoxModel.roleInEvent.FATH"));
        addElement(NbBundle.getMessage(EventsRoleComboBoxModel.class, "EventsRoleComboBoxModel.roleInEvent.SPOU"));
    }
}
