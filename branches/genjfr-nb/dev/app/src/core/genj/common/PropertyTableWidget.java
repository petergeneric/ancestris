/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.common;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.gedcom.TagPath;
import genj.renderer.Options;
import genj.renderer.PropertyRenderer;
import genj.util.Dimension2d;
import genj.util.WordBuffer;
import genj.util.swing.Action2;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.LinkWidget;
import genj.util.swing.SortableTableModel;
import genj.view.ContextProvider;
import genj.view.ContextSelectionEvent;
import genj.view.ViewContext;
import genj.window.WindowBroadcastEvent;
import genj.window.WindowBroadcastListener;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * A widget that shows entities in rows and columns
 */
public class PropertyTableWidget extends JPanel implements WindowBroadcastListener {
  
  private final static Logger LOG = Logger.getLogger("genj.common");
  
  /** table component */
  private Table table;
  
  /** shortcuts panel */
  private JPanel panelShortcuts;
  
  /**
   * Constructor
   */
  public PropertyTableWidget() {
    this(null);
  }
  
  /**
   * Constructor
   */
  public PropertyTableWidget(PropertyTableModel propertyModel) {
    
    // create table comp
    table = new Table();
    setModel(propertyModel);
    
    // create panel for shortcuts
    panelShortcuts = new JPanel();
    
    // setup layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(table));
    add(BorderLayout.EAST, panelShortcuts);
    
    // done
  }
  
  /**
   * Accessor for table model being shown
   */
  public TableModel getTableModel() {
    return table.getModel();
  }
  
  /**
   * Component lifecycle callback - removed
   */
  public void removeNotify() {
    super.removeNotify();
    // clear table's current model - lifecycle destructor
    // so to say that will disconnect listeners recursively
    table.setPropertyModel(null);
  }
  
  /**
   * Setter for current model
   */
  public void setModel(PropertyTableModel set) {
    table.setPropertyModel(set);
  }
  
  /**
   * Set column resize behavior
   */
  public void setAutoResize(boolean on) {
    table.setAutoResizeMode(on ? JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF);
  }
  
  /**
   * Resolve row for property
   */
  public int getRow(Property property) {
    return table.getRow(property);
  }
  
  /**
   * Select a cell
   */
  public boolean handleBroadcastEvent(WindowBroadcastEvent event) {
    
    // let flow through if it's a message from ourselves
    if (event.isOutbound())
      return true;

    // a meaningful event for us?
    ContextSelectionEvent cse = ContextSelectionEvent.narrow(event, table.propertyModel.getGedcom());
    if (cse==null)
      return true;

    // set selection
    try {
      table.ignoreSelection = true;
      
      // loop over selected properties
      Context context = cse.getContext();
      Property[] props = context.getProperties();
      
      // use all of selected entities properties if there are no property selections
      if (props.length==0) {
        List all = new ArrayList();
        Entity[] ents = context.getEntities();
        for (int i = 0; i < ents.length; i++) {
          all.addAll(ents[i].getProperties(Property.class));
          all.add(ents[i]);
        }
        props = Property.toArray(all);
      }
      
      ListSelectionModel rows = table.getSelectionModel();
      ListSelectionModel cols = table.getColumnModel().getSelectionModel();
      table.clearSelection();
      
      int r=-1,c=-1;
      for (int i=0;i<props.length;i++) {
  
        Property prop = props[i];
        r = getRow(prop.getEntity());
        if (r<0)
          continue;
        c = table.getCol(r, prop);

        // change selection
        rows.addSelectionInterval(r,r);
        if (c>=0)
          cols.addSelectionInterval(c,c);
      }
      
      // scroll to last selection
      if (r>=0) {
        Rectangle visible = table.getVisibleRect();
        Rectangle scrollto = table.getCellRect(r,c,true);
        if (c<0) scrollto.x = visible.x;
        table.scrollRectToVisible(scrollto);
      }

    } finally {
      table.ignoreSelection = false;
    }
    
    // don't think anyone cares but we'll let it through
    return true;
  }
  
  /**
   * Return column layout - a string that can be used to return column widths and sorting
   */
  public String getColumnLayout() {
    
    // e.g. 4, 40, 60, 70, 48, 0, -1, 1, 1 
    // for a table with 4 columns and two sort directives
    
    SortableTableModel model = (SortableTableModel)table.getModel();
    TableColumnModel columns = table.getColumnModel();
    List directives = model.getDirectives();

    WordBuffer result = new WordBuffer(",");
    result.append(columns.getColumnCount());
    
    for (int c=0; c<columns.getColumnCount(); c++) 
      result.append(columns.getColumn(c).getWidth());
    
    for (int d=0;d<directives.size();d++) {
      SortableTableModel.Directive dir = (SortableTableModel.Directive)directives.get(d);
      result.append(dir.getColumn());
      result.append(dir.getDirection());
    }
    
    return result.toString();
  }
  
