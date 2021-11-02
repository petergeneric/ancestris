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

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.core.pluginservice.PluginInterface;
import ancestris.gedcom.GedcomDirectory;
import ancestris.util.swing.DialogManager;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.time.PointInTime;
import java.awt.Component;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.openide.modules.Places;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Top component which displays something.
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public final class GedcomHistoryTopComponent extends AncestrisTopComponent implements ChangeListener, LookupListener {

    private static final String PREFERRED_ID = "GedcomHistoryTopComponent";  // NOI18N
    private static final Logger log = Logger.getLogger(GedcomHistoryTopComponent.class.getName());
    private GedcomHistory gedcomHistory = null;
    private GedcomHistoryTableModel historyTableModel = null;
    private Gedcom gedcom = null;
    static final String ICON_PATH = "ancestris/modules/gedcom/history/DisplayHistoryIcon.png";

    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.TABLE;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public void resultChanged(LookupEvent le) {
        Context context = Utilities.actionsGlobalContext().lookup(Context.class);
        setEnabled(context != null && !context.getProperties().isEmpty());
    }

    private class RowListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }

            String currentId = (String) historyTableModel.getValueAt(gedcomHistoryTable.convertRowIndexToModel(gedcomHistoryTable.getSelectedRow()), GedcomHistoryTableModel.ENTITY_ID);

            if (currentId != null && getGedcom() != null) {
                Entity entity = getGedcom().getEntity(currentId);
                if (entity != null) {
                    SelectionDispatcher.fireSelection(new Context(entity));
                }
            }
        }
    }

    public GedcomHistoryTopComponent() {

        Context context = Utilities.actionsGlobalContext().lookup(Context.class);
        if (context == null) {
            List<Context> gedcontexts = GedcomDirectory.getDefault().getContexts();
            if (gedcontexts.isEmpty()) {
                return;
            }
            context = gedcontexts.get(0);
        }
        if (context != null) {
            for (PluginInterface pluginInterface : Lookup.getDefault().lookupAll(PluginInterface.class)) {
                if (pluginInterface instanceof GedcomHistoryPlugin) {

                    this.gedcomHistory = ((GedcomHistoryPlugin) pluginInterface).getGedcomHistory(context.getGedcom());
                    if (this.gedcomHistory != null) {
                        this.gedcom = context.getGedcom();
                        this.historyTableModel = new GedcomHistoryTableModel(this.gedcomHistory, this.getGedcom());
                        initComponents();
                        setContext(context);
                        setName(context.getGedcom().getDisplayName());
                        setToolTipText(NbBundle.getMessage(this.getClass(), "HINT_GedcomHistoryTopComponent", context.getGedcom().getDisplayName()));
                        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
                        jLabel1.setText(NbBundle.getMessage(this.getClass(), "CTL_GedcomHistoryTopComponent"));
                        gedcomHistoryTable.getSelectionModel().addListSelectionListener(new RowListener());
                        gedcomHistoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                            @Override
                            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focs, int row, int col) {
                                DateFormat dateFormat = new SimpleDateFormat(" HH:mm:ss");
                                if (value instanceof GregorianCalendar) {
                                    // Get Date in same Ancestris date Format.
                                    PointInTime pit = new PointInTime((GregorianCalendar) value);
                                    // add hour time.
                                    value = pit.getValue() + dateFormat.format(((GregorianCalendar) value).getTime());
                                }

                                // ready
                                return super.getTableCellRendererComponent(table, value, selected, focs, row, col);
                            }
                        });
                    } else {
                        log.log(Level.FINE, "No history recorder found for {0}", context.getGedcom().getDisplayName());
                    }
                    return;
                }
            }
            log.log(Level.FINE, "No Instance of GedcomHistoryPlugin found");

        } else {
            log.log(Level.FINE, "No context found");
        }

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
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        gedcomHistoryTable.setAutoCreateRowSorter(true);
        gedcomHistoryTable.setModel(historyTableModel);
        gedcomHistoryScrollPane.setViewportView(gedcomHistoryTable);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcom/history/ClearHistoryIcon.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(GedcomHistoryTopComponent.class, "GedcomHistoryTopComponent.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(GedcomHistoryTopComponent.class, "GedcomHistoryTopComponent.jButton1.toolTipText")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GedcomHistoryTopComponent.class, "GedcomHistoryTopComponent.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addComponent(gedcomHistoryScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1)
                    .addComponent(jButton1))
                .addGap(0, 0, 0)
                .addComponent(gedcomHistoryScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        DialogManager dm = DialogManager.createYesNo(NbBundle.getMessage(GedcomHistoryTopComponent.class, "delete.Title"), NbBundle.getMessage(GedcomHistoryTopComponent.class, "delete.Text"));

        if (DialogManager.YES_OPTION.equals(dm.show())) {
            String gedcomName = gedcom.getName().substring(0, gedcom.getName().lastIndexOf(".") == -1 ? gedcom.getName().length() : gedcom.getName().lastIndexOf("."));
            File cacheSubdirectory = Places.getCacheSubdirectory("ModificationsHistory");
            File historyFile = new File(cacheSubdirectory.getAbsolutePath() + System.getProperty("file.separator") + gedcomName + ".hist");
            if (historyFile.exists() == true) {
                historyFile.delete();
            }
            gedcomHistory.clear();
            historyTableModel.fireTableDataChanged();
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane gedcomHistoryScrollPane;
    private javax.swing.JTable gedcomHistoryTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        AncestrisPlugin.register(this);
        if (gedcomHistory != null) {
            gedcomHistory.addChangeListener(this);
        }
    }

    @Override
    public void componentClosed() {
        AncestrisPlugin.unregister(this);
        if (gedcomHistory != null) {
            gedcomHistory.removeChangeListener(this);
        }
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        historyTableModel.fireTableRowsInserted(gedcomHistory.getHistoryList().size() - 1, gedcomHistory.getHistoryList().size() - 1);
    }

    /**
     * @return the gedcom
     */
    public Gedcom getGedcom() {
        return gedcom;
    }
}
