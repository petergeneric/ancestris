package ancestris.reports.calendar;

import ancestris.core.actions.AbstractAncestrisAction;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * Ancestris - http://www.ancestris.org
 *
 * Produces a calendar in iCalendar format.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 * @version 0.1
 */
@ServiceProvider(service = Report.class)
public class ReportCalendar extends Report {

    private static final TagPath PATH_INDIDEATPLAC = new TagPath("INDI:DEAT:PLAC");

    /**
     * Whether to output birthdays.
     */
    public boolean output_births = true;

    /**
     * Whether to output death anniversaries.
     */
    public boolean output_deaths = true;

    /**
     * Age a person is assumed dead. Option disabled when zero.
     */
    public int assume_dead = 100;

    /**
     * Whether to output birthdays for dead people.
     */
    public boolean dead_birthdays = false;

    /**
     * How to output wedding anniversaries.
     */
    public int anniversary = 0;
    public String[] anniversarys = {translate("both_alive"), translate("one_alive"), translate("all"), translate("none")};

    /**
     * Calendar mode: upcomin year (with numbers) or generic (without)
     */
    public int year_mode = 0;
    public String[] year_modes = {translate("upcoming"), translate("generic")};

    public int hour_mode = 0;
    public String[] hour_modes = {"HH:mm", "hh:mm a", "hh-mm a", "HH-mm", "HH.mm", "hh;mm a", "HH:mm:ss", "hh:mm:ss a", "hh-mm-ss a", "HH-mm-ss", "HH.mm.ss", "hh;mm;ss a"};

    public int date_long_mode = 0;
    public String[] date_long_modes = {"dd MMMM yyyy", "dd/MM/yyyy", "MM/dd/yyyy", "MMMM dd yyyy"};

