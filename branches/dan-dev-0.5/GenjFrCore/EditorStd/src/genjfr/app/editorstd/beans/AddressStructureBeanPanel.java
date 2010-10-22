/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AddressStructureBeanPanel.java
 *
 * Created on 10 oct. 2010, 13:44:25
 */
package genjfr.app.editorstd.beans;

import genj.gedcom.Property;
import genj.gedcom.PropertyChoiceValue;
import genj.gedcom.PropertyMultilineValue;
import genj.gedcom.PropertySimpleValue;
import genjfr.app.editorstd.EditorStdTopComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.openide.awt.UndoRedo.Manager;

/**
 *
 * @author frederic
 */
public class AddressStructureBeanPanel extends javax.swing.JPanel implements PropertyChangeListener, DocumentListener {

    private int index = 0;
    private String title = "";
    private Property parentProperty;
    public boolean isModified;
    private boolean isSetURmanager = false;
    private EditorStdTopComponent editor;
    private boolean busy = false;

    /** Creates new form AddressStructureBeanPanel */
    public AddressStructureBeanPanel() {
        initComponents();
    }

    public void init(int index) {
        this.index = index;
        this.title = ((javax.swing.JTabbedPane) getParent()).getTitleAt(index);
        addressStructure.addPropertyChangeListener(this);
        // change listeners
        address_line.getDocument().addDocumentListener(this);
        address_line1.getDocument().addDocumentListener(this);
        address_line2.getDocument().addDocumentListener(this);
        address_city.getDocument().addDocumentListener(this);
        address_state.getDocument().addDocumentListener(this);
        address_postal_code.getDocument().addDocumentListener(this);
        address_country.getDocument().addDocumentListener(this);
        phone_number1.getDocument().addDocumentListener(this);
        phone_number2.getDocument().addDocumentListener(this);
        phone_number3.getDocument().addDocumentListener(this);
        // reset modified flag
        setModified(false);
    }

    public void setProperties(Property parentProperty) {
        this.parentProperty = parentProperty;
        addressStructure.setAddr((PropertyMultilineValue) (parentProperty.getProperty(AddressStructureBean.PROP_ADDR)));
        if (addressStructure.getAddr() != null) {
            addressStructure.setAddr1((PropertySimpleValue) (addressStructure.getAddr().getProperty(AddressStructureBean.PROP_ADDR1)));
            addressStructure.setAddr2((PropertySimpleValue) (addressStructure.getAddr().getProperty(AddressStructureBean.PROP_ADDR2)));
            addressStructure.setCity((PropertyChoiceValue) (addressStructure.getAddr().getProperty(AddressStructureBean.PROP_CITY)));
            addressStructure.setStae((PropertyChoiceValue) (addressStructure.getAddr().getProperty(AddressStructureBean.PROP_STAE)));
            addressStructure.setPost((PropertyChoiceValue) (addressStructure.getAddr().getProperty(AddressStructureBean.PROP_POST)));
            addressStructure.setCtry((PropertyChoiceValue) (addressStructure.getAddr().getProperty(AddressStructureBean.PROP_CTRY)));
        }
        addressStructure.setPhon((Property[]) (parentProperty.getProperties(AddressStructureBean.PROP_PHON)));
        setModified(false);
    }

    public void displayProperties() {
        if (!busy) {
            updateField(address_line, addressStructure.getAddr());
            updateField(address_line1, addressStructure.getAddr1());
            updateField(address_line2, addressStructure.getAddr2());
            updateField(address_city, addressStructure.getCity());
            updateField(address_state, addressStructure.getStae());
            updateField(address_postal_code, addressStructure.getPost());
            updateField(address_country, addressStructure.getCtry());
            updateField(addressStructure.getPhon());
        }
    }

    public void saveProperties() {
        busy = true;
        save(parentProperty, addressStructure.getAddr(), AddressStructureBean.PROP_ADDR, address_line.getText());
        if (addressStructure.getAddr() != null) {
            save(addressStructure.getAddr(), addressStructure.getAddr1(), AddressStructureBean.PROP_ADDR1, address_line1.getText());
            save(addressStructure.getAddr(), addressStructure.getAddr2(), AddressStructureBean.PROP_ADDR2, address_line2.getText());
            save(addressStructure.getAddr(), addressStructure.getCity(), AddressStructureBean.PROP_CITY, address_city.getText());
            save(addressStructure.getAddr(), addressStructure.getStae(), AddressStructureBean.PROP_STAE, address_state.getText());
            save(addressStructure.getAddr(), addressStructure.getPost(), AddressStructureBean.PROP_POST, address_postal_code.getText());
            save(addressStructure.getAddr(), addressStructure.getCtry(), AddressStructureBean.PROP_CTRY, address_country.getText());
        }
        save(parentProperty, addressStructure.getPhon(), AddressStructureBean.PROP_PHON);
        busy = false;
        setModified(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addressStructure = new genjfr.app.editorstd.beans.AddressStructureBean();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        address_line = new javax.swing.JTextArea();
        address_line1 = new javax.swing.JTextField();
        address_line2 = new javax.swing.JTextField();
        address_city = new javax.swing.JTextField();
        address_postal_code = new javax.swing.JTextField();
        address_state = new javax.swing.JTextField();
        address_country = new javax.swing.JTextField();
        phone_number1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        phone_number2 = new javax.swing.JTextField();
        phone_number3 = new javax.swing.JTextField();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel1.text")); // NOI18N

        address_line.setColumns(20);
        address_line.setRows(5);
        jScrollPane1.setViewportView(address_line);

        address_line1.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.address_line1.text")); // NOI18N

        address_line2.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.address_line2.text")); // NOI18N

        address_city.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.address_city.text")); // NOI18N

        address_postal_code.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.address_postal_code.text")); // NOI18N

        address_state.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.address_state.text")); // NOI18N

        address_country.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.address_country.text")); // NOI18N

        phone_number1.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.phone_number1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel3.text")); // NOI18N

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel4.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel5.text")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel6.text")); // NOI18N

