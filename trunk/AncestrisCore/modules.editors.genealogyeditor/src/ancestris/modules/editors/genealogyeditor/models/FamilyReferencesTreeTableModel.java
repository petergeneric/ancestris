package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyChild;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class FamilyReferencesTreeTableModel extends AbstractTreeTableModel {

    public static int FAMILY_CHILD = 1;
    public static int FAMILY_SPOUSE = 2;
    private int mFamilyTableType = FAMILY_CHILD;
    private static String[] familyChildColumnsName = {
        NbBundle.getMessage(FamilyReferencesTreeTableModel.class, "FamilyReferencesTreeTableModel.familyChild.column.ID.title"),
        NbBundle.getMessage(FamilyReferencesTreeTableModel.class, "FamilyReferencesTreeTableModel.familyChild.column.husband.title"),
        NbBundle.getMessage(FamilyReferencesTreeTableModel.class, "FamilyReferencesTreeTableModel.familyChild.column.wife.title"),
        NbBundle.getMessage(FamilyReferencesTreeTableModel.class, "FamilyReferencesTreeTableModel.familyChild.column.date.title"),};
    private static String[] familySpouseColumnsName = {
        NbBundle.getMessage(FamilyReferencesTreeTableModel.class, "FamilyReferencesTreeTableModel.familySpouse.column.ID.title"),
        NbBundle.getMessage(FamilyReferencesTreeTableModel.class, "FamilyReferencesTreeTableModel.familySpouse.column.husband.title"),
        NbBundle.getMessage(FamilyReferencesTreeTableModel.class, "FamilyReferencesTreeTableModel.familySpouse.column.wife.title"),
        NbBundle.getMessage(FamilyReferencesTreeTableModel.class, "FamilyReferencesTreeTableModel.familySpouse.column.date.title"),};
    private String[] familyColumnsName = familyChildColumnsName;
    private String mFemale = "";
    private String mMale = "";
    private Property mRoot = null;

    public FamilyReferencesTreeTableModel() {
        this(FAMILY_CHILD);
    }

    public FamilyReferencesTreeTableModel(int familyType) {
        super(new DefaultMutableTreeNode());
        mFamilyTableType = familyType;
        if (mFamilyTableType == FAMILY_CHILD) {
            familyColumnsName = familyChildColumnsName;
            mFemale = NbBundle.getMessage(FamilyReferencesTreeTableModel.class,
                    "FamilyReferencesTreeTableModel.familyChild.female.title");
            mMale = NbBundle.getMessage(FamilyReferencesTreeTableModel.class,
                    "FamilyReferencesTreeTableModel.familyChild.male.title");
        } else if (mFamilyTableType == FAMILY_SPOUSE) {
            familyColumnsName = familySpouseColumnsName;
            mFemale = NbBundle.getMessage(FamilyReferencesTreeTableModel.class,
                    "FamilyReferencesTreeTableModel.familySpouse.female.title");
            mMale = NbBundle.getMessage(FamilyReferencesTreeTableModel.class,
                    "FamilyReferencesTreeTableModel.familySpouse.male.title");
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
            Property property = (Property) dataNode.getUserObject();
            if (property instanceof PropertyXRef) {
                Entity entity = ((PropertyXRef) property).getTargetEntity();
                if (entity instanceof Fam) {
                    Fam family = (Fam) entity;
                    switch (index) {
                        case 0:
                            return family.getId();

                        case 1:
                            return family.getHusband() != null ? family.getHusband().getName() : "";

                        case 2:
                            return family.getWife() != null ? family.getWife().getName() : "";

                        case 3:
                            return family.getMarriageDate() != null
                                    ? NbBundle.getMessage(FamilyReferencesTreeTableModel.class,
                                            "FamilyReferencesTreeTableModel.family.wedding") + " " + family.getMarriageDate().getDisplayValue()
                                    : "";

                        default:
                            return "";
                    }
                } else if (entity instanceof Indi) {
                    Indi child = (Indi) entity;
                    switch (index) {
                        case 0:
                            if (child == mRoot) {
                                return child.getId();
                            } else {
                                switch (child.getSex()) {
                                    case PropertySex.MALE:
                                        return mMale + " (" + child.getId() + ")";
                                    case PropertySex.FEMALE:
                                        return mFemale + " (" + child.getId() + ")";
                                    default:
                                        return child.getId();
                                }
                            }

                        case 1:
                            return child.getFirstName();

                        case 2:
                            return child.getLastName();

                        case 3:
                            return child.getBirthDate() != null
                                    ? NbBundle.getMessage(
                                            FamilyReferencesTreeTableModel.class,
                                            "FamilyReferencesTreeTableModel.child.birth") + " " + child.getBirthDate().getDisplayValue()
                                    : "";

                        default:
                            return "";
                    }
                } else {
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

    public void setRoot(Property root) {
        mRoot = root;
    }

    public void add(PropertyXRef familyRef) {
        DefaultMutableTreeNode familyNode = new DefaultMutableTreeNode(familyRef);
        Entity entity = familyRef.getTargetEntity();
        if (entity instanceof Fam) {
            Fam family = (Fam) entity;
            for (PropertyChild childRef : family.getProperties(PropertyChild.class)) {
                familyNode.add(new DefaultMutableTreeNode(childRef));
            }
        }
        ((DefaultMutableTreeNode) getRoot()).add(familyNode);
        modelSupport.fireNewRoot();
    }

    public void addAll(List<? extends PropertyXRef> familiesList) {
        for (PropertyXRef familyRef : familiesList) {
            DefaultMutableTreeNode familyNode = new DefaultMutableTreeNode(familyRef);
            Entity entity = familyRef.getTargetEntity();
            if (entity instanceof Fam) {
                Fam family = (Fam) entity;
                for (PropertyChild childRef : family.getProperties(PropertyChild.class)) {
                    familyNode.add(new DefaultMutableTreeNode(childRef));
                }
                ((DefaultMutableTreeNode) getRoot()).add(familyNode);
            }
            modelSupport.fireNewRoot();
        }
    }

    public void remove(DefaultMutableTreeNode dataNode) {
        TreeNode parent = dataNode.getParent();
        if (parent instanceof DefaultMutableTreeNode) {
            ((DefaultMutableTreeNode) parent).remove(dataNode);
        }
        modelSupport.fireNewRoot();
    }

    public void clear() {
        if (root instanceof DefaultMutableTreeNode) {
            ((DefaultMutableTreeNode) root).removeAllChildren();
        }
        modelSupport.fireNewRoot();
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }
}
