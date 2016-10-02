/**
 * Ancestris
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2016 Frederic Lapeyre <frederic@ancestris.org>
 * 
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.modules.almanac;

import ancestris.view.GenjViewTopComponent;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisViewInterface;
import genj.timeline.TimelineView;
import genj.timeline.TimelineViewFactory;
import genj.view.ViewFactory;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.RetainLocation;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//ancestris.app//Timeline//EN",
autostore = false)
@RetainLocation(AncestrisDockModes.TABLE)
@ServiceProvider(service = AncestrisViewInterface.class)
public final class TimelineTopComponent extends GenjViewTopComponent {

    private static final String PREFERRED_ID = "TimelineTopComponent";
    private static TimelineTopComponent factory;
    private static ViewFactory viewfactory = new TimelineViewFactory();
    private boolean firstActivation = false;

    public ViewFactory getViewFactory() {
        return viewfactory;
    }

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized TimelineTopComponent getFactory() {
        if (factory == null) {
            factory = new TimelineTopComponent();
        }
        return factory;
    }

    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        super.writeProperties(p);
    }

    public void readProperties(java.util.Properties p) {
        super.readProperties(p);
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    /**
     * On timeline activation, need to force selected individual to appear
     * (if TopComponent visible at launch, selection works in timelineview ;
     * but if TopComponent hiddent at launch, selection does not work ; So this method forces to redo CenterSelection after component is shown).
     */
    @Override
    protected void componentActivated() {
        super.componentActivated();
        if (!firstActivation) {
            firstActivation = true;
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    ((TimelineView) getView()).centerToSelection();
                }
            });
        }
    }



}
