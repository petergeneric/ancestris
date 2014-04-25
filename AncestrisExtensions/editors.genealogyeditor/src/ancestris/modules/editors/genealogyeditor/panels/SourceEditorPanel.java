package ancestris.modules.editors.genealogyeditor.panels;

import genj.gedcom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.util.Exceptions;

/*
 * SOURCE_RECORD:=
 * n @<XREF:SOUR>@ SOUR
 * +1 DATA
 * +2 EVEN <EVENTS_RECORDED>
 * +3 DATE <DATE_PERIOD>
 * +3 PLAC <SOURCE_JURISDICTION_PLACE>
 * +2 AGNC <RESPONSIBLE_AGENCY>
 * +2 <<NOTE_STRUCTURE>>
 * +1 AUTH <SOURCE_ORIGINATOR>
 * +2 [CONC|CONT] <SOURCE_ORIGINATOR>
 * +1 TITL <SOURCE_DESCRIPTIVE_TITLE>
 * +2 [CONC|CONT] <SOURCE_DESCRIPTIVE_TITLE>
 * +1 ABBR <SOURCE_FILED_BY_ENTRY>
 * +1 PUBL <SOURCE_PUBLICATION_FACTS>
 * +2 [CONC|CONT] <SOURCE_PUBLICATION_FACTS>
 * +1 TEXT <TEXT_FROM_SOURCE>
 * +2 [CONC|CONT] <TEXT_FROM_SOURCE>
 * +1 <<SOURCE_REPOSITORY_CITATION>>
 * +1 REFN <USER_REFERENCE_NUMBER>
 * +2 TYPE <USER_REFERENCE_TYPE>
 * +1 RIN <AUTOMATED_RECORD_ID>
 * +1 <<CHANGE_DATE>>
 * +1 <<NOTE_STRUCTURE>>
 * +1 <<MULTIMEDIA_LINK>>
 */
/**
 *
 * @author dominique
 */
public class SourceEditorPanel extends javax.swing.JPanel {

    private Source mSource;

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
        agencyLabel = new javax.swing.JLabel();
        agencyTextField = new javax.swing.JTextField();
        sourceInformationTabbedPane = new javax.swing.JTabbedPane();
        registeredEventsPanel = new javax.swing.JPanel();
        eventTypePanel = new ancestris.modules.editors.genealogyeditor.panels.SourceEventTypeListPanel();
        sourceTextPanel = new javax.swing.JPanel();
        sourceTextScrollPane = new javax.swing.JScrollPane();
        sourceTextTextArea = new javax.swing.JTextArea();
        SourceTextToolBar = new javax.swing.JToolBar();
        publicationFactsPanel = new javax.swing.JPanel();
        publicationFactsToolBar = new javax.swing.JToolBar();
        publicationFactsScrollPane = new javax.swing.JScrollPane();
        publicationFactsTextArea = new javax.swing.JTextArea();
        repositoriesPanel = new javax.swing.JPanel();
        repositoryCitationsListPanel = new ancestris.modules.editors.genealogyeditor.panels.RepositoryCitationsListPanel();
        notesPanel = new javax.swing.JPanel();
        noteCitationsListPanel = new ancestris.modules.editors.genealogyeditor.panels.NoteCitationsListPanel();
        referencesPanel = new javax.swing.JPanel();
        referencesListPanel = new ancestris.modules.editors.genealogyeditor.panels.ReferencesListPanel();
        multimediaObjectPanel = new javax.swing.JPanel();
        multimediaObjectCitationsListPanel = new ancestris.modules.editors.genealogyeditor.panels.MultimediaObjectCitationsListPanel();
        abbreviationLabel = new javax.swing.JLabel();
        abbreviationTextField = new javax.swing.JTextField();

        sourceIDLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.sourceIDLabel.text"), new Object[] {})); // NOI18N

        sourceIDTextField.setEditable(false);
        sourceIDTextField.setColumns(8);
        sourceIDTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.sourceIDTextField.text"), new Object[] {})); // NOI18N

        authorLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.authorLabel.text"), new Object[] {})); // NOI18N

        authorTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.authorTextField.text"), new Object[] {})); // NOI18N

        sourceTitleLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.sourceTitleLabel.text"), new Object[] {})); // NOI18N

        sourceTitleTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.sourceTitleTextField.text"), new Object[] {})); // NOI18N

