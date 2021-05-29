package ancestris.modules.releve.imageBrowser;

import ancestris.util.swing.FileChooserBuilder;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */
public class BrowserPanel extends javax.swing.JPanel {

    private File currentImageDirectory = null;

    /**
     * Creates new form ImagePanel
     */
    public BrowserPanel() {
        initComponents();

        ListSelectionListener lsl = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (listFiles.getSelectedValue() != null) {
                    String fileName = listFiles.getSelectedValue().toString();
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

        jPanelImage.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK), "downLeftCornerImage");
        jPanelImage.getActionMap().put("downLeftCornerImage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jButtonBottomActionPerformed(null);
            }
        });

        jPanelImage.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK), "upLeftCornerImage");
        jPanelImage.getActionMap().put("upLeftCornerImage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jButtonTopActionPerformed(null);
            }
        });

        // Image browser
        // currentImageDirectory
        String imageDirectory = NbPreferences.forModule(BrowserPanel.class).get("ImageDirectory", "");
        File f = new File(imageDirectory);
        if (f.exists()) {
            currentImageDirectory = f;
            populateImageList(currentImageDirectory);
        }

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
            NbPreferences.forModule(BrowserPanel.class).put("ImageDirectory", imageDirectory);
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

        } finally {
            setCursor(Cursor.getDefaultCursor());
        }

    }

    public void showImage(final String city, final String cote, final String page) {

        imagePanel.showImage(null);
        lbFileName.setText("");
        listFiles.clearSelection();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final Cursor browserPanelCursor = getCursor();
                final Cursor imagePanelCursor = imagePanel.getCursor();
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                imagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                try {

                    //je cherche le repertoire
                    File imageFile = findImage(city, cote, page);
                    if (imageFile != null) {

                        populateImageList(imageFile.getParentFile());
                        btnFolder.setText(imageFile.getParent());
                        BufferedImage image = ImageIO.read(imageFile);
                        imagePanel.showImage(image);
                        if (image != null) {
                            currentImageDirectory = imageFile.getParentFile();
                            lbFileName.setText(imageFile.getName());
                            listFiles.setSelectedValue(imageFile.getName(), true);
                        } else {
                            lbFileName.setText("");
                            listFiles.clearSelection();
                        }

                    }
                } catch (IOException ex) {

                } finally {
                    setCursor(browserPanelCursor);
                    imagePanel.setCursor(imagePanelCursor);
                }
            }
        });

    }
    
    public void selectImage(final String city, final String cote, final String page) {

        if (listFiles == null || (cote.trim().isEmpty() && page.trim().isEmpty())) {
            // leave the current image selected to be selected (do not clear)
            //imagePanel.showImage(null);
            //lbFileName.setText("");
            //listFiles.clearSelection();
            return;
        }

        File imageFile = findImage(city, cote, page);
        if (imageFile != null) {
            listFiles.setSelectedValue(imageFile.getName(), true);
            jScrollPaneImage.getVerticalScrollBar().setValue(0);
            imagePanel.moveToLeft();
            imagePanel.moveToTop();
        }
    }

    private File findImage(String city, String cote, String page) {
        // je determine si page contient seulement un numéro 
        boolean isNumeric = true;
        for (char c : page.toCharArray()) {
            if (!Character.isDigit(c)) {
                isNumeric = false;
                break;
            }
        }
        // j'extrait le numéro
        int pageNum = parsePageInFreeComment(page);

        DirectoryCityFilter cityFilter = new DirectoryCityFilter(city);
        DirectoryCoteFilter coteFilter = new DirectoryCoteFilter(cote);
        FilePageNameFilter pageNameFilter = new FilePageNameFilter(page);
        FilePageNumberFilter pageNumberFilter = new FilePageNumberFilter(pageNum);

        //je cherche l'image
        File imageFile = null;
        if (!cote.isEmpty()) {
            // si page  n'est pas un nombre, je cherche d'abord un fichier contenant le nom de la page
            if (isNumeric == false) {
                for (File directory : BrowserOptionsPanel.ImageDirectoryModel.getModel().getImageBrowserDirectories()) {
                    imageFile = findImage(directory, cityFilter, coteFilter, pageNameFilter);
                    if (imageFile != null) {
                        break;
                    }
                }
            }
            if (imageFile == null) {
                for (File directory : BrowserOptionsPanel.ImageDirectoryModel.getModel().getImageBrowserDirectories()) {
                    imageFile = findImage(directory, cityFilter, coteFilter, pageNumberFilter);
                    if (imageFile != null) {
                        break;
                    }
                }
            }
        }

        if (imageFile == null) {
            // je cherche sans la cote
            if (isNumeric == false) {
                // je cherche une image contenant le nom de la page 
                for (File directory : BrowserOptionsPanel.ImageDirectoryModel.getModel().getImageBrowserDirectories()) {
                    imageFile = findImage(directory, cityFilter, pageNameFilter);
                    if (imageFile != null) {
                        break;
                    }
                }
            }
            if (imageFile == null) {
                // je cherche une image contenant le nuéro de la page 
                for (File directory : BrowserOptionsPanel.ImageDirectoryModel.getModel().getImageBrowserDirectories()) {
                    imageFile = findImage(directory, cityFilter, pageNumberFilter);
                    if (imageFile != null) {
                        break;
                    }
                }
            }
        }

        return imageFile;
    }

    private File findImage(File directory, DirectoryCityFilter cityFilter, DirectoryCoteFilter coteFilter, FilePageFilter pageFilter) {
        File result = null;

        if (directory.isDirectory()) {
            // je cherche la ville dans le repertoire courant
            File cityDirectories[] = directory.listFiles(cityFilter);
            for (File citySubDir : cityDirectories) {
                // je cherche la cote dans les sous repertoires
                result = findImage(citySubDir, coteFilter, pageFilter);
                if (result != null) {
                    break;
                }
            }

            // je cherche la ville et la cote dans les sous-repertoires
            if (result == null) {
                for (File subdir : directory.listFiles(new DirectoryFilter())) {
                    result = findImage(subdir, cityFilter, coteFilter, pageFilter);
                    if (result != null) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    private File findImage(File directory, DirectoryCityFilter cityFilter, FilePageFilter pageFilter) {
        File result = null;

        if (directory.isDirectory()) {
            // je cherche la ville dans le repertoire courant
            File cityDirectories[] = directory.listFiles(cityFilter);
            for (File citySubDir : cityDirectories) {
                // je cherche l'image dans le sous repertoire
                result = findImage(citySubDir, pageFilter);
                if (result != null) {
                    break;
                }
            }

            // je cherche la ville dans les sous-repertoires
            if (result == null) {
                for (File subdir : directory.listFiles(new DirectoryFilter())) {
                    result = findImage(subdir, cityFilter, pageFilter);
                    if (result != null) {
                        break;
                    }
                }
            }

        }

        return result;
    }

    private File findImage(File directory, DirectoryCoteFilter coteFilter, FilePageFilter pageFilter) {
        File result = null;

        if (directory.isDirectory()) {
            // je cherche la cote dans le repertoire courant
            File coteDirectories[] = directory.listFiles(coteFilter);
            for (File coteSubDir : coteDirectories) {
                // je cherche l'image dans les sous repertoires de la cote             
                result = findImage(coteSubDir, pageFilter);
                if (result != null) {
                    break;
                }
            }

            // je cherche la cote dans les sous-repertoires
            if (result == null) {
                for (File subdir : directory.listFiles(new DirectoryFilter())) {
                    result = findImage(subdir, coteFilter, pageFilter);
                    if (result != null) {
                        break;
                    }
                }
            }

        }

        return result;
    }

    private File findImage(File directory, FilePageFilter pageFilter) {
        File result = null;

        if (directory.isDirectory()) {
            // je cherche l'image dans le repertoire courant
            File[] files = directory.listFiles(pageFilter);
            if (files != null && files.length > 0) {
                result = files[0];
            }
            if (result == null) {
                // je cherche l'image dans les sous repertoires
                File cityDirectories[] = directory.listFiles(new DirectoryFilter());
                for (File subDir : cityDirectories) {
                    result = findImage(subDir, pageFilter);
                    if (result != null) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    static private class DirectoryCityFilter implements FileFilter {

        String city;

        public DirectoryCityFilter(String reference) {
            this.city = reference.toLowerCase();
        }

        @Override
        public boolean accept(File dir) {
            return dir.isDirectory() && dir.getName().toLowerCase().contains(city);
        }
    }

    static private class DirectoryCoteFilter implements FileFilter {

        String cote;

        public DirectoryCoteFilter(String reference) {
            this.cote = reference.toLowerCase();
        }

        @Override
        public boolean accept(File dir) {
            return dir.isDirectory() && dir.getName().toLowerCase().contains(cote);
        }
    }

    static private class DirectoryFilter implements FileFilter {

        public DirectoryFilter() {
        }

        @Override
        public boolean accept(File dir) {
            return dir.isDirectory();
        }
    }

    static abstract private class FilePageFilter implements FileFilter {

        static final String[] EXTENSIONS = new String[]{
            "jpeg", "jpg", "png", "gif", "bmp", "tiff", "tif"
        };

    }

    static private class FilePageNameFilter extends FilePageFilter {

        String pageName;

        public FilePageNameFilter(String pageName) {
            this.pageName = pageName.toLowerCase();
        }

        @Override
        public boolean accept(File dir) {
            if (dir.isFile()) {
                String name = dir.getName().toLowerCase();

                boolean extension = false;
                for (final String ext : EXTENSIONS) {
                    if (name.endsWith("." + ext)) {
                        extension = true;
                        break;
                    }
                }

                if (extension) {
                    return name.toLowerCase().contains(pageName);
                } else {
                    return false;
                }
            } else {
                // ce n'est pas un fichier
                return false;
            }

        }
    }

    static private class FilePageNumberFilter extends FilePageFilter {

        int pageNumber;

        public FilePageNumberFilter(int searchedPage) {
            this.pageNumber = searchedPage;
        }

        @Override
        public boolean accept(File dir) {
            if (dir.isFile()) {
                String name = dir.getName().toLowerCase();

                boolean extension = false;
                for (final String ext : EXTENSIONS) {
                    if (name.endsWith("." + ext)) {
                        extension = true;
                        break;
                    }
                }

                if (extension) {
                    int dotIndex = name.lastIndexOf('.');
                    if (dotIndex > 0) {
                        name = name.substring(0, dotIndex);
                    }

                    int page = parsePageInFileName(name);
                    if (page != pageNumber) {
                        int lastSeparatorPos = name.lastIndexOf('-');
                        if (lastSeparatorPos != -1) {
                            page = parsePageInFileName(name.substring(0, lastSeparatorPos));
                        }
                    }
                    if (page != pageNumber) {
                        int firstSeparatorPos = name.indexOf('-');
                        if (firstSeparatorPos != -1) {
                            int secondSeparatorPos = name.indexOf('-',firstSeparatorPos+1);
                            if (secondSeparatorPos != -1) {
                                page = parsePageInFileName(name.substring(firstSeparatorPos+1, secondSeparatorPos));
                            }
                        }
                    }
                    return page == pageNumber;
                } else {
                    return false;
                }
            } else {
                // ce n'est pas un fichier
                return false;
            }

        }
    }

    static private int parsePageInFreeComment(String freeComment) {
        int i;
        int page = -1;
        // Je recherche  le dernier chiffre en partant de la droite
        for (i = freeComment.length() - 1; i >= 0 && freeComment.charAt(i) >= '0' && freeComment.charAt(i) <= '9'; i--) {
        }
        i++;
        // je cree le format pour préserver les zeros à gauche
        if (i < freeComment.length()) {
            try {
                page = new Integer(freeComment.substring(i, freeComment.length())).intValue();
            } catch (NumberFormatException ex) {
                page = -1;
            }
        }
        return page;
    }
    
    static private int parsePageInFileName(String name) {
        int i;
        int page = -1;
        // Je recherche  le dernier chiffre en partant de la droite
        for (i = name.length() - 1; i >= 0 && name.charAt(i) >= '0' && name.charAt(i) <= '9'; i--) {
        }
        i++;
        // je cree le format pour préserver les zeros à gauche
        if (i < name.length()) {
            try {
                page = new Integer(name.substring(i, name.length())).intValue();
            } catch (NumberFormatException ex) {
                page = -1;
            }
        }
        return page;
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
        jButtonBottom = new javax.swing.JButton();
        jButtonTop = new javax.swing.JButton();
        jButtonAdjust = new javax.swing.JButton();
        jPanelImage = new javax.swing.JPanel();
        jSplitPaneBrowser = new javax.swing.JSplitPane();
        jPanelFiles = new javax.swing.JPanel();
        jScrollPaneFiles = new javax.swing.JScrollPane();
        listFiles = new javax.swing.JList<String>();
        jPanel4 = new javax.swing.JPanel();
        jPanelImageInfo = new javax.swing.JPanel();
        lbFileName = new javax.swing.JLabel();
        jScrollPaneImage = new javax.swing.JScrollPane();
        imagePanel = new ancestris.modules.releve.imageBrowser.ImagePanel();

        setLayout(new java.awt.BorderLayout());

        jPanelButton.setPreferredSize(new java.awt.Dimension(0, 40));
        jPanelButton.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        org.openide.awt.Mnemonics.setLocalizedText(btnFolder, org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.btnFolder.text")); // NOI18N
        btnFolder.setToolTipText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.btnFolder.toolTipText")); // NOI18N
        btnFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFolderActionPerformed(evt);
            }
        });
        jPanelButton.add(btnFolder);
        jPanelButton.add(jSeparator1);

        jButtonLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Back.png"))); // NOI18N
        jButtonLeft.setToolTipText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.jButtonLeft.toolTipText")); // NOI18N
        jButtonLeft.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonLeft.setPreferredSize(new java.awt.Dimension(52, 27));
        jButtonLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLeftActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonLeft);

        jButtonRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Forward.png"))); // NOI18N
        jButtonRight.setToolTipText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.jButtonRight.toolTipText")); // NOI18N
        jButtonRight.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonRight.setPreferredSize(new java.awt.Dimension(52, 27));
        jButtonRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRightActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonRight);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonBottom, org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.jButtonBottom.text")); // NOI18N
        jButtonBottom.setToolTipText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.jButtonDown.toolTiptext")); // NOI18N
        jButtonBottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBottomActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonBottom);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonTop, org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.jButtonTop.text")); // NOI18N
        jButtonTop.setToolTipText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.jButtonTop.toolTiptext")); // NOI18N
        jButtonTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTopActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonTop);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAdjust, org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.jButtonAdjust.text")); // NOI18N
        jButtonAdjust.setToolTipText(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BrowserPanel.jButtonAdjust.toolTiptext")); // NOI18N
        jButtonAdjust.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAdjustActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonAdjust);

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
                .addComponent(lbFileName, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
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
            .addGap(0, 583, Short.MAX_VALUE)
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

        File file = new FileChooserBuilder(BrowserPanel.class)
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
            String fileName = listFiles.getSelectedValue().toString();
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
            String fileName = listFiles.getSelectedValue().toString();
            showImage(fileName, false);
            jScrollPaneImage.getVerticalScrollBar().setValue(0);
            imagePanel.moveToLeft();
            imagePanel.moveToTop();
        } else {
            imagePanel.moveToRight();
        }
    }//GEN-LAST:event_jButtonRightActionPerformed

    private void jButtonBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBottomActionPerformed
        // show down left corner
        //JScrollBar vertical = jScrollPaneImage.getVerticalScrollBar();
        if (imagePanel.isBottomSideVisible()) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            imagePanel.moveToBottom();
        }

    }//GEN-LAST:event_jButtonBottomActionPerformed

    private void jButtonTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTopActionPerformed
        // show up left corner
        //JScrollBar vertical = jScrollPaneImage.getVerticalScrollBar();
        if (imagePanel.isTopSideVisible()) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            imagePanel.moveToTop();
        }
    }//GEN-LAST:event_jButtonTopActionPerformed

    private void jButtonAdjustActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAdjustActionPerformed
        imagePanel.adjustAreaColor();
    }//GEN-LAST:event_jButtonAdjustActionPerformed

    private void listFilesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listFilesKeyPressed
        /* XXX: proper constants */
    }//GEN-LAST:event_listFilesKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFolder;
    private ancestris.modules.releve.imageBrowser.ImagePanel imagePanel;
    private javax.swing.JButton jButtonAdjust;
    private javax.swing.JButton jButtonBottom;
    private javax.swing.JButton jButtonLeft;
    private javax.swing.JButton jButtonRight;
    private javax.swing.JButton jButtonTop;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelButton;
    private javax.swing.JPanel jPanelFiles;
    private javax.swing.JPanel jPanelImage;
    private javax.swing.JPanel jPanelImageInfo;
    private javax.swing.JScrollPane jScrollPaneFiles;
    private javax.swing.JScrollPane jScrollPaneImage;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPaneBrowser;
    private javax.swing.JLabel lbFileName;
    private javax.swing.JList<String> listFiles;
    // End of variables declaration//GEN-END:variables
}
