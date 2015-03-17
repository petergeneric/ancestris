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
package ancestris.modules.beans;

import genj.edit.beans.BeanHelper;
import genj.edit.beans.PropertyBean;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import javax.swing.JLabel;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

public final class AEventBean extends PropertyBean {

    private boolean detailedView = false;
    private ancestris.modules.beans.ADateBean aDateBean1;
    private ancestris.modules.beans.APlaceBean aPlaceBean1;
    private JLabel ageLabel;
    private JLabel agncLabel;
    private JLabel causLabel;
    private javax.swing.JCheckBox cbIsKnown;
    private ancestris.modules.beans.AAddrBean evtAddr;
    private ancestris.modules.beans.ASimpleBean evtAge;
    private ancestris.modules.beans.ASimpleBean evtAgency;
    private ancestris.modules.beans.ASimpleBean evtCause;
    private ancestris.modules.beans.AChoiceBean evtType;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel placeLabel;
    private javax.swing.JLabel typeLabel;

    /**
     * Get the name of tag
     *
     * @return the name of tag
     */
    private String getTagName() {
        String tag = getTag();
        return tag == null ? "" : Gedcom.getName(tag);
    }

    /**
     * Set the value of tag
     *
     * @param tag new value of tag
     */
    @Override
    public void setTag(String tag) {
        super.setTag(tag);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, getTagName());
        repaint();
    }

    public boolean isDetailedView() {
        return detailedView;
    }

    public void setDetailedView(boolean detailedView) {
        this.detailedView = detailedView;
    }

    @Override
    protected void setPropertyImpl(Property event) {
        cbIsKnown.setSelected(event != null);
        aDateBean1.setContext(root, path, event, (String) null);
        aPlaceBean1.setContext(root, path, event, (String) null);

        evtAddr.setContext(root, path, event, (String) null);
        evtType.setContext(root, path, event, "TYPE");
        evtAge.setContext(root, path, event, "AGE");
        evtAgency.setContext(root, path, event, "AGNC");
        evtCause.setContext(root, path, event, "CAUS");

        showOrHide();
    }

    /** Creates new form NewGedcomVisualPanel2 */
    public AEventBean() {
        // layout the bean
        MigLayout layout = new MigLayout(
                new LC().fillX().hideMode(2),
                new AC().align("right").gap("rel").grow().fill());
        setLayout(layout);

        cbIsKnown = new javax.swing.JCheckBox();
        add(cbIsKnown, new CC().alignX("left").spanX().wrap());
        cbIsKnown.setFont(new java.awt.Font("DejaVu Sans", 2, 10));
        org.openide.awt.Mnemonics.setLocalizedText(cbIsKnown, org.openide.util.NbBundle.getMessage(AEventBean.class, "AEventBean.cbIsKnown.text")); // NOI18N
        cbIsKnown.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showOrHide();
                changeSupport.fireChangeEvent();
            }
        });

        // Event TYPE
        typeLabel = BeanHelper.createTagLabel("TYPE");
        evtType = new ancestris.modules.beans.AChoiceBean();
        evtType.addChangeListener(changeSupport);
        add(typeLabel);
        add(evtType, new CC().growX().wrap());

        // Date
        aDateBean1 = new ancestris.modules.beans.ADateBean();
        aDateBean1.addChangeListener(changeSupport);
        add(aDateBean1, new CC().skip().split(3).growX(0));
        // AGE
        ageLabel = BeanHelper.createTagLabel("AGE");
        evtAge = new ancestris.modules.beans.ASimpleBean();
        evtAge.addChangeListener(changeSupport);
        add(ageLabel, new CC().growX(0));
        add(evtAge, new CC().wrap());

        // PLACE
        placeLabel = BeanHelper.createTagLabel("PLAC");
        aPlaceBean1 = new ancestris.modules.beans.APlaceBean();
        aPlaceBean1.addChangeListener(changeSupport);
        add(placeLabel);
        add(aPlaceBean1, new CC().wrap());

        // Address
        evtAddr = new ancestris.modules.beans.AAddrBean();
        evtAddr.addChangeListener(changeSupport);

        agncLabel = BeanHelper.createTagLabel("AGNC");
        evtAgency = new ancestris.modules.beans.ASimpleBean();
        evtAgency.addChangeListener(changeSupport);
        add(agncLabel);
        add(evtAgency, new CC().wrap());

        causLabel = BeanHelper.createTagLabel("CAUS");
        evtCause = new ancestris.modules.beans.ASimpleBean();
        evtCause.addChangeListener(changeSupport);
        add(causLabel);
        add(evtCause, new CC().wrap());

        jLabel2 = new javax.swing.JLabel();


        cbIsKnown.setSelected(false);
        showOrHide();
    }
    private boolean showKnown = false;

    /**
     * Set the value of showCheck
     *
     * @param showKnown new value of showCheck
     */
    public void setShowKnown(boolean showKnown) {
        this.showKnown = showKnown;
        showOrHide();
    }

    /**
     * commit beans - transaction has to be running already
     */
    @Override
    protected void commitImpl(Property property) throws GedcomException {
        aDateBean1.commit();
        aPlaceBean1.commit();
        evtAddr.commit();
        evtAge.commit();
        evtAgency.commit();
        evtCause.commit();
        evtType.commit();
    }

    private void showOrHide() {
        if (showKnown) {
            boolean checked = cbIsKnown.isSelected();
            aDateBean1.setVisible(checked);
            placeLabel.setVisible(checked);
            aPlaceBean1.setVisible(checked);
            cbIsKnown.setVisible(true);

            evtAddr.setVisible(checked);
            typeLabel.setVisible(checked);
            evtType.setVisible(checked);

            evtAge.setVisible(checked);
            ageLabel.setVisible(checked);
            evtAgency.setVisible(checked);
            agncLabel.setVisible(checked);
            evtCause.setVisible(checked);
            causLabel.setVisible(checked);

//            if (checked) {
//                cbIsKnown.setText(null);
//            } else {
//                org.openide.awt.Mnemonics.setLocalizedText(cbIsKnown, org.openide.util.NbBundle.getMessage(AEventBean.class, "AEventBean.cbIsKnown.text")); // NOI18N
//            }
        } else {
            cbIsKnown.setVisible(false);
//            cbIsKnown.setSelected(true);
        }
        boolean showDetails = isDetailedView();

        evtAddr.setVisible(showDetails);

        typeLabel.setVisible(showDetails);
        evtType.setVisible(showDetails);

        evtAge.setVisible(showDetails);
        ageLabel.setVisible(showDetails);
        evtAgency.setVisible(showDetails);
        agncLabel.setVisible(showDetails);
        evtCause.setVisible(showDetails);
        causLabel.setVisible(showDetails);

        revalidate();
    }
}
