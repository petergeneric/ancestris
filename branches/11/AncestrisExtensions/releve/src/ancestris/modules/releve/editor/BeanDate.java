package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldDate;
import genj.gedcom.GedcomException;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import genj.util.swing.DateWidget;
import genj.util.swing.NestedBlockLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.openide.util.Exceptions;



/**
 * Remarque : remplace genj.edit.beans.SimpleValueBean a laquelle il manque
 * dans le constructeur "defaultFocus = tfield;"
 * @author Michel
 */
public class BeanDate extends Bean {

    //private final static ImageIcon PIT = new ImageIcon(PropertyBean.class, "/genj/gedcom/images/Time");
    private final static NestedBlockLayout H = new NestedBlockLayout("<row><choose/><date1/><label2/><date2/><phrase/></row>");
    private final static NestedBlockLayout V = new NestedBlockLayout("<table><row><choose/><date1/></row><row><label2/><date2/></row><row><phrase cols=\"2\"/></row></table>");
    private DateWidget dateWidget;
    private Calendar preferedCalendar = PointInTime.GREGORIAN;


    public BeanDate() {
        setLayout(V.copy());
        setAlignmentX(0);

        dateWidget = new DateWidget();
        dateWidget.addChangeListener(changeSupport);
        add(dateWidget);

        setPreferedCalendar(PointInTime.GREGORIAN, PointInTime.FRENCHR);

        // setup default focus
        defaultFocus = dateWidget;

        // je configure le raccourci des touches de direction haut et bas pour increment ou decrementer la date d'un jour
        if ( dateWidget.getComponent(1)  instanceof JTextField) {
            JTextField jTextFieldDay = (JTextField) dateWidget.getComponent(1);
            // je desactive les touches haut et pas pour supprimer l'action du scrollbar parent
            jTextFieldDay.getInputMap(JComponent.WHEN_FOCUSED).remove( KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
            jTextFieldDay.getInputMap(JComponent.WHEN_FOCUSED).remove( KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
            // j'associe les touches et les actions
            jTextFieldDay.getInputMap(JComponent.WHEN_FOCUSED).put( KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "Increase");
            jTextFieldDay.getInputMap(JComponent.WHEN_FOCUSED).put( KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "Decrease");
            // j'ajoute les nouvelles actions
            jTextFieldDay.getActionMap().put("Increase", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // j'incremente la valeur
                    dateWidget.setValue(dateWidget.getValue().add(1, 0, 0));
                }
            });

            jTextFieldDay.getActionMap().put("Decrease", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // je decremente la valeur
                    dateWidget.setValue(dateWidget.getValue().add(-1, 0, 0));
                }
            });

            // le focus sera donné au champs de saisie du jour
            defaultFocus = jTextFieldDay;
        }

    }

    public final void setPreferedCalendar(Calendar prefered, Calendar alternate) {
        preferedCalendar = prefered;
        dateWidget.setPreferedCalendar(prefered, alternate);
        //date2.setPreferedCalendar(prefered, alternate);

    }

    /**
     * Set context to edit
     */
    @Override
    public void setFieldImpl() {
        FieldDate fieldDate = (FieldDate) getField();
        PropertyDate prop = null;
        if( fieldDate != null) {
            prop = fieldDate.getPropertyDate();
        }
        PointInTime pit;
        if (prop == null) {
            pit = new PointInTime();
        } else {
            pit = prop.getStart();
            if (pit.getCalendar() != preferedCalendar) {
                try {
                    pit = pit.getPointInTime(preferedCalendar);
                } catch (GedcomException ex) {
                    // si ce n'est pas convertible, j'affiche la date avec son calendrier
                    //Exceptions.printStackTrace(ex);
                }
            }
        }
        dateWidget.setValue(pit);
    }

    @Override
    protected void replaceValueImpl(Field field) {
        FieldDate fieldDate = (FieldDate) field;
        PropertyDate prop = null;
        if( fieldDate != null) {
            prop = fieldDate.getPropertyDate();
        }
        PointInTime pit;
        if (prop == null) {
            pit = new PointInTime();
        } else {
            pit = prop.getStart();
            if (pit.getCalendar() != preferedCalendar) {
                try {
                    pit = pit.getPointInTime(preferedCalendar);
                } catch (GedcomException ex) {
                    // si ce n'est pas convertible, j'affiche la date avec son calendrier
                    //Exceptions.printStackTrace(ex);
                }
            }
        }
        dateWidget.setValue(pit);

        // je sélectionne le texte contenu dans le premier champ de la date
        if (dateWidget.getComponent(0) != null  && dateWidget.getComponent(0) instanceof JTextField) {
            ((JTextField)dateWidget.getComponent(0)).selectAll();
        }
    }


    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl() {
        String result;
        PointInTime pit = dateWidget.getValue();
        if( pit != null) {
            if( dateWidget.getCalendar() != PointInTime.GREGORIAN) {
                // je convertis la  date dans le calendrier GREGORIAN
                try {
                    pit =  pit.getPointInTime(PointInTime.GREGORIAN);
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                    return;
                }
            }

            int  day2 = pit.getDay();
            int month2 = pit.getMonth();
            int  year2 = pit.getYear();
            if ( year2 == PointInTime.UNKNOWN ) {
                result = "";
            } else if (month2 == PointInTime.UNKNOWN  ) {
                result = String.format("%04d", year2);
            } else if (day2 == PointInTime.UNKNOWN) {
                result = String.format("%02d/%04d", month2 +1 , year2);
            } else {
                result = String.format("%02d/%02d/%04d", day2 +1 , month2+1 , year2);
            }
        } else {
            result = "";
        }

        setFieldValue(result);

    }

}
