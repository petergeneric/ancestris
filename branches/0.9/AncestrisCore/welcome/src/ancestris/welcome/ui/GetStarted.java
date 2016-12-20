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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ancestris.welcome.content.BundleSupport;
import ancestris.welcome.content.ActionButton;
import ancestris.welcome.content.Constants;
import ancestris.welcome.content.Utils;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.ImageIcon;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht & Frederic Lapeyre
 */
class GetStarted extends JPanel implements Constants {

    private int row;

    public GetStarted(String rootName) {
        super(new GridBagLayout());
        setOpaque(false);
        buildContent(rootName);
    }

    private void buildContent(String str) {
        String rootName = str;
        FileObject root = FileUtil.getConfigFile(rootName);
        if (null == root) {
            Logger.getLogger(GetStarted.class.getName()).log(Level.INFO,
                    "Start page content not found: " + "FileObject: " + rootName); //NOI18N
            return;
        }
        DataFolder folder = DataFolder.findFolder(root);
        if (null == folder) {
            Logger.getLogger(GetStarted.class.getName()).log(Level.INFO,
                    "Start page content not found: " + "DataFolder: " + rootName); //NOI18N
            return;
        }
        DataObject[] children = folder.getChildren();
        if (null == children) {
            Logger.getLogger(GetStarted.class.getName()).log(Level.INFO,
                    "Start page content not found: " + "DataObject: " + rootName); //NOI18N
            return;
        }
        for (int i = 0; i < children.length; i++) {
            if (children[i].getPrimaryFile().isFolder()) {
                String headerText = children[i].getNodeDelegate().getDisplayName();
                JLabel lblTitle = new JLabel(headerText);
                lblTitle.setFont(BUTTON_FONT);
                lblTitle.setHorizontalAlignment(JLabel.LEFT);
                lblTitle.setOpaque(true);
                lblTitle.setBorder(HEADER_TEXT_BORDER);
                add(lblTitle, new GridBagConstraints(0, row++, 1, 1, 1.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));

                DataFolder subFolder = DataFolder.findFolder(children[i].getPrimaryFile());
                DataObject[] subFolderChildren = subFolder.getChildren();
                for (int j = 0; j < subFolderChildren.length; j++) {
                    row = addLink(row, subFolderChildren[j]);
                }

            } else {
                row = addLink(row, children[i]);
            }
        }
    }

    private int addLink(int row, DataObject dob) {
        Action action = extractAction(dob);
        if (null != action) {
            ActionButton lb = new ActionButton(action, Utils.getUrlString(dob), Utils.getColor(COLOR_HEADER), true, dob.getPrimaryFile().getPath());
            lb.setFont(GET_STARTED_FONT);
            lb.getAccessibleContext().setAccessibleName(lb.getText());
            lb.getAccessibleContext().setAccessibleDescription(BundleSupport.getAccessibilityDescription("GettingStarted", lb.getText())); //NOI18N
            ImageIcon icon = extractIcon(dob);
            if (icon != null) {
                lb.setIcon(icon);
            }
            add(lb);
            add(Box.createRigidArea(new Dimension(0, 5)));

        }
        return row;
    }

    private Action extractAction(DataObject dob) {
        OpenCookie oc = dob.getCookie(OpenCookie.class);
        if (null != oc) {
            return new LinkAction(dob);
        }

        InstanceCookie.Of instCookie = dob.getCookie(InstanceCookie.Of.class);
        if (null != instCookie && instCookie.instanceOf(Action.class)) {
            try {
                Action res = (Action) instCookie.instanceCreate();
                if (null != res) {
                    res.putValue(Action.NAME, dob.getNodeDelegate().getDisplayName());
                }
                return res;
            } catch (Exception e) {
                Logger.getLogger(GetStarted.class.getName()).log(Level.INFO, null, e);
            }
        }
        return null;
    }

    private ImageIcon extractIcon(DataObject dob) {
        String str = (String) dob.getPrimaryFile().getAttribute("iconBase");
        if (str != null && !str.isEmpty()) {
            return new ImageIcon(ImageUtilities.loadImage(str));
        }
        return null;
    }

}
