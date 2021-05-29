package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.PropertyChild;
import genj.gedcom.PropertyFamilySpouse;
import genj.gedcom.PropertySex;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class ChildrenTreeTableModel extends AbstractTreeTableModel {

    private static final String[] CHILDREN_COLUMN_NAME = {
        NbBundle.getMessage(ChildrenTreeTableModel.class, "ChildrenTreeTableModel.familySpouse.column.ID.title"),
        "",
        "",
        NbBundle.getMessage(ChildrenTreeTableModel.class, "ChildrenTreeTableModel.familySpouse.column.date.title")};

    public ChildrenTreeTableModel() {
        super(new DefaultMutableTreeNode());
    }

    @Override
    public int getColumnCount() {
        return CHILDREN_COLUMN_NAME.length;
    }

    @Override
    public String getColumnName(int column) {
        if (column < CHILDREN_COLUMN_NAME.length) {
            return CHILDREN_COLUMN_NAME[column];
        } else {
            return "";
        }
    }
    
    public String[] getColumnsName() {
        return CHILDREN_COLUMN_NAME;
    }

    @Override
    public Object getValueAt(Object object, int index) {
        if (object instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) object;
            if (dataNode.getUserObject() instanceof PropertyChild) {
                TreeNode parent = dataNode.getParent();
                if (parent instanceof DefaultMutableTreeNode) {
                    if (((DefaultMutableTreeNode) parent).isRoot()) { // Son
                        Indi child = ((PropertyChild) dataNode.getUserObject()).getChild();
                        switch (index) {
                            case 0:
                                switch (child.getSex()) {
                                    case PropertySex.MALE:
                                        return NbBundle.getMessage(ChildrenTreeTableModel.class, "ChildrenTreeTableModel.son.title")
                                                + " (" + child.getId() + ")";
                                    case PropertySex.FEMALE:
                                        return NbBundle.getMessage(ChildrenTreeTableModel.class, "ChildrenTreeTableModel.daughter.title")
                                                + " (" + child.getId() + ")";
                                    default:
                                        return child.getId();
                                }

                            case 1:
                                return child.getFirstName();

                            case 2:
                                return child.getLastName();

                            case 3:
                                return child.getBirthDate() != null
                                        ? NbBundle.getMessage(ChildrenTreeTableModel.class, "ChildrenTreeTableModel.child.birth") + " " + child.getBirthDate().getDisplayValue()
                                        : "";

                            default:
                                return "";
                        }
                    } else { // Grandson
                        Indi child = ((PropertyChild) dataNode.getUserObject()).getChild();
                        switch (index) {
                            case 0:
                                switch (child.getSex()) {
                                    case PropertySex.MALE:
                                        return NbBundle.getMessage(ChildrenTreeTableModel.class, "ChildrenTreeTableModel.grandson.title")
                                                + " (" + child.getId() + ")";
                                    case PropertySex.FEMALE:
                                        return NbBundle.getMessage(ChildrenTreeTableModel.class, "ChildrenTreeTableModel.granddaughter.title")
                                                + " (" + child.getId() + ")";
                                    default:
                                        return child.getId();
                                }

                            case 1:
                                return child.getFirstName();

                            case 2:
                                return child.getLastName();

                            case 3:
                                return child.getBirthDate() != null
                                        ? NbBundle.getMessage(ChildrenTreeTableModel.class, "ChildrenTreeTableModel.child.birth") + " " + child.getBirthDate().getDisplayValue()
                                        : "";

                            default:
                                return "";
                        }
                    }
                } else {
                    return object.getClass().getCanonicalName();
                }
            } else if (dataNode.getUserObject() instanceof PropertyFamilySpouse) {
                Fam family = ((PropertyFamilySpouse) dataNode.getUserObject()).getFamily();
                switch (index) {
                    case 0:
                        return family.getId();

                    case 1:
                        return family.getHusband() != null ? family.getHusband().getName() : "";

                    case 2:
                        return family.getWife() != null ? family.getWife().getName() : "";

                    case 3:
                        return family.getMarriageDate() != null
                                ? NbBundle.getMessage(ChildrenTreeTableModel.class, "ChildrenTreeTableModel.family.wedding") + " " + family.getMarriageDate().getDisplayValue()
                                : "";

                    default:
                        return "";
                }
            } else {
                return "";
            }
        } else {
            return object.getClass().getCanonicalName();
        }
    }

    @Override
    public Object getChild(Object object, int index) {

        if (object instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode nodes = (DefaultMutableTreeNode) object;
            return nodes.getChildAt(index);
        }
        return null;
    }

    @Override
    public int getChildCount(Object object) {
        if (object instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode nodes = (DefaultMutableTreeNode) object;
            return nodes.getChildCount();
        }
        return 0;
    }

    @Override
    public int getIndexOfChild(Object o, Object o1) {
        return 0;
    }

    public void add(PropertyChild child) {
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

        for (PropertyFamilySpouse familyRef : child.getProperties(PropertyFamilySpouse.class)) {
            childNode.add(new DefaultMutableTreeNode(familyRef));
        }

        ((DefaultMutableTreeNode) getRoot()).add(childNode);
        modelSupport.fireNewRoot();
    }

    public void addAll(List<PropertyChild> children) {
        for (PropertyChild child : children) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

            for (PropertyFamilySpouse familyRef : child.getChild().getProperties(PropertyFamilySpouse.class)) {
                Fam family = familyRef.getFamily();
                DefaultMutableTreeNode familyNode = new DefaultMutableTreeNode(familyRef);

                childNode.add(familyNode);

                for (PropertyChild childRef : family.getProperties(PropertyChild.class)) {
                    familyNode.add(new DefaultMutableTreeNode(childRef));
                }
            }

            ((DefaultMutableTreeNode) getRoot()).add(childNode);
        }
        modelSupport.fireNewRoot();
    }

    public void remove(DefaultMutableTreeNode dataNode) {
        TreeNode parent = dataNode.getParent();
        if (parent instanceof DefaultMutableTreeNode) {
            ((DefaultMutableTreeNode) parent).remove(dataNode);
        }
        modelSupport.fireNewRoot();
    }

    public void clear() {
        Object localRoot = getRoot();
        if (localRoot instanceof DefaultMutableTreeNode) {
            ((DefaultMutableTreeNode) localRoot).removeAllChildren();
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }
}