        phone_number2.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.phone_number2.text")); // NOI18N

        phone_number3.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.phone_number3.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)
                        .addGap(42, 42, 42)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(phone_number1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(phone_number2, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(phone_number3, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
                            .addComponent(address_line1, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(address_line2, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(address_postal_code, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING))
                                    .addComponent(jLabel6))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(address_country, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                                    .addComponent(address_city, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                                    .addComponent(address_state, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(address_line1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(address_line2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(address_postal_code, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(address_city, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(address_state, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(address_country, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(phone_number1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(phone_number2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(phone_number3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private genjfr.app.editorstd.beans.AddressStructureBean addressStructure;
    private javax.swing.JTextField address_city;
    private javax.swing.JTextField address_country;
    private javax.swing.JTextArea address_line;
    private javax.swing.JTextField address_line1;
    private javax.swing.JTextField address_line2;
    private javax.swing.JTextField address_postal_code;
    private javax.swing.JTextField address_state;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField phone_number1;
    private javax.swing.JTextField phone_number2;
    private javax.swing.JTextField phone_number3;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!busy) {
            if (evt.getPropertyName().equals(AddressStructureBean.PROP_ADDR)) {
                updateField(address_line, addressStructure.getAddr());
            }
            if (evt.getPropertyName().equals(AddressStructureBean.PROP_ADDR1)) {
                updateField(address_line1, addressStructure.getAddr1());
            }
            if (evt.getPropertyName().equals(AddressStructureBean.PROP_ADDR2)) {
                updateField(address_line2, addressStructure.getAddr2());
            }
            if (evt.getPropertyName().equals(AddressStructureBean.PROP_CITY)) {
                updateField(address_city, addressStructure.getCity());
            }
            if (evt.getPropertyName().equals(AddressStructureBean.PROP_STAE)) {
                updateField(address_state, addressStructure.getStae());
            }
            if (evt.getPropertyName().equals(AddressStructureBean.PROP_POST)) {
                updateField(address_postal_code, addressStructure.getPost());
            }
            if (evt.getPropertyName().equals(AddressStructureBean.PROP_CTRY)) {
                updateField(address_country, addressStructure.getCtry());
            }
            if (evt.getPropertyName().equals(AddressStructureBean.PROP_PHON)) {
                updateField(addressStructure.getPhon());
            }
        }
    }

    private void updateField(JTextComponent text, Property prop) {
        if (prop != null) {
            String oldText = text.getText();
            String newText = prop.getDisplayValue();
            text.setText(newText);
            if (!oldText.equals(newText)) {
                setModified(true);
            }
        } else {
            text.setText("");
        }
    }

    private void updateField(Property[] phon) {
        if (phon == null) {
            return;
        }
        if (phon.length > 2) {
            updateField(phone_number3, phon[2]);
        } else {
            updateField(phone_number3, null);
        }
        if (phon.length > 1) {
            updateField(phone_number2, phon[1]);
        } else {
            updateField(phone_number2, null);
        }
        if (phon.length > 0) {
            updateField(phone_number1, phon[0]);
        } else {
            updateField(phone_number1, null);
        }
    }

    private void save(Property parentProperty, Property propToSave, String PROP_TAG, String value) {
        if (parentProperty == null || value == null) {
            return;
        }
        if (propToSave != null) {
            propToSave.setValue(value);
            return;
        }
        if (propToSave == null && !value.isEmpty()) {
            parentProperty.addProperty(PROP_TAG, value);
            return;
        }
    }

    private void save(Property parentProperty, Property[] phon, String PROP_PHON) {
        if (phon == null) {
            return;
        }
        if (phon.length > 0) {
            save(parentProperty, phon[0], AddressStructureBean.PROP_PHON, phone_number1.getText());
        }
        if (phon.length > 1) {
            save(parentProperty, phon[1], AddressStructureBean.PROP_PHON, phone_number2.getText());
        }
        if (phon.length > 2) {
            save(parentProperty, phon[2], AddressStructureBean.PROP_PHON, phone_number3.getText());
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        setModified(true);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        setModified(true);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        setModified(true);
    }

    private void setModified(boolean modified) {
        isModified = modified;
        ((javax.swing.JTabbedPane) getParent()).setTitleAt(index, modified ? title + "*" : title);
        if (editor != null) {
            editor.setModified(modified);
        }
    }

    public void setManagers(Manager URmanager, EditorStdTopComponent editor) {
        if (!isSetURmanager) {
            isSetURmanager = true;
            // change listeners
            address_line.getDocument().addUndoableEditListener(URmanager);
            address_line1.getDocument().addUndoableEditListener(URmanager);
            address_line2.getDocument().addUndoableEditListener(URmanager);
            address_city.getDocument().addUndoableEditListener(URmanager);
            address_state.getDocument().addUndoableEditListener(URmanager);
            address_postal_code.getDocument().addUndoableEditListener(URmanager);
            address_country.getDocument().addUndoableEditListener(URmanager);
            phone_number1.getDocument().addUndoableEditListener(URmanager);
        }
        this.editor = editor;
    }
}
