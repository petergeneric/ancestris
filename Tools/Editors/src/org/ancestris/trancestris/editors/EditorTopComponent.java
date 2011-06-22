/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.ancestris.trancestris.resources.ResourceFile;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.ancestris.trancestris.editors//Editor//EN", autostore = false)
public final class EditorTopComponent extends TopComponent {

    private ResourceFile resourceFile;
    private Locale translatedLocale;

    private class Listener implements ListSelectionListener, ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionevent) {
            ResourceFile resourcefile = resourceFileView.getResourceFile();
            int i = resourceFileView.getSelectedIndex();
            if (i >= 0) {
                resourcefile.setLineTranslation(i, jTextAreaTranslation.getText());
            }
            resourceFileView.incSelection();
            fireTranslationHappened();
        }

        @Override
        public void valueChanged(ListSelectionEvent lse) {
            if (lse.getValueIsAdjusting()) {
                return;
            }
            String s = null;
            boolean flag = false;
            if (resourceFileView.getSelectedIndex() >= 0) {
                ResourceFile resourcefile = resourceFileView.getResourceFile();
                int i = resourceFileView.getSelectedIndex();
                flag = resourcefile.getLineState(i) != -1;
                s = resourcefile.getLineTranslation(i);
            }
            jTextAreaTranslation.setText(s);
            jTextAreaTranslation.setEditable(flag);
            jTextAreaTranslation.setCaretPosition(0);
            jButtonConfirm.setEnabled(flag);
        }
    }

    private class DummyNode extends AbstractNode {

        final FileNameExtensionFilter filter = new FileNameExtensionFilter("Resources files type", "properties");
        JFileChooser fileChooser = new JFileChooser() {

            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    Confirmation CrfMsg = new NotifyDescriptor.Confirmation(
                            "O.K. to overwrite " + f.getName() + "?",
                            NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.QUESTION_MESSAGE);
                    DialogDisplayer.getDefault().notify(CrfMsg);
                    Object result = CrfMsg.getValue();
                    if (result == NotifyDescriptor.OK_OPTION) {
                        super.approveSelection();
                    } else {
                        super.cancelSelection();
                    }
                } else {
                    if (filter.accept(f) == false) {
                        if (f.getName().indexOf('.') > 0) {
                            Confirmation CrfMsg = new NotifyDescriptor.Confirmation(
                                    "Not standard file name extension " + f.getName() + " ok to save ?",
                                    NotifyDescriptor.OK_CANCEL_OPTION,
                                    NotifyDescriptor.QUESTION_MESSAGE);
                            DialogDisplayer.getDefault().notify(CrfMsg);
                            Object result = CrfMsg.getValue();
                            if (result == NotifyDescriptor.OK_OPTION) {
                                super.approveSelection();
                            } else {
                                super.cancelSelection();
                            }
                        } else {
                            setSelectedFile(new File(f.getName() + ".properties"));
                            super.approveSelection();
                        }
                    } else {
                        super.approveSelection();
                    }
                }
            }
        };

        public DummyNode() {
            super(Children.LEAF);
        }

        @Override
        public Node.Cookie getCookie(Class type) {
            if (type == SaveCookie.class && change == true) {
                return new SaveCookie() {

                    @Override
                    public void save() throws IOException {
                        String defaultBundleFileName = resourceFile.getDefaultBundleFile().getName();
                        int extensionIndex = defaultBundleFileName.lastIndexOf(".");
                        int LanguageExtIndex = defaultBundleFileName.lastIndexOf("_");
                        String translatedBundleFileName = "";
                        if (LanguageExtIndex > 0) {
                            translatedBundleFileName = defaultBundleFileName.substring(0, LanguageExtIndex);
                        } else {
                            translatedBundleFileName = defaultBundleFileName.substring(0, extensionIndex);
                        }
                        translatedBundleFileName += "_" + translatedLocale.getLanguage();
                        translatedBundleFileName += defaultBundleFileName.substring(extensionIndex, defaultBundleFileName.length());
                        fileChooser.setSelectedFile(new File(translatedBundleFileName));
                        if (fileChooser.showSaveDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
                            File file = fileChooser.getSelectedFile();
                            if (file != null) {
                                try {
                                    resourceFileView.getResourceFile().writeTo(file, true);
                                    change = false;
                                    fire();
                                } catch (Exception exception) {
                                    NotifyDescriptor errorMsg = new NotifyDescriptor.Message("Error saving to " + file.getName() + "\n<" + exception.getMessage() + ">", NotifyDescriptor.ERROR_MESSAGE);
                                    DialogDisplayer.getDefault().notify(errorMsg);
                                    return;
                                }
                            }
                        }
                    }
                };
            } else {
                return super.getCookie(type);
            }
        }

        public void fire() {
            fireCookieChange();
        }
    }
    private static EditorTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/ancestris/trancestris/editor/Advanced.png";
    private static final String PREFERRED_ID = "EditorTopComponent";
    private ResourceFileView resourceFileView;
    private boolean change = false;
    private DummyNode dummyNode;

    public EditorTopComponent() {
        resourceFileView = new ResourceFileView();
        Listener listener = new Listener();
        initComponents();
        setName(NbBundle.getMessage(EditorTopComponent.class, "CTL_EditorTopComponent"));
        setToolTipText(NbBundle.getMessage(EditorTopComponent.class, "HINT_EditorTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        resourceFileView.addListSelectionListener(listener);
//        jTextAreaTranslation.addActionListener(listener);
        jButtonConfirm.addActionListener(listener);
        setActivatedNodes(new Node[]{
                    dummyNode = new DummyNode()
                });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPaneResourceView = new javax.swing.JScrollPane(resourceFileView);
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaTranslation = new javax.swing.JTextArea();
        jButtonConfirm = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.75);
        jSplitPane1.setLeftComponent(jScrollPaneResourceView);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jTextAreaTranslation.setColumns(20);
        jTextAreaTranslation.setRows(5);
        jScrollPane1.setViewportView(jTextAreaTranslation);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonConfirm, org.openide.util.NbBundle.getMessage(EditorTopComponent.class, "EditorTopComponent.jButtonConfirm.text")); // NOI18N
        jPanel1.add(jButtonConfirm, java.awt.BorderLayout.SOUTH);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));
        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jSplitPane1.setRightComponent(jPanel1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonConfirm;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPaneResourceView;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextArea jTextAreaTranslation;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized EditorTopComponent getDefault() {
        if (instance == null) {
            instance = new EditorTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the EditorTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized EditorTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(EditorTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof EditorTopComponent) {
            return (EditorTopComponent) win;
        }
        Logger.getLogger(EditorTopComponent.class.getName()).warning(
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
        // TODO add custom code on component opening
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

    public void fireTranslationHappened() {
        firePropertyChange("translation", "1", "2");
        change = true;

        dummyNode.fire();
    }

    public void setBundles(ResourceFile resourceFile, Locale locale) {
        this.resourceFile = resourceFile;
        this.translatedLocale = locale;
        resourceFileView.setResourceFile(this.resourceFile);
    }

    public void setTranslatedLocale(Locale locale) {
    }
}
