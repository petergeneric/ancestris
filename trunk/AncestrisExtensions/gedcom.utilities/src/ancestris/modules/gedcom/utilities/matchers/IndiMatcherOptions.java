package ancestris.modules.gedcom.utilities.matchers;

/**
 *
 * @author lemovice
 */
public class IndiMatcherOptions extends MatcherOptions {

    private int dateinterval = 2000;
    private boolean checkAllNames = true;
    private boolean allFirstNamesEquals = true;
    private boolean checkFamilies = false;
    private boolean emptyValueValid = true;

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
    }

    /**
     * @return the checkFamilies
     */
    public boolean isCheckFamilies() {
        return checkFamilies;
    }

    /**
     * @param checkFamilies the checkFamilies to set
     */
    public void setCheckFamilies(boolean checkFamilies) {
        this.checkFamilies = checkFamilies;
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
    }
}
