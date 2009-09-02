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
package genj.search;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Grammar;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import genj.view.ContextProvider;
import genj.view.ContextSelectionEvent;
import genj.view.ToolBarSupport;
import genj.view.ViewContext;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import spin.Spin;

/**
 * View for searching
 */
public class SearchView extends JPanel implements ToolBarSupport {
  
  /** formatting */
  private final static String
   OPEN = "<font color=red>",
   CLOSE = "</font>",
   NEWLINE = "<br>";
  
  /** max # hits */
  private final static int MAX_HITS = 255;
  
  /** default values */
  private final static String[]
    DEFAULT_VALUES = {
      "M(a|e)(i|y)er", "San.+Francisco", "^(M|F)"
    },
    DEFAULT_PATHS = {
      "INDI", "INDI:NAME", "INDI:BIRT", "INDI:OCCU", "INDI:NOTE", "INDI:RESI",
      "FAM"
    }
  ;
  
  /** how many old values we remember */
  private final static int MAX_OLD = 16;
  
  /** resources */
  /*package*/ static Resources resources = Resources.get(SearchView.class);
  
  /** gedcom */
  private Gedcom gedcom;
  
  /** registry */
  private Registry registry;
  
  /** manager */
  private ViewManager manager;
  
  /** shown results */
  private Results results = new Results();
  private ResultWidget listResults = new ResultWidget();

  /** headless label used for view creation */
  private HeadlessLabel viewFactory = new HeadlessLabel(listResults.getFont()); 

  /** criterias */
  private ChoiceWidget choicePath, choiceValue;
  private JCheckBox checkRegExp;
  private JLabel labelCount;
  
  /** history */
  private LinkedList oldPaths, oldValues;
  
  /** buttons */
  private AbstractButton bSearch, bStop;
  
  /** images */
  private final static ImageIcon
    IMG_START = new ImageIcon(SearchView.class, "Start"),
    IMG_STOP  = new ImageIcon(SearchView.class, "Stop" );

