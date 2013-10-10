/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2013 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.view;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import genj.renderer.BlueprintRenderer;
import genj.renderer.RenderOptions;
import genj.util.Registry;
import genj.util.Resources;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JToolTip;

/**
 * A rendering component showing the currently selected entity
 * via html
 */
public class BpToolTip extends JToolTip {

    /** language resources we use */
    /* package */ final static Resources resources = Resources.get(BpToolTip.class);
    /** a dummy blueprint */
    private final static Blueprint BLUEPRINT_SELECT = new Blueprint(resources.getString("html.select"));
    /** a registry we keep */
    private final static Registry REGISTRY = Registry.get(BpToolTip.class);
    /** the renderer we're using */
    private BlueprintRenderer renderer = null;
    /** the blueprints we're using */
    private Map<String, Blueprint> type2blueprint = new HashMap<String, Blueprint>();
    /** whether we do antialiasing */
    private boolean isAntialiasing = true;
    private Entity entity;

    /**
     * Constructor
     */
    public BpToolTip() {
        super();

        this.setOpaque(false);
        // grab data from registry
        BlueprintManager bpm = BlueprintManager.getInstance();
        for (int t = 0; t < Gedcom.ENTITIES.length; t++) {
            String tag = Gedcom.ENTITIES[t];
            type2blueprint.put(tag, bpm.getBlueprint(tag, ""));//REGISTRY.get("blueprint." + tag, "")));
        }
        isAntialiasing = REGISTRY.get("antial", false);

        // done    
    }
    private static BpToolTip instance;

    public static BpToolTip getdefault() {
        if (instance == null) {
            instance = new BpToolTip();
        }
        return instance;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
        Blueprint blueprint;
        if (entity == null) {
            blueprint = BLUEPRINT_SELECT;
        } else {
            blueprint = getBlueprint(entity.getTag());
        }
        renderer = new BlueprintRenderer(blueprint);
    }

    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(256, 256);
    }

    @Override
    public void paintComponent(Graphics g) {
        if (entity == null || renderer == null) {
            return;
        }
// create a round rectangle
        Shape round = new RoundRectangle2D.Float(4, 4,
                this.getWidth() - 1 - 8,
                this.getHeight() - 1 - 8,
                15, 15);


// draw the white background
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(getBounds());
        g2.fill(round);

// draw the gray border
        g2.setColor(new Color(0, 0, 0));
        g2.setStroke(new BasicStroke(2));
        g2.draw(round);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                isAntialiasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

// draw the blueprint
        g2.setColor(Color.BLACK);
        g2.setFont(RenderOptions.getInstance().getDefaultFont());
        renderer.render(g2, entity, new Rectangle(4, 4, getWidth() - 9, getHeight() - 9));
    }

    /**
     * Get blueprint used for given type
     */
    private Blueprint getBlueprint(String tag) {
        Blueprint result = type2blueprint.get(tag);
        if (result != null && "tooltip".equals(result.getName()))
            return result;
        result = BlueprintManager.getInstance().getBlueprint(tag, "tooltip");
        if (result == null)
            result = BlueprintManager.getInstance().getBlueprint(tag, "complete");
        type2blueprint.put(tag, result);
        return result;
    }

    /**
     * Sets isAntialiasing
     */
    public void setAntialiasing(boolean set) {
        isAntialiasing = set;
        repaint();
    }

    /**
     * Gets isAntialiasing
     */
    public boolean isAntialiasing() {
        return isAntialiasing;
    }
}
