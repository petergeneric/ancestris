package ancestris.modules.editors.genealogyeditor.models;

import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class SexComboBoxModel extends DefaultComboBoxModel<String> {

    public SexComboBoxModel() {
        addElement(NbBundle.getMessage(SexComboBoxModel.class, "SexComboBoxModel.SexType.UNKNOWN"));
        addElement(NbBundle.getMessage(SexComboBoxModel.class, "SexComboBoxModel.SexType.MALE"));
        addElement(NbBundle.getMessage(SexComboBoxModel.class, "SexComboBoxModel.SexType.FEMALE"));
    }
}
