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

package ancestris.welcome;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.prefs.Preferences;
import ancestris.welcome.content.BundleSupport;
import ancestris.welcome.content.Constants;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author S. Aubrecht
 */
public class WelcomeOptions {

    private static WelcomeOptions theInstance;
    
    private static final String PROP_SHOW_ON_STARTUP = "showOnStartup";
    private static final String PROP_LAST_ACTIVE_TAB = "lastActiveTab";
    private static final String PROP_START_COUNTER = "startCounter";
    
    private static final int INITIAL_START_LIMIT = 3;
    
    private PropertyChangeSupport propSupport;
    
    /** Creates a new instance of WelcomeOptions */
    private WelcomeOptions() {
    }

    private Preferences prefs() {
        return NbPreferences.forModule(WelcomeOptions.class);
    }

    public static synchronized WelcomeOptions getDefault() {
        if( null == theInstance ) {
            theInstance = new WelcomeOptions();
        }
        return theInstance;
    }
 
    public void setShowOnStartup( boolean show ) {
        boolean oldVal = isShowOnStartup();
        if( oldVal == show ) {
            return;
        }
        prefs().putBoolean(PROP_SHOW_ON_STARTUP, show);
        if( null != propSupport )
            propSupport.firePropertyChange( PROP_SHOW_ON_STARTUP, oldVal, show );

        LogRecord rec = new LogRecord(Level.INFO, "USG_SHOW_START_PAGE"); //NOI18N
        rec.setParameters(new Object[] {show} );
        rec.setLoggerName(Constants.USAGE_LOGGER.getName());
        rec.setResourceBundle(NbBundle.getBundle(BundleSupport.BUNDLE_NAME));
        rec.setResourceBundleName(BundleSupport.BUNDLE_NAME);

        Constants.USAGE_LOGGER.log(rec);

    }

    public boolean isShowOnStartup() {
        return prefs().getBoolean(PROP_SHOW_ON_STARTUP, !Boolean.getBoolean("netbeans.full.hack"));
    }

    public void setLastActiveTab( int tabIndex ) {
        int oldVal = getLastActiveTab();
        prefs().putInt(PROP_LAST_ACTIVE_TAB, tabIndex);
        if( null != propSupport )
            propSupport.firePropertyChange( PROP_LAST_ACTIVE_TAB, oldVal, tabIndex );
    }

    public int getLastActiveTab() {
        return prefs().getInt(PROP_LAST_ACTIVE_TAB, -1);
    }
    
    public boolean isFirstStarts() {
        int i = prefs().getInt(PROP_START_COUNTER, -1) + 1;
        return i <= INITIAL_START_LIMIT;
    }
    
    public boolean isSecondStart() {
        return prefs().getInt(PROP_START_COUNTER, -1) == 2;
    }
    
    public void incrementStartCounter() {
        int count = prefs().getInt(PROP_START_COUNTER, 0) + 1;
        if( count > INITIAL_START_LIMIT )
            return; //we're just interested in the first and second start so don't bother any more then
        prefs().putInt( PROP_START_COUNTER, count );
    }
    
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        if( null == propSupport )
            propSupport = new PropertyChangeSupport( this );
        propSupport.addPropertyChangeListener( l );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        if( null == propSupport )
            return;
        propSupport.removePropertyChangeListener( l );
    }
}
