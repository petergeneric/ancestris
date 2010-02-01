/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package merge;

import genj.gedcom.Entity;
import genj.gedcom.Property;

import genj.gedcom.Note;
import genj.gedcom.Source;
import genj.gedcom.Submitter;
import genj.gedcom.Repository;

import genj.gedcom.TagPath;

import java.util.Comparator;

/**
 * GenJ - Tools
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
public class InfoFactory implements Comparator {

   Info info = new Info();
        
   public InfoFactory() {
      this(new Entity());
      }
   
   public InfoFactory(Entity entity) {
      info.entity = entity;
      info.id = entity.getId();
      
      if (entity instanceof Source) {
         info.title = ((Source)entity).getTitle();
         info.titleLength = PersonFactory.encode(info.title.trim(), info.titleCode);
         
         Property prop = (Property)entity.getProperty(new TagPath("SOUR:AUTH"));
         info.auth = (prop == null) ? "" : prop.getValue();
         info.authLength = PersonFactory.encode(info.auth.trim(), info.authCode);
         
         prop = (Property)entity.getProperty(new TagPath("SOUR:ABBR"));
         info.abbr = (prop == null) ? "" : prop.getValue();
         info.abbrLength = PersonFactory.encode(info.abbr.trim(), info.abbrCode);

         info.text = ((Source)entity).getText();
         info.textLength = PersonFactory.encode(info.text.trim(), info.textCode);
         }      
     
      if (entity instanceof Note) {
         Property prop = (Property)entity.getProperty(new TagPath("NOTE"));
         info.text = (prop == null) ? "" : prop.getValue();
         info.textLength = PersonFactory.encode(info.text.trim(), info.textCode);
         }      
         
      if (entity instanceof Submitter) {
         Property prop = (Property)entity.getProperty(new TagPath("SUBM:NAME"));
         info.text = (prop == null) ? "" : prop.getValue();
         info.textLength = PersonFactory.encode(info.text.trim(), info.textCode);
         }      
         
      if (entity instanceof Repository) {
         Property prop = (Property)entity.getProperty(new TagPath("SUBM:NAME"));
         info.text = (prop == null) ? "" : prop.getValue();
         info.textLength = PersonFactory.encode(info.text.trim(), info.textCode);
         }      
      }
     
   public Info create() {
      return info;
      }
   
   static public String toString(Info i) {
      return "("+((i.entity == null)? "null" : ((Entity)i.entity).getId())+") - "+i.title;
      }
   
   static public String display(Info i) {
      StringBuffer sb = new StringBuffer();
      
      sb.append("id="+i.id+"\n");
      sb.append("merged="+i.merged+"\n");
      sb.append("title="+i.title+"\n");
      sb.append("text="+i.text+"\n");
      sb.append("auth="+i.auth+"\n");
      sb.append("abbr="+i.abbr+"\n");
      sb.append("titleLength="+i.titleLength+"\n");
      sb.append("textLength="+i.textLength+"\n");
      sb.append("authLength="+i.authLength+"\n");
      sb.append("abbrLength="+i.abbrLength+"\n");
      sb.append("titlecode="+codeDisplay(i.titleCode)+"\n");
      sb.append("authcode="+codeDisplay(i.authCode)+"\n");
      sb.append("abbrcode="+codeDisplay(i.abbrCode)+"\n");
      return sb.toString();
      }
   
   static private String codeDisplay(int[] code) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < code.length; i++) {
         sb.append(""+code[i]+";");
         }
      return sb.toString();      
      }
        
   public int compare(Object o1, Object o2) {
      return ((Info)o1).id.compareTo(((Info)o2).id);
      }
   
} // end of object
  
