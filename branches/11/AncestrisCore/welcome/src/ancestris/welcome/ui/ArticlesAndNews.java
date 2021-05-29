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

import ancestris.welcome.content.BundleSupport;
import ancestris.welcome.content.CombinationRSSFeed;
import ancestris.welcome.content.RSSFeed;
import ancestris.welcome.content.RSSFeedReaderPanel;
import ancestris.welcome.content.WebLink;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author S. Aubrecht & Frederic Lapeyre
 */
class ArticlesAndNews extends RSSFeedReaderPanel {

    private RSSFeed feed;

    private static final int MAX_ARTICLES_COUNT = 10;

    public ArticlesAndNews() {
        super( "ArticlesAndNews", true ); // NOI18N
       
    }

    protected JComponent buildBottomContent() {
        WebLink news = new WebLink("ForumLists", true); // NOI18N
        BundleSupport.setAccessibilityProperties( news, "ForumLists" ); //NOI18N

        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque(false);

        panel.add( news, new GridBagConstraints(0,1,1,1,0.0,0.0,
                GridBagConstraints.SOUTHWEST,GridBagConstraints.HORIZONTAL,
                new Insets(5,0,0,15),0,0) );
        panel.add( new JLabel(), new GridBagConstraints(1,1,1,1,1.0,0.0,
                GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,
                new Insets(5,5,0,0),0,0) );

        return panel;
    }

    private static class ArticlesAndNewsRSSFeed extends CombinationRSSFeed {
        public ArticlesAndNewsRSSFeed( String url1, String url2, boolean showProxyButton ) {
            super( url1, url2, showProxyButton, MAX_ARTICLES_COUNT );
        }
    }
}
