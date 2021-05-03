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
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyNote;
import genj.gedcom.PropertyXRef;
import genj.util.Registry;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
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
public class NoteChooser extends javax.swing.JPanel {

    private static int THUMB_WIDTH = 120;
    private static int THUMB_HEIGHT = 140;
    
    private Registry registry = null;
    private ThumbComparator thumbComparator = new ThumbComparator();
    private TreeSet<NoteThumb> allNote = new TreeSet<NoteThumb>(thumbComparator);
    private DefaultListModel filteredModel = new DefaultListModel();
    
    private Gedcom gedcom = null;
    private NoteWrapper mainNote = null;
    private String mainText = null;
    private String mainTitle = null;
    private JButton okButton = null;
    private JButton cancelButton = null;
    
    /**
     * Creates new form NoteChooser
     */
    public NoteChooser(Gedcom gedcom, NoteWrapper note, JButton okButton, JButton cancelButton) {
        this.gedcom = gedcom;
        this.mainNote = note;
        this.okButton = okButton;
        this.cancelButton = cancelButton;
        
        registry = Registry.get(getClass());
        initComponents();
        this.setPreferredSize(new Dimension(registry.get("noteWindowWidth", this.getPreferredSize().width), registry.get("noteWindowHeight", this.getPreferredSize().height)));
        jSplitPane.setDividerLocation(registry.get("noteSplitDividerLocation", jSplitPane.getDividerLocation()));
        
        noteList.setCellRenderer(new ListEntryCellRenderer());
        okButton.setEnabled(false);
        textFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) {}
            private void filter() {
                filterModel(textFilter.getText());
            }
        });

        // Run Note collection from separate thread
        createNoteThumbs();
        Thread noteThread = new Thread() {
            @Override
            public void run() {
                displayNoteThumbs();
                selectNote(mainNote);
            }
        };
        noteThread.setName("Note reading thread");
        noteThread.start();
        
    }


    private void selectNote(NoteWrapper note) {
        NoteThumb selectedNote = null;
        String memorizedNoteId = registry.get("noteSelected", "");
        for (NoteThumb notei : allNote) {
            if (note == null && notei.entity == null) { // select default note if note is null
                selectedNote = notei;
            }
            if (note == null && notei.getId().equals(memorizedNoteId)) { // overwrite with memorized note otherwise
                selectedNote = notei;
            }
            if (note != null && notei.entity != null && notei.entity.equals(note.getTargetNote())) { // select note otherwise
                selectedNote = notei;
                break;
            }
        }
        if (selectedNote != null) {
            final NoteThumb notei = selectedNote;
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    noteList.setSelectedValue(notei, true);
                    noteList.ensureIndexIsVisible(noteList.getSelectedIndex());
                    noteList.scrollRectToVisible(noteList.getCellBounds(noteList.getMinSelectionIndex(), noteList.getMaxSelectionIndex()));
                    textFilter.requestFocus();
                }
            });
        }
    }
    

    private void displayIconAndTitle(int width, int height) {
        noteTitle.setText("<html><center>" + mainTitle + "</center></html>");
        noteTitle.setPreferredSize(new Dimension(width, -1));
        noteText.setText(mainText);
        noteText.setCaretPosition(0);
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
        noteTitle = new javax.swing.JLabel();
        noteScrollPane = new javax.swing.JScrollPane();
        noteText = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        filterLabel = new javax.swing.JLabel();
        textFilter = new javax.swing.JTextField();
        jScrollPaneNote = new javax.swing.JScrollPane();
        noteList = new javax.swing.JList(filteredModel);
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

        jPanel1.setPreferredSize(new java.awt.Dimension(250, 383));

        noteTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        noteTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(noteTitle, org.openide.util.NbBundle.getMessage(NoteChooser.class, "NoteChooser.noteTitle.text")); // NOI18N
        noteTitle.setToolTipText(org.openide.util.NbBundle.getMessage(NoteChooser.class, "NoteChooser.noteTitle.toolTipText")); // NOI18N
        noteTitle.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        noteTitle.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5,5,5,5)));

        noteScrollPane.setPreferredSize(new java.awt.Dimension(188, 93));

        noteText.setEditable(false);
        noteText.setColumns(20);
        noteText.setFont(new java.awt.Font("DejaVu Sans", 2, 12)); // NOI18N
        noteText.setLineWrap(true);
        noteText.setRows(5);
        noteText.setText(org.openide.util.NbBundle.getMessage(NoteChooser.class, "NoteChooser.noteText.text")); // NOI18N
        noteText.setToolTipText(org.openide.util.NbBundle.getMessage(NoteChooser.class, "NoteChooser.noteText.toolTipText")); // NOI18N
        noteText.setWrapStyleWord(true);
        noteText.setPreferredSize(null);
        noteScrollPane.setViewportView(noteText);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(noteTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                    .addComponent(noteScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(noteTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noteScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane.setLeftComponent(jPanel1);

        jPanel2.setPreferredSize(new java.awt.Dimension(200, 58));

        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(NoteChooser.class, "NoteChooser.filterLabel.text")); // NOI18N

        textFilter.setText(org.openide.util.NbBundle.getMessage(NoteChooser.class, "NoteChooser.textFilter.text")); // NOI18N

        noteList.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        noteList.setFont(new java.awt.Font("DejaVu Sans Condensed", 0, 10)); // NOI18N
        noteList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        noteList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        noteList.setVisibleRowCount(-1);
        noteList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                noteListMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                noteListMousePressed(evt);
            }
        });
        noteList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                noteListValueChanged(evt);
            }
        });
        jScrollPaneNote.setViewportView(noteList);

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NoteChooser.class, "NoteChooser.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneNote)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(filterLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
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
                .addComponent(jScrollPaneNote, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jLabel1)
                .addContainerGap())
        );

        jSplitPane.setRightComponent(jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 625, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void noteListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_noteListValueChanged
        if (!noteList.isSelectionEmpty()) {
            NoteThumb note = (NoteThumb) filteredModel.get(noteList.getSelectedIndex());
            mainText = note.text;
            mainTitle = note.title;
            displayIconAndTitle(noteText.getWidth(), noteText.getHeight());
            okButton.setEnabled(true);
            registry.put("noteSelected", note.getId());
        } else {
            okButton.setEnabled(false);
        }
    }//GEN-LAST:event_noteListValueChanged

    private void noteListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noteListMouseClicked
        if (evt.getClickCount() == 2) {
            okButton.doClick();
        }
    }//GEN-LAST:event_noteListMouseClicked

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
        registry.put("noteWindowWidth", w);
        registry.put("noteWindowHeight", h);
    }//GEN-LAST:event_formComponentResized

    private void jSplitPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPanePropertyChange
        registry.put("noteSplitDividerLocation", jSplitPane.getDividerLocation());
    }//GEN-LAST:event_jSplitPanePropertyChange

    private void noteListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_noteListMousePressed
        if (evt.getButton() == MouseEvent.BUTTON3) { 
            noteList.setSelectedIndex(noteList.locationToIndex(evt.getPoint()));
            JPopupMenu menu = new JPopupMenu();
            final NoteThumb note = getSelectedThumb();
            final Entity entity = note.entity;
            if (entity == null) {
                return;
            }
            Entity[] ents = PropertyXRef.getReferences(note.entity);
            if (note.isNote && ents.length > 0) {
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
            } else if (!note.isNote) {
                JMenuItem menuItem = new JMenuItem(NbBundle.getMessage(getClass(), "EditEntity", entity.toString(true)));
                menu.add(menuItem);
                menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        edit(entity);
                    }
                });
            }
            menu.show(noteList, evt.getX(), evt.getY()); 
        }
    }//GEN-LAST:event_noteListMousePressed

    private void edit(Entity entity) {
        cancelButton.doClick();
        SelectionDispatcher.fireSelection(new Context(entity));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filterLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPaneNote;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JList noteList;
    private javax.swing.JScrollPane noteScrollPane;
    private javax.swing.JTextArea noteText;
    private javax.swing.JLabel noteTitle;
    private javax.swing.JTextField textFilter;
    // End of variables declaration//GEN-END:variables

    
    
    
    private void createNoteThumbs() {
        
        // Clear note list
        allNote.clear();
        
        // Add new note
        allNote.add(new NoteThumb());

        // Get all note throughout the whole gedcom, excluding those underneath SOUR only
        String[] ENTITIES = { Gedcom.INDI, Gedcom.FAM, Gedcom.SUBM };
        for (String type : ENTITIES) {
            Collection<Entity> entities = (Collection<Entity>) gedcom.getEntities(type);
            for (Entity entity : entities) {
                Property[] properties = entity.getProperties("NOTE");
                for (Property noteProp : properties) {
                    if (noteProp == null) {
                        continue;
                    }
                    if (noteProp instanceof PropertyNote) {
                        continue; //text = ((Note) ((PropertyNote) noteProp).getTargetEntity()).getValue().trim();
                    }
                    String text = noteProp.getValue().trim();
                    if (text.isEmpty()) {
                        continue;
                    }
                    NoteThumb note = new NoteThumb(noteProp, entity, text);
                    allNote.add(note);
                }
            }
        }
        
        // Get all note entities (NOTE)
        Collection<Note> entities = (Collection<Note>) gedcom.getEntities(Gedcom.NOTE);
        for (Note entity : entities) {
            if (entity == null || isSourceOnly(entity)) {
                continue;
            }
            String text = entity.getValue().trim();
            NoteThumb note = new NoteThumb(entity, entity, text);
            Entity[] ents = PropertyXRef.getReferences(entity);
            note.setUnused(ents.length == 0);
            allNote.add(note);
        }
        
    }

    private void displayNoteThumbs() {
        // Put them in model in sorted order
        filteredModel.clear();
        for (NoteThumb item : allNote) {
            filteredModel.addElement(item);
        }
        
    }

    private NoteThumb getSelectedThumb() {
        return (NoteThumb) filteredModel.get(noteList.getSelectedIndex());
    }
    
    public boolean isSelectedEntityNote() {
        NoteThumb note = getSelectedThumb();
        return note == null ? false : note.isNote;
    }

    public Entity getSelectedEntity() {
        NoteThumb note = getSelectedThumb();
        return note == null ? null : note.entity;
    }

    public String getSelectedText() {
        NoteThumb note = getSelectedThumb();
        return note == null ? "" : note.text;
    }


    
    
    
    
    public void filterModel(String filter) {
        noteList.clearSelection();
        noteList.setModel(new DefaultListModel());
        filteredModel.clear();
        for (NoteThumb item : allNote) {
            if (item.text.toLowerCase().contains(filter.toLowerCase())) {
                filteredModel.addElement(item);
            }
        }
        noteList.setModel(filteredModel);
    }    

    private boolean isSourceOnly(Note entity) {
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

    public int getNbNotes() {
        return allNote.size();
    }

  
    
    
    private static class ListEntryCellRenderer extends JLabel implements ListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            NoteThumb entry = (NoteThumb) value;

            setHorizontalTextPosition(JLabel.CENTER);
            setVerticalTextPosition(JLabel.TOP);
            setVerticalAlignment(JLabel.TOP);
            setHorizontalAlignment(JLabel.CENTER);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredSoftBevelBorder())));

            int labelWidth = THUMB_WIDTH - 8;
            int labelHeight = THUMB_HEIGHT - 8;  
            int nbLines = labelHeight / 24 ; // 12 pixels per line for font size 10 set in component netbeans parameters
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
            // blue  : private note, used
            // red   : not used
            String color = entry.isNote && !entry.isUnused ? "black" : !entry.isNote && !entry.isUnused ? "blue" : "red";
            if (entry.entity == null) { // new note
                text = "<center><font size=+0><br><br><i><b>" + text + "</b></i></font></center>";
            }
            setText("<html><center><font color="+color+">" + text + "</font></center></html>");

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


    
    
    
    
    
    private class NoteThumb {
        
        public String id = "";
        public boolean isNote = false;
        public Entity entity = null;
        public String title = "";
        public String text = "";
        public boolean isUnused = false;
        
        private NoteThumb() {
            this.id = "";
            this.isNote = true;
            this.entity = null;
            this.title = NbBundle.getMessage(getClass(), "NewNoteTitle");
            this.text = NbBundle.getMessage(getClass(), "NewNoteText");
        }

        private NoteThumb(Property origin, Note entity, String text) {
            this.id = origin.getEntity().getId() + "-" + origin.getPath(true).toString();
            System.out.println("ancestris.modules.editors.standard.tools.NoteChooser.NoteThumb id="+id);
            this.isNote = true;
            this.entity = entity;
            this.title = getTitle(entity);
            this.text = text;
        }

        private NoteThumb(Property origin, Entity entity, String text) {
            this.id = entity.getId() + "-" + origin.getPath(true).toString();
            System.out.println("ancestris.modules.editors.standard.tools.NoteChooser.NoteThumb id="+id);
            this.isNote = (entity instanceof Note);
            this.entity = entity;
            this.title = getTitle(entity);
            this.text = text;
        }
        
        private void setUnused(boolean b) {
            isUnused = b;
        }

        private String getTitle(Entity entity) {
            if (entity == null) {
                return "";
            }
            if (entity instanceof Note) {
                Entity[] ents = PropertyXRef.getReferences((Note) entity);
                if (ents.length > 0) {
                    for (Entity ent : ents) {
                        return entity.getId() + "<br>" + ent.toString(true) + ((ents.length > 1) ? "<br>..." : "") ;
                    }
                }
            } else {
                return entity.toString(true);
            }
            return entity.getId();
        }

        private String getId() {
            return id;
        }
    }



    
    private class ThumbComparator implements Comparator<NoteThumb> {

        public int compare(NoteThumb o1, NoteThumb o2) {
            String ent1 = o1.entity == null ? "0" : "1";
            String ent2 = o2.entity == null ? "0" : "1";
            String title1 = o1.title;
            String title2 = o2.title;
            String total1 = ent1 + o1.text.toLowerCase() + title1;
            String total2 = ent2 + o2.text.toLowerCase() + title2;
            return total1.compareTo(total2);
        }
    }
    
}
