/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * from genjreports.ReportRelatives by Nils Meier
 *
 */
package ancestris.modules.reports.relatives;

import static ancestris.gedcom.PropertyFinder.Constants.*;
import ancestris.gedcom.Relative;
import genj.fo.Document;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.report.Report;
import java.util.Collection;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dominique
 */
@ServiceProvider(service = Report.class)
public class ReportRelatives extends Report {

    private final static Relative[] RELATIVES = {
        Relative.get(FATHER),
        Relative.get(MOTHER),
        Relative.get(GRANDPARENT),
//XXX:        Relative.create("farfar", "father+father"),
//        Relative.create("farmor", "father+mother"),
//        Relative.create("morfar", "mother+father"),
//        Relative.create("mormor", "mother+mother"),
        Relative.get(BROTHER),
        Relative.get(SISTER),
        Relative.get(HUSBAND),
        Relative.get(WIFE),
        Relative.get(DAUGHTER),
        Relative.get(SON),
        Relative.get(GRANDSON),
        Relative.get(GRANDDAUGHTER),
        Relative.get(UNCLE_AUNT),
//        Relative.get(PUNCLE),
//        Relative.get(MUNCLE),
//        Relative.get(PAUNT),
//        Relative.get(MAUNT),
//        Relative.get(FNEPHEW),
//        Relative.get(FNIECE),
//        Relative.get(SNEPHEW),
//        Relative.get(SNIECE),
//        Relative.create("cousin.paternal", "uncle.paternal+son"),
//        Relative.create("cousin.maternal", "uncle.maternal+son"),
//        Relative.create("cousine.paternal", "uncle.paternal+daughter"),
//        Relative.create("cousine.maternal", "uncle.maternal+daughter")
    };

    /**
     * the report's entry point Our main logic
     */
    public Document start(Indi indiDeCujus) {
        Document document = new Document(NbBundle.getMessage(this.getClass(), "title", indiDeCujus.getName()));

        document.startSection(document.getTitle(), 5);

        for (int i = 0; i < RELATIVES.length; i++) {
            Relative relative = RELATIVES[i];
            Collection<Entity> find = relative.find(indiDeCujus);

            if (!find.isEmpty()) {
                document.startSection(relative.getDescription(), 3);
                document.startTable("border=1, width=100%, cellpadding=5, cellspacing=2, frame=below, rules=rows");
                document.addTableColumn("column-width=10%");
                document.addTableColumn("column-width=90%");
                document.nextTableRow("font-weight=bold");
                document.addText(NbBundle.getMessage(this.getClass(), "indi.ID"));
                document.nextTableCell();
                document.addText(NbBundle.getMessage(this.getClass(), "indi.name"));
                document.nextTableRow("font-weight=normal");

                for (Entity found : find) {
                    document.addLink(found.getId(), found.getAnchor());
                    document.nextTableCell();
                    if (found instanceof Indi){
                        document.addText(((Indi)found).getName());
                    }
                    document.nextTableRow("font-weight=normal");
                }

                document.endTable();
            }
        }

        return document;
    }
}
