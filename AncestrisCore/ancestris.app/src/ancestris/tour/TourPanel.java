package ancestris.tour;

/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2018 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */


import ancestris.view.Images;
import java.awt.Color;
import java.awt.Dimension;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class TourPanel extends javax.swing.JPanel {

    /**
     * Creates new form TourPanel
     */
    public TourPanel(int num, String message, Color bgcolor, Color fgcolor, int gapL, int gapR, boolean end) {
        
        initComponents();
        
        numLabel.setText("- "+num+" -");
        numLabel.setBackground(bgcolor);
        numLabel.setForeground(fgcolor);
        closeLabel.setIcon(Images.imgBigClose);
        closeLabel.setText("");
        nextLabel.setIcon(Images.imgNext);
        nextLabel.setForeground(fgcolor);
        if (end) {
            nextLabel.setText(NbBundle.getMessage(getClass(), "TourPanel.nextLabel.end"));
        }
        setBackground(bgcolor);
        
        leftbufferPanel.setBackground(bgcolor);
        leftbufferPanel.setPreferredSize(new Dimension(gapL, leftbufferPanel.getPreferredSize().height));
        rightbufferPanel.setBackground(bgcolor);
        rightbufferPanel.setPreferredSize(new Dimension(gapR, rightbufferPanel.getPreferredSize().height));

        messageLabel.setText(message);
        messageLabel.setForeground(fgcolor);
    }

    public void closeDemo(boolean set) {
    };
   
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        numLabel = new javax.swing.JLabel();
        closeLabel = new javax.swing.JLabel();
        leftbufferPanel = new javax.swing.JPanel();
        messageLabel = new javax.swing.JLabel();
        rightbufferPanel = new javax.swing.JPanel();
        nextLabel = new javax.swing.JLabel();

        numLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 18)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(numLabel, org.openide.util.NbBundle.getMessage(TourPanel.class, "TourPanel.numLabel.text")); // NOI18N
        numLabel.setBorder(null);

        org.openide.awt.Mnemonics.setLocalizedText(closeLabel, org.openide.util.NbBundle.getMessage(TourPanel.class, "TourPanel.closeLabel.text")); // NOI18N
        closeLabel.setBorder(null);
        closeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeLabelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout leftbufferPanelLayout = new javax.swing.GroupLayout(leftbufferPanel);
        leftbufferPanel.setLayout(leftbufferPanelLayout);
        leftbufferPanelLayout.setHorizontalGroup(
            leftbufferPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 90, Short.MAX_VALUE)
        );
        leftbufferPanelLayout.setVerticalGroup(
            leftbufferPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        messageLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 18)); // NOI18N
        messageLabel.setForeground(new java.awt.Color(0, 0, 255));
        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(TourPanel.class, "TourPanel.messageLabel.text")); // NOI18N

        rightbufferPanel.setPreferredSize(new java.awt.Dimension(90, 0));

        javax.swing.GroupLayout rightbufferPanelLayout = new javax.swing.GroupLayout(rightbufferPanel);
        rightbufferPanel.setLayout(rightbufferPanelLayout);
        rightbufferPanelLayout.setHorizontalGroup(
            rightbufferPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 90, Short.MAX_VALUE)
        );
        rightbufferPanelLayout.setVerticalGroup(
            rightbufferPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        nextLabel.setFont(new java.awt.Font("DejaVu Sans", 3, 12)); // NOI18N
        nextLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(nextLabel, org.openide.util.NbBundle.getMessage(TourPanel.class, "TourPanel.nextLabel.text")); // NOI18N
        nextLabel.setBorder(null);
        nextLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        nextLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                nextLabelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(leftbufferPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(numLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(closeLabel)))
                        .addGap(0, 0, 0))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(nextLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rightbufferPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numLabel)
                    .addComponent(closeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(nextLabel)
                .addGap(15, 15, 15))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(leftbufferPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(rightbufferPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void closeLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeLabelMouseClicked
        closeDemo(true);
    }//GEN-LAST:event_closeLabelMouseClicked

    private void nextLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nextLabelMouseClicked
        closeDemo(false);
    }//GEN-LAST:event_nextLabelMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel closeLabel;
    private javax.swing.JPanel leftbufferPanel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JLabel nextLabel;
    private javax.swing.JLabel numLabel;
    private javax.swing.JPanel rightbufferPanel;
    // End of variables declaration//GEN-END:variables
}
