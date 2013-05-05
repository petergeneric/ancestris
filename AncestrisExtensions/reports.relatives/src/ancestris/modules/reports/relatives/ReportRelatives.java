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

import ancestris.gedcom.Relative;
import genj.fo.Document;
import genj.gedcom.Indi;
import genj.report.Report;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dominique
 */
@ServiceProvider(service = Report.class)
public class ReportRelatives extends Report {

    private final static Relative[] RELATIVES = {
        Relative.get(Relative.FATHER),
        Relative.get(Relative.MOTHER),
        Relative.create("farfar", "father+father"),
        Relative.create("farmor", "father+mother"),
        Relative.create("morfar", "mother+father"),
        Relative.create("mormor", "mother+mother"),
        Relative.get(Relative.BROTHER),
        Relative.get(Relative.SISTER),
        Relative.get(Relative.HUSBAND),
        Relative.get(Relative.WIFE),
        Relative.get(Relative.DAUGHTER),
        Relative.get(Relative.SON),
        Relative.get(Relative.GRANDSON),
        Relative.get(Relative.GRANDDAUGHTER),
        Relative.get(Relative.PUNCLE),
        Relative.get(Relative.MUNCLE),
        Relative.get(Relative.PAUNT),
        Relative.get(Relative.MAUNT),
        Relative.get(Relative.FNEPHEW),
        Relative.get(Relative.FNIECE),
        Relative.get(Relative.SNEPHEW),
        Relative.get(Relative.SNIECE),
        Relative.create("cousin.paternal", "uncle.paternal+son"),
        Relative.create("cousin.maternal", "uncle.maternal+son"),
        Relative.create("cousine.paternal", "uncle.paternal+daughter"),
        Relative.create("cousine.maternal", "uncle.maternal+daughter")
    };

    /**
     * the report's entry point Our main logic
     */
    public Document start(Indi indiDeCujus) {
        Document document = new Document(NbBundle.getMessage(this.getClass(), "title", indiDeCujus.getName()));

        document.startSection(document.getTitle(), 5);

        for (int i = 0; i < RELATIVES.length; i++) {
            Relative relative = RELATIVES[i];
            Collection<Indi> find = relative.find(indiDeCujus);

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

                for (Indi found : find) {
                    document.addLink(found.getId(), found.getAnchor());
                    document.nextTableCell();
                    document.addText(found.getName());
                    document.nextTableRow("font-weight=normal");
                }

                document.endTable();
            }
        }

        return document;
    }
}