  /**
   * Set column layout
   */
  public void setColumnLayout(String layout) {
    
    SortableTableModel model = (SortableTableModel)table.getModel();
    TableColumnModel columns = table.getColumnModel();

    try {
      StringTokenizer tokens = new StringTokenizer(layout, ",");
      int n = Integer.parseInt(tokens.nextToken());
      if (n!=model.getColumnCount())
        return;
  
      for (int i=0;i<n;i++) {
        TableColumn col = columns.getColumn(i);
        int w = Integer.parseInt(tokens.nextToken());
        col.setWidth(w);
        col.setPreferredWidth(w);
      }
      
      model.cancelSorting();
      while (tokens.hasMoreTokens()) {
        int c = Integer.parseInt(tokens.nextToken());
        int d = Integer.parseInt(tokens.nextToken());
        model.setSortingStatus(c, d);
      }

    } catch (Throwable t) {
      // ignore
    }
  }
  
  /**
   * Return current sort status
   */
  public int[] getColumnDirections() {
    SortableTableModel model = (SortableTableModel)table.getModel();
    int[] result = new int[model.getColumnCount()];
    for (int i = 0; i < result.length; i++) {
      result[i] = model.getSortingStatus(i);
    }
    return result;
  }

  /**
   * Set sorted columns
   */
  public void setColumnDirections(int[] set) {
    SortableTableModel model = (SortableTableModel)table.getModel();
    for (int i = 0; i<set.length && i<model.getColumnCount(); i++) {
      model.setSortingStatus(i, set[i]);
    }
  }
  
  /**
   * Table Content
   */
  private class Table extends JTable implements ContextProvider, ListSelectionListener  {
    
    private PropertyTableModel propertyModel;
    private boolean ignoreSelection  = false;
    private SortableTableModel sortableModel = new SortableTableModel();
    
