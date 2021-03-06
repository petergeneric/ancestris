package ancestris.modules.editors.genealogyeditor.editors;

import genj.gedcom.*;
import genj.view.ViewContext;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
/*
 * REPOSITORY_RECORD
 * n @<XREF:REPO>@ REPO
 * +1 NAME <NAME_OF_REPOSITORY>
 * +1 <<ADDRESS_STRUCTURE>>
 * +1 <<NOTE_STRUCTURE>>
 * +1 REFN <USER_REFERENCE_NUMBER>
 * +2 TYPE <USER_REFERENCE_TYPE>
 * +1 RIN <AUTOMATED_RECORD_ID>
 * +1 <<CHANGE_DATE>>
 */
public class RepositoryEditor extends EntityEditor {

    private Context context;
    private Repository mRepository;

    /**
     * Creates new form RepositoryEditorPanel
     */
    public RepositoryEditor() {
        this(false);
    }

    public RepositoryEditor(boolean isNew) {
        super(isNew);
        initComponents();
        repositoryNameTextField.getDocument().addDocumentListener(changes);
        addressEditorPanel.addChangeListener(changes);
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

        repositoryIDLabel = new javax.swing.JLabel();
        repositoryIDTextField = new javax.swing.JTextField();
        repositoryNameLabel = new javax.swing.JLabel();
        repositoryNameTextField = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        addressPanel = new javax.swing.JPanel();
        addressEditorPanel = new ancestris.modules.editors.genealogyeditor.panels.AddressEditorPanel();
        notesPanel = new javax.swing.JPanel();
        noteCitationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.NoteCitationsTablePanel();
        referencesPanel = new javax.swing.JPanel();
        referencesTablePanel = new ancestris.modules.editors.genealogyeditor.panels.ReferencesTablePanel();
        changeDateLabel = new javax.swing.JLabel();
        changeDateLabeldate = new javax.swing.JLabel();

        repositoryIDLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("RepositoryEditor.repositoryIDLabel.text"), new Object[] {})); // NOI18N

        repositoryIDTextField.setEditable(false);
        repositoryIDTextField.setColumns(8);
        repositoryIDTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("RepositoryEditor.repositoryIDTextField.text"), new Object[] {})); // NOI18N
        repositoryIDTextField.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("RepositoryEditor.repositoryIDTextField.toolTipText"), new Object[] {})); // NOI18N

        repositoryNameLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("RepositoryEditor.repositoryNameLabel.text"), new Object[] {})); // NOI18N

        repositoryNameTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("RepositoryEditor.repositoryNameTextField.text"), new Object[] {})); // NOI18N

        javax.swing.GroupLayout addressPanelLayout = new javax.swing.GroupLayout(addressPanel);
        addressPanel.setLayout(addressPanelLayout);
        addressPanelLayout.setHorizontalGroup(
            addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addressEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
        );
        addressPanelLayout.setVerticalGroup(
            addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addressEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(RepositoryEditor.class, "RepositoryEditor.addressPanel.TabConstraints.tabTitle"), addressPanel); // NOI18N

        javax.swing.GroupLayout notesPanelLayout = new javax.swing.GroupLayout(notesPanel);
        notesPanel.setLayout(notesPanelLayout);
        notesPanelLayout.setHorizontalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
        );
        notesPanelLayout.setVerticalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Notes", new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/note.png")), notesPanel); // NOI18N

        javax.swing.GroupLayout referencesPanelLayout = new javax.swing.GroupLayout(referencesPanel);
        referencesPanel.setLayout(referencesPanelLayout);
        referencesPanelLayout.setHorizontalGroup(
            referencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, referencesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(referencesTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE))
        );
        referencesPanelLayout.setVerticalGroup(
            referencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, referencesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(referencesTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("References", new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/association.png")), referencesPanel); // NOI18N

        changeDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        changeDateLabel.setText(org.openide.util.NbBundle.getMessage(RepositoryEditor.class, "RepositoryEditor.changeDateLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(repositoryNameLabel)
                        .addGap(24, 24, 24)
                        .addComponent(repositoryNameTextField))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(repositoryIDLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(repositoryIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(changeDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeDateLabeldate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(repositoryIDLabel)
                    .addComponent(repositoryIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(repositoryNameLabel)
                    .addComponent(repositoryNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(changeDateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(changeDateLabeldate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.editors.genealogyeditor.panels.AddressEditorPanel addressEditorPanel;
    private javax.swing.JPanel addressPanel;
    private javax.swing.JLabel changeDateLabel;
    private javax.swing.JLabel changeDateLabeldate;
    private javax.swing.JTabbedPane jTabbedPane1;
    private ancestris.modules.editors.genealogyeditor.panels.NoteCitationsTablePanel noteCitationsTablePanel;
    private javax.swing.JPanel notesPanel;
    private javax.swing.JPanel referencesPanel;
    private ancestris.modules.editors.genealogyeditor.panels.ReferencesTablePanel referencesTablePanel;
    private javax.swing.JLabel repositoryIDLabel;
    private javax.swing.JTextField repositoryIDTextField;
    private javax.swing.JLabel repositoryNameLabel;
    private javax.swing.JTextField repositoryNameTextField;
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

    @Override
    protected void setContextImpl(Context context) {
        this.context = context;

        changes.mute();
        Entity entity = context.getEntity();
        if (entity != null && entity instanceof Repository && entity.getGedcom() != null) {
            mRepository = (Repository) entity;

            setTitle(NbBundle.getMessage(RepositoryEditor.class, isNew() ? "RepositoryEditor.create.title" : "RepositoryEditor.edit.title", mRepository));

            repositoryIDTextField.setText(mRepository.getId());

            Property repositoryName = mRepository.getProperty("NAME");
            repositoryNameTextField.setText(repositoryName != null ? repositoryName.getValue() : "");

            Property address = mRepository.getProperty("ADDR");
            addressEditorPanel.set(mRepository, address);

            List<Entity> entitiesList = new ArrayList<>();
            for (PropertyXRef entityRef : mRepository.getProperties(PropertyXRef.class)) {
                entitiesList.add(entityRef.getTargetEntity());
            }

            referencesTablePanel.set(mRepository, entitiesList);

            noteCitationsTablePanel.set(mRepository, Arrays.asList(mRepository.getProperties("NOTE")));

            Property changeDate = mRepository.getProperty("CHAN");
            if (changeDate != null) {
                changeDateLabeldate.setText(((PropertyChange) changeDate).getDisplayValue());
            }
        }
        changes.unmute();
    }

    @Override
    public void commit() {
        if (changes.hasChanged()) {
            Property repositoryName = mRepository.getProperty("NAME");
            if (repositoryName == null) {
                mRepository.addProperty("NAME", repositoryNameTextField.getText());
            } else {
                repositoryName.setValue(repositoryNameTextField.getText());
            }
        }
        addressEditorPanel.commit();
    }

    @Override
    public Entity getEditedEntity() {
        return mRepository;
    }
}
