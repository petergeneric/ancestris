/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package ancestris.modules.commonAncestor.quicksearch.module;
import ancestris.modules.commonAncestor.quicksearch.spi.SearchProvider;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.text.JTextComponent;
import org.openide.util.ImageUtilities;

/**
 * Quick search toolbar component
 * @author  Jan Becicka
 */
public class QuickSearchComboBar extends AbstractQuickSearchComboBar {

    private final ImageIcon findIcon = new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/commonAncestor/quicksearch/module/resources/find.png")); // NOI18N
    
    /** Timer used for progress animation (see #143019).  */
    private final Timer animationTimer = new Timer(100, new ActionListener() {

        ImageIcon icons[];
        int index = 0;

        public void actionPerformed(ActionEvent e) {
            if (icons == null) {
                icons = new ImageIcon[8];
                for (int i = 0; i < 8; i++) {
                    icons[i] = ImageUtilities.loadImageIcon("ancestris/modules/commonAncestor/quicksearch/module/resources/progress_" + i + ".png", false);  //NOI18N
                }
            }
            jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 6));
            jLabel2.setIcon(icons[index]);
            //mac os x
            jLabel2.repaint();
            
            index = (index + 1) % 8;
        }
    });

    /*
     * Contructor for quicksearch mono instance .
     * Categories and providers must be declared in configuration file parameter /QuickSearch
     * @ see ProviderModel class
     * 
     */
    public QuickSearchComboBar(KeyStroke ks) {
        super( ks );

        initComponents();
    }
    
    /*
     * Contructor for quicksearch multi instances in same module
     * Categories and serachProvider are given in parameters
     */
    public QuickSearchComboBar(String categoryName, String categoryDisplayName, SearchProvider searchProvider, String commandPrefix, KeyStroke ks) {
        super(categoryName, categoryDisplayName, searchProvider, commandPrefix, ks );

        initComponents();
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jSeparator1 = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(getComboBorderColor()));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setIcon(findIcon);
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(QuickSearchComboBar.class, "QuickSearchComboBar.jLabel2.toolTipText")); // NOI18N
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel2.setName("jLabel2"); // NOI18N
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabel2MousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel1.add(jLabel2, gridBagConstraints);

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setViewportBorder(null);
        jScrollPane1.setMinimumSize(new java.awt.Dimension(2, 18));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jScrollPane1.setViewportView(command);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jPanel1, gridBagConstraints);
    }

    private void jLabel2MousePressed(java.awt.event.MouseEvent evt) {
        maybeShowPopup(evt);
    }

    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;

    @Override
    protected JTextComponent createCommandField() {
        JTextArea res = new DynamicWidthTA();
        res.setRows(1);
        res.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 1));
        // disable default Swing's Ctrl+Shift+O binding to enable our global action
        InputMap curIm = res.getInputMap(JComponent.WHEN_FOCUSED);
        while (curIm != null) {
            curIm.remove(KeyStroke.getKeyStroke(
                    KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
            curIm = curIm.getParent();
        }
        return res;
    }

    @Override
    protected JComponent getInnerComponent() {
        return jPanel1;
    }

    void startProgressAnimation() {
        if (animationTimer != null && !animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    void stopProgressAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
            jLabel2.setIcon(findIcon);
            jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }
    }

    private final class DynamicWidthTA extends JTextArea {
        private Dimension prefWidth;

        @Override
        public Dimension getPreferredSize() {
            if (prefWidth == null) {
                Dimension orig = super.getPreferredSize();
                prefWidth = new Dimension(computePrefWidth(), orig.height);
            }
            return prefWidth;
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }
}
