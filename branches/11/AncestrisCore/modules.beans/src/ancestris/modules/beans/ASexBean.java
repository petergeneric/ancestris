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

/*
 * ASexBean.java
 *
 * Created on 15 janv. 2011, 23:31:08
 */
package ancestris.modules.beans;

import genj.edit.beans.PropertyBean;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.DimConstraint;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author daniel
 */
public class ASexBean extends PropertyBean {

    public enum Dir {

        X_AXIS, Y_AXIS
    };
    private javax.swing.JRadioButton female;
    private javax.swing.JRadioButton male;
    private javax.swing.JRadioButton unknown;
    private javax.swing.ButtonGroup group;


    private LC constraint = new LC().fillX().alignX("right").hideMode(2).flowY().gridGap("0","0");

    /** Creates new form ASexBean */
    public ASexBean() {
        MigLayout yLayout = new MigLayout(constraint);

        group = new javax.swing.ButtonGroup();
        male = new javax.swing.JRadioButton();
        female = new javax.swing.JRadioButton();
        unknown = new javax.swing.JRadioButton();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));

        group.add(male);
        male.setText(org.openide.util.NbBundle.getMessage(ASexBean.class, "ASexBean.male.text")); // NOI18N
        add(male);

        group.add(female);
        female.setText(org.openide.util.NbBundle.getMessage(ASexBean.class, "ASexBean.female.text")); // NOI18N
        add(female);

        group.add(unknown);
        unknown.setText(org.openide.util.NbBundle.getMessage(ASexBean.class, "ASexBean.unknown.text")); // NOI18N
        add(unknown);

        setLayout(yLayout);

        ActionHandler handler = new ActionHandler();
        male.addActionListener(handler);
        female.addActionListener(handler);
        unknown.addActionListener(handler);
    }

    @Override
    public String getTag() {
        return "SEX";
    }

    private Dir direction = Dir.Y_AXIS;

    /**
     * Get the value of direction
     *
     * @return the value of direction
     */
    public Dir getDirection() {
        return direction;
    }

    /**
     * Set the value of direction
     *
     * @param direction new value of direction
     */
    public void setDirection(Dir direction) {
        this.direction = direction;
        switch (direction) {
            case X_AXIS:
                constraint.flowX();
                break;
            case Y_AXIS:
                constraint.flowY();
                break;
        }
        invalidate();
        repaint();
    }

    @Override
    protected void setPropertyImpl(Property prop) {
        defaultFocus = male;
        group.clearSelection();

        PropertySex sex = (PropertySex) prop;
        if (sex != null) {
            switch (sex.getSex()) {
                case PropertySex.MALE:
                    male.doClick();
                    defaultFocus = male;
                    break;
                case PropertySex.FEMALE:
                    female.doClick();
                    defaultFocus = female;
                    break;
                case PropertySex.UNKNOWN:
                    unknown.doClick();
                    defaultFocus = unknown;
                    break;
            }
        }

        // Done
    }

    @Override
    protected void commitImpl(Property property) throws GedcomException {
        PropertySex sex = (PropertySex) property;
        sex.setSex(getSex());
    }

    /**
     * Get current sex
     */
    private int getSex() {

        if (male.isSelected()) {
            return PropertySex.MALE;
        }
        if (female.isSelected()) {
            return PropertySex.FEMALE;
        }
        return PropertySex.UNKNOWN;

    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    private class ActionHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            changeSupport.fireChangeEvent();
        }
    }
}
