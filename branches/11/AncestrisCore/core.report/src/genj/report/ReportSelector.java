/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2009 Nils Meier <nils@meiers.net>
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
package genj.report;

import genj.option.Option;
import genj.option.OptionsWidget;
import genj.util.Resources;
import ancestris.core.actions.AbstractAncestrisAction;
import genj.util.swing.ImageIcon;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.Action;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * Select report from list, show details, show options
 */
class ReportSelector extends JPanel {

    private ReportDetail detail = new ReportDetail();
    private ReportList list = new ReportList(ReportLoader.getInstance().getReports(), ReportList.VIEW_TREE);
    private OptionsWidget options = new OptionsWidget("");
    private Action actionGroup = new ActionGroup();
    static final Resources RESOURCES = Resources.get(ReportView.class);
    

    /** Constructor */
    public ReportSelector() {

        super(new BorderLayout());

        final Resources res = Resources.get(this);

        final JTabbedPane right = new JTabbedPane();
        right.add(res.getString("title"), detail);
        right.add(res.getString("report.options"), options);

        detail.setOpaque(false);

        detail.setPreferredSize(new Dimension(320, 200));
        options.setPreferredSize(new Dimension(320, 200));

        add(new JScrollPane(list), BorderLayout.WEST);
        add(right, BorderLayout.CENTER);

        list.setSelectionListener(new ReportSelectionListener() {

            @Override
            public void valueChanged(Report report) {
                detail.setReport(report);
                if (report != null) {
                    right.setTitleAt(0, report.getName());
                    options.setOptions(report.getOptions());
                } else {
                    right.setTitleAt(0, res.getString("report.options"));
                    options.setOptions(new ArrayList<Option>());
                }
            }
        });
    }

    /**
     * select a report
     */
    public void select(Report report) {
        if (report != null) {
            list.setSelection(report);
        }
    }

    /** Selected report */
    Report getReport() {

        // commit edits
        options.stopEditing();

        // selection
        return list.getSelection();
    }

    public Action getActionGroup() {
        return actionGroup;
    }

    /**
     * Toggles gouping of reports into categories.
     * Action: GROUP
     */
    private class ActionGroup extends AbstractAncestrisAction {

        /**
         * Creates the action object.
         */
        protected ActionGroup() {
            setImage(new ImageIcon(ReportView.class, "Group"));
            setTip(RESOURCES.getString("report.group.tip"));
        }

        /**
         * Toggles grouping of reports.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            int viewType = list.getViewType();
            if (viewType == ReportList.VIEW_LIST) {
                list.setViewType(ReportList.VIEW_TREE);
            } else {
                list.setViewType(ReportList.VIEW_LIST);
            }
//        registry.put("group", list.getViewType());
        }
    } //ActionGroup
}
