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

package ancestris.welcome.content;

import ancestris.util.RecentFiles;
import ancestris.util.RecentFiles.GedcomFileInformation;
import genjfr.app.ActionOpen;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel showing all recent files as clickable buttons.
 * 
 * @author S. Aubrecht
 */
public class RecentFilesPanel extends JPanel implements Constants {

    private static final int MAX_FILES = 10;

    private PropertyChangeListener changeListener;

    /** Creates a new instance of RecentFilesPanel */
    public RecentFilesPanel() {
        super( new BorderLayout() );
        setOpaque(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        removeAll();
        add( rebuildContent(), BorderLayout.CENTER );
//        RecentProjects.getDefault().addPropertyChangeListener( getPropertyChangeListener() );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
//        RecentProjects.getDefault().removePropertyChangeListener( getPropertyChangeListener() );
    }

//    private PropertyChangeListener getPropertyChangeListener() {
//        if( null == changeListener ) {
//            changeListener = new PropertyChangeListener() {
//                @Override
//                public void propertyChange(PropertyChangeEvent e) {
//                    if( RecentProjects.PROP_RECENT_PROJECT_INFO.equals( e.getPropertyName() ) ) {
//                        removeAll();
//                        add( rebuildContent(), BorderLayout.CENTER );
//                        invalidate();
//                        revalidate();
//                        repaint();
//                    }
//                }
//            };
//        }
//        return changeListener;
//    }
//
    private JPanel rebuildContent() {
        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );
        int row = 0;
        List<GedcomFileInformation> files = RecentFiles.getDefault().getRecentFilesInformation();
        for( GedcomFileInformation p : files ) {
            addFile( panel, row++, p );
            if( row >= MAX_FILES )
                break;
        }
        if( 0 == row ) {
            panel.add( new JLabel(BundleSupport.getLabel( "NoRecentFile" )), //NOI18N
                    new GridBagConstraints( 0,row,1,1,1.0,1.0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(10,10,10,10), 0, 0 ) );
        } else {
            panel.add( new JLabel(), new GridBagConstraints( 0,row,1,1,0.0,1.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0,0,0,0), 0, 0 ) );
        }
        return panel;
    }

    private void addFile( JPanel panel, int row, final GedcomFileInformation file ) {
        OpenGedcomFileAction action = new OpenGedcomFileAction( file );
        ActionButton b = new ActionButton( action, file.getURL().toString(), false, "RecentFile" ); //NOI18N
        b.setFont( BUTTON_FONT );
        b.getAccessibleContext().setAccessibleName( b.getText() );
        b.getAccessibleContext().setAccessibleDescription(
                BundleSupport.getAccessibilityDescription( "RecentFile", b.getText() ) ); //NOI18N
        b.setIcon(file.getIcon());
        panel.add( b, new GridBagConstraints( 0,row,1,1,1.0,0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2,2,2,2), 0, 0 ) );
    }

    private static class OpenGedcomFileAction extends ActionOpen {
        private GedcomFileInformation file;
        public OpenGedcomFileAction( GedcomFileInformation file ) {
            super( file.getURL());
            setText((new File(file.getURL().getFile())).getName());
            this.file = file;
        }
    }
}
