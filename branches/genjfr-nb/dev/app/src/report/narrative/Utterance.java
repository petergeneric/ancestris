/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package narrative;

import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import genj.gedcom.Entity;
import genj.util.Resources;
import genj.fo.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Utterance is used to generate sentences and phrases in different languages.
 * Linguists use the term "utterance" as a general "speech act".
 */
public class Utterance {
  private final String template;
  private final Map props = new HashMap(); // key/value; "1" for 1st positional property
  private final ArrayList linkedEntities = new ArrayList();
  private Resources resources;
  private int gender = 0; // PropertySex.MALE, PropertySex.FEMALE

  private static final String SUBJECT = "SUBJECT";

  private Utterance(Resources resources, String template) {
    this.resources = resources;
    this.template = template;
    props.put("LBRACKET", "[");
    props.put("RBRACKET", "]");
    props.put("LBRACE", "{");
    props.put("RBRACE", "}");
  }

  public static Utterance forProperty(Resources resources, String property) {
    return forTemplate(resources, translate(property, resources));
  }

  public static Utterance forProperty(Resources resources, String property, String[] params, Entity[] linkedEntities) {
    return forTemplate(resources, translate(property, resources), params, linkedEntities);
  }

  public static Utterance forProperty(Resources resources, String property, String[] params) {
    return forTemplate(resources, translate(property, resources), params);
  }

  public static Utterance forTemplate(Resources resources, String template) {
    return new Utterance(resources, template);
  }

  public static Utterance forTemplate(Resources resources, String template, String[] params, Entity[] linkedEntities) {
    Utterance result = forTemplate(resources, template, params);
    for (int i = 0; i < linkedEntities.length; i++) {
      Entity entity = linkedEntities[i];
      result.linkedEntities.add(entity);
    }
    return result;
  }

  public static Utterance forTemplate(Resources resources, String template, String[] params) {
    Utterance result = new Utterance(resources, template);
    for (int i=0; i < params.length; i++) {
      result.set(Integer.toString(i+1), params[i]);
    }
    return result;
  }

  /** language we're trying to use */
  private final static String lang = Locale.getDefault().getLanguage(); // todo bk don't duplicate the stuff in Report!

  /**
   * Look up a string value in resources - this one patches leading/trailing spaces marked with _
   */
  private String translate(String key) {
    return translate(key, resources);
  }

  public static boolean isTranslatable(String key, Resources resources) {
      return translate(key, resources) != null;
  }

  /**
   * Look up a string value in resources - this one patches leading/trailing spaces marked with _
   */
  public static String translate(String key, Resources resources) {
    // do we have resources?
    if (resources==null)
      return key;


    // look it up in language
    String result = null;
    if (lang!=null  && !lang.equals("en"))
      result = resources.getString(key+'.'+lang);
    if (result != null) return result;

    // fallback if necessary
    // look it up
    result = resources.getString(key);
    if (result.equals(key))
      return null; // second-guess Report's default mechanism (todo better way to tell if configured or not)

    // todo How get leading/trailing blanks in properties?
    if (result.startsWith("_")) {
      result = " " + result.substring(1);
    }
    if (result.endsWith("_")) {
      result = result.substring(0, result.length()-1) + " ";
    }

//    System.err.println("Key " + key + " -> " + result);
    return result;
  }

  private String getGenderKeySuffix() {
    if (gender == PropertySex.MALE) {
      return ".male";
    } else if (gender == PropertySex.FEMALE) {
      return ".female";
    } else {
      return ".genderUnknown";
    }
  }

  public void setSubject(Indi indi) {
    gender = indi.getSex();
    props.put(SUBJECT, translate("pronoun.nom" + getGenderKeySuffix()));
  }

  private static final Pattern argPattern = Pattern.compile("\\[[^\\[\\]]*\\]");

  public void addText(Document doc) {
    // todo: link 1, 2 instead of adding text
    doc.addText(toString());
  }

  public String toString() {
    Matcher matcher = argPattern.matcher(template);
    String result = template;
    int start = 0;
    while (matcher.find(start)) {
      int where = matcher.start();
      String key = matcher.group();
      key = key.substring(1, key.length()-1);
      String value = (String) props.get(key);
      if (value == null) {
        if (key.startsWith("OPTIONAL_")) {
          value = "";
        } else {
          System.err.println("No value for key " + key + " in sentence template " + template);
          if (key.startsWith("ending.")) {
            value = translate(key + getGenderKeySuffix());
          } else if (key.startsWith("SUBJECT.")) {
            value = translate("pronoun." + key.substring(8) + getGenderKeySuffix());
          }
          if (value == null) {
            value = key;
          }
        }
      }
      if (where == 0) {
        if (value.length() == 0) {
          System.err.println("Empty string for key " + key);
        } else {
          value = Character.toUpperCase(value.charAt(0)) + value.substring(1);
        }
      }
      // Insert space between words unless we're inserting an ending
      if (where > 0 && value.length() > 0 && !key.startsWith("ending.") &&
          (!Character.isSpaceChar(result.charAt(where-1)) &&
          Character.isLetterOrDigit(value.charAt(0))
          ||
          !Character.isSpaceChar(result.charAt(where-1)) &&
          punctuationRequiresPrecedingBlank(value.charAt(0))
          )
      ) {
          value = " " + value;
      }
      // Don't want to use matcher.replaceFirst, because that reinterprets any
      // brackets in the output (does reset() and match starting at pos. 0).
      String before = result.substring(0, where);
      String after = result.substring(where + matcher.group().length());
      start = where + value.length();
      result = before + value + after; // matcher.replaceFirst(value); // does reset and starts at 0...not good
      // todo Strange bug here that doesn't replace the closing ] (with SOUR in German report), then has start which is length+5
      // Happened when using [LBRACKET] and [RBRACKET] ... somehow [RBRACKET] was not removed although the "]" was inserted?
      if (start > result.length()) {
        System.err.println("OutOfBoundsException about to happen");
        result = result.substring(0, result.length() - key.length() -2);
        break;
      }
      matcher.reset(result);
    }
    return result;
  }

  /**
   * Not only words require a preceding blank, but also certain
   * punctuation characters.
   * @param c Next char in utterance
   * @return Whether to insert a space.
   */
  private boolean punctuationRequiresPrecedingBlank(char c) {
    return c == '(' || c == '{' || c == '[';
  }

  public static void main(String[] args) {
    // NM where do we get the resources in this main case from?
    Utterance s = forTemplate(null, "[SUBJECT] wurde geboren[OPTIONAL_PP_PLACE][OPTIONAL_PP_DATE].");
    s.set("SUBJECT", "er");
    System.out.println(s);
    s.set("OPTIONAL_PP_PLACE", "in Frankfurt");
    System.out.println(s);
    s = forTemplate(null, "Geboren wurde [SUBJECT][OPTIONAL_PP_PLACE][OPTIONAL_PP_DATE].");
    s.set("SUBJECT", "sie");
    System.out.println(s);
    s.set("OPTIONAL_PP_PLACE", "in Duesseldorf");
    System.out.println(s);

    Utterance pp = forTemplate(null, "in [CITY]");
    pp.set("CITY", "Frankfurt");
    s.set("OPTIONAL_PP_PLACE", pp.toString());
    System.out.println(s);
  }

  public void set(String key, String value) {
    props.put(key, value);
  }

  public String get(String key) {
    return (String) props.get(key);
  }

  public boolean hasKey(String key) {
    return props.containsKey(key);
  }

}
