/**
 * Ancestris
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2016 Frederic Lapeyre <frederic@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.timeline;

import genj.almanac.Almanac;
import genj.gedcom.Gedcom;
import genj.gedcom.Grammar;
import genj.gedcom.PropertyEvent;
import genj.gedcom.TagPath;
import genj.util.Resources;
import genj.util.swing.ColorsWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.ListSelectionWidget;
import genj.util.swing.NestedBlockLayout;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The ViewInfo representing settings of a TimelineView
 */
public class TimelineViewSettings extends JTabbedPane {

    /**
     * resources we use
     */
    private Resources resources = Resources.get(this);

    /**
     * a widget for selecting paths to show
     */
    private final ListSelectionWidget<TagPath> pathsList;

    /**
     * almanac panel
     */
    private AlmanacPanel almanacPanel = null;

    /**
     * Checkbox for options
     */
    private final JCheckBox checkTags, checkDates, checkGrid, packIndi;

    /**
     * spinners
     */
    private final JSpinner spinCmBefEvent, spinCmAftEvent, spinFontSize;

    /**
     * colorchooser for colors
     */
    private final ColorsWidget colorWidget;
    
    private final JComboBox fontName;

    public Commit commit;

    /**
     * @see genj.view.Settings#init(genj.view.ViewManager)
     */
    TimelineViewSettings(final TimelineView view) {

        final Gedcom gedcom = view.getModel().getGedcom();

        commit = new Commit(view);

        
        
        // events to pick from
        pathsList = new ListSelectionWidget<TagPath>() {
            @Override
            protected ImageIcon getIcon(TagPath path) {
                Grammar grammar = Grammar.V55;
                if (gedcom != null) {
                    grammar = gedcom.getGrammar();
                }
                return grammar.getMeta(path).getImage();
            }
            @Override
            protected String getText(TagPath path) {
                return path.getName();
            }
        };
        if (gedcom != null) {
            pathsList.setChoices(PropertyEvent.getTagPaths(gedcom));
        }
        pathsList.setCheckedChoices(view.getModel().getPaths());
        pathsList.addChangeListener(commit);

        
        
        
        // Almanac list and categories
        Almanac almanac = Almanac.getInstance();
        almanac.waitLoaded();
        almanacPanel = new AlmanacPanel(almanac, view, commit);
        
        
        // create a panel for options
        JPanel panelOptions = new JPanel(new NestedBlockLayout(
                "<col><check gx=\"1\"/><check gx=\"1\"/><check gx=\"1\"/><check gx=\"1\"/><row><label/><spin/></row><row><label/><spin/></row><row><label/><choose/></row><row><label/><spin/></row></col>"
        ));
        panelOptions.setOpaque(false);

        
        
        
        // ... checkboxes    
        checkTags = createCheck("info.show.tags", view.isPaintTags());
        checkDates = createCheck("info.show.dates", view.isPaintDates());
        checkGrid = createCheck("info.show.grid", view.isPaintGrid());
        packIndi = createCheck("info.pack.indi", view.isPackIndi());
        panelOptions.add(checkTags);
        panelOptions.add(checkDates);
        panelOptions.add(checkGrid);
        panelOptions.add(packIndi);

        spinCmBefEvent = createSpinner(TimelineView.MIN_CM_BEF_EVENT, view.getCmBeforeEvents(), TimelineView.MAX_CM_BEF_EVENT, "info.befevent.tip");
        panelOptions.add(new JLabel(resources.getString("info.befevent")));
        panelOptions.add(spinCmBefEvent);

        spinCmAftEvent = createSpinner(TimelineView.MIN_CM_AFT_EVENT, view.getCmAfterEvents(), TimelineView.MAX_CM_AFT_EVENT, "info.aftevent.tip");
        panelOptions.add(new JLabel(resources.getString("info.aftevent")));
        panelOptions.add(spinCmAftEvent);
        
        
        panelOptions.add(new JLabel(resources.getString("info.fontname")));
        fontName = new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontName.setPreferredSize(new java.awt.Dimension(200, 25));
        fontName.setSelectedItem(view.getFontName());
        fontName.addItemListener(commit);
        panelOptions.add(fontName);

        panelOptions.add(new JLabel(resources.getString("info.fontsize")));
        spinFontSize = new JSpinner(new SpinnerNumberModel(view.getDefaulFontHeight(), 8, 30, 1));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinFontSize, "#0");
        spinFontSize.setEditor(editor);
        spinFontSize.addChangeListener(editor);
        spinFontSize.addChangeListener(commit);
        panelOptions.add(spinFontSize);
        
