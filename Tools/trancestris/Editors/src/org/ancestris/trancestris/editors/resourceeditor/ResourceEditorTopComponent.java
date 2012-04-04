/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.editors.resourceeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.ancestris.trancestris.resources.ResourceFile;
import org.ancestris.trancestris.resources.ZipDirectory;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.ancestris.trancestris.editors.resourceeditor//ResourceEditor//EN", autostore = false)
public final class ResourceEditorTopComponent extends TopComponent implements LookupListener {

    private class ResourceFileModel implements ListModel {

        @Override
        public void addListDataListener(ListDataListener listdatalistener1) {
        }

        @Override
        public void removeListDataListener(ListDataListener listdatalistener1) {
        }

        @Override
        public Object getElementAt(int i) {
            return resourceFile == null ? "" : resourceFile.getLine(i);
        }

        @Override
        public int getSize() {
            return resourceFile == null ? 0 : resourceFile.getLineCount();
        }
    }

    private class ResourceFileCellRenderer extends JTextArea implements ListCellRenderer {

        // This is the only method defined by ListCellRenderer.
        // We just reconfigure the JLabel each time we're called.
        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value, // value to display
                int index, // cell index
                boolean isSelected, // is the cell selected
                boolean cellHasFocus) // the list and the cell have the focus
        {
            String s = value.toString();
            setText(s);
            Color color;
            switch (resourceFile.getLineState(index)) {
                // The line is the same
                case -1:
                    color = Color.BLUE;
                    break;

                // the line is not translated
                case 0:
                    color = Color.RED;
                    break;

                // the line is translated or non modifiable
                case 1:
                default:
                    color = list.getForeground();
                    break;
            }
            if (isSelected) {
                // for Arvernes specific pb
                setBackground(Color.DARK_GRAY);
                setForeground(Color.YELLOW);
//                    setBackground(list.getSelectionBackground());
//                    setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(color);
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    class MyUndoRedo implements UndoRedo {

        @Override
        public boolean canUndo() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean canRedo() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void undo() throws CannotUndoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void redo() throws CannotRedoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addChangeListener(ChangeListener cl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removeChangeListener(ChangeListener cl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getUndoPresentationName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getRedoPresentationName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    class MyAbstractUndoableEdit extends AbstractUndoableEdit {

        private final int index;
        private final String oldValue;
        private final String newValue;

        private MyAbstractUndoableEdit(int index, String oldValue, String newValue) {
            this.index = index;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        @Override
        public boolean canRedo() {
            return true;
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() throws CannotUndoException {
            undoRedoEvent = true;
            resourceFile.setLineTranslation(index, oldValue);
            resourceFileView.setSelectedIndex(index);
            resourceFileView.ensureIndexIsVisible(index);
            undoRedoEvent = false;
        }

        @Override
        public void redo() throws CannotUndoException {
            undoRedoEvent = true;
            resourceFile.setLineTranslation(index, newValue);
            resourceFileView.setSelectedIndex(index);
            resourceFileView.ensureIndexIsVisible(index);
            undoRedoEvent = false;
        }
    }
    private static ResourceEditorTopComponent instance;
    /**
     * path to the icon used by the component and its open action
     */
    static final String ICON_PATH = "org/ancestris/trancestris/editors/resourceeditor/Advanced.png";
    private static final String PREFERRED_ID = "ResourceEditorTopComponent";
    private Lookup.Result result = null;
    private ResourceFile resourceFile = null;
    private static final Logger logger = Logger.getLogger(ResourceEditorTopComponent.class.getName());
    private UndoRedo.Manager manager = new UndoRedo.Manager();
    private boolean undoRedoEvent;

    public ResourceEditorTopComponent() {
        initComponents();
        ToolTipManager.sharedInstance().setDismissDelay(60 * 1000);
        String fontName = NbPreferences.forModule(ResourceEditorTopComponent.class).get("Font.Name", "Dialog");
        int fontStyle = Integer.valueOf(NbPreferences.forModule(ResourceEditorTopComponent.class).get("Font.Style", "0"));
        int fontSize = Integer.valueOf(NbPreferences.forModule(ResourceEditorTopComponent.class).get("Font.Size", "12"));
        setFont(new Font(fontName, fontStyle, fontSize));
        setName(NbBundle.getMessage(ResourceEditorTopComponent.class, "CTL_ResourceEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(ResourceEditorTopComponent.class, "HINT_ResourceEditorTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        scrollPaneComments = new javax.swing.JScrollPane();
        textAreaComments = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        scrollPaneResourceView = new javax.swing.JScrollPane(resourceFileView);
        resourceFileView = new javax.swing.JList();
        jPanel3 = new javax.swing.JPanel();
        panelTranslation = new javax.swing.JPanel();
        scrollPaneTranslation = new javax.swing.JScrollPane();
        textAreaTranslation = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        previousButton = new javax.swing.JButton();
        buttonConfirmTranslation = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.jPanel1.border.title"))); // NOI18N
        jPanel1.setToolTipText(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ToolTip-Comment-Window")); // NOI18N
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        textAreaComments.setBackground(new java.awt.Color(184, 207, 229));
        textAreaComments.setColumns(20);
        textAreaComments.setEditable(false);
        textAreaComments.setRows(5);
        scrollPaneComments.setViewportView(textAreaComments);

        jPanel1.add(scrollPaneComments);

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.jPanel2.border.title"))); // NOI18N
        jPanel2.setToolTipText(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ToolTip-FileToTranslate-Window")); // NOI18N
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        resourceFileView.setFont(new java.awt.Font("Dialog", 0, 12));
        resourceFileView.setModel(new ResourceFileModel ());
        resourceFileView.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        resourceFileView.setCellRenderer(new ResourceFileCellRenderer());
        resourceFileView.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                resourceFileViewValueChanged(evt);
            }
        });
        scrollPaneResourceView.setViewportView(resourceFileView);

        jPanel2.add(scrollPaneResourceView);

        add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.jPanel3.border.title"))); // NOI18N
        jPanel3.setToolTipText(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ToolTip-Translation-Window")); // NOI18N
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        panelTranslation.setLayout(new java.awt.BorderLayout());

        textAreaTranslation.setColumns(20);
        textAreaTranslation.setEditable(false);
        textAreaTranslation.setRows(5);
        textAreaTranslation.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textAreaTranslationKeyTyped(evt);
            }
        });
        scrollPaneTranslation.setViewportView(textAreaTranslation);

        panelTranslation.add(scrollPaneTranslation, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.GridLayout());

        org.openide.awt.Mnemonics.setLocalizedText(previousButton, org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.previousButton.text")); // NOI18N
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });
        jPanel4.add(previousButton);

        org.openide.awt.Mnemonics.setLocalizedText(buttonConfirmTranslation, org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.buttonConfirmTranslation.text")); // NOI18N
        buttonConfirmTranslation.setEnabled(false);
        buttonConfirmTranslation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConfirmTranslationActionPerformed(evt);
            }
        });
        jPanel4.add(buttonConfirmTranslation);

        org.openide.awt.Mnemonics.setLocalizedText(nextButton, org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.nextButton.text")); // NOI18N
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        jPanel4.add(nextButton);

        panelTranslation.add(jPanel4, java.awt.BorderLayout.SOUTH);

        jPanel3.add(panelTranslation);

        add(jPanel3, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void buttonConfirmTranslationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConfirmTranslationActionPerformed
        int i = resourceFileView.getSelectedIndex();

        logger.log(Level.INFO, "Selected index is {0}", i);

        if (i >= 0) {
            String oldValue = resourceFile.getLineTranslation(i);
            String newValue = textAreaTranslation.getText();
            logger.log(Level.INFO, "Save translation for index {0}", i);
            resourceFile.setLineTranslation(i, newValue);
            manager.addEdit(new MyAbstractUndoableEdit(i, oldValue, newValue));
        }

        // Search for the first next non translated line
        while (i + 1 < resourceFileView.getModel().getSize()) {
            if (resourceFile.getLineState(++i) == 0) {
                logger.log(Level.INFO, "New selected index is {0}", i);
                resourceFileView.setSelectedIndex(i);
                resourceFileView.ensureIndexIsVisible(i);
                break;
            }
        }
    }//GEN-LAST:event_buttonConfirmTranslationActionPerformed

    private void resourceFileViewValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_resourceFileViewValueChanged
        if (evt.getValueIsAdjusting()) {
            return;
        }
        String translation = "";
        String comment = "";

        int i = resourceFileView.getSelectedIndex();
        logger.log(Level.INFO, "Selected index is {0}", i);

        if (i >= 0) {
            translation = resourceFile.getLineTranslation(i);
            comment = resourceFile.getLineComment(i);
        }
        textAreaComments.setText(comment);
        textAreaComments.setCaretPosition(0);
        if (comment.contains("NOI18N") == true) {
            textAreaTranslation.setEditable(false);
            buttonConfirmTranslation.setEnabled(false);
        } else {
            textAreaTranslation.setEditable(true);
            buttonConfirmTranslation.setEnabled(false);
        }
        textAreaTranslation.setText(translation);
        textAreaTranslation.setCaretPosition(0);
    }//GEN-LAST:event_resourceFileViewValueChanged

    private void textAreaTranslationKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textAreaTranslationKeyTyped
        buttonConfirmTranslation.setEnabled(true);
    }//GEN-LAST:event_textAreaTranslationKeyTyped

    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        int i = resourceFileView.getSelectedIndex();
        logger.log(Level.INFO, "Selected index is {0}", i);
        // Search for the first next non translated line
        while (i - 1 > 0) {
            if (resourceFile.getLineState(--i) == 0) {
                logger.log(Level.INFO, "New selected index is {0}", i);
                resourceFileView.setSelectedIndex(i);
                resourceFileView.ensureIndexIsVisible(i);
                break;
            }
        }
    }//GEN-LAST:event_previousButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        int i = resourceFileView.getSelectedIndex();
        logger.log(Level.INFO, "Selected index is {0}", i);
        // Search for the first next non translated line
        while (i + 1 < resourceFileView.getModel().getSize()) {
            if (resourceFile.getLineState(++i) == 0) {
                logger.log(Level.INFO, "New selected index is {0}", i);
                resourceFileView.setSelectedIndex(i);
                resourceFileView.ensureIndexIsVisible(i);
                break;
            }
        }
    }//GEN-LAST:event_nextButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonConfirmTranslation;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton nextButton;
    private javax.swing.JPanel panelTranslation;
    private javax.swing.JButton previousButton;
    private javax.swing.JList resourceFileView;
    private javax.swing.JScrollPane scrollPaneComments;
    private javax.swing.JScrollPane scrollPaneResourceView;
    private javax.swing.JScrollPane scrollPaneTranslation;
    private javax.swing.JTextArea textAreaComments;
    private javax.swing.JTextArea textAreaTranslation;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ResourceEditorTopComponent getDefault() {
        if (instance == null) {
            instance = new ResourceEditorTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ResourceEditorTopComponent instance. Never call {@link #getDefault}
     * directly!
     */
    public static synchronized ResourceEditorTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ResourceEditorTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ResourceEditorTopComponent) {
            return (ResourceEditorTopComponent) win;
        }
        Logger.getLogger(ResourceEditorTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        Lookup.Template<ZipDirectory> tpl = new Lookup.Template<ZipDirectory>(ZipDirectory.class);
        result = Utilities.actionsGlobalContext().lookup(tpl);
        result.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public void resultChanged(LookupEvent le) {
        Lookup.Result r = (Lookup.Result) le.getSource();
        Collection c = r.allInstances();
        if (!c.isEmpty()) {
            for (Iterator i = c.iterator(); i.hasNext();) {
                ZipDirectory zipDirectory = (ZipDirectory) i.next();
                resourceFile = zipDirectory.getResourceFile();
                resourceFileView.updateUI();
                manager.discardAllEdits();
                if (resourceFile != null) {
                    logger.log(Level.INFO, "Editing file in directory {0}", zipDirectory.getName());

                    resourceFileView.setSelectedIndex(0);
                    resourceFileView.ensureIndexIsVisible(0);

                    textAreaComments.setText(resourceFile.getLineComment(0));
                    textAreaComments.setCaretPosition(0);
                    if (resourceFile.getLineComment(0).contains("NOI18N") == true) {
                        textAreaTranslation.setEditable(false);
                        buttonConfirmTranslation.setEnabled(false);
                    } else {
                        textAreaTranslation.setEditable(true);
                        buttonConfirmTranslation.setEnabled(false);
                    }
                    textAreaTranslation.setText(resourceFile.getLineTranslation(0));
                    textAreaTranslation.setCaretPosition(0);
                } else {
                    logger.log(Level.INFO, "No file under edition");

                    textAreaComments.setText("");
                    textAreaComments.setCaretPosition(0);
                    textAreaTranslation.setText("");
                    textAreaTranslation.setEditable(false);
                    textAreaTranslation.setCaretPosition(0);
                    buttonConfirmTranslation.setEnabled(false);
                }
            }
        }
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        resourceFileView.setFont(font);
        textAreaTranslation.setFont(font);
        textAreaComments.setFont(font);
        NbPreferences.forModule(ResourceEditorTopComponent.class).put("Font.Name", font.getName());
        NbPreferences.forModule(ResourceEditorTopComponent.class).put("Font.Style", String.valueOf(font.getStyle()));
        NbPreferences.forModule(ResourceEditorTopComponent.class).put("Font.Size", String.valueOf(font.getSize()));
    }

    @Override
    public UndoRedo getUndoRedo() {
        return manager;
    }
}
