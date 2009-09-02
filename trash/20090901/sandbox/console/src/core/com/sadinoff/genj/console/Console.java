/**
 * Console.java
 * $Header: /cvsroot/genj/sandbox/console/src/core/com/sadinoff/genj/console/Console.java,v 1.26 2006/05/22 07:01:25 sadinoff Exp $
 *
 * A client of the SF genj GEDCOM model which providedes a text UI to 
 * browsing and editing gedcom.
 * 
 * This module is dedicated to the memory of Anne Cohen Rezak. 
 * 
 
 ** This program is licenced under the GNU license, v 2.0
 *  AUTHOR: Danny Sadinoff
 */

package com.sadinoff.genj.console;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.io.GedcomReader;
import genj.io.GedcomWriter;
import genj.util.Origin;
import genj.util.Resources;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Console {
    
    private static final boolean SOUND = Boolean.getBoolean("console.sound");//experimental feature //$NON-NLS-1$
    protected static final String LB = System.getProperty("line.separator"); //$NON-NLS-1$
    private static final boolean DEBUG = Boolean.getBoolean("console.debug");// $NON-NLS-1$
    

    /** i18n resources */
    private Resources resources = Resources.get(this);
    protected final Gedcom gedcom;
    protected final LineSource in;
    protected final PrintWriter out;

    /**
     * User-visible string to use to describe the sex of an Individual
     */
    protected  final Map<Integer, String> sexMap  = new HashMap<Integer,String>();
    {
        sexMap.put(PropertySex.MALE,resources.getString("sex-indicator.male")); //$NON-NLS-1$
        sexMap.put(PropertySex.FEMALE,resources.getString("sex-indicator.female")); //$NON-NLS-1$
        sexMap.put(PropertySex.UNKNOWN,resources.getString("sex-indicator.unknown")); //$NON-NLS-1$
    }

    /**
     * Constructor for a Console session.
     * @gedcomArg the Gedcom to be edited.
     * @param userInput the BufferedReader from whence to fetch the typed input
     * @param output Where to send user output.  As of right now, warnings go here too.
     */
    public Console(Gedcom gedcomArg, final BufferedReader userInput, final PrintWriter output) {
        out = output;
        in = new BufferedReaderSource(userInput, out);
        gedcom = gedcomArg;
        setPrompt();
    }

    private void setPrompt()
    {
        in.setPrompt("> ");    
    }
    
    public Console(Gedcom gedcomArg, boolean useReadLine) throws IOException
    {
        gedcom = gedcomArg;
        out = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8")); //$NON-NLS-1$
        if( useReadLine )
        {
            ReadLineSource rlSource = new ReadLineSource();
            in = rlSource;
        }
        else
        {
            in = new BufferedReaderSource(new BufferedReader(new InputStreamReader(System.in,"UTF-8")), out);  //$NON-NLS-1$
        }
        setPrompt();
    }
    /**
     * Constructor for a Console session attached to System.in and System.out
     * @param gedcomArg the Gedcom to be edited.
     * @throws IOException 
     */
    public Console(Gedcom gedcomArg) throws IOException {
         this(gedcomArg,Boolean.getBoolean("console.use-readline"));
    }

    
    /**
     * experimental sound support...
     *      
     */
    enum UIFeedbackType{ SYNTAX_ERROR, MOTION_SIDEWAYS, MOTION_UP, MOTION_DOWN, MOTION_SPOUSE,
                       MOTION_HYPERSPACE,
                       HIT_WALL,
                       SET_VALUE,
                       NOT_FOUND,
                       STALL,
                       MISSILE_LOCK,
                       INTERSECT_GRANITE_CLOUD, //general failure
    };

    /**
     * Provide the user feedback that an even occurred
     * @param event
     */
    public void giveFeedback(UIFeedbackType event) {
        if (! SOUND)
            return;
      switch(  event)
      {
      case SYNTAX_ERROR:
          AudioUtil.play(resources.getString("soundfile.syntax-error")); //$NON-NLS-1$
          break;
      case MOTION_HYPERSPACE:
          AudioUtil.play(resources.getString("soundfile.jump")); //$NON-NLS-1$
          break;
      case HIT_WALL:
          AudioUtil.play(resources.getString("soundfile.hit-wall")); //$NON-NLS-1$
          break;
      case SET_VALUE:
          AudioUtil.play(resources.getString("soundfile.set-value")); //$NON-NLS-1$
          break;
      default:
      }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)  
        throws Exception
    {
        Resources resources = Resources.get("com.sadinoff.genj.console"); //$NON-NLS-1$

        if( args.length< 1)
        {
            System.err.println(resources.getString("usage")+ Console.class.getName() +" filename "); //$NON-NLS-1$ //$NON-NLS-2$
            System.err.println("       java [classpath_options] "+ Console.class.getName() +" -u URL"); //$NON-NLS-1$ //$NON-NLS-2$
            System.exit(1);
        }

        Origin origin;
        if( args.length ==2 )
        {
            if(! args[0].equals("-u")) //$NON-NLS-1$
            {
                System.err.println(resources.getString("startup.unknown-option")+args[0]); //$NON-NLS-1$
                System.err.println("usage: java [classpath_options] "+ Console.class.getName() +" filename "); //$NON-NLS-1$ //$NON-NLS-2$
                System.err.println("       java [classpath_options] "+ Console.class.getName() +" -u URL"); //$NON-NLS-1$ //$NON-NLS-2$
                System.exit(1);
            }
            URL url = new URL(args[0]);
            origin = Origin.create(url);
        }
        else
        {
            origin = Origin.create(new File(args[0]).toURL());
        }
        // read the gedcom file

        GedcomReader reader = new GedcomReader(origin);
        Gedcom gedcom = reader.read();
        Console tt = new Console(gedcom);
        tt.go();
    }

    interface Action
    {
        enum ArgType{ARG_NO, ARG_YES,ARG_OPTIONAL};
        Indi doIt(Indi theIndi, String arg) throws Exception;
        String getDoc();
        ArgType getArgUse();
        String getArgName();
        boolean modifiesDatamodel();
    }

    abstract class ActionHelper implements Action
    {
        public ArgType getArgUse(){ return ArgType.ARG_NO; }
        public String getArgName(){ return null; }
        public boolean modifiesDatamodel() { return false; } 
    }

    /*
     * @param arg string to be parsed
     * @default the value to be returned if it's null
     * @throws NumberParseException on numberparse error.
     */
    final protected int parseInt(String arg, int nullDefault) {
        if (null == arg)
                return nullDefault;

        return Integer.parseInt(arg);
    }

    private List<String> resourceGetList(String key, String[] defaultList)
    {
        List<String> ret = new ArrayList<String>();
        for(int i=1; ;i++)
        {
            String actualKey = key +"."+i; //$NON-NLS-1$
            String val = resources.getString(actualKey,false);
            if( null == val)
                break;
            ret.add(val);
        }
        if( ret.size() == 0)
            return Arrays.asList(defaultList);
        return ret;
    }
    
    private List<String> resourceGetList(String key, String defaultSingleVal)
    {
        return resourceGetList(key, new String[] {defaultSingleVal});
    }
    
    /**
     * fetch the  
     * @return A Sorted Map of 
     */
    protected Map<List<String>,Action> getActionMap()
    {
        final Map<List<String>,Action>  actionMap = new LinkedHashMap<List<String>,Action>();
        

        actionMap.put(resourceGetList("version.command", "version"), new ActionHelper(){public Indi doIt(Indi ti, String arg){ //$NON-NLS-1$ //$NON-NLS-2$
            out.println(getVersion());
            return ti;}
        public String getDoc() {return resources.getString("version.help");} //$NON-NLS-1$
            });
        
        
        actionMap.put(resourceGetList("help.command", "help"), new ActionHelper(){public Indi doIt(Indi ti, String arg){ //$NON-NLS-1$ //$NON-NLS-2$
            out.println(getHelpText(actionMap));
            return ti;}
        public String getDoc() {return resources.getString("help.help", "print this help message");} //$NON-NLS-1$ //$NON-NLS-2$
            });
        
        
        actionMap.put(resourceGetList("exit.command", "exit"), new ActionHelper(){ //$NON-NLS-1$ //$NON-NLS-2$
            public Indi doIt(Indi ti, String arg) throws IOException  {
                if( ! gedcom.hasUnsavedChanges())
                    System.exit(0);
                out.println();
                out.print(resources.getString("exit.unsaved-changes")); //$NON-NLS-1$
                out.flush();
                String line = in.readLine();
                if( null != line && line.toLowerCase().startsWith(resources.getString("yesno.yes")))  //$NON-NLS-1$
                    System.exit(0);
                out.println(resources.getString("exit.try-save-filename")); //$NON-NLS-1$
                return ti;
            }
                public String getDoc(){return resources.getString("exit.help", "quit the program");} //$NON-NLS-1$ //$NON-NLS-2$
            });

        

        
        actionMap.put(resourceGetList("save.command","save"), new Action(){ //$NON-NLS-1$ //$NON-NLS-2$
            
            public Indi doIt(Indi ti, String arg){
                try{
                    File saveTo = new File( arg );
                    String  fname = saveTo.getName();
                    File canon = saveTo.getCanonicalFile();
                    File parentdir = canon.getParentFile();
                    File tempFile = File.createTempFile(fname,"",parentdir); //$NON-NLS-1$
                    
                    OutputStream fos = new BufferedOutputStream(new FileOutputStream(tempFile));
                    
                    GedcomWriter writer = new GedcomWriter(gedcom,arg,null,fos);                    
                    writer.write(); //closes fos
                    if(!  tempFile.renameTo(saveTo) )
                        throw new Exception(resources.getString("save.error.unable-to-rename")+tempFile+" to "+saveTo); //$NON-NLS-1$ //$NON-NLS-2$
                    out.println(resources.getString("save.wrote-file-successfully",arg)); //$NON-NLS-1$ //$NON-NLS-2$
                    out.println(resources.getString("save.remember")); //$NON-NLS-1$
                    gedcom.setUnchanged();
                }
                catch( Exception e)
                {
                    out.println(resources.getString("save.error.io-error")+e); //$NON-NLS-1$
                    giveFeedback(UIFeedbackType.INTERSECT_GRANITE_CLOUD);
                }
                return ti;
            }
            public boolean modifiesDatamodel() { return true; } 

        public String getDoc() { return resources.getString("save.help"); } //$NON-NLS-1$
        public ArgType getArgUse() { return ArgType.ARG_YES; }
        public String getArgName() { return resources.getString("save.arg");} //$NON-NLS-1$
        });
        
        actionMap.put(resourceGetList("undo.command","undo"), new Action(){ //$NON-NLS-1$
            
            public Indi doIt(Indi ti, String arg){
                String oldID = ti.getId();
                try
                {
                    gedcom.undo();
                }
                catch( Exception e)
                {
                    out.println(resources.getString("undo.error.couldn't-undo")+ e); //$NON-NLS-1$
                    giveFeedback(UIFeedbackType.INTERSECT_GRANITE_CLOUD);
                }
                
                //if the last operation was a create...                
                ti = (Indi)gedcom.getEntity(oldID);
                //TODO go back to previous location.
                if( null == ti )
                    out.println(resources.getString("undo.warn.returning-to-root")); //$NON-NLS-1$
                return (Indi)gedcom.getFirstEntity(Gedcom.INDI);
            }
            public boolean modifiesDatamodel() { return false; } 

        public String getDoc() { return resources.getString("undo.help");} //$NON-NLS-1$
        public ArgType getArgUse() { return ArgType.ARG_NO; }
        public String getArgName() { return "";} //$NON-NLS-1$
        });

        actionMap.put(resourceGetList("look.command","look"), new ActionHelper() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    public Indi doIt(final Indi ti ,final String targetID){
                        if( targetID != null && targetID.length()>0)
                        {
                            Indi target = (Indi)gedcom.getEntity("INDI", targetID); //$NON-NLS-1$
                            if( null == target)
                                out.println(resources.getString("look.no-record")); //$NON-NLS-1$
                            else
                                out.println(dump(target));
                        }
                        else
                            out.println(dump(ti));
                        return ti;
                    }
                    public String getDoc(){return resources.getString("look.help");} //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_OPTIONAL; }
                    public String getArgName() { return resources.getString("look.arg");} //$NON-NLS-1$
                    });        
        
        actionMap.put(resourceGetList("gind.command","gind"), new Action() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    public Indi doIt(final Indi ti ,final String targetID){
                        Entity  newEntity = gedcom.getEntity("INDI", targetID); //$NON-NLS-1$
                        if (null == newEntity )
                        {
                            out.println(resources.getString("error.can't-find-entity")+targetID); //$NON-NLS-1$
                            return ti;
                        }
                        Indi newInd = (Indi)newEntity;
                        giveFeedback(UIFeedbackType.MOTION_HYPERSPACE);                                                    
                        return newInd;
                    }
                    public boolean modifiesDatamodel() { return false; } 
                    public String getDoc(){return resources.getString("goto.help");} //$NON-NLS-1$
                    public ArgType getArgUse() {  return ArgType.ARG_YES;}
                    public String getArgName() {  return resources.getString("goto.arg"); } //$NON-NLS-1$
                });

        actionMap.put(resourceGetList("search.command","search"), new Action() //$NON-NLS-1$ //$NON-NLS-2$
                {
                    public Indi doIt(final Indi ti ,final String searchArg){
                        out.println(resources.getString("search.results.start")); //$NON-NLS-1$
                        for( Object entity : gedcom.getEntities("INDI")) //$NON-NLS-1$
                        {
                            Indi candidate = (Indi)entity;
                            if( candidate.getName().toLowerCase().contains(searchArg.toLowerCase()))
                                out.println("  "+candidate); //$NON-NLS-1$
                        }
                        out.println(resources.getString("search.results.end")); //$NON-NLS-1$
                        out.println();
                        return ti;
                    }
                    public boolean modifiesDatamodel() { return false; } 
                    public String getDoc(){return resources.getString("search.help");} //$NON-NLS-1$
                    public ArgType getArgUse() {  return ArgType.ARG_YES;}
                    public String getArgName() {  return resources.getString("search.arg"); } //$NON-NLS-1$
                });
        
        
        actionMap.put(resourceGetList("gdad.command","gdad"), new ActionHelper() //$NON-NLS-1$ //$NON-NLS-2$
                {
                    public Indi doIt(final Indi ti , String arg){
                        Indi dad = ti.getBiologicalFather();
                        if( null == dad)
                        {   
                            out.println(resources.getString("gdad.error.no-dad")); //$NON-NLS-1$
                            giveFeedback(UIFeedbackType.HIT_WALL);                            
                            return ti;
                        }
                        else
                            return dad;
                    }
                    public String getDoc(){return resources.getString("gdad.help");} //$NON-NLS-1$
                });        
        
        actionMap.put(resourceGetList("gmom.command","gmom"), new ActionHelper() //$NON-NLS-1$ //$NON-NLS-2$
                {
                    public Indi doIt(final Indi ti, String arg){
                        Indi mom = ti.getBiologicalMother();
                        if( null == mom)
                        {   
                            out.println(resources.getString("gmom.error.nomom")); //$NON-NLS-1$
                            giveFeedback(UIFeedbackType.HIT_WALL);                            
                            return ti;
                        }
                        else
                            return mom;
                    }
                    public String getDoc(){return resources.getString("gmom.help");} //$NON-NLS-1$
                });
        
        actionMap.put(resourceGetList("gspo.command","gspo"),new Action() //$NON-NLS-1$ //$NON-NLS-2$
                {

                    public Indi doIt(Indi theIndi, String arg) {
                        Fam[] marriages = theIndi.getFamiliesWhereSpouse();
                        if( marriages.length ==0)
                        {
                            out.println(resources.getString("gspo.error.notmarried")); //$NON-NLS-1$
                            giveFeedback(UIFeedbackType.HIT_WALL);                            
                            return theIndi;
                        }
                        
                        int targetMarriage;
                        try
                        {
                            int marriageArg = parseInt(arg,1);
                            targetMarriage = marriageArg -1;
                        }
                        catch(NumberFormatException nfe)
                        {
                            out.println(resources.getString("gspo.error.cantparse")+arg+" as a number"); //$NON-NLS-1$ //$NON-NLS-2$
                            giveFeedback(UIFeedbackType.SYNTAX_ERROR);
                            return theIndi;
                        }
                        return  marriages[targetMarriage].getOtherSpouse(theIndi);
                    }

                    public boolean modifiesDatamodel() { return false; } 
                    
                    public String getDoc() {return resources.getString("gspo.help");} //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_OPTIONAL;}
                    public String getArgName() { return resources.getString("gspo.arg");} //$NON-NLS-1$
                    });

        
        actionMap.put(resourceGetList("gsib.command","gsib"), new Action() //$NON-NLS-1$
                {
                    public Indi doIt(Indi theIndi, String arg) {
                        Fam bioKidFamily = theIndi.getFamilyWhereBiologicalChild();
                        if( null == bioKidFamily)
                        {
                            out.println(resources.getString("gsib.not-a-kid-in-biofamily")); //$NON-NLS-1$
                            giveFeedback(UIFeedbackType.HIT_WALL);                            
                            return theIndi;
                        }
                        Indi[]sibs =  bioKidFamily.getChildren();
                        if( arg == null || arg.length() == 0)
                        {
                            //find the next kid in the family;
                            int myIndex =-1; 
                            for( int i =0; i<sibs.length; i++)
                                if( sibs[i]==theIndi)  //somewhat risky. 
                                    myIndex =i;
                            if( myIndex == -1)
                            {
                                out.println(resources.getString("gsib.error.cant-find-myself")); //$NON-NLS-1$
                                return theIndi;
                            }
                            return sibs[(myIndex+1)%sibs.length];
                        }
                        else
                        {//return specified sib
                            try 
                            {
                                int kidNumber = parseInt(arg, 0);
                                if( kidNumber <1 || kidNumber > sibs.length)
                                {
                                    out.println("bad sib number"); //$NON-NLS-1$
                                    giveFeedback(UIFeedbackType.NOT_FOUND);
                                    return theIndi;
                                }
                                return sibs[kidNumber-1];
                            }
                            catch(NumberFormatException nfe)
                            {
                                out.println(resources.getString("error.cant-parse-arg-as-number")+arg+" as a number"); //$NON-NLS-1$ //$NON-NLS-2$
                                giveFeedback(UIFeedbackType.SYNTAX_ERROR);                                
                                return theIndi;
                            }
                        }
                    }
                    public boolean modifiesDatamodel() { return false; } 
                    public String getDoc() {    return resources.getString("gsib.help");}  //$NON-NLS-1$
                    public ArgType getArgUse() {return ArgType.ARG_OPTIONAL;}
                    public String getArgName() { return "N";} //$NON-NLS-1$

                });
   
        actionMap.put(resourceGetList("gchi.command","gchi"), new Action() //$NON-NLS-1$ //$NON-NLS-2$
                {
                    
                public String getDoc() {    return resources.getString("gkid.help");} //$NON-NLS-1$
                public ArgType getArgUse() {return ArgType.ARG_OPTIONAL;}
                public String getArgName() { return resources.getString("gkid.arg");} //$NON-NLS-1$
                public boolean modifiesDatamodel() { return false; } 
            public Indi doIt(Indi theIndi, String arg) {
                
                // FIX: M'th marriage not implemented.
                Indi[] children=  theIndi.getChildren();
                if(0==children.length)
                {
                    out.println(resources.getString("gkid.error.no-kids"));  //$NON-NLS-1$
                    giveFeedback(UIFeedbackType.HIT_WALL);                            
                    return theIndi;
                }
                if( null == arg || arg.length()==0)
                    return children[0];
                try 
                {
                    if( Character.isDigit(arg.charAt(0)))
                    {
                        int kidNumber = Integer.parseInt(arg);
                        if( kidNumber <1 || kidNumber > children.length)
                        {
                            out.println(resources.getString("gkid.error.bad-sib-number"));  //$NON-NLS-1$
                            giveFeedback(UIFeedbackType.NOT_FOUND);                            
                            return theIndi;
                        }
                        return children[kidNumber-1];
                    }
                    else
                    {
                        Indi foundKid =null;
                        for( int i =0; i< children.length;i++)
                        {
                            Indi kid = children[i];
                            if( ! kid.getFirstName().equals(arg))
                                continue;
                            if( null== foundKid)
                                foundKid = kid;
                            else
                            {
                                out.println(resources.getString("gkid.error.two-or-more-kids-named",arg)); //$NON_NLS-1$
                                giveFeedback(UIFeedbackType.NOT_FOUND);
                                return theIndi;
                            }
                        }
                        if (null == foundKid)
                        {
                            out.println(resources.getString("gkid.error.no-kid-named",arg)); //$NON_NLS-1$
                            giveFeedback(UIFeedbackType.NOT_FOUND);
                            return theIndi;
                        }
                        return foundKid;
                    }
                }
                catch(NumberFormatException nfe)
                {
                    out.println(resources.getString("gkid.error.cant-parse-arg")+arg+"] as a number"); //$NON-NLS-1$ //$NON-NLS-2$
                    giveFeedback(UIFeedbackType.SYNTAX_ERROR);
                }
                return theIndi;
            }
                });
        

        actionMap.put(resourceGetList("cbro.command","cbro"), new Action() //$NON-NLS-1$ //$NON-NLS-2$
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        Indi newSib =  createBiologicalSibling(ti,PropertySex.MALE);
                        if(null != arg && arg.length() > 0)
                            setFirstName(newSib, arg);
                        return newSib;
                    }

                    public String getDoc(){return resources.getString("cbro.help");}  //$NON-NLS-1$
                    public ArgType getArgUse() {return ArgType.ARG_OPTIONAL;}
                    public String getArgName() {return "FNAME";} //$NON-NLS-1$
                    public boolean modifiesDatamodel() { return true; } 
                });

        actionMap.put(resourceGetList("csis.command","csis"), new Action() //$NON-NLS-1$
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        Indi newSib =  createBiologicalSibling(ti,PropertySex.FEMALE);
                        if(null != arg && arg.length() > 0)
                            setFirstName(newSib, arg);
                        return newSib;
                    }
                        
                    public String getDoc(){return "Create a biological sister [with first name FNAME]";} //$NON-NLS-1$
                    public ArgType getArgUse() {return ArgType.ARG_OPTIONAL;}
                    public String getArgName() {return "FNAME";} //$NON-NLS-1$
                    public boolean modifiesDatamodel() { return true; } 
                });

        actionMap.put(resourceGetList("cson.command","cson"), new Action() //$NON-NLS-1$
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        int marriageNumber =1;
                        boolean numeric = false;
                        if( null == arg || arg.length() ==0 || Character.isDigit(arg.charAt(0)))
                        {
                            marriageNumber = parseInt(arg,1);
                            numeric = true;
                        }
                        Indi kid = createChild(ti,marriageNumber-1,PropertySex.MALE);
                        if( ! numeric )
                            setFirstName(kid,arg);
                        return kid;
                    }
                        
                    public String getDoc(){return resources.getString("cson.help");} //$NON-NLS-1$

                    public ArgType getArgUse() {return ArgType.ARG_OPTIONAL;}

                    public String getArgName() {return "N/FNAME";} //$NON-NLS-1$
                    public boolean modifiesDatamodel() { return true; } 
                });
        
        actionMap.put(resourceGetList("cdaut.command","cdaut"), new Action(){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            public Indi doIt(final Indi ti, String arg) throws GedcomException{
                int marriageNumber =1 ;
                boolean numeric = false;
                if( null == arg || arg.length() ==0 || Character.isDigit(arg.charAt(0)))
                {
                    marriageNumber = parseInt(arg,1);
                    numeric = true;
                }
                Indi kid = createChild(ti,marriageNumber-1,PropertySex.FEMALE);
                if( ! numeric )
                    setFirstName(kid,arg);
                return kid;

            }
                    
            public String getDoc(){return resources.getString("cdau.help");}  //$NON-NLS-1$
            public ArgType getArgUse() { return ArgType.ARG_OPTIONAL;}
            public String getArgName() { return "N/FNAME";} //$NON-NLS-1$
            public boolean modifiesDatamodel() { return true; } 
        });

        
        actionMap.put(resourceGetList("cspou.command","cspou"), new Action() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        Indi spouse = createFamilyAndSpouse(ti);
                        if(null != arg && arg.length() > 0 )
                            setFirstName(spouse,arg);
                        return spouse;
                    }
                    public String getDoc(){return resources.getString("cspo.help");} //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_OPTIONAL;}
                    public String getArgName() { return "FNAME";} //$NON-NLS-1$
                    public boolean modifiesDatamodel() { return true; } 
                });

        
        actionMap.put(resourceGetList("cdad.command","cdad"), new Action() //$NON-NLS-1$
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        Indi parent= createParent(ti,PropertySex.MALE);
                        if(null != arg && arg.length() > 0 )
                            setFirstName(parent,arg);
                        return parent;
                    }
                    public String getDoc(){return resources.getString("cdad.help");} //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_OPTIONAL;}
                    public String getArgName() { return "FNAME";} //$NON-NLS-1$
                    public boolean modifiesDatamodel() { return true; } 
                });
        actionMap.put(resourceGetList("cmom.command","cmom"), new Action() //$NON-NLS-1$
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        Indi parent= createParent(ti,PropertySex.FEMALE);
                        if(null != arg && arg.length() > 0 )
                            setFirstName(parent,arg);
                        return parent;
                    }
                    public String getDoc(){return "Create and goto a mother [with first name FNAME]";} //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_OPTIONAL;}
                    public String getArgName() { return "FNAME";} //$NON-NLS-1$
                    public boolean modifiesDatamodel() { return true; } 
                });

                
        actionMap.put(resourceGetList("rsib.command","rsib"), new Action(){ //$NON-NLS-1$ //$NON-NLS-2$
                    public Indi doIt(final Indi ti, final String existingSibID) throws GedcomException{
                        Fam theFam = getCreateBiologicalFamily(ti);
                        Indi existingSib = (Indi)gedcom.getEntity("INDI", existingSibID); //$NON-NLS-1$
                        if (null == existingSib)
                        {
                            System.out.println(resources.getString("error.can't-find-individual-named",new Object[]{existingSibID})); //$NON-NLS-1$
                            return ti;
                        }
                        Fam existingFam = existingSib.getFamilyWhereBiologicalChild();
                        if( null != existingFam )
                        {
                            out.println(resources.getString("rsib.error-already-in-family",new Object[] {existingSib,ti, existingFam})); //$NON-NLS-1$
                            return ti;
                        }
                        theFam.addChild(existingSib);
                        return existingSib;
                    }
                    public String getDoc(){return resources.getString("rsib.help");} //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_YES;}
                    public String getArgName() { return resources.getString("rsib.arg");} //$NON-NLS-1$
                    public boolean modifiesDatamodel() { return true; } 
                });        
        
        actionMap.put(resourceGetList("del.command","del"), new ActionHelper() //$NON-NLS-1$ //$NON-NLS-2$
                {
                    public Indi doIt(final Indi ti, String arg) throws GedcomException{
                        Fam[] famsc = ti.getFamiliesWhereChild();
                        Fam[] famss = ti.getFamiliesWhereSpouse();
                        gedcom.deleteEntity(ti);
                        for (Fam fam : famsc)
                            fam.getChildren();
                        for(Fam fam: famss)
                        {
                            fam.getHusband();
                            fam.getWife();
                        }
                            
                        //FIX handle empty database.
                      out.println(resources.getString("del.returning-to-root")); //$NON-NLS-1$
                      return (Indi)gedcom.getFirstEntity(Gedcom.INDI);
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc(){return resources.getString("del.help");} //$NON-NLS-1$
                });
        
        

        actionMap.put(resourceGetList("sname.command","sname"), new Action() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
            final Pattern firstLastPat = Pattern.compile("((\\S+\\s+)+)(\\S+)"); //$NON-NLS-1$
                    public Indi doIt(Indi theIndi, String arg) {
                        Matcher firstLastMatcher = firstLastPat.matcher(arg);
                        if( ! firstLastMatcher.find())
                        {
                            giveFeedback(UIFeedbackType.SYNTAX_ERROR);
                            out.println(resources.getString("snam.error.syntax"));  //$NON-NLS-1$
                        }
                        String first = firstLastMatcher.group(1).trim();
                        String last = firstLastMatcher.group(3);
                        theIndi.setName(first, last);
                        return theIndi;
                    }
                    
                    public String getDoc() {return resources.getString("snam.help");}  //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_YES;}
                    public String getArgName() { return resources.getString("snam.arg"); } //$NON-NLS-1$
                    public boolean modifiesDatamodel() { return true; 
                    } 
                });

        actionMap.put(resourceGetList("sfnm.command","sfnm"), new Action() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    public Indi doIt(Indi theIndi, String arg) {
                        theIndi.setName(arg,theIndi.getLastName());
                        return theIndi;
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc() { return resources.getString("fnam.help");}  //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_YES ; }
                    public String getArgName() { return resources.getString("fnam.arg");}  //$NON-NLS-1$
                });
        actionMap.put(resourceGetList("slnm.command","slnm"), new Action() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    public Indi doIt(Indi theIndi, String arg) {
                        theIndi.setName(theIndi.getFirstName(), arg);
                        return theIndi;
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc() { return resources.getString("lnam.help");}  //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_YES ; }
                    public String getArgName() { return resources.getString("lnam.arg");} //$NON-NLS-1$
                });

        actionMap.put(resourceGetList("ssex.command","ssex"), new Action() //$NON-NLS-1$ //$NON-NLS-2$
                {
                    public Indi doIt(Indi theIndi, String newSex) {
                        newSex = newSex.toLowerCase();
                        if(newSex.equals(resources.getString("ssex.M")))  //$NON-NLS-1$
                            theIndi.setSex(PropertySex.MALE);
                        else if( newSex.equals(resources.getString("ssex.F")))  //$NON-NLS-1$
                            theIndi.setSex(PropertySex.FEMALE);
                        else if(newSex.equals(resources.getString("ssex.U")))  //$NON-NLS-1$
                            theIndi.setSex(PropertySex.UNKNOWN);
                        else
                            out.println(resources.getString("ssex.error.input"));  //$NON-NLS-1$
                        return theIndi;
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc() { return resources.getString("ssex.help");} //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_YES ; }
                    public String getArgName() { return "S";} //$NON-NLS-1$
                });

        actionMap.put(resourceGetList("bday.command","bday"), new Action() //$NON-NLS-1$ //$NON-NLS-2$
                {
                    public Indi doIt(Indi theIndi, String arg) {
                        PropertyDate date =theIndi.getBirthDate(true) ;
                        setDate(date, arg);
                        return theIndi;
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc() { return resources.getString("bday.help");}  //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_YES ; }
                    public String getArgName() { return "BDAY";} //$NON-NLS-1$
                });


        actionMap.put(resourceGetList("dday.command","dday"), new Action() //$NON-NLS-1$ //$NON-NLS-2$
                {
                    public Indi doIt(Indi theIndi, String arg) {
                        PropertyDate date =theIndi.getDeathDate(true) ;
                        setDate(date, arg);
                        return theIndi;
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc() { return resources.getString("dday.help");} //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_YES ; }
                    public String getArgName() { return resources.getString("dday.arg");}  //$NON-NLS-1$
                });

        
        actionMap.put(resourceGetList("mday.command","mday"), new Action() //$NON-NLS-1$ //$NON-NLS-2$
                {
                    final Pattern mdayPat = Pattern.compile("(?:#(\\d+)\\s+)?(\\p{Alnum}.*)"); 

                    public Indi doIt(Indi theIndi, String arg) {
                        Matcher matcher = mdayPat.matcher(arg);
                        if( ! matcher.matches())
                        {
                            giveFeedback(UIFeedbackType.SYNTAX_ERROR);
                            out.println("arg to mday has bad syntax! [" +arg+"]");
                            return theIndi;
                        }
                        final String marNumStr = matcher.group(1);
                        final String mdateArg  = matcher.group(2);
                        final int marNumArg = parseInt(marNumStr, 1);
                        final int familyIndex = marNumArg -1;
                        Fam[] fams = theIndi.getFamiliesWhereSpouse();
                        if( fams.length <1)
                        {
                            giveFeedback(UIFeedbackType.NOT_FOUND);
                            out.println("This Individual is not a spouse in a Family");
                            return theIndi;
                        }
                        if( marNumArg > fams.length)
                        {
                            giveFeedback(UIFeedbackType.NOT_FOUND);
                            out.println("Marriage/Family number is out of range:"+ marNumArg);
                            return theIndi;
                        }
                        Fam theFam = fams[familyIndex];
                        PropertyDate mdateProperty = theFam.getMarriageDate(true);
                        setDate(mdateProperty, mdateArg);
                        return theIndi;
                    }
                    public boolean modifiesDatamodel() { return true; } 
                    public String getDoc() { return resources.getString("mday.help");} //$NON-NLS-1$
                    public ArgType getArgUse() { return ArgType.ARG_YES ; }
                    public String getArgName() { return resources.getString("mday.arg");}  //$NON-NLS-1$
                });
        
        
        return actionMap;
    }
    
    
    public void go()  throws Exception{
        Indi theIndi = (Indi)gedcom.getFirstEntity(Gedcom.INDI);

        final Map<List<String>,Action> actionMap = getActionMap();
        final Map<String, Action> commandToAction= expandActionMap(actionMap);

        final Pattern commandPat = Pattern.compile("^(\\p{Alnum}+)(\\s+(\\S.*))?"); //$NON-NLS-1$
        for(;;)
        {
            out.println("------"); //$NON-NLS-1$
            out.print(resources.getString("you-are-at")); //$NON-NLS-1$
            out.println(brief(theIndi));
            out.flush();
            final String line;
            {
                final String inputResult = in.readLine();
                if( null==inputResult)
                    continue;
                line = inputResult.trim();
            }
            if( line.length() ==0)
                continue;
            Matcher lineMatcher = commandPat.matcher(line);
            if( ! lineMatcher.matches())
            {
                giveFeedback(UIFeedbackType.SYNTAX_ERROR);
                out.println(resources.getString("error.syntax-error")); //$NON-NLS-1$
                continue;
            }
            String command = lineMatcher.group(1);
            String args = lineMatcher.group(3);
            if( DEBUG )
                out.println("cmd=["+command+"], args=["+args+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if( ! commandToAction.containsKey(command) ) {
                out.println(resources.getString(resources.getString("error.unknown-command", command))); //$NON-NLS-1$
                giveFeedback(UIFeedbackType.SYNTAX_ERROR);
                continue;
            }
            Action action = commandToAction.get(command);
            try
            {
                if(action.modifiesDatamodel())
                {
                    gedcom.startTransaction();
                }
                theIndi = action.doIt(theIndi, args);
            }
            catch( Exception re)
            {
                out.println(resources.getString("error.exception")+re);  //$NON-NLS-1$
                
                re.printStackTrace();
            }
            finally
            {
                if( action.modifiesDatamodel())
                {
                    gedcom.endTransaction();
                }
            }
        }
    }

    private Map<String, Action> expandActionMap(Map<List<String>, Action> actionMap) {
        Map<String,Action>  theMap = new HashMap<String, Action>();
        for(Map.Entry<List<String>,Action> entry  : actionMap.entrySet())
            for( String command : entry.getKey())
            {
                if( theMap.containsKey(command))
                {
                    throw new RuntimeException(resources.getString("error.configerr")+command);  //$NON-NLS-1$
                }
                theMap.put(command, entry.getValue());
            }
        return theMap;
    }

    /**
     * Translate a family to a string for output on the console.
     * @param fam the Family to be emitted
     * @return a string-representation.
     */
    private String dump(Fam fam)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(fam);
        PropertyDate mdate = fam.getMarriageDate();
        if( null != mdate)
            buf.append(" {"+mdate+"}");
        buf.append(LB);
        buf.append(resources.getString("dump.husband")+ fam.getHusband()+LB);  //$NON-NLS-1$
        buf.append(resources.getString("dump.wife")+fam.getWife()+LB);      //$NON-NLS-1$
        for( Indi child: fam.getChildren() )
        {
            buf.append("\t"); //$NON-NLS-1$
            buf.append(child.toString());
            buf.append(LB);
        }
        buf.append("\t"); //$NON-NLS-1$
        return buf.toString();
    }
    
    private String indent(String str )
    {
        StringBuffer buf = new StringBuffer(str.length());
        for( String line : str.split("\\r?\\n")) //$NON-NLS-1$
        {
            buf.append("  "); //$NON-NLS-1$
            buf.append(line);
            buf.append(LB);
        }
        return buf.toString();
    }

    protected String brief(final Indi theInd)
    {
        StringBuffer buf = new StringBuffer(theInd.toString());
        buf.append( "["); //$NON-NLS-1$
        buf.append( sexMap.get(theInd.getSex()));
        buf.append("]"); //$NON-NLS-1$
        buf.append(LB);
        buf.append("\t"); //$NON-NLS-1$
        buf.append(resources.getString("dump.born"));  //$NON-NLS-1$
        buf.append(theInd.getBirthAsString());
        buf.append(resources.getString("dump.died"));  //$NON-NLS-1$
        buf.append(theInd.getDeathAsString());
        buf.append("}"); //$NON-NLS-1$
        buf.append(LB);
        return buf.toString();
    }
    
    protected String dump(final Indi theInd)
    {
        StringBuffer buf = new StringBuffer(brief(theInd));
        Fam bioKidFamily = theInd.getFamilyWhereBiologicalChild();
        if( null != bioKidFamily)
        {
            buf.append(resources.getString("dump.kid-in-family")+LB);  //$NON-NLS-1$
            buf.append(indent(dump(bioKidFamily)));
        }
        buf.append(resources.getString("dump.marriages"));  //$NON-NLS-1$
        buf.append(LB);
        Fam[] spouseFamilies = theInd.getFamiliesWhereSpouse();
        for(Fam fam : spouseFamilies)
        {
            buf.append(indent(dump(fam)));
        }
        return buf.toString();
    }

    protected Indi createChild(final Indi parent, int marriageIndex, int sex) throws GedcomException
    {
        Fam[] families = parent.getFamiliesWhereSpouse();
        Fam theFamily;
        if( families.length > 1)
        {
            if( marriageIndex > families.length-1 || marriageIndex<0)
                throw new IllegalArgumentException(resources.getString("error.bad-marriage-index") +marriageIndex+ " "+parent   //$NON-NLS-1$//$NON-NLS-2$
                       +" is only a spouse in "+families.length+" families");  //$NON-NLS-1$ //$NON-NLS-2$
            theFamily = families[marriageIndex];
        }
        else if( families.length== 0)
        {
            createFamilyAndSpouse(parent);
            theFamily = parent.getFamiliesWhereSpouse()[0];
        }
        else
            theFamily = families[0];
        Indi child = (Indi)gedcom.createEntity(Gedcom.INDI);
        child.setSex(sex);
        theFamily.addChild(child);
        Indi father = child.getBiologicalFather();
        child.setName("",father.getLastName()); //$NON-NLS-1$
        return child;
    }
    
    protected Indi createFamilyAndSpouse(Indi ti) throws GedcomException
    {
        Fam theFamily =  (Fam) gedcom.createEntity(Gedcom.FAM);
        Indi spouse = (Indi)gedcom.createEntity(Gedcom.INDI);
        if(ti.getSex() == PropertySex.FEMALE)
        {
            theFamily.setWife(ti);
            spouse.setSex(PropertySex.MALE);
            theFamily.setHusband(spouse);
        }
        else
        {
            theFamily.setHusband(ti);
            spouse.setSex(PropertySex.FEMALE);
            theFamily.setWife(spouse);
        }
        return spouse;
    }
    
    protected final Fam getCreateBiologicalFamily(Indi ti ) throws GedcomException
    {
        Fam theFam =  ti.getFamilyWhereBiologicalChild();
        if( null == theFam)
        {
             createParent(ti,PropertySex.MALE);
            theFam =  ti.getFamilyWhereBiologicalChild();
        }
        return theFam;
    }
    
    protected Indi createBiologicalSibling(Indi ti, int sex) throws GedcomException {
        Fam theFam =  getCreateBiologicalFamily(ti);
        Indi child = (Indi)gedcom.createEntity(Gedcom.INDI);
        child.setSex(sex);
        theFam.addChild(child);
        Indi father = child.getBiologicalFather();
        child.setName("",father.getLastName()); //$NON-NLS-1$
        return child;       
    }
    
    
    /*
     * creates a pair of parents, and returns one of them.  
     * Also, link theChild in as a the sole child of the new FAM 
     * <B>NOTE</b> This won't be appropriate in 100% of cases, (just 95).
     */
    protected Indi createParent(Indi theChild, int sex) throws GedcomException
    {
        if( null != theChild.getFamilyWhereBiologicalChild())
            throw new IllegalArgumentException(resources.getString("error.cant-have-many-biofamilies"));  //$NON-NLS-1$
        Indi parent = (Indi)gedcom.createEntity(Gedcom.INDI);
        parent.setSex(sex);
        Indi newOtherParent = createFamilyAndSpouse(parent);
        if( PropertySex.MALE  == sex)
            parent.setName("",theChild.getLastName()); //$NON-NLS-1$
        else
            newOtherParent.setName("",theChild.getLastName()); //$NON-NLS-1$
        Fam newFamily = parent.getFamiliesWhereSpouse()[0];
        newFamily.addChild(theChild);
        return parent;
    }

    /**
     * 
     * @param date the date property to set
     * @param newValue the new string value
     * @return true if the value was set.
     */
    protected boolean setDate(PropertyDate date, String newValue)
    {
        String oldValue = date.getValue();
        date.setValue(newValue);
        if( date.isValid())
            return true;
        out.println(resources.getString("error.parse-date")); //$NON-NLS-1$
        giveFeedback(UIFeedbackType.SYNTAX_ERROR);
        date.setValue(oldValue);
        assert(date.isValid());
        return false;
    }
    
    private String getVersion()
    {
        return resources.getString("version.version") //$NON-NLS-1$
        + "$Revision: 1.26 $".replace("Revision:","").replace("$",""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$  
    }
    

    private  String getHelpText(Map<List<String>, Action> actionMap) {
        
        /*
        String[] help = {" COMMAND LIST :",
        "====Entity Creation ====",
        "cdau - create daughter ",
        "cson - create son ",
        "csis - create sister ",
        "cbro - create brother ",
        "cspo- create spouse",
        "cdad- create father",
        "cmom- create mother",

        "====Navigation=====",
        "gspo [n]- goto [nth] spouse",
        "gsib [n]- goto next[nth] sibling",
        "gchi [n]- goto first[nth] child",
        "gmom - goto mother",
        "gdad - goto father",
        
        "====Edits=====",
        "snam, n - set name",
        "anam - add name",
        "bday, b - set birthdate",
        "dday, d - set death date",
        
        "==OTHER==",
        "quit    -  exits the program",
        "save [filename] - saves",
        "help  - display this message",
        ""};
        */
        StringBuffer buf = new StringBuffer(1000);
        buf.append(resources.getString("help.available-commands"));  //$NON-NLS-1$
        buf.append(LB);
        for( List<String> actionKey: actionMap.keySet())
        {
            Action a = actionMap.get(actionKey);
            for(String cmdName : actionKey)
            {
                buf.append(cmdName);
                buf.append(" "); //$NON-NLS-1$
                switch (a.getArgUse())
                {
                case ARG_OPTIONAL:
                    buf.append('[');  //$NON-NLS-1$
                    buf.append(a.getArgName());
                    buf.append(']');  //$NON-NLS-1$
                    break;
                case ARG_YES:
                    buf.append(a.getArgName());
                    break;
                }
                buf.append(LB);
            }
            buf.append("-"); //$NON-NLS-1$
            buf.append(a.getDoc());
            buf.append(LB);
            buf.append(LB);
        }
        return buf.toString();
    }
    
    private void setFirstName(Indi indi, String firstName) {
        indi.setName(firstName,indi.getLastName());
    }

}


