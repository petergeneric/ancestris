package ancestris.modules.gedcom.mergeentity.node;

import java.awt.Image;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 *
 * @author lemovice
 */
public class PropertyNode extends AbstractNode {

    public PropertyNode(Children create, Lookup singleton) {
        super(create, singleton);
    }

    @Override
    public String getDisplayName() {
        genj.gedcom.Property obj = getLookup().lookup(genj.gedcom.Property.class);
        if (obj != null) {
            return PropertyTag2Name.getTagName(obj.getTag()) + " " + obj.getValue();
        } else {
            return null;
        }
    }

    @Override
    public Image getIcon(int type) {
        genj.gedcom.Property obj = getLookup().lookup(genj.gedcom.Property.class);
        if (obj != null) {
            return PropertyTag2Icon.getIcon(obj.getTag());
        } else {
            return null;
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        genj.gedcom.Property obj = getLookup().lookup(genj.gedcom.Property.class);
        if (obj != null) {
            return PropertyTag2Icon.getIcon(obj.getTag());
        } else {
            return null;
        }
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        genj.gedcom.Property obj = getLookup().lookup(genj.gedcom.Property.class);
        try {
            Property valueProp = new PropertySupport.Reflection(obj, String.class, "getValue", null);

            valueProp.setName("value");

            set.put(valueProp);

        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault();
        }

        sheet.put(set);
        return sheet;
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
}
