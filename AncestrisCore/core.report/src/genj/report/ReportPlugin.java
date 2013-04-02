/**
 * Ancestris - http://www.ancestris.org (Formerly GenJ - GenealogyJ)
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2010 - 2013 Ancestris
 * Author: Daniel Andre <daniel@ancestris.org>
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

import ancestris.core.actions.AncestrisActionProvider;
import ancestris.core.actions.CommonActions;
import ancestris.core.actions.SubMenuAction;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.core.report.ReportTopComponent;
import ancestris.gedcom.PropertyNode;
import ancestris.view.AncestrisTopComponent;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.Resources;
import genj.util.swing.Action2;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.Action;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Plugin
 */
@ServiceProvider(service = AncestrisActionProvider.class)
@NbBundle.Messages({"popup.title=Run Reports"})
public class ReportPlugin implements AncestrisActionProvider {

    private final static Resources RESOURCES = Resources.get(ReportPlugin.class);
    private final static int MAX_HISTORY = 5;
    private boolean showReportPickerOnOpen = true;

    public ReportPlugin() {
        AncestrisPlugin.register(this);

    }

    @Override
    public List<Action> getActions(boolean hasFocus, Node[] nodes) {
        if (hasFocus){
            return new ArrayList<Action>();
        }
        List<Property> props = new ArrayList<Property>();
        for (Node node : nodes) {
            if (node instanceof PropertyNode) {
                props.add(((PropertyNode) node).getProperty());
            }
        }
        if (props.isEmpty()) {
            return new ArrayList<Action>();
        }
        Gedcom gedcom = props.get(0).getGedcom();
        Context context = new Context(gedcom, null, props);
        List<Action> result = new ArrayList<Action>(5);

        // props
        List<? extends Property> properties = context.getProperties();
        if (properties.size() > 1) {
            SubMenuAction group = new SubMenuAction("'" + Property.getPropertyNames(properties, 5) + "' (" + properties.size() + ")");
            getActions(properties, context.getGedcom(), group);
            result.add(group);
        } else if (properties.size() == 1) {
            Property property = context.getProperty();
            SubMenuAction group = new SubMenuAction(
                    Property.LABEL + " '" + TagPath.get(property).getName() + '\'',
                    property.getImage(false));
            getActions(context.getProperty(), context.getGedcom(), group);
            result.add(group);
        }

        // entities
        List<? extends Entity> entities = context.getEntities();
        if (entities.size() > 1) {
            SubMenuAction group = new SubMenuAction("'" + Property.getPropertyNames(entities, 5) + "' (" + entities.size() + ")");
            getActions(entities, context.getGedcom(), group);
            result.add(group);
        } else if (entities.size() == 1) {
            Entity entity = context.getEntity();
            SubMenuAction group = new SubMenuAction(
                    Gedcom.getName(entity.getTag(), false) + " '" + entity.getId() + '\'',
                    entity.getImage());
            getActions(context.getEntity(), context.getGedcom(), group);
            result.add(group);
        }

        // gedcom
        SubMenuAction group = new SubMenuAction("Gedcom '" + gedcom.getName() + '\'', Gedcom.getImage());
        getActions(context.getGedcom(), context.getGedcom(), group);
        result.add(group);

        if (!result.isEmpty()){
            result.add(0, CommonActions.createSeparatorAction(NbBundle.getMessage(ReportPlugin.class, "popup.title")));
            result.add(0, null);
        }
        return result;
        // done
    }

//    /**
//     * collects actions for reports valid for given context
//     */
//        @Override
//    public List<Action> getActions(Node[] nodes) {
//            List<Property> properties = new ArrayList<Property>();
//            for (Node node:nodes){
//                if (node instanceof PropertyNode){
//                    properties.add(((PropertyNode)node).getProperty());
//                }
//            }
//            if (properties.isEmpty()){
//                return new ArrayList<Action>();
//            }
//            Gedcom gedcom = properties.get(0).getGedcom();
//            Context context = new Context(gedcom, null, properties);
//
//        SubMenuAction action = new SubMenuAction("Reports", ReportViewFactory.IMG);
//        getActions(context, gedcom, action);
//        List<Action> result = new ArrayList<Action>();
//        if (!action.getActions().isEmpty()) {
//            result.add(action);
//        }
//        return result;
//
//    }
//
    private void getActions(Object context, Gedcom gedcom, SubMenuAction group) {

        // Look through reports
        Map<String, SubMenuAction> categories = new HashMap<String, SubMenuAction>();
        for (Report report : ReportLoader.getInstance().getReports()) {
            try {
                String accept = report.accepts(context);
                if (accept != null) {
                    ActionRun run = new ActionRun(accept, context, report, gedcom);
                    String cat = report.getCategory();
                    if (cat == null) {
                        group.addAction(run);
                    } else {
                        SubMenuAction catgroup = categories.get(cat);
                        if (catgroup == null) {
                            catgroup = new SubMenuAction(RESOURCES.getString("title") + " (" + cat + ")", report.getIcon());
                            categories.put(cat, catgroup);
                        }
                        catgroup.addAction(run);
                    }
                }
            } catch (Throwable t) {
                ReportView.LOG.log(Level.WARNING, "Report " + report.getClass().getName() + " failed in accept()", t);
            }
        }

        for (SubMenuAction cat : categories.values()) {
            group.addAction(cat);
        }
        // done
    }

