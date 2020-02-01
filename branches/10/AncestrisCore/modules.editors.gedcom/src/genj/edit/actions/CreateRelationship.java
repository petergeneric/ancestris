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
package genj.edit.actions;

import ancestris.util.swing.SelectRelationshipPanel;
import genj.common.SelectEntityWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.util.Registry;
import genj.util.WordBuffer;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Base type for all relationships we create - it always uses the same pattern
 * <il>
 * <li>ask the user for a new or existing entity based on the target type of the relationship and
 * <li>explain what is going to happen
 * <li>perform the necessary actions in a concrete implementation
 * </il>
 */
public abstract class CreateRelationship extends AbstractChange {

    protected Registry REGISTRY = Registry.get(CreateRelationship.class);
    private SelectRelationshipPanel select = null;
    /** the referenced entity */
    private Entity existing;
    private Entity created;
    /** text field for entering id */
    private JTextField requestID;
    /** the target type of the relationship (where it points to) */
    protected String targetType;
    private boolean isNew;

    /**
     * Constructor
     */
    public CreateRelationship() {
        super();
    }

    public CreateRelationship(String name, String targetType) {
        this();
        setTargetType(targetType);
        setText(name);
    }

    public final void setTargetType(String targetType) {
        if (targetType != null) {
            setImage(Gedcom.getEntityImage(targetType));
        }
        this.targetType = targetType;
    }

    /**
     * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
     */
    @Override
    protected String getConfirmMessage() {

        WordBuffer result = new WordBuffer("<br>");

        // You are about to create a {0} in {1}! / You are about to reference {0} in {1}!
        // This {0} will be {1}.
        result.append(existing == null
                ? resources.getString("confirm.new", new Object[]{Gedcom.getName(targetType, false), getGedcom()})
                : resources.getString("confirm.use", new Object[]{existing.getId(), getGedcom()}));

        // relationship detail
        result.append(" ");  // line break
        result.append(resources.getString("confirm.new.related", getDescription()));

        // A warning already?
        String warning = getWarning(existing);
        if (warning != null) {
            result.append("<br><br>/!\\ " + warning);
        }

        // combine
        return result.toString();
    }

    /**
     * Provide a description
     */
    public abstract String getDescription();

    /**
     * Provide a warning for given existing target (default none)
     */
    public String getWarning(Entity target) {
        return null;
    }

    /**
     * Override content components to show to user
     * @return selection panel
     */
    @Override
    protected JPanel getDialogContent() {

        // In some instances, existing should be ignored (set to null), in other instances should be used
        // Used: non indi
        // Ignored : Indi or Fam
        if (targetType.equals(Gedcom.INDI) || targetType.equals(Gedcom.FAM)) {
            existing = null;
        } else {
            existing = getGedcom().getEntity(REGISTRY.get("select." + getGedcom().getName() + "." + targetType, (String) null));
        }
        select = new SelectRelationshipPanel(getGedcom(), targetType, getConfirmMessage(), existing, SelectEntityWidget.NEW) {
            @Override
            public String getLabel() {
                existing = select.getSelection();
                return getConfirmMessage();
            }
        };
        requestID = select.getTextIDComponent();
        return select;
    }

    /**
     * perform the change
     */
    @Override
    protected final Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {
        // create the entity if necessary
        Entity change;
        existing = select.getSelection();
        if (existing != null) {
            change = existing;
        } else {
            // check id
            String id = null;
            if (requestID.isEditable()) {
                id = requestID.getText();
                if (gedcom.getEntity(targetType, id) != null) {
                    throw new GedcomException(resources.getString("assign_id_error", id));
                }
            }
            // focus always changes to new that we create now
            change = gedcom.createEntity(targetType, id);
            change.addDefaultProperties();
        }

        // perform the change
        isNew = change != existing;
        created = change;
        Property focus = change(change, change != existing);

        // remember target of relationship as next time target
        REGISTRY.put("select." + gedcom.getName() + "." + targetType, change.getId());

        // done
        return new Context(focus.getEntity());
    }

    public boolean isNew() {
        return isNew;
    }

    public Entity getCreated() {
        return created;
    }

    /**
     * Apply the relationship
     *
     * @param target      the entity that the resulting relationship has to point to
     * @param targetIsNew whether the target was newly created for this relationship
     *
     * @return the property that should receive focus after this action
     */
    protected abstract Property change(Entity target, boolean targetIsNew) throws GedcomException;
}
