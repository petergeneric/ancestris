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
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyDate.Format;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.time.PointInTime;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkerMulti extends Worker {

    private static PointInTime minPIT = new PointInTime(1, 1, -999999);
    private static PointInTime maxPIT = new PointInTime(1, 1, +999999);
    
    private String lastnameText;
    private String firstnameText;
    
    private DateBean birthDateBean;
    private DateBean deathDateBean;

    private String placeText;

    private boolean isMale;
    private boolean isFemale;
    private boolean isUnknown;

    private boolean isMarried;
    private boolean isSingle;

    public WorkerMulti(WorkerListener listener) {
        super(listener);
    }

    @Override
    public void start(Gedcom gedcom, int max_hits, boolean case_sensitive, Object... args) {
        lastnameText = (String) args[0];
        firstnameText = (String) args[1];
        birthDateBean = (DateBean) args[2];
        deathDateBean = (DateBean) args[3];
        placeText = (String) args[4];
        isMale = (Boolean) args[5];
        isFemale = (Boolean) args[6];
        isUnknown = (Boolean) args[7];
        isMarried = (Boolean) args[8];
        isSingle = (Boolean) args[9];
        
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

            thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        WorkerMulti.this.listener.started();
                        search(WorkerMulti.this.gedcom);
                        flush();
                    } catch (Throwable t) {
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
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }


    @Override
    public void search(Entity entity, Property prop) {
        
        if (isEmptyCriteria()) {
            return;
        }
        
        if (!(entity instanceof Indi) && !(entity instanceof Fam)) {
            return;
        }
        
        // Get elements when entity corresponds
        if (entity instanceof Indi) {
            Indi indi = (Indi) entity;
            if (isMatch(indi)) {
                addHit(indi);
//            } else {
//                System.out.println("DEBUG********** indi="+indi);
            }
        }
//        if (entity instanceof Fam) {
//            Fam fam = (Fam) entity;
//            Indi husb = fam.getHusband();
//            Indi wife = fam.getWife();
//            if (isMatch(fam, husb, wife)) {
//                addHit(fam);
//                return;
//            }
//            if (husb != null && isMatch(husb)) {
//                addHit(fam);
//                return;
//            }
//            if (wife != null && isMatch(wife)) {
//                addHit(fam);
//            }
//        }
    }
    
    private boolean isMatch(Indi indi) {
        if (indi.getId().equals("I2111")) {
            String str= "";
        }
        return (isCommonString(indi.getLastNames(), lastnameText)
                && isCommonString(indi.getFirstNames(), firstnameText)
                && isCommonDate(indi.getBirthDate(), birthDateBean)
                && isCommonDate(indi.getDeathDate(), deathDateBean)
                && isCommonPlace(indi, placeText)
                && isSameSex(indi.getSex(), isMale, isFemale, isUnknown)
                && isSameStatus(indi.getFamiliesWhereSpouse(), isMarried, isSingle));
    }

//    private boolean isMatch(Fam fam, Indi husb, Indi wife) {
//        if (isSingle) {
//            return false;
//        }
//        String name = fam.toString(false);
//        boolean ret = isCommonString(name, lastnameText) && isCommonString(name, firstnameText); 
//        if (birthFrom != minDate || deathTo != maxDate) {
//            ret &= isCommonDate(getDate(fam.getMarriageDate()), birthFrom, deathTo);
//            if (!placeText.isEmpty()) {
//                return ret && isCommonPlace(fam, placeText);
//            } else {
//                return ret;
//            }
//        } else {
//            if (!placeText.isEmpty()) {
//                return ret && isCommonPlace(fam, placeText);
//            } else {
//                return false;
//            }
//        }
//    }


    private boolean isCommonString(String[] names, String nameText) {
        for (String name : names) {
            if (case_sensitive) {
                if (name.contains(nameText)) {
                    return true;
                }
            } else {
                if (name.toLowerCase().contains(nameText.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    
    /**
     * Compares dateFound with datebean
     * @param dateFound
     * @param dateBean
     * @return 
     * true if dateFound is between date bits boundaries (if range) or matches date bits if not range
     */
    private boolean isCommonDate(PropertyDate dateFound, DateBean dateBean) {
        
        // Determines boundaries of dates criteria
        Integer dFrom = dateBean.getFromDay();
        Integer mFrom = dateBean.getFromMonth();
        Integer yFrom = dateBean.getFromYear();
        if (dFrom != null && dFrom == PointInTime.UNKNOWN) {
            dFrom = null;
        }
        if (mFrom != null && mFrom == PointInTime.UNKNOWN) {
            mFrom = null;
        }
        if (yFrom != null && yFrom == PointInTime.UNKNOWN) {
            yFrom = null;
        }
        Integer dTo = dateBean.getToDay();
        Integer mTo = dateBean.getToMonth();
        Integer yTo = dateBean.getToYear();
        if (dTo != null && dTo == PointInTime.UNKNOWN) {
            dTo = null;
        }
        if (mTo != null && mTo == PointInTime.UNKNOWN) {
            mTo = null;
        }
        if (yTo != null && yTo == PointInTime.UNKNOWN) {
            yTo = null;
        }
        
        // Return if no criteria entered
        if (dFrom == null && mFrom == null && yFrom == null && dTo == null && mTo == null && yTo == null) {
            return true;
        }
        
        // Swap From and To in case of Range for certain ranges
        if (isToBeSwapped(dateBean.getFormat())) {
            dTo = dFrom;
            mTo = mFrom;
            yTo = yFrom;
            dFrom = null;
            mFrom = null;
            yFrom = null;
        }
        
        // Complete ranges in case of ranges
        if (isRange(dateBean.getFormat())) {
            if (dateFound == null) {
                if (dFrom != null || mFrom != null || yFrom != null || dTo != null || mTo != null || yTo != null) { // date found is null and criteria not empty, return false
                    return false;
                } else { // date found is null but no criteria has been indicated, return true
                    return true;
                }
            }
            if (dFrom == null) {
                dFrom = 0;
            }
            if (dTo == null) {
                dTo = 31;
            }
            if (mFrom == null) {
                mFrom = 0;
            }
            if (mTo == null) {
                mTo = 12;
            }
            if (yFrom == null) {
                yFrom = -99999999;
            }
            if (yTo == null) {
                yTo = +99999999;
            }
        } else { // date criteria is precise
            if (dateFound == null) { 
                if (dFrom != null || mFrom != null || yFrom != null) { // date found is null and criteria not empty, return false
                    return false;
                } else { // date found is null but no criteria has been indicated, return true
                    return true;
                }
            }
        }
        
        
        // Determines boundaries of dates found
        Integer d1 = dateFound.getStart() != null ? dateFound.getStart().getDay() : null;
        Integer m1 = dateFound.getStart() != null ? dateFound.getStart().getMonth() : null;
        Integer y1 = dateFound.getStart() != null ? dateFound.getStart().getYear() : null;
        if (d1 != null && d1 == PointInTime.UNKNOWN) {
            d1 = null;
        }
        if (m1 != null && m1 == PointInTime.UNKNOWN) {
            m1 = null;
        }
        if (y1 != null && y1 == PointInTime.UNKNOWN) {
            y1 = null;
        }
        Integer d2 = dateFound.getEnd() != null ? dateFound.getEnd().getDay() : null;
        Integer m2 = dateFound.getEnd() != null ? dateFound.getEnd().getMonth() : null;
        Integer y2 = dateFound.getEnd() != null ? dateFound.getEnd().getYear() : null;
        if (d2 != null && d2 == PointInTime.UNKNOWN) {
            d2 = null;
        }
        if (m2 != null && m2 == PointInTime.UNKNOWN) {
            m2 = null;
        }
        if (y2 != null && y2 == PointInTime.UNKNOWN) {
            y2 = null;
        }

        // Swap From and To in case of Range for certain ranges
        if (isToBeSwapped(dateFound.getFormat())) {
            d2 = d1;
            m2 = m1;
            y2 = y1;
            d1 = null;
            m1 = null;
            y1 = null;
        }
        
        // Complete ranges in case of ranges
        if (isRange(dateFound.getFormat())) {
            if (d1 == null) {
                d1 = 0;
            }
            if (d2 == null) {
                d2 = 31;
            }
            if (m1 == null) {
                m1 = 0;
            }
            if (m2 == null) {
                m2 = 12;
            }
            if (y1 == null) {
                y1 = -99999999;
            }
            if (y2 == null) {
                y2 = +99999999;
            }
        }
        
        
        // Results to be tested
        boolean bDay = false;
        boolean bMonth = false;
        boolean bYear = false;

        // Search between date bits in case criteria is a date range
        if (isRange(dateBean.getFormat())) {
            if (isRange(dateFound.getFormat())) {
                bDay = (dFrom >= d1 && dFrom <= d2) || (d1 >= dFrom && d1 <= dTo);
                bMonth = (mFrom >= m1 && mFrom <= m2) || (m1 >= mFrom && m1 <= mTo);
                bYear = (yFrom >= y1 && yFrom <= y2) || (y1 >= yFrom && y1 <= yTo);
            } else {
                bDay = (d1 != null && d1 >= dFrom && d1 <= dTo);
                bMonth = (m1 != null && m1 >= mFrom && m1 <= mTo);
                bYear = (y1 != null && y1 >= yFrom && y1 <= yTo);
                if (d1 == null && m1 == null && y1 == null && (dFrom != null || mFrom != null || yFrom != null || dTo != null || mTo != null || yTo != null)) {
                    bDay = false;
                }
            }
        } 
        // Search for exact criteria in case criteria is NOT a date range
        else {
            if (isRange(dateFound.getFormat())) {
                return false;
            } else {
                bDay = (dFrom == null) || (d1 != null && dFrom.compareTo(d1) == 0);
                bMonth = (mFrom == null) || (m1 != null && mFrom.compareTo(m1) == 0);
                bYear = (yFrom == null) || (y1 != null && yFrom.compareTo(y1) == 0);
                if (d1 == null && m1 == null && y1 == null && (dFrom != null || mFrom != null || yFrom != null)) {
                    bDay = false;
                }
            }
        }
        return bDay && bMonth && bYear;
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
                if (place.toLowerCase().contains(placeText.toLowerCase())) {
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
                    if (place.toLowerCase().contains(placeText.toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    private boolean isSameSex(int sex, boolean male, boolean female, boolean unknown) {
        if (!male && !female && !unknown) {
            return false;
        }
        return (sex == PropertySex.MALE && male) || (sex == PropertySex.FEMALE && female) || (sex == PropertySex.UNKNOWN && unknown);
    }

    private boolean isSameStatus(Fam[] familiesWhereSpouse, boolean married, boolean single) {
        if (!married && !single) {
            return false;
        }
        return (married && (familiesWhereSpouse != null && familiesWhereSpouse.length != 0)) || (single && (familiesWhereSpouse == null || familiesWhereSpouse.length == 0));
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
//                && birthFrom == minDate && birthTo == maxDate 
//                && deathFrom == minDate && deathTo == maxDate 
                && placeText.isEmpty() && !isMale && !isFemale && !isUnknown && !isMarried && !isSingle);
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
