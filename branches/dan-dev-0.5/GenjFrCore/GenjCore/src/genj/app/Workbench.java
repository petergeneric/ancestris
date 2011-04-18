/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
package genj.app;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import genj.io.BackupFile;
import genj.io.Filter;
import genj.io.GedcomEncodingException;
import genj.io.GedcomIOException;
import genj.io.GedcomReader;
import genj.io.GedcomReaderContext;
import genj.io.GedcomReaderFactory;
import genj.io.GedcomWriter;
import genj.io.IGedcomWriter;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.ServiceLookup;
import genj.util.swing.Action2;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.DialogHelper;
import genj.util.swing.TextFieldWidget;
import genj.view.MySelectionListener;
import genj.view.SelectionSink;
import genj.view.View;
import genj.view.ViewContext;
import genj.view.ViewFactory;
import genjfr.app.pluginservice.GenjFrPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;


import spin.Spin;

/**
 * The central component of the GenJ application
 */
public class Workbench /*extends JPanel*/ implements SelectionSink {
    
  /*package*/ final static Logger LOG = Logger.getLogger("genj.app");
  /*package*/ final static Resources RES = Resources.get(Workbench.class);
  /*package*/ final static Registry REGISTRY = Registry.get(Workbench.class);

// Callback
      IWorkbenchHelper helper;

      // Instance
      private static Workbench instance = null;

      /** members */
        private IGedcomWriter writer = null;

  private Workbench(IWorkbenchHelper callback) {
      this.helper = callback;
    // plugins
    LOG.info("loading plugins");
    for (PluginFactory pf : ServiceLookup.lookup(PluginFactory.class)) {
      LOG.info("Loading plugin "+pf.getClass());
      Object plugin = pf.createPlugin(this);
      this.helper.register(plugin);
    }
    LOG.info("/loading plugins");
  }

  public static Workbench getInstance(){
      return instance;
  }
  public static Workbench getInstance(IWorkbenchHelper callback) {
      if (instance == null)
          instance = new Workbench(callback);
      return instance;
  }
  
   /**
   * create a new gedcom file
   */
  public Context newGedcom() {
    
    // let user choose a file
    File file = helper.chooseFile(RES.getString("cc.create.title"), RES.getString("cc.create.action"), null);
    if (file == null)
      return null;
    if (!file.getName().endsWith(".ged"))
      file = new File(file.getAbsolutePath() + ".ged");
    if (file.exists()) {
      int rc = DialogHelper.openDialog(RES.getString("cc.create.title"), DialogHelper.WARNING_MESSAGE, RES.getString("cc.open.file_exists", file.getName()), Action2.yesNo(), null);
      if (rc != 0)
        return null;
    }
    
    // form the origin
    Gedcom gedcom;
    try {
      gedcom = new Gedcom(Origin.create(new URL("file:"+file.getAbsolutePath())));
    } catch (MalformedURLException e) {
      LOG.log(Level.WARNING, "unexpected exception creating new gedcom", e);
      return null;
    }
    
    // done
    return setGedcom(gedcom);
  }
  
  /**
   * asks and loads gedcom file
   */
  public Context openGedcom() {

    // ask user
    File file = helper.chooseFile(RES.getString("cc.open.title"), RES.getString("cc.open.action"), null);
    if (file == null)
      return null;
    REGISTRY.put("last.dir", file.getParentFile().getAbsolutePath());
    
    // form origin
    try {
      return helper.openGedcom(new URL("file:"+file.getAbsolutePath()));
    } catch (Throwable t) {
      // shouldn't
        LOG.info(t.toString());
      return null;
    }
    // done
  }
  
