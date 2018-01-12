package ancestris.modules.gedcom.utilities.matchers;

import genj.util.Registry;

/**
 *
 * @author lemovice & frederic
 */


public class FamMatcherOptions extends MatcherOptions {

    private Registry registry;
    private int dateinterval;
    private boolean emptyValueValid;

    public FamMatcherOptions() {
        registry = Registry.get(MatcherOptions.class);
        dateinterval = registry.get("MatcherOptions.Fam.dateinterval", 365);
        emptyValueValid = registry.get("MatcherOptions.Fam.emptyValueValid", true);
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
        registry.put("MatcherOptions.Fam.dateinterval", dateinterval);
}

    /**
     * @return the emptyValueValid
     */
    public boolean isEmptyValueValid() {
        return emptyValueValid;
    }

    /**
     * @param emptyValueValid the emptyValueValid to set
     */
    public void setEmptyValueValid(boolean emptyValueValid) {
        this.emptyValueValid = emptyValueValid;
        registry.put("MatcherOptions.Fam.checkAllNames", emptyValueValid);
    }
}
