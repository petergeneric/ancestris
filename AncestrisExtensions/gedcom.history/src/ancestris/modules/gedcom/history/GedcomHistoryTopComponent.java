/*
 * Copyright (C) 2012 lemovice
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

import ancestris.view.AncestrisDockModes;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.NbBundle;
import org.openide.windows.RetainLocation;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ancestris.modules.gedcom.history//GedcomHistory//EN", autostore = false)
@RetainLocation(AncestrisDockModes.TABLE)
@TopComponent.Description(preferredID = "GedcomHistoryTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
//@TopComponent.OpenActionRegistration(displayName = "#CTL_GedcomHistoryAction", preferredID = "GedcomHistoryTopComponent")
public final class GedcomHistoryTopComponent extends TopComponent implements ChangeListener {

    GedcomHistory gedcomHistory = null;
    GedcomHistoryTableModel historyTableModel = null;

    public GedcomHistoryTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(this.getClass(), "CTL_GedcomHistoryTopComponent", new Object [] {gedcomHistory.getGedcomName()}));
        setToolTipText(NbBundle.getMessage(this.getClass(), "HINT_GedcomHistoryTopComponent"));
    }

    public GedcomHistoryTopComponent(GedcomHistory gedcomHistory) {
        this.gedcomHistory = gedcomHistory;
        this.historyTableModel = new GedcomHistoryTableModel(gedcomHistory);
        initComponents();
        setName(NbBundle.getMessage(this.getClass(), "CTL_GedcomHistoryTopComponent"));
        setToolTipText(NbBundle.getMessage(this.getClass(), "HINT_GedcomHistoryTopComponent"));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        gedcomHistoryScrollPane = new javax.swing.JScrollPane();
        gedcomHistoryTable = new javax.swing.JTable();

        gedcomHistoryTable.setAutoCreateRowSorter(true);
        gedcomHistoryTable.setModel(historyTableModel);
        gedcomHistoryScrollPane.setViewportView(gedcomHistoryTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gedcomHistoryScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gedcomHistoryScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane gedcomHistoryScrollPane;
    private javax.swing.JTable gedcomHistoryTable;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        if (gedcomHistory != null)
        gedcomHistory.addChangeListener(this);
    }

    @Override
    public void componentClosed() {
        if (gedcomHistory != null)
        gedcomHistory.removeChangeListener(this);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        historyTableModel.fireTableRowsInserted(gedcomHistory.getHistoryList().size() - 1, gedcomHistory.getHistoryList().size() - 1);
    }
}
