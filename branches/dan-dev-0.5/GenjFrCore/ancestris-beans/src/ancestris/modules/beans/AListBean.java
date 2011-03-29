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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
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
    }

    /**
     * Get blueprint used for given type
     */
    private Blueprint getBlueprint(String tag) {
        Blueprint result = (Blueprint) type2blueprint.get(tag);
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
    public void add(Entity entity, MouseListener listener) {
        ABluePrintBeans bean = new ABluePrintBeans(){

            @Override
            public Dimension getMinimumSize() {
                return defaultMinimumSize;
            }

            @Override
            public Dimension getPreferredSize() {
                return defaultPreferedSize;
            }
        };
        String tag = entity.getTag();
        bean.setBlueprint(tag, getBlueprint(tag));
        bean.setContext(entity);
        if (listener != null)
            bean.addMouseListener(listener);
        add(bean);
    }

    public void add(Entity[] entities, Entity exclude,MouseListener listener) {
        if (entities == null) {
            return;
        }
        for (Entity entity : entities) {
            if (entity.equals(exclude)) {
                continue;
            }
            add(entity, listener);
        }
    }
}
