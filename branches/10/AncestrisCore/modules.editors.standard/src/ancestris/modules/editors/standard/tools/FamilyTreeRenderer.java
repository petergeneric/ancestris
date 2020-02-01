/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard.tools;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author frederic
 */
public class FamilyTreeRenderer extends DefaultTreeCellRenderer {

    private final ImageIcon ICON_PARENTS  = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_Parents.png"));
    private final ImageIcon ICON_SIBLING = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_Sibling.png"));
    private final ImageIcon ICON_BROTHER = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_Brother.png"));
    private final ImageIcon ICON_SISTER = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_Sister.png"));
    private final ImageIcon ICON_MEUNKNOWN = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_MeUnknown.png"));
    private final ImageIcon ICON_MEMALE = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_MeMale.png"));
    private final ImageIcon ICON_MEFEMALE = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_MeFemale.png"));
    private final ImageIcon ICON_SPOUSE = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_Spouse.png"));
    private final ImageIcon ICON_CHILD = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_ChildUnknown.png"));
    private final ImageIcon ICON_BOY = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_ChildMale.png"));
    private final ImageIcon ICON_GIRL = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_ChildFemale.png"));
    
    private ImageIcon[] ICONS = { ICON_PARENTS, ICON_SIBLING, ICON_BROTHER, ICON_SISTER, ICON_MEUNKNOWN, ICON_MEMALE, ICON_MEFEMALE, ICON_SPOUSE, ICON_CHILD, ICON_BOY, ICON_GIRL };
    
    private String b, m, d;

    public FamilyTreeRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        NodeWrapper node = getNodeWrapper(value);
        setIcon(ICONS[node.getType()]);
        setText(node.getDisplayValue());
        if (node.isMe()) {
            setOpaque(true);
            setForeground(selected ? Color.BLUE : Color.BLACK); 
            setBackground(new Color(254, 255, 150));
        } else {
            setOpaque(false);
            this.setBorderSelectionColor(this.getBackgroundSelectionColor());
        }
        return this;
    }

    protected NodeWrapper getNodeWrapper(Object value) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
        return (NodeWrapper)(treeNode.getUserObject());
    }

}
