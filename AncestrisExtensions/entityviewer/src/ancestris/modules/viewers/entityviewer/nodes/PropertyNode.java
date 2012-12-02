package ancestris.modules.viewers.entityviewer.nodes;

import ancestris.modules.gedcom.utilities.PropertyTag2Icon;
import ancestris.modules.gedcom.utilities.PropertyTag2Name;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyFamilyChild;
import genj.gedcom.PropertyFamilySpouse;
import genj.gedcom.PropertyXRef;
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
            if (obj instanceof PropertyAssociation) {
                return PropertyTag2Name.getTagName(obj.getTag()) + " " + obj.getDisplayValue();
            } else if (obj instanceof PropertyFamilySpouse) {
                return PropertyTag2Name.getTagName(obj.getTag()) +
                        " @" + ((PropertyFamilySpouse) obj).getFamily().getId() +
                        "@ " + ((PropertyFamilySpouse) obj).getFamily().getHusband().getName() +
                        " & " + ((PropertyFamilySpouse) obj).getFamily().getWife().getName();
            } else if (obj instanceof PropertyFamilyChild) {
                return PropertyTag2Name.getTagName(obj.getTag()) +
                        " @" + ((PropertyFamilyChild) obj).getFamily().getId() +
                        "@ " + ((PropertyFamilyChild) obj).getFamily().getHusband().getName() +
                        " & " + ((PropertyFamilyChild) obj).getFamily().getWife().getName();
            } else if (obj instanceof PropertyXRef) {
                return PropertyTag2Name.getTagName(obj.getTag()) + " " + obj.getDisplayValue();
            } else {
                return PropertyTag2Name.getTagName(obj.getTag()) + " " + obj.getDisplayValue();
            }
        } else {
            return null;
        }
    }

    @Override
    public Image getIcon(int type) {
        genj.gedcom.Property obj = getLookup().lookup(genj.gedcom.Property.class);
        if (obj != null) {
            return PropertyTag2Icon.getImage(obj.getTag());
        } else {
            return null;
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        genj.gedcom.Property obj = getLookup().lookup(genj.gedcom.Property.class);
        if (obj != null) {
            return PropertyTag2Icon.getImage(obj.getTag());
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
