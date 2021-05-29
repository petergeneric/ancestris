package ancestris.modules.editors.genealogyeditor.models;

import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbBundle;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.util.ReferenceSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author dominique
 */
public class NameTypeComboBoxModel extends DefaultComboBoxModel<String> {

    final static Map<String, String> NAME_TYPE_COMBO_BOX = new HashMap<String, String>() {

        {
            put("aka", NbBundle.getMessage(NameTypeComboBoxModel.class, "NameTypeComboBoxModelModel.NamesType.aka"));
            put("birth", NbBundle.getMessage(NameTypeComboBoxModel.class, "NameTypeComboBoxModelModel.NamesType.birth"));
            put("immigrant", NbBundle.getMessage(NameTypeComboBoxModel.class, "NameTypeComboBoxModelModel.NamesType.immigrant"));
            put("maiden", NbBundle.getMessage(NameTypeComboBoxModel.class, "NameTypeComboBoxModelModel.NamesType.maiden"));
            put("married", NbBundle.getMessage(NameTypeComboBoxModel.class, "NameTypeComboBoxModelModel.NamesType.married"));
        }
    };
    
    private final Set<String> elements = new HashSet<>(4);

    public NameTypeComboBoxModel() {
        addDefault();
    }
    
    /**
     * Add NAME:TYPE value to the list of TYPE name in name editor.
     * @param gedcom The gedcom to find data.
     */
    public void setGedcom(final Gedcom gedcom) {
        removeAllElements();
 
        final ReferenceSet<String, Property> gedcomList =  gedcom.getReferenceSet("TYPE");
        
        // Loop on keys to find only TYPE related to NAME tag.
        for (String key : gedcomList.getKeys()) {
            final Set<Property> propList = gedcomList.getReferences(key);
            elements.addAll(propList.stream().filter(prop -> "NAME".equals(prop.getParent().getTag())).map(Property::getValue).collect(Collectors.toSet()));
        }
        
        addDefault();
        
    }
        
    private void addDefault() {
        elements.addAll(NAME_TYPE_COMBO_BOX.values());
        final List<String> elemList = elements.stream().sorted().collect(Collectors.toList());
        elemList.forEach(s -> addElement(s));
        
    }
}