  /**
   * loads gedcom file
   */
  public Context openGedcom(URL url) {

    // open connection
    final Origin origin = Origin.create(url);

    // Open Connection and get input stream
    final List<ViewContext> warnings = new ArrayList<ViewContext>();
    GedcomReader reader;
    try {

      // .. prepare our reader
      reader = (GedcomReader)Spin.off(GedcomReaderFactory.createReader(origin, (GedcomReaderContext)Spin.over(new GedcomReaderContext() {
        public String getPassword() {
          return DialogHelper.openDialog(origin.getName(), DialogHelper.QUESTION_MESSAGE, RES.getString("cc.provide_password"), "", null);
        }
        public void handleWarning(int line, String warning, Context context) {
          warnings.add(new ViewContext(RES.getString("cc.open.warning", new Object[] { new Integer(line), warning}), context));
        }
      })));

    } catch (IOException ex) {
      String txt = RES.getString("cc.open.no_connect_to", origin) + "\n[" + ex.getMessage() + "]";
      DialogHelper.openDialog(origin.getName(), DialogHelper.ERROR_MESSAGE, txt, Action2.okOnly(), null);
      return null;
    }
    
    Context context = null;
    try {
        helper.processStarted(this, reader);
      context = setGedcom(reader.read());
      // FIXME: Afficher la liste des erreurs
//      if (!warnings.isEmpty()) {
//        dockingPane.putDockable("warnings", new GedcomDockable(this,
//            RES.getString("cc.open.warnings", context.getGedcom().getName()),
//            IMG_OPEN,
//            new JScrollPane(new ContextListWidget(warnings)))
//        );
//      }
    } catch (GedcomIOException ex) {
      // tell the user about it
      DialogHelper.openDialog(origin.getName(), DialogHelper.ERROR_MESSAGE, RES.getString("cc.open.read_error", "" + ex.getLine()) + ":\n" + ex.getMessage(), Action2.okOnly(), null);
      // abort
      return null;
    } finally {
        helper.processStopped(this, reader);
    }
    
    // remember
    List<String> history = REGISTRY.get("history", new ArrayList<String>());
    history.remove(origin.toString());
    history.add(0, origin.toString());
    if (history.size()>5)
      history.remove(history.size()-1);
    REGISTRY.put("history", history);
    
    // done
    return context;
  }
  
  public Context setGedcom(Gedcom gedcom) {
      Context context = new Context();
    
    // restore context
    try {
//      context = Context.fromString(gedcom, REGISTRY.get(gedcom.getName()+".context", gedcom.getName()));
        Registry r = gedcom.getRegistry();
      context = Context.fromString(gedcom, r/*gedcom.getRegistry()*/.get("context", gedcom.getName()));
    } catch (GedcomException ge) {
    } finally {
      // fixup context if necessary - start with adam if available
      Entity adam = gedcom.getFirstEntity(Gedcom.INDI);
      if (context.getEntities().isEmpty())
        context = new Context(gedcom, adam!=null ? Collections.singletonList(adam) : null, null);
    }
    
    // tell everone
    helper.gedcomOpened(this, gedcom);
  
    fireSelection(null,context, true);
    return context;
    
    // done
  }
  