    /**
     * Maximal number of first names to display.
     */
    public int max_names = 0;
    public String[] max_namess = {translate("nolimit"), "1", "2", "3"};

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * The report's entry point.
     */
    public void start(Gedcom gedcom) {

        File file = getFileFromUser("Choose calendar file", AbstractAncestrisAction.TXT_OK, true, "ics");
        if (file == null) {
            return;
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);) {
            outputHeader(writer);

            Collection<Indi> individuals = (Collection<Indi>) gedcom.getEntities(Gedcom.INDI);
            Collection<Fam> families = (Collection<Fam>) gedcom.getEntities(Gedcom.FAM);

            for (Indi indi : individuals) {
                if (output_births) {
                    outputBirthday(writer, indi);
                }
                if (output_deaths) {
                    outputDeathAnniv(writer, indi);
                }
                // TODO other anniversaries of individuals
            }

            for (Fam fam : families) {
                if (anniversary != 3) {
                    outputWeddingAnniv(writer, fam);
                }
                // TODO other anniversaries of families
            }
            outputFooter(writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log(translate("report_done") + " " + file.getAbsolutePath());
    }

    /**
     * Writes the iCalendar header.
     */
    private void outputHeader(Writer writer) throws IOException {
        writer.write("BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:Ancestris-ReportCalendar\r\n");
    }

    /**
     * Writes the iCalendar footer.
     */
    private void outputFooter(Writer writer) throws IOException {
        writer.write("END:VCALENDAR\r\n");
    }

    /**
     * Writes birthday event for individual.
     */
    private void outputBirthday(Writer writer, Indi indi) throws IOException {
        final String birAnniv = translate("birthday");
        Event event = getDate(indi.getBirthDate());
        if (event == null) {
            return;
        }
        event.time = getTime(indi.getBirthDate());
        event.categorie = birAnniv;

        PropertyPlace pp = indi.getBirthPlace();

        if (pp != null) {
            event.place = pp.getCity();
        }

        if (!dead_birthdays && !isAlive(indi, event.date)) {
            return;
        }

        event.summary = birAnniv + " : " + getIndiNameId(indi);
        event.description = getIndiNameId(indi) + "\\n\r\n " + birAnniv + " : " + event.count + " " + translate("years") 
                + "\\n\r\n " + translate("birth") + " : " + event.origine.format(DateTimeFormatter.ofPattern(date_long_modes[date_long_mode]))
                + getPlace(event);

        outputEvent(writer, event);
    }

    /**
     * Writes death anniversary event for individual.
     */
    private void outputDeathAnniv(Writer writer, Indi indi) throws IOException {
        String deaAnniv = translate("death_anniversary");
        Event event = getDate(indi.getDeathDate());
        if (event == null) {
            return;
        }
        event.time = getTime(indi.getDeathDate());
        event.categorie = deaAnniv;

        PropertyPlace pp = indi.getDeathPlace();
        if (pp != null) {
            event.place = pp.getCity();
        }

        event.summary = deaAnniv + " : " + getIndiNameId(indi);
        event.description = getIndiNameId(indi) + "\\n\r\n " + deaAnniv + " : " + event.count + " " + translate("years")
                + "\\n\r\n " + translate("death") + " : " + event.origine.format(DateTimeFormatter.ofPattern(date_long_modes[date_long_mode]))
                + getPlace(event);

        outputEvent(writer, event);
    }

    /**
     * Writes wedding anniversary event for family.
     */
    private void outputWeddingAnniv(Writer writer, Fam fam) throws IOException {
        String wedAnniv = translate("wedding_anniversary");
        Event event = getDate(fam.getMarriageDate());
        if (event == null) {
            return;
        }
        event.time = getTime(fam.getMarriageDate());
        event.categorie = wedAnniv;

        // Check who's alive
        Indi wife = fam.getWife();
        Indi husband = fam.getHusband();
        boolean wifeDead = false;
        boolean husbandDead = false;

        if (wife != null) {
            wifeDead = !isAlive(wife, event.date);
        }
        if (husband != null) {
            husbandDead = !isAlive(husband, event.date);
        }

        if (anniversary == 0 && (wifeDead || husbandDead)) {
            return;
        }
        if (anniversary == 1 && wifeDead && husbandDead) {
            return;
        }

        PropertyPlace pp = fam.getMarriagePlace();
        if (pp != null) {
            event.place = pp.getCity();
        }

        event.summary = wedAnniv + " : " + getFamName(fam);
        event.description = getFamName(fam) + "\\n\r\n " + wedAnniv + " : " + event.count + " " + translate("years") + "\\n\r\n "
                + translate("wedding") + " : "
                + event.origine.format(DateTimeFormatter.ofPattern(date_long_modes[date_long_mode]))
                + getPlace(event);
        outputEvent(writer, event);
    }

    /**
     * Writes an event in iCalendar format.
     */
    private void outputEvent(Writer writer, Event event) throws IOException {
        if (year_mode == 0) {
            event.summary = event.count + " " + event.summary;
        }

        event.summary = event.summary.replace(",", "\\,");

        writer.write("BEGIN:VEVENT\r\n");
        try {
            MessageDigest salt = MessageDigest.getInstance("SHA-256");
            salt.update(UUID.randomUUID().toString().getBytes("UTF-8"));
            String digest = encodeHexString(salt.digest());
            writer.write("UID:" + digest + "\r\n");
        } catch (NoSuchAlgorithmException e) {
            // Don't write UID if no algorithm
        }
        writer.write("DTSTAMP:" + LocalDateTime.now().format(DATETIME_FORMATTER) + "\r\n");
        writer.write("CATEGORIES:" + event.categorie + "\r\n");
        if (!"".equals(event.time)) {
            writer.write("DTSTART:" + DATE_FORMAT.format(event.date) + event.time + "\r\n");
            writer.write("DURATION:PT15M\r\n");
        } else {
            writer.write("DTSTART:" + DATE_FORMAT.format(event.date) + "\r\n");
        }
        writer.write("SUMMARY:" + event.summary + "\r\n");
        writer.write("DESCRIPTION:" + event.description + "\r\n");
        if (!"".equals(event.place)) {
            writer.write("LOCATION:" + event.place + "\r\n");
        }
        if (year_mode == 1) {
            // Repeat every year
            writer.write("RRULE:FREQ=YEARLY\r\n");
        }
        writer.write("END:VEVENT\r\n");
    }

    private String encodeHexString(byte[] byteArray) {
        StringBuilder hexStringBuilder = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuilder.append(byteToHex(byteArray[i]));
        }
        return hexStringBuilder.toString();
    }

