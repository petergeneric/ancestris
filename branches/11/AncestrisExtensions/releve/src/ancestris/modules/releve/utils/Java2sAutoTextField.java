package ancestris.modules.releve.utils;

/* From http://java.sun.com/docs/books/tutorial/index.html */

/*
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */
import java.util.List;
import javax.swing.JTextField;
import javax.swing.text.*;

import genj.util.ChangeSupport;
import java.awt.AWTEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.event.ChangeListener;

public class Java2sAutoTextField extends JTextField {

    class AutoDocument extends PlainDocument {

        @Override
        public void replace(int i, int j, String s, AttributeSet attributeset)
          throws BadLocationException {
            super.remove(i, j);
            insertString(i, s, attributeset);
            changeSupport.fireChangeEvent();
        }

        @Override
        public void insertString(int i, String s, AttributeSet attributeset)
                throws BadLocationException {
            if (s == null || "".equals(s)) {
                return;
            }
            String s1 = getText(0, i);
            if (isUpperAllChar) {
                // je mets toutes les lettres en majuscule
                s = s.toUpperCase(getLocale());
            } else if (isUpperAllFirstChar) {
                // je convertis le premier caractere de chaque mot en majuscule
                // Sont considérés comme premiers caractères :
                //  le permier caractere du champ,
                //  un caractere qui suit un espace,
                //  un caractere qui suit un tiret
                StringBuilder s3 = new StringBuilder();
                for( int j = 0 ; j < s.length(); j++) {
                    if ( (i+j == 0 )
                        || (( i>0 && j==0) && ((s1.charAt(i-1) == ' ') || (s1.charAt(i-1) == '-') || (s1.charAt(i-1) == ',') ))
                        || (( j>0) && ((s.charAt(j-1) == ' ') || (s.charAt(j-1) == '-') || (s.charAt(j-1) == ',') ))
                        ) {
                        s3.append( s.substring(j,j+1).toUpperCase(getLocale()));
                    } else {
                        s3.append( s.substring(j,j+1).toLowerCase(getLocale()));
                    }
                }
                s = s3.toString();
            } else if (isUpperFirstChar) {
                // je convertis le premier caractere du champ en majuscule
                // Les autres caractères ne sont pas modifiés.
                StringBuilder s3 = new StringBuilder();
                for( int j = 0 ; j < s.length(); j++) {
                    if ( (i+j == 0 )) {
                        s3.append( s.substring(j,j+1).toUpperCase(getLocale()));
                    } else {
                        s3.append( s.substring(j,j+1));
                    }
                }
                s = s3.toString();
            }

            String s2 = getMatch(s1 + s);
            int j = (i + s.length()) - 1;
            if (isStrict && s2 == null) {
                s2 = getMatch(s1);
                j--;
            } else if (!isStrict && s2 == null) {
                super.insertString(i, s, attributeset);
                return;
            }
            if (autoComboBox != null && s2 != null) {
                autoComboBox.setSelectedValue(s2);
            }
            super.remove(0, getLength());
            super.insertString(0, s2, attributeset);
            setSelectionStart(j + 1);
            setSelectionEnd(getLength());
            changeSupport.fireChangeEvent();
        }

        @Override
        public void remove(int i, int j) throws BadLocationException {
            int k = getSelectionStart();
            if (k > 0) {
                k--;
            }
            String s = getMatch(getText(0, k));
            if (i <= 1 && k == 0 ) {
                // s'il ne reste qu'un caractere a effacer, je n'utilise pas la completion
                super.remove(0, getLength());
                s = "";
            } else if (!isStrict && s == null) {
                super.remove(i, j);
            } else {
                super.remove(0, getLength());
                super.insertString(0, s, null);
            }

            if (autoComboBox != null && s != null) {
                autoComboBox.setSelectedValue(s);
            }
            try {
                setSelectionStart(k);
                setSelectionEnd(getLength());
                changeSupport.fireChangeEvent();
            } catch (Exception exception) {
            }
        }