  /**
   * save gedcom to a new file
   * @return new origin if filters applied (ie exported to a new file), null otherwise
   */
  public Origin saveAsGedcom(Context context) {
    
    if (context == null || context.getGedcom() == null)
      return null;
    
    // ask everyone to commit their data
    fireCommit(context);
    
    // .. choose file
//FIXME: DAN    Box options = new Box(BoxLayout.Y_AXIS);
//    options.add(new JLabel(RES.getString("save.options.encoding")));
//
//    ChoiceWidget comboEncodings = new ChoiceWidget(Gedcom.ENCODINGS, Gedcom.ANSEL);
//    comboEncodings.setEditable(false);
//    comboEncodings.setSelectedItem(context.getGedcom().getEncoding());
//    options.add(comboEncodings);
//
//    options.add(new JLabel(RES.getString("save.options.password")));
//    String pwd = context.getGedcom().getPassword();
//    TextFieldWidget textPassword = new TextFieldWidget(context.getGedcom().hasPassword() ? pwd : "", 10);
//    textPassword.setEditable(pwd!=Gedcom.PASSWORD_UNKNOWN);
//    options.add(textPassword);

    Collection<? extends Filter> filters = GenjFrPlugin.lookupAll(Filter.class);
    ArrayList<Filter> theFilters = new ArrayList<Filter>(5);
    for (Filter f:filters) {
        if (f.canApplyTo(context.getGedcom()))
            theFilters.add(f);
    }
    SaveOptionsWidget options = new SaveOptionsWidget(context.getGedcom(),theFilters.toArray(new Filter[]{}));//, (Filter[])viewManager.getViews(Filter.class, gedcomBeingSaved));
    File file = helper.chooseFile(RES.getString("cc.save.title"), RES.getString("cc.save.action"), options);
    if (file == null)
      return null;
  
    // .. take chosen one & filters
    if (!file.getName().endsWith(".ged"))
      file = new File(file.getAbsolutePath() + ".ged");

    // Need confirmation if File exists?
    if (file.exists()) {
      int rc = DialogHelper.openDialog(RES.getString("cc.save.title"), DialogHelper.WARNING_MESSAGE, RES.getString("cc.open.file_exists", file.getName()), Action2.yesNo(), null);
      if (rc != 0) 
        return null;
    }

    Gedcom gedcom = context.getGedcom();

    // Remember some previous values before setting them
    String prevPassword = gedcom.getPassword();
    String prevEncoding = gedcom.getEncoding();
    Origin prevOrigin = gedcom.getOrigin();

    gedcom.setPassword(options.getPassword());
    gedcom.setEncoding(options.getEncoding());

    Origin newOrigin = null;
    // .. create new origin
    try {
        newOrigin = Origin.create(new URL("file", "", file.getAbsolutePath()));
        gedcom.setOrigin(newOrigin);
    } catch (Throwable t) {
      LOG.log(Level.FINER, "Failed to create origin for file "+file, t);
      // restore
        gedcom.setEncoding(prevEncoding);
        gedcom.setPassword(prevPassword);
        gedcom.setOrigin(prevOrigin);
      return null;
    }
  
    // save
    if (!saveGedcomImpl(gedcom,options.getFilters())){
        gedcom.setEncoding(prevEncoding);
        gedcom.setPassword(prevPassword);
        gedcom.setOrigin(prevOrigin);
    	return null;
    }
    if (writer.hasFiltersVetoed()){
        gedcom.setEncoding(prevEncoding);
        gedcom.setPassword(prevPassword);
        gedcom.setOrigin(prevOrigin);
    	return newOrigin;
    }
    
    // .. note changes are saved now
    if (gedcom.hasChanged())
      gedcom.doMuteUnitOfWork(new UnitOfWork() {
            @Override
        public void perform(Gedcom gedcom) throws GedcomException {
          gedcom.setUnchanged();
        }
      });

    // .. done
    return null;
  }
  
  /**
   * save gedcom file
   */
  public boolean saveGedcom(Context context) {

    if (context.getGedcom() == null)
      return false;

    // ask everyone to commit their data
    fireCommit(context);
    
    // do it
    Gedcom gedcom = context.getGedcom();
    if (!saveGedcomImpl(gedcom, null)){
        return false;
    }
    // .. note changes are saved now
    if (gedcom.hasChanged())
      gedcom.doMuteUnitOfWork(new UnitOfWork() {
        public void perform(Gedcom gedcom) throws GedcomException {
          gedcom.setUnchanged();
        }
      });

    // .. done
    return true;
    
  }

