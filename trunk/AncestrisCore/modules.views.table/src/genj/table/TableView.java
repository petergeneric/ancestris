/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
//XXX: try to not export this api
package genj.table;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.swing.ToolBar;
import ancestris.swing.atable.ATable;
import ancestris.swing.atable.ATable.ShortCut;
import ancestris.swing.atable.ATableFilterWidget;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.FileChooserBuilder;
import ancestris.view.ExplorerHelper;
import genj.common.AbstractPropertyTableModel;
import genj.common.PropertyTableModel;
import genj.common.PropertyTableWidget;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import static genj.table.Bundle.filter_txt_file;
import static genj.table.Bundle.tableview_action_export;
import static genj.table.Bundle.tableview_export_dialog_title;
import static genj.table.Bundle.tableview_export_error;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;
import genj.view.SettingsAction;
import genj.view.View;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.miginfocom.swing.MigLayout;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Component for showing entities of a gedcom file in a tabular way
 */
public class TableView extends View {

    private final static Logger LOG = Logger.getLogger("ancestris.table");
    private final static Registry REGISTRY = Registry.get(TableView.class);
    /** a static set of resources */
    private final Resources resources = Resources.get(this);
    /** the table we're using */
    /* package */ PropertyTableWidget propertyTable;
    /** the modes we're offering */
    private final Map<String, Mode> modes = new HashMap<String, Mode>();
    private final JPanel panelShortcuts;
            

    {
        modes.put(Gedcom.INDI, new Mode(Gedcom.INDI, new String[]{
            "INDI",
            "INDI:SEX",
            "INDI:NAME",
            "INDI:BIRT:DATE",
            "INDI:BIRT:PLAC",
            "INDI:DEAT:DATE",
            "INDI:DEAT:PLAC",
            "INDI:FAMS",
            "INDI:FAMC",
            "INDI:RESI:ADDR",
            "INDI:RESI:ADDR:CTRY",
            "INDI:DEAT:AGE",
            "INDI:ASSO",
            "INDI:ASSO:RELA",
            "INDI:CHAN",
            "INDI:NAME:GIVN",
            "INDI:NAME:SURN"
        }));
        modes.put(Gedcom.FAM, new Mode(Gedcom.FAM, new String[]{
            "FAM",
            "FAM:MARR:DATE",
            "FAM:MARR:PLAC",
            "FAM:HUSB",
            "FAM:WIFE",
            "FAM:MARR:HUSB:AGE",
            "FAM:MARR:WIFE:AGE",
            "FAM:DIV:DATE",
            "FAM:NOTE",
            "FAM:MARC:DATE",
            "FAM:MARC:AGNC",
            "FAM:MARC:PLAC",
            "FAM:CHAN"
        }));

        modes.put(Gedcom.OBJE, new Mode(Gedcom.OBJE, new String[]{
            "OBJE",
            "OBJE:FILE:TITL",
            "OBJE:FILE"
        }));

        modes.put(Gedcom.NOTE, new Mode(Gedcom.NOTE, new String[]{
            "NOTE",
            "NOTE:NOTE",
            "NOTE:CHAN"
        }));

        modes.put(Gedcom.SOUR, new Mode(Gedcom.SOUR, new String[]{
            "SOUR",
            "SOUR:TITL",
            "SOUR:DATA:EVEN:DATE",
            "SOUR:REPO",
            "SOUR:REPO:CALN",
            "SOUR:REPO:CALN:MEDI",
            "SOUR:CHAN"
        }));

        modes.put(Gedcom.SUBM, new Mode(Gedcom.SUBM, new String[]{
            "SUBM",
            "SUBM:NAME",
            "SUBM:ADDR",
            "SUBM:ADDR:CITY",
            "SUBM:ADDR:POST",
            "SUBM:ADDR:CTRY",
            "SUBM:PHON",
            "SUBM:CHAN"
        }));

        modes.put(Gedcom.REPO, new Mode(Gedcom.REPO, new String[]{
            "REPO",
            "REPO:NAME",
            "REPO:ADDR",
            "REPO:ADDR:CITY",
            "REPO:ADDR:POST",
            "REPO:ADDR:CTRY",
            "REPO:PHON",
            "REPO:NOTE",
            "REPO:CHAN"
        }));
    }
    ;

    private final Map<String, String> defaultLayouts = new HashMap<String, String>();

