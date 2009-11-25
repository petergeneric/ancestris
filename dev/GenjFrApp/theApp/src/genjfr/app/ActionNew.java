/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app;

import genj.app.Images;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomDirectory;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import genj.gedcom.Submitter;
import genj.util.EnvironmentChecker;
import genj.util.Origin;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.window.WindowManager;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public final class ActionNew extends Action2 {

      private Resources resources = Resources.get(genj.app.ControlCenter.class);
      private WindowManager windowManager = App.center.getWindowManager();


    /** constructor */
   public ActionNew() {
//      setAccelerator(ACC_NEW);
      setText(resources, "cc.menu.new" );
      setTip(resources, "cc.tip.create_file");
      setImage(Images.imgNew);
    }

    /** execute callback */
    protected void execute() {

        // let user choose a file
        File file = App.center.chooseFile(resources.getString("cc.create.title"), resources.getString("cc.create.action"), null);
        if (file == null)
          return;
        if (!file.getName().endsWith(".ged"))
          file = new File(file.getAbsolutePath()+".ged");
        if (file.exists()) {
          int rc = windowManager.openDialog(
            null,
            resources.getString("cc.create.title"),
            WindowManager.WARNING_MESSAGE,
            resources.getString("cc.open.file_exists", file.getName()),
            Action2.yesNo(),
            App.center
          );
          if (rc!=0)
            return;
        }
        // form the origin
        try {
          Gedcom gedcom  = new Gedcom(Origin.create(new URL("file", "", file.getAbsolutePath())));
          // create default entities
          try {
            Indi adam = (Indi)gedcom.createEntity(Gedcom.INDI);
            adam.addDefaultProperties();
            adam.setName("Adam","");
            adam.setSex(PropertySex.MALE);
            Submitter submitter = (Submitter)gedcom.createEntity(Gedcom.SUBM);
            submitter.setName(EnvironmentChecker.getProperty(this, "user.name", "?", "user name used as submitter in new gedcom"));
          } catch (GedcomException e) {
          }
          // remember
          GedcomDirectory.getInstance().registerGedcom(gedcom);
        } catch (MalformedURLException e) {
        }

    }

  } //ActionNew
