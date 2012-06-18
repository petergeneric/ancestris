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
package genj.report;

import genj.util.Registry;
import genj.util.Resources;

import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

/**
 * Report list capable of displaying the report list, info and settings Reports
 * are either sorted alphabetically or it is a tree with reports within their
 * categories.
 */
/* package */class ReportList extends JList {

  /**
   * Object with callback functions.
   */
  private Callback callback = new Callback();

  /**
   * Listener for changes in the currently selected report.
   */
  private ReportSelectionListener selectionListener = null;

  /**
   * Registry for storing configuration.
   */
  private Registry registry;

  /**
   * Language resources.
   */
  private static final Resources RESOURCES = Resources.get(ReportView.class);
  
  private boolean byGroup;

  /**
   * Creates the component.
   */
  public ReportList(Report[] reports, boolean byGroup) {
    
    this.byGroup = byGroup;

    setReports(reports);
    setVisibleRowCount(3);
    getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setCellRenderer(callback);
    addListSelectionListener(callback);

    // done
  }

  /**
   * Sets the given report as selected.
   */
  public void setSelection(Report report) {
    if (report == null) {
      clearSelection();
    } else {
      setSelectedValue(report, true);
    }
  }

  /**
   * Returns the currently selected report.
   */
  public Report getSelection() {
    return (Report)getSelectedValue();
  }

  /**
   * Sets the selection listener.
   */
  public void setSelectionListener(ReportSelectionListener listener) {
    selectionListener = listener;
  }

  /**
   * Sets a new list of reports.
   */
  public void setReports(Report[] reports) {
    setModel(new DefaultComboBoxModel(reports));
  }

  /**
   * A private callback for various messages coming in.
   */
  private class Callback extends DefaultListCellRenderer implements ListCellRenderer, ListSelectionListener {
    


    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      
      Report report = (Report) value;
      setText(report.getName());
      setIcon(report.getIcon());
      
      return this;
    }

    /**
     * Monitors changes to selection of reports.
     */
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
      Report r = (Report)getSelectedValue();
      if (selectionListener != null)
        selectionListener.valueChanged(r);
    }

  }

}
