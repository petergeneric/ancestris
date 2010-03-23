/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package merge;

import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Fam;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.GedcomException;
import genj.gedcom.TagPath;

import java.util.Comparator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.lang.IllegalArgumentException;  
import java.util.Vector;
import java.util.Iterator;


/**
 * GenJ - Tools
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
public class PersonFactory implements Comparator {

   static private int SIZE = 36;
   
   Person person = new Person();
        
   public PersonFactory() {
      this(new Indi());
      }
   
   public PersonFactory(Indi indi) {
      person.indi = indi;
      person.id = indi.getId();
      person.sex = indi.getSex();
      person.lastName = indi.getLastName();
      person.lastPhonex = getPhonex(person.lastName);
      person.lastNameLength = encode(person.lastName.trim(), person.lastNameCode);
      person.defLastName = person.lastName;
      person.deflnLength = person.lastNameLength;
      person.firstName = indi.getFirstName();
      person.firstNameLength = encode(person.firstName.trim(), person.firstNameCode);
      person.firstPhonex = getPhonex(person.firstName);
      
      person.yearMin = -4000;
      person.yearMax = +4000;

      // get birth date
      PropertyDate date = indi.getBirthDate();
      if ((date != null) && date.isValid() && date.getStart().isValid()) {
         person.yearMin = date.getStart().getYear();
         person.yearMax = date.isRange() ? date.getEnd().getYear() : person.yearMin;
         try {
            person.bS = date.getStart().getJulianDay();
            person.bE = date.isRange() ? (date.getEnd().getJulianDay()) : person.bS;
            } catch (GedcomException e) {
            throw new IllegalArgumentException("Birth date of "+indi.toString()+ "=" + date.toString()+" : "+e.getMessage());
            }
         if (person.bS > person.bE) person.bE = person.bS;
         person.birthInfo = true;
         }

      // get birth place
      PropertyPlace place = getBirthPlace(indi); 
      person.birth = (place == null) ? "" : place.getCity() + getCountry(place);
      person.birthLength = encode(person.birth.trim(), person.birthCode);

      // if birth date and birth place are null, use christening if it exists
      if (date == null && place == null) {
         date = getChristDate(indi);
         if ((date != null) && date.isValid() && date.getStart().isValid()) {
            person.yearMin = date.getStart().getYear();
            person.yearMax = date.isRange() ? date.getEnd().getYear() : person.yearMin;
            try {
               person.bS = date.getStart().getJulianDay();
               person.bE = date.isRange() ? (date.getEnd().getJulianDay()) : person.bS;
               } catch (GedcomException e) {
               throw new IllegalArgumentException("Birth date of "+indi.toString()+ "=" + date.toString()+" : "+e.getMessage());
               }
            if (person.bS > person.bE) person.bE = person.bS;
            person.birthInfo = true;
            }
         place = getChristPlace(indi); 
         person.birth = (place == null) ? "" : place.getCity() + getCountry(place);
         person.birthLength = encode(person.birth.trim(), person.birthCode);
         }
 
      // get death date
      date = indi.getDeathDate();
      if ((date != null) && date.isValid() && date.getStart().isValid()) {
         try {
            person.dS = date.getStart().getJulianDay();
            person.dE = date.isRange() ? (date.getEnd().getJulianDay()) : person.dS;
            } catch (GedcomException e) {
            throw new IllegalArgumentException("Death date of "+indi.toString()+ "=" + date.toString()+" : "+e.getMessage());
            }
         if (person.dS > person.dE) person.dE = person.dS;
         if (person.yearMin == -4000) {
            person.yearMin = date.getStart().getYear() - 110;
            }
         }

      // get burial date
      date = getBurialDate(indi);
      if ((date != null) && date.isValid() && date.getStart().isValid()) {
         try {
            person.buS = date.getStart().getJulianDay();
            person.buE = date.isRange() ? (date.getEnd().getJulianDay()) : person.buS;
            } catch (GedcomException e) {
            throw new IllegalArgumentException("Burial date of "+indi.toString()+ "=" + date.toString()+" : "+e.getMessage());
            }
         if (person.buS > person.buE) person.buE = person.buS;
         if (person.yearMin == -4000) {
            person.yearMin = date.getStart().getYear() - 110;
            }
         }

      // get first marriage date
      Fam[] fams = indi.getFamiliesWhereSpouse();
      Fam fam = (fams != null && fams.length > 0) ? fams[0] : null;
      date = (fam != null ? fam.getMarriageDate() : null);
      if ((date != null) && date.isValid() && date.getStart().isValid()) {
         try {
            person.mS = date.getStart().getJulianDay();
            person.mE = date.isRange() ? (date.getEnd().getJulianDay()) : person.mS;
            } catch (GedcomException e) {
            throw new IllegalArgumentException("Death date of "+indi.toString()+ "=" + date.toString()+" : "+e.getMessage());
            }
         if (person.mS > person.mE) person.mE = person.mS;
         if (person.yearMin == -4000) {
            person.yearMin = date.getStart().getYear() - 50;
            }
         }

      // fix date range
      if (person.yearMin > -4000) {
         person.yearMin -= 5 * ((Math.abs(2050) - person.yearMin) / 100) + 5 ;
         }
      if (person.yearMax < 4000) {
         person.yearMax += 5 * ((Math.abs(2050) - person.yearMax) / 100) + 5 ;
         }

      // get death place
      place = getDeathPlace(indi); 
      person.death = (place == null) ? "" : place.getCity() + getCountry(place);
      person.deathLength = encode(person.death.trim(), person.deathCode);
      
      // get burial place
      place = getBurialPlace(indi); 
      person.burial = (place == null) ? "" : place.getCity() + getCountry(place);
      person.burialLength = encode(person.burial.trim(), person.burialCode);
      
      // get first marriage place
      place = getMarriagePlace(fam); 
      person.marr = (place == null) ? "" : place.getCity() + getCountry(place);
      person.marrLength = encode(person.marr.trim(), person.marrCode);
      }
     
   public Person create() {
      return person;
      }
  

   // Get country
   public String getCountry(Property prop) {
     String ctry = "";
     if (prop instanceof PropertyPlace) {
        String[] dataBits = prop.toString().split("\\,", -1);
        ctry = dataBits[dataBits.length-1].trim();
        }
     return ctry;
     }



   static public void getRelatives(Person p, Map indi2Person) {
      Indi indi = null;
      Person person = null;

      // Get parents
      indi = p.indi.getBiologicalFather();
      if (indi != null) p.father = (Person)indi2Person.get(indi);
      indi = p.indi.getBiologicalMother();
      if (indi != null) p.mother = (Person)indi2Person.get(indi);
      
      // Get partners
      HashSet partners = new HashSet(Arrays.asList(p.indi.getPartners()));
      p.partners = new HashSet();
      for (Iterator it = partners.iterator(); it.hasNext();) {
         person = new Person();
         person = (Person)indi2Person.get((Indi)it.next());
         if (person != null)
            p.partners.add(person);
         }
      
      // Get kids
      HashSet kids = new HashSet(Arrays.asList(p.indi.getChildren()));
      p.kids = new HashSet();
      for (Iterator it = kids.iterator(); it.hasNext();) {
         person = new Person();
         person = (Person)indi2Person.get((Indi)it.next());
         if (person != null)
            p.kids.add(person);
         }
      
      // Get siblings
      HashSet siblings = new HashSet(Arrays.asList(p.indi.getSiblings(false)));
      p.siblings = new HashSet();
      for (Iterator it = siblings.iterator(); it.hasNext();) {
         person = new Person();
         person = (Person)indi2Person.get((Indi)it.next());
         if (person != null)
            p.siblings.add(person);
         }

      // Defaults lastname to father, or natural kids (well, first kid for now) or siblings from same father (well, first sibling for now)
      if (p.deflnLength == 0) {
         if (p.father != null && p.father.lastName != null) {
            person.defLastName = p.father.lastName;
            person.deflnLength = encode(person.defLastName.trim(), person.deflnCode);
            }
         else if (p.kids != null && p.kids.iterator() != null && p.kids.iterator().hasNext()) {
            person.defLastName = ((Person)(p.kids.iterator().next())).lastName;
            if (person.defLastName == null) person.defLastName = "";
            person.deflnLength = encode(person.defLastName.trim(), person.deflnCode);
            }
         else if (p.siblings != null && p.siblings.iterator() != null && p.siblings.iterator().hasNext()) {
            person.defLastName = ((Person)(p.siblings.iterator().next())).lastName;
            if (person.defLastName == null) person.defLastName = "";
            person.deflnLength = encode(person.defLastName.trim(), person.deflnCode);
            }
         }
      }
   
   static public String toString(Person p) {
      return "("+((p.indi == null)? "null" : ((Entity)p.indi).getId())+") - ["+p.yearMin+";"+p.yearMax+"]";
      }
   
   static public void copy(Person s1, Person s2) {
      s2.indi = s1.indi;
      s2.yearMin = s1.yearMin;
      s2.yearMax = s1.yearMax;
      return;
      }
   
   // sort is ascending order
   static public int compareSpans(Object o1, Object o2) {
      if ( ((Person)o1).yearMin < ((Person)o2).yearMin ) return -1;
      if (( ((Person)o1).yearMin == ((Person)o2).yearMin ) && ( ((Person)o1).yearMax < ((Person)o2).yearMax )) return -1;
      if (( ((Person)o1).yearMin == ((Person)o2).yearMin ) && ( ((Person)o1).yearMax == ((Person)o2).yearMax )) return 0;
      if (( ((Person)o1).yearMin == ((Person)o2).yearMin ) && ( ((Person)o1).yearMax >       ((Person)o2).yearMax )) return +1;
      if ( ((Person)o1).yearMin > ((Person)o2).yearMin ) return +1;
      return 0;
      }
   
   public int compare(Object o1, Object o2) {
      if ( ((Person)o1).yearMin < ((Person)o2).yearMin ) return -1;
      if (( ((Person)o1).yearMin == ((Person)o2).yearMin ) && ( ((Person)o1).yearMax < ((Person)o2).yearMax )) return -1;
      if (( ((Person)o1).yearMin == ((Person)o2).yearMin ) && ( ((Person)o1).yearMax == ((Person)o2).yearMax )) return 0;
      if (( ((Person)o1).yearMin == ((Person)o2).yearMin ) && ( ((Person)o1).yearMax >       ((Person)o2).yearMax )) return +1;
      if ( ((Person)o1).yearMin > ((Person)o2).yearMin ) return +1;
      return 0;
      }
   
   static public boolean areNotOverlapping(Object o1, Object o2) {
      return ((((Person)o1).yearMax < ((Person)o2).yearMin) || (((Person)o2).yearMax < ((Person)o1).yearMin));
      }
      
   private PropertyPlace getBirthPlace(Indi indi) {
      return (PropertyPlace)indi.getProperty(new TagPath("INDI:BIRT:PLAC"));
      }
   
   private PropertyDate getChristDate(Indi indi) {
      return (PropertyDate)indi.getProperty(new TagPath("INDI:CHR:DATE"));
      }
   
   private PropertyPlace getChristPlace(Indi indi) {
      return (PropertyPlace)indi.getProperty(new TagPath("INDI:CHR:PLAC"));
      }
   
   private PropertyPlace getDeathPlace(Indi indi) {
      return (PropertyPlace)indi.getProperty(new TagPath("INDI:DEAT:PLAC"));
      }
      
   private PropertyDate getBurialDate(Indi indi) {
      return (PropertyDate)indi.getProperty(new TagPath("INDI:BURI:DATE"));
      }
   
   private PropertyPlace getBurialPlace(Indi indi) {
      return (PropertyPlace)indi.getProperty(new TagPath("INDI:BURI:PLAC"));
      }
      
   private PropertyPlace getMarriagePlace(Fam fam) {
      if (fam == null) return null;
      return (PropertyPlace)fam.getProperty(new TagPath("FAM:MARR:PLAC"));
      }
      
   static public String display(Person p) {
      StringBuffer sb = new StringBuffer();
      
      sb.append("indi="+p.indi.toString()+"\n");
      sb.append("merged="+p.merged+"\n");
      sb.append("sex="+p.sex+"\n");
      sb.append("birth="+p.birth+"\n");
      sb.append("yearMin="+p.yearMin+"\n");
      sb.append("yearMax="+p.yearMax+"\n");
      sb.append("id="+p.id+"\n");
      sb.append("lastname="+p.lastName+"\n");
      sb.append("firstname="+p.firstName+"\n");
      sb.append("birthStart="+p.bS+"\n");
      sb.append("birthEnd="+p.bE+"\n");
      sb.append("deathStart="+p.dS+"\n");
      sb.append("deathEnd="+p.dE+"\n");
      sb.append("deathCity="+p.death+"\n");
      sb.append("burialStart="+p.buS+"\n");
      sb.append("burialEnd="+p.buE+"\n");
      sb.append("burialCity="+p.burial+"\n");
      sb.append("lastNameLength="+p.lastNameLength+"\n");
      sb.append("firstNameLength="+p.firstNameLength+"\n");
      sb.append("birthCityLength="+p.birthLength+"\n");
      sb.append("deathCityLength="+p.deathLength+"\n");
      sb.append("codelast="+codeDisplay(p.lastNameCode)+"\n");
      sb.append("codefirst="+codeDisplay(p.firstNameCode)+"\n");
      sb.append("codebirthcity="+codeDisplay(p.birthCode)+"\n");
      sb.append("codedeathcity="+codeDisplay(p.deathCode)+"\n");
      sb.append((p.father == null)? "father=null\n" : "father="+p.father.toString()+"\n");
      sb.append((p.mother == null)? "mother=null\n" : "mother="+p.mother.toString()+"\n");
      sb.append("nbPartners="+p.partners.size()+"\n");
      sb.append("nbKids="+p.kids.size()+"\n");
      sb.append("nbSiblings="+p.siblings.size()+"\n");
      return sb.toString();
      }
   
   static public int encode(String str, int[] code) {
      Vector map = initMap();      
 
      for (int i = 0; i < code.length; i++) {
         code[i] = 0;
      }
      if (str == null) return 0;

      char[] c = str.toCharArray();
      StringBuffer sb = new StringBuffer();
      // remove accents
      for (int i = 0; i < c.length; i++) {
         sb.append(((c[i] > 192) && (c[i] < 256)) ? (String)map.get(c[i]-192) : String.valueOf(c[i]));
         }
      // code in letters [A-Z] and figures [0-9]
      for (int i = 0; i < sb.length(); i++) {
         int val = 0;
         if ((sb.charAt(i) >= 48) && (sb.charAt(i) <= 57)) {
            val = sb.charAt(i) - 48 + 26;
            }
         else {   
            val = Character.getNumericValue(sb.charAt(i)) - 10;
            }
         if ((val >= 0) && (val < SIZE)) code[val]++;
         }
      // calculate length
      int length = 0;
      for (int i = 0; i < code.length; i++) {
         length += code[i];
      }
      return length;
      }
   
   static public int encode(String str, HashSet code) {

      if (str == null) return 0;

      // lowercase and remove accents
      String cleanText = getCleanedString(str.toLowerCase());

      // store words in set
      String[] words = cleanText.split(" ");
      for (int i = 0; i < words.length; i++) {
         if (words[i].length() >= 4) {
            code.add(words[i]);
            }
         }

      // calculate length
      return code.size();
      }
   
   static private String codeDisplay(int[] code) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < code.length; i++) {
         sb.append(""+code[i]+";");
         }
      return sb.toString();      
      }
   


   public static String getCleanedString(String pStringToBeCleaned) {

      Vector map = initMap();      
      StringBuffer tmp = new StringBuffer();
      char car;
        
      int i=0;
      while (i < pStringToBeCleaned.length()) {
          car = pStringToBeCleaned.charAt(i);
          if (car == ' ') {
             tmp.append(car);        
             }  
          else if (Character.isJavaIdentifierPart(car) && 
                   Character.getNumericValue(car) >= 0) {
             tmp.append(car);        
             } 
          else if (car > 192 && car < 255) {
             tmp.append( (String) (map.get(car-192)) );
             }
          else {
             tmp.append(" ");        
             }
          i++;
          }
      return tmp.toString();
      }
 
   
   private static Vector initMap() {
      Vector vector = new Vector();
      String car  = null;
       
      car = "A";
      vector.add( car );            /* '\u00C0'       alt-0192  */ 
      vector.add( car );            /* '\u00C1'       alt-0193  */
      vector.add( car );            /* '\u00C2'       alt-0194  */
      vector.add( car );            /* '\u00C3'       alt-0195  */
      vector.add( car );            /* '\u00C4'       alt-0196  */
      vector.add( car );            /* '\u00C5'       alt-0197  */
      car = "A";
      vector.add( car );            /* '\u00C6'       alt-0198  AE*/
      car = "C";
      vector.add( car );            /* '\u00C7'       alt-0199  */
      car = "E";
      vector.add( car );            /* '\u00C8'       alt-0200  */
      vector.add( car );            /* '\u00C9'       alt-0201  */
      vector.add( car );            /* '\u00CA'       alt-0202  */
      vector.add( car );            /* '\u00CB'       alt-0203  */
      car = "I";
      vector.add( car );            /* '\u00CC'       alt-0204  */
      vector.add( car );            /* '\u00CD'       alt-0205  */
      vector.add( car );            /* '\u00CE'       alt-0206  */
      vector.add( car );            /* '\u00CF'       alt-0207  */
      car = "D";
      vector.add( car );            /* '\u00D0'       alt-0208  */
      car = "N";
      vector.add( car );            /* '\u00D1'       alt-0209  */
      car = "O";
      vector.add( car );            /* '\u00D2'       alt-0210  */
      vector.add( car );            /* '\u00D3'       alt-0211  */
      vector.add( car );            /* '\u00D4'       alt-0212  */
      vector.add( car );            /* '\u00D5'       alt-0213  */
      vector.add( car );            /* '\u00D6'       alt-0214  */
      car = "*";
      vector.add( car );            /* '\u00D7'       alt-0215  */
      car = "0";
      vector.add( car );            /* '\u00D8'       alt-0216  */
      car = "U";
      vector.add( car );            /* '\u00D9'       alt-0217  */
      vector.add( car );            /* '\u00DA'       alt-0218  */
      vector.add( car );            /* '\u00DB'       alt-0219  */
      vector.add( car );            /* '\u00DC'       alt-0220  */
      car = "Y";
      vector.add( car );            /* '\u00DD'       alt-0221  */
      car = " ";
      vector.add( car );            /* '\u00DE'       alt-0222  */
      car = "B";
      vector.add( car );            /* '\u00DF'       alt-0223  */
      car = "a";
      vector.add( car );            /* '\u00E0'       alt-0224  */
      vector.add( car );            /* '\u00E1'       alt-0225  */
      vector.add( car );            /* '\u00E2'       alt-0226  */
      vector.add( car );            /* '\u00E3'       alt-0227  */
      vector.add( car );            /* '\u00E4'       alt-0228  */
      vector.add( car );            /* '\u00E5'       alt-0229  */
      car = "a";
      vector.add( car );            /* '\u00E6'       alt-0230  ae */
      car = "c";
      vector.add( car );            /* '\u00E7'       alt-0231  */
      car = "e";
      vector.add( car );            /* '\u00E8'       alt-0232  */
      vector.add( car );            /* '\u00E9'       alt-0233  */
      vector.add( car );            /* '\u00EA'       alt-0234  */
      vector.add( car );            /* '\u00EB'       alt-0235  */
      car = "i";
      vector.add( car );            /* '\u00EC'       alt-0236  */
      vector.add( car );            /* '\u00ED'       alt-0237  */
      vector.add( car );            /* '\u00EE'       alt-0238  */
      vector.add( car );            /* '\u00EF'       alt-0239  */
      car = "d";
      vector.add( car );            /* '\u00F0'       alt-0240  */
      car = "n";
      vector.add( car );            /* '\u00F1'       alt-0241  */
      car = "o";
      vector.add( car );            /* '\u00F2'       alt-0242  */
      vector.add( car );            /* '\u00F3'       alt-0243  */
      vector.add( car );            /* '\u00F4'       alt-0244  */
      vector.add( car );            /* '\u00F5'       alt-0245  */
      vector.add( car );            /* '\u00F6'       alt-0246  */
      car = "/";
      vector.add( car );            /* '\u00F7'       alt-0247  */
      car = "0";
      vector.add( car );            /* '\u00F8'       alt-0248  */
      car = "u";
      vector.add( car );            /* '\u00F9'       alt-0249  */
      vector.add( car );            /* '\u00FA'       alt-0250  */
      vector.add( car );            /* '\u00FB'       alt-0251  */
      vector.add( car );            /* '\u00FC'       alt-0252  */
      car = "y";
      vector.add( car );            /* '\u00FD'       alt-0253  */
      car = " ";
      vector.add( car );            /* '\u00FE'       alt-0254  */
      car = "y";
      vector.add( car );            /* '\u00FF'       alt-0255  */
      vector.add( car );            /* '\u00FF'       alt-0255  */
       
      return vector;
      }
        

   /**
    * Algorithme de Frédéric BROUARD (31/3/99) mis en java par Frédéric Lapeyre (sept 2009)
    */
   public static String getPhonex(String str) {
      
      String phonex = "";
      String tempStr = str.toUpperCase();

      //1 remplacer les y par des i
      tempStr = tempStr.replaceAll("Y","I");

      //2 remplacement du son É:
      tempStr = tempStr.replaceAll("É","Y");
      tempStr = tempStr.replaceAll("È","Y");
      tempStr = tempStr.replaceAll("Ê","Y");

      //3 On enlève les caractères parasites et accentués
      tempStr = getCleanedString(tempStr);
      tempStr = tempStr.replaceAll(" ","");

      //4 supprimer les h qui ne sont pas précédées de c ou de s ou de p
      tempStr = tempStr.replaceAll("([^P|C|S])H","$1");

      //5 remplacement du ph par f
      tempStr = tempStr.replaceAll("PH","F");
  
      //6 remplacer les groupes de lettres suivantes :
      tempStr = tempStr.replaceAll("G(AI?[N|M])","K$1");
  
      //7 remplacer les occurrences suivantes, si elles sont suivies par une lettre a, e, i, o, ou u :
      tempStr = tempStr.replaceAll("[A|E]I[N|M]([A|E|I|O|U])","YN$1");
    
      //8 remplacement de groupes de 3 lettres (sons 'o', 'oua', 'ein') :
      tempStr = tempStr.replaceAll("EAU","O");
      tempStr = tempStr.replaceAll("OUA","2");
      tempStr = tempStr.replaceAll("EIN","4");
      tempStr = tempStr.replaceAll("AIN","4");
      tempStr = tempStr.replaceAll("EIM","4");
      tempStr = tempStr.replaceAll("AIM","4");
  
      //9 remplacement du reste du son É:
      tempStr = tempStr.replaceAll("AI","Y");
      tempStr = tempStr.replaceAll("EI","Y");
      tempStr = tempStr.replaceAll("ER","YR");
      tempStr = tempStr.replaceAll("ESS","YS");
      tempStr = tempStr.replaceAll("ET","YT");
      tempStr = tempStr.replaceAll("EZ","YZ");

      //10 remplacer les groupes de 2 lettres suivantes (son â..anâ.. et â..inâ..), sauf sâ..il sont suivi par une lettre a, e, i o, u ou un son 1 Ã  4 :
      tempStr = tempStr.replaceAll("AN([^A|E|I|O|U|1|2|3|4])","1$1");
      tempStr = tempStr.replaceAll("ON([^A|E|I|O|U|1|2|3|4])","1$1");
      tempStr = tempStr.replaceAll("AM([^A|E|I|O|U|1|2|3|4])","1$1");
      tempStr = tempStr.replaceAll("EN([^A|E|I|O|U|1|2|3|4])","1$1");
      tempStr = tempStr.replaceAll("EM([^A|E|I|O|U|1|2|3|4])","1$1");
      tempStr = tempStr.replaceAll("IN([^A|E|I|O|U|1|2|3|4])","4$1");

      //11 remplacer les s par des z sâ..ils sont suivi et précédés des lettres a, e, i, o,u ou dâ..un son 1 Ã  4
      tempStr = tempStr.replaceAll("([A|E|I|O|U|Y|1|2|3|4])S([A|E|I|O|U|Y|1|2|3|4])","$1Z$2");

      //12 remplacer les groupes de 2 lettres suivants :
      tempStr = tempStr.replaceAll("OE","E");
      tempStr = tempStr.replaceAll("EU","E");
      tempStr = tempStr.replaceAll("AU","O");
      tempStr = tempStr.replaceAll("OI","2");
      tempStr = tempStr.replaceAll("OY","2");
      tempStr = tempStr.replaceAll("OU","3");

      //13 remplacer les groupes de lettres suivants
      tempStr = tempStr.replaceAll("CH","5");
      tempStr = tempStr.replaceAll("SCH","5");
      tempStr = tempStr.replaceAll("SH","5");
      tempStr = tempStr.replaceAll("SS","S");
      tempStr = tempStr.replaceAll("SC","S");

      //14 remplacer le c par un s s'il est suivi d'un e ou d'un i
      tempStr = tempStr.replaceAll("C([E|I])","S$1");
  
      //15 remplacer les lettres ou groupe de lettres suivants :
      tempStr = tempStr.replaceAll("Q","K");
      tempStr = tempStr.replaceAll("QU","K");
      tempStr = tempStr.replaceAll("GU","K");
      tempStr = tempStr.replaceAll("GA","KA");
      tempStr = tempStr.replaceAll("GO","KO");
      tempStr = tempStr.replaceAll("GY","KY");

      //16 remplacer les lettres suivante :
      tempStr = tempStr.replaceAll("A","O");
      tempStr = tempStr.replaceAll("D","T");
      tempStr = tempStr.replaceAll("P","T");
      tempStr = tempStr.replaceAll("J","G");
      tempStr = tempStr.replaceAll("B","F");
      tempStr = tempStr.replaceAll("V","F");
      tempStr = tempStr.replaceAll("M","N");
 
      //17 Supprimer les lettres dupliquées
      char oldc = '#';
      String newr = "";
      for (int i = 0; i < tempStr.length(); i++) {
         int val = 0;
         if (tempStr.charAt(i) != oldc) {
            newr = newr + tempStr.charAt(i);
            }
         oldc = tempStr.charAt(i);
         }
      tempStr = newr;      

      //18 Supprimer les terminaisons suivantes : t, x
      tempStr = tempStr.replaceAll("(.*)[T|X]$","$1");

      phonex = tempStr;
      return phonex;
      }
 
} // end of object



