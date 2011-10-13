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
package genj.edit.beans;

import genj.edit.Options;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.util.swing.Action2;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.DialogHelper;
import genj.util.swing.TextFieldWidget;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.openide.util.NbBundle;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
public class FullNameBean extends PropertyBean {

    /** our components */
    private Property[] sameLastNames = new Property[0];
    private ChoiceWidget cLast, cFirst;
    private JCheckBox cAll;
    private TextFieldWidget tSuff, tNick;
    // Extended Panel
    private javax.swing.JPanel extPanel;
    private TextFieldWidget tNPfx, tSPfx;

    /**
     * Calculate message for replace all last names
     */
    private String getReplaceAllMsg() {
        if (sameLastNames.length < 2) {
            return null;
        }
        // we're using getDisplayValue() here
        // because like in PropertyRelationship's case there might be more
        // in the gedcom value than what we want to display (witness@INDI:BIRT)
        return RESOURCES.getString("choice.global.confirm", "" + sameLastNames.length, ((PropertyName) getProperty()).getLastName(), cLast.getText());
    }

    public FullNameBean() {
        MigLayout layout = new MigLayout(
                new LC().fillX().hideMode(2),
                new AC().align("right").gap("rel").grow().fill());

        setLayout(layout);

        cLast = new ChoiceWidget();
        cLast.addChangeListener(changeSupport);
        cLast.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                String msg = getReplaceAllMsg();
                if (msg != null) {
                    cAll.setVisible(true);
                    cAll.setToolTipText(msg);
                }
            }
        });
        cLast.setIgnoreCase(true);

        cFirst = new ChoiceWidget();
        cFirst.addChangeListener(changeSupport);
        cFirst.setIgnoreCase(true);

        tSuff = new TextFieldWidget("", 10);
        tSuff.addChangeListener(changeSupport);

        tNick = new TextFieldWidget("", 10);
        tNick.addChangeListener(changeSupport);

        tNPfx = new TextFieldWidget("", 10);
        tNPfx.addChangeListener(changeSupport);

        tSPfx = new TextFieldWidget("", 10);
        tSPfx.addChangeListener(changeSupport);

        cAll = new JCheckBox();
        cAll.setBorder(new EmptyBorder(1, 1, 1, 1));
        cAll.setVisible(false);
        cAll.setRequestFocusEnabled(false);
        // listen to selection of global and ask for confirmation
        cAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String msg = getReplaceAllMsg();
                if (msg != null && cAll.isSelected()) {
                    int rc = DialogHelper.openDialog(RESOURCES.getString("choice.global.enable"), DialogHelper.QUESTION_MESSAGE, msg, Action2.yesNo(), FullNameBean.this);
                    cAll.setSelected(rc == 0);
                }
            }
        });

        // Layout the bean
        add(BeanHelper.createTagLabel(this, "SURN", "", 0));
        add(cLast, new CC().split().growX().gapRight("0"));
        add(cAll, new CC().wrap());

        add(BeanHelper.createTagLabel(this, "GIVN", "", 0));
        add(cFirst, new CC().wrap());

//        add(new JLabel(PropertyName.getLabelForSuffix()));
//        add(tSuff, new CC().wrap());
//
        extPanel = new JPanel(new MigLayout(
                new LC().fillX(),
                new AC().align("right").gap("rel").grow().fill()));

        extPanel.add(BeanHelper.createTagLabel(this, "NICK", "", 10));
        extPanel.add(tNick, new CC().wrap());

        extPanel.add(BeanHelper.createTagLabel(this, "NPFX", "", 10));
        extPanel.add(tNPfx, new CC().wrap());

        extPanel.add(BeanHelper.createTagLabel(this, "SPFX", "", 10));
        extPanel.add(tSPfx, new CC().wrap());

        extPanel.add(BeanHelper.createTagLabel(this, "NSFX", "", 10));
        extPanel.add(tSuff, new CC().wrap());

        final JCheckBox showCb = BeanHelper.createShowHide(
                NbBundle.getMessage(FullNameBean.class, "FullNameBean.showCb.text") // NOI18N
                , NbBundle.getMessage(FullNameBean.class, "FullNameBean.showCb.hint") // NOI18N
                , extPanel);
        extPanel.setVisible(showCb.isSelected());


        add(showCb, new CC().spanX().alignX("left").wrap());
        add(extPanel, new CC().grow().spanX().gapBefore("20").wrap());


        // we're done aside from declaring the default focus
        defaultFocus = cFirst;

    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    @SuppressWarnings("fallthrough")
    protected void commitImpl(Property property) {

        PropertyName p = (PropertyName) property;

        // ... calc texts
        String first = cFirst.getText().trim();
        String last = cLast.getText().trim();
        String suff = tSuff.getText().trim();
        String nick = tNick.getText().trim();

        Gedcom ged = p.getGedcom();
        if (ged != null) {
            switch (Options.getInstance().correctName) {
                // John DOE
                case 2:
                    last = last.toUpperCase(ged.getLocale());
                    cLast.setText(last);
                // John Doe
                case 1:
                    if (first.length() > 0) {
                        first = Character.toString(first.charAt(0)).toUpperCase(ged.getLocale()) + first.substring(1);
                        cFirst.setText(first);
                    }
            }
        }

        // ... store changed value
        p.setName(
                tNPfx.getText().trim(),
                first,
                tSPfx.getText().trim(),
                last, suff, cAll.isSelected());
        p.setNick(nick);

        // start fresh
        setPropertyImpl(p);

        // Done
    }

    /**
     * Set context to edit
     */
    public void setPropertyImpl(Property prop) {

        PropertyName name = (PropertyName) prop;
        if (name == null) {
            sameLastNames = new Property[0];
            cLast.setValues(PropertyName.getLastNames(getRoot().getGedcom(), true));
            cLast.setText("");
            cFirst.setValues(PropertyName.getFirstNames(getRoot().getGedcom(), true));
            cFirst.setText("");
            tSuff.setText("");
            tNick.setText("");
            tNPfx.setText("");
            tSPfx.setText("");
        } else {
            // keep track of who has the same last name
            sameLastNames = name.getSameLastNames();
            // first, last, suff
            cLast.setValues(name.getLastNames(true));
            cLast.setText(name.getLastName());
            cFirst.setValues(name.getFirstNames(true));
            cFirst.setText(name.getFirstName());
            tSuff.setText(name.getSuffix());

            tNick.setText(name.getNick());
            tNPfx.setText(name.getNamePrefix());
            tSPfx.setText(name.getSurnamePrefix());

        }

        cAll.setVisible(false);
        cAll.setSelected(false);

        // done
    }
} //ProxyName

