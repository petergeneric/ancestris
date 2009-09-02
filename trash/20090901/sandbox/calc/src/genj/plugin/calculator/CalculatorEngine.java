/* ***************************************************************************
 *  
 *       File:  CalculatorEngine.java
 *    Package:  ca.janeg.calc
 * 
 * References:  Object Oriented Programming and Java, 
 *              by Danny C.C. Poo and Derek B.K. Kiong, Springer, 1999 (p48-49)
 * 
 * 
 * Date         Author          Changes
 * ------------ -------------   ----------------------------------------------
 * Oct 17, 2002 Jane Griscti    Created
 * Oct 18, 2002 Jane Griscti    Added unary functions %, sqrt, reciprocal, etc
 * Oct 20, 2002 Jane Griscti    Added var display, number formatter and related
 *                              methods
 *                              Added integer binary operations: xor, or, and
 *                              leftShift, rightShift
 * Oct 21, 2002 Jane Griscti    Cleaned up comments
 * Oct 22, 2002 Jane Griscti    Added trig and log unary functions
 * *************************************************************************** */

package genj.plugin.calculator;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *  A class to perform standard calculator operations. 
 *  For example,
 * 
 *  <pre>
 *      CalculatorEngine c = new CalculatorEngine();
 *      c.digit( 1 );
 *      c.digit( 2 );
 *      c.add();
 *      c.digit( 1 );
 *      c.digit( 3 );
 *      c.equals();
 *      System.out.println( c.display() );
 *  </pre>
 * 
 *  Accuracy is limited to fifteen decimal places.
 * 
 *  @author     Jane Griscti    jane@janeg.ca
 *  @version    1.2             Oct 20, 2002 
 */
public class CalculatorEngine {

    private StringBuffer display    = new StringBuffer( 64 );
    private DecimalFormat df        = (DecimalFormat)NumberFormat.getInstance();
    private boolean newOp           = false;
    private boolean inDecimals      = false;
        
    private double  value;          // current digits
    private double  keep;           // previous value or operation result
    private int     toDo;           // binary operation waiting for 2nd value
    private int     decimalCount;   // number of decimal positions in current
                                    //   value

    /**
     *  Creates a new <code>CalculatorEngine</code> object.
     */
    public CalculatorEngine(){
        super();
        df.setMaximumFractionDigits( 15 );
    }

    /* -- Digits and the decimal point handler -- */
    
    /**
     *  Accept a digit or decimal as input.
     */
    public void digit(final int n ){
        
        /*
         *  Strategy:
         *      1. Start a new value if at the beginning of a new operation.
         * 
         *      2. Append the input character, setting the decimal flag if it's
         *         a decimal point or increasing the decimal count if we're
         *         already into decimals.
         * 
         *      3. Convert the revised input string to a double for use in
         *         calculations; forcing input errors to return a 0.0 value.
         */
        
        if( newOp ){
            display.delete( 0, display.length() );
            newOp = false;
        }
        
        char c = (char)n;
        
        if( c == '.' ){
            display.append( '.' );
            inDecimals = true;
        }else if( !inDecimals ){
            display.append( n );
        }else{
            if( decimalCount < 16 ){
                display.append( n );
                decimalCount++;
            }
        }
        
        try{
            value = Double.parseDouble( display.toString() );
        }catch( NumberFormatException e ){
            value = Double.parseDouble( "0.0" );
        }
    }
    
    /* -- Binary operations -- 
     * 
     *   A binary operation signals the engine to:
     *     1. store the current value
     *     2. set the 'toDo' flag with the requested operation
     *     3. accept input for a second value
     *     4. perform the 'toDo' op when '=' or another binary operation
     *        is requested
     */
    
    /**
     *  Add the next input value to the previous value
     */         
    public void add(){
        binaryOperation( "+" );
    }
    
    /**
     *  Subtract the next input value from the previous value
     */
    public void subtract(){
        binaryOperation( "-" );
    }
    
    /**
     *  Multiply the next input value by the previous value
     */
    public void multiply(){
        binaryOperation( "*" );
    }
    
    /**
     *  Divide the previous value by the next input value
     */
    public void divide(){
        binaryOperation( "/" );
    }

    /**
     *  Bitwise And ( & ) 
     */ 
    public void and(){
        binaryOperation( "&" );
    }
    
    /**
     *  Bitwise Or ( | ) 
     */
    public void or(){
        binaryOperation( "|" );     
    }
    
    /**
     *  Bitwise ( ^ )
     */
    public void xor(){
        binaryOperation( "^" );
    }
    
    /**
     *  Bitwise left shift ( < )
     */
    public void leftShift(){
        binaryOperation( "<" );
    }
    
    /**
     *  Bitwise right shift ( > )
     */
    public void rightShift(){
        binaryOperation( ">" );
    }
    