        // panel for main options
        JPanel panelMain = new JPanel(new BorderLayout());
        panelMain.add(new JLabel(resources.getString("info.events")), BorderLayout.NORTH);
        panelMain.add(pathsList, BorderLayout.CENTER);
        panelMain.add(panelOptions, BorderLayout.SOUTH);

        // color chooser
        colorWidget = new ColorsWidget();
        List<String> keys = new ArrayList<>(view.colors.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            String name = resources.getString("color." + key).replace("color.", "");
            colorWidget.addColor(key, name, view.colors.get(key));
        }
        colorWidget.addChangeListener(commit);

        // layout 
        add(resources.getString("page.main"), panelMain);
        add(resources.getString("page.almanac"), almanacPanel);
        add(resources.getString("page.colors"), colorWidget);

        // done
    }

    private JCheckBox createCheck(String key, boolean on) {
        JCheckBox result = new JCheckBox(resources.getString(key), on);
        result.addActionListener(commit);
        result.setOpaque(false);
        return result;
    }

    private JSpinner createSpinner(double min, double value, double max, String tip) {
        JSpinner result = new JSpinner(new SpinnerNumberModel(value, min, max, 0.1D));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(result, "##0.0");
        result.setEditor(editor);
        result.addChangeListener(editor);
        result.addChangeListener(commit);
        result.setToolTipText(resources.getString(tip));
        return result;
    }

    public class Commit implements ChangeListener, ActionListener, ItemListener {

        private final TimelineView view;

        private Commit(TimelineView view) {
            this.view = view;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            actionPerformed(new ActionEvent(e.getSource(), 0, ""));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            String command = e.getActionCommand();
            if (source instanceof ListSelectionWidget) {
                command = "rebuild";
            }
            if (source instanceof JSpinner) {
                command = "redraw";
            }
            if (source instanceof JCheckBox) {
                JCheckBox cb = (JCheckBox) source;
                if (cb == packIndi) {
                    command = "redraw";
                }
            }
            
            // choosen EventTags
            view.getModel().setPaths(pathsList.getCheckedChoices(), command.equals("rebuild"));
            
            // checks
            view.setPaintTags(checkTags.isSelected());
            view.setPaintDates(checkDates.isSelected());
            view.setPaintGrid(checkGrid.isSelected());

            // sliders
            view.setCMPerEvents(((Double) spinCmBefEvent.getModel().getValue()), ((Double) spinCmAftEvent.getModel().getValue()), command.equals("redraw"));
            view.setPackIndi(packIndi.isSelected(), command.equals("redraw"));
            view.setFontSize((Integer)spinFontSize.getModel().getValue());

            // colors
            view.colors.keySet().forEach((key) -> {
                view.colors.put(key, colorWidget.getColor(key));
            });
            view.getRegistry().put("color", view.colors);
            

            // almanac categories
            view.setAlmanacs(almanacPanel.getCheckedAlmanacs());
            view.setAlmanacCategories(almanacPanel.getCheckedCategories());
            view.setAlmanacSigLevel(almanacPanel.getAlmanacSigLevel());
            
            
            // save registry
            view.saveInRegistry();
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            view.setFontName((String) fontName.getSelectedItem());
            view.saveInRegistry();
        }
    }

} //TimelineViewSettings
