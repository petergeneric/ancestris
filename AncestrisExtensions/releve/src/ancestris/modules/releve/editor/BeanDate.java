package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldDate;
import genj.edit.beans.PropertyBean;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import genj.util.swing.Action2;
import genj.util.swing.DateWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.PopupWidget;
import genj.util.swing.TextFieldWidget;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

/**
 * Remarque : remplace genj.edit.beans.SimpleValueBean a laquelle il manque
 * dans le constructeur "defaultFocus = tfield;"
 * @author Michel
 */
public class BeanDate extends Bean {

    private final static ImageIcon PIT = new ImageIcon(PropertyBean.class, "/genj/gedcom/images/Time");
    private final static NestedBlockLayout H = new NestedBlockLayout("<row><choose/><date1/><label2/><date2/><phrase/></row>"),
            V = new NestedBlockLayout("<table><row><choose/><date1/></row><row><label2/><date2/></row><row><phrase cols=\"2\"/></row></table>");
    /** members */
    private PropertyDate.Format format;
    private DateWidget date1, date2;
    private PopupWidget choose;
    private JLabel label2;
    private TextFieldWidget phrase;

    public BeanDate() {

        setLayout(V.copy());
        setAlignmentX(0);

        // prepare format change actions
        List<ChangeFormat> actions = new ArrayList<ChangeFormat>(10);
        for (int i = 0; i < PropertyDate.FORMATS.length; i++) {
            actions.add(new ChangeFormat(PropertyDate.FORMATS[i]));
        }

        // .. the chooser (making sure the preferred size is pre-computed to fit-it-all)
        choose = new PopupWidget();
        choose.addItems(actions);
        add(choose);

        // .. first date
        date1 = new DateWidget();
        date1.addChangeListener(changeSupport);
        add(date1);

        // .. second date
        label2 = new JLabel();
        add(label2);

        date2 = new DateWidget();
        date2.addChangeListener(changeSupport);
        add(date2);

        // phrase
        phrase = new TextFieldWidget("", 10);
        phrase.addChangeListener(changeSupport);
        add(phrase);

        // do the layout and format
        setPreferHorizontal(false);
        setFormat(PropertyDate.FORMATS[0]);
        setPreferedCalendar(PointInTime.GREGORIAN, PointInTime.FRENCHR);

        // setup default focus
        defaultFocus = date1;
    }

    public final void setPreferedCalendar(Calendar prefered, Calendar alternate) {
        date1.setPreferedCalendar(prefered, alternate);
        date2.setPreferedCalendar(prefered, alternate);
    }

    public final void setPreferHorizontal(boolean set) {

        setLayout(set ? H.copy() : V.copy());
        PropertyDate.Format f = format;
        format = null;
        setFormat(f);

        revalidate();
        repaint();
    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {

        PropertyDate prop = ((FieldDate) getField()).getPropertyDate();
        if (prop == null) {
            PointInTime pit = new PointInTime();
            date1.setValue(pit);
            date2.setValue(pit);
            phrase.setText("");
            setFormat(PropertyDate.FORMATS[0]);
        } else {
            PropertyDate date = prop;
            date1.setValue(date.getStart());
            date2.setValue(date.getEnd());
            phrase.setText(date.getPhrase());
            setFormat(date.getFormat());
        }
    }

    @Override
    protected void replaceValueImpl(Field field) {
         PropertyDate prop = ((FieldDate) field).getPropertyDate();
        if (prop == null) {
            PointInTime pit = new PointInTime();
            date1.setValue(pit);
            date2.setValue(pit);
            phrase.setText("");
            setFormat(PropertyDate.FORMATS[0]);
        } else {
            PropertyDate date = prop;
            date1.setValue(date.getStart());
            date2.setValue(date.getEnd());
            phrase.setText(date.getPhrase());
            setFormat(date.getFormat());
        }
    }


    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        PropertyDate p = ((FieldDate) getField()).getPropertyDate();

        p.setValue(format, date1.getValue(), date2.getValue(), phrase.getText());

    }

    /**
     * Setup format
     */
    private void setFormat(PropertyDate.Format set) {

        // already?
        if (format == set) {
            return;
        }

        // signal
        changeSupport.fireChangeEvent();

        // remember
        format = set;

        // prepare chooser with 1st prefix
        choose.setToolTipText(format.getName());
        String prefix1 = format.getPrefix1Name();
        choose.setIcon(prefix1 == null ? PIT : null);
        choose.setText(prefix1 == null ? "" : prefix1);

        // check label2/date2 visibility
        if (format.isRange()) {
            date2.setVisible(true);
            label2.setVisible(true);
            label2.setText(format.getPrefix2Name());
        } else {
            date2.setVisible(false);
            label2.setVisible(false);
        }

        // check phrase visibility
        phrase.setVisible(format.usesPhrase());

        // show
        revalidate();
        repaint();
    }


    /**
     * Action for format change
     */
    private class ChangeFormat extends Action2 {

        private PropertyDate.Format formatToSet;

        private ChangeFormat(PropertyDate.Format set) {
            formatToSet = set;
            super.setText(set.getName());
        }

        public void actionPerformed(ActionEvent event) {
            setFormat(formatToSet);
            date1.requestFocusInWindow();
        }
    } //ChangeFormat
}
