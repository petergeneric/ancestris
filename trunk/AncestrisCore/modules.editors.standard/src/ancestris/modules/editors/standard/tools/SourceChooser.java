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

import static ancestris.modules.editors.standard.tools.Utils.scaleImage;
import static ancestris.modules.editors.standard.tools.Utils.getImageFromFile;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Media;
import genj.gedcom.Source;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
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
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
public class SourceChooser extends javax.swing.JPanel {

    private static int THUMB_WIDTH = 50;
    private static int THUMB_HEIGHT = 70;
    
    private static Map<String, ImageIcon> cacheIcon = new HashMap<String, ImageIcon>();
    
    private Registry registry = null;
    private ThumbComparator thumbComparator = new ThumbComparator();
    private TreeSet<SourceThumb> allSource = new TreeSet<SourceThumb>(thumbComparator);
    private DefaultListModel filteredModel = new DefaultListModel();
    
    private Gedcom gedcom = null;
    private File mainFile = null;
    private Image mainImage = null;
    private Image scaledImage = null;
    private String mainTitle = null;
    private String mainText = null;
    private JButton okButton = null;
    private JButton cancelButton = null;
    
    /**
     * Creates new form SourceChooser
     */
    public SourceChooser(Gedcom gedcom, File file, Image image, String title, JButton okButton, JButton cancelButton) {
        this.gedcom = gedcom;
        mainFile = file;
        mainImage = image;
        mainTitle = title;
        this.okButton = okButton;
        this.cancelButton = cancelButton;
        
        // Run source collection from separate thread
        createSourceThumbs();
        Thread sourceThread = new Thread() {
            @Override
            public void run() {
                displaySourceThumbs();
            }
        };
        sourceThread.setName("Source reading thread");
        sourceThread.start();
        
        registry = Registry.get(getClass());
        initComponents();
        this.setPreferredSize(new Dimension(registry.get("sourceWindowWidth", this.getPreferredSize().width), registry.get("sourceWindowHeight", this.getPreferredSize().height)));
        jSplitPane.setDividerLocation(registry.get("sourceSplitDividerLocation", jSplitPane.getDividerLocation()));
        sourceMedia.setText("");
        displayIconAndTitle();
        sourceList.setCellRenderer(new ListEntryCellRenderer());
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
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                displayIconAndTitle(sourceMedia.getWidth(), sourceMedia.getHeight());
            }
        });
    }
    
    private void displayIconAndTitle(int width, int height) {
        if (mainFile == null && !mainText.isEmpty()) {
            sourceMedia.setVisible(false);
            sourceText.setVisible(true);
            sourceText.setText(mainText);
            sourceText.setCaretPosition(0);
        } else if (mainImage != null) {
            sourceText.setText("");
            sourceText.setVisible(false);
            sourceMedia.setVisible(true);
            if (width == 0 || height == 0) {
                scaledImage = null;
                sourceMedia.repaint();
                return;
            }
            double imageRatio = (double) mainImage.getWidth(null) / (double) mainImage.getHeight(null);
            double targetRatio = (double) width / (double) height;
            if (targetRatio < imageRatio) {
                scaledImage = mainImage.getScaledInstance(width, -1, Image.SCALE_DEFAULT);
            } else {
                scaledImage = mainImage.getScaledInstance(-1, height, Image.SCALE_DEFAULT);
            }
        }
        jLayeredPane1.revalidate();
        jLayeredPane1.repaint();
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
        jLayeredPane1 = new javax.swing.JLayeredPane();
        sourceMedia = new javax.swing.JLabel() {
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
        jScrollPane1 = new javax.swing.JScrollPane();
        sourceText = new javax.swing.JTextArea();
        photoTitle = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        textFilter = new javax.swing.JTextField();
        jScrollPaneSource = new javax.swing.JScrollPane();
        sourceList = new javax.swing.JList(filteredModel);

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

        sourceMedia.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(sourceMedia, org.openide.util.NbBundle.getMessage(SourceChooser.class, "SourceChooser.sourceMedia.text")); // NOI18N
        sourceMedia.setToolTipText(org.openide.util.NbBundle.getMessage(SourceChooser.class, "SourceChooser.sourceMedia.toolTipText")); // NOI18N
        sourceMedia.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        sourceMedia.setPreferredSize(new java.awt.Dimension(232, 352));
        sourceMedia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sourceMediaMouseClicked(evt);
            }
        });
        sourceMedia.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                sourceMediaComponentResized(evt);
            }
        });

        sourceText.setEditable(false);
        sourceText.setColumns(20);
        sourceText.setFont(new java.awt.Font("DejaVu Sans", 2, 12)); // NOI18N
        sourceText.setLineWrap(true);
        sourceText.setRows(5);
        sourceText.setText(org.openide.util.NbBundle.getMessage(SourceChooser.class, "SourceChooser.sourceText.text")); // NOI18N
        sourceText.setWrapStyleWord(true);
        jScrollPane1.setViewportView(sourceText);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                .addComponent(sourceMedia, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane1Layout.createSequentialGroup()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourceMedia, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE))
        );
        jLayeredPane1.setLayer(sourceMedia, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(jScrollPane1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        photoTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        photoTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(photoTitle, org.openide.util.NbBundle.getMessage(SourceChooser.class, "SourceChooser.photoTitle.text")); // NOI18N
        photoTitle.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        photoTitle.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5,5,5,5)));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLayeredPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(photoTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLayeredPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(photoTitle))
        );

        jSplitPane.setLeftComponent(jPanel1);

        jPanel2.setPreferredSize(new java.awt.Dimension(200, 58));

        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(SourceChooser.class, "SourceChooser.filterLabel.text")); // NOI18N

        textFilter.setText(org.openide.util.NbBundle.getMessage(SourceChooser.class, "SourceChooser.textFilter.text")); // NOI18N

        sourceList.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        sourceList.setFont(new java.awt.Font("DejaVu Sans Condensed", 0, 10)); // NOI18N
        sourceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        sourceList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        sourceList.setVisibleRowCount(-1);
        sourceList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sourceListMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                sourceListMousePressed(evt);
            }
        });
        sourceList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                sourceListValueChanged(evt);
            }
        });
        jScrollPaneSource.setViewportView(sourceList);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneSource)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(filterLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)))
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
                .addComponent(jScrollPaneSource, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE))
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

    private void sourceListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_sourceListValueChanged
        if (!sourceList.isSelectionEmpty()) {
            SourceThumb source = (SourceThumb) filteredModel.get(sourceList.getSelectedIndex());
            mainImage = source.getImage();
            mainTitle = source.title;
            mainFile = source.file;
            mainText = source.text;
            displayIconAndTitle();
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }//GEN-LAST:event_sourceListValueChanged

    private void sourceMediaComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_sourceMediaComponentResized
        displayIconAndTitle();
    }//GEN-LAST:event_sourceMediaComponentResized

    private void sourceListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sourceListMouseClicked
        if (evt.getClickCount() == 2) {
            okButton.doClick();
        }
    }//GEN-LAST:event_sourceListMouseClicked

    private void sourceMediaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sourceMediaMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1 && mainFile != null) {
            try {
                Desktop.getDesktop().open(mainFile);
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }

    }//GEN-LAST:event_sourceMediaMouseClicked

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        registry.put("sourceWindowWidth", evt.getComponent().getWidth());
        registry.put("sourceWindowHeight", evt.getComponent().getHeight());
    }//GEN-LAST:event_formComponentResized

    private void jSplitPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPanePropertyChange
        registry.put("sourceSplitDividerLocation", jSplitPane.getDividerLocation());
    }//GEN-LAST:event_jSplitPanePropertyChange

    private void sourceListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sourceListMousePressed
        if (evt.getButton() == MouseEvent.BUTTON3) { 
            sourceList.setSelectedIndex(sourceList.locationToIndex(evt.getPoint()));
            JPopupMenu menu = new JPopupMenu();
            final SourceThumb source = getSelectedThumb();
            final Entity entity = source.entity; 
            Entity[] ents = PropertyXRef.getReferences(source.entity);
            if (source.isSource && ents.length > 0) {
                for (Entity ent : ents) {
                    JMenuItem menuItem = new JMenuItem(NbBundle.getMessage(getClass(), "EditEntity", ent.toString(true)));
                    menu.add(menuItem);
                    final Entity finalEntity = ent;
                    menuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae) {
                            edit(finalEntity);
                        }
                    });
                }
            } else {
                JMenuItem menuItem = new JMenuItem(NbBundle.getMessage(getClass(), "EditSource"));
                menu.add(menuItem);
                menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        edit(entity);
                    }
                });
            }
            menu.show(sourceList, evt.getX(), evt.getY()); 
        }
    }//GEN-LAST:event_sourceListMousePressed

    private void edit(Entity entity) {
        cancelButton.doClick();
        SelectionDispatcher.fireSelection(new Context(entity));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filterLabel;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPaneSource;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JLabel photoTitle;
    private javax.swing.JList sourceList;
    private javax.swing.JLabel sourceMedia;
    private javax.swing.JTextArea sourceText;
    private javax.swing.JTextField textFilter;
    // End of variables declaration//GEN-END:variables

    
    
    
    private void createSourceThumbs() {
        
        // Clear source list
        allSource.clear();
        
        // Get all source throughout the whole gedcom, excluding those underneath SOUR only
        String[] ENTITIES = { Gedcom.INDI, Gedcom.FAM, Gedcom.SUBM };
        for (String type : ENTITIES) {
            Collection<Entity> entities = (Collection<Entity>) gedcom.getEntities(type);
            for (Entity entity : entities) {
                List<PropertyFile> properties = entity.getProperties(PropertyFile.class);
                for (PropertyFile sourceFile : properties) {
                    if (!Utils.parentTagsContains(sourceFile, "SOUR")) {
                        continue;
                    }
                    String title = "";
                    File file = sourceFile.getFile();
                    Property sourceTitle = sourceFile.getParent().getProperty("TITL");
                    boolean flag = false;
                    if (sourceTitle != null && !sourceTitle.getDisplayValue().trim().isEmpty()) {
                        title = sourceTitle.getDisplayValue().trim();
                        flag = true;
                    } else {
                        title = entity.toString(false).trim();
                        flag = false;
                    }
                    String text = "";
                    Property sourceText = sourceFile.getParent().getProperty("TEXT");
                    if (sourceText != null && !sourceText.getDisplayValue().trim().isEmpty()) {
                        text = sourceText.getDisplayValue().trim();
                    }
                    SourceThumb source = new SourceThumb(entity, file, title, text);
                    source.setTrueTitle(flag);
                    allSource.add(source);
                }
            }
        }
        

        
        // Get all source entities (SOURCE)
        Collection<Source> entities = (Collection<Source>) gedcom.getEntities(Gedcom.SOUR);
        for (Source entity : entities) {
            File file = null;
            String title = "";
            boolean flag = false;
            
            Property propTitle = entity.getProperty("TITL", true);
            if (propTitle != null) {
                title = propTitle.getDisplayValue().trim();
                flag = true;
            }
            String text = "";
            Property propText = entity.getProperty("TEXT");
            if (propText != null && !propText.getDisplayValue().trim().isEmpty()) {
                text = propText.getDisplayValue().trim();
            }
            Property propMedia = entity.getProperty("OBJE", true);
            if (propMedia != null && propMedia instanceof PropertyMedia) {
                PropertyMedia pm = (PropertyMedia) propMedia;
                file = ((Media) pm.getTargetEntity()).getFile();
            }
            SourceThumb source = new SourceThumb(entity, file, title, text);
            source.setTrueTitle(flag);
            Entity[] ents = PropertyXRef.getReferences(entity);
            source.setUnused(ents.length == 0);
            allSource.add(source);
        }
        
    }

    private void displaySourceThumbs() {
        // Put them in model in sorted order
        filteredModel.clear();
        for (SourceThumb item : allSource) {
            item.setIcon();
            filteredModel.addElement(item);
        }
        
    }

    private SourceThumb getSelectedThumb() {
        return (SourceThumb) filteredModel.get(sourceList.getSelectedIndex());
    }
    
    public boolean isSelectedEntitySource() {
        SourceThumb source = getSelectedThumb();
        return source == null ? false : source.isSource;
    }

    public Entity getSelectedEntity() {
        SourceThumb source = getSelectedThumb();
        return source == null ? null : source.entity;
    }

    public File getSelectedFile() {
        SourceThumb source = getSelectedThumb();
        return source == null ? null : source.file;
    }

    public String getSelectedTitle() {
        SourceThumb source = getSelectedThumb();
        return source == null ? "" : source.title;
    }


    
    
    
    
    public void filterModel(String filter) {
        sourceList.clearSelection();
        sourceList.setModel(new DefaultListModel());
        filteredModel.clear();
        for (SourceThumb item : allSource) {
            if (item.title.contains(filter)) {
                filteredModel.addElement(item);
            }
        }
        sourceList.setModel(filteredModel);
    }    

    public int getNbSource() {
        return allSource.size();
    }

  
    
    
    private static class ListEntryCellRenderer extends JLabel implements ListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            SourceThumb entry = (SourceThumb) value;

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


    
    
    
    
    
    public class SourceThumb {
        
        public boolean isSource = false;
        public Entity entity = null;
        public File file = null;
        public ImageIcon icon = null;
        public String title = "";
        public String text = "";
        public boolean isTrueTitle = true;
        public boolean isUnused = false;
        
        public SourceThumb(Source entity, File file, String title, String text) {
            this.isSource = true;
            this.entity = entity;
            this.file = file;
            this.title = title;
            this.text = text;
        }

        private SourceThumb(Entity entity, File file, String title, String text) {
            this.isSource = (entity instanceof Source);
            this.entity = entity;
            this.file = file;
            this.title = title;
            this.text = text;
        }
        
        public Image getImage() {
            return getImageFromFile(file, getClass());
        }
        
        public void setIcon() {
            icon = (file == null ? null : cacheIcon.get(file.getAbsolutePath()));
            if (icon == null) {
                icon = new ImageIcon(scaleImage(file , getClass(), THUMB_WIDTH, THUMB_HEIGHT, text.trim().isEmpty()));
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



    
    private class ThumbComparator implements Comparator<SourceThumb> {

        public int compare(SourceThumb o1, SourceThumb o2) {
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
