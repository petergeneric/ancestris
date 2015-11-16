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
import static ancestris.modules.editors.standard.tools.Utils.getResizedIcon;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Grammar;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyXRef;
import genj.util.Registry;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
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

/**
 *
 * @author frederic
 */
public class MediaChooser extends javax.swing.JPanel {

    private static int THUMB_WIDTH = 50;
    private static int THUMB_HEIGHT = 70;
    
    private static Map<String, ImageIcon> cacheIcon = new HashMap<String, ImageIcon>();
    
    private Registry registry = null;
    private ThumbComparator thumbComparator = new ThumbComparator();
    private TreeSet<MediaThumb> allMedia = new TreeSet<MediaThumb>(thumbComparator);
    private DefaultListModel filteredModel = new DefaultListModel();
    
    private Gedcom gedcom = null;
    private File mainFile = null;
    private Image mainImage = null;
    private Image scaledImage = null;
    private String mainTitle = null;
    private JButton okButton = null;
    private JButton cancelButton = null;
    
    /**
     * Creates new form MediaChooser
     */
    public MediaChooser(Gedcom gedcom, File file, Image image, String title, JButton okButton, JButton cancelButton) {
        this.gedcom = gedcom;
        mainFile = file;
        mainImage = image;
        mainTitle = title;
        this.okButton = okButton;
        this.cancelButton = cancelButton;
        
        // Run media collection from separate thread
        createMediaThumbs();
        Thread mediaThread = new Thread() {
            @Override
            public void run() {
                displayMediaThumbs();
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


    private void displayIconAndTitle() {
        displayIconAndTitle(labelPhoto.getPreferredSize().width, labelPhoto.getPreferredSize().height);
    }
    
    private void displayIconAndTitle(int width, int height) {
        if (mainImage != null) {
            double imageRatio = (double) mainImage.getWidth(null) / (double) mainImage.getHeight(null);
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
                .addComponent(labelPhoto, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(photoTitle))
        );

        jSplitPane.setLeftComponent(jPanel1);

        jPanel2.setPreferredSize(new java.awt.Dimension(200, 58));

        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(MediaChooser.class, "MediaChooser.filterLabel.text")); // NOI18N

        textFilter.setText(org.openide.util.NbBundle.getMessage(MediaChooser.class, "MediaChooser.textFilter.text")); // NOI18N

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneMedia)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(filterLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterLabel)
                    .addComponent(textFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneMedia))
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
            mainFile = media.file;
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
        if (evt.getButton() == MouseEvent.BUTTON1 && mainFile != null) {
            try {
                Desktop.getDesktop().open(mainFile);
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }

    }//GEN-LAST:event_labelPhotoMouseClicked

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        registry.put("mediaWindowWidth", evt.getComponent().getWidth());
        registry.put("mediaWindowHeight", evt.getComponent().getHeight());
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
                    JMenuItem menuItem = new JMenuItem("Editer " + ent.toString(true));
                    menu.add(menuItem);
                    final Entity finalEntity = ent;
                    menuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae) {
                            edit(finalEntity);
                        }
                    });
                }
            } else {
                JMenuItem menuItem = new JMenuItem("Editer " + entity.toString(true));
                menu.add(menuItem);
                menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        edit(entity);
                    }
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPaneMedia;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JLabel labelPhoto;
    private javax.swing.JList mediaList;
    private javax.swing.JLabel photoTitle;
    private javax.swing.JTextField textFilter;
    // End of variables declaration//GEN-END:variables

    
    
    
    private void createMediaThumbs() {
        
        // Clear media list
        allMedia.clear();
        
        // Get all media throughout the whole gedcom, excluding those underneath SOUR only
        String[] ENTITIES = { Gedcom.INDI, Gedcom.FAM, Gedcom.SUBM };
        for (String type : ENTITIES) {
            Collection<Entity> entities = (Collection<Entity>) gedcom.getEntities(type);
            for (Entity entity : entities) {
                List<PropertyFile> properties = entity.getProperties(PropertyFile.class);
                for (PropertyFile mediaFile : properties) {
                    if (isSourceOnly(mediaFile)) {
                        continue;
                    }
                    String title = "";
                    File file = mediaFile.getFile();
                    Property mediaTitle = mediaFile.getParent().getProperty("TITL");
                    boolean flag = false;
                    if (mediaTitle != null && !mediaTitle.getDisplayValue().trim().isEmpty()) {
                        title = mediaTitle.getDisplayValue().trim();
                        flag = true;
                    } else {
                        title = entity.toString(false).trim();
                        flag = false;
                    }
                    MediaThumb media = new MediaThumb(entity, file, title);
                    media.setTrueTitle(flag);
                    allMedia.add(media);
                }
            }
        }
        

        
        // Get all media entities (OBJE)
        if (gedcom.getGrammar().equals(Grammar.V551)) {
            Collection<Media> entities = (Collection<Media>) gedcom.getEntities(Gedcom.OBJE);
            for (Media entity : entities) {
                if (isSourceOnly(entity)) {
                    continue;
                }
                File file = null;
                String title = "";
                Property mediaFile = entity.getProperty("FILE", true);
                boolean flag = false;
                if (mediaFile != null && mediaFile instanceof PropertyFile) {
                    file = ((PropertyFile) mediaFile).getFile();
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
                MediaThumb media = new MediaThumb(entity, file, title);
                media.setTrueTitle(flag);
                Entity[] ents = PropertyXRef.getReferences(entity);
                media.setUnused(ents.length == 0); 
                allMedia.add(media);
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

    public File getSelectedFile() {
        MediaThumb media = getSelectedThumb();
        return media == null ? null : media.file;
    }

    public String getSelectedTitle() {
        MediaThumb media = getSelectedThumb();
        return media == null ? "" : media.title;
    }


    
    
    
    
    public void filterModel(String filter) {
        mediaList.clearSelection();
        filteredModel.clear();
        for (MediaThumb item : allMedia) {
            if (item.title.contains(filter)) {
                filteredModel.addElement(item);
            }
        }
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
            String color = entry.isTrueTitle && !entry.isUnused ? "black" : !entry.isTrueTitle && !entry.isUnused ? "blue" : "red";
                setText("<html><center><font color="+color+">" + entry.title + "</font></center></html>");
            setIcon(entry.icon);

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


    
    
    
    
    
    private class MediaThumb {
        
        public boolean isMedia = false;
        public Entity entity = null;
        public File file = null;
        public ImageIcon icon = null;
        public String title = "";
        public boolean isTrueTitle = true;
        public boolean isUnused = false;
        
        public MediaThumb(Media entity, File file, String title) {
            this.isMedia = true;
            this.entity = entity;
            this.file = file;
            this.title = title;
        }

        private MediaThumb(Entity entity, File file, String title) {
            this.isMedia = (entity instanceof Media);
            this.entity = entity;
            this.file = file;
            this.title = title;
        }
        
        public Image getImage() {
            return getImageFromFile(file, getClass());
        }
        
        public void setIcon() {
            icon = (file == null ? null : cacheIcon.get(file.getAbsolutePath()));
            if (icon == null) {
                icon = getResizedIcon(new ImageIcon(getImage()), THUMB_WIDTH, THUMB_HEIGHT);
                if (file != null) {
                    cacheIcon.put(file.getAbsolutePath(), icon);
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

        public int compare(MediaThumb o1, MediaThumb o2) {
            File file1 = o1.file;
            File file2 = o2.file;
            String str1 = file1 != null ? file1.getAbsolutePath() : "";
            String str2 = file2 != null ? file2.getAbsolutePath() : "";
            String id1 = o1.entity.getId();
            String id2 = o2.entity.getId();
            String total1 = o1.title.toLowerCase() + str1 + id1;
            String total2 = o2.title.toLowerCase() + str2 + id2;
            return total1.compareTo(total2);
        }
    }
    
}
