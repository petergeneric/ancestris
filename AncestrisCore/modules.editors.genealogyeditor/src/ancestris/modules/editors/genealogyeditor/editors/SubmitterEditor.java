package ancestris.modules.editors.genealogyeditor.editors;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyChange;
import genj.gedcom.Submitter;
import genj.view.ViewContext;
import java.awt.Component;
import java.util.Arrays;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class SubmitterEditor extends EntityEditor {

    private Context context;
    private Submitter mSubmitter;

    /**
     * Creates new form SubmitterEditor
     */
    public SubmitterEditor() {
        this(false);
    }

    public SubmitterEditor(boolean isNew) {
        super(isNew);
        initComponents();
        submitterNameTextField.getDocument().addDocumentListener(changes);
        firstPreferLanguageTextField.getDocument().addDocumentListener(changes);
        secondPreferLanguageTextField.getDocument().addDocumentListener(changes);
        thirdPreferLanguageTextField.getDocument().addDocumentListener(changes);
        addressEditorPanel.addChangeListener(changes);
        multimediaObjectCitationsTablePanel.addChangeListener(changes);
        noteCitationsTablePanel.addChangeListener(changes);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        languagesPanel = new javax.swing.JPanel();
        firstPreferLanguageTextField = new javax.swing.JTextField();
        secondPreferLanguageTextField = new javax.swing.JTextField();
        thirdPreferLanguageTextField = new javax.swing.JTextField();
        changeDateLabel = new javax.swing.JLabel();
        changeDateLabeldate = new javax.swing.JLabel();
        submitterNameLabel = new javax.swing.JLabel();
        submitterNameTextField = new javax.swing.JTextField();
        submitterTabbedPane = new javax.swing.JTabbedPane();
        addressEditorPanel = new ancestris.modules.editors.genealogyeditor.panels.AddressEditorPanel();
        multimediaObjectCitationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.MultimediaObjectCitationsTablePanel();
        noteCitationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.NoteCitationsTablePanel();

        languagesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(SubmitterEditor.class, "SubmitterEditor.languagesPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP)); // NOI18N

        javax.swing.GroupLayout languagesPanelLayout = new javax.swing.GroupLayout(languagesPanel);
        languagesPanel.setLayout(languagesPanelLayout);
        languagesPanelLayout.setHorizontalGroup(
            languagesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(languagesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(languagesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(firstPreferLanguageTextField)
                    .addComponent(secondPreferLanguageTextField)
                    .addComponent(thirdPreferLanguageTextField))
                .addContainerGap())
        );
        languagesPanelLayout.setVerticalGroup(
            languagesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(languagesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(firstPreferLanguageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(secondPreferLanguageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(thirdPreferLanguageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        changeDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.openide.awt.Mnemonics.setLocalizedText(changeDateLabel, org.openide.util.NbBundle.getMessage(SubmitterEditor.class, "SubmitterEditor.changeDateLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(submitterNameLabel, org.openide.util.NbBundle.getMessage(SubmitterEditor.class, "SubmitterEditor.submitterNameLabel.text")); // NOI18N

        submitterTabbedPane.addTab(org.openide.util.NbBundle.getMessage(SubmitterEditor.class, "SubmitterEditor.addressEditorPanel.TabConstraints.tabTitle"), addressEditorPanel); // NOI18N
        submitterTabbedPane.addTab(org.openide.util.NbBundle.getMessage(SubmitterEditor.class, "SubmitterEditor.multimediaObjectCitationsTablePanel.TabConstraints.tabTitle"), multimediaObjectCitationsTablePanel); // NOI18N
        submitterTabbedPane.addTab(org.openide.util.NbBundle.getMessage(SubmitterEditor.class, "SubmitterEditor.noteCitationsTablePanel.TabConstraints.tabTitle"), noteCitationsTablePanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(languagesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(submitterTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(changeDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeDateLabeldate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(submitterNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(submitterNameTextField)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(submitterNameLabel)
                    .addComponent(submitterNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(languagesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submitterTabbedPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(changeDateLabel)
                    .addComponent(changeDateLabeldate, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.editors.genealogyeditor.panels.AddressEditorPanel addressEditorPanel;
    private javax.swing.JLabel changeDateLabel;
    private javax.swing.JLabel changeDateLabeldate;
    private javax.swing.JTextField firstPreferLanguageTextField;
    private javax.swing.JPanel languagesPanel;
    private ancestris.modules.editors.genealogyeditor.panels.MultimediaObjectCitationsTablePanel multimediaObjectCitationsTablePanel;
    private ancestris.modules.editors.genealogyeditor.panels.NoteCitationsTablePanel noteCitationsTablePanel;
    private javax.swing.JTextField secondPreferLanguageTextField;
    private javax.swing.JLabel submitterNameLabel;
    private javax.swing.JTextField submitterNameTextField;
    private javax.swing.JTabbedPane submitterTabbedPane;
    private javax.swing.JTextField thirdPreferLanguageTextField;
    // End of variables declaration//GEN-END:variables
    @Override
    public ViewContext getContext() {
        return new ViewContext(context);
    }

    @Override
    public Component getEditorComponent() {
        return this;
    }

    @Override
    protected String getTitleImpl() {
        if (context == null || context.getEntity() == null) {
            return "";
        }
        return (new ViewContext(context.getEntity())).getText();
    }

    /*
     * 5.5
     * n @<XREF:SUBM>@ SUBM
     * +1 NAME <SUBMITTER_NAME>
     * +1 <<ADDRESS_STRUCTURE>>
     * +1 <<MULTIMEDIA_LINK>>
     * +1 LANG <LANGUAGE_PREFERENCE>
     * +1 RFN <SUBMITTER_REGISTERED_RFN>
     * +1 RIN <AUTOMATED_RECORD_ID>
     * +1 <<NOTE_STRUCTURE>>
     * +1 <<CHANGE_DATE>>
     * 5.5.1
     * n @<XREF:SUBM>@ SUBM
     * +1 NAME <SUBMITTER_NAME>
     * +1 <<ADDRESS_STRUCTURE>>
     * +1 <<MULTIMEDIA_LINK>>
     * +1 LANG <LANGUAGE_PREFERENCE>
     * +1 RFN <SUBMITTER_REGISTERED_RFN>
     * +1 RIN <AUTOMATED_RECORD_ID>
     * +1 <<NOTE_STRUCTURE>>
     * +1 <<CHANGE_DATE>>
     */
    @Override
    protected void setContextImpl(Context context) {
        this.context = context;

        Entity entity = context.getEntity();

        if (entity != null && entity instanceof Submitter) {
            mSubmitter = (Submitter) entity;

            setTitle(NbBundle.getMessage(SubmitterEditor.class, isNew() ? "SubmitterEditor.create.title" : "SubmitterEditor.edit.title", mSubmitter));

            /*
             * +1 NAME <SUBMITTER_NAME>
             */
            Property name = mSubmitter.getProperty("NAME");
            submitterNameTextField.setText(name != null ? name.getDisplayValue() : "");

            /*
             * +1 <<ADDRESS_STRUCTURE>>
             */
            Property address = mSubmitter.getProperty("ADDR", false);
            addressEditorPanel.set(mSubmitter, address);

            /*
             * +1 <<MULTIMEDIA_LINK>>
             */
            multimediaObjectCitationsTablePanel.set(mSubmitter, Arrays.asList(mSubmitter.getProperties("OBJE")));

            /*
             * +1 LANG <LANGUAGE_PREFERENCE>    
             */
            Property[] preferedLanguages = mSubmitter.getProperties("LANG", false);
            int index = 0;
            for (; index < preferedLanguages.length; index++) {
                if (index == 0) {
                    firstPreferLanguageTextField.setText(preferedLanguages[0].getValue());
                } else if (index == 1) {
                    secondPreferLanguageTextField.setText(preferedLanguages[1].getValue());
                } else if (index == 2) {
                    thirdPreferLanguageTextField.setText(preferedLanguages[2].getValue());
                }
            }

            for (; index < 3; index++) {
                if (index == 0) {
                    firstPreferLanguageTextField.setText("");
                } else if (index == 1) {
                    secondPreferLanguageTextField.setText("");
                } else if (index == 2) {
                    thirdPreferLanguageTextField.setText("");
                }
            }
            /*
             * +1 <<NOTE_STRUCTURE>>
             */
            if (!mSubmitter.getGedcom().getGrammar().getVersion().equals("5.5.1")) {
                int indexOfTab = submitterTabbedPane.indexOfTab(NbBundle.getMessage(SubmitterEditor.class, "SubmitterEditor.noteCitationsTablePanel.TabConstraints.tabTitle"));
                if (indexOfTab != -1) {
                    submitterTabbedPane.removeTabAt(indexOfTab);
                }
            } else {
                if (submitterTabbedPane.indexOfTab(NbBundle.getMessage(SubmitterEditor.class, "SubmitterEditor.noteCitationsTablePanel.TabConstraints.tabTitle")) == -1) {
                    submitterTabbedPane.addTab(org.openide.util.NbBundle.getMessage(SubmitterEditor.class, "SubmitterEditor.noteCitationsTablePanel.TabConstraints.tabTitle"), noteCitationsTablePanel);
                }
                noteCitationsTablePanel.set(mSubmitter, Arrays.asList(mSubmitter.getProperties("NOTE")));
            }

            /*
             * +1 <<CHANGE_DATE>>
             * Handle by gedcom doUnitOfWork
             */
            Property changeDate = mSubmitter.getProperty("CHAN");
            if (changeDate != null) {
                changeDateLabeldate.setText(((PropertyChange) changeDate).getDisplayValue());
            }
        }
    }

    @Override
    public void commit() throws GedcomException {

        if (changes.hasChanged()) {
            Property name = mSubmitter.getProperty("NAME");
            if (name != null) {
                name.setValue(submitterNameTextField.getText());
            } else {
                mSubmitter.addProperty("NAME", submitterNameTextField.getText());
            }

            Property[] preferedLanguages = mSubmitter.getProperties("LANG", false);

            if (!firstPreferLanguageTextField.getText().isEmpty()) {
                if (preferedLanguages.length > 0 && preferedLanguages[0] != null) {
                    if (firstPreferLanguageTextField.getText().isEmpty()) {
                        preferedLanguages[0].setValue(firstPreferLanguageTextField.getText());
                    }
                } else {
                    mSubmitter.addProperty("LANG", firstPreferLanguageTextField.getText());
                }
            } else {
                if (preferedLanguages.length > 0 && preferedLanguages[0] != null) {
                    mSubmitter.delProperty(preferedLanguages[0]);
                }
            }

            if (!secondPreferLanguageTextField.getText().isEmpty()) {
                if (preferedLanguages.length > 1 && preferedLanguages[1] != null) {
                    if (secondPreferLanguageTextField.getText().isEmpty()) {
                        preferedLanguages[1].setValue(secondPreferLanguageTextField.getText());
                    }
                } else {
                    mSubmitter.addProperty("LANG", secondPreferLanguageTextField.getText());
                }
            } else {
                if (preferedLanguages.length > 1 && preferedLanguages[1] != null) {
                    mSubmitter.delProperty(preferedLanguages[1]);
                }
            }

            if (!thirdPreferLanguageTextField.getText().isEmpty()) {
                if (preferedLanguages.length > 2 && preferedLanguages[2] != null) {
                    if (thirdPreferLanguageTextField.getText().isEmpty()) {
                        preferedLanguages[2].setValue(thirdPreferLanguageTextField.getText());
                    }
                } else {
                    mSubmitter.addProperty("LANG", thirdPreferLanguageTextField.getText());
                }
            } else {
                if (preferedLanguages.length > 2 && preferedLanguages[2] != null) {
                    mSubmitter.delProperty(preferedLanguages[2]);
                }
            }

            addressEditorPanel.commit();
        }
    }

    @Override
    public Entity getEditedEntity() {
        return mSubmitter;
    }
}
