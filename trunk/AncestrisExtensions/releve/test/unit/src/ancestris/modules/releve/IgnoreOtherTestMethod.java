package ancestris.modules.releve;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Regle pour n'executer qu'une seule methode de test dans un test Junit
 * exemple utilisation
    public class MergeModelMarriageTest {
        @Rule
        public IgnoreOtherTestMethod rule = new IgnoreOtherTestMethod("testMergeRecordMarriage");
        ....
     }
 *
 */
public class IgnoreOtherTestMethod implements TestRule {

    private final String testMethodShortName;

    public IgnoreOtherTestMethod(String testMethodShortName) {
        this.testMethodShortName = testMethodShortName;
    }

    @Override
    public Statement apply(final Statement statement, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (testMethodShortName.isEmpty() || testMethodShortName.equals(description.getMethodName())) {
                    // si le nom de la methode est égal a celui passé en parametre au constructeur , j'exécute la methode
                    statement.evaluate();
                } else {
                    // Sinon je n'execute pas la methode et je retourne "skipped" qui sera affiché dans le rapport de tests de JUnit
                    throw new AssumptionViolatedException("skipped");
                }
            }
        };
    }
}

