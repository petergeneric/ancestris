package ancestris.modules.gedcom.searchduplicates;

import ancestris.modules.gedcom.utilities.PropertyTag2Name;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;

/**
 *
 * @author lemovice
 */
public class PropertiesDiffPanel extends javax.swing.JPanel {

    class PropertyListSelectionModel extends DefaultListSelectionModel{
        @Override
        public boolean isSelectedIndex(int index) {
            return false;
        }
    }
    
    class PropertyListRenderer extends JTextArea implements ListCellRenderer {

        public PropertyListRenderer() {
            setOpaque(true);
            setColumns(40);
            setLineWrap(true);
            setWrapStyleWord(true);
        }

        /*
         * This method finds the text corresponding to the selected value and
         * returns the textArea, set up to display the text.
         */
        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            // same height for the both sides
            int leftlinesCount = 1;
            int rightlinesCount = 1;
            if (index < leftPropertyListModel.getSize()) {
                if (leftPropertyListModel.getElementAt(index) != null) {
                    leftlinesCount = leftPropertyListModel.getElementAt(index).toString().length() / getColumns();
                    if (leftPropertyListModel.getElementAt(index).toString().length() % getColumns() > 0) {
                        leftlinesCount += 1;
                    }
                }
            }
            if (index < rightPropertyListModel.getSize()) {
                if (rightPropertyListModel.getElementAt(index) != null) {
                    rightlinesCount = rightPropertyListModel.getElementAt(index).toString().length() / getColumns();
                    if (rightPropertyListModel.getElementAt(index).toString().length() % getColumns() > 0) {
                        rightlinesCount += 1;
                    }
                }
            }
            setRows(Math.max(leftlinesCount, rightlinesCount));

            //Set the text.
            if (value != null) {
                setText(value.toString());
            } else {
                setText("*");
            }

            return this;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getRowHeight() * getRows(), getColumnWidth() * getColumns());
        }
    }
    PropertyListModel leftPropertyListModel = new PropertyListModel();
    PropertyListModel rightPropertyListModel = new PropertyListModel();
    PropertyListRenderer propertyListRenderer = new PropertyListRenderer();
    PropertyListSelectionModel propertyListSelectionModel = new PropertyListSelectionModel();

    /**
     * Creates new form PropertiesDiffPanel
     */
    public PropertiesDiffPanel(Property leftProperty, Property rightProperty) {
        ArrayList<TagPath> PropertyTagPathArray = new ArrayList<TagPath>();
        PropertyTagPathArray.add(leftProperty != null ? leftProperty.getPath() : rightProperty.getPath());

        if (leftProperty != null) {
            for (Property property : leftProperty.getProperties(Property.class)) {
                PropertyTagPathArray.add(property.getPath());
            }
        }

        if (rightProperty != null) {
            for (Property property : rightProperty.getProperties(Property.class)) {
                if (!PropertyTagPathArray.contains(property.getPath())) {
                    PropertyTagPathArray.add(property.getPath());
                }
            }
        }

        for (Iterator<TagPath> it = PropertyTagPathArray.iterator(); it.hasNext();) {
            TagPath tagPath = it.next();
            if (leftProperty != null) {
                leftPropertyListModel.add(PropertyTagPathArray.indexOf(tagPath), leftProperty.getParent().getProperty(tagPath));
            }
            if (rightProperty != null) {
                rightPropertyListModel.add(PropertyTagPathArray.indexOf(tagPath), rightProperty.getParent().getProperty(tagPath));
            }
        }
        initComponents();

        propertyTagLabel.setText(PropertyTag2Name.getTagName(leftProperty != null ? leftProperty.getTag() : rightProperty.getTag()));
        leftPropertyList.setFixedCellWidth(propertyListRenderer.getWidth());
        rightPropertyList.setFixedCellWidth(propertyListRenderer.getWidth());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        propertyTagLabel = new javax.swing.JLabel();
        leftPropertyList = new javax.swing.JList();
        rightPropertyList = new javax.swing.JList();

        setPreferredSize(new java.awt.Dimension(0, 0));

        propertyTagLabel.setText(org.openide.util.NbBundle.getMessage(PropertiesDiffPanel.class, "PropertiesDiffPanel.propertyTagLabel.text")); // NOI18N
        propertyTagLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        leftPropertyList.setModel(leftPropertyListModel);
        leftPropertyList.setCellRenderer(propertyListRenderer);
        leftPropertyList.setSelectionBackground(new java.awt.Color(57, 117, 215));
        leftPropertyList.setSelectionModel(propertyListSelectionModel);

        rightPropertyList.setModel(rightPropertyListModel);
        rightPropertyList.setCellRenderer(propertyListRenderer);
        rightPropertyList.setSelectionBackground(new java.awt.Color(57, 117, 215));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(propertyTagLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                .addComponent(leftPropertyList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rightPropertyList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(propertyTagLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(leftPropertyList, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                    .addComponent(rightPropertyList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList leftPropertyList;
    private javax.swing.JLabel propertyTagLabel;
    private javax.swing.JList rightPropertyList;
    // End of variables declaration//GEN-END:variables

    public List <Property>getSelectedProperties() {
        return (rightPropertyList.getSelectedValuesList());
    }
}
