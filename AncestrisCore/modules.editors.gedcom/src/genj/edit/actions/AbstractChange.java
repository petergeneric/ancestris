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
 */
package genj.edit.actions;

import ancestris.core.resources.Images;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextAreaWidget;
import ancestris.view.SelectionSink;
import genj.gedcom.Property;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * ActionChange - change the gedcom information
 */
public abstract class AbstractChange extends Action2 
implements LookupListener{
  
  /** resources */
  /*package*/ final static Resources resources = Resources.get(AbstractChange.class);
   
  /** Lookup Context */
    protected Lookup context;
    
    /** Lookup.Result to get properties from lookup 
     * for resultChange in default implementation*/
    protected Lookup.Result<Property> lkpInfo;
    
    /** Properties in lookup */
    protected List<Property> contextProperties = new ArrayList<Property>(5);

  private Context selection;
  
  /** image *new* */
  protected final static ImageIcon imgNew = Images.imgNew;
  
  private JTextArea confirm;

  /**
   * Conxtructor
   */
    public AbstractChange(Lookup context) {
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        this.context = context;
    }
    
    public AbstractChange(){
        this(Utilities.actionsGlobalContext());
    }

  /**
   * Constructor
   */
  public AbstractChange(Gedcom ged, ImageIcon img, String text) {
      this();
      setImageText(img, text);
  }
  
  /**
   * Convenient shortcut
   * @param img
   * @param text 
   */
  public final void setImageText(ImageIcon img, String text){
    super.setImage(img);
    super.setText(text);
    super.setTip(text);
  }
  
    Gedcom getGedcom(){
      return ancestris.util.Utilities.getGedcomFromContext(context);
  }
    
    /**
     * helper to set whole context
     * @param props 
     */
    protected void setContextProperties(Collection<? extends Property> props){
        contextProperties.clear();
        contextProperties.addAll(props);
    }

    protected void setContextProperties(Property prop){
        contextProperties.clear();
        contextProperties.add(prop);
    }

    @Override
    public boolean isEnabled() {
        initLookupListner();
        return super.isEnabled();
    }
    
    /**
     * Setup Lookup change listener on Property object. This is the default
     * implementation and may be overiden to listen to other object changes
     * in Lookup (ie Entity).
     */
    protected void initLookupListner() {
        assert SwingUtilities.isEventDispatchThread() 
               : "this shall be called just from AWT thread";
 
        if (context == null)
            return;
        if (lkpInfo != null) {
            return;
        }
 
        //The thing we want to listen for the presence or absence of
        //on the global selection
        lkpInfo = context.lookupResult(Property.class);
        lkpInfo.addLookupListener(this);
        resultChanged(null);
    }
 
    /**
     * callback for Lookup Result change. This can be overidden to get
     * properties from LookupResult on which this action should apply. 
     * contextChanged is then called to change text, tip or image based 
     * on new context.
     * @param ev 
     */
    @Override
    public void resultChanged(LookupEvent ev) {
        if (lkpInfo != null){
            contextProperties.clear();
            contextProperties.addAll(lkpInfo.allInstances());
        }
        contextChanged();        
    }

    /**
     * Called upon context change in Lookup to update text, tip or image 
     * representation for this action
     */
    protected void contextChanged(){
    }
    
  /** 
   * Returns the confirmation message - null if none
   */
  protected String getConfirmMessage() {
    return null;
  }
  
  /**
   * Return the dialog content to show to the user
   */
  protected JPanel getDialogContent() {
    JPanel result = new JPanel(new NestedBlockLayout("<col><text wx=\"1\" wy=\"1\"/></col>"));
    result.add(getConfirmComponent());
    return result;
  }
  
  protected JComponent getConfirmComponent() {
    if (confirm==null) {
      confirm = new TextAreaWidget(getConfirmMessage(), 6, 40);
      confirm.setWrapStyleWord(true);
      confirm.setLineWrap(true);
      confirm.setEditable(false);
    }
    return new JScrollPane(confirm);
  }
  
  /** 
   * Callback to update confirm text
   */
  protected void refresh() {
    // might be no confirmation showing
    if (confirm!=null)
      confirm.setText(getConfirmMessage());
  }
  
  /**
   * @see genj.util.swing.Action2#execute()
   */
    @Override
  public void actionPerformed(final ActionEvent event) {
        if (getGedcom() == null)
            return;

        initLookupListner();
    
    // cleanup first
    confirm = null;
	  
    // prepare confirmation message for user
    String msg = getConfirmMessage();
    if (msg!=null) {
  
      // prepare actions
      Action[] actions = { 
          new Action2(resources.getString("confirm.proceed", getText())),
          Action2.cancel() 
      };
      
      // Recheck with the user
      int rc = DialogHelper.openDialog(getText(), DialogHelper.QUESTION_MESSAGE, getDialogContent(), actions, event) ;
      if (rc!=0)
        return;
    }
        
    // do the change
    try {
      getGedcom().doUnitOfWork(new UnitOfWork() {
        public void perform(Gedcom gedcom) throws GedcomException {
          selection = execute(gedcom, event);
        }
      });
    } catch (Throwable t) {
      DialogHelper.openDialog(null, DialogHelper.ERROR_MESSAGE, t.getMessage(), Action2.okOnly(), event);
    }
    
    // propagate selection
    if (selection!=null)
    	SelectionSink.Dispatcher.fireSelection(event, selection);

    // Propagate changes in lookup too
    resultChanged(null);
    
    // done
  }
  
  /**
   * perform the actual change
   */
  protected abstract Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException;

} //Change

