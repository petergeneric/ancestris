package ancestris.modules.gedcom.agecalc;

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEventDetails;
import genj.gedcom.UnitOfWork;
import genj.gedcom.time.PointInTime;
import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.*;

/**
 *
 * @author frédéric
 */
@ActionID(id = "ancestris.modules.gedcom.agecalc", category = "Edit")
@ActionRegistration(
        displayName = "#CTL_AgeCalcAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Edit", name = "AgeCalcAction", position = 2600)
public class AgeCalclAction extends AbstractAncestrisContextAction {

    private Context contextToOpen = null;
    private Gedcom gedcom = null;
    

    public AgeCalclAction() {
        super();
        setImage("ancestris/modules/gedcom/agecalc/agecalc16.png");
        setText(NbBundle.getMessage(AgeCalclAction.class, "CTL_AgeCalcAction", ""));
        setTip(NbBundle.getMessage(AgeCalclAction.class, "CTL_AgeCalcAction.tip"));
    }
    
    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {

        contextToOpen = getContext();
        gedcom = contextToOpen.getGedcom();

        AgeCalcPanel ageCalcPanel = new AgeCalcPanel(getContext());
        Object choice = DialogManager.create(NbBundle.getMessage(AgeCalclAction.class, "AgeCalclAction.AskParams"), ageCalcPanel)
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                .setDialogId("ageCalcPanel")
                .show();

        if (choice == DialogManager.OK_OPTION) {
            ageCalcPanel.savePreferences();
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {
                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        calculateAges(gedcom, ageCalcPanel.isOverwrite(), ageCalcPanel.isAfterDeath(), ageCalcPanel.isGuessed());
                    }
                }); // end of doUnitOfWork
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    
    private void calculateAges(Gedcom gedcom, boolean overwriteAgeString, boolean guessedAfterDeath, boolean isGuessed) {
        
        for (Indi indi : gedcom.getIndis()) {

            // Get birth date and determine action
            PointInTime pitBirth =  indi.getStartPITOfAge(); 
            if (pitBirth == null || !pitBirth.isValid()) {
                continue;
            }
            
            // Get death date and determine action
            PropertyDate deathDate = indi.getDeathDate();
            if (!guessedAfterDeath) {
                deathDate = null;
            }
                
            // Loop on events
            for (PropertyEventDetails event : indi.getEvents()) {
                
                // skip if date invalid
                Property eventDate = event.getDate();
                if (eventDate == null) {
                    continue;
                }
                
                event.updateAge(true, overwriteAgeString, (deathDate != null && deathDate.isValid() && eventDate.compareTo(deathDate) > 0) || isGuessed);
                
            }
        }
    }
}
