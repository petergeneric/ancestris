/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SubmitterBeanPanel.java
 *
 * Created on 23 oct. 2010, 11:13:00
 */
package genjfr.app.editorstd.beans;

import genj.gedcom.Property;
import genj.gedcom.PropertySimpleValue;
import genjfr.app.editorstd.EditorStdTopComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.openide.awt.UndoRedo.Manager;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class SubmitterBeanPanel extends javax.swing.JPanel implements PropertyChangeListener, DocumentListener {

    final private int nbLanguages = 87;
    private Map<String, String> langMap = new TreeMap<String, String>();
    private String[] languages = new String[nbLanguages];  // will be displayed
    //
    private int index = 0;
    private String title = "";
    private Property parentProperty;
    public boolean isModified;
    private boolean isSetURmanager = false;
    private EditorStdTopComponent editor;

    /** Creates new form SubmitterBeanPanel */
    public SubmitterBeanPanel() {
        initLanguages();
        initComponents();
    }

    public void init(int index) {
        this.index = index;
        this.title = ((javax.swing.JTabbedPane) getParent().getParent()).getTitleAt(index);
        submitter.addPropertyChangeListener(this);
        // change listeners
        submitter_name.getDocument().addDocumentListener(this);
        ((JTextComponent) lang1.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        ((JTextComponent) lang2.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        ((JTextComponent) lang3.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        // reset modified flag
        setModified(false);
    }

    public void setProperties(Property parentProperty) {
        this.parentProperty = parentProperty;
        submitter.setName((PropertySimpleValue) (parentProperty.getProperty(SubmitterBean.PROP_NAME)));
        submitter.setLang((Property[]) (parentProperty.getProperties(SubmitterBean.PROP_LANG)));
        setModified(false);
    }

    public void displayProperties() {
        if (!editor.isBusy()) {
            updateField(submitter_name, submitter.getName());
            updateField(submitter.getLang());
        }
    }

    public void saveProperties() {
        save(parentProperty, submitter.getName(), SubmitterBean.PROP_NAME, submitter_name.getText());
        save(parentProperty, submitter.getLang());
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

        submitter = new genjfr.app.editorstd.beans.SubmitterBean();
        jLabel1 = new javax.swing.JLabel();
        submitter_name = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        lang1 = new AutoCompleteCombo(languages);
        lang2 = new AutoCompleteCombo(languages);
        lang3 = new AutoCompleteCombo(languages);
        jLabel3 = new javax.swing.JLabel();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(SubmitterBeanPanel.class, "SubmitterBeanPanel.jLabel1.text")); // NOI18N

        submitter_name.setText(org.openide.util.NbBundle.getMessage(SubmitterBeanPanel.class, "SubmitterBeanPanel.submitter_name.text")); // NOI18N
        submitter_name.setToolTipText(org.openide.util.NbBundle.getMessage(SubmitterBeanPanel.class, "TTT_subm_id_name")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(SubmitterBeanPanel.class, "SubmitterBeanPanel.jLabel2.text")); // NOI18N

        lang1.setEditable(true);
        lang1.setToolTipText(org.openide.util.NbBundle.getMessage(SubmitterBeanPanel.class, "TTT_subm_id_lang1")); // NOI18N

        lang2.setEditable(true);
        lang2.setToolTipText(org.openide.util.NbBundle.getMessage(SubmitterBeanPanel.class, "TTT_subm_id_lang2")); // NOI18N
        lang2.setMinimumSize(new java.awt.Dimension(100, 27));
        lang2.setPreferredSize(new java.awt.Dimension(100, 27));

        lang3.setEditable(true);
        lang3.setToolTipText(org.openide.util.NbBundle.getMessage(SubmitterBeanPanel.class, "TTT_subm_id_lang3")); // NOI18N
        lang3.setMinimumSize(new java.awt.Dimension(100, 27));
        lang3.setPreferredSize(new java.awt.Dimension(100, 27));

        jLabel3.setText(org.openide.util.NbBundle.getMessage(SubmitterBeanPanel.class, "SubmitterBeanPanel.jLabel3.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(33, 33, 33)
                        .addComponent(submitter_name, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(lang1, 0, 269, Short.MAX_VALUE))
                    .addComponent(jLabel3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lang2, 0, 190, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(lang3, 0, 196, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(submitter_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lang1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lang2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lang3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JComboBox lang1;
    private javax.swing.JComboBox lang2;
    private javax.swing.JComboBox lang3;
    private genjfr.app.editorstd.beans.SubmitterBean submitter;
    private javax.swing.JTextField submitter_name;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!editor.isBusy()) {
            if (evt.getPropertyName().equals(SubmitterBean.PROP_NAME)) {
                updateField(submitter_name, submitter.getName());
            }
            if (evt.getPropertyName().equals(SubmitterBean.PROP_LANG)) {
                updateField(submitter.getLang());
            }
        }
    }

    /**
     * Changes the display text on the panel using the content of the property
     * @param text
     * @param prop
     */
    private void updateField(JTextComponent text, Property prop) {
        if (prop != null) {
            updateField(text, prop.getDisplayValue());
        } else {
            text.setText("");
        }
    }

    private void updateField(JTextComponent text, String newText) {
        String oldText = text.getText();
        text.setText(newText);
        if (!editor.isBusy() && !oldText.equals(newText)) {
            setModified(true);
        }
    }

    private void updateField(Property[] lang) {
        if (lang == null) {
            return;
        }
        if (lang.length > 2) {
            updateField(((JTextComponent) lang3.getEditor().getEditorComponent()), translateG2U(lang[2]));
        } else {
            updateField(((JTextComponent) lang3.getEditor().getEditorComponent()), "");
        }
        if (lang.length > 1) {
            updateField(((JTextComponent) lang2.getEditor().getEditorComponent()), translateG2U(lang[1]));
        } else {
            updateField(((JTextComponent) lang2.getEditor().getEditorComponent()), "");
        }
        if (lang.length > 0) {
            updateField(((JTextComponent) lang1.getEditor().getEditorComponent()), translateG2U(lang[0]));
        } else {
            updateField(((JTextComponent) lang1.getEditor().getEditorComponent()), "");
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

    private void save(Property parentProperty, Property[] lang) {
        if (parentProperty == null) {
            return;
        }
        // lang is initialised new Property[langSize] so should be non null and of correct size
        save(parentProperty, lang[0], SubmitterBean.PROP_LANG, translateU2G(((JTextComponent) lang1.getEditor().getEditorComponent()).getText()));
        save(parentProperty, lang[1], SubmitterBean.PROP_LANG, translateU2G(((JTextComponent) lang2.getEditor().getEditorComponent()).getText()));
        save(parentProperty, lang[2], SubmitterBean.PROP_LANG, translateU2G(((JTextComponent) lang3.getEditor().getEditorComponent()).getText()));
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
        ((javax.swing.JTabbedPane) getParent().getParent()).setTitleAt(index, modified ? title + "*" : title);
        if (editor != null) {
            editor.setModified(modified);
        }
    }

    public void setManagers(Manager URmanager, EditorStdTopComponent editor) {
        if (!isSetURmanager) {
            isSetURmanager = true;
            // change listeners
            submitter_name.getDocument().addUndoableEditListener(URmanager);
            ((JTextComponent) lang1.getEditor().getEditorComponent()).getDocument().addUndoableEditListener(URmanager);
            ((JTextComponent) lang2.getEditor().getEditorComponent()).getDocument().addUndoableEditListener(URmanager);
            ((JTextComponent) lang3.getEditor().getEditorComponent()).getDocument().addUndoableEditListener(URmanager);
        }
        this.editor = editor;
    }

    private String translateG2U(Property prop) {
        if (prop == null) {
            return "";
        }
        return langMap.get(prop.getDisplayValue());
    }

    private String translateU2G(String text) {
        for (Iterator<String> it = langMap.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            String Ulang = langMap.get(key);
            if (Ulang.equals(text)) {
                return key;
            }
        }
        return "";
    }

    private void initLanguages() {
        langMap.put("", "");
        langMap.put("French", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_French"));
        langMap.put("Afrikaans", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Afrikaans"));
        langMap.put("Albanian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Albanian"));
        langMap.put("Amharic", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Amharic"));
        langMap.put("Anglo-Saxon", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Anglo-Saxon"));
        langMap.put("Arabic", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Arabic"));
        langMap.put("Armenian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Armenian"));
        langMap.put("Assamese", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Assamese"));
        langMap.put("Belorusian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Belorusian"));
        langMap.put("Bengali", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Bengali"));
        langMap.put("Braj", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Braj"));
        langMap.put("Bulgarian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Bulgarian"));
        langMap.put("Burmese", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Burmese"));
        langMap.put("Cantonese", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Cantonese"));
        langMap.put("Catalan", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Catalan"));
        langMap.put("Catalan_Spn", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Catalan_Spn"));
        langMap.put("Church-Slavic", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Church-Slavic"));
        langMap.put("Czech", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Czech"));
        langMap.put("Danish", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Danish"));
        langMap.put("Dogri", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Dogri"));
        langMap.put("Dutch", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Dutch"));
        langMap.put("English", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_English"));
        langMap.put("Esperanto", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Esperanto"));
        langMap.put("Estonian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Estonian"));
        langMap.put("Faroese", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Faroese"));
        langMap.put("Finnish", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Finnish"));
        langMap.put("Georgian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Georgian"));
        langMap.put("German", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_German"));
        langMap.put("Greek", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Greek"));
        langMap.put("Gujarati", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Gujarati"));
        langMap.put("Hawaiian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Hawaiian"));
        langMap.put("Hebrew", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Hebrew"));
        langMap.put("Hindi", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Hindi"));
        langMap.put("Hungarian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Hungarian"));
        langMap.put("Icelandic", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Icelandic"));
        langMap.put("Indonesian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Indonesian"));
        langMap.put("Italian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Italian"));
        langMap.put("Japanese", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Japanese"));
        langMap.put("Kannada", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Kannada"));
        langMap.put("Khmer", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Khmer"));
        langMap.put("Konkani", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Konkani"));
        langMap.put("Korean", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Korean"));
        langMap.put("Lahnda", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Lahnda"));
        langMap.put("Lao", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Lao"));
        langMap.put("Latvian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Latvian"));
        langMap.put("Lithuanian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Lithuanian"));
        langMap.put("Macedonian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Macedonian"));
        langMap.put("Maithili", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Maithili"));
        langMap.put("Malayalam", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Malayalam"));
        langMap.put("Mandrin", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Mandrin"));
        langMap.put("Manipuri", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Manipuri"));
        langMap.put("Marathi", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Marathi"));
        langMap.put("Mewari", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Mewari"));
        langMap.put("Navaho", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Navaho"));
        langMap.put("Nepali", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Nepali"));
        langMap.put("Norwegian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Norwegian"));
        langMap.put("Oriya", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Oriya"));
        langMap.put("Pahari", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Pahari"));
        langMap.put("Pali", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Pali"));
        langMap.put("Panjabi", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Panjabi"));
        langMap.put("Persian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Persian"));
        langMap.put("Polish", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Polish"));
        langMap.put("Portuguese", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Portuguese"));
        langMap.put("Prakrit", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Prakrit"));
        langMap.put("Pusto", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Pusto"));
        langMap.put("Rajasthani", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Rajasthani"));
        langMap.put("Romanian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Romanian"));
        langMap.put("Russian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Russian"));
        langMap.put("Sanskrit", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Sanskrit"));
        langMap.put("Serb", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Serb"));
        langMap.put("Serbo_Croa", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Serbo_Croa"));
        langMap.put("Slovak", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Slovak"));
        langMap.put("Slovene", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Slovene"));
        langMap.put("Spanish", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Spanish"));
        langMap.put("Swedish", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Swedish"));
        langMap.put("Tagalog", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Tagalog"));
        langMap.put("Tamil", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Tamil"));
        langMap.put("Telugu", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Telugu"));
        langMap.put("Thai", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Thai"));
        langMap.put("Tibetan", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Tibetan"));
        langMap.put("Turkish", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Turkish"));
        langMap.put("Ukrainian", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Ukrainian"));
        langMap.put("Urdu", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Urdu"));
        langMap.put("Vietnamese", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Vietnamese"));
        langMap.put("Wendic", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Wendic"));
        langMap.put("Yiddish", NbBundle.getMessage(SubmitterBeanPanel.class, "Lang_Yiddish"));

        languages = langMap.values().toArray(new String[0]);
        Arrays.sort(languages);
    }
}
