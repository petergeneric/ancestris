/*
 * Copyright (C) 2012 Lemovice <lemovice at ancestris-dot-org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ancestris.modules.gedcom.history;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Lemovice <lemovice at ancestris-dot-org>
 */
class GedcomHistoryTableModel extends AbstractTableModel implements ChangeListener {

    final static int date = 0;
    final static int entityTag = 1;
    final static int entityId = 2;
    final static int action = 3;
    final static int property = 4;
    final static int oldValue = 5;
    final static int newValue = 6;
    private String[] columnNames = {
        NbBundle.getMessage(this.getClass(), "HistoryTableModel.columnNames.date"),
        NbBundle.getMessage(this.getClass(), "HistoryTableModel.columnNames.entityTag"),
        NbBundle.getMessage(this.getClass(), "HistoryTableModel.columnNames.entityId"),
        NbBundle.getMessage(this.getClass(), "HistoryTableModel.columnNames.action"),
        NbBundle.getMessage(this.getClass(), "HistoryTableModel.columnNames.Property"),
        NbBundle.getMessage(this.getClass(), "HistoryTableModel.columnNames.oldValue"),
        NbBundle.getMessage(this.getClass(), "HistoryTableModel.columnNames.newValue")
    };
    private ArrayList<EntityHistory> gedcomHistoryList = null;

    public GedcomHistoryTableModel(GedcomHistory gedcomHistory) {
        gedcomHistoryList = gedcomHistory.getHistoryList();
        gedcomHistory.addChangeListener(this);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return gedcomHistoryList == null ? 0 : gedcomHistoryList.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (gedcomHistoryList != null) {
            EntityHistory entityHistory = gedcomHistoryList.get(row);
            switch (col) {
                case date:
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                    return dateFormat.format(entityHistory.getDate().getTime());
                case entityTag:
                    return entityHistory.getEntityTag();
                case entityId:
                    return entityHistory.getEntityId();
                case action:
                    return entityHistory.getAction();
                case property:
                    return entityHistory.getProperty();
                case oldValue:
                    return entityHistory.getOldValue();
                case newValue:
                    return entityHistory.getNewValue();
                default:
                    return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public Class<? extends Object> getColumnClass(int c) {
        return String.class;
    }

    /*
     * Don't need to implement this method unless your table's editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    /*
     * Don't need to implement this method unless your table's data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        fireTableRowsInserted(gedcomHistoryList.size() - 1, gedcomHistoryList.size() - 1);
    }
}
