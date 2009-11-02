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
package genj.app;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomDirectory;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.SortableTableModel;
import genj.view.ContextProvider;
import genj.view.ContextSelectionEvent;
import genj.view.ViewContext;
import genj.view.ViewManager;
import genj.window.WindowBroadcastEvent;
import genj.window.WindowBroadcastListener;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * A component displaying a list of Gedcoms
 */
/*package*/ class GedcomTableWidget extends JTable implements ContextProvider, WindowBroadcastListener {
  
  /** default column widths */
  private static final int defaultWidths[] = {
    96, 24, 24, 24, 24, 24, 24, 24, 48
  };
  
  private static final Object COLUMNS[] = {
    Resources.get(GedcomTableWidget.class).getString("cc.column_header.name"),
    Gedcom.getEntityImage(Gedcom.INDI),
    Gedcom.getEntityImage(Gedcom.FAM),
    Gedcom.getEntityImage(Gedcom.OBJE), 
    Gedcom.getEntityImage(Gedcom.NOTE), 
    Gedcom.getEntityImage(Gedcom.SOUR), 
    Gedcom.getEntityImage(Gedcom.SUBM), 
    Gedcom.getEntityImage(Gedcom.REPO),
    Grammar.V55.getMeta(new TagPath("INDI:CHAN")).getImage()
  };

  /** a registry */
  private Registry registry;
  
  /**
   * Constructor
   */
  public GedcomTableWidget(ViewManager mgr, Registry reGistry) {

    registry = reGistry;
    
    // prepare model
    GedcomTableModel model = new GedcomTableModel();
    
    // Prepare a column model
    TableColumnModel cm = new DefaultTableColumnModel();
    for (int h=0; h<COLUMNS.length; h++) {
      TableColumn col = new TableColumn(h);
      col.setHeaderValue(COLUMNS[h]);
      col.setWidth(defaultWidths[h]);
      col.setPreferredWidth(defaultWidths[h]);
      cm.addColumn(col);
    }
    setModel(new SortableTableModel(model, getTableHeader()));
    setColumnModel(cm);

    // change looks    
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    getTableHeader().setReorderingAllowed(false);
    
    // grab the preferred columns
    int[] widths = registry.get("columns",new int[0]);
    for (int c=0, max=getColumnModel().getColumnCount(); c<widths.length&&c<max; c++) {
      TableColumn col = getColumnModel().getColumn(c);
      col.setPreferredWidth(widths[c]);
      col.setWidth(widths[c]);
    }    
    
    // add motion listener for tooltips
    getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        int col = getColumnModel().getColumnIndexAtX(e.getX());
        String tip = col<=0||col>Gedcom.ENTITIES.length ? null : Gedcom.getName(Gedcom.ENTITIES[col-1]);
        getTableHeader().setToolTipText(tip);
      }
    });

    // done
  }
  
  public Dimension getPreferredScrollableViewportSize() {
    return new Dimension(Math.max(128, getColumnModel().getTotalColumnWidth()), Math.max(4, getModel().getRowCount())*getRowHeight());
  }
  
  /**
   * ContextProvider - callback
   */
  public ViewContext getContext() {
    int row = getSelectedRow();
    return row<0 ? null : new ViewContext(GedcomDirectory.getInstance().getGedcoms().get(row));
  }

  /**
   * A windows broadcast message
   */
  public boolean handleBroadcastEvent(WindowBroadcastEvent event) {
    
    ContextSelectionEvent cse = ContextSelectionEvent.narrow(event);
    if (cse!=null) {
      int row = GedcomDirectory.getInstance().getGedcoms().indexOf(cse.getContext().getGedcom());
      if (row>=0)
        getSelectionModel().setSelectionInterval(row,row);
    }
    
    return true;
  }

  /**
   * Hooking into the tear-down process to store our
   * settings in (set) registry
   */
  public void removeNotify() {
    // remember our layout
    int[] widths = new int[getColumnModel().getColumnCount()];
    for (int c=0; c<widths.length; c++) {
      widths[c] = getColumnModel().getColumn(c).getWidth();
    }
    registry.put("columns", widths);
    // continue
    super.removeNotify();
  }
  
  /**
   * The selected gedcom
   */
  public Gedcom getSelectedGedcom() {
    int row = getSelectedRow();
    return row<0 ? null : GedcomDirectory.getInstance().getGedcoms().get(row);
  }

  /**
   * our model
   */
  private class GedcomTableModel extends AbstractTableModel implements GedcomDirectory.Listener, GedcomMetaListener {
    
    @Override
    public void addTableModelListener(TableModelListener l) {
      // listen
      if (getTableModelListeners().length==0)
        GedcomDirectory.getInstance().addListener(this);
      // continue
      super.addTableModelListener(l);
    }
    
    @Override
    public void removeTableModelListener(TableModelListener l) {
      // continue
      super.removeTableModelListener(l);
      // listen
      if (getTableModelListeners().length==0)
        GedcomDirectory.getInstance().removeListener(this);
    }
    
    public Class<?> getColumnClass(int col) {
      return col==0 ? String.class : Integer.class;
    }

    public int getColumnCount() {
      return COLUMNS.length;
    }

    public int getRowCount() {
      return GedcomDirectory.getInstance().getGedcoms().size();
    }

    public Object getValueAt(int row, int col) {
      Gedcom gedcom = GedcomDirectory.getInstance().getGedcoms().get(row);
      switch (col) {
        case 0: return gedcom.getName() + (gedcom.hasChanged() ? "*" : "" );
        case 8: return gedcom.getLastChange();
        default: return new Integer(gedcom.getEntities(Gedcom.ENTITIES[col-1]).size());
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
      throw new IllegalArgumentException("n/a");
    }
    
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property prop) {
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
    }

    public void gedcomHeaderChanged(Gedcom gedcom) {
    }

    public void gedcomWriteLockAcquired(Gedcom gedcom) {
    }

    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    }
    
    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
    }

    public void gedcomWriteLockReleased(Gedcom gedcom) {
      int row = GedcomDirectory.getInstance().getGedcoms().indexOf(gedcom);
      if (row>=0) fireTableRowsUpdated(row,row);
    }

    public void gedcomRegistered(int pos, Gedcom gedcom) {
      gedcom.addGedcomListener(this);
      fireTableRowsInserted(pos, pos);
      getSelectionModel().setSelectionInterval(pos, pos);
    }

    public void gedcomUnregistered(int pos, Gedcom gedcom) {
      gedcom.removeGedcomListener(this);
      fireTableRowsDeleted(pos, pos);
    }
  }

}