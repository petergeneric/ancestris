/**
 * Ancestris - http://www.ancestris.org (Formerly GenJ - GenealogyJ)
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2010 - 2013 Ancestris Author: Daniel Andre <daniel@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import org.openide.util.Utilities;

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
    "report.runon.gedcom=Run report on Gedcom ''{0}''",})
public class ReportPlugin {

    private final static Resources RESOURCES = Resources.get(ReportPlugin.class);
    private final static int MAX_HISTORY = 5;
    private boolean showReportPickerOnOpen = true;
    //XXX: maybe we could put this value in a more gloally scope
    public static final int POSITION = 2000;

    public ReportPlugin() {
        AncestrisPlugin.register(this);
    }

    protected static Collection<Action> getReportActions(Object context, Gedcom gedcom) {

        // Look through reports
        Map<String, SubMenuAction> categories = new TreeMap<>();
        List<Action> result = new ArrayList<>(5);
        for (Report report : ReportLoader.getInstance().getReports()) {
            try {
                String accept = report.accepts(context);
                if (accept != null) {
                    ActionRun run = new ActionRun(accept, context, report, gedcom);
                    String cat = report.getCategory().getDisplayName();
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
     * get all Properties in Lookup lookup and return Gedcom Context associated to these properties. If no properties are found, returns an empty Context (returned value is never null)
     *
     * @param lookup
     * @return
     */
    private static Context getContextFromLookup(org.openide.util.Lookup lookup) {
        Collection<? extends Property> properties = lookup.lookupAll(Property.class);
        Context ctx;
        if (properties.isEmpty()) {
            ctx = new Context();
        } else {
            Gedcom gedcom = properties.iterator().next().getGedcom();
            ctx = new Context(gedcom, null, properties);
        }
        return ctx;
    }

    @ActionID(category = "Reports", id = "genj.report.PropertiesReportSubMenu")
    @ActionRegistration(displayName = "Properties Reports", iconBase = "View.png")
    @ActionReferences({@ActionReference(path = "Ancestris/Actions/GedcomProperty/Tools", separatorBefore = POSITION - 1, position = POSITION)})
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
                setText(report_runon_group(Property.getPropertyNames(properties, MAX_HISTORY), properties.size()));
                addActions(getReportActions(properties, ctx.getGedcom()));
            } else if (properties.size() == 1) {
                Property property = properties.iterator().next();
                setText(report_runon("", property.getDisplayTitle()));
                setImage(property.getImage(false));
                setImage(ReportViewFactory.IMG);
                addActions(getReportActions(property, property.getGedcom()));
            }
            return super.createContextAwareInstance(context);
        }
    }

    @ActionID(category = "Reports", id = "genj.report.EntitiesAction")
    @ActionRegistration(displayName = "Entities Reports")
    @ActionReferences({@ActionReference(path = "Ancestris/Actions/GedcomProperty/Tools", position = POSITION + 10)})
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
                setText(report_runon_group(Property.getPropertyNames(entities, MAX_HISTORY), entities.size()));
                addActions(getReportActions(entities, ctx.getGedcom()));
            } else if (entities.size() == 1) {
                Entity entity = entities.iterator().next();
                setText(report_runon("", entity.getDisplayTitle()));
                setImage(entity.getImage());
                setImage(ReportViewFactory.IMG);
                addActions(getReportActions(entity, entity.getGedcom()));
            }
            return super.createContextAwareInstance(context);
        }
    }

    @ActionID(category = "Reports", id = "genj.report.GedcomReportSubMenu")
    @ActionRegistration(displayName = "Gedcom Reports")
    @ActionReferences({@ActionReference(path = "Ancestris/Actions/GedcomProperty/Tools", position = POSITION + 20, separatorAfter = POSITION + 99)})
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
                setImage(ReportViewFactory.IMG);
                addActions(getReportActions(gedcom, gedcom));
            }
            return super.createContextAwareInstance(context);
        }
    }

    /**
     * Run a report
     */
    private static class ActionRun extends AbstractAncestrisAction {

        /**
         * context
         */
        private Object context;
        /**
         * report
         */
        private Report report;
        /**
         * gedcom
         */
        //XXX: will be removed
        private Gedcom gedcom;

        /**
         * constructor
         */
        private ActionRun(String txt, Object context, Report report, Gedcom gedcom) {
            // remember
            this.context = context;
            this.report = report;
            this.gedcom = gedcom;
            // show
            setText(txt);

            StringBuffer tip = new StringBuffer();
            tip.append("<html><body><table width=500><tr><td>");
            String info = report.getInfo();
            int br = info.indexOf("</p>");
            if (br > 0) {
                info = info.substring(0, br + 4);
            }
            tip.append(info);
            tip.append("</td></tr></table>");
            setTip(tip.toString());
        }

        /**
         * callback
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            if (context == null) {
                return;
            }
            // DANIEL:
            //XXX: Very quick fix!!!
            //XXX: we need to have some report runner independent from reportView class
            // which displays a list of report.
            // One report Viw must be opened to run a report as the runner task is defined in this class.
            // A report can be run from a context menu and in this case the view (ie list of reports) 
            // is not necessary. We will have to find a way to set options in this case.
            
            // FL (2022): context cannot be an entity in order to activate context during AncestrisTopComponent.init(context).setContext(context)
            // Bevcause PropertyNode.getChildren(context) does not work when context is a gedcom only
            Context lookupContext = Utilities.actionsGlobalContext().lookup(Context.class);;
            ReportView view = getReportView(lookupContext);
            if (view != null) {
                view.startReport(report, context, gedcom);
            }
        }
    } //ActionRun

    public static ReportView getReportView(Context contextToOpen) {
        // XXX: Find reportview if opened, must be done using lookup
        // XXX: quick fix to allow report to be launched from right clic, Reports API must be desesigned later
        ReportView view = null;
        AncestrisTopComponent atc = null;
        for (ReportTopComponent tc : AncestrisPlugin.lookupAll(ReportTopComponent.class)) {
            // Reuse a same gedcom report TC component, because the title will otherwise be that of another gedcom
            if (!((Context) contextToOpen).getGedcom().equals(tc.getGedcom())) {
                continue;
            }
            if (!(tc.getView() instanceof ReportView)) {
                continue;
            }
            atc = (AncestrisTopComponent) tc;
            atc.init(contextToOpen); // to make sure the top component will use the proper gedcom
            view = (ReportView) tc.getView();
        }

        if (view == null) {
            //XXX: can't be called from ancestriscore
            ReportTopComponent tc = ((ReportTopComponent) ReportTopComponent.getFactory().create(contextToOpen));
            view = (ReportView) (tc.getView());
            view.setContext(contextToOpen);
            atc = tc;
            //            win.init(contextToOpen);
        }
        if (view != null) {
            atc.open();
            atc.requestActive();
            return view;
        }
        return view; // FIXME: view can't be null
    }

}
