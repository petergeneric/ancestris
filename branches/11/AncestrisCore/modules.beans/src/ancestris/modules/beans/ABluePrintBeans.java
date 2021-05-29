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
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import genj.renderer.BlueprintRenderer;
import genj.renderer.RenderOptions;
import genj.util.Registry;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.view.PropertyProvider;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 * A rendering component showing the currently selected entity
 * via html
 */
/*
 * from entityview
 */
public class ABluePrintBeans extends JPanel implements GedcomListener, PropertyProvider {
    //View implements ContextProvider {

    /** a dummy blueprint */
    private Blueprint emptyBluePrint = new Blueprint("");
    /** a registry we keep */
    private final static Registry REGISTRY = Registry.get(ABluePrintBeans.class);
    /** the renderer we're using */
    private BlueprintRenderer renderer = null;
    private Property property;
    /** the blueprints we're using */
    private Map<String, Blueprint> type2blueprint = new HashMap<String, Blueprint>();
    /** whether we do antialiasing */
    private boolean isAntialiasing = true;

    /**
     * Constructor
     */
    public ABluePrintBeans() {

        // grab data from registry
        BlueprintManager bpm = BlueprintManager.getInstance();
        for (int t = 0; t < Gedcom.ENTITIES.length; t++) {
            String tag = Gedcom.ENTITIES[t];
            type2blueprint.put(tag, bpm.getBlueprint(tag, REGISTRY.get("blueprint." + tag, "")));
        }
        isAntialiasing = REGISTRY.get("antial", false);

        // done
    }

    public void setEmptyBluePrint(String bp) {
        emptyBluePrint = new Blueprint(bp);
    }

    /**
     * Context getter (Entity)
     */
    public Property getProperty() {
        return property;
    }

    /**
     * our context setter
     */
    public void setContext(Property newProperty) {

        renderer = null;

        // keep new
        property = newProperty;

        // resolve blueprint & renderer
        Blueprint blueprint;
        if (property == null) {
            blueprint = emptyBluePrint;
        } else {
            blueprint = getBlueprint(property.getTag());
        }
        renderer = new BlueprintRenderer(blueprint);

        repaint();
    }

    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(256, 160);
    }

//    // store settings in registry
//    for (int t=0;t<Gedcom.ENTITIES.length;t++) {
//      String tag = Gedcom.ENTITIES[t];
//      registry.put("blueprint."+tag, getBlueprint(tag).getName()); 
//    }
//    registry.put("antial", isAntialiasing );
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) {

        Rectangle bounds = getBounds();
        g.setColor(Color.white);
        g.fillRect(0, 0, bounds.width, bounds.height);
        g.setColor(Color.black);
        g.setFont(RenderOptions.getInstance().getDefaultFont());

//    if (entity==null||renderer==null)
        if (renderer == null) {
            return;
        }

        ((Graphics2D) g).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                isAntialiasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

        renderer.render(g, property, new Rectangle(0, 0, bounds.width, bounds.height));
    }

    /**
     * Get blueprint used for given type
     */
    private Blueprint getBlueprint(String tag) {
        Blueprint result = type2blueprint.get(tag);
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
    public void setBlueprint(String tag, Blueprint bp){
        if (bp == null)
            return;
        type2blueprint.put(tag, bp);
    }

    public void setBlueprint(String tag, String bp){
        if (bp == null)
            return;
        type2blueprint.put(tag, new Blueprint(bp));
    }

    /**
     * Sets isAntialiasing   */
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

    @Override
    public void addNotify() {
        super.addNotify();
        AncestrisPlugin.register(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        AncestrisPlugin.unregister(this);
    }

    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        if (this.property == entity) {
            setContext(null);
        }
    }

    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        if (this.property == property.getEntity()) {
            repaint();
        }
    }

    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        gedcomPropertyChanged(gedcom, property);
    }

    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
        gedcomPropertyChanged(gedcom, property);
    }

    @Override
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
    }

    @Override
    public Property provideVisibleProperty(Point point) {
        return property;
    }
} //EntityView

