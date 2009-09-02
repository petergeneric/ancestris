/**
 * This GenJ Plugin Source is Freeware Code
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
/** 
 * note - normally source files are not unicode so comments should not contain non ASCII characters
 * TODO; ajouter un bouton de remise a zero ;-)
 * TODO: ajouter la possibilite de l'activer ou de la desactiver, 
 * TODO: enlever les noms tracker pour la nommer "calculette" ou autre  ;-) 
 */
package genj.plugin.calculator;

import genj.app.ExtendMenubar;
import genj.plugin.ExtensionPoint;
import genj.plugin.Plugin;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import genj.view.ExtendContextMenu;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * A sample plugin that tracks all changes to all gedcom files and adds an update counter to each changed entity
 */
public class CalculatorPlugin implements Plugin {
  
  private final ImageIcon IMG = new ImageIcon(this, "/Calc.gif");
  
  private final Resources RESOURCES = Resources.get(this);
  
  /**
   * Our change to enrich an extension point
   * @see genj.plugin.Plugin#extend(genj.plugin.ExtensionPoint)
   */
  public void extend(ExtensionPoint ep) {
    
    
    if (ep instanceof ExtendContextMenu) {
      // show a context related tracker action
      ((ExtendContextMenu)ep).getContext().addAction(new Calc());
      }
    
    if (ep instanceof ExtendMenubar) {
      ExtendMenubar em = (ExtendMenubar)ep;
      em.addAction(ExtendMenubar.TOOLS_MENU, new Calc());
    }
    
  }

  /**
   * Our little about dialog action
   */
  private class Calc extends Action2 {
    Calc() {
      setText(RESOURCES.getString("action.calculator"));
      setImage(IMG);
    }
    protected void execute() {
//TODO:      String text = RESOURCES.getString("info.txt", RESOURCES.getString((active?"info.active":"info.inactive")));
    	if (!WindowManager.getInstance(getTarget()).show("cal"))
      WindowManager.getInstance(getTarget()).openWindow("cal", "Calculatrice", IMG, new Calculator());
      
    }
  } //About
    

    
} //TrackerPlugin


class CalculatorPanel extends JPanel implements ActionListener {
  public CalculatorPanel() {
    setLayout(new BorderLayout());

    display = new JTextField("0");
    display.setEditable(false);
    add(display, "North");

    JPanel p = new JPanel();
    p.setLayout(new GridLayout(5, 4));
    String buttons = "CE()789/456*123-0.=+";
    for (int i = 0; i < buttons.length(); i++)
      addButton(p, buttons.substring(i, i + 1));
    add(p, "Center");
  }

  private void addButton(Container c, String s) {
    JButton b = new JButton(s);
    c.add(b);
    b.addActionListener(this);
  }

  public void actionPerformed(ActionEvent evt) {
    String s = evt.getActionCommand();
    if ('0' <= s.charAt(0) && s.charAt(0) <= '9' || s.equals(".")) {
      if (start)
        display.setText(s);
      else
        display.setText(display.getText() + s);
      start = false;
    } else if (s.equals("E")) {
        display.setText("0");
        start = true;
    } else if (s.equals("C")) {
        op = "=";
        calculate(0);
    start = true;
    } else if (false && start && s.equals("-")) {
          display.setText(s);
          start = false;
      } else {
        calculate(Double.parseDouble(display.getText()));
        op = s;
        start = true;
      
    }
  }

  public void calculate(double n) {
    if (op.equals("+"))
      arg += n;
    else if (op.equals("-"))
      arg -= n;
    else if (op.equals("*"))
      arg *= n;
    else if (op.equals("/"))
      arg /= n;
    else if (op.equals("="))
      arg = n;
    display.setText("" + arg);
  }

  private JTextField display;

  private double arg = 0;

  private String op = "=";

  private boolean start = true;
}

class CalculatorFrame extends JFrame {
  public CalculatorFrame() {
    setTitle("Calculator");
    setSize(200, 200);

    Container contentPane = getContentPane();
    contentPane.add(new CalculatorPanel());
  }
}
