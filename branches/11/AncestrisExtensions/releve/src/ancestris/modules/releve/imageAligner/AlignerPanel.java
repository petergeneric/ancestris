package ancestris.modules.releve.imageAligner;

import ancestris.util.swing.FileChooserBuilder;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */
public class AlignerPanel extends javax.swing.JPanel implements ImagePanel.CoordinateListener {

    private File currentImageDirectory = null;
    private File outputDirectory = null;
    private static final String INPUT_IMAGE_DIRECTORY_KEY = "AlignerInputImageDirectory";
    private static final String OUTPUT_IMAGE_DIRECTORY_KEY= "AlignerOutputImageDirectory";

    /**
     * Creates new form ImagePanel
     */
    public AlignerPanel() {
        initComponents();
        
        imagePanel.setCoordinateListener(this);

        ListSelectionListener lsl = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (listFiles.getSelectedValue() != null) {
                    String fileName = listFiles.getSelectedValue();
                    showImage(fileName, false);
                }
            }
        };
        listFiles.addListSelectionListener(lsl);

        jPanelImage.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK), "previousImage");
        jPanelImage.getActionMap().put("previousImage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jButtonLeftActionPerformed(null);
            }
        });

        jPanelImage.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK), "nextImage");
        jPanelImage.getActionMap().put("nextImage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jButtonRightActionPerformed(null);
            }
        });

				// Image browser
        // currentImageDirectory
        String imageDirectory = NbPreferences.forModule(AlignerPanel.class).get(INPUT_IMAGE_DIRECTORY_KEY, "");
        File f = new File(imageDirectory);
        if (f.exists()) {
            currentImageDirectory = f;
            populateImageList(currentImageDirectory);
        }

        // outpu directory 
        String outputDirectoryString = NbPreferences.forModule(AlignerPanel.class).get(OUTPUT_IMAGE_DIRECTORY_KEY, "");
        File outpuFile = new File(outputDirectoryString);
        if (outpuFile.exists()) {
            outputDirectory = outpuFile;
        }
        
        updateNameValue();

    }

    /**
     * sauvegarde de la configuration a la fermeture du composant
     */
    public void componentClosed() {

        // je memorise le nom du repertoire courant des images
        String imageDirectory = "";
        if (currentImageDirectory != null) {
            try {
                imageDirectory = currentImageDirectory.getCanonicalPath();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            NbPreferences.forModule(AlignerPanel.class).put(INPUT_IMAGE_DIRECTORY_KEY, imageDirectory);
        }
        if (outputDirectory != null) {
            try {
                imageDirectory = outputDirectory.getCanonicalPath();
            } catch (IOException ex) {
                imageDirectory = "";
                Exceptions.printStackTrace(ex);
            }
            NbPreferences.forModule(AlignerPanel.class).put(OUTPUT_IMAGE_DIRECTORY_KEY, imageDirectory);
        }
    }

    private void populateImageList(File folder) {
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif");
            }
        });
        if( files != null ) {
            String[] fileNames = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                fileNames[i] = files[i].getName();
            }
            Arrays.sort(fileNames, String.CASE_INSENSITIVE_ORDER);
            listFiles.setListData(fileNames);
        } else {
            System.err.println("BrowserPanel.populateImageList folder.listFiles=null for  folder="+folder.getAbsolutePath());            
        }
    }

    private void showImage(String fileName, boolean forceRefesh) {
        if (lbFileName.getText().equals(fileName) && !forceRefesh) {
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        imagePanel.showImage(null);
        lbFileName.setText("");
        try {
            String fullFileName = currentImageDirectory.getCanonicalPath() + File.separator + fileName;
            BufferedImage image = ImageIO.read(new File(fullFileName));
            imagePanel.showImage(image);
            lbFileName.setText(fileName);
        } catch (IOException ex) {
            System.err.println("AlignerPanel.showImage error ="+ ex.getMessage()); 
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelButton = new javax.swing.JPanel();
        btnFolder = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jButtonLeft = new javax.swing.JButton();
        jButtonRight = new javax.swing.JButton();
        jTextFieldMouseCoords = new javax.swing.JTextField();
        jTextFieldAlignCoords = new javax.swing.JTextField();
        jButtonOutputFolder = new javax.swing.JButton();
        jTextFieldNameFormat = new javax.swing.JTextField();
        jTextFieldNameIndex = new javax.swing.JTextField();
        jTextFieldNameValue = new javax.swing.JTextField();
        jPanelImage = new javax.swing.JPanel();
        jSplitPaneBrowser = new javax.swing.JSplitPane();
        jPanelFiles = new javax.swing.JPanel();
        jScrollPaneFiles = new javax.swing.JScrollPane();
        listFiles = new javax.swing.JList<String>();
        jPanel4 = new javax.swing.JPanel();
        jPanelImageInfo = new javax.swing.JPanel();
        lbFileName = new javax.swing.JLabel();
        jScrollPaneImage = new javax.swing.JScrollPane();
        imagePanel = new ancestris.modules.releve.imageAligner.ImagePanel();

        setLayout(new java.awt.BorderLayout());

        jPanelButton.setPreferredSize(new java.awt.Dimension(0, 40));
        jPanelButton.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        org.openide.awt.Mnemonics.setLocalizedText(btnFolder, org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.btnFolder.text")); // NOI18N
        btnFolder.setToolTipText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.btnFolder.toolTipText")); // NOI18N
        btnFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFolderActionPerformed(evt);
            }
        });
        jPanelButton.add(btnFolder);
        jPanelButton.add(jSeparator1);

        jButtonLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Back.png"))); // NOI18N
        jButtonLeft.setToolTipText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jButtonLeft.toolTipText")); // NOI18N
        jButtonLeft.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonLeft.setPreferredSize(new java.awt.Dimension(52, 27));
        jButtonLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLeftActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonLeft);

        jButtonRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Forward.png"))); // NOI18N
        jButtonRight.setToolTipText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jButtonRight.toolTipText")); // NOI18N
        jButtonRight.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRight.setPreferredSize(new java.awt.Dimension(52, 27));
        jButtonRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRightActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonRight);

        jTextFieldMouseCoords.setText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jTextFieldMouseCoords.text")); // NOI18N
        jTextFieldMouseCoords.setToolTipText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jTextFieldMouseCoords.toolTipText")); // NOI18N
        jTextFieldMouseCoords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldMouseCoordsActionPerformed(evt);
            }
        });
        jPanelButton.add(jTextFieldMouseCoords);

        jTextFieldAlignCoords.setText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jTextFieldAlignCoords.text")); // NOI18N
        jTextFieldAlignCoords.setToolTipText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jTextFieldAlignCoords.toolTipText")); // NOI18N
        jPanelButton.add(jTextFieldAlignCoords);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonOutputFolder, org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jButtonOutputFolder.text")); // NOI18N
        jButtonOutputFolder.setToolTipText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jButtonOutputFolder.toolTipText")); // NOI18N
        jButtonOutputFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOutputFolderActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonOutputFolder);

        jTextFieldNameFormat.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldNameFormat.setText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jTextFieldNameFormat.text")); // NOI18N
        jTextFieldNameFormat.setToolTipText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jTextFieldNameFormat.toolTipText")); // NOI18N
        jTextFieldNameFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldNameFormatActionPerformed(evt);
            }
        });
        jTextFieldNameFormat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldNameFormatKeyReleased(evt);
            }
        });
        jPanelButton.add(jTextFieldNameFormat);

        jTextFieldNameIndex.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldNameIndex.setText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jTextFieldNameIndex.text")); // NOI18N
        jTextFieldNameIndex.setToolTipText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jTextFieldNameIndex.toolTipText")); // NOI18N
        jTextFieldNameIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldNameIndexActionPerformed(evt);
            }
        });
        jTextFieldNameIndex.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextFieldNameIndexKeyReleased(evt);
            }
        });
        jPanelButton.add(jTextFieldNameIndex);

        jTextFieldNameValue.setEditable(false);
        jTextFieldNameValue.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldNameValue.setText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jTextFieldNameValue.text")); // NOI18N
        jTextFieldNameValue.setToolTipText(org.openide.util.NbBundle.getMessage(AlignerPanel.class, "AlignerPanel.jTextFieldNameValue.toolTipText")); // NOI18N
        jTextFieldNameValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldNameValueActionPerformed(evt);
            }
        });
        jPanelButton.add(jTextFieldNameValue);

        add(jPanelButton, java.awt.BorderLayout.NORTH);

        jPanelImage.setLayout(new java.awt.BorderLayout());

        jPanelFiles.setMinimumSize(new java.awt.Dimension(150, 23));
        jPanelFiles.setPreferredSize(new java.awt.Dimension(150, 497));
        jPanelFiles.setLayout(new java.awt.BorderLayout());

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

        javax.swing.GroupLayout jPanelImageInfoLayout = new javax.swing.GroupLayout(jPanelImageInfo);
        jPanelImageInfo.setLayout(jPanelImageInfoLayout);
        jPanelImageInfoLayout.setHorizontalGroup(
            jPanelImageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelImageInfoLayout.createSequentialGroup()
                .addComponent(lbFileName, javax.swing.GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelImageInfoLayout.setVerticalGroup(
            jPanelImageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lbFileName, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
        );

        jPanel4.add(jPanelImageInfo, java.awt.BorderLayout.NORTH);

        imagePanel.setMinimumSize(new java.awt.Dimension(100, 100));

        javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(imagePanel);
        imagePanel.setLayout(imagePanelLayout);
        imagePanelLayout.setHorizontalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 736, Short.MAX_VALUE)
        );
        imagePanelLayout.setVerticalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 475, Short.MAX_VALUE)
        );

        jScrollPaneImage.setViewportView(imagePanel);

        jPanel4.add(jScrollPaneImage, java.awt.BorderLayout.CENTER);

        jSplitPaneBrowser.setRightComponent(jPanel4);

        jPanelImage.add(jSplitPaneBrowser, java.awt.BorderLayout.CENTER);

        add(jPanelImage, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFolderActionPerformed

        File file = new FileChooserBuilder(AlignerPanel.class)
                .setDirectoriesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(getClass(), "BrowserPanel.btnFolder.text"))
                .setApproveText(NbBundle.getMessage(getClass(), "BrowserOptionsPanel.dialogTitle.ok"))
                .setSelectedFile(currentImageDirectory)
                .setFileHiding(true)
                .showOpenDialog();
        
        if (file != null) {
            String folderName;
            try {
                currentImageDirectory = file;
                folderName = currentImageDirectory.getCanonicalPath();
            } catch (IOException ex) {
                return;
            }
            btnFolder.setText(folderName);
            populateImageList(currentImageDirectory);
        }
    }//GEN-LAST:event_btnFolderActionPerformed

    private void jButtonLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLeftActionPerformed
        if (imagePanel.isleftSideVisible()) {
            int currentIndex = listFiles.getSelectedIndex();

            if (currentIndex > 0) {
                listFiles.setSelectedIndex(currentIndex - 1);
            } else {
                listFiles.setSelectedIndex(listFiles.getModel().getSize() - 1);
                Toolkit.getDefaultToolkit().beep();
            }
                        if (listFiles == null || listFiles.getSelectedValue() == null) {
                return;
            }
            String fileName = listFiles.getSelectedValue();
            showImage(fileName, false);
            jScrollPaneImage.getVerticalScrollBar().setValue(0);
            imagePanel.moveToRight();
            imagePanel.moveToBottom();
        } else {
            imagePanel.moveToLeft();
        }
    }//GEN-LAST:event_jButtonLeftActionPerformed

    private void jButtonRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRightActionPerformed

        if (imagePanel.isRightSideVisible()) {
            int currentIndex = listFiles.getSelectedIndex();
            if (currentIndex < listFiles.getModel().getSize() - 1) {
                listFiles.setSelectedIndex(currentIndex + 1);
            } else {
                listFiles.setSelectedIndex(0);
                Toolkit.getDefaultToolkit().beep();
            }
            if (listFiles == null || listFiles.getSelectedValue() == null) {
                return;
            }
            String fileName = listFiles.getSelectedValue();
            showImage(fileName, false);
            jScrollPaneImage.getVerticalScrollBar().setValue(0);
            imagePanel.moveToLeft();
            imagePanel.moveToTop();
        } else {
            imagePanel.moveToRight();
        }
    }//GEN-LAST:event_jButtonRightActionPerformed

    private void listFilesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listFilesKeyPressed
        /* XXX: proper constants */
    }//GEN-LAST:event_listFilesKeyPressed

    private void jTextFieldMouseCoordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldMouseCoordsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMouseCoordsActionPerformed

    private void jButtonOutputFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOutputFolderActionPerformed
        File file = new FileChooserBuilder(AlignerPanel.class)
                .setDirectoriesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(getClass(), "AlignerPanel.jButtonOutputFolder.text"))
                .setApproveText(NbBundle.getMessage(getClass(), "BrowserOptionsPanel.dialogTitle.ok"))
                .setSelectedFile(outputDirectory)
                .setFileHiding(true)
                .showOpenDialog();
        
        if (file != null) {
            String folderName;
            try {
                outputDirectory = file;
                folderName = outputDirectory.getCanonicalPath();
            } catch (IOException ex) {
                return;
            }
            jButtonOutputFolder.setText(folderName);
        }
    
    }//GEN-LAST:event_jButtonOutputFolderActionPerformed

    private void jTextFieldNameFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldNameFormatActionPerformed
        updateNameValue();
    }//GEN-LAST:event_jTextFieldNameFormatActionPerformed

    private void jTextFieldNameIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldNameIndexActionPerformed
        updateNameValue();
    }//GEN-LAST:event_jTextFieldNameIndexActionPerformed

    private void jTextFieldNameFormatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldNameFormatKeyReleased
        updateNameValue();
    }//GEN-LAST:event_jTextFieldNameFormatKeyReleased

    private void jTextFieldNameIndexKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldNameIndexKeyReleased
        updateNameValue();
    }//GEN-LAST:event_jTextFieldNameIndexKeyReleased

    private void jTextFieldNameValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldNameValueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldNameValueActionPerformed

    private void updateNameValue() {

        if ( jTextFieldNameIndex.getText().isEmpty()) {
            jTextFieldNameValue.setText(String.format("%s-", jTextFieldNameFormat.getText())); 
        } else {
            try {
                jTextFieldNameValue.setText(String.format("%s-%03d", jTextFieldNameFormat.getText(), Integer.parseInt(jTextFieldNameIndex.getText())));        
            } catch (Exception e) {
                jTextFieldNameValue.setText(String.format("%s-", jTextFieldNameFormat.getText())); 
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFolder;
    private ancestris.modules.releve.imageAligner.ImagePanel imagePanel;
    private javax.swing.JButton jButtonLeft;
    private javax.swing.JButton jButtonOutputFolder;
    private javax.swing.JButton jButtonRight;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelButton;
    private javax.swing.JPanel jPanelFiles;
    private javax.swing.JPanel jPanelImage;
    private javax.swing.JPanel jPanelImageInfo;
    private javax.swing.JScrollPane jScrollPaneFiles;
    private javax.swing.JScrollPane jScrollPaneImage;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPaneBrowser;
    private javax.swing.JTextField jTextFieldAlignCoords;
    private javax.swing.JTextField jTextFieldMouseCoords;
    private javax.swing.JTextField jTextFieldNameFormat;
    private javax.swing.JTextField jTextFieldNameIndex;
    private javax.swing.JTextField jTextFieldNameValue;
    private javax.swing.JLabel lbFileName;
    private javax.swing.JList<String> listFiles;
    // End of variables declaration//GEN-END:variables

    @Override
    public void updateMouseCoordinates(ImagePanel.CoordImage coords) {
        
        jTextFieldMouseCoords.setText(String.format("%d / %d", coords.x , coords.y));
        
    }

    @Override
    public void updateAlignCoordinates(ImagePanel.CoordImage coords) {
        if (coords != null) {
            jTextFieldAlignCoords.setText(String.format("%d / %d", coords.x , coords.y));
        } else {
            jTextFieldAlignCoords.setText(" / ");
        }
    }

    @Override
    public void saveCurrentImage(BufferedImage currentImage) {
        
        try {
            String fileName = listFiles.getSelectedValue();
            
            fileName = jTextFieldNameValue.getText() + ".jpg";
            String fullFileName = outputDirectory.getCanonicalPath() + File.separator + fileName;
            File file= new File(fullFileName);
            
            //ImageIO.write(currentImage, "jpg", file);
            
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(0.8f);

            ImageOutputStream  outputStream =  ImageIO.createImageOutputStream(file);
            jpgWriter.setOutput(outputStream);            
            IIOImage outputImage = new IIOImage(currentImage, null, null);
            jpgWriter.write(null, outputImage, jpgWriteParam);
            outputStream.flush();
            outputStream.close();
            jpgWriter.dispose();
            
            // j'incremente le compteur
            int newIndex = Integer.parseInt(jTextFieldNameIndex.getText()) +1;
            jTextFieldNameIndex.setText( String.valueOf(newIndex));
            updateNameValue();
            
            // select next image
            int currentIndex = listFiles.getSelectedIndex();
            if (currentIndex < listFiles.getModel().getSize() - 1) {
                listFiles.setSelectedIndex(currentIndex + 1);
            } else {
                listFiles.setSelectedIndex(0);
                Toolkit.getDefaultToolkit().beep();
            }
            fileName = listFiles.getSelectedValue();
            showImage(fileName, false);
            jScrollPaneImage.getVerticalScrollBar().setValue(0);
            
            
        } catch (IOException ex) {
            System.err.println("AlignerPanel.showImage error ="+ ex.getMessage()); 
            Toolkit.getDefaultToolkit().beep();
        }
    }

    void frameUpdated() {
        imagePanel.frameUpdated();
    }
}
