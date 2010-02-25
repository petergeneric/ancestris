/* ***************************************************************************
 *  
 *       File:  Calculator.java
 *    Package:  ca.janeg.calc
 * 
 *   Contains:  Inner classes
 *                  CalcButton
 *                      DigitButton
 *                      FunctionButton
 *                      UnaryButton
 *                      ControlButton
 * 
 * References:   Visual Components: Sum It Up with JCalculator
 *               by Claude Duguay, 
 *               Article at http://archive.devx.com
 *               (Layout)
 * 
 *               The Java Programming Language: 2nd Edition
 *               by Ken Arnold and James Gosling
 *               Addison-Wesley, 1998, 7th Printing 2000 (p311) 
 * 
 *               The Java Developers Almanac 1.4 (online) 
 *                  http://javaalmanac.com/egs/java.awt/screen_CenterScreen.html
 *                  http://www.javaalmanac.com/egs/javax.swing/LookFeelNative.html              
 * 
 * Date         Author          Changes
 * ------------ -------------   ----------------------------------------------
 * Oct 17, 2002 Jane Griscti    Created
 * Oct 22, 2002 Jane Griscti    Cleaned up comments, layouts and action listener
 * Oct 23, 2002 Jane Griscti    changed CalcButton to use a white foreground as
 *                              the default button color and removed redundant
 *                              calls from the subclasses
 *                              re-arranged the code in the class body to place
 *                              inner classes after all methods except main()
 * *************************************************************************** */


package genj.plugin.calculator;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import genj.edit.beans.DateBean;
import genj.util.swing.DateWidget;


/**
 *  A GUI interface for <code>CalculatorEngine</code>.
 * 
 *  @author     Jane Griscti    jane@janeg.ca
 *  @version    1.0             Oct 17, 2002 
 */
public class Calculator extends JPanel implements KeyListener{
	private final Class ENGINE; 
	private final CalculatorEngine engine = new CalculatorEngine();
	private final JTextField display = new JTextField();
	private char lastChar = 0;

	/**
	 *  Create a new calculator instance.
	 */ 
	public Calculator(){        

		display.setEditable( false );
		display.setBackground( Color.WHITE );   
		display.addKeyListener(this);
		// set up a Class object used in actionPerformed()
		// to invoke methods on the CalculatorEngine
		ENGINE = engine.getClass();

		buildGUI();     
	}
	// Execute the command corresponding to a keyboard key.
	boolean executeKeyCommand(char key) {
		switch(key) {
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			engine.digit(new Integer( key).intValue()-new Integer('0').intValue());
			break;
		case '.':
			engine.digit(new Integer( key).intValue());
			break;
		case '/':engine.divide();break;
		case '*':engine.multiply();break;
		case '-':engine.subtract();break;
		case '+':engine.add();break;
		case '\r':
		case '\n':
			engine.equals();break;
		case 0x08: engine.backspace();break;
		case 0x1b: 
			if (lastChar == 0x1b)
				engine.clear();
			else 
				engine.clearEntry();
			break;
		default:
			lastChar = key;
		return false;
		}
		display.setText(engine.display());
		lastChar = key;
		return true;
	}

	private void buildGUI(){

		//		Container cp = getRootPane();
		Container cp = this;
		cp.setLayout( new BoxLayout( cp, BoxLayout.Y_AXIS ) );

		cp.add( display );
		cp.add( buildControlPanel() );
		cp.add( buildDatePanel() );
		cp.add( buildButtonPanels() );                                              
	}
	public void keyTyped(KeyEvent e) {e.consume();}
	public void keyReleased(KeyEvent e) {e.consume();}
	public void keyPressed(KeyEvent e) {
		char key = e.getKeyChar();
		executeKeyCommand(key);
	}

	private JPanel buildControlPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		panel.add( Box.createHorizontalGlue() );
		panel.add( new ControlButton( "Backspace", "backspace" ) );
		panel.add( Box.createRigidArea( new Dimension( 2, 0 ) ) );

		JPanel panel2 = new JPanel( new GridLayout( 1, 1, 2, 2 ) );
		panel2.add( new ControlButton( "CE", "clearEntry" ) );
		panel2.add( new ControlButton( "C", "clear" ) );
		panel.add( panel2 );

		return panel;
	}   

	private JPanel buildDatePanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		panel.add( Box.createHorizontalGlue() );
		panel.add( new DateWidget() );
		panel.add( Box.createRigidArea( new Dimension( 2, 0 ) ) );

