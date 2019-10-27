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
package ancestris.modules.views.graph;

/**
 * Parser for Sosa/D'Aboville String
 * @author Zurga
 */
public class SosaParser {
    private Long sosa;
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
            sosa = Long.valueOf(espace[0].substring(0, premierPoint));
            daboville = "1"+ espace[0].substring(premierPoint);
            } else {
                sosa = Long.valueOf(espace[0]);
            }
        }
        
    }
    
    /**
     * Getter.
     * @return The Sosa number 
     */
     public Long getSosa() {
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
