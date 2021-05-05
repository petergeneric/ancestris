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

import ancestris.api.editor.AncestrisEditor;
import ancestris.core.CoreOptions;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.util.swing.DialogManager;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.util.GridBagHelper;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.Updateable;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A Proxy knows how to generate interaction components that the user will use
 * to change a property : UNKNOWN
 */
public class PlaceBean extends PropertyBean {

    private final GridBagHelper gh = new GridBagHelper(this);
    private int rows = 0;
    private JCheckBox global = new JCheckBox();
    private Property[] sameChoices = new Property[0];

    public PlaceBean() {

        // nothing much we can do - hook up to change events and show changeAll on change
        changeSupport.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                setupGlobal(true);
            }
        });
        // listen to selection of global and ask for confirmation
        global.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (global.isSelected()) {
                    boolean yes = (DialogManager.YES_OPTION
                            == DialogManager.createYesNo(RESOURCES.getString("choice.global.enable"), getGlobalConfirmMessage(false))
                            .show());
                    global.setSelected(yes);
                }
            }
        });

    }

    /**
     * Compute commit value
     */
    private String getCommitValue() {

        boolean hierarchy = CoreOptions.getInstance().isSplitJurisdictions() && ((PropertyPlace) getProperty()).getFormatAsString().length() > 0;

        // collect the result by looking at all of the choices
        StringBuilder result = new StringBuilder();
        for (int c = 0, n = getComponentCount(), j = 0; c < n; c++) {

            // check each text field
            Component comp = getComponent(c);
            if (comp instanceof ChoiceWidget) {

                String jurisdiction = ((ChoiceWidget) comp).getText().trim();

                // make sure the user doesn't enter a comma ',' if there is a field per jurisdiction
                if (hierarchy) {
                    jurisdiction = jurisdiction.replaceAll(PropertyPlace.JURISDICTION_SEPARATOR, ";");
                }

                // always add separator for jurisdictions j>0 regardless of jurisdiction.length()
                if (j++ > 0) {
                    result.append(PropertyPlace.JURISDICTION_SEPARATOR);
                }
                result.append(jurisdiction);

            }
            // next
        }

        return PropertyPlace.formatSpaces(result.toString());
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl(Property property) {

        // propagate change
        PropertyPlace place = (PropertyPlace) property;
        place.setValue(getCommitValue(), global.isSelected());
        place.setCoordinates(global.isSelected());

        // reset
        setPropertyImpl(property);

    }

    /**
     * Set context to edit
     */
    public void setPropertyImpl(final Property prop) {

        // remove all current fields and clear current default focus - this is all dynamic for each context
        removeAll();
        rows = 0;
        defaultFocus = null;

        final Gedcom ged = getRoot().getGedcom();
        PropertyPlace place = (PropertyPlace) prop;
        String value;
        String formatAsString;
        String[] jurisdictions;
        Boolean showJuridictions[] = ged.getShowJuridictions();

        if (place == null) {
            sameChoices = new Property[0];
            value = "";
            jurisdictions = new String[0];
            formatAsString = ged.getPlaceFormat();
        } else {
            sameChoices = place.getSameChoices();
            /*
             thought about using getDisplayValue() here but the problem is that getAllJurisdictions()
             works on values (PropertyChoiceValue stuff) - se we have to use getValue() here
             */
            value = place.isSecret() ? "" : place.getValue();
            formatAsString = place.getFormatAsString();
            jurisdictions = place.getJurisdictions();
        }

        Updateable updater = new Updateable() {

            @Override
            public Object[] getValues() {
                return PropertyPlace.getAllJurisdictions(ged, -1, true);
            }
        };
        // either a simple value or broken down into comma separated jurisdictions
        if (!CoreOptions.getInstance().isSplitJurisdictions() || formatAsString.length() == 0) {
            createChoice(null, value, updater, formatAsString, true);
        } else {
            String[] format = PropertyPlace.getFormat(ged);
            for (int i = 0; i < Math.max(format.length, jurisdictions.length); i++) {
                boolean showIt = (showJuridictions == null || i >= showJuridictions.length || showJuridictions[i]);
                createChoice(i < format.length ? format[i] : "?", i < jurisdictions.length ? jurisdictions[i] : "", PropertyPlace.getAllJurisdictions(ged, i, true), null, showIt);
            }
        }

        // add 'change all'
        setupGlobal(false);
        gh.setAnchor(GridBagConstraints.LINE_START).
                add(global, 1, ++rows, 1, 1);
        gh.setAnchor(-1);

        // FIXME: We don't show edit button ATM
        final AncestrisEditor editor = AncestrisEditor.findEditor(prop);
        if (false && editor != null) {
            gh.add(new JButton((Action)
                    new AbstractAncestrisAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            SelectionDispatcher.muteSelection(true);
                            if (editor != null) {
//                        PropertyPlace p = new PropertyPlace()
                                editor.edit(prop);
                                PlaceBean.this.changeSupport.setChanged(true);
                                setPropertyImpl(prop);
                            }
                            SelectionDispatcher.muteSelection(false);
                        }

                    }.setText("modifier")),
                    3,
                    rows);
        }

        // add filler
        gh.addFiller(1, ++rows);

        // Done
    }

    private void createChoice(String label, String value, Object values, String tip, boolean showIt) {
        // next row
        rows++;
        // add a label for the jurisdiction name?
        if (label != null && showIt) {
            gh.add(new JLabel(label, SwingConstants.RIGHT), 0, rows, 1, 1, GridBagHelper.FILL_HORIZONTAL);
        }
        // and a textfield
        ChoiceWidget choice = new ChoiceWidget();
        choice.setComponentPopupMenu(new CCPMenu(choice.getTextEditor()));
        choice.setIgnoreCase(true);
        choice.setEditable(true);
        if (values instanceof Updateable) {
            choice.setUpdater((Updateable) values);
        } else {
            choice.setValues((Object[]) values);
        }

//    if (value.length()>0) {
        choice.setText(value);
//    } else {
//      choice.setText("["+Gedcom.getName(PropertyPlace.TAG)+"]");
//      choice.setTemplate(true);
//    }
        choice.addChangeListener(changeSupport);
        if (tip != null && tip.length() > 0) {
            choice.setToolTipText(tip);
        }
        gh.add(choice, 1, rows, 1, 1, GridBagHelper.GROWFILL_HORIZONTAL);
        // set default focus if not done yet
        if (defaultFocus == null) {
            defaultFocus = choice;
        }
        choice.setVisible(showIt);
        // done
    }

    /**
     * Create confirm message for global
     */
    private String getGlobalConfirmMessage(boolean isShort) {
        if (sameChoices.length < 2) {
            return null;
        }
        // we're using getDisplayValue() here
        // because like in PropertyRelationship's case there might be more
        // in the gedcom value than what we want to display (witness@INDI:BIRT)
        String messageId = isShort ? "choice.global.confirm.short" : "choice.global.confirm";
        return RESOURCES.getString(messageId, "" + sameChoices.length, sameChoices[0].getDisplayValue(), getCommitValue());
    }

    private void setupGlobal(boolean enabled) {
        String confirm = getGlobalConfirmMessage(true);
        global.setEnabled(confirm != null && enabled);
        global.setText(confirm == null ? RESOURCES.getString("choice.global.hidden") : confirm);
    }

} //PlaceBean

