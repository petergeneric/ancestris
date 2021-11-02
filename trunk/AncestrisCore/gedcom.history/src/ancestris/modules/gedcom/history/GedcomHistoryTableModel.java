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

import genj.gedcom.Gedcom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import javax.swing.table.DefaultTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Lemovice <lemovice at ancestris-dot-org>
 */
class GedcomHistoryTableModel extends DefaultTableModel {

    public final static int DATE = 0;
    public final static int ENTITY_TAG = 1;
    public final static int ENTITY_ID = 2;
    public final static int ACTION = 3;
    public final static int PROPERTY = 4;
    public final static int OLD_VALUE = 5;
    public final static int NEW_VALUE = 6;
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
    private final Gedcom gedcom;

    public GedcomHistoryTableModel(GedcomHistory gedcomHistory, Gedcom gedcom) {
        if (gedcomHistory == null) {
            throw new IllegalArgumentException("gedcomHistory can't be null");
        }
        if (gedcomHistory.getHistoryList() == null) {
            throw new IllegalArgumentException("gedcomHistoryList can't be null");
        }
        gedcomHistoryList = gedcomHistory.getHistoryList();
        this.gedcom = gedcom;
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
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return GregorianCalendar.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (gedcomHistoryList != null) {
            EntityHistory entityHistory = gedcomHistoryList.get(row);
            switch (col) {
                case DATE:
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    return entityHistory.getDate();
                case ENTITY_TAG:
                    return Gedcom.getName(entityHistory.getEntityTag());
                case ENTITY_ID:
                    return entityHistory.getEntityId();
                case ACTION:
                    return NbBundle.getMessage(this.getClass(), "HistoryTableModel.Action." + entityHistory.getAction());
                case PROPERTY:
                    return entityHistory.getPropertyPath();
                case OLD_VALUE:
                    return entityHistory.getOldValue();
                case NEW_VALUE:
                    return entityHistory.getNewValue();
                default:
                    return "";
            }
        } else {
            return "";
        }
    }
}
