package ancestris.modules.editors.genealogyeditor.panels;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyChoiceValue;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author dominique
 */
public class ShelfNumberEditorPanel extends javax.swing.JPanel {

    private Property mParentProperty = null;
    private Property mShelfNumberProperty = null;
    private final ChangeListner changeListner = new ChangeListner();

    /**
     * Creates new form ShelfNumberPanel
     */
    public ShelfNumberEditorPanel() {
        initComponents();

        mediaTypeChoiceWidget.setEditable(false);
        mediaTypeChoiceWidget.addChangeListener(changeListner);
        shelfNumberTextField.getDocument().addDocumentListener(changeListner);
        shelfNumberTextField.getDocument().putProperty("name", "shelfNumberTextField");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        shelfNumberLabel = new javax.swing.JLabel();
        shelfNumberTextField = new javax.swing.JTextField();
        mediaTypeLabel = new javax.swing.JLabel();
        mediaTypeChoiceWidget = new genj.util.swing.ChoiceWidget();

        shelfNumberLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(shelfNumberLabel, org.openide.util.NbBundle.getMessage(ShelfNumberEditorPanel.class, "ShelfNumberEditorPanel.shelfNumberLabel.text")); // NOI18N

        mediaTypeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(mediaTypeLabel, org.openide.util.NbBundle.getMessage(ShelfNumberEditorPanel.class, "ShelfNumberEditorPanel.mediaTypeLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mediaTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(shelfNumberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(shelfNumberTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                    .addComponent(mediaTypeChoiceWidget, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shelfNumberLabel)
                    .addComponent(shelfNumberTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mediaTypeLabel)
                    .addComponent(mediaTypeChoiceWidget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private genj.util.swing.ChoiceWidget mediaTypeChoiceWidget;
    private javax.swing.JLabel mediaTypeLabel;
    private javax.swing.JLabel shelfNumberLabel;
    private javax.swing.JTextField shelfNumberTextField;
    // End of variables declaration//GEN-END:variables

    public void set(Property parentProperty, Property shelfNumberProperty) {
        changeListner.mute();
        mParentProperty = parentProperty;
        mShelfNumberProperty = shelfNumberProperty;
        mediaTypeChoiceWidget.setValues(PropertyChoiceValue.getChoices(parentProperty.getGedcom(), "MEDI", true));

        if (mShelfNumberProperty != null) {
            shelfNumberTextField.setText(mShelfNumberProperty.getValue());
            Property mediProperty = mShelfNumberProperty.getProperty("MEDI");
            mediaTypeChoiceWidget.setSelectedItem(mediProperty != null ? mediProperty.getValue() : "");
        } else {
            shelfNumberTextField.setText("");
            mediaTypeChoiceWidget.setSelectedItem("");
        }
        changeListner.unmute();
    }

    public void commit() {
        if (changeListner.hasChange()) {
            if (mShelfNumberProperty != null) {
                mShelfNumberProperty.setValue(shelfNumberTextField.getText());
                Property mediProperty = mShelfNumberProperty.getProperty("MEDI");
                if (mShelfNumberProperty != null) {
                    mediProperty.setValue(mediaTypeChoiceWidget.getText());
                } else {
                    mShelfNumberProperty.addProperty("MEDI", mediaTypeChoiceWidget.getText());
                }
            } else {
                mShelfNumberProperty = mParentProperty.addProperty("CALN", shelfNumberTextField.getText());
                mShelfNumberProperty.addProperty("MEDI", mediaTypeChoiceWidget.getText());
            }
            changeListner.setChange(false);
        }
    }

    public class ChangeListner implements DocumentListener, ChangeListener {

        private boolean mute = false;
        private boolean hasChange = false;

        @Override
        public void insertUpdate(DocumentEvent de) {
            if (!mute) {
                Object propertyName = de.getDocument().getProperty("name");
                if (propertyName != null) {
                    if (propertyName.equals("shelfNumberTextField")) {
                        setChange(true);
                    } else if (propertyName.equals("mediaTypeTextField")) {
                        setChange(true);
                    }
                }
            }
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            if (!mute) {
                Object propertyName = de.getDocument().getProperty("name");
                if (propertyName != null) {
                    if (propertyName.equals("shelfNumberTextField")) {
                        setChange(true);
                    } else if (propertyName.equals("mediaTypeTextField")) {
                        setChange(true);
                    }
                }
            }
        }

        @Override
        public void changedUpdate(DocumentEvent de) {
            if (!mute) {
                Object propertyName = de.getDocument().getProperty("name");
                if (propertyName != null) {
                    if (propertyName.equals("shelfNumberTextField")) {
                        setChange(true);
                    } else if (propertyName.equals("mediaTypeTextField")) {
                        setChange(true);
                    }
                }
            }
        }

        public void mute() {
            mute = true;
        }

        public void unmute() {
            mute = false;
        }

        /**
         * @return the hasChange
         */
        public boolean hasChange() {
            return hasChange;
        }

        /**
         * @param hasChange the hasChange to set
         */
        public void setChange(boolean hasChange) {
            this.hasChange = hasChange;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            setChange(true);
        }
    }
}
