/**
 * Ancestris - http://www.ancestris.org (Formerly GenJ - GenealogyJ)
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2010 - 2013 Ancestris
 * Author: Daniel Andre <daniel@ancestris.org>
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

import ancestris.core.actions.SubMenuAction;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.util.swing.NestedBlockLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Add a new entity
 */
public class CreateEntity extends AbstractChange {

    private static final String ADD_ACTION_SUBMENU = "Ancestris/Actions/GedcomProperty/CreateOther";

    /** the type of the added entity */
    private String tag;
    /** text field for entering id */
    private JTextField requestID;

    /**
     * Constructor
     */
    public CreateEntity(String tag) {
        super();
        setImageText(Gedcom.getEntityImage(tag).getOverLayed(imgNew), resources.getString("create."+tag.toLowerCase(), Gedcom.getName(tag, false)));
        this.tag = tag;
    }

    /**
     * Override content components to show to user
     */
    @Override
    protected JPanel getDialogContent() {

        JPanel result = new JPanel(new NestedBlockLayout("<col><row><text wx=\"1\" wy=\"1\"/></row><row><check/><text/></row></col>"));

        // prepare id checkbox and textfield
        requestID = new JTextField(getGedcom().getNextAvailableID(tag), 8);
        requestID.setEditable(false);

        final JCheckBox check = new JCheckBox(resources.getString("assign_id"));
        check.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                requestID.setEditable(check.isSelected());
                if (check.isSelected()) {
                    requestID.requestFocusInWindow();
                }
            }
        });

        result.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        result.add(getConfirmComponent());
        result.add(check);
        result.add(requestID);

        // Setpreferred size
        result.setPreferredSize(new Dimension(520, 220));
        
        // done
        return result;
    }

    /**
     * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
     */
    @Override
    protected String getConfirmMessage() {
        // You are about to create a {0} in {1}!
        String about = resources.getString("confirm.new", new Object[]{Gedcom.getName(tag, false), getGedcom()});
        // This entity will not be connected ... 
        String detail = resources.getString("confirm.new.unrelated");
        // done
        return about + "<br>" + "<br>" + detail;
    }

    /**
     * @see genj.edit.EditViewFactory.Change#change()
     */
    @Override
    protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {
        // check id
        String id = null;
        if (requestID.isEditable()) {
            id = requestID.getText();
            if (gedcom.getEntity(tag, id) != null) {
                throw new GedcomException(resources.getString("assign_id_error", id));
            }
        }
        // create the entity
        Entity entity = gedcom.createEntity(tag, id);
        entity.addDefaultProperties();
        // done
        return new Context(entity);
    }

    
    
    
    
// register actions
    @ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateIndiAction")
    @ActionRegistration(displayName = "#create.indi",
        lazy = false)
    @ActionReferences(value = {
        @ActionReference(position=999,path = ADD_ACTION_SUBMENU)})    // ,separatorBefore=900,
    public static CreateEntity createIndiFactory() {
        return new CreateEntity(Gedcom.INDI);
    }

    @ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateFam")
    @ActionRegistration(displayName = "#create.fam",
        lazy = false)
    @ActionReferences(value = {
        @ActionReference(position=1099,path = ADD_ACTION_SUBMENU)})
    public static CreateEntity createFamFactory() {
        return new CreateEntity(Gedcom.FAM);
    }

    @ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateNoteAction")
    @ActionRegistration(displayName = "#create.note",
        lazy = false)
    @ActionReferences(value = {
        @ActionReference(position=1200,path = ADD_ACTION_SUBMENU)})
    public static CreateEntity createNoteFactory() {
        return new CreateEntity(Gedcom.NOTE);
    }

    @ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateObjeAction")
    @ActionRegistration(displayName = "#create.obje",
        lazy = false)
    @ActionReferences(value = {
        @ActionReference(position=1300,path = ADD_ACTION_SUBMENU)})
    public static CreateEntity createObjeFactory() {
        return new CreateEntity(Gedcom.OBJE);
    }

    @ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateSourAction")
    @ActionRegistration(displayName = "#create.sour",
        lazy = false)
    @ActionReferences(value = {
        @ActionReference(position=1400,path = ADD_ACTION_SUBMENU)})
    public static CreateEntity createSourFactory() {
        return new CreateEntity(Gedcom.SOUR);
    }

    @ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateRepoAction")
    @ActionRegistration(displayName = "#create.repo",
        lazy = false)
    @ActionReferences(value = {
        @ActionReference(position=1500,path = ADD_ACTION_SUBMENU)})
    public static CreateEntity createRepoFactory() {
        return new CreateEntity(Gedcom.REPO);
    }

    @ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateSubmAction")
    @ActionRegistration(displayName = "#create.subm",
        lazy = false)
    @ActionReferences(value = {
        @ActionReference(position=1600,path = ADD_ACTION_SUBMENU)})
    public static CreateEntity createSubmFactory() {
        return new CreateEntity(Gedcom.SUBM);
    }

    
    
    
    @ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.CreateEntityMenu")
    @ActionRegistration(displayName = "#create.entity",
        lazy = false)
    public static SubMenuAction getCreateEntityMenu() {
        SubMenuAction menuAction = new SubMenuAction(NbBundle.getMessage(CreateEntity.class, "create.entity"));
        menuAction.putValue(Action.SMALL_ICON, Gedcom.getImage());
        menuAction.addAction(createIndiFactory());
        menuAction.addAction(createFamFactory());
        menuAction.addAction(createNoteFactory());
        menuAction.addAction(createObjeFactory());
        menuAction.addAction(createSourFactory());
        menuAction.addAction(createRepoFactory());
        menuAction.addAction(createSubmFactory());
        return menuAction;
    }
} //Create

