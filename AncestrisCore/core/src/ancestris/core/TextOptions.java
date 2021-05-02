/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011-16 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.core;

import ancestris.util.Utilities;
import genj.gedcom.Gedcom;
import genj.util.AncestrisPreferences;
import genj.util.Registry;
import java.util.Locale;


/**
 * Options for report package and various text output
 */
public class TextOptions {

    /** 'singleton' instance */
    private static AncestrisPreferences textOptions;

    private TextOptions() {
        //XXX: preference path must be defined in core options namespace
        textOptions = Registry.get(TextOptions.class);
    }

    public static TextOptions getInstance() {
        return OptionsHolder.INSTANCE;
    }

    private static class OptionsHolder {

        private static final TextOptions INSTANCE = new TextOptions();
    }
    
    private String trim(String symbol, String fallback) {
        if (symbol == null) {
            return fallback;
        }
        symbol = symbol.trim();
        int len = symbol.length();
        if (symbol.length() == 0) {
            return fallback;
        }
        if (!Character.isLetter(symbol.charAt(len - 1))) {
            return symbol;
        }
        return symbol;
    }

    public int getIndentPerLevel() {
        return textOptions.get("indentPerLevel",2);
    }

    public void setIndentPerLevel(int set) {
        textOptions.put("indentPerLevel", Math.max(2, set));
    }

    public int getPositions() {
        return textOptions.get("positions",5);
    }

    public void setPositions(int set) {
        textOptions.put("positions", Math.max(0, set));
    }

    public String getBirthSymbol() {
        return textOptions.get("birthSymbol","°");
    }

    public void setBirthSymbol(String set) {
        textOptions.put("birthSymbol", trim(set, "°"));
    }

    public String getBaptismSymbol() {
        return textOptions.get("baptismSymbol","~");
    }

    public void setBaptismSymbol(String set) {
        textOptions.put("baptismSymbol", trim(set, "~"));
    }

    public String getEngagingSymbol() {
        return textOptions.get("engagingSymbol","o");
    }

    public void setEngagingSymbol(String set) {
        textOptions.put("engagingSymbol", trim(set, "o"));
    }

    public String getMarriageSymbol() {
        return textOptions.get("marriageSymbol","x");
    }

    public void setMarriageSymbol(String set) {
        textOptions.put("marriageSymbol", trim(set, "x"));
    }

    public String getDivorceSymbol() {
        return textOptions.get("divorceSymbol","o|o");
    }

    public void setDivorceSymbol(String set) {
        textOptions.put("divorceSymbol", trim(set, "o|o"));
    }

    public String getDeathSymbol() {
        return textOptions.get("deathSymbol","+");
    }

    public void setDeathSymbol(String set) {
        textOptions.put("deathSymbol", trim(set, "+"));
    }

    public String getBurialSymbol() {
        return textOptions.get("burialSymbol","[]");
    }

    public void setBurialSymbol(String set) {
        textOptions.put("burialSymbol", trim(set, "[]"));
    }

    public String getOccuSymbol() {
        return textOptions.get("occuSymbol","=");
    }

    public void setOccuSymbol(String set) {
        textOptions.put("occuSymbol", trim(set, "="));
    }

    public String getResiSymbol() {
        return textOptions.get("resiSymbol","^");
    }

    public void setResiSymbol(String set) {
        textOptions.put("resiSymbol", trim(set, "^"));
    }

    public String getChildOfSymbol() {
        return textOptions.get("childOfSymbol","/");
    }

    public void setChildOfSymbol(String set) {
        textOptions.put("childOfSymbol", trim(set, "/"));
    }

    public String getSymbol(String tag) {
        if ("BIRT".equals(tag)) {
            return getBirthSymbol();
        }
        if ("BAPM".equals(tag) || "CHR".equals(tag)) {
            return getBaptismSymbol();
        }
        if ("ENGA".equals(tag)) {
            return getEngagingSymbol();
        }
        if ("MARR".equals(tag)) {
            return getMarriageSymbol();
        }
        if ("DIV".equals(tag)) {
            return getDivorceSymbol();
        }
        if ("DEAT".equals(tag)) {
            return getDeathSymbol();
        }
        if ("BURI".equals(tag)) {
            return getBurialSymbol();
        }
        if ("OCCU".equals(tag)) {
            return getOccuSymbol();
        }
        if ("RESI".equals(tag)) {
            return getResiSymbol();
        }
        if ("FAMC".equals(tag)) {
            return getChildOfSymbol();
        }

        return Gedcom.getName(tag);
    }
    /**
     * get and set locale for outputs (reports, ...)
     */
    public Locale getOutputLocale() {
        return getOutputLocale(Locale.getDefault());
    }

    public Locale getOutputLocale(Locale defaultLocale) {
        String outLocale = textOptions.get("locale.output", (String) null);
        return outLocale == null ? defaultLocale : Utilities.getLocaleFromString(outLocale);
    }

    public void setOutputLocale(Locale outLoc) {
        if (outLoc == null) {
            textOptions.put("locale.output", (String) null);
        } else {
            textOptions.put("locale.output", outLoc.toString());
        }
    }
    
    public boolean isUseChr() {
        return textOptions.get("useCHR", false);
    }
    
    public void setUseChr(boolean value) {
        textOptions.put("useCHR", value);
    }
} 
