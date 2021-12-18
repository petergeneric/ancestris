package ancestris.reports.summaryofrecords;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 */
import ancestris.reports.relatives.ReportRelatives;
import ancestris.util.swing.DialogManager;
import genj.fo.Document;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.MultiLineProperty;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.io.InputSource;
import genj.io.input.FileInput;
import genj.report.Report;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * Report
 *
 * @author Nils Meier <nils@meiers.net>
 * @version 1.0
 */
@ServiceProvider(service = Report.class)
public class ReportSummaryOfRecords extends Report {

    private final static TagPath PATH2IMAGES = new TagPath("INDI:OBJE:FILE");

    /**
     * whether we're generating indexes for places
     */
    public int generatePlaceIndex = 0;
    public String[] generatePlaceIndexs = {
        translate("place.index.none"), translate("place.index.one"), translate("place.index.each")
    };
    public boolean generatePageBreak = false;

    /**
     * max # of images per record
     */
    private int maxImagesPerRecord = 4;

    /**
     * include IDs in output
     */
    public boolean includeIds = true;

    /**
     * sort properties
     */
    public boolean sortProperties = false;

    /**
     * filter properties
     */
    public String filterProperties = "CHAN";

    public int getMaxImagesPerRecord() {
        return maxImagesPerRecord;
    }

    public void setMaxImagesPerRecord(int set) {
        maxImagesPerRecord = Math.max(0, set);
    }

    /**
     * The report's entry point
     */
    public Document start(Gedcom gedcom) {

        // create a document
        Document doc = new Document(translate("title", gedcom.getName()));

        doc.addText(translate("outputHeader") + ": " + gedcom.getName());

        // prepare filter
        Pattern tagFilter = null;
        try {
            if (filterProperties.length() > 0) {
                tagFilter = Pattern.compile(filterProperties);
            }
        } catch (IllegalArgumentException e) {
            println("Filter for properties is not a valid regular expression (" + e.getMessage() + ")");
        }

        // This report cannot be run on the whole gedcom. It does not make sense.
        // Therefore we will use for instance the result of the advanced research view
        List<Entity> searchResult = getSearchEntities(gedcom);
        // If empty, default to the list of relatives as an initial set of indivuals and families to avoid an empty report
        if (searchResult.isEmpty()) {
            Indi indi = getActiveIndi(gedcom);
            if (indi != null) {
                List<Indi> relatives = new ReportRelatives().getRelatives(indi);
                searchResult = new ArrayList<>(relatives);
            }
        }

        if (searchResult.size() > 1000) {
            if (DialogManager.OK_OPTION != DialogManager.create(translate("TITL_SizeWarning"),
                    translate("MSG_SizeWarning", searchResult.size()))
                    .setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).setDialogId("report.SummaryOfRecords").show()) {
                return doc;
            }
        }

        if (generatePageBreak) doc.nextPage();
        
        exportEntities(searchResult.toArray(new Entity[searchResult.size()]), doc, tagFilter);

        // add a new page here - before the index is generated
        doc.nextPage();

        // Done
        return doc;
    }

    /**
     * Exports the given entities
     */
    private void exportEntities(Entity[] ents, Document doc, Pattern tagFilter) {
        for (int e = 0; e < ents.length; e++) {
            exportEntity(ents[e], doc, tagFilter);
        }
    }

    /**
     * Exports the given entity
     */
    private void exportEntity(Entity ent, Document doc, Pattern tagFilter) {

        println(translate("exporting", ent.toString()));

        // start a new section
        if (generatePageBreak) doc.nextPage();
        doc.startSection(ent.toString(this.includeIds), ent.getLinkAnchor());

        // start a table for the entity
        doc.startTable("width=100%");
        doc.addTableColumn("column-width=80%");
        doc.addTableColumn("column-width=20%");

        // export its properties
        exportProperties(ent, doc, tagFilter, 0);

        // add images in next column
        doc.nextTableCell();
        Property[] files = ent.getProperties(PATH2IMAGES);
        for (int f = 0; f < files.length && f < maxImagesPerRecord; f++) {
            PropertyFile file = (PropertyFile) files[f];
            Optional<InputSource> input = file.getInput();
            if (input.isPresent() && input.get() instanceof FileInput) {
                doc.addImage(((FileInput) input.get()).getFile(), "");
            }
        }

        // done
        doc.endTable();
    }

    /**
     * Exports the given property's properties
     */
    private void exportProperties(Property of, Document doc, Pattern tagFilter, int level) {

        // anything to do?
        if (of.getNoOfProperties() == 0) {
            return;
        }

        // create a list
        doc.startList();

        // sort properties
        Property[] props = of.getProperties();
        if (sortProperties) {
            Arrays.sort(props, new Comparator<Property>() {
                public int compare(Property p1, Property p2) {
                    return Gedcom.getName((p1).getTag()).compareTo(Gedcom.getName((p2).getTag()));
                }
            });
        }

        // an item per property
        for (int i = 0; i < props.length; i++) {

            Property prop = props[i];

            if (tagFilter != null && tagFilter.matcher(prop.getTag()).matches()) {
                continue;
            }

            // we don't do anything for xrefs to non-indi/fam
            if (prop instanceof PropertyXRef) {
                PropertyXRef xref = (PropertyXRef) prop;
                if (xref.isTransient() || !(xref.getTargetEntity() instanceof Indi || xref.getTargetEntity() instanceof Fam || xref.getTargetEntity() instanceof Note)) {
                    continue;
                }
            }

            // here comes the item
            doc.nextListItem();

            // fill index while we're at it
            if (prop instanceof PropertyName) {
                PropertyName name = (PropertyName) prop;
                doc.addIndexTerm(translate("index.names"), name.getLastName(), name.getFirstName());
            }
            if (generatePlaceIndex > 0 && (prop instanceof PropertyPlace)) {
                String index = generatePlaceIndex == 1 ? translate("index.places") : translate("index.places.of", prop.getParent().getPropertyName());
                doc.addIndexTerm(index, ((PropertyPlace) prop).getCity());
            }

            // ... and the text
            String format = "";
            if (level == 0) {
                format = "font-weight=bold";
            }
            if (level == 1) {
                format = "font-style=italic";
            }
            doc.addText(Gedcom.getName(prop.getTag()), format);
            doc.addText(" ");

            // with its value
            exportPropertyValue(prop, doc);

            // recurse into it
            exportProperties(prop, doc, tagFilter, level + 1);
        }
        doc.endList();
    }

    /**
     * Exports the given property's value
     */
    private void exportPropertyValue(Property prop, Document doc) {

        // check for links to other indi/fams
        if (prop instanceof PropertyXRef) {

            PropertyXRef xref = (PropertyXRef) prop;
            Entity ent = xref.getTargetEntity();
            doc.addLink(ent.toString(includeIds), ent.getLinkAnchor());

            // done
            return;
        }

        // multiline needs loop
        if (prop instanceof MultiLineProperty) {
            MultiLineProperty.Iterator lines = ((MultiLineProperty) prop).getLineIterator();
            do {
                doc.addText(lines.getValue());
            } while (lines.next());
            // done
            return;
        }

        // patch for NAME
        String value;
        if (prop instanceof PropertyName) {
            value = ((PropertyName) prop).getDisplayValue();
        } else {
            value = prop.getDisplayValue();
        }

        doc.addText(value);

        // done
    }

} //SummaryOfRecords
