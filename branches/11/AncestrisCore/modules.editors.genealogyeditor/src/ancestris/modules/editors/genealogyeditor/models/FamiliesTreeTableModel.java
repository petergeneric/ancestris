package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.PropertyChild;
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
public class FamiliesTreeTableModel extends AbstractTreeTableModel {

    public static int FAMILY_CHILD = 1;
    public static int FAMILY_SPOUSE = 2;
    private int mFamilyTableType = FAMILY_CHILD;
    private static String[] familyChildColumnsName = {
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familyChild.column.ID.title"),
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familyChild.column.husband.title"),
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familyChild.column.wife.title"),
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familyChild.column.date.title"),};
    private static String[] familySpouseColumnsName = {
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familySpouse.column.ID.title"),
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familySpouse.column.husband.title"),
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familySpouse.column.wife.title"),
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familySpouse.column.date.title"),};
    private String[] familyColumnsName = familyChildColumnsName;
    private String mFemale = "";
    private String mMale = "";

    public FamiliesTreeTableModel() {
        this(FAMILY_CHILD);
    }

    public FamiliesTreeTableModel(int familyType) {
        super(new DefaultMutableTreeNode());
        mFamilyTableType = familyType;
        if (mFamilyTableType == FAMILY_CHILD) {
            familyColumnsName = familyChildColumnsName;
            mFemale = NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familyChild.female.title");
            mMale = NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familyChild.male.title");
        } else if (mFamilyTableType == FAMILY_SPOUSE) {
            familyColumnsName = familySpouseColumnsName;
            mFemale = NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familySpouse.female.title");
            mMale = NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familySpouse.male.title");
        }
    }

    @Override
    public int getColumnCount() {
        return familyColumnsName.length;
    }

    @Override
    public String getColumnName(int column) {
        if (column < familyColumnsName.length) {
            return familyColumnsName[column];
        } else {
            return "";
        }
    }

    @Override
    public Object getValueAt(Object object, int index) {
        if (object instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) object;
            if (dataNode.getUserObject() instanceof Fam) {
                Fam family = (Fam) dataNode.getUserObject();
                switch (index) {
                    case 0:
                        return family.getId();

                    case 1:
                        return family.getHusband() != null ? family.getHusband().getName() : "";

                    case 2:
                        return family.getWife() != null ? family.getWife().getName() : "";

                    case 3:
                        return family.getMarriageDate() != null
                                ? NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.family.wedding") + " " + family.getMarriageDate().getDisplayValue()
                                : "";

                    default:
                        return "";
                }
            } else if (dataNode.getUserObject() instanceof PropertyChild) {
                Indi child = ((PropertyChild)dataNode.getUserObject()).getChild();
                switch (index) {
                    case 0:
                switch (child.getSex()) {
                    case PropertySex.MALE:
                        return mMale + " (" + child.getId() + ")";
                    case PropertySex.FEMALE:
                        return mFemale + " (" + child.getId() + ")";
                    default:
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

    public void add(Fam family) {
        DefaultMutableTreeNode familyNode = new DefaultMutableTreeNode(family);

        for (PropertyChild childRef : family.getProperties(PropertyChild.class)) {
            familyNode.add(new DefaultMutableTreeNode(childRef));
        }

        ((DefaultMutableTreeNode) getRoot()).add(familyNode);
        modelSupport.fireNewRoot();
    }

    public void addAll(List<Fam> familiesList) {
        for (Fam family : familiesList) {
            DefaultMutableTreeNode familyNode = new DefaultMutableTreeNode(family);

            for (PropertyChild childRef : family.getProperties(PropertyChild.class)) {
                familyNode.add(new DefaultMutableTreeNode(childRef));
            }
            ((DefaultMutableTreeNode) getRoot()).add(familyNode);
        }
        modelSupport.fireNewRoot();
    }

    public void remove(DefaultMutableTreeNode dataNode) {
        TreeNode parent = dataNode.getParent();
        if (parent instanceof DefaultMutableTreeNode) {
            ((DefaultMutableTreeNode)parent).remove(dataNode);
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