  /**
   * Constructor
   */
  public SearchView(Gedcom geDcom, Registry reGistry, ViewManager maNager) {
    
    // remember
    gedcom = geDcom;
    registry = reGistry;
    manager = maNager;
    
    // lookup old search values & settings
    oldPaths = new LinkedList(Arrays.asList(registry.get("old.paths" , DEFAULT_PATHS)));
    oldValues= new LinkedList(Arrays.asList(registry.get("old.values", DEFAULT_VALUES)));
    boolean useRegEx = registry.get("regexp", false);

    // prepare an action listener connecting to click
    ActionListener aclick = new ActionListener() {
      /** button */
      public void actionPerformed(ActionEvent e) {
        bStop.doClick();
        bSearch.doClick();
      }
    };
    
    // prepare search criteria
    JLabel labelValue = new JLabel(resources.getString("label.value"));
    checkRegExp = new JCheckBox(resources.getString("label.regexp"), useRegEx);

    choiceValue = new ChoiceWidget(oldValues);
    choiceValue.addActionListener(aclick);

    PopupWidget popupPatterns = new PopupWidget("...", null, createPatternActions());
    popupPatterns.setMargin(new Insets(0,0,0,0));

    JLabel labelPath = new JLabel(resources.getString("label.path"));    
    choicePath = new ChoiceWidget(oldPaths);
    choicePath.addActionListener(aclick);
    
    PopupWidget popupPaths = new PopupWidget("...", null, createPathActions());
    popupPaths.setMargin(new Insets(0,0,0,0));
    
    labelCount = new JLabel();
    
    JPanel paneCriteria = new JPanel();
    try {
      paneCriteria.setFocusCycleRoot(true);
    } catch (Throwable t) {
    }
    
    GridBagHelper gh = new GridBagHelper(paneCriteria);
    // .. line 0
    gh.add(labelValue    ,0,0,2,1,0, new Insets(0,0,0,8));
    gh.add(checkRegExp   ,2,0,1,1, GridBagHelper.GROW_HORIZONTAL|GridBagHelper.FILL_HORIZONTAL);
    gh.add(labelCount    ,3,0,1,1);
    // .. line 1
    gh.add(popupPatterns ,0,1,1,1);
    gh.add(choiceValue   ,1,1,3,1, GridBagHelper.GROW_HORIZONTAL|GridBagHelper.FILL_HORIZONTAL, new Insets(3,3,3,3));
    // .. line 2
    gh.add(labelPath     ,0,2,4,1, GridBagHelper.GROW_HORIZONTAL|GridBagHelper.FILL_HORIZONTAL);
    // .. line 3
    gh.add(popupPaths    ,0,3,1,1);
    gh.add(choicePath    ,1,3,3,1, GridBagHelper.GROW_HORIZONTAL|GridBagHelper.FILL_HORIZONTAL, new Insets(0,3,3,3));
    
    // prepare layout
    setLayout(new BorderLayout());
    add(BorderLayout.NORTH , paneCriteria);
    add(BorderLayout.CENTER, new JScrollPane(listResults) );
    choiceValue.requestFocusInWindow();

    // done
  }
  
  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    // start listening
    gedcom.addGedcomListener((GedcomListener)Spin.over(results));
    // continue
    super.addNotify();
    // set focus
    choiceValue.requestFocusInWindow();
  }
  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // stop listening
    gedcom.removeGedcomListener((GedcomListener)Spin.over(results));
    // keep old
    registry.put("regexp"    , checkRegExp.isSelected());
    registry.put("old.values", oldValues);
    registry.put("old.paths" , oldPaths );
    // continue
    super.removeNotify();
  }

  
  /**
   * @see genj.view.ToolBarSupport#populate(javax.swing.JToolBar)
   */
  public void populate(JToolBar bar) {
    ButtonHelper bh = new ButtonHelper().setContainer(bar).setInsets(0);
    ActionSearch search = new ActionSearch();
    ActionStop   stop   = new ActionStop  (search);
    bSearch = bh.create(search);
    bStop   = bh.create(stop);
  }
  
  /**
   * Remembers a value
   */
  private void remember(ChoiceWidget choice, LinkedList old, String value) {
    // not if empty
    if (value.trim().length()==0) return;
    // keep (up to max)
    old.remove(value);
    old.addFirst(value);
    if (old.size()>MAX_OLD) old.removeLast();
    // update choice
    choice.setValues(old);
    choice.setText(value);
    // done
  }

  /**
   * Returns a matcher for given pattern and regex flag
   */
  private Matcher getMatcher(String pattern, boolean regex) {

    Matcher result = regex ? (Matcher)new RegExMatcher() : (Matcher)new SimpleMatcher();
    
    // init
    result.init(pattern);
    
    // done
    return result;
  }

  /**
   * Create preset Path Actions
   */
  private List createPathActions() {
    
    // loop through DEFAULT_PATHS
    List result = new ArrayList();
    for (int i=0;i<DEFAULT_PATHS.length;i++) {
      result.add(new ActionPath(DEFAULT_PATHS[i]));
    }
    
    // done
    return result;
  }

  /**
   * Create RegExp Pattern Actions
   */
  private List createPatternActions() {
    // loop until ...
    List result = new ArrayList();
    for (int i=0;;i++) {
      // check text and pattern
      String 
        key = "regexp."+i,
        txt = resources.getString(key+".txt", false),
        pat = resources.getString(key+".pat", false);
      // no more?
      if (txt==null) break;
      // pattern?
      if (pat==null) 
        continue;
      // create action
      result.add(new ActionPattern(txt,pat));
    }
    return result; 
  }
  
  /**
   * Action - select predefined paths
   */
  private class ActionPath extends Action2 {
    
    private TagPath tagPath;
    
    /**
     * Constructor
     */
    private ActionPath(String path) {
      tagPath = new TagPath(path);
      MetaProperty meta = Grammar.V55.getMeta(tagPath);
      setText(meta.getName());
      setImage(meta.getImage());
    }
    
    /**
     * @see genj.util.swing.Action2#execute()
     */
    protected void execute() {
      choicePath.setText(tagPath.toString());
    }
  } //ActionPath

  /**
   * Action - insert regexp construct
   *   {0} all text
   *   {1} before selection
   *   {2} (selection)
   *   {3} after selection
   */
  private class ActionPattern extends Action2 {
    /** pattern */
    private String pattern;
    /**
     * Constructor
     */
    private ActionPattern(String txt, String pat) {
      // make first word bold
      int i = txt.indexOf(' ');
      if (i>0)
        txt = "<html><b>"+txt.substring(0,i)+"</b>&nbsp;&nbsp;&nbsp;"+txt.substring(i)+"</html>";
          
      setText(txt);
      pattern = pat;
    }
    /**
     * @see genj.util.swing.Action2#execute()
     */
    protected void execute() {

      // analyze what we've got
      final JTextField field = choiceValue.getTextEditor();
      int 
        selStart = field.getSelectionStart(),
        selEnd   = field.getSelectionEnd  ();
      if (selEnd<=selStart) {
        selStart = field.getCaretPosition();
        selEnd   = selStart;
      }
      // {0} all text
      String all = field.getText();
      // {1} before selection
      String before = all.substring(0, selStart);
      // {2} (selection)
      String selection = selEnd>selStart ? '('+all.substring(selStart, selEnd)+')' : "";
      // {3] after selection
      String after = all.substring(selEnd);

      // calculate result
      final String result = MessageFormat.format(pattern, new Object[]{ all, before, selection, after} );

      // invoke this later - selection might otherwise not work correctly
      SwingUtilities.invokeLater(new Runnable() { public void run() {
        
        int pos = result.indexOf('#');
        
        // show
        field.setText(result.substring(0,pos)+result.substring(pos+1));
        field.select(0,0);
        field.setCaretPosition(pos);
        
        // make sure regular expressions are enabled now
        checkRegExp.setSelected(true);
      }});
      
      // done
    }
  } //ActionInsert

  /**
   * Action - trigger search
   */
  private class ActionSearch extends Action2 {
    /** tag path */
    private TagPath tagPath = null;
    /** count of hits found */
    private int hitCount = 0;
    /** entities found */
    private Set entities = new HashSet();
    /** hits */
    private List hits = new ArrayList(MAX_HITS);
    /** the current matcher*/
    private Matcher matcher;
    /** constructor */
    private ActionSearch() {
      setImage(IMG_START);
      setAsync(ASYNC_SAME_INSTANCE);
    }
    /**
     * before execute (sync)
     */
    protected boolean preExecute() {
      // reset results
      results.clear();
      // update buttons
      bSearch.setEnabled(false);
      bStop.setEnabled(true);
      // prepare matcher & path
      String value = choiceValue.getText();
      String path = choicePath.getText();
      try {
        matcher = getMatcher(value, checkRegExp.isSelected());
        tagPath = path.length()>0 ? new TagPath(path) : null;
      } catch (IllegalArgumentException e) {
        WindowManager.getInstance(getTarget()).openDialog(null,value,WindowManager.ERROR_MESSAGE,e.getMessage(),Action2.okOnly(),SearchView.this);
        return false;
      }
      // remember
      remember(choiceValue, oldValues, value);
      remember(choicePath , oldPaths , path );
      // continue
      return true;
    }

    /** run (async) */
    protected void execute() {
      search(gedcom);
    }
    
    /** run (sync() callback on EDT) */
    protected void syncExecute() {
      synchronized (hits) {
        results.add(hits);
        hits.clear();
      }
      labelCount.setText(""+hitCount);
    }
    
    /**
     * @see genj.util.swing.Action2#handleThrowable(java.lang.String, java.lang.Throwable)
     */
    protected void handleThrowable(String phase, Throwable t) {
      WindowManager.getInstance(getTarget()).openDialog(null,null,WindowManager.INFORMATION_MESSAGE,t.getMessage() ,Action2.okOnly(),SearchView.this);
    }

    /**
     * after execute (on EDT)
     */
    protected void postExecute(boolean preExecuteResult) {
      // update count
      labelCount.setText(""+hitCount);
      // reset our state
      entities.clear();
      hits.clear();
      hitCount = 0;
      // toggle buttons
      bSearch.setEnabled(true);
      bStop.setEnabled(false);
      // done
    }
    
    /** search in gedcom (not on EDT) */
    private void search(Gedcom gedcom) {
      for (int t=0; t<Gedcom.ENTITIES.length; t++) {
        for (Iterator es=gedcom.getEntities(Gedcom.ENTITIES[t]).iterator();es.hasNext();) {
          search((Entity)es.next());
        }
      }
    }
    
    /** search entity (not on EDT) */
    private void search(Entity entity) {
      search(entity, entity, 0);
    }
    
    /** search property (not on EDT) */
    private void search(Entity entity, Property prop, int pathIndex) {
      // still going?
      if (getThread().isInterrupted()) return;
      // got a path?
      boolean searchThis = true;
      if (tagPath!=null) {
        // break if we don't match path
        if (pathIndex<tagPath.length()&&!tagPath.get(pathIndex).equals(prop.getTag())) 
          return;
        // search this if path is consumed 
        searchThis = pathIndex>=tagPath.length()-1;
      }
      // parse all but transients
      if (searchThis&&!prop.isTransient()) {
        // check entity's id
        if (entity==prop)
          search(entity, entity, entity.getId(), true);
        // check prop's value
        search(entity, prop, prop.getDisplayValue(), false);
      }
      // check subs
      int n = prop.getNoOfProperties();
      for (int i=0;i<n;i++) {
        search(entity, prop.getProperty(i), pathIndex+1);
      }
      // done
    }

    /** search property's value */
    private void search(Entity entity, Property prop, String value, boolean isID) {
      // look for matches
      Matcher.Match[] matches = matcher.match(value);
      if (matches.length==0)
        return;
      // too many?
      if (hitCount==MAX_HITS)
        throw new IndexOutOfBoundsException(resources.getString("maxhits", Integer.toString(MAX_HITS)));
      hitCount++;
      // keep entity
      entities.add(entity);
      // create a hit
      Hit hit = new Hit(prop, value, matches, entities.size(), isID);
      // keep it
      synchronized (hits) {
        hits.add(hit);
        // sync (on first)?
        if (hits.size()==1) sync();
      }
      // done
    }
    
  } //ActionSearch
  
  /**
   * Action - stop search
   */
  private class ActionStop extends Action2 {
    /** start */
    private ActionSearch start;
    /** constructor */
    private ActionStop(ActionSearch start) {
      setImage(IMG_STOP);
      setEnabled(false);
      this.start = start;
    }
    /** run */
    protected void execute() {
      start.cancel(false);
    }
  } //ActionStop

  /**
   * Our result bucket
   */
  private class Results extends AbstractListModel implements GedcomListener {
    
    /** the results */
    private List hits = new ArrayList(255);
    
    /**
     * clear the results (sync to EDT)
     */
    private void clear() {
      // nothing to do?
      if (hits.isEmpty())
        return;
      // clear&notify
      int size = hits.size();
      hits.clear();
      fireIntervalRemoved(this, 0, size-1);
      // done
    }
    
    /**
     * add a result (sync to EDT)
     */
    private void add(List list) {
      // nothing to do?
      if (list.isEmpty()) 
        return;
      // remember 
      int size = hits.size();
      hits.addAll(list);
      fireIntervalAdded(this, size, hits.size()-1);
      // done
    }
    
    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
      return hits.get(index);
    }
    
    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
      return hits.size();
    }
    
    /**
     * access to property
     */
    private Hit getHit(int i) {
      return (Hit)hits.get(i);
    }

    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
      // TODO could do a re-search here
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      // ignored
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
      // TODO could do a re-search here
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property prop) {
      for (int i=0;i<hits.size();i++) {
        Hit hit = (Hit)hits.get(i);
        if (hit.getProperty()==prop) 
          fireContentsChanged(this, i, i);
      }
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
      for (int i=0;i<hits.size();) {
        Hit hit = (Hit)hits.get(i);
        if (hit.getProperty()==removed) {
          hits.remove(i);
          fireIntervalRemoved(this, i, i);
        } else {
          i++;
        }
      }
    }

  } //Results

  /**
   * our specialized list
   */  
  private class ResultWidget extends JList implements ListSelectionListener, ListCellRenderer, ContextProvider  {
    
    /** our text component for rendering */
    private JTextPane text = new JTextPane();
    
    /** background colors */
    private Color[] bgColors = new Color[3];

    /**
     * Constructor
     */
    private ResultWidget() {
      super(results);
      // colors
      bgColors[0] = getSelectionBackground();
      bgColors[1] = getBackground();
      bgColors[2] = new Color( 
        Math.max(bgColors[1].getRed  ()-16,  0), 
        Math.min(bgColors[1].getGreen()+16,255), 
        Math.max(bgColors[1].getBlue ()-16,  0)
      );
      
      // rendering
      setCellRenderer(this);
      addListSelectionListener(this);
      text.setOpaque(true);
    }
    
    /**
     * ContextProvider - callback
     */
    public ViewContext getContext() {
      
      ViewContext result = new ViewContext(gedcom);
      
      Object[] selection = getSelectedValues();
      for (int i = 0; i < selection.length; i++) {
        Hit hit = (Hit)selection[i];
        result.addProperty(hit.getProperty());
      }
      return result;
    }

    /**
     * we know about action delegates and will use that here if applicable
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      Hit hit = (Hit)value;
      
      // prepare color
      int c = isSelected ? 0 : 1 + (hit.getEntity()&1);  
      text.setBackground(bgColors[c]);
      
      // show hit document (includes image and text)
      text.setDocument(hit.getDocument());
      
      // done
      return text;
    }
    
    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e) {
      int row = listResults.getSelectedIndex();
      if (row>=0)
        WindowManager.broadcast(new ContextSelectionEvent(new ViewContext(results.getHit(row).getProperty()), this));
    }

    
  } //ResultWidget
 
} //SearchView
