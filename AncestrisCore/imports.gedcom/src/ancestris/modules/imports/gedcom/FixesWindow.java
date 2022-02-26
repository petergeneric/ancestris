package ancestris.modules.imports.gedcom;


import ancestris.api.imports.Import;
import ancestris.api.imports.ImportFix;
import ancestris.modules.document.view.FopDocumentView;
import ancestris.util.TimingUtility;
import genj.edit.actions.GedcomEditorAction;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.MetaProperty;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.swing.ImageIcon;
import genj.view.ViewContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2021 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

/**
 *
 * @author frederic
 */
public class FixesWindow {

    private final static Logger LOG = Logger.getLogger("ancestris.import", null);
    private static String[] sections = new String[]{
            "summary", 
            "header", 
            "repairLine", 
            "missingEntity",
            "invalidEntity",
            "invalidTag",
            "invalidTagLocation",
            "invalidSourceLocation",
            "missingTag",
            "invalidFileStructure",
            "invalidName",
            "invalidSex",
            "eventValue",
            "invalidDate",
            "invalidPlace",
            "invalidAge",
            "invalidCardinality",
            "eventsCardinality",
            "switchAssos",
            "duplicateAssociations",
            "invalidInformation",
            "textformatting",
            "createdEntity"
        };
    
    private TreeMap<String, String> summary;
    private Context context;
    private List<ImportFix> fixes;
    private Map<String, ImportFix> fixesMap;
    private List<ViewContext> issues;
    
    /**
     * Constructor
     */
    public FixesWindow(HashMap<String, String> summary, Context context, List<ImportFix> fixes) {
        
        this.summary = new TreeMap<>();
        this.summary.putAll(summary);
        this.context = context;
        this.fixes = fixes;
        
    }

    /**
     * Need to keep all fix information in the ViewContexts so we will use the text for that.
     * At the same time, we need to build a sorting key to be faster.
     * So text will be the sorting-key, and the last element of the sortig-key will be the key to the fix to retrieve it in the loop
     *      Key = section + correction + entity type + entity number + key of fix
     * All issues need also to have a valid entity as well, so in case an entity is null (should not happen), attach it to the first entity.
     * 
     */
    private void convertFixesToIssues() {
        
        issues = new ArrayList<>();
        fixesMap = new HashMap<>();
        Gedcom gedcom = context.getGedcom();
        String nbmax = "000000000000000000000000000000";
        Pattern p = Pattern.compile("([^0-9]+\\?)([0-9]+)(.*)");
        
        for (ImportFix fix : fixes) {
            
            // Build fix map
            fixesMap.put(fix.getId().toString(), fix);
            
            // Build prop : get entity tag, entity id, tagpath
            String xref = fix.getXref();  // xref is an id or a tag if the entity had no id
            Entity ent = gedcom.getEntity(xref);
            if (ent == null) {
                ent = gedcom.getFirstEntity(xref);
            }
            Property property = ent;
            if (ent != null && !fix.getNewTag().isEmpty()) {
                String path = fix.getNewTag().replaceAll("\\(", "?").replaceAll("\\)", "");
                Matcher m = p.matcher(path);
                if (m.matches()) {
                    String result = "";
                    while (m.matches()) {
                        result += m.group(1);
                        int counter = Integer.valueOf(m.group(2)) - 1;
                        result += counter;
                        Matcher m2 = p.matcher(m.group(3));
                        if (!m2.matches()) {
                            result += m.group(3);
                        }
                        m = m2;
                    }
                    path = result;
                }
                try {
                    TagPath tagPath = new TagPath(path);
                    property = ent.getProperty(tagPath);
                } catch (Exception e) {
                    LOG.warning("invalid path: "+path+" from entity="+ent+" and for fix="+fix.getCode());
                }
            }
            
            // Build image
            ImageIcon icon;
            if (ent == null) {
                LOG.warning("Display Issues: following xref is null, so correction attached to first entity: "+fix.getXref() + " - code="+fix.getCode() + " - oldtag="+fix.getOldTag() + " - oldvalue="+fix.getOldValue() + " - newTag="+fix.getNewTag() + " - newValue="+fix.getNewValue());
                ent = gedcom.getFirstEntity("HEAD");
                icon = MetaProperty.IMG_ERROR;
            } else {
                icon = ent.getImage();
            }
            if (property == null) {
                property = ent;
            }
            
            // Build key
            String key = getSectionIndex(fix.getCode()) + ";";    // section
            key += fix.getCode() + ";" ;  // correction
            key += ent.getTag() + ";";  // entity type
            String id = ent.getId().replaceAll("[^0-9]", "");
            id = id.substring(0,Math.min(nbmax.length(), id.length())); // get id number
            if (id.isEmpty()) {
                id = "0";
            }
            key += nbmax.substring(id.length()) + id + ";";
            key += fix.getId().toString();   // fix id
            
            issues.add(new ViewContext(property).setCode(fix.getCode()).setText(key).setImage(icon));
        }
    }

