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

import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextAreaWidget;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import modules.editors.gedcomproperties.GedcomPropertiesPlaceFormatPanel;
import modules.editors.gedcomproperties.GedcomPropertiesWizardIterator;
import modules.editors.gedcomproperties.utils.GedcomPlacesAligner;
import modules.editors.gedcomproperties.utils.GedcomPlacesConverter;
import modules.editors.gedcomproperties.utils.PlaceFormatConverterPanel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import modules.editors.gedcomproperties.utils.PlaceFormatterInterface;

/**
 * Set the place hierarchy used in a gedcom file
 */
@ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.SetPlaceHierarchy")
@ActionRegistration(displayName = "#place.hierarchy",
        lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Ancestris/Actions/GedcomProperty", position = 545)})
public class SetPlaceHierarchyAction extends AbstractChange implements PlaceFormatterInterface {

    private static genj.util.swing.ImageIcon IMG = new ImageIcon(new javax.swing.ImageIcon(SetPlaceHierarchyAction.class.getResource("/genj/edit/images/PlaceFormat.png"))); // NOI18N

    /** the place to use as the global example */
    private PropertyPlace place;

    /** main place panel */
    GedcomPropertiesPlaceFormatPanel placePanel;
            
    /** textfield for warning msg */
    private JLabel wrngMsg;

    /** place format converter */
    PlaceFormatConverterPanel pfc;


    
    public SetPlaceHierarchyAction() {
        super();
        setImageText(IMG, resources.getString("place.hierarchy"));
        setTip(resources.getString("place.hierarchy.tip"));
    }

    /**
     * Constructor
     */
    public SetPlaceHierarchyAction(PropertyPlace place) {
        this();
        this.place = place;
        contextChanged();
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends Property> props = lkpInfo.allInstances();
        if (props.size() != 1) {
            place = null;
        } else {
            Property p = props.iterator().next();
            if (p instanceof PropertyPlace) {
                place = (PropertyPlace) p;
            } else {
                place = null;
            }
        }
        super.resultChanged(ev);
    }

    @Override
    protected final void contextChanged() {
        if (place != null) {
            setEnabled(true);
            setTip(resources.getString("place.hierarchy.tip"));
        } else {
            setEnabled(false);
        }
    }

    protected JComponent getComponent(String msg, int lines, Color color) {
        TextAreaWidget result = new TextAreaWidget(msg, lines, 40);
        result.setWrapStyleWord(true);
        result.setLineWrap(true);
        result.setEditable(false);
        result.setBackground(color);
        return new JScrollPane(result);
    }

    /**
     * no confirmation message needed
     */
    @Override
    protected String getConfirmMessage() {
        return "";
    }

    /**
     * Override content components to show to user to change place format
     */
    @Override
    protected JPanel getDialogContent() {
        JPanel result = new JPanel(new NestedBlockLayout("<col>"
                + "<row><text wx=\"1\" wy=\"1\" pad=\"5\"/></row>"
                + "<row><text wx=\"1\" wy=\"1\" pad=\"17\"/></row>"
                + "</col>"));        
        placePanel = new GedcomPropertiesPlaceFormatPanel(this);
        placePanel.setPLAC(getOriginalPlaceFormat());
        result.add(placePanel);
        wrngMsg = new JLabel("");
        // Add both lines below is warning is to be displayed (not so useful)
        //result.add(wrngMsg);
        //result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().width));
        
        return result;
    }

    @Override
    public void warnVersionChange(boolean canBeConverted) {
        if (wrngMsg == null) {
            return;
        }
        if (canBeConverted) {
           wrngMsg.setText(NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "WNG_PlaceFormatChanged"));    
        } else {
           wrngMsg.setText("");    
        }
    }

    @Override
    public String getOriginalPlaceFormat() {
        return place.getGedcom().getPlaceFormat();
    }

    @Override
    public void setPlaceFormatConverter(PlaceFormatConverterPanel pfc) {
        this.pfc = pfc;
    }

    @Override
    public PlaceFormatConverterPanel getPlaceFormatConverter() {
        return this.pfc;
    }

    
    /**
     * Converts places to new place format
     * @param gedcom
     * @param event
     * @return 
     * @throws genj.gedcom.GedcomException
     */
    @Override
    protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {
        
        // Places Alignment
        if (placePanel.getPlacesAlignmentToBeDone()) {
            String title = NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "GedcomPropertiesPlaceFormatPanel.jCheckBox2.text");
            String msg = NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "GedcomPropertiesPlaceFormatPanel.jCheckBox2.toolTipText", PropertyPlace.getFormat(gedcom.getPlaceFormat()).length); 
            msg += "\n\n" + NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "WNG_ConfirmPlaceAlignment");
            Object o = DialogManager.createYesNo(title, msg).setMessageType(DialogManager.YES_NO_OPTION).show();
            if (o == DialogManager.YES_OPTION) {
                title = NbBundle.getMessage(SetPlaceHierarchyAction.class, "TITL_PlacesAlignment");
                msg = "";
                GedcomPlacesAligner placesAligner = new GedcomPlacesAligner(gedcom);
                if (placesAligner.convert()) {
                    msg = NbBundle.getMessage(SetPlaceHierarchyAction.class, "MSG_GedcomPlacesAligned", placesAligner.getNbOfPlacesAligned(), placesAligner.getNbOfPlaces());
                    DialogManager.create(title, msg).setMessageType(DialogManager.INFORMATION_MESSAGE).show();
                } else {
                    msg = NbBundle.getMessage(SetPlaceHierarchyAction.class, "MSG_GedcomPlacesNotAligned", placesAligner.getNbOfPlacesAligned(), placesAligner.getNbOfPlaces());
                    msg += "\n\n" + placesAligner.getError().getMessage();
                    DialogManager.create(title, msg).setMessageType(DialogManager.WARNING_MESSAGE).show();
                }
            }
        }
        
        // Places conversion
        if (placePanel.getConversionToBeDone()) {
            if (pfc == null || !pfc.isValidatedMap()) {
                    DialogManager.createError(NbBundle.getMessage(SetPlaceHierarchyAction.class, "TITL_PlacesFormatModification"),
                            NbBundle.getMessage(GedcomPropertiesWizardIterator.class, "MSG_ConversionMapMandatory")).show();
                    return null;
            }

            Object o = DialogManager.createYesNo(
                    NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jCheckBox1.text"),
                    NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "WNG_ConfirmPlaceConversion")).setMessageType(DialogManager.YES_NO_OPTION).show();
            if (o == DialogManager.YES_OPTION) {
                String title = NbBundle.getMessage(SetPlaceHierarchyAction.class, "TITL_PlacesFormatModification");
                String msg = "";
                GedcomPlacesConverter placesConverter = new GedcomPlacesConverter(gedcom, getOriginalPlaceFormat(), placePanel.getPLAC(), pfc.getConversionMapAsString());
                if (placesConverter.convert()) {
                    msg = NbBundle.getMessage(SetPlaceHierarchyAction.class, "MSG_GedcomModified", placesConverter.getNbOfDifferentChangedPlaces(), placesConverter.getNbOfDifferentFoundPlaces());
                    gedcom.setPlaceFormat(placePanel.getPLAC());
                    DialogManager.create(title, msg).setMessageType(DialogManager.INFORMATION_MESSAGE).show();
                } else {
                    msg = NbBundle.getMessage(SetPlaceHierarchyAction.class, "MSG_GedcomNotModified", placesConverter.getNbOfDifferentChangedPlaces(), placesConverter.getNbOfDifferentFoundPlaces());
                    msg += "\n\n" + placesConverter.getError().getMessage();
                    DialogManager.create(title, msg).setMessageType(DialogManager.WARNING_MESSAGE).show();
                }
            }
        } else {
            // Place Header change (if no conversion done already)
            String oldFormat = gedcom.getPlaceFormat();
            String newFormat = placePanel.getPLAC();
            if (!oldFormat.equals(newFormat)) {
                Object o = DialogManager.createYesNo(
                        NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "STEP_4_name"),
                        NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "WNG_ConfirmPlaceHeaderChange")).setMessageType(DialogManager.YES_NO_OPTION).show();
                if (o == DialogManager.YES_OPTION) {
                    String title = NbBundle.getMessage(SetPlaceHierarchyAction.class, "TITL_PlacesHeaderModification");
                    gedcom.setPlaceFormat(newFormat);
                    String msg = NbBundle.getMessage(SetPlaceHierarchyAction.class, "MSG_GedcomHeaderModified");
                    DialogManager.create(title, msg).setMessageType(DialogManager.INFORMATION_MESSAGE).show();
                }
            }
        }
        
        return place != null ? new Context(place) : null;
    }

    @Override
    public int getMode() {
        return GedcomPropertiesPlaceFormatPanel.DEFAULT_MODE;
    }

    @Override
    public void setConfirmEnabled(boolean set) {
        super.setConfirmEnabled(set);
    }
    
    
} //SetPlaceFormat