    private String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    /**
     * Creates an event object for a date in the past.
     */
    private Event getDate(PropertyDate date) {
        if (date == null) {
            return null;
        }

        // If not exact date then return
        if (date.getFormat() != PropertyDate.DATE) {
            return null;
        }
        if (!date.getStart().isComplete()) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        PointInTime pit = date.getStart();

        cal.set(now.get(Calendar.YEAR), pit.getMonth(), pit.getDay() + 1);
        if (cal.before(now)) {
            cal.roll(Calendar.YEAR, true);
        }

        int count = cal.get(Calendar.YEAR) - date.getStart().getYear();

        LocalDate local = cal.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate origine = LocalDate.of(date.getStart().getYear(), date.getStart().getMonth()+1, date.getStart().getDay()+1);

        return new Event(local, origine, count);
    }

    /**
     * Get the time of a date.
     *
     * @param pdate PropertyDate
     * @return the time as string available for display in the report
     */
    private String getTime(PropertyDate pdate) {
        Property[] ptime = pdate.getParent().getProperties("_TIME");
        if (ptime.length == 0) {
            return "";
        }
        String time = "";

        try {

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(hour_modes[hour_mode]);
            LocalTime ldt = LocalTime.parse(ptime[0].getValue(), dtf);
            time = ldt.format(HOUR_FORMAT);
        } catch (DateTimeParseException e) {
            return "";
        }
        if ("".equals(time)) {
            return "";
        }

        return "T" + time;
    }

    private String getPlace(Event event) {
        if ("".equals(event.place)) {
            return "";
        }
        return " ( " + event.place + " )";
    }

    /**
     * Returns the name of a peron with their ID in parentheses.
     */
    private String getIndiNameId(Indi indi) {
        return (getIndiName(indi) + " (" + indi.getId() + ")").trim();
    }

    /**
     * Returns the name of a peron.
     */
    private String getIndiName(Indi indi) {
        return (getFirstNames(indi) + " " + indi.getLastName()).trim();
    }

    /**
     * Returns the married couple description..
     */
    private String getFamName(Fam fam) {
        Indi wife = fam.getWife();
        Indi husband = fam.getHusband();
        String id = "(" + fam.getId() + ")";

        if (wife == null && husband == null) {
            return id;
        }

        if (wife == null) {
            return getIndiName(husband) + " + " + translate("wife") + " " + id;
        }
        if (husband == null) {
            return getIndiName(wife) + " + " + translate("husband") + " " + id;
        }

        return getFirstNames(wife) + " + " + getIndiName(husband) + " " + id;
    }

    /**
     * Returns a maximum of <code>maxNames</code> given names of the given
     * individual. If <code>maxNames</code> is 0, this method returns all given
     * names.
     */
    private String getFirstNames(Indi indi) {
        String firstName = indi.getFirstName();
        if (max_names <= 0) {
            return firstName;
        }
        if (firstName.trim().equals("")) {
            return "";
        }

        String[] names = firstName.split("  *");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max_names && i < names.length; i++) {
            sb.append(names[i]).append(" ");
        }

        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Checks if a person is alive at a given date. A person may be assumed dead
     * if he/she is old enough (100 years old by default). If it is assumed a
     * person is alive if the date of birth is not known.
     */
    private boolean isAlive(Indi indi, LocalDate date) {
        if (indi.getDeathDate() != null || indi.getProperty(PATH_INDIDEATPLAC) != null) {
            return false;
        }
        if (assume_dead == 0) {
            return true;
        }

        PropertyDate birth = indi.getBirthDate();
        if (birth == null) {
            return true;
        }

        int d = date.getDayOfMonth();
        Delta delta = birth.getAnniversary(new PointInTime(d - 1, date.getMonthValue(), date.getYear()));
        if (delta == null) {
            return true;
        }
        return (delta.getYears() < assume_dead);
    }

    /**
     * Event data structure.
     */
    private static class Event {

        public LocalDate date;
        public LocalDate origine;
        public int count;
        public String categorie;
        public String summary;
        public String place = "";
        public String time = "";
        public String description = "";

        public Event(LocalDate date, LocalDate origine, int count) {
            this.date = date;
            this.origine = origine;
            this.count = count;

        }

    }
}