    /**
     * Constructor
     */
    Table() {

      setPropertyModel(null);
      setDefaultRenderer(Object.class, new Renderer());
      getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      getColumnModel().setColumnSelectionAllowed(true);
      getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      getTableHeader().setReorderingAllowed(false);
      
      setRowHeight((int)Math.ceil(Options.getInstance().getDefaultFont().getLineMetrics("", new FontRenderContext(null,false,false)).getHeight())+getRowMargin());
      
      getColumnModel().getSelectionModel().addListSelectionListener(this);
      getSelectionModel().addListSelectionListener(this);
      
      // prep sortable model
      setModel(sortableModel);
      sortableModel.setTableHeader(getTableHeader());
      
      // 20050721 we want the same focus forward/backwards keys as everyone else
      setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
      setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
      
      // patch selecting
      addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          // make sure something is selected but don't screw current multi-selection
          int row = rowAtPoint(e.getPoint());
          int col = columnAtPoint(e.getPoint());
          if (row<0||col<0)
            clearSelection();
          else {
            if (!isCellSelected(row, col)) {
              getSelectionModel().setSelectionInterval(row, row);
              getColumnModel().getSelectionModel().setSelectionInterval(col, col);
            }
          }
        }
      });
      
      // listen to changes for generating shortcuts
      sortableModel.addTableModelListener(new TableModelListener() {
        public void tableChanged(TableModelEvent e) {
          if (e.getLastRow()==Integer.MAX_VALUE) createShortcuts();
        }
      });
      addComponentListener(new ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
          createShortcuts();
        }
      });
      
      // done
    }
    
    /** create a shortcut */
    Action2 createShortcut(String txt, final int y, final Container container) {
      
      Action2 shortcut = new Action2(txt.toUpperCase()) {
        protected void execute() {
          int x = 0;
          try { x = ((JViewport)table.getParent()).getViewPosition().x; } catch (Throwable t) {};
          table.scrollRectToVisible(new Rectangle(x, y, 1, getParent().getHeight()));
        }
      };
      
      LinkWidget link = new LinkWidget(shortcut);
      link.setAlignmentX(0.5F);
      link.setBorder(new EmptyBorder(0,1,0,1));
      container.add(link);
      
      return shortcut;
    }
    
    /** generate */
    void createShortcuts(int col, JComponent container) {
      
      TableModel model = getModel();
      Collator collator = propertyModel.getGedcom().getCollator();

      // loop over rows
      String cursor = "";
      for (int r=0;r<model.getRowCount();r++) {
        Property prop = (Property)model.getValueAt(r, col);
        if (prop instanceof PropertyDate)
          break;
        if (prop==null)
          continue;
        
        String value = prop instanceof PropertyName ? ((PropertyName)prop).getLastName().trim() : prop.getDisplayValue().trim(); 
        if (value.length()==0)
          continue;
        value = value.substring(0,1).toLowerCase();
        
        if (collator.compare(cursor, value)>=0)
          continue;
        cursor = value;
        
        Action2 shortcut = createShortcut(value, table.getCellRect(r, col, true).y, container);
      
        // create key binding
        InputMap imap = container.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap amap = container.getActionMap();
        imap.put(KeyStroke.getKeyStroke(value.charAt(0)), shortcut);
        amap.put(shortcut, shortcut);
      }
      
      // done
    }
    
    /**
     * Create shortcuts 
     */
    void createShortcuts() {
      
      // remove old shortcuts
      panelShortcuts.removeAll();
      panelShortcuts.setLayout(new BoxLayout(panelShortcuts, BoxLayout.Y_AXIS));
      panelShortcuts.getInputMap(WHEN_IN_FOCUSED_WINDOW).clear();
      panelShortcuts.getActionMap().clear();
      panelShortcuts.revalidate();
      panelShortcuts.repaint();
      
      // anything we can offer? need ascending sorted column and at least 10 rows
      if (!sortableModel.isSorting())
        return;
      SortableTableModel.Directive directive = (SortableTableModel.Directive)sortableModel.getDirectives().get(0);
      if (directive.getDirection()<=0)
        return;
      
      createShortcuts(directive.getColumn(), panelShortcuts);

      // done
    }
    
    /**
     * look up a column for given property
     */
    int getCol(int row, Property property) {
      
      // find col
      TableModel model = getModel();
      for (int i=0, j=model.getColumnCount(); i<j; i++) {
        if (model.getValueAt(row,i)==property)
          return i;
      }
      
      // not found
      return -1;
    }
    
    /**
     * look up a row
     */
    int getRow(Property property) {
      if (propertyModel==null)
        return -1;
      SortableTableModel model = (SortableTableModel)getModel();
      for (int i=0;i<model.getRowCount();i++) {
        if (propertyModel.getProperty(model.modelIndex(i))==property)
          return i;
      }
      return -1;
    }

    /**
     * setting a property model
     */
    void setPropertyModel(PropertyTableModel propertyModel) {
      // remember
      this.propertyModel = propertyModel;
      // pass through 
      sortableModel.setTableModel(new Model(propertyModel));

    }
    
    /** 
     * ListSelectionListener - callback
     */
    public void valueChanged(ListSelectionEvent e) {
      
      // let super handle it (strange that JTable implements this as well)
      super.valueChanged(e);
      
      // propagate selection change?
      if (ignoreSelection||e.getValueIsAdjusting())
        return;

      ViewContext context = null;
      ListSelectionModel rows = getSelectionModel();
      ListSelectionModel cols  = getColumnModel().getSelectionModel();
      
      for (int r=rows.getMinSelectionIndex() ; r<=rows.getMaxSelectionIndex() ; r++) {
        for (int c=cols.getMinSelectionIndex(); c<=cols.getMaxSelectionIndex(); c++) {
          // check specific row col
          if (!rows.isSelectedIndex(r)||!cols.isSelectedIndex(c))
            continue;
          // 20050721 check arguments - Swing might not always send something smart here
          SortableTableModel model = (SortableTableModel)getModel();
          if (r<0||r>=model.getRowCount()||c<0||c>=model.getColumnCount())
            continue;
          Property prop = (Property)getValueAt(r,c);
          if (prop==null)
            prop = propertyModel.getProperty(model.modelIndex(r));
          // keep it
          if (context==null) context = new ViewContext(prop);
          else context.addProperty(prop);
        }
      }
      
      // tell about it
      if (context!=null)
        WindowManager.broadcast(new ContextSelectionEvent(context, this));

      
      // done
    }
    
    /** 
     * The Scollpane we're using asks this JTable's preferred srollable viewport size (via ViewportLayout) which strangely is
     * hardcoded to something around 400 - we want to use the preferred size though since it is caculated by JTable's
     * tablelayout depending on the number of rows. We're restricting this to 128 pixels height though.
     */ 
    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }
    
    /**
     * ContextProvider - callback 
     */
    public ViewContext getContext() {
      
      // check gedcom first
      Gedcom ged = propertyModel.getGedcom();
      if (ged==null)
        return null;
      SortableTableModel model = (SortableTableModel)getModel();
      
      // prepare result
      ViewContext result = new ViewContext(ged);
      
      // one row one col?
      int[] rows = getSelectedRows();
      if (rows.length>0) {
        int[] cols = getSelectedColumns();

        // loop over rows
        for (int r=0;r<rows.length;r++) {
          
          // loop over cols
          boolean rowRepresented = false;
          for (int c=0;c<cols.length;c++) {
            // add property for each cell
            Property p = (Property)getValueAt(rows[r], cols[c]);
            if (p!=null) {
              result.addProperty(p);
              rowRepresented = true;
            }
            // next selected col
          }
          
          // add representation for each row that wasn't represented by a property
          if (!rowRepresented)
            result.addProperty(propertyModel.getProperty(model.modelIndex(rows[r])));
          
          // next selected row
        }
      }
      
      // done
      return result;
    }
    
    /**
     * The logical model
     */
    private class Model extends AbstractTableModel implements PropertyTableModelListener {
      
      /** our model */
      private PropertyTableModel model;
      
      /** cached table content */
      private Property cells[][];
      
      /** constructor */
      private Model(PropertyTableModel set) {
        // setup state
        model = set;
        // done
      }
      
      private Gedcom getGedcom() {
        return model!=null ? model.getGedcom() : null;
      }
      
      public void handleRowsAdded(PropertyTableModel model, int rowStart, int rowEnd) {
        
        // flush cell state
        int 
          rows = model.getNumRows(), 
          cols = model.getNumCols();
        cells = new Property[rows][cols];
        
        // tell about it
        fireTableRowsInserted(rowStart, rowEnd);
      }
      
      public void handleRowsDeleted(PropertyTableModel model, int rowStart, int rowEnd) {
        // flush cell state
        int 
          rows = model.getNumRows(), 
          cols = model.getNumCols();
        cells = new Property[rows][cols];
        
        // tell about it
        fireTableRowsDeleted(rowStart, rowEnd);
      }
      
      public void handleRowsChanged(PropertyTableModel model, int rowStart, int rowEnd, int col) {
        // flush cell state
        for (int i=rowStart; i<=rowEnd; i++) 
          cells[i][col] = null;
        // tell about it
        fireTableChanged(new TableModelEvent(this, rowStart, rowEnd, col));
      }
      
      /** someone interested in us */
      public void addTableModelListener(TableModelListener l) {
        super.addTableModelListener(l);
        // start listening ?
        if (model!=null&&getListeners(TableModelListener.class).length==1)
          model.addListener(this);
      }
      
      /** someone lost interest */
      public void removeTableModelListener(TableModelListener l) {
        super.removeTableModelListener(l);
        // stop listening ?
        if (model!=null&&getListeners(TableModelListener.class).length==0)
          model.removeListener(this);
      }
      
      /**
       *  patched column name
       */
      public String getColumnName(int col) {
        return model!=null ? model.getName(col) : "";
      }
      
      /** num columns */
      public int getColumnCount() {
        return model!=null ? model.getNumCols() : 0;
      }
      
      /** num rows */
      public int getRowCount() {
        return model!=null ? model.getNumRows() : 0;
      }
      
      /** path in column */
      private TagPath getPath(int col) {
        return model!=null ? model.getPath(col) : null;
      }
      
      /** context */
      private Context getContextAt(int row, int col) {
        // nothing to do?
        if (model==null)
          return null;
        // selected property?
        Property prop = getPropertyAt(row, col);
        if (prop!=null)
          return new Context(prop);
        
        // selected row at least?
        Property root = model.getProperty(row);
        if (root!=null)
          return new Context(root.getEntity());

        // fallback
        return new Context(model.getGedcom());
      }
      
      /** property */
      private Property getPropertyAt(int row, int col) {
        
        // is cache setup?
        if (cells==null) 
          cells = new Property[model.getNumRows()][model.getNumCols()];
        
        Property prop = cells[row][col];
        if (prop==null) {
          prop = model.getProperty(row).getProperty(model.getPath(col));
          cells[row][col] = prop;
        }
        return prop;
      }
      
      /** value */
      public Object getValueAt(int row, int col) {
        return getPropertyAt(row, col);
      }
      
      /**
       * get property by row
       */
      private Property getProperty(int row) {
        return model.getProperty(row);
      }
      
    } //Model
    
    /**
     * Renderer for properties in cells
     */
    private class Renderer extends HeadlessLabel implements TableCellRenderer {
      
      /** current property */
      private Property curProp;
      
      /** table */
      private JTable curTable;
      
      /** attributes */
      private boolean isSelected;
      
      /**
       * constructor
       */
      /*package*/ Renderer() {
        setFont(Options.getInstance().getDefaultFont());
      }
      
      /**
       * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
       */
      public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focs, int row, int col) {
        // there's a property here
        curProp = (Property)value;
        curTable = table;
        // and some status
        isSelected = selected;
        // ready
        return this;
      }
      
      /**
       * patched preferred size
       */
      public Dimension getPreferredSize() {
        if (curProp==null)
          return new Dimension(0,0);
        return Dimension2d.getDimension(PropertyRenderer.get(curProp).getSize(getFont(), new FontRenderContext(null, false, false), curProp, new HashMap(), Options.getInstance().getDPI()));
      }
      
      /**
       * @see genj.util.swing.HeadlessLabel#paint(java.awt.Graphics)
       */
      public void paint(Graphics g) {
        Graphics2D graphics = (Graphics2D)g;
        // our bounds
        Rectangle bounds = getBounds();
        bounds.x=0; bounds.y=0;
        // background?
        if (isSelected) {
          g.setColor(curTable.getSelectionBackground());
          g.fillRect(0,0,bounds.width,bounds.height);
          g.setColor(curTable.getSelectionForeground());
        } else {
          g.setColor(curTable.getForeground());
        }
        // no prop and we're done
        if (curProp==null) 
          return;
        // set font
        g.setFont(getFont());
        // get the proxy
        PropertyRenderer proxy = PropertyRenderer.get(curProp);
        // add some space left and right
        bounds.x += 1;
        bounds.width -= 2;
        // let it render
        proxy.render(graphics, bounds, curProp, new HashMap(), Options.getInstance().getDPI());
        // done
      }
      
    } //PropertyTableCellRenderer
    
  } //Content

  
  
  
