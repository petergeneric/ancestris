package ancestris.modules.gedcom.utilities.matchers;

/**
 *
 * @author lemovice
 */


public class FamMatcherOptions extends MatcherOptions {
    private int dateinterval = 2000;
    private boolean checkParents = false;

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
     * @return the checkParents
     */
    public boolean isCheckParents() {
        return checkParents;
    }

    /**
     * @param checkParents the checkParents to set
     */
    public void setCheckParents(boolean checkParents) {
        this.checkParents = checkParents;
    }
}
