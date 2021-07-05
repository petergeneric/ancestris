/*
 * Copyright (C) 2020 Zurga
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
package ancestris.modules.exports.geneanet;

import ancestris.modules.exports.geneanet.entity.GeneanetParserResult;
import ancestris.modules.exports.geneanet.entity.GeneanetStatusEnum;
import ancestris.modules.exports.geneanet.entity.GeneanetToken;
import ancestris.modules.exports.geneanet.entity.GeneanetUpdateStatus;
import ancestris.modules.exports.geneanet.utils.GeneanetException;
import ancestris.modules.exports.geneanet.utils.GeneanetLogWorker;
import ancestris.modules.exports.geneanet.utils.GeneanetMediaProducer;
import ancestris.modules.exports.geneanet.utils.GeneanetMediaWorker;
import ancestris.modules.exports.geneanet.utils.GeneanetQueueManager;
import ancestris.modules.exports.geneanet.utils.GeneanetUtil;
import ancestris.usage.UsageManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Registry;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.SwingWorker;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Zurga
 */
public class GeneanetSynchronizePanel extends javax.swing.JPanel {

    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    private final File geneanetFile;
    private String clientId;
    private String secretId;
    private GeneanetToken token;
    private final Context currentContext;
    private Set<String> mediaAlreadySentList = ConcurrentHashMap.newKeySet();
    private Preferences prefs;
    private int nbWorker = 5;

    /**
     * Creates new form GeneanetSynchronizePanel
     */
    public GeneanetSynchronizePanel(File file, Context context) {
        initComponents();
        geneanetFile = file;
        fileTextField.setText(file.getAbsolutePath());
        currentContext = context;
        getIds();
        loadSettings();
        checkMedias();
        encoursFile.setText("");
        nbEncoursFile.setText("");
    }

