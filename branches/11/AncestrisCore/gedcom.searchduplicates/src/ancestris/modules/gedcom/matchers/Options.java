package ancestris.modules.gedcom.matchers;

/**
 *
 * @author lemovice
 */


public interface Options <O extends MatcherOptions>{

    public O getOptions();

    public void setOptions(O options);
}
