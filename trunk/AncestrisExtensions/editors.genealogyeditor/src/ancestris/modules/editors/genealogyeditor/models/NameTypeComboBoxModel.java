package ancestris.modules.editors.genealogyeditor.models;

import java.util.LinkedHashMap;
import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class NameTypeComboBoxModel extends DefaultComboBoxModel<String> {

    LinkedHashMap<String, String> nameTypeComboBox = new LinkedHashMap<String, String>() {

        {
            put("aka", NbBundle.getMessage(NameTypeComboBoxModel.class, "NameTypeComboBoxModelModel.NamesType.aka"));
            put("birth", NbBundle.getMessage(NameTypeComboBoxModel.class, "NameTypeComboBoxModelModel.NamesType.birth"));
            put("immigrant", NbBundle.getMessage(NameTypeComboBoxModel.class, "NameTypeComboBoxModelModel.NamesType.immigrant"));
            put("maiden", NbBundle.getMessage(NameTypeComboBoxModel.class, "NameTypeComboBoxModelModel.NamesType.maiden"));
            put("married", NbBundle.getMessage(NameTypeComboBoxModel.class, "NameTypeComboBoxModelModel.NamesType.married"));
        }
    };

    public NameTypeComboBoxModel() {
        for (String key : nameTypeComboBox.keySet()) {
            addElement(nameTypeComboBox.get(key));
        }
    }
}
