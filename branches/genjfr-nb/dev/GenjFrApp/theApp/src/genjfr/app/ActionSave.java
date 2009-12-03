/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genj.app.Images;
import genj.app.SaveOptionsWidget;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomDirectory;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import genj.io.Filter;
import genj.io.GedcomEncodingException;
import genj.io.GedcomIOException;
import genj.io.GedcomWriter;
import genj.util.Origin;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ProgressWidget;
import genj.view.CommitRequestedEvent;
import genj.window.WindowManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

  /**
   * Action - Save
   */
  public class ActionSave extends Action2 {
    /** whether to ask user */
    private boolean ask;
    /** gedcom */
    public Gedcom gedcomBeingSaved;
    /** writer */
    private GedcomWriter gedWriter;
    /** origin to load after successfull save */
    private Origin newOrigin;
    /** filters we're using */
    private Filter[] filters;
    /** progress key */
    private String progress;
    /** exception we might encounter */
    private GedcomIOException ioex = null;
    /** temporary and target file */
    private File temp, file;
    /** password used */
    private String password;

      private Resources resources = Resources.get(genj.app.ControlCenter.class);
      private WindowManager windowManager = App.center.getWindowManager();
  private final static String
    ACC_SAVE = "ctrl S";

    /**
     * Constructor for saving gedcom file without interaction
     */
    protected ActionSave(Gedcom gedcom) {
      this(false, true);

      // remember gedcom
      this.gedcomBeingSaved = gedcom;
    }
    public ActionSave(){
        this(false,true);
    }
    /**
     * Constructor
     */
    protected ActionSave(boolean ask, boolean enabled) {
      // setup default target
      setTarget(App.center);
      // setup accelerator - IF this is a no-ask save it instead of SaveAs
      if (!ask) setAccelerator(ACC_SAVE);
      // remember
      this.ask = ask;
      // text
      if (ask)
        setText(resources.getString("cc.menu.saveas"));
      else
        setText(resources.getString("cc.menu.save"));
      setTip(resources, "cc.tip.save_file");
      // setup
      setImage(Images.imgSave);
      setAsync(ASYNC_NEW_INSTANCE);
      setEnabled(enabled);
    }
    /**
     * Initialize save
     * @see genj.util.swing.Action2#preExecute()
     */
    protected boolean preExecute() {

      // Choose currently selected Gedcom if necessary
      if (gedcomBeingSaved==null) {
	      gedcomBeingSaved = App.center.getSelectedGedcom();
	      if (gedcomBeingSaved == null)
	        return false;
      }

      // Do we need a file-dialog or not?
      Origin origin = gedcomBeingSaved.getOrigin();
      String encoding = gedcomBeingSaved.getEncoding();
      password = gedcomBeingSaved.getPassword();

      if (ask || origin==null || origin.getFile()==null) {

        // .. choose file
        SaveOptionsWidget options = new SaveOptionsWidget(gedcomBeingSaved, (Filter[])App.center.getViewManager().getViews(Filter.class, gedcomBeingSaved));
        file = App.center.chooseFile(resources.getString("cc.save.title"), resources.getString("cc.save.action"), options);
        if (file==null)
          return false;

        // .. take choosen one & filters
        if (!file.getName().endsWith(".ged"))
          file = new File(file.getAbsolutePath()+".ged");
        filters = options.getFilters();
        if (gedcomBeingSaved.hasPassword())
          password = options.getPassword();
        encoding = options.getEncoding();

        // .. create new origin
        try {
          newOrigin = Origin.create(new URL("file", "", file.getAbsolutePath()));
        } catch (Throwable t) {
        }


      } else {

        // .. form File from URL
        file = origin.getFile();

      }

      // Need confirmation if File exists?
      if (file.exists()&&ask) {

        int rc = windowManager.openDialog(null,resources.getString("cc.save.title"),WindowManager.WARNING_MESSAGE,resources.getString("cc.open.file_exists", file.getName()),Action2.yesNo(),App.center);
        if (rc!=0) {
          newOrigin = null;
          //20030221 no need to go for newOrigin in postExecute()
          return false;
        }

      }

      // ask everyone to commit their data
      WindowManager.broadcast(new CommitRequestedEvent(gedcomBeingSaved, App.center));

      // .. open io
      try {

        // .. resolve to canonical file now to make sure we're writing to the file being pointed to by a sym link
        file = file.getCanonicalFile();

        // .. create a temporary output
        temp = File.createTempFile("genj", ".ged", file.getParentFile());

        // .. create writer
        gedWriter =
          new GedcomWriter(gedcomBeingSaved, file.getName(), encoding, new FileOutputStream(temp));

        // .. set options
        gedWriter.setFilters(filters);
        gedWriter.setPassword(password);

      } catch (GedcomEncodingException ex) {
        windowManager.openDialog(null,gedcomBeingSaved.getName(),
            WindowManager.ERROR_MESSAGE,
            resources.getString("cc.save.write_encoding_error", ex.getMessage()),
            Action2.okOnly(),
            App.center);
        return false;

      } catch (IOException ex) {

          windowManager.openDialog(null,gedcomBeingSaved.getName(),
                      WindowManager.ERROR_MESSAGE,
                      resources.getString("cc.save.open_error", file.getAbsolutePath()),
                      Action2.okOnly(),
                      App.center);
        return false;
      }

      // .. open progress dialog
      progress = windowManager.openNonModalDialog(
        null,
        resources.getString("cc.save.saving", file.getName()),
        WindowManager.INFORMATION_MESSAGE,
        new ProgressWidget(gedWriter, getThread()),
        Action2.cancelOnly(),
        getTarget()
      );

      // .. continue (async)
      return true;

    }

    /**
     * (async) execute
     * @see genj.util.swing.Action2#execute()
     */
    protected void execute() {

      // catch io problems
      try {

        // .. do the write
        gedWriter.write();

        // .. make backup
        if (file.exists()) {
          File bak = new File(file.getAbsolutePath()+"~");
          if (bak.exists())
            bak.delete();
          file.renameTo(bak);
        }

        // .. and now !finally! move from temp to result
        if (!temp.renameTo(file))
          throw new GedcomIOException("Couldn't move temporary "+temp.getName()+" to "+file.getName(), -1);

        // .. note changes are saved now
        if (newOrigin == null) gedcomBeingSaved.doMuteUnitOfWork(new UnitOfWork() {
          public void perform(Gedcom gedcom) throws GedcomException {
            gedcomBeingSaved.setUnchanged();
          }
        });

      } catch (GedcomIOException ex) {
        ioex = ex;
      }

      // done
    }

    /**
     * (sync) post write
     * @see genj.util.swing.Action2#postExecute(boolean)
     */
    protected void postExecute(boolean preExecuteResult) {

      // close progress
      windowManager.close(progress);

      // problem encountered?
      if (ioex!=null) {
          if( ioex instanceof GedcomEncodingException)  {
              windowManager.openDialog(null,
                      gedcomBeingSaved.getName(),
                      WindowManager.ERROR_MESSAGE,
                      resources.getString("cc.save.write_encoding_error", ioex.getMessage() ),
                      Action2.okOnly(),App.center);
          }
          else {
              windowManager.openDialog(null,
                      gedcomBeingSaved.getName(),
                      WindowManager.ERROR_MESSAGE,
                      resources.getString("cc.save.write_error", "" + ioex.getLine()) + ":\n" + ioex.getMessage(),
                      Action2.okOnly(),App.center);

          }
      } else {
        // SaveAs?
        if (newOrigin != null) {

          // .. close old
          Gedcom alreadyOpen  = GedcomDirectory.getInstance().getGedcom(newOrigin.getName());
          if (alreadyOpen!=null)
            GedcomDirectory.getInstance().unregisterGedcom(alreadyOpen);

          // .. open new
          ActionOpen open = new ActionOpen(newOrigin)
//TODO: a remettre          {
//            protected void postExecute(boolean preExecuteResult) {
//              super.postExecute(preExecuteResult);
//              // copy registry from old
//              if (gedcomBeingLoaded!=null) {
//                ViewManager.getRegistry(gedcomBeingLoaded).set(ViewManager.getRegistry(gedcomBeingSaved));
//              }
//            }
//          }
        ;
          open.password = password;
          open.trigger();

        }
      }

      // track what we read
      if (gedWriter!=null)
        App.center.getStats().handleWrite(gedWriter.getLines());

      // .. done
    }

  } //ActionSave
