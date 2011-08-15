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
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;
import ancestris.welcome.content.Constants;
import org.openide.util.ImageUtilities;

/**
 * Base class for inner tabs in the Welcome Page
 * 
 * @author S. Aubrecht
 */
abstract class AbstractTab extends JPanel implements Constants {

    private boolean initialized = false;
    private final Image bottomBar;
    
    public AbstractTab() {
        super( new BorderLayout() );
        setOpaque(false);
         bottomBar = ImageUtilities.loadImage("ancestris/welcome/resources/bottom_bar.png"); //NOI18N
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if( !initialized ) {
            buildContent();
            initialized = true;
        }
    }

    protected abstract void buildContent();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int imgWidth = bottomBar.getWidth(this);
        int imgHeight = bottomBar.getHeight(this);
        g.drawImage(bottomBar, getWidth()-imgWidth, getHeight()-imgHeight, this);
    }
}
