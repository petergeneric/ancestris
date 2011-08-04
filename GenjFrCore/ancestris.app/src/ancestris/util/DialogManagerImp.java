/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.util;

import genj.util.swing.Action2;
import genj.util.swing.DialogHelper.DialogManager;
import javax.swing.Action;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
/**
 *
 * @author daniel
 */
public class DialogManagerImp implements DialogManager{

    private static DialogManager instance = null;

    public static DialogManager getInstance(){
        if (instance == null){
            instance = new DialogManagerImp();
        }
        return instance;
    }

    public int show(String title, int messageType, String txt, Action[] actions, Object source) {
        Object options[]=actions2options(actions);
        NotifyDescriptor d = new NotifyDescriptor(txt, title, NotifyDescriptor.DEFAULT_OPTION, messageType, options, null);
        return getResult(DialogDisplayer.getDefault().notify(d),options);
    }

    // Wrapper pour convertir les dialogues gnj en dialogue NB
    //TODO: ce qui n'est pas fait: la possibilite de desactiver des boutons par le caller
    // p.ex via action[0].setEnable(...)
    //
    // Aussi il faudra peut-etre prevoir le dimensionnement automatique de la boite de dialogue
    // en fonction de son contenu
    public int show(String title, int messageType, JComponent content, Action[] actions, Object source) {
        Object options[]=actions2options(actions);
        NotifyDescriptor d = new NotifyDescriptor(content, title, NotifyDescriptor.DEFAULT_OPTION, messageType, options, null);
        return getResult(DialogDisplayer.getDefault().notify(d),options);
    }

    public String show(String title, int messageType, String txt, String value, Object source) {
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(txt, title, NotifyDescriptor.OK_CANCEL_OPTION, messageType);
        d.setInputText(value);
        // analyze
        if (NotifyDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)){
            return d.getInputText();
        }
        return null;
    }

    private static int getResult(Object returnValue,Object[] options){
        // close w/o any button means cancel
        if (NotifyDescriptor.CLOSED_OPTION.equals(returnValue))
            returnValue = NotifyDescriptor.CANCEL_OPTION;

        for (int a=0; a<options.length; a++)
            if (returnValue==options[a])
                return a;

        // None found: tries with cancel
        returnValue = NotifyDescriptor.CANCEL_OPTION;
        for (int a=0; a<options.length; a++)
            if (returnValue==options[a])
                return a;
      return -1;
    }

    private static Object[] actions2options(Action[] actions){
        Object options[] = new Object[actions.length];
        for (int i = 0; i < actions.length; i++){
            if (actions[i] instanceof Action2){
                Action2 a = (Action2)(actions[i]);
                if (a.getText().equals(Action2.TXT_CANCEL)){
                    options[i] = NotifyDescriptor.CANCEL_OPTION;
                    continue;
                }
                if (a.getText().equals(Action2.TXT_OK)){
                    options[i] = NotifyDescriptor.OK_OPTION;
                    continue;
                }
                if (a.getText().equals(Action2.TXT_YES)){
                    options[i] = NotifyDescriptor.YES_OPTION;
                    continue;
                }
                if (a.getText().equals(Action2.TXT_NO)){
                    options[i] = NotifyDescriptor.NO_OPTION;
                    continue;
                }
                options[i] = a.getText();
                continue;
            }
            options[i] = actions[i];
        }
        return options;
    }
}
