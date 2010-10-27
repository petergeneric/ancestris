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
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class AddressStructureBeanPanel extends javax.swing.JPanel implements PropertyChangeListener, DocumentListener {

    private String[] countries = new String[247];
    //
    private int index = 0;
    private String title = "";
    private Property parentProperty;
    public boolean isModified;
    private boolean isSetURmanager = false;
    private EditorStdTopComponent editor;

    /** Creates new form AddressStructureBeanPanel */
    public AddressStructureBeanPanel() {
        initCountries();
        initComponents();
        address_line1.setVisible(false);
        address_line2.setVisible(false);
    }

    public void init(int index) {
        this.index = index;
        this.title = ((javax.swing.JTabbedPane) getParent()).getTitleAt(index);
        addressStructure.addPropertyChangeListener(this);
        // change listeners
        address_line.getDocument().addDocumentListener(this);
        //address_line1.getDocument().addDocumentListener(this);
        //address_line2.getDocument().addDocumentListener(this);
        address_city.getDocument().addDocumentListener(this);
        address_state.getDocument().addDocumentListener(this);
        address_postal_code.getDocument().addDocumentListener(this);
        ((JTextComponent) address_country.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        phone_number1.getDocument().addDocumentListener(this);
        phone_number2.getDocument().addDocumentListener(this);
        phone_number3.getDocument().addDocumentListener(this);
        email.getDocument().addDocumentListener(this);
        web_site.getDocument().addDocumentListener(this);
        // reset modified flag
        setModified(false);
    }

    public void setProperties(Property parentProperty) {
        this.parentProperty = parentProperty;
        addressStructure.setAddr((PropertyMultilineValue) (parentProperty.getProperty(AddressStructureBean.PROP_ADDR)));
        if (addressStructure.getAddr() != null) {
            //addressStructure.setAddr1((PropertySimpleValue) (addressStructure.getAddr().getProperty(AddressStructureBean.PROP_ADDR1)));
            //addressStructure.setAddr2((PropertySimpleValue) (addressStructure.getAddr().getProperty(AddressStructureBean.PROP_ADDR2)));
            addressStructure.setCity((PropertyChoiceValue) (addressStructure.getAddr().getProperty(AddressStructureBean.PROP_CITY)));
            addressStructure.setStae((PropertyChoiceValue) (addressStructure.getAddr().getProperty(AddressStructureBean.PROP_STAE)));
            addressStructure.setPost((PropertyChoiceValue) (addressStructure.getAddr().getProperty(AddressStructureBean.PROP_POST)));
            addressStructure.setCtry((PropertyChoiceValue) (addressStructure.getAddr().getProperty(AddressStructureBean.PROP_CTRY)));
        }
        addressStructure.setPhon((Property[]) (parentProperty.getProperties(AddressStructureBean.PROP_PHON)));
        addressStructure.setEmail((PropertySimpleValue) (parentProperty.getProperty(AddressStructureBean.PROP_EMAIL)));
        addressStructure.setWww((PropertySimpleValue) (parentProperty.getProperty(AddressStructureBean.PROP_WWW)));
        setModified(false);
    }

    public void displayProperties() {
        if (!editor.isBusy()) {
            updateField(address_line, addressStructure.getAddr());
            //updateField(address_line1, addressStructure.getAddr1());
            //updateField(address_line2, addressStructure.getAddr2());
            updateField(address_city, addressStructure.getCity());
            updateField(address_state, addressStructure.getStae());
            updateField(address_postal_code, addressStructure.getPost());
            updateField(((JTextComponent) address_country.getEditor().getEditorComponent()), addressStructure.getCtry());
            updateField(addressStructure.getPhon());
            updateField(email, addressStructure.getEmail());
            email.setCaretPosition(0);
            updateField(web_site, addressStructure.getWww());
            web_site.setCaretPosition(0);
        }
    }

    public void saveProperties() {
        save(parentProperty, addressStructure.getAddr(), AddressStructureBean.PROP_ADDR, address_line.getText());
        if (addressStructure.getAddr() != null) {
            //save(addressStructure.getAddr(), addressStructure.getAddr1(), AddressStructureBean.PROP_ADDR1, lineFromAddress(address_line.getText(), 1));
            //address_line1.setText(addressStructure.getAddr1().getDisplayValue());
            //save(addressStructure.getAddr(), addressStructure.getAddr2(), AddressStructureBean.PROP_ADDR2, lineFromAddress(address_line.getText(), 2));
            //address_line2.setText(addressStructure.getAddr2().getDisplayValue());
            save(addressStructure.getAddr(), addressStructure.getCity(), AddressStructureBean.PROP_CITY, address_city.getText());
            save(addressStructure.getAddr(), addressStructure.getStae(), AddressStructureBean.PROP_STAE, address_state.getText());
            save(addressStructure.getAddr(), addressStructure.getPost(), AddressStructureBean.PROP_POST, address_postal_code.getText());
            save(addressStructure.getAddr(), addressStructure.getCtry(), AddressStructureBean.PROP_CTRY, ((JTextComponent) address_country.getEditor().getEditorComponent()).getText());
        }
        save(parentProperty, addressStructure.getPhon());
        save(parentProperty, addressStructure.getEmail(), AddressStructureBean.PROP_EMAIL, email.getText());
        save(parentProperty, addressStructure.getWww(), AddressStructureBean.PROP_WWW, web_site.getText());
        setModified(false);
    }

    private String lineFromAddress(String text, int i) {
        String[] lines = text.split("\\n", 3);
        return lines[i-1];
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
        phone_number1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        phone_number2 = new javax.swing.JTextField();
        phone_number3 = new javax.swing.JTextField();
        address_country = new AutoCompleteCombo(countries);
        jLabel7 = new javax.swing.JLabel();
        email = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        web_site = new javax.swing.JTextField();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel1.text")); // NOI18N

        address_line.setColumns(20);
        address_line.setRows(5);
        jScrollPane1.setViewportView(address_line);

        address_line1.setEditable(false);
        address_line1.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.address_line1.text")); // NOI18N

        address_line2.setEditable(false);
        address_line2.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.address_line2.text")); // NOI18N

        address_city.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.address_city.text")); // NOI18N

        address_postal_code.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.address_postal_code.text")); // NOI18N

        address_state.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.address_state.text")); // NOI18N

        phone_number1.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.phone_number1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel3.text")); // NOI18N

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel4.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel5.text")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel6.text")); // NOI18N

        phone_number2.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.phone_number2.text")); // NOI18N

        phone_number3.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.phone_number3.text")); // NOI18N

        address_country.setEditable(true);

        jLabel7.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel7.text")); // NOI18N

        email.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.email.text")); // NOI18N
        email.setToolTipText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "TTT_subm_addr_email")); // NOI18N

        jLabel8.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.jLabel8.text")); // NOI18N

        web_site.setText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "AddressStructureBeanPanel.web_site.text")); // NOI18N
        web_site.setToolTipText(org.openide.util.NbBundle.getMessage(AddressStructureBeanPanel.class, "TTT_subm_addr_www")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(address_line1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                                        .addComponent(address_line2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(29, 29, 29))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(10, 10, 10)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(web_site, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                            .addComponent(phone_number1, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(address_postal_code, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING))
                                    .addComponent(jLabel6))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(address_city, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                                    .addComponent(address_state, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                                    .addComponent(address_country, javax.swing.GroupLayout.Alignment.TRAILING, 0, 309, Short.MAX_VALUE)))
                            .addComponent(phone_number3, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                            .addComponent(phone_number2, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                            .addComponent(email, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
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
                            .addComponent(jLabel6)
                            .addComponent(address_country, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(address_line1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(address_line2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(phone_number1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(phone_number2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(phone_number3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(web_site, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private genjfr.app.editorstd.beans.AddressStructureBean addressStructure;
    private javax.swing.JTextField address_city;
    private javax.swing.JComboBox address_country;
    private javax.swing.JTextArea address_line;
    private javax.swing.JTextField address_line1;
    private javax.swing.JTextField address_line2;
    private javax.swing.JTextField address_postal_code;
    private javax.swing.JTextField address_state;
    private javax.swing.JTextField email;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField phone_number1;
    private javax.swing.JTextField phone_number2;
    private javax.swing.JTextField phone_number3;
    private javax.swing.JTextField web_site;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!editor.isBusy()) {
            if (evt.getPropertyName().equals(AddressStructureBean.PROP_ADDR)) {
                updateField(address_line, addressStructure.getAddr());
            }
//            if (evt.getPropertyName().equals(AddressStructureBean.PROP_ADDR1)) {
//                updateField(address_line1, addressStructure.getAddr1());
//            }
//            if (evt.getPropertyName().equals(AddressStructureBean.PROP_ADDR2)) {
//                updateField(address_line2, addressStructure.getAddr2());
//            }
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
                updateField(((JTextComponent) address_country.getEditor().getEditorComponent()), addressStructure.getCtry());
            }
            if (evt.getPropertyName().equals(AddressStructureBean.PROP_PHON)) {
                updateField(addressStructure.getPhon());
            }
            if (evt.getPropertyName().equals(AddressStructureBean.PROP_EMAIL)) {
                updateField(email, addressStructure.getEmail());
            }
            if (evt.getPropertyName().equals(AddressStructureBean.PROP_WWW)) {
                updateField(web_site, addressStructure.getWww());
            }
        }
    }

    private void updateField(JTextComponent text, Property prop) {
        if (prop != null) {
            String oldText = text.getText();
            String newText = prop.getDisplayValue();
            text.setText(newText);
            if (!editor.isBusy() && !oldText.equals(newText)) {
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

    private void save(Property parentProperty, Property[] phon) {
        if (parentProperty == null) {
            return;
        }
        // phon is initialised new Property[phonSize] so should be non null and of correct size
        save(parentProperty, phon[0], AddressStructureBean.PROP_PHON, phone_number1.getText());
        save(parentProperty, phon[1], AddressStructureBean.PROP_PHON, phone_number2.getText());
        save(parentProperty, phon[2], AddressStructureBean.PROP_PHON, phone_number3.getText());
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (!editor.isBusy()) {
            setModified(true);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (!editor.isBusy()) {
            setModified(true);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if (!editor.isBusy()) {
            setModified(true);
        }
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
            //address_line1.getDocument().addUndoableEditListener(URmanager);
            //address_line2.getDocument().addUndoableEditListener(URmanager);
            address_city.getDocument().addUndoableEditListener(URmanager);
            address_state.getDocument().addUndoableEditListener(URmanager);
            address_postal_code.getDocument().addUndoableEditListener(URmanager);
            ((JTextComponent) address_country.getEditor().getEditorComponent()).getDocument().addUndoableEditListener(URmanager);
            phone_number1.getDocument().addUndoableEditListener(URmanager);
            email.getDocument().addUndoableEditListener(URmanager);
            web_site.getDocument().addUndoableEditListener(URmanager);
        }
        this.editor = editor;
    }

    private void initCountries() {
        countries[0] = "";
        countries[1] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_FR");
        countries[2] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AF");
        countries[3] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_ZA");
        countries[4] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AX");
        countries[5] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AL");
        countries[6] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_DZ");
        countries[7] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_DE");
        countries[8] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AD");
        countries[9] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AO");
        countries[10] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AI");
        countries[11] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AQ");
        countries[12] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AG");
        countries[13] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AN");
        countries[14] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SA");
        countries[15] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AR");
        countries[16] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AM");
        countries[17] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AW");
        countries[18] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AU");
        countries[19] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AT");
        countries[20] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AZ");
        countries[21] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BS");
        countries[22] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BH");
        countries[23] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BD");
        countries[24] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BB");
        countries[25] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BY");
        countries[26] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BE");
        countries[27] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BZ");
        countries[28] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BJ");
        countries[29] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BM");
        countries[30] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BT");
        countries[31] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BO");
        countries[32] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BA");
        countries[33] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BW");
        countries[34] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BV");
        countries[35] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BR");
        countries[36] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BN");
        countries[37] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BG");
        countries[38] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BF");
        countries[39] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_BI");
        countries[40] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_KY");
        countries[41] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_KH");
        countries[42] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CM");
        countries[43] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CA");
        countries[44] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CV");
        countries[45] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CF");
        countries[46] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CL");
        countries[47] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CN");
        countries[48] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CX");
        countries[49] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CY");
        countries[50] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CC");
        countries[51] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CO");
        countries[52] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_KM");
        countries[53] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CG");
        countries[54] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CD");
        countries[55] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CK");
        countries[56] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_KR");
        countries[57] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_KP");
        countries[58] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CR");
        countries[59] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CI");
        countries[60] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_HR");
        countries[61] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CU");
        countries[62] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_DK");
        countries[63] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_DJ");
        countries[64] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_DO");
        countries[65] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_DM");
        countries[66] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_EG");
        countries[67] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SV");
        countries[68] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AE");
        countries[69] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_EC");
        countries[70] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_ER");
        countries[71] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_ES");
        countries[72] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_EE");
        countries[73] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_US");
        countries[74] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_ET");
        countries[75] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_FK");
        countries[76] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_FO");
        countries[77] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_FJ");
        countries[78] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_FI");
        countries[79] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GA");
        countries[80] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GM");
        countries[81] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GE");
        countries[82] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GS");
        countries[83] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GH");
        countries[84] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GI");
        countries[85] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GR");
        countries[86] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GD");
        countries[87] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GL");
        countries[88] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GP");
        countries[89] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GU");
        countries[90] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GT");
        countries[91] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GG");
        countries[92] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GN");
        countries[93] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GQ");
        countries[94] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GW");
        countries[95] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GY");
        countries[96] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GF");
        countries[97] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_HT");
        countries[98] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_HM");
        countries[99] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_HN");
        countries[100] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_HK");
        countries[101] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_HU");
        countries[102] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_IM");
        countries[103] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_UM");
        countries[104] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_VG");
        countries[105] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_VI");
        countries[106] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_IN");
        countries[107] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_ID");
        countries[108] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_IR");
        countries[109] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_IQ");
        countries[110] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_IE");
        countries[111] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_IS");
        countries[112] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_IL");
        countries[113] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_IT");
        countries[114] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_JM");
        countries[115] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_JP");
        countries[116] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_JE");
        countries[117] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_JO");
        countries[118] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_KZ");
        countries[119] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_KE");
        countries[120] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_KG");
        countries[121] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_KI");
        countries[122] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_KW");
        countries[123] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_LA");
        countries[124] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_LS");
        countries[125] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_LV");
        countries[126] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_LB");
        countries[127] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_LR");
        countries[128] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_LY");
        countries[129] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_LI");
        countries[130] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_LT");
        countries[131] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_LU");
        countries[132] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_LU");
        countries[133] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MO");
        countries[134] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MK");
        countries[135] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MG");
        countries[136] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MY");
        countries[137] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MW");
        countries[138] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MV");
        countries[139] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_ML");
        countries[140] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MT");
        countries[141] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MP");
        countries[142] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MA");
        countries[143] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MH");
        countries[144] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MQ");
        countries[145] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MU");
        countries[146] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MR");
        countries[147] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_YT");
        countries[148] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MX");
        countries[149] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_FM");
        countries[150] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MD");
        countries[151] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MC");
        countries[152] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MN");
        countries[153] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_ME");
        countries[154] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MS");
        countries[155] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MZ");
        countries[156] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MM");
        countries[157] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_NA");
        countries[158] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_NR");
        countries[159] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_NP");
        countries[160] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_NI");
        countries[161] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_NE");
        countries[162] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_NG");
        countries[163] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_NU");
        countries[164] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_NF");
        countries[165] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_NO");
        countries[166] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_NC");
        countries[167] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_NZ");
        countries[168] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_IO");
        countries[169] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_OM");
        countries[170] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_UG");
        countries[171] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_UZ");
        countries[172] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PK");
        countries[173] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PW");
        countries[174] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PS");
        countries[175] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PA");
        countries[176] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PG");
        countries[177] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PY");
        countries[178] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_NL");
        countries[179] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PE");
        countries[180] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PH");
        countries[181] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PN");
        countries[182] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PL");
        countries[183] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PF");
        countries[184] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PR");
        countries[185] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PT");
        countries[186] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_QA");
        countries[187] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_RE");
        countries[188] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_RO");
        countries[189] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_GB");
        countries[190] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_RU");
        countries[191] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_RW");
        countries[192] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_EH");
        countries[193] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_KN");
        countries[194] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SM");
        countries[195] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_MF");
        countries[196] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_PM");
        countries[197] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_VA");
        countries[198] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_VC");
        countries[199] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SH");
        countries[200] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_LC");
        countries[201] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SB");
        countries[202] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_WS");
        countries[203] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_AS");
        countries[204] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_ST");
        countries[205] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SN");
        countries[206] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_RS");
        countries[207] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SC");
        countries[208] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SL");
        countries[209] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SG");
        countries[210] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SK");
        countries[211] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SI");
        countries[212] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SO");
        countries[213] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SD");
        countries[214] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_LK");
        countries[215] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SE");
        countries[216] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CH");
        countries[217] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SR");
        countries[218] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SJ");
        countries[219] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SZ");
        countries[220] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_SY");
        countries[221] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TJ");
        countries[222] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TW");
        countries[223] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TZ");
        countries[224] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TD");
        countries[225] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_CZ");
        countries[226] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TF");
        countries[227] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TH");
        countries[228] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TL");
        countries[229] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TG");
        countries[230] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TK");
        countries[231] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TO");
        countries[232] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TT");
        countries[233] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TN");
        countries[234] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TM");
        countries[235] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TC");
        countries[236] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TR");
        countries[237] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_TV");
        countries[238] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_UA");
        countries[239] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_UY");
        countries[240] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_VU");
        countries[241] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_VE");
        countries[242] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_VN");
        countries[243] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_WF");
        countries[244] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_YE");
        countries[245] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_ZM");
        countries[246] = NbBundle.getMessage(AddressStructureBeanPanel.class, "Countrycode_ZW");

    }

}
