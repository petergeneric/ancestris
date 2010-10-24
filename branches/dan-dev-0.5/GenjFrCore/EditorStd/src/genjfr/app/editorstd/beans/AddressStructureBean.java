/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.editorstd.beans;

import genj.gedcom.Property;
import genj.gedcom.PropertyChoiceValue;
import genj.gedcom.PropertyMultilineValue;
import genj.gedcom.PropertySimpleValue;
import java.beans.*;
import java.io.Serializable;

/**
 *
 * @author frederic
 */
public class AddressStructureBean implements Serializable {

    private PropertyMultilineValue addr;
    public static final String PROP_ADDR = "ADDR";
    private PropertySimpleValue addr1;
    public static final String PROP_ADDR1 = "ADR1";
    private PropertySimpleValue addr2;
    public static final String PROP_ADDR2 = "ADR2";
    private PropertyChoiceValue city;
    public static final String PROP_CITY = "CITY";
    private PropertyChoiceValue stae;
    public static final String PROP_STAE = "STAE";
    private PropertyChoiceValue post;
    public static final String PROP_POST = "POST";
    private PropertyChoiceValue ctry;
    public static final String PROP_CTRY = "CTRY";
    public final int phonSize = 3;
    private Property[] phon = new Property[phonSize];
    public static final String PROP_PHON = "PHON";
    private PropertySimpleValue email;
    public static final String PROP_EMAIL = "_EMAIL";
    private PropertySimpleValue www;
    public static final String PROP_WWW = "_WWW";





    private PropertyChangeSupport propertySupport;

    public AddressStructureBean() {
        propertySupport = new PropertyChangeSupport(this);
    }

    /**
     * Get the value of addr
     *
     * @return the value of addr
     */
    public PropertyMultilineValue getAddr() {
        return addr;
    }

    /**
     * Set the value of addr
     *
     * @param addr new value of addr
     */
    public void setAddr(PropertyMultilineValue addr) {
        PropertyMultilineValue oldAddr = this.addr;
        this.addr = addr;
        propertySupport.firePropertyChange(PROP_ADDR, oldAddr, addr);
    }

    /**
     * Get the value of addr1
     *
     * @return the value of addr1
     */
    public PropertySimpleValue getAddr1() {
        return addr1;
    }

    /**
     * Set the value of addr1
     *
     * @param addr1 new value of addr1
     */
    public void setAddr1(PropertySimpleValue addr1) {
        PropertySimpleValue oldAddr1 = this.addr1;
        this.addr1 = addr1;
        propertySupport.firePropertyChange(PROP_ADDR1, oldAddr1, addr1);
    }

    /**
     * Get the value of addr2
     *
     * @return the value of addr2
     */
    public PropertySimpleValue getAddr2() {
        return addr2;
    }

    /**
     * Set the value of addr2
     *
     * @param addr2 new value of addr2
     */
    public void setAddr2(PropertySimpleValue addr2) {
        PropertySimpleValue oldAddr2 = this.addr2;
        this.addr2 = addr2;
        propertySupport.firePropertyChange(PROP_ADDR2, oldAddr2, addr2);
    }

    /**
     * Get the value of city
     *
     * @return the value of city
     */
    public PropertyChoiceValue getCity() {
        return city;
    }

    /**
     * Set the value of city
     *
     * @param city new value of city
     */
    public void setCity(PropertyChoiceValue city) {
        PropertyChoiceValue oldCity = this.city;
        this.city = city;
        propertySupport.firePropertyChange(PROP_CITY, oldCity, city);
    }

    /**
     * Get the value of stae
     *
     * @return the value of stae
     */
    public PropertyChoiceValue getStae() {
        return stae;
    }

    /**
     * Set the value of stae
     *
     * @param stae new value of stae
     */
    public void setStae(PropertyChoiceValue stae) {
        PropertyChoiceValue oldStae = this.stae;
        this.stae = stae;
        propertySupport.firePropertyChange(PROP_STAE, oldStae, stae);
    }

    /**
     * Get the value of post
     *
     * @return the value of post
     */
    public PropertyChoiceValue getPost() {
        return post;
    }

    /**
     * Set the value of post
     *
     * @param post new value of post
     */
    public void setPost(PropertyChoiceValue post) {
        PropertyChoiceValue oldPost = this.post;
        this.post = post;
        propertySupport.firePropertyChange(PROP_POST, oldPost, post);
    }

    /**
     * Get the value of ctry
     *
     * @return the value of ctry
     */
    public PropertyChoiceValue getCtry() {
        return ctry;
    }

    /**
     * Set the value of ctry
     *
     * @param ctry new value of ctry
     */
    public void setCtry(PropertyChoiceValue ctry) {
        PropertyChoiceValue oldCtry = this.ctry;
        this.ctry = ctry;
        propertySupport.firePropertyChange(PROP_CTRY, oldCtry, ctry);
    }



    /**
     * Get the value of phon
     *
     * @return the value of phon
     */
    public Property[] getPhon() {
        return phon;
    }

    /**
     * Set the value of phon
     *
     * @param phon new value of phon
     */
    public void setPhon(Property[] phon) {
        if (phon == null) {
            return;
        }
        Property[] oldLang = this.phon;
        for (int i = 0; i < phonSize; i++) {
            if (phon.length > i) {
                this.phon[i] = phon[i];
            } else {
                this.phon[i] = null;
            }
        }
        propertySupport.firePropertyChange(PROP_PHON, oldLang, phon);
    }

    /**
     * Get the value of email
     *
     * @return the value of email
     */
    public PropertySimpleValue getEmail() {
        return email;
    }

    /**
     * Set the value of email
     *
     * @param email new value of email
     */
    public void setEmail(PropertySimpleValue email) {
        PropertySimpleValue oldEmail = this.email;
        this.email = email;
        propertySupport.firePropertyChange(PROP_EMAIL, oldEmail, email);
    }

    /**
     * Get the value of www
     *
     * @return the value of www
     */
    public PropertySimpleValue getWww() {
        return www;
    }

    /**
     * Set the value of www
     *
     * @param www new value of www
     */
    public void setWww(PropertySimpleValue www) {
        PropertySimpleValue oldWww = this.www;
        this.www = www;
        propertySupport.firePropertyChange(PROP_WWW, oldWww, www);
    }

    
    // listerners //
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
}