    /**
     * Run a report
     */
    private class ActionRun extends Action2 {

        /** context */
        private Object context;
        /** report */
        private Report report;
        /** gedcom */
        private Gedcom gedcom;

//        /** constructor */
//        private ActionRun(Report report) {
//            this(report.getName(), null, report);
//        }
        /** constructor */
        private ActionRun(String txt, Object context, Report report, Gedcom gedcom) {
            // remember
            this.context = context;
            this.report = report;
            this.gedcom = gedcom;
            // show
            setText(txt);

            StringBuffer tip = new StringBuffer();
            tip.append("<html><body><table width=320><tr><td>");
            String info = report.getInfo();
            int br = info.indexOf("</p>");
            if (br > 0) {
                info = info.substring(0, br + 4);
            }
            tip.append(info);
            tip.append("</td></tr></table>");
            setTip(tip.toString());
        }

        /** callback */
        @Override
        public void actionPerformed(ActionEvent event) {
            if (context == null) {
                return;
            }
            //XXX: Very quick fix!!!
            ReportView view = getReportView(new Context(gedcom));
            if (view != null) {
                view.startReport(report, context);
            }
        }
    } //ActionRun

    public static ReportView getReportView(Context contextToOpen) {
        // XXX: Find reportview if opened, must be done using lookup
        // XXX: quick fix to allow reoprt to be launched from right clic, Reports API must be desesigned later
        ReportView view = null;
        AncestrisTopComponent atc = null;
        for (ReportTopComponent tc : AncestrisPlugin.lookupAll(ReportTopComponent.class)) {
//                    if (!((Context)context).getGedcom().equals(tc.getGedcom()))
//                        continue;
            if (!(tc.getView() instanceof ReportView)) {
                continue;
            }
            atc = (AncestrisTopComponent) tc;
            view = (ReportView) tc.getView();
        }

        if (view != null) {
            atc.open();
            atc.requestActive();
            return view;
        }

        //XXX: can't be called from ancestriscore
        ReportTopComponent win = ((ReportTopComponent)ReportTopComponent.getFactory().create(contextToOpen));
        //            win.init(contextToOpen);
        win.open();
        win.requestActive();
        return (ReportView) ( win.getView());
    }

//XXX:    @Override
//  public void viewOpened(View view) {
//    if (view instanceof ReportView) {
//      if (showReportPickerOnOpen)
//        ((ReportView)view).startReport();
//    }
//  }
}
