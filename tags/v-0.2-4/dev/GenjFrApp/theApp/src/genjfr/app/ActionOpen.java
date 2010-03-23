/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.app.Images;
import genj.common.ContextListWidget;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomDirectory;
import genj.io.GedcomEncryptionException;
import genj.io.GedcomIOException;
import genj.io.GedcomReader;
import genj.util.DirectAccessTokenizer;
import genj.util.Origin;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.ProgressWidget;
import genj.window.WindowManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

//public final class ActionOpen implements ActionListener {
//
//    public void actionPerformed(ActionEvent e) {
//        // TODO implement action body
//    }
//}

public /*final*/ class ActionOpen extends Action2 {
  private final static String ACC_OPEN = "ctrl O";
  private Resources resources = Resources.get(genj.app.ControlCenter.class);
  WindowManager windowManager = App.center.getWindowManager();

    /** a preset origin we're reading from */
    private Origin origin;

    /** a reader we're working on */
    private GedcomReader reader;

    /** an error we might encounter */
    private GedcomIOException exception;

    /** a gedcom we're creating */
    protected Gedcom gedcomBeingLoaded;

    /** key of progress dialog */
    private String progress;

    /** password in use */
    protected String password = Gedcom.PASSWORD_NOT_SET;

    /** views to load */
    private List views2restore = new ArrayList();

    private boolean mustOpenDefaultViews = false;

    /** constructor - good for reloading */
    public ActionOpen(String restore) throws MalformedURLException {
        this(restore,false);
    }
    public ActionOpen(String restore, boolean mustOpenDefaultViews) throws MalformedURLException {

      setAsync(ASYNC_SAME_INSTANCE);

      // grab "file[, password][, view#x]"
      DirectAccessTokenizer tokens = new DirectAccessTokenizer(restore, ",", false);
      String url = tokens.get(0);
      String pwd = tokens.get(1);
      if (url==null)
        throw new IllegalArgumentException("can't restore "+restore);

      origin = Origin.create(url);
      if (pwd!=null&&pwd.length()>0) password = pwd;

      // grab views we're going to open if successful
//      for (int i=2; ; i++) {
//        String token = tokens.get(i);
//        if (token==null) break;
//        if (token.length()>0) views2restore.add(tokens.get(i));
//      }

      this.mustOpenDefaultViews = mustOpenDefaultViews;

      // done
    }

    /** constructor - good for button or menu item */
    public ActionOpen() {
      setAccelerator(ACC_OPEN);
      setTip(resources, "cc.tip.open_file");
      setText(resources, "cc.menu.open");
      setImage(Images.imgOpen);
      setAsync(ASYNC_NEW_INSTANCE);
      this.mustOpenDefaultViews = true;
    }

    /** constructor - good for loading a specific file*/
    public ActionOpen(Origin setOrigin) {
      setAsync(ASYNC_SAME_INSTANCE);
      origin = setOrigin;
    }

    /**
     * (sync) pre execute
     */
    protected boolean preExecute() {

      // need to ask for origin?
      if (origin==null) {
        Action actions[] = {
          new Action2(resources, "cc.open.choice.local"),
          new Action2(resources, "cc.open.choice.inet" ),
          Action2.cancel(),
        };
        int rc = windowManager.openDialog(
          null,
          resources.getString("cc.open.title"),
          WindowManager.QUESTION_MESSAGE,
          resources.getString("cc.open.choice"),
          actions,
          App.center
        );
        switch (rc) {
          case 0 :
            origin = chooseExisting();
            break;
          case 1 :
            origin = chooseURL();
            break;
        }
      }
      // try to open it
      return origin==null ? false : open(origin);
    }

    /**
     * (Async) execute
     */
    protected void execute() {
      try {
        gedcomBeingLoaded = reader.read();
      } catch (GedcomIOException ex) {
        exception = ex;
      }
    }

    /**
     * (sync) post execute
     */
    protected void postExecute(boolean preExecuteResult) {

      // close progress
      windowManager.close(progress);

      // any error bubbling up?
      if (exception != null) {

        // maybe try with different password
        if (exception instanceof GedcomEncryptionException) {

          password = windowManager.openDialog(
            null,
            origin.getName(),
            WindowManager.QUESTION_MESSAGE,
            resources.getString("cc.provide_password"),
            "",
            App.center
          );

          if (password==null)
            password = Gedcom.PASSWORD_UNKNOWN;

          // retry
          exception = null;
          trigger();

          return;
        }

        // tell the user about it
        windowManager.openDialog(
          null,
          origin.getName(),
          WindowManager.ERROR_MESSAGE,
          resources.getString("cc.open.read_error", "" + exception.getLine()) + ":\n" + exception.getMessage(),
          Action2.okOnly(),
          App.center
        );

        return;

      }

      // got a successfull gedcom
      if (gedcomBeingLoaded != null) {

        GedcomDirectory.getInstance().registerGedcom(gedcomBeingLoaded);

        // open views again
//        if (Options.getInstance().isRestoreViews) {
//          for (int i=0;i<views2restore.size();i++) {
//            ViewHandle handle = ViewHandle.restore(viewManager, gedcomBeingLoaded, (String)views2restore.get(i));
//            if (handle!=null)
//              new ActionSave(gedcomBeingLoaded).setTarget(handle.getView()).install(handle.getView(), JComponent.WHEN_IN_FOCUSED_WINDOW);
//          }
//        }

      }

      // show warnings&stats
      if (reader!=null) {

        App.center.stats.handleRead(reader.getLines());

        // warnings
        List warnings = reader.getWarnings();
        if (!warnings.isEmpty()) {
          windowManager.openNonModalDialog(
            null,
            resources.getString("cc.open.warnings", gedcomBeingLoaded.getName()),
            WindowManager.WARNING_MESSAGE,
            new JScrollPane(new ContextListWidget(gedcomBeingLoaded, warnings)),
            Action2.okOnly(),
            App.center
          );
        }
      }
      if (mustOpenDefaultViews && preExecuteResult) {
          openDefaultViews(gedcomBeingLoaded);
      }

      // done
    }

    public static void openDefaultViews(Gedcom ged) {

        Preferences prefs = NbPreferences.forModule(GenjViewTopComponent.class);
        List<String> openedViews = new ArrayList<String>();


        for (int i = 0; i<20; i++){
            String item = prefs.get("openViews" + i, null);
            if (item == null)
                break;
            openedViews.add(item);
        }
        if (openedViews.isEmpty()){
            openedViews.add("genjfr.app.TableTopComponent");
            openedViews.add("genjfr.app.TreeTopComponent");
            openedViews.add("genjfr.app.EditTopComponent");
        }

        GenjViewTopComponent tc = null;
        for (String className: openedViews){
            try {
                tc = (GenjViewTopComponent) Class.forName(className).newInstance();
                tc.init(ged);
                tc.open();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (tc != null) {
            tc.requestActive();
        }
      }

    /**
     * choose a file
     */
    private Origin chooseExisting() {
      // ask user
      File file = App.center.chooseFile(resources.getString("cc.open.title"), resources.getString("cc.open.action"), null);
      if (file == null)
        return null;
      // remember last directory
      App.center.registry.put("last.dir", file.getParentFile().getAbsolutePath());
      // form origin
      try {
        return Origin.create(new URL("file", "", file.getAbsolutePath()));
      } catch (MalformedURLException e) {
        return null;
      }
      // done
    }

    /**
     * choose a url
     */
    private Origin chooseURL() {

      // pop a chooser
      String[] choices = (String[])App.center.registry.get("urls", new String[0]);
      ChoiceWidget choice = new ChoiceWidget(choices, "");
      JLabel label = new JLabel(resources.getString("cc.open.enter_url"));

      int rc = windowManager.openDialog(null, resources.getString("cc.open.title"), WindowManager.QUESTION_MESSAGE, new JComponent[]{label,choice}, Action2.okCancel(), App.center);

      // check the selection
      String item = choice.getText();
      if (rc!=0||item.length()==0) return null;

      // Try to form Origin
      Origin origin;
      try {
        origin = Origin.create(item);
      } catch (MalformedURLException ex) {
        windowManager.openDialog(null, item, WindowManager.ERROR_MESSAGE, resources.getString("cc.open.invalid_url"), Action2.okCancel(), App.center);
        return null;
      }

      // Remember URL for dialog
      Set remember = new HashSet();
      remember.add(item);
      for (int c=0; c<choices.length&&c<9; c++) {
        remember.add(choices[c]);
      }
      App.center.registry.put("urls", remember);

      // ... continue
      return origin;
    }

    /**
     * Open Origin - continue with (async) execute if true
     */
    private boolean open(Origin origin) {

      // Check if already open
      if (GedcomDirectory.getInstance().getGedcom(origin.getName())!=null) {
          if (!mustOpenDefaultViews)
        windowManager.openDialog(null,origin.getName(),WindowManager.ERROR_MESSAGE,resources.getString("cc.open.already_open", origin.getName()),Action2.okOnly(),App.center);
        return false;
      }

      // Open Connection and get input stream
      try {

        // .. prepare our reader
        reader = new GedcomReader(origin);

        // .. set password we're using
        reader.setPassword(password);

      } catch (IOException ex) {
        String txt =
          resources.getString("cc.open.no_connect_to", origin)
            + "\n["
            + ex.getMessage()
            + "]";
        windowManager.openDialog(null, origin.getName(), WindowManager.ERROR_MESSAGE, txt, Action2.okOnly(), App.center);
        return false;
      }

      // .. show progress dialog
      progress = windowManager.openNonModalDialog(
        null,
        resources.getString("cc.open.loading", origin.getName()),
        WindowManager.INFORMATION_MESSAGE,
        new ProgressWidget(reader, getThread()),
        Action2.cancelOnly(),
        App.center
      );

      // .. continue into (async) execute
      return true;
    }

  } //ActionOpen
