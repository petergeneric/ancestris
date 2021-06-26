/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.util;

import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parser for Sosa/D'Aboville String
 * @author Zurga
 */
public class SosaParser {
    
     private final static Logger LOG = Logger.getLogger("ancestris.app", null);
    
    private BigInteger sosa;
    private String daboville;
    private Integer generation;
    
    /**
     * Constructeur.
     * @param sosaString String représenting a Sosa or d'aboville or Sosa/d'aboville number. 
     */
    public SosaParser(String sosaString) {
        parseSosa(sosaString);
    }
    
    private void parseSosa(String sosaString) {
        final String[] espace = sosaString.split(" ");
        // manage Generation
        if (espace.length == 2) {
            generation = Integer.valueOf(espace[1].substring(1));
        }
        if (espace[0] != null && !"".equals(espace[0])) {
            final int premierPoint = espace[0].indexOf('-');
            if (premierPoint > 0) {
            sosa = new BigInteger(espace[0].substring(0, premierPoint));
            daboville = "1"+ espace[0].substring(premierPoint);
            } else  {
                try {
                    sosa = new BigInteger(espace[0]);
                } catch (NumberFormatException e) {
                    LOG.log(Level.FINER, "Not a Sosa number (for the records) :", e);
                    //Not a pure Sosa number
                    daboville = espace[0];
                }
                
            }
        }
        
    }
    
    /**
     * Getter.
     * @return The Sosa number 
     */
     public BigInteger getSosa() {
        return sosa;
    }

    /**
     * Getter.
     * @return D'aboville value 
     */
    public String getDaboville() {
        return daboville;
    }

    /**
     * Getter.
     * @return Genération from Cujus 
     */
    public Integer getGeneration() {
        return generation;
    }
    
    
}
