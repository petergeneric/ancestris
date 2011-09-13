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

package ancestris.modules.editors.standard;

import ancestris.api.editor.Editor;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.view.SelectionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public class EditorTopComponent extends AncestrisTopComponent implements SelectionListener {
    private final Map<Class, Editor> panels = new HashMap<Class,Editor>();


    private static final String PREFERRED_ID = "EntityEditTopComponent";  // NOI18N
    private static EditorTopComponent factory;

    @Override
    public boolean createPanel() {
        panels.put(Fam.class,new FamPanel());
        panels.put(Indi.class,new IndiPanel());
        setContext(getContext(), true);
        return true;
    }
    @Override
    public String getDefaultFactoryMode() {
        return AncestrisDockModes.EDITOR; // NOI18N
    }

    public static synchronized EditorTopComponent getFactory() {
        if (factory == null) {
            factory = new EditorTopComponent();
        }
        return factory;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }


    public void setContext(Context context, boolean isActionPerformed) {
        if (context == null) {
            return;
        }
        if (context.getEntity() == null){
            return;
        }
        Editor panel = panels.get(context.getEntity().getClass());
        JPanel thePanel = new JPanel();
        if (panel != null){
            panel.setContext(context);

            JLabel titleLabel = new JLabel(panel.getTitle());
            titleLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N

            thePanel.add(titleLabel);
            thePanel.add(panel);
        }
        setPanel(thePanel);
        repaint();
    }

}