  /**
   * save gedcom file
   */
  public boolean saveGedcomImpl(Gedcom gedcom,Collection<Filter> filters) {

//  // .. open progress dialog
//  progress = WindowManager.openNonModalDialog(null, RES.getString("cc.save.saving", file.getName()), WindowManager.INFORMATION_MESSAGE, new ProgressWidget(gedWriter, getThread()), Action2.cancelOnly(), getTarget());

    try {

      // prep files and writer
      writer = null;
      File file = null, temp = null;
      try {
        // .. resolve to canonical file now to make sure we're writing to the
        // file being pointed to by a symbolic link
        file = gedcom.getOrigin().getFile().getCanonicalFile();

        // .. create a temporary output
        temp = File.createTempFile("genj", ".ged", file.getParentFile());

        // .. create writer
        writer = (IGedcomWriter)Spin.off(new GedcomWriter(gedcom, new FileOutputStream(temp)));


      } catch (GedcomEncodingException gee) {
        DialogHelper.openDialog(gedcom.getName(), DialogHelper.ERROR_MESSAGE, RES.getString("cc.save.write_encoding_error", gee.getMessage()), Action2.okOnly(), null);
        return false;
      } catch (IOException ex) {
        DialogHelper.openDialog(gedcom.getName(), DialogHelper.ERROR_MESSAGE, RES.getString("cc.save.open_error", gedcom.getOrigin().getFile().getAbsolutePath()), Action2.okOnly(), null);
        return false;
      }

      if (filters!=null)
          writer.setFilters(filters);

      // .. write it
    try {
        helper.processStarted(this, writer);
        writer.write();
    } finally {
        helper.processStopped(this, writer);
    }

      // .. make backup
      BackupFile.createBackup(file);
//      if (file.exists()) {
//        File bak = new File(file.getAbsolutePath() + "~");
//        if (bak.exists()&&!bak.delete())
//          throw new GedcomIOException("Couldn't delete backup file " + bak.getName(), -1);
//        if (!file.renameTo(bak))
//          throw new GedcomIOException("Couldn't create backup for " + file.getName(), -1);
//      }

      // .. and now !finally! move from temp to result
      if (!temp.renameTo(file))
        throw new GedcomIOException("Couldn't move temporary " + temp.getName() + " to " + file.getName(), -1);

    } catch (GedcomIOException gioex) {
      DialogHelper.openDialog(gedcom.getName(), DialogHelper.ERROR_MESSAGE, RES.getString("cc.save.write_error", "" + gioex.getLine()) + ":\n" + gioex.getMessage(), Action2.okOnly(), null);
      return false;
    }

//  // close progress
//  WindowManager.close(progress);

    // .. done
    return true;
  }


  /**
   * closes gedcom file
   */
  public boolean closeGedcom(Context context) {

    // noop?
    if (context.getGedcom()==null)
      return true;

    // commit changes
    fireCommit(context);
    
    // changes?
    if (context.getGedcom().hasChanged()) {
      
      // close file officially
      int rc = DialogHelper.openDialog(null, DialogHelper.WARNING_MESSAGE, RES.getString("cc.savechanges?", context.getGedcom().getName()), Action2.yesNoCancel(), null);
      // cancel - we're done
      if (rc == 2)
        return false;
      // yes - close'n save it
      if (rc == 0) 
        if (!saveGedcom(context))
          return false;
      
    }
    
    // tell 
    helper.gedcomClosed(this, context.getGedcom());
    
    // remember context
    context.getGedcom().getRegistry().put("context", context.toString());

    // remember and tell
//    context = new Context();
// FIXME ne sert a rien?    helper.selectionChanged(this, context, true);
    
    // done
    return true;
  }
  
  /**
   * Restores last loaded gedcom file
   */
  @SuppressWarnings("deprecation")
  public void restoreGedcom() {

    String restore = REGISTRY.get("restore.url", (String)null);
    try {
      // no known key means load default
      if (restore==null)
      	// we're intentionally not going through toURI.toURL here since
      	// that would add space-to-%20 conversion which kills our relative
      	// file check operations down the line
        restore = new File("gedcom/example.ged").toURL().toString();
      // known key needs value
      if (restore.length()>0)
        helper.openGedcom(new URL(restore));
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "unexpected error", t);
    }
  }
    
  public void fireCommit(Context context) {
    helper.commitRequested(this, context);
  }
  
  public void fireSelection(MySelectionListener from, Context context, boolean isActionPerformed) {
      helper.fireSelection(from, context, isActionPerformed);
  } 
  
  public void addWorkbenchListener(WorkbenchListener listener) {
      helper.register(listener);
  }

  public void removeWorkbenchListener(WorkbenchListener listener) {
      helper.unregister(listener);
  }

  /**
   * (re)open a view
   */
  public View openView(Class<? extends ViewFactory> factory) {
    return openView(factory, helper.getContext());
  }
  
  /**
   * (re)open a view
   */
  public View openView(Class<? extends ViewFactory> factory, Context context) {
        try {
            return helper.openViewImpl(factory.newInstance(), context);
        } catch (InstantiationException ex) {
            Logger.getLogger(Workbench.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Workbench.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
  }
  
} // ControlCenter