//  /**
//   * A generator for shortcuts to names
//   */
//  private class NameSG extends ValueSG {
//
//    /** collect first letters of names */
//    Set keys(int col) {
//      
//      // check first letter of lastnames
//      Set letters = new TreeSet();
//      for (Iterator names = PropertyName.getLastNames(table.propertyModel.getGedcom(), false).iterator(); names.hasNext(); ) {
//        String name = names.next().toString();
//        if (name.length()>0) {
//          char c = name.charAt(0);
//          if (Character.isLetter(c))
//            letters.add(String.valueOf(Character.toUpperCase(c)));
//        }
//      }
//      // done
//      return letters;
//    }
//    
//    /** create item */
//    protected Property prop(String key) {
//      return new PropertyName("", key);
//    }
//  } //NameShortcutGenerator
//  
//  /**
//   * A generator for shortcuts to years
//   */
//  private class DateSG extends ShortcutGenerator {
//    /** generate */
//    void generate(int col, JComponent container) {
//      
//      // how many text lines fit on screen?
//      int visibleRows = Math.max(0, getHeight() / new LinkWidget("9999",null).getPreferredSize().height);
//      TableModel model = table.getModel();
//      int rows = model.getRowCount();
//      if (rows>visibleRows) try {
//        
//        // find all applicable years
//        Set years = new TreeSet();
//        for (int row=0;row<rows;row++) {
//          PropertyDate date = (PropertyDate)model.getValueAt(row, col);
//          if (date==null || !date.getStart().isValid())
//            continue;
//          try {
//            years.add(new Integer(date.getStart().getPointInTime(PointInTime.GREGORIAN).getYear()));
//          } catch (Throwable t) {
//          }
//        }
//        
//        // generate shortcuts for all years
//        Object[] ys = years.toArray();
//        for (int y=0; y<visibleRows; y++) {
//          int index = y<visibleRows-1  ?   (int)( y * ys.length / visibleRows)  : ys.length-1;
//          int year = ((Integer)ys[index]).intValue();
//          add(new PropertyDate(year), container);
//        }
//
//      } catch (Throwable t) {
//      }
//      
//      // done
//    }
//  } //DateShortcutGenerator
  
} //PropertyTableWidget
