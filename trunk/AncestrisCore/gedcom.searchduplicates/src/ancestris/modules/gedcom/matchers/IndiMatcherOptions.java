package ancestris.modules.gedcom.matchers;

import genj.util.Registry;

/**
 *
 * @author lemovice & frederic
 */
public class IndiMatcherOptions extends MatcherOptions {

    private final Registry registry;
    private int dateinterval;
    private boolean checkAllNames;
    private boolean allFirstNamesEquals;
    private boolean emptyValueInvalid;
    private boolean excludeSameFamily;
    private boolean excludeEmptyNames;

    public IndiMatcherOptions() {
        registry = Registry.get(MatcherOptions.class);
        dateinterval = registry.get("MatcherOptions.Indi.dateinterval", 365);
        checkAllNames = registry.get("MatcherOptions.Indi.checkAllNames", true);
        allFirstNamesEquals = registry.get("MatcherOptions.Indi.allFirstNamesEquals", true);
        emptyValueInvalid = registry.get("MatcherOptions.Indi.emptyValueInvalid", true);
        excludeSameFamily = registry.get("MatcherOptions.Indi.excludeSameFamily", true);
        excludeEmptyNames = registry.get("MatcherOptions.Indi.excludeEmptyNames", true);
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
     * @param checkAllNames check all the names.
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
     * @return the emptyValueInvalid
     */
    public boolean isEmptyValueInvalid() {
        return emptyValueInvalid;
    }

    /**
     * @param emptyValueInvalid the emptyDateInvalid to set
     */
    public void setEmptyValueInvalid(boolean emptyValueInvalid) {
        this.emptyValueInvalid = emptyValueInvalid;
        registry.put("MatcherOptions.Indi.emptyValueInvalid", emptyValueInvalid);
    }

    /**
     * @return the excludeSameFamily
     */
    public boolean isExcludeSameFamily() {
        return excludeSameFamily;
    }

    /**
     * @param excludeSameFamily the excludeSameFamily to set
     */
    public void setExcludeSameFamily(boolean excludeSameFamily) {
        this.excludeSameFamily = excludeSameFamily;
        registry.put("MatcherOptions.Indi.excludeSameFamily", excludeSameFamily);
    }

    /**
     * @return the excludeSameFamily
     */
    public boolean isExcludeEmptyNames() {
        return excludeEmptyNames;
    }

    /**
     * @param excludeEmptyNames the excludeEmptyNames to set
     */
    public void setExcludeEmptyNames(boolean excludeEmptyNames) {
        this.excludeEmptyNames = excludeEmptyNames;
        registry.put("MatcherOptions.Indi.excludeEmptyNames", excludeEmptyNames);
    }
}