    {
        defaultLayouts.put(Gedcom.INDI, "17,52,24,310,96,163,94,156,356,397,224,113,99,388,218,167,254,172,2,1");
        defaultLayouts.put(Gedcom.FAM, "13,52,99,375,296,323,93,92,116,283,100,250,429,154,2,1");
        defaultLayouts.put(Gedcom.OBJE, "3,149,428,634,1");
        defaultLayouts.put(Gedcom.NOTE, "3,55,1425,173,1,1");
        defaultLayouts.put(Gedcom.SOUR, "7,75,578,227,381,287,115,174,0,1");
        defaultLayouts.put(Gedcom.SUBM, "8,75,385,458,202,84,152,149,174,0,1");
        defaultLayouts.put(Gedcom.REPO, "9,60,283,530,174,75,75,120,202,144,1,1");
    }
    ;

    /** current type we're showing */
    private Mode currentMode;
    private final Sticky sticky = new Sticky();

    // TableView Preferences
    public static boolean getFollowEntity() {
        return REGISTRY.get("entity.follow", false);
    }
    // filter
    private final ATableFilterWidget filter;

    public static void setFollowEntity(boolean followEntity) {
        REGISTRY.put("entity.follow", followEntity);
    }

    /**
     * Constructor
     */
    public TableView() {
        this.filter = new ATableFilterWidget();
        // create panel for shortcuts
        panelShortcuts = new JPanel();
        panelShortcuts.setLayout(new MigLayout("flowx, insets 0, gap 0, right"));

        // get modes
        for (Mode mode : modes.values()) {
            mode.load();
        }

        // create our table
        propertyTable = new PropertyTableWidget();
        setExplorerHelper(new ExplorerHelper(propertyTable.getTableComponent()));
        propertyTable.setAutoResize(false);

        // lay it out
        setLayout(new BorderLayout());
        add(propertyTable, BorderLayout.CENTER);

        // get current mode
        currentMode = getMode(Gedcom.INDI);
        String tag = REGISTRY.get("mode", "");
        if (modes.containsKey(tag)) {
            currentMode = getMode(tag);
        }

        propertyTable.setFilterWidget(filter);
        
        // Init shortcuts letter keys
        List<ATable.ShortCut> shortcutsList = new ArrayList<ATable.ShortCut>();
        propertyTable.setShortcut(panelShortcuts, shortcutsList);
        
        // Set entiy mode shortcuts (only work at the table level, not the shorcutpanel level
        propertyTable.setTableShortcut(new ShortCut(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK), new NextMode(true)));
        propertyTable.setTableShortcut(new ShortCut(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK), new NextMode(false)));
        
        propertyTable.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setMovedPath(propertyTable.getColumnsMoved());
            }
        });

        
        // done
        
        
    }
    
    public Gedcom getGedcom() {
        try {
            return propertyTable.getModel().getGedcom();
        } catch (NullPointerException e) {
            return null;
        }
    }

    /* package */ PropertyTableWidget getTable() {
        return propertyTable;
    }

    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(480, 320);
    }

    /**
     * Returns a mode for given tag
     */
    /* package */ Mode getMode() {
        return currentMode;
    }

    /**
     * Returns a mode for given tag
     */
    /* package */ final Mode getMode(String tag) {
        // known mode?
        Mode mode = modes.get(tag);
        if (mode == null) {
            mode = new Mode(tag, new String[0]);
            modes.put(tag, mode);
        }
        return mode;
    }

    /**
     * Sets the type of entities to look at
     */
    /* package */ void setMode(Mode set, boolean reset) {

        REGISTRY.put("mode", set.getTag());

        // give mode a change to grab what it wants to preserve
        if (currentMode != null) {
            currentMode.save();
        }

        // remember current mode
        currentMode = set;

        // tell to table
        PropertyTableModel currentModel = propertyTable.getModel();
        if (currentModel != null) {
            propertyTable.setModel(currentMode.getModel(), reset);
            propertyTable.setColumnLayout(currentMode.layout);
            filter.setColumn(currentMode.getColFilter());
            filter.refresh();
        }
    }

    @Override
    public void setContext(final Context context) {

        if (sticky.isSelected()) {
            return;
        }

        // Do not do anything if sticky is on
        if (sticky.isSelected()) {
            return;
        }

        // Save settings
        currentMode.save();

        // starting a new gedcom?
        PropertyTableModel old = propertyTable.getModel();
        if (old == null) {
            // refresh modes
            for (Mode mode : modes.values()) {
                mode.load(context.getGedcom());
            }
            propertyTable.setModel(currentMode.getModel(), false);
            propertyTable.setColumnLayout(currentMode.layout);
            filter.setColumn(currentMode.getColFilter());
        }

        // pick good mode
        final Mode mode = getModeFor(context);
        if (getFollowEntity()) {
            if (mode != currentMode) {
                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    @Override
                    public void run() {
                        mode.setSelected(true);
                        propertyTable.select(context);
                    }
                });
            }
        }

        if (mode == currentMode) { // select
            propertyTable.select(context);
        }
    }

    private Mode getModeFor(Context context) {
        // any entity matching current mode?
        for (Entity entity : context.getEntities()) {
            if (currentMode.tag.equals(entity.getTag())) {
                return currentMode;
            }
        }
        // pick better
        for (Entity entity : context.getEntities()) {
            Mode m = modes.get(entity.getTag());
            if (m != null) {
                return m;
            }
        }
        // leave as is
        return currentMode;
    }

    /**
     * @see genj.view.ToolBarSupport#populate(JToolBar)
     */
    @Override
    public void populate(ToolBar toolbar) {

        ButtonGroup group = new ButtonGroup();

        for (int i = 0, j = 1; i < Gedcom.ENTITIES.length; i++) {
            String tag = Gedcom.ENTITIES[i];
            Mode mode = getMode(tag);
            JToggleButton b = new JToggleButton((Action)mode);
            toolbar.add(b);
            group.add(b);
            if (currentMode == mode) {
                mode.setSelected(true);
            }
        }

        toolbar.add(filter);
        toolbar.add(panelShortcuts, "growx, pushx");
        // gap
        toolbar.addSeparator();

        // sticky
        toolbar.add(new Download());
        toolbar.add(new JToggleButton((Action)sticky));
        toolbar.add(new Settings());

        toolbar.setFloatable(false);
    }

    /**
     * Write table settings before going
     */
    @Override
    public void removeNotify() {
        // save modes
        for (Mode mode : modes.values()) {
            mode.save();
        }
        // continue
        super.removeNotify();
    }

    private void setMovedPath(int[] columnsMoved) {
        if (currentMode == null) {
            return;
        }
        // Rebuild the paths
        TagPath[] paths = currentMode.getPaths();
        int from = columnsMoved[0];
        int to = columnsMoved[1];

        // Rebuild the sort
        int nbCols = paths.length;
        int[] map = new int[nbCols];
        
        // Prepare map for sort
        for (int i = 0; i< nbCols ; i++) {
            map[i] = i;
        }
        
        // Do a loop permutation of "from" to "to" (we know from and to are different) and build correspondance map during loop
        TagPath tmpPath = paths[from];  // memorise last path
        if (from < to) {
            for (int i = from; i < to; i++) {
                paths[i] = paths[i + 1];
                map[i+1] = i;
            }
        } else {
            for (int i = from; i > to; i--) {
                paths[i] = paths[i - 1];
                map[i-1] = i;
            }
        }
        paths[to] = tmpPath;
        map[from] = to;
        
        // Rebuild sort layout
        String oldLayout = propertyTable.getColumnLayout();
        WordBuffer newLayout = new WordBuffer(",");
        try {
            StringTokenizer tokens = new StringTokenizer(oldLayout, ",");
            int n = Integer.parseInt(tokens.nextToken());
            newLayout.append(n);
            for (int i = 0; i < n && i < nbCols; i++) {
                newLayout.append(tokens.nextToken());
            }
            while (tokens.hasMoreTokens()) {
                try {
                    int c = Integer.parseInt(tokens.nextToken());
                    newLayout.append(map[c]);
                    newLayout.append(tokens.nextToken());
                } catch (IllegalArgumentException e) {
                }
            }
        } catch (Exception t) {
        }
        propertyTable.setColumnLayout(newLayout.toString());
        
        // Update table
        currentMode.setPaths(paths, true);
    }
    
    
    
    

    /**
     * Action - settings
     */
    private class Settings extends SettingsAction {

        @Override
        protected TableViewSettings getEditor() {
            return new TableViewSettings(TableView.this);
        }
    }

    /**
     * Action - go to previous/next mode
     */
    private class NextMode extends AbstractAncestrisAction {

        private final int dir;

        private NextMode(boolean left) {
            if (left) {
                dir = -1;
            } else {
                dir = 1;
        }
    }

        @Override
        public void actionPerformed(ActionEvent event) {
        int next = -1;
        for (int i = 0, j = Gedcom.ENTITIES.length; i < j; i++) {
            next = (i + j + dir) % Gedcom.ENTITIES.length;
            if (currentMode == getMode(Gedcom.ENTITIES[i])) {
                break;
            }
        }
        getMode(Gedcom.ENTITIES[next]).setSelected(true);
    }
    } //NextMode

    
    /**
     * Action - toggle sticky mode
     */
    private class Sticky extends AbstractAncestrisAction {

        /** constructor */
        protected Sticky() {
            super.setImage(ancestris.core.resources.Images.imgStickOff);
            super.setTip(resources.getString("action.stick.tip"));
            super.setSelected(false);
        }

        /** run */
        @Override
        public void actionPerformed(ActionEvent event) {
            setSelected(isSelected());
        }

        @Override
        public boolean setSelected(boolean selected) {
            super.setImage(selected ? ancestris.core.resources.Images.imgStickOn : ancestris.core.resources.Images.imgStickOff);
            return super.setSelected(selected);
        }
    } //Sticky

    /**
     * Action - toggle sticky mode
     */
    @NbBundle.Messages({
        "tableview.action.export=Export",
        "tableview.action.export.tip=<html>Export all information from that Table <br/>View to a text file delimitated by tabs <br/>(tabulations).</html>",
        "# {0} - file path",
        "tableview.export.error=Error while saving to\n{0}",
        "tableview.export.dialog.title=Choose export destination file"
    })
    private class Download extends AbstractAncestrisAction {

        /** constructor */
        protected Download() {
            super.setImage(ancestris.core.resources.Images.imgDownload);
            super.setTip(resources.getString("tableview.action.export.tip"));
            super.setSelected(false);
        }

        /** run */
        @NbBundle.Messages({
            "filter_txt_file=Text Files (*.txt, *.csv)"
        })
        @Override
        public void actionPerformed(ActionEvent event) {
            // .. choose file
            File file  = new FileChooserBuilder(TableView.class)
                    .setTitle(tableview_export_dialog_title())
                    .setApproveText(tableview_action_export())
                    .setFileHiding(true)
                    .setParent((Component)(event.getSource()))
                    .setFileFilter(new FileNameExtensionFilter(filter_txt_file(),"txt","csv"))
                    .setDefaultExtension(FileChooserBuilder.getTextFilter().getExtensions()[0])
                    .setDefaultBadgeProvider()
                    .setDefaultWorkingDirectory(new File(System.getProperty("user.home")))
                    .showSaveDialog(true);
            if (file == null) {
                return;
            }
            try {
                propertyTable.csvExport(file);
            } catch (IOException e) {
                DialogManager.createError("table", tableview_export_error(file.getAbsolutePath())).show();
            }
        }
    } //Download

    
    
    
    
    
    
    
    
    
    
    /**
     * A mode is a configuration for a set of entities
     * The date set Model corresponding to the mode is included in the Mode
     */
    /* package */ class Mode extends AbstractAncestrisAction {

        /** attributes */
        private String tag;
        private final String[] defaults;
        private TagPath[] paths;
        private String layout;
        private int colFilter;
        
        /** data */
        private Gedcom gedcom;
        private Model model;
        
        

        /** constructor */
        private Mode(String t, String[] d) {
            // remember
            tag = t;
            defaults = d;
            paths = TagPath.toArray(defaults);
            colFilter = 0;

            // look
            setTip(resources.getString("mode.tip", Gedcom.getName(tag, true)));
            setImage(Gedcom.getEntityImage(tag));
        }

        private PropertyTableModel getModel() {
            if (model == null) {
                model = new Model(gedcom, this);
            }
            return model;
        }

        public int getColFilter() {
            return colFilter;
        }

        public void setColFilter(int colFilter) {
            this.colFilter = colFilter;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setSelected(true);
        }

        @Override
        public boolean setSelected(boolean selected) {
            if (selected) {
                setMode(this, false);
            }
            return super.setSelected(selected);
        }

        /** load properties from registry */
        private void load() {
            load(getGedcom());
        }

        private void load(Gedcom gedcom) {
            this.gedcom = gedcom;
            
            Registry r = (gedcom == null) ? REGISTRY : gedcom.getRegistry();

            String[] ps = r.get(tag + ".paths", (String[]) null);
            if (ps != null) {
                paths = TagPath.toArray(ps);
            }

            layout = r.get(tag + ".layout", defaultLayouts.get(tag));
            setColFilter(r.get(tag + ".colfilter", 0));
        }

        /** set paths */
        /* package */ void setPaths(TagPath[] set, boolean force) {
            if (force || areDifferent(set, paths)) {
                paths = set;
                if (currentMode == this) {
                    setMode(currentMode, true);
                }
            }
        }

        /** get paths */
        /* package */ TagPath[] getPaths() {
            return paths;
        }

        private boolean areDifferent(TagPath[] set1, TagPath[] set2) {
            if (set1.length != set2.length) {
                return true;
            }
            for (int i = 0 ; i < set1.length ; i++) {
                if (set1[i].compareTo(set2[i]) != 0) {
                    return true;
                }
            }
            return false;
        }
        
        /** save properties from registry */
        private void save() {
            Registry r = (gedcom == null) ? REGISTRY : gedcom.getRegistry();

            // grab current column widths & sort column
            if (currentMode == this && propertyTable.getModel() != null) {
                layout = propertyTable.getColumnLayout();
                colFilter = filter.getColFilter();
            }
            r.put(tag + ".paths", paths);
            r.put(tag + ".layout", layout);
            r.put(tag + ".colfilter", getColFilter());
        }

        /** tag */
        /* package */ String getTag() {
            return tag;
        }

    } //Mode
    
    
    /**
     * A PropertyTableModelWrapper contains the data set for a given entity type (and therefore for a given mode)
     */
    private class Model extends AbstractPropertyTableModel {

        /** mode */
        private final Mode mode; // the mode the model belongs to.
        
        /** our cached rows */
        private List<Entity> rows;  // the data set 

        /** constructor */
        private Model(Gedcom gedcom, Mode set) {
            super(gedcom);
            mode = set;
        }

        /** # columns */
        @Override
        public int getNumCols() {
            return mode.getPaths().length;
        }

        /** # rows */
        @Override
        public int getNumRows() {
            // cache entities if not there yet
            if (rows == null) {
                rows = Collections.synchronizedList(new ArrayList<Entity>(super.getGedcom().getEntities(mode.getTag())));
            }
            // ready
            return rows.size();
        }

        /** path for colum */
        @Override
        public TagPath getColPath(int col) {
            return mode.getPaths()[col];
        }

        /** property for row */
        @Override
        public Property getRowRoot(int row) {

            // init rows
            getNumRows();

            // and look it up
            Property result = (Property) rows.get(row);
            if (result == null) {
                return result;
            }

            // since we do a lazy update after a gedcom write lock we check if cached properties are still good
            if (result.getEntity() == null) {
                result = null;
                rows.set(row, null);
            }

            // done
            return result;
        }

        /** gedcom callback */
        @Override
        public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
            // an entity we're not looking at?
            if (!mode.getTag().equals(entity.getTag())) {
                return;
            }
            synchronized (rows) {
                // add it
                rows.add(entity);
                // tell about it
                fireRowsAdded(rows.size() - 1, rows.size() - 1);
                // done
            }
        }

        /** gedcom callback */
        @Override
        public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
            // an entity we're not looking at?
            if (!mode.getTag().equals(entity.getTag())) {
                return;
            }
            synchronized (rows) {
                // delete it
                for (int i = 0; i < rows.size(); i++) {
                    if (rows.get(i) == entity) {
                        rows.remove(i);
                        // tell about it
                        fireRowsDeleted(i, i);
                        // done
                        return;
                    }
                }
            }
            // hmm, strange
            LOG.log(Level.WARNING, "got notified that entity {0} was deleted but it wasn''t in rows in the first place", entity.getId());
        }

        /** gedcom callback */
        @Override
        public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
            invalidate(gedcom, property.getEntity(), added.getPath());
        }

        /** gedcom callback */
        @Override
        public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
            invalidate(gedcom, property.getEntity(), property.getPath());
        }

        /** gedcom callback */
        @Override
        public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
            invalidate(gedcom, property.getEntity(), new TagPath(property.getPath(), deleted.getTag()));
        }

        private void invalidate(Gedcom gedcom, final Entity entity, final TagPath path) {
            if (mode == null || entity == null || !mode.getTag().equals(entity.getTag())) {
                return;
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // an entity we're not looking at?
                    synchronized (rows) {
                        // a path we're interested in?
                        TagPath[] paths = mode.getPaths();
                        for (int i = 0; i < paths.length; i++) {
                            if (paths[i].equals(path)) {
                                for (int j = 0; j < rows.size(); j++) {
                                    if (rows.get(j) == entity) {
                                        fireRowsChanged(j, j, i);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            });
            
            
        }

        @Override
        public void gedcomHeaderChanged(Gedcom gedcom) {
        }

        Context selectedContext = null;
        
        @Override
        public void gedcomWriteLockAcquired(Gedcom gedcom) {
            Property prop = propertyTable.getSelectedRow();
            if (prop != null) {
                selectedContext = new Context(prop);
            }
        }

        @Override
        public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
        }

        @Override
        public void gedcomAfterUnitOfWork(Gedcom gedcom) {
        }

        @Override
        public void gedcomWriteLockReleased(Gedcom gedcom) {
            // Resort table in case sort columns' content changed
            setMode(currentMode, false);
            if (selectedContext != null) {
                propertyTable.select(selectedContext);
            }
        }

        
    } //Model

    
} //TableView

