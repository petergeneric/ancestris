/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * HoverPanel.java
 *
 * Created on 14 mars 2010, 15:05:40
 */
package ancestris.modules.geo;

import ancestris.modules.editors.geoplace.PlaceEditor;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;

/**
 *
 * @author frederic
 */
public class HoverPanel extends javax.swing.JPanel {

    private GeoMapTopComponent parent = null;
    private GeoNodeObject[] selectedMarkers = null;
    private GeoNodeObject currentGno;
    private Color backgroundColor;

    /** Creates new form HoverPanel
     * @param parent */
    public HoverPanel(GeoMapTopComponent parent) {
        this.parent = parent;
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        initComponents();
        setMouseListener();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(backgroundColor);
        // g.fillRoundRect(10, 0, this.getWidth() - 10, this.getHeight(), 20, 20);
        g.fill3DRect(10, 0, this.getWidth() - 10, this.getHeight(), true);
        Polygon triangle = new Polygon();
        triangle.addPoint(0, 35);
        triangle.addPoint(10, 20);
        triangle.addPoint(10, 50);
        g.fillPolygon(triangle);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jScrollBar1 = new javax.swing.JScrollBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setBackground(new java.awt.Color(254, 254, 254));
        setAlignmentX(0.0F);
        setAlignmentY(0.0F);
        setOpaque(false);

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(254, 254, 254));
        jLabel2.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel2.text")); // NOI18N
        jLabel2.setAlignmentY(0.0F);

        jLabel3.setForeground(new java.awt.Color(254, 254, 254));
        jLabel3.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel3.text")); // NOI18N
        jLabel3.setAlignmentY(0.0F);

        jLabel4.setForeground(new java.awt.Color(254, 254, 254));
        jLabel4.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel4.text")); // NOI18N
        jLabel4.setAlignmentY(0.0F);

        jLabel6.setFont(new java.awt.Font("DejaVu Sans", 2, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(254, 254, 254));
        jLabel6.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel6.text")); // NOI18N
        jLabel6.setAlignmentY(0.0F);

        jLabel5.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(254, 254, 254));
        jLabel5.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel5.text")); // NOI18N
        jLabel5.setAlignmentY(0.0F);

