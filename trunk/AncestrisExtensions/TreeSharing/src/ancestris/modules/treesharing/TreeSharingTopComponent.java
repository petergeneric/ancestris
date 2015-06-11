/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.treesharing;

import org.openide.util.ImageUtilities;
import ancestris.modules.treesharing.panels.TreeSharingPanel;
import javax.swing.GroupLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author frederic
 */
@ConvertAsProperties(dtd = "-//ancestris.modules.treesharing//EN",
autostore = false)
public class TreeSharingTopComponent extends TopComponent {

    private static TreeSharingTopComponent instance;
    private static final String PREFERRED_ID = "TreeSharingTopComponent";  // NOI18N
    private static final String ICON_PATH = "ancestris/modules/treesharing/resources/TreeSharing.png";

    public TreeSharingTopComponent() {
        setName(NbBundle.getMessage(TreeSharingTopComponent.class, "CTL_TreeSharingTopComponent"));
        setToolTipText(NbBundle.getMessage(TreeSharingTopComponent.class, "HINT_TreeSharingTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        initMainPanel();

    }

    private void initMainPanel() {
        TreeSharingPanel panel = new TreeSharingPanel();
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized TreeSharingTopComponent getDefault() {
        if (instance == null) {
            instance = new TreeSharingTopComponent();
        }
        return instance;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }    
    
}
