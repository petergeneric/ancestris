/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard.tools;

import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.gedcom.UnitOfWork;
import genj.util.ReferenceSet;
import genj.util.Registry;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
public class RepoChooser extends JPanel implements DocumentListener {

    private static int THUMB_WIDTH = 120;
    private static int THUMB_HEIGHT = 140;
    
    private Registry registry = null;
    private ThumbComparator thumbComparator = new ThumbComparator();
    private TreeSet<RepoThumb> allRepo = new TreeSet<RepoThumb>(thumbComparator);
    private DefaultListModel filteredModel = new DefaultListModel();
    private DefaultListModel sourceListModel = new DefaultListModel();
    private DefaultComboBoxModel mediaListModel = new DefaultComboBoxModel();
    
    private Gedcom gedcom = null;
    
    private SourceWrapper source = null;
    private Repository repo = null;
    private JButton okButton = null;
    private JButton cancelButton = null;
    
    private boolean isBusy = false;
    
    /**
     * Creates new form NoteChooser
     */
    public RepoChooser(Gedcom gedcom, SourceWrapper source, JButton okButton, JButton cancelButton) {
        this.gedcom = gedcom;
        this.source = source;
        this.repo = source != null ? source.getRepo() : null;
        this.okButton = okButton;
        this.cancelButton = cancelButton;
        
        createRepoThumbs();
        registry = Registry.get(getClass());
        initComponents();
        this.setPreferredSize(new Dimension(registry.get("repoWindowWidth", this.getPreferredSize().width), registry.get("repoWindowHeight", this.getPreferredSize().height)));
        jSplitPane.setDividerLocation(registry.get("repoSplitDividerLocation", jSplitPane.getDividerLocation()));
        
        repoList.setCellRenderer(new ListEntryCellRenderer());
        textFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) {}
            private void filter() {
                filterModel(textFilter.getText());
            }
        });
        addListeners();
        setHotKeys();
        
        // Run Repositories collection from separate thread
        Thread repoThread = new Thread() {
            @Override
            public void run() {
                displayRepoThumbs();
                selectRepo(repo);
            }
        };
        repoThread.setName("Repo reading thread");
        repoThread.start();
    }

    
    private void addListeners() {
        jTextName.getDocument().addDocumentListener(this);
        jTextAddress.getDocument().addDocumentListener(this);
        jTextZip.getDocument().addDocumentListener(this);
        jTextCity.getDocument().addDocumentListener(this);
        jTextCountry.getDocument().addDocumentListener(this);
        jTextEmail.getDocument().addDocumentListener(this);
        jTextWeb.getDocument().addDocumentListener(this);
        reponoteText.getDocument().addDocumentListener(this);
        jTextCaln.getDocument().addDocumentListener(this);
        ((JTextComponent) jComboBoxMedia.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        noteText.getDocument().addDocumentListener(this);
    }

    
    private void setHotKeys() {
        // Enter key
        KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        Action enterAction = new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (jButtonSave.isEnabled()) {
                    jButtonSaveActionPerformed(evt);
                } else {
                    okButton.doClick();
                }
            }
        };
        registerKeyboardAction(enterAction, enterStroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Esc key
        KeyStroke escStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        Action escAction = new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (jButtonCancel.isEnabled()) {
                   jButtonCancelActionPerformed(evt);
                } else {
                    cancelButton.doClick();
                }
            }
        };
        registerKeyboardAction(escAction, escStroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
    }
    

    
    
    private void refreshAll(Repository selectedRepo) {
        createRepoThumbs();
        displayRepoThumbs();
        selectRepo(selectedRepo);
    }

    private void selectRepo(Repository selectedRepo) {
        RepoThumb selectedRepotb = null;
        for (RepoThumb repotb : allRepo) {
            if (repotb.entity == null && selectedRepo == null) {
                selectedRepotb = repotb;
                break;
            }
            if (repotb.entity != null && selectedRepo != null && repotb.entity.getId().equals(selectedRepo.getId())) {
                selectedRepotb = repotb;
                break;
            }
        }
        if (selectedRepotb != null) {
            final RepoThumb repotb = selectedRepotb;
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    repoList.setSelectedValue(repotb, true);
                }
            });
        }
    }
    
    private void clearForm() {
        jTextName.setText("");
        jTextAddress.setText("");
        jTextZip.setText("");
        jTextCity.setText("");
        jTextCountry.setText("");
        jTextEmail.setText("");
        jTextWeb.setText("");
        reponoteText.setText("");
        sourceListModel.removeAllElements();
        sourceListLabel.setText(NbBundle.getMessage(getClass(), "RepoChooser.sourceListLabel.text", sourceListModel.getSize()));
        jTextCaln.setText("");
        ((JTextComponent) jComboBoxMedia.getEditor().getEditorComponent()).setText("");
        noteText.setText("");
    }

    private void displayRepoDetails(Repository repo, SourceWrapper source) {
        isBusy = true;
        clearForm();
        
        if (repo == null) {
            isBusy = false;
            enableButtons(false);
            return;
        }

        jTextName.setText(repo.getRepositoryName());
        jTextName.setCaretPosition(0);
        
        Property pAddr = repo.getProperty("ADDR");
        jTextAddress.setText(pAddr != null ? pAddr.getDisplayValue() : "");
        jTextAddress.setCaretPosition(0);
        if (pAddr != null) {
            Property prop = pAddr.getProperty("POST");
            jTextZip.setText(prop != null ? prop.getDisplayValue() : "");
            prop = pAddr.getProperty("CITY");
            jTextCity.setText(prop != null ? prop.getDisplayValue() : "");
            prop = pAddr.getProperty("CTRY");
            jTextCountry.setText(prop != null ? prop.getDisplayValue() : "");
            jTextZip.setCaretPosition(0);
            jTextCity.setCaretPosition(0);
            jTextCountry.setCaretPosition(0);
            jTextEmail.setCaretPosition(0);
            jTextWeb.setCaretPosition(0);
        } else {
            jTextZip.setText("");
            jTextCity.setText("");
            jTextCountry.setText("");
        }

        Property prop = repo.getProperty(gedcom.getGrammar().getVersion().startsWith("5.5.1") ? "EMAIL" : "_EMAIL");
        jTextEmail.setText(prop != null ? prop.getDisplayValue() : "");
        jTextEmail.setCaretPosition(0);
        
        prop = repo.getProperty(gedcom.getGrammar().getVersion().startsWith("5.5.1") ? "WWW" : "_WWW");
        jTextWeb.setText(prop != null ? prop.getDisplayValue() : "");
        jTextWeb.setCaretPosition(0);
        
        Property pNote = repo.getProperty("NOTE");
        reponoteText.setText(pNote != null ? pNote.getDisplayValue() : "");
        reponoteText.setCaretPosition(0);
        
        getSourceList(repo);
        sourceListLabel.setText(NbBundle.getMessage(getClass(), "RepoChooser.sourceListLabel.text", sourceListModel.getSize()));
        sourceList.setModel(sourceListModel);
        
        // If selected repo corresponds to original source, select the corresponding source in the list
        int selectedSource = 0;
        if (source != null && source.getRepo() == repo) {
            for (int i = 0; i < sourceListModel.getSize(); i++) {
                Entity ent = (Entity) sourceListModel.getElementAt(i);
                if (ent == source.getTargetSource()) {
                    selectedSource = i;
                    break;
                }
            }
        }
        
        getMediaList();
        jComboBoxMedia.setModel(mediaListModel);

        // Finally select source (after medialist)
        if (!sourceListModel.isEmpty()) {
            sourceList.setSelectedIndex(selectedSource);
        } else {
            ((JTextComponent) jComboBoxMedia.getEditor().getEditorComponent()).setText("");
        }
        
        isBusy = false;
        enableButtons(false);
    }

    private DefaultListModel getSourceList(Repository repo) {
        // Clear set
        sourceListModel.removeAllElements();
        
        // Get list of sources in a sorted set
        List<Entity> sources = new ArrayList<Entity>();
        Entity[] ents = PropertyXRef.getReferences(repo);
        for (Entity ent : ents) {
            sources.add(ent);
        }
        Collections.sort(sources, new Comparator<Entity>() {
            public int compare(Entity e1, Entity e2) {
                return e1.toString(true).compareTo(e2.toString(true));
            }
        });
        
        // Add set to model
        for (Entity ent : sources) {
            sourceListModel.addElement(ent);
        }
        return sourceListModel;
    }

    private DefaultComboBoxModel getMediaList() {
        ReferenceSet<String, Property> refs = gedcom.getReferenceSet("MEDI");
        mediaListModel.removeAllElements();
        List<String> keys = refs.getKeys();
        Collections.sort(keys, new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1.toLowerCase().compareTo(s2.toLowerCase());
            }
        });
        for (String key : keys) {
            mediaListModel.addElement(key);
        }
        return mediaListModel;
    }

    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabelName = new javax.swing.JLabel();
        jTextName = new javax.swing.JTextField();
        jLabelAddress = new javax.swing.JLabel();
        jTextAddress = new javax.swing.JTextField();
        jTextZip = new javax.swing.JTextField();
        jTextCity = new javax.swing.JTextField();
        jTextCountry = new javax.swing.JTextField();
        jLabelMail = new javax.swing.JLabel();
        jTextEmail = new javax.swing.JTextField();
        repoEmailLinkButton = new javax.swing.JButton();
        jLabelWeb = new javax.swing.JLabel();
        jTextWeb = new javax.swing.JTextField();
        repoWebLinkButton = new javax.swing.JButton();
        reponoteScrollPane = new javax.swing.JScrollPane();
        reponoteText = new ancestris.swing.UndoTextArea();
        sourceListLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourceList = new javax.swing.JList();
        jLabelCaln = new javax.swing.JLabel();
        jTextCaln = new javax.swing.JTextField();
        jLabelMedia = new javax.swing.JLabel();
        jComboBoxMedia = new javax.swing.JComboBox();
        jLabelNote = new javax.swing.JLabel();
        noteLinkButton = new javax.swing.JButton();
        noteScrollPane = new javax.swing.JScrollPane();
        noteText = new ancestris.swing.UndoTextArea();
        jButtonSave = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        textFilter = new javax.swing.JTextField();
        jScrollPaneRepo = new javax.swing.JScrollPane();
        repoList = new javax.swing.JList(filteredModel);
        jLabel1 = new javax.swing.JLabel();

        setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.toolTipText")); // NOI18N
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jSplitPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSplitPanePropertyChange(evt);
            }
        });

        jPanel1.setPreferredSize(new java.awt.Dimension(250, 383));

        org.openide.awt.Mnemonics.setLocalizedText(jLabelName, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jLabelName.text")); // NOI18N

        jTextName.setText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelAddress, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jLabelAddress.text")); // NOI18N

        jTextAddress.setText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextAddress.text")); // NOI18N
        jTextAddress.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextAddress.toolTipText")); // NOI18N

        jTextZip.setText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextZip.text")); // NOI18N
        jTextZip.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextZip.toolTipText")); // NOI18N
        jTextZip.setPreferredSize(new java.awt.Dimension(50, 27));

        jTextCity.setText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextCity.text")); // NOI18N
        jTextCity.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextCity.toolTipText")); // NOI18N
        jTextCity.setPreferredSize(new java.awt.Dimension(126, 27));

        jTextCountry.setText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextCountry.text")); // NOI18N
        jTextCountry.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextCountry.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelMail, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jLabelMail.text")); // NOI18N

        jTextEmail.setText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextEmail.text")); // NOI18N
        jTextEmail.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextEmail.toolTipText")); // NOI18N

        repoEmailLinkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/mail.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(repoEmailLinkButton, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.repoEmailLinkButton.text")); // NOI18N
        repoEmailLinkButton.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.repoEmailLinkButton.toolTipText")); // NOI18N
        repoEmailLinkButton.setPreferredSize(new java.awt.Dimension(22, 22));
        repoEmailLinkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                repoEmailLinkButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabelWeb, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jLabelWeb.text")); // NOI18N

        jTextWeb.setText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextWeb.text")); // NOI18N
        jTextWeb.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextWeb.toolTipText")); // NOI18N

        repoWebLinkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/web.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(repoWebLinkButton, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.repoWebLinkButton.text")); // NOI18N
        repoWebLinkButton.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.repoWebLinkButton.toolTipText")); // NOI18N
        repoWebLinkButton.setPreferredSize(new java.awt.Dimension(22, 22));
        repoWebLinkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                repoWebLinkButtonActionPerformed(evt);
            }
        });

        reponoteScrollPane.setPreferredSize(new java.awt.Dimension(188, 93));

        reponoteText.setColumns(20);
        reponoteText.setLineWrap(true);
        reponoteText.setRows(3);
        reponoteText.setText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.reponoteText.text")); // NOI18N
        reponoteText.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.reponoteText.toolTipText")); // NOI18N
        reponoteText.setWrapStyleWord(true);
        reponoteScrollPane.setViewportView(reponoteText);

        sourceListLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        sourceListLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(sourceListLabel, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.sourceListLabel.text")); // NOI18N
        sourceListLabel.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.sourceListLabel.toolTipText")); // NOI18N
        sourceListLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        sourceList.setModel(sourceListModel);
        sourceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        sourceList.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.sourceList.toolTipText")); // NOI18N
        sourceList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                sourceListMousePressed(evt);
            }
        });
        sourceList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                sourceListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(sourceList);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelCaln, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jLabelCaln.text")); // NOI18N

        jTextCaln.setText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jTextCaln.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelMedia, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jLabelMedia.text")); // NOI18N

        jComboBoxMedia.setEditable(true);
        jComboBoxMedia.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabelNote, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jLabelNote.text")); // NOI18N

        noteLinkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/web.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(noteLinkButton, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.noteLinkButton.text")); // NOI18N
        noteLinkButton.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.noteLinkButton.toolTipText")); // NOI18N
        noteLinkButton.setPreferredSize(new java.awt.Dimension(22, 22));
        noteLinkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noteLinkButtonActionPerformed(evt);
            }
        });

        noteText.setColumns(20);
        noteText.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        noteText.setLineWrap(true);
        noteText.setRows(3);
        noteText.setText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.noteText.text")); // NOI18N
        noteText.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.noteText.toolTipText")); // NOI18N
        noteText.setWrapStyleWord(true);
        noteScrollPane.setViewportView(noteText);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonSave, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jButtonSave.text")); // NOI18N
        jButtonSave.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jButtonSave.toolTipText")); // NOI18N
        jButtonSave.setEnabled(false);
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonCancel, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jButtonCancel.text")); // NOI18N
        jButtonCancel.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jButtonCancel.toolTipText")); // NOI18N
        jButtonCancel.setEnabled(false);
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonDelete, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jButtonDelete.text")); // NOI18N
        jButtonDelete.setToolTipText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jButtonDelete.toolTipText")); // NOI18N
        jButtonDelete.setEnabled(false);
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(sourceListLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(reponoteScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelAddress)
                                    .addComponent(jLabelName)
                                    .addComponent(jLabelMail)
                                    .addComponent(jLabelWeb))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jTextZip, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextCity, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                                    .addComponent(jTextName)
                                    .addComponent(jTextAddress)
                                    .addComponent(jTextCountry)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jTextEmail)
                                            .addComponent(jTextWeb))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(repoEmailLinkButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(repoWebLinkButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelCaln)
                                    .addComponent(jLabelMedia)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabelNote)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(noteLinkButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextCaln)
                                    .addComponent(jComboBoxMedia, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(noteScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jButtonSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonDelete)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelName)
                    .addComponent(jTextName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelAddress)
                    .addComponent(jTextAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextZip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(jTextCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabelMail)
                    .addComponent(jTextEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(repoEmailLinkButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabelWeb)
                    .addComponent(jTextWeb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(repoWebLinkButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reponoteScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(sourceListLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1)
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelCaln)
                    .addComponent(jTextCaln, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelMedia)
                    .addComponent(jComboBoxMedia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(noteScrollPane))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(noteLinkButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelNote))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonCancel)
                    .addComponent(jButtonSave)
                    .addComponent(jButtonDelete))
                .addContainerGap())
        );

        jSplitPane.setLeftComponent(jPanel1);

        jPanel2.setPreferredSize(new java.awt.Dimension(200, 58));

        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.filterLabel.text")); // NOI18N

        textFilter.setText(org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.textFilter.text")); // NOI18N

        repoList.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        repoList.setFont(new java.awt.Font("DejaVu Sans Condensed", 0, 10)); // NOI18N
        repoList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        repoList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        repoList.setVisibleRowCount(-1);
        repoList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                repoListMouseClicked(evt);
            }
        });
        repoList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                repoListValueChanged(evt);
            }
        });
        jScrollPaneRepo.setViewportView(repoList);

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RepoChooser.class, "RepoChooser.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPaneRepo)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(filterLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel1)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterLabel)
                    .addComponent(textFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneRepo, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane.setRightComponent(jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void repoListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_repoListValueChanged
        displaySelection();
    }//GEN-LAST:event_repoListValueChanged

    private void repoListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repoListMouseClicked
        if (evt.getClickCount() == 2) {
            okButton.doClick();
        }
    }//GEN-LAST:event_repoListMouseClicked

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        registry.put("repoWindowWidth", evt.getComponent().getWidth());
        registry.put("repoWindowHeight", evt.getComponent().getHeight());
    }//GEN-LAST:event_formComponentResized

    private void jSplitPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPanePropertyChange
        registry.put("repoSplitDividerLocation", jSplitPane.getDividerLocation());
    }//GEN-LAST:event_jSplitPanePropertyChange

    private void sourceListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_sourceListValueChanged
        if (sourceList.getSelectedIndex() == -1) {
            return;
        }
        isBusy = true;
        Source selectedSource = (Source) sourceListModel.getElementAt(sourceList.getSelectedIndex());
        if (selectedSource != null) {
            Property pRepo = selectedSource.getProperty("REPO");
            if (pRepo != null) {
                Property pCaln = pRepo.getProperty("CALN");
                String strCaln = (pCaln != null ? pCaln.getDisplayValue() : "");
                String strMedi = "";
                JTextComponent jTextMedia = (JTextComponent) jComboBoxMedia.getEditor().getEditorComponent();
                if (pCaln != null) {
                    Property pMedi = pCaln.getProperty("MEDI");
                    strMedi = (pMedi != null ? pMedi.getDisplayValue() : "");
                }
                Property pNote = pRepo.getProperty("NOTE");
                String strNote = (pNote != null ? pNote.getDisplayValue() : "");
                jTextCaln.setText(strCaln);
                jTextMedia.setText(strMedi);
                noteText.setText(strNote);
            }
        }
        isBusy = false;
    }//GEN-LAST:event_sourceListValueChanged

    private void sourceListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sourceListMousePressed
        if (evt.getButton() == MouseEvent.BUTTON3 && sourceList.getSelectedIndex() != -1) { 
            Source selectedSource = (Source) sourceListModel.getElementAt(sourceList.getSelectedIndex());
            JPopupMenu menu = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem(NbBundle.getMessage(getClass(), "EditEntity", selectedSource.toString(true)));
            menu.add(menuItem);
            final Entity finalEntity = selectedSource;
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    cancelButton.doClick();
                    SelectionDispatcher.fireSelection(new Context(finalEntity));
                }
            });
            menu.show(sourceList, evt.getX(), evt.getY()); 
        }
    }//GEN-LAST:event_sourceListMousePressed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        if (gedcom.isWriteLocked()) {
            commit();
        } else {
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {
                    
                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        commit();
                    }
                });
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        displaySelection();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        if (gedcom.isWriteLocked()) {
            delete();
        } else {
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {
                    
                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        delete();
                    }
                });
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void noteLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noteLinkButtonActionPerformed
        gotoLink(noteText.getText());
    }//GEN-LAST:event_noteLinkButtonActionPerformed

    private void repoEmailLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_repoEmailLinkButtonActionPerformed
        openMail(jTextEmail.getText());
    }//GEN-LAST:event_repoEmailLinkButtonActionPerformed

    private void repoWebLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_repoWebLinkButtonActionPerformed
        gotoLink(jTextWeb.getText());
    }//GEN-LAST:event_repoWebLinkButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filterLabel;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JComboBox jComboBoxMedia;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelAddress;
    private javax.swing.JLabel jLabelCaln;
    private javax.swing.JLabel jLabelMail;
    private javax.swing.JLabel jLabelMedia;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelNote;
    private javax.swing.JLabel jLabelWeb;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPaneRepo;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JTextField jTextAddress;
    private javax.swing.JTextField jTextCaln;
    private javax.swing.JTextField jTextCity;
    private javax.swing.JTextField jTextCountry;
    private javax.swing.JTextField jTextEmail;
    private javax.swing.JTextField jTextName;
    private javax.swing.JTextField jTextWeb;
    private javax.swing.JTextField jTextZip;
    private javax.swing.JButton noteLinkButton;
    private javax.swing.JScrollPane noteScrollPane;
    private javax.swing.JTextArea noteText;
    private javax.swing.JButton repoEmailLinkButton;
    private javax.swing.JList repoList;
    private javax.swing.JButton repoWebLinkButton;
    private javax.swing.JScrollPane reponoteScrollPane;
    private javax.swing.JTextArea reponoteText;
    private javax.swing.JList sourceList;
    private javax.swing.JLabel sourceListLabel;
    private javax.swing.JTextField textFilter;
    // End of variables declaration//GEN-END:variables

    
    
    
    private void createRepoThumbs() {
        
        // Clear note list
        allRepo.clear();
        
        // Add new repo
        allRepo.add(new RepoThumb());

        // Get all repository entities (REPO)
        Collection<Repository> entities = (Collection<Repository>) gedcom.getEntities(Gedcom.REPO);
        for (Repository entity : entities) {
            RepoThumb repotb = new RepoThumb(entity);
            Entity[] ents = PropertyXRef.getReferences(entity);
            repotb.setUnused(ents.length == 0);
            allRepo.add(repotb);
        }
        
    }

    private void displayRepoThumbs() {
        // Put them in model in sorted order
        filteredModel.clear();
        for (RepoThumb item : allRepo) {
            filteredModel.addElement(item);
        }
        
    }

    private RepoThumb getSelectedThumb() {
        return (RepoThumb) filteredModel.get(repoList.getSelectedIndex());
    }
    
    public boolean isSelectedEntityRepo() {
        RepoThumb repotb = getSelectedThumb();
        return repotb == null ? false : repotb.isRepo;
    }

    public Entity getSelectedEntity() {
        RepoThumb repotb = getSelectedThumb();
        return repotb == null ? null : repotb.entity;
    }

    public String getSelectedText() {
        RepoThumb repotb = getSelectedThumb();
        return repotb == null ? "" : repotb.text;
    }


    
    
    
    
    public void filterModel(String filter) {
        repoList.clearSelection();
        repoList.setModel(new DefaultListModel());
        filteredModel.clear();
        for (RepoThumb item : allRepo) {
            if (item.text.toLowerCase().contains(filter.toLowerCase())) {
                filteredModel.addElement(item);
            }
        }
        repoList.setModel(filteredModel);
    }    

    public int getNbRepos() {
        return allRepo.size();
    }

    
    // Document listener methods
    public void insertUpdate(DocumentEvent e) {
        enableButtons(true);
    }

    public void removeUpdate(DocumentEvent e) {
        enableButtons(true);
    }

    public void changedUpdate(DocumentEvent e) {
        enableButtons(true);
    }

    private void enableButtons(boolean flag) {
        if (!isBusy) {
            jButtonSave.setEnabled(flag);
            jButtonCancel.setEnabled(flag);
            okButton.setEnabled(!flag && okButton.isEnabled());
            cancelButton.setEnabled(!flag);
            repoEmailLinkButton.setEnabled(jTextEmail.getText().toLowerCase().contains("@"));
            repoWebLinkButton.setEnabled(jTextWeb.getText().toLowerCase().contains("http"));
            noteLinkButton.setEnabled(noteText.getText().toLowerCase().contains("http"));
        }
    }

    private void displaySelection() {
        if (!repoList.isSelectionEmpty()) {
            RepoThumb repotb = (RepoThumb) filteredModel.get(repoList.getSelectedIndex());
            displayRepoDetails(repotb.entity, source);
            okButton.setEnabled(true);
            jButtonDelete.setEnabled(repotb.entity != null);
        } else {
            displayRepoDetails(repo, source);
            okButton.setEnabled(false);
            jButtonDelete.setEnabled(false);
        }
    }

    private void commit() {
        Repository repoToSave = null;
        if (!repoList.isSelectionEmpty()) {
            RepoThumb repotb = (RepoThumb) filteredModel.get(repoList.getSelectedIndex());
            repoToSave = repotb.entity;
        } else {
            repoToSave = repo;
        }
        if (repoToSave == null) {
            try {
                repoToSave = (Repository) gedcom.createEntity("REPO");
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
        }
        
        String value;
        Property prop;
        
        // name
        value = jTextName.getText().trim();
        prop = repoToSave.getProperty("NAME");
        if (prop != null) {
            prop.setValue(value);
        } else if (!value.isEmpty()) {
            repoToSave.addProperty("NAME", value);
        }
        
        // address
        value = jTextAddress.getText().trim();
        prop = repoToSave.getProperty("ADDR");
        if (prop != null) {
            prop.setValue(value);
        } else if (!value.isEmpty() || !jTextZip.getText().trim().isEmpty() || !jTextCity.getText().trim().isEmpty() || !jTextCountry.getText().trim().isEmpty()) {
            prop = repoToSave.addProperty("ADDR", value);
        }

        if (prop != null) {
            value = jTextZip.getText().trim();
            Property p = prop.getProperty("POST");
            if (p != null) {
                p.setValue(value);
            } else if (!value.isEmpty()) {
                prop.addProperty("POST", value);
            }

            value = jTextCity.getText().trim();
            p = prop.getProperty("CITY");
            if (p != null) {
                p.setValue(value);
            } else if (!value.isEmpty()) {
                prop.addProperty("CITY", value);
            }

            value = jTextCountry.getText().trim();
            p = prop.getProperty("CTRY");
            if (p != null) {
                p.setValue(value);
            } else if (!value.isEmpty()) {
                prop.addProperty("CTRY", value);
            }
        }
        
        value = jTextEmail.getText().trim();
        String tag = gedcom.getGrammar().getVersion().startsWith("5.5.1") ? "EMAIL" : "_EMAIL";
        Property p = repoToSave.getProperty(tag);
        if (p != null) {
            p.setValue(value);
        } else if (!value.isEmpty()) {
            repoToSave.addProperty(tag, value);
        }
        
        value = jTextWeb.getText().trim();
        tag = gedcom.getGrammar().getVersion().startsWith("5.5.1") ? "WWW" : "_WWW";
        p = repoToSave.getProperty(tag);
        if (p != null) {
            p.setValue(value);
        } else if (!value.isEmpty()) {
            repoToSave.addProperty(tag, value);
        }
        
        // note
        value = reponoteText.getText().trim();
        prop = repoToSave.getProperty("NOTE");
        if (prop != null) {
            prop.setValue(value);
        } else if (!value.isEmpty()) {
            repoToSave.addProperty("NOTE", value);
        }
        
        // source details
        Source sourceToSave = null;
        if (sourceList.getSelectedIndex() != -1) {
            sourceToSave = (Source) sourceListModel.getElementAt(sourceList.getSelectedIndex());
        } else {
            sourceToSave = source != null ? (Source) source.getTargetSource() : null;
        }
        if (sourceToSave != null) {
            Property pRepo = sourceToSave.getProperty("REPO");
            if (pRepo != null) {
                String strCaln = jTextCaln.getText().trim();
                String strMedi = ((JTextComponent) jComboBoxMedia.getEditor().getEditorComponent()).getText().trim();
                Property pCaln = pRepo.getProperty("CALN");
                if (pCaln != null) {
                    pCaln.setValue(strCaln);
                } else if (!strCaln.isEmpty()) {
                    pCaln = pRepo.addProperty("CALN", strCaln);
                }
                Property pMedi = pCaln.getProperty("MEDI");
                if (pMedi != null) {
                    pMedi.setValue(strMedi);
                } else if (!strMedi.isEmpty()) {
                    pMedi = pCaln.addProperty("MEDI", strMedi);
                }
                String strNote = noteText.getText().trim();
                Property pNote = pRepo.getProperty("NOTE");
                if (pNote != null) {
                    pNote.setValue(strNote);
                } else if (!strNote.isEmpty()) {
                    pNote = pRepo.addProperty("NOTE", strNote);
                }
            }
        }

        refreshAll(repoToSave);
    }

    private void delete() {
        Repository repoToDelete = null;
        if (!repoList.isSelectionEmpty()) {
            RepoThumb repotb = (RepoThumb) filteredModel.get(repoList.getSelectedIndex());
            repoToDelete = repotb.entity;
        } else {
            repoToDelete = repo;
        }
        if (repoToDelete != null) {
            gedcom.deleteEntity(repoToDelete);
            Repository firstEnt = (Repository) gedcom.getFirstEntity("REPO");
            refreshAll(firstEnt != null ? firstEnt : null);
        }
    }

    private void gotoLink(String text) {
        try {
            String link = text.toLowerCase().replaceAll(" ", "%20");
            int i = link.indexOf("http");
            Desktop.getDesktop().browse(new URI(link.substring(i)));
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        } catch (URISyntaxException ex) {
            //Exceptions.printStackTrace(ex);
        }
    }

    private void openMail(String text) {
        try {
            Desktop.getDesktop().mail(new URI("mailto:"+text));
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        } catch (URISyntaxException ex) {
            //Exceptions.printStackTrace(ex);
        }
    }


  
    
    
    private static class ListEntryCellRenderer extends JLabel implements ListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            RepoThumb entry = (RepoThumb) value;

            setHorizontalTextPosition(JLabel.CENTER);
            setVerticalTextPosition(JLabel.TOP);
            setVerticalAlignment(JLabel.TOP);
            setHorizontalAlignment(JLabel.CENTER);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredSoftBevelBorder())));

            int labelWidth = THUMB_WIDTH - 8;
            int labelHeight = THUMB_HEIGHT - 8;  
            int nbLines = labelHeight / 18 ; // 12 pixels per line for font size 10 set in component netbeans parameters
            int nbTotalLines = getFontMetrics(getFont()).stringWidth(entry.text) / labelWidth;
            int nbMaxCars = entry.text.length();
            String add = "";
            if (nbTotalLines > nbLines) {
                nbMaxCars = nbMaxCars * nbLines / nbTotalLines - 3;
                add = "...";
            }
            String text = entry.text.substring(0, Math.min(entry.text.length(), nbMaxCars)) + add;
            setPreferredSize(new Dimension(labelWidth, labelHeight));
            // black : entity & used
            // blue  : ?
            // red   : not used
            String color = entry.isRepo && !entry.isUnused ? "black" : !entry.isRepo && !entry.isUnused ? "blue" : "red";
            if (entry.entity == null) { // new note
                text = "<center><font size=+0><br><br><i><b>" + text + "</b></i></font></center>";
            }
            setText("<html><font color="+color+">" + text + "</font></html>");

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);

            return this;
        }
    }


    
    
    
    
    
    private class RepoThumb {
        
        public boolean isRepo = false;
        public Repository entity = null;
        public String title = ""; // name of repo
        public String text = "";  // text appearing in the repo box (the name + note)
        public boolean isUnused = false;
        
        public RepoThumb() { // used for new repository
            this.isRepo = true;
            this.entity = null;
            this.title = NbBundle.getMessage(getClass(), "NewRepoTitle");
            this.text = NbBundle.getMessage(getClass(), "NewRepoText");
        }

        public RepoThumb(Repository entity) {
            this.isRepo = true;
            this.entity = entity;
            this.title = entity.toString(true);
            this.text = "<center>" + entity.toString(true) + "</center>"; //entity.getRepositoryName();
            Property pAddr = entity.getProperty("ADDR");
            if (pAddr != null) {
                Property prop = pAddr.getProperty("CITY");
                if (prop != null && !prop.getDisplayValue().trim().isEmpty()) {
                    this.text += "<br>&bull;&nbsp;" + prop.getDisplayValue();
                }
                prop = pAddr.getProperty("CTRY");
                if (prop != null && !prop.getDisplayValue().trim().isEmpty()) {
                    this.text += ", " + prop.getDisplayValue();
                }
                if (!pAddr.getDisplayValue().trim().isEmpty()) {
                    String str = pAddr.getDisplayValue();
                    if (str.length()>0) {
                        this.text += "<br>&bull;&nbsp;" + str.substring(0, Math.min(16, str.length()-1)) + ".";
                    }
                }
            }
            Property prop = entity.getProperty("NOTE");
            if (prop != null) {
                this.text += "<br>&bull;&nbsp;" + prop.getDisplayValue();
            }
        }

        private void setUnused(boolean b) {
            isUnused = b;
        }
    }



    
    private class ThumbComparator implements Comparator<RepoThumb> {

        public int compare(RepoThumb o1, RepoThumb o2) {
            String ent1 = o1.entity == null ? "0" : "1";
            String ent2 = o2.entity == null ? "0" : "1";
            String id1 = o1.entity == null ? "" : o1.entity.getId();
            String id2 = o2.entity == null ? "" : o2.entity.getId();
            String total1 = ent1 + o1.title.toLowerCase() + id1;
            String total2 = ent2 + o2.title.toLowerCase() + id2;
            return total1.compareTo(total2);
        }
    }

    
}
