package ancestris.modules.releve.editor;

import ancestris.modules.releve.MenuCommandProvider;
import ancestris.modules.releve.ReleveOptionsPanel.ImageDirectoryModel;
import ancestris.modules.releve.RelevePanel;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
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
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
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

    private File currentImageDirectory = null;
    private boolean browserVisible = false;
    private Rectangle standaloneEditorBounds = new Rectangle();
    private int editorWidth ;


    /**
     * Cree une nouvelle fenetre
     * Recupere la taille et la position de la session précédente
     *
     * Remarque : A sa fermeture (windowClosing) la fenetre enregistre sa taille
     * et sa position et appelle ReleveTopComponent.setStandaloneEditor(false)
     * pour signaler sa fermeture
     */
    public StandaloneEditor() {
        initComponents();

        ImageIcon icon = new ImageIcon(StandaloneEditor.class.getResource("/ancestris/modules/releve/images/Releve.png"));
        setIconImage(icon.getImage());
        // J'applique un poids=1 pour que seule la largeur du composant de gauche soit modifiée quand on change la taille de la fenetre
        jSplitPane1.setResizeWeight(1.0);

        //setAlwaysOnTop(true);
        // je configure les editeurs
        panelBirth.setStandaloneMode();
        panelMarriage.setStandaloneMode();
        panelDeath.setStandaloneMode();
        panelMisc.setStandaloneMode();
        panelAll.setStandaloneMode();

        // je configure la taille de la fenetre
        browserVisible = Boolean.parseBoolean(NbPreferences.forModule(StandaloneEditor.class).get("ImgageBrowserVisible", "false"));
        editorWidth = Integer.parseInt(NbPreferences.forModule(StandaloneEditor.class).get("StandaloneEditorWidth", "300"));
        String size = NbPreferences.forModule(StandaloneEditor.class).get("StandaloneEditorSize", "300,450,0,0");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
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

        // listener pour intercepter l'evenement de fermeture de la fenetre.

        addWindowListener( new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                componentClosed();
            }
        });

 
        // Image browser
        // currentImageDirectory
        String imageDirectory = NbPreferences.forModule(StandaloneEditor.class).get("ImageDirectory", "");
        File f = new File(imageDirectory);
        if (f.exists()) {
            currentImageDirectory = f;
            populateImageList(currentImageDirectory);
        }

        ListSelectionListener lsl = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if ( listFiles.getSelectedValue() != null ) {
                    String fileName = listFiles.getSelectedValue().toString();
                    showImage(fileName, false);
                }
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

        // je crée les raccourcis pour créer un nouveau relevé
        String shortCut = "StandaloneShortcut";
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt N"), shortCut);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt M"), shortCut);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt D"), shortCut);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt V"), shortCut);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt T"), shortCut);
        jTabbedPane1.getActionMap().put(shortCut, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().toUpperCase().equals("N")) {
                    jTabbedPane1.setSelectedComponent(panelBirth);
                    panelBirth.createRecord();
                } else if (actionEvent.getActionCommand().toUpperCase().equals("M")) {
                    jTabbedPane1.setSelectedComponent(panelMarriage);
                    panelMarriage.createRecord();
                } else if (actionEvent.getActionCommand().toUpperCase().equals("D")) {
                    jTabbedPane1.setSelectedComponent(panelDeath);
                    panelDeath.createRecord();
                } else if (actionEvent.getActionCommand().toUpperCase().equals("V")) {
                    jTabbedPane1.setSelectedComponent(panelMisc);
                    panelMisc.createRecord();
                } else if (actionEvent.getActionCommand().toUpperCase().equals("T")) {
                    jTabbedPane1.setSelectedComponent(panelAll);
                    panelAll.createRecord();
                }

            }
        });

    }

     /**
     * sauvegarde de la configuration a la fermeture du composant
     */
    public void closeComponent() {
       componentClosed();
       dispose();
    }

    /**
     * sauvegarde de la configuration a la fermeture du composant
     */
    public void componentClosed() {
        // Chaque panel sauvegarde la largeur des colonnes
        panelBirth.componentClosed();
        panelMarriage.componentClosed();
        panelDeath.componentClosed();
        panelMisc.componentClosed();
        panelAll.componentClosed();

        // j'affiche la fenetre dans le mode normal pour récuperer la
        // position et la taille
        if (getExtendedState() != JFrame.NORMAL) {
            setExtendedState(JFrame.NORMAL);
        }
        // j'enregistre la taille dans les preferences
        String size;
        if (browserVisible) {
            editorWidth = jSplitPane1.getWidth() - jSplitPane1.getDividerLocation() - jSplitPane1.getDividerSize();
            size = String.valueOf(this.getWidth()) + ","
                    + String.valueOf(this.getHeight()) + ","
                    + String.valueOf(this.getLocation().x) + ","
                    + String.valueOf(this.getLocation().y);

        } else {
            editorWidth = this.getWidth() - jSplitPane1.getDividerSize();
            size = String.valueOf(standaloneEditorBounds.width) + ","
                    + String.valueOf(this.getHeight()) + ","
                    + String.valueOf(this.getLocation().x - standaloneEditorBounds.width + editorWidth) + ","
                    + String.valueOf(this.getLocation().y);
        }

        NbPreferences.forModule(StandaloneEditor.class).put("StandaloneEditorSize", size);
        NbPreferences.forModule(StandaloneEditor.class).put("StandaloneEditorWidth", String.valueOf(editorWidth));

        // je memorise le nom du repertoire courant des images
        String imageDirectory = "";
        if (currentImageDirectory != null) {
            try {
                imageDirectory = currentImageDirectory.getCanonicalPath();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            NbPreferences.forModule(StandaloneEditor.class).put("ImageDirectory", imageDirectory);
        }

        this.setVisible(false);
    }

    /**
     * Initialise les modeles de donnée des 4 panneaux.
     *
     * Cette methode doit être appelée systematiquemnt  apres le contructeur
     * StandaloneEditor() pour que la fenetre puisse afficher les données d'un
     * modele.
     *
     */
    public void setDataManager(DataManager dataManager, MenuCommandProvider menuCommandProvider,
        int recordBirthIndex, int recordMarriageIndex, int recordDeathIndex, int recordMiscIndex, int recordAllIndex, int selectedPanel ) {

        panelBirth.setModel(dataManager, RelevePanel.PanelType.birth, menuCommandProvider);
        panelMarriage.setModel(dataManager, RelevePanel.PanelType.marriage, menuCommandProvider);
        panelDeath.setModel(dataManager, RelevePanel.PanelType.death, menuCommandProvider);
        panelMisc.setModel(dataManager, RelevePanel.PanelType.misc, menuCommandProvider);
        panelAll.setModel(dataManager, RelevePanel.PanelType.all, menuCommandProvider);

        // je selectionne le panel
        jTabbedPane1.setSelectedIndex(selectedPanel);
        // je selectionne le relevé
        panelBirth.selectRecord(recordBirthIndex);
        panelMarriage.selectRecord(recordMarriageIndex);
        panelDeath.selectRecord(recordDeathIndex);
        panelMisc.selectRecord(recordMiscIndex);
        panelAll.selectRecord(recordAllIndex);

    }

    /**
     * selection un relevé
     *  et affiche la photo si le browser est visible
     * @param record
     * @param recordIndex
     */
    public void selectRecord(DataManager dataManager, int panelIndex, int recordIndex) {

        Record record = dataManager.getRecord(recordIndex);
        if (record != null) {
            jTabbedPane1.setSelectedIndex(panelIndex);

            switch (panelIndex) {
                case 0:
                    panelBirth.selectRecord(recordIndex);
                    break;
                case 1:
                    panelMarriage.selectRecord(recordIndex);
                    break;
                case 2:
                    panelDeath.selectRecord(recordIndex);
                    break;
                case 3:
                    panelMisc.selectRecord(recordIndex);
                    break;
                default:
                    panelAll.selectRecord(recordIndex);
                    break;
            }
            if (browserVisible) {
                final String city = dataManager.getCityName();
                final String cote = record.getCote().toString();
                String freeComment = record.getFreeComment().toString();
                final int page = parsePage(freeComment);
                if (page != -1) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            showImage(city, cote, page);
                            setCursor(Cursor.getDefaultCursor());

                        }
                    });

                } else {
                    browserPanel1.showImage(null);
                    lbFileName.setText("");
                    listFiles.clearSelection();
                }
            }
        }
    }


    static private int parsePage(String freeComment) {
        int i;
        int page = -1;
        // Je recherche  le dernier chiffre en partant de la droite
        for (i = freeComment.length() - 1; i >= 0 && freeComment.charAt(i) >= '0' && freeComment.charAt(i) <= '9'; i--) {
        }
        i++;
        // je cree le format pour préserver les zeros à gauche
        String format = String.format("%%0%dd", freeComment.length() - i);
        if (i < freeComment.length()) {
            try {
                page = new Integer(freeComment.substring(i, freeComment.length())).intValue();
            } catch (NumberFormatException ex) {
                page = -1;
            }
        }
        return page;
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

    @SuppressWarnings("unchecked")
    private void populateImageList(File folder) {
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
            }
        });
        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++)
            fileNames[i] = files[i].getName();
        Arrays.sort(fileNames, String.CASE_INSENSITIVE_ORDER);
        listFiles.setListData(fileNames);
    }


    private void showImage(String fileName, boolean forceRefesh) {
        if (lbFileName.getText().equals(fileName) && !forceRefesh)
            return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            String fullFileName = currentImageDirectory.getCanonicalPath() + File.separator + fileName;
            BufferedImage image = ImageIO.read(new File(fullFileName));
            browserPanel1.showImage(image);
            lbFileName.setText(fileName);
            setCursor(Cursor.getDefaultCursor());
        } catch (IOException ex) {
            setCursor(Cursor.getDefaultCursor());
        }

    }

    private void showImage(String city, String cote, int page) {
        //je cherche le repertoire
        File imageFile = findImage( city,  cote,  page);
        if (imageFile != null) {
            try {
                populateImageList(imageFile.getParentFile());
                btnFolder.setText(imageFile.getParent());
                BufferedImage image = ImageIO.read(imageFile);
                browserPanel1.showImage(image);
                if ( image != null) {
                    currentImageDirectory = imageFile.getParentFile();
                    lbFileName.setText(imageFile.getName());
                    listFiles.setSelectedValue(imageFile.getName(), true);
                } else {
                    lbFileName.setText("");
                    listFiles.clearSelection();
                }
            } catch (IOException ex) {
                setCursor(Cursor.getDefaultCursor());
            }
        } else {
            browserPanel1.showImage(null);
            lbFileName.setText("");
            listFiles.clearSelection();
        }
    }

    private File findImage(String city, String cote, int page) {
        File imageFile = null;

        //je cherche le repertoire
        DirectoryCityFilter cityFilter = new DirectoryCityFilter(city);
        DirectoryCoteFilter coteFilter = new DirectoryCoteFilter(cote);
        FileReferenceFilter pageFilter = new FileReferenceFilter(page);

        if (!cote.isEmpty()) {
            for (File directory : ImageDirectoryModel.getModel().getImageBrowserDirectories()) {
                imageFile = findImage(directory, cityFilter, coteFilter, pageFilter);
                if (imageFile != null) {
                    break;
                }
            }
        }
        
        if (imageFile == null) {
            // je cherche sans la cote
            for (File directory : ImageDirectoryModel.getModel().getImageBrowserDirectories()) {
                imageFile = findImage(directory, cityFilter, pageFilter);
                if (imageFile != null) {
                    break;
                }
            }
        }

        return imageFile;
    }

    private File findImage(File directory, DirectoryCityFilter cityFilter, DirectoryCoteFilter coteFilter, FileReferenceFilter pageFilter) {
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
    
    private File findImage(File directory, DirectoryCityFilter cityFilter, FileReferenceFilter pageFilter) {
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
    
    private File findImage(File directory, DirectoryCoteFilter coteFilter, FileReferenceFilter pageFilter) {
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
    
     private File findImage(File directory, FileReferenceFilter pageFilter) {
        File result = null;

        if (directory.isDirectory()) {
            // je cherche l'image dans le repertoire courant
            File[]  files= directory.listFiles(pageFilter);
            if (files != null && files.length > 0) {
                result = files[0];
            }
            if (result == null ) {
               // je cherche l'image dans les sous repertoires
                File cityDirectories[] = directory.listFiles(new DirectoryFilter());
                for (File subDir : cityDirectories) {
                    if (result == null) {
                        files = subDir.listFiles(pageFilter);
                        if (files != null && files.length > 0) {
                            result = files[0];
                        }
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

    static private class FileReferenceFilter implements FileFilter {
        int searchedPage;


        static final String[] EXTENSIONS = new String[]{
            "jpeg", "jpg", "png", "gif", "bmp", "tiff", "tif"
        };

        public FileReferenceFilter(int searchedPage) {
            this.searchedPage = searchedPage;
        }

        @Override
        public boolean accept(File dir) {
            if ( dir.isFile() ) {
                String name = dir.getName().toLowerCase();

                boolean extension = false;
                for (final String ext : EXTENSIONS) {
                    if (name.endsWith("." + ext)) {
                        extension =true;
                        break;
                    }
                }

                if (extension) {
                    int dotIndex = name.lastIndexOf('.');
                    if (dotIndex > 0) {
                        name = name.substring(0,dotIndex);
                    }

                    int page = parsePage(name);
                    if (page != searchedPage) {
                        int lastSeparatorPos = name.lastIndexOf('-');
                        if (lastSeparatorPos != -1) {
                            page = parsePage(name.substring(0, lastSeparatorPos));
                        }
                    }
                    return  page == searchedPage ;
                } else {
                    return false;
                }
            } else {
                // ce n'est pas un fichier
                return false;
            }

            
        }
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
        panelBirth = new ancestris.modules.releve.RelevePanel();
        panelMarriage = new ancestris.modules.releve.RelevePanel();
        panelDeath = new ancestris.modules.releve.RelevePanel();
        panelMisc = new ancestris.modules.releve.RelevePanel();
        panelAll = new ancestris.modules.releve.RelevePanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        jpanelImage.setLayout(new java.awt.BorderLayout());

        jPanel1.setPreferredSize(new java.awt.Dimension(0, 40));
        jPanel1.setLayout(new java.awt.FlowLayout(0));

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
                .addComponent(lbFileName, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelImageInfoLayout.setVerticalGroup(
            jPanelImageInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lbFileName, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
        );

        jPanel4.add(jPanelImageInfo, java.awt.BorderLayout.NORTH);

        javax.swing.GroupLayout browserPanel1Layout = new javax.swing.GroupLayout(browserPanel1);
        browserPanel1.setLayout(browserPanel1Layout);
        browserPanel1Layout.setHorizontalGroup(
            browserPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 583, Short.MAX_VALUE)
        );
        browserPanel1Layout.setVerticalGroup(
            browserPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 475, Short.MAX_VALUE)
        );

        jScrollPaneImage.setViewportView(browserPanel1);

        jPanel4.add(jScrollPaneImage, java.awt.BorderLayout.CENTER);

        jSplitPaneBrowser.setRightComponent(jPanel4);

        jPanelBrowser.add(jSplitPaneBrowser, java.awt.BorderLayout.CENTER);

        jpanelImage.add(jPanelBrowser, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jpanelImage);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.birthEditor.TabConstraints.tabTitle"), panelBirth); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.marriageEditor.TabConstraints.tabTitle"), panelMarriage); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.deathEditor.TabConstraints.tabTitle"), panelDeath); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.miscEditor.TabConstraints.tabTitle"), panelMisc); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.panelAll.TabConstraints.tabTitle"), panelAll); // NOI18N

        jSplitPane1.setRightComponent(jTabbedPane1);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFolderActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (currentImageDirectory != null) {
            fc.setCurrentDirectory(currentImageDirectory);
        }
        int fcr = fc.showDialog(this, NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.btnFolder.text"));
        if (fcr != JFileChooser.APPROVE_OPTION)
            return;
        String folderName;
        try {
            currentImageDirectory = fc.getSelectedFile();
            folderName = currentImageDirectory.getCanonicalPath();
        } catch (IOException ex) {
            return;
        }
        btnFolder.setText(folderName);
        populateImageList(currentImageDirectory);
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
             showImage(fileName, false);
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
            showImage(fileName, false);
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
    private ancestris.modules.releve.editor.BrowserPanel browserPanel1;
    private javax.swing.JButton btnFolder;
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
    private javax.swing.JList<String> listFiles;
    private ancestris.modules.releve.RelevePanel panelAll;
    private ancestris.modules.releve.RelevePanel panelBirth;
    private ancestris.modules.releve.RelevePanel panelDeath;
    private ancestris.modules.releve.RelevePanel panelMarriage;
    private ancestris.modules.releve.RelevePanel panelMisc;
    // End of variables declaration//GEN-END:variables

}
