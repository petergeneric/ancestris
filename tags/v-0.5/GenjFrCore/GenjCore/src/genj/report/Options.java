/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Revision: 1.18 $ $Author: nmeier $ $Date: 2010-01-27 14:11:45 $
 */
package genj.report;

import genj.gedcom.Gedcom;
import genj.gedcom.PrivacyPolicy;
import genj.option.Option;
import genj.option.OptionProvider;
import genj.option.PropertyOption;

import java.util.List;

/**
 * Options for report package
 */
public class Options extends OptionProvider {
    
    /** 'singleton' instance */
    private static Options instance = new Options();
    
    /** Positions after decimal point */
    private int positions = 2;
    
    /** indent per level in reports */
    private int indentPerLevel = 5;
    
    /** birth symbol in reports */
    private String birthSymbol = "*";
    
    /** baptism symbol in reports */
    private String baptismSymbol =  "~";
    
    /** engagement symbol in reports */
    private String engagingSymbol = "o";
    
    /** marriage symbol in reports */
    private String marriageSymbol = "oo";
    
    /** divorce symbol in reports */
    private String divorceSymbol = "o|o";
    
    /** death symbol in reports */
    private String deathSymbol = "+";
    
    /** burial symbol in reports */
    private String burialSymbol = "[]";
    
    /** occupation symbol in reports */
    private String  occuSymbol = "=";

    /** residence symbol in reports */
    private String  resiSymbol = "^";
    
    /** child of symbol in reports */
    private String childOfSymbol = "/";
    
    /** tag marking private */
    public  String privateTag = "_PRIV";
    
    /** whether information pertaining to deceased people is public */
    public boolean deceasedIsPublic = true;
    
    /** number of years an event is private */
    public int yearsEventsArePrivate = 0; 
    
    private String trim(String symbol, String fallback) {
      if (symbol==null)
        return fallback;
      symbol = symbol.trim();
      int len = symbol.length();
      if (symbol.length()==0)
        return fallback;
      if (!Character.isLetter(symbol.charAt(len-1)))
        return symbol;
      return symbol + ' ';
    }
    
    public int getIndentPerLevel() {
        return indentPerLevel;
    }
    
    public void setIndentPerLevel(int set) {
        indentPerLevel = Math.max(2,set);
    }
    
    public int getPositions() {
        return positions;
    }
    
    public void setPositions(int set) {
        positions = Math.max(0,set);
    }
    
    public String getBirthSymbol() {
        return birthSymbol;
    }
    
    public void setBirthSymbol(String set) {
      birthSymbol = trim(set, "*");
    }
    
    public String getBaptismSymbol() {
        return baptismSymbol;
    }
    
    public void setBaptismSymbol(String set) {
        baptismSymbol = trim(set, "~");
    }
    
    public String getEngagingSymbol() {
        return engagingSymbol;
    }
    
    public void setEngagingSymbol(String set) {
        engagingSymbol = trim(set, "o");
    }
    
    public String getMarriageSymbol() {
        return marriageSymbol;
    }
    
    public void setMarriageSymbol(String set) {
        marriageSymbol  = trim(set, "oo");
    }
    
    public String getDivorceSymbol() {
        return divorceSymbol;
    }
    
    public void setDivorceSymbol(String set) {
        divorceSymbol = trim(set, "o|o");
    }
    
    public String getDeathSymbol() {
        return deathSymbol;
    }
    
    public void setDeathSymbol(String set) {
        deathSymbol = trim(set, "+");
    }
    
    public String getBurialSymbol() {
        return burialSymbol;
    }
    
    public void setBurialSymbol(String set) {
        burialSymbol = trim(set, "[]");
    }
    
    public String getOccuSymbol() {
		return occuSymbol;
	}

	public void setOccuSymbol(String set) {
	  occuSymbol  = trim(set, "=");
	}

	public String getResiSymbol() {
		return resiSymbol;
	}

	public void setResiSymbol(String set) {
	   	resiSymbol  = trim(set, "^");
	}

	public String getChildOfSymbol() {
        return childOfSymbol;
    }
    
    public void setChildOfSymbol(String set) {
       childOfSymbol  = trim(set, "/");
    }
    
    public String getSymbol(String tag) {
      if ("BIRT".equals(tag))
        return getBirthSymbol();
      if ("BAPM".equals(tag))
        return getBaptismSymbol();
      if ("ENGA".equals(tag))
        return getEngagingSymbol();
      if ("MARR".equals(tag))
        return getMarriageSymbol();
      if ("DIV".equals(tag))
        return getDivorceSymbol();
      if ("DEAT".equals(tag))
        return getDeathSymbol();
      if ("BURI".equals(tag))
        return getBurialSymbol();
      if ("OCCU".equals(tag))
        return getOccuSymbol();
      if ("RESI".equals(tag))
        return getResiSymbol();
      if ("FAMC".equals(tag))
        return getChildOfSymbol();
      
      return Gedcom.getName(tag);
    }
    
    /**
     * accessor - PrivacyPolicy configured by user
     */
    public PrivacyPolicy getPrivacyPolicy() {
      return new PrivacyPolicy(deceasedIsPublic, yearsEventsArePrivate, privateTag);    
    }
    
    /**
     * callback - provide options during system init
     */
    public List<? extends Option> getOptions() {
      // load report async
      new Thread(new Runnable() {
        public void run() {
          ReportLoader.getInstance();
        }
      }).start();
      
      // introspect for options
      return PropertyOption.introspect(getInstance());
    }
    
    /**
     * accessor - singleton instance
     */
    public static Options getInstance() {
        return instance;
    }
    
} //Options
