package ancestris.modules.editors.genealogyeditor.editors;

import ancestris.util.swing.FileChooserBuilder;
import genj.gedcom.*;
import genj.util.Registry;
import genj.view.ViewContext;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class MultiMediaObjectEditor extends EntityEditor {

    private Context context;
    private Property mRoot;
    private Property mMultiMediaObject;
    private File mFile = null;

    /**
     * Creates new form MultiMediaObjectEditor
     */
    public MultiMediaObjectEditor() {
        this(false);
    }

    public MultiMediaObjectEditor(boolean isNew) {
        super(isNew);
        initComponents();
        multiMediaObjectTitleTextField.getDocument().addDocumentListener(changes);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        multiMediaObjectIDLabel = new javax.swing.JLabel();
        multiMediaObjectIDTextField = new javax.swing.JTextField();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        multiMediaObjectTabbedPane = new javax.swing.JTabbedPane();
        imagePanel = new javax.swing.JPanel();
        imageBean = new ancestris.modules.editors.genealogyeditor.beans.ImageBean();
        notesPanel = new javax.swing.JPanel();
        noteCitationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.NoteCitationsTablePanel();
        multiMediaObjectReferencesPanel = new javax.swing.JPanel();
        referencesTablePanel = new ancestris.modules.editors.genealogyeditor.panels.ReferencesTablePanel();
        multiMediaObjectTitleLabel = new javax.swing.JLabel();
        multiMediaObjectTitleTextField = new javax.swing.JTextField();
        changeDateLabel = new javax.swing.JLabel();
        changeDateLabeldate = new javax.swing.JLabel();

        multiMediaObjectIDLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("MultiMediaObjectEditor.multiMediaObjectIDLabel.text"), new Object[] {})); // NOI18N

        multiMediaObjectIDTextField.setEditable(false);
        multiMediaObjectIDTextField.setColumns(8);
        multiMediaObjectIDTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("MultiMediaObjectEditor.multiMediaObjectIDTextField.text"), new Object[] {})); // NOI18N

        imagePanel.setLayout(new java.awt.BorderLayout());

        imageBean.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imageBeanMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout imageBeanLayout = new javax.swing.GroupLayout(imageBean);
        imageBean.setLayout(imageBeanLayout);
        imageBeanLayout.setHorizontalGroup(
            imageBeanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        imageBeanLayout.setVerticalGroup(
            imageBeanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        imagePanel.add(imageBean, java.awt.BorderLayout.CENTER);

        multiMediaObjectTabbedPane.addTab(org.openide.util.NbBundle.getMessage(MultiMediaObjectEditor.class, "MultiMediaObjectEditor.imagePanel.TabConstraints.tabTitle"), imagePanel); // NOI18N

        noteCitationsTablePanel.setMinimumSize(null);
        noteCitationsTablePanel.setPreferredSize(null);

        javax.swing.GroupLayout notesPanelLayout = new javax.swing.GroupLayout(notesPanel);
        notesPanel.setLayout(notesPanelLayout);
        notesPanelLayout.setHorizontalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        notesPanelLayout.setVerticalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(notesPanelLayout.createSequentialGroup()
                .addComponent(noteCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        multiMediaObjectTabbedPane.addTab(org.openide.util.NbBundle.getMessage(MultiMediaObjectEditor.class, "MultiMediaObjectEditor.notesPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Note.png")), notesPanel); // NOI18N

        referencesTablePanel.setPreferredSize(null);

        javax.swing.GroupLayout multiMediaObjectReferencesPanelLayout = new javax.swing.GroupLayout(multiMediaObjectReferencesPanel);
        multiMediaObjectReferencesPanel.setLayout(multiMediaObjectReferencesPanelLayout);
        multiMediaObjectReferencesPanelLayout.setHorizontalGroup(
            multiMediaObjectReferencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(referencesTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        multiMediaObjectReferencesPanelLayout.setVerticalGroup(
            multiMediaObjectReferencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(referencesTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        multiMediaObjectTabbedPane.addTab(org.openide.util.NbBundle.getMessage(MultiMediaObjectEditor.class, "MultiMediaObjectEditor.multiMediaObjectReferencesPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/association.png")), multiMediaObjectReferencesPanel); // NOI18N

        multiMediaObjectTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        multiMediaObjectTitleLabel.setText(org.openide.util.NbBundle.getMessage(MultiMediaObjectEditor.class, "MultiMediaObjectEditor.multiMediaObjectTitleLabel.text")); // NOI18N

        multiMediaObjectTitleTextField.setMinimumSize(new java.awt.Dimension(303, 19));
        multiMediaObjectTitleTextField.setPreferredSize(new java.awt.Dimension(303, 19));

        changeDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        changeDateLabel.setText(org.openide.util.NbBundle.getMessage(MultiMediaObjectEditor.class, "MultiMediaObjectEditor.changeDateLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(multiMediaObjectTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(multiMediaObjectTitleTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(10, 10, 10)
                        .addComponent(multiMediaObjectIDLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(multiMediaObjectIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(changeDateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeDateLabeldate, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(multiMediaObjectTabbedPane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(multiMediaObjectIDLabel)
                        .addComponent(multiMediaObjectIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(multiMediaObjectTitleLabel)
                        .addComponent(multiMediaObjectTitleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(filler1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(multiMediaObjectTabbedPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(changeDateLabel)
                    .addComponent(changeDateLabeldate))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void imageBeanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageBeanMouseClicked

        File file = new FileChooserBuilder(MultiMediaObjectEditor.class)
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(getClass(), "TITL_ChooseImage"))
                .setApproveText(NbBundle.getMessage(getClass(), "OK_Button"))
                .setDefaultExtension(FileChooserBuilder.getTextFilter().getExtensions()[0])
                .setFileFilter(FileChooserBuilder.getImageFilter())
                .setAcceptAllFileFilterUsed(true)
                .setFileHiding(true)
                .setDefaultWorkingDirectory(new File(Registry.get(MultiMediaObjectEditor.class).get("rootPath", ".")))
                .showOpenDialog();

        if (file == null) {
            return;
        }

        mFile = file;
        imageBean.setImage(mFile, PropertySex.UNKNOWN);
        changes.fireChangeEvent();
        
    }//GEN-LAST:event_imageBeanMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel changeDateLabel;
    private javax.swing.JLabel changeDateLabeldate;
    private javax.swing.Box.Filler filler1;
    private ancestris.modules.editors.genealogyeditor.beans.ImageBean imageBean;
    private javax.swing.JPanel imagePanel;
    private javax.swing.JLabel multiMediaObjectIDLabel;
    private javax.swing.JTextField multiMediaObjectIDTextField;
    private javax.swing.JPanel multiMediaObjectReferencesPanel;
    private javax.swing.JTabbedPane multiMediaObjectTabbedPane;
    private javax.swing.JLabel multiMediaObjectTitleLabel;
    private javax.swing.JTextField multiMediaObjectTitleTextField;
    private ancestris.modules.editors.genealogyeditor.panels.NoteCitationsTablePanel noteCitationsTablePanel;
    private javax.swing.JPanel notesPanel;
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

    @Override
    protected void setContextImpl(Context context) {
        Property property;

        this.context = context;
        property = context.getEntity();
        if (property == null || !(property instanceof Media)) {
            property = context.getProperty();
        }

        if (property != null) {
            if (property instanceof PropertyMedia) {
                mMultiMediaObject = ((PropertyMedia) property).getTargetEntity();
            } else {
                mMultiMediaObject = property;
            }

            setTitle(NbBundle.getMessage(MultiMediaObjectEditor.class, isNew() ? "MultiMediaObjectEditor.create.title" : "MultiMediaObjectEditor.edit.title", mMultiMediaObject));

            mRoot = mMultiMediaObject.getParent() == null ? mMultiMediaObject : mMultiMediaObject.getParent();
            if (mMultiMediaObject instanceof Media) {
                multiMediaObjectIDTextField.setText(((Media) mMultiMediaObject).getId());
                multiMediaObjectTitleTextField.setText(((Media) mMultiMediaObject).getTitle());

                changeDateLabel.setVisible(true);
                changeDateLabeldate.setVisible(true);
                Property changeDate = mMultiMediaObject.getProperty("CHAN");
                if (changeDate != null) {
                    changeDateLabeldate.setText(((PropertyChange) changeDate).getDisplayValue());
                }

                List<Entity> entitiesList = new ArrayList<Entity>();
                for (PropertyXRef entityRef : mMultiMediaObject.getProperties(PropertyXRef.class)) {
                    entitiesList.add(entityRef.getTargetEntity());
                }

                referencesTablePanel.set((Media) mMultiMediaObject, entitiesList);
                int indexOfTab = multiMediaObjectTabbedPane.indexOfTab(NbBundle.getMessage(MultiMediaObjectEditor.class, "MultiMediaObjectEditor.multiMediaObjectReferencesPanel.TabConstraints.tabTitle"));
                if (indexOfTab == -1) {
                    multiMediaObjectTabbedPane.addTab(org.openide.util.NbBundle.getMessage(MultiMediaObjectEditor.class, "MultiMediaObjectEditor.multiMediaObjectReferencesPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/association.png")), multiMediaObjectReferencesPanel); // NOI18N
                }

            } else {
                Property propertyTitle = mMultiMediaObject.getProperty("TITL");
                multiMediaObjectTitleTextField.setText(propertyTitle != null ? propertyTitle.getValue() : "");
                multiMediaObjectIDTextField.setVisible(false);
                multiMediaObjectIDLabel.setVisible(false);
                changeDateLabel.setVisible(false);
                changeDateLabeldate.setVisible(false);
                int indexOfTab = multiMediaObjectTabbedPane.indexOfTab(NbBundle.getMessage(MultiMediaObjectEditor.class, "MultiMediaObjectEditor.multiMediaObjectReferencesPanel.TabConstraints.tabTitle"));
                if (indexOfTab != -1) {
                    multiMediaObjectTabbedPane.removeTabAt(multiMediaObjectTabbedPane.indexOfTab(NbBundle.getMessage(MultiMediaObjectEditor.class, "MultiMediaObjectEditor.multiMediaObjectReferencesPanel.TabConstraints.tabTitle")));
                }
            }

            Property multimediaFile = mMultiMediaObject.getProperty("FILE", true);
            if (multimediaFile != null && multimediaFile instanceof PropertyFile) {
                imageBean.setImage(((PropertyFile) multimediaFile).getFile(), PropertySex.UNKNOWN);
            } else {
                PropertyBlob propertyBlob = (PropertyBlob) mMultiMediaObject.getProperty("BLOB", true);
                imageBean.setImage(propertyBlob != null ? propertyBlob.getBlobData() : (byte[]) null, PropertySex.UNKNOWN);
            }

            /*
             * +1 <<NOTE_STRUCTURE>>
             */
            noteCitationsTablePanel.set(mMultiMediaObject, Arrays.asList(mMultiMediaObject.getProperties("NOTE")));
        }
    }

    @Override
    public void commit() {
        if (changes.hasChanged()) {
            if (mMultiMediaObject instanceof Media) {
                if (mFile != null) {
                    ((Media) mMultiMediaObject).addFile(mFile);
                }
                ((Media) mMultiMediaObject).setTitle(multiMediaObjectTitleTextField.getText().isEmpty() ? ((mFile != null) ? mFile.getName() : "") : multiMediaObjectTitleTextField.getText());
            } else {
                if (mFile != null) {
                    mMultiMediaObject.addFile(mFile);
                }
                Property propertyTitle = mMultiMediaObject.getProperty("TITL");
                if (propertyTitle == null) {
                    mMultiMediaObject.addProperty("TITL", multiMediaObjectTitleTextField.getText().isEmpty() ? ((mFile != null) ? mFile.getName() : "") : multiMediaObjectTitleTextField.getText());
                } else {
                    propertyTitle.setValue(multiMediaObjectTitleTextField.getText().isEmpty() ? ((mFile != null) ? mFile.getName() : "") : multiMediaObjectTitleTextField.getText());
                }
            }
        }
    }
}
