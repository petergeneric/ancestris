package ancestris.modules.gedcom.utilities.matchers;

import genj.util.Registry;

/**
 *
 * @author lemovice & frederic
 */
public class IndiMatcherOptions extends MatcherOptions {

    private Registry registry;
    private int dateinterval;
    private boolean checkAllNames;
    private boolean allFirstNamesEquals;
    private boolean emptyValueValid;

    public IndiMatcherOptions() {
        registry = Registry.get(MatcherOptions.class);
        dateinterval = registry.get("MatcherOptions.Indi.dateinterval", 365);
        checkAllNames = registry.get("MatcherOptions.Indi.checkAllNames", true);
        allFirstNamesEquals = registry.get("MatcherOptions.Indi.allFirstNamesEquals", true);
        emptyValueValid = registry.get("MatcherOptions.Indi.emptyValueValid", false);
    }
    
    /**
     * @return the dateinterval
     */
    public int getDateinterval() {
        return dateinterval;
    }

    /**
     * @param dateinterval the dateinterval to set
     */
    public void setDateinterval(int dateinterval) {
        this.dateinterval = dateinterval;
        registry.put("MatcherOptions.Indi.dateinterval", dateinterval);
    }

    /**
     * @return the allLastNames
     */
    public boolean isCheckAllNames() {
        return checkAllNames;
    }

    /**
     * @param allLastNames the allLastNames to set
     */
    public void setCheckAllNames(boolean checkAllNames) {
        this.checkAllNames = checkAllNames;
        registry.put("MatcherOptions.Indi.checkAllNames", checkAllNames);
    }

    /**
     * @return the allFirstNames
     */
    public boolean isAllFirstNamesEquals() {
        return allFirstNamesEquals;
    }

    /**
     * @param allFirstNames the allFirstNames to set
     */
    public void setAllFirstNames(boolean allFirstNames) {
        this.allFirstNamesEquals = allFirstNames;
        registry.put("MatcherOptions.Indi.allFirstNamesEquals", allFirstNames);
    }

    /**
     * @return the emptyValueValid
     */
    public boolean isEmptyValueValid() {
        return emptyValueValid;
    }

    /**
     * @param emptyValueValid the emptyDateValid to set
     */
    public void setEmptyValueValid(boolean emptyValueValid) {
        this.emptyValueValid = emptyValueValid;
        registry.put("MatcherOptions.Indi.emptyValueValid", emptyValueValid);
    }
}
