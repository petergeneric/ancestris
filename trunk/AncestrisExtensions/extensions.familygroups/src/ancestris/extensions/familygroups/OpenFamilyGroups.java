package ancestris.extensions.familygroups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.util.NbPreferences;

public final class OpenFamilyGroups implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        FamilyGroupsTopComponent window = FamilyGroupsTopComponent.findInstance();
        window.setMinGroupSize(Integer.valueOf(NbPreferences.forModule(OpenFamilyGroups.class).get("minGroupSize", "2")));
        window.setMaxGroupSize(Integer.valueOf(NbPreferences.forModule(OpenFamilyGroups.class).get("maxGroupSize", "20")));
        window.openAtTabPosition(0);
        window.requestActive();
        window.start();
    }
}
