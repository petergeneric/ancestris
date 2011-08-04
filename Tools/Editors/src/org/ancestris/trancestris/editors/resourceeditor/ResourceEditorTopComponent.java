/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.editors.resourceeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.ancestris.trancestris.resources.ResourceFile;
import org.ancestris.trancestris.resources.ZipDirectory;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.ancestris.trancestris.editors.resourceeditor//ResourceEditor//EN", autostore = false)
public final class ResourceEditorTopComponent extends TopComponent implements LookupListener {

    private class Listener implements ListSelectionListener, ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionevent) {
            int i = resourceFileView.getSelectedIndex();
            if (i >= 0) {
                resourceFile.setLineTranslation(i, textAreaTranslation.getText());
            }

            while (i + 1 < resourceFileView.getModel().getSize()) {
                if (resourceFile.getLineState(++i) == 0) {
                    resourceFileView.setSelectedIndex(i);
                    resourceFileView.ensureIndexIsVisible(i);
                    break;
                }
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent lse) {
            if (lse.getValueIsAdjusting()) {
                return;
            }
            String translation = "";
            String comment = "";

            boolean flag = false;
            if (resourceFileView.getSelectedIndex() >= 0) {
                int i = resourceFileView.getSelectedIndex();
                flag = resourceFile.getLineState(i) != -1;
                translation = resourceFile.getLineTranslation(i);
                comment = resourceFile.getLineComment(i);
            }
            textAreaComments.setText(comment);
            textAreaTranslation.setText(translation);
            textAreaTranslation.setEditable(true);
            textAreaTranslation.setCaretPosition(0);
            buttonConfirmTranslation.setEnabled(true);
        }
    }

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
                case -1:
                    color = Color.BLUE;
                    break;

                case 0: // '\0'
                    color = Color.RED;
                    break;

                case 1: // '\001'
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
    private static ResourceEditorTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/ancestris/trancestris/editors/resourceeditor/Advanced.png";
    private static final String PREFERRED_ID = "ResourceEditorTopComponent";
//    private ResourceFileView resourceFileView;
    private Lookup.Result result = null;
    private ResourceFile resourceFile = null;

    public ResourceEditorTopComponent() {
//        resourceFileView = new ResourceFileView();
        initComponents();
        String fontName = NbPreferences.forModule(ResourceEditorTopComponent.class).get("Font.Name", "Dialog");
        int fontStyle = Integer.valueOf(NbPreferences.forModule(ResourceEditorTopComponent.class).get("Font.Style", "0"));
        int fontSize = Integer.valueOf(NbPreferences.forModule(ResourceEditorTopComponent.class).get("Font.Size", "12"));
        setFont(new Font(fontName, fontStyle, fontSize));
        Listener listener = new Listener();
        setName(NbBundle.getMessage(ResourceEditorTopComponent.class, "CTL_ResourceEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(ResourceEditorTopComponent.class, "HINT_ResourceEditorTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        resourceFileView.addListSelectionListener(listener);
        buttonConfirmTranslation.addActionListener(listener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPaneComments = new javax.swing.JScrollPane();
        textAreaComments = new javax.swing.JTextArea();
        scrollPaneResourceView = new javax.swing.JScrollPane(resourceFileView);
        resourceFileView = new javax.swing.JList();
        panelTranslation = new javax.swing.JPanel();
        scrollPaneTranslation = new javax.swing.JScrollPane();
        textAreaTranslation = new javax.swing.JTextArea();
        buttonConfirmTranslation = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        textAreaComments.setColumns(20);
        textAreaComments.setEditable(false);
        textAreaComments.setRows(5);
        scrollPaneComments.setViewportView(textAreaComments);

        add(scrollPaneComments, java.awt.BorderLayout.NORTH);

        resourceFileView.setFont(new java.awt.Font("Dialog", 0, 12));
        resourceFileView.setModel(new ResourceFileModel ());
        resourceFileView.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        resourceFileView.setCellRenderer(new ResourceFileCellRenderer());
        scrollPaneResourceView.setViewportView(resourceFileView);

        add(scrollPaneResourceView, java.awt.BorderLayout.CENTER);

        panelTranslation.setLayout(new java.awt.BorderLayout());

        textAreaTranslation.setColumns(20);
        textAreaTranslation.setEditable(false);
        textAreaTranslation.setRows(5);
        scrollPaneTranslation.setViewportView(textAreaTranslation);

        panelTranslation.add(scrollPaneTranslation, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(buttonConfirmTranslation, org.openide.util.NbBundle.getMessage(ResourceEditorTopComponent.class, "ResourceEditorTopComponent.buttonConfirmTranslation.text")); // NOI18N
        buttonConfirmTranslation.setEnabled(false);
        panelTranslation.add(buttonConfirmTranslation, java.awt.BorderLayout.SOUTH);

        add(panelTranslation, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonConfirmTranslation;
    private javax.swing.JPanel panelTranslation;
    private javax.swing.JList resourceFileView;
    private javax.swing.JScrollPane scrollPaneComments;
    private javax.swing.JScrollPane scrollPaneResourceView;
    private javax.swing.JScrollPane scrollPaneTranslation;
    private javax.swing.JTextArea textAreaComments;
    private javax.swing.JTextArea textAreaTranslation;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ResourceEditorTopComponent getDefault() {
        if (instance == null) {
            instance = new ResourceEditorTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ResourceEditorTopComponent instance. Never call {@link #getDefault} directly!
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
                if (resourceFile != null) {
                    resourceFileView.setSelectedIndex(0);
                    textAreaTranslation.setText(resourceFile.getLineTranslation(0));
                    textAreaTranslation.setEditable(true);
                    textAreaTranslation.setCaretPosition(0);
                    buttonConfirmTranslation.setEnabled(true);
                } else {
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
}
