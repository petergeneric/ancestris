package ancestris.modules.editors.genealogyeditor.panels;

import genj.gedcom.*;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;

/**
 *
 * @author dominique
 */
public class SourceEditorPanel extends javax.swing.JPanel {

    private Source source;

    /**
     * Creates new form SourceEditorPanel
     */
    public SourceEditorPanel() {
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

        sourceIDLabel = new javax.swing.JLabel();
        sourceIDTextField = new javax.swing.JTextField();
        authorLabel = new javax.swing.JLabel();
        authorTextField = new javax.swing.JTextField();
        sourceTitleLabel = new javax.swing.JLabel();
        sourceTitleTextField = new javax.swing.JTextField();
        recordedEventsLabel = new javax.swing.JLabel();
        recordedEventsTextField = new javax.swing.JTextField();
        sourceInformationTabbedPane = new javax.swing.JTabbedPane();
        repositoriesPanel = new javax.swing.JPanel();
        repositoriesListPanel = new ancestris.modules.editors.genealogyeditor.panels.RepositoriesListPanel();
        notesPanel = new javax.swing.JPanel();
        notesListPanel = new ancestris.modules.editors.genealogyeditor.panels.NotesListPanel();
        referencesPanel = new javax.swing.JPanel();
        referencesListPanel = new ancestris.modules.editors.genealogyeditor.panels.ReferencesListPanel();

        sourceIDLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.sourceIDLabel.text"), new Object[] {})); // NOI18N

        sourceIDTextField.setColumns(8);
        sourceIDTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.sourceIDTextField.text"), new Object[] {})); // NOI18N

        authorLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.authorLabel.text"), new Object[] {})); // NOI18N

        authorTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.authorTextField.text"), new Object[] {})); // NOI18N

        sourceTitleLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.sourceTitleLabel.text"), new Object[] {})); // NOI18N

        sourceTitleTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.sourceTitleTextField.text"), new Object[] {})); // NOI18N

        recordedEventsLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.recordedEventsLabel.text"), new Object[] {})); // NOI18N

        recordedEventsTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.recordedEventsTextField.text"), new Object[] {})); // NOI18N

        javax.swing.GroupLayout repositoriesPanelLayout = new javax.swing.GroupLayout(repositoriesPanel);
        repositoriesPanel.setLayout(repositoriesPanelLayout);
        repositoriesPanelLayout.setHorizontalGroup(
            repositoriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, repositoriesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(repositoriesListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        repositoriesPanelLayout.setVerticalGroup(
            repositoriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, repositoriesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(repositoriesListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE))
        );

        sourceInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.repositoriesPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Repository.png")), repositoriesPanel); // NOI18N

        javax.swing.GroupLayout notesPanelLayout = new javax.swing.GroupLayout(notesPanel);
        notesPanel.setLayout(notesPanelLayout);
        notesPanelLayout.setHorizontalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(notesListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        notesPanelLayout.setVerticalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(notesListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE))
        );

        sourceInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.notesPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Note.png")), notesPanel); // NOI18N

        javax.swing.GroupLayout referencesPanelLayout = new javax.swing.GroupLayout(referencesPanel);
        referencesPanel.setLayout(referencesPanelLayout);
        referencesPanelLayout.setHorizontalGroup(
            referencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, referencesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(referencesListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        referencesPanelLayout.setVerticalGroup(
            referencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(referencesListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
        );

        sourceInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.referencesPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/association.png")), referencesPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sourceTitleLabel)
                            .addComponent(sourceIDLabel)
                            .addComponent(recordedEventsLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sourceTitleTextField)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(sourceIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(recordedEventsTextField)))
                    .addComponent(sourceInformationTabbedPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(authorLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(authorTextField)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceIDLabel)
                    .addComponent(sourceIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(authorLabel)
                    .addComponent(authorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceTitleLabel)
                    .addComponent(sourceTitleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(recordedEventsLabel)
                    .addComponent(recordedEventsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourceInformationTabbedPane)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel authorLabel;
    private javax.swing.JTextField authorTextField;
    private ancestris.modules.editors.genealogyeditor.panels.NotesListPanel notesListPanel;
    private javax.swing.JPanel notesPanel;
    private javax.swing.JLabel recordedEventsLabel;
    private javax.swing.JTextField recordedEventsTextField;
    private ancestris.modules.editors.genealogyeditor.panels.ReferencesListPanel referencesListPanel;
    private javax.swing.JPanel referencesPanel;
    private ancestris.modules.editors.genealogyeditor.panels.RepositoriesListPanel repositoriesListPanel;
    private javax.swing.JPanel repositoriesPanel;
    private javax.swing.JLabel sourceIDLabel;
    private javax.swing.JTextField sourceIDTextField;
    private javax.swing.JTabbedPane sourceInformationTabbedPane;
    private javax.swing.JLabel sourceTitleLabel;
    private javax.swing.JTextField sourceTitleTextField;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the source
     */
    public Source getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(Source source) {
        this.source = source;
        update();
    }

    private void update() {
        sourceIDTextField.setText(source.getId());
        Property propertyByPath = source.getPropertyByPath("DATA:EVEN");
        if (propertyByPath != null) {
            recordedEventsTextField.setText(propertyByPath.getDisplayValue());
        }

        List<Note> notesList = new ArrayList<Note>();
        for (PropertyNote noteRef : source.getProperties(PropertyNote.class)) {
            notesList.add((Note) noteRef.getTargetEntity());
        }
        notesListPanel.setNotesList(source, notesList);

        List<Repository> repositporiesList = new ArrayList<Repository>();
        for (PropertyRepository repositoryRef : source.getProperties(PropertyRepository.class)) {
            repositporiesList.add((Repository) repositoryRef.getTargetEntity());
        }
        repositoriesListPanel.set(source, repositporiesList);

        List<Entity> entitiesList = new ArrayList<Entity>();
        for (PropertyXRef entityRef : source.getProperties(PropertyXRef.class)) {
            entitiesList.add(entityRef.getTargetEntity());
        }
        referencesListPanel.set(source, entitiesList);
    }

    public void commit() {
        try {
            source.getGedcom().doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    notesListPanel.commit();
                    repositoriesListPanel.commit();
                    referencesListPanel.commit();
                }
            }); // end of doUnitOfWork
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
