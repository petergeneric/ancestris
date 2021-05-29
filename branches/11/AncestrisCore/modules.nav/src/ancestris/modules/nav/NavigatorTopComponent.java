/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2011 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.nav;

import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.Image;
import javax.swing.JScrollPane;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Top component which displays something.
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public final class NavigatorTopComponent extends AncestrisTopComponent {

    private static final String PREFERRED_ID = "NavigatorExtendedTopComponent";  // NOI18N
    private static NavigatorTopComponent factory;
    private Gedcom gedcom = null;
    FamilyPanel familyPanel = null;
    JScrollPane familyScrolPane = null;
    
    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.OUTPUT;
    }

    @Override
    public boolean createPanel() {
        setContext(getContext());
        return true;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized NavigatorTopComponent getFactory() {
        if (factory == null) {
            factory = new NavigatorTopComponent();
        }
        return factory;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public Image getImageIcon() {
        return ImageUtilities.loadImage("ancestris/modules/nav/NaviconPlus.png", true);
    }

    @Override
    public void setContextImpl(Context context) {
        if (context == null) {
            return;
        }
        if (familyPanel == null) {
            familyPanel = new FamilyPanel();
            familyScrolPane = new JScrollPane(familyPanel);
            setPanel(familyScrolPane);
            gedcom = context.getGedcom();
            familyPanel.init(gedcom);
        }
        familyPanel.setContext(context);
        repaint();
    }
    
    public void componentClosed() {
        if (gedcom != null && familyPanel != null) {
            familyPanel.close(gedcom);
        }
    }
}
