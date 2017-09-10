package ancestris.modules.gedcom.utilities.matchers;

/**
 *
 * @author lemovice
 */


public interface Options <O extends MatcherOptions>{

    public O getOptions();

    public void setOptions(O options);
}
