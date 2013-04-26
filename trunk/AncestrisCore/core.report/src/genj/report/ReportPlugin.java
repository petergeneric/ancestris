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
import ancestris.util.Utilities;
import ancestris.view.AncestrisTopComponent;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import static genj.report.Bundle.*;
import genj.util.Resources;
import ancestris.core.actions.AbstractAncestrisAction;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Plugin
 */
@ServiceProvider(service = AncestrisActionProvider.class)
@NbBundle.Messages({
    "report.popup.title=Run Reports",
    "# {0} - Property Label",
    "# {1} - Property",
    "report.runon=Run report on {0} '{1}'",
    "# {0} - Properties, ",
    "# {1} - count",
    "report.runon.group=Run report on '{0}' ({1}",
    "# {0} - Gedcom",
    "report.runon.gedcom=Run report on Gedcom {0}",
})
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

        result.add(getPropertiesMenuActions(context));
        result.add(getEntitiesMenuActions(context));
        result.add(getGedcomMenuActions(context));

        if (!result.isEmpty()){
            result.add(0, CommonActions.createSeparatorAction(NbBundle.getMessage(ReportPlugin.class, "report.popup.title")));
            result.add(0, null);
        }
        return result;
        // done
    }
    
    //XXX: replace with some interface (Action or ActionListener)
    private SubMenuAction getPropertiesMenuActions(Context context){
        SubMenuAction group=null;
        // props
        List<? extends Property> properties = context.getProperties();
        if (properties.size() > 1) {
            group = new SubMenuAction(
                    report_runon_group(Property.getPropertyNames(properties, MAX_HISTORY),properties.size()));
            getActions(properties, context.getGedcom(), group);
        } else if (properties.size() == 1) {
            Property property = context.getProperty();
            group = new SubMenuAction(
                    report_runon(Property.LABEL, TagPath.get(property).getName()),
                    property.getImage(false));
            getActions(context.getProperty(), context.getGedcom(), group);
        }
        return group;
    }

    //XXX: replace with some interface (Action or ActionListener)
    private SubMenuAction getEntitiesMenuActions(Context context){
        SubMenuAction group=null;
        // entities
        List<? extends Entity> entities = context.getEntities();
        if (entities.size() > 1) {
            group = new SubMenuAction(
                    report_runon_group(Property.getPropertyNames(entities, MAX_HISTORY),entities.size()));
            getActions(entities, context.getGedcom(), group);
        } else if (entities.size() == 1) {
            Entity entity = context.getEntity();
            group = new SubMenuAction(
                    report_runon(Gedcom.getName(entity.getTag(), false), entity.getId()),
                    entity.getImage());
            getActions(context.getEntity(), context.getGedcom(), group);
        }
        return group;
    }

    //XXX: replace with some interface (Action or ActionListener)
    private SubMenuAction getGedcomMenuActions(Context context){
        // gedcom
        SubMenuAction group = new SubMenuAction("Gedcom '" + context.getGedcom().getName() + '\'', Gedcom.getImage());
        getActions(context.getGedcom(), context.getGedcom(), group);
        return group;
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

//    XXX: we should put action in layer    
//@ActionID(category = "Reports", id = "genj.report.EntitiesAction")
//@ActionRegistration(displayName = "SetRoot")
//@ActionReferences({
//    @ActionReference(path = "Ancestris/Actions/GedcomProperty", separatorBefore = 950, position = 1000)})
//public static Action getEntityReportsActions(){
//    return new EntitiesReportActions();
//}
///**
// * ActionRoot
// */
//public static class ReportSubMenuActions extends SubMenuAction {
//
//    public @Override
//    void actionPerformed(ActionEvent e) {
//        assert false;
//    }
//
//    public @Override
//    Action createContextAwareInstance(org.openide.util.Lookup context) {
//
//
//        
//        // entities
//        Collection<? extends Entity> entities = context.lookupAll(Entity.class);
//        if (entities.size() > 1) {
//            SubMenuAction group = new SubMenuAction(
//                    report_runon_group(Property.getPropertyNames(entities, MAX_HISTORY),entities.size()));
//            getActions(entities, Utilities.getGedcomFromContext(context), group);
//            return group;
//        } else if (entities.size() == 1) {
//            Entity entity = context.lookup(Entity.class);
//            SubMenuAction group = new SubMenuAction(
//                    report_runon(Gedcom.getName(entity.getTag(), false), entity.getId()),
//                    entity.getImage());
//            getActions(entity, entity.getGedcom(), group);
//            return group;
//        }
//        return CommonActions.NOOP;
//    }
//}
//
//    
    
    /**
     * Run a report
     */
    private class ActionRun extends AbstractAncestrisAction {

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