    /**
     *  Modulous ( % )
     */
    public void mod(){
        binaryOperation( "m" );
    }
    
    /**
     *  Raise the previous value to the 'power; of the next input value
     */
    public void pow(){
        binaryOperation( "p" );     
    }

    /**
     *  Perform any waiting binary operation and clear previous value
     */
    public void equals(){
        compute();
        toDo = 0;
        newOp = true;       
    }
    
    /*
     *  Setup registers for next input value
     */
    private void binaryOperation( final String op ){
        
        if( toDo == 0 ){
            keep = value;
        }else{
            compute();
        }
        
        value = 0;
        toDo = op.hashCode();
        resetDecimals();
        setDisplay();       
    }
    
    /*
     *  Perform a binary operation
     */     
    private void compute(){
        
        switch( toDo ){
            case '+':   value = keep + value;   break;
            case '-':   value = keep - value;   break;
            case '*':   value = keep * value;   break;
            case '/':
                if( value != 0 ){           // ignore divide by zero
                    value = keep / value;
                }
            case '&':   value = (int)keep & (int)value;     break;
            case '|':   value = (int)keep | (int)value;     break;
            case '^':   value = (int)keep ^ (int)value;     break;
            case '<':   value = (int)keep << (int)value;    break;
            case '>':   value = (int)keep >> (int)value;    break;
            case 'm':   value = keep % value;               break;
            case 'p':   value = Math.pow( keep, value );    break;                                  
        }       
                
        keep = value;
        setDisplay();       
    }
    
    /* -- Unary Operations -- */
    
    /**
     *  Compute the square of the current value
     */     
    public void sqrt(){
        value = Math.sqrt( value );
        unaryOperation();
    }
    
    /**
     *  Reverse the sign on the current value
     */
    public void sign(){
        value = value * -1;
        unaryOperation();
    }
    
    /**
     *  Convert the current value to a percent
     */
    public void percent(){
        value = value / 100;
        unaryOperation();
    }
    
    /**
     *  Convert the current value to it's reciprocal value
     */
    public void reciprocal(){
        if( value > 0 ){
            value = 1 / value;
        }else{
            value = 0;
        }
        unaryOperation();
    }       
    
    /**
     *  Compute the sine of the current value.
     */
    public void sin(){
        value = Math.sin( value );
        unaryOperation();
    }
    
    /**
     *  Compute the cosine of the current value
     */
    public void cos(){
        value = Math.cos( value );
        unaryOperation();
    }
    
    /**
     *  Compute the tan of the current value
     */
    public void tan(){
        value = Math.tan( value );
        unaryOperation();
    }
    
    /**
     *  Compute the asine of the current value
     */
    public void asin(){
        value = Math.asin( value );
        unaryOperation();
    }
    
    /**
     *  Compute the acosine of the current value
     */
    public void acos(){
        value = Math.acos( value );
        unaryOperation();
    }
    
    /**
     *  Compute the atan of the current value
     */
    public void atan(){
        value = Math.atan( value );
        unaryOperation();
    }

    /**
     *  Compute the log of the current value
     */
    public void log(){
        value = Math.log( value );
        unaryOperation();
    }

    /**
     *  Convert the current value to degrees
     */
    public void degrees(){
        value = Math.toDegrees( value );
        unaryOperation();
    }

    /**
     *  Convert the current value to radians
     */
    public void radians(){
        value = Math.toRadians( value );
        unaryOperation();
    }

    
    /*
     *  Setup flag to signal start of a new operation and
     *  set the display to match the value generated by a
     *  unary operation
     */
    private void unaryOperation(){
        newOp = true;
        setDisplay();
    }
        
    /* -- Control operations -- */  
    
    /**
     *  Delete the last entered digit
     */
    public void backspace(){
        if (display.length()>0)
        	display.deleteCharAt( display.length() - 1 );
        if (display.length()==0)
        	clearEntry();
        else {
        	value = Double.parseDouble( display.toString() );
        	setDisplay();
        }
    }
    
    /**
     *  Clear all values
     */     
    public void clear(){
        display.delete( 0, display.length() );
        value = 0;
        keep = 0;
        toDo = 0;
        resetDecimals();
    }
    
    /**
     *  Clear the current value
     */
    public void clearEntry(){
        display.delete( 0, display.length() );
        value = 0;
        resetDecimals();
    }
    
    /*
     *  Reset the decimal flag and counter
     */ 
    private void resetDecimals(){
        inDecimals = false;
        decimalCount = 0;
    }
    
    /**
     *  Convert the current value to a formatted string for
     *  display
     */                 
    private void setDisplay(){
        if( value == 0 ){
            display.delete( 0, display.length() );
        }else{
            display.replace( 0, display.length(), df.format( value ) );
        }
    }
    
    /**
     *  Returns the current value as a decimal formatted string
     */         
    public String display(){        
        return display.toString();
    }   

}   
