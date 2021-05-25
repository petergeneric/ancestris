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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package ancestris.welcome.ui;

import ancestris.app.ActionOpen;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.resources.Images;
import ancestris.gedcom.RecentFiles;
import ancestris.welcome.content.ActionButton;
import ancestris.welcome.content.BundleSupport;
import ancestris.welcome.content.Constants;
import genj.gedcom.Gedcom;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import static ancestris.welcome.ui.Bundle.*;
import org.apache.commons.lang.WordUtils;
        
/**
 * Panel showing all recent files as clickable buttons.
 *
 * Modified to support Gedcom Files
 *
 * @author S. Aubrecht & Frederic Lapeyre
 */
public class RecentFilesPanel extends JPanel implements Constants {

    private static final int MAX_FILES = 5;
    private PropertyChangeListener changeListener = null;

    /** Creates a new instance of RecentFilesPanel */
    public RecentFilesPanel() {
        super(new BorderLayout());
        setOpaque(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        removeAll();
        add(rebuildContent(), BorderLayout.CENTER);
        RecentFiles.getDefault().addPropertyChangeListener(getPropertyChangeListener());
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        RecentFiles.getDefault().removePropertyChangeListener(getPropertyChangeListener());
    }

    private PropertyChangeListener getPropertyChangeListener() {
        if (null == changeListener) {
            changeListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent e) {
                    if (RecentFiles.PROP_RECENT_FILE_INFO.equals(e.getPropertyName())) {
                        removeAll();
                        add(rebuildContent(), BorderLayout.CENTER);
                        invalidate();
                        revalidate();
                        repaint();
                    }
                }
            };
        }
        return changeListener;
    }

    private JPanel rebuildContent() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        int row = 0;
        List<FileObject> files = RecentFiles.getDefault().getAll();
        for (FileObject p : files) {
            addFile(panel, row++, p);
            if (row >= MAX_FILES) {
                break;
            }
        }
        if (0 == row) {
            panel.add(new JLabel(BundleSupport.getLabel("NoRecentFile")), //NOI18N
                    new GridBagConstraints(0, row, 1, 1, 1.0, 0.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets(0, 10, 10, 10), 0, 0));
        } else {
            panel.add(new JLabel(), new GridBagConstraints(0, row, 1, 1, 0.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
        return panel;
    }

    private void addFile(JPanel panel, int row, final FileObject file) {
        ActionOpen action = new ActionOpen(file);
        action.setText(getDisplayName(file.getNameExt()));
        ActionButton b;
        b = new ActionButton(action, file.toURL().toString(), false, "RecentFile"); //NOI18N
        b.setFont(BUTTON_FONT);
        b.getAccessibleContext().setAccessibleName(b.getText());
        b.getAccessibleContext().setAccessibleDescription(
                BundleSupport.getAccessibilityDescription("RecentFile", b.getText())); //NOI18N
        //FIXME: Only gedcom are shown. We could probably resolve Mime type 
        // and get icon from this or DAO. Not sure it is possible
        b.setIcon(Gedcom.getImage());
        panel.add(b, new GridBagConstraints(0, row, 1, 1, 1.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        b = new ActionButton(new DelFile(file), "", false, "DelRecentFile"); //NOI18N
        panel.add(b, new GridBagConstraints(1, row, 1, 1, 1.0, 0.0,
                GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    }
    @NbBundle.Messages({
        "# {0} - gedcom file in history",
        "del.from.history=<html>Remove <b>{0}</b> from history</html>"
    })
    private static class DelFile extends AbstractAncestrisAction{
        private final FileObject file;

        public DelFile(FileObject file) {
            this.file = file;
            setText("");
            setTip(del_from_history(file.getNameExt()));
            setImage(Images.imgDel);
        }
        

        @Override
        public void actionPerformed(ActionEvent e) {
            RecentFiles.getDefault().remove(file);
        }
        
    }
    
   /**
     * Returns the displayed name of the Gedcom file
     * @return 
     */
    private String getDisplayName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        name = name.replace("_", " ");
        char[] delimiters = {' ', '-'};
        String ret = WordUtils.capitalize(name.substring(0, name.lastIndexOf(".") == -1 ? name.length() : name.lastIndexOf(".")), delimiters);
        if (ret.length() > 44) {
            ret = ret.substring(0, 44) + "...";
        }
        return ret;
    }


}
