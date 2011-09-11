package org.ancestris.extensions.familygroups;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class OpenFamilyGroups implements ActionListener {

/*  private int minGroupSize = 2;  // Don't print groups with size less than this
    private int maxGroupSize = 20;
    private FamilyGroupsOptionsPanel familyGroupsOptionsPanel = null;
    private DialogDescriptor familyGroupsOptionsPanelDescriptor = null;

    private class FamilyGroupsOptionsPanelDescriptorActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            minGroupSize = familyGroupsOptionsPanel.getMinGroupSizeValue();
            NbPreferences.forModule(OpenFamilyGroups.class).put("minGroupSize", String.valueOf(minGroupSize));
            maxGroupSize = familyGroupsOptionsPanel.getMaxGroupSizeValue();
            NbPreferences.forModule(OpenFamilyGroups.class).put("maxGroupSize", String.valueOf(maxGroupSize));
        }
    };
*/
    @Override
    public void actionPerformed(ActionEvent e) {
/*      minGroupSize = Integer.valueOf(NbPreferences.forModule(OpenFamilyGroups.class).get("minGroupSize", "2"));
        maxGroupSize = Integer.valueOf(NbPreferences.forModule(OpenFamilyGroups.class).get("maxGroupSize", "20"));
        familyGroupsOptionsPanel = new FamilyGroupsOptionsPanel(minGroupSize, maxGroupSize);
        familyGroupsOptionsPanelDescriptor = new DialogDescriptor(
                familyGroupsOptionsPanel,
                NbBundle.getMessage(FamilyGroupsTopComponent.class, "CTL_FamilyGroupsAction"),
                true,
                new FamilyGroupsOptionsPanelDescriptorActionListener());

        Dialog dialog = DialogDisplayer.getDefault().createDialog(familyGroupsOptionsPanelDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = familyGroupsOptionsPanelDescriptor.getValue() == DialogDescriptor.CANCEL_OPTION;
        if (!cancelled) {
*/            FamilyGroupsTopComponent window = FamilyGroupsTopComponent.findInstance();
            window.setMaxGroupSize(Integer.valueOf(NbPreferences.forModule(OpenFamilyGroups.class).get("minGroupSize", "2")));
            window.setMinGroupSize(Integer.valueOf(NbPreferences.forModule(OpenFamilyGroups.class).get("maxGroupSize", "20")));
            window.openAtTabPosition(0);
            window.requestActive();
            window.start();
//        }
    }
}