        /**
         * set text without completion
         * @param s
         */
        public void setText(String s ) {
            try {
                String oldValue = super.getText(0,getLength());
                if (!oldValue.equals(s)) {
                    super.remove(0, getLength());
                    super.insertString(0, s, null);
                    changeSupport.fireChangeEvent();
                }
            } catch (BadLocationException ex) {
                //Exceptions.printStackTrace(ex);
            }

        }

    }

    public Java2sAutoTextField(List<String> list) {
        isCaseSensitive = false;
        isStrict = false;
        autoComboBox = null;
        if (list == null) {
            throw new IllegalArgumentException("list can not be null");
        } else {
            dataList = list;
            init();
        }
    }

    Java2sAutoTextField(List<String> list, Java2sAutoComboBox b) {
        isCaseSensitive = false;
        isStrict = false;
        autoComboBox = null;
        if (list == null) {
            throw new IllegalArgumentException("list can not be null");
        } else {
            dataList = list;
            autoComboBox = b;
            init();
        }
    }

    private void init() {
        setDocument(new AutoDocument());
        if (isStrict && dataList.size() > 0) {
            setText(dataList.get(0));
        }
    }

    private String getMatch(String s) {
        for (int i = 0; i < dataList.size(); i++) {
            String s1 = dataList.get(i);
            if (s1 != null) {
                if (!isCaseSensitive
                        && s1.toLowerCase().startsWith(s.toLowerCase())) {
                    return s1;
                }
                if (isCaseSensitive && s1.startsWith(s)) {
                    return s1;
                }
            }
        }

        return null;
    }

    @Override
    public void replaceSelection(String s) {
        AutoDocument _lb = (AutoDocument) getDocument();
        if (_lb != null) {
            try {
                int i = Math.min(getCaret().getDot(), getCaret().getMark());
                int j = Math.max(getCaret().getDot(), getCaret().getMark());
                _lb.replace(i, j - i, s, null);
            } catch (Exception exception) {
            }
        }
    }

    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    public void setCaseSensitive(boolean flag) {
        isCaseSensitive = flag;
    }

    public boolean isStrict() {
        return isStrict;
    }

    public void setStrict(boolean flag) {
        isStrict = flag;
    }

    public void setUpperAllChar(boolean flag) {
        isUpperAllChar = flag;

    }

    public void setUpperAllFirstChar(boolean flag) {
        isUpperAllFirstChar = flag;
    }

    public void setUpperFirstChar(boolean flag) {
        isUpperFirstChar = flag;
    }

    public List<String> getDataList() {
        return dataList;
    }

    public void setDataList(List<String> list) {
        if (list == null) {
            throw new IllegalArgumentException("list can not be null");
        } else {
            dataList = list;
            //System.out.println(dataList.toString());
        }
    }

    @Override
    public void setText(String text) {
       ((AutoDocument)getDocument()).setText(text);
    }

    /**
     * Add change listener
     */
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    /**
     * Remove change listener
     */
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void processKeyEvent(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) && (e.getModifiers() & InputEvent.ALT_MASK)!= 0 ) {
            if( autoComboBox != null) {
                autoComboBox.getParent().dispatchEvent((AWTEvent)e);
            } else {
                getParent().dispatchEvent((AWTEvent)e);
            }
            return; //don't process the event
        }
        super.processKeyEvent(e);
    }

    private List<String> dataList;
    private boolean isCaseSensitive;
    private boolean isStrict;
    private boolean isUpperAllChar = false;
    private boolean isUpperAllFirstChar = false;
    private boolean isUpperFirstChar = false;
    //private Locale  locale = Locale.getDefault();
    private Java2sAutoComboBox autoComboBox;
    private ChangeSupport changeSupport = new ChangeSupport(this) {

        @Override
        public void fireChangeEvent() {
            //isTemplate = false;
            super.fireChangeEvent();
        }
    };
}
