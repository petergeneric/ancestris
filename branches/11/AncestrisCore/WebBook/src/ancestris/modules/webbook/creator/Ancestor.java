package ancestris.modules.webbook.creator;

import genj.gedcom.Indi;
import java.math.BigInteger;

/**
 * Ancestris - Report creating a web Book or reports
 *
 * @author Frederic Lapeyre <frederic@ancestris.org>
 * @version 0.1
 */
public class Ancestor {

    BigInteger sosa = BigInteger.ZERO;
    int gen = 0;
    Indi indi = null;

    public Ancestor(BigInteger sosa, Indi indi, int gen) {
        this.sosa = sosa;
        this.indi = indi;
        this.gen = gen;
    }
}