    /**
     * Similar code to GedcomValidateAction
     */
    protected void displayFixes(boolean extract) {
        
        // Prepare issues
        TimingUtility.getInstance().reset();
        LOG.fine("Display Issues: started. Time= "+TimingUtility.getInstance().getTime());
        convertFixesToIssues();
        LOG.fine("Display Issues: list prepared. Time="+TimingUtility.getInstance().getTime());
        
        
        // Prepare thread
        final Preferences modulePreferences = NbPreferences.forModule(FixesWindow.class);
        final int size = issues != null ? issues.size() : 0;
        final ProgressMonitor progressMonitor = new ProgressMonitor(null, getResource("process.name", ""+size), "", 0, size);
        final String title = getResource("title", summary.get("a.software")); 
        final String mode = NbBundle.getMessage(Import.class, (!extract ? "mode.displayFullList" : "mode.displayExtractOnly"));

        progressMonitor.setProgress(0);  
        Task fullTask = new Task(progressMonitor, size) {
            @Override
            public Void doInBackground() {
                String noissues = getResource("noissues");
                final genj.fo.Document doc = new genj.fo.Document(title);
                String goToToc = "  \u2191";
                String tocAnchor = "toc";

                if (fixes != null) {
                    doc.nextParagraph("text-align=center, space-before=2cm, space-after=1cm");
                    doc.addText(title + "  (" + issues.size() + ") - " + mode, "font-size=20, font-weight=bold");
                    doc.nextParagraph();

                    // Summary section
                    String text = getSectionName("summary");
                    doc.addText(text, "text-align=left, font-size=14, font-weight=bold, space-before=2cm, space-after=1cm, keep-with-next.within-page=always, text-decoration=underline");
                    doc.addLink(goToToc, tocAnchor);
                    doc.addTOCEntry(text);
                    doc.nextParagraph();
                    doc.startTable("genj:csv=true,width=60%");
                    doc.addTableColumn("column-width=70%");
                    doc.addTableColumn("column-width=30%");
                    for (String key : summary.keySet()) {
                        doc.nextTableRow();
                        doc.addText(getResource("section.summary."+key));
                        doc.nextTableCell();
                        doc.addText(summary.get(key));
                    }
                    doc.endTable();
                    doc.nextParagraph();

                    LOG.fine("Display Issues: sort starting. Time="+TimingUtility.getInstance().getTime());

                    try {
                        
                        Collections.sort(issues, (Object o1, Object o2) -> {
                            ViewContext vc1 = (ViewContext) o1;
                            ViewContext vc2 = (ViewContext) o2;
                            return vc1.getText().compareTo(vc2.getText());
                        });
                        LOG.fine("Display Issues: sort completed. Time="+TimingUtility.getInstance().getTime());

                        // Issues sections
                        String section = "", newSection = "";
                        String code = "", newCode = "";
                        Iterator<ViewContext> iterator = issues.listIterator();
                        int p = 0;
                        int counterPerCorrection = 0;
                        Set<String> extractedTags = new HashSet<>();
                        boolean truncated = false;
                        LOG.fine("Display Issues: start editing. Time="+TimingUtility.getInstance().getTime());
                        while (iterator.hasNext() && !progressMonitor.isCanceled()) {
                            p++;
                            progressMonitor.setProgress(p);
                            ViewContext c = iterator.next();
                            newCode = c.getCode();
                            newSection = newCode.split("\\.")[0];
                            if (c != null && !code.equals(newCode)) {
                                if (!code.isEmpty()) {
                                    doc.endTable();
                                    doc.nextParagraph();
                                }
                                code = newCode;
                                LOG.fine("Display Issues: editing new code. Time="+TimingUtility.getInstance().getTime()+" - code="+newCode);
                                counterPerCorrection = 0;
                                extractedTags.clear();
                                truncated = false;
                                if (!section.equals(newSection)) {
                                    section = newSection;
                                    LOG.fine("Display Issues: editing new section. Time="+TimingUtility.getInstance().getTime()+" - section="+newSection);
                                    doc.nextParagraph("space-before=3cm, space-after=0cm, keep-with-next.within-page=always");
                                    String sectionStr = getSectionName(section) + "  (" + getSectionCount(section) + ")";
                                    doc.addText(sectionStr, "font-size=14, font-weight=bold, text-decoration=underline");
                                    doc.addLink(goToToc, tocAnchor);
                                    doc.addTOCEntry(sectionStr);
                                    doc.nextParagraph();
                                }
                                doc.nextParagraph("space-before=1cm, space-after=0.25cm, keep-with-next.within-page=always");
                                doc.addText(getResource("col.correction")+":   ", "font-weight=bold");
                                doc.addText(getResource("section."+code) + "  (" + getCorrectionCount(code) + ")");
                                doc.nextParagraph();

                                // table definition
                                doc.startTable("genj:csv=true,width=100%");
                                doc.addTableColumn("column-width=5%");
                                doc.addTableColumn("column-width=15%");
                                doc.addTableColumn("column-width=10%");
                                doc.addTableColumn("column-width=15%");
                                doc.addTableColumn("column-width=10%");
                                doc.addTableColumn("column-width=15%");

                                // colun names
                                doc.nextTableRow();
                                doc.nextTableCell(); doc.addText(getResource("col.id"), "font-weight=bold");
                                doc.nextTableCell(); doc.addText(getResource("col.name"), "font-weight=bold");
                                doc.nextTableCell(); doc.addText(getResource("col.previousTag"), "font-weight=bold");
                                doc.nextTableCell(); doc.addText(getResource("col.previousValue"), "font-weight=bold");
                                doc.nextTableCell(); doc.addText(getResource("col.newTag"), "font-weight=bold");
                                doc.nextTableCell(); doc.addText(getResource("col.newValue"), "font-weight=bold");
                            }

                            // Display line for each correction
                            ImportFix fix = fixesMap.get(getKey(c.getText()));
                            String tag = fix.getOldTag().replaceAll("[0123456789]","");
                            counterPerCorrection++;


                            // In extract mode, do not display line above a certain limit: display only different tags after the limit of first 20
                            if (extract && counterPerCorrection > 20 && (extractedTags.contains(tag) || extractedTags.size() > 500)) {
                                if (!truncated) {
                                    doc.nextTableRow();
                                    doc.addText("...");
                                    truncated = true;
                                }
                                continue;  
                            }
                            extractedTags.add(tag);
                            truncated = false;

                            // col.id
                            doc.nextTableRow();
                            doc.addLink(c.getEntity().getId().isEmpty() ? c.getEntity().getTag() : c.getEntity().getId(), 
                                        c.getProperty() != null ? c.getProperty().getLinkAnchor() : c.getEntity().getLinkAnchor());

                            // col.name
                            doc.nextTableCell();
                            String entityString = c.getEntity().toString(false);
                            if (entityString.length() > 100) {
                                entityString = entityString.substring(0, 75);
                            }
                            if (c.getEntity() instanceof Note) {
                                entityString = c.getEntity().getPropertyName();
                            }
                            doc.addText(entityString);

                            // col.previousTag
                            doc.nextTableCell();
                            doc.addText(fix.getOldTag());

                            // col.previousValue
                            doc.nextTableCell();
                            doc.addText(fix.getOldValue());

                            // col.newTag
                            doc.nextTableCell();
                            doc.addText(fix.getNewTag());

                            // col.newValue
                            doc.nextTableCell();
                            doc.addText(fix.getNewValue());
                        }
                        doc.endTable();
                        doc.nextParagraph(); doc.addText("   ");
                        doc.nextParagraph(); doc.addText("   ");
                        doc.nextParagraph(); doc.addText("   ");
                        doc.addLink(goToToc, tocAnchor);
                    
                    } catch (Exception e) {
                        LOG.severe("Display Issues during sorting and editing: exception = "+e);
                        Exceptions.printStackTrace(e);
                    }
                    
                } else {
                    doc.nextParagraph("text-align=center, space-before=2cm, space-after=1cm");
                    doc.addText(noissues);
                }

                SwingUtilities.invokeLater(() -> {
                    // Open result window
                    LOG.fine("Display Issues: window opening. Time="+TimingUtility.getInstance().getTime());
                    FopDocumentView window = new FopDocumentView(context, title, getResource("tabtip")); 
                    window.displayDocument(doc, modulePreferences);
                    LOG.fine("Display Issues: window displayed. Time="+TimingUtility.getInstance().getTime());
                    
                });

                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    public void run() {
                        // Make sure the Gedcom editor is also open as it will be useful to analyse the issues
                        GedcomEditorAction action = new GedcomEditorAction();
                        action.edit(context.getProperty(), false);
                    }
                });
                    
                progressMonitor.setProgress(size);
                LOG.fine("Display Issues: background task completed. Time="+TimingUtility.getInstance().getTime());
                return null;
            }
        };
        fullTask.execute();
        LOG.fine("Display Issues: background task started. Time="+TimingUtility.getInstance().getTime());
    }

    private String getSectionIndex(String code) {
        String str = code.split("\\.")[0];
        for (int i=0; i < sections.length ; i++) {
            if (sections[i].equals(str)) {
                return String.format("%03d", i);
            }
        }
        return "000";
    }

    private String getSectionName(String section) {
        for (String item : sections) {
            if (item.equals(section)) {
                return getResource("section." + item);
            }
        }
        return "";
    }

    private String getSectionCount(String section) {
        int count = 0;
        for (ViewContext c : issues) {
            String str = c.getCode().split("\\.")[0];
            count += str.equals(section) ? 1 : 0;
        }
        return "" + count;
    }

    private String getCorrectionCount(String correction) {
        int count = 0;
        for (ViewContext c : issues) {
            String str = c.getCode();
            count += str.equals(correction) ? 1 : 0;
        }
        return "" + count;
    }

    private String getKey(String str) {
        String[] bits = str.split(";");
        return bits[4];
    }

    private Integer order(String entityTag) {
        int order = 9;
        
        for (String type : Gedcom.ENTITIES) {
            if (type.equals(entityTag)) {
                return order;
            }
            order++;
        }
        return order;
    }

    
    private String getResource(String resource, String... params) {
        String ret = "";
        try {
            ret = NbBundle.getMessage(FixesWindow.class, resource, params);
        } catch (MissingResourceException e) {
            System.err.println();
            LOG.log(Level.WARNING, "MissingResourceException: " + resource);
            ret = resource;
        }
        return ret;
    }
    
    private class Task extends SwingWorker<Void, Void> {

        private final ProgressMonitor pm;
        private int maxp = 0;

        public Task(ProgressMonitor progressMonitor, int maxProgress) {
            pm = progressMonitor;
            maxp = maxProgress;
        }

        @Override
        public Void doInBackground() {
            return null;
        }

        @Override
        public void done() {
            if (pm != null) {
                pm.setProgress(maxp);
            }
        }
    }
    
 
    
    

}
