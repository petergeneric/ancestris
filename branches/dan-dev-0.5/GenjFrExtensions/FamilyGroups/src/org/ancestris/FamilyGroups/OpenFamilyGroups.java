package org.ancestris.FamilyGroups;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

public final class OpenFamilyGroups implements ActionListener {

    private int minGroupSize = 2;  // Don't print groups with size less than this
    private int maxGroupSize = 20;
    private FamilyGroupsOptionsPanel familyGroupsOptionsPanel = null;
    private DialogDescriptor familyGroupsOptionsPanelDescriptor = null;

    private class FamilyGroupsOptionsPanelDescriptorActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            minGroupSize = familyGroupsOptionsPanel.getMinGroupSizeValue();
            maxGroupSize = familyGroupsOptionsPanel.getMaxGroupSizeValue();
        }
    };

    @Override
    public void actionPerformed(ActionEvent e) {
        familyGroupsOptionsPanel = new FamilyGroupsOptionsPanel(minGroupSize, maxGroupSize);
        familyGroupsOptionsPanelDescriptor = new DialogDescriptor(
                familyGroupsOptionsPanel,
                "test",
                true,
                new FamilyGroupsOptionsPanelDescriptorActionListener());

        Dialog dialog = DialogDisplayer.getDefault().createDialog(familyGroupsOptionsPanelDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = familyGroupsOptionsPanelDescriptor.getValue() == DialogDescriptor.CANCEL_OPTION;
        if (!cancelled) {
            FamilyGroupsTopComponent window = FamilyGroupsTopComponent.findInstance();
            window.setMaxGroupSize(maxGroupSize);
            window.setMinGroupSize(minGroupSize);
            window.openAtTabPosition(0);
            window.requestActive();
        }
    }

    /**
     * @return the minGroupSize
     */
    public int getMinGroupSize() {
        return minGroupSize;
    }

    /**
     * @param minGroupSize the minGroupSize to set
     */
    public void setMinGroupSize(int minGroupSize) {
        this.minGroupSize = minGroupSize;
    }

    /**
     * @return the maxGroupSize
     */
    public int getMaxGroupSize() {
        return maxGroupSize;
    }

    /**
     * @param maxGroupSize the maxGroupSize to set
     */
    public void setMaxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }
}