        agencyLabel.setText(org.openide.util.NbBundle.getMessage(SourceEditorPanel.class, "SourceEditorPanel.agencyLabel.text")); // NOI18N

        sourceInformationTabbedPane.setRequestFocusEnabled(false);

        javax.swing.GroupLayout registeredEventsPanelLayout = new javax.swing.GroupLayout(registeredEventsPanel);
        registeredEventsPanel.setLayout(registeredEventsPanelLayout);
        registeredEventsPanelLayout.setHorizontalGroup(
            registeredEventsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(eventTypePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
        );
        registeredEventsPanelLayout.setVerticalGroup(
            registeredEventsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(eventTypePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
        );

        sourceInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.registeredEventsPanel.TabConstraints.tabTitle"), new Object[] {}), registeredEventsPanel); // NOI18N

        sourceTextTextArea.setColumns(20);
        sourceTextTextArea.setRows(5);
        sourceTextScrollPane.setViewportView(sourceTextTextArea);

        SourceTextToolBar.setRollover(true);

        javax.swing.GroupLayout sourceTextPanelLayout = new javax.swing.GroupLayout(sourceTextPanel);
        sourceTextPanel.setLayout(sourceTextPanelLayout);
        sourceTextPanelLayout.setHorizontalGroup(
            sourceTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourceTextScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
            .addComponent(SourceTextToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        sourceTextPanelLayout.setVerticalGroup(
            sourceTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceTextPanelLayout.createSequentialGroup()
                .addComponent(SourceTextToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourceTextScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
        );

        sourceInformationTabbedPane.addTab(org.openide.util.NbBundle.getMessage(SourceEditorPanel.class, "SourceEditorPanel.sourceTextPanel.TabConstraints.tabTitle"), sourceTextPanel); // NOI18N

        publicationFactsToolBar.setRollover(true);

        publicationFactsTextArea.setColumns(20);
        publicationFactsTextArea.setRows(5);
        publicationFactsScrollPane.setViewportView(publicationFactsTextArea);

        javax.swing.GroupLayout publicationFactsPanelLayout = new javax.swing.GroupLayout(publicationFactsPanel);
        publicationFactsPanel.setLayout(publicationFactsPanelLayout);
        publicationFactsPanelLayout.setHorizontalGroup(
            publicationFactsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(publicationFactsToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(publicationFactsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
        );
        publicationFactsPanelLayout.setVerticalGroup(
            publicationFactsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(publicationFactsPanelLayout.createSequentialGroup()
                .addComponent(publicationFactsToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(publicationFactsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
        );

        sourceInformationTabbedPane.addTab(org.openide.util.NbBundle.getMessage(SourceEditorPanel.class, "SourceEditorPanel.publicationFactsPanel.TabConstraints.tabTitle"), publicationFactsPanel); // NOI18N

        repositoriesPanel.setRequestFocusEnabled(false);

        javax.swing.GroupLayout repositoriesPanelLayout = new javax.swing.GroupLayout(repositoriesPanel);
        repositoriesPanel.setLayout(repositoriesPanelLayout);
        repositoriesPanelLayout.setHorizontalGroup(
            repositoriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(repositoryCitationsListPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        repositoriesPanelLayout.setVerticalGroup(
            repositoriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(repositoryCitationsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
        );

        sourceInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.repositoriesPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/repository.png")), repositoriesPanel); // NOI18N

        notesPanel.setRequestFocusEnabled(false);

        javax.swing.GroupLayout notesPanelLayout = new javax.swing.GroupLayout(notesPanel);
        notesPanel.setLayout(notesPanelLayout);
        notesPanelLayout.setHorizontalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        notesPanelLayout.setVerticalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
        );

        sourceInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.notesPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Note.png")), notesPanel); // NOI18N

        referencesPanel.setRequestFocusEnabled(false);

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
            .addComponent(referencesListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
        );

        sourceInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("SourceEditorPanel.referencesPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/association.png")), referencesPanel); // NOI18N

        javax.swing.GroupLayout multimediaObjectPanelLayout = new javax.swing.GroupLayout(multimediaObjectPanel);
        multimediaObjectPanel.setLayout(multimediaObjectPanelLayout);
        multimediaObjectPanelLayout.setHorizontalGroup(
            multimediaObjectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(multimediaObjectCitationsListPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        multimediaObjectPanelLayout.setVerticalGroup(
            multimediaObjectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(multimediaObjectCitationsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
        );

        sourceInformationTabbedPane.addTab(org.openide.util.NbBundle.getMessage(SourceEditorPanel.class, "SourceEditorPanel.multimediaObjectPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Media.png")), multimediaObjectPanel); // NOI18N

        abbreviationLabel.setText(org.openide.util.NbBundle.getMessage(SourceEditorPanel.class, "SourceEditorPanel.abbreviationLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sourceInformationTabbedPane)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(sourceIDLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourceIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(authorLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                                .addComponent(sourceTitleLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(agencyLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(abbreviationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(agencyTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(sourceTitleTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(authorTextField)
                            .addComponent(abbreviationTextField))))
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
                    .addComponent(abbreviationLabel)
                    .addComponent(abbreviationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(agencyLabel)
                    .addComponent(agencyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourceInformationTabbedPane)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar SourceTextToolBar;
    private javax.swing.JLabel abbreviationLabel;
    private javax.swing.JTextField abbreviationTextField;
    private javax.swing.JLabel agencyLabel;
    private javax.swing.JTextField agencyTextField;
    private javax.swing.JLabel authorLabel;
    private javax.swing.JTextField authorTextField;
    private ancestris.modules.editors.genealogyeditor.panels.SourceEventTypeListPanel eventTypePanel;
    private ancestris.modules.editors.genealogyeditor.panels.MultimediaObjectCitationsListPanel multimediaObjectCitationsListPanel;
    private javax.swing.JPanel multimediaObjectPanel;
    private ancestris.modules.editors.genealogyeditor.panels.NoteCitationsListPanel noteCitationsListPanel;
    private javax.swing.JPanel notesPanel;
    private javax.swing.JPanel publicationFactsPanel;
    private javax.swing.JScrollPane publicationFactsScrollPane;
    private javax.swing.JTextArea publicationFactsTextArea;
    private javax.swing.JToolBar publicationFactsToolBar;
    private ancestris.modules.editors.genealogyeditor.panels.ReferencesListPanel referencesListPanel;
    private javax.swing.JPanel referencesPanel;
    private javax.swing.JPanel registeredEventsPanel;
    private javax.swing.JPanel repositoriesPanel;
    private ancestris.modules.editors.genealogyeditor.panels.RepositoryCitationsListPanel repositoryCitationsListPanel;
    private javax.swing.JLabel sourceIDLabel;
    private javax.swing.JTextField sourceIDTextField;
    private javax.swing.JTabbedPane sourceInformationTabbedPane;
    private javax.swing.JPanel sourceTextPanel;
    private javax.swing.JScrollPane sourceTextScrollPane;
    private javax.swing.JTextArea sourceTextTextArea;
    private javax.swing.JLabel sourceTitleLabel;
    private javax.swing.JTextField sourceTitleTextField;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the source
     */
    public Source getSource() {
        return mSource;
    }

    /**
     * @param source the source to set
     */
    /*
     * SOURCE_RECORD:=
     */
    public void set(Source source) {
        this.mSource = source;

        /*
         * n @<XREF:SOUR>@ SOUR
         */
        sourceIDTextField.setText(mSource.getId());

        /*
         * +1 DATA
         * +2 EVEN <EVENTS_RECORDED>
         * +3 DATE <DATE_PERIOD>
         * +3 PLAC <SOURCE_JURISDICTION_PLACE>
         * +2 AGNC <RESPONSIBLE_AGENCY>
         * +2 <<NOTE_STRUCTURE>>
         */
        final Property sourceData = mSource.getProperty("DATA");
        if (sourceData != null) {
            Property[] sourceDataEvents = sourceData.getProperties("EVEN");
            eventTypePanel.setEventTypesList(sourceData, Arrays.asList(sourceDataEvents));
        } else {
            try {
                mSource.getGedcom().doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        eventTypePanel.setEventTypesList(mSource.addProperty("DATA", ""), null);
                    }
                }); // end of doUnitOfWork
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /*
         * +1 AUTH <SOURCE_ORIGINATOR>
         * +2 [CONC|CONT] <SOURCE_ORIGINATOR>
         */
        Property sourceAuthor = mSource.getProperty("AUTH");
        authorTextField.setText(sourceAuthor != null ? sourceAuthor.getValue() : "");

        /*
         * +1 TITL <SOURCE_DESCRIPTIVE_TITLE>
         * +2 [CONC|CONT] <SOURCE_DESCRIPTIVE_TITLE>
         */
        Property sourceTitle = mSource.getProperty("TITL");
        sourceTitleTextField.setText(sourceTitle != null ? sourceTitle.getValue() : "");

        /*
         * +1 ABBR <SOURCE_FILED_BY_ENTRY>
         */
        Property abbreviation = mSource.getProperty("ABBR");
        abbreviationTextField.setText(abbreviation != null ? abbreviation.getValue() : "");

        /*
         * +1 PUBL <SOURCE_PUBLICATION_FACTS>
         * +2 [CONC|CONT] <SOURCE_PUBLICATION_FACTS>
         */
        Property publicationFacts = mSource.getProperty("PUBL");
        publicationFactsTextArea.setText(publicationFacts != null ? publicationFacts.getValue() : "");

        /*
         * +1 TEXT <TEXT_FROM_SOURCE>
         * +2 [CONC|CONT] <TEXT_FROM_SOURCE>
         */
        Property sourceText = mSource.getProperty("TEXT");
        sourceTextTextArea.setText(sourceText != null ? sourceText.getValue() : "");

        /*
         * +1 <<SOURCE_REPOSITORY_CITATION>>
         */
        repositoryCitationsListPanel.set(mSource, mSource.getProperties(PropertyRepository.class));

        /*
         * +1 REFN <USER_REFERENCE_NUMBER>
         * Not used
         *
         * +2 TYPE <USER_REFERENCE_TYPE>
         * Not used
         *
         * +1 RIN <AUTOMATED_RECORD_ID>
         * Not used
         *
         * +1 <<CHANGE_DATE>>
         * Handle by gedcom doUnitOfWork
         * not displayed
         */

        /*
         * +1 <<NOTE_STRUCTURE>>
         */
        List<Note> notesList = new ArrayList<Note>();
        for (PropertyNote noteRef : mSource.getProperties(PropertyNote.class)) {
            notesList.add((Note) noteRef.getTargetEntity());
        }
        noteCitationsListPanel.set(mSource, Arrays.asList(mSource.getProperties("NOTE")));

        /*
         * +1 <<MULTIMEDIA_LINK>>
         */
        multimediaObjectCitationsListPanel.set(mSource, Arrays.asList(mSource.getProperties("OBJE")));

        List<Entity> entitiesList = new ArrayList<Entity>();
        for (PropertyXRef entityRef : mSource.getProperties(PropertyXRef.class)) {
            entitiesList.add(entityRef.getTargetEntity());
        }
        referencesListPanel.set(mSource, entitiesList);
    }

    public Source commit() {
        try {
            mSource.getGedcom().doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    Property sourceTitle = mSource.getProperty("TITL");
                    if (sourceTitle == null) {
                        mSource.addProperty("TITL", sourceTitleTextField.getText());
                    } else {
                        sourceTitle.setValue(sourceTitleTextField.getText());
                    }
                    Property publicationFacts = mSource.getProperty("PUBL");
                    if (publicationFacts == null) {
                        mSource.addProperty("PUBL", sourceTextTextArea.getText());
                    } else {
                        publicationFacts.setValue(sourceTextTextArea.getText());
                    }
                    Property sourceText = mSource.getProperty("TEXT");
                    if (sourceTitle == null) {
                        mSource.addProperty("TEXT", sourceTextTextArea.getText());
                    } else {
                        sourceText.setValue(sourceTextTextArea.getText());
                    }
                    Property abbreviation = mSource.getProperty("ABBR");
                    if (abbreviation == null) {
                        mSource.addProperty("ABBR", abbreviationTextField.getText());
                    } else {
                        abbreviation.setValue(abbreviationTextField.getText());
                    }
                    Property sourceAuthor = mSource.getProperty("AUTH");
                    if (sourceAuthor == null) {
                        mSource.addProperty("AUTH", authorTextField.getText());
                    } else {
                        sourceAuthor.setValue(authorTextField.getText());
                    }
                }
            }); // end of doUnitOfWork
            return mSource;
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
}
