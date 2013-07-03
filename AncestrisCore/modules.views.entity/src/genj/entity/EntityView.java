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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.entity;

import ancestris.core.actions.AncestrisActionProvider;
import ancestris.gedcom.PropertyNode;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Property;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import genj.renderer.BlueprintRenderer;
import genj.renderer.ChooseBlueprintAction;
import genj.renderer.RenderOptions;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.View;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.openide.nodes.Node;
import spin.Spin;

/**
 * A rendering component showing the currently selected entity
 * via html
 */
public class EntityView extends View implements AncestrisActionProvider {

    /** language resources we use */
    /* package */ final static Resources resources = Resources.get(EntityView.class);
    /** a dummy blueprint */
    private final static Blueprint BLUEPRINT_SELECT = new Blueprint(resources.getString("html.select"));
    /** a registry we keep */
    private final static Registry REGISTRY = Registry.get(EntityView.class);
    /** the renderer we're using */
    private BlueprintRenderer renderer = null;
    /** our current context */
    /* package */ Context context = new Context();
    /** the blueprints we're using */
    private Map<String, Blueprint> type2blueprint = new HashMap<String, Blueprint>();
    /** whether we do antialiasing */
    private boolean isAntialiasing = true;
    private transient GedcomListener callback = new GedcomListenerAdapter() {

        @Override
        public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
            if (context.getEntity() == entity) {
                setContext(new Context(context.getGedcom()));
            }
        }

        @Override
        public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
            if (context.getEntity() == property.getEntity()) {
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
    };

    /**
     * Constructor
     */
    public EntityView() {

        // grab data from registry
        BlueprintManager bpm = BlueprintManager.getInstance();
        for (int t = 0; t < Gedcom.ENTITIES.length; t++) {
            String tag = Gedcom.ENTITIES[t];
            type2blueprint.put(tag, bpm.getBlueprint(tag, REGISTRY.get("blueprint." + tag, "")));
        }
        isAntialiasing = REGISTRY.get("antial", false);

        // done    
    }

    @Override
    public List<Action> getActions(boolean hasFocus, Node[] nodes) {
        if (!hasFocus) {
            return new ArrayList<Action>();
        }
        List<Action> actions = new ArrayList<Action>();
        if (nodes.length == 1) {
            if (nodes[0] instanceof PropertyNode) {
                PropertyNode node = (PropertyNode) nodes[0];
                Entity entity = node.getProperty().getEntity();
                actions.add(new ChooseBlueprintAction(entity, getBlueprint(entity.getTag())) {

                    @Override
                    protected void commit(Entity recipient, Blueprint blueprint) {
                        type2blueprint.put(blueprint.getTag(), blueprint);
                        setContext(context);
                        REGISTRY.put("blueprint." + blueprint.getTag(), blueprint.getName());
                    }
                });
            }
        }
        return actions;
    }

    /**
     * our context setter
     */
    @Override
    public void setContext(Context newContext) {

        // disconnect from old
        if (context.getGedcom() != null) {
            context.getGedcom().removeGedcomListener((GedcomListener) Spin.over(callback));
        }
        renderer = null;

        // keep new
        context = newContext;

        // hook-up
        if (context.getGedcom() != null) {
            context.getGedcom().addGedcomListener((GedcomListener) Spin.over(callback));

            // resolve blueprint & renderer
            Entity e = context.getEntity();
            Blueprint blueprint;
            if (e == null) {
                blueprint = BLUEPRINT_SELECT;
            } else {
                blueprint = getBlueprint(e.getTag());
            }
            renderer = new BlueprintRenderer(blueprint);

        }

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

        if (context == null || renderer == null) {
            return;
        }

        ((Graphics2D) g).setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                isAntialiasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

        renderer.render(g, context.getEntity(), new Rectangle(0, 0, bounds.width, bounds.height));
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
} //EntityView
