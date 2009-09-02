package com.sadinoff.genj.console;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.gnu.readline.Readline;
import org.gnu.readline.ReadlineLibrary;

public class ReadLineSource extends Readline implements LineSource {
    String prompt = ">";
    
    public ReadLineSource()
    {
        String appName = "GenJ-Console";
        // Readline.setThrowExceptionOnUnsupportedMethod(true);
        
        try {
            Readline.load(ReadlineLibrary.GnuReadline);
        }
        catch (UnsatisfiedLinkError ignore_me) {
            try
            {
                System.err.println("couldn't load GnuReadline lib.  Trying Pure-Java...");
                Readline.load(ReadlineLibrary.PureJava);
            }
            catch( UnsatisfiedLinkError ignore_me2)
            {
                System.err.println("couldn't load readline lib. Using simple stdin.");
            }
        }


        System.out.println("initializing Readline...");
        Readline.initReadline(appName); // init, set app name, read inputrc
        System.out.println("... done");
/*
        try {
          if (args.length > 0)
        Readline.readInitFile(args[0]);    // read private inputrc
        } catch (IOException e) {              // this deletes any initialization
          System.out.println(e.toString());    // from /etc/inputrc and ~/.inputrc
        System.exit(0);
        }
*/
        // read history file, if available

        File history = new File(System.getProperty("user.home"),".rltest_history");

        try {
            if (history.exists())
                Readline.readHistoryFile(history.getPath());
        } catch (Exception e) {
            System.err.println("Error reading history file!");
        }
          
        // define some additional function keys

        Readline.parseAndBind("\"\\e[18~\": \"Function key F7\"");
        Readline.parseAndBind("\"\\e[19~\": \"Function key F8\"");

        // Set word break characters
        try {
            Readline.setWordBreakCharacters(" \t;");
        }
        catch (UnsupportedEncodingException enc) {
            System.err.println("Could not set word break characters");
            System.exit(0);
        }
        System.out.println("encoding is "+getEncoding());
        // set test completer

//        Readline.setCompleter(new TestCompleter());

        // main input loop
                
    }
    
    public String readLine() throws IOException {
        return Readline.readline(prompt);
    }

    public void setPrompt(String prompt) {
       this.prompt =prompt; 
    }
 
}
