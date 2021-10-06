/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015-2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcomcompare.tools;

import ancestris.modules.gedcomcompare.GedcomCompareTopComponent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class StatsPanel extends javax.swing.JPanel {

    private final GedcomCompareTopComponent owner;

    private int nbIndis = 0;
    private int nbFams = 0;
    private int nbSTs = 0;
    private String maxArea = "";
    private int activeUsersNb = 0;
    private int rcvdConnectionsNb = 0;
    private int rcvdUniqueUsersNb = 0;
    private int rcvdUniqueOverlapsNb = 0;
    private int maxoverlap = 0;
    private int citynamesNb = 0;
    private int eventsNb = 0;
    
    
    /**
     * Creates new form ListEntitiesPanel
     */
    public StatsPanel(GedcomCompareTopComponent tstc) {
        this.owner = tstc;
        initComponents();
        
        ((JLabel) OR_Table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        calcValues();

    }

    final public void calcValues() {

        LocalGedcomFrame mgf = owner.getMain();
        
        nbIndis = mgf == null ? 0 : mgf.getNbOfPublicIndis();
        nbFams = mgf == null ? 0 : mgf.getNbOfPublicFams();
        nbSTs = mgf == null ? 0 : mgf.getNbOfSTs();
        activeUsersNb = 0;
        rcvdConnectionsNb = 0;
        rcvdUniqueUsersNb = 0;
        rcvdUniqueOverlapsNb = 0;
        maxoverlap = 0;
        citynamesNb = 0;
        eventsNb = 0;
        
        int size = owner.getConnectedUsers() == null ? 1 : owner.getConnectedUsers().size() + 1;
        int[][] tableData = new int[size][3]; // stores values
        int row = 0;
        
        Map<String, Integer> areasMap = new HashMap<>();
        List<String> areaCityNames = new ArrayList<>();
        
        if (mgf != null) {
            updateMap(areasMap, mgf.getSTs());
        }
        
        if (owner.getConnectedUsers() != null) {
            for (ConnectedUserFrame user : owner.getConnectedUsers()) {

                if (user.isActive()) {
                    activeUsersNb++;
                }
                nbIndis += user.getNbIndis();
                nbFams += user.getNbFams();
                nbSTs += user.getNbSTs();
                updateMap(areasMap, user.getSTs());
                
                tableData[row++] = user.getStats();

                rcvdConnectionsNb += user.getConnections();
                if (user.hasConnections()) {
                    rcvdUniqueUsersNb++;
                }
                if (user.hasOverlap()) {
                    rcvdUniqueOverlapsNb++;
                    ComparisonFrame cf = owner.getComparisonFrame(user);
                    maxoverlap = cf.getOverlap(maxoverlap);
                    citynamesNb += cf.getLastCityNb();
                    eventsNb += cf.getEventNb();
                    areaCityNames.addAll(cf.getAreaCityNames());
                }
            }
        }
        tableData[row] = new int[] { maxoverlap, citynamesNb, eventsNb };
        
        // Get top 10Â areas from connected users and myself
        Map<String, Integer> topMap = areasMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(20)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        String[] areas = new String[20];
        int i = 0;
        for (String key : topMap.keySet()) {
            areas[i] = key + " (" + topMap.get(key) + ")";
            i++;
        }
        maxArea = areas[0];
        UA_areasList.setListData(areas);

        
        // Update display
        UA_users.setText( "1 + " + (owner.getConnectedUsers() != null ? owner.getConnectedUsers().size() : 0) );
        int ua = owner.isSharingOn() ? 1 : 0;
        UA_activeusers.setText(ua + " + " + activeUsersNb);
        UA_indis.setText("" + nbIndis);
        UA_fams.setText("" + nbFams);
        UA_areas.setText("" + nbSTs);
        
        setTable(tableData);
        
        YR_connections.setText("" + rcvdConnectionsNb);
        YR_users.setText("" + rcvdUniqueUsersNb);
        YR_overlaps.setText("" + rcvdUniqueOverlapsNb + " / " + maxoverlap + "%"); 
        YR_citynames.setText("" + citynamesNb);
        YR_events.setText("" + eventsNb);
        
        YR_areasList.setListData(areaCityNames.toArray(new String[areaCityNames.size()]));
        
        
    }
    
    private void updateMap(Map<String, Integer> areasMap, String[] array) {

        for (String key : array) {
            if (key.trim().isEmpty()) { // should not happen so just in case
                continue;
            }
            // key is in the form "area (value)" => extract area and value
            String bits[] = key.split("_");
            String area = bits[0].trim();
            int value = 0;
            if (bits.length > 1) {
                value = Integer.parseInt(bits[1].substring(1, bits[1].length()-1));
            }

            Integer score = areasMap.get(area);
            if (score == null) {
                score = 0;
            }
            score += value;
            areasMap.put(area, score);
        }
    }

    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        UAPanel = new javax.swing.JPanel();
        UA_users_Label = new javax.swing.JLabel();
        UA_users = new javax.swing.JLabel();
        UA_activeusers_Label = new javax.swing.JLabel();
        UA_activeusers = new javax.swing.JLabel();
        UA_indis_Label = new javax.swing.JLabel();
        UA_indis = new javax.swing.JLabel();
        UA_fams_Label = new javax.swing.JLabel();
        UA_fams = new javax.swing.JLabel();
        UA_areas_Label = new javax.swing.JLabel();
        UA_areas = new javax.swing.JLabel();
        UA_areaslist_Label = new javax.swing.JLabel();
        UA_AreasPane = new javax.swing.JScrollPane();
        UA_areasList = new javax.swing.JList<>();
        ORPanel = new javax.swing.JPanel();
        OR_Pane = new javax.swing.JScrollPane();
        OR_Table = new javax.swing.JTable();
        YRPanel = new javax.swing.JPanel();
        YR_connectionsLabel = new javax.swing.JLabel();
        YR_connections = new javax.swing.JLabel();
        YR_usersLabel = new javax.swing.JLabel();
        YR_users = new javax.swing.JLabel();
        YR_overlapsLabel = new javax.swing.JLabel();
        YR_overlaps = new javax.swing.JLabel();
        YR_citynamesLabel = new javax.swing.JLabel();
        YR_citynames = new javax.swing.JLabel();
        YR_eventsLabel = new javax.swing.JLabel();
        YR_events = new javax.swing.JLabel();
        YR_areaslist_Label = new javax.swing.JLabel();
        YR_AreasPane = new javax.swing.JScrollPane();
        YR_areasList = new javax.swing.JList<>();

        UAPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.UAPanel.border.title"))); // NOI18N

        UA_users_Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/friend16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(UA_users_Label, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.UA_users_Label.text")); // NOI18N

        UA_users.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(UA_users, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.UA_users.text")); // NOI18N

        UA_activeusers_Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/active.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(UA_activeusers_Label, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.UA_activeusers_Label.text")); // NOI18N

        UA_activeusers.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(UA_activeusers, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.UA_activeusers.text")); // NOI18N

        UA_indis_Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/indi.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(UA_indis_Label, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.UA_indis_Label.text")); // NOI18N

        UA_indis.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(UA_indis, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.UA_indis.text")); // NOI18N

        UA_fams_Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/fam.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(UA_fams_Label, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.UA_fams_Label.text")); // NOI18N

        UA_fams.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(UA_fams, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.UA_fams.text")); // NOI18N

        UA_areas_Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/geost.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(UA_areas_Label, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.UA_areas_Label.text")); // NOI18N

        UA_areas.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(UA_areas, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.UA_areas.text")); // NOI18N

        UA_areaslist_Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        UA_areaslist_Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/star.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(UA_areaslist_Label, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.UA_areaslist_Label.text")); // NOI18N

        UA_areasList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        UA_AreasPane.setViewportView(UA_areasList);

        javax.swing.GroupLayout UAPanelLayout = new javax.swing.GroupLayout(UAPanel);
        UAPanel.setLayout(UAPanelLayout);
        UAPanelLayout.setHorizontalGroup(
            UAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UAPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(UAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(UA_areas_Label)
                    .addComponent(UA_fams_Label)
                    .addComponent(UA_indis_Label)
                    .addComponent(UA_activeusers_Label)
                    .addComponent(UA_users_Label))
                .addGap(18, 18, 18)
                .addGroup(UAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(UA_users, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(UA_activeusers, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(UA_indis, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(UA_fams, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(UA_areas, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(UAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(UA_areaslist_Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(UA_AreasPane, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
                .addContainerGap())
        );
        UAPanelLayout.setVerticalGroup(
            UAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UAPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(UA_areaslist_Label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(UAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(UA_AreasPane, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                    .addGroup(UAPanelLayout.createSequentialGroup()
                        .addGroup(UAPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(UAPanelLayout.createSequentialGroup()
                                .addComponent(UA_users_Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(UA_activeusers_Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(UA_indis_Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(UA_fams_Label)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(UA_areas_Label))
                            .addGroup(UAPanelLayout.createSequentialGroup()
                                .addComponent(UA_users)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(UA_activeusers)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(UA_indis)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(UA_fams)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(UA_areas)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        ORPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.ORPanel.border.title"))); // NOI18N

        OR_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Overlaps", null, null, null, null},
                {"City-Names", null, null, null, null},
                {"Events", null, null, null, null}
            },
            new String [] {
                "Variable", "Count", "%", "Average", "Max"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Float.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        OR_Table.getTableHeader().setReorderingAllowed(false);
        OR_Pane.setViewportView(OR_Table);

        javax.swing.GroupLayout ORPanelLayout = new javax.swing.GroupLayout(ORPanel);
        ORPanel.setLayout(ORPanelLayout);
        ORPanelLayout.setHorizontalGroup(
            ORPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ORPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(OR_Pane)
                .addContainerGap())
        );
        ORPanelLayout.setVerticalGroup(
            ORPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ORPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(OR_Pane, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        YRPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.YRPanel.border.title"))); // NOI18N

        YR_connectionsLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/connm.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(YR_connectionsLabel, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.YR_connectionsLabel.text")); // NOI18N

        YR_connections.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(YR_connections, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.YR_connections.text")); // NOI18N

        YR_usersLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/connu.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(YR_usersLabel, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.YR_usersLabel.text")); // NOI18N

        YR_users.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(YR_users, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.YR_users.text")); // NOI18N

        YR_overlapsLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/connoverlap.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(YR_overlapsLabel, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.YR_overlapsLabel.text")); // NOI18N

        YR_overlaps.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(YR_overlaps, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.YR_overlaps.text")); // NOI18N

        YR_citynamesLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/indi.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(YR_citynamesLabel, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.YR_citynamesLabel.text")); // NOI18N

        YR_citynames.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(YR_citynames, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.YR_citynames.text")); // NOI18N

        YR_eventsLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/even.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(YR_eventsLabel, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.YR_eventsLabel.text")); // NOI18N

        YR_events.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(YR_events, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.YR_events.text")); // NOI18N

        YR_areaslist_Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        YR_areaslist_Label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/gedcomcompare/resources/star.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(YR_areaslist_Label, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.YR_areaslist_Label.text")); // NOI18N

        YR_areasList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        YR_AreasPane.setViewportView(YR_areasList);

        javax.swing.GroupLayout YRPanelLayout = new javax.swing.GroupLayout(YRPanel);
        YRPanel.setLayout(YRPanelLayout);
        YRPanelLayout.setHorizontalGroup(
            YRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(YRPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(YRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(YR_citynamesLabel)
                    .addComponent(YR_overlapsLabel)
                    .addComponent(YR_usersLabel)
                    .addComponent(YR_connectionsLabel)
                    .addComponent(YR_eventsLabel))
                .addGap(18, 18, 18)
                .addGroup(YRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(YR_connections, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(YR_users, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(YR_overlaps, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(YR_citynames, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(YR_events, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(YRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(YR_areaslist_Label, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                    .addComponent(YR_AreasPane))
                .addContainerGap())
        );
        YRPanelLayout.setVerticalGroup(
            YRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(YRPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(YR_areaslist_Label, javax.swing.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(YRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(YR_AreasPane, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                    .addGroup(YRPanelLayout.createSequentialGroup()
                        .addGroup(YRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(YR_connectionsLabel)
                            .addComponent(YR_connections))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(YRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(YR_usersLabel)
                            .addComponent(YR_users))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(YRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(YR_overlapsLabel)
                            .addComponent(YR_overlaps))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(YRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(YR_citynamesLabel)
                            .addComponent(YR_citynames))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(YRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(YR_eventsLabel)
                            .addComponent(YR_events))))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(UAPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(YRPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ORPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(UAPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ORPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(YRPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ORPanel;
    private javax.swing.JScrollPane OR_Pane;
    private javax.swing.JTable OR_Table;
    private javax.swing.JPanel UAPanel;
    private javax.swing.JScrollPane UA_AreasPane;
    private javax.swing.JLabel UA_activeusers;
    private javax.swing.JLabel UA_activeusers_Label;
    private javax.swing.JLabel UA_areas;
    private javax.swing.JList<String> UA_areasList;
    private javax.swing.JLabel UA_areas_Label;
    private javax.swing.JLabel UA_areaslist_Label;
    private javax.swing.JLabel UA_fams;
    private javax.swing.JLabel UA_fams_Label;
    private javax.swing.JLabel UA_indis;
    private javax.swing.JLabel UA_indis_Label;
    private javax.swing.JLabel UA_users;
    private javax.swing.JLabel UA_users_Label;
    private javax.swing.JPanel YRPanel;
    private javax.swing.JScrollPane YR_AreasPane;
    private javax.swing.JList<String> YR_areasList;
    private javax.swing.JLabel YR_areaslist_Label;
    private javax.swing.JLabel YR_citynames;
    private javax.swing.JLabel YR_citynamesLabel;
    private javax.swing.JLabel YR_connections;
    private javax.swing.JLabel YR_connectionsLabel;
    private javax.swing.JLabel YR_events;
    private javax.swing.JLabel YR_eventsLabel;
    private javax.swing.JLabel YR_overlaps;
    private javax.swing.JLabel YR_overlapsLabel;
    private javax.swing.JLabel YR_users;
    private javax.swing.JLabel YR_usersLabel;
    // End of variables declaration//GEN-END:variables

    public int getNbIndis() {
        return nbIndis;
    }

    public int getNbFams() {
        return nbFams;
    }

    public int getNbSTs() {
        return nbSTs;
    }

    public String getMaxArea() {
        return maxArea;
    }

    public int getNbConnections() {
        return rcvdConnectionsNb;
    }

    public int getNbUniqueUsers() {
        return rcvdUniqueUsersNb;
    }

    public int getNbOverlaps() {
        return rcvdUniqueOverlapsNb;
    }


    public String getValues() {
        return "" + maxoverlap + " " + citynamesNb + " " + eventsNb;
    }
 
    
    private void setTable(int[][] tableData) {

        int[] count = new int[] { 0, 0, 0 };
        int[] percent = new int[] { 0, 0, 0 };
        int[] average = new int[] { 0, 0, 0 };
        int[] max = new int[] { 0, 0, 0 };
        
        // Calc stats
        for (int[] row : tableData) {
            for (int i = 0; i<3; i++) {
                count[i] += (row[i] > 0 ? 1 : 0);
                percent[i] = count[i] * 100 / tableData.length;
                average[i] += row[i];
                max[i] = Math.max(max[i], row[i]);
            }
        }
        for (int i = 0; i<3; i++) {
            if (count[i] != 0) {
                average[i] = average[i] / count[i];
            }
        }
        
        
        // Display stats
        OR_Table.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{
                    {NbBundle.getMessage(getClass(), "STAT_Overlaps"), count[0], percent[0], average[0], max[0]},
                    {NbBundle.getMessage(getClass(), "STAT_CityNames"), count[1], percent[1], average[1], max[1]},
                    {NbBundle.getMessage(getClass(), "STAT_Events"), count[2], percent[2], average[2], max[2]}
                },
                new String[]{
                    NbBundle.getMessage(getClass(), "STAT_Variable"),
                    NbBundle.getMessage(getClass(), "STAT_Count"),
                    NbBundle.getMessage(getClass(), "STAT_Percent"),
                    NbBundle.getMessage(getClass(), "STAT_Average"),
                    NbBundle.getMessage(getClass(), "STAT_Max")
                }
        ) {
            @Override
            public Class getColumnClass(int columnIndex) {
                return (columnIndex == 0 ? String.class : Integer.class);
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i < 5; i++) {
            OR_Table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        
    }
    
}
