package ancestris.modules.editors.placeeditor.topcomponents;

import ancestris.core.beans.ConfirmChangeWidget;
import ancestris.modules.editors.geoplace.PlaceEditorPanel;
import ancestris.modules.editors.placeeditor.PlaceEditor;
import ancestris.modules.editors.placeeditor.models.GedcomPlaceTableModel;
import ancestris.util.EventUsage;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.FileChooserBuilder;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import ancestris.view.SelectionDispatcher;
import genj.edit.actions.SetPlaceHierarchyAction;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.UnitOfWork;
import genj.util.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static java.awt.event.MouseEvent.BUTTON3;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public final class PlacesListTopComponent extends AncestrisTopComponent implements ExplorerManager.Provider, GedcomMetaListener, ConfirmChangeWidget.ConfirmChangeCallBack  {

    final static Logger LOG = Logger.getLogger("ancestris.editor");
    private genj.util.Registry registry = null;

    static final String ICON_PATH = "ancestris/modules/editors/placeeditor/actions/Place.png";

    private Gedcom gedcom = null;
    private GedcomPlaceTableModel gedcomPlaceTableModel;
    private TableRowSorter<TableModel> placeTableSorter;
    private PlaceEditorPanel placesEditor = null;
	
	private ConfirmChangeWidget confirmPanel;

    private boolean isBusyCommitting = false;
	private boolean updateTable = false; // flag to regroup all external gedcom updates in one refresh only
    private UndoRedoListener undoRedoListener;

	private String actionTextEdit = "PlacesListTopComponent.edit.menu";
    private KeyStroke actionKSEdit = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK);

    private String actionTextEvents = "PlacesListTopComponent.events.menu";
    private KeyStroke actionKSEvents = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);

    private AbstractAction actionFormat = null;
    

    public PlacesListTopComponent() {
        super();
    }
    
    
    @Override
    public void componentOpened() {
        // Set undo/redo
        undoRedoListener = new UndoRedoListener();
        UndoRedo undoRedo = getUndoRedo();
        undoRedo.addChangeListener(undoRedoListener);
        gedcom.addGedcomListener(this);
    }

    @Override
    public void componentClosed() {
        placeTable.removeChangeListener(confirmPanel);
        super.componentClosed();
        gedcomPlaceTableModel.eraseModel();
        gedcom.removeGedcomListener(this);
    }

    
    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.OUTPUT;
    }

    @Override
    public Image getImageIcon() {
        return ImageUtilities.loadImage(ICON_PATH, true);
    }

    @Override
    public void setName() {
        setName(NbBundle.getMessage(getClass(), "CTL_PlacesTableTopComponent"));
    }

    @Override
    public void setToolTipText() {
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_PlacesTableTopComponent"));
    }

    @Override
    public boolean createPanel() {

        this.gedcom = getGedcom();
        registry = gedcom.getRegistry();
        gedcomPlaceTableModel = new GedcomPlaceTableModel(gedcom);

        initComponents();

        // Add confirm panel
        if (confirmPanel == null) {
            confirmPanel = new ConfirmChangeWidget(this);
            confirmPanel.setChanged(false);
            placeHolderPanel.add(confirmPanel, BorderLayout.PAGE_END);
        }
        
        String city = PropertyPlace.getCityTag(gedcom);
        String memoField = registry.get("placeTableFilter", city);
        if (!memoField.isEmpty()) {
            searchPlaceComboBox.setSelectedItem(memoField);
        }

        // Create global actions
        createGlobalActions();
        
        // Init table with values
        updateGedcomPlaceTable();
        placeTable.setID(gedcom, PlacesListTopComponent.class.getName(), searchPlaceComboBox.getSelectedIndex()-1);
        placeTableSorter = placeTable.getSorter();
        
        // Mouse listener and popup menu
        placeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2 && e.isControlDown()) {
                    editPlace(placeTable.getSelectedRow());
                }
                if (e.getClickCount() == 1 && (e.isPopupTrigger() || e.getButton() == BUTTON3)) {
                    JTable source = (JTable)e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int col = source.columnAtPoint(e.getPoint());
                    createPopupMenu(e.getComponent(), e.getPoint().x, e.getPoint().y, row, col);
                }
            }
        });

		// Focus
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                filterGedcomPlaceTextField.requestFocusInWindow();
				placeTable.addChangeListener(confirmPanel);  // listen to edits
            }
        });

        return true; // registers the AncestrisTopComponent name, tooltip and gedcom context as it continues the code within AncestrisTopComponent
    }

    private void createGlobalActions() {

        // Filter
        filterGedcomPlaceButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "FILTER");
        filterGedcomPlaceButton.getActionMap().put("FILTER", new AbstractAction("FILTER") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                filterGedcomPlaceButtonActionPerformed(evt);
            }
        });
        
        // Edit places
        Action actionEdit = new AbstractAction(NbBundle.getMessage(getClass(), actionTextEdit, ""), new ImageIcon(PlaceEditor.class, "actions/Geo16.png")) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (placeTable.getSelectedRow() >=0) {
                    editPlace(placeTable.getSelectedRow());
                }
            }
        };
        getActionMap().put(actionTextEdit, actionEdit);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(actionKSEdit, actionTextEdit); 
        
        // Change place format
        actionFormat = new SetPlaceHierarchyAction() {
            @Override
            public boolean isEnabled() {
                return true;
            }
            @Override
            public String getOriginalPlaceFormat() {
                return gedcom.getPlaceFormat();
            }
        };
        
        
        // Show random event
        Action actionEvent = new AbstractAction(NbBundle.getMessage(getClass(), actionTextEvents, ""), new ImageIcon(PlaceEditor.class, "actions/Events16.png")) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (placeTable.getSelectedRow() >=0) {
                    Property event = getPlacesFromRow(placeTable.getSelectedRow()).iterator().next().getParent();
                    SelectionDispatcher.fireSelection(new Context(event));
                }
            }
        };
        getActionMap().put(actionTextEvents, actionEvent);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(actionKSEvents, actionTextEvents);

    }
    
    
    
    private void createPopupMenu(Component component, int x, int y, int row, int col) {
        JPopupMenu popup = new JPopupMenu();
        
        // Add CCP actions
        for (Action action : placeTable.getActions()) {
            popup.add(new JMenuItem(action));
        }
        
        popup.addSeparator();
        
        // Add specific actions
        // - Edit place with place editor
        String place = ((GedcomPlaceTableModel) placeTable.getModel()).getValueAt(placeTable.convertRowIndexToModel(row)).iterator().next().getDisplayValue();
        Action action = new AbstractAction(NbBundle.getMessage(getClass(), actionTextEdit, place), new ImageIcon(PlaceEditor.class, "actions/Geo16.png")) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                editPlace(row);
            }
        };
        action.putValue(AbstractAction.ACCELERATOR_KEY, actionKSEdit);
        JMenuItem me = new JMenuItem(action);
        me.setToolTipText(NbBundle.getMessage(getClass(), actionTextEdit+".tip"));
        popup.add(me);
        
        // - Change place format
        popup.add(new JMenuItem(actionFormat));
        
        popup.addSeparator();
        
        // - Show list of corresponding events
        JMenu m = new JMenu(NbBundle.getMessage(getClass(), actionTextEvents));
        popup.add(m);
        Set<PropertyPlace> propertyPlaces = getPlacesFromRow(row);
        Property keyEvent = propertyPlaces.iterator().next().getParent();
        List<Property> events = new ArrayList<>();
        for (PropertyPlace pPlace : propertyPlaces) {
            events.add(pPlace.getParent());
        }
        Collections.sort(events, sortEvents);
        for (Property event : events) {
            String displayEvent = event.getPropertyName() + " - " + event.getEntity().toString();
            Action actionEvent = new AbstractAction(displayEvent, event.getImage()) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    SelectionDispatcher.fireSelection(new Context(event));
                }
            };
            m.add(new JMenuItem(actionEvent));
            if (event == keyEvent) {
                actionEvent.putValue(AbstractAction.ACCELERATOR_KEY, actionKSEvents);
            }
        }
        
        
        // If the clicked cell was not in the slected range, select the clicked cell
        int[] rows = placeTable.getSelectedRows();
        int[] cols = placeTable.getSelectedColumns();
        int minRow = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        for (int r : rows) {
            if (r > maxRow) {
                maxRow = r;
            }
            if (r < minRow) {
                minRow = r;
            }
        }
        for (int c : cols) {
            if (c > maxCol) {
                maxCol = c;
            }
            if (c < minCol) {
                minCol = c;
            }
        }
        if (row < minRow || row > maxRow || col < minCol || col > maxCol) {
            placeTable.changeSelection(row, col, false, false);
        }

        // Show popup menu
        popup.show(component, x, y);
    }

    

    private void updateGedcomPlaceTable() {
        
        // Memorise what cell & place was selected
        String[] line = null;
        int row = placeTable.getSelectedRow();
        int col = placeTable.getSelectedColumn();
        if (row >=0) {
            line = getLineArrayFromRow(row);
        }
        
        // Updatetable
        gedcomPlaceTableModel.update();
        nbPlaces.setText(NbBundle.getMessage(getClass(), "PlacesListTopComponent.nbPlaces.text", gedcomPlaceTableModel.getRowCount()));
        if (placeTableSorter != null) {
            placeTableSorter.sort();
        }
        
        // Select memorized row
        if (line != null) {
            row = getRowFromLineArray(line);
        } else {
            row = 0;
            col = 0;
        }
        placeTable.setRowSelectionInterval(row, row);
        placeTable.setColumnSelectionInterval(col, col);
        Rectangle cellRect = placeTable.getCellRect(row, col, true);
        placeTable.scrollRectToVisible(cellRect);
    }
    
    private void editPlace(int row) {
        
        // Ensure no change is in progress
        if (confirmPanel.hasChanged()) {
            DialogManager.create(NbBundle.getMessage(getClass(), "TITL_EditPending"), NbBundle.getMessage(getClass(), "MSG_EditPending")).setMessageType(DialogManager.WARNING_MESSAGE).show();
            return;
        }
        
        final Set<PropertyPlace> propertyPlaces = getPlacesFromRow(row);
        placesEditor = new PlaceEditorPanel();
        placesEditor.set(gedcom, propertyPlaces);
        final boolean search = propertyPlaces.iterator().next().getLatitude(true) == null;
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                if (search) {
                    placesEditor.runSearch();
                }
                JButton OKButton = new JButton(NbBundle.getMessage(getClass(), "Button_Ok"));
                JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
                Object[] options = new Object[]{OKButton, cancelButton};
                Object o = DialogManager.create(NbBundle.getMessage(PlaceEditorPanel.class, "PlaceEditorPanel.edit.all", propertyPlaces.iterator().next().getGeoValue()), placesEditor).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
                placesEditor.close();
                if (o == OKButton) {
                    placeEditorcommit();
                    updateGedcomPlaceTable();
                    confirmPanel.setChanged(false);
                }
            }
        });
    }
    
    private Set<PropertyPlace> getPlacesFromRow(int row) {
        int rowIndex = placeTable.convertRowIndexToModel(row);
        return ((GedcomPlaceTableModel) placeTable.getModel()).getValueAt(rowIndex);
    }
    
    private String[] getLineArrayFromRow(int row) {
        int rowIndex = placeTable.convertRowIndexToModel(row);
        String[] line = new String[placeTable.getColumnCount()];
        for (int col = 0; col < placeTable.getColumnCount(); col++) {
            line[col] = (String) ((GedcomPlaceTableModel) placeTable.getModel()).getValueAt(rowIndex, col);
        }
        return line;
    }
    
    private int getRowFromLineArray(String[] line) {
        int row = 0;
        boolean found = false;
        for (; row < placeTable.getRowCount(); row++) {
            int i = 0;
            for (int col = 0; col < placeTable.getColumnCount(); col++) {
                String debug = (String) placeTable.getValueAt(row, col);
                if (!line[col].equals((String) placeTable.getValueAt(row, col))) {
                    break;
                }
                i++;
            }
            if (i == placeTable.getColumnCount()) {
                found = true;
                break;
            }
        }
        return found ? row : 0;
    }    

    private void newFilter(String filter) {
        RowFilter<TableModel, Integer> rf;
        // If current expression doesn't parse, don't update.
        try {
            if (searchPlaceComboBox.getSelectedIndex() == 0) {
                rf = RowFilter.regexFilter("(?i)" + filter);
            } else {
                rf = RowFilter.regexFilter("(?i)" + filter, searchPlaceComboBox.getSelectedIndex()-1);
            }
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }

        placeTableSorter.setRowFilter(rf);
    }
    

    /**
     * Create RegExp tooltiptext
     */
    private String createToolTipText() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("<html><strong>");
        sb.append(NbBundle.getMessage(getClass(), "PlacesListTopComponent.filterGedcomPlaceTextField.toolTipText"));
        sb.append("&nbsp;:</strong><br><table>");
        String str = "";
        for (int i = 0;; i++) {
            // check text and pattern
            try {
            str = NbBundle.getMessage(getClass(), "regexp." + i);
            } catch (MissingResourceException ex) {
                break; 
            }
            String bits[] = str.split(";");
            if (bits.length != 2) {
                continue;
            }
            sb.append("<tr><td>&bull;&nbsp;");
            sb.append(bits[0]);
            sb.append("</td><td>&rarr;&nbsp;&nbsp;");
            sb.append(bits[1]);
            sb.append("</td></tr>");
        }
        sb.append("</table></html>");
        return sb.toString();
    }

    	

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchPlaceLabel = new javax.swing.JLabel();
		searchPlaceComboBox = new javax.swing.JComboBox();
        filterGedcomPlaceTextField = new javax.swing.JTextField();
        filterGedcomPlaceButton = new javax.swing.JButton();
        clearFilterGedcomPlaceButton = new javax.swing.JButton();
        nbPlaces = new javax.swing.JLabel();
        jBDownload = new javax.swing.JButton();
		jScrollPane1 = new javax.swing.JScrollPane();
        placeTable = new ancestris.modules.editors.placeeditor.topcomponents.EditorTable();
        placeHolderPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();

        org.openide.awt.Mnemonics.setLocalizedText(searchPlaceLabel, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.searchPlaceLabel.text")); // NOI18N

		String[] criteria = new String[PropertyPlace.getFormat(gedcom).length + 1];
        criteria[0] = "*";
        int pos = 1;
        for (String element : PropertyPlace.getFormat(gedcom)) {
            criteria[pos] = element;
            pos++;
        }
        searchPlaceComboBox.setModel(new DefaultComboBoxModel(criteria));
        searchPlaceComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.searchPlaceComboBox.toolTipText")); // NOI18N
        searchPlaceComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchPlaceComboBoxItemStateChanged(evt);
            }
        });

        filterGedcomPlaceTextField.setText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.filterGedcomPlaceTextField.text")); // NOI18N
        filterGedcomPlaceTextField.setToolTipText(createToolTipText());
        filterGedcomPlaceTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                filterGedcomPlaceTextFieldKeyTyped(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(filterGedcomPlaceButton, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.filterGedcomPlaceButton.text")); // NOI18N
        filterGedcomPlaceButton.setToolTipText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.filterGedcomPlaceButton.toolTipText")); // NOI18N
        filterGedcomPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterGedcomPlaceButtonActionPerformed(evt);
            }
        });

        clearFilterGedcomPlaceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/placeeditor/actions/Reset.png"))); // NOI18N
		org.openide.awt.Mnemonics.setLocalizedText(clearFilterGedcomPlaceButton, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.clearFilterGedcomPlaceButton.text")); // NOI18N
        clearFilterGedcomPlaceButton.setToolTipText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.clearFilterGedcomPlaceButton.toolTipText")); // NOI18N
        clearFilterGedcomPlaceButton.setPreferredSize(new java.awt.Dimension(29, 27));
		clearFilterGedcomPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFilterGedcomPlaceButtonActionPerformed(evt);
            }
        });

        nbPlaces.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(nbPlaces, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.nbPlaces.text")); // NOI18N
        nbPlaces.setToolTipText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.nbPlaces.toolTipText")); // NOI18N

        jBDownload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/placeeditor/actions/Download.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jBDownload, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.jBDownload.text")); // NOI18N
        jBDownload.setToolTipText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.jBDownload.toolTipText")); // NOI18N
        jBDownload.setMinimumSize(new java.awt.Dimension(29, 25));
        jBDownload.setPreferredSize(new java.awt.Dimension(29, 27));
        jBDownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBDownloadActionPerformed(evt);
            }
        });

        placeTable.setAutoCreateRowSorter(true);
        placeTable.setModel(gedcomPlaceTableModel);
        placeTable.setCellSelectionEnabled(true);
        jScrollPane1.setViewportView(placeTable);

        placeHolderPanel.setLayout(new java.awt.BorderLayout());

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/placeeditor/actions/PlaceFormat.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.jButton1.toolTipText")); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(29, 27));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(10, 27));

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setPreferredSize(new java.awt.Dimension(5, 27));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addComponent(placeHolderPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchPlaceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchPlaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterGedcomPlaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterGedcomPlaceButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearFilterGedcomPlaceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBDownload, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(nbPlaces)
				.addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(searchPlaceLabel)
                    .addComponent(searchPlaceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterGedcomPlaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filterGedcomPlaceButton)
                    .addComponent(clearFilterGedcomPlaceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nbPlaces)
                    .addComponent(jBDownload, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(placeHolderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void filterGedcomPlaceTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterGedcomPlaceTextFieldKeyTyped
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            newFilter(filterGedcomPlaceTextField.getText());
        }
    }//GEN-LAST:event_filterGedcomPlaceTextFieldKeyTyped

    private void filterGedcomPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterGedcomPlaceButtonActionPerformed
        newFilter(filterGedcomPlaceTextField.getText());
    }//GEN-LAST:event_filterGedcomPlaceButtonActionPerformed

    private void clearFilterGedcomPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFilterGedcomPlaceButtonActionPerformed
        filterGedcomPlaceTextField.setText("");
        newFilter(filterGedcomPlaceTextField.getText());
    }//GEN-LAST:event_clearFilterGedcomPlaceButtonActionPerformed

    private void searchPlaceComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_searchPlaceComboBoxItemStateChanged
        registry.put("placeTableFilter", (String) searchPlaceComboBox.getSelectedItem());
    }//GEN-LAST:event_searchPlaceComboBoxItemStateChanged

    private void jBDownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBDownloadActionPerformed
        File file  = new FileChooserBuilder(PlacesListTopComponent.class)
                    .setTitle(NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.export"))
                    .setApproveText(NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.export"))
                    .setFileHiding(true)
                    .setParent(this)
                    .setFileFilter(new FileNameExtensionFilter(NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.export.filter.text"),"txt","csv"))
                    .setDefaultExtension(FileChooserBuilder.getTextFilter().getExtensions()[0])
                    .setDefaultBadgeProvider()
                    .setDefaultWorkingDirectory(new File(System.getProperty("user.home")))
                    .showSaveDialog(true);
            if (file == null) {
                return;
            }
            try {
                tsvExport(file);
            } catch (IOException e) {
                DialogManager.createError(NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.export"),
                        NbBundle.getMessage(PlacesListTopComponent.class, "PlacesListTopComponent.export.error", file.getAbsoluteFile())).show();
            }
    }//GEN-LAST:event_jBDownloadActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        changePlaceFormat();
    }//GEN-LAST:event_jButton1ActionPerformed

    
    public void tsvExport(File file) throws IOException {
        final FileWriter writer = new FileWriter(file);

        for (int i = 0; i < gedcomPlaceTableModel.getColumnCount(); i++) {
            writer.write(gedcomPlaceTableModel.getColumnName(i) + "\t");
        }

        writer.write("\n");

        for (int r = 0; r < placeTableSorter.getViewRowCount(); r++) {
            for (int col = 0; col < gedcomPlaceTableModel.getColumnCount(); col++) {
                writer.write(exportCellValue(gedcomPlaceTableModel.getValueAt(placeTable.convertRowIndexToModel(r), col), r, col));
                writer.write("\t");
            }
            writer.write("\n");
        }
        writer.close();
    }
    
    private String exportCellValue(Object object, int row, int col) {
        if (object == null) {
            return "";
        }
        return object.toString();
    }

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearFilterGedcomPlaceButton;
    private javax.swing.JButton filterGedcomPlaceButton;
    private javax.swing.JTextField filterGedcomPlaceTextField;
    private javax.swing.JButton jBDownload;
	private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel nbPlaces;
	private javax.swing.JPanel placeHolderPanel;
    private ancestris.modules.editors.placeeditor.topcomponents.EditorTable placeTable;
    private javax.swing.JComboBox searchPlaceComboBox;
    private javax.swing.JLabel searchPlaceLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Update table in case of a change of gedcom somwhere else in Ancestris
     * @param gedcom
     * @param entity 
     */
    @Override
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        if (!updateTable && !entity.getProperties(PropertyPlace.class).isEmpty()) {
            updateTable = true;
        }
    }

    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        if (!updateTable && !entity.getProperties(PropertyPlace.class).isEmpty()) {
            updateTable = true;
        }
    }

    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        if (!updateTable && property.getTag().equals("PLAC")) {
            updateTable = true;
        }
    }

    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        if (!updateTable && property.getTag().equals("PLAC")) {
            updateTable = true;
        }
    }

    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        if (!updateTable && property.getTag().equals("PLAC")) {
            updateTable = true;
        }
    }

    @Override
    public void gedcomHeaderChanged(Gedcom gedcom) {
    }

    @Override
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
        updateTable = false;
    }

    @Override
    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    }

    @Override
    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
    }

    @Override
    public void gedcomWriteLockReleased(Gedcom gedcom) {
        if (updateTable) {
            updateGedcomPlaceTable();
            updateTable = false;
        }
		confirmPanel.setChanged(false);
    }

    /**
     * Launches the change place format panel for the whole Gedcom
     */
    private void changePlaceFormat() {
        actionFormat.actionPerformed(new ActionEvent(this, 0, "format-change"));
    }
    
    /**
     * Take the new data model and perform the corresponding changes in the gedcom file
     */
    private void performPlaceChanges() {
        gedcomPlaceTableModel.setGeoPlacesFromModel();
    }

    
    /**
     * Specific commit to allow multiple changes
     * (change must be committed after ok in the editor to be able to use it for another place)
     * 
     */
    private void placeEditorcommit() {
        // Is busy committing ?
        if (isBusyCommitting) {
            return;
        }
        isBusyCommitting = true;
        try {
            if (gedcom.isWriteLocked()) {
                placesEditor.commit();
            } else {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        placesEditor.commit();
                    }
                });
            }

        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error committing editor", t);
        } finally {
            isBusyCommitting = false;
        }
    }
    
    // Change listener - start
    @Override
    public void okCallBack(ActionEvent event) {
        commit(false);
    }

    @Override
    public void cancelCallBack(ActionEvent event) {
        // memorize selected cell
        int row = placeTable.getSelectedRow();
        int col = placeTable.getSelectedColumn();
        updateGedcomPlaceTable();
        // reposition selected cell
        placeTable.resetPendingPaste();
        placeTable.changeSelection(row, col, false, false);
        confirmPanel.setChanged(false);
    }

    /**
     * commit of data modifications directly made in the table
     * @param ask 
     */
    @Override
    public void commit(boolean ask) {
        // Is busy committing ?
        if (isBusyCommitting) {
            return;
        }

        // Changes?
        if (confirmPanel == null || !confirmPanel.hasChanged()) {
            return;
        }

        // We only consider committing IF we're still in a visible top level ancestor (window)
        if (!isOpen) {
            return;
        }

        // Do not commit for auto commit
        if (ask && !confirmPanel.isCommitChanges()) {
            //TODOÂ cancel();
            confirmPanel.setChanged(false);
            return;
        }

        isBusyCommitting = true;
        try {

            if (gedcom.isWriteLocked()) {
                if (!confirmPanel.hasChanged()) { // only commit changes from other modules (eg: undo) if confirm is off (do not automatically commit a pending change for the user)
                    performPlaceChanges();
                }
            } else {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        performPlaceChanges();
                    }
                });
            }

        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error committing editor", t);
        } finally {
            confirmPanel.setChanged(false);
            isBusyCommitting = false;
        }
    }
	// Change listener - end

	/**
     * Update table in case of UNDO REDO somewhere else in Ancestris
     */
    private class UndoRedoListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            updateGedcomPlaceTable();
        }
    }

    /**
     * Event sorter
     */
    private static Map<String, EventUsage> eventUsages = null;
    private static void initEventUsages() {
        eventUsages = new HashMap<String, EventUsage>();
        EventUsage.init(eventUsages);
    }


    /**
     * Comparator to sort events
     */
    public static Comparator<Property> sortEvents = new Comparator<Property>() {

        public int compare(Property o1, Property o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return +1;
            }
            if (o2 == null) {
                return -1;
            }
            if (eventUsages == null) {
                initEventUsages();
            }
            EventUsage eu1 = eventUsages.get(o1.getTag());
            EventUsage eu2 = eventUsages.get(o2.getTag());
            if (eu1 == null) {
                return +1;
            }
            if (eu2 == null) {
                return -1;
            }
            String s1 = eu1.getOrder() + o1.getDisplayValue();
            String s2 = eu2.getOrder() + o2.getDisplayValue();
            return s1.compareTo(s2);
        }
    };

    
}
