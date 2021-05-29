package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.editors.genealogyeditor.utilities.PropertyTag2Name;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author dominique
 */
public class EventsTypeComboBoxModel extends DefaultComboBoxModel<String> {

    public EventsTypeComboBoxModel() {
        addElement("");
        
        // Individual events
        addElement(PropertyTag2Name.getTagName("ADOP"));
        addElement(PropertyTag2Name.getTagName("BIRT"));
        addElement(PropertyTag2Name.getTagName("BAPM"));
        addElement(PropertyTag2Name.getTagName("BARM"));
        addElement(PropertyTag2Name.getTagName("BASM"));
        addElement(PropertyTag2Name.getTagName("BLES"));
        addElement(PropertyTag2Name.getTagName("BURI"));
        addElement(PropertyTag2Name.getTagName("CENS"));
        addElement(PropertyTag2Name.getTagName("CHR"));
        addElement(PropertyTag2Name.getTagName("CHRA"));
        addElement(PropertyTag2Name.getTagName("CONF"));
        addElement(PropertyTag2Name.getTagName("CREM"));
        addElement(PropertyTag2Name.getTagName("DEAT"));
        addElement(PropertyTag2Name.getTagName("EMIG"));
        addElement(PropertyTag2Name.getTagName("FCOM"));
        addElement(PropertyTag2Name.getTagName("GRAD"));
        addElement(PropertyTag2Name.getTagName("IMMI"));
        addElement(PropertyTag2Name.getTagName("NATU"));
        addElement(PropertyTag2Name.getTagName("ORDN"));
        addElement(PropertyTag2Name.getTagName("RETI"));
        addElement(PropertyTag2Name.getTagName("PROB"));
        addElement(PropertyTag2Name.getTagName("WILL"));
        addElement(PropertyTag2Name.getTagName("EVEN"));

        // Family events
        addElement(PropertyTag2Name.getTagName("ANUL"));
        addElement(PropertyTag2Name.getTagName("CENS"));
        addElement(PropertyTag2Name.getTagName("DIV"));
        addElement(PropertyTag2Name.getTagName("DIVF"));
        addElement(PropertyTag2Name.getTagName("ENGA"));
        addElement(PropertyTag2Name.getTagName("MARR"));
        addElement(PropertyTag2Name.getTagName("MARB"));
        addElement(PropertyTag2Name.getTagName("MARC"));
        addElement(PropertyTag2Name.getTagName("MARL"));
        addElement(PropertyTag2Name.getTagName("MARS"));
        addElement(PropertyTag2Name.getTagName("EVEN"));
    }
}
