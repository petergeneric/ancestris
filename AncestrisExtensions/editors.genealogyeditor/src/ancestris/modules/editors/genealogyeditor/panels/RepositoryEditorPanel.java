package ancestris.modules.editors.genealogyeditor.panels;

import genj.gedcom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.util.Exceptions;

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
public class RepositoryEditorPanel extends javax.swing.JPanel {

    private Repository mRepository;

    /**
     * Creates new form RepositoryEditorPanel
     */
    public RepositoryEditorPanel() {
        initComponents();
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
        noteCitationsListPanel = new ancestris.modules.editors.genealogyeditor.panels.NoteCitationsListPanel();
        referencesPanel = new javax.swing.JPanel();
        referencesListPanel = new ancestris.modules.editors.genealogyeditor.panels.ReferencesListPanel();

        repositoryIDLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.repositoryIDLabel.text"), new Object[] {})); // NOI18N

        repositoryIDTextField.setColumns(8);
        repositoryIDTextField.setEditable(false);
        repositoryIDTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.repositoryIDTextField.text"), new Object[] {})); // NOI18N
        repositoryIDTextField.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.repositoryIDTextField.toolTipText"), new Object[] {})); // NOI18N

        repositoryNameLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.repositoryNameLabel.text"), new Object[] {})); // NOI18N

        repositoryNameTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.repositoryNameTextField.text"), new Object[] {})); // NOI18N

        javax.swing.GroupLayout addressPanelLayout = new javax.swing.GroupLayout(addressPanel);
        addressPanel.setLayout(addressPanelLayout);
        addressPanelLayout.setHorizontalGroup(
            addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addressEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
        );
        addressPanelLayout.setVerticalGroup(
            addressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addressEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(RepositoryEditorPanel.class, "RepositoryEditorPanel.addressPanel.TabConstraints.tabTitle"), addressPanel); // NOI18N

        javax.swing.GroupLayout notesPanelLayout = new javax.swing.GroupLayout(notesPanel);
        notesPanel.setLayout(notesPanelLayout);
        notesPanelLayout.setHorizontalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
        );
        notesPanelLayout.setVerticalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Notes", new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Note.png")), notesPanel); // NOI18N

        javax.swing.GroupLayout referencesPanelLayout = new javax.swing.GroupLayout(referencesPanel);
        referencesPanel.setLayout(referencesPanelLayout);
        referencesPanelLayout.setHorizontalGroup(
            referencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, referencesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(referencesListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE))
        );
        referencesPanelLayout.setVerticalGroup(
            referencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, referencesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(referencesListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("References", new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/association.png")), referencesPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(repositoryNameLabel)
                            .addComponent(repositoryIDLabel))
                        .addGap(24, 24, 24)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(repositoryIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(repositoryNameTextField))))
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
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.editors.genealogyeditor.panels.AddressEditorPanel addressEditorPanel;
    private javax.swing.JPanel addressPanel;
    private javax.swing.JTabbedPane jTabbedPane1;
    private ancestris.modules.editors.genealogyeditor.panels.NoteCitationsListPanel noteCitationsListPanel;
    private javax.swing.JPanel notesPanel;
    private ancestris.modules.editors.genealogyeditor.panels.ReferencesListPanel referencesListPanel;
    private javax.swing.JPanel referencesPanel;
    private javax.swing.JLabel repositoryIDLabel;
    private javax.swing.JTextField repositoryIDTextField;
    private javax.swing.JLabel repositoryNameLabel;
    private javax.swing.JTextField repositoryNameTextField;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the repository
     */
    public Repository getRepository() {
        return mRepository;
    }

    /**
     * @param repository the repository to set
     */
    public void setRepository(Repository repository) {
        this.mRepository = repository;

        repositoryIDTextField.setText(mRepository.getId());

        Property repositoryName = mRepository.getProperty("NAME");
        repositoryNameTextField.setText(repositoryName != null ? repositoryName.getValue() : "");

        Property address = mRepository.getProperty("ADDR");
        addressEditorPanel.set(mRepository, address);

        List<Entity> entitiesList = new ArrayList<Entity>();
        for (PropertyXRef entityRef : mRepository.getProperties(PropertyXRef.class)) {
            entitiesList.add(entityRef.getTargetEntity());
        }

        referencesListPanel.set(mRepository, entitiesList);

        noteCitationsListPanel.setNotesList(mRepository, Arrays.asList(mRepository.getProperties("NOTE")));

    }

    public Repository commit() {
        try {
            mRepository.getGedcom().doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                }
            }); // end of doUnitOfWork

            addressEditorPanel.commit();

            return mRepository;
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
}