        jLabel7.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(254, 254, 254));
        jLabel7.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel7.text")); // NOI18N
        jLabel7.setAlignmentY(0.0F);

        jLabel8.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(254, 254, 254));
        jLabel8.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel8.text")); // NOI18N
        jLabel8.setAlignmentY(0.0F);

        jLabel9.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(254, 254, 254));
        jLabel9.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel9.text")); // NOI18N
        jLabel9.setAlignmentY(0.0F);

        jLabel10.setForeground(new java.awt.Color(254, 254, 254));
        jLabel10.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel10.text")); // NOI18N

        jLabel11.setForeground(new java.awt.Color(254, 254, 254));
        jLabel11.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel11.text")); // NOI18N

        jLabel12.setForeground(new java.awt.Color(254, 254, 254));
        jLabel12.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel12.text")); // NOI18N

        jLabel13.setForeground(new java.awt.Color(254, 254, 254));
        jLabel13.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel13.text")); // NOI18N

        jLabel14.setForeground(new java.awt.Color(254, 254, 254));
        jLabel14.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel14.text")); // NOI18N

        jLabel15.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(254, 254, 254));
        jLabel15.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel15.text")); // NOI18N

        jLabel16.setForeground(new java.awt.Color(254, 254, 254));
        jLabel16.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel16.text")); // NOI18N

        jLabel17.setForeground(new java.awt.Color(254, 254, 254));
        jLabel17.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel17.text")); // NOI18N

        jLabel18.setForeground(new java.awt.Color(254, 254, 254));
        jLabel18.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel18.text")); // NOI18N

        jLabel19.setForeground(new java.awt.Color(254, 254, 254));
        jLabel19.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel19.text")); // NOI18N

        jLabel20.setForeground(new java.awt.Color(254, 254, 254));
        jLabel20.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel20.text")); // NOI18N

        jLabel21.setForeground(new java.awt.Color(254, 254, 254));
        jLabel21.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel21.text")); // NOI18N

        jLabel22.setForeground(new java.awt.Color(254, 254, 254));
        jLabel22.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel22.text")); // NOI18N

        jLabel23.setForeground(new java.awt.Color(254, 254, 254));
        jLabel23.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jLabel23.text")); // NOI18N

        jScrollBar1.setBackground(new java.awt.Color(254, 254, 254));
        jScrollBar1.setForeground(new java.awt.Color(255, 250, 250));
        jScrollBar1.setPreferredSize(new java.awt.Dimension(14, 48));
        jScrollBar1.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBar1AdjustmentValueChanged(evt);
            }
        });

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/edit.png"))); // NOI18N
        jButton1.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jButton1.toolTipText")); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(20, 20));
        jButton1.setMinimumSize(new java.awt.Dimension(20, 20));
        jButton1.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/geo/GoToList.png"))); // NOI18N
        jButton2.setText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jButton2.text")); // NOI18N
        jButton2.setToolTipText(org.openide.util.NbBundle.getMessage(HoverPanel.class, "HoverPanel.jButton2.toolTipText")); // NOI18N
        jButton2.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jScrollBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(jLabel10)
                            .addComponent(jLabel17)
                            .addComponent(jLabel16)
                            .addComponent(jLabel19)
                            .addComponent(jLabel18)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel15))
                            .addComponent(jLabel5)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel21)
                                    .addComponent(jLabel20)
                                    .addComponent(jLabel23)
                                    .addComponent(jLabel22)))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(jLabel2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(2, 2, 2)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel6)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(jLabel5))
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(jLabel7))
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(jLabel8))
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(jLabel20))
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(jLabel21))
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18)
                            .addComponent(jLabel22))
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel19)
                            .addComponent(jLabel23))
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(jLabel9)
                            .addComponent(jLabel14)
                            .addComponent(jLabel15))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jScrollBar1AdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBar1AdjustmentValueChanged
        currentGno = selectedMarkers[jScrollBar1.getValue()];
        displayInfo();
    }//GEN-LAST:event_jScrollBar1AdjustmentValueChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        new PlaceEditor().edit(currentGno.getFirstPropertyPlace(), currentGno.getGeoPosition());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        parent.showListAtLocation(currentGno);
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollBar jScrollBar1;
    // End of variables declaration//GEN-END:variables

    public void setPanel(GeoNodeObject gno, Color color) {
        // get markers and return if null
        GeoNodeObject[] markers = parent.getMarkers();
        if (markers == null) {
            return;
        }
        backgroundColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 200);

        // loop on markers and store all those with same geocoordinates
        Double lat = gno.getLatitude();
        Double lon = gno.getLongitude();
        List<GeoNodeObject> list = new ArrayList<GeoNodeObject>();
        for (int i = 0; i < markers.length; i++) {
            GeoNodeObject geoNodeObject = markers[i];
            if (geoNodeObject.getLatitude().equals(lat) && geoNodeObject.getLongitude().equals(lon)) {
                if (parent.getFilter().compliesNode(geoNodeObject)) {
                    list.add(geoNodeObject);
                }
            }
        }
        selectedMarkers = list.toArray(new GeoNodeObject[list.size()]);
        if (selectedMarkers == null || selectedMarkers.length == 0) { // should never be true but we never know
            return;
        }

        // set scrolling
        currentGno = selectedMarkers[0];
        jScrollBar1.setVisible(selectedMarkers.length > 1);
        jScrollBar1.setUnitIncrement(1);
        jScrollBar1.setMinimum(0);
        jScrollBar1.setMaximum(selectedMarkers.length);
        jScrollBar1.setValue(0);

        // display info
        displayInfo();
    }

    private void displayInfo() {
        jLabel2.setText("<html>"+currentGno.toDisplayString()+"</html>");
        jLabel6.setText("(" + currentGno.getTextCoordinates() + ")");
        jLabel4.setText(currentGno.getPopulation());
        String[] info = currentGno.getEventsInfo(parent.getFilter());  
        jLabel5.setText(info[0]);
        jLabel7.setText(info[1]);
        jLabel8.setText(info[2]);
        jLabel20.setText(info[3]);
        jLabel21.setText(info[4]);
        jLabel22.setText(info[5]);
        jLabel23.setText(info[6]);
        jLabel9.setText(info[7]);
        jLabel15.setText(info[8]);
    }

    private void setMouseListener() {
        this.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                if (currentGno == null || (e.getClickCount() < 2)) {
                    return;
                }
                parent.showListAtLocation(currentGno);
                // boolean right_click_pressed = (e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK;
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
    }

}
