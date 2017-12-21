/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.edit.beans;

import ancestris.core.actions.AncestrisActionProvider;
import ancestris.view.ExplorerHelper;
import ancestris.view.SelectionDispatcher;
import static genj.edit.beans.PropertyBean.REGISTRY;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.renderer.Blueprint;
import genj.renderer.BlueprintManager;
import genj.renderer.ChooseBlueprintAction;
import genj.view.ViewContext;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.Node;

/**
 * A proxy for a property that links entities
 */
public class XRefBean extends PropertyBean implements AncestrisActionProvider {

    private Preview preview;
    private PropertyXRef xref;

    public XRefBean() {

        preview = new Preview();

        new ExplorerHelper(preview).setPopupAllowed(true);

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, preview);

        preview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1 || e.getID() != MouseEvent.MOUSE_CLICKED) {
                    return;
                }
                // no double-click?
                if (e.getClickCount() < 2) {
                    return;
                }
                // property good? (should)
                if (xref == null) {
                    return;
                }
                // tell about it
                SelectionDispatcher.fireSelection(e, new ViewContext(xref));
            }
        });
    }

    public List<Action> getActions(boolean hasFocus, Node[] nodes) {
        if (!hasFocus || xref == null) {
            return new ArrayList<Action>();
        }
        List<Action> actions = new ArrayList<Action>();
        final Entity entity = xref.getTargetEntity();
        String tag = entity.getTag();
        Blueprint bp = BlueprintManager.getInstance().getBlueprint(tag, REGISTRY.get("blueprint.entity" + tag, ""));
        actions.add(new ChooseBlueprintAction(entity, bp) {
            @Override
            protected void commit(Entity recipient, Blueprint blueprint) {
                REGISTRY.put("blueprint.entity" + blueprint.getTag(), blueprint.getName());
                preview.setEntity(entity);
            }
        });
        return actions;
    }

    @Override
    protected void commitImpl(Property property) {
        //noop
    }

    /**
     * Nothing to edit
     */
    public boolean isEditable() {
        return false;
    }

    /**
     * Set context to edit
     */
    public void setPropertyImpl(Property prop) {

        PropertyXRef xref = (PropertyXRef) prop;
        this.xref = xref;

        // set preview
        if (xref != null && xref.getTargetEntity() != null) {
            preview.setEntity(xref.getTargetEntity());
        } else {
            preview.setEntity(null);
        }
    }

    /**
     * Preferred
     */
    public Dimension getPreferredSize() {
        return new Dimension(64, 48);
    }

} //ProxyXRef
