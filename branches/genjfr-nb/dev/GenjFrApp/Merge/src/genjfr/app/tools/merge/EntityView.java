/**
 * Copyright (C) 2006 Frederic Lapeyre <frederic@lapeyre-frederic.com>
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genjfr.app.tools.merge;

import javax.swing.JOptionPane;
//import javax.swing.JTextArea;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Insets;
import java.awt.TextArea;
import java.lang.StringBuffer;

import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.GridBagHelper;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

  
/**
 * GenJ - Tools
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
  public class EntityView {

    private JPanel jpanel = null;
    JLabel question = null;
    JLabel title1 = null;
    JLabel title2 = null;
    TextArea textArea1 = null; 
    TextArea textArea2 = null;
    
    String windowTitle = "";
    String questionText = "";
    String area1Title = "";
    String area2Title = "";
    String area1Text = "";
    String area2Text = "";
    
    private static int LINESIZE = 80;
    private static int TITLESIZE = 30;
  
    public EntityView(String windowTitle, String questionText, boolean small) {
       
       this.windowTitle = windowTitle;
       this.questionText = questionText;
       jpanel = new JPanel();
       GridBagHelper gh = new GridBagHelper(jpanel).setInsets(new Insets(2,2,2,2)).setParameter(GridBagHelper.GROW_HORIZONTAL | GridBagHelper.FILL_HORIZONTAL);
       question = new JLabel(questionText, JLabel.LEFT);
       title1 = new JLabel(area1Title, JLabel.CENTER);
       title2 = new JLabel(area2Title, JLabel.CENTER);
       textArea1 = new TextArea(area1Text, small?10:18, 35);
       textArea2 = new TextArea(area2Text, small?10:18, 35);
       gh.add(question, 0, 0);
       gh.add(title1, 0, 10);
       gh.add(title2, 200, 10);
       gh.add(textArea1, 0, 20);
       gh.add(textArea2, 200, 20);
       }
  
    public void setQuestion(String str) {
       questionText = str;
       question.setText(str);
       }
       
    public int getEntityFromUser(Entity ent1, Entity ent2, Object[] values, Object valDef) {
       
       title1.setText(getTitle(ent1.getTag()+" : "+ent1.toString()));
       title2.setText(getTitle(ent2.getTag()+" : "+ent2.toString()));
       textArea1.setText(getText(ent1));
       textArea2.setText(getText(ent2));
       
       List strList = Arrays.asList(values);
       Object selection = JOptionPane.showInputDialog(null, jpanel, windowTitle,JOptionPane.QUESTION_MESSAGE, null, values, valDef);
       if (selection == null) return -1;
       int choice = strList.indexOf((String)selection);
       return choice;
       }
      
        
    public int getEntityFromUser(Property prop1, Property prop2, Object[] values, Object valDef) {
       
       String title1 = getTitle(prop1.getEntity().getTag()+" : "+prop1.getEntity().toString());
       String title2 = getTitle(prop2.getEntity().getTag()+" : "+prop2.getEntity().toString());
       return getEntityFromUser(prop1, prop2, title1, title2, values, valDef);
       }
      
    public int getEntityFromUser(Property prop1, Property prop2, String str1, String str2, Object[] values, Object valDef) {
       
       title1.setText(str1);
       title2.setText(str2);
       textArea1.setText(getText(prop1));
       textArea2.setText(getText(prop2));
       
       List strList = Arrays.asList(values);
       Object selection = JOptionPane.showInputDialog(null,jpanel, windowTitle,JOptionPane.QUESTION_MESSAGE, null, values, valDef);
       if (selection == null) return -1;
       int choice = strList.indexOf((String)selection);
       return choice;
       }
      
        
    private String getText(Property prop) {
   
      String text = "";
      if (!(prop instanceof Entity)) {
         text += " "+prop.getTag()+": "+prop.toString()+"\n";
         }
      List listProp = new LinkedList();
      Property[] properties = prop.getProperties();
      listProp.addAll(Arrays.asList(properties));
      Property propItem = null;
      while (listProp.size() > 0) {
         propItem = (Property) ((LinkedList)listProp).removeFirst();
         int indent = (propItem.getPath().length() - 2) * 3 + 1;
         Property[] subProps = propItem.getProperties();
         listProp.addAll(0, Arrays.asList(subProps));
         StringBuffer str = new StringBuffer(indent);
         for (int i=0; i<indent; i++) str.append(" ");
         String value = propItem.toString();
         if (value.length() > LINESIZE) 
            value = value.substring(0, LINESIZE)+"...";
         text += str+propItem.getTag()+": "+value+"\n";
         }
      
      return text;
      }
      
    private String getTitle(String str) {
      if (str.length() > TITLESIZE) 
          str = str.substring(0, TITLESIZE)+"...";
      return str;
      }
       
} // End of object
  
