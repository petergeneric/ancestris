package ancestris.modules.editors.genealogyeditor.panels;

import genj.gedcom.*;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;

/**
 *
 * @author dominique
 */
public class RepositoryEditorPanel extends javax.swing.JPanel {

    private Repository repository;

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
        repositoryAddressLabel = new javax.swing.JLabel();
        repositoryAddressTextField = new javax.swing.JTextField();
        addRepositoryAddressButton = new javax.swing.JButton();
        editRepositoryAddressButton = new javax.swing.JButton();
        removeRepositoryAddressButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        referencesPanel = new javax.swing.JPanel();
        referencesListPanel = new ancestris.modules.editors.genealogyeditor.panels.ReferencesListPanel();
        notesPanel = new javax.swing.JPanel();
        notesListPanel = new ancestris.modules.editors.genealogyeditor.panels.NotesListPanel();

        repositoryIDLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.repositoryIDLabel.text"), new Object[] {})); // NOI18N

        repositoryIDTextField.setColumns(8);
        repositoryIDTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.repositoryIDTextField.text"), new Object[] {})); // NOI18N
        repositoryIDTextField.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.repositoryIDTextField.toolTipText"), new Object[] {})); // NOI18N

        repositoryNameLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.repositoryNameLabel.text"), new Object[] {})); // NOI18N

        repositoryNameTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.repositoryNameTextField.text"), new Object[] {})); // NOI18N

        repositoryAddressLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.repositoryAddressLabel.text"), new Object[] {})); // NOI18N

        repositoryAddressTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.repositoryAddressTextField.text"), new Object[] {})); // NOI18N

        addRepositoryAddressButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addRepositoryAddressButton.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.addRepositoryAddressButton.text"), new Object[] {})); // NOI18N

        editRepositoryAddressButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
        editRepositoryAddressButton.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("RepositoryEditorPanel.editRepositoryAddressButton.text"), new Object[] {})); // NOI18N
        editRepositoryAddressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editRepositoryAddressButtonActionPerformed(evt);
            }
        });

        removeRepositoryAddressButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N

        javax.swing.GroupLayout referencesPanelLayout = new javax.swing.GroupLayout(referencesPanel);
        referencesPanel.setLayout(referencesPanelLayout);
        referencesPanelLayout.setHorizontalGroup(
            referencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, referencesPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(referencesListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        referencesPanelLayout.setVerticalGroup(
            referencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, referencesPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(referencesListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("References", new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Association.png")), referencesPanel); // NOI18N

        javax.swing.GroupLayout notesPanelLayout = new javax.swing.GroupLayout(notesPanel);
        notesPanel.setLayout(notesPanelLayout);
        notesPanelLayout.setHorizontalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notesPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(notesListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        notesPanelLayout.setVerticalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notesPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(notesListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("Notes", new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Note.png")), notesPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(repositoryNameLabel)
                            .addComponent(repositoryIDLabel)
                            .addComponent(repositoryAddressLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(repositoryAddressTextField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addRepositoryAddressButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(editRepositoryAddressButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeRepositoryAddressButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(repositoryIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(repositoryNameTextField)))
                    .addComponent(jTabbedPane1))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addRepositoryAddressButton)
                    .addComponent(editRepositoryAddressButton)
                    .addComponent(removeRepositoryAddressButton)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(repositoryAddressLabel)
                        .addComponent(repositoryAddressTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void editRepositoryAddressButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editRepositoryAddressButtonActionPerformed
    }//GEN-LAST:event_editRepositoryAddressButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addRepositoryAddressButton;
    private javax.swing.JButton editRepositoryAddressButton;
    private javax.swing.JTabbedPane jTabbedPane1;
    private ancestris.modules.editors.genealogyeditor.panels.NotesListPanel notesListPanel;
    private javax.swing.JPanel notesPanel;
    private ancestris.modules.editors.genealogyeditor.panels.ReferencesListPanel referencesListPanel;
    private javax.swing.JPanel referencesPanel;
    private javax.swing.JButton removeRepositoryAddressButton;
    private javax.swing.JLabel repositoryAddressLabel;
    private javax.swing.JTextField repositoryAddressTextField;
    private javax.swing.JLabel repositoryIDLabel;
    private javax.swing.JTextField repositoryIDTextField;
    private javax.swing.JLabel repositoryNameLabel;
    private javax.swing.JTextField repositoryNameTextField;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the repository
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * @param repository the repository to set
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
        update();
    }

    private void update() {
        repositoryIDTextField.setText(repository.getId());

        Property repositoryName = repository.getProperty("NAME");
        repositoryNameTextField.setText(repositoryName != null ? repositoryName.getValue() : "");

        Property address = repository.getProperty("ADDR");
        repositoryAddressTextField.setText(address != null ? address.getValue() : "");

        List<Entity> entitiesList = new ArrayList<Entity>();
        for (PropertyXRef entityRef : repository.getProperties(PropertyXRef.class)) {
            entitiesList.add(entityRef.getTargetEntity());
        }
        referencesListPanel.setReferencesList(repository, entitiesList);

        List<Note> notesList = new ArrayList<Note>();
        for (PropertyNote noteRef : repository.getProperties(PropertyNote.class)) {
            notesList.add((Note) noteRef.getTargetEntity());
        }
        notesListPanel.setNotesList(repository, notesList);
    }

    public void commit() {
        try {
            repository.getGedcom().doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    notesListPanel.commit();
                    referencesListPanel.commit();
                }
            }); // end of doUnitOfWork
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
