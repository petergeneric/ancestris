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

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertySex;
import genj.gedcom.time.PointInTime;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkerMulti extends Worker {

    private static int nullDate = -999999;
    private static int minDate = -999999;
    private static int maxDate = +999999;
    
    private String lastnameText;
    private String firstnameText;
    
    private int birthFrom;
    private int birthTo;

    private int deathFrom;
    private int deathTo;

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
    public void start(Gedcom gedcom, Object... args) {
        lastnameText = (String) args[0];
        firstnameText = (String) args[1];
        birthFrom = getDate(args[2], true);
        birthTo = getDate(args[3], false);
        deathFrom = getDate(args[4], true);
        deathTo = getDate(args[5], false);
        placeText = (String) args[6];
        isMale = (Boolean) args[7];
        isFemale = (Boolean) args[8];
        isUnknown = (Boolean) args[9];
        isMarried = (Boolean) args[10];
        isSingle = (Boolean) args[11];
        
        // sync up
        synchronized (lock) {

            // bail if already running
            if (thread != null) {
                throw new IllegalStateException("can't start while running");
            }

            // prepare matcher & path
            this.gedcom = gedcom;
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
            } else {
                System.out.println("DEBUG************* indi = "+indi);
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
        if (indi.getId().equals("I2472")) {
            String str = "ok";
        }
        
        return (isCommonString(indi.getLastName(), lastnameText)
                && isCommonString(indi.getFirstName(), firstnameText)
                && isCommonDate(getDate(indi.getBirthDate()), birthFrom, birthTo)
                && isCommonDate(getDate(indi.getDeathDate()), deathFrom, deathTo)
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

    private int getDate(Object arg, boolean isMin) {
        String dateStr = (String) arg;
        try {
            int date = Integer.parseInt(dateStr);
            return date;
        } catch (Exception e) {
        }
        return isMin ? minDate : maxDate;
    }

    private int getDate(PropertyDate pDate) {
        if (pDate == null) {
            return nullDate;
        }
        PointInTime pit = pDate.getStart();
        if (pit == null || !pit.isValid()) {
            return nullDate;
        }
        return pit.getYear();
    }

    private boolean isCommonString(String name, String nameText) {
        return name.toLowerCase().contains(nameText.toLowerCase());
    }

    
    private boolean isCommonDate(int date, int from, int to) {
        return date >= from && date <= to;
    }

    private boolean isCommonPlace(Indi indi, String placeText) {
        if (placeText.isEmpty()) {
            return true;
        }
        placeText = placeText.toLowerCase();
        for (PropertyPlace prop : indi.getProperties(PropertyPlace.class)) {
            String place = prop.getDisplayValue();
            if (place.toLowerCase().contains(placeText)) {
                return true;
            }
        }
        
        Fam fams[] = indi.getFamiliesWhereSpouse();
        for (Fam fam : fams) {
            for (PropertyPlace prop : fam.getProperties(PropertyPlace.class)) {
                String place = prop.getDisplayValue();
                if (place.toLowerCase().contains(placeText)) {
                    return true;
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
        if (hitCount >= MAX_HITS) {
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
        return (lastnameText.isEmpty() && firstnameText.isEmpty() && birthFrom == minDate && birthTo == maxDate 
                && deathFrom == minDate && deathTo == maxDate && placeText.isEmpty() && !isMale && !isFemale && !isUnknown && !isMarried && !isSingle);
    }


    
}
