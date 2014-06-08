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
public class ChlidrenTreeTableModel extends AbstractTreeTableModel {

    private static String[] mChildrenColumnsName = {
        NbBundle.getMessage(FamiliesTreeTableModel.class, "ChlidrenTreeTableModel.familySpouse.column.ID.title"),
        "",
        "",
        NbBundle.getMessage(FamiliesTreeTableModel.class, "ChlidrenTreeTableModel.familySpouse.column.date.title")};
    private String mFemale = NbBundle.getMessage(FamiliesTreeTableModel.class, "ChlidrenTreeTableModel.daughter.title");
    private String mMale = NbBundle.getMessage(FamiliesTreeTableModel.class, "ChlidrenTreeTableModel.sun.title");

    public ChlidrenTreeTableModel() {
        super(new DefaultMutableTreeNode());
    }

    @Override
    public int getColumnCount() {
        return mChildrenColumnsName.length;
    }

    @Override
    public String getColumnName(int column) {
        if (column < mChildrenColumnsName.length) {
            return mChildrenColumnsName[column];
        } else {
            return "";
        }
    }

    @Override
    public Object getValueAt(Object object, int index) {
        if (object instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) object;
            if (dataNode.getUserObject() instanceof Indi) {
                Indi child = (Indi) dataNode.getUserObject();
                switch (index) {
                    case 0:
                        if (child.getSex() == PropertySex.MALE) {
                            return mMale + " (" + child.getId() + ")";
                        } else if (child.getSex() == PropertySex.FEMALE) {
                            return mFemale + " (" + child.getId() + ")";
                        } else {
                            return child.getId();
                        }

                    case 1:
                        return child.getFirstName();

                    case 2:
                        return child.getLastName();

                    case 3:
                        return child.getBirthDate() != null
                                ? NbBundle.getMessage(ChlidrenTreeTableModel.class, "FamiliesTreeTableModel.child.birth") + " " + child.getBirthDate().getDisplayValue()
                                : "";

                    default:
                        return "";
                }
            } else if (dataNode.getUserObject() instanceof Fam) {
                Fam family = ((Fam) dataNode.getUserObject());
                switch (index) {
                    case 0:
                        return family.getId();

                    case 1:
                        return family.getHusband() != null ? family.getHusband().getName() : "";

                    case 2:
                        return family.getWife() != null ? family.getWife().getName() : "";

                    case 3:
                        return family.getMarriageDate() != null
                                ? NbBundle.getMessage(ChlidrenTreeTableModel.class, "FamiliesTreeTableModel.family.wedding") + " " + family.getMarriageDate().getDisplayValue()
                                : "";

                    default:
                        return "";
                }
            } else if (dataNode.getUserObject() instanceof PropertyChild) {
                Indi child = ((PropertyChild) dataNode.getUserObject()).getChild();
                switch (index) {
                    case 0:
                        if (child.getSex() == PropertySex.MALE) {
                            return mMale + " (" + child.getId() + ")";
                        } else if (child.getSex() == PropertySex.FEMALE) {
                            return mFemale + " (" + child.getId() + ")";
                        } else {
                            return child.getId();
                        }

                    case 1:
                        return child.getFirstName();

                    case 2:
                        return child.getLastName();

                    case 3:
                        return child.getBirthDate() != null
                                ? NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.child.birth") + " " + child.getBirthDate().getDisplayValue()
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

    public void add(PropertyChild children) {
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(children);

        for (PropertyFamilySpouse familyRef : children.getProperties(PropertyFamilySpouse.class)) {
            childNode.add(new DefaultMutableTreeNode(familyRef));
        }

        ((DefaultMutableTreeNode) getRoot()).add(childNode);
        modelSupport.fireNewRoot();
    }

    public void addAll(List<PropertyChild> childrenList) {
        for (PropertyChild propertyChild : childrenList) {
            Indi child = propertyChild.getChild();
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

            for (PropertyFamilySpouse familyRef : child.getProperties(PropertyFamilySpouse.class)) {
                Fam family = familyRef.getFamily();
                DefaultMutableTreeNode familyNode = new DefaultMutableTreeNode(family);

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

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }
}
