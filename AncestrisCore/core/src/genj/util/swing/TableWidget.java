/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
package genj.util.swing;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * A row and column based table
 */
public class TableWidget<ROW> extends JTable {
  
  private List<Column> columns = new ArrayList<Column>();
  private Mouser mouser = new Mouser();
  
  public TableWidget() {
    super(new DefaultTableModel(), new DefaultTableColumnModel());
    setModel(new Model());
    
    setPreferredScrollableViewportSize(new Dimension(32,32));
    setRowSelectionAllowed(true);
    setColumnSelectionAllowed(false);
    setFillsViewportHeight(true);
    getTableHeader().setReorderingAllowed(false);
    addMouseListener(mouser);
    addMouseMotionListener(mouser);
    
  }
  
  @Override
  public TableCellRenderer getCellRenderer(int row, int column) {
    Object val = getValueAt(row, column);
    if (val instanceof Action2) 
      return new ActionRenderer();
    return super.getCellRenderer(row, column);
  }
  
  @Override
  public String getToolTipText(MouseEvent event) {
    int col = columnAtPoint(event.getPoint());
    int row = rowAtPoint(event.getPoint());
    if (col<0||row<0)
      return null;
    Object val = getValueAt(row, col);
    if (!(val instanceof Action2))
      return null;
    Action2 action = (Action2)val;
    String tip = action.getTip();
    if (tip!=null)
      return tip;
    return action.getText();
  }
  
  public void setRows(List<ROW> rows) {
    model().setRows(rows);
  }
  
  public void addRow(ROW row) {
    model().addRow(row);
  }

  public void deleteRow(ROW row) {
    model().delRow(row);
  }
  
  private Model model() {
    return (Model)getModel();
  }
  
  /**
   * table model
   */
  private class Model extends AbstractTableModel {
    
    private List<ROW> rows = new ArrayList<ROW>();
    
    private void setRows(List<ROW> rows) {
      if (!this.rows.isEmpty()) {
        int n = this.rows.size();
        this.rows.clear();
        fireTableRowsDeleted(0, n-1);
      }
      this.rows.addAll(rows);
      if (!this.rows.isEmpty()) {
        fireTableRowsInserted(0, this.rows.size()-1);
      }
    }
    
    private void addRow(ROW row) {
      rows.add(row);
      fireTableRowsInserted(rows.size()-1, rows.size()-1);
    }

    private void delRow(ROW row) {
      int i = rows.indexOf(row);
      rows.remove(i);
      fireTableRowsDeleted(i, i);
    }

    private ROW getRow(int row) {
      return rows.get(row);
    }
    
    @Override
    public int getRowCount() {
      return rows.size();
    }
    
    @Override
    public int getColumnCount() {
      return columns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      return columns.get(columnIndex).getValue(rows.get(rowIndex));
    }
    
    @Override
    public String getColumnName(int column) {
      return getColumnModel().getColumn(column).getHeaderValue().toString();
    }
    
  }

  /**
   * A column in the model
   */
  public abstract class Column {
    
    protected Column(String name) {
      this(name, String.class);
    }
    
    protected Column(String name, Class<?> valueType) {
      columns.add(this);
      TableColumn c = new TableColumn(columns.size()-1);
      if (Action2.class.isAssignableFrom(valueType))
        c.setMaxWidth(16);
      c.setHeaderValue(name);
      getColumnModel().addColumn(c);
    }
    
    public abstract Object getValue(ROW row);
    
  }
  
  private class Mouser extends MouseAdapter implements MouseMotionListener {
    
    private ROW getRow(MouseEvent e) {
      int row = rowAtPoint(e.getPoint());
      return row<0 ? null : model().getRow(row);
    }
    
    private Column getColumn(MouseEvent e) {
      int col = columnAtPoint(e.getPoint());
      return col<0 ? null : columns.get(col);
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
      Cursor cursor = null;
      Column col = getColumn(e);
      ROW row = getRow(e);
      if (row!=null) {
        Object val = col.getValue(row);
        if (val instanceof Action)
          cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
      }
      setCursor(cursor);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
      int c = columnAtPoint(e.getPoint());
      if (c<0)
        return;
      Column col = columns.get(c);
      ROW row = getRow(e);
      if (row!=null) {
        Object val = col.getValue(row);
        if (val instanceof Action)
          ((Action)val).actionPerformed(new ActionEvent(TableWidget.this, c, ""));
      }
    }
  }
  
  private static class ActionRenderer extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object value) {
      if (value!=null)
        super.setIcon( ((Action2)value).getImage() );
    }
  }

}
