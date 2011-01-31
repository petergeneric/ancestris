/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package ancestris.modules.beans;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import genj.renderer.BlueprintRenderer;
import genj.renderer.Options;
import genj.util.Registry;
import genj.util.Resources;
import genjfr.app.pluginservice.GenjFrPlugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
public class ABluePrintBeans extends JPanel implements GedcomListener {
    //View implements ContextProvider {

    /** language resources we use */
    /*package*/ final static Resources resources = Resources.get(ABluePrintBeans.class);
    /** a dummy blueprint */
    private Blueprint emptyBluePrint = new Blueprint("");
    /** a registry we keep */
    private final static Registry REGISTRY = Registry.get(ABluePrintBeans.class);
    /** the renderer we're using */
    private BlueprintRenderer renderer = null;
    private Entity entity;
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
    public Entity getContext() {
        return entity;
    }

    /**
     * our context setter
     */
    public void setContext(Entity newEntity) {

        renderer = null;

        // keep new
        entity = newEntity;

        // resolve blueprint & renderer
        Blueprint blueprint;
        if (entity == null) {
            blueprint = emptyBluePrint;
        } else {
            blueprint = getBlueprint(entity.getTag());
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
        g.setFont(Options.getInstance().getDefaultFont());

//    if (entity==null||renderer==null)
        if (renderer == null) {
            return;
        }

        ((Graphics2D) g).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                isAntialiasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

        renderer.render(g, entity, new Rectangle(0, 0, bounds.width, bounds.height));
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
        GenjFrPlugin.register(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        GenjFrPlugin.unregister(this);
    }

    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        if (this.entity == entity) {
            setContext(null);
        }
    }

    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        if (this.entity == property.getEntity()) {
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
} //EntityView

