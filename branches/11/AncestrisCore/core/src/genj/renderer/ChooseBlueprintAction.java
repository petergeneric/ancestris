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
package genj.renderer;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbBundle;

/**
 * Action for picking/editing a blueprint
 */
public abstract class ChooseBlueprintAction extends AbstractAncestrisAction {

    private final static Resources RESOURCES = Resources.get(ChooseBlueprintAction.class);
    private final static ImageIcon IMAGE = new ImageIcon(ChooseBlueprintAction.class, "Blueprint.png");
    private final static BlueprintManager MGR = BlueprintManager.getInstance();

    private Entity recipient;
    private Blueprint current;
    private JList blueprints;
    private BlueprintEditor editor;

    protected ChooseBlueprintAction(Entity recipient, Blueprint current) {

        if (recipient == null) {
            throw new IllegalArgumentException("recipient==null");
        }

        this.recipient = recipient;
        this.current = current;
        //setText(RESOURCES.getString("blueprint.select"));
        setText(RESOURCES.getString("blueprint.select.for", Gedcom.getName(recipient.getTag(), true)));
        setImage(IMAGE.getOverLayed(recipient.getImage(false)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        editor = new BlueprintEditor(recipient);

        blueprints = new JList(BlueprintManager.getInstance().getBlueprints(recipient.getTag()).toArray());
        blueprints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final AbstractAncestrisAction add = new Add();
        final AbstractAncestrisAction del = new Del();
        blueprints.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (editor.isChanged()) {
                    String title = NbBundle.getMessage(getClass(), "TITL_BluePrintEditor");
                    String msg = NbBundle.getMessage(getClass(), "MSG_ConfirmChange");
                    if (DialogManager.YES_OPTION == DialogManager.createYesNo(title, msg).setMessageType(DialogManager.WARNING_MESSAGE).show()) {
                        editor.commit();
                    }
                }

                Blueprint selection = (Blueprint) blueprints.getSelectedValue();

                // no selection?
                if (selection == null) {
                    if (blueprints.getModel().getSize() > 0) {
                        blueprints.setSelectedIndex(e.getFirstIndex());
                    } else {
                        del.setEnabled(false);
                        editor.set(null);
                    }
                    return;
                }
                editor.set(selection);
                del.setEnabled(!selection.isReadOnly());

            }
        });
        blueprints.setSelectedValue(current, true);

        JPanel content = new JPanel(new NestedBlockLayout(
                "<col><for gx=\"1\"/><row><col><row><list wy=\"1\" gx=\"1\" gy=\"1\"/></row><row><add/><del/></row></col><col><editor wy=\"1\" wx=\"1\"/></col></row></col>"
        ));

        ButtonHelper bh = new ButtonHelper();
        content.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        content.add(new JLabel(RESOURCES.getString("blueprint.select.for", Gedcom.getName(recipient.getTag(), true))));
        content.add(new JScrollPane(blueprints));
        content.add(bh.create(add));
        content.add(bh.create(del));
        content.add(editor);

        if (DialogManager.OK_OPTION == DialogManager.create(RESOURCES.getString("blueprint"), content).
                setDialogId("genj.renderer.blueprint").
                setOptionType(DialogManager.OK_CANCEL_OPTION).
                show()) {
            editor.commit();

            current = (Blueprint) blueprints.getSelectedValue();
            if (current != null) {
                commit(recipient, current);
            }
        }
    }

    private class Add extends AbstractAncestrisAction {

        Add() {
            super(RESOURCES.getString("blueprint.add"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            // check selection
            Blueprint selection = (Blueprint) blueprints.getSelectedValue();
            // get name
            String name = DialogManager.create(null, RESOURCES.getString("blueprint.add.confirm"), "").show();
            name = MGR.name2key(name);
            if (name == null || name.length() == 0) {
                MGR.showError("", "blueprint.error.add", null);
                return;
            }

            // get html
            String html = selection != null ? selection.getHTML() : "";
            // add it
            try {
                String key = name;
                Blueprint blueprint = MGR.addBlueprint(new Blueprint(recipient.getTag(), key, name, html, false));
                blueprints.setListData(MGR.getBlueprints(recipient.getTag()).toArray());
                blueprints.setSelectedValue(blueprint, true);
            } catch (Exception ex) {
                Logger.getLogger("ancestris.renderer").log(Level.WARNING, "can't add blueprint " + name, ex);
                MGR.showError("", "blueprint.error.add", ex);
            }
            // done
        }
    }

    private class Del extends AbstractAncestrisAction {

        Del() {
            super(RESOURCES.getString("blueprint.del"));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Blueprint selection = (Blueprint) blueprints.getSelectedValue();
            if (selection == null || selection.isReadOnly()) {
                return;
            }
            if (DialogManager.create((String) null, RESOURCES.getString("blueprint.del.confirm", selection.getDisplayName()))
                    .setMessageType(DialogManager.WARNING_MESSAGE)
                    .setOptionType(DialogManager.OK_CANCEL_OPTION)
                    .show() != DialogManager.OK_OPTION) {
                return;
            }

            try {
                MGR.delBlueprint(selection);
            } catch (Exception ex) {
                Logger.getLogger("ancestris.renderer").log(Level.WARNING, "can't delete blueprint " + selection, ex);
                MGR.showError("", "blueprint.error.del", ex);
            }
            blueprints.setListData(MGR.getBlueprints(recipient.getTag()).toArray());
            if (blueprints.getModel().getSize() > 0) {
                blueprints.setSelectedIndex(0);
            }
        }
    };

    protected abstract void commit(Entity recipient, Blueprint blueprint);

}
