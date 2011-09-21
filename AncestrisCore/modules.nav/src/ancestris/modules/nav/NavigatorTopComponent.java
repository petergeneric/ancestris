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
import genj.gedcom.Context;
import genj.view.SelectionListener;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import java.awt.Image;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Top component which displays something.
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public final class NavigatorTopComponent extends AncestrisTopComponent implements SelectionListener {

    private static final String PREFERRED_ID = "AncestrisNavigator";  // NOI18N
    private static NavigatorTopComponent factory;
    FamilyPanel familyPanel = new FamilyPanel();

    @Override
    public boolean createPanel() {
        setContext(getContext(), true);
        return true;
    }

    @Override
    public String getDefaultFactoryMode() {
        return AncestrisDockModes.OUTPUT; // NOI18N
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
        return ImageUtilities.loadImage("ancestris/modules/nav/NavIcon.png", true);
    }

    @Override
    public void setContext(Context context, boolean isActionPerformed) {
        if (context == null) {
            return;
        }
        familyPanel.setContext(context);
        setPanel(familyPanel);
        repaint();
    }
}
