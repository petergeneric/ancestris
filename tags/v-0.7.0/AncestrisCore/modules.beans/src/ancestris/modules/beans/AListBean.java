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
package ancestris.modules.beans;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 *
 * @author daniel
 */
public class AListBean extends JPanel {

    /** the blueprints we're using */
    private Map<String, Blueprint> type2blueprint = new HashMap<String, Blueprint>();
    Dimension defaultPreferedSize = new Dimension(150, 32);
    Dimension defaultMinimumSize = new Dimension(10, 32);

    public AListBean() {
        super();
        setBlueprint(Gedcom.INDI, "<prop path=INDI:NAME>");
        setBlueprint(Gedcom.FAM, "<prop path=FAM:HUSB> - <prop path=FAM:WIFE>");
        setBlueprint(Gedcom.NOTE, "<name tag=NOTE>&nbsp;id=<prop path=NOTE> - <prop path=NOTE:NOTE>");
        setBlueprint(Gedcom.SOUR, "<name tag=SOUR>&nbsp;id=<prop path=SOUR> - <prop path=SOUR:TITL>");
        //XXX: add all other entities
    }

    /**
     * Get blueprint used for given type
     */
    private Blueprint getBlueprint(String tag) {
        Blueprint result = type2blueprint.get(tag);
        // try fallback
        if (result == null) {
            result = type2blueprint.get("");
        }
        // fallback to global
        if (result == null) {
            result = BlueprintManager.getInstance().getBlueprint(tag, "");
            type2blueprint.put(tag, result);
        }
        return result;
    }

    /**
     * Set blueprint for tag
     * FIXME: use blueprintmanager or same mecanisme for that!
     * @param tag
     * @param bp
     */
    public void setBlueprint(String tag, Blueprint bp) {
        if (bp == null) {
            return;
        }
        type2blueprint.put(tag, bp);
    }

    public void setBlueprint(String tag, String bp) {
        if (bp == null) {
            return;
        }
        setBlueprint(tag, new Blueprint(bp));
    }

    public void add(Property property, MouseListener listener) {
        ABluePrintBeans bean = new ABluePrintBeans() {

            @Override
            public Dimension getMinimumSize() {
                return defaultMinimumSize;
            }

            @Override
            public Dimension getPreferredSize() {
                return defaultPreferedSize;
            }
        };
        String tag = property.getTag();
        bean.setBlueprint(tag, getBlueprint(tag));
        bean.setContext(property);
        if (listener != null) {
            bean.addMouseListener(listener);
        }
        add(bean);
    }

    public void add(Property[] properties, Property exclude, MouseListener listener) {
        if (properties == null) {
            return;
        }
        for (Property property : properties) {
            if (property.equals(exclude)) {
                continue;
            }
            //XXX: must be changed as display and editing xrefs must be handled differently
            // from embedded entities
            if (property instanceof PropertyXRef) {
                property = ((PropertyXRef) property).getTargetEntity();
            }
            add(property, listener);
        }
    }
}
