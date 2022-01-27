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

import static ancestris.modules.editors.standard.tools.Utils.getImageFromFile;
import static ancestris.modules.editors.standard.tools.Utils.scaleImage;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Grammar;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyXRef;
import genj.io.FileAssociation;
import genj.io.InputSource;
import genj.io.input.FileInput;
import genj.io.input.URLInput;
import genj.renderer.MediaRenderer;
import genj.util.Registry;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
public class MediaChooser extends javax.swing.JPanel {
    
    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    private static int THUMB_WIDTH = 50;
    private static int THUMB_HEIGHT = 70;
    
    private static Map<String, ImageIcon> cacheIcon = new HashMap<String, ImageIcon>();
    
    private Registry registry = null;
    private ThumbComparator thumbComparator = new ThumbComparator();
    private TreeSet<MediaThumb> allMedia = new TreeSet<>(thumbComparator);
    private DefaultListModel filteredModel = new DefaultListModel();
    
    private Gedcom gedcom = null;
    private InputSource mainInput = null;
    private MediaWrapper mainMedia = null;
    private Image mainImage = null;
    private Image scaledImage = null;
    private String mainTitle = null;
    private JButton okButton = null;
    private JButton cancelButton = null;
    
    /**
     * Creates new form MediaChooser
     */
    public MediaChooser(Gedcom gedcom, InputSource is, Image image, String title, MediaWrapper media, JButton okButton, JButton cancelButton, boolean sourceImages) {
        this.gedcom = gedcom;
        mainInput = is;
        mainMedia = media;
        mainImage = image;
        mainTitle = title;
        this.okButton = okButton;
        this.cancelButton = cancelButton;
        
        // Run media collection from separate thread
        createMediaThumbs(sourceImages);
        Thread mediaThread = new Thread() {
            @Override
            public void run() {
                displayMediaThumbs();
                selectMedia(mainMedia);
            }
        };
        mediaThread.setName("Media reading thread");
        mediaThread.start();
        
        registry = Registry.get(getClass());
        initComponents();
        this.setPreferredSize(new Dimension(registry.get("mediaWindowWidth", this.getPreferredSize().width), registry.get("mediaWindowHeight", this.getPreferredSize().height)));
        jSplitPane.setDividerLocation(registry.get("mediaSplitDividerLocation", jSplitPane.getDividerLocation()));
        labelPhoto.setText("");
        displayIconAndTitle();
        mediaList.setCellRenderer(new ListEntryCellRenderer());
        okButton.setEnabled(false);
        textFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) {}
            private void filter() {
                filterModel(textFilter.getText());
            }
        });
        
    }

    private void selectMedia(MediaWrapper media) {
        MediaThumb selectedMedia = null;
        for (MediaThumb mediai : allMedia) {
            if (mediai.entity == null && media == null) {
                selectedMedia = mediai;
                break;
            }
            if (mediai.entity != null && media != null && mediai.entity.equals(media.getTargetMedia())) {
                selectedMedia = mediai;
                break;
            }
        }
        if (selectedMedia != null) {
            final MediaThumb mediai = selectedMedia;
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    mediaList.setSelectedValue(mediai, true);
                    mediaList.scrollRectToVisible(mediaList.getCellBounds(mediaList.getMinSelectionIndex(), mediaList.getMaxSelectionIndex()));
                }
            });
        }
    }
    


    private void displayIconAndTitle() {
        displayIconAndTitle(labelPhoto.getPreferredSize().width, labelPhoto.getPreferredSize().height);
    }
    
    private void displayIconAndTitle(int width, int height) {
        if (mainImage != null) {
            double imageRatio = (double) mainImage.getWidth(null) / (double) mainImage.getHeight(null);
            height = Math.max(height, 10);
            double targetRatio = (double) width / (double) height;
            if (targetRatio < imageRatio) {
                scaledImage = mainImage.getScaledInstance(width, -1, Image.SCALE_DEFAULT);
            } else {
                scaledImage = mainImage.getScaledInstance(-1, height, Image.SCALE_DEFAULT);
            }
        }
        labelPhoto.repaint(); 
        photoTitle.setText("<html><center>" + mainTitle + "</center></html>");
        photoTitle.setPreferredSize(new Dimension(width, -1));
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
        labelPhoto = new javax.swing.JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (scaledImage != null) {
                    ((Graphics2D) g).drawImage(scaledImage, 0 + ((getWidth() - scaledImage.getWidth(this)) / 2), ((getHeight() - scaledImage.getHeight(this)) / 2), null);
                    //registry.put("mediaWindowWidth", getParent().getWidth());
                    //registry.put("mediaWindowHeight", getParent().getHeight());
                }
            }

        };
        photoTitle = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        textFilter = new javax.swing.JTextField();
        jScrollPaneMedia = new javax.swing.JScrollPane();
        mediaList = new javax.swing.JList(filteredModel);
        jLabel1 = new javax.swing.JLabel();

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

        jPanel1.setPreferredSize(new java.awt.Dimension(200, 383));

        labelPhoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(labelPhoto, org.openide.util.NbBundle.getMessage(MediaChooser.class, "MediaChooser.labelPhoto.text")); // NOI18N
        labelPhoto.setToolTipText(org.openide.util.NbBundle.getMessage(MediaChooser.class, "MediaChooser.labelPhoto.toolTipText")); // NOI18N
        labelPhoto.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labelPhoto.setPreferredSize(new java.awt.Dimension(232, 352));
        labelPhoto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelPhotoMouseClicked(evt);
            }
        });
        labelPhoto.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                labelPhotoComponentResized(evt);
            }
        });

        photoTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        photoTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(photoTitle, org.openide.util.NbBundle.getMessage(MediaChooser.class, "MediaChooser.photoTitle.text")); // NOI18N
        photoTitle.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        photoTitle.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5,5,5,5)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelPhoto, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(photoTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelPhoto, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(photoTitle))
        );

        jSplitPane.setLeftComponent(jPanel1);

        jPanel2.setPreferredSize(new java.awt.Dimension(200, 58));

        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(MediaChooser.class, "MediaChooser.filterLabel.text")); // NOI18N

        textFilter.setText(org.openide.util.NbBundle.getMessage(MediaChooser.class, "MediaChooser.textFilter.text")); // NOI18N

        mediaList.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        mediaList.setFont(new java.awt.Font("DejaVu Sans Condensed", 0, 10)); // NOI18N
        mediaList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        mediaList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        mediaList.setVisibleRowCount(-1);
        mediaList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mediaListMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mediaListMousePressed(evt);
            }
        });
        mediaList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                mediaListValueChanged(evt);
            }
        });
        jScrollPaneMedia.setViewportView(mediaList);

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MediaChooser.class, "MediaChooser.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPaneMedia)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(filterLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                .addComponent(jScrollPaneMedia, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jLabel1))
        );

        jSplitPane.setRightComponent(jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void mediaListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_mediaListValueChanged
        if (!mediaList.isSelectionEmpty()) {
            MediaThumb media = (MediaThumb) filteredModel.get(mediaList.getSelectedIndex());
            mainImage = media.getImage();
            mainTitle = media.title;
            mainInput = media.inputSource;
            displayIconAndTitle(labelPhoto.getWidth(), labelPhoto.getHeight());
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }//GEN-LAST:event_mediaListValueChanged

    private void labelPhotoComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_labelPhotoComponentResized
        displayIconAndTitle(labelPhoto.getWidth(), labelPhoto.getHeight());
    }//GEN-LAST:event_labelPhotoComponentResized

    private void mediaListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mediaListMouseClicked
        if (evt.getClickCount() == 2) {
            okButton.doClick();
        }
    }//GEN-LAST:event_mediaListMouseClicked

    private void labelPhotoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelPhotoMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1 && mainInput != null) {
            if (mainInput instanceof FileInput) {
                FileAssociation.getDefault().execute(((FileInput)mainInput).getFile().getAbsolutePath());
            }
            if (mainInput instanceof URLInput) {
                FileAssociation.getDefault().execute(((URLInput)mainInput).getURL());
            }
        }

    }//GEN-LAST:event_labelPhotoMouseClicked

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = evt.getComponent().getWidth();
        if (w > dim.width*8/10) {
            w = dim.width*8/10;
        }
        int h = evt.getComponent().getHeight();
        if (h > dim.height*8/10) {
            h = dim.height*8/10;
        }
        registry.put("mediaWindowWidth", w);
        registry.put("mediaWindowHeight", h);
    }//GEN-LAST:event_formComponentResized

    private void jSplitPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPanePropertyChange
        registry.put("mediaSplitDividerLocation", jSplitPane.getDividerLocation());
    }//GEN-LAST:event_jSplitPanePropertyChange

    private void mediaListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mediaListMousePressed
        if (evt.getButton() == MouseEvent.BUTTON3) { 
            mediaList.setSelectedIndex(mediaList.locationToIndex(evt.getPoint()));
            JPopupMenu menu = new JPopupMenu();
            final MediaThumb media = getSelectedThumb();
            final Entity entity = media.entity; 
            Entity[] ents = PropertyXRef.getReferences(media.entity);
            if (media.isMedia && ents.length > 0) {
                for (Entity ent : ents) {
                    JMenuItem menuItem = new JMenuItem(NbBundle.getMessage(getClass(), "EditEntity", ent.toString(true)));
                    menu.add(menuItem);
                    final Entity finalEntity = ent;
                    menuItem.addActionListener((ActionEvent ae) -> {
                        edit(finalEntity);
                    });
                }
            } else if (!media.isMedia) {
                JMenuItem menuItem = new JMenuItem(NbBundle.getMessage(getClass(), "EditEntity", entity.toString(true)));
                menu.add(menuItem);
                menuItem.addActionListener((ActionEvent ae) -> {
                    edit(entity);
                });
            }
            menu.show(mediaList, evt.getX(), evt.getY()); 
        }
    }//GEN-LAST:event_mediaListMousePressed

    private void edit(Entity entity) {
        cancelButton.doClick();
        SelectionDispatcher.fireSelection(new Context(entity));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filterLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPaneMedia;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JLabel labelPhoto;
    private javax.swing.JList mediaList;
    private javax.swing.JLabel photoTitle;
    private javax.swing.JTextField textFilter;
    // End of variables declaration//GEN-END:variables

    
    
    
    private void createMediaThumbs(boolean sourceImages) {
        
        // Clear media list
        allMedia.clear();
        
        // Get all media throughout the whole gedcom, excluding those underneath SOUR only
        String[] ENTITIES = { Gedcom.INDI, Gedcom.FAM, Gedcom.SOUR, Gedcom.SUBM };
        for (String type : ENTITIES) {
            Collection<Entity> entities = (Collection<Entity>) gedcom.getEntities(type);
            for (Entity entity : entities) {
                List<PropertyFile> properties = entity.getProperties(PropertyFile.class);
                for (PropertyFile mediaFile : properties) {
                    boolean isSourceMedia = isSourceOnly(mediaFile);
                    if ((isSourceMedia && sourceImages) || (!isSourceMedia && !sourceImages)) {
                        String title;
                        InputSource is = MediaRenderer.getSource(mediaFile);
                        Property mediaTitle = mediaFile.getParent().getProperty("TITL");
                        boolean flag;
                        if (mediaTitle != null && !mediaTitle.getDisplayValue().trim().isEmpty()) {
                            title = mediaTitle.getDisplayValue().trim();
                            flag = true;
                        } else {
                            title = entity.toString(false).trim();
                            flag = false;
                        }
                        MediaThumb media = new MediaThumb(entity, is, title);
                        media.setTrueTitle(flag);
                        allMedia.add(media);
                    }
                }
            }
        }
        

        
        // Get all media entities (OBJE)
        if (gedcom.getGrammar().equals(Grammar.V551)) {
            Collection<Media> entities = (Collection<Media>) gedcom.getEntities(Gedcom.OBJE);
            for (Media entity : entities) {
                boolean isSourceMedia = isSourceOnly(entity);
                if ((isSourceMedia && sourceImages) || (!isSourceMedia && !sourceImages)) {
                    InputSource is = null;
                    String title = "";
                    Property mediaFile = entity.getProperty("FILE", false);
                    boolean flag = false;
                    if (mediaFile != null && mediaFile instanceof PropertyFile) {
                        is = MediaRenderer.getSource(mediaFile);
                        Property mediaTitle = mediaFile.getProperty("TITL");
                        if (mediaTitle != null && !mediaTitle.getDisplayValue().trim().isEmpty()) {
                            title = mediaTitle.getDisplayValue().trim();
                            flag = true;
                        } else {
                            Entity[] ents = PropertyXRef.getReferences(entity);
                            if (ents.length > 0) {
                                title = ents[0].toString(false).trim();
                                flag = false;
                            } else {
                                title = entity.toString(false).trim();
                                flag = false;
                            }
                        }
                    }
                    MediaThumb media = new MediaThumb(entity, is, title);
                    media.setTrueTitle(flag);
                    Entity[] ents = PropertyXRef.getReferences(entity);
                    media.setUnused(ents.length == 0);
                    allMedia.add(media);
                }
            }
        }
        
    }

    private void displayMediaThumbs() {
        // Put them in model in sorted order
        filteredModel.clear();
        for (MediaThumb item : allMedia) {
            item.setIcon();
            filteredModel.addElement(item);
        }
        
    }

    private MediaThumb getSelectedThumb() {
        return (MediaThumb) filteredModel.get(mediaList.getSelectedIndex());
    }
    
    public boolean isSelectedEntityMedia() {
        MediaThumb media = getSelectedThumb();
        return media == null ? false : media.isMedia;
    }

    public Entity getSelectedEntity() {
        MediaThumb media = getSelectedThumb();
        return media == null ? null : media.entity;
    }

    public InputSource getSelectedInput() {
        MediaThumb media = getSelectedThumb();
        return media == null ? null : media.inputSource;
    }

    public String getSelectedTitle() {
        MediaThumb media = getSelectedThumb();
        return media == null ? "" : media.title;
    }


    
    
    
    
    public void filterModel(String filter) {
        mediaList.clearSelection();
        mediaList.setModel(new DefaultListModel());
        filteredModel.clear();
        for (MediaThumb item : allMedia) {
            if (item.title.toLowerCase().contains(filter.toLowerCase())) {
                filteredModel.addElement(item);
            }
        }
        mediaList.setModel(filteredModel);
    }    

    private boolean isSourceOnly(Property property) {
        return Utils.parentTagsContains(property, "SOUR");
    }
    
    private boolean isSourceOnly(Media entity) {
        boolean ret = false;
        List<PropertyXRef> references = entity.getProperties(PropertyXRef.class);
        for (PropertyXRef refProp : references) {
            if (Utils.parentTagsContains(refProp, "SOUR")) {
                ret = true;
            } else {
                return false;
            }
        }
        return ret;
    }

    public int getNbMedia() {
        return allMedia.size();
    }
    
    
    private static class ListEntryCellRenderer extends JLabel implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            MediaThumb entry = (MediaThumb) value;

            setHorizontalTextPosition(JLabel.CENTER);
            setVerticalTextPosition(JLabel.BOTTOM);
            setHorizontalAlignment(JLabel.CENTER);
            setVerticalAlignment(JLabel.TOP);

            int labelWidth = THUMB_WIDTH + 30;
            int nbLines = getFontMetrics(getFont()).stringWidth(entry.title) / labelWidth + 3; // +1 for rounding and +2 again to compensate for average line breaks
            int labelHeight = THUMB_HEIGHT + 12 * nbLines;  // 12 pixels per line for font size 10 set in component netbeans parameters
            
            setPreferredSize(new Dimension(labelWidth, labelHeight));
            // black : title & used
            // blue  : no title & used
            // red   : not used
            String color = entry.isTrueTitle && !entry.isUnused ? "black" : !entry.isTrueTitle && !entry.isUnused ? "blue" : "red";
            setText("<html><center><font color="+color+">" + entry.title + "</font></center></html>");
            setIcon(entry.icon);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
                setOpaque(true);
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
                setOpaque(false);
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());

            return this;
        }
    }
    
    
    private class MediaThumb {
        
        public boolean isMedia = false;
        public Entity entity = null;
        public InputSource inputSource = null;
        public ImageIcon icon = null;
        public String title = "";
        public boolean isTrueTitle = true;
        public boolean isUnused = false;
        
        public MediaThumb(Media entity, InputSource is, String title) {
            this.isMedia = true;
            this.entity = entity;
            this.inputSource = is;
            this.title = title;
        }

        private MediaThumb(Entity entity, InputSource is, String title) {
            this.isMedia = (entity instanceof Media);
            this.entity = entity;
            this.inputSource = is;
            this.title = title;
        }
        
        public Image getImage() {
            return getImageFromFile(inputSource, getClass(), Utils.IMG_INVALID_PHOTO);
        }
        
        public void setIcon() {
            icon = (inputSource == null ? null : cacheIcon.get(inputSource.getLocation()));
            if (icon == null) {
                icon = new ImageIcon(scaleImage(inputSource , getClass(), THUMB_WIDTH, THUMB_HEIGHT));
                if (inputSource != null) {
                    cacheIcon.put(inputSource.getLocation(), icon);
                }
            }
        }

        private void setTrueTitle(boolean flag) {
            isTrueTitle = flag;
        }

        private void setUnused(boolean b) {
            isUnused = b;
        }
    }

    private class ThumbComparator implements Comparator<MediaThumb> {

        @Override
        public int compare(MediaThumb o1, MediaThumb o2) {
           InputSource is1 = o1.inputSource;
           InputSource is2 = o2.inputSource;
            String str1 = is1 != null ? is1.getName() : "";
            String str2 = is2 != null ? is2.getName() : "";
            String id1 = o1.entity.getId();
            String id2 = o2.entity.getId();
            String total1 = o1.title.toLowerCase() + str1 + id1;
            String total2 = o2.title.toLowerCase() + str2 + id2;
            return total1.compareTo(total2);
        }
    }
    
}
