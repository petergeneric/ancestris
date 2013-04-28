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

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.actions.SubMenuAction;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.core.report.ReportTopComponent;
import ancestris.view.AncestrisTopComponent;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import static genj.report.Bundle.*;
import genj.util.Resources;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Plugin
 */
@NbBundle.Messages({
    "report.popup.title=Run Reports",
    "# {0} - Property Label",
    "# {1} - Property",
    "report.runon=Run report on {0} ''{1}''",
    "# {0} - Properties, ",
    "# {1} - count",
    "report.runon.group=Run report on ''{0}'' ({1}",
    "# {0} - Gedcom",
    "report.runon.gedcom=Run report on Gedcom ''{0}''",
})
public class ReportPlugin{

    private final static Resources RESOURCES = Resources.get(ReportPlugin.class);
    private final static int MAX_HISTORY = 5;
    private boolean showReportPickerOnOpen = true;
    //XXX: maybe we could put this value in a more gloally scope
    private static final int POSITION = 2000;

    public ReportPlugin() {
        AncestrisPlugin.register(this);

    }

//    /**
//     * collects actions for reports valid for given context
//     */
//        @Override
//    public List<Action> getReportActions(Node[] nodes) {
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
//        getReportActions(context, gedcom, action);
//        List<Action> result = new ArrayList<Action>();
//        if (!action.getReportActions().isEmpty()) {
//            result.add(action);
//        }
//        return result;
//
//    }
//
    private static Collection<Action> getReportActions(Object context, Gedcom gedcom) {

        // Look through reports
        Map<String, SubMenuAction> categories = new TreeMap<String, SubMenuAction>();
        List<Action> result = new ArrayList<Action>(5);
        for (Report report : ReportLoader.getInstance().getReports()) {
            try {
                String accept = report.accepts(context);
                if (accept != null) {
                    ActionRun run = new ActionRun(accept, context, report, gedcom);
                    String cat = report.getCategory();
                    if (cat == null) {
                        result.add(run);
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
        result.addAll(categories.values());
        return result;
        // done
    }

    /**
     * get all Properties in Lookup lookup and return Gedcom Context 
     * associated to these properties.
     * If no properties are found, returns an empty Context (returned value is
     * never null)
     * @param lookup
     * @return 
     */
    private static Context getContextFromLookup(org.openide.util.Lookup lookup){
        Collection<? extends Property> properties = lookup.lookupAll(Property.class);
        Context ctx;
        if (properties.isEmpty()){
            ctx = new Context();
        } else {
            Gedcom gedcom = properties.iterator().next().getGedcom();
            ctx = new Context(gedcom, null, properties);
        }
        return ctx;
    }

@ActionID(category = "Reports", id = "genj.report.PropertiesReportSubMenu")
@ActionRegistration(displayName = "Properties Reports")
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty/Tools", separatorBefore = POSITION-1, position = POSITION)})
public static class PropertiesReportSubMenu extends SubMenuAction {

    public @Override
    void actionPerformed(ActionEvent e) {
        assert false;
    }

    public @Override
    Action createContextAwareInstance(org.openide.util.Lookup context) {
        Context ctx = getContextFromLookup(context);
        Collection<? extends Property> properties = ctx.getProperties();

        clearActions();
        if (properties.size() > 1) {
            setText(report_runon_group(Property.getPropertyNames(properties, MAX_HISTORY),properties.size()));
            addActions(getReportActions(properties, ctx.getGedcom()));
//            getReportActions(entities, Utilities.getGedcomFromContext(context), group);
        } else if (properties.size() == 1) {
            Property property = properties.iterator().next();
            setText(report_runon(Property.LABEL, TagPath.get(property).getName()));
            setImage(property.getImage(false));
            addActions(getReportActions(property, property.getGedcom()));
        }
        return super.createContextAwareInstance(context);
    }
}

@ActionID(category = "Reports", id = "genj.report.EntitiesAction")
@ActionRegistration(displayName = "Entities Reports")
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty/Tools", position = POSITION+10)})
public static class EntitiesReportSubMenu extends SubMenuAction {

    public @Override
    void actionPerformed(ActionEvent e) {
        assert false;
    }

    public @Override
    Action createContextAwareInstance(org.openide.util.Lookup context) {
        Context ctx = getContextFromLookup(context);
       // entities
        Collection<? extends Entity> entities = ctx.getEntities();

        clearActions();
        if (entities.size() > 1) {
            setText(report_runon_group(Property.getPropertyNames(entities, MAX_HISTORY),entities.size()));
            addActions(getReportActions(entities, ctx.getGedcom()));
        } else if (entities.size() == 1) {
            Entity entity = entities.iterator().next();
            setText(report_runon(Gedcom.getName(entity.getTag(), false), entity.getId()));
            setImage(entity.getImage());
            addActions(getReportActions(entity, entity.getGedcom()));
        }
        return super.createContextAwareInstance(context);
    }
}

@ActionID(category = "Reports", id = "genj.report.GedcomReportSubMenu")
@ActionRegistration(displayName = "Gedcom Reports")
@ActionReferences({
    @ActionReference(path = "Ancestris/Actions/GedcomProperty/Tools", position = POSITION+20, separatorAfter = POSITION+99 )})
public static class GedcomReportSubMenu extends SubMenuAction {

    public @Override
    void actionPerformed(ActionEvent e) {
        assert false;
    }

    public @Override
    Action createContextAwareInstance(org.openide.util.Lookup context) {
        Gedcom gedcom = getContextFromLookup(context).getGedcom();
        
        clearActions();
        if (gedcom != null) {
            setText(report_runon_gedcom(gedcom.getName()));
            setImage(Gedcom.getImage());
            addActions(getReportActions(gedcom,gedcom));
        }
        return super.createContextAwareInstance(context);
    }
}


    
    /**
     * Run a report
     */
    private static class ActionRun extends AbstractAncestrisAction {

        /** context */
        private Object context;
        /** report */
        private Report report;
        /** gedcom */
        //XXX: will be removed
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
