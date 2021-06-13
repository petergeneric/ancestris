/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.editors.resourceeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListDataListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import org.ancestris.trancestris.resources.ResourceFile;
import org.ancestris.trancestris.resources.ZipDirectory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.UndoRedo;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.ancestris.trancestris.editors.resourceeditor//ResourceEditor//EN", autostore = false)
public final class ResourceEditorTopComponent extends TopComponent implements LookupListener {

    /**
     * @return the resourceFileView
     */
    public javax.swing.JList<String> getResourceFileView() {
        return resourceFileView;
    }

    /**
     * @param resourceFileView the resourceFileView to set
     */
    public void setResourceFileView(javax.swing.JList<String> resourceFileView) {
        this.resourceFileView = resourceFileView;
    }

    /**
     * @return the resourceFile
     */
    public ResourceFile getResourceFile() {
        return resourceFile;
    }

    /**
     * @param resourceFile the resourceFile to set
     */
    public void setResourceFile(ResourceFile resourceFile) {
        this.resourceFile = resourceFile;
    }

    private class ResourceFileModel implements ListModel<String> {

        @Override
        public void addListDataListener(ListDataListener listdatalistener1) {
        }

        @Override
        public void removeListDataListener(ListDataListener listdatalistener1) {
        }

        @Override
        public String getElementAt(int i) {
            return getResourceFile() == null ? "" : getResourceFile().getLine(i);
        }

        @Override
        public int getSize() {
            return getResourceFile() == null ? 0 : getResourceFile().getLineCount();
        }
    }

    private class ResourceFileCellRenderer extends JTextArea implements ListCellRenderer<String> {

