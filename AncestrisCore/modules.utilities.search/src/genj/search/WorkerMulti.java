/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.search;

import genj.edit.beans.DateBean;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyDate.Format;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import java.text.Normalizer;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkerMulti extends Worker {

    private final static int MT_EQ = 0;
    private final static int MT_GT = 1;
    private final static int MT_LT = 2;

    private String lastnameText;
    private String spouselastnameText;
    private String firstnameText;
    private String spousefirstnameText;

    private DateBean birthDateBean;
    private DateBean deathDateBean;

    private String placeText, occuText;

    private boolean isMale;
    private boolean isFemale;
    private boolean isUnknown;

    private boolean isMarried;
    private boolean isMultiMarried;
    private boolean isSingle;

    private boolean isAllBut;

    private boolean useResult1, useResult2;

    public WorkerMulti(WorkerListener listener) {
        super(listener);
    }

    @Override
    public void start(Gedcom gedcom, int max_hits, boolean case_sensitive, Set<Entity> preResult, Object... args) {
        lastnameText = (String) args[0];
        spouselastnameText = (String) args[1];
        firstnameText = (String) args[2];
        spousefirstnameText = (String) args[3];
        birthDateBean = (DateBean) args[4];
        deathDateBean = (DateBean) args[5];
        placeText = (String) args[6];
        occuText = (String) args[7];
        isMale = (Boolean) args[8];
        isFemale = (Boolean) args[9];
        isUnknown = (Boolean) args[10];
        isMarried = (Boolean) args[11];
        isMultiMarried = (Boolean) args[12];
        isSingle = (Boolean) args[13];
        isAllBut = (Boolean) args[14];

        // sync up
        synchronized (lock) {

            // bail if already running
            if (thread != null) {
                throw new IllegalStateException("can't start while running");
            }

            // prepare matcher & path
            this.gedcom = gedcom;
            this.max_hits = max_hits;
            this.case_sensitive = case_sensitive;
            this.matcher = getMatcher(!lastnameText.isEmpty() ? lastnameText : firstnameText, false);
            this.hits.clear();
            this.entities.clear();
            this.hitCount = 0;

            lock.set(true);

            thread = new Thread(() -> {
                try {
                    WorkerMulti.this.listener.started();
                    search(WorkerMulti.this.gedcom, preResult);
                    flush();
                } catch (Throwable t) {
                    t.printStackTrace();
                    Logger.getLogger("ancestris.search").log(Level.FINE, "worker bailed", t);
                } finally {
                    synchronized (lock) {
                        thread = null;
                        lock.set(false);
                        lock.notifyAll();
                    }
                    try {
                        WorkerMulti.this.listener.stopped();
                    } catch (Throwable t) {
                        // this will happen if we are being interrupted
                        // going through Spin's transition to EDT
                        // and we don't care about the result
                        Logger.getLogger("ancestris.search").log(Level.FINEST, "worker stopped", t);
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void search(Entity entity, Property prop) {

        if (!(entity instanceof Indi) && !(entity instanceof Fam)) {
            return;
        }

        // Get elements when entity corresponds
        if (entity instanceof Indi) {
            Indi indi = (Indi) entity;
            if (isEmptyCriteria()) {
                if (isAllBut) {
                    addHit(indi);
                }
                return;
            }
            if (isMatch(indi)) {
                if (!isAllBut) {
                    addHit(indi);
                }
            } else {
                if (isAllBut) {
                    addHit(indi);
                }
            }
        }
    }

    private boolean isMatch(Indi indi) {
        return (isCommonString(indi.getLastNames(), lastnameText)
                && isCommonString(indi.getPartnersLastNames(), spouselastnameText)
                && isCommonString(indi.getFirstNames(), firstnameText)
                && isCommonString(indi.getPartnersFirstNames(), spousefirstnameText)
                && isCommonDate(indi.getBirthDate(), birthDateBean)
                && isCommonDate(indi.getDeathDate(), deathDateBean)
                && isCommonPlace(indi, placeText)
                && isCommonOccupation(indi, occuText)
                && isSameSex(indi.getSex(), isMale, isFemale, isUnknown)
                && isSameStatus(indi.getFamiliesWhereSpouse(), isMarried, isMultiMarried, isSingle));
    }

    private boolean isCommonString(String[] names, String nameText) {
        if (nameText == null || nameText.isEmpty()) {
            return true;
        }
        for (String name : names) {
            if (case_sensitive) {
                if (name.contains(nameText)) {
                    return true;
                }
            } else {
                String str1 = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
                String str2 = Normalizer.normalize(nameText, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
                if (str1.contains(str2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compares dateFound with datebean taking into account their respective
     * calendar
     *
     * @param dateFound : date to be tested, spot date or range
     * @param dateBean : date criteria, spot date or range
     * @return : true if dateFound is between date bits boundaries (if range) or
     * matches date bits if not range
     *
     * => test pointInTime (pit), so convert all four dates into pits and then
     * compare
     *
     * Part 1 : Extract pits - pit1 = point in time of dateFound start date -
     * pit2 = point in time of dateFound end date - pitFrom = point in time of
     * dateBean start date - pitTo = point in time of dateBean end date Default
     * pit is (unknown day, unknown month, unknown year, gregorian) Swap certain
     * ranges to make sure pitFrom is always the start date if one is null
     *
     * Part 2 : Eliminate obvious cases - null criteria => always true - null
     * date to be tested but criteria not null => always false
     *
     * Part 3 : compare pits - Because pits can have null values for either day,
     * month or year, do not use pit compare built in function, use specific
     * function : match(pit, EQ/GT/LT, pitRef) - Comparison will have to take
     * into account non filled in bits of dates - Comparison of spot or range
     * dates are the following:
     *
     * dateFound spot | range -------------------------------------- | D | C | |
     * match(pit1, | | spot | EQ, pitFrom)| FALSE | | | | dateBean
     * -------------------------------------- | B | A | | match(pit1, |
     * match(pit1, | | GT, pitFrom)| GT, pitFrom) | | && | && | | match(pit1, |
     * match(pit1, | | LT, pitTo) | LT, pitTo) | range | | or | | (pit1 |
     * match(pitFrom, | | between | GT, pit1) | | pitFrom | && | | and |
     * match(pitFrom, | | pitTo) | LT, pit2) | | | |
     * --------------------------------------
     *
     *
     */
    private boolean isCommonDate(PropertyDate dateFound, DateBean dateBean) {

        PointInTime nullPit = new PointInTime();

        // Part 1&2 : Extract pits && Eliminate obvious cases
        //     - null criteria => always true
        //     - null date to be tested but criteria not null => always false
        // Criteria
        if (dateBean == null) {
            return true;
        }
        PointInTime pitFrom = dateBean.getFromPIT();
        PointInTime pitTo = dateBean.getToPIT();
        if (isToBeSwapped(dateBean.getFormat())) { // Swap From and To in case of Range for certain ranges
            pitTo = pitFrom;
            pitFrom = nullPit;
        }
        if (isEqual(pitFrom, nullPit) && isEqual(pitTo, nullPit)) {
            return true;
        }

        // Date to be tested
        if (dateFound == null) {
            return false;
        }
        PointInTime pit1 = dateFound.getStart();
        PointInTime pit2 = dateFound.getEnd();

        if (isToBeSwapped(dateFound.getFormat())) { // Swap From and To in case of Range for certain ranges
            pit2 = pit1;
            pit1 = nullPit;
        }
        if (isEqual(pit1, nullPit) && isEqual(pit2, nullPit)) {
            return false;
        }

        // Part 3 : Compare pits
        if (isRange(dateBean.getFormat())) {
            if (isRange(dateFound.getFormat())) {
                // A
                if (isEqual(pitFrom, nullPit)) {
                    return (match(pit1, MT_LT, pitTo) && match(pit2, MT_GT, pitTo)) || (match(pit1, MT_LT, pitTo) && match(pit2, MT_LT, pitTo));
                } else {
                    return (match(pit1, MT_GT, pitFrom) && match(pit1, MT_LT, pitTo)) || (match(pit1, MT_LT, pitFrom) && match(pit2, MT_GT, pitFrom));
                }
            } else {
                // B
                return match(pit1, MT_GT, pitFrom) && match(pit1, MT_LT, pitTo);
            }
        } else {
            if (isRange(dateFound.getFormat())) {
                // C
                return false;
            } else {
                // D
                return match(pit1, MT_EQ, pitFrom);
            }
        }
    }

    /**
     * Compares pit with pitRef for matchType (GT, LT, or EQ)
     *
     * @param pit
     * @param matchType
     * @param pitRef
     * @return true if pit EQ/LT/GT pitRef
     *
     * Because pit bits can be null, caparison has to be interpreted in
     * different ways. For instance, if day is 5, is to be tested for day 10, it
     * is lower. But if date 5th of april is to be tested against 10th of march,
     * it is larger. So the day test depends on the month if a month is
     * indicated. The full matrix of comparisons based on all 64 cases is below.
     * Legend of pitRef and pit values: 0 means field is null, 1 means it is not
     * Legend of intersection: 0 means false, 1 means true, C means compare, for
     * each case (A, B, C, etc.)
     *
     * pitRef
     *
     * year | 0 1 | | | month | 0-----------1 0-----------1 | | | | | day |
     * 0-----1 0-----1 0-----1 0-----1
     * ---------------------+----------------------------------------------------
     * year month day | +-0---+-0 | 1 0 0 0 0 0 0 0 | | | | +-1 | 1 C-A 0 0 0 0
     * 0 0 | | 0---+-1---+-0 | 1 0 C-B 0 0 0 0 0 | | pit +-1 | 1 C-A C-B C-F 0 0
     * 0 0 | 1---+-0---+-0 | 1 0 0 0 C-C 0 0 0 | | | | +-1 | 1 C-A 0 0 C-C C-D 0
     * 0 | | +-1---+-0 | 1 0 C-B 0 C-C 0 C-H 0 | | +-1 | 1 C-A C-B C-E C-C C-D
     * C-G C-I |
     *
     * Test spot X X X X X X X X range X X X X X X X X
     */
    private boolean match(PointInTime pit, int matchType, PointInTime pitRef) {
        int N = PointInTime.UNKNOWN;
        int yR = pitRef.getYear();
        int mR = pitRef.getMonth();
        int dR = pitRef.getDay();
        Calendar calR = pitRef.getCalendar();
        int y = pit.getYear();
        int m = pit.getMonth();
        int d = pit.getDay();
        Calendar cal = pit.getCalendar();

        // Convert all elements to gregorian if calendars non Gregorian 
        if (calR != PointInTime.GREGORIAN || cal != PointInTime.GREGORIAN) {
            try {
                if (calR != PointInTime.GREGORIAN) {
                    pitRef = pitRef.convertIncomplete(PointInTime.GREGORIAN);
                    yR = pitRef.getYear();
                    mR = pitRef.getMonth();
                    dR = pitRef.getDay();
                    calR = PointInTime.GREGORIAN;
                }
                if (cal != PointInTime.GREGORIAN) {
                    pit = pit.convertIncomplete(PointInTime.GREGORIAN);
                    y = pit.getYear();
                    m = pit.getMonth();
                    d = pit.getDay();
                    cal = PointInTime.GREGORIAN;
                }
                String str = "";
            } catch (GedcomException ex) {
                return false;
            }
        }

        // True cases (8)
        if (yR == N && mR == N && dR == N) {
            return true;
        }

        // False cases (7)
        if (y == N && m == N && d == N) {
            return false;
        }

        // False cases (12)
        if (yR != N && y == N) {
            return false;
        }

        // False cases (10)
        if (mR != N && m == N) {
            return false;
        }

        // False cases (8)
        if (dR != N && d == N) {
            return false;
        }

        // Comparison cases 
        // C-A (4)
        if (yR == N && mR == N && dR != N && d != N) {
            return compare(d, matchType, dR);
        }

        // C-B (4)
        if (yR == N && mR != N && dR == N && m != N) {
            return compare(m, matchType, mR);
        }

        // C-C (4)
        if (yR != N && mR == N && dR == N && y != N) {
            return compare(y, matchType, yR);
        }

        // C-D (2)
        if (yR != N && mR == N && dR != N && y != N && d != N) {
            return compare(y, matchType, yR) && compare(d, matchType, dR);
        }

        // C-E (1) and C-F (1)
        if (mR != N && dR != N) {
            if (yR == N && y != N) {
                yR = y;
                calR = cal;
            } else if (yR == N && y == N) {
                yR = 2016;
                calR = PointInTime.GREGORIAN;
                y = 2016;
                cal = PointInTime.GREGORIAN;
            }
            return compare(new PointInTime(d, m, y, cal), matchType, new PointInTime(dR, mR, yR, calR));
        }

        // C-G (1) and C-H (1)
        if (yR != N && mR != N && dR == N) {
            if (d != N) {
                dR = d;
            } else if (d == N) {
                dR = 15;
                d = 15;
            }
            return compare(new PointInTime(d, m, y, cal), matchType, new PointInTime(dR, mR, yR, calR));
        }

        // C-I (1)
        if (yR != N && mR != N && dR != N && y != N && m != N && d != N) {
            return compare(pit, matchType, pitRef);
        }

        return false;
    }

    private boolean isEqual(PointInTime pit1, PointInTime pit2) {
        return (pit1.getDay() == pit2.getDay() && pit1.getMonth() == pit2.getMonth() && pit1.getYear() == pit2.getYear() && pit1.getCalendar().getName().equals(pit2.getCalendar().getName()));
    }

    private boolean compare(int i, int matchType, int ref) {
        switch (matchType) {
            case MT_EQ:
                return i == ref;
            case MT_GT:
                return i >= ref;
            case MT_LT:
                return i <= ref;
            default:
                return false;
        }
    }

    private boolean compare(PointInTime pit, int matchType, PointInTime pitRef) {
        switch (matchType) {
            case MT_EQ:
                return pit.compareTo(pitRef) == 0;
            case MT_GT:
                return pit.compareTo(pitRef) >= 0;
            case MT_LT:
                return pit.compareTo(pitRef) <= 0;
            default:
                return false;
        }
    }

    private boolean isCommonPlace(Indi indi, String placeText) {
        if (placeText.isEmpty()) {
            return true;
        }
        for (PropertyPlace prop : indi.getProperties(PropertyPlace.class)) {
            String place = prop.getDisplayValue();
            if (case_sensitive) {
                if (place.contains(placeText)) {
                    return true;
                }
            } else {
                String str1 = Normalizer.normalize(place, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
                String str2 = Normalizer.normalize(placeText, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
                if (str1.contains(str2)) {
                    return true;
                }
            }
        }

        Fam fams[] = indi.getFamiliesWhereSpouse();
        for (Fam fam : fams) {
            for (PropertyPlace prop : fam.getProperties(PropertyPlace.class)) {
                String place = prop.getDisplayValue();
                if (case_sensitive) {
                    if (place.contains(placeText)) {
                        return true;
                    }
                } else {
                    String str1 = Normalizer.normalize(place, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
                    String str2 = Normalizer.normalize(placeText, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
                    if (str1.contains(str2)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isCommonOccupation(Indi indi, String occuText) {
        if (occuText.isEmpty()) {
            return true;
        }
        for (Property prop : indi.getAllProperties("OCCU")) {
            String occupation = prop.getDisplayValue();
            if (case_sensitive) {
                if (occupation.contains(occuText)) {
                    return true;
                }
            } else {
                String str1 = Normalizer.normalize(occupation, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
                String str2 = Normalizer.normalize(occuText, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
                if (str1.contains(str2)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isSameSex(int sex, boolean male, boolean female, boolean unknown) {
        // if none selected, assume true
        if (!male && !female && !unknown) {
            return true;
        }
        return (sex == PropertySex.MALE && male) || (sex == PropertySex.FEMALE && female) || (sex == PropertySex.UNKNOWN && unknown);
    }

    private boolean isSameStatus(Fam[] familiesWhereSpouse, boolean married, boolean multimarried, boolean single) {
        // if none selected, assume true
        if (!married && !multimarried && !single) {
            return true;
        }
        return (married && (familiesWhereSpouse != null && familiesWhereSpouse.length != 0)) || (multimarried && (familiesWhereSpouse != null && familiesWhereSpouse.length > 1)) || (single && (familiesWhereSpouse == null || familiesWhereSpouse.length == 0));
    }

    private void addHit(Entity entity) {

        Matcher.Match[] matches = matcher.match(entity.toString(true));

        // too many?
        if (hitCount >= max_hits) {
            return;
        }
        // keep entity
        entities.add(entity);
        // create a hit
        Hit hit = new Hit(entity, entity.toString(true), matches, entities.size(), true);
        // keep it
        hits.add(hit);
        hitCount++;
        // sync every 500ms
        long now = System.currentTimeMillis();
        if (now - lastFlush > 500) {
            flush();
        }
        lastFlush = now;
        // done
    }

    private boolean isEmptyCriteria() {
        return (lastnameText.isEmpty() && firstnameText.isEmpty()
                && spouselastnameText.isEmpty() && spousefirstnameText.isEmpty()
                && birthDateBean.getFromPIT().isEmpty() && birthDateBean.getToPIT().isEmpty()
                && deathDateBean.getFromPIT().isEmpty() && deathDateBean.getToPIT().isEmpty()
                && placeText.isEmpty() && !isMale && !isFemale && !isUnknown && !isMarried && !isMultiMarried && !isSingle);
    }

    //    FROM_TO = new Format("FROM", "TO"),
    //    FROM = new Format("FROM", ""),
    //    TO = new Format("TO", ""),
    //    BETWEEN_AND = new Format("BET", "AND"),
    //    BEFORE = new Format("BEF", ""),
    //    AFTER = new Format("AFT", ""),
    private boolean isRange(Format format) {
        return format.equals(PropertyDate.FROM_TO) || format.equals(PropertyDate.FROM)
                || format.equals(PropertyDate.TO) || format.equals(PropertyDate.BETWEEN_AND)
                || format.equals(PropertyDate.BEFORE) || format.equals(PropertyDate.AFTER);
    }

    private boolean isToBeSwapped(Format format) {
        return format.equals(PropertyDate.TO) || format.equals(PropertyDate.BEFORE);
    }

}
