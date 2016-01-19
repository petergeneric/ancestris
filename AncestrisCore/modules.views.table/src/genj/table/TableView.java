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
import genj.util.Registry;
import genj.util.Resources;
import genj.view.SettingsAction;
import genj.view.View;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import net.miginfocom.swing.MigLayout;
import org.openide.util.NbBundle;
import static genj.table.Bundle.*;
import java.awt.Component;
import javax.swing.filechooser.FileNameExtensionFilter;

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
//        modes.put(Gedcom.INDI, new Mode(Gedcom.INDI, new String[]{"INDI", "INDI:NAME", "INDI:SEX", "INDI:BIRT:DATE", "INDI:BIRT:PLAC", "INDI:OCCU", "INDI:FAMS", "INDI:FAMC"}));
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
//        modes.put(Gedcom.FAM, new Mode(Gedcom.FAM, new String[]{"FAM", "FAM:MARR:DATE", "FAM:MARR:PLAC", "FAM:HUSB", "FAM:WIFE", "FAM:CHIL"}));
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

//        modes.put(Gedcom.OBJE, new Mode(Gedcom.OBJE, new String[]{"OBJE", "OBJE:FILE:TITL"}));
        modes.put(Gedcom.OBJE, new Mode(Gedcom.OBJE, new String[]{
            "OBJE",
            "OBJE:FILE:TITL"
        }));

//        modes.put(Gedcom.NOTE, new Mode(Gedcom.NOTE, new String[]{"NOTE", "NOTE:NOTE"}));
        modes.put(Gedcom.NOTE, new Mode(Gedcom.NOTE, new String[]{
            "NOTE",
            "NOTE:NOTE",
            "NOTE:CHAN"
        }));

//        modes.put(Gedcom.SOUR, new Mode(Gedcom.SOUR, new String[]{"SOUR", "SOUR:TITL", "SOUR:TEXT"}));
        modes.put(Gedcom.SOUR, new Mode(Gedcom.SOUR, new String[]{
            "SOUR",
            "SOUR:TITL",
            "SOUR:DATA:EVEN:DATE",
            "SOUR:REPO",
            "SOUR:REPO:CALN",
            "SOUR:REPO:CALN:MEDI",
            "SOUR:CHAN"
        }));

//        modes.put(Gedcom.SUBM, new Mode(Gedcom.SUBM, new String[]{"SUBM", "SUBM:NAME"}));
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

//        modes.put(Gedcom.REPO, new Mode(Gedcom.REPO, new String[]{"REPO", "REPO:NAME", "REPO:NOTE"}));
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
        defaultLayouts.put(Gedcom.OBJE, "2,149,1529,0,1");
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
        propertyTable.setShortcut(panelShortcuts);

        // shortcuts KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK)
        //XXX: shortcut should be placed in @Action... Annotations (layer)
//        new NextMode(true).install(this, "ctrl pressed LEFT");
//        new NextMode(false).install(this, "ctrl pressed RIGHT");
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
    /* package */ void setMode(Mode set) {

        REGISTRY.put("mode", set.getTag());

        PropertyTableModel currentModel = propertyTable.getModel();

        // give mode a change to grab what it wants to preserve
        if (currentModel != null && currentMode != null) {
            currentMode.save();
        }

        // remember current mode
        currentMode = set;

        // tell to table
        if (currentModel != null) {
            propertyTable.setModel(new Model(currentModel.getGedcom(), currentMode));
            propertyTable.setColumnLayout(currentMode.layout);
            filter.setColumn(currentMode.getColFilter());
            filter.refresh();
        }
    }

    @Override
    public void setContext(Context context) {

        if (sticky.isSelected()) {
            return;
        }

        // save settings
        currentMode.save();

        // clear?
        PropertyTableModel old = propertyTable.getModel();
        if (context.getGedcom() == null) {
            if (old != null) {
                propertyTable.setModel(null);
            }
            return;
        }

        // new gedcom?
        if (old == null || old.getGedcom() != context.getGedcom()) {
            // refresh modes
            for (Mode mode : modes.values()) {
                mode.load(context.getGedcom());
            }
            propertyTable.setModel(new Model(context.getGedcom(), currentMode));
            propertyTable.setColumnLayout(currentMode.layout);
            filter.setColumn(currentMode.getColFilter());
        }

        // pick good mode
        Mode mode = getModeFor(context);
        if (getFollowEntity()) {
            if (mode != currentMode) {
                mode.setSelected(true);
            }
        }

        if (mode == currentMode) // select
        {
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
            JToggleButton b = new JToggleButton(mode);
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
        toolbar.add(new JToggleButton(sticky));
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
     * Action - go to next mode
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
                    .setDefaultExtension("txt")
                    .showSaveDialog(true);
            if (file == null) {
                return;
            }
            try {
                propertyTable.tsvExport(file);
            } catch (IOException e) {
                DialogManager.createError("table", tableview_export_error(file.getAbsolutePath())).show();
            }
        }
    } //Download

    /**
     * A PropertyTableModelWrapper
     */
    private static class Model extends AbstractPropertyTableModel {

        /** mode */
        private final Mode mode;
        /** our cached rows */
        private List<Entity> rows;

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
                rows = new ArrayList<Entity>(super.getGedcom().getEntities(mode.getTag()));
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
            // add it
            rows.add(entity);
            // tell about it
            fireRowsAdded(rows.size() - 1, rows.size() - 1);
            // done
        }

        /** gedcom callback */
        @Override
        public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
            // an entity we're not looking at?
            if (!mode.getTag().equals(entity.getTag())) {
                return;
            }
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

        private void invalidate(Gedcom gedcom, Entity entity, TagPath path) {
            // an entity we're not looking at?
            if (!mode.getTag().equals(entity.getTag())) {
                return;
            }
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
            // done
        }
    } //Model

    /**
     * A mode is a configuration for a set of entities
     */
    /* package */ class Mode extends AbstractAncestrisAction {

        /** attributes */
        private String tag;
        private final String[] defaults;
        private TagPath[] paths;
        private String layout;
        private int colFilter;

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
                setMode(this);
            }
            return super.setSelected(selected);
        }

        /** load properties from registry */
        private void load() {
            load(getGedcom());
        }

        private void load(Gedcom gedcom) {
            Registry r = (gedcom == null) ? REGISTRY : gedcom.getRegistry();

            String[] ps = r.get(tag + ".paths", (String[]) null);
            if (ps != null) {
                paths = TagPath.toArray(ps);
            }

            layout = r.get(tag + ".layout", defaultLayouts.get(tag));
            setColFilter(r.get(tag + ".colfilter", 0));
        }

        /** set paths */
        /* package */ void setPaths(TagPath[] set) {
            paths = set;
            if (currentMode == this) {
                setMode(currentMode);
            }
        }

        /** get paths */
        /* package */ TagPath[] getPaths() {
            return paths;
        }

        /** save properties from registry */
        private void save() {
            Registry r = (getGedcom() == null) ? REGISTRY : getGedcom().getRegistry();

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
} //TableView