/*		JPanel panel2 = new JPanel( new GridLayout( 1, 1, 2, 2 ) );
		panel2.add( new ControlButton( "CE", "clearEntry" ) );
		panel2.add( new ControlButton( "C", "clear" ) );
		panel.add( panel2 );
*/
		return panel;
	}   

	private JPanel buildButtonPanels() {
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.setFont(new Font("Courier", 10, Font.BOLD));

//		TODO: on enleve les fonction math        buttons.add( buildUnaryPanel() );               
		buttons.add( buildDigitPanel() );
		buttons.add( buildFunctionSimplePanel() );                

		return buttons;
	}

	private JPanel buildDigitPanel(){
		JPanel panel = new JPanel();        
		panel.setLayout( new GridLayout( 4, 3, 2, 2 ) );                

		panel.add( new DigitButton( "7" ) );
		panel.add( new DigitButton( "8" ) );
		panel.add( new DigitButton( "9" ) );    

		panel.add( new DigitButton( "4" ) );
		panel.add( new DigitButton( "5" ) );
		panel.add( new DigitButton( "6" ) );    

		panel.add( new DigitButton( "1" ) );
		panel.add( new DigitButton( "2" ) );
		panel.add( new DigitButton( "3" ) );    

		panel.add( new DigitButton( "0" ) );
		panel.add( new DigitButton( "." ) );

		// not a digit but added here to balance out the panel      
		panel.add( new UnaryButton( " +/- ", "sign" ) );        

		return panel;       
	}


	private JPanel buildFunctionPanel(){
		JPanel buttons = new JPanel( new GridLayout( 4, 3, 2, 2 ) );

		buttons.add( new FunctionButton( "/", "divide" ) );             
		buttons.add( new FunctionButton( "&", "and" ) );        
		buttons.add( new FunctionButton( "<<", "leftShift" ) );

		buttons.add( new FunctionButton( "*", "multiply" ) );                       
		buttons.add( new FunctionButton( "|", "divide" ) );                     
		buttons.add( new FunctionButton( ">>", "rightShift" ) );

		buttons.add( new FunctionButton( "-", "subtract" ) );
		buttons.add( new FunctionButton( "^" , "xor" ) );
		buttons.add( new FunctionButton( "pow" ) );

		buttons.add( new FunctionButton( "+", "add" ) );
		buttons.add( new FunctionButton( "=", "equals" ) );
		buttons.add( new FunctionButton( "mod" ) );

		return buttons;             

	}
	private JPanel buildFunctionSimplePanel(){
		JPanel buttons = new JPanel( new GridLayout( 4, 2, 2, 2 ) );

		buttons.add( new FunctionButton( "/", "divide" ) );             
//		buttons.add( new CalcButton( "", "noop" ) );        
		buttons.add(new JLabel());
		
		buttons.add( new FunctionButton( "*", "multiply" ) );                       
		buttons.add( new UnaryButton( "1/x", "reciprocal" ) );

		buttons.add( new FunctionButton( "-", "subtract" ) );
		buttons.add( new UnaryButton( "%", "percent" ) );       

		buttons.add( new FunctionButton( "+", "add" ) );
		buttons.add( new FunctionButton( "=", "equals" ) );

		return buttons;             

	}
	private JPanel buildUnaryPanel(){
		JPanel buttons = new JPanel( new GridLayout( 4, 3, 2, 2 ) );

		buttons.add( new UnaryButton( "sin" ) );
		buttons.add( new UnaryButton( "cos" ) );
		buttons.add( new UnaryButton( "tan" ) );
		buttons.add( new UnaryButton( "asin" ) );

		buttons.add( new UnaryButton( "acos" ) );
		buttons.add( new UnaryButton( "atan" ) );
		buttons.add( new UnaryButton( "log" ) );
		buttons.add( new UnaryButton( "deg", "degrees" ) );

		buttons.add( new UnaryButton( "rad", "radians" ) );             
		buttons.add( new UnaryButton( "sqrt" ) );

		buttons.add( new UnaryButton( "%", "percent" ) );       
		buttons.add( new UnaryButton( "1/x", "reciprocal" ) );

		return buttons;     

	}

	/*
	 *  Helper class to handle button formatting.
	 *  Each button acts as its own listener.
	 */
	private class CalcButton extends JButton implements ActionListener{

		CalcButton( String s, String action ){
			super( s );
			setActionCommand( action );
			setMargin( new Insets( 2, 2, 2, 2 ) );
			//setForeground( Color.WHITE );
			addActionListener( this );
		}
		CalcButton( String s, String action, String key ){
			this(s,action);
			getInputMap().put(KeyStroke.getKeyStroke(key),action);
		}

		/*
		 *  Captures the button events and then uses 'reflection'
		 *  to invoke the right method in the calculator engine
		 * 
		 *  Digit buttons are handled slightly different as they
		 *  all use the digit( int ) method and their values must
		 *  be passed as arguments. 
		 * 
		 *  The digit button for the decimal has special handling; 
		 *  new Integer( "." ) throws a NumberFormatException, 
		 *  have to use new Integer( '.' ) which converts the ASCII
		 *  value of '.' to an integer.
		 * 
		 */ 

		public void actionPerformed(ActionEvent e) {

			String methodName = e.getActionCommand();

			Method method = null;
			lastChar = 0;

			try {
				if ( e.getSource() instanceof DigitButton ) {
					method =
						ENGINE.getMethod("digit", new Class[] { int.class });

					if (methodName.equals(".")) {
						method.invoke(engine, new Object[] { new Integer( '.' )});
					} else {
						method.invoke(engine, new Object[] { 
								new Integer( methodName )});
					}
				} else {
					method = ENGINE.getMethod(methodName, null);
					method.invoke(engine, null);
				}
			} catch (NoSuchMethodException ex) {
				System.out.println("No such method: " + methodName);
			} catch (IllegalAccessException ea) {
				System.out.println("Illegal access" + methodName);
			} catch (InvocationTargetException et) {
				System.out.println("Target exception: " + methodName);
			}

			display.setText(engine.display());
		}
	}

	private class DigitButton extends CalcButton {
		DigitButton( String s ){
			super( s, s ,s);
			setForeground( Color.BLUE );
		}
	}

	private class FunctionButton extends CalcButton {
		FunctionButton( String s ){
			this( s, s );           
		}

		FunctionButton( String s, String action ){          
			super( s, action );
			setBackground( Color.GRAY );
		}
	}

	private class ControlButton extends CalcButton{
		ControlButton( String s ){
			this( s, s );
		}

		ControlButton( String s, String action ){
			super( s, action );
			setBackground( Color.RED );
		}
	}

	private class UnaryButton extends CalcButton {
		UnaryButton( String s ){
			this( s, s );
		}

		UnaryButton( String s, String action ){
			super( s, action );
			setBackground( Color.BLUE );
		}
	}

}