        // This is the only method defined by ListCellRenderer.
        // We just reconfigure the JLabel each time we're called.
        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list,
                String value, // value to display
                int index, // cell index
                boolean isSelected, // is the cell selected
                boolean cellHasFocus) // the list and the cell have the focus
        {
            setText(value);
            Color foreground;
            Color background;
            switch (getResourceFile().getLineState(index)) {
                // Translation is missing
                case 3:
                    foreground = ResourceFile.TR_MISSING_COL;
                    background = list.getBackground();
                    setToolTipText(null);
                    break;

                // Translation is to be updated
                case 2:
                    foreground = ResourceFile.TR_UPDATE_COL;
                    background = list.getBackground();
                    String tip = getResourceFile().getRefValue(index);
                    if (tip != null) {
                        if (tip.contains("<html>")) {
                            tip = tip.replace("<html>", "<...>").replace("</html>", "<...>");
                        }
                        setToolTipText(tip);
                    }
                    if (value.isEmpty()) {
                        setText(NbBundle.getMessage(getClass(), "ResourceEditorTopComponent.empty"));
                    }
                    break;

                // Translation is the same
                case 1:
                    foreground = ResourceFile.TR_SAME_COL;
                    background = list.getBackground();
                    setToolTipText(NbBundle.getMessage(getClass(), "ResourceEditorTopComponent.SameTranslation"));
                    break;

                // Translation appears ok
                case 0:
                default:
                    foreground = list.getForeground();
                    background = list.getBackground();
                    setToolTipText(null);
                    break;
            }
            if (isSelected) {
                // for Arvernes specific pb
                setBackground(Color.LIGHT_GRAY);
                setForeground(foreground);
            } else {
                setBackground(background);
                setForeground(foreground);
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
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
            getResourceFile().setLineTranslation(index, oldValue);
            getResourceFileView().setSelectedIndex(index);
            getResourceFileView().ensureIndexIsVisible(index);
            textAreaTranslation.setText(oldValue);
            undoRedoEvent = false;
        }

        @Override
        public void redo() throws CannotUndoException {
            undoRedoEvent = true;
            getResourceFile().setLineTranslation(index, newValue);
            getResourceFileView().setSelectedIndex(index);
            getResourceFileView().ensureIndexIsVisible(index);
            textAreaTranslation.setText(newValue);
            undoRedoEvent = false;
        }
    }
    private static ResourceEditorTopComponent instance;
    /**
     * path to the icon used by the component and its open action
     */
    static final String ICON_PATH = "org/ancestris/trancestris/editors/resourceeditor/Advanced.png";
    private static final String PREFERRED_ID = "ResourceEditorTopComponent";
    private Lookup.Result<? extends ZipDirectory> result = null;
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
        textAreaTranslation.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) { 
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    confirmTranslation();
                }
                if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    nextButtonDone();
                }
                if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_LEFT) {
                    previousButtonDone();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });        
    }

    public void search(boolean next, String expression, boolean fromLocale, boolean caseSensitive) {
        int index = -1;
        if (resourceFile != null) {
            if (next) {
                index = resourceFile.searchNext(getResourceFileView().getSelectedIndex(), expression, fromLocale, caseSensitive);

            } else {
                index = resourceFile.searchPrevious(getResourceFileView().getSelectedIndex(), expression, fromLocale, caseSensitive);
            }
            if (index > -1) {
                resourceFileView.setSelectedIndex(index);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        localeButtonGroup = new javax.swing.ButtonGroup();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        scrollPaneComments = new javax.swing.JScrollPane();
        textAreaComments = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        scrollPaneResourceView = new javax.swing.JScrollPane(resourceFileView);
        resourceFileView = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        panelTranslation = new javax.swing.JPanel();
        scrollPaneTranslation = new javax.swing.JScrollPane();
        textAreaTranslation = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        previousButton = new javax.swing.JButton();
        buttonConfirmTranslation = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.BorderLayout());
        jPanel5.add(new org.ancestris.trancestris.editors.actions.EditorSearchPanel(this), java.awt.BorderLayout.NORTH);
        add(jPanel5, java.awt.BorderLayout.NORTH);

        jPanel6.setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.jPanel1.border.title"))); // NOI18N
        jPanel1.setToolTipText(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ToolTip-Comment-Window")); // NOI18N
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        textAreaComments.setEditable(false);
        textAreaComments.setBackground(new java.awt.Color(184, 207, 229));
        textAreaComments.setColumns(20);
        textAreaComments.setRows(5);
        scrollPaneComments.setViewportView(textAreaComments);

        jPanel1.add(scrollPaneComments);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.jLabel3.text")); // NOI18N
        jLabel3.setToolTipText(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.jLabel3.toolTipText")); // NOI18N
        jPanel1.add(jLabel3);

        jPanel6.add(jPanel1, java.awt.BorderLayout.NORTH);

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.75);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.jPanel2.border.title"))); // NOI18N
        jPanel2.setToolTipText(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ToolTip-FileToTranslate-Window")); // NOI18N
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        resourceFileView.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.jLabel2.text")); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.jLabel2.toolTipText")); // NOI18N
        jPanel2.add(jLabel2);

        jSplitPane1.setTopComponent(jPanel2);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.jPanel3.border.title"))); // NOI18N
        jPanel3.setToolTipText(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ToolTip-Translation-Window")); // NOI18N
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        panelTranslation.setLayout(new java.awt.BorderLayout());

        textAreaTranslation.setEditable(false);
        textAreaTranslation.setColumns(20);
        textAreaTranslation.setRows(5);
        textAreaTranslation.setDragEnabled(true);
        textAreaTranslation.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textAreaTranslationKeyTyped(evt);
            }
        });
        scrollPaneTranslation.setViewportView(textAreaTranslation);

        panelTranslation.add(scrollPaneTranslation, java.awt.BorderLayout.CENTER);

        jPanel3.add(panelTranslation);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.jLabel1.toolTipText")); // NOI18N
        jPanel3.add(jLabel1);

        jSplitPane1.setBottomComponent(jPanel3);

        jPanel6.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        add(jPanel6, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.GridLayout(1, 0));

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

        add(jPanel4, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void buttonConfirmTranslationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConfirmTranslationActionPerformed
        confirmTranslation();

    }//GEN-LAST:event_buttonConfirmTranslationActionPerformed


    private void resourceFileViewValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_resourceFileViewValueChanged
        if (evt.getValueIsAdjusting()) {
            return;
        }
        String translation = "";
        String comment = "";

        int i = getResourceFileView().getSelectedIndex();
        logger.log(Level.INFO, "Selected index is {0}", i);

        getResourceFileView().ensureIndexIsVisible(i);

        if (i >= 0) {
            translation = getResourceFile().getLineTranslation(i);
            comment = getResourceFile().getLineComment(i);
        }
        if (Pattern.compile("NOI18N$").matcher(comment).find() == true) {
            logger.log(Level.INFO, "index {0} shall not be translated", i);
            textAreaComments.setText(NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.NoTranslation"));
            textAreaComments.setCaretPosition(0);
            textAreaTranslation.setEditable(false);
            buttonConfirmTranslation.setEnabled(false);
        } else {
            comment = Pattern.compile("(#\\s)|(#\t)").matcher(comment).replaceAll("");
            textAreaComments.setText(comment);
            textAreaComments.setCaretPosition(0);
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
        previousButtonDone();
    }//GEN-LAST:event_previousButtonActionPerformed


    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        nextButtonDone();
    }//GEN-LAST:event_nextButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonConfirmTranslation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.ButtonGroup localeButtonGroup;
    private javax.swing.JButton nextButton;
    private javax.swing.JPanel panelTranslation;
    private javax.swing.JButton previousButton;
    private javax.swing.JList<String> resourceFileView;
    private javax.swing.JScrollPane scrollPaneComments;
    private javax.swing.JScrollPane scrollPaneResourceView;
    private javax.swing.JScrollPane scrollPaneTranslation;
    private javax.swing.JTextArea textAreaComments;
    private javax.swing.JTextArea textAreaTranslation;
    // End of variables declaration//GEN-END:variables

    private void confirmTranslation() {
        int i = getResourceFileView().getSelectedIndex();

        logger.log(Level.INFO, "Selected index is {0}", i);

        // Store update
        if (i >= 0) {
            String oldValue = getResourceFile().getLineTranslation(i);
            String newValue = textAreaTranslation.getText();
            logger.log(Level.INFO, "Save translation for index {0}", i);
            getResourceFile().setLineTranslation(i, newValue);
            MyAbstractUndoableEdit ue = new MyAbstractUndoableEdit(i, oldValue, newValue);
            if (!undoRedoEvent) {
                manager.undoableEditHappened(new UndoableEditEvent(this, ue));
            }
        }

        // Search for the first next non translated line
        while (i + 1 < getResourceFileView().getModel().getSize()) {
            if (getResourceFile().getLineState(++i) > 0) {
                logger.log(Level.INFO, "New selected index is {0}", i);
                getResourceFileView().setSelectedIndex(i);
                break;
            }
        }
    }

    private void nextButtonDone() {
        int i = getResourceFileView().getSelectedIndex();
        logger.log(Level.INFO, "Selected index is {0}", i);
        // Search for the first next non translated line
        while (i + 1 < getResourceFileView().getModel().getSize()) {
            if (getResourceFile().getLineState(++i) > 0) {
                logger.log(Level.INFO, "New selected index is {0}", i);
                getResourceFileView().setSelectedIndex(i);
                break;
            } else {
                logger.log(Level.INFO, "index {0} is translated", i);
            }

        }
    }

    private void previousButtonDone() {
        int i = getResourceFileView().getSelectedIndex();
        logger.log(Level.INFO, "Selected index is {0}", i);
        // Search for the first next non translated line
        while (i - 1 >= 0) {
            if (getResourceFile().getLineState(--i) > 0) {
                logger.log(Level.INFO, "New selected index is {0}", i);
                getResourceFileView().setSelectedIndex(i);
                break;
            } else {
                logger.log(Level.INFO, "index {0} is translated", i);
            }
        }
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized ResourceEditorTopComponent getDefault() {
        if (instance == null) {
            instance = new ResourceEditorTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ResourceEditorTopComponent instance. Never call
     * {@link #getDefault} directly!
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
    @SuppressWarnings("unchecked")
    public void resultChanged(LookupEvent le) {
        Object o = le.getSource();
        Lookup.Result<ZipDirectory> r = (Lookup.Result<ZipDirectory>) o;
        Collection<? extends ZipDirectory> c = r.allInstances();
        if (!c.isEmpty()) {
            for (Iterator<? extends ZipDirectory> i = c.iterator(); i.hasNext();) {
                ZipDirectory zipDirectory = i.next();
                setResourceFile(zipDirectory.getResourceFile());
                getResourceFileView().updateUI();
                manager.discardAllEdits();
                if (getResourceFile() != null) {
                    logger.log(Level.INFO, "Editing file in directory {0}", zipDirectory.getName());

                    getResourceFileView().setSelectedIndex(0);
                    getResourceFileView().ensureIndexIsVisible(0);

                    String comment = getResourceFile().getLineComment(0);
                    if (Pattern.compile("NOI18N$").matcher(comment).find() == true) {
                        logger.log(Level.INFO, "index {0} shall not be translated", i);
                        textAreaComments.setText(NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.NoTranslation"));
                        textAreaComments.setCaretPosition(0);
                        textAreaTranslation.setEditable(false);
                        buttonConfirmTranslation.setEnabled(false);
                    } else {
                        comment = Pattern.compile("(#\\s)|(#\t)").matcher(comment).replaceAll("");
                        textAreaComments.setText(comment);
                        textAreaComments.setCaretPosition(0);
                        textAreaTranslation.setEditable(true);
                        buttonConfirmTranslation.setEnabled(false);
                    }
                    textAreaTranslation.setText(getResourceFile().getLineTranslation(0));
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
        getResourceFileView().setFont(font);
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
