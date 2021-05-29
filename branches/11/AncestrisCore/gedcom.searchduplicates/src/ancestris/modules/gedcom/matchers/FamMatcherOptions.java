package ancestris.modules.gedcom.matchers;

import genj.util.Registry;

/**
 *
 * @author lemovice & frederic
 */


public class FamMatcherOptions extends MatcherOptions {

    private Registry registry;
    private int dateinterval;
    private boolean emptyValueInvalid;

    public FamMatcherOptions() {
        registry = Registry.get(MatcherOptions.class);
        dateinterval = registry.get("MatcherOptions.Fam.dateinterval", 365);
        emptyValueInvalid = registry.get("MatcherOptions.Fam.emptyValueInvalid", true);
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
     * @return the emptyValueInvalid
     */
    public boolean isEmptyValueInvalid() {
        return emptyValueInvalid;
    }

    /**
     * @param emptyValueInvalid the emptyValueInvalid to set
     */
    public void setEmptyValueInvalid(boolean emptyValueInvalid) {
        this.emptyValueInvalid = emptyValueInvalid;
        registry.put("MatcherOptions.Fam.emptyValueInvalid", emptyValueInvalid);
    }
}
