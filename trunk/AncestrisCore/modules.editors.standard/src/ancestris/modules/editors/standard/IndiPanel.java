/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard;

import ancestris.modules.beans.PropertyTabbedPane;
import ancestris.api.editor.Editor;
import ancestris.modules.beans.AEventBean;
import ancestris.modules.beans.AListBean;
import ancestris.modules.beans.ANameBean;
import genj.edit.beans.PropertyBean;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.gedcom.TagPath;
import genj.view.ViewContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class IndiPanel extends Editor {

    private ancestris.modules.beans.ANameBean aNameBean2;
    private PropertyTabbedPane namePane;
    private ancestris.modules.beans.AEventBean birthBean;
    private PropertyTabbedPane birthPane;
    private ancestris.modules.beans.AEventBean deathBean;
    private PropertyTabbedPane deathPane;
    private ancestris.modules.beans.AChoiceBean occuBean;
    private PropertyTabbedPane occuPane;
    private ancestris.modules.beans.APlaceBean resiBean;
    private PropertyTabbedPane resiPane;
    AListBean eventsPanel;
    private List<PropertyBean> childBeans;
    private Context context;

    public IndiPanel() {

        aNameBean2 = new ANameBean();
        namePane = new PropertyTabbedPane(aNameBean2, Gedcom.getName("NAME"), null, null);

        birthBean = new AEventBean();
        birthBean.setDetailedView(false);
        birthBean.setTag("BIRT"); // NOI18N
        birthPane = new PropertyTabbedPane(birthBean, Gedcom.getName("BIRT"), null, null);

        deathBean = new AEventBean();
        deathBean.setShowKnown(true);
        deathBean.setDetailedView(false);
        deathBean.setTag("DEAT"); // NOI18N
        deathPane = new PropertyTabbedPane(deathBean, Gedcom.getName("DEAT"), null, null);

        resiBean = new ancestris.modules.beans.APlaceBean();
        resiPane = new PropertyTabbedPane(resiBean, Gedcom.getName("RESI"), null, null);

        occuBean = new ancestris.modules.beans.AChoiceBean();
        occuPane = new PropertyTabbedPane(occuBean, Gedcom.getName("OCCU"), null, null);

        // Summary panel
        JPanel summary = new JPanel(new MigLayout(
                new LC().fillX().hideMode(2).flowY(),
                new AC().align("left").grow().fill()));

        summary.setOpaque(true);
        summary.add(namePane);
        summary.add(birthPane);
        summary.add(deathPane);
        summary.add(occuPane);
        summary.add(resiPane);

        eventsPanel = new AListBean();
        eventsPanel.setBlueprint("", "<i><name path=.></i>&nbsp;:&nbsp;<prop path=.:DATE img=no>&nbsp;(<prop path=.:PLAC>)");  // NOI18N
        eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.PAGE_AXIS));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(NbBundle.getMessage(IndiPanel.class, "IndiPanel.tab.summary"), new JScrollPane(summary));
        tabs.addTab(NbBundle.getMessage(IndiPanel.class, "IndiPanel.tab.events"), new JScrollPane(eventsPanel));

        setLayout(new MigLayout(
                new LC().fillX().hideMode(2).flowY()));
        add(tabs, new CC().grow());


        childBeans = Arrays.asList(deathBean, birthBean, aNameBean2, occuBean, resiBean);
        // Add changes to each bean change listeners
        for (PropertyBean bean : childBeans) {
            bean.addChangeListener(changes);
        }

    }
    private Indi indi;

    /**
     * Get the value of indi
     *
     * @return the value of indi
     */
    public Indi getIndi() {
        return indi;
    }

    /**
     * Set the value of indi
     *
     * @param entity new value of indi
     */
    @Override
    protected void setContextImpl(Context context) {
        this.context = context;

        Entity entity = context.getEntity();
        if (entity == null || !(entity instanceof Indi)) {
            return;
        }

        this.indi = (Indi) entity;
        deathBean.setContext(indi, null);
        birthBean.setContext(indi, null);
        aNameBean2.setContext(indi, null);
        occuBean.setContext(indi, TagPath.valueOf(".:OCCU"));
        resiBean.setContext(indi, TagPath.valueOf(".:RESI:PLAC"));


        ArrayList<Property> events = new ArrayList<Property>(5);
        for (Property p : indi.getProperties()) {
            if (p instanceof PropertyEvent) {
                events.add(p);
            }
        }
            eventsPanel.removeAll();
            eventsPanel.repaint();
        eventsPanel.add(events.toArray(new Property[]{}), null, null);
            eventsPanel.revalidate();
    }

    @Override
    public ViewContext getContext() {
        return new ViewContext(context);
    }

    /**
     * commit beans - transaction has to be running already
     */
    @Override
    public void commit() {
        try {
            for (PropertyBean bean : childBeans) {
                bean.commit();
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected String getTitleImpl() {
        if (context == null || context.getEntity() == null) {
            return "";
        }
        return (new ViewContext(context.getEntity())).getText();
    }
}
