/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SearchPanel.java
 *
 * Created on 30 avr. 2012, 16:31:25
 */
package org.ancestris.trancestris.application.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.GroupLayout.Alignment;
import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.ancestris.trancestris.editors.actions.EditorSearchPanel;
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.ancestris.trancestris.resources.ZipArchive;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author lemovice
 */
public class SearchPanel extends javax.swing.JPanel {

    private class SearchPanellinkListener implements HyperlinkListener {

        @Override
        public void hyperlinkUpdate(HyperlinkEvent evt) {
            if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");
                if (tc != null) {
                    // Show the new page in the editor pane.
                    ((ZipExplorerTopComponent) tc).selectNode(evt.getDescription());
                }
            }
        }
    }
    ZipArchive zipArchive = null;
    EditorSearchPanel editorSearchPanel = EditorSearchPanel.getInstance();

    /**
     * Creates new form SearchPanel
     */
    public SearchPanel(ZipArchive zipArchive) {
        this.zipArchive = zipArchive;
        initComponents();
        fromToggleButton.setText(zipArchive.getFromLocale().getDisplayLanguage());
        toToggleButton.setText(zipArchive.getToLocale().getDisplayLanguage());
        fromToggleButton.setSelected(editorSearchPanel.isFromLocaleToggleButtonSelected());
        toToggleButton.setSelected(editorSearchPanel.isToLocaleToggleButtonSelected());
        caseSensitiveCheckBox.setSelected(editorSearchPanel.isCaseSensitiveCheckBoxSelected());
        expressionTextField.setText(editorSearchPanel.getExpressionTextField());
        resultEditorPane.addHyperlinkListener(new SearchPanellinkListener());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        localeButtonGroup = new ButtonGroup();
        jPanel1 = new JPanel();
        expressionTextField = new JTextField();
        searchButton = new JButton();
        caseSensitiveCheckBox = new JCheckBox();
        fromToggleButton = new JToggleButton();
        toToggleButton = new JToggleButton();
        jPanel2 = new JPanel();
        resultScrollPane = new JScrollPane();
        resultEditorPane = new JEditorPane();

        setLayout(new BorderLayout());

        expressionTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                expressionTextFieldKeyPressed(evt);
            }
        });

        searchButton.setText(NbBundle.getMessage(SearchPanel.class, "SearchPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        caseSensitiveCheckBox.setSelected(true);
        caseSensitiveCheckBox.setText(NbBundle.getMessage(SearchPanel.class, "SearchPanel.caseSensitiveCheckBox.text")); // NOI18N
        caseSensitiveCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                caseSensitiveCheckBoxActionPerformed(evt);
            }
        });

        localeButtonGroup.add(fromToggleButton);
        fromToggleButton.setText("From Locale");
        fromToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fromToggleButtonActionPerformed(evt);
            }
        });

        localeButtonGroup.add(toToggleButton);
        toToggleButton.setText("To Locale");
        toToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                toToggleButtonActionPerformed(evt);
            }
        });

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(caseSensitiveCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(fromToggleButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(toToggleButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(expressionTextField, GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(searchButton)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(caseSensitiveCheckBox)
                    .addComponent(fromToggleButton)
                    .addComponent(toToggleButton)
                    .addComponent(expressionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, BorderLayout.NORTH);

        resultEditorPane.setEditable(false);
        resultEditorPane.setContentType("text/html"); // NOI18N
        resultEditorPane.setMinimumSize(new Dimension(106, 210));
        resultEditorPane.setPreferredSize(new Dimension(106, 210));
        resultScrollPane.setViewportView(resultEditorPane);

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 421, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(resultScrollPane, GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 256, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addComponent(resultScrollPane, GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        add(jPanel2, BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        search();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void expressionTextFieldKeyPressed(KeyEvent evt) {//GEN-FIRST:event_expressionTextFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            search();
        }
    }//GEN-LAST:event_expressionTextFieldKeyPressed

    private void caseSensitiveCheckBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_caseSensitiveCheckBoxActionPerformed
        editorSearchPanel.setCaseSensitiveCheckBoxSelected(caseSensitiveCheckBox.isSelected());
    }//GEN-LAST:event_caseSensitiveCheckBoxActionPerformed

    private void fromToggleButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_fromToggleButtonActionPerformed
        editorSearchPanel.setFromLocaleToggleButtonSelected(fromToggleButton.isSelected());
    }//GEN-LAST:event_fromToggleButtonActionPerformed

    private void toToggleButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_toToggleButtonActionPerformed
        editorSearchPanel.setToLocaleToggleButtonSelected(toToggleButton.isSelected());
    }//GEN-LAST:event_toToggleButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox caseSensitiveCheckBox;
    private JTextField expressionTextField;
    private JToggleButton fromToggleButton;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private ButtonGroup localeButtonGroup;
    private JEditorPane resultEditorPane;
    private JScrollPane resultScrollPane;
    private JButton searchButton;
    private JToggleButton toToggleButton;
    // End of variables declaration//GEN-END:variables

    private void search() {
        List<String> search = null;
        boolean caseSensitive = caseSensitiveCheckBox.isSelected();
        if (fromToggleButton.isSelected()) {
            search = zipArchive.search(expressionTextField.getText(), true, caseSensitive);
        } else {
            search = zipArchive.search(expressionTextField.getText(), false, caseSensitive);
        }

        editorSearchPanel.setExpressionTextField(expressionTextField.getText());
        editorSearchPanel.setCaseSensitiveCheckBoxSelected(caseSensitive);
        if (fromToggleButton.isSelected()) {
            editorSearchPanel.setFromLocaleToggleButtonSelected(true);
            editorSearchPanel.setToLocaleToggleButtonSelected(false);
        } else {
            editorSearchPanel.setFromLocaleToggleButtonSelected(false);
            editorSearchPanel.setToLocaleToggleButtonSelected(true);
        }

        // Clear the Text Area
        resultEditorPane.setText("");
        if (search.isEmpty()) {
            resultEditorPane.setText(NbBundle.getMessage(SearchPanel.class, "SearchPanel.searchResult.text", expressionTextField.getText()));
        } else {
            MessageFormat oddlink = new MessageFormat("<p align=\"left\" bgcolor=Silver><a href= {0}>{0}</a></p>");
            MessageFormat evenlink = new MessageFormat("<p align=\"left\" bgcolor=white><a href= {0}>{0}</a></p>");
            String resultList = "<style>p {margin-top: 0em; margin-bottom: 2em;}</style>";
            int i = 0;

            for (String dirName : search) {
                if (i++ % 2 == 0) {
                    resultList += oddlink.format(new Object[]{dirName});
                } else {
                    resultList += evenlink.format(new Object[]{dirName});
                }
            }
            resultEditorPane.setText(resultList);
        }
    }
}
