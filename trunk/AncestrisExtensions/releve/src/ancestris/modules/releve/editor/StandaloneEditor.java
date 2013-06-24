package ancestris.modules.releve.editor;

import ancestris.modules.releve.MenuCommandProvider;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.PlaceManager;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/*
 * StandaloneEditor.java
 *
 * Created on 1 avr. 2012, 17:54:42
 */

/**
 *
 * @author Michel
 */
public class StandaloneEditor extends javax.swing.JFrame {

    private File folder = null;
    private String[] fileNames;
    boolean browserVisible = false;
    Rectangle standaloneEditorBounds = new Rectangle();
    int editorWidth ;
    
    /**
     * Cree une nouvelle fenetre
     * Recupere la taille et la position de la session précédente
     *
     * Remarque : A sa fermeture (windowClosing) la fenetre enregistre sa taille
     * et sa position et appelle ReleveTopComponent.setStandaloneEditor(false)
     * pour signaler sa fermeture
     */
    public StandaloneEditor() {
        //super(new javax.swing.JFrame(), false);
        initComponents();
        ImageIcon icon = new ImageIcon(StandaloneEditor.class.getResource("/ancestris/modules/releve/images/Releve.png"));
        setIconImage(icon.getImage());
        // J'applique un poids=1 pour que seule la largeur du composant de gauche soit mdofiées quand on change la taille de la fenetre
        jSplitPane1.setResizeWeight(1.0);


        //setAlwaysOnTop(true);
        // je configure les editeurs
        birthEditor.setStandaloneMode();
        marriageEditor.setStandaloneMode();
        deathEditor.setStandaloneMode();
        miscEditor.setStandaloneMode();
        
        browserVisible = Boolean.parseBoolean(NbPreferences.forModule(StandaloneEditor.class).get("ImgageBrowserVisible", "false"));
        editorWidth = Integer.parseInt(NbPreferences.forModule(StandaloneEditor.class).get("StandaloneEditorWidth", "300"));

        // je configure la taille de la fenetre
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        String size = NbPreferences.forModule(StandaloneEditor.class).get("StandaloneEditorSize", "300,450,0,0");
        String[] dimensions = size.split(",");
        if ( dimensions.length >= 4 ) {
            int width = Integer.parseInt(dimensions[0]);
            int height = Integer.parseInt(dimensions[1]);
            int x = Integer.parseInt(dimensions[2]);
            int y = Integer.parseInt(dimensions[3]);
            if ( width < 100 ) {
                width = 100;
            }
            if ( height < 100 ) {
                height = 100;
            }
            if ( x < 10 || x > screen.width -10) {
                x = (screen.width / 2) - (width / 2);
            }
            if ( y < 10 || y > screen.height -10) {
                y = (screen.height / 2) - (height / 2);
            }
            standaloneEditorBounds.setBounds(x, y, width, height);
        } else {
            standaloneEditorBounds.setBounds(screen.width / 2 -100, screen.height / 2- 100, 300, 450);
        }

        // j'applique la taille de la fenetre avant de dimensionner jSplitPane1
        setBounds();
//        validate();

//        // je dimensionne le panneau droit de jSplitPane1
//        if (browserVisible) {
//            if (jSplitPane1.getWidth()  > editorWidth ) {
//                jSplitPane1.setDividerLocation(jSplitPane1.getWidth() - editorWidth - jSplitPane1.getDividerSize() );
//            }
//        } else {
//
//        }
        // Image browser
        String imageDirectory = NbPreferences.forModule(StandaloneEditor.class).get("ImageDirectory", "");
        File f = new File(imageDirectory);
        if (f.exists()) {
            folder = f;
            populateImageList(folder);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    populateImageList(folder);
                }
            });
        }

        ListSelectionListener lsl = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String fileName = listFiles.getSelectedValue().toString();
                refreshShownImage(fileName, false);
            }
        };
        listFiles.addListSelectionListener(lsl);

        jPanelBrowser.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke(KeyEvent.VK_LEFT , InputEvent.ALT_DOWN_MASK), "previousImage");
        jPanelBrowser.getActionMap().put("previousImage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jButtonLeftActionPerformed(null);
            }
        });

        jPanelBrowser.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT , InputEvent.ALT_DOWN_MASK ) , "nextImage");
        jPanelBrowser.getActionMap().put("nextImage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jButtonRightActionPerformed(null);
            }
        });

        jPanelBrowser.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke(KeyEvent.VK_DOWN , InputEvent.ALT_DOWN_MASK ) , "downLeftCornerImage");
        jPanelBrowser.getActionMap().put("downLeftCornerImage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jButtonBottomActionPerformed(null);
            }
        });

        jPanelBrowser.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke(KeyEvent.VK_UP , InputEvent.ALT_DOWN_MASK ) , "upLeftCornerImage");
        jPanelBrowser.getActionMap().put("upLeftCornerImage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jButtonTopActionPerformed(null);
            }
        });
    }

    /**
     * Initialise les modeles de donnée des 4 panneaux.
     *
     * Cette methode doit être appelée systematiquemnt  apres le contructeur
     * StandaloneEditor() pour que la fenetre puisse afficher les données d'un
     * modele.
     *
     * @param releveBirthModel
     * @param releveMarriageModel
     * @param releveDeathModel
     * @param releveMiscModel
     */
    public void setDataManager(DataManager dataManager, PlaceManager placeManager, final MenuCommandProvider menuCommandProvider) {
        birthEditor.setModel(dataManager, DataManager.ModelType.birth, placeManager, menuCommandProvider);
        marriageEditor.setModel(dataManager, DataManager.ModelType.marriage, placeManager, menuCommandProvider);
        deathEditor.setModel(dataManager, DataManager.ModelType.death, placeManager, menuCommandProvider);
        miscEditor.setModel(dataManager, DataManager.ModelType.misc, placeManager, menuCommandProvider);

        // je selection le premier releve
        selectRecord(0, 0, 0, 0, 0);

        // je crée les raccourcis pour créer un nouveau relevé
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt N"), jTabbedPane1);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt M"), jTabbedPane1);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt D"), jTabbedPane1);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt V"), jTabbedPane1);
        jTabbedPane1.getActionMap().put(jTabbedPane1, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if ( actionEvent.getActionCommand().toUpperCase().equals("N") ) {
                    jTabbedPane1.setSelectedComponent(birthEditor);
                    birthEditor.createRecord();
                } else if ( actionEvent.getActionCommand().toUpperCase().equals("M") ) {
                    jTabbedPane1.setSelectedComponent(marriageEditor);
                    marriageEditor.createRecord();
                } else if ( actionEvent.getActionCommand().toUpperCase().equals("D") ) {
                    jTabbedPane1.setSelectedComponent(deathEditor);
                    deathEditor.createRecord();
                } else if ( actionEvent.getActionCommand().toUpperCase().equals("V") ) {
                    jTabbedPane1.setSelectedComponent(miscEditor);
                    miscEditor.createRecord();
                }
            }
        });
        
         // listener pour intercepter l'evenement de fermeture de la fenetre.
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // j'enregistre la taille dans les preferences
                String size ;
                int editorWidth;
                if ( browserVisible) {
                    editorWidth = jSplitPane1.getWidth() - jSplitPane1.getDividerLocation() - jSplitPane1.getDividerSize();
                    size= String.valueOf(e.getWindow().getWidth())+","
                            + String.valueOf(e.getWindow().getHeight()) + ","
                            + String.valueOf(e.getWindow().getLocation().x) + ","
                            + String.valueOf(e.getWindow().getLocation().y)
                            ;

                } else {
                     editorWidth = e.getWindow().getWidth() - jSplitPane1.getDividerSize();
                     size= String.valueOf(standaloneEditorBounds.width)+","
                            + String.valueOf(e.getWindow().getHeight()) + ","
                            + String.valueOf( e.getWindow().getLocation().x - standaloneEditorBounds.width + editorWidth ) + ","
                            + String.valueOf(e.getWindow().getLocation().y)
                            ;
                }
                
                NbPreferences.forModule(StandaloneEditor.class).put("StandaloneEditorSize", size);

                NbPreferences.forModule(StandaloneEditor.class).put("StandaloneEditorWidth", String.valueOf(editorWidth));
                String imageDirectory = "";
                if (folder != null) {
                    try {
                        imageDirectory = folder.getCanonicalPath();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    NbPreferences.forModule(StandaloneEditor.class).put("ImageDirectory", imageDirectory);
                }

                menuCommandProvider.standaloneEditorClosed();
            }
        });
    }

    /**
     * initialise le titre de la fenetre
     * @param fileName
     */
    @Override
    public void setTitle(String fileName) {
        super.setTitle(fileName);
    }

    public void setBrowserVisible(boolean visible) {
        browserVisible = visible;
        setBounds();
    }

    private void setBounds() {
        if( browserVisible) {
            // j'affiche le browser d'image
            jpanelImage.setVisible(true);
            setBounds(standaloneEditorBounds);
            validate();
            // je dimensionne le panneau droit de jSplitPane1
            if (jSplitPane1.getWidth()  > editorWidth ) {
                jSplitPane1.setDividerLocation(jSplitPane1.getWidth() - editorWidth - jSplitPane1.getDividerSize() );
            }
        } else {
            // je masque le browser
            jpanelImage.setVisible(false);
            int x = standaloneEditorBounds.x + (standaloneEditorBounds.width - editorWidth );
            int y = standaloneEditorBounds.y;
            int width = editorWidth + jSplitPane1.getDividerSize();
            int height = standaloneEditorBounds.height;
            setBounds(x,y, width, height);
            validate();
        }
    }

    /**
     * selectionne les releves a afficher dans l'editeur
     *
     * @param recordBirthIndex
     * @param recordMarriageIndex
     * @param recordDeathIndex
     * @param recordMiscIndex
     */
    public void selectRecord(int recordBirthIndex, int recordMarriageIndex, int recordDeathIndex, int recordMiscIndex, int selectedPanel) {
        birthEditor.selectRecord(recordBirthIndex);
        marriageEditor.selectRecord(recordMarriageIndex);
        deathEditor.selectRecord(recordDeathIndex);
        miscEditor.selectRecord(recordMiscIndex);
        // je selectionne l'onglet
        jTabbedPane1.setSelectedIndex(selectedPanel);

    }

   @SuppressWarnings("unchecked")
    private void populateImageList(File folder) {
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
            }
        });

        fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++)
            fileNames[i] = files[i].getName();
        Arrays.sort(fileNames, String.CASE_INSENSITIVE_ORDER);
        listFiles.setListData(fileNames);
    }


    private void refreshShownImage(String fileName, boolean force) {
        if (lbFileName.getText().equals(fileName) && !force)
            return;

        lbFileName.setText(fileName);
        String fullFileName;
        BufferedImage image;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            fullFileName = folder.getCanonicalPath() + File.separator + fileName;
            image = ImageIO.read(new File(fullFileName));
        } catch (IOException ex) {
            setCursor(Cursor.getDefaultCursor());
            return;
        }

        browserPanel1.showImage(image);
        setCursor(Cursor.getDefaultCursor());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jpanelImage = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        btnFolder = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jButtonLeft = new javax.swing.JButton();
        jButtonRight = new javax.swing.JButton();
        jButtonBottom = new javax.swing.JButton();
        jButtonTop = new javax.swing.JButton();
        jButtonAdjust = new javax.swing.JButton();
        jPanelBrowser = new javax.swing.JPanel();
        jSplitPaneBrowser = new javax.swing.JSplitPane();
        jPanelFiles = new javax.swing.JPanel();
        jScrollPaneFiles = new javax.swing.JScrollPane();
        listFiles = new javax.swing.JList<String>();
        jPanel4 = new javax.swing.JPanel();
        jPanelImageInfo = new javax.swing.JPanel();
        lbFileName = new javax.swing.JLabel();
        jScrollPaneImage = new javax.swing.JScrollPane();
        browserPanel1 = new ancestris.modules.releve.editor.BrowserPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        birthEditor = new ancestris.modules.releve.editor.ReleveEditor();
        marriageEditor = new ancestris.modules.releve.editor.ReleveEditor();
        deathEditor = new ancestris.modules.releve.editor.ReleveEditor();
        miscEditor = new ancestris.modules.releve.editor.ReleveEditor();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jpanelImage.setLayout(new java.awt.BorderLayout());

        jPanel1.setPreferredSize(new java.awt.Dimension(0, 40));
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnFolder.setText(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.btnFolder.text")); // NOI18N
        btnFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFolderActionPerformed(evt);
            }
        });
        jPanel1.add(btnFolder);
        jPanel1.add(jSeparator1);

        jButtonLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Back.png"))); // NOI18N
        jButtonLeft.setToolTipText(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.jButtonLeft.toolTipText")); // NOI18N
        jButtonLeft.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLeftActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonLeft);

        jButtonRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Forward.png"))); // NOI18N
        jButtonRight.setToolTipText("Next"); // NOI18N
        jButtonRight.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRightActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonRight);

        jButtonBottom.setText(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.jButtonBottom.text")); // NOI18N
        jButtonBottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBottomActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonBottom);

        jButtonTop.setText(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.jButtonTop.text")); // NOI18N
        jButtonTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTopActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonTop);

        jButtonAdjust.setText(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.jButtonAdjust.text")); // NOI18N
        jButtonAdjust.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAdjustActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonAdjust);

        jpanelImage.add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanelBrowser.setLayout(new java.awt.BorderLayout());

        jPanelFiles.setMinimumSize(new java.awt.Dimension(150, 23));
        jPanelFiles.setPreferredSize(new java.awt.Dimension(150, 497));
        jPanelFiles.setLayout(new java.awt.BorderLayout());

        listFiles.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "<choose a folder>" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        listFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listFiles.setMaximumSize(new java.awt.Dimension(500, 16));
        listFiles.setMinimumSize(new java.awt.Dimension(150, 16));
        listFiles.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                listFilesKeyPressed(evt);
            }
        });
        jScrollPaneFiles.setViewportView(listFiles);

        jPanelFiles.add(jScrollPaneFiles, java.awt.BorderLayout.CENTER);

        jSplitPaneBrowser.setLeftComponent(jPanelFiles);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanelImageInfo.setPreferredSize(new java.awt.Dimension(585, 20));

        lbFileName.setText(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.lbFileName.text")); // NOI18N

        javax.swing.GroupLayout jPanelImageInfoLayout = new javax.swing.GroupLayout(jPanelImageInfo);
        jPanelImageInfo.setLayout(jPanelImageInfoLayout);
        jPanelImageInfoLayout.setHorizontalGroup(
            jPanelImageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelImageInfoLayout.createSequentialGroup()
                .addComponent(lbFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(70, Short.MAX_VALUE))
        );
        jPanelImageInfoLayout.setVerticalGroup(
            jPanelImageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelImageInfoLayout.createSequentialGroup()
                .addComponent(lbFileName)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel4.add(jPanelImageInfo, java.awt.BorderLayout.NORTH);

        javax.swing.GroupLayout browserPanel1Layout = new javax.swing.GroupLayout(browserPanel1);
        browserPanel1.setLayout(browserPanel1Layout);
        browserPanel1Layout.setHorizontalGroup(
            browserPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 270, Short.MAX_VALUE)
        );
        browserPanel1Layout.setVerticalGroup(
            browserPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 365, Short.MAX_VALUE)
        );

        jScrollPaneImage.setViewportView(browserPanel1);

        jPanel4.add(jScrollPaneImage, java.awt.BorderLayout.CENTER);

        jSplitPaneBrowser.setRightComponent(jPanel4);

        jPanelBrowser.add(jSplitPaneBrowser, java.awt.BorderLayout.CENTER);

        jpanelImage.add(jPanelBrowser, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jpanelImage);

        birthEditor.setPreferredSize(new java.awt.Dimension(180, 100));
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.birthEditor.TabConstraints.tabTitle"), birthEditor); // NOI18N

        marriageEditor.setPreferredSize(new java.awt.Dimension(180, 100));
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.marriageEditor.TabConstraints.tabTitle"), marriageEditor); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.deathEditor.TabConstraints.tabTitle"), deathEditor); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.miscEditor.TabConstraints.tabTitle"), miscEditor); // NOI18N

        jSplitPane1.setRightComponent(jTabbedPane1);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFolderActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (folder != null) {
            //saveVotes();
            fc.setCurrentDirectory(folder);
        }
        int fcr = fc.showDialog(this, "Select folder");
        if (fcr != JFileChooser.APPROVE_OPTION)
            return;
        String folderName;
        try {
            folder = fc.getSelectedFile();
            folderName = folder.getCanonicalPath();
        } catch (IOException ex) {
            return;
        }
        btnFolder.setText(folderName);
        populateImageList(folder);
}//GEN-LAST:event_btnFolderActionPerformed

    private void jButtonLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLeftActionPerformed
         if ( browserPanel1.isleftSideVisible()  ) {
             int currentIndex = listFiles.getSelectedIndex();

             if (currentIndex > 0) {
                 listFiles.setSelectedIndex(currentIndex - 1);
             } else {
                 listFiles.setSelectedIndex(listFiles.getModel().getSize() - 1);
                 Toolkit.getDefaultToolkit().beep();
             }
             String fileName = listFiles.getSelectedValue().toString();
             refreshShownImage(fileName, false);
             jScrollPaneImage.getVerticalScrollBar().setValue(0);
             browserPanel1.moveToRight();
             browserPanel1.moveToBottom();
        } else {
             browserPanel1.moveToLeft();
        }
}//GEN-LAST:event_jButtonLeftActionPerformed

    private void jButtonRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRightActionPerformed

        if ( browserPanel1.isRightSideVisible()  ) {
            int currentIndex = listFiles.getSelectedIndex();
            if (currentIndex < listFiles.getModel().getSize() - 1) {
                listFiles.setSelectedIndex(currentIndex + 1);
            } else {
                listFiles.setSelectedIndex(0);
                Toolkit.getDefaultToolkit().beep();
            }
            String fileName = listFiles.getSelectedValue().toString();
            refreshShownImage(fileName, false);
            jScrollPaneImage.getVerticalScrollBar().setValue(0);
             browserPanel1.moveToLeft();
             browserPanel1.moveToTop();
        } else {
            browserPanel1.moveToRight();
        }
}//GEN-LAST:event_jButtonRightActionPerformed

    private void listFilesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listFilesKeyPressed
        /* XXX: proper constants */
}//GEN-LAST:event_listFilesKeyPressed

    private void jButtonBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBottomActionPerformed
        // show down left corner
        //JScrollBar vertical = jScrollPaneImage.getVerticalScrollBar();
        if( browserPanel1.isBottomSideVisible()) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            browserPanel1.moveToBottom();
        }


    }//GEN-LAST:event_jButtonBottomActionPerformed

    private void jButtonTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTopActionPerformed
        // show up left corner
        //JScrollBar vertical = jScrollPaneImage.getVerticalScrollBar();
        if( browserPanel1.isTopSideVisible() ) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            browserPanel1.moveToTop();
        }
    }//GEN-LAST:event_jButtonTopActionPerformed

    private void jButtonAdjustActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAdjustActionPerformed
        browserPanel1.adjustAreaColor();
    }//GEN-LAST:event_jButtonAdjustActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.releve.editor.ReleveEditor birthEditor;
    private ancestris.modules.releve.editor.BrowserPanel browserPanel1;
    private javax.swing.JButton btnFolder;
    private ancestris.modules.releve.editor.ReleveEditor deathEditor;
    private javax.swing.JButton jButtonAdjust;
    private javax.swing.JButton jButtonBottom;
    private javax.swing.JButton jButtonLeft;
    private javax.swing.JButton jButtonRight;
    private javax.swing.JButton jButtonTop;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelBrowser;
    private javax.swing.JPanel jPanelFiles;
    private javax.swing.JPanel jPanelImageInfo;
    private javax.swing.JScrollPane jScrollPaneFiles;
    private javax.swing.JScrollPane jScrollPaneImage;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPaneBrowser;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel jpanelImage;
    private javax.swing.JLabel lbFileName;
    private javax.swing.JList listFiles;
    private ancestris.modules.releve.editor.ReleveEditor marriageEditor;
    private ancestris.modules.releve.editor.ReleveEditor miscEditor;
    // End of variables declaration//GEN-END:variables

}
