package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
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
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familyChild.column.weddingDate.title"),};
    private static String[] familySpouseColumnsName = {
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familySpouse.column.ID.title"),
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familySpouse.column.husband.title"),
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familySpouse.column.wife.title"),
        NbBundle.getMessage(FamiliesTreeTableModel.class, "FamiliesTreeTableModel.familySpouse.column.weddingDate.title"),};
    private String[] familyColumnsName = familyChildColumnsName;

    public FamiliesTreeTableModel() {
        this(FAMILY_CHILD);
    }

    public FamiliesTreeTableModel(int familyType) {
        super(new DefaultMutableTreeNode());
        mFamilyTableType = familyType;
        if (mFamilyTableType == FAMILY_CHILD) {
            familyColumnsName = familyChildColumnsName;
        } else if (mFamilyTableType == FAMILY_SPOUSE) {
            familyColumnsName = familySpouseColumnsName;
        }
    }

    @Override
    public int getColumnCount() {
        return familyColumnsName.length;
    }

    @Override
    public String getColumnName(int column) {
        if (column < familyColumnsName.length) {
            return (String) familyColumnsName[column];
        } else {
            return "";
        }
    }

    @Override
    public Object getValueAt(Object object, int index) {
        if (object instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode) object;
            Entity entity = (Entity) dataNode.getUserObject();
            if (entity instanceof Fam) {
                Fam family = (Fam) entity;
                switch (index) {
                    case 0:
                        return family.getId();

                    case 1:
                        return family.getHusband().getName();

                    case 2:
                        return family.getWife().getName();

                    case 3:
                        return family.getMarriageDate() != null ? family.getMarriageDate().toString() : "";

                    default:
                        return "";
                }
            } else if (entity instanceof Indi) {
                Indi child = (Indi) entity;
                switch (index) {
                    case 0:
                        return child.getId();

                    case 1:
                        return child.getFirstName();

                    case 2:
                        return child.getLastName();

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

        for (Indi child : family.getChildren()) {
            familyNode.add(new DefaultMutableTreeNode(child));
        }

        ((DefaultMutableTreeNode) getRoot()).add(familyNode);
        modelSupport.fireNewRoot();
    }

    public void addAll(List<Fam> familiesList) {
        for (Fam family : familiesList) {
            DefaultMutableTreeNode familyNode = new DefaultMutableTreeNode(family);

            for (Indi child : family.getChildren()) {
                familyNode.add(new DefaultMutableTreeNode(child));
            }
            ((DefaultMutableTreeNode) getRoot()).add(familyNode);
        }
        modelSupport.fireNewRoot();
    }

    public Fam remove(int row) {
        return null;
    }

    public void update(List<Fam> familiesList) {
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }
}