    private void getIds() {
        String retour = UsageManager.getKey("K0000001");
        if (retour == null || "".equals(retour)) {
            LOG.log(Level.SEVERE, NbBundle.getMessage(GeneanetSynchronizePanel.class, "ids.not.found"));
            progressArea.setText("");
            updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, "ids.not.found"));
            updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, "ids.solution"));
            syncButton.setEnabled(false);
            return;
        }
        String[] ids = retour.split(" ");
        clientId = ids[0];
        secretId = ids[1];
    }

    private void loadSettings() {
       // Remove old settings without losing eventual cache. Coe to remove after 2020/12/06
        final Registry registry = currentContext.getGedcom().getRegistry();
        final String oldUsername = registry.get("Geneanet.username", "");
        final String oldPwd = registry.get("Geneanet.pwd", "");
        String oldMediaList = registry.get("Geneanet.medialist", "");
        registry.remove("Geneanet.username");
        registry.remove("Geneanet.pwd");
        registry.remove("Geneanet.medialist");

        // New settings
        if (prefs == null) {
            prefs = NbPreferences.forModule(GeneanetSynchronizePanel.class).node(currentContext.getGedcom().getName());
        }
        idTextField.setText(prefs.get("Geneanet.username", oldUsername));
        pwdTextField.setText(prefs.get("Geneanet.pwd", oldPwd));
        String mediaList = prefs.get("Geneanet.medialist", oldMediaList);
        mediaAlreadySentList = Arrays.stream(mediaList.split(",")).collect(Collectors.toCollection(HashSet::new));
    }

    private void saveSettings() {
        if (prefs == null) {
            return;
        }   
        prefs.put("Geneanet.username", idTextField.getText());
        prefs.put("Geneanet.pwd", String.valueOf(pwdTextField.getPassword()));
        prefs.put("Geneanet.medialist", mediaAlreadySentList.stream().collect(Collectors.joining(",")));
    }

    private void checkMedias() {
        final Gedcom gedcom = currentContext.getGedcom();
        // Count FILE and remove 1 for header.
        nbMediaValue.setText(String.valueOf(gedcom.getPropertyCount("FILE") - 1));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        syncButton = new javax.swing.JButton();
        idLabel = new javax.swing.JLabel();
        idTextField = new javax.swing.JTextField();
        pwdTextField = new javax.swing.JPasswordField();
        pwdLabel = new javax.swing.JLabel();
        fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        nbMediaLabel = new javax.swing.JLabel();
        nbMediaValue = new javax.swing.JLabel();
        sendMediaCb = new javax.swing.JCheckBox();
        nbEncoursFile = new javax.swing.JLabel();
        encoursFile = new javax.swing.JLabel();
        sendAgainCb = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        progressArea = new javax.swing.JTextArea();

        org.openide.awt.Mnemonics.setLocalizedText(syncButton, org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.syncButton.text")); // NOI18N
        syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(idLabel, org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.idLabel.text")); // NOI18N
        idLabel.setMaximumSize(new java.awt.Dimension(119, 25));
        idLabel.setMinimumSize(new java.awt.Dimension(119, 25));
        idLabel.setPreferredSize(new java.awt.Dimension(119, 25));

        idTextField.setText(org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.idTextField.text")); // NOI18N
        idTextField.setToolTipText(org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.idTextField.toolTipText")); // NOI18N
        idTextField.setMaximumSize(new java.awt.Dimension(500, 25));
        idTextField.setMinimumSize(new java.awt.Dimension(6, 25));
        idTextField.setPreferredSize(new java.awt.Dimension(69, 25));

        pwdTextField.setText(org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.pwdTextField.text")); // NOI18N
        pwdTextField.setMaximumSize(new java.awt.Dimension(500, 25));
        pwdTextField.setMinimumSize(new java.awt.Dimension(6, 25));
        pwdTextField.setPreferredSize(new java.awt.Dimension(69, 25));

        org.openide.awt.Mnemonics.setLocalizedText(pwdLabel, org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.pwdLabel.text")); // NOI18N
        pwdLabel.setMaximumSize(new java.awt.Dimension(113, 25));
        pwdLabel.setMinimumSize(new java.awt.Dimension(113, 25));
        pwdLabel.setPreferredSize(new java.awt.Dimension(113, 25));

        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.fileLabel.text")); // NOI18N
        fileLabel.setMaximumSize(new java.awt.Dimension(92, 25));
        fileLabel.setMinimumSize(new java.awt.Dimension(92, 25));
        fileLabel.setPreferredSize(new java.awt.Dimension(92, 25));

        fileTextField.setText(org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.fileTextField.text")); // NOI18N
        fileTextField.setToolTipText(org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.fileTextField.toolTipText")); // NOI18N
        fileTextField.setEnabled(false);
        fileTextField.setMaximumSize(new java.awt.Dimension(2147483647, 25));
        fileTextField.setMinimumSize(new java.awt.Dimension(6, 25));
        fileTextField.setPreferredSize(new java.awt.Dimension(69, 25));

        org.openide.awt.Mnemonics.setLocalizedText(nbMediaLabel, org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.nbMediaLabel.text")); // NOI18N
        nbMediaLabel.setMaximumSize(new java.awt.Dimension(173, 25));
        nbMediaLabel.setMinimumSize(new java.awt.Dimension(92, 25));
        nbMediaLabel.setPreferredSize(new java.awt.Dimension(92, 25));

        org.openide.awt.Mnemonics.setLocalizedText(nbMediaValue, org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.nbMediaValue.text")); // NOI18N

        sendMediaCb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(sendMediaCb, org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.sendMediaCb.text")); // NOI18N
        sendMediaCb.setToolTipText(org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.sendMediaCb.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(nbEncoursFile, org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.nbEncoursFile.text")); // NOI18N
        nbEncoursFile.setMaximumSize(new java.awt.Dimension(15, 25));
        nbEncoursFile.setMinimumSize(new java.awt.Dimension(15, 25));

        org.openide.awt.Mnemonics.setLocalizedText(encoursFile, org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.encoursFile.text")); // NOI18N
        encoursFile.setMaximumSize(new java.awt.Dimension(300, 25));
        encoursFile.setMinimumSize(new java.awt.Dimension(300, 25));

        org.openide.awt.Mnemonics.setLocalizedText(sendAgainCb, org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.sendAgainCb.text")); // NOI18N
        sendAgainCb.setToolTipText(org.openide.util.NbBundle.getMessage(GeneanetSynchronizePanel.class, "GeneanetSynchronizePanel.sendAgainCb.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(fileLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pwdLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(idLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addComponent(nbMediaLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(nbMediaValue)))
                    .addComponent(nbEncoursFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(encoursFile, javax.swing.GroupLayout.PREFERRED_SIZE, 443, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 99, Short.MAX_VALUE)
                        .addComponent(syncButton))
                    .addComponent(pwdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(idTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(sendMediaCb, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sendAgainCb, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(idLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(idTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pwdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pwdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nbMediaLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nbMediaValue)
                    .addComponent(sendMediaCb)
                    .addComponent(sendAgainCb))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(syncButton)
                    .addComponent(nbEncoursFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(encoursFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        progressArea.setColumns(20);
        progressArea.setRows(5);
        jScrollPane1.setViewportView(progressArea);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void syncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncButtonActionPerformed
        SwingWorker<String, String> worker = new SwingWorker() {
            @Override
            protected String doInBackground() throws Exception {
                synchronize();
                return "done";
            }
        };
        worker.execute();

    }//GEN-LAST:event_syncButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel encoursFile;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel idLabel;
    private javax.swing.JTextField idTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel nbEncoursFile;
    private javax.swing.JLabel nbMediaLabel;
    private javax.swing.JLabel nbMediaValue;
    private javax.swing.JTextArea progressArea;
    private javax.swing.JLabel pwdLabel;
    private javax.swing.JPasswordField pwdTextField;
    private javax.swing.JCheckBox sendAgainCb;
    private javax.swing.JCheckBox sendMediaCb;
    private javax.swing.JButton syncButton;
    // End of variables declaration//GEN-END:variables

    private void synchronize() {
        progressArea.setText("");
        if (sendAgainCb.isSelected()) {
            mediaAlreadySentList.clear();
        }
        try {
            if (token == null) {
                token = GeneanetUtil.getToken(idTextField.getText(), String.valueOf(pwdTextField.getPassword()), clientId, secretId);
            }
            updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, "token.ok.message"));

            if (!GeneanetUtil.getUserInfo(token)) {
                updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, "userinfo.tree.error"));
                return;
            }
            updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, "userinfo.ok.message"));
            GeneanetUtil.sendFile(token, geneanetFile);
            updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, "tree.ok.message"));
            GeneanetUpdateStatus status = GeneanetUtil.getStatus(token);
            updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, status.getStatus().getStatus())
                    + " " + status.getStep().getStep() + "/5 " + NbBundle.getMessage(GeneanetSynchronizePanel.class, status.getStep().getStepName()));
            while (GeneanetStatusEnum.RUNNING == status.getStatus()) {
                // Wait 5 seconds before trying again.
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // Nothing to log.
                    LOG.log(Level.FINE, "Error during sleep", e);
                }
                status = GeneanetUtil.getStatus(token);
                updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, status.getStatus().getStatus())
                        + " " + status.getStep().getStep() + "/5 " + NbBundle.getMessage(GeneanetSynchronizePanel.class, status.getStep().getStepName()));
            }
            if ("error".equals(status.getStatus())) {
                throw new GeneanetException("status.done.error", null, null);
            }
            updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, "status.ok.message"));
            if (sendMediaCb.isSelected()) {
                sendMedia();
            }
            updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, "done.ok"));
        } catch (GeneanetException e) {
            updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, e.getLocalCode()));
        }
        saveSettings();
   }

    private void sendMedia() throws GeneanetException {
        GeneanetParserResult pResult = GeneanetUtil.getMediaStatus(token);
        updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, "media.number.ok") + pResult.getNbMedia());
        nbMediaValue.setText(String.valueOf(pResult.getNbMedia()));
        if (!pResult.getKoMedia().isEmpty()) {
            updateTextArea(NbBundle.getMessage(GeneanetSynchronizePanel.class, "media.ko"));
            for (String koMedia : pResult.getKoMedia()) {
                updateTextArea(koMedia);
            }
        }
        // Create threaded workers.
        final GeneanetQueueManager gqm = new GeneanetQueueManager();
        
        new Thread(new GeneanetMediaProducer(gqm, mediaAlreadySentList, pResult.getOkMedia(), currentContext.getGedcom(), nbWorker)).start();
        
        for (int i = 0; i < nbWorker; i++) {
            new Thread(new GeneanetMediaWorker(gqm, token, mediaAlreadySentList, nbEncoursFile, encoursFile)).start();
        }
        new Thread(new GeneanetLogWorker(gqm, progressArea, nbWorker)).start();
        
        // Wiat completion
        try {
            // Wait 5 seconds before continue, allow workers to begin job.
            Thread.sleep(5000);
            while (gqm.countMedia() != 0) {
                LOG.log(Level.INFO, "sleep media");
                Thread.sleep(1000);
            }
            while (gqm.countUpdate() != 0) {
                LOG.log(Level.INFO, "sleep message");
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            LOG.log(Level.INFO, "interrupted during rest ", e);
        }
    }

    private void updateTextArea(String value) {
        progressArea.append(value);
        progressArea.append("\n");
    }

}
