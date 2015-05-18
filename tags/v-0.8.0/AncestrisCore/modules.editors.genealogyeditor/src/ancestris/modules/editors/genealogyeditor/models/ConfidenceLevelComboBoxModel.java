package ancestris.modules.editors.genealogyeditor.models;

import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class ConfidenceLevelComboBoxModel extends DefaultComboBoxModel<String> {

    public ConfidenceLevelComboBoxModel() {
        addElement("");
        addElement(NbBundle.getMessage(ConfidenceLevelComboBoxModel.class, "ConfidenceLevelComboBoxModel.confidenceLevel.unreliable"));
        addElement(NbBundle.getMessage(ConfidenceLevelComboBoxModel.class, "ConfidenceLevelComboBoxModel.confidenceLevel.questionable"));
        addElement(NbBundle.getMessage(ConfidenceLevelComboBoxModel.class, "ConfidenceLevelComboBoxModel.confidenceLevel.secondary"));
        addElement(NbBundle.getMessage(ConfidenceLevelComboBoxModel.class, "ConfidenceLevelComboBoxModel.confidenceLevel.direct"));
    }
}
