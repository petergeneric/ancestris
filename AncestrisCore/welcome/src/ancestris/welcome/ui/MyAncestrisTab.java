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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import ancestris.welcome.content.BundleSupport;
import ancestris.welcome.content.ContentSection;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;

/**
 *
 * @author S. Aubrecht & Frederic Lapeyre
 */
class MyAncestrisTab extends AbstractTab {
    
    public MyAncestrisTab() {
        setName(BundleSupport.getLabel( "MyAncestrisTab")); //NOI18N
    }

    @Override
    protected void buildContent() {
        JPanel main = new JPanel( new GridLayout(1,0) );
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder());
        add( main, BorderLayout.CENTER );

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(BorderFactory.createEmptyBorder());
        main.add(new ContentSection( leftPanel,false));
        leftPanel.add( new ContentSection( BundleSupport.getLabel( "SectionRecentFiles" ), new RecentFilesPanel() ) );   //NOI18N
        leftPanel.add( new ContentSection( new OpenGedcomPanel()) );
        leftPanel.add( new ContentSection( new PreferencesPanel()) );
        //leftPanel.add( new ContentSection( BundleSupport.getLabel( "SectionPersonalise"), new PreferencesPanel()) );
        //leftPanel.add( new ContentSection( BundleSupport.getLabel( "SectionCreateOpen"), new NewGedcomPanel() ) );
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createEmptyBorder());
        main.add(new ContentSection( rightPanel,true));
        //rightPanel.add( new ContentSection( BundleSupport.getLabel( "SectionPersonalise"), new PluginsPanel(true) ) );   //NOI18N
        rightPanel.add(new ContentSection(BundleSupport.getLabel("SectionNewsAndTutorials"), new ArticlesAndNews(), true, true));  //NOI18N

        add( new BottomBar(), BorderLayout.SOUTH );
    }
}
