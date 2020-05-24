package ancestris.modules.editors.genealogyeditor.editors;

import genj.gedcom.*;
import genj.view.ViewContext;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class NoteEditor extends EntityEditor {
    
    private Context context;
    private Note mNote;
    private boolean mNoteModified = false;

    /**
     * Creates new form NoteEditorPanel
     */
    public NoteEditor() {
        this(false);
    }
    
    public NoteEditor(boolean isNew) {
        super(isNew);
        initComponents();
        noteTextTextArea.getDocument().addDocumentListener(changes);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        noteIDPanel = new javax.swing.JPanel();
        noteIDLabel = new javax.swing.JLabel();
        noteIDTextField = new javax.swing.JTextField();
        noteInformationTabbedPane = new javax.swing.JTabbedPane();
        noteTextPanel = new javax.swing.JPanel();
        noteTextToolBar = new javax.swing.JToolBar();
        noteTextScrollPane = new javax.swing.JScrollPane();
        noteTextTextArea = new ancestris.swing.UndoTextArea();
        noteReferencesPanel = new javax.swing.JPanel();
        referencesTablePanel = new ancestris.modules.editors.genealogyeditor.panels.ReferencesTablePanel();
        changeDateLabel = new javax.swing.JLabel();
        changeDateLabeldate = new javax.swing.JLabel();

        noteIDLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("NoteEditor.noteIDLabel.text"), new Object[] {})); // NOI18N

        noteIDTextField.setEditable(false);
        noteIDTextField.setColumns(8);
        noteIDTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("NoteEditor.noteIDTextField.text"), new Object[] {})); // NOI18N

        javax.swing.GroupLayout noteIDPanelLayout = new javax.swing.GroupLayout(noteIDPanel);
        noteIDPanel.setLayout(noteIDPanelLayout);
        noteIDPanelLayout.setHorizontalGroup(
            noteIDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(noteIDPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(noteIDLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noteIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        noteIDPanelLayout.setVerticalGroup(
            noteIDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(noteIDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(noteIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(noteIDLabel))
        );

        noteTextToolBar.setFloatable(false);
        noteTextToolBar.setRollover(true);

        noteTextTextArea.setColumns(20);
        noteTextTextArea.setLineWrap(true);
        noteTextTextArea.setRows(5);
        noteTextTextArea.setWrapStyleWord(true);
        noteTextScrollPane.setViewportView(noteTextTextArea);

        javax.swing.GroupLayout noteTextPanelLayout = new javax.swing.GroupLayout(noteTextPanel);
        noteTextPanel.setLayout(noteTextPanelLayout);
        noteTextPanelLayout.setHorizontalGroup(
            noteTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(noteTextToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(noteTextScrollPane)
        );
        noteTextPanelLayout.setVerticalGroup(
            noteTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(noteTextPanelLayout.createSequentialGroup()
                .addComponent(noteTextToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noteTextScrollPane))
        );

        noteInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("NoteEditor.noteTextPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/note.png")), noteTextPanel); // NOI18N

        javax.swing.GroupLayout noteReferencesPanelLayout = new javax.swing.GroupLayout(noteReferencesPanel);
        noteReferencesPanel.setLayout(noteReferencesPanelLayout);
        noteReferencesPanelLayout.setHorizontalGroup(
            noteReferencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(referencesTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
        );
        noteReferencesPanelLayout.setVerticalGroup(
            noteReferencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(referencesTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
        );

        noteInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("NoteEditor.noteReferencesPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/association.png")), noteReferencesPanel); // NOI18N

        changeDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        changeDateLabel.setText(org.openide.util.NbBundle.getMessage(NoteEditor.class, "NoteEditor.changeDateLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(noteInformationTabbedPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(changeDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeDateLabeldate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(noteIDPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(noteIDPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noteInformationTabbedPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(changeDateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(changeDateLabeldate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel changeDateLabel;
    private javax.swing.JLabel changeDateLabeldate;
    private javax.swing.JLabel noteIDLabel;
    private javax.swing.JPanel noteIDPanel;
    private javax.swing.JTextField noteIDTextField;
    private javax.swing.JTabbedPane noteInformationTabbedPane;
    private javax.swing.JPanel noteReferencesPanel;
    private javax.swing.JPanel noteTextPanel;
    private javax.swing.JScrollPane noteTextScrollPane;
    private javax.swing.JTextArea noteTextTextArea;
    private javax.swing.JToolBar noteTextToolBar;
    private ancestris.modules.editors.genealogyeditor.panels.ReferencesTablePanel referencesTablePanel;
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

    /**
     * @param context the context to set
     *
     * @<XREF:NOTE>@ NOTE <SUBMITTER_TEXT>
     * +1 [CONC|CONT] <SUBMITTER_TEXT>
     * +1 REFN <USER_REFERENCE_NUMBER>
     * +2 TYPE <USER_REFERENCE_TYPE>
     * +1 RIN <AUTOMATED_RECORD_ID>
     * +1 <<SOURCE_CITATION>> +1 <<CHANGE_DATE>>
     */
    @Override
    protected void setContextImpl(Context context) {
        this.context = context;
        
        Entity entity = context.getEntity();
        if (entity != null && entity instanceof Note) {
            mNote = (Note) entity;
            
            setTitle(NbBundle.getMessage(NoteEditor.class, isNew() ? "NoteEditor.create.title" : "NoteEditor.edit.title", mNote));
            
            noteIDTextField.setText(mNote.getId());
            List<Entity> entitiesList = new ArrayList<>();
            for (PropertyXRef entityRef : mNote.getProperties(PropertyXRef.class)) {
                entitiesList.add(entityRef.getTargetEntity());
            }
            referencesTablePanel.set(mNote, entitiesList);
            
            noteTextTextArea.setText(mNote.getValue() != null ? mNote.getValue() : "");
            
            Property changeDate = mNote.getProperty("CHAN");
            if (changeDate != null) {
                changeDateLabeldate.setText(((PropertyChange) changeDate).getDisplayValue());
            }
            
            noteTextTextArea.getDocument().addDocumentListener(new DocumentListener() {
                
                @Override
                public void changedUpdate(DocumentEvent e) {
                    mNoteModified = true;
                }
                
                @Override
                public void removeUpdate(DocumentEvent e) {
                    mNoteModified = true;
                }
                
                @Override
                public void insertUpdate(DocumentEvent e) {
                    mNoteModified = true;
                }
            });
        }
    }
    
    @Override
    public void commit() {
        
        if (mNoteModified) {
            mNote.setValue(noteTextTextArea.getText());
        }
    }

    @Override
    public Entity getEditedEntity() {
        return mNote;
    }
}
